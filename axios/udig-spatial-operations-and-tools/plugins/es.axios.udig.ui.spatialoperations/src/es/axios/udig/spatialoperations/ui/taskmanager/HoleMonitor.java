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
package es.axios.udig.spatialoperations.ui.taskmanager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IHoleInExistentLayer;
import es.axios.udig.spatialoperations.internal.parameters.IHoleInNewLayer;
import es.axios.udig.spatialoperations.internal.parameters.IHoleParameters;
import es.axios.udig.spatialoperations.tasks.IHoleTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Monitoring the hole operation.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
final class HoleMonitor extends SOTaskMonitor<FeatureStore<SimpleFeatureType, SimpleFeature>> {

	private Future<FeatureStore<SimpleFeatureType, SimpleFeature>>	future;
	private IHoleParameters											params;
	private ILayer													targetLayer	= null;

	private ILayer													sourceLayer	= null;
	private ILayer													usingLayer	= null;
	private boolean													isCreatingNewLayer;
	private CoordinateReferenceSystem								targetCrs	= null;

	/**
	 * New instance for {@link HoleMonitor}. Client must use
	 * {@link #newInstance(IHoleParameters)}.
	 * 
	 * @param params
	 */
	private HoleMonitor(IHoleParameters params) {

		assert params != null;

		this.params = params;
	}

	public static ISOTaskMonitor newInstance(IHoleParameters params) {

		return new HoleMonitor(params);
	}

	@Override
	protected void deliveryResult(Future<FeatureStore<SimpleFeatureType, SimpleFeature>> future, ILayer target)
		throws InterruptedException {

		presentFeaturesOnTargetLayer(target);
	}

	@Override
	protected String getBeginMessage() {

		final String msg = MessageFormat.format(Messages.HoleMonitor_cutting_layers, sourceLayer.getName(), usingLayer
					.getName());
		return msg;
	}

	@Override
	protected String getCancelMessage() {

		return Messages.HoleMonitor_process_canceled;
	}

	@Override
	protected String getDoneMessage() {

		return Messages.HoleMonitor_finished_success;
	}

	@Override
	protected Future<FeatureStore<SimpleFeatureType, SimpleFeature>> getFuture() {

		return future;
	}

	@Override
	protected IMap getMap() {

		IMap map = this.usingLayer.getMap();
		assert map != null;
		return map;
	}

	@Override
	protected ILayer getTarget() {

		return this.targetLayer;
	}

	@Override
	protected void initTaskMonitor(IProgressMonitor progress) throws InvocationTargetException {

		sourceLayer = params.getSourceLayer();
		assert sourceLayer != null;

		usingLayer = params.getUsingLayer();
		assert usingLayer != null;

		Filter sourceFilter = params.getSourceFilter();
		assert sourceFilter != null;

		Filter usingFilter = params.getUsingFilter();
		assert usingFilter != null;

		FeatureCollection<SimpleFeatureType, SimpleFeature> usingFeatures, sourceFeatures;

		try {

			sourceFeatures = LayerUtil.getSelectedFeatures(sourceLayer, sourceFilter);
			assert sourceFeatures != null;

			usingFeatures = LayerUtil.getSelectedFeatures(this.usingLayer, usingFilter);
			assert usingFeatures != null;

			SimpleFeatureStore targetStore = getTargetStore(this.params, progress);

			final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(getMap());
			final CoordinateReferenceSystem usingCrs = params.getUsingCRS();
			final CoordinateReferenceSystem sourceCrs = params.getSourceCRS();
			final String sourceName = sourceLayer.getName();
			final String targetLayerName = targetLayer.getName();

			IHoleTask task = SpatialOperationFactory.createHole(sourceFeatures, usingFeatures, targetStore, mapCrs,
						targetCrs, sourceCrs, usingCrs, isCreatingNewLayer, sourceName, targetLayerName);

			ExecutorService executor = Executors.newCachedThreadPool();

			this.future = executor.submit(task);

			progress.worked(1);
		} catch (IOException e) {
			throw makeInitializeExcepion(progress, e);
		}
	}

	private SimpleFeatureStore getTargetStore(	IHoleParameters params,
																			IProgressMonitor progress)
		throws IOException {

		SimpleFeatureStore targetStore;

		if (params instanceof IHoleInExistentLayer) {

			targetLayer = ((IHoleInExistentLayer) params).getTargetLayer();

			targetStore = getFeatureStore(targetLayer);

			this.isCreatingNewLayer = false;
			targetCrs = ((IHoleInExistentLayer) params).getTargetCRS();
		} else {

			SimpleFeatureType type = ((IHoleInNewLayer) params).getTargetFeatureType();

			IGeoResource targetGeoResource = AppGISMediator.createTempGeoResource(type);
			assert targetGeoResource != null;

			targetStore = (SimpleFeatureStore) targetGeoResource.resolve(FeatureStore.class, progress);

			this.targetLayer = addLayerToMap(getMap(), targetGeoResource);

			this.isCreatingNewLayer = true;
			targetCrs = LayerUtil.getCrs(targetLayer);
		}

		assert this.targetLayer != null;
		assert LayerUtil.getCrs(this.targetLayer) != null;

		assert targetStore != null;

		return targetStore;
	}

}
