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
package net.refractions.udig.catalog.wfs.cache;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveChangeEvent;
import net.refractions.udig.catalog.IResolveDelta;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.internal.CatalogImpl;
import net.refractions.udig.catalog.internal.ResolveChangeEvent;
import net.refractions.udig.catalog.internal.ResolveDelta;
import net.refractions.udig.catalog.wfs.cache.internal.Messages;
import net.refractions.udig.ui.ErrorManager;
import net.refractions.udig.ui.UDIGDisplaySafeLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.xml.wfs.WFSSchema;

/**
 * Handle for a WFS service.
 * 
 * @author David Zwiers, Refractions Research
 * @since 0.6
 */
public class WFScServiceImpl extends IService {
    private URL identifier = null;
    private Map<String, Serializable> params = null;
    protected Lock rLock = new UDIGDisplaySafeLock();

    /**
     * The key for the type of cache to use.
     * <p>
     * The associated value should be one of:
     * <li>CACHE_MEMORY</li>
     * <li>CACHE_DISK</li>
     * <li>CACHE_NONE</li>
     */
    public static final String CACHE_TYPE_KEY = "CACHE_TYPE"; //$NON-NLS-1$
    /**
     * The location of the cache if a disk cache.
     */
    public static final String CACHE_DIR_KEY = "CACHE_DIR"; //$NON-NLS-1$
    /**
     * The size of the page size.
     */
    public static final String CACHE_PAGE_SIZE_KEY = "CACHE_PAGE_SIZE"; //$NON-NLS-1$
    /**
     * The size of the grid cache.
     * <p>
     * If the feature source has a lot of big feature you'll want the index size to be large. If the
     * feature source has a lot of small features you'll want the index size to be smaller. The
     * <i>ideal</i> size is twice the mean size of the data.
     * </p>
     * <p>
     * This value should be the total number of tiles excpected. If you want 10x10 grid then this
     * connection parameter should be set to 100.
     * </p>
     */
    public static final String GRID_CACHE_SIZE_KEY = "GRID_CACHE_SIZE"; //$NON-NLS-1$

    /**
     * The maximum number of features allowed in the cache.
     * <p>
     * If not specified the default is Integer.MAX_VALUE;
     * </p>
     */
    public static final String CACHE_FEATURE_SIZE_KEY = "CACHE_FEATURE_SIZE"; //$NON-NLS-1$

    /**
     * Key for In Memeory cache.
     */
    public static final String CACHE_MEMORY = "inmemory"; //$NON-NLS-1$
    /**
     * Key for OnDisk cache
     */
    public static final String CACHE_DISK = "ondisk"; //$NON-NLS-1$
    /**
     * Key for no cache
     */
    public static final String CACHE_NONE = "nocache"; //$NON-NLS-1$

    public WFScServiceImpl( URL identifier, Map<String, Serializable> dsParams ) {
        this.identifier = identifier;
        this.params = dsParams;
    }

    /*
     * Required adaptions: <ul> <li>IServiceInfo.class <li>List.class <IGeoResource> </ul>
     * @see net.refractions.udig.catalog.IService#resolve(java.lang.Class,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (adaptee == null)
            return null;
        if (adaptee.isAssignableFrom(WFScDataStore.class)) {
            return adaptee.cast(getDS(monitor));
        }
        return super.resolve(adaptee, monitor);
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee == null)
            return false;
        return adaptee.isAssignableFrom(WFSDataStore.class) || super.canResolve(adaptee);
    }

    public void dispose( IProgressMonitor monitor ) {
        
        if (members != null) {
            int steps = (int) ((double) 99 / (double) members.size());
            for( IResolve resolve : members ) {
                try {
                    SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, steps);
                    resolve.dispose(subProgressMonitor);
                    subProgressMonitor.done();
                } catch (Throwable e) {
                    ErrorManager.get().displayException(e,"Error disposing members of service: " + getIdentifier(), CatalogPlugin.ID); //$NON-NLS-1$
                }
            }
        }
        if (this.ds != null){
            this.ds.dispose();
        }
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#members(org.eclipse.core.runtime.IProgressMonitor)
     */
    public List<WFScGeoResourceImpl> resources( IProgressMonitor monitor ) throws IOException {

        if (members == null) {
            rLock.lock();
            try {
                if (members == null) {
                    getDS(monitor); // load ds
                    members = new LinkedList<WFScGeoResourceImpl>();
                    String[] typenames = ds.getTypeNames();
                    if (typenames != null)
                        for( int i = 0; i < typenames.length; i++ ) {
                            try {
                                members.add(new WFScGeoResourceImpl(this, typenames[i]));
                            } catch (Exception e) {
                                WfsCachePlugin.log("", e); //$NON-NLS-1$
                            }
                        }
                }
            } finally {
                rLock.unlock();
            }
        }
        return members;
    }

    private volatile List<WFScGeoResourceImpl> members = null;

    /*
     * @see net.refractions.udig.catalog.IService#getInfo(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IServiceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        getDS(monitor); // load ds
        if (info == null && ds != null) {
            rLock.lock();
            try {
                if (info == null) {
                    info = new IServiceWFSInfo(ds);
                    IResolveDelta delta = new ResolveDelta(this, IResolveDelta.Kind.CHANGED);
                    ((CatalogImpl) CatalogPlugin.getDefault().getLocalCatalog())
                            .fire(new ResolveChangeEvent(this,
                                    IResolveChangeEvent.Type.POST_CHANGE, delta));
                }
            } finally {
                rLock.unlock();
            }
        }
        return info;
    }
    private volatile IServiceInfo info = null;

    /*
     * @see net.refractions.udig.catalog.IService#getConnectionParams()
     */
    public Map<String, Serializable> getConnectionParams() {
        return params;
    }

    private Throwable msg = null;
    private volatile WFSDataStoreFactory dsf;
    // private volatile WFSDataStore ds = null;
    private volatile WFScDataStore ds = null;
    private static final Lock dsLock = new UDIGDisplaySafeLock();

    WFScDataStore getDS(IProgressMonitor monitor) throws IOException {
		if (ds == null) {
			if (monitor == null)
				monitor = new NullProgressMonitor();
			monitor.beginTask(Messages.WFScServiceImpl_task_name, 3);
			dsLock.lock();
			monitor.worked(1);
			try {
				if (ds == null) {
					if (dsf == null) {
						dsf = new WFSDataStoreFactory();
					}
					
					
					monitor.worked(1);
					if (dsf.canProcess(params)) {
						monitor.worked(1);
						try {
						    String cacheType = (String)params.get(WFScServiceImpl.CACHE_TYPE_KEY);
						    //lets try to connect to the WFSDataStore
						    //HACK: explicitly ask for WFS 1.0
						    URL url = (URL)params.get(WFSDataStoreFactory.URL.key);
						    url = WFSDataStoreFactory.createGetCapabilitiesRequest(url);
						    params = new HashMap<String, Serializable>(params);
						    params.put(WFSDataStoreFactory.URL.key, url);
						    IOException ex = null;
						    try{
						        ds =  new WFScDataStore((WFSDataStore) dsf.createDataStore(params), params);
						    }catch (IOException e){
						        ex = e;
						        
						    }
						    //lets check the cache and see if we can find anything cached						    
						    if (ds == null && ex != null && cacheType != null && cacheType.equals(WFScServiceImpl.CACHE_DISK)){
						        try{
						            ds = new DiskCacheDataStore(params);
						        }catch (IOException e){
						            WfsCachePlugin.log("Cannot connect to wfs server, will try to find a local cache of the data.", e); //$NON-NLS-1$
						            ex = e;
						        }
						        if (ds == null){
						            if (ex != null)
						                throw ex;  
						            else
						                throw new IOException("Unable to connecto to WFS server or cache."); //$NON-NLS-1$
						        }
						        
						    }
							monitor.worked(1);
						} catch (IOException e) {
							msg = e;
							throw e;
						}
					}
				}
			} finally {
				dsLock.unlock();
				monitor.done();
			}
			IResolveDelta delta = new ResolveDelta(this,
					IResolveDelta.Kind.CHANGED);
			((CatalogImpl) CatalogPlugin.getDefault().getLocalCatalog())
					.fire(new ResolveChangeEvent(this,
							IResolveChangeEvent.Type.POST_CHANGE, delta));
		}
		return ds;
	}
    
    /*
     * @see net.refractions.udig.catalog.IResolve#getStatus()
     */
    public Status getStatus() {
        return msg != null ? Status.BROKEN : ds == null ? Status.NOTCONNECTED : Status.CONNECTED;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return msg;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URL getIdentifier() {
        return identifier;
    }

    private class IServiceWFSInfo extends IServiceInfo {

        private WFScDataStore ds;

        IServiceWFSInfo( WFScDataStore resource ) {
            super();
            this.ds = resource;
            icon = AbstractUIPlugin.imageDescriptorFromPlugin(WfsCachePlugin.PLUGIN_ID,
                    "icons/obj16/wfs_obj.16"); //$NON-NLS-1$
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getAbstract()
         */
        public String getAbstract() {
            return ds.getInfo().getDescription();
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getKeywords()
         */
        public Set<String> getKeywords() {
            return ds.getInfo().getKeywords();
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getSchema()
         */
        public URI getSchema() {
            return WFSSchema.NAMESPACE;
        }

        public String getDescription() {
            return getIdentifier().toString();
        }

        public URI getSource() {
            try {
                return getIdentifier().toURI();
            } catch (URISyntaxException e) {
                // This would be bad
                throw (RuntimeException) new RuntimeException().initCause(e);
            }
        }

        public String getTitle() {
            String title = ds.getInfo().getTitle();
            if (title == null) {
                title = getIdentifier() == null ? Messages.WFScServiceImpl_broken : getIdentifier()
                        .toString();
            } else {
                title += " (WFS " + ds.getInfo().getVersion() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            title = "Cached " + title; //$NON-NLS-1$
            return title;
        }
    }
}