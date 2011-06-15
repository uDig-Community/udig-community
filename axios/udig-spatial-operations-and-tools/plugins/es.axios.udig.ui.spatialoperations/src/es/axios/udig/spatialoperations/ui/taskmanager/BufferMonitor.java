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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.render.IViewportModel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;

import es.axios.geotools.util.GeoToolsUtils;
import es.axios.geotools.util.UnitList;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IBufferInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IBufferInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IBufferParameters;
import es.axios.udig.spatialoperations.tasks.IBufferTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Monitoring the buffer operation.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
final class BufferMonitor extends SOTaskMonitor<FeatureStore<SimpleFeatureType, SimpleFeature>> {

	private Future<FeatureStore<SimpleFeatureType, SimpleFeature>>	future;
	private IBufferParameters			params;
	private ILayer						sourceLayer	= null;
	private CoordinateReferenceSystem	sourceCRS	= null;
	private ILayer						targetLayer	= null;
	private CoordinateReferenceSystem	targetCRS	= null;

	/**
	 * New instance of {@link BufferMonitor}. Clients must use
	 * {@link #newInstance(IBufferParameters)}.
	 * 
	 * @param params
	 */
	private BufferMonitor(final IBufferParameters params) {

		assert params != null;

		this.params = params;
	}

	public static ISOTaskMonitor newInstance(IBufferParameters params) {
		return new BufferMonitor(params);
	}

	/**
	 * sets the source feature collection to process and the target (or result)
	 * feature store
	 */
	@Override
	protected void initTaskMonitor(IProgressMonitor progress) throws InvocationTargetException {

		this.sourceLayer = params.getSourceLayer();
		assert this.sourceLayer != null;

		this.sourceCRS = params.getSourceCRS();
		assert this.sourceCRS != null;

		Filter filter = params.getFilter();
		assert filter != null;

		FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures;
		try {
			selectedFeatures = LayerUtil.getSelectedFeatures(this.sourceLayer, filter);
			assert selectedFeatures != null;

			SimpleFeatureStore targetStore = getTargetStore(selectedFeatures, this.params,
						progress);

			final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(getMap());

			final Unit<?> mapUnits = GeoToolsUtils.getDefaultCRSUnit(mapCrs);
			final Unit<?> targetUnits = params.getUnitsOfMeasure();

			double widthConverted = convertBufferWidth(params.getWidth(), mapUnits, targetUnits);

			IBufferTask task = SpatialOperationFactory.createBuffer(selectedFeatures, targetStore, mapCrs,
						widthConverted, params.getUnitsOfMeasure(), params.isMergeGeometries(), params
									.getQuadrantSegments(), params.getCapStyle(), sourceCRS, targetCRS);

			ExecutorService executor = Executors.newCachedThreadPool();

			this.future = executor.submit(task);

			progress.worked(1);

		} catch (IOException e) {
			throw makeInitializeExcepion(progress, e);
		}
	}

	/**
	 * Converts the distance <code>bufferWidth</code>
	 * from/home/mauro/workspaces-eclipse/udig-ext/udig-extensions-1.2.x-sdk-1.1
	 * .x <code>targetUnits</code> to <code>sourceUnits</code>
	 * 
	 * @param bufferWidth
	 * @param sourceUnits
	 * @param targetUnits
	 * @return
	 */
	private double convertBufferWidth(double bufferWidth, final Unit<?> sourceUnits, final Unit<?> targetUnits) {

		assert sourceUnits != null;
		assert targetUnits != null;

		double convertedWidth;
		if (UnitList.PIXEL_UNITS.equals(targetUnits)) {

			IViewportModel viewportModel = this.sourceLayer.getMap().getViewportModel();
			Coordinate origin = viewportModel.pixelToWorld(0, 0);
			int fixedBufferWidth = (int) Math.round(bufferWidth);

			Coordinate originPlusWidth = viewportModel.pixelToWorld(fixedBufferWidth, fixedBufferWidth);
			convertedWidth = Math.abs(originPlusWidth.x - origin.x);
		} else {
			UnitConverter converter = targetUnits.getConverterTo(sourceUnits);
			convertedWidth = converter.convert(bufferWidth);
		}
		return convertedWidth;
	}

	/**
	 * Returns the associated {@link FeatureStore} to the targetLayer or a new
	 * {@link FeatureStore} associated to a new target layer will be created in
	 * the map
	 */
	private SimpleFeatureStore getTargetStore(	final FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures,
																			final IBufferParameters params,
																			final IProgressMonitor progress)
		throws IOException {

		SimpleFeatureStore targetStore;

		if (params instanceof IBufferInExistentLayerParameters) {

			targetLayer = ((IBufferInExistentLayerParameters) params).getTargetLayer();
			assert this.targetLayer != null;

			targetCRS = ((IBufferInExistentLayerParameters) params).getTargetCRS();
			assert this.targetCRS != null;

			targetStore = getFeatureStore(targetLayer);

		} else { // a new layer is required IBufferInNewLayerParameters !=
			// null

			// create new layer (store and resource) with the feature type
			// required
			SimpleFeatureType featureType = ((IBufferInNewLayerParameters) params).getTargetFeatureType();

			IGeoResource targetGeoResource = AppGISMediator.createTempGeoResource(featureType);
			assert targetGeoResource != null;

			targetStore = (SimpleFeatureStore) targetGeoResource.resolve(FeatureStore.class, progress);

			this.targetLayer = addLayerToMap(getMap(), targetGeoResource);
			assert this.targetLayer != null;

			this.targetCRS = targetLayer.getCRS();
			assert this.targetCRS != null;

			// targetStore.getSchema().getDefaultGeometry().getCoordinateSystem();
		}
		assert targetStore != null;

		return targetStore;
	}

	@Override
	protected void deliveryResult(Future<FeatureStore<SimpleFeatureType, SimpleFeature>> future, ILayer target) {

		presentFeaturesOnTargetLayer(target);
	}

	@Override
	protected String getBeginMessage() {
		return Messages.BufferProcess_subtaskBufferringFeatures;
	}

	@Override
	protected String getCancelMessage() {
		return Messages.BufferProcess_canceled;
	}

	@Override
	protected String getDoneMessage() {

		return Messages.BufferMonitor_successful;
	}

	@Override
	protected Future<FeatureStore<SimpleFeatureType, SimpleFeature>>getFuture() {
		return this.future;
	}

	@Override
	protected IMap getMap() {
		IMap map = this.sourceLayer.getMap();
		assert map != null;
		return map;
	}

	@Override
	protected ILayer getTarget() {
		return this.targetLayer;
	}

}
