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
 * Common parameter for split operation
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
abstract class AbstractFillParameters implements IFillParameters {

	private ILayer						firstLayer			= null;
	private CoordinateReferenceSystem	firstLayerCRS		= null;
	private ILayer						secondLayer			= null;
	private CoordinateReferenceSystem	secondLayerCRS		= null;
	private Filter						filterInFirstLayer	= null;
	private Filter						filterInSecondLayer	= null;
	private boolean 					copySourceFeatures;

	/**
	 * Initialization of common parameters
	 * 
	 * @param firstLayer
	 * @param secondLayer
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 */
	public AbstractFillParameters(	final ILayer firstLayer,
									final CoordinateReferenceSystem firstLayerCRS,
									final ILayer secondLayer,
									final CoordinateReferenceSystem secondLayerCRS,
									final Filter filterInFirstLayer,
									final Filter filterInSecondLayer,
									final boolean copySourceFeatures) {

		assert firstLayer != null;
		assert firstLayerCRS != null;
		assert secondLayer != null;
		assert secondLayerCRS != null;
		assert filterInFirstLayer != null;
		assert filterInSecondLayer != null;
		

		this.firstLayer = firstLayer;
		this.firstLayerCRS = firstLayerCRS;
		this.secondLayer = secondLayer;
		this.secondLayerCRS = secondLayerCRS;
		this.filterInFirstLayer = filterInFirstLayer;
		this.filterInSecondLayer = filterInSecondLayer;
		this.copySourceFeatures = copySourceFeatures;
	}

	/**
	 * @return Returns the firstLayer.
	 */
	public ILayer getFirstLayer() {

		return firstLayer;
	}

	/**
	 * @return Returns the firstLayerCRS
	 */
	public CoordinateReferenceSystem getFirstLayerCRS() {
		return firstLayerCRS;
	}

	/**
	 * @return Returns the secondLayer.
	 */
	public ILayer getSecondLayer() {

		return secondLayer;
	}

	/**
	 * @return Returns the secondLayerCRS
	 */
	public CoordinateReferenceSystem getSecondLayerCRS() {
		return secondLayerCRS;
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

	public boolean isCopySourceFeatures() {
		return copySourceFeatures;
	}

	public void setCopySourceFeatures(boolean copySourceFeatures) {
		this.copySourceFeatures = copySourceFeatures;
	}

}
