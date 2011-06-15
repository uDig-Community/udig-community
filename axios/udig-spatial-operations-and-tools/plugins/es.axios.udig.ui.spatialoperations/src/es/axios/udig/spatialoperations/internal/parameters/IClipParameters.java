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
 * Clip Parameter
 * <p>
 * Parameters for clip process
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
public interface IClipParameters {

	/**
	 * @return Returns the sourceLayer.
	 */
	public ILayer getClipSourceLayer();

	/**
	 * @return Returns the source CRS
	 */
	public CoordinateReferenceSystem getClipSourceCRS();

	/**
	 * @return Returns the usingLayer.
	 */
	public ILayer getUsingLayer();

	/**
	 * @return Returns the using layer CRS
	 */
	public CoordinateReferenceSystem getUsingCRS();

	/**
	 * @return Returns the usingFilter.
	 */
	public Filter getUsingFilter();

	/**
	 * @return Returns the ClipSourceLayer.
	 */
	public Filter getClipSourceFilter();

}
