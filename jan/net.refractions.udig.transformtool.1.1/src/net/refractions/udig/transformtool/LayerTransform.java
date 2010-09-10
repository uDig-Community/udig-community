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

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SimpleFeature;
import org.geotools.feature.SimpleFeatureType;
import org.opengis.referencing.operation.MathTransform;


/**
 * The transformation is made here.
 *
 * @author jezekjan
 */
public class LayerTransform implements IRunnableWithProgress {
    LayerImpl sourceLayer;
    MathTransform transform;

    LayerTransform(LayerImpl sourceLayer, MathTransform transform) {
        this.sourceLayer = sourceLayer;
        this.transform = transform;
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
        try {
            FeatureSource source = (FeatureSource) sourceLayer
                .getResource(FeatureSource.class, null);
           
           
            FeatureCollection collection = new MemoryFeatureCollection(source
                    .getSchema());                            

          
           Iterator ii =source.getFeatures().iterator();
           monitor.beginTask("Transforming...", source.getFeatures().size());
           
           TransformFilter tf = new TransformFilter(transform);
           Map map = (Map) ApplicationGIS.getActiveMap();
            monitor.subTask("Applying specified transformation...");
            collection.addAll(source.getFeatures());
            for (Iterator j = collection.iterator(); j.hasNext();) {
                ((Feature) j.next()).getDefaultGeometry().apply(tf);
                monitor.worked(1);

                /*
                 * monitor.subTask("Transforming " + i + ". of " +
                 * source.getFeatures().size() + " features."); i++;
                 */
                if (monitor.isCanceled()) {
                    throw new InterruptedException(
                        "The transformation operation was cancelled.");
                }
            }

            monitor.subTask("Adding features to new layer...");
            IGeoResource resource = (new DialogUtility()).createResource(sourceLayer.getName()+"_trans", sourceLayer.getSchema().getAttributeTypes());
            resource.resolve(FeatureStore.class, null).addFeatures(collection);
            ApplicationGIS.addLayersToMap(map,
                Collections.singletonList(resource), 0);

            monitor.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
