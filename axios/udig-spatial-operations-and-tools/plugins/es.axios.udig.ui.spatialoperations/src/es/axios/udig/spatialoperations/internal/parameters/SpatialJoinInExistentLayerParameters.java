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
package es.axios.udig.spatialoperations.internal.parameters;

/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
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

import net.refractions.udig.project.ILayer;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.tasks.SpatialRelation;

/**
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
final class SpatialJoinInExistentLayerParameters extends AbstractSpatialJoinParameters implements
			ISpatialJoinInExistentLayerParameters {

	private ILayer						targetLayer	= null;
	private CoordinateReferenceSystem	targetCRS	= null;

	public SpatialJoinInExistentLayerParameters(final ILayer firstLayer,
												final CoordinateReferenceSystem firstCRS,
												final ILayer secondLayer,
												final CoordinateReferenceSystem secondCRS,
												final SpatialRelation spatialRelation,
												final CoordinateReferenceSystem mapCrs,
												final ILayer targetLayer,
												final CoordinateReferenceSystem targetCRS,
												final Filter filterInFirstLayer,
												final Filter filterInSecondLayer,
												final Boolean selection) {

		super(firstLayer, firstCRS, secondLayer, secondCRS, spatialRelation, mapCrs, filterInFirstLayer,
					filterInSecondLayer, selection);

		assert targetLayer != null;
		assert targetCRS != null;

		this.targetLayer = targetLayer;
		this.targetCRS = targetCRS;
	}

	public ILayer getTargetLayer() {
		return this.targetLayer;
	}

	public CoordinateReferenceSystem getTargetCRS() {
		return this.targetCRS;
	}

}
