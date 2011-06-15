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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.geotools.util.FeatureTypeUnionBuilder;
import es.axios.geotools.util.GeoToolsUtils;
import es.axios.lib.geometry.util.GeometryUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;

/**
 * Makes an intersection on the target layer
 * <p>
 * This process creates new features that result from intersection of other two
 * layer.
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
final class IntersectTask extends AbstractSpatialOperationTask<FeatureStore<SimpleFeatureType, SimpleFeature>>
			implements IIntersectTask {

	private FeatureCollection<SimpleFeatureType, SimpleFeature>	featuresInFirstLayer;
	private FeatureCollection<SimpleFeatureType, SimpleFeature>	featuresInSecondLayer;
	private CoordinateReferenceSystem							firstLayerCrs;
	private CoordinateReferenceSystem							mapCrs;
	private boolean												isCreatingNewLayer;
	private FeatureTypeUnionBuilder								featureUnionBuilder;
	private CoordinateReferenceSystem							secondLayerCrs;

	/**
	 * To create an instance must use
	 * {@link #createProcess(FeatureStore, FeatureCollection, FeatureCollection, CoordinateReferenceSystem, CoordinateReferenceSystem)}
	 */
	private IntersectTask() {

	}

	/**
	 * 
	 * @param targetStore
	 * @param firstLayer
	 * @param secondLayer
	 * @param featuresInFirstLayer
	 * @param featuresInSecondLayer
	 * @param firstLayerCrs
	 * @param mapCrs
	 * @param isCreatingNewLayer
	 * @param secondLayerCrs
	 * @param featureUnionBuilder2
	 * @return an instance of {@link IntersectTask}
	 */
	public static IIntersectTask createProcess(	final SimpleFeatureStore targetStore,
												final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer,
												final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer,
												final CoordinateReferenceSystem firstLayerCrs,
												final CoordinateReferenceSystem mapCrs,
												final boolean isCreatingNewLayer,
												final FeatureTypeUnionBuilder featureUnionBuilder,
												final CoordinateReferenceSystem secondLayerCrs) {

		assert targetStore != null;
		assert featuresInFirstLayer != null;
		assert featuresInSecondLayer != null;
		assert firstLayerCrs != null;
		assert mapCrs != null;
		assert isCreatingNewLayer == true || isCreatingNewLayer == false;
		if (isCreatingNewLayer == true) {
			assert featureUnionBuilder != null;
		}
		assert secondLayerCrs != null;

		IntersectTask task = new IntersectTask();
		task.targetStore = targetStore;
		task.featuresInFirstLayer = featuresInFirstLayer;
		task.featuresInSecondLayer = featuresInSecondLayer;
		task.firstLayerCrs = firstLayerCrs;
		task.mapCrs = mapCrs;
		task.isCreatingNewLayer = isCreatingNewLayer;
		task.featureUnionBuilder = featureUnionBuilder;
		task.secondLayerCrs = secondLayerCrs;
		return task;
	}

	@Override
	protected FeatureStore<SimpleFeatureType, SimpleFeature> getResult() {

		return this.targetStore;
	}

	@Override
	protected void perform() throws SpatialOperationException {

		FeatureIterator<SimpleFeature> iter = null;
		try {
			iter = featuresInFirstLayer.features();
			while (iter.hasNext()) {

				SimpleFeature featureInFirstLayer = iter.next();

				createIntersectionFeaturesUsingGeomety(this.featuresInSecondLayer, featureInFirstLayer, firstLayerCrs,
							this.targetStore, mapCrs);
			}

		} catch (Exception e) {

			throw makeException(e, e.getMessage());
		} finally {

			if (iter != null) {
				featuresInFirstLayer.close(iter);
			}
		}
	}

	/**
	 * Creates new features using the intersection between the baseGeometry and
	 * the geometries of featuresInSecondLayer.
	 * 
	 * @param featuresInSecondLayer
	 *            features used to intersect
	 * @param baseGeometry
	 *            geometry used to intersect the features on second layer
	 * @param targetFeatureType
	 *            feature type used to create the new intersection features
	 * 
	 * @return list of new intersection features
	 * @throws SpatialOperationException
	 */
	private final void createIntersectionFeaturesUsingGeomety(	final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer,
																final SimpleFeature featureInFirstLayer,
																final CoordinateReferenceSystem baseGeomCrs,
																final FeatureStore<SimpleFeatureType, SimpleFeature> store,
																final CoordinateReferenceSystem mapCrs)
		throws SpatialOperationException {

		assert featuresInSecondLayer != null;
		assert featureInFirstLayer != null;
		assert store != null;

		final SimpleFeatureType featureType = this.targetStore.getSchema();
		final CoordinateReferenceSystem targetLayerCrs = featureType.getGeometryDescriptor()
					.getCoordinateReferenceSystem();

		FeatureIterator<SimpleFeature> iter = null;
		try {

			// project the base geometry to map
			Geometry baseGeometry = (Geometry) featureInFirstLayer.getDefaultGeometry();
			Geometry baseGeomOnMapCrs = GeoToolsUtils.reproject(baseGeometry, baseGeomCrs, mapCrs);
			// System.out.println(baseGeomOnMapCrs);
			// First does the graphic operations and creates a command
			// for each feature intersected finally executes all command.
			iter = featuresInSecondLayer.features();
			while (iter.hasNext()) {

				SimpleFeature featureInSecondLayer = iter.next();

				Geometry featureGeometry = (Geometry) featureInSecondLayer.getDefaultGeometry();

				Geometry featureGeomOnMapCrs = GeoToolsUtils.reproject(featureGeometry, secondLayerCrs, mapCrs);

				if (baseGeomOnMapCrs.intersects(featureGeomOnMapCrs)) {

					// makes the intersection on map crs
					final Geometry intersectionOnMapCrs = baseGeomOnMapCrs.intersection(featureGeomOnMapCrs);

					final Geometry intersectionGeometry = GeoToolsUtils.reproject(intersectionOnMapCrs, mapCrs,
								targetLayerCrs);

					if (GeometryCollection.class.equals(intersectionGeometry.getClass())
								&& containsDistinctGeoms(intersectionGeometry)) {

						// group lines with lines, polygons with polygons, and
						// points with points, make one geometry of each group
						// and insert it as a separate features.
						groupAndInsert(intersectionGeometry, featureInFirstLayer, featureInSecondLayer, store);
					} else {
						// adds the feature in the store associated to the layer
						SimpleFeature intersectFeature = createIntersectFeature(intersectionGeometry,
									featureInFirstLayer, featureInSecondLayer);

						insert(store, intersectFeature);
					}
				}

			}

		} catch (Exception e) {

			final String emsg = MessageFormat.format(Messages.IntersectProcess_intersection_fail, e.getMessage());
			makeException(e, emsg);
		} finally {
			if (iter != null) {
				featuresInSecondLayer.close(iter);
			}
		}
	}

	/**
	 * From the geometry collection, extract the geometries that match with the
	 * given classes.
	 * 
	 * @param collection
	 *            Geometry collection.
	 * @param clazz1
	 *            First given class. i.e: LineString.
	 * @param clazz2
	 *            Second given class. i.e: MultiLineString.
	 * @return
	 */
	private List<Geometry> extract(Geometry collection, Class<?> clazz1, Class<?> clazz2) {

		int numGeometries = collection.getNumGeometries();
		List<Geometry> geomList = new ArrayList<Geometry>();
		// go through the geometries finding the correspondent ones.
		for (int i = 0; i < numGeometries; i++) {

			Geometry geom = collection.getGeometryN(i);
			// check if it is the wanted geometry.
			if (clazz1.equals(geom.getClass()) || clazz2.equals(geom.getClass())) {
				geomList.add(geom);
			}
		}
		return geomList;
	}

	/**
	 * Groups similar geometries and then inserts that geometries as one
	 * feature. Example: On the geometry collection there are 2 polygons and 1
	 * line. It will create a new geometry using those 2 polygons and insert as
	 * one feature, then, the remaining line will insert as other feature.
	 * 
	 * @param intersectionGeometry
	 * @param featureInFirstLayer
	 * @param featureInSecondLayer
	 * @param store
	 * @throws SpatialOperationException
	 */
	private void groupAndInsert(Geometry intersectionGeometry,
								SimpleFeature featureInFirstLayer,
								SimpleFeature featureInSecondLayer,
								FeatureStore<SimpleFeatureType, SimpleFeature> store) throws SpatialOperationException {

		GeometryFactory gf = intersectionGeometry.getFactory();
		// Get polygons
		List<Geometry> polyList = extract(intersectionGeometry, Polygon.class, MultiPolygon.class);
		if (!polyList.isEmpty()) {
			Geometry polygonGeom = gf.buildGeometry(polyList);
			// adds the feature in the store associated to the layer
			SimpleFeature intersectFeature = createIntersectFeature(polygonGeom, featureInFirstLayer,
						featureInSecondLayer);

			insert(store, intersectFeature);
		}
		// Get lines
		List<Geometry> lineList = extract(intersectionGeometry, LineString.class, MultiLineString.class);
		if (!lineList.isEmpty()) {
			Geometry lineGeom = gf.buildGeometry(lineList);
			// adds the feature in the store associated to the layer
			SimpleFeature intersectFeature = createIntersectFeature(lineGeom, featureInFirstLayer, featureInSecondLayer);

			insert(store, intersectFeature);
		}
		// Get points
		List<Geometry> pointList = extract(intersectionGeometry, Point.class, MultiPoint.class);
		if (!pointList.isEmpty()) {
			Geometry pointGeom = gf.buildGeometry(pointList);
			// adds the feature in the store associated to the layer
			SimpleFeature intersectFeature = createIntersectFeature(pointGeom, featureInFirstLayer,
						featureInSecondLayer);

			insert(store, intersectFeature);
		}
	}

	/**
	 * Check if the geometries contained on the geometry collection are of the
	 * same type.
	 * 
	 * @param collection
	 *            The geometry collection.
	 * @return True if it contains different geometry classes.
	 */
	private boolean containsDistinctGeoms(Geometry collection) {

		Class<?> firstClazz = collection.getGeometryN(0).getClass();
		for (int i = 0; i < collection.getNumGeometries(); i++) {
			// compare the classes.
			Class<?> clazz = collection.getGeometryN(i).getClass();
			if (!firstClazz.equals(clazz)) {
				return true;
			}
		}
		// all the classes are the same as the first one, so return false.
		return false;
	}

	/**
	 * Makes a new feature using the fields present in both features and the
	 * intersection geometry.
	 * 
	 * @param intersectGeom
	 * @param firstFeature
	 * @param secondFeature
	 * 
	 * @return Feature
	 * 
	 * @throws SpatialOperationException
	 */
	private SimpleFeature createIntersectFeature(	final Geometry intersectGeom,
													final SimpleFeature firstFeature,
													final SimpleFeature secondFeature) throws SpatialOperationException {

		try {
			SimpleFeature newFeature;
			if (isCreatingNewLayer) {
				// Makes the Union Feature with the values present in first and
				// second feature
				SimpleFeatureType targetType = this.featureUnionBuilder.getFeatureType();
				GeometryDescriptor geoAttr = targetType.getGeometryDescriptor();
				Class<? extends Geometry> geomClass = (Class<? extends Geometry>) geoAttr.getType().getBinding();
				Geometry adjustedGeom = GeometryUtil.adapt(intersectGeom, geomClass);

				this.featureUnionBuilder.add(firstFeature).add(secondFeature).setGeometry(adjustedGeom);

				newFeature = this.featureUnionBuilder.getFeature();
			} else {
				// Makes the new feature and assigns the intersect geometry
				SimpleFeatureType targetType = this.targetStore.getSchema();
				newFeature = DataUtilities.template(targetType);

				// assigns the intersect geometry

				final Geometry adjustedGeom = adjustGeometryAttribute(intersectGeom, newFeature);
				newFeature.setDefaultGeometry(adjustedGeom);

			}
			return newFeature;

		} catch (Exception e) {

			throw makeException(e);
		}
	}

	/**
	 * Returns the multi geometry required by the feature
	 * 
	 * @param geometry
	 *            must be simple geometry (point, polygon, linestring)
	 * @param feature
	 * @return adjusted geometry
	 */
	private Geometry adjustGeometryAttribute(final Geometry geometry, final SimpleFeature feature) {

		GeometryDescriptor geoAttr = feature.getFeatureType().getGeometryDescriptor();
		Class<? extends Geometry> geomClass = (Class<? extends Geometry>) geoAttr.getType().getBinding();
		Geometry adjustedGeom = GeometryUtil.adapt(geometry, geomClass);

		return adjustedGeom;
	}

}
