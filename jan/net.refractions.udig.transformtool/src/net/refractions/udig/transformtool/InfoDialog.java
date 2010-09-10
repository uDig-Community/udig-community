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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * The dialog for showing  informations about selected transformation.
 *
 * @author jezekjan
 */
public class InfoDialog extends Dialog {
    String info;
/**
 * Adds the {@link #info} to the Dialog.
 * @param parentShell
 * @param info to be shown
 */
    public InfoDialog(Shell parentShell, String info) {
        super(parentShell);

        this.info = info;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Text text = new Text(parent, SWT.MULTI);
        text.append(info);
        text.setEditable(false);

        
        return super.createDialogArea(parent);
    }
}
