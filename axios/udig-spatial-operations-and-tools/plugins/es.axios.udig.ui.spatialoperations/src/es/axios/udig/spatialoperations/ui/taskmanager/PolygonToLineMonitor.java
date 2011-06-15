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

import com.vividsolutions.jts.geom.Geometry;

import es.axios.geotools.util.FeatureUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IPolygonToLineInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IPolygonToLineInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IPolygonToLineParameters;
import es.axios.udig.spatialoperations.tasks.IPolygonToLineTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.LayerUtil;

/**
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
final class PolygonToLineMonitor extends SOTaskMonitor<FeatureStore<SimpleFeatureType, SimpleFeature>> {

	private Future<FeatureStore<SimpleFeatureType, SimpleFeature>>	future;
	private IPolygonToLineParameters								params;
	private ILayer													sourceLayer	= null;
	private ILayer													targetLayer	= null;
	private CoordinateReferenceSystem								mapCrs		= null;

	// private String newAttributeName = null;

	/**
	 * A new instance of {@link PolygonToLineMonitor}. Client must use
	 * {@link #newInstance(IPolygonToLineParameters)}.
	 * 
	 * @param params
	 */
	private PolygonToLineMonitor(IPolygonToLineParameters params) {

		assert params != null;

		this.params = params;
	}

	/**
	 * A new instance of {@link PolygonToLineMonitor}.
	 * 
	 * @param params
	 * @return
	 */
	public static ISOTaskMonitor newInstance(IPolygonToLineParameters params) {

		return new PolygonToLineMonitor(params);
	}

	@Override
	protected void deliveryResult(Future<FeatureStore<SimpleFeatureType, SimpleFeature>> future, ILayer target)
		throws InterruptedException {

		presentFeaturesOnTargetLayer(target);
	}

	@Override
	protected String getBeginMessage() {

		final String msg = MessageFormat.format(Messages.PolygonToLayerBegin, this.sourceLayer.getName());

		return msg;
	}

	@Override
	protected String getCancelMessage() {

		return Messages.PolygonToLayerCanceled;
	}

	@Override
	protected String getDoneMessage() {

		return Messages.PolygonToLayerSuccessful;
	}

	@Override
	protected Future<FeatureStore<SimpleFeatureType, SimpleFeature>> getFuture() {

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

	@Override
	protected void initTaskMonitor(IProgressMonitor progress) throws InvocationTargetException {

		try {
			this.sourceLayer = params.getSourceLayer();
			this.mapCrs = this.params.getMapCrs();

			final CoordinateReferenceSystem sourceLayerCrs = params.getSourceCRS();
			final Boolean explode = params.getExplode();

			Filter filter = this.params.getFilter();

			if (Filter.EXCLUDE.equals(filter)) {
				filter = Filter.INCLUDE;
			}

			FeatureCollection<SimpleFeatureType, SimpleFeature> featuresFromSource = LayerUtil.getSelectedFeatures(
						sourceLayer, filter);
			SimpleFeatureStore targetStore =  getTargetStore(params, progress);

			IPolygonToLineTask task = SpatialOperationFactory.createPolygonToLine(targetStore, featuresFromSource,
						sourceLayerCrs, explode);

			ExecutorService executor = Executors.newCachedThreadPool();

			this.future = executor.submit(task);

			progress.worked(1);
		} catch (Exception e) {
			throw makeInitializeExcepion(progress, e);
		}
	}

	/**
	 * Returns the associated {@link FeatureStore} to the targetLayer or a new
	 * {@link FeatureStore} associated to a new target layer will be created in
	 * the map
	 */
	private SimpleFeatureStore getTargetStore(	IPolygonToLineParameters params,
																			IProgressMonitor progress) throws Exception {

		SimpleFeatureStore targetStore = null;

		if (params instanceof IPolygonToLineInExistentLayerParameters) {

			this.targetLayer = ((IPolygonToLineInExistentLayerParameters) params).getTargetLayer();
			// gets the store from target layer
			targetStore = getFeatureStore(targetLayer);

		} else {

			Class<? extends Geometry> targetGeometryClass = ((IPolygonToLineInNewLayerParameters) params)
						.getTargetGeometryClass();
			String targetLayerName = ((IPolygonToLineInNewLayerParameters) params).getTargetName();

			SimpleFeatureType type = FeatureUtil.createFeatureType(this.sourceLayer.getSchema(), targetLayerName,
						mapCrs, targetGeometryClass);

			// TODO only used if called from Transformation View, its old, so
			// now don't use it.
			// this.newAttributeName = ((IPolygonToLineInNewLayerParameters)
			// this.params).getNewAttributeName();
			//
			// // By default the attribute will be of the String class.
			// type = FeatureUtil.addAttributeToFeatureType(type,
			// newAttributeName, String.class);

			IGeoResource targetGeoResource = AppGISMediator.createTempGeoResource(type);
			assert targetGeoResource != null;

			targetStore = (SimpleFeatureStore) targetGeoResource.resolve(FeatureStore.class, progress);

			this.targetLayer = addLayerToMap(getMap(), targetGeoResource);
		}

		assert targetStore != null;

		return targetStore;
	}

}
