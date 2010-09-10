/*
s * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */

package net.refractions.udig.wps.internal.ui;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.wps.WPSServiceExtension;
import net.refractions.udig.wps.WpsPlugin;

import net.refractions.udig.wps.internal.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;

import org.geotools.data.wfs.protocol.http.AbstractHttpProtocol;
import org.geotools.data.wfs.protocol.http.HTTPProtocol;
import org.geotools.data.wfs.protocol.http.SimpleHttpProtocol;
import org.geotools.data.wfs.protocol.wfs.Version;
/**
 * Simple WPS server specification wizard page
 * 
 * @author Lucas Reed, Refractions Research Inc
 */
public class ImportWizardPage0 extends WizardPage implements Listener {
    private Text url = null;
    public IService service = null;

    private static final String WPS_WIZARD = "WPS_WIZARD"; //$NON-NLS-1$
    private static final String WPS_RECENT = "WPS_RECENT"; //$NON-NLS-1$
    private IDialogSettings settings;
    private Combo urlCombo;

    public ImportWizardPage0( String arg0 ) {
        super(arg0);
        this.setTitle(Messages.WPSWizardPage_title);
        this.setDescription(Messages.WPSWizardPage_label_url_text);

        /* Added for WPS RECENT START */

        settings = WpsPlugin.getDefault().getDialogSettings().getSection(WPS_WIZARD);
        if (settings == null) {
            settings = WpsPlugin.getDefault().getDialogSettings().addNewSection(WPS_WIZARD);
        }

        /* Added for WPS RECENT END */
    }

    @Override
    public boolean isPageComplete() {
        return null != this.service;
    }

    @Override
    public boolean canFlipToNextPage() {
        URL url = null;

        try {
            url = new URL(this.url.getText());
            url = createGetCapabilitiesRequest(url);
        } catch (MalformedURLException e) {
            return false;
        }

        WPSServiceExtension service = new WPSServiceExtension();

        Map<String, Serializable> params = service.createParams(url);

        this.service = service.createService(url, params);

        return true;
    }

    public static URL createGetCapabilitiesRequest( URL host ) {
        if (host == null) {
            throw new NullPointerException("null url");
        }

        HTTPProtocol httpUtils = new SimpleHttpProtocol();
        Map<String, String> getCapsKvp = new HashMap<String, String>();
        getCapsKvp.put("SERVICE", "WPS");
        getCapsKvp.put("REQUEST", "GetCapabilities");
        URL getcapsUrl;
        try {
            getcapsUrl = httpUtils.createUrl(host, getCapsKvp);
        } catch (MalformedURLException e) {
            // logger.log(Level.WARNING, "Can't create GetCapabilities request from " + host, e);
            throw new RuntimeException(e);
        }

        return getcapsUrl;
    }

    public void createControl( Composite parent ) {

        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout gl = new GridLayout();
        gl.numColumns = 2;
        composite.setLayout(gl);

        new Label(composite, SWT.NONE).setText(Messages.WPSWizardPage_url_field_label);
        this.url = new Text(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        this.url.setLayoutData(gd);

        this.setControl(composite);
    }

    public void handleEvent( Event event ) {
        this.setPageComplete(null != this.service);
    }
}
