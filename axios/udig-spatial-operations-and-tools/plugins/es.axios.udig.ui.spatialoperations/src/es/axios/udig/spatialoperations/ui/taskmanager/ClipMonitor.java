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
import es.axios.udig.spatialoperations.internal.parameters.IClipInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IClipInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IClipParameters;
import es.axios.udig.spatialoperations.tasks.IClipTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Monitoring the clip operation.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
final class ClipMonitor extends SOTaskMonitor<FeatureStore<SimpleFeatureType, SimpleFeature>> {

	private Future<FeatureStore<SimpleFeatureType, SimpleFeature>>	future;
	private IClipParameters											params;
	private ILayer													targetLayer		= null;
	private CoordinateReferenceSystem								targetCRS		= null;
	private ILayer													usingLayer		= null;
	private CoordinateReferenceSystem								usingCRS		= null;
	private boolean													isCreatingNewLayer;
	private ILayer													clipSourceLayer;
	private CoordinateReferenceSystem								clipSourceCRS	= null;

	/**
	 * New instance of {@link ClipMonitor}. Client must use
	 * {@link #newInstance(IClipParameters)}.
	 * 
	 * @param params
	 */
	private ClipMonitor(final IClipParameters params) {

		assert params != null;

		this.params = params;
	}

	public static ISOTaskMonitor newInstance(IClipParameters params) {

		return new ClipMonitor(params);
	}

	@Override
	protected void deliveryResult(Future<FeatureStore<SimpleFeatureType, SimpleFeature>> future, ILayer target)
		throws InterruptedException {

		presentFeaturesOnTargetLayer(target);
	}

	@Override
	protected String getBeginMessage() {

		final String msg = MessageFormat.format(Messages.ClipProcess_clipping_with, clipSourceLayer.getName(),
					usingLayer.getName());
		return msg;
	}

	@Override
	protected String getCancelMessage() {

		return Messages.ClipProcess_clip_was_canceled;
	}

	@Override
	protected String getDoneMessage() {

		return Messages.ClipProcess_successful;
	}

	@Override
	protected Future<FeatureStore<SimpleFeatureType, SimpleFeature>> getFuture() {

		return this.future;
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

		usingLayer = params.getUsingLayer();
		assert usingLayer != null;

		usingCRS = params.getUsingCRS();
		assert usingCRS != null;

		clipSourceLayer = params.getClipSourceLayer();
		assert clipSourceLayer != null;

		clipSourceCRS = params.getClipSourceCRS();
		assert clipSourceCRS != null;

		Filter usingFilter = params.getUsingFilter();
		assert usingFilter != null;

		Filter clipSourceFilter = params.getClipSourceFilter();
		assert clipSourceFilter != null;

		FeatureCollection<SimpleFeatureType, SimpleFeature> usingFeatures, clipSourceFeatures;

		try {
			usingFeatures = LayerUtil.getSelectedFeatures(this.usingLayer, usingFilter);
			assert usingFeatures != null;

			clipSourceFeatures = LayerUtil.getSelectedFeatures(clipSourceLayer, clipSourceFilter);
			assert clipSourceFeatures != null;

			SimpleFeatureStore targetStore = getTargetStore(this.params, progress);

			final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(getMap());
			final String clipSourceName = clipSourceLayer.getName();
			final String targetLayerName = targetLayer.getName();

			IClipTask task = SpatialOperationFactory.createClip(usingFeatures, clipSourceFeatures, targetStore, mapCrs,
						usingCRS, clipSourceCRS, isCreatingNewLayer, clipSourceName, targetLayerName, targetCRS);

			ExecutorService executor = Executors.newCachedThreadPool();

			this.future = executor.submit(task);

			progress.worked(1);
		} catch (IOException e) {
			throw makeInitializeExcepion(progress, e);
		}
	}

	private SimpleFeatureStore getTargetStore(	IClipParameters params,
																			IProgressMonitor progress)
		throws IOException {

		SimpleFeatureStore targetStore;

		if (params instanceof IClipInExistentLayerParameters) {

			targetLayer = ((IClipInExistentLayerParameters) params).getTargetLayer();
			assert this.targetLayer != null;
			targetCRS = ((IClipInExistentLayerParameters) params).getTargetCRS();
			assert this.targetCRS != null;

			targetStore = getFeatureStore(targetLayer);

			this.isCreatingNewLayer = false;

		} else {

			SimpleFeatureType type = ((IClipInNewLayerParameters) params).getTargetFeatureType();

			IGeoResource targetGeoResource = AppGISMediator.createTempGeoResource(type);
			assert targetGeoResource != null;

			targetStore = (SimpleFeatureStore) targetGeoResource.resolve(FeatureStore.class, progress);

			this.targetLayer = addLayerToMap(getMap(), targetGeoResource);
			assert targetLayer != null;

			this.targetCRS = targetLayer.getCRS();
			assert this.targetCRS != null;

			this.isCreatingNewLayer = true;
		}

		assert this.targetLayer != null;
		assert LayerUtil.getCrs(this.targetLayer) != null;

		assert targetStore != null;

		return targetStore;
	}
}
