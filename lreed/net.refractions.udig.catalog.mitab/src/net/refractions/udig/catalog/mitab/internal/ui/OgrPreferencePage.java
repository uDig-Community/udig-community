/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2008, AmanziTel
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package net.refractions.udig.catalog.mitab.internal.ui;

import java.io.File;

import net.refractions.udig.catalog.mitab.internal.Activator;
import net.refractions.udig.catalog.mitab.internal.Messages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference pane for ogr2ogr executable selection
 *
 * @author Lucas Reed, (Refractions Research Inc)
 * @since 1.1.0
 */
@SuppressWarnings("nls")
public class OgrPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    public  static final String executablePathKey = "executable_path";
    private              String executablePath    = null;

    protected Control createContents(Composite parent) {
        final Composite  comp   = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout(3, false);

        layout.marginBottom = 0;
        layout.marginTop    = 0;
        layout.marginRight  = 0;
        layout.marginLeft   = 0;

        comp.setLayout(layout);

        // ---

        Label label = new Label(comp, SWT.FLAT);
        label.setText(Messages.Ogr2OgrPreference_selectExecutable);
        GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        label.setLayoutData(layoutData);

        final Text input = new Text(comp, SWT.SINGLE | SWT.BORDER);

        input.addModifyListener(new ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                if ("".equals(input.getText())) {
                    return;
                }

                File executable = new File(input.getText());

                if (false == executable.exists()) {
                    error(Messages.Ogr2OgrPreference_fileNotFound, input);
                } else {
                    clearError(input);
                }
            };
        });

        this.executablePath = getPreferenceStore().getString(OgrPreferencePage.executablePathKey);
        if (0 == executablePath.trim().length()) {
            this.executablePath = null;
        }
        input.setText(this.executablePath == null ? "" : this.executablePath);

        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        input.setLayoutData(layoutData);

        // ----

        Button browse = new Button(comp, SWT.PUSH);
        browse.setText(Messages.Ogr2OgrPreference_browseButton);

        browse.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(comp.getShell(), SWT.OPEN);
                fileDialog.setFilterExtensions(new String[]{"*"});
                fileDialog.setFilterNames(new String[]{Messages.Ogr2OgrPreference_execuables});

                executablePath = fileDialog.open();

                if (null != executablePath) {
                    input.setText(executablePath);
                }
            }
        });

        layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        browse.setLayoutData(layoutData);

        return comp;
    }

    private void error(String message, Text field) {
        setMessage(message, IStatus.WARNING);
        field.setForeground(new Color(getShell().getDisplay(), 255, 0, 0));
        field.setToolTipText(message);
    }

    private void clearError(Text field) {
        setMessage("", IStatus.OK);
        field.setForeground(new Color(getShell().getDisplay(), 0, 0, 0));
        field.setToolTipText("");
    }

    public void init(IWorkbench workbench) {
        // Intentionally blank
    }

    @Override
    public boolean performOk() {
        getPreferenceStore().putValue(OgrPreferencePage.executablePathKey, this.executablePath);

        return true;
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        Activator        ac = Activator.getInstance();
        IPreferenceStore ps = ac.getPreferenceStore();

        return ps;
    }
}