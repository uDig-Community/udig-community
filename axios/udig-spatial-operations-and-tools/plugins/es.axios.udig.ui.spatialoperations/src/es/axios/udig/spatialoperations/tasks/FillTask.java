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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.geotools.util.FeatureUtil;
import es.axios.geotools.util.GeoToolsUtils;
import es.axios.lib.geometry.util.GeometryUtil;

/**
 * <pre>
 * Fill operation.
 * 
 * -Get a feature lineString from the second layer.
 * -Convert its geometry into polygon geometry if it can.
 * -Get the features from the source layer that intersects with that geometry.
 * -Create a new feature that will be the difference between seconds layer polygon geometry 
 * 	and the features from the first layer that intersect.
 * -Insert that new feature into target store.
 * 
 * </pre>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
final class FillTask extends AbstractSpatialOperationTask<FeatureStore<SimpleFeatureType, SimpleFeature>> implements
			IFillTask {

	private SimpleFeatureCollection								featuresInFirstLayer	= null;
	private SimpleFeatureCollection								featuresInSecondLayer	= null;
	private CoordinateReferenceSystem							firstLayerCrs			= null;
	private CoordinateReferenceSystem							secondLayerCrs			= null;
	private CoordinateReferenceSystem							mapCrs					= null;
	private CoordinateReferenceSystem							targetCrs				= null;
	private boolean isCopySourceFeatures;

	/**
	 * To create an instance must use:
	 * {@link #createProcess(FeatureStore, FeatureCollection, FeatureCollection, CoordinateReferenceSystem, CoordinateReferenceSystem, boolean, CoordinateReferenceSystem, CoordinateReferenceSystem)}
	 */
	private FillTask() {

	}

	/**
	 * Parameters for fill operation.
	 * 
	 * @param targetStore
	 * @param featuresInFirstLayer
	 * @param featuresInSecondLayer
	 * @param firstLayerCrs
	 * @param mapCrs
	 * @param secondLayerCrs
	 * @param targetCrs
	 * @return
	 */
	public static IFillTask createProcess(	final SimpleFeatureStore targetStore,
											final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer,
											final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer,
											final CoordinateReferenceSystem firstLayerCrs,
											final CoordinateReferenceSystem mapCrs,
											final CoordinateReferenceSystem secondLayerCrs,
											final CoordinateReferenceSystem targetCrs,
											final boolean isCopySourceFeatures) {

		assert targetStore != null;
		assert featuresInFirstLayer != null;
		assert featuresInSecondLayer != null;
		assert firstLayerCrs != null;
		assert mapCrs != null;
		assert secondLayerCrs != null;
		assert targetCrs != null;

		FillTask task = new FillTask();
		task.targetStore = targetStore;
		task.featuresInFirstLayer = (SimpleFeatureCollection) featuresInFirstLayer;
		task.featuresInSecondLayer = (SimpleFeatureCollection) featuresInSecondLayer;
		task.firstLayerCrs = firstLayerCrs;
		task.secondLayerCrs = secondLayerCrs;
		task.mapCrs = mapCrs;
		task.targetCrs = targetCrs;
		task.isCopySourceFeatures = isCopySourceFeatures;

		return task;
	}

	@Override
	protected FeatureStore<SimpleFeatureType, SimpleFeature> getResult() {

		return this.targetStore;
	}

	@Override
	protected void perform() throws SpatialOperationException {

		FeatureIterator<SimpleFeature> iterLinesFeatures = null;

		try {
			iterLinesFeatures = featuresInSecondLayer.features();
			// pick each feature of second layer.

			while (iterLinesFeatures.hasNext()) {

				SimpleFeature feature = iterLinesFeatures.next();

				assert feature != null : "Feature can not be null"; //$NON-NLS-1$
				assert feature.getDefaultGeometry().getClass().isAssignableFrom(LineString.class)
							|| feature.getDefaultGeometry().getClass().isAssignableFrom(MultiLineString.class) : "Feature must be LineString or MultiLineString"; //$NON-NLS-1$

				Geometry fillGeom = null;
				fillGeom = GeometryUtil.convertLineStringIntoPolygonGeometry(feature);

				for (int i = 0; i < fillGeom.getNumGeometries(); i++) {

					Geometry eachFillGeom = fillGeom.getGeometryN(i);

					if (!isValid(eachFillGeom)) {
						// can't do fill operation with this line, so continue
						// and pick the next line.
						continue;
					}

					Geometry intersectGeometries = null;
					intersectGeometries = getIntersectGeometries(eachFillGeom);

					if (!isValid(intersectGeometries)) {
						// any geometry from the first layer intersects with
						// that fillGeom, so continue;
						continue;
					}
					List<Geometry> diffirenceGeomList = null;
					diffirenceGeomList = applyDifference(eachFillGeom, intersectGeometries);

					if (!diffirenceGeomList.isEmpty()) {
						// create and insert the new features.
						createFeatureAndInserts(diffirenceGeomList);
					}
				}
			}
			if( isCopySourceFeatures ){
				copyFeatures((SimpleFeatureCollection) this.featuresInFirstLayer, targetStore);
			}

		} catch (Exception e) {
			throw makeException(e, e.getMessage());
		} finally {
			if (iterLinesFeatures != null) {
				iterLinesFeatures.close();
			}
		}

	}

	/**
	 * Creates the feature or features, depends on the target geometry type, and
	 * insert those features into store.
	 * 
	 * @param diffirenceGeomList
	 * @throws TransformException
	 * @throws OperationNotFoundException
	 * @throws IllegalAttributeException
	 * @throws SpatialOperationException
	 */
	private void createFeatureAndInserts(List<Geometry> diffirenceGeomList)
		throws OperationNotFoundException, TransformException, IllegalAttributeException, SpatialOperationException {

		SimpleFeatureType targetType = targetStore.getSchema();

		// if there are more than one geometries and the target isn't
		// MultiGeometry. For each geometry, creates a new feature.
		if ((diffirenceGeomList.size() > 1 && !targetType.getGeometryDescriptor().getType().getBinding()
					.isAssignableFrom(MultiPolygon.class))
					|| !targetType.getGeometryDescriptor().getType().getBinding().isAssignableFrom(MultiPolygon.class)) {

			for (Geometry newGeom : diffirenceGeomList) {

				Geometry geomOnTargetCrs = GeoToolsUtils.reproject(newGeom, mapCrs, targetCrs);
				SimpleFeature newFeature = DataUtilities.template(targetStore.getSchema());

				newFeature.setDefaultGeometry(geomOnTargetCrs);

				insert(targetStore, newFeature);
			}
		} else {

			SimpleFeature newFeature = DataUtilities.template(targetStore.getSchema());

			GeometryFactory factory = new GeometryFactory();
			Geometry mpolygon = factory.createMultiPolygon(diffirenceGeomList.toArray(new Polygon[diffirenceGeomList
						.size()]));

			Geometry mpolygonOnTargetCrs = GeoToolsUtils.reproject(mpolygon, mapCrs, targetCrs);

			newFeature.setDefaultGeometry(mpolygonOnTargetCrs);

			insert(targetStore, newFeature);

		}
	}

	/**
	 * Applies the difference between geometries and return a List with the
	 * geometries created from the difference operation.
	 * 
	 * @param fillGeom
	 * @param intersectGeometries
	 * @return
	 * @throws TransformException
	 * @throws OperationNotFoundException
	 */
	private List<Geometry> applyDifference(Geometry fillGeom, Geometry intersectGeometries)
		throws OperationNotFoundException, TransformException {

		Geometry fillGeomOnMapCrs = GeoToolsUtils.reproject(fillGeom, secondLayerCrs, mapCrs);
		Geometry differenceGeometry = fillGeomOnMapCrs.difference(intersectGeometries);

		List<Geometry> diffirenceGeomList = new ArrayList<Geometry>();

		for (int i = 0; i < differenceGeometry.getNumGeometries(); i++) {
			diffirenceGeomList.add(differenceGeometry.getGeometryN(i));
		}

		return diffirenceGeomList;
	}

	/**
	 * Check is the geometry is valid or empty. If not is valid or is empty,
	 * return false.
	 * 
	 * @param geom
	 * @return
	 */
	private boolean isValid(Geometry geom) {

		if (geom == null || !geom.isValid() || geom.isEmpty()) {

			return false;
		}
		return true;
	}

	/**
	 * Go through features in first layer, and the features that intersect with
	 * fillGeom and to the Set. At the end, combine all the geometries of the
	 * Set into one geometry.
	 * 
	 * @param fillGeom
	 * @return
	 * @throws TransformException
	 * @throws OperationNotFoundException
	 */
	private Geometry getIntersectGeometries(Geometry fillGeom) throws OperationNotFoundException, TransformException {

		FeatureIterator<SimpleFeature> iterFirst = null;

		try {
			iterFirst = featuresInFirstLayer.features();

			// re-projects on mapCrs
			Geometry fillGeomOnMap = GeoToolsUtils.reproject(fillGeom, secondLayerCrs, mapCrs);

			Set<Geometry> intersectsGeometries = new HashSet<Geometry>();

			while (iterFirst.hasNext()) {

				SimpleFeature polygonFeature = iterFirst.next();

				// re-projects on mapCrs
				Geometry polyGeom = (Geometry) polygonFeature.getDefaultGeometry();
				Geometry polyGeomOnMap = GeoToolsUtils.reproject(polyGeom, firstLayerCrs, mapCrs);

				for (int i = 0; i < polyGeomOnMap.getNumGeometries(); i++) {

					Geometry referenceGeom = polyGeomOnMap.getGeometryN(i);
					// if intersects, add to the Set.
					if (fillGeomOnMap.intersects(referenceGeom)) {
						intersectsGeometries.add(referenceGeom);
					}
				}
			}

			// combined the intersects geometries and return it.
			Geometry combined = combineIntoOneGeometry(intersectsGeometries);

			return combined;
		} finally {
			if (iterFirst != null) {
				iterFirst.close();
			}
		}
	}
	
	private void copyFeatures(SimpleFeatureCollection featureCollection, SimpleFeatureStore targetStore) throws SpatialOperationException{
		
		FeatureIterator<SimpleFeature> iterFirst = null;

		try {
			// make the property list 
			
			iterFirst = featureCollection.features();
			while (iterFirst.hasNext()) {

				SimpleFeature sourceFeature = iterFirst.next();
				
				// create the new feature and set the geometry fragment
				SimpleFeature newFeature = DataUtilities.template(targetStore.getSchema());

				// copies the data in the new feature, if they have equals names and classes
				newFeature = FeatureUtil.copyAttributes( sourceFeature, newFeature);
				
				// re-projects on mapCrs
				Geometry geomOnTargetCRS = GeoToolsUtils.reproject(
						(Geometry) sourceFeature.getDefaultGeometry(), 
						featureCollection.getSchema().getCoordinateReferenceSystem(), 
						targetStore.getSchema().getCoordinateReferenceSystem());
				newFeature.setDefaultGeometry(geomOnTargetCRS);

				// insert the feature
				insert(targetStore, newFeature);
			}
		}catch(Exception ex){
			throw makeException(ex);
			
		} finally {
			if (iterFirst != null) {
				iterFirst.close();
			}
		}
		
		
		
	}

	/**
	 * Collect the provided geometries into a single geometry.
	 * 
	 * @param geometryCollection
	 * @return A single geometry which is the union of the provided
	 *         geometryCollection
	 */
	private Geometry combineIntoOneGeometry(Collection<Geometry> geometryCollection) {

		GeometryFactory factory = new GeometryFactory();

		Geometry combined = factory.buildGeometry(geometryCollection);
		return combined.union();
	}
}
