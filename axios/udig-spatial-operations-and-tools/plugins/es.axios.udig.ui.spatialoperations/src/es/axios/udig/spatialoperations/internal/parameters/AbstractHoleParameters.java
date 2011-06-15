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
 * Common parameters for hole operation.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
abstract class AbstractHoleParameters implements IHoleParameters {

	private ILayer						sourceLayer;
	private CoordinateReferenceSystem	sourceCRS;
	private ILayer						usingLayer;
	private CoordinateReferenceSystem	usingCRS;
	private Filter						sourceFilter;
	private Filter						usingFilter;

	public AbstractHoleParameters(	final ILayer sourceLayer,
									final CoordinateReferenceSystem sourceCRS,
									final ILayer usingLayer,
									final CoordinateReferenceSystem usingCRS,
									final Filter sourceFilter,
									final Filter usingFilter) {

		assert sourceLayer != null;
		assert sourceCRS != null;
		assert usingLayer != null;
		assert usingCRS != null;

		assert sourceFilter != null;
		assert usingFilter != null;

		this.sourceLayer = sourceLayer;
		this.sourceCRS = sourceCRS;
		this.usingLayer = usingLayer;
		this.usingCRS = usingCRS;

		this.sourceFilter = sourceFilter;
		this.usingFilter = usingFilter;

	}

	/**
	 * @return Returns the sourceLayer.
	 */
	public ILayer getSourceLayer() {
		return this.sourceLayer;
	}

	/**
	 * @return Returns the source layer CRS
	 */
	public CoordinateReferenceSystem getSourceCRS() {
		return sourceCRS;
	}

	/**
	 * @return Returns the sourceFilter.
	 */
	public Filter getSourceFilter() {
		return this.sourceFilter;
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
		return usingCRS;
	}

	/**
	 * @return Returns the usingFilter.
	 */
	public Filter getUsingFilter() {
		return this.usingFilter;
	}

}
