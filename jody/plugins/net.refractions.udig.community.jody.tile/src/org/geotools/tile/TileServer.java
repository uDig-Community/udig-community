package org.geotools.tile;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.tile.cache.SimpleTileCache;
import org.geotools.tile.cache.TileCache;
import org.geotools.tile.nasa.WorldWindTileProtocolFactory;
import org.geotools.tile.osgeo.OSGeoTileProtocolFactory;
import org.geotools.util.ProgressListener;

/**
 * Represents a TileServer, allows connection and retreval of TileSets.
 * <p>
 * This class makes use of a "stratagy" object depending on the protocol
 * used by the TileService. Currently the following protocols are supported:
 * <ul>
 * <li>WorldWind: url points to a xml file conformat to the LayerSet.xsd schema
 * <li>OSGeo: url points to an xml file conformant to WMS Tile Server specification
 * </ul>
 * </p>
 * @author Jody Garnett, Refractions Research, Inc.
 */
public final class TileServer {
    
    private TileServiceInfo info;
    
    /** Map<URI,TileMap> each representing a different data layer */
    private Map layers = new HashMap();
    
    TileProtocol protocol;
    TileCache cache;
    
    static private TileProtocol negotiateProtocol( URL server, ProgressListener monitor ) throws IOException{
    	List factories = listAvailableProtocols();
    	Throwable cause = null;
    	for( Iterator i=factories.iterator();i.hasNext();){
    		TileProtocolFactory factory = (TileProtocolFactory) i.next();
    		try {
	    		if( factory.canTile( server )){
	    			return factory.createTileStratagy( server );
	    		}
    		}
    		catch( Throwable t ){
    			// could not support protocol
    			cause = t;
    		}
    	}
    	if( cause != null ){
    		throw (IOException) new IOException("Could not connet to "+server ).initCause( cause );	
    	}
    	else {
    		throw new IOException("No protocol available for "+server );
    	}		
    }
    
    static private List listAvailableProtocols() {
    	List available = new ArrayList(2);
    	available.add( new WorldWindTileProtocolFactory() );
    	available.add( new OSGeoTileProtocolFactory() );
		return available;
	}
    /**
     * Connect to provided URL, blocking operation.
     * @param server Location of server to connect to
     * @param monitor Used to monitor (and cancel) the connection process
     * @throws IOException If server is unavailable, or protocol not supported.
     */
	public TileServer( URL server, ProgressListener monitor ) throws IOException {    	
        protocol = negotiateProtocol( server, null );
        info = protocol.getInfo( monitor );        
        cache = new SimpleTileCache();
    }
    
    /** Incase info was created beforehand */
    public TileServer( TileServiceInfo info ){
        this.info = info;
        this.protocol = info.getTileStratagy();
        this.cache = new SimpleTileCache();
    }
    
    /**
     * Used to provide a custom implementation of tileCache.
     * <p>
     * By default TileServer will use "SimpleTileCache", this method
     * allows you to configure a more approriate implemntation
     * for you application.
     * </p>
     * @param cache TileCache implementation to use.
     */
    public void setTileCache( TileCache cache ){
    	this.cache = cache;
    }
    
    public TileServiceInfo getInfo(){
        return info;
    }
    
    public List getTileMapIds( ProgressListener monitor ){
        return protocol.getTileMapIds( info, monitor );
    }
    
    public TileMapInfo getTileMapInfo( URI id ){
        return getTileMap( id ).getInfo();
    }
    
    public synchronized TileMap getTileMap( URI id ){
        if( layers.containsKey( id )){
            return (TileMap) layers.get( id );
        }        
        TileMapInfo tileMapInfo = protocol.getTileMapInfo( info, id, null );        
        TileMap tileMap = new TileMap( this, tileMapInfo );
        layers.put( id , tileMap );
        return tileMap;
    }

}
