/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
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
 * Parameters for Intersect Operation
 * <p>
 * Used this object to create the intersection in an existent layer
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
final class IntersectInExistenLayerParameters extends AbstractIntersectParameters implements
			IIntersectInExistentLayerParameters {

	private ILayer						targetLayer	= null;
	private CoordinateReferenceSystem	targetCRS	= null;

	public IntersectInExistenLayerParameters(	final ILayer firstLayer,
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
	 * @return Returns the target layer CRS.
	 */
	public CoordinateReferenceSystem getTargetCRS() {
		return targetCRS;
	}
}
