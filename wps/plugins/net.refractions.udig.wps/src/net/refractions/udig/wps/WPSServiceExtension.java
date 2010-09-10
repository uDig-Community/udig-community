/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.wps;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.wps.WPSServiceImpl;
import net.refractions.udig.wps.internal.Messages;

/**
 * WPS Service Extension.
 * 
 * @author gdavis, Refractions Research
 */
public class WPSServiceExtension implements ServiceExtension {

    public Map<String, Serializable> createParams( URL url ) {
        if (!isWPS(url)) {
            return null;
        }

        // wps check
        Map<String, Serializable> params2 = new HashMap<String, Serializable>();
        params2.put(WPSServiceImpl.WPS_URL_KEY, url);

        return params2;
    }

    public IService createService( URL id, Map<String, Serializable> params ) {
        if (params == null)
            return null;

        if ((!params.containsKey(WPSServiceImpl.WPS_URL_KEY) && id == null)
                && !params.containsKey(WPSServiceImpl.WPS_WPS_KEY)) {
            return null; // nope we don't have a WPS_URL_KEY
        }

        URL extractedId = extractId(params);
        if (extractedId != null) {
            if (id != null)
                return new WPSServiceImpl(id, params);
            else
                return new WPSServiceImpl(extractedId, params);
        }

        return null;
    }

    private URL extractId( Map<String, Serializable> params ) {
        if (params.containsKey(WPSServiceImpl.WPS_URL_KEY)) {
            URL base = null; // base url for service

            if (params.get(WPSServiceImpl.WPS_URL_KEY) instanceof URL) {
                base = (URL) params.get(WPSServiceImpl.WPS_URL_KEY); // use provided url for base
            } else {
                try {
                    base = new URL((String) params.get(WPSServiceImpl.WPS_URL_KEY)); // upcoverting
                                                                                        // string to
                                                                                        // url for
                                                                                        // base
                } catch (MalformedURLException e1) {
                    // log this?
                    e1.printStackTrace();
                    return null;
                }
                params.remove(params.get(WPSServiceImpl.WPS_URL_KEY));
                params.put(WPSServiceImpl.WPS_URL_KEY, base);
            }
            // params now has a valid url

            return base;
        }
        return null;
    } 
    
    public static boolean isWPS( URL url ) {
        return processURL(url) == null;
    }    

    private static String processURL( URL url ) {
        if (url == null) {
            return Messages.WPSServiceExtension_nullURL;
        }

        String PATH = url.getPath();
        String QUERY = url.getQuery();
        String PROTOCOL = url.getProtocol();
        if (PROTOCOL==null || PROTOCOL.indexOf("http") == -1) { //$NON-NLS-1$ supports 'https' too.
            return Messages.WPSServiceExtension_protocol + "'"+PROTOCOL+"'"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if( QUERY != null && QUERY.toUpperCase().indexOf( "SERVICE=" ) != -1){ //$NON-NLS-1$
            int indexOf = QUERY.toUpperCase().indexOf( "SERVICE=" ); //$NON-NLS-1$
            // we have a service! it better be wfs            
            if( QUERY.toUpperCase().indexOf( "SERVICE=WPS") == -1 ){ //$NON-NLS-1$
                int endOfExp = QUERY.indexOf('&', indexOf);
                if( endOfExp == -1 )
                    endOfExp=QUERY.length();
                if( endOfExp>indexOf+8)
                    return Messages.WPSServiceExtension_badService+QUERY.substring(indexOf+8, endOfExp );
                else{
                    return Messages.WPSServiceExtension_badService+""; //$NON-NLS-1$
                }
            }
        } else if (PATH != null && PATH.toUpperCase().indexOf("GEOSERVER/WPS") != -1) { //$NON-NLS-1$
            return null;
        }
        return null; // try it anyway
    }    
    
    public String reasonForFailure( Map<String, Serializable> params ) {
        URL id = extractId(params);
        if (id == null)
            return Messages.WPSServiceExtension_needsKey + WPSServiceImpl.WPS_URL_KEY
                    + Messages.WPSServiceExtension_nullValue;
        return reasonForFailure(id);
    }

    public String reasonForFailure( URL url ) {
        return processURL(url);
    }    
}
