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
package es.axios.udig.spatialoperations.internal.ui.processconnectors;

import net.refractions.udig.project.ILayer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.common.AbstractTargetLayerValidator;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.util.LayerUtil;

/**
 * Validate if the source has simple geometry, then the target must have simple
 * geometry. If the source has a geometry collection, the target also must be a
 * geometry collection layer type.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
class PolygonToLineTargetValidator extends AbstractTargetLayerValidator {

	/**
	 * Check if the source layer and target layer are compatible, this means
	 * that: The source layer has simple geometry, then the target layer must
	 * have simple geometry and the same is applied if the source layer is a
	 * geometry collection.
	 * 
	 * @return
	 */
	@Override
	public boolean validGeometryCollectionCompatible() {

		if(this.sourceLayer == null){
			return false;
		}
		Class<? extends Geometry> sourceGeom = LayerUtil.getGeometryClass(this.sourceLayer);
		boolean targetOK = true;
		if (this.targetGeometryClass.equals(Geometry.class)) {
			return true; // Geometry as target is compatible with all possible source classes
		}
		
		if (sourceGeom.getSuperclass().equals(GeometryCollection.class)) {
			// is a geometry collection (multi -polygon/line/point)
			if (!targetGeometryClass.getSuperclass().equals(GeometryCollection.class)) {
				targetOK = false;
				this.message = new InfoMessage(Messages.PolygonToLineTargetValidator_target_geometry_collection,
							InfoMessage.Type.ERROR);
			}
		} else { // is simple

			if (targetGeometryClass.getSuperclass().equals(GeometryCollection.class)) {
				targetOK = false;
				this.message = new InfoMessage(Messages.PolygonToLineTargetValidator_target_geometry_simple,
							InfoMessage.Type.ERROR);
			}
		}
		return targetOK;
	}

	@Override
	public boolean validGeometryCompatible(ILayer sourceLayer2) {
		throw new UnsupportedOperationException("null implementation!");
	}

}
