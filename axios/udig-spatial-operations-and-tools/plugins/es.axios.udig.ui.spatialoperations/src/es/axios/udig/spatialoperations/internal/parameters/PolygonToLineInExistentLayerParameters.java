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
package es.axios.udig.spatialoperations.internal.parameters;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.refractions.udig.project.ILayer;

/**
 * PolygonToLine operation parameters.
 * <p>
 * Required parameters to do PolygonToLine operation in existent layer.
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
final class PolygonToLineInExistentLayerParameters extends AbstractPolygonToLineParameters implements
			IPolygonToLineInExistentLayerParameters {

	private ILayer						targetLayer;
	private CoordinateReferenceSystem	targetCRS;

	public PolygonToLineInExistentLayerParameters(	final Filter filter,
													final CoordinateReferenceSystem mapCrs,
													final ILayer sourceLayer,
													final CoordinateReferenceSystem sourceCRS,
													final ILayer targetLayer,
													final CoordinateReferenceSystem targetCRS,
													final Boolean explode) {

		super(filter, mapCrs, sourceLayer, sourceCRS, explode);

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
	 * @return the target layer CRS
	 */
	public CoordinateReferenceSystem getTargetCRS() {
		return targetCRS;
	}

}
