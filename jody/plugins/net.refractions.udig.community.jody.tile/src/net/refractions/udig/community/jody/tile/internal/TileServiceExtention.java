package net.refractions.udig.community.jody.tile.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.geotools.catalog.Catalog;
import org.geotools.tile.TileServer;
import org.geotools.tile.TileService;

import com.sun.jndi.toolkit.url.UrlUtil;

import sun.misc.UUEncoder;
import sun.net.www.protocol.file.FileURLConnection;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.catalog.util.GeoToolsAdapters;

/**
 * TileServiceExtension will create a TileServer using
 * GeoToolsAdapters utility class.
 * 
 * @author Jody Garnett
 * @since 1.1.0
 */
public class TileServiceExtention implements ServiceExtension {
    /**
     *  Quickly change to URI to work with geotools service.
     */
    public Map<String, Serializable> createParams( URL url ) {
        if( isGoodURL( url )){
            Map<String, Serializable> map = new HashMap<String, Serializable>();
            URI uri = toURI( url ); // make a "safe uri" incase of files with spaces
            if( url != null ){
                map.put( "uri", uri );
                try {
                    map.put( "url", uri.toURL() );
                } catch (MalformedURLException e) {
                    // try and update url with something "uri safe"
                    return null;
                }
            }
            return map;
        }
        return null;
    }
    
    private URI toURI( URL url ){
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            //e.printStackTrace();
        }
        if( "file".equalsIgnoreCase(url.getProtocol() ) ){
            File file;            
            
            // having some platform specific troubles?            
            file = new File( url.toString() );
            if( file.exists() ){
                return file.toURI();
            }
            return toSafeURI( url );
        }        
        return null;
    }
    
    static URI toSafeURI(URL url){
        if( "file".equalsIgnoreCase(url.getProtocol() ) &&
            url.toExternalForm().indexOf(' ') != -1){
            
            String string = url.toExternalForm();
            StringBuffer build = new StringBuffer( string );
            for( int found = build.indexOf(" "); found != -1; found = build.indexOf(" ")){
                build.replace( found, found+1, "%20" );                        
            }                
            string = build.toString();
            try {
                return new URI( string );
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
    private boolean isGoodURL( URL url ){
        return url.toExternalForm().endsWith(".xml");
    }

    /**
     * Wraps an existing GeoTools service up for fun.
     */
    public IService createService( URL id, Map<String, Serializable> params ){
        if(  params.get("url") == null ||
            !params.get("url").toString().endsWith(".xml") ){
            return null;
        }
        try {
            Catalog localCatalog = GeoToolsAdapters.getLocalCatalog();
            
            TileService service = new TileService( localCatalog, params );
            return GeoToolsAdapters.service( service );            
        }
        catch( Exception ignore ){
            ignore.printStackTrace();
            return null; // indicate provided params are not for us
        }        
    }    
}