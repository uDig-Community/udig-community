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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Id;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

import es.axios.geotools.util.GeoToolsUtils;
import es.axios.lib.geometry.util.GeometryUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;

/**
 * <pre>
 * This class will have methods and logic that is common for ClipTask and HoleTask.
 * </pre>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
abstract class AbstractCommonTask extends AbstractSpatialOperationTask<FeatureStore<SimpleFeatureType, SimpleFeature>> {

	protected static final Logger									LOGGER					= Logger
																										.getLogger(AbstractCommonTask.class
																													.getName());
	protected FeatureCollection<SimpleFeatureType, SimpleFeature>	sourceFeatures;
	protected FeatureCollection<SimpleFeatureType, SimpleFeature>	usingFeatures;
	protected CoordinateReferenceSystem								mapCrs;
	protected CoordinateReferenceSystem								usingCrs;
	protected CoordinateReferenceSystem								sourceCrs;
	protected boolean												isCreatingNewLayer;

	/** maintains the feature processed ID */
	protected List<String>											featuresInProcessing	= null;
	protected String												sourceName				= null;
	protected String												targetLayerName			= null;
	protected CoordinateReferenceSystem								targetCrs				= null;

	@Override
	protected FeatureStore<SimpleFeatureType, SimpleFeature> getResult() {

		return this.targetStore;
	}

	protected boolean cutSourceWithUsingFeature(final String fidToHole,
												final CoordinateReferenceSystem featureToHoleCrs,
												final SimpleFeature usingFeature,
												final CoordinateReferenceSystem usingCrs,
												final CoordinateReferenceSystem mapCrs)
		throws SpatialOperationException {

		// implemented on each subclass.
		return false;
	}

	/**
	 * Does the geometry difference on the current geometry of feature to hole.
	 * 
	 * @param fidToHole
	 * @param currentGeometry
	 *            the geometry to process
	 * @param diffGeomOnMap
	 * 
	 * @throws SpatialOperationException
	 */
	protected void transactionDifference(final String fidToHole, final int currentGeometry, final Geometry diffGeomOnMap)
		throws SpatialOperationException {

		// retrieve the feature partially modified and update its geometry
		SimpleFeature featureToUpdate = findFeature(this.targetStore, fidToHole);

		Geometry resultGeom = (Geometry) featureToUpdate.getDefaultGeometry();

		Geometry result = updateGeometry(resultGeom, currentGeometry, diffGeomOnMap);
		Geometry geomProjected = projectOnTargetLayer(result);

		modifyFeatureGeometryInStore(fidToHole, geomProjected, this.targetStore);
	}

	/**
	 * Replaces old geometry in the indeed position by the new geometry. The
	 * method will create a new geometry adapted to the target geometry.
	 * 
	 * @param originalGeometry
	 * @param oldGeomPosition
	 * @param newGeometry
	 *            simple or collection of geometries (fragments)
	 * @return a new geometry
	 */
	protected Geometry updateGeometry(	final Geometry originalGeometry,
										final int oldGeomPosition,
										final Geometry newGeometry) {

		int numGeoms = originalGeometry.getNumGeometries();

		ArrayList<Geometry> newGeomArray = new ArrayList<Geometry>(numGeoms);
		for (int i = 0; i < numGeoms; i++) {

			Geometry currentGeom = originalGeometry.getGeometryN(i);
			if (i == oldGeomPosition) {
				// adds the new geometries
				for (int j = 0; j < newGeometry.getNumGeometries(); j++) {

					newGeomArray.add(newGeometry.getGeometryN(j));
				}
			} else {
				// not modified
				newGeomArray.add(currentGeom);
			}
		}
		Geometry result = makeCompatibleGeometry(newGeomArray, newGeometry.getFactory(), originalGeometry.getClass());

		return result;
	}

	/**
	 * Modifies the geometry of feature doing the difference with the using
	 * geometry. precondition: this method supposes that "using" does not
	 * contain featrue's geometry but intersects it.
	 * 
	 * @param simpleGeometryOnMap
	 * @param usingGeometryOnMap
	 * @return the difference
	 * @throws SpatialOperationException
	 */
	protected Geometry computeGeometryDifference(final Geometry simpleGeometryOnMap, final Geometry usingGeometryOnMap)
		throws SpatialOperationException {

		Geometry diffGeometry = simpleGeometryOnMap.difference(usingGeometryOnMap);
		return diffGeometry;
	}

	/**
	 * 
	 * @param fidToUpdate
	 * @param newGeometry
	 * @param store
	 * @throws SpatialOperationException
	 */
	protected void modifyFeatureGeometryInStore(final String fidToUpdate,
												final Geometry newGeometry,
												final FeatureStore<SimpleFeatureType, SimpleFeature> store)
		throws SpatialOperationException {
		Transaction transaction = store.getTransaction();
		try {
			// adapts the geometry
			Class<? extends GeometryCollection> expectedClass = (Class<? extends GeometryCollection>) getSourceLayerGeometry();

			Geometry adaptedGeom = GeometryUtil.adapt(newGeometry, expectedClass);

			// modifies the feature's geometry in the store
			GeometryDescriptor geomAttr = store.getSchema().getGeometryDescriptor();

			Id filter = getFilterId(fidToUpdate);
			store.modifyFeatures(geomAttr, adaptedGeom, filter);

			transaction.commit();
		} catch (IOException e) {
			try {
				transaction.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
				throw makeException(e1);
			}

			final String msg = e.getMessage();
			LOGGER.severe(msg);
			throw makeException(e, msg);
		} finally {
			try {
				transaction.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw makeException(e);
			}
		}

	}

	protected Geometry makeCompatibleGeometry(	final ArrayList<Geometry> newGeomArray,
												final GeometryFactory geomFactory,
												final Class<? extends Geometry> originalGeom) {

		Class<? extends Geometry> targetGeom = getSourceLayerGeometry();

		Geometry result;
		if (GeometryCollection.class.equals(targetGeom.getSuperclass())) {

			Class<? extends GeometryCollection> geomCollectionClass = (Class<? extends GeometryCollection>) targetGeom;
			result = GeometryUtil.adaptToGeomCollection(newGeomArray, geomCollectionClass);

		} else { // Geometry or Simple Geometry class is required
			// Geometry type as target is required then do not need adaptation,
			// creates the geometry required by the source feature
			result = GeometryUtil.adapt(newGeomArray, originalGeom);
		}

		return result;
	}

	protected Geometry projectOnTargetLayer(final Geometry geom) throws SpatialOperationException {

		try {
			// change the geometry in result by the projected on target's crs
			Geometry geomProjected = GeoToolsUtils.reproject(geom, mapCrs, targetCrs);

			return geomProjected;
		} catch (Exception e) {

			throw makeException(e, e.getMessage());
		}

	}

	/**
	 * Get the geometry of the clip layer.
	 * 
	 * @param layer
	 * @return
	 */
	protected Class<? extends Geometry> getSourceLayerGeometry() {

		GeometryDescriptor attr = sourceFeatures.getSchema().getGeometryDescriptor();
		Class<? extends Geometry> result = (Class<? extends Geometry>) attr.getType().getBinding();

		return result;
	}

}
