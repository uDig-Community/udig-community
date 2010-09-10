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
package net.refractions.udig.catalog.usg;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;

import com.vividsolutions.jts.geom.Envelope;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ICatalogInfo;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveChangeListener;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.memory.MemoryService;
import net.refractions.udig.catalog.memory.internal.MemoryGeoResourceImpl;
import net.refractions.udig.catalog.memory.internal.MemoryServiceImpl;

/**
 * TODO Purpose of 
 * <p>
 *
 * </p>
 * @author Jody Garnett
 * @since 1.0.0
 */
public class USGCatalog extends ICatalog {
    private Exception die;

    @Override
    public void add( IService service ) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove( IService service ) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replace( URL id, IService service ) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /*
     * Required adaptions:
     * <ul>
     * <li>ICatalogInfo.class
     * <li>List.class <IService>
     * </ul>
     * @see net.reurl.fractions.udig.catalog.IResolve#resolve(java.lang.Class, org.eclipse.core.runtime.IProgressMonitor)
     */
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException{
        if(adaptee == null)
            return null;
        if(adaptee.isAssignableFrom(ICatalogInfo.class)){
            return adaptee.cast(getInfo(monitor));
        }
        if(adaptee.isAssignableFrom(List.class)){
            return adaptee.cast(members(monitor));
        }
        return null;
    }

    @Override
    public List<IResolve> search( String pattern, Envelope bbox, IProgressMonitor monitor )
            throws IOException {
        AddressSeeker seek = new AddressSeeker();
        List<Feature> stuff;
        try {
            stuff = seek.geocode( pattern );
        } catch (IOException e) {
            return null;
        } catch (XmlRpcException e) {
            return null;
        }        
        MemoryServiceImpl memory = new MemoryServiceImpl();
        MemoryGeoResourceImpl handle = new MemoryGeoResourceImpl("Address", memory );
        
        MemoryDataStore store = handle.resolve( MemoryDataStore.class, monitor );
        store.addFeatures( stuff );
        
        List<IResolve> results = new ArrayList<IResolve>();
        results.add( handle );
        return results;
    }

    @Override
    public void addCatalogListener( IResolveChangeListener listener ) {
    }

    @Override
    public void removeCatalogListener( IResolveChangeListener listener ) {
    }

    public <T> boolean canResolve( Class<T> adaptee ) {
        return false;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#members(org.eclipse.core.runtime.IProgressMonitor)
     */
    public List< ? extends IResolve> members( IProgressMonitor monitor ) throws IOException{
        return new LinkedList<IResolve>();
    }
    
    /*
     * @see net.refractions.udig.catalog.ICatalog#find(java.net.URL)
     */
    public List<IResolve> find( URL id ) {
        return new LinkedList<IResolve>();
    }

    public Status getStatus() {
        return null;
    }

    public Throwable getMessage() {
        return null;
    }

    public URL getIdentifier() {
        return null;
    }

}
