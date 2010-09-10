package net.refractions.udig.community.jody.tile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IService;

import org.eclipse.ui.IStartup;
import org.geotools.tile.nasa.WorldWindTileProtocol;

/**
 * Used to preload some nice datasets (ie worldwind).
 */
public class Preload implements IStartup {

    public void earlyStartup() {
        Map<String,Serializable> params = new HashMap<String,Serializable>();
        params.put( "url", WorldWindTileProtocol.class.getResource("earthimages.xml") );
        
        List<IService> match = CatalogPlugin.getDefault().getServiceFactory().acquire( params );
        if( !match.isEmpty()){
            ICatalog local = CatalogPlugin.getDefault().getLocalCatalog();
            local.add( match.get(0));
        }         
    }

}
