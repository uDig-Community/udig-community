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
 * Common parameters for clip operation.
 * <p>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
abstract class AbstractClipParameters implements IClipParameters {

	private ILayer						usingLayer;
	private CoordinateReferenceSystem	usingCRS;
	private ILayer						clipSourceLayer;
	private CoordinateReferenceSystem	clipSourceCRS;
	private Filter						usingFilter;
	private Filter						clipSourceFilter;

	public AbstractClipParameters(	final ILayer usingLayer,
									final CoordinateReferenceSystem usingCRS,
									final ILayer clipSourceLayer,
									final CoordinateReferenceSystem clipSourceCRS,
									final Filter usingFilter,
									final Filter clipSourceFilter) {

		assert usingLayer != null;
		assert usingCRS != null;
		assert clipSourceLayer != null;
		assert clipSourceCRS != null;
		assert usingFilter != null;
		assert clipSourceFilter != null;

		this.usingLayer = usingLayer;
		this.usingCRS = usingCRS;
		this.clipSourceLayer = clipSourceLayer;
		this.clipSourceCRS = clipSourceCRS;

		this.usingFilter = usingFilter;
		this.clipSourceFilter = clipSourceFilter;

	}

	/**
	 * @return Returns the sourceLayer.
	 */
	public ILayer getClipSourceLayer() {
		return this.clipSourceLayer;
	}

	/**
	 * @return Returns the source CRS.
	 */
	public CoordinateReferenceSystem getClipSourceCRS() {
		return this.clipSourceCRS;
	}

	/**
	 * @return Returns the usingLayer.
	 */
	public ILayer getUsingLayer() {
		return this.usingLayer;
	}

	/**
	 * @return Returns the using layer CRS.
	 */
	public CoordinateReferenceSystem getUsingCRS() {
		return this.usingCRS;
	}

	/**
	 * @return Returns the usingFilter.
	 */
	public Filter getUsingFilter() {
		return this.usingFilter;
	}

	/**
	 * @return Returns the clipSourceFilter.
	 */
	public Filter getClipSourceFilter() {
		return this.clipSourceFilter;
	}

}
