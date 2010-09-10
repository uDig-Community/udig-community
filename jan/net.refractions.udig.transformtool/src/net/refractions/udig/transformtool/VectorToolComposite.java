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

import java.util.List;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.tool.IToolManager;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.LineString;


class VectorToolComposite {
	private RadioGroupComposite radios;
	Composite parent;
    protected VectorToolComposite(Composite parent) {
    	this.parent = parent;
    }

    protected void addWidgets(GridData gridData, RadioGroupComposite radios) {
    	this.radios = radios;
    	IToolManager manager = ApplicationGIS
        .getToolManager();

    	Group group = new Group(parent, SWT.NULL);
        group.setText("Transformation method:");      
        group.setLayoutData(gridData);

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;     
       
        layout.makeColumnsEqualWidth = true;                    
        group.setLayout(layout);   
             
        GridData localGridData = new GridData();
        localGridData.verticalSpan = 1;
        localGridData.horizontalSpan = 2;
        localGridData.horizontalAlignment = SWT.FILL;
       // localGridData.minimumWidth = group.getSize().x*2;
        final Label labelvector = new Label(group, SWT.SINGLE);
        labelvector.setText("Vector Layer (leave empty to create new):   ");
        labelvector.setLayoutData(localGridData);

           IMap map = ApplicationGIS.getActiveMap();

           localGridData = new GridData();
           localGridData.verticalSpan = 1;
           localGridData.horizontalSpan = 2;
           localGridData.horizontalAlignment = GridData.FILL; 
        final ComboViewer comboVector = new ComboViewer(group, SWT.SINGLE);
        comboVector.setLabelProvider(new LayerLabelProvider());
        comboVector.setContentProvider(new ArrayContentProvider());
        comboVector.setInput(map.getMapLayers().toArray());
     
        comboVector.getCombo().setLayoutData(localGridData);
        
        IBlackboard blackboard = map.getBlackboard();
   	    if (blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER) != null) {
            comboVector.setSelection((ISelection) (new StructuredSelection(
                    (Layer) blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER))),
                true);
        }
        if (blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER) != null) {
            comboVector.setSelection((ISelection) (new StructuredSelection(
                    (Layer) blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER))),
                true);
        }
       
    
        comboVector.addSelectionChangedListener(new ComboHandler());
        
        Button addButton = new Button(group, SWT.BUTTON1);
        addButton.setData( manager.getTool("net.refractions.udig.ui.transformtool.lineEdit",
        "net.refractions.udig.tool.edit.create"));
        addButton.setText("Add Vectors");        
        localGridData = new GridData();
        localGridData.verticalSpan = 1;
        localGridData.horizontalSpan = 1;
        localGridData.horizontalAlignment = GridData.FILL; 
        
        addButton.setLayoutData(localGridData);
        addButton.addSelectionListener(new AddHandler());

        Button deleteButton = new Button(group, SWT.BUTTON1);
        deleteButton.setData(manager.getTool("net.refractions.udig.ui.transformtool.deleteTool",
        "net.refractions.udig.tool.edit.feature"));
        deleteButton.setText("Remove Vectors");
        deleteButton.addSelectionListener(new AddHandler());
       deleteButton.setLayoutData(localGridData);
      
    }
    
    private class ComboHandler implements ISelectionChangedListener {
        public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event
                .getSelection();
            IMap map = ApplicationGIS.getActiveMap();
            IBlackboard blackboard = map.getBlackboard();
            blackboard.put(TransformTool.BLACKBOARD_VECTORLAYER,
                (LayerImpl) selection.getFirstElement());
          
            (new DialogUtility()).readLayer();          
            radios.refresh();
            
        }
    };
    
    private class AddHandler extends SelectionAdapter {
    	public void widgetSelected(SelectionEvent event) {
    		parent.getShell().setVisible(false);
    		final Button button = (Button) event.widget;
            //  	createLayer("vector");
            Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                                               
                        IMap map = ApplicationGIS.getActiveMap();
                        IBlackboard blackboard = map.getBlackboard();
                        Layer sourceLayer = (Layer)blackboard.get(
                                TransformTool.BLACKBOARD_SOURCELAYER);
                        if (blackboard.get(
                                    TransformTool.BLACKBOARD_VECTORLAYER) == null) {
                        	
                        	SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
                        	builder.setName(sourceLayer.getName() + "_vector" );                        	
                        	builder.setCRS( sourceLayer.getCRS() );
                        	builder.add( "Location", LineString.class );
                        
                        	SimpleFeatureType FLAG = builder.buildFeatureType();

                        	//SimpleFeature flag1 = SimpleFeatureBuilder.build( FLAG, new Object[]{ point, "Here"}, "flag.1" );

                        	List<AttributeDescriptor> attributes = FLAG.getAttributeDescriptors();
                            (new DialogUtility()).createLayer( sourceLayer
                                .getName() + "_vector", FLAG.getAttributeDescriptors());
                           
                        }
                        Layer vectorLayer = (Layer)blackboard.get(
                                TransformTool.BLACKBOARD_VECTORLAYER);
                        ((Map) map).getEditManagerInternal()
                         .setSelectedLayer(vectorLayer);
                        ((IAction)button.getData()).run();                     
                    }
                });
        }
    }
}
