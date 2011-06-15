/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
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
package es.axios.udig.spatialoperations.ui.taskmanager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;

import org.geotools.feature.SchemaException;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.geotools.util.FeatureTypeUnionBuilder;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.ISpatialJoinGeomParameters;
import es.axios.udig.spatialoperations.internal.parameters.ISpatialJoinInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.ISpatialJoinInNewLayerParameters;
import es.axios.udig.spatialoperations.tasks.ISpatialJoinTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationException;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.spatialoperations.tasks.SpatialRelation;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.LayerUtil;

/**
 * Monitoring of Spatial Join Operation.
 * <p>
 * This is an user interface that allows to see the progress of the spatial join
 * and cancel it, if that is needed.
 * </p>
 * 
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
final class SpatialJoinMonitor extends SOTaskMonitor<Object> {

	private static final Logger			LOGGER				= Logger.getLogger(SpatialJoinMonitor.class.getName());

	private FeatureTypeUnionBuilder		featureUnionBuilder	= null;
	private ISpatialJoinGeomParameters	params				= null;
	private ILayer						targetLayer			= null;
	private Future<Object>				future;

	private CoordinateReferenceSystem	targetCrs;

	/**
	 * New instance of ISOTaskMonitor
	 * 
	 * @param params
	 * @return SOProcessMonitor
	 */
	public static ISOTaskMonitor newInstance(ISpatialJoinGeomParameters params) {

		SpatialJoinMonitor monitor = new SpatialJoinMonitor(params);

		return monitor;
	}

	private SpatialJoinMonitor(final ISpatialJoinGeomParameters params) {

		this.params = params;
	}

	/**
	 * Creates the {@link SimpleFeatureType} required for the target store. The
	 * method will create the new type doing the union of attributes in first
	 * and second feature types.
	 * 
	 * @param typeName
	 * @param firsType
	 * @param secondType
	 * @param geomClass
	 * @param crs
	 * @return FeatureType
	 * @throws SchemaException
	 */
	private SimpleFeatureType createJoinFeatureType(final String typeName,
													final SimpleFeatureType firsType,
													final SimpleFeatureType secondType,
													final Class<? extends Geometry> geomClass,
													final CoordinateReferenceSystem crs) throws SchemaException {

		try {
			// build the join feature type
			this.featureUnionBuilder = new FeatureTypeUnionBuilder(typeName);

			this.featureUnionBuilder.add(firsType).add(secondType).setGeometryClass("geometry", geomClass, crs); //$NON-NLS-1$
			SimpleFeatureType newFeatureType = this.featureUnionBuilder.getFeatureType();

			return newFeatureType;

		} catch (SchemaException e) {
			LOGGER.severe(e.getMessage());
			throw e;
		}
	}

	/**
	 * Initializes the join process
	 */
	@Override
	protected void initTaskMonitor(IProgressMonitor progress) throws InvocationTargetException {

		final Filter filterInFirstLayer = this.params.getFilterInFirstLayer();

		final Filter filterInSecondLayer = this.params.getFilterInSecondLayer();

		FeatureCollection<SimpleFeatureType, SimpleFeature> firstSource;
		FeatureCollection<SimpleFeatureType, SimpleFeature> secondSource;
		try {
			firstSource = LayerUtil.getSelectedFeatures(this.params.getFirstLayer(), filterInFirstLayer);

			secondSource = LayerUtil.getSelectedFeatures(this.params.getSecondLayer(), filterInSecondLayer);
		} catch (IOException e) {
			throw makeInitializeExcepion(progress, e);
		}

		final SpatialRelation spatialRelation = this.params.getSpatialRelation();

		final CoordinateReferenceSystem mapCrs = this.params.getMapCrs();
		final CoordinateReferenceSystem firstCrs = this.params.getFirstCRS();
		final CoordinateReferenceSystem secondCrs = this.params.getSecondCRS();

		progress.worked(1);

		// creates the join geometry process
		try {

			progress.worked(2);

			ISpatialJoinTask<Object> task;
			if (this.params.isSelection()) {

				task = SpatialOperationFactory.createSpatialJoinSelection(firstSource, secondSource, spatialRelation,
							mapCrs, firstCrs, secondCrs);

			} else {
				SimpleFeatureStore targetStore = getTargetStore(firstSource, secondSource,
							this.params, progress);

				task = SpatialOperationFactory.createSpatialJoin(firstSource, secondSource, spatialRelation, mapCrs,
							targetStore, firstCrs, secondCrs, targetCrs);
			}
			ExecutorService executor = Executors.newCachedThreadPool();
			this.future = executor.submit(task);
			progress.worked(3);

		} catch (Exception e) {

			throw makeInitializeExcepion(progress, e);
		}
	}

	/**
	 * Creates the target {@link FeatureStore} doing the join of the feature
	 * types of the first and second source.
	 * 
	 * @param firstSource
	 * @param secondSource
	 * @param params
	 * @param monito
	 *            r
	 * @return {@link FeatureStore}
	 * @throws IOException
	 */
	private SimpleFeatureStore getTargetStore(	FeatureCollection<SimpleFeatureType, SimpleFeature> firstSource,
																			FeatureCollection<SimpleFeatureType, SimpleFeature> secondSource,
																			ISpatialJoinGeomParameters params,
																			IProgressMonitor monitor)
		throws IOException {

		SimpleFeatureStore targetStore;

		try {
			if (params instanceof ISpatialJoinInExistentLayerParameters) {

				targetLayer = ((ISpatialJoinInExistentLayerParameters) params).getTargetLayer();
				targetStore = getFeatureStore(this.targetLayer);
				targetCrs = ((ISpatialJoinInExistentLayerParameters) params).getTargetCRS();
			} else {
				// creates a new target layer
				ISpatialJoinInNewLayerParameters p = (ISpatialJoinInNewLayerParameters) this.params;

				// creates the join type for the new target store
				SimpleFeatureType joinType = createJoinFeatureType(p.getTargetName(), firstSource.getSchema(),
							secondSource.getSchema(), p.getTargetGeometry(), p.getMapCrs());

				IMap map = this.params.getFirstLayer().getMap();

				targetStore = (SimpleFeatureStore) createDataStore(map, joinType, monitor);

				targetCrs = LayerUtil.getCrs(targetLayer);
			}
			return targetStore;

		} catch (Exception e) {

			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Creates the target {@link FeatureStore} which will be returned and the
	 * new {@link ILayer} that will be added in the current map.
	 * 
	 * If the Spatial Join will return the features selected that fulfill the
	 * spatial relation then, it will return nothing.
	 * 
	 * @param map
	 * @param featureType
	 * @param monitor
	 * @return a new FetureStore
	 * @throws SpatialOperationException
	 */
	private FeatureStore<SimpleFeatureType, SimpleFeature> createDataStore(	final IMap map,
																			final SimpleFeatureType featureType,
																			final IProgressMonitor monitor)
		throws SpatialOperationException {

		if (this.params.isSelection()) {
			return null;
		}

		IGeoResource geoResource = AppGISMediator.createTempGeoResource(featureType);
		assert geoResource != null;

		try {
			FeatureStore<SimpleFeatureType, SimpleFeature> featureStore;
			featureStore = geoResource.resolve(FeatureStore.class, monitor);
			assert featureStore != null;

			this.targetLayer = addLayerToMap(map, geoResource);

			return featureStore;

		} catch (IOException e) {

			final String msg = e.getMessage();

			throw new SpatialOperationException(msg);
		}
	}

	/**
	 * Prepares the result of this process
	 * 
	 * @throws InterruptedException
	 */
	@Override
	protected void deliveryResult(Future<Object> future, ILayer target) throws InterruptedException {

		try {

			if (this.params.isSelection()) {

				Id filter = (Id) future.get();

				LayerUtil.presentSelection(this.params.getFirstLayer(), filter);
			} else {
				presentFeaturesOnTargetLayer(this.targetLayer);
			}
		} catch (Exception e) {
			throw makeTaskInterrumptedException(getMonitor(), e);
		}
	}

	@Override
	protected String getBeginMessage() {

		final String msg = MessageFormat.format("Spatial Join {0}.{1}.{2} ", params.getFirstLayer().getName(), params //$NON-NLS-1$
					.getSpatialRelation().toString(), params.getSecondLayer().getName());
		return msg;
	}

	@Override
	protected String getCancelMessage() {

		return Messages.SpatialJoinMonitor_canceled;
	}

	@Override
	protected String getDoneMessage() {

		return Messages.SpatialJoinMonitor_successful;
	}

	@Override
	protected Future<Object> getFuture() {

		return this.future;
	}

	@Override
	protected IMap getMap() {

		return this.params.getFirstLayer().getMap();
	}

	@Override
	protected ILayer getTarget() {

		return this.targetLayer;
	}

}
