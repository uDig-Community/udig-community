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

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.tasks.SpatialRelation;

/**
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
final class SpatialJoinInNewLayerParameters extends AbstractSpatialJoinParameters implements
			ISpatialJoinInNewLayerParameters {

	private String						layerName			= null;
	private Class<? extends Geometry>	targetGeometryClass	= null;

	public SpatialJoinInNewLayerParameters(	final ILayer firstLayer,
											final CoordinateReferenceSystem firstCRS,
											final ILayer secondLayer,
											final CoordinateReferenceSystem secondCRS,
											final SpatialRelation relation,
											final CoordinateReferenceSystem mapCrs,
											final String layerName,
											final Class<? extends Geometry> targetGeometryClass,
											final Filter filterInFirstLayer,
											final Filter filterInSecondLayer,
											final Boolean selection) {

		super(firstLayer, firstCRS, secondLayer, secondCRS, relation, mapCrs, filterInFirstLayer, filterInSecondLayer,
					selection);

		assert layerName != null;
		assert targetGeometryClass != null;

		this.layerName = layerName;
		this.targetGeometryClass = targetGeometryClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seees.axios.udig.spatialoperations.internal.parameters.
	 * ISpatialJoinGeomInNewLayerParameters#getTargetGeometry()
	 */
	public Class<? extends Geometry> getTargetGeometry() {
		return this.targetGeometryClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seees.axios.udig.spatialoperations.internal.parameters.
	 * ISpatialJoinGeomInNewLayerParameters#getTargetName()
	 */
	public String getTargetName() {
		return this.layerName;
	}

}
