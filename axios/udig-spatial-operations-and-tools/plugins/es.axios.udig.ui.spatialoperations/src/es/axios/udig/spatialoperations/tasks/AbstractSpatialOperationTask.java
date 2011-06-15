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
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureEvent.Type;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.spatial.BBOX;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import es.axios.geotools.util.FeatureUtil;
import es.axios.geotools.util.GeoToolsUtils;
import es.axios.lib.geometry.util.GeometryUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;

/**
 * Implements the common behavior for spatial operations task.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
abstract class AbstractSpatialOperationTask<V> {

	private static final Logger								LOGGER			= Logger.getLogger(AbstractSpatialOperationTask.class
																										.getName());

	protected SimpleFeatureStore							targetStore		= null;

	protected static final FilterFactory					FILTER_FACTORY	= CommonFactoryFinder
																							.getFilterFactory(null);

	public V call() throws Exception {

		assert targetStore != null : "target store is null!"; //$NON-NLS-1$

		addListeners();

		perform();

		return getResult();
	}

	/**
	 * Performs the algorithm. The subclass must provide an implementation.
	 */
	protected abstract void perform() throws SpatialOperationException;

	/**
	 * @return the task result. The subclass must provide an implementation.
	 */
	protected abstract V getResult();

	/**
	 * Standard exception handle.
	 * 
	 * @param e
	 * @return SpatialDataProcessException
	 */
	protected SpatialOperationException createException(Exception e) {
		LOGGER.severe(e.getMessage());
		final String msg = Messages.AbstractSpatialOperationTask_failed_inserting;
		SpatialOperationException processException = new SpatialOperationException(msg);
		return processException;
	}

	/**
	 * Standard exception handle.
	 * 
	 * @param msg
	 * @return SpatialDataProcessException
	 */
	protected SpatialOperationException createException(final String msg) {
		LOGGER.severe(msg);
		return new SpatialOperationException(msg);
	}

	/**
	 * Logs the exception message and creates an
	 * {@link SpatialOperationException}
	 * 
	 * @param e
	 * 
	 * @return {@link SpatialOperationException}
	 */
	protected SpatialOperationException makeException(final Exception e) {

		final String msg = e.getMessage();
		LOGGER.severe(msg);
		e.printStackTrace();
		return new SpatialOperationException(e);
	}

	/**
	 * Logs the user message and build the {@link SpatialOperationException}
	 * 
	 * @param e
	 * @param messageToUser
	 * @return {@link SpatialOperationException}
	 */
	protected SpatialOperationException makeException(final Exception e, final String messageToUser) {

		final String msg = e.getMessage();
		LOGGER.severe(msg);
		e.printStackTrace();
		SpatialOperationException ex = new SpatialOperationException(messageToUser);
		LOGGER.severe(msg);

		return ex;
	}

	/**
	 * Creates a new feature in the store using the geometry. This method copies
	 * the data present in the "to clip feature" as source to create the new
	 * feature.
	 * 
	 * @param store
	 * @param feature
	 * @return the new feature
	 * @throws SpatialOperationException
	 */
	// TODO REFACTOR.
	protected SimpleFeature createFeatureInStore(final FeatureStore<SimpleFeatureType, SimpleFeature> store,
													final SimpleFeature feature,
													final Class<? extends Geometry> expectedClass,
													final boolean isCreatingNewLayer) throws SpatialOperationException {

		Transaction transaction = store.getTransaction();
		List<FeatureId> fidSet;
		try {
			// project the feature geometry on store's CRS
			
			Geometry geomProjected = GeoToolsUtils.reproject(
					(Geometry) feature.getDefaultGeometry(), 
					feature.getFeatureType().getCoordinateReferenceSystem(), 
					store.getSchema().getCoordinateReferenceSystem());
			
			SimpleFeature newFeature = DataUtilities.template(store.getSchema());

			Geometry adaptedGeometry = GeometryUtil.adapt(geomProjected, expectedClass);

			newFeature.setDefaultGeometry(adaptedGeometry);

			if (isCreatingNewLayer) {

				newFeature = FeatureUtil.copyAttributes(feature, newFeature);
			}

			// clear the list.
			addedID.clear();
			committedID.clear();

			fidSet = store.addFeatures(DataUtilities.collection(new SimpleFeature[] { newFeature }));

			transaction.commit();

			retrieveAfterCommit();

			String fid;

			if (addedID.size() != 0) {
				fid = getCommittedFID();
			} else {
				FeatureId id = fidSet.iterator().next();
				fid = id.getID();
			}

			addedID.clear();
			committedID.clear();

			SimpleFeature storedFeature = findFeature(store, fid);
			assert storedFeature != null;

			return storedFeature;
		} catch (Exception e) {

			try {
				transaction.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
				throw makeException(e1);
			}
			final String msg = Messages.AbstractCommonTask_failed_creating_layer;
			LOGGER.severe(msg);
			throw makeException(e, msg);
		} finally {
			try {
				transaction.close();
			} catch (IOException e) {
				throw makeException(e);
			}
		}

	}

	/**
	 * Retrieves the feature from store. The feature must exist in the store.
	 * 
	 * @param store
	 * @param requestedFid
	 *            id of an existent feature
	 * 
	 * @return the found feature
	 * @throws SpatialOperationException
	 */
	protected SimpleFeature findFeature(final FeatureStore<SimpleFeatureType, SimpleFeature> store,
										final String requestedFid) throws SpatialOperationException {

		FeatureCollection<SimpleFeatureType, SimpleFeature> featuresCollection = null;
		FeatureIterator<SimpleFeature> iter = null;
		try {

			Id filter = getFilterId(requestedFid);

			featuresCollection = store.getFeatures(filter);
			iter = featuresCollection.features();

			assert iter.hasNext() : "feature not found :" + requestedFid; //$NON-NLS-1$

			SimpleFeature feature = iter.next();

			return feature;
		} catch (IOException e) {
			final String msg = e.getMessage();
			LOGGER.severe(msg);
			throw makeException(e, msg);
		} finally {
			if (iter != null) {
				iter.close();
			}
		}
	}

	protected SimpleFeature insertFeature(FeatureStore<SimpleFeatureType, SimpleFeature> store, SimpleFeature newFeature)
		throws SpatialOperationException {

		Transaction transaction = store.getTransaction();
		List<FeatureId> newIds;
		try {

			// clear the list.
			addedID.clear();
			committedID.clear();
			newIds = store.addFeatures(DataUtilities.collection(new SimpleFeature[] { newFeature }));

			if (newIds.size() != 1) {
				final String msg = Messages.DissolveTask_failed_inserting;
				throw createException(msg);
			}
			transaction.commit();

			retrieveAfterCommit();

			String fid;

			if (addedID.size() != 0) {
				fid = getCommittedFID();
			} else {
				FeatureId id = newIds.iterator().next();
				fid = id.getID();
			}

			addedID.clear();
			committedID.clear();
			SimpleFeature insertedFeature = findFeature(store, fid);

			return insertedFeature;
		} catch (IOException e) {
			try {
				transaction.rollback();
			} catch (IOException e1) {
				throw createException(e1);
			}
			throw createException(e);
		} finally {
			try {
				transaction.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw createException(e);
			}
		}
	}

	/**
	 * Adds the features in the store.
	 * 
	 * @param features
	 * @param store
	 * 
	 * @return set of new identifier for the created features
	 * 
	 * @throws SpatialOperationException
	 */
	protected Set<String> insertFeaturesInStore(final Set<SimpleFeature> features,
												final FeatureStore<SimpleFeatureType, SimpleFeature> store)
		throws SpatialOperationException {

		Transaction transaction = store.getTransaction();

		try {
			SimpleFeature[] featuresArray = features.toArray(new SimpleFeature[features.size()]);

			// clear the list.
			addedID.clear();
			committedID.clear();

			List<FeatureId> fidSet = store.addFeatures(DataUtilities.collection(featuresArray));

			transaction.commit();

			retrieveAfterCommit();

			Set<String> fid = new HashSet<String>();

			if (addedID.size() != 0) {
				fid = getCommittedFIDs();
			} else {
				FeatureId id = fidSet.iterator().next();
				fid.add(id.getID());
			}

			addedID.clear();
			committedID.clear();

			// Set<String> insertedID = new HashSet<String>();
			// for (FeatureId id : fidSet) {
			// insertedID.add(id.getID());
			// }
			return fid;

		} catch (Exception e) {

			try {
				transaction.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			e.printStackTrace();
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

	/**
	 * Will use a query created when features were added to the store to
	 * retrieve the same features but whit different ID because they were
	 * committed.
	 * 
	 * @throws IOException
	 */
	protected void retrieveAfterCommit() throws IOException {

		if (!addedID.isEmpty()) {

			assert query != null : "query musn't be null."; //$NON-NLS-1$

			FeatureCollection<SimpleFeatureType, SimpleFeature> collection = null;
			FeatureIterator<SimpleFeature> iter = null;

			try {
				collection = targetStore.getFeatures(query);
				iter = collection.features();
				while (iter.hasNext()) {

					SimpleFeature feature = iter.next();
					committedID.add(feature.getID());
				}
			} finally {
				if (iter != null) {
					iter.close();
				}
			}
		}
	}

	/**
	 * Search for the ID contained on the committedID set that isn't contained
	 * on the addedID set.
	 * 
	 * @return The ID of the feature added after being committed.
	 */
	private String getCommittedFID() {

		for (String idAfter : committedID) {
			// we assume this ID is the new one.
			boolean newID = true;
			for (String idBefore : addedID) {
				if (idBefore.equals(idAfter)) {
					newID = false;
					break;
				}
			}
			if (newID) {
				return idAfter;
			}
		}
		// instead of returning null
		return addedID.iterator().next();
	}

	private Set<String> getCommittedFIDs() {

		Set<String> newIDs = new HashSet<String>();

		for (String idAfter : committedID) {
			// we assume this ID is the new one.
			boolean newID = true;
			for (String idBefore : addedID) {
				if (idBefore.equals(idAfter)) {
					newID = false;
					break;
				}
			}
			if (newID) {
				newIDs.add(idAfter);
			}
		}

		return newIDs;
	}

	/**
	 * insert the feature in the target store
	 * 
	 * @param newFeature
	 * @throws SpatialOperationException
	 */
	protected void insert(FeatureStore<SimpleFeatureType, SimpleFeature> targetStore, SimpleFeature newFeature)
		throws SpatialOperationException {

		Transaction transaction = targetStore.getTransaction();

		try {
			List<FeatureId> newIds = targetStore.addFeatures(DataUtilities
						.collection(new SimpleFeature[] { newFeature }));

			if (newIds.size() != 1) {
				final String msg = Messages.AbstractTask_failed_inserting_feature;

				throw createException(msg);
			}

			transaction.commit();
		} catch (IOException e) {
			try {
				transaction.rollback();
			} catch (IOException e1) {

				throw makeException(e1);
			}
			throw makeException(e);
		} finally {
			try {
				transaction.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw makeException(e);
			}
		}
	}

	protected void addListeners() {

		// initialize the listener.
		FeatureListener targetListener = new FeatureListener() {

			public void changed(FeatureEvent featureEvent) {

				Type eventType = featureEvent.getType();
				switch (eventType) {

				case ADDED:

					featuresAdded(featureEvent.getBounds());
					break;
				case COMMIT:
					// TODO this will work on uDig trunk, right now the commit
					// event doesn't notify.
					// TEST IT NOW!
					featuresChanged(featureEvent.getBounds());
					break;
				case REMOVED:

					break;
				default:
					break;
				}
			}
		};

		assert targetListener != null : "listener is null"; //$NON-NLS-1$

		targetStore.addFeatureListener(targetListener);
	}

	private Set<String>		addedID		= new HashSet<String>();
	private Set<String>		committedID	= new HashSet<String>();
	private Query	query		= null;

	protected void featuresAdded(Envelope bounds) {

		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = null;
		FeatureIterator<SimpleFeature> iter = null;

		try {

			collection = getFeatureCollection(bounds);
			iter = collection.features();
			while (iter.hasNext()) {

				SimpleFeature feature = iter.next();
				addedID.add(feature.getID());
			}
		} finally {
			if (iter != null) {
				iter.close();
			}
		}
	}

	protected void featuresChanged(Envelope bounds) {

		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = null;
		FeatureIterator<SimpleFeature> iter = null;

		try {

			collection = getFeatureCollection(bounds);
			iter = collection.features();
			while (iter.hasNext()) {

				SimpleFeature feature = iter.next();
				committedID.add(feature.getID());
			}
		} finally {
			if (iter != null) {
				iter.close();
			}
		}
	}

	private FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(Envelope bounds) {

		SimpleFeatureType schema = targetStore.getSchema();
		final List<String> queryAtts = obtainQueryAttributesForFeatureTable(schema);
		final Query query = new Query(schema.getTypeName(), Filter.EXCLUDE, queryAtts
					.toArray(new String[0]));

		// TODO TEST IT.
		BBOX bboxFilter;
		String name = schema.getGeometryDescriptor().getName().getLocalPart();
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = null;
		try {
			double minx = bounds.getMinX();
			double miny = bounds.getMinY();
			double maxx = bounds.getMaxX();
			double maxy = bounds.getMaxY();
			String srs = CRS.lookupIdentifier(schema.getCoordinateReferenceSystem(), false);
			bboxFilter = FILTER_FACTORY.bbox(name, minx, miny, maxx, maxy, srs);

			query.setFilter(bboxFilter);

			collection = targetStore.getFeatures(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// set the value of the query.
		this.query = query;

		return collection;
	}

	private List<String> obtainQueryAttributesForFeatureTable(final SimpleFeatureType schema) {
		final List<String> queryAtts = new ArrayList<String>();

		for (int i = 0; i < schema.getAttributeCount(); i++) {
			AttributeDescriptor attr = schema.getDescriptor(i);
			if (!(attr instanceof GeometryDescriptor)) {
				queryAtts.add(attr.getName().getLocalPart());
			}
		}
		return queryAtts;
	}

	/**
	 * Get a filter id from a feature id.
	 * 
	 * @param requestedFid
	 *            feature id name
	 * @return The Id of the filter
	 */
	protected Id getFilterId(String requestedFid) {

		FeatureId fid = FILTER_FACTORY.featureId(requestedFid);
		Set<FeatureId> ids = new HashSet<FeatureId>(1);
		ids.add(fid);
		Id filter = FILTER_FACTORY.id(ids);

		return filter;
	}
}