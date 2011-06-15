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
package es.axios.udig.spatialoperations.tasks;

import java.util.concurrent.Callable;

import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * Interface for buffer task
 * 
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
public interface IBufferTask extends Callable<FeatureStore<SimpleFeatureType, SimpleFeature>>  {

	public static enum CapStyle {

			//capButt(BufferParameters.CAP_FLAT),
			capFlat(BufferParameters.CAP_FLAT),
			capRound(BufferParameters.CAP_ROUND),
			capSquare(BufferParameters.CAP_SQUARE);

		private final int	value;

		CapStyle(final int v) {
			value = v;
		}

		public int value() {
			return value;
		}
	};

	public SimpleFeatureStore call() throws Exception;

}
