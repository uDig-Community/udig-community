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

import java.util.List;

import net.refractions.udig.project.ILayer;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since
 */
final class DissolveInExistentLayerParameters extends AbstractDissolveParameters implements
			IDissolveInExistentLayerParameters {

	private ILayer						targetLayer	= null;
	private CoordinateReferenceSystem	targetCRS	= null;

	public DissolveInExistentLayerParameters(	final ILayer sourceLayer,
												final CoordinateReferenceSystem sourceCRS,
												final Filter filter,
												final List<String> list,
												final CoordinateReferenceSystem mapCrs,
												final ILayer targetLayer,
												final CoordinateReferenceSystem targetCRS) {

		super(sourceLayer, sourceCRS, filter, list, mapCrs);

		assert targetLayer != null;
		assert targetCRS != null;

		this.targetLayer = targetLayer;
		this.targetCRS = targetCRS;
	}

	/**
	 * @return the targetLayer
	 */
	public ILayer getTargetLayer() {
		return targetLayer;
	}

	/**
	 * @return the targetCRS
	 */
	public CoordinateReferenceSystem getTargetCRS() {
		return targetCRS;
	}

}
