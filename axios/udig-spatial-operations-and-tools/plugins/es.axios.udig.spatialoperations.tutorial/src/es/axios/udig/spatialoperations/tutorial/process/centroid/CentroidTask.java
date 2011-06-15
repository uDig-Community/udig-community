/*
 * uDig Spatial Operations - Tutorial - http://www.axios.es (C) 2009,
 * Axios Engineering S.L. This product is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This product is distributed as part of tutorial, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.tutorial.process.centroid;

import java.io.IOException;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import es.axios.geotools.util.FeatureUtil;
import es.axios.geotools.util.GeoToolsUtils;

/**
 * 
 * This process makes the centroid using the features present in the source data
 * store. The new features with the centroid geometry are inserted in the target
 * data store.
 * 
 * @author Mauricio Pazos (www.axios.es)
 */
public final class CentroidTask {

	private final SimpleFeatureStore			sourceStore;
	private final Filter						sourceFilter;
	private final CoordinateReferenceSystem		sourceCRS;
	private final SimpleFeatureStore			targetStore;
	private final CoordinateReferenceSystem		targetCRS;
	private final CoordinateReferenceSystem		mapCRS;

	/**
	 * Creates a new instance of {@link CentroidTask}
	 * 
	 * @param sourceStore
	 * @param sourceCRS
	 * @param sourceFilter
	 * @param targetStore
	 * @param targetCRS
	 * @param mapCRS
	 * @return new instance of {@link CentroidTask}
	 */
	public CentroidTask(final SimpleFeatureStore sourceStore,
						final CoordinateReferenceSystem sourceCRS,
						final Filter sourceFilter,
						final SimpleFeatureStore targetStore,
						final CoordinateReferenceSystem targetCRS,
						final CoordinateReferenceSystem mapCRS) {

		assert sourceStore != null : "Illegal argument. Expects sourceStore != null"; //$NON-NLS-1$
		assert sourceCRS != null : "Illegal argument. Expects sourceCRS != null"; //$NON-NLS-1$
		assert sourceFilter != null : "Illegal argument. Expects sourceFilter != null"; //$NON-NLS-1$
		assert targetStore != null : "Illegal argument. Expects targetStore != null"; //$NON-NLS-1$
		assert targetCRS != null : "Illegal argument. Expects targetCRS != null"; //$NON-NLS-1$
		assert mapCRS != null : "Illegal argument. Expects targetCRS != null"; //$NON-NLS-1$

		this.sourceStore = sourceStore;
		this.sourceCRS = sourceCRS;
		this.sourceFilter = sourceFilter;
		this.targetStore = targetStore;
		this.targetCRS = targetCRS;
		this.mapCRS = mapCRS;
	}

	public void run() {

		SimpleFeatureCollection features = null;
		SimpleFeatureIterator iter = null;
		try {
			features = (SimpleFeatureCollection) this.sourceStore.getFeatures(this.sourceFilter);

			iter = features.features();
			while (iter.hasNext()) {
				SimpleFeature feature = iter.next();

				SimpleFeature centroid = makeCentroid(feature, this.sourceCRS, this.targetStore.getSchema(),
							this.targetCRS, this.mapCRS);

				insert(this.targetStore, centroid);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (features != null)
				iter.close();

		}
	}

	public SimpleFeatureStore getResult() {
		return this.targetStore;
	}

	/**
	 * Makes the centroid geometry and attaches it to ths new Centroid Feature
	 * 
	 * @param sourceFeature
	 * @param sourceCRS
	 * @param targetFeatureType
	 * @param targetCRS
	 * @param mapCRS
	 * @return a new Centroid Feature
	 * @throws IOException
	 */
	private SimpleFeature makeCentroid(	final SimpleFeature sourceFeature,
										final CoordinateReferenceSystem sourceCRS,
										final SimpleFeatureType targetFeatureType,
										final CoordinateReferenceSystem targetCRS,
										final CoordinateReferenceSystem mapCRS) throws IOException {

		try {

			Geometry geom = (Geometry) sourceFeature.getDefaultGeometry();
			geom = GeoToolsUtils.reproject(geom, sourceCRS, mapCRS);
			Point point = geom.getCentroid();
			point = (Point) GeoToolsUtils.reproject(point, mapCRS, targetCRS);

			SimpleFeature newFeature = FeatureUtil.createFeatureUsing(sourceFeature, targetFeatureType, point);

			return newFeature;

		} catch (Exception e) {

			throw new IOException(e.getMessage());
		}

	}

	/**
	 * Adds the centroid feature in the data store
	 * 
	 * @param targetStore
	 * @param centroidFeature
	 */
	private void insert(final SimpleFeatureStore targetStore,
						final SimpleFeature centroidFeature) {

		Transaction transaction = targetStore.getTransaction();
		try {
			List<FeatureId> newIds = targetStore.addFeatures(DataUtilities
						.collection(new SimpleFeature[] { centroidFeature }));
			if (newIds.size() != 1) {
				final String msg = "failed inserting."; //$NON-NLS-1$
				throw new IOException(msg);
			}
			transaction.commit();
		} catch (IOException e) {
			try {
				targetStore.getTransaction().rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
