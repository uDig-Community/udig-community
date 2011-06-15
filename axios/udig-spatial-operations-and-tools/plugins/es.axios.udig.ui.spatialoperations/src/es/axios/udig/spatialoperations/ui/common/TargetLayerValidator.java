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
package es.axios.udig.spatialoperations.ui.common;

import net.refractions.udig.project.ILayer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Check if the target layer geometry is a geometry collection.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
public class TargetLayerValidator extends AbstractTargetLayerValidator {

	/**
	 * Target must be GeometryCollction
	 * 
	 * @param secondLayerGeom
	 * @param targetGeometry
	 * @return true if the target geometry is MultiPoint, MultiLine, or
	 *         MultiPolygon
	 */
	@Override
	public boolean validGeometryCollectionCompatible() {

		assert targetGeometryClass != null;

		boolean targetOK = targetGeometryClass.getSuperclass().equals(GeometryCollection.class)
					|| targetGeometryClass.equals(Geometry.class);
		if (!targetOK) {
			this.message = new InfoMessage(Messages.TargetLayerValidator_target_collection, InfoMessage.Type.ERROR);
		}
		return targetOK;
	}

	@Override
	public boolean validGeometryCompatible(ILayer sourceLayer2) {
		throw new UnsupportedOperationException("null implementation!");
	}
}
