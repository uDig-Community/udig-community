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
 * Parameters for Fill Operation
 * <p>
 * Used this object to create the Fill in existent layer
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
final class FillInExistentLayerParameters extends AbstractFillParameters implements IFillInExistentLayerParameters {

	private ILayer						targetLayer	= null;
	private CoordinateReferenceSystem	targetCRS	= null;

	/**
	 * New instance of FillInExistentLayerParameters
	 * 
	 * @param firstLayer
	 * @param firstLayerCRS
	 * @param secondLayer
	 * @param secondLayerCRS
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 * @param targetLayer
	 * @param targetCRS
	 */
	public FillInExistentLayerParameters(	final ILayer firstLayer,
											final CoordinateReferenceSystem firstLayerCRS,
											final ILayer secondLayer,
											final CoordinateReferenceSystem secondLayerCRS,
											final Filter filterInFirstLayer,
											final Filter filterInSecondLayer,
											final ILayer targetLayer,
											final CoordinateReferenceSystem targetCRS,
											final boolean copySourceFeatures) {

		super(firstLayer, firstLayerCRS, secondLayer, secondLayerCRS, filterInFirstLayer, filterInSecondLayer, copySourceFeatures);

		assert targetLayer != null;
		assert targetCRS != null;

		this.targetLayer = targetLayer;
		this.targetCRS = targetCRS;
	}

	public ILayer getTargetLayer() {

		return targetLayer;
	}

	public CoordinateReferenceSystem getTargetCRS() {
		return targetCRS;
	}
}
