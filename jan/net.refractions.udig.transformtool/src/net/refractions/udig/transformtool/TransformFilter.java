/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.transformtool;

import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;


/**
 * The Filter for appying the transformation on the geometry  example:
 * Feature.getDefaultGeometry().apply(new TransformFilter(MathTransform
 * transform));
 *
 * @author jezekjan
 */
public class TransformFilter implements CoordinateFilter {
    /* Transform to apply to each coordinate*/
    private MathTransform transform;

/**
 * Creats the filter useing {@param transform}.
 * 
 * @param transform to be applyed.
 */
    public TransformFilter(MathTransform transform) {
        this.transform = transform;
    }

    /**
     * Performs a transformation on a coordinate
     *
     * @param coordinate to be transformed.
     */
    public void filter(Coordinate coordinate) {
        DirectPosition point = new DirectPosition2D(coordinate.x, coordinate.y);

        try {
            point = transform.transform(point, point);
        } catch (org.opengis.referencing.operation.TransformException e) {
            System.out.println("Error in transformation: " + e);
        }

        coordinate.x = point.getCoordinates()[0];
        coordinate.y = point.getCoordinates()[1];
    }
}
