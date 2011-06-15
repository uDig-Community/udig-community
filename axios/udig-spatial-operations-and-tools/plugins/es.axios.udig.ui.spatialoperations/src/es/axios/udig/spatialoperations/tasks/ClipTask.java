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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Id;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Point;

import es.axios.geotools.util.FeatureUtil;
import es.axios.geotools.util.GeoToolsUtils;
import es.axios.lib.geometry.util.GeometryUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;

/**
 * <p>
 * Uses the feature's geometries of clipping collection to cut the features
 * contained in the layer to clip.
 * </p>
 * <p>
 * This process must:
 * <ul>
 * <li>split geometries saving its data.
 * <li>delete geometries of features included in clip area  
 * <li>does the difference of that geometries that intersect the clipping
 * geometry.
 * <li>makes hole in feature's geometry if the clipping area is contained by
 * clipping geometry .
 * <ul>
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
final class ClipTask extends AbstractCommonTask implements IClipTask {

	/**
	 * To create an instance must use
	 * {@link #createProcess(FeatureCollection, FeatureCollection, FeatureStore, CoordinateReferenceSystem, CoordinateReferenceSystem, CoordinateReferenceSystem)}
	 */
	private ClipTask() {

	}

	/**
	 * Creates the clip process implementation.
	 * 
	 * @param usingFeatures
	 * @param clipSourceFeatures
	 * @param targetStore
	 * @param mapCrs
	 * @param usingCrs
	 * @param clipSourceCrs
	 * @param targetGeomAttrType
	 * @param isCreatingNewLayer
	 * @param layerToClip
	 * @param targetLayer
	 * @param targetLayerName
	 * @param clipSourceName
	 * @param clipSourceFeatureType
	 * @param targetCrs
	 * @return an instance of {@link ClipTask}
	 */
	public static IClipTask createProcess(	final FeatureCollection<SimpleFeatureType, SimpleFeature> usingFeatures,
											final FeatureCollection<SimpleFeatureType, SimpleFeature> clipSourceFeatures,
											final SimpleFeatureStore targetStore,
											final CoordinateReferenceSystem mapCrs,
											final CoordinateReferenceSystem usingCrs,
											final CoordinateReferenceSystem clipSourceCrs,
											final boolean isCreatingNewLayer,
											final String clipSourceName,
											final String targetLayerName,
											final CoordinateReferenceSystem targetCrs) {

		assert usingFeatures != null;
		assert clipSourceFeatures != null;
		assert targetStore != null;
		assert mapCrs != null;
		assert usingCrs != null;
		assert clipSourceCrs != null;
		assert isCreatingNewLayer == true || isCreatingNewLayer == false;
		assert clipSourceName != null;
		assert targetLayerName != null;
		assert targetCrs != null;

		ClipTask task = new ClipTask();

		task.usingFeatures = usingFeatures;
		task.sourceFeatures = clipSourceFeatures;
		task.targetStore = targetStore;
		task.mapCrs = mapCrs;
		task.usingCrs = usingCrs;
		task.sourceCrs = clipSourceCrs;
		task.isCreatingNewLayer = isCreatingNewLayer;
		task.sourceName = clipSourceName;
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
					iterUsing.close();
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
	 * Clips the feature using the geometries of the clipping feature. If the
	 * geometry collection of feature to clip was modified by the clip process
	 * this method will return true.
	 * 
	 * @param fidToClip
	 *            the feature to clip
	 * @param featureToClipCrs
	 *            crs of feature to clip
	 * @param clippingFeature
	 *            the feature used to clip the feature collection
	 * @param clippingCrs
	 *            crs of clipping feature
	 * @param mapCrs
	 *            map's crs
	 * 
	 * @return true if the count of geometry to clip was modified, false in
	 *         other case.
	 * @throws SpatialOperationException
	 */
	@Override
	protected boolean cutSourceWithUsingFeature(final String fidToClip,
												final CoordinateReferenceSystem featureToClipCrs,
												final SimpleFeature clippingFeature,
												final CoordinateReferenceSystem clippingCrs,
												final CoordinateReferenceSystem mapCrs)
		throws SpatialOperationException {

		try {

			Geometry clippingFeatureGeometry = (Geometry) clippingFeature.getDefaultGeometry();

			boolean numGeomToClipModified = false;
			final int numClippigGeometries = clippingFeatureGeometry.getNumGeometries();

			for (int i = 0; (i < numClippigGeometries) && !numGeomToClipModified; i++) {

				Geometry clippingGeometry = clippingFeatureGeometry.getGeometryN(i);

				SimpleFeature featureToClip = findFeature(this.targetStore, fidToClip);
				numGeomToClipModified = clipFeatureUsing(featureToClip, featureToClipCrs, clippingGeometry,
							clippingCrs, mapCrs);
			}
			return numGeomToClipModified;

		} catch (Exception e) {

			final String emsg = e.getMessage();
			LOGGER.severe(emsg);
			throw makeException(e, emsg);
		}
	}

	/**
	 * Clips the feature using the clipping geometry, and adds the change.
	 * 
	 * @param featureToClip
	 *            this feature could be the original or a partially processed
	 *            feature (processed in previous step)
	 * @param featureToClipCrs
	 * @param clippingGeometry
	 *            used to cut the featureToClip
	 * @param clippingCrs
	 * @param mapCrs
	 * 
	 * @return true if the count of geometry to clip was modified by delete or
	 *         split geometry operation
	 * @throws SpatialOperationException
	 */
	private boolean clipFeatureUsing(	final SimpleFeature featureToClip,
										final CoordinateReferenceSystem featureToClipCrs,
										final Geometry clippingGeometry,
										final CoordinateReferenceSystem clippingCrs,
										final CoordinateReferenceSystem mapCrs) throws SpatialOperationException {

		try {
			Geometry featureGeometryToClip = (Geometry) featureToClip.getDefaultGeometry();
			String fidToClip = featureToClip.getID();

			Geometry clippingGeometryOnMap = GeoToolsUtils.reproject(clippingGeometry, clippingCrs, mapCrs);

			// Iterates in the feature's geometries to clip then applies the
			// delete,
			// split or difference operation using the clipping geometry.
			// The process is reinitialized if the geometry collection is
			// modified
			// by a delete or split operation.
			boolean geomCollectionModified = false;
			final int numGeomToClip = featureGeometryToClip.getNumGeometries();

			for (int i = 0; (i < numGeomToClip) && !geomCollectionModified; i++) {

				Geometry geom = featureGeometryToClip.getGeometryN(i);

				Geometry currentGeomOnMap = GeoToolsUtils.reproject(geom, featureToClipCrs, mapCrs); // TODO
				// could
				// be
				// extracted
				// from
				// loop

				IntersectionMatrix geomRelation = clippingGeometryOnMap.relate(currentGeomOnMap);

				// Does the examination of the geometry's position and applies
				// the geometry operation. The partial result is saved in the
				// target layer.
				if (geomRelation.isContains()) {

					transactionDelete(fidToClip, i);

					geomCollectionModified = true;
				} else if (geomRelation.isWithin()) {

					Geometry diffGeomOnMap = computeGeometryDifference(currentGeomOnMap, clippingGeometryOnMap);

					transactionHole(fidToClip, i, diffGeomOnMap);

				} else if (splits(clippingGeometryOnMap, currentGeomOnMap, geomRelation)) {

					GeometryCollection splitGeomOnMap = computeGeometrySplit(currentGeomOnMap, clippingGeometryOnMap,
								featureGeometryToClip);

					transactionSplit(fidToClip, i, splitGeomOnMap);

					geomCollectionModified = true;

				} else if (geomRelation.isIntersects()) {

					Geometry diffGeomOnMap = computeGeometryDifference(currentGeomOnMap, clippingGeometryOnMap);

					transactionDifference(fidToClip, i, diffGeomOnMap);
				}
			}
			return geomCollectionModified;
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			throw makeException(e);
		}

	}

	/**
	 * Makes the geometry difference between the feature's geometry and the
	 * clipping geometry. If the features type is geometry or multigeometry
	 * (geometry collection ) then modifies the feature's geometry. If the
	 * feature type has a simple geometry (point, polygon, line) then makes new
	 * geometries for each.
	 * 
	 * @param featureCrs
	 * @param simpleGeometry
	 * @param clippingGeometryOnMapCrs
	 * @param mapCrs
	 * @return split geometry.
	 * @throws SpatialOperationException
	 */
	private GeometryCollection computeGeometrySplit(final Geometry simpleGeometry,
													final Geometry clippingGeometryOnMapCrs,
													final Geometry featureGeometryToClip)
		throws SpatialOperationException {

		try {
			// does the difference
			Geometry geoDiff = simpleGeometry.difference(clippingGeometryOnMapCrs);
			assert geoDiff instanceof GeometryCollection;

			GeometryCollection geoCollection = (GeometryCollection) geoDiff;

			return geoCollection;

		} catch (Exception e) {

			final String msg = e.getMessage();
			LOGGER.severe(msg);
			throw makeException(e, msg);
		}
	}

	/**
	 * Evaluates if clippingGeometry divides or not the feature's geometry. The
	 * params featureGeometry cannot be instance of Geometry Collection (Muli
	 * ....)
	 * 
	 * @param clippingGeometry
	 * @param featureGeometry
	 * @param matrix
	 * 
	 * @return true if clippingGeometry divides the feature's geometry, false in
	 *         other case.
	 */
	private final boolean splits(	final Geometry clippingGeometry,
									final Geometry featureGeometry,
									final IntersectionMatrix matrix) {

		assert !(featureGeometry instanceof GeometryCollection);

		if (featureGeometry instanceof Point) {
			return false; // cannot split a point
		}
		// If the result of difference is a multiGeometry then the geometry is
		// split.
		Geometry geoDiff = featureGeometry.difference(clippingGeometry);

		boolean isGeometryCollection = geoDiff instanceof GeometryCollection;
		boolean intersects = matrix.isIntersects();

		return isGeometryCollection && intersects;
	}

	/**
	 * Does a hole in featureToClip
	 * 
	 * @param fidToClip
	 * @param posCurrentGeom
	 * @param diffGeomOnMap
	 * 
	 * @throws SpatialOperationException
	 */
	private void transactionHole(final String fidToClip, final int posCurrentGeom, final Geometry diffGeomOnMap)
		throws SpatialOperationException {

		transactionDifference(fidToClip, posCurrentGeom, diffGeomOnMap);
	}

	/**
	 * Creates feature for each geometry fragment. The method take into account
	 * the following two case:
	 * <p>
	 * If the original feature (to clip feature) has simple geometry, creates
	 * new features for each new fragment. If it has Geometry collection, adds
	 * the fragments in the collection replacing the old geometry.
	 * </p>
	 * 
	 * @param fidToClip
	 * @param posGeometry
	 * @param geometryFragmentsOnMap
	 * 
	 * @throws SpatialOperationException
	 */
	private void transactionSplit(	final String fidToClip,
									final int posGeometry,
									final GeometryCollection geometryFragmentsOnMap) throws SpatialOperationException {

		// retrieve the feature partially modified and update its geometry
		SimpleFeature featureToClip = findFeature(this.targetStore, fidToClip);
		Geometry originalGeom = (Geometry) featureToClip.getDefaultGeometry();

		if (originalGeom instanceof GeometryCollection) {

			// updates the geometry presents in the result
			GeometryCollection newGeometry = replaceSplitByFragments(originalGeom, posGeometry, geometryFragmentsOnMap);

			Geometry geomProjected = projectOnTargetLayer(newGeometry);
			modifyFeatureGeometryInStore(fidToClip, geomProjected, this.targetStore);

		} else { // update the feature with Geometry collection with the new
			// fragments
			// if toClip layer and target are equals, inserts the fragments
			// and deletes the original feature else only inserts the new
			// fragments
			final boolean modifyLayerToClip = this.sourceName.equals(this.targetLayerName);

			// saves the attributes values in fragments before deletes the
			// original feature
			final boolean copyData = modifyLayerToClip || isCreatingNewLayer;
			// has simple geometry
			Set<SimpleFeature> featureList = createFeatureFragments(featureToClip, geometryFragmentsOnMap, copyData);
			assert !featureList.isEmpty();

			Set<String> newFidFragments = insertFeaturesInStore(featureList, this.targetStore);
			this.featuresInProcessing.addAll(newFidFragments);

			// if the values were saved the source feature must be deleted
			String processedFID = featureToClip.getID();
			deleteFeature(processedFID);
			this.featuresInProcessing.remove(processedFID);

		}
	}

	/**
	 * Replaces the split geometry in the original geometry by its fragments.
	 * 
	 * @param originalGeometry
	 * @param posSplitGeometry
	 * @param geometryFragments
	 * 
	 * @return a geometry collection with the new geometry fragments
	 */
	private GeometryCollection replaceSplitByFragments(	final Geometry originalGeometry,
														final int posSplitGeometry,
														final GeometryCollection geometryFragments) {

		final int numGeoms = originalGeometry.getNumGeometries();

		// adds geometries contained in the original geometry without the split
		// geometry
		ArrayList<Geometry> newGeomArray = new ArrayList<Geometry>(numGeoms - 1);
		for (int i = 0; i < numGeoms; i++) {

			if (i != posSplitGeometry) {
				Geometry currentGeom = originalGeometry.getGeometryN(i);
				newGeomArray.add(currentGeom);
			}
		}
		// adds the fragments to the final result
		final int numFragments = geometryFragments.getNumGeometries();
		for (int i = 0; i < numFragments; i++) {
			Geometry g = geometryFragments.getGeometryN(i);
			newGeomArray.add(g);
		}

		Class<GeometryCollection> expectedClass = (Class<GeometryCollection>) getClipLayerGeometry();

		GeometryCollection result = GeometryUtil.adaptToGeomCollection(newGeomArray, expectedClass);

		return result;
	}

	/**
	 * Creates the features for each geometry fragment.
	 * 
	 * @param featurePrototype
	 * @param geomFragmentCollectionOnMap
	 * @param requireSaveData
	 * @return return the fragments projected on target layer
	 * @throws SpatialOperationException
	 */
	private final Set<SimpleFeature> createFeatureFragments(final SimpleFeature featurePrototype,
															final GeometryCollection geomFragmentCollectionOnMap,
															final boolean requireSaveData)
		throws SpatialOperationException {

		try {
			Set<SimpleFeature> newFeatures = new HashSet<SimpleFeature>();

			// creates a new feature for each geometry fragment
			for (int i = 0; i < geomFragmentCollectionOnMap.getNumGeometries(); i++) {

				Geometry geom = geomFragmentCollectionOnMap.getGeometryN(i);
				Geometry geomOnTarget = projectOnTargetLayer(geom);

				SimpleFeature feature = createFeature(featurePrototype, geomOnTarget, requireSaveData);
				newFeatures.add(feature);
			}
			return newFeatures;

		} catch (Exception e) {
			String msg = e.getMessage();
			LOGGER.severe(msg);
			throw makeException(e, msg);
		}
	}

	/**
	 * Creates a new feature using a prototype. The new feature will have the
	 * new geometry and all the attributes present in the prototype.
	 * 
	 * @param featurePrototype
	 * @param newGeometry
	 * @param requiresCopyData
	 * @return a new feature
	 * @throws SpatialOperationException
	 */
	private final SimpleFeature createFeature(	final SimpleFeature featurePrototype,
												final Geometry newGeometry,
												final boolean requiresCopyData) throws SpatialOperationException {

		try {

			// create the new feature and set the geometry fragment
			SimpleFeature newFeature = DataUtilities.template(featurePrototype.getFeatureType());

			// copies the data in the new feature
			if (requiresCopyData) {
				newFeature = FeatureUtil.copyAttributes(featurePrototype, newFeature);
			}
			newFeature.setDefaultGeometry(newGeometry);

			return newFeature;

		} catch (Exception e) {
			final String msg = e.getMessage();
			LOGGER.severe(msg);
			throw makeException(e, msg);
		}
	}

	/**
	 * Deletes the geometry's feature. The feature to clip could be deleted if
	 * the feature result has not geometry. In other case the method will delete
	 * only the clipped geometry and modifies the processed feature.
	 * 
	 * @param fidToClip
	 * @param geomPosition
	 * 
	 * @return the feature without the geometry
	 * @throws SpatialOperationException
	 */
	private void transactionDelete(final String fidToClip, final int geomPosition) throws SpatialOperationException {

		try {
			// retrieve the feature partially modified and update its geometry
			SimpleFeature featureToClip = findFeature(this.targetStore, fidToClip);
			Geometry originalGeom = (Geometry) featureToClip.getDefaultGeometry();

			Geometry deleteGeom = computeGeometryDelete(geomPosition, originalGeom);
			featureToClip.setDefaultGeometry(deleteGeom);

			// updates the store with the result feature or deletes it, if the
			// result feature has not any geometries
			Geometry resultPorjected = projectOnTargetLayer(deleteGeom);
			if (resultPorjected.getNumGeometries() == 0) {

				deleteFeature(fidToClip);
				this.featuresInProcessing.remove(fidToClip);
			} else {
				modifyFeatureGeometryInStore(fidToClip, resultPorjected, this.targetStore);
			}

		} catch (IllegalAttributeException e) {
			final String msg = e.getMessage();
			LOGGER.severe(msg);
			throw makeException(e, msg);
		}
	}

	/**
	 * Removes the geometry from the feature's geometries.
	 * 
	 * @param geomToDelete
	 *            a simple geometry
	 * @param originalGeometry
	 *            feature's geometry
	 * @return a geometry collection without the geometry to clip
	 */
	private Geometry computeGeometryDelete(final int positionOfGeomToDelete, final Geometry originalGeometry) {

		final int numOriginalGeom = originalGeometry.getNumGeometries();
		// create a new geometry array without the geometry to delete
		final int size = numOriginalGeom - 1;
		ArrayList<Geometry> newGeomArray = new ArrayList<Geometry>(size);

		for (int i = 0; i < numOriginalGeom; i++) {

			Geometry currentGeom = originalGeometry.getGeometryN(i);
			if (i != positionOfGeomToDelete) {
				newGeomArray.add(currentGeom);
			}
		}
		GeometryFactory geomFactory = originalGeometry.getFactory();

		Geometry result = makeCompatibleGeometry(newGeomArray, geomFactory, originalGeometry.getClass());

		return result;
	}

	/**
	 * Get the geometry of the clip layer.
	 * 
	 * @param layer
	 * @return
	 */
	private Class<? extends Geometry> getClipLayerGeometry() {

		GeometryDescriptor attr = sourceFeatures.getSchema().getGeometryDescriptor();
		Class<? extends Geometry> result = (Class<? extends Geometry>) attr.getType().getBinding();

		return result;
	}

	/**
	 * Deletes the feature if the target layer is equal to the layer to clip
	 * 
	 * @param fid
	 */
	private void deleteFeature(final String fid) {

		FeatureStore<SimpleFeatureType, SimpleFeature> store = getTargetStore();
		Transaction transaction = targetStore.getTransaction();

		try {
			// Deletes only if the target is equal to the layer to clip
			Id filter = getFilterId(fid);

			store.removeFeatures(filter);
			transaction.commit();
		} catch (IOException e) {

			try {
				transaction.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			final String msg = Messages.ClipProcess_failed_deleting;
			LOGGER.severe(msg);
			throw (RuntimeException) new RuntimeException(msg).initCause(e);
		} finally {
			try {
				transaction.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the FeatureStore of target layer
	 */
	private FeatureStore<SimpleFeatureType, SimpleFeature> getTargetStore() {

		assert this.targetStore != null;

		return this.targetStore;
	}
}
