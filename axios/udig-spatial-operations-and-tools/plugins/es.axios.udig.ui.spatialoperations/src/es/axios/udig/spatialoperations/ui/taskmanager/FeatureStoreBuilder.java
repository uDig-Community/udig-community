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
import java.util.Collections;
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeatureType;

import es.axios.udig.spatialoperations.tasks.SpatialOperationException;
import es.axios.udig.ui.commons.mediator.AppGISMediator;

/**
 * Builds a Feature Store and the layer associated.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
class FeatureStoreBuilder {

	private ILayer				targetLayer;
	private SimpleFeatureStore	featureStore;

	/**
	 * Creates the target {@link FeatureStore} which will be returned and the
	 * new {@link ILayer} that will be added in the current map.
	 * 
	 * @param map
	 * @param featureType
	 * @param monitor
	 * 
	 * @return a new FetureStore
	 * @throws SpatialOperationException
	 * 
	 */
	public void buildFeatureStore(final IMap map, final SimpleFeatureType featureType, final IProgressMonitor monitor)
		throws SpatialOperationException {

		IGeoResource geoResource = AppGISMediator.createTempGeoResource(featureType);
		assert geoResource != null;

		try {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) geoResource.resolve(FeatureStore.class, monitor);
			assert featureStore != null;

			this.targetLayer = addLayerToMap(map, geoResource);

			this.featureStore = featureStore;

		} catch (IOException e) {

			final String msg = e.getMessage();

			throw new SpatialOperationException(msg);
		}
	}

	public SimpleFeatureStore getFeatureStore() {

		assert this.featureStore != null;

		return this.featureStore;
	}

	/**
	 * Return the target layer
	 * 
	 * @return
	 */
	public ILayer getTargetLayer() {

		assert this.targetLayer != null;

		return targetLayer;
	}

	/**
	 * Adds a new layer to map using the georesource of the feature store
	 * 
	 * @param map
	 * @param geoResource
	 * 
	 * @return the new layer
	 */
	private ILayer addLayerToMap(IMap map, IGeoResource geoResource) {

		int index = map.getMapLayers().size();
		List<? extends ILayer> listLayer = AppGISMediator.addLayersToMap(map, Collections.singletonList(geoResource),
					index);

		assert listLayer.size() == 1; // creates only one layer

		ILayer layer = listLayer.get(0);

		return layer;
	}

}
