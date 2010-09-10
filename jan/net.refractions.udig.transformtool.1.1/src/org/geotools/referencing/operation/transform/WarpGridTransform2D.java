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
package org.geotools.referencing.operation.transform;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.media.jai.Warp;
import javax.media.jai.WarpGrid;

import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.Parameter;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.Transformation;
import org.opengis.spatialschema.geometry.DirectPosition;


/**
 * Basic implementation of JAI's GridWarp Transformation. This class just encapsulate GridWarp into the
 * GeoTools transformations conventions.
 * @author jezekjan
 *
 */
public class WarpGridTransform2D extends WarpTransform2D {
    // private final Warp warp;
    private MathTransform inverse;
    private MathTransform worldToGrid;
    private final Warp warp;
    private final int xStart;
    private final int xStep;
    private final int xNumCells;
    private final int yStart;
    private final int yStep;
    private final int yNumCells;
    private final float[] warpPositions;

    /**
     *
     * @param xStart
     * @param xStep
     * @param xNumCells
     * @param yStart
     * @param yStep
     * @param yNumCells
     * @param warpPositions
     */
    public WarpGridTransform2D(int xStart, int xStep, int xNumCells, int yStart, int yStep,
        int yNumCells, float[] warpPositions) {
        super( new WarpGrid(xStart, xStep, xNumCells, yStart, yStep, yNumCells, warpPositions), null);

        this.warp = super.getWarp();
        
        this.xStart = xStart;
        this.xStep = xStep;
        this.xNumCells = xNumCells;
        this.yStart = yStart;
        this.yStep = yStep;
        this.yNumCells = yNumCells;
        this.warpPositions = warpPositions;
    }

    /**
     * Constructs a transform using the specified warp object.
     *
     * @param warp    The image warp to wrap into a math transform.
     * @param inverse An image warp to uses for the {@linkplain #inverse inverse transform},
     *                or {@code null} in none.
     */
  /*  protected WarpGridTransform2D(Warp warp, Warp inverse) {
        super(warp, inverse);
        this.warp = warp;
        
        this.xStart = warp.xStart;
        this.xStep = xStep;
        this.xNumCells = xNumCells;
        this.yStart = yStart;
        this.yStep = yStep;
        this.yNumCells = yNumCells;
        this.warpPositions = warpPositions;

        //this.inverse = (inverse!=null) ? new WarpTransform2D(inverse, this) : null;
        // this.warp = warp;
        //this.inverse = inverse;
    }

   /* public WarpGridTransform2D(final String latGridName, final String longGridName)
        throws MalformedURLException, IOException, FactoryException {
        super((createWarpGrid(latGridName, longGridName)).getWarp(),
        null);
    }*/

   
    /**
     * Returns a URL from the string representation. If the string has no
     * path, the default path preferece is added.
     *
     * @param str a string representation of a URL
     * @return a URL created from the string representation
     * @throws MalformedURLException if the URL cannot be created
     */
    private static URL makeURL(final String str) throws MalformedURLException {
        //has '/' or '\' or ':', so probably full path to file
        if ((str.indexOf('\\') >= 0) || (str.indexOf('/') >= 0) || (str.indexOf(':') >= 0)) {
            return makeURLfromString(str);
        } else {
            // just a file name , prepend base location
            final Preferences prefs = Preferences.userNodeForPackage(WarpGridTransform2D.class);
            final String baseLocation = prefs.get("GRID_LOCATION", "");

            return makeURLfromString(baseLocation + "/" + str);
        }
    }

    /**
     * Returns a URL based on a string representation. If no protocol is given,
     * it is assumed to be a local file.
     *
     * @param str a string representation of a URL
     * @return a URL created from the string representation
     * @throws MalformedURLException if the URL cannot be created
     */
    private static URL makeURLfromString(final String str)
        throws MalformedURLException {
        try {
            return new URL(str);
        } catch (MalformedURLException e) {
            //try making this with a file protocal
            return new URL("file", "", str);
        }
    }

    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }
    /**
     * Returns the parameter values for this math transform.
     */
   
    public ParameterValueGroup getParameterValues() {
        if (this.warp instanceof WarpGrid) {
            final WarpGrid wGrid = (WarpGrid) warp;
            final ParameterValue[] p = new ParameterValue[7];
            int c = 0;
            p[c++] = new Parameter(Provider.xStart,   new Integer(this.xStart));
            p[c++] = new Parameter(Provider.xStep, new Integer(this.xStep));
            p[c++] = new Parameter(Provider.xNumCells, new Integer(this.xNumCells));
            p[c++] = new Parameter(Provider.yStart,   new Integer(this.yStart));
            p[c++] = new Parameter(Provider.yStep, new Integer(this.yStep));
            p[c++] = new Parameter(Provider.yNumCells, new Integer(this.yNumCells));
         
            p[c++] = new Parameter(Provider.warpPositions, (Object)this.warpPositions.clone());
            
            return new ParameterGroup(getParameterDescriptors(), (ParameterValue[]) XArray.resize(p, c));
        } else {
            return super.getParameterValues();
        }
    }

    public void setWorldtoGridTransform(MathTransform worldToGrid) {
        this.worldToGrid = worldToGrid;
    }

    public MathTransform getWorldtoGridTransform() {
        return worldToGrid;
    }

    public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) {
        // TODO Auto-generated method stub
        //transformToGrid(srcPts, srcOff, srcPts, srcOff, numPts, false);
        try {
            if (worldToGrid != null) {
                worldToGrid.transform(srcPts, srcOff, srcPts, srcOff, numPts);
            }
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        super.transform(srcPts, srcOff, dstPts, dstOff, numPts);

        try {
            if (worldToGrid != null) {
                worldToGrid.inverse().transform(dstPts, dstOff, dstPts, dstOff, numPts);
            }
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void transform(float[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts) {
        try {
            if (worldToGrid != null) {
                worldToGrid.transform(srcPts, srcOff, srcPts, srcOff, numPts);
            }
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        super.transform(srcPts, srcOff, dstPts, dstOff, numPts);

        try {
            if (worldToGrid != null) {
                worldToGrid.inverse().transform(dstPts, dstOff, dstPts, dstOff, numPts);
            }
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Point2D transform(Point2D ptSrc, Point2D ptDst) {
        try {
            if (worldToGrid != null) {
                worldToGrid.transform((DirectPosition) ptSrc, (DirectPosition) ptSrc);
            }
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ptDst = super.transform(ptSrc, ptDst);

        try {
            if (worldToGrid != null) {
                worldToGrid.inverse().transform((DirectPosition) ptDst, (DirectPosition) ptDst);
            }
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ptDst;
    }

    /**
     * Calculation inverse values. Calculation is not exact, but should provide good results
     * when shifts are smaller than grid cells.
     * @param xStart
     * @param xStep
     * @param xNumCells
     * @param yStart
     * @param yStep
     * @param yNumCells
     * @param warpPositions
     * @return
     */
    protected WarpGridTransform2D calculateInverse(int xStart, int xStep, int xNumCells,
        int yStart, int yStep, int yNumCells, float[] warpPositions) {
        float[] inversePos = new float[warpPositions.length];

        for (int i = 0; i <= yNumCells; i++) {
            for (int j = 0; j <= xNumCells; j++) {
                inversePos[(i * ((1 + xNumCells) * 2)) + (2 * j)] = (2 * ((j * xStep) + xStart))
                    - warpPositions[(i * ((1 + xNumCells) * 2)) + (2 * j)];

                inversePos[(i * ((1 + xNumCells) * 2)) + (2 * j) + 1] = (2 * ((i * yStep) + yStart))
                    - warpPositions[(i * ((1 + xNumCells) * 2)) + (2 * j) + 1];
            }
        }

        WarpGridTransform2D wgt = new WarpGridTransform2D(xStart, xStep, xNumCells, yStart, yStep,
                yNumCells, inversePos);

        wgt.setWorldtoGridTransform(this.worldToGrid);

        return wgt;
    }

    public MathTransform inverse() throws NoninvertibleTransformException {
        // TODO Auto-generated method stub
        if (inverse == null) {
            inverse = calculateInverse(xStart, xStep, xNumCells, yStart, yStep, yNumCells,
                    warpPositions);
        }

        return inverse;
    }

    /**
     *
     * The provider for the {@linkplain WarpGridTransform2D}. This provider constructs a JAI
     * {@linkplain WarpGrid image warp} from a set of mapped positions,
     * and wrap it in a {@linkplain WarpGridTransform2D} object.
     *
     * @author jezekjan
     *
     */
    public static class Provider extends MathTransformProvider {
        /** Serial number for interoperability with different versions. */
        private static final long serialVersionUID = -1126785723468L;

        /** Descriptor for the "{@link WarpGrid#getXStart  xStart}" parameter value. */
        public static final ParameterDescriptor xStart = new DefaultParameterDescriptor("xStart",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getXStep xStep}" parameter value. */
        public static final ParameterDescriptor xStep = new DefaultParameterDescriptor("xStep",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getXNumCells xNumCells}" parameter value. */
        public static final ParameterDescriptor xNumCells = new DefaultParameterDescriptor("xNumCells",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getYStart yStart}" parameter value. */
        public static final ParameterDescriptor yStart = new DefaultParameterDescriptor("yStart",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getYStep yStep}" parameter value. */
        public static final ParameterDescriptor yStep = new DefaultParameterDescriptor("yStep",
                int.class, null, null);

        /** Descriptor for the "{@link WarpGrid#getYNumCells yNumCells}" parameter value. */
        public static final ParameterDescriptor yNumCells = new DefaultParameterDescriptor("yNumCells",
                int.class, null, null);

        /** Descriptor for the warpPositions parameter value. */
        public static final ParameterDescriptor warpPositions = new DefaultParameterDescriptor("warpPositions",
                float[].class, null, null);

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.GEOTOOLS, "Warp Grid")
                },
                new ParameterDescriptor[] {
                    xStart, xStep, xNumCells, yStart, yStep, yNumCells, warpPositions
                });

        /**
         * Create a provider for warp transforms.
         */
        public Provider() {
            super(2, 2, PARAMETERS);
        }

        /**
         * Returns the operation type.
         */
        public Class getOperationType() {
            return Transformation.class;
        }

        /**
         * Creates a warp transform from the specified group of parameter values.
         *
         * @param  values The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup values)
            throws ParameterNotFoundException {
            final int XSTART = intValue(xStart, values);
            final int XSTEP = intValue(xStep, values);
            final int XNUMCELLS = intValue(xNumCells, values);
            final int YSTART = intValue(yStart, values);
            final int YSTEP = intValue(yStep, values);
            final int YNUMCELLS = intValue(yNumCells, values);
            final float[] WARPPOSITIONS = (float[]) value(warpPositions, values);

            /*
               final Warp warp = new WarpGrid(XSTART, XSTEP, XNUMCELLS, YSTART, YSTEP, YNUMCELLS,
                       WARPPOSITIONS);
               final Warp inverse = new WarpGrid(XSTART, XSTEP, XNUMCELLS, YSTART, YSTEP, YNUMCELLS,
                       calculateInverse(values));
             */
            WarpGridTransform2D wgt = new WarpGridTransform2D(XSTART, XSTEP, XNUMCELLS, YSTART,
                    YSTEP, YNUMCELLS, WARPPOSITIONS);

            /*WarpGridTransform2D inversewgt = new WarpGridTransform2D(XSTART, XSTEP, XNUMCELLS, YSTART, YSTEP, YNUMCELLS,
               WARPPOSITIONS);*/
            return wgt; //new WarpGridTransform2D(warp, inverse);
        }
    }

    /**
     * The provider for {@link NADCONTransform}. This provider will construct
     * transforms from {@linkplain org.geotools.referencing.crs.DefaultGeographicCRS
     * geographic} to {@linkplain org.geotools.referencing.crs.DefaultGeographicCRS
     * geographic} coordinate reference systems.
     *
     */
    public static class ProviderFile extends MathTransformProvider {
        /** Serial number for interoperability with different versions. */
        private static final long serialVersionUID = -42356975310348L;

        /**
         * The operation parameter descriptor for the "Latitude_difference_file"
         * parameter value. The default value is "conus.las".
         */
        public static final ParameterDescriptor X_DIFF_FILE = new DefaultParameterDescriptor("X_difference_file",
                String.class, null, "");

        /**
         * The operation parameter descriptor for the "Longitude_difference_file"
         * parameter value. The default value is "conus.los".
         */
        public static final ParameterDescriptor Y_DIFF_FILE = new DefaultParameterDescriptor("Y_difference_file",
                String.class, null, "");

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                    new NamedIdentifier(Citations.EPSG, "9613"),
                    new NamedIdentifier(Citations.GEOTOOLS, "Warp Grid (form file)")
                }, new ParameterDescriptor[] { X_DIFF_FILE, Y_DIFF_FILE });

        /**
         * Constructs a provider.
         */
        public ProviderFile() {
            super(2, 2, PARAMETERS);
        }

        /**
         * Returns the operation type.
         */
        public Class getOperationType() {
            return Transformation.class;
        }

        public MathTransform createMathTransform(final ParameterValueGroup values)
            throws ParameterNotFoundException {
            try {
                return createWarpGrid(values.parameter("X_difference_file").stringValue(),
                    values.parameter("Y_difference_file").stringValue());
            } catch (InvalidParameterTypeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FactoryException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }
        /**
        *
        * @param latGridName
        * @param longGridName
        * @return
        */
       private static WarpGridTransform2D createWarpGrid(final String xGridName, final String yGridName)
           throws MalformedURLException, IOException, FactoryException {
           final URL xGridURL = makeURL(xGridName);
           final URL yGridURL = makeURL(yGridName);
           
           return loadTextGrid(xGridURL, yGridURL);
       }

       /**
        *
        * @param latGridUrl
        * @param longGridUrl
        * @throws IOException
        * @throws FactoryException
        */
       private static WarpGridTransform2D loadTextGrid(URL xGridUrl, URL longGridUrl)
           throws IOException, FactoryException {
           String xLine;
           String longLine;
           StringTokenizer xSt;
           StringTokenizer longSt;

           ////////////////////////
           //setup
           ////////////////////////
           InputStreamReader xIsr = new InputStreamReader(xGridUrl.openStream());
           BufferedReader xBr = new BufferedReader(xIsr);

           InputStreamReader longIsr = new InputStreamReader(longGridUrl.openStream());
           BufferedReader longBr = new BufferedReader(longIsr);

           ////////////////////////
           //read header info
           ////////////////////////
           xLine = xBr.readLine(); //skip header description        
           xLine = xBr.readLine();
           xSt = new StringTokenizer(xLine, " ");

           if (xSt.countTokens() > 8) {
               throw new FactoryException(Errors.format(ErrorKeys.HEADER_UNEXPECTED_LENGTH_$1,
                       String.valueOf(xSt.countTokens())));
           }

           int nc = Integer.parseInt(xSt.nextToken());
           int nr = Integer.parseInt(xSt.nextToken());
           int nz = Integer.parseInt(xSt.nextToken());

           float xStart = Float.parseFloat(xSt.nextToken());
           float xStep = Float.parseFloat(xSt.nextToken());
           float yStart = Float.parseFloat(xSt.nextToken());
           float yStep = Float.parseFloat(xSt.nextToken());

           // float angle = Float.parseFloat(latSt.nextToken());
           float xmax = xStart + ((nc - 1) * xStart);
           float ymax = yStart + ((nr - 1) * yStep);

           //now read long shift grid
           longLine = longBr.readLine(); //skip header description
           longLine = longBr.readLine();
           longSt = new StringTokenizer(longLine, " ");

           if (longSt.countTokens() > 8) {
               throw new FactoryException(Errors.format(ErrorKeys.HEADER_UNEXPECTED_LENGTH_$1,
                       String.valueOf(longSt.countTokens())));
           }

           //check that latitude grid header is the same as for latitude grid
           if ((nc != Integer.parseInt(longSt.nextToken()))
                   || (nr != Integer.parseInt(longSt.nextToken()))
                   || (nz != Integer.parseInt(longSt.nextToken()))
                   || (xStart != Float.parseFloat(longSt.nextToken()))
                   || (xStep != Float.parseFloat(longSt.nextToken()))
                   || (yStart != Float.parseFloat(longSt.nextToken()))
                   || (yStep != Float.parseFloat(longSt.nextToken()))) {
               // || (angle != Float.parseFloat(longSt.nextToken()))) {
               throw new FactoryException(Errors.format(ErrorKeys.GRID_LOCATIONS_UNEQUAL));
           }

           ////////////////////////
           //read grid shift data into LocalizationGrid
           ////////////////////////    
           int i = 0;
           int j = 0;
           float[] warpPos = new float[2 * (nc) * (nr)];
           
          
           for (i = 0; i < nr; i++) {
               for (j = 0; j < nc;) {
                   xLine = xBr.readLine();
                   xSt = new StringTokenizer(xLine, " ");
                   longLine = longBr.readLine();
                   longSt = new StringTokenizer(longLine, " ");

                   while (xSt.hasMoreTokens() && longSt.hasMoreTokens()) {     
                        warpPos[2*j+nc*i*2] = xStart + j*xStep  +(float) Float.parseFloat(xSt.nextToken());
                        warpPos[2*j+nc*i*2 +1 ] = yStart + i*yStep + (float) Float.parseFloat(longSt.nextToken());
                       ++j;
                   }
               }
           }
          

           return new WarpGridTransform2D((int) xStart, (int) xStep, nc-1, (int) yStart, (int) yStep, nr-1, warpPos);
       }

        
    }
}
