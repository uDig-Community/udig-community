/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.referencing.operation.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.MismatchedSizeException;

import org.geotools.referencing.operation.builder.algorithm.MapTriangulationFactory;
import org.geotools.referencing.operation.builder.algorithm.Quadrilateral;
import org.geotools.referencing.operation.builder.algorithm.RubberSheetTransform;
import org.geotools.referencing.operation.builder.algorithm.TINTriangle;
import org.geotools.referencing.operation.builder.algorithm.TriangulationException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;



/**
 * Builds  {@linkplain org.geotools.referencing.operation.builder.RubberSheetBuilder
 * RubberSheet} transformation from a list of {@linkplain
 * org.geotools.referencing.operation.builder.MappedPosition MappedPosition}. The
 * explanation of RubberSheet transformation can be seen <a href =
 * "http://planner.t.u-tokyo.ac.jp/member/fuse/rubber_sheeting.pdf">here</a>.
 *
 * @since 2.4
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/operation/builder/RubberSheetBuilder.java $
 * @version $Id: RubberSheetBuilder.java 24925 2007-03-27 20:12:08Z jgarnett $
 * @author Jan Jezek
 */
public class RubberSheetBuilder extends MathTransformBuilder {
    /** trianglesMap Map of the original and destination triangles. */
    private HashMap trianglesMap;

    /**
     * trianglesToKeysMap Map of a original triangles and associated
     * AffineTransformation Objects.
     */
    private HashMap trianglesToKeysMap;

/**
     * Creates the transformation from specified pairs of points and
     * quadrilateral that deffines the area of transformation.
     * 
     * 
     * 
     * @param vectors list of {@linkplain org.geotools.referencing.operation.builder.MappedPosition MappedPosition}
     * 
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws MismatchedReferenceSystemException
     * @throws TriangulationException
     */
    public RubberSheetBuilder(List vectors, Quadrilateral quad)
        throws MismatchedSizeException, MismatchedDimensionException,
            MismatchedReferenceSystemException, TriangulationException {
        super.setMappedPositions(vectors);

        checkQuad(quad);

        //Quadrilateral mQuad = mappedQuad(quad, vectors);

        MapTriangulationFactory trianglemap = new MapTriangulationFactory(quad,
                vectors);
        this.trianglesMap = (HashMap) trianglemap.getTriangleMap();
        this.trianglesToKeysMap = mapTrianglesToKey();
    }

    /**
     * Returns the minimum number of points required by this builder.
     *
     * @return 1
     */
    public int getMinimumPointCount() {
        return 1;
    }

    /**
     * Checks the Coordinate Reference System of the quad.
     *
     * @param quad to be tested
     *
     * @throws MismatchedReferenceSystemException
     */
    private void checkQuad(Quadrilateral quad)
        throws MismatchedReferenceSystemException {
        CoordinateReferenceSystem crs;

        try {
            crs = getSourceCRS();
        } catch (FactoryException e) {
            // Can't fetch the CRS. Use the one from the first quad point instead.
            crs = quad.p0.getCoordinateReferenceSystem();
        }

        if ((quad.p0.getCoordinateReferenceSystem() != crs)
                || (quad.p1.getCoordinateReferenceSystem() != crs)
                || (quad.p2.getCoordinateReferenceSystem() != crs)
                || (quad.p3.getCoordinateReferenceSystem() != crs)) {
            throw new MismatchedReferenceSystemException(
                "Wrong Coordinate Reference System of the quad.");
        }
    }

    /**
     * Returns the map of source and destination triangles.
     *
     * @return The Map of source and destination triangles.
     */
    public HashMap getMapTriangulation() {
        return trianglesMap;
    }

    /**
     * Calculates affine transformation prameters from the pair of
     * triagles.
     *
     * @return The HashMap where the keys are the original triangles and values
     *         are AffineTransformation Objects.
     */
    private HashMap mapTrianglesToKey() {
        AffineTransformBuilder calculator;
        
        HashMap trianglesToKeysMap = (HashMap) trianglesMap.clone();

        Iterator it = trianglesToKeysMap.entrySet().iterator();

        while (it.hasNext()) {
            
            Map.Entry a = (Map.Entry) it.next();
            List pts = new ArrayList();

            for (int i = 1; i <= 3; i++) {
                pts.add(new MappedPosition(
                        ((TINTriangle) a.getKey()).getPoints()[i],
                        ((TINTriangle) a.getValue()).getPoints()[i]));
                
            }

            try {
                calculator = new AffineTransformBuilder(pts);
                a.setValue(calculator.getMathTransform());
            } catch (Exception e) {
                // should never reach here because AffineTransformBuilder(pts)
            	// should not throw any Exception.
            	e.printStackTrace();
            }
        }

        return trianglesToKeysMap;
    }

    /**
     * Returns MathTransform transformation setup as RubberSheet.
     *
     * @return calculated MathTransform
     *
     * @throws FactoryException when the size of source and destination point
     *         is not the same.
     */
    protected MathTransform computeMathTransform() throws FactoryException {
        return new RubberSheetTransform(trianglesToKeysMap);
    }
}
