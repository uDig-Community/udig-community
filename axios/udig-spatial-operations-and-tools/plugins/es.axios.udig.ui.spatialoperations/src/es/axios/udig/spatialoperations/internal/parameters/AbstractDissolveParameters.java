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

import java.util.List;

import net.refractions.udig.project.ILayer;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Common parameters for dissolve operation.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1
 */
abstract class AbstractDissolveParameters implements IDissolveParameters {

	private ILayer						sourceLayer		= null;
	private CoordinateReferenceSystem	sourceCRS		= null;
	private List<String>				propDissolve	= null;
	private CoordinateReferenceSystem	mapCrs			= null;
	private Filter						filter			= null;

	public AbstractDissolveParameters(	final ILayer sourceLayer,
										final CoordinateReferenceSystem sourceCRS,
										final Filter filter,
										final List<String> list,
										final CoordinateReferenceSystem mapCrs) {

		assert sourceLayer != null;
		assert sourceCRS != null;
		assert list != null;
		assert mapCrs != null;
		assert filter != null;

		this.sourceLayer = sourceLayer;
		this.sourceCRS = sourceCRS;
		this.filter = filter;
		this.propDissolve = list;
		this.mapCrs = mapCrs;
	}

	/**
	 * @return the sourceLayer
	 */
	public ILayer getSourceLayer() {
		return sourceLayer;
	}

	/**
	 * @return the sourceCRS
	 */
	public CoordinateReferenceSystem getSourceCRS() {
		return this.sourceCRS;
	}

	/**
	 * @return the propDissolve
	 */
	public List<String> getPropDissolve() {
		return propDissolve;
	}

	/**
	 * @return the mapCrs
	 */
	public CoordinateReferenceSystem getMapCrs() {
		return mapCrs;
	}

	public Filter getFilter() {
		return this.filter;
	}
}
