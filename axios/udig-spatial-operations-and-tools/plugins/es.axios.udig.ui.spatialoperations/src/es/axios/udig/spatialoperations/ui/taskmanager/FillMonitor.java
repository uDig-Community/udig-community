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
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.geotools.util.FeatureUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IFillInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IFillInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IFillParameters;
import es.axios.udig.spatialoperations.tasks.IFillTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Monitoring the fill operation.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
final class FillMonitor extends SOTaskMonitor<FeatureStore<SimpleFeatureType, SimpleFeature>> {

	private Future<FeatureStore<SimpleFeatureType, SimpleFeature>>	future		= null;
	private IFillParameters											params		= null;
	private ILayer													targetLayer	= null;
	private ILayer													secondLayer	= null;
	private ILayer													firstLayer	= null;
	private CoordinateReferenceSystem								targetCrs	= null;

	/**
	 * New instance of {@link FillMonitor}. Clients must use
	 * {@link #newInstance(IFillParameters)}.
	 * 
	 * @param params
	 */
	private FillMonitor(final IFillParameters params) {

		assert params != null;

		this.params = params;
	}

	/**
	 * New instance of {@link FillMonitor}.
	 * 
	 * @param params
	 * @return
	 */
	public static ISOTaskMonitor newInstance(IFillParameters params) {

		return new FillMonitor(params);
	}

	@Override
	protected void deliveryResult(Future<FeatureStore<SimpleFeatureType, SimpleFeature>> future, ILayer target)
		throws InterruptedException {

		presentFeaturesOnTargetLayer(target);

	}

	@Override
	protected String getBeginMessage() {

		final String msg = MessageFormat.format(Messages.FillMonitor_filling_layer, this.firstLayer.getName(),
					this.secondLayer.getName());
		return msg;
	}

	@Override
	protected String getCancelMessage() {

		return Messages.FillMonitor_canceled;
	}

	@Override
	protected String getDoneMessage() {

		return Messages.FillMonitor_success;
	}

	@Override
	protected Future<FeatureStore<SimpleFeatureType, SimpleFeature>> getFuture() {

		return future;
	}

	@Override
	protected IMap getMap() {

		IMap map = this.firstLayer.getMap();
		assert map != null;
		return map;
	}

	@Override
	protected ILayer getTarget() {

		return targetLayer;
	}

	@Override
	protected void initTaskMonitor(IProgressMonitor progress) throws InvocationTargetException {

		this.firstLayer = params.getFirstLayer();
		assert this.firstLayer != null;

		this.secondLayer = params.getSecondLayer();
		assert this.secondLayer != null;

		Filter filterInFirstLayer = params.getFilterInFirstLayer();
		assert filterInFirstLayer != null;

		Filter filterInSecondLayer = params.getFilterInSecondLayer();
		assert filterInSecondLayer != null;

		FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer, featuresInSecondLayer;

		try {

			featuresInFirstLayer = LayerUtil.getSelectedFeatures(this.firstLayer, filterInFirstLayer);
			assert featuresInFirstLayer != null;

			featuresInSecondLayer = LayerUtil.getSelectedFeatures(this.secondLayer, filterInSecondLayer);
			assert featuresInSecondLayer != null;

			SimpleFeatureStore targetStore = getTargetStore(this.params, progress);

			// gets the crs of map
			final CoordinateReferenceSystem firstLayerCrs = params.getFirstLayerCRS();
			final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(getMap());
			final CoordinateReferenceSystem secondLayerCrs = params.getSecondLayerCRS();

			IFillTask task = SpatialOperationFactory.createFill(targetStore, featuresInFirstLayer,
						featuresInSecondLayer, firstLayerCrs, mapCrs, secondLayerCrs, targetCrs, params.isCopySourceFeatures());

			ExecutorService executor = Executors.newCachedThreadPool();

			this.future = executor.submit(task);

			progress.worked(1);

		} catch (IOException e) {
			throw makeInitializeExcepion(progress, e);
		} catch (SchemaException e) {
			throw makeInitializeExcepion(progress, e);
		}

	}

	private SimpleFeatureStore getTargetStore(	IFillParameters params,
																			IProgressMonitor progress)
		throws IOException, SchemaException {

		SimpleFeatureStore targetStore;

		if (params instanceof IFillInExistentLayerParameters) {

			this.targetLayer = ((IFillInExistentLayerParameters) params).getTargetLayer();

			targetStore = getFeatureStore(targetLayer);
			targetCrs = ((IFillInExistentLayerParameters) params).getTargetCRS();
		} else {

			final String targetName = ((IFillInNewLayerParameters) params).getTargetName();
			final Class<? extends Geometry> geomClass = ((IFillInNewLayerParameters) params).getTargetGeometryClass();
			final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(getMap());

			SimpleFeatureType type = FeatureUtil.createFeatureType(this.firstLayer.getSchema(), targetName, mapCrs,
						geomClass);

			IGeoResource targetGeoResource = AppGISMediator.createTempGeoResource(type);
			assert targetGeoResource != null;

			targetStore = (SimpleFeatureStore) targetGeoResource.resolve(FeatureStore.class, progress);

			this.targetLayer = addLayerToMap(getMap(), targetGeoResource);
			targetCrs = LayerUtil.getCrs(targetLayer);
		}

		return targetStore;
	}

}
