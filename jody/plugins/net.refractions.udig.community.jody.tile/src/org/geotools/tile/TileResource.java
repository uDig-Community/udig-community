package org.geotools.tile;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.geotools.catalog.GeoResource;
import org.geotools.catalog.GeoResourceInfo;
import org.geotools.catalog.Resolve;
import org.geotools.catalog.ResolveChangeEvent;
import org.geotools.catalog.ResolveChangeListener;
import org.geotools.catalog.Service;
import org.geotools.util.NullProgressListener;
import org.geotools.util.ProgressListener;

public class TileResource implements GeoResource {
    private TileService parent;
    /**
     * This is the "name" used to identify the layer to the TileService.
     * It is usually very specific.
     */
    private URI id;
    /**
     * This is the public name used to record the entry in the uDig catalog.
     */
    private URI identifier;
    private TileMapInfo info;
    private TileMap tileMap;
    
    public TileResource( TileService service, URI id ) {
        parent = service;
        this.id = id;        
        this.identifier = id;
    }
    public TileResource( TileService service, URI id, URI identifier ) {
        parent = service;
        this.id = id;
        this.identifier = identifier;
    }    
    public synchronized GeoResourceInfo getInfo( ProgressListener monitor ) throws IOException {        
        return parent.getServer( monitor ).getTileMapInfo(id);
    }
    /**
     * This is the resource the handle is pointing to.
     *
     * @param monitor
     * @return
     * @throws IOException
     */
    public synchronized TileMap getTileMap( ProgressListener monitor ) throws IOException {        
        return parent.getServer( monitor ).getTileMap(id);
    }

    public Object resolve( Class adaptee, ProgressListener monitor ) throws IOException {
        if( adaptee == TileMap.class ){
            return getTileMap( monitor );
        }
        if( Service.class.isAssignableFrom( adaptee )){
            return parent( monitor ); 
        }
        if( GeoResourceInfo.class.isAssignableFrom( adaptee )){
            return getInfo( monitor ); 
        }
        return null;
    }

    public boolean canResolve( Class adaptee ) {
        return Service.class.isAssignableFrom( adaptee ) ||
               GeoResourceInfo.class.isAssignableFrom( adaptee ) ||
               adaptee == TileMap.class;
    }

    public URI getIdentifier() {
        return identifier;
    }
    public URI getId(){
        return id;
    }

    public Throwable getMessage() {
        return null;
    }

    public Status getStatus() {
        if( tileMap == null ) return Status.NOTCONNECTED;
        return Status.CONNECTED;
    }

    public List members( ProgressListener arg0 ) throws IOException {
        return Collections.EMPTY_LIST;
    }

    public Resolve parent( ProgressListener monitor) throws IOException {
        if( monitor == null ) monitor = new NullProgressListener();
        try {
            return parent;
        }
        finally {
            monitor.complete();
        }
    }
    
    public void fire( ResolveChangeEvent arg0 ) {
    }

    public void addListener( ResolveChangeListener arg0 ) throws UnsupportedOperationException {
    }

    public void removeListener( ResolveChangeListener arg0 ) {
    }
}