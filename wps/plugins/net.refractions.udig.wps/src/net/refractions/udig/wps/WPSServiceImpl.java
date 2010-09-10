/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.wps;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import net.opengis.ows11.LanguageStringType;
import net.opengis.wps10.ProcessBriefType;
import net.opengis.wps10.ProcessDescriptionType;
import net.opengis.wps10.WPSCapabilitiesType;
import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.ui.ErrorManager;
import net.refractions.udig.ui.UDIGDisplaySafeLock;
import net.refractions.udig.wps.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.ows.AbstractWPSGetCapabilitiesResponse;
import org.geotools.data.ows.GetCapabilitiesRequest;
import org.geotools.data.ows.Specification;
import org.geotools.data.wps.WPS1_0_0;
import org.geotools.data.wps.WPSFactory;
import org.geotools.data.wps.WebProcessingService;
import org.geotools.data.wps.request.DescribeProcessRequest;
import org.geotools.data.wps.request.ExecuteProcessRequest;
import org.geotools.data.wps.response.DescribeProcessResponse;
import org.geotools.data.wps.response.ExecuteProcessResponse;
import org.geotools.ows.ServiceException;
import org.xml.sax.SAXException;

/**
 * Connect to a WPS.
 * 
 * @author gdavis, Refractions Research Inc
 * @author Lucas Reed, Refractions Research Inc
 */
public class WPSServiceImpl extends IService {

    /**
     * <code>WPS_URL_KEY</code> field Magic param key for Catalog WPS persistence.
     */
    public static final String WPS_URL_KEY = "net.refractions.udig.wps.WPSServiceImpl.WPS_URL_KEY"; //$NON-NLS-1$
    public static final String WPS_WPS_KEY = "net.refractions.udig.wps.WPSServiceImpl.WPS_WMS_KEY"; //$NON-NLS-1$

    private Map<String, Serializable> params;

    private Throwable error;
    private URL url;

    private volatile WebProcessingService wps = null;
    private volatile WPSServiceInfo info;
    protected final Lock rLock = new UDIGDisplaySafeLock();
    private volatile List<IResolve> members;
    private int currentFolderID = 0;
    private WPSCapabilitiesType caps = null;

    /**
     * Construct <code>WPSServiceImpl</code>.
     * 
     * @param url
     * @param params
     */
    public WPSServiceImpl( URL url, Map<String, Serializable> params ) {
        this.params = params;
        this.url = url;
        if (params.containsKey(WPS_WPS_KEY)) {
            Object obj = params.get(WPS_WPS_KEY);

            if (obj instanceof WebProcessingService) {
                this.wps = (WebProcessingService) obj;
            }
        }
    }

    public Status getStatus() {
        return error != null ? Status.BROKEN : wps == null ? Status.NOTCONNECTED : Status.CONNECTED;
    }
    private static final Lock dsLock = new UDIGDisplaySafeLock();

    /**
     * Aquire the actual geotools WebProcessingService instance.
     * <p>
     * Note this method is blocking and throws an IOException to indicate such.
     * </p>
     * 
     * @param theUserIsWatching
     * @return WebProcessingService instance
     * @throws IOException
     */
    protected WebProcessingService getWPS( IProgressMonitor theUserIsWatching ) throws IOException {
        if (wps == null) {
            dsLock.lock();
            try {
                if (wps == null) {
                    try {
                        if (theUserIsWatching != null) {
                            String message = MessageFormat.format(
                                    Messages.WPSServiceImpl_connecting_to, new Object[]{url});
                            theUserIsWatching.beginTask(message, 100);
                        }
                        URL url1 = (URL) getConnectionParams().get(WPS_URL_KEY);
                        if (theUserIsWatching != null)
                            theUserIsWatching.worked(5);
                        wps = new CustomWPS(url1);
                        if (theUserIsWatching != null)
                            theUserIsWatching.done();
                    } catch (IOException persived) {
                        error = persived;
                        throw persived;
                    } catch (Throwable nak) {
                        IOException broken = new IOException(MessageFormat.format(
                                Messages.WPSServiceImpl_could_not_connect, new Object[]{nak
                                        .getLocalizedMessage()}));
                        broken.initCause(nak);
                        error = broken;
                        throw broken;
                    }
                }
            } finally {
                dsLock.unlock();
            }
        }
        return wps;
    }

    public IServiceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        if (info == null) {
            getWPS(monitor);
            rLock.lock();
            try {
                if (info == null) {
                    info = new WPSServiceInfo(monitor);
                }
            } finally {
                rLock.unlock();
            }
        }
        return info;
    }

    /*
     * @see net.refractions.udig.catalog.IService#resolve(java.lang.Class,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (adaptee == null) {
            return null;
        }

        if (adaptee.isAssignableFrom(IServiceInfo.class)) {
            return adaptee.cast(getInfo(monitor));
        }

        if (adaptee.isAssignableFrom(List.class)) {
            return adaptee.cast(members(monitor));
        }

        if (adaptee.isAssignableFrom(WebProcessingService.class)) {
            return adaptee.cast(getWPS(monitor));
        }

        return super.resolve(adaptee, monitor);
    }

    /**
     * @see net.refractions.udig.catalog.IService#getConnectionParams()
     */
    public Map<String, Serializable> getConnectionParams() {
        return params;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    @Override
    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee == null)
            return false;

        return adaptee.isAssignableFrom(WebProcessingService.class) || super.canResolve(adaptee);
    }
    @Override
    public void dispose( IProgressMonitor monitor ) {
        if (members == null)
            return;

        int steps = (int) ((double) 99 / (double) members.size());
        for( IResolve resolve : members ) {
            try {
                SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, steps);
                resolve.dispose(subProgressMonitor);
                subProgressMonitor.done();
            } catch (Throwable e) {
                ErrorManager.get().displayException(e,
                        "Error disposing members of service: " + getIdentifier(), CatalogPlugin.ID); //$NON-NLS-1$
            }
        }
    }

    /**
     * This Serivce does not provide any GeoResources.
     */
    public List< ? extends IGeoResource> resources( IProgressMonitor monitor ) throws IOException {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IResolve> members( IProgressMonitor monitor ) throws IOException {
        if (null == this.members) {
            rLock.lock();
            try {
                if (caps == null) {
                    try {
                        caps = getWPS(monitor).getCapabilities();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        caps = null;
                    }
                }

                this.members = new LinkedList<IResolve>();
                EList processes = getWPS(monitor).getCapabilities().getProcessOfferings()
                        .getProcess();

                String pString = ""; //$NON-NLS-1$

                // Concatenate the identifiers to request all ProcessDescriptions
                for( int i = 0; i < processes.size(); i++ ) {
                    ProcessBriefType process = (ProcessBriefType) processes.get(i);
                    pString += process.getIdentifier().getValue() + ","; //$NON-NLS-1$
                }

                DescribeProcessRequest req = getWPS(monitor).createDescribeProcessRequest();
                DescribeProcessResponse response = null;

                req.setIdentifier(pString.substring(0, pString.length() - 1));

                try {
                    response = getWPS(monitor).issueRequest(req);
                } catch (ServiceException e) {
                    throw new IOException(e.getMessage());
                }

                List<ProcessDescriptionType> processDescriptionBeans = response.getProcessDesc()
                        .getProcessDescription();
                for( ProcessDescriptionType processDesc : processDescriptionBeans ) {
                    try {
                        WPSFactory processFactory = new WPSFactory(processDesc, this.url);
                        WPSProcessImpl process = new WPSProcessImpl(this, null, processFactory);
                        this.members.add(process);
                        WpsPlugin.log("Added " + processDesc.getIdentifier().getValue(), null); //$NON-NLS-1$
                    } catch (Exception e) {
                        WpsPlugin.log("Failed process schema definition, not adding: "
                                + processDesc.getIdentifier().getValue(), null); //$NON-NLS-1$
                    }
                }
            } finally {
                rLock.unlock();
            }
        }
        return this.members;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return error;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URL getIdentifier() {
        return url;
    }

    class WPSServiceInfo extends IServiceInfo {
        WPSServiceInfo( IProgressMonitor monitor ) {
            try {
                caps = getWPS(monitor).getCapabilities();
            } catch (Throwable t) {
                t.printStackTrace();
                caps = null;
            }

            if (caps == null || caps.getServiceIdentification() == null
                    || caps.getServiceIdentification().getKeywords().isEmpty()) {
                keywords = null;
            } else {
                List<String> s = WPSUtils.getKeywords(caps, null);
                keywords = new String[s.size()];
                keywords = s.toArray(keywords);
            }

            String[] t;
            if (keywords == null) {
                t = new String[2];
            } else {
                t = new String[keywords.length + 2];
                System.arraycopy(keywords, 0, t, 2, keywords.length);
            }

            t[0] = "WPS"; //$NON-NLS-1$
            t[1] = getIdentifier().toString();
            keywords = t;
            icon = AbstractUIPlugin.imageDescriptorFromPlugin(WpsPlugin.ID,
                    "icons/obj16/wms_obj.gif"); //$NON-NLS-1$
        }

        @Override
        public String getAbstract() {
            // return caps == null ? null : caps.getService() == null ? null : caps
            // .getService().get_abstract();
            if (caps == null
                    || caps.getServiceIdentification() == null
                    || caps.getServiceIdentification().getAbstract().isEmpty()
                    || caps.getServiceIdentification().getAbstract().get(0).toString().length() == 0) {
                return null;
            }
            return caps.getServiceIdentification().getAbstract().get(0).toString();
        }

        @Override
        public String getDescription() {
            return getIdentifier().toString();
        }

        // public URI getSchema() {
        // return WPSSchema.NAMESPACE;
        // }

        @Override
        public URI getSource() {
            try {
                return getIdentifier().toURI();
            } catch (URISyntaxException e) {
                // This would be bad
                throw (RuntimeException) new RuntimeException().initCause(e);
            }
        }

        @Override
        public String getTitle() {
            if (caps == null || caps.getServiceIdentification() == null
                    || caps.getServiceIdentification().getTitle().isEmpty()
                    || caps.getServiceIdentification().getTitle().get(0).toString().length() == 0) {
                return getIdentifier().toString();
            }

            Object str = caps.getServiceIdentification().getTitle().get(0);
            String title;

            if (str instanceof LanguageStringType) {
                LanguageStringType a = (LanguageStringType) caps.getServiceIdentification()
                        .getTitle().get(0);
                title = a.getValue().toString();
            } else {
                title = caps.getServiceIdentification().getTitle().get(0).toString();
            }

            return title;
        }
    }

    public static class CustomWPS extends WebProcessingService {

        /**
         * @throws SAXException
         * @throws ServiceException
         * @param serverURL
         * @throws IOException
         */
        public CustomWPS( URL serverURL ) throws IOException, ServiceException, SAXException {
            super(serverURL);
            // if( WpsPlugin.isDebugging( REQUEST ) )
            WpsPlugin.log("Connection to WPS located at: " + serverURL, null); //$NON-NLS-1$
            if (getCapabilities() == null) {
                throw new IOException("Unable to parse capabilities document."); //$NON-NLS-1$
            }
        }

        @Override
        public AbstractWPSGetCapabilitiesResponse issueRequest( GetCapabilitiesRequest arg0 )
                throws IOException, ServiceException {
            WpsPlugin.log("GetCapabilities: " + arg0.getFinalURL(), null); //$NON-NLS-1$
            return super.issueRequest(arg0);
        }

        @Override
        public DescribeProcessResponse issueRequest( DescribeProcessRequest arg0 )
                throws IOException, ServiceException {
            WpsPlugin.log("DescribeProcessRequest: " + arg0.getFinalURL(), null); //$NON-NLS-1$
            return super.issueRequest(arg0);
        }

        @Override
        public ExecuteProcessResponse issueRequest( ExecuteProcessRequest arg0 )
                throws IOException, ServiceException {
            WpsPlugin.log("ExecuteProcessRequest: " + arg0.getFinalURL(), null); //$NON-NLS-1$
            return super.issueRequest(arg0);
        }

        @Override
        protected void setupSpecifications() {
            specs = new Specification[1];
            specs[0] = new WPS1_0_0();
        }
    }

    public int nextFolderID() {
        return currentFolderID++;
    }
}
