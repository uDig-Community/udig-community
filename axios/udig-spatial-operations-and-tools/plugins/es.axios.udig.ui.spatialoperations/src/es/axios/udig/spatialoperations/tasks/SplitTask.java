/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.tasks;

import java.util.LinkedList;
import java.util.List;

import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import es.axios.geotools.util.split.CannotSplitException;
import es.axios.geotools.util.split.SplitFeatureBuilder;

/**
 * Task for doing the split operation.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
final class SplitTask extends AbstractSpatialOperationTask<FeatureStore<SimpleFeatureType, SimpleFeature>> implements
			ISplitTask {

	private FeatureCollection<SimpleFeatureType, SimpleFeature>	featuresInFirstLayer	= null;

	private FeatureCollection<SimpleFeatureType, SimpleFeature>	splitLineLayer	= null;
	private CoordinateReferenceSystem							mapCrs					= null;

	private boolean												isCreatingNewLayer;

	/** maintains the feature processed ID */

	private String												layerToSplitName;
	private String												targetLayerName;

	/**
	 * To create an instance must use:
	 * {@link #createProccess(FeatureStore, FeatureCollection, FeatureCollection, CoordinateReferenceSystem, CoordinateReferenceSystem, boolean, CoordinateReferenceSystem, CoordinateReferenceSystem, FeatureType, String, String)}
	 */
	private SplitTask() {

	}

	/**
	 * Creates the split process implementation.
	 * 
	 * @param targetStore
	 * @param featuresInFirstLayer
	 * @param featuresInSecondLayer
	 * @param firstLayerCrs
	 * @param mapCrs
	 * @param isCreatingNewLayer
	 * @param secondLayerCrs
	 * @param targetCrs
	 * @param polygonFeatureType
	 * @param layerToSplitName
	 * @param targetLayerName
	 * @return
	 */
	public static ISplitTask createProccess(final SimpleFeatureStore targetStore,
											final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer,
											final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer,
											final CoordinateReferenceSystem mapCrs,
											final boolean isCreatingNewLayer,
											final String layerToSplitName,
											final String targetLayerName) {

		assert targetStore != null;
		assert featuresInFirstLayer != null;
		assert featuresInSecondLayer != null;
		assert mapCrs != null;
		assert isCreatingNewLayer == true || isCreatingNewLayer == false;
		assert layerToSplitName != null;
		assert targetLayerName != null;

		SplitTask task = new SplitTask();
		task.targetStore = targetStore;
		task.featuresInFirstLayer = featuresInFirstLayer;
		task.splitLineLayer = featuresInSecondLayer;
		task.mapCrs = mapCrs;
		task.isCreatingNewLayer = isCreatingNewLayer;
		task.layerToSplitName = layerToSplitName;
		task.targetLayerName = targetLayerName;

		return task;
	}

	@Override
	protected FeatureStore<SimpleFeatureType, SimpleFeature> getResult() {

		return this.targetStore;
	}

	protected void perform() throws SpatialOperationException {

		// iterators of the first and second layers.

		try {
			// if a new result layer is required or was selected a
			// target different of layer to split (source)
			// adds the feature without changes in target store before
			// split them.
			if (isCreatingNewLayer || (!this.layerToSplitName.equals(this.targetLayerName))) {

				// adds features from source layer to the target store.
				FeatureIterator<SimpleFeature> iterSource = null;
				try{
					iterSource = featuresInFirstLayer.features();
					while (iterSource.hasNext()) {

						SimpleFeature sourceFeature = iterSource.next();

						sourceFeature = createFeatureInStore(this.targetStore,
								sourceFeature, getTargetGeometry(),
								isCreatingNewLayer);
					}
				} finally{
					if (iterSource != null) {
						iterSource.close();
					}
				}
			}
			//postcondition: the target layer have all source feature without modifications

			// Splits the initialized collection, in the previous step, using the lineString present in the second layer
			FeatureIterator <SimpleFeature> iterSplitLineLayer = null;
			try{
				iterSplitLineLayer = this.splitLineLayer.features();
				
				while (iterSplitLineLayer.hasNext()) {

					SimpleFeature splitLine = iterSplitLineLayer.next();
					splitFeatureUsingSplitLine((SimpleFeatureStore) this.targetStore,  splitLine, mapCrs);
				}
			} finally {
				if (iterSplitLineLayer != null) {
					iterSplitLineLayer.close();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			makeException(e);
		} 
	}

	/**
	 * Using the splitLine feature, go through the target feature store, and
	 * apply the split operation for each feature, if that correspond.
	 * 
	 * @param store the store to split
	 * @param splitLine
	 *            the feature used to split the feature collection
	 * @param mapCrs
	 *            map's crs
	 * 
	 * @throws SpatialOperationException
	 */
	private final void splitFeatureUsingSplitLine( 
			final SimpleFeatureStore store, 
			final SimpleFeature splitLine,
			final CoordinateReferenceSystem mapCrs)
		throws SpatialOperationException {

		FeatureCollection<SimpleFeatureType, SimpleFeature> targetFeatreCollection = null;
		FeatureIterator<SimpleFeature> targetIterator = null;
		try {
			Geometry splitLineGeometry = (Geometry) splitLine.getDefaultGeometry();
			
			// the number of geometries the line is made of.
			final int numLineGeometries = splitLineGeometry.getNumGeometries();

			for (int i = 0; (i < numLineGeometries); i++) {

				LineString currentSplitLine = (LineString) splitLineGeometry.getGeometryN(i);
				
				currentSplitLine.setUserData(splitLine.getFeatureType().getCoordinateReferenceSystem());


				// for each feature that intersect with the split line applies the split operation.

				// retrieves the intersected features with the split line
				String geometryName = store.getSchema().getGeometryDescriptor().getName().toString();
		    	
		    	FilterFactory2 ff = (FilterFactory2)FILTER_FACTORY;
				Intersects intersect = ff.intersects(ff.property(geometryName),  ff.literal( currentSplitLine.toText()) );
		    	
				List<SimpleFeature> intersectedFeatures = new LinkedList<SimpleFeature>();
				targetFeatreCollection = store.getFeatures(intersect);
				targetIterator = targetFeatreCollection.features();
				while (targetIterator.hasNext()) {

					intersectedFeatures.add( targetIterator.next() );
				}
				targetIterator.close();

				// executes the split operation in the intersected features
				SplitFeatureBuilder builder = SplitFeatureBuilder.newInstance(intersectedFeatures, currentSplitLine, mapCrs);
			    try{
			    	builder.buildSplit();
			    	builder.buildNeighbours();
			    	
					// modify the target store
					SplitTransaction transaction = SplitTransaction.newInstance(
							store, builder.getSplitResult(), builder.getFeaturesThatSufferedSplit(), builder.getNeighbourResult());
					transaction.execute();

			    } catch (CannotSplitException cse){
			    	// continue processing the next feature
			    }
				
			}
		} catch (Exception e) {
			throw makeException(e, e.getMessage());
		} finally {
			if (targetIterator != null) {
				targetIterator.close();
			}
		}
	}

	/**
	 * Get the geometry class of the target store.
	 * 
	 * @return
	 */
	private Class<? extends Geometry> getTargetGeometry() {

		Class<? extends Geometry> result = (Class<? extends Geometry>) targetStore.getSchema().getGeometryDescriptor()
					.getType().getBinding();

		return result;
	}

	/**
	 * Split the feature using the splitLine geometry, and adds the change.
	 * 
	 * @param featureToSplit
	 *            this feature could be the original or a partially processed
	 *            feature (processed in previous step)
	 * @param featureToSplitCrs
	 * @param splitGeometry
	 *            used to split the featureToSplit
	 * @param splitLineCrs
	 * @param mapCrs
	 * 
	 * @throws SpatialOperationException
	 */
// FIXME should be deleted	
//	private void splitFeatureUsing(	final SimpleFeature featureToSplit,
//									final CoordinateReferenceSystem featureToSplitCrs,
//									final Geometry splitGeometry,
//									final CoordinateReferenceSystem splitLineCrs,
//									final CoordinateReferenceSystem mapCrs) throws SpatialOperationException {
//
//		try {
//
//			Geometry sourceGeometryToSplit = (Geometry) featureToSplit.getDefaultGeometry();
//			String fidToSplit = featureToSplit.getID();
//
//			Geometry splitGeometryOnMap = GeoToolsUtils.reproject(splitGeometry, splitLineCrs, mapCrs);
//
//			// Iterates in the feature's geometries to split them.
//			final int numGeomToSplit = sourceGeometryToSplit.getNumGeometries();
//
//			for (int i = 0; (i < numGeomToSplit); i++) {
//
//				// get the correspondent geometry from the source feature
//				Geometry geom = sourceGeometryToSplit.getGeometryN(i);
//
//				// source geometry on mapCrs
//				Geometry currentSourceGeomOnMap = GeoToolsUtils.reproject(geom, featureToSplitCrs, mapCrs);
//
//				LineString splitLine = (LineString) splitGeometryOnMap;
//
//				SplitStrategy splitOp = new SplitStrategy(splitLine);
//
//				if (splitOp.canSplit(currentSourceGeomOnMap)) {
//
//					List<Geometry> splitted = null;
//
//					try {
//						splitted = splitOp.split(currentSourceGeomOnMap);
//					} catch (IllegalStateException e) {
//						continue;
//					}
//
//					for (Geometry splitResult : splitted) {
//
//						// the split geometry
//						splitResult = GeoToolsUtils.reproject(splitResult, mapCrs, targetCrs);
//
//						transactionSplit(fidToSplit, i, splitResult);
//
//					}
//
//				}
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw makeException(e, Messages.SplitProcess_failed_executing_reproject);
//		}
//
//	}
}
