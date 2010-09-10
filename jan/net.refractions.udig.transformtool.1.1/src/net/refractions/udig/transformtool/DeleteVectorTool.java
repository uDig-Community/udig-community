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

import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.impl.DeleteTool;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


/**
 * Deletes a feature from the currently selected layer or the top layer. On
 * double click the TransformDialog is open.
 *
 * @author jezekjan
 */
public class DeleteVectorTool extends DeleteTool {
    public DeleteVectorTool() {
        super();
    }

    /**
     * The TransformDialog is opened on double click.
     *
     * @param event
     */
    @Override
    public void mouseDoubleClicked(MapMouseEvent event) {
        Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    Shell shell = PlatformUI.getWorkbench()
                                            .getActiveWorkbenchWindow()
                                            .getShell();

                    (new DialogUtility()).readLayer();

                    Dialog dial = new TransformDialog(shell);

                    dial.open();
                }
            });
        super.mouseDoubleClicked(event);
    }
}
