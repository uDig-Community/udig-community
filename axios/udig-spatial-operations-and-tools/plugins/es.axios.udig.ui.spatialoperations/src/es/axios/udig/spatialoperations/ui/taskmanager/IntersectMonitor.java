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

import es.axios.geotools.util.FeatureTypeUnionBuilder;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectParameters;
import es.axios.udig.spatialoperations.tasks.IIntersectTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationException;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Monitoring the intersect operation.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
final class IntersectMonitor extends SOTaskMonitor<FeatureStore<SimpleFeatureType, SimpleFeature>> {

	private Future<FeatureStore<SimpleFeatureType, SimpleFeature>>	future;
	private IIntersectParameters									params;
	private ILayer													targetLayer	= null;
	private ILayer													secondLayer	= null;
	private ILayer													firstLayer	= null;
	private boolean													isCreatingNewLayer;
	private FeatureTypeUnionBuilder									featureUnionBuilder;

	/**
	 * New instance of {@link IntersectMonitor}. Client must use
	 * {@link #newInstance(IIntersectParameters)}.
	 * 
	 * @param params
	 */
	private IntersectMonitor(final IIntersectParameters params) {

		assert params != null;

		this.params = params;
	}

	/**
	 * New instance of {@link IntersectMonitor}.
	 * 
	 * @param params
	 * @return
	 */
	public static ISOTaskMonitor newInstance(IIntersectParameters params) {

		return new IntersectMonitor(params);
	}

	@Override
	protected void deliveryResult(Future<FeatureStore<SimpleFeatureType, SimpleFeature>> future, ILayer target)
		throws InterruptedException {

		presentFeaturesOnTargetLayer(target);
	}

	@Override
	protected String getBeginMessage() {

		final String msg = MessageFormat.format(Messages.IntersectProcess_intersectin_with, this.firstLayer.getName(),
					this.secondLayer.getName());
		return msg;
	}

	@Override
	protected String getCancelMessage() {

		return Messages.IntersectProcess_canceled;
	}

	@Override
	protected String getDoneMessage() {

		return Messages.IntersectProcess_successful;
	}

	@Override
	protected Future<FeatureStore<SimpleFeatureType, SimpleFeature>> getFuture() {

		return this.future;
	}

	@Override
	protected IMap getMap() {

		IMap map = this.firstLayer.getMap();
		assert map != null;
		return map;
	}

	@Override
	protected ILayer getTarget() {

		return this.targetLayer;
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

			// Traverses the first layer doing the intersection of each feature
			// in first layer
			// with each feature in second layer.

			// gets the crs of map
			final CoordinateReferenceSystem firstLayerCrs = params.getFirstCRS();
			final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(getMap());
			final CoordinateReferenceSystem secondLayerCrs = params.getSecondCRS();

			IIntersectTask task = SpatialOperationFactory.createIntersect(targetStore, featuresInFirstLayer,
						featuresInSecondLayer, firstLayerCrs, mapCrs, isCreatingNewLayer, featureUnionBuilder,
						secondLayerCrs);

			ExecutorService executor = Executors.newCachedThreadPool();

			this.future = executor.submit(task);

			progress.worked(1);
		} catch (IOException e) {
			throw makeInitializeExcepion(progress, e);
		} catch (SpatialOperationException e) {
			throw makeInitializeExcepion(progress, e);
		}
	}

	private SimpleFeatureStore getTargetStore(	IIntersectParameters params,
																			IProgressMonitor progress)
		throws IOException, SpatialOperationException {

		SimpleFeatureStore targetStore;

		if (params instanceof IIntersectInExistentLayerParameters) {

			this.targetLayer = ((IIntersectInExistentLayerParameters) params).getTargetLayer();

			targetStore = getFeatureStore(targetLayer);

			this.isCreatingNewLayer = false;
		} else {

			final String targetName = ((IIntersectInNewLayerParameters) params).getTargetName();
			final Class<? extends Geometry> geomClass = ((IIntersectInNewLayerParameters) params)
						.getTargetGeometryClass();
			final CoordinateReferenceSystem crs = MapUtil.getCRS(getMap());
			SimpleFeatureType type = buildTargetFeatureType(this.firstLayer.getSchema(), this.secondLayer.getSchema(),
						targetName, geomClass, crs);

			IGeoResource targetGeoResource = AppGISMediator.createTempGeoResource(type);
			assert targetGeoResource != null;

			targetStore = (SimpleFeatureStore) targetGeoResource.resolve(FeatureStore.class, progress);

			this.targetLayer = addLayerToMap(getMap(), targetGeoResource);

			this.isCreatingNewLayer = true;
		}

		return targetStore;
	}

	private SimpleFeatureType buildTargetFeatureType(	final SimpleFeatureType firstType,
														final SimpleFeatureType secondType,
														final String layerName,
														final Class<? extends Geometry> geometryClass,
														final CoordinateReferenceSystem crs)
		throws SpatialOperationException {

		try {

			this.featureUnionBuilder = new FeatureTypeUnionBuilder(layerName);

			featureUnionBuilder.add(firstType).add(secondType).setGeometryClass("geometry", geometryClass, crs); //$NON-NLS-1$
			SimpleFeatureType newFeatureType = featureUnionBuilder.getFeatureType();

			return newFeatureType;

		} catch (SchemaException e) {

			throw new SpatialOperationException(e);
		}
	}
}
