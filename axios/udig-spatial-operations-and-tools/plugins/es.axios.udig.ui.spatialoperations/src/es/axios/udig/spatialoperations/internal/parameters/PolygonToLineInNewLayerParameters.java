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

import com.vividsolutions.jts.geom.Geometry;

/**
 * PolygonToLine operation parameters.
 * <p>
 * Required parameters to create a new layer with the PolygonToLine operation.
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
final class PolygonToLineInNewLayerParameters extends AbstractPolygonToLineParameters implements
			IPolygonToLineInNewLayerParameters {

	private String						layerName			= null;
	private Class<? extends Geometry>	targetGeometryClass	= null;

	public PolygonToLineInNewLayerParameters(	final Filter filter,
												final CoordinateReferenceSystem mapCrs,
												final ILayer layer,
												final CoordinateReferenceSystem layerCRS,
												final String layerName,
												final Class<? extends Geometry> targetGeometryClass,
												final Boolean explode) {

		super(filter, mapCrs, layer, layerCRS, explode);

		assert layerName != null;
		assert targetGeometryClass != null;

		this.layerName = layerName;
		this.targetGeometryClass = targetGeometryClass;
	}

	public Class<? extends Geometry> getTargetGeometryClass() {

		return this.targetGeometryClass;
	}

	public String getTargetName() {

		return this.layerName;
	}

}
