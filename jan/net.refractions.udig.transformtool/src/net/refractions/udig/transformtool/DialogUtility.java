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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.ServiceFactoryImpl;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.operation.builder.MathTransformBuilder;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;


/**
 * The methods for TransfomDialog are implemented here.
 *
 * @author jezekjan
 */
class DialogUtility {
    private int i = 1;
    private String name;

    public void transClick() {
        IMap map = ApplicationGIS.getActiveMap();
        String path = "";

        try {
            if (map == null) {
                return;
            }

            IBlackboard blackboard = map.getBlackboard();
            LayerImpl sourceLayer = (LayerImpl) blackboard.get(TransformTool.BLACKBOARD_SOURCELAYER);

            Shell shell = Display.getDefault().getActiveShell();

           // if (isRasterLayer(sourceLayer)) {
                DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
                dialog.setText("Directory for results (File names will be generated automatically)");
                path = dialog.open();
          //  }

            

            if (path == null) {
                shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                Dialog dial = new TransformDialog(shell);
                dial.open();
                return;
            }

            if ((blackboard.get(TransformTool.BLACKBOARD_CALCULATOR) == null)
                    || (blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER) == null)) {
                throw new NullPointerException();
            }

            shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

            new ProgressMonitorDialog(shell).run(true, true,
                new LayerTransform(path, sourceLayer,
                    ((MathTransformBuilder)blackboard.get(TransformTool.BLACKBOARD_CALCULATOR)).getMathTransform()));

            ((Map) map).getEditManagerInternal().setSelectedLayer(sourceLayer);
        } catch (Exception e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Transformation ",
                e.toString());
        }
    }

    /**
     * Invokes the progress monitor that reads the Layer.
     */
    protected void readLayer() {
        try {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

            new ProgressMonitorDialog(shell).run(true, true, new VectorLayerReader());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the vector Layer with current name.
     *
     * @param name name
     */
    protected void createLayer(String name, List<AttributeDescriptor> descriptors) {
        try {
            Map map = (Map) ApplicationGIS.getActiveMap();
            IBlackboard blackboard = map.getBlackboard();
            Layer sourceLayer = (Layer) blackboard.get(TransformTool.BLACKBOARD_SOURCELAYER);

        //    AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom",
         //           LineString.class, true, 0, null, sourceLayer.getCRS());
            IGeoResource resource = createResource(name, descriptors);//, new AttributeType[] { geom });
            Layer vectorLayer;
            vectorLayer = map.getLayerFactory().createLayer(resource);
            vectorLayer.setCRS(sourceLayer.getCRS());

            blackboard.put(TransformTool.BLACKBOARD_VECTORLAYER, vectorLayer);

            vectorLayer.setBounds(sourceLayer.getBounds(null, sourceLayer.getCRS()));
            map.getLayersInternal().add(vectorLayer);
            ((Map) map).getEditManagerInternal().setSelectedLayer(vectorLayer);
        } catch (Exception f) {
            f.printStackTrace();
        }
    }

    protected IGeoResource createResource(String name, List<AttributeDescriptor> descriptors) {
        // String orig = new String(name);    	
        this.name = name;

        IGeoResource resource = null;

        FeatureType feature;

        String oldname = name;
        int i = 1;
        String newname = " ";

        while (newname != oldname) {
            newname = oldname;

            try {
            	SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
                builder.setName(newname);//SimpleFeatureTypeBuilder.newFeatureType(atrributetype, newname);
               // builder.add("the_geom", LineString.class);
                builder.addAll(descriptors);
                 feature = builder.buildFeatureType();
                resource = (CatalogPlugin.getDefault().getLocalCatalog()
                                         .createTemporaryResource(feature));
            } catch (Exception e) {
                oldname = name + i;
                i++;
            }
        }

        return resource;
    }

    protected IGeoResource createCoverageResource(String name, Coverage coverage) {
        IGeoResource resource = (CatalogPlugin.getDefault().getLocalCatalog()
                                              .createTemporaryResource(coverage));

        return resource;
    }

    public static boolean addURLToMap(URL url) throws IOException {
        List<IService> services = new ServiceFactoryImpl().acquire(url);

        ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
        catalog.add(services.get(0));

        IMap map = ApplicationGIS.getActiveMap();

        return searchServiceForResource(new NullProgressMonitor(), map.getMapLayers().size(), map,
            services.get(0));
    }

    public static boolean searchServiceForResource(IProgressMonitor progressMonitor,
        int addPosition, IMap map, IService found) throws IOException {
        List<?extends IGeoResource> resources = found.resources(progressMonitor);

        // now find the resource you want.
        for (IGeoResource resource : resources) {
            if (true) {
                // ok we've found it 
                // add the resource to the map and return
                ApplicationGIS.addLayersToMap(map, Collections.singletonList(resource), addPosition);

                 return true;
            }
        }

        return false;
    }

    public boolean isRasterLayer(LayerImpl layer) {
        if (layer.getGeoResource().canResolve(GridCoverageReader.class)) {
            return true;
        }

        return false;
    }
}
