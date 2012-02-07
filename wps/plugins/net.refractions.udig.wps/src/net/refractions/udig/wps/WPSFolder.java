/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
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
package net.refractions.udig.wps;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IResolveManager;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.CatalogUIPlugin;
import net.refractions.udig.catalog.ui.ISharedImages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wps.WebProcessingService;
import org.opengis.feature.type.Name;

/**
 * Since WPSFolder is not a IGeoResource/IProcess but it shares most of its code with
 * {@link WPSProcessImpl} this class exists for sharing that code. If mixins were permitted in
 * Java this wouldn't be necessary... But it is.
 * 
 * This class is based on WMSFolder from {@link net.refractions.udig.catalog.internal.wms}
 * 
 * @author gdavis
 */
public class WPSFolder implements IResolveFolder {

    private WPSServiceImpl service;
    private IResolve parent;
    private org.geotools.process.ProcessFactory processFactory;
    private ArrayList<IResolve> members;
    private URL identifier;
    private ImageDescriptor icon;
    private Lock iconLock = new ReentrantLock();
    private Name name;
    
    /**
     * Construct <code>WPSProcessImpl</code>.
     * 
     * @param service
     * @param parent the parent Process may be null if parent is the service.
     * @param process
     */
    public WPSFolder( WPSServiceImpl service, IResolve parent, org.geotools.process.ProcessFactory processFactory ) {
        this.service = service;
        if (parent == null) {
            this.parent = service;
        } else {
            this.parent = parent;
        }
        this.processFactory = processFactory;
        members = new ArrayList<IResolve>();
//        for( Process child : process.getChildren() ) {
//            if (child != process) {
//                if (child.getName() == null) {
//                    members.add(new WPSFolder(service, this, child));
//                } else {
//                    members.add(new WPSProcessImpl(service, this, child));
//                }
//            }
//        }
        this.name = processFactory.getNames().iterator().next();
        try {
        	String title = processFactory.getTitle( name ).toString();
            if (title == null) {
            	title = name.getLocalPart();
            }
            identifier = new URL(service.getIdentifier().toString() + "#" + name); //$NON-NLS-1$

        } catch (Throwable e) {
            WpsPlugin.log(null, e);
            identifier = service.getIdentifier();
        }
    }

    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee == null) {
            return false;
        }

        if (adaptee.isAssignableFrom(WPSFolder.class)
                || adaptee.isAssignableFrom(WebProcessingService.class)
                || adaptee.isAssignableFrom(org.geotools.process.Process.class)
                || adaptee.isAssignableFrom(ImageDescriptor.class)
                || adaptee.isAssignableFrom(IService.class)) {
            return true;
        }

        return CatalogPlugin.getDefault().getResolveManager().canResolve(this, adaptee);
    }
    
    public void dispose( IProgressMonitor monitor ) {
    }

    public URL getIdentifier() {
        return identifier;
    }

    public ID getID() {
    	return new ID( getIdentifier() );
    }

    public Throwable getMessage() {
        return null;
    }

    public Status getStatus() {
        return Status.CONNECTED;
    }

    public List<IResolve> members( IProgressMonitor monitor ) throws IOException {
        return members;
    }

    public IResolve parent( IProgressMonitor monitor ) throws IOException {
        return parent;
    }

    public IService getService(IProgressMonitor monitor) {
    	return (IService) parent;
    }
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (adaptee == null) {
            throw new NullPointerException();
        }

        if (adaptee.isAssignableFrom(WPSFolder.class)) {
            return adaptee.cast(this);
        }

        if (adaptee.isAssignableFrom(WebMapServer.class)) {
            return adaptee.cast(service.getWPS(monitor));
        }

        if (adaptee.isAssignableFrom(org.geotools.process.Process.class)) {
            return adaptee.cast(this.processFactory);
        }
        if (adaptee.isAssignableFrom(ImageDescriptor.class)) {
            return adaptee.cast(getIcon(monitor));
        }

        IResolveManager rm = CatalogPlugin.getDefault().getResolveManager();
        if (rm.canResolve(this, adaptee)) {
            return rm.resolve(this, adaptee, monitor);
        }
        return null; // no adapter found (check to see if ResolveAdapter is registered?)
    }

    /** Must be the same as resolve( ImageDescriptor.class ) */
    public ImageDescriptor getIcon( IProgressMonitor monitor ) {
        iconLock.lock();
        try {
            if (icon == null) {
                icon = WPSProcessImpl.fetchIcon(monitor, this.processFactory, service);
                if (icon == null) {
                    icon = CatalogUIPlugin.getDefault().getImageRegistry().getDescriptor(
                            ISharedImages.GRID_OBJ);
                }
            }
            return icon;
        } finally {
            iconLock.unlock();
        }
    }

    public String getTitle() {
        return this.processFactory.getTitle(name).toString();
    }

}
