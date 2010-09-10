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
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditToolConfigurationHelper;
import net.refractions.udig.tools.edit.behaviour.AcceptOnDoubleClickBehaviour;
import net.refractions.udig.tools.edit.behaviour.DrawCreateVertexSnapAreaBehaviour;
import net.refractions.udig.tools.edit.behaviour.MoveGeometryBehaviour;
import net.refractions.udig.tools.edit.behaviour.MoveVertexBehaviour;
import net.refractions.udig.tools.edit.behaviour.SelectFeatureBehaviour;
import net.refractions.udig.tools.edit.behaviour.SelectVertexBehaviour;
import net.refractions.udig.tools.edit.behaviour.SelectVertexOnMouseDownBehaviour;
import net.refractions.udig.tools.edit.behaviour.SelectVerticesWithBoxBehaviour;
import net.refractions.udig.tools.edit.behaviour.SetSnapSizeBehaviour;
import net.refractions.udig.tools.edit.behaviour.StartEditingBehaviour;
import net.refractions.udig.tools.edit.impl.LineTool;
import net.refractions.udig.tools.edit.support.ShapeType;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.LineString;


/**
 * Tool for vector drawing. On double click the TransformDialog is
 * activated.
 *
 * @author jezekjan
 */
public class VectorTool extends LineTool {
    boolean start = true;
    int i;

    @Override
    protected void onMouseDoubleClicked(MapMouseEvent e) {
        /*  if (!start) {
           super.onMouseDoubleClicked(e);
           }
         **/
        Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    //	 MessageDialog.openInformation(
                    //			 shell,"Readme Editor","View Action executed");
                    IMap map = ApplicationGIS
                        .getActiveMap();

                    if (map == null) {
                        return;
                    }

                    IBlackboard blackboard = map.getBlackboard();
                    LayerImpl vectorLayer = (LayerImpl) ((Map) map).getEditManagerInternal()
                                                         .getSelectedLayer();
                    blackboard.put(TransformTool.BLACKBOARD_VECTORLAYER,
                        vectorLayer);

                    Shell shell = PlatformUI.getWorkbench()
                                            .getActiveWorkbenchWindow()
                                            .getShell();

                    // vektor leyer is check and read here:
                    (new DialogUtility()).readLayer();

                    Dialog dial = new TransformDialog(shell);
                    dial.open();
                }
            });
    }

    @Override
    protected void initEventBehaviours(EditToolConfigurationHelper helper) {
        helper.add(new DrawCreateVertexSnapAreaBehaviour());

        helper.startAdvancedFeatures();
       //  helper.add( new CursorControlBehaviour(handler, new ConditionalProvider(handler, "Click to select or create feature", "Click to add vertex, double-click to finish"),
       //        new CursorControlBehaviour.SystemCursorProvider(SWT.CURSOR_SIZEALL),new ConditionalProvider(handler, "Drag to move vertex",null), 
       //      new CursorControlBehaviour.SystemCursorProvider(SWT.CURSOR_CROSS), new ConditionalProvider(handler, "Click to add vertex", null)));
        helper.stopAdvancedFeatures();

        //      vertex selection OR geometry selection should not both happen so make them a mutual exclusion behaviour
        helper.startMutualExclusiveList();

        helper.add(new VertexBehaviour());

        helper.startAdvancedFeatures();
        helper.add(new SelectVertexOnMouseDownBehaviour());
        helper.add(new SelectVertexBehaviour());
        helper.stopAdvancedFeatures();

        helper.startAdvancedFeatures();

        SelectFeatureBehaviour selectGeometryBehaviour = new SelectFeatureBehaviour(new Class[] {
                    LineString.class}, Intersects.class);
      //  selectGeometryBehaviour.setCreateGeomOnNoneSelect(ShapeType.LINE);
        helper.add(selectGeometryBehaviour);
        helper.add(new VertexBehaviour());

        helper.startElseFeatures();
        helper.add(new StartEditingBehaviour(ShapeType.LINE));
        helper.stopElseFeatures();

        helper.stopAdvancedFeatures();
        helper.stopMutualExclusiveList();

        helper.startAdvancedFeatures();
        helper.startMutualExclusiveList();
        helper.add(new MoveVertexBehaviour());
        helper.add(new MoveGeometryBehaviour());
        helper.stopMutualExclusiveList();

        helper.add(new SelectVerticesWithBoxBehaviour());
        helper.stopAdvancedFeatures();
        helper.add(new AcceptOnDoubleClickBehaviour());
        helper.add(new SetSnapSizeBehaviour());
        helper.done();                
    }

    @Override
    protected void onMouseMoved(MapMouseEvent e) {
        getContext().updateUI(new Runnable() {
                public void run() {
                    final IStatusLineManager statusBar = getContext()
                                                             .getActionBars()
                                                             .getStatusLineManager();

                    statusBar.setErrorMessage(null);
                    statusBar.setMessage(
                        "Set Vetors or double click to return into dialog.");
                }
            });
        // TODO Auto-generated method stub
        super.onMouseMoved(e);
    }
}
