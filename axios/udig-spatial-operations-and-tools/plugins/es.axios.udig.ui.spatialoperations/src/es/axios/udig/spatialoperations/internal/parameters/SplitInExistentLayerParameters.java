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
 * Parameters for Split Operation
 * <p>
 * Used this object to create the Split in existent layer
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
final class SplitInExistentLayerParameters extends AbstractSplitParameters implements ISplitInExistentLayerParameters {

	private ILayer						targetLayer	= null;
	private CoordinateReferenceSystem	targetCRS	= null;

	public SplitInExistentLayerParameters(	final ILayer firstLayer,
											final CoordinateReferenceSystem firstCRS,
											final ILayer secondLayer,
											final CoordinateReferenceSystem secondCRS,
											final ILayer targetLayer,
											final CoordinateReferenceSystem targetCRS,
											final Filter filterInFirstLayer,
											final Filter filterInSecondLayer) {

		super(firstLayer, firstCRS, secondLayer, secondCRS, filterInFirstLayer, filterInSecondLayer);

		assert targetLayer != null;
		assert targetCRS != null;

		this.targetLayer = targetLayer;
		this.targetCRS = targetCRS;
	}

	/**
	 * @return Returns the targetLayer.
	 */
	public ILayer getTargetLayer() {

		return targetLayer;
	}

	/**
	 * @return Return the target layer CRS
	 */
	public CoordinateReferenceSystem getTargetCRS() {
		return targetCRS;
	}

}
