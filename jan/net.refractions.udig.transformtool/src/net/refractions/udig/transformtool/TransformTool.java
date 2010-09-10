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
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.tool.AbstractActionTool;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;



/**
 * Action that invokes the TransformDialog.
 *
 * @author jezekjan
 */
public class TransformTool extends AbstractActionTool {
    /** Key for putting source layer on the blackboard. */
    public static final String BLACKBOARD_SOURCELAYER = "net.refractions.udig.transformtool.VectorLayer";

    /** Key for putting vector layer on the blackboard. */
    public static final String BLACKBOARD_VECTORLAYER = "net.refractions.udig.transformtool.SourceLayer";

    /** Key for putting MathTransform on the blackboard. */
  //  public static final String BLACKBOARD_MATHTRANSFORM = "net.refractions.udig.transformtool.mathTransform";

    /** Key for putting ParameterCalculator. */
    public static final String BLACKBOARD_CALCULATOR = "net.refractions.udig.transformtool.calculator";

    /**
     * This method is called upon plug-in activation
     */
    public void run() {
        IMap map = ApplicationGIS.getActiveMap();

        if (map == null) {
            return;
        }

        IBlackboard blackboard = map.getBlackboard();
        blackboard.clear();

        Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    Shell shell = PlatformUI.getWorkbench()
                                            .getActiveWorkbenchWindow()
                                            .getShell();

                    //	 MessageDialog.openInformation(
                    //			 shell,"Readme Editor","View Action executed");
                    Dialog dialog = new TransformDialog(shell);

                    int code = dialog.open();

                    if (code == Window.CANCEL) {
                        return;
                    }
                }
            });
    }

    /**
     * Clears the blackboard.
     */
    public void dispose() {
        IMap map = ApplicationGIS.getActiveMap();

        if (map == null) {
            return;
        }

        IBlackboard blackboard = map.getBlackboard();
        blackboard.clear();
    }
    
    
}
