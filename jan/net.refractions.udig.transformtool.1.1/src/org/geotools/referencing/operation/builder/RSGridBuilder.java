/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import java.util.List;

import javax.vecmath.MismatchedSizeException;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.builder.algorithm.Quadrilateral;
import org.geotools.referencing.operation.builder.algorithm.TriangulationException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;


public class RSGridBuilder extends WarpGridBuilder {
    private final RubberSheetBuilder rsBuilder;

    /**
     * Builds controlling Grid using RubberSheet Transformation
     * @param vectors
     * @param dx
     * @param dy
     * @param envelope
     * @param realToGrid
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws MismatchedReferenceSystemException
     * @throws TransformException
     */
    public RSGridBuilder(List vectors, double dx, double dy, Envelope envelope,
        MathTransform realToGrid)
        throws MismatchedSizeException, MismatchedDimensionException,
            MismatchedReferenceSystemException, TransformException, TriangulationException {
        super(vectors, dx, dy, envelope, realToGrid);

        DirectPosition p0 = new DirectPosition2D(((GeneralEnvelope)envelope).getCoordinateReferenceSystem(),
                envelope.getLowerCorner().getOrdinate(0) - 5.5,
                envelope.getLowerCorner().getOrdinate(1) - 5.5);
        DirectPosition p2 = new DirectPosition2D(((GeneralEnvelope)envelope).getCoordinateReferenceSystem(),
                envelope.getUpperCorner().getOrdinate(0) + 5.5,
                envelope.getUpperCorner().getOrdinate(1) + 5.5);

        DirectPosition p1 = new DirectPosition2D(((GeneralEnvelope)envelope).getCoordinateReferenceSystem(),
                p0.getOrdinate(0), p2.getOrdinate(1));
        DirectPosition p3 = new DirectPosition2D(((GeneralEnvelope)envelope).getCoordinateReferenceSystem(),
                p2.getOrdinate(0), p0.getOrdinate(1));

        Quadrilateral quad = new Quadrilateral(p0, p1, p2, p3);

        rsBuilder = new RubberSheetBuilder(vectors, quad);
    }

    /**
     * Generates grid of source points.
     * @param values general values of grid
     * @return generated grid
     */
    private float[] generateSourcePoints(ParameterValueGroup values) {
        float[] sourcePoints = ((float[]) values.parameter("warpPositions").getValue());

        for (int i = 0; i <= values.parameter("yNumCells").intValue(); i++) {
            for (int j = 0; j <= values.parameter("xNumCells").intValue(); j++) {
                float x = (j * values.parameter("xStep").intValue())
                    + values.parameter("xStart").intValue();
                float y = (i * values.parameter("yStep").intValue())
                    + values.parameter("yStart").intValue();

                sourcePoints[(i * ((1 + values.parameter("xNumCells").intValue()) * 2)) + (2 * j)] = (float) x;

                sourcePoints[(i * ((1 + values.parameter("xNumCells").intValue()) * 2)) + (2 * j)
                + 1] = (float) y;
            }
        }

        return sourcePoints;
    }

    /**
     * Computes target grid.
     * @return computed target grid.
     */
    protected float[] computeWarpGrid(ParameterValueGroup values) {
        float[] source = generateSourcePoints(values);

        try {
            rsBuilder.getMathTransform().transform(source, 0, source, 0, (source.length + 1) / 2);
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FactoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return source;
    }   
}
