package org.geotools.tile;

import java.net.URI;
import java.util.List;

import org.geotools.util.ProgressListener;

public abstract class TileProtocol {

    public abstract TileServiceInfo getInfo( ProgressListener monitor );    

    public abstract TileMapInfo getTileMapInfo( TileServiceInfo info, URI id, ProgressListener monitor );
    
    /**
     * TileDraw used to render indicated TileSet.
     * <p>
     * @param tileset
     * @return TileDraw
     */
    public abstract TileDraw getTileDraw( TileSet tileset );
    
    /**
     * List<URI> of children identifiers.
     * <p>
     * Each uri indicates a valid TileMap that may be aquired from this
     * service.
     * </p>
     * @param info
     * @param monitor
     * @return List<URI>
     */
    public abstract List getTileMapIds( TileServiceInfo info, ProgressListener monitor );
}