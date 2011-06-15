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

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.tasks.IBufferTask.CapStyle;

/**
 * Parameters to create buffer in existent layer
 * <p>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
final class BufferInExistentLayer extends AbstractBufferParameters implements IBufferInExistentLayerParameters {

	private ILayer						targetLayer	= null;
	private CoordinateReferenceSystem	targetCRS	= null;

	/**
	 * New instance of BufferInExistentLayer
	 * 
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param targetLayer
	 * @param targetCRS
	 * @param mergeGeometry
	 * @param width
	 * @param quadrantSegments
	 * @param unit
	 */
	public BufferInExistentLayer(	final ILayer sourceLayer,
									final CoordinateReferenceSystem sourceCRS,
									final ILayer targetLayer,
									final CoordinateReferenceSystem targetCRS,
									final Boolean mergeGeometry,
									final Double width,
									final Integer quadrantSegments,
									final Unit<?> unit,
									final Filter filter,
									final CapStyle endCapStyle) {

		super(sourceLayer, sourceCRS, filter, mergeGeometry, width, unit, quadrantSegments, endCapStyle);

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
	 * @return Returns the target CRS.
	 */
	public CoordinateReferenceSystem getTargetCRS() {

		return this.targetCRS;
	}
}
