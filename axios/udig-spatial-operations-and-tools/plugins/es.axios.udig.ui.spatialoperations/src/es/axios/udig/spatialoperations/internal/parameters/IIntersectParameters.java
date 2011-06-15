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
 * 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
public interface IIntersectParameters {

	/**
	 * @return Returns the firstLayer.
	 */
	public ILayer getFirstLayer();
	
	/**
	 * @return Returns the first layer CRS.
	 */
	public CoordinateReferenceSystem getFirstCRS();

	/**
	 * @return Returns the secondLayer.
	 */
	public ILayer getSecondLayer();

	/**
	 * @return Returns the second layer CRS.
	 */
	public CoordinateReferenceSystem getSecondCRS();
	
	/**
	 * @return Returns the FilterInFirstLayer.
	 */
	public Filter getFilterInFirstLayer();

	/**
	 * @return Returns the FilterInSecondLayer.
	 */
	public Filter getFilterInSecondLayer();

}
