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

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.FeatureTypeFactory;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.LineString;


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

        if (map == null) {
            return;
        }

        try {
            IBlackboard blackboard = map.getBlackboard();
            LayerImpl sourceLayer = (LayerImpl) blackboard.get(TransformTool.BLACKBOARD_SOURCELAYER);

            if ((blackboard.get(TransformTool.BLACKBOARD_MATHTRANSFORM) == null)
                    || (blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER) == null)) {
                throw new NullPointerException();
            }

            //  LayerTransform.transformlayer(sourceLayer, ptSrc, ptDst);
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                    .getShell();

            new ProgressMonitorDialog(shell).run(true, true,
                new LayerTransform(sourceLayer,
                    (MathTransform) blackboard.get(
                        TransformTool.BLACKBOARD_MATHTRANSFORM)));

            ((Map) map).getEditManagerInternal().setSelectedLayer(sourceLayer);
        } catch (Exception e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(),
                "Transformation ", e.toString());
        }
    }

    /**
     * Invokes the progress monitor that reads the Layer.
     */
    protected void readLayer() {
        try {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                    .getShell();

            new ProgressMonitorDialog(shell).run(true, true,
                new VectorLayerReader());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the vector Layer with current name.
     *
     * @param name name
     */
    protected void createLayer2(String name) {
        Map map = (Map) ApplicationGIS.getActiveMap();
        IBlackboard blackboard = map.getBlackboard();
        Layer sourceLayer = (Layer) blackboard.get(TransformTool.BLACKBOARD_SOURCELAYER);

        AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom",
                LineString.class, true, 0, null, sourceLayer.getCRS());

        FeatureType ftVector;

        try {
            ftVector = FeatureTypeFactory.newFeatureType(new AttributeType[] {
                        geom
                    }, name);

            IGeoResource resource = null;

            resource = CatalogPlugin.getDefault().getLocalCatalog()
                                    .createTemporaryResource(ftVector);

            Layer vectorLayer;
            vectorLayer = map.getLayerFactory().createLayer(resource);

            blackboard.put(TransformTool.BLACKBOARD_VECTORLAYER, vectorLayer);

            map.getLayersInternal().add(vectorLayer);
            ((Map) map).getEditManagerInternal().setSelectedLayer(vectorLayer);
        } catch (RuntimeException e) {
           // System.out.println(e.toString());
            i++;
            createLayer(name + "_" + i);
        } catch (Exception f) {
            f.printStackTrace();
        }
    }
    protected void createLayer(String name) {
    	try{
    	  Map map = (Map) ApplicationGIS.getActiveMap();
          IBlackboard blackboard = map.getBlackboard();
          Layer sourceLayer = (Layer) blackboard.get(TransformTool.BLACKBOARD_SOURCELAYER);

    	AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom",
                LineString.class, true, 0, null, sourceLayer.getCRS());
    	IGeoResource resource = createResource(name, new AttributeType[] {geom});  
    	 Layer vectorLayer;
         vectorLayer = map.getLayerFactory().createLayer(resource);

         blackboard.put(TransformTool.BLACKBOARD_VECTORLAYER, vectorLayer);

         map.getLayersInternal().add(vectorLayer);
         ((Map) map).getEditManagerInternal().setSelectedLayer(vectorLayer);
    	} catch (Exception f){
    		f.printStackTrace();
    	}
    	
    }

    protected IGeoResource createResource(String name, AttributeType[] atrributetype  ) {
        // String orig = new String(name);    	
        this.name = name;

        IGeoResource resource = null;

        FeatureType feature; //	pom.setName(name+"_trans_"+i);
                             //newLayer.setName(name);

        String oldname = name;
        int i = 1;
        String newname = " ";

        while (newname != oldname) {
            newname = oldname;

            try {
                feature = FeatureTypeBuilder.newFeatureType(atrributetype,
                        newname);
                resource = (CatalogPlugin.getDefault().getLocalCatalog()
                                         .createTemporaryResource(feature));

               // Map map = (Map) ApplicationGIS.getActiveMap();
                                

                //newLayer = map.getLayerFactory().createLayer(resource);
            } catch (Exception e) {
                oldname = name + i;
                i++;

                // e.printStackTrace();                 
                // newLayer =createLayer2(this.name +"new", Modellayer);                    
            }
        }

        return resource;
    }
}
