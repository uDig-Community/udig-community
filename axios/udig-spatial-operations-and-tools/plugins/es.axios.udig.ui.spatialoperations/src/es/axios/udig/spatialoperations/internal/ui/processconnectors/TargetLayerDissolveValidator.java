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
package es.axios.udig.spatialoperations.internal.ui.processconnectors;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import net.refractions.udig.project.ILayer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.spatialoperations.ui.common.TargetLayerValidator;

/**
 * Valids the the target geometry required by the dissolve operation
 * <p>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
class TargetLayerDissolveValidator extends TargetLayerValidator {

	/**
	 * Check the target geometry compatibility for dissolve operation.
	 * 
	 * @return
	 */
	@Override
	public boolean validGeometryCompatible(ILayer sourceLayer) {

		SimpleFeatureType sourceType = sourceLayer.getSchema();
		GeometryDescriptor firstGeomAttr = sourceType.getGeometryDescriptor();
		Class<? extends Geometry> sourceGeomClass = (Class<? extends Geometry>) firstGeomAttr.getType().getBinding();

		Class<? extends Geometry> targetGeomClass = null;

		if (targetLayer != null) {
			SimpleFeatureType targetType = targetLayer.getSchema();

			GeometryDescriptor secondGeomAttr = targetType.getGeometryDescriptor();
			targetGeomClass = (Class<? extends Geometry>) secondGeomAttr.getType().getBinding();
		} else {
			targetGeomClass = this.targetGeometryClass;
		}
		if (targetGeomClass.equals(Geometry.class)) {
			return true;
		}

		if ((sourceGeomClass.equals(Polygon.class) || sourceGeomClass.equals(MultiPolygon.class))
					&& (!targetGeomClass.equals(Polygon.class) && !targetGeomClass.equals(MultiPolygon.class))) {
			return false;
		}

		if ((sourceGeomClass.equals(LineString.class) || sourceGeomClass.equals(MultiLineString.class))
					&& (!targetGeomClass.equals(LineString.class) && !targetGeomClass.equals(MultiLineString.class))) {
			return false;
		}

		if ((sourceGeomClass.equals(Point.class) || sourceGeomClass.equals(MultiPoint.class))
					&& (!targetGeomClass.equals(Point.class) && !targetGeomClass.equals(MultiPoint.class))) {
			return false;
		}

		return true;
	}

}
