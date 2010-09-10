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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.ui.graphics.Glyph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.ResourceInfo;
import org.geotools.data.wfs.WFSDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Access a feature type in a wfs.
 * 
 * @author David Zwiers, Refractions Research
 * @since 0.6
 */
public class WFScGeoResourceImpl extends IGeoResource {
    WFScServiceImpl parent;
    String typename = null;
    private URL identifier;
    
    @SuppressWarnings("unused")
    private WFScGeoResourceImpl(){/*not for use*/}
    /**
     * Construct <code>WFSGeoResourceImpl</code>.
     *
     * @param parent
     * @param typename
     */
    public WFScGeoResourceImpl(WFScServiceImpl parent, String typename){
        this.parent = parent; 
        this.typename = typename;
        try {
            identifier= new URL(parent.getIdentifier().toString()+"#"+typename); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            identifier= parent.getIdentifier();
        }
    }
    
    public URL getIdentifier() {
        return identifier;
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#getStatus()
     */
    public Status getStatus() {
        return parent.getStatus();
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#getStatusMessage()
     */
    public Throwable getMessage() {
        return parent.getMessage();
    }
    
    /*
     * Required adaptions:
     * <ul>
     * <li>IGeoResourceInfo.class
     * <li>IService.class
     * </ul>
     * @see net.refractions.udig.catalog.IResolve#resolve(java.lang.Class, org.eclipse.core.runtime.IProgressMonitor)
     */
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if(adaptee == null)
            return null;
//        if(adaptee.isAssignableFrom(IService.class))
//            return adaptee.cast( parent );        
        if(adaptee.isAssignableFrom(WFScDataStore.class))
            return parent.resolve( adaptee, monitor );
        if (adaptee.isAssignableFrom(IGeoResource.class))
            return adaptee.cast( this );
        if(adaptee.isAssignableFrom(IGeoResourceInfo.class))
            return adaptee.cast( getInfo(monitor));
        if(adaptee.isAssignableFrom(FeatureStore.class)){
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = parent.getDS(monitor).getFeatureSource(typename);
            if(fs instanceof FeatureStore)
                return adaptee.cast( fs);
        if(adaptee.isAssignableFrom(FeatureSource.class))
            return adaptee.cast( parent.getDS(monitor).getFeatureSource(typename));
        }
        
        return super.resolve(adaptee, monitor);
    }
    public IService service( IProgressMonitor monitor ) throws IOException {
        return parent;
    }
    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public <T> boolean canResolve( Class<T> adaptee ) {
        if(adaptee == null)
            return false;
        return (adaptee.isAssignableFrom(IGeoResourceInfo.class) || 
                adaptee.isAssignableFrom(FeatureStore.class) || 
                adaptee.isAssignableFrom(FeatureSource.class) || 
                adaptee.isAssignableFrom(WFScDataStore.class) ||
                adaptee.isAssignableFrom(IService.class) )||
                super.canResolve(adaptee);
    }
    
    
//    private volatile IGeoResourceInfo info;
//    public IGeoResourceInfo getInfo(IProgressMonitor monitor) throws IOException{
//        if(info == null && getStatus()!=Status.BROKEN){
//            parent.rLock.lock();
//            try{
//                if(info == null){
//                    info = new IGeoResourceWFSInfo();
//                }
//            }finally{
//                parent.rLock.unlock();
//            }
//        }
//        return info;
//    }

    class IGeoResourceWFSInfo extends IGeoResourceInfo {
        CoordinateReferenceSystem crs = null;
        
        IGeoResourceWFSInfo() throws IOException{
            
            WFSDataStore ds = parent.getDS(null);
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = ds.getFeatureSource(typename);
            ResourceInfo resourceInfo = featureSource.getInfo();
			SimpleFeatureType ft = ds.getSchema(typename);
			if (resourceInfo != null){
			    bounds = resourceInfo.getBounds();
			    description = resourceInfo.getDescription();
			    title = resourceInfo.getTitle();
			}else{
			    bounds = null;
			    description = ""; //$NON-NLS-1$
			    title = ""; //$NON-NLS-1$
			}
            crs = ft.getCoordinateReferenceSystem();
            
            name = typename;
            try {
				schema = new URI( ft.getName().getNamespaceURI());
			} catch (URISyntaxException e) {
				schema = null;
			}
              
            keywords = new String[]{
                "wfs", //$NON-NLS-1$
                typename,
                ft.getName().getNamespaceURI()
            };

            icon=Glyph.icon(ft);
        }
        
        /*
         * @see net.refractions.udig.catalog.IGeoResourceInfo#getCRS()
         */
        public CoordinateReferenceSystem getCRS() {
            if(crs != null)
                return crs;
            return super.getCRS();
        }
    }

    @Override
    protected IGeoResourceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        if(info == null && getStatus()!=Status.BROKEN){
            parent.rLock.lock();
            try{
                if(info == null){
                    info = new IGeoResourceWFSInfo();
                }
            }finally{
                parent.rLock.unlock();
            }
        }
        return info;
    }
}