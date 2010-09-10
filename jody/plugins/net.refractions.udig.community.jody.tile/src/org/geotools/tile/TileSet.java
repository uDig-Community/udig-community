package org.geotools.tile;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.tile.cache.TileRange;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Set of tiles at a provided ZoomLevel.
 * <p>
 * This TIleSet can be understood according to:
 * <ul>
 * <li>getMap().getBounds() - Envelope including CRS
 * <li>getZoomLevel().getRows();
 * <li>getZoomLevel().getRows();
 * </ul>
 */
public final class TileSet {
    TileMap tileMap;
    ZoomLevel level; // valid according to metadata
    TileDraw draw; // provided from stratagy
    private TileServer server;
    
    protected TileSet( TileServer server, TileMap tileMap, ZoomLevel level ){
        this.server = server;
        this.tileMap = tileMap;
        this.level = level;
        this.draw = server.protocol.getTileDraw( this );
    }
    
    public TileMap getTileMap(){
        return tileMap;
    }
    public ZoomLevel getZoomLevel(){
        return level;
    }
    
    /**
     * Request tiles in a provided range
     * @param bbox Understood to match CRS
     * @return
     */
    public TileRange getTileRange( Rectangle range ){
        limitRange( range );        
        return server.cache.createRange( draw, range );
    }
    
    /**
     * Request tiles in a provided range
     * 
     * @param bbox Understood to match CRS
     * @return
     */
    public TileRange getTileRange( Envelope bbox ){
        Envelope bounds = tileMap.getInfo().getBounds();
        Envelope area = bounds.intersection( bbox );
        DirectPosition2D areaMin = new DirectPosition2D( area.getMinX(), area.getMinY() );
        DirectPosition2D areaMax = new DirectPosition2D( area.getMaxX(), area.getMaxY() );
        
        Point min = tileMin( areaMin );        
        Point max = tileMax( areaMax );
        
        Rectangle range = new Rectangle( min );
        range.add( max );
        
        limitRange( range );        
        return getTileRange( range );
    }

    /** Used to limit the range to 3x3 tiles */
    private void limitRange( Rectangle range ) {
        if( range.width > 3 ){
            range.x += range.width/2-1;
            range.width = 3;
        }
        if( range.height > 3 ){
            range.y += range.height/2-1;
            range.height = 3;
        }
    }
    
    /**
     * Find the minimum tile with information about the provided pt.
     * 
     * @param pt Position (in CRS)
     * @return Point( col, row ) for the provided position
     */
    private Point tileMin( DirectPosition2D pt ) {
        Point2D ratio = toRatio( pt );
               
        int col = level.getColFromRatio( ratio.getY() );
        int row = level.getRowFromRatio( ratio.getX() );
        return new Point( col, row );
    }
    /**
     * Find the lower left tile coordinate for the provided pt
     *
     * @param pt Position (in CRS)
     * @return Point( col, row ) for the provided position
     */    
    private Point tileMax( DirectPosition2D pt ){
        Point2D ratio = toRatio( pt );
        
        int col = level.getColFromRatio( ratio.getY() );
        int row = level.getRowFromRatio( ratio.getX() );
        
        if( ratio.getX() == (double)col/(double)level.getNumberOfColumns() ){
            // we are exactly on the edge
        }
        else {
            // we need to include up to the boundary of the next tile                        
            col += (col< level.getNumberOfColumns() ? 1 : 0);
        }
        if( ratio.getY() == (double)row/(double)level.getNumberOfRows() ){
            // we are exactly on the tile boundary
        }
        else {
            // we need to include up to the boundary of the next tile
            row += (row < level.getNumberOfRows() ? 1 : 0);
        }
        return new Point( col, row );
    }
    
    private Point2D toRatio( DirectPosition2D pt ) {
        Envelope bounds = tileMap.getInfo().getBounds();
        
        double dx = pt.getX() - bounds.getMinX();
        double dy = pt.getY() - bounds.getMinY();
        
        double colRatio = dx / bounds.getWidth();
        double rowRatio = dy / bounds.getHeight();

        return new Point2D.Double( rowRatio, colRatio );
    }    
}
