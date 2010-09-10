/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.wps.internal.ui;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.UDIGConnectionFactory;
import net.refractions.udig.wps.WPSProcessImpl;
import net.refractions.udig.wps.WPSServiceExtension;
import net.refractions.udig.wps.WPSServiceImpl;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.wps.WebProcessingService;

public class WPSConnectionFactory extends UDIGConnectionFactory {

	public boolean canProcess(Object context) {
		 if( context instanceof IResolve ){
           IResolve resolve = (IResolve) context;
           return resolve.canResolve( WebProcessingService.class );
       }
       return toCapabilitiesURL(context) != null;        
	}
	
	public Map<String, Serializable> createConnectionParameters(Object context) {
		  if( context instanceof IResolve  ){
	            Map params = createParams( (IResolve) context );
	            if( !params.isEmpty() ) return params;            
	        } 
	        URL url = toCapabilitiesURL( context );
	        if( url == null ){
	            // so we are not sure it is a wps url
	            // lets guess
	            url = CatalogPlugin.locateURL(context);
	        }
	        if( url != null ) {
	            // well we have a url - lets try it!            
	            List<IResolve> list = CatalogPlugin.getDefault().getLocalCatalog().find( url, null );
	            for( IResolve resolve : list ){
	                Map params = createParams( resolve );
	                if( !params.isEmpty() ) return params; // we got the goods!
	            }
	            return createParams( url );            
	        }        
	        return Collections.EMPTY_MAP;
	}

	static public Map<String,Serializable> createParams( IResolve handle ){
        if( handle instanceof WPSServiceImpl) {
            // got a hit!
            WPSServiceImpl wps = (WPSServiceImpl) handle;
            return wps.getConnectionParams();
        }
        else if (handle instanceof WPSProcessImpl ){
            WPSProcessImpl layer = (WPSProcessImpl) handle;
            WPSServiceImpl wps;
            try {
                wps = layer.service( new NullProgressMonitor());
                return wps.getConnectionParams();
            } catch (IOException e) {
                checkedURL( layer.getIdentifier() );
            }                    
        }
        else if( handle.canResolve( WebProcessingService.class )){
            // must be some kind of handle from a search!
            return createParams( handle.getIdentifier() );
        }
        return Collections.EMPTY_MAP;
    }
	
	/** 'Create' params given the provided url, no magic occurs */
    static public Map<String,Serializable> createParams( URL url ){
        WPSServiceExtension factory = new WPSServiceExtension();
        Map params = factory.createParams( url );
        if( params != null) return params;
        
        Map<String,Serializable> params2 = new HashMap<String,Serializable>();
        params2.put(WPSServiceImpl.WPS_URL_KEY,url);
        return params2;
    }

    
	 /**
     * Convert "data" to a wps capabilities url
     * <p>
     * Candidates for conversion are:
     * <ul>
     * <li>URL - from browser DnD
     * <li>URL#process - from browser DnD
     * <li>WPSService - from catalog DnD
     * <li>WPSProcess - from catalog DnD
     * <li>IService - from search DnD
     * </ul>
     * </p>
     * <p>
     * No external processing should be required here, it is enough to guess and let
     * the ServiceFactory try a real connect.
     * </p>
     * @param data IService, URL, or something else
     * @return URL considered a possibility for a WPS Capabilities, or null
     */
    static URL toCapabilitiesURL( Object data ) {
        if( data instanceof IResolve ){
            return toCapabilitiesURL( (IResolve) data );
        }
        else if( data instanceof URL ){
            return toCapabilitiesURL( (URL) data );
        }
        else if( CatalogPlugin.locateURL(data) != null ){
            return toCapabilitiesURL( CatalogPlugin.locateURL(data) );
        }
        else {
            return null; // no idea what this should be
        }
    }

    static URL toCapabilitiesURL( IResolve resolve ){
        if( resolve instanceof IService ){
            return toCapabilitiesURL( (IService) resolve );
        }
        return toCapabilitiesURL( resolve.getIdentifier() );        
    }

    static URL toCapabilitiesURL( IService resolve ){
        if( resolve instanceof WPSServiceImpl ){
            return toCapabilitiesURL( (WPSServiceImpl) resolve );
        }
        return toCapabilitiesURL( resolve.getIdentifier() );        
    }

    /** No further QA checks needed - we know this one works */
    static URL toCapabilitiesURL( WPSServiceImpl wps ){
        return wps.getIdentifier();                
    }

    /** Quick sanity check to see if url is a WPS url */
    static URL toCapabilitiesURL( URL url ){
        if (url == null) return null;
    
        String path = url.getPath() == null ? null : url.getPath().toLowerCase();
        String query = url.getQuery() == null ? null : url.getQuery().toLowerCase();
        String protocol = url.getProtocol() == null ? null : url.getProtocol().toLowerCase();
    
        if (!"http".equals(protocol) //$NON-NLS-1$
                && !"https".equals(protocol)) { //$NON-NLS-1$ 
            return null;
        }
        if (query != null && query.indexOf("service=wps") != -1) { //$NON-NLS-1$
            return checkedURL( url );
        }else if( query != null && query.indexOf("service=") == -1 && query.indexOf("request=getcapabilities") != -1){ //$NON-NLS-1$ //$NON-NLS-2$
            try {
                return new URL( url.toString()+"&SERVICE=WPS"); //$NON-NLS-1$
            } catch (MalformedURLException e) {
                return null;
            }
        }
        
        if (path != null && path.toUpperCase().indexOf("GEOSERVER/WPS") != -1 ) { //$NON-NLS-1$
            return checkedURL( url );
        }
        if (url.toExternalForm().indexOf("WPS") != -1) { //$NON-NLS-1$
            return checkedURL( url );
        }
        return null;
    }
    
    /** Check that any trailing #layer is removed from the url */
    static public URL checkedURL( URL url ){
        String check = url.toExternalForm();
        int hash = check.indexOf('#');
        if ( hash == -1 ){
            return url;            
        }
        try {
            return new URL( check.substring(0, hash ));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
	public URL createConnectionURL(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

}
