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

import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

import es.axios.geotools.util.GeoToolsUtils;
import es.axios.lib.geometry.util.GeometryUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;

/**
 * *
 * 
 * <pre>
 * Hole operation.
 * 
 * -Get a feature lineString from the second layer.
 * -Convert its geometry into polygon geometry if it can.
 * -Get the features from the source that are withIn the converted feature(usingFeature).
 * -Make a hole on the features from source layer.
 * -Insert that new feature into target store.
 * 
 * </pre>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
final class HoleTask extends AbstractCommonTask implements IHoleTask {

	/**
	 * To create an instance must use:
	 * {@link #createProcess(FeatureCollection, FeatureCollection, FeatureStore, CoordinateReferenceSystem, CoordinateReferenceSystem, CoordinateReferenceSystem, CoordinateReferenceSystem, boolean, String, String)}
	 * .
	 */
	private HoleTask() {

	}

	/**
	 * 
	 * @param sourceFeatures
	 * @param usingFeatures
	 * @param targetStore
	 * @param mapCrs
	 * @param targetCrs
	 * @param sourceCrs
	 * @param usingCrs
	 * @param isCreatingNewLayer
	 * @param sourceName
	 * @param targetLayerName
	 * @return
	 */
	public static IHoleTask createProcess(	final FeatureCollection<SimpleFeatureType, SimpleFeature> sourceFeatures,
											final FeatureCollection<SimpleFeatureType, SimpleFeature> usingFeatures,
											final SimpleFeatureStore targetStore,
											final CoordinateReferenceSystem mapCrs,
											final CoordinateReferenceSystem targetCrs,
											final CoordinateReferenceSystem sourceCrs,
											final CoordinateReferenceSystem usingCrs,
											final boolean isCreatingNewLayer,
											final String sourceName,
											final String targetLayerName) {

		assert usingFeatures != null;
		assert sourceFeatures != null;
		assert targetStore != null;
		assert mapCrs != null;
		assert usingCrs != null;
		assert sourceCrs != null;
		assert isCreatingNewLayer == true || isCreatingNewLayer == false;
		assert sourceName != null;
		assert targetLayerName != null;
		assert targetCrs != null;

		HoleTask task = new HoleTask();

		task.usingFeatures = usingFeatures;
		task.sourceFeatures = sourceFeatures;
		task.targetStore = targetStore;
		task.mapCrs = mapCrs;
		task.usingCrs = usingCrs;
		task.sourceCrs = sourceCrs;
		task.isCreatingNewLayer = isCreatingNewLayer;
		task.sourceName = sourceName;
		task.targetLayerName = targetLayerName;
		task.targetCrs = targetCrs;

		return task;
	}
	@Override
	protected void perform() throws SpatialOperationException {

		FeatureIterator<SimpleFeature> iterSource = null;
		FeatureIterator<SimpleFeature> iterUsing = null;
		try {
			// adds each feature to hole in the target layer
			iterSource = this.sourceFeatures.features();
			while (iterSource.hasNext()) {

				// if a new result layer is required or was selected a target
				// different of source layer,
				// adds the feature without changes in target store before hole
				// it.
				SimpleFeature featureToHole = iterSource.next();

				if (isCreatingNewLayer || (!this.sourceName.equals(this.targetLayerName))) {

					featureToHole = createFeatureInStore( 
								this.targetStore, featureToHole,
								getSourceLayerGeometry(), isCreatingNewLayer);
				}
				// creates the initial collection of features to hole, this
				// collection
				// could change if a hole feature is done (original feature
				// replaced by its fragments).

				this.featuresInProcessing = new LinkedList<String>();
				this.featuresInProcessing.add(featureToHole.getID());
				int i = 0;
				while (i < this.featuresInProcessing.size()) {

					String fidToHole = this.featuresInProcessing.get(i);

					iterUsing = this.usingFeatures.features();
					boolean numGeomToHoleModified = false;
					while (iterUsing.hasNext() && !numGeomToHoleModified) {

						SimpleFeature usingFeature = iterUsing.next();

						numGeomToHoleModified = cutSourceWithUsingFeature(fidToHole, sourceCrs, usingFeature, usingCrs, mapCrs);
					}
					i = (numGeomToHoleModified) ? 0 : i + 1;
				}
			}

		} catch (Exception e) {

			final String failedMsg = Messages.AbstractCommonTask_procces_failed;
			makeException(e, failedMsg);
			
		} finally {

			if (iterUsing != null) {
				iterUsing.close();
			}
			if (iterSource != null) {
				iterSource.close();
			}
		}
	}

	/**
	 * Hole cuts the feature using the geometries of the usingFeature. If the
	 * geometry collection of feature to hole was modified by the hole process
	 * this method will return true.
	 * 
	 * @param fidToHole
	 *            the feature to hole
	 * @param featureToHoleCrs
	 *            crs of feature to hole
	 * @param usingFeature
	 *            the feature used to hole cut the feature collection
	 * @param usingCrs
	 *            crs of using feature
	 * @param mapCrs
	 *            map's crs
	 * 
	 * @return true if the count of geometry to hole was modified, false in
	 *         other case.
	 * @throws SpatialOperationException
	 */
	protected boolean cutSourceWithUsingFeature(final String fidToHole,
												final CoordinateReferenceSystem featureToHoleCrs,
												final SimpleFeature usingFeature,
												final CoordinateReferenceSystem usingCrs,
												final CoordinateReferenceSystem mapCrs)
		throws SpatialOperationException {

		try {

			// Convert the feature lineString into polygon and return its
			// geometry.
			Geometry usingFeatureGeometry = GeometryUtil.convertLineStringIntoPolygonGeometry(usingFeature);

			boolean geomToHoleModified = false;
			final int numUsingGeometries = usingFeatureGeometry.getNumGeometries();

			for (int i = 0; (i < numUsingGeometries) && !geomToHoleModified; i++) {

				Geometry usingGeometry = usingFeatureGeometry.getGeometryN(i);

				SimpleFeature featureToHole = findFeature(this.targetStore, fidToHole);
				geomToHoleModified = holeFeatureUsing(featureToHole, featureToHoleCrs, usingGeometry, usingCrs, mapCrs);
			}
			return geomToHoleModified;

		} catch (Exception e) {

			final String emsg = e.getMessage();
			LOGGER.severe(emsg);
			throw makeException(e, emsg);
		}
	}

	/**
	 * Hole cut the feature using the usingGeometry, and adds the change.
	 * 
	 * @param featureToHole
	 *            this feature could be the original or a partially processed
	 *            feature (processed in previous step)
	 * @param featureToHoleCrs
	 * @param usingGeometry
	 *            used to cut the featureToHole
	 * @param usingCrs
	 * 
	 * @param mapCrs
	 * 
	 * @return true if the count of geometry to hole was modified
	 * 
	 * @throws SpatialOperationException
	 */
	private boolean holeFeatureUsing(	final SimpleFeature featureToHole,
										final CoordinateReferenceSystem featureToHoleCrs,
										final Geometry usingGeometry,
										final CoordinateReferenceSystem usingCrs,
										final CoordinateReferenceSystem mapCrs) throws SpatialOperationException {

		try {
			Geometry featureGeometryToHole = (Geometry) featureToHole.getDefaultGeometry();
			String fidToHole = featureToHole.getID();

			Geometry usingGeometryOnMap = GeoToolsUtils.reproject(usingGeometry, usingCrs, mapCrs);

			// Iterates in the feature's geometries to hole then applies cut
			// operation using the usingGeometry.
			// The process is reinitialized if the geometry collection is
			// modified
			boolean geomCollectionModified = false;
			final int numGeomToHole = featureGeometryToHole.getNumGeometries();

			for (int i = 0; (i < numGeomToHole) && !geomCollectionModified; i++) {

				Geometry geom = featureGeometryToHole.getGeometryN(i);

				Geometry currentGeomOnMap = GeoToolsUtils.reproject(geom, featureToHoleCrs, mapCrs);

				IntersectionMatrix geomRelation = usingGeometryOnMap.relate(currentGeomOnMap);

				if (geomRelation.isWithin()) {

					Geometry diffGeomOnMap = computeGeometryDifference(currentGeomOnMap, usingGeometryOnMap);

					transactionDifference(fidToHole, i, diffGeomOnMap);
				}
			}
			return geomCollectionModified;
			
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			throw makeException(e, Messages.HoleTask_failed_doing_a_hole);
		}
	}
}
