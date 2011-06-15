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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.ui.ProgressManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.geotools.util.FeatureUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IDissolveInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IDissolveInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IDissolveParameters;
import es.axios.udig.spatialoperations.tasks.IDissolveTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationException;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;

/**
 * Monitoring the Dissolve operation.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
final class DissolveMonitor extends SOTaskMonitor<FeatureStore<SimpleFeatureType, SimpleFeature>> {

	private static final Logger										LOGGER			= Logger
																								.getLogger(DissolveMonitor.class
																											.getName());

	private IDissolveParameters										params;

	private ILayer													targetLayer;

	private Future<FeatureStore<SimpleFeatureType, SimpleFeature>>	futureDissolve;

	private FeatureStoreBuilder										storeBuilder	= new FeatureStoreBuilder();

	/**
	 * Use the method factory {@link #newInstance(IDissolveParameters)}
	 */
	private DissolveMonitor(final IDissolveParameters params) {

		this.params = params;
	}

	/**
	 * New instance of {@link DissolveMonitor}
	 * 
	 * @param params
	 * @return a new {@link DissolveMonitor}
	 */
	public static ISOTaskMonitor newInstance(final IDissolveParameters params) {

		return new DissolveMonitor(params);
	}

	/**
	 * Initializes the dissolve process.
	 * 
	 * @throws Exception
	 */
	@Override
	protected void initTaskMonitor(IProgressMonitor progress) throws InvocationTargetException {

		try {
			ILayer sourceLayer = this.params.getSourceLayer();
			CoordinateReferenceSystem mapCrs = this.params.getMapCrs();
			CoordinateReferenceSystem sourceCrs = this.params.getSourceCRS();
			progress.worked(3);
			// prepares the target store, if new layer is required the store
			// will be created
			SimpleFeatureStore targetStore;
			SimpleFeatureType disolveFeatureType;
			CoordinateReferenceSystem targetCrs;
			if (this.params instanceof IDissolveInExistentLayerParameters) {

				targetLayer = ((IDissolveInExistentLayerParameters) this.params).getTargetLayer();
				// gets the store from target layer
				targetStore = getFeatureStore(targetLayer);
				targetCrs = ((IDissolveInExistentLayerParameters) this.params).getTargetCRS();

			} else {
				// creates a new target layer and the target FeatureStore
				IDissolveInNewLayerParameters p = (IDissolveInNewLayerParameters) this.params;

				// creates the dissolve type for the new target store
				disolveFeatureType = createDissolveFeatureType(p.getTargetLayerName(), sourceLayer.getSchema(), p
							.getPropDissolve(), p.getTargetGeomClass(), mapCrs);
				IMap map = sourceLayer.getMap();

				this.storeBuilder.buildFeatureStore(map, disolveFeatureType, progress);

				targetStore = this.storeBuilder.getFeatureStore();

				this.targetLayer = this.storeBuilder.getTargetLayer();
				targetCrs = targetLayer.getCRS();

			}
			progress.worked(1);

			List<String> dissolveProperty = this.params.getPropDissolve();

			Filter filter = this.params.getFilter();

			SimpleFeatureSource featureSource = (SimpleFeatureSource) sourceLayer.getResource(
						FeatureSource.class, ProgressManager.instance().get());

			IDissolveTask task = SpatialOperationFactory.createDissolve(featureSource, filter, dissolveProperty,
						mapCrs, targetStore, sourceCrs, targetCrs);

			ExecutorService executor = Executors.newCachedThreadPool();
			this.futureDissolve = executor.submit(task);

			progress.worked(1);

			LOGGER.finest("the dissolve task was initialized correctly"); //$NON-NLS-1$

		} catch (Exception e) {
			throw makeInitializeExcepion(progress, e);
		}

	}

	/**
	 * Creates the dissolve type for the new target store
	 * 
	 * @param targetLayerName
	 * @param schema
	 * @param list
	 * @param targetGeomClass
	 * @param crs
	 * @return the new FeatureType
	 * @throws SpatialOperationException
	 */
	private SimpleFeatureType createDissolveFeatureType(String targetLayerName,
														SimpleFeatureType sourceFeatureType,
														List<String> list,
														Class<? extends Geometry> targetGeomClass,
														CoordinateReferenceSystem crs) throws SpatialOperationException {

		SimpleFeatureTypeBuilder ftBuilder = FeatureUtil
					.createDefaultFeatureType(targetLayerName, crs, targetGeomClass);

		for (String property : list) {
			// creates the dissolve property
			int position = sourceFeatureType.indexOf(property);
			AttributeDescriptor dissolveAttr = sourceFeatureType.getDescriptor(position);

			AttributeTypeBuilder build = new AttributeTypeBuilder();
			build.setName(dissolveAttr.getLocalName());
			build.setBinding(dissolveAttr.getType().getBinding());
			build.setNillable(dissolveAttr.isNillable());

			dissolveAttr = build.buildDescriptor(dissolveAttr.getLocalName());

			ftBuilder.add(dissolveAttr);

		}

		SimpleFeatureType dissolveType;
		dissolveType = ftBuilder.buildFeatureType();

		return dissolveType;

	}

	@Override
	protected final String getBeginMessage() {
		final String msg = MessageFormat.format(Messages.DissolveMonitor_dissolve_using, params.getSourceLayer()
					.getName(), params.getPropDissolve());
		return msg;
	}

	@Override
	protected final String getCancelMessage() {
		return Messages.DissolveMonitor_canceled;
	}

	@Override
	protected final String getDoneMessage() {
		return Messages.DissolveMonitor_successful;
	}

	@Override
	protected IMap getMap() {
		return this.params.getSourceLayer().getMap();
	}

	@Override
	protected ILayer getTarget() {
		return this.targetLayer;
	}

	@Override
	protected Future<FeatureStore<SimpleFeatureType, SimpleFeature>> getFuture() {

		assert this.futureDissolve != null;

		return this.futureDissolve;
	}

	/**
	 * Prepares the dissolve result
	 */
	@Override
	protected void deliveryResult(Future<FeatureStore<SimpleFeatureType, SimpleFeature>> future, ILayer target) {
		presentFeaturesOnTargetLayer(target);

	}
}
