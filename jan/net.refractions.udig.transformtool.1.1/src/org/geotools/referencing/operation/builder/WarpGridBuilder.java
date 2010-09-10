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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.MismatchedSizeException;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.transform.WarpGridTransform2D;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;


/**
 * Provides a basic implementation for {@linkplain WarpGridTransform2D warp grid math transform} builders.
 *
 * @see <A HREF="http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/WarpGrid.html">WarpGrid at JAI </A>
 *
 * @author jezekjan
 *
 */
public abstract class WarpGridBuilder extends MathTransformBuilder {
    /**
     * Grid width
     */
    private int width;

    /**
     * Grid height
     */
    private int height;

    /**
     * Envelope for generated Grid
     */
    Envelope envelope;

    /**
     * List of Mapped Positions in ggrid coordinates
     */
    List /*<MappedPosition>*/ localpositions = new ArrayList();

    //List /*<MappedPosition>*/ worldpositions ;

    /** GridValues like maxx maxt dx dy etc.. */
    GridParamValues globalValues;

    /** RealToGrid Math Transform */
    MathTransform worldToGrid;

    /** Grid of x shifts */
    private float[][] dxgrid;

    /** Grid of y shifts*/
    private float[][] dygrid;

    /** Warp positions*/
    private float[] warpPositions;

    /**
     * Constructs Builder
     * @param vectors Mapped positions
     * @param dx The horizontal spacing between grid cells.
     * @param dy The vertical spacing between grid cells.
     * @param envelope Envelope of generated grid.
     * @throws MismatchedSizeException
     * @throws MismatchedDimensionException
     * @throws MismatchedReferenceSystemException
     */
    public WarpGridBuilder(List vectors, double dx, double dy, Envelope envelope,
        MathTransform realToGrid)
        throws MismatchedSizeException, MismatchedDimensionException,
            MismatchedReferenceSystemException, TransformException {
        this.worldToGrid = realToGrid;

        globalValues = new GridParamValues(envelope, realToGrid, dx, dy);
        super.setMappedPositions(vectors);

        // super.setMappedPositions(transformMPToGrid(vectors, realToGrid));
        localpositions = transformMPToGrid(vectors, realToGrid);
        this.envelope = envelope;
    }

    public List getGridMappedPositions() {
        if (localpositions == null) {
            localpositions = transformMPToGrid(getMappedPositions(), worldToGrid);
        }

        return localpositions;
    }

    /**
     * Transforms MappedPostions to grid system
     *
     */
    private List transformMPToGrid(List MappedPositions, MathTransform trans) {
        List gridmp = new ArrayList();

        for (Iterator i = MappedPositions.iterator(); i.hasNext();) {
            MappedPosition mp = (MappedPosition) i.next();

            try {
                DirectPosition2D gridSource = new DirectPosition2D();
                DirectPosition2D gridTarget = new DirectPosition2D();
                trans.transform(mp.getSource(), gridSource);
                trans.transform(mp.getTarget(), gridTarget);
                gridmp.add(new MappedPosition(gridSource, gridTarget));
            } catch (MismatchedDimensionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TransformException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return gridmp;
    }

    private void ensureVectorsInsideEnvelope() {
        /* @TODO - ensure that source MappedPositions are inside the  envelope*/
    }

    /*
     *
     * (non-Javadoc)
     * @see org.geotools.referencing.operation.builder.MathTransformBuilder#computeMathTransform()
     */
    protected MathTransform computeMathTransform() throws FactoryException {
        warpPositions = getGrid();

        globalValues.WarpGridParameters.parameter("warpPositions").setValue(warpPositions);

        WarpGridTransform2D wt = (WarpGridTransform2D) (new WarpGridTransform2D.Provider())
            .createMathTransform(globalValues.getWarpGridParameters());
                
        wt.setWorldtoGridTransform(this.worldToGrid);

        return wt;
    }

    /*
     * (non-Javadoc)
     * @see org.geotools.referencing.operation.builder.MathTransformBuilder#getMinimumPointCount()
     */
    public int getMinimumPointCount() {
        return 1;
    }

    /**
     * Computes WarpGrid Positions.
     *
     */
    abstract protected float[] computeWarpGrid(ParameterValueGroup values);

    /**
     * Returs Grid
     * @return
     */
    private float[] getGrid() {
        if (warpPositions == null) {
            warpPositions = computeWarpGrid(globalValues.WarpGridParameters);
        } else {
            return warpPositions;
        }

        return warpPositions;
    }

    /**
     * Return array of Shifts. This method is useful to create Coverage2D object.
     * @return array of Shifts
     */
    public float[][] getDxGrid() {
        if ((dxgrid == null) || (dxgrid.length == 0)) {
            ParameterValueGroup WarpParams = globalValues.WarpGridParameters;
            final int xNumCells = WarpParams.parameter("xNumCells").intValue();
            final int yNumCells = WarpParams.parameter("yNumCells").intValue();
            final int xStep = WarpParams.parameter("xStep").intValue();
            final int yStep = WarpParams.parameter("yStep").intValue();

            final float[] warpPositions = getGrid();
            dxgrid = new float[yNumCells + 1][xNumCells + 1];

            for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
                for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
                    dxgrid[i][j] = (float) warpPositions[(int) ((i * (1 + xNumCells) * 2) + (2 * j))]
                        - (j * xStep);
                }
            }
        }

        return dxgrid;
    }

    /**
     * Return array of Shifts. This method is useful to create Coverage2D object.
     * @return array of Shifts
     */
    public float[][] getDyGrid() {
        if ((dygrid == null) || (dygrid.length == 0)) {
            ParameterValueGroup WarpParams = globalValues.WarpGridParameters;
            final int xNumCells = WarpParams.parameter("xNumCells").intValue();
            final int yNumCells = WarpParams.parameter("yNumCells").intValue();
            final int xStep = WarpParams.parameter("xStep").intValue();
            final int yStep = WarpParams.parameter("yStep").intValue();

            final float[] warpPositions = getGrid();

            dygrid = new float[yNumCells + 1][xNumCells + 1];

            for (int i = 0; i <= WarpParams.parameter("yNumCells").intValue(); i++) {
                for (int j = 0; j <= WarpParams.parameter("xNumCells").intValue(); j++) {
                    dygrid[i][j] = (float) warpPositions[(int) ((i * (1 + xNumCells) * 2) + (2 * j)
                        + 1)] - (i * yStep);
                }
            }
        }

        return dygrid;
    }

    /**
     *
     * @param dim
     * @param path
     * @return
     * @throws IOException
     */
    public File getDeltaFile(int dim, String path) throws IOException {
        ParameterValueGroup WarpParams = globalValues.WarpGridParameters;
        final float[] warpPositions = (float[]) WarpParams.parameter("warpPositions").getValue();
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        /*Print the header*/
        osw.write("Warp Grid transformation");
        osw.write("\n");

        /*Print first row (number of columns, number of rows,
         * number of z–values (always one), minimum longitude, cell
         *  size, minimum latitude, cell size, and not used. )*/
        osw.write((WarpParams.parameter("xNumCells").intValue()+1) + " "
            + (WarpParams.parameter("yNumCells").intValue()+1) + " " + "1 "
            + WarpParams.parameter("xStart").doubleValue() + " "
            + WarpParams.parameter("xStep").doubleValue() + " "
            + WarpParams.parameter("yStart").doubleValue() + " "
            + WarpParams.parameter("yStep").doubleValue() + " 0");

        //osw.write("\n");

        int ii = 0;
        if (dim == 0) {
            for (int i = 0; i < getDxGrid().length; i++) {
            	osw.write(String.valueOf("\n"));
                for (int j = 0; j < getDxGrid()[i].length; j++) {
                	                	
                    osw.write(String.valueOf(getDxGrid()[i][j]) + " ");
                	                            
                }
                
            }
        } else if (dim == 1) {
            for (int i = 0; i < getDxGrid().length; i++) {
            	osw.write(String.valueOf("\n"));
                for (int j = 0; j < getDxGrid()[i].length; j++) {
                    osw.write(String.valueOf(getDyGrid()[i][j]) + " ");
                }
            }
        } else {
            throw new IndexOutOfBoundsException(Double.toString(dim));
        }

        osw.close();

        return file;
    }

    /**
     * Converts warp positions from float[] containing target 
     * positions to float[][] containing deltas.
     *
     */
    public static void warpPosToDeltas(int xStart, int xStep, int xNumCells, int  yStart, int yStep, int yNumCells, float[][] yDeltas, float[][] xDeltas){
    	
    }
    
    public static float[] deltasToWarpPos(int xStart, int xStep, int xNumCells, int  yStart, int yStep, int yNumCells, float[][] yDeltas, float[][] xDeltas){
    	
    	float[] warpPos = new float[(xNumCells+1)*(yNumCells+1)*2];
    	for (int i = 0; i < yNumCells; i++) {
            for (int j = 0; j < xNumCells; j++) {            	
                     warpPos[2*j+xNumCells*i*2] = xStart + j*xStep  +xDeltas[i][j];
                     warpPos[2*j+xNumCells*i*2 +1 ] = yStart + i*yStep + yDeltas[i][j];                    
                }
            }
    	return warpPos;
    }

    /**
     * Takes care about parameters
     * @author jezekjan
     *
     */
    private static class GridParamValues {
        private ParameterValueGroup WarpGridParameters;

        /**
         * Constructs GridParamValues from such properties.
         * @param env Envelope
         * @param trans Transformation to Grid CRS.
         * @param dx x step
         * @param dy y step
         * @throws TransformException
         */
        public GridParamValues(Envelope env, MathTransform trans, double dx, double dy)
            throws TransformException {
            Envelope dxdy = new Envelope2D(((Envelope2D)env).getCoordinateReferenceSystem(), env.getMinimum(0),
                    env.getMinimum(1), dx, dy);

            /* Transforms dx, dy and envelope to grid system */
          //  dxdy = CRS.transform(trans, dxdy);
          //  env = CRS.transform(trans, env);

            try {
                final DefaultMathTransformFactory factory = new DefaultMathTransformFactory();
                WarpGridParameters = factory.getDefaultParameters("Warp Grid");
                WarpGridParameters.parameter("xStart").setValue((int) (env.getMinimum(0) + 0.5));
                WarpGridParameters.parameter("yStart").setValue((int) (env.getMinimum(1) + 0.5));
                WarpGridParameters.parameter("xStep").setValue((int) Math.ceil(dxdy.getLength(0)));
                WarpGridParameters.parameter("yStep").setValue((int) Math.ceil(dxdy.getLength(1)));
                WarpGridParameters.parameter("xNumCells")
                                  .setValue((int) Math.ceil(env.getLength(0) / dxdy.getLength(0)));
                WarpGridParameters.parameter("yNumCells")
                                  .setValue((int) Math.ceil(env.getLength(1) / dxdy.getLength(1)));

                WarpGridParameters.parameter("warpPositions")
                                  .setValue(new float[2 * (WarpGridParameters.parameter("xNumCells")
                                                                             .intValue() + 1) * (WarpGridParameters.parameter(
                        "yNumCells").intValue() + 1)]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Sets the grid warp positions in
         * @param warpPos array of grid warp positions
         */
        public void setGridWarpPostions(float[] warpPos) {
            WarpGridParameters.parameter("warpPositions").setValue(warpPos);
        }

        /**
         * Returns warp grid positions.
         * @return warp grid positions
         */
        public ParameterValueGroup getWarpGridParameters() {
            return WarpGridParameters;
        }
    }
}
