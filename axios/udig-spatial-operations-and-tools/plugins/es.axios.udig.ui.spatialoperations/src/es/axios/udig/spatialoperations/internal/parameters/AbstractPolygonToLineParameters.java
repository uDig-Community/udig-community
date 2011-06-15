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
 * Common parameters for Polygon to line operation.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
abstract class AbstractPolygonToLineParameters implements IPolygonToLineParameters {

	private Filter						filter		= null;
	private CoordinateReferenceSystem	mapCrs		= null;
	private ILayer						sourceLayer	= null;
	private CoordinateReferenceSystem	sourceCRS	= null;
	private Boolean						explode		= null;

	/**
	 * Initialization of commons parameters.
	 * 
	 * @param filter
	 * @param mapCrs
	 * @param layer
	 */
	public AbstractPolygonToLineParameters(	final Filter filter,
											final CoordinateReferenceSystem mapCrs,
											final ILayer sourceLayer,
											final CoordinateReferenceSystem sourceCRS,
											final Boolean explode) {

		assert filter != null;
		assert mapCrs != null;
		assert sourceLayer != null;
		assert sourceCRS != null;
		assert explode != null;

		this.filter = filter;
		this.mapCrs = mapCrs;
		this.sourceLayer = sourceLayer;
		this.sourceCRS = sourceCRS;
		this.explode = explode;
	}

	public Filter getFilter() {

		return this.filter;
	}

	public CoordinateReferenceSystem getMapCrs() {

		return this.mapCrs;
	}

	public ILayer getSourceLayer() {

		return this.sourceLayer;
	}

	public CoordinateReferenceSystem getSourceCRS() {
		return this.sourceCRS;
	}

	public Boolean getExplode() {

		return this.explode;
	}

}
