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

import net.refractions.udig.project.ILayer;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Common parameter for intersect operation
 * <p>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
abstract class AbstractIntersectParameters implements IIntersectParameters {

	private ILayer						firstLayer			= null;
	private CoordinateReferenceSystem	firstCRS			= null;
	private ILayer						secondLayer			= null;
	private CoordinateReferenceSystem	secondCRS			= null;
	private Filter						filterInFirstLayer	= null;
	private Filter						filterInSecondLayer	= null;

	/**
	 * Initialization of common parameters
	 * 
	 * @param firstLayer
	 * @param firstCRS
	 * @param featuresInFirstLayer
	 * @param secondLayer
	 * @param secondCRS
	 * @param featuresInSecondLayer
	 * @param filterInSecondLayer
	 * @param filterInFirstLayer
	 */
	public AbstractIntersectParameters(	final ILayer firstLayer,
										final CoordinateReferenceSystem firstCRS,
										final ILayer secondLayer,
										final CoordinateReferenceSystem secondCRS,
										final Filter filterInFirstLayer,
										final Filter filterInSecondLayer) {

		assert firstLayer != null;
		assert firstCRS != null;
		assert secondLayer != null;
		assert secondCRS != null;
		assert filterInFirstLayer != null;
		assert filterInSecondLayer != null;

		this.firstLayer = firstLayer;
		this.firstCRS = firstCRS;
		this.secondLayer = secondLayer;
		this.secondCRS = secondCRS;
		this.filterInFirstLayer = filterInFirstLayer;
		this.filterInSecondLayer = filterInSecondLayer;

	}

	/**
	 * @return Returns the firstLayer.
	 */
	public ILayer getFirstLayer() {
		return firstLayer;
	}
	
	/**
	 * @return Returns the first layer CRS.
	 */
	public CoordinateReferenceSystem getFirstCRS() {
		return firstCRS;
	}

	/**
	 * @return Returns the secondLayer.
	 */
	public ILayer getSecondLayer() {
		return secondLayer;
	}
	
	/**
	 * @return Returns the second layer CRS.
	 */
	public CoordinateReferenceSystem getSecondCRS() {
		return secondCRS;
	}

	/**
	 * @return Returns the FilterInFirstLayer.
	 */
	public Filter getFilterInFirstLayer() {
		return filterInFirstLayer;
	}

	/**
	 * @return Returns the FilterInSecondLayer.
	 */
	public Filter getFilterInSecondLayer() {
		return filterInSecondLayer;
	}

}
