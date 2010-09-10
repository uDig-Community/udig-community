package org.geotools.tile.nasa;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.geotools.tile.TileDraw;
import org.geotools.tile.TileMap;
import org.geotools.tile.TileMapInfo;
import org.geotools.tile.TileProtocol;
import org.geotools.tile.TileServiceInfo;
import org.geotools.tile.TileSet;
import org.geotools.util.NullProgressListener;
import org.geotools.util.ProgressListener;

/**
 * Able to handle a LayerSet.xsd from world wind.
 * <p>
 * The reference file is available here:
 * <a href="http://worldwind25.arc.nasa.gov/layerConfig/earthimages.xml">earthimages.xml</a>
 * <p>
 * The file must follow the LayerSet.xsd schema ...
 * 
 * @author jgarnett
 * @since 1.1.0
 */
public class WorldWindTileProtocol extends TileProtocol {
    
    private URL server;
        
    public WorldWindTileProtocol( URL url ) {
        server = url;
    }

    public TileServiceInfo getInfo( ProgressListener monitor ){        
        return new WorldWindTileServiceInfo( server, monitor );
    }

    /**
     * List<URI> of Ids of children.
     * <p>
     * Please note that only supported children will be listed.
     * </p>
     * @param info
     * @return 
     */
    public List getTileMapIds( TileServiceInfo info, ProgressListener monitor ){
        if( monitor == null ) monitor = new NullProgressListener();
        try{ 
            WorldWindTileServiceInfo parent = (WorldWindTileServiceInfo) info;     
            return parent.childrenIds();
        }
        finally {
            monitor.complete();
        }
    }
    
    /**
     * Aquire tilemap metadata for the provided id.
     *
     * @param monitor
     * @return List<TileMapInfo>
     */
    public TileMapInfo getTileMapInfo( TileServiceInfo info, URI id, ProgressListener monitor ){
        if( monitor == null ) monitor = new NullProgressListener();
        try{
            WorldWindTileServiceInfo parent = (WorldWindTileServiceInfo) info;
            return parent.getInfo( id );        
        }
        finally {
            monitor.complete();
        }
    }
    

    public TileDraw getTileDraw( TileSet tileset ){
        TileMap tileMap = tileset.getTileMap();               
        QuadTileMapInfo tileMapInfo = (QuadTileMapInfo) tileMap.getInfo();
        
        return new WorldWindTileDraw( tileset, tileMapInfo.accessor );
    }
}
