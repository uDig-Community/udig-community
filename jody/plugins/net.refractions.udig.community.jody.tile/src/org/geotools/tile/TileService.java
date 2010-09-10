package org.geotools.tile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.catalog.AbstractService;
import org.geotools.catalog.Catalog;
import org.geotools.catalog.ServiceInfo;
import org.geotools.tile.nasa.WorldWindTileProtocol;
import org.geotools.util.ProgressListener;

/**
 * Service handle responsible for creating a TileServer.
 * <p>
 * This is a handle only; it can be used to:
 * <ul>
 * <li>Access TileInfo
 * <li>Access TileServer
 * <ul>
 */
public class TileService extends AbstractService {
    
    private TileServiceInfo info;
    private TileServer server;
    private List members;
    
    public TileService( Catalog parent, Map params ) {
        super(parent, params);        
    }
    
    /**
     * Provides a TileServiceInfo.
     */
    public synchronized ServiceInfo getInfo( ProgressListener monitor ) throws IOException {
        if( info != null ) return info;
        // should process available stratagies...
        Object connect =getConnectionParams().get("url");
        if( connect instanceof URL){
            TileProtocol stratagy = new WorldWindTileProtocol( (URL) getConnectionParams().get("url") );
            info = stratagy.getInfo( monitor );
        }
        else if( connect instanceof String){
            System.out.println("Udig did not preserve my URL");
            TileProtocol stratagy = new WorldWindTileProtocol( new URL( (String) connect) );
            info = stratagy.getInfo( monitor );            
        }
        else {
            throw new IOException("Connection 'url' incorrect:"+connect);
        }
        return info;
    }

    public synchronized TileServer getServer(ProgressListener monitor) throws IOException {
        if( server != null ) return server;
        
        server = new TileServer( (TileServiceInfo) getInfo( monitor ));
        return server;
    }
    
    public synchronized List members( ProgressListener monitor ) throws IOException {
        if( members != null ) return members;        
        TileServer server = getServer( null );
        List ids = server.getTileMapIds( monitor );
        
        members = new ArrayList( ids.size() );
        int count=0;
        for( Iterator i=ids.iterator(); i.hasNext(); ){
            count++;
            URI id = (URI) i.next();
            String str = id.toString();
            int split = str.indexOf("#");
            if( split != -1 ){
                str = str.substring(split+1);
                if( "null".equals(str)){
                    str = String.valueOf(count);
                }
            }
            
            try {
                URL url = new URL( getIdentifier().toString() + "#"+ str );
                URI identifier = new URI( getIdentifier()+"#"+str );
                members.add( new TileResource( this, id, identifier ));                
            } catch (URISyntaxException e) {
                members.add( new TileResource( this, id ));
            }
        }
        return members;
    }
    
    public Object resolve( Class adaptee, ProgressListener monitor ) throws IOException {
        if( adaptee == TileService.class ){
            return getServer(monitor);
        }
        if( adaptee == List.class ){
            return members(monitor);
        }
        return null;
    }

    public boolean canResolve( Class adaptee ) {
        return adaptee == TileServer.class ||
               adaptee == List.class ||
               adaptee == TileServiceInfo.class;
    }

    public URI getIdentifier() {
        Object value = getConnectionParams().get("url");
        if( value == null ){
            throw new RuntimeException( "Parameter 'url' is required for identifier" );
        }
        else if( value instanceof String){
            try {
                return new URI( (String) value );
            } catch (URISyntaxException e) {
                throw (RuntimeException) new RuntimeException( ).initCause( e );            
            }
        }
        else if( value instanceof URI ){
            URL url = (URL) value;        
            try {
                return url.toURI();
            } catch (URISyntaxException e) {
                throw (RuntimeException) new RuntimeException( ).initCause( e );
            }
        }
        else {
            String string = value.toString();
            try {
                return new URI( string );
            } catch (URISyntaxException e) {
                throw (RuntimeException) new RuntimeException( ).initCause( e );
            }
        }
    }    
}