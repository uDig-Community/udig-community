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

import net.refractions.udig.project.ILayer;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
final class HoleInExistentLayerParameters extends AbstractHoleParameters implements IHoleInExistentLayer {

	private ILayer						targetLayer;
	private CoordinateReferenceSystem	targetCRS;

	/**
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param usingLayer
	 * @param usingCRS
	 * @param sourceFilter
	 * @param usingFilter
	 * @param targetLayer
	 * @param targetCRS
	 */
	public HoleInExistentLayerParameters(	final ILayer sourceLayer,
											final CoordinateReferenceSystem sourceCRS,
											final ILayer usingLayer,
											final CoordinateReferenceSystem usingCRS,
											final Filter sourceFilter,
											final Filter usingFilter,
											final ILayer targetLayer,
											final CoordinateReferenceSystem targetCRS) {

		super(sourceLayer, sourceCRS, usingLayer, usingCRS, sourceFilter, usingFilter);

		assert targetLayer != null;
		assert targetCRS != null;

		this.targetLayer = targetLayer;
		this.targetCRS = targetCRS;

	}

	/**
	 * @return Returns the targetLayer.
	 */
	public ILayer getTargetLayer() {
		return this.targetLayer;
	}

	/**
	 * @return Returns the target layer CRS
	 */
	public CoordinateReferenceSystem getTargetCRS() {
		return this.targetCRS;
	}
}
