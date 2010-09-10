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

        GridLayout layout3 = new GridLayout();
        layout3.numColumns = 2;
        
        group.setLayout(layout3);

           
        final Label labelvector = new Label(group, SWT.SINGLE);
        labelvector.setText("Vector Layer (leave empty to create new):");

        gridData = new GridData();
        gridData.verticalSpan = 1;
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        labelvector.setLayoutData(gridData);

           IMap map = ApplicationGIS.getActiveMap();

        final ComboViewer comboVector = new ComboViewer(group, SWT.SINGLE);
        comboVector.setLabelProvider(new LayerLabelProvider());
        comboVector.setContentProvider(new ArrayContentProvider());
        comboVector.setInput(map.getMapLayers().toArray());
        gridData = new GridData();
        gridData.verticalSpan = 8;
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = GridData.FILL;
      //  gridData.verticalAlignment   = GridData.FILL;
        comboVector.getCombo().setLayoutData(gridData);
        
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
        
        gridData = new GridData();
        gridData.verticalSpan = 1;
        gridData.horizontalSpan = 1;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;   
        addButton.setLayoutData(gridData);
        addButton.addSelectionListener(new AddHandler());

        Button deleteButton = new Button(group, SWT.BUTTON1);
        deleteButton.setData(manager.getTool("net.refractions.udig.ui.transformtool.deleteTool",
        "net.refractions.udig.tool.edit.feature"));
        deleteButton.setText("Remove Vectors");
        deleteButton.addSelectionListener(new AddHandler());
       deleteButton.setLayoutData(gridData);
      
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
                            (new DialogUtility()).createLayer(sourceLayer
                                .getName() + "_vector");
                           
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
