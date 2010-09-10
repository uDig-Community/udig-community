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

import java.awt.image.RenderedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;

import javax.media.jai.RenderedOp;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultDerivedCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.operation.LinearTransform;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.DefaultCoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;


/**
 * The transformation is made here.
 *
 * @author jezekjan
 */
public class LayerTransform implements IRunnableWithProgress {
    LayerImpl sourceLayer;
    MathTransform transform;
    String path;

    LayerTransform(String path, LayerImpl sourceLayer, MathTransform transform) {
        this.sourceLayer = sourceLayer;
        this.transform = transform;

        String name = sourceLayer.getName();
        int dot = name.indexOf("i");

        if (dot != 0) {
            try {
                name = sourceLayer.getName().substring(0, dot - 2);
            } catch (Exception e) {
            }
        }

        this.path = path + "/" + name + "_trans";

        /*  for (int i = 1; ;i++){
           URL url= new URL("file://"+path);
        
           }*/
    }

    /**
     * Transforms the {@link #sourceLayer} by {@link #transform}.
     *
     * @param monitor
     *
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public void run(IProgressMonitor monitor)
        throws InvocationTargetException, InterruptedException {
        GridCoverage p;
        IGeoResource res = sourceLayer.getGeoResource();

        if (sourceLayer.getGeoResource().canResolve(GridCoverageReader.class)) {
            transformCoverage(monitor);
        } else if (sourceLayer.getGeoResource().canResolve(FeatureSource.class)) {
            transformFeatureLayer(monitor);
        }
    }

    /**
     * Transoform Coverage
     * @param monitor
     */
    private void transformCoverage(IProgressMonitor monitor) {
        //Transform raster Layer
        try {
            GridCoverageReader reader = (GridCoverageReader) sourceLayer.getResource(GridCoverageReader.class,
                    null);

            monitor.subTask("Reading raster layer...");

            GridCoverage2D csource = (GridCoverage2D) reader.read(null); //(GridCoverage2D)sourceLayer.getResource(Coverage.class, null);

            CoordinateReferenceSystem gridCRS = new DefaultDerivedCRS("gridCRS",
                     csource.getCoordinateReferenceSystem(),
                    transform, DefaultCartesianCS.GENERIC_2D);

            monitor.subTask("Transforming raster layer...");            
            
            GridCoverage2D target;
           

            /**
             * Note - when trying to generate geometry automaticaly the results
             * are upside down for LinearTransformations (similar, affine)
             *
             */
            if (LinearTransform.class.isAssignableFrom(transform.getClass())) {
                GeneralGridRange range = (GeneralGridRange) csource.getGridGeometry().getGridRange(); // Keep the same dimension
                GeneralEnvelope envelope = CRS.transform(transform, csource.getEnvelope());
                envelope.setCoordinateReferenceSystem(csource.getCoordinateReferenceSystem());

                GridGeometry2D geometry = new GridGeometry2D(range, envelope);

                target = projectTo(csource, gridCRS, geometry, null, true); //(GridGeometry2D) csource.getGridGeometry()
           } else {
                target = projectTo(csource, gridCRS, null, null, true); //(GridGeometry2D) csource.getGridGeometry()
                Envelope2D en = target.getEnvelope2D();
                en.setCoordinateReferenceSystem(csource.getCoordinateReferenceSystem());
                
                // target coverage should have same crs as target.
                GridCoverageFactory gcf = new GridCoverageFactory();                
                target = gcf.create("target", target.getRenderedImage(), en);
                }

            //target.s
            Envelope targetEnv = CRS.transform(transform, csource.getEnvelope());
//
           // Envelope2D tEnc = new Envelope2D(csource.getCoordinateReferenceSystem(),
             //       targetEnv.getMinimum(0), targetEnv.getMinimum(1), targetEnv.getLength(0),
               //     targetEnv.getLength(1));

            monitor.subTask("Saving raster layer...");

            this.path = path + ".tif";
            GeoTiffWriter writer = new GeoTiffWriter((Object) (new File(path)));
           // WorldImageWriter writer = new WorldImageWriter((Object) (new File(path)));
            writer.write(target, null);

            monitor.subTask("Opening raster layer...");

            DialogUtility.addURLToMap(new URL("file://" + path));
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     * Transform Features Layer
     * @param monitor
     */
    private void transformFeatureLayer(IProgressMonitor monitor) {
        try {
            FeatureSource source = (FeatureSource) sourceLayer.getResource(FeatureSource.class, null);
            monitor.beginTask("Transforming...", source.getFeatures().size());

            // monitor.beginTask("Transforming...", source.getFeatures().size());
            TransformFilter tf = new TransformFilter(transform);
            FeatureCollection collection = new MemoryFeatureCollection((SimpleFeatureType)source.getSchema());

           
            
            int i = 1;
            int col = 1;

            Map map = (Map) ApplicationGIS.getActiveMap();

            collection.addAll(source.getFeatures());

            monitor.subTask("Applying specified transformation...");

            for (Iterator j = collection.iterator(); j.hasNext();) {
                ((Geometry)((SimpleFeatureImpl) j.next()).getDefaultGeometry()).apply(tf);
                monitor.worked(1);

                /*
                 * monitor.subTask("Transforming " + i + ". of " +
                 * source.getFeatures().size() + " features."); i++;
                 */
                if (monitor.isCanceled()) {
                    throw new InterruptedException(
                        "The transformation operation has been canceled.");
                }
            }

            monitor.subTask("Adding features to new layer...");

        /*    IGeoResource resource = (new DialogUtility()).createResource(sourceLayer.getName()
                    + "_trans", sourceLayer.getSchema().getAttributeDescriptors());
            resource.resolve(FeatureStore.class, null).addFeatures(collection);

            ApplicationGIS.addLayersToMap(map, Collections.singletonList(resource), 0);*/

            /**Shapefileeeee*/ 
            FileDataStoreFactorySpi factory = new IndexedShapefileDataStoreFactory();
            //URL url = new URL("file:///home/jezekjan/tmp/linest.shp");
            this.path = path + ".shp";
            URL url = new URL("file://"+path);
            DataStore shpData = factory.createDataStore(url);
            
            shpData.createSchema((SimpleFeatureType)source.getSchema());
            
            DefaultTransaction transaction = new DefaultTransaction("transaction");
             
            //shpData.getFeatureWriter("w", transaction).
            FeatureStore shpStore = (FeatureStore) shpData.getFeatureSource(source.getName());
            shpStore.setTransaction(transaction);
             
            shpStore.addFeatures(collection)        ;   
                                             
            transaction.commit();
            transaction.close();
            DialogUtility.addURLToMap(url);
            monitor.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static GridCoverage2D projectTo(final GridCoverage2D coverage,
        final CoordinateReferenceSystem targetCRS, final GridGeometry2D geometry,
        final Hints hints, final boolean useGeophysics) {
        final AbstractProcessor processor = (hints != null) ? new DefaultProcessor(hints)
                                                            : AbstractProcessor.getInstance();
        final String arg1;
        final Object value1;
        final String arg2;
        final Object value2;

        if (targetCRS != null) {
            arg1 = "CoordinateReferenceSystem";
            value1 = targetCRS;

            if (geometry != null) {
                arg2 = "GridGeometry";
                value2 = geometry;
            } else {
                arg2 = "InterpolationType";
                value2 = "bilinear";
            }
        } else {
            arg1 = "GridGeometry";
            value1 = geometry;
            arg2 = "InterpolationType";
            value2 = "bilinear";
        }

        GridCoverage2D projected = coverage.geophysics(useGeophysics);
        final ParameterValueGroup param = processor.getOperation("Resample").getParameters();
        param.parameter("Source").setValue(projected);
        param.parameter(arg1).setValue(value1);
        param.parameter(arg2).setValue(value2);
        projected = (GridCoverage2D) processor.doOperation(param);

        final RenderedImage image = projected.getRenderedImage();
        projected = projected.geophysics(false);

        String operation = null;

        if (image instanceof RenderedOp) {
            operation = ((RenderedOp) image).getOperationName();
            AbstractProcessor.LOGGER.fine("Applied \"" + operation + "\" JAI operation.");
        }

        return projected;
    }
}
