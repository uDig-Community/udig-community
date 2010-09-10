package org.geotools.tile.nasa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.tile.TileDraw;
import org.geotools.tile.TileMap;
import org.geotools.tile.TileMapInfo;
import org.geotools.tile.TileSet;
import org.geotools.tile.ZoomLevel;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class WorldWindTileDraw extends TileDraw {
    int tile=0;

    private TileSet tileset;

    private Accessor accessor;
    
    static GridCoverageFactory factory = new GridCoverageFactory();
    
    WorldWindTileDraw( TileSet tileset, Accessor accessor ){
        this.tileset = tileset;
        this.accessor = accessor;
    }
    
    @Override
    public String name( int row, int col ) {
        String name = tileset.getTileMap().getInfo().getName();
        name += tileset.getZoomLevel().getScaleDenominator();
        name += "grid"+col+"x"+row;
        return name;
    }
    @Override
    public GridCoverage2D drawPlaceholder( int row, int col ) {
        Envelope2D rectangle = createRectangle( row, col );
        RenderedImage image=createEmpty( row, col );
        return factory.create( name(row,col), image, rectangle );
    }
    
    @Override
    public GridCoverage2D drawTile( int row, int col ) {
        Envelope2D rectangle = createRectangle( row, col );        
        try {            
            RenderedImage image = accessor.tileImage(tileset, row, col );
            if (image == null ) {
                image=createEmpty( row, col );
            }
            return factory.create( name(row,col), image, rectangle );
        }
        catch (IOException error){
            RenderedImage image=createImage( error );            
            return factory.create( error.getMessage(), image, rectangle );
        }
    }

    private RenderedImage createImage( Throwable e ) {
        BufferedImage image = new BufferedImage( 90, 90, BufferedImage.TYPE_INT_ARGB );        
        Graphics2D g = (Graphics2D) image.getGraphics();        
        g.setColor( Color.RED );
        String message = e.getMessage();
        g.drawString( message, 30, 45 );
        return image;
    }

    private Envelope2D createRectangle( int row, int col ){        
        ZoomLevel level = tileset.getZoomLevel();
        TileMap tileMap = tileset.getTileMap();
        TileMapInfo tileMapInfo = tileMap.getInfo();
        
        CoordinateReferenceSystem crs = tileMapInfo.getCRS();        
        Envelope bounds = tileMapInfo.getBounds();
        
        DirectPosition2D p1 = location(bounds, level, row, col);
        DirectPosition2D p2 = location(bounds , level, row + 1, col + 1);
        double w = p2.x - p1.x;
        double h = p2.y - p1.y;
        return new Envelope2D( tileMapInfo.getCRS(), p1.x, p1.y, w, h );
    }

    /** Position in the provided ZoomLevel */
    private DirectPosition2D location( Envelope bbox, ZoomLevel level, int row, int col ) {
        double dx = bbox.getWidth() * level.getColRatio(col);
        double dy = bbox.getHeight() * level.getRowRatio(row);
        double x = bbox.getMinX() + dx;
        double y = bbox.getMinY() + dy;
        return new DirectPosition2D(DefaultGeographicCRS.WGS84, x, y);
    }
    
    BufferedImage empty;
    RenderedImage createEmpty( int row, int col ){
        if( empty != null ) return empty;
        
        empty = new BufferedImage( 45, 45, BufferedImage.TYPE_INT_ARGB );        
        Graphics2D g = (Graphics2D) empty.getGraphics();        
        g.setColor( Color.BLACK );
        String message = "x";
        g.drawString( message, 30, 45 );
        
        return empty;
    }
    
    RenderedImage createImage( int row, int col ){        
        BufferedImage image = new BufferedImage( 45, 45, BufferedImage.TYPE_INT_ARGB );        
        Graphics2D g = (Graphics2D) image.getGraphics();        
        g.setColor( Color.BLACK );
        String message = row+"x"+col;
        g.drawString( message, 30, 45 );
        return image;
    }
}