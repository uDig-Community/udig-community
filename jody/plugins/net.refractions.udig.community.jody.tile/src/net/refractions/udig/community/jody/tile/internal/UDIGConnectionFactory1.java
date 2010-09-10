package net.refractions.udig.community.jody.tile.internal;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.wms.WebMapServer;
import org.geotools.tile.TileResource;
import org.geotools.tile.TileServer;
import org.geotools.tile.TileService;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.UDIGConnectionFactory;

public class UDIGConnectionFactory1 extends UDIGConnectionFactory {

    public UDIGConnectionFactory1() {
    }

    public boolean canProcess( Object context ) {
        if (context instanceof IResolve) {
            IResolve resolve = (IResolve) context;
            return resolve.canResolve(TileServer.class);
        }
        return createConnectionURL( context ) != null;
    }

    public Map<String, Serializable> createConnectionParameters( Object context ) {
        if (context instanceof IResolve) {
            Map params = createParams((IResolve) context);
            if (!params.isEmpty())
                return params;
        }
        URL url = toCapabilitiesURL(context);
        if (url == null) {
            // so we are not sure it is a wms url
            // lets guess
            url = CatalogPlugin.locateURL(context);
        }
        if (url != null) {
            // well we have a url - lets try it!
            List<IResolve> list = CatalogPlugin.getDefault().getLocalCatalog().find(url, null);
            for( IResolve resolve : list ) {
                Map params = createParams(resolve);
                if (!params.isEmpty())
                    return params; // we got the goods!
            }
            return createParams(url);
        }
        return Collections.EMPTY_MAP;
    }

    static private Map<String, Serializable> createParams( IResolve handle ) {        
        if (handle instanceof TileService) {
            TileService tile = (TileService) handle;
            return tile.getConnectionParams();
        }
        else if (handle instanceof TileResource) {
            TileResource layer = (TileResource) handle;
            TileResource wms;
            try {
                TileService tile = (TileService) layer.parent(null);
                return tile.getConnectionParams();
            } catch (IOException e) {                
            }
        } else if (handle.canResolve(TileServer.class)) {
            // must be some kind of handle from a search!
            // (or a geotools wrap up)
            return createParams(handle.getIdentifier());
        }
        return Collections.EMPTY_MAP;
    }

    /** 'Create' params given the provided url, no magic occurs */
    static private Map<String, Serializable> createParams( URL url ) {
        TileServiceExtention factory = new TileServiceExtention();
        Map params = factory.createParams(url);
        if (params != null) return params;

        Map<String, Serializable> params2 = new HashMap<String, Serializable>();
        params2.put("url", url);
        return params2;
    }

    /**
     * Convert "data" to a wms capabilities url
     * <p>
     * Candidates for conversion are:
     * <ul>
     * <li>URL - from browser DnD
     * <li>URL#layer - from browser DnD
     * <li>WMSService - from catalog DnD
     * <li>WMSGeoResource - from catalog DnD
     * <li>IService - from search DnD
     * </ul>
     * </p>
     * <p>
     * No external processing should be required here, it is enough to guess and let the
     * ServiceFactory try a real connect.
     * </p>
     * 
     * @param data IService, URL, or something else
     * @return URL considered a possibility for a WMS Capabilities, or null
     */
    static URL toCapabilitiesURL( Object data ) {
        if (data instanceof IResolve) {
            return toCapabilitiesURL((IResolve) data);
        } else if (data instanceof URL) {
            return toCapabilitiesURL((URL) data);
        } else if (CatalogPlugin.locateURL(data) != null) {
            return toCapabilitiesURL(CatalogPlugin.locateURL(data));
        } else {
            return null; // no idea what this should be
        }
    }

    public URL createConnectionURL( Object context ) {
        URL url = CatalogPlugin.locateURL( context );        
        if( url == null ) return null;
        
        String string = url.toString();
        if( string.endsWith(".xml")){
            return url;
        }
        return null;
    }

}
