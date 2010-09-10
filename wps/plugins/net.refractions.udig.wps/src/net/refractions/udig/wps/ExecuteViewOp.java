/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */

package net.refractions.udig.wps;

import net.refractions.udig.catalog.IProcess;
import net.refractions.udig.ui.operations.IOp;
import net.refractions.udig.wps.internal.ui.ProcessView;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Used to display the execute configuration view
 *
 * @author Lucas Reed, Refractions Research Inc
 * @author Graham Davis, Refractions Research Inc
 */
public class ExecuteViewOp implements IOp {
	public void op(Display display, Object target, IProgressMonitor monitor)throws Exception {

	    final IProcess process = (IProcess)target;

		// We can't see the active workbench window from this context so we have to get in
		// the display thread to find it and add a view to it		
		display.asyncExec( new Runnable(){
            public void run() {
                IWorkbench workbench = PlatformUI.getWorkbench();
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();        
                IWorkbenchPage page = window.getActivePage();
                try {
                    IViewPart view = page.showView( ProcessView.VIEW_ID );
                    // Now set the process info in the newly created/opened view
                    ProcessView processView = (ProcessView) view;
                    processView.setProcess(process);
                } catch (PartInitException e) {
                    // TODO: log this
                }
            }		    
		});		
			
	}
}
