/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Wien Government 
 *
 *      http://wien.gov.at
 *      http://www.axios.es 
 *
 * (C) 2010, Vienna City - Municipal Department of Automated Data Processing, 
 * Information and Communications Technologies.
 * Vienna City agrees to license under Lesser General Public License (LGPL).
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.geotools.util.GeoToolsUtils;

/**
 * Executes the add, modify, and Delete operations in the transaction context, 
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
final class SplitTransaction {

	private static final Logger LOGGER = Logger
			.getLogger(SplitTransaction.class.getName());

	protected static final FilterFactory FILTER_FACTORY = CommonFactoryFinder
			.getFilterFactory(null);

	private List<SimpleFeature> createdFeatures = new LinkedList<SimpleFeature>();
	private List<SimpleFeature> deletedFeatures = new LinkedList<SimpleFeature>();
	private List<SimpleFeature> modifiedFeatures = new LinkedList<SimpleFeature>();

	private SimpleFeatureStore store;	

	/**
	 * New instance of {@link SplitTransaction}
	 * 
	 * @param store
	 * @param featuresToAdd
	 * @param featuresToDelete
	 * @param featuresToModify
	 * @return {@link SplitTransaction}
	 */
	public static SplitTransaction newInstance(
			final SimpleFeatureStore store,
			final List<SimpleFeature> featuresToAdd,
			final List<SimpleFeature> featuresToDelete,
			final List<SimpleFeature> featuresToModify) {

		assert store != null;
		assert featuresToAdd != null;
		assert featuresToDelete != null;
		assert featuresToModify != null;
		
		SplitTransaction st = new SplitTransaction();
		st.store = store;
		st.createdFeatures = featuresToAdd;
		st.deletedFeatures = featuresToDelete;
		st.modifiedFeatures = featuresToModify;
		
		return st;
	}

	/**
	 * Executes the add, modify, and Delete operations in the store. 
	 * @throws IOException
	 */
	public void execute() throws IOException {

		// Adds the split result in the target layer
		insertFeaturesInStore(this.store, this.createdFeatures);

		// modifies the neighbors features
		modifyFeaturesInStore(this.store, this.modifiedFeatures);

		// deletes the feature what was split.
		deleteFeatureInStore(this.store, this.deletedFeatures);
	}
	
	/**
	 * Removes the features from the indeed store
	 * 
	 * @param store	the store
	 * @param featureList the feature that will be deleted from store
	 * @throws SpatialOperationException
	 */
	private void deleteFeatureInStore(
			SimpleFeatureStore store,
			List<SimpleFeature> featureList)
		throws IOException {

		// makes a filter ID with the feature's identifiers that will be deleted
		Set<Identifier> idsToDelete = new HashSet<Identifier>();
		for (SimpleFeature feature: featureList) {
			Identifier id = FILTER_FACTORY.featureId(feature.getID());
			idsToDelete.add(id);
		}
		Transaction tx = store.getTransaction();
		try {
			Id IdFilter = FILTER_FACTORY.id(idsToDelete);
			store.removeFeatures(IdFilter);
			tx.commit();
			
		} catch (Exception e) {
			
			tx.rollback();
			LOGGER.severe(e.getMessage());
			throw new IOException(e);
		} finally {
			tx.close();
		}
	}

	/**
	 * Modifies the geometries of indeed features
	 * 
	 * @param store
	 * @param featureList	features that its geometries were modified
	 * 
	 * @throws TransformException 
	 * @throws OperationNotFoundException 
	 * @throws IOException 
	 */
	private void modifyFeaturesInStore(
			final SimpleFeatureStore store,
			final List<SimpleFeature> featureList) 
		throws  IOException  {

		for (SimpleFeature feature : featureList) {

			Geometry geomInTargetCRS = null;
			try {
				geomInTargetCRS = GeoToolsUtils.reproject((Geometry) feature
						.getDefaultGeometry(), feature.getFeatureType()
						.getCoordinateReferenceSystem(), store.getSchema()
						.getCoordinateReferenceSystem());

			} catch (Exception e) {
				LOGGER.severe(e.getMessage());
				throw new IOException(e.getMessage());
			}
			Identifier id = FILTER_FACTORY.featureId(feature.getID());
			Set<Identifier> ids = new HashSet<Identifier>();
			ids.add(id);
			Id filter = FILTER_FACTORY.id(ids);

			Transaction transaction = this.store.getTransaction();
			try {
				store.modifyFeatures(feature.getDefaultGeometryProperty()
						.getName(), geomInTargetCRS, filter);
				transaction.commit();
			} catch (Exception e) {
				LOGGER.severe(e.getMessage());
				transaction.rollback();
				throw new IOException(e);
			} finally {
				transaction.close();
			}

		}
	}

	/**
	 * Inserts the features in the data store
	 * @param store the data store
	 * @param featureList the list of features to insert
	 * 
	 * @throws TransformException 
	 * @throws OperationNotFoundException 
	 * @throws IOException 
	 */
	private void insertFeaturesInStore(
			final SimpleFeatureStore store,
			final List<SimpleFeature> featureList)
			throws  IOException {

		SimpleFeature[] featuresArray = new SimpleFeature[featureList.size()];
		try {
			int i = 0;
			for (SimpleFeature fragment : featureList) {

				// projects the geometry to target
				Geometry geomInTargetCRS = GeoToolsUtils.reproject(
						(Geometry) fragment.getDefaultGeometry(), fragment
								.getFeatureType()
								.getCoordinateReferenceSystem(), store
								.getSchema().getCoordinateReferenceSystem());
				fragment.setDefaultGeometry(geomInTargetCRS);
				featuresArray[i++] = fragment;
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			throw new IOException(e.getMessage());
		}

		Transaction tx = store.getTransaction();
		try{
			List<FeatureId> fidSet = store.addFeatures(DataUtilities
					.collection(featuresArray));
			if (fidSet.isEmpty()) {
				tx.rollback();
				new SpatialOperationException(
						"The split features are not inserted in the store.s"); //$NON-NLS-1$
			}
			tx.commit();
		} catch (Exception e){
			tx.rollback();
			LOGGER.severe(e.getMessage());
			throw new IOException(e.getMessage());
		} finally{
			tx.close();
		}
	}	

}
