/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2010, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under General Public License (GPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package es.axios.udig.sextante.task;

import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.geotools.GTOutputFactory;
import es.unex.sextante.geotools.GTVectorLayer;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.vectorTools.polylinesToPolygons.PolylinesToPolygonsAlgorithm;

/**
 * This task executes an algorithm from sextante suit.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2
 */
public final class LineToPolygonTask {

	private final FeatureStore<SimpleFeatureType, SimpleFeature>	sourceStore;
	private FeatureStore<SimpleFeatureType, SimpleFeature>			targetStore	= null;

	public LineToPolygonTask(final FeatureStore<SimpleFeatureType, SimpleFeature> sourceStore) {

		assert sourceStore != null : "Illegal argument. Expects sourceStore != null";

		this.sourceStore = sourceStore;
	}

	public void run() throws GeoAlgorithmExecutionException, IOException {

		PolylinesToPolygonsAlgorithm alg = new PolylinesToPolygonsAlgorithm();

		// set the inputs.
		DataStore ds = (DataStore) sourceStore.getDataStore();
		GTVectorLayer layer = GTVectorLayer.createLayer(ds, ds.getNames().get(0).getLocalPart());

		ParametersSet params = alg.getParameters();
		params.getParameter(PolylinesToPolygonsAlgorithm.LAYER).setParameterValue(layer);

		// set the outputs.
		OutputFactory outputFactory = new GTOutputFactory();

		OutputObjectsSet outputs = alg.getOutputObjects();
		Output contours = outputs.getOutput(PolylinesToPolygonsAlgorithm.RESULT);

		alg.execute(null, outputFactory);
		IVectorLayer result = (IVectorLayer) contours.getOutputObject();
		targetStore = (FeatureStore) result.getBaseDataObject();
	}

	public FeatureStore<SimpleFeatureType, SimpleFeature> getResult() {
		return this.targetStore;
	}

}
