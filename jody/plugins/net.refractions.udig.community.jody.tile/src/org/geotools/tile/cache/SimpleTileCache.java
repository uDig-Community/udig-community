package org.geotools.tile.cache;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.tile.TileDraw;
import org.geotools.util.SimpleInternationalString;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Default that simply caches on TileRange.
 * <p>
 * </p>
 * @author Jody Garnett, Refractions Research Inc.
 */
public class SimpleTileCache implements TileCache {
    
    DirectTileRange cached = null;
    
    public TileRange createRange( TileDraw draw, Rectangle range ) {
        Set loaded = cacheHits( draw, range );
        List tiles = createClearRange(draw, range);
        DirectTileRange tileRange = new DirectTileRange( draw, range, tiles, loaded );        
        if( tileRange.equals( cached )){
            return cached;            
        }
        else {
            cached = tileRange;
        }
        return tileRange;
    }

    synchronized GridCoverage2D cacheHit( String name ){
        if( cached == null ) return null;
        for( Iterator i = cached.getTiles().iterator(); i.hasNext(); ){
            GridCoverage2D tile = (GridCoverage2D) i.next();
            if( name.equals( tile.getName().toString() )){
                return tile;
            }
        }
        return null;
    }
    synchronized Set cacheHits( TileDraw draw, Rectangle range ) {
        if( cached == null ) return Collections.EMPTY_SET;        
        Set hits = new HashSet( range.width*range.height);
        for( int col = (int)range.x; col<range.getMaxX(); col++){
            for( int row = (int)range.y; row<range.getMaxY(); row++){
                String name = draw.name(row, col);
                if( cached.loaded.contains(name) ){
                    hits.add( name );
                }
            }
        }
        return hits;
    }
    
    synchronized List createClearRange( TileDraw draw, Rectangle range ) {
        List clear = new ArrayList( range.width*range.height);
        for( int col = (int)range.x; col<range.getMaxX(); col++){
            for( int row = (int)range.y; row<range.getMaxY(); row++){
                String name = draw.name(row, col);
                GridCoverage2D hit = cacheHit( name );
                if( hit != null ){
                    clear.add( hit );
                }
                else {
                    clear.add( draw.drawPlaceholder( row, col ) );
                }
            }
        }
        return clear;
    }
    
    public void flushTiles( TileDraw draw ) {
        cached = null;
    }

    public void retireTiles( TileDraw draw ) {
        cached = null;
    }
    
    public void close() {
        cached = null;
    }
    
    class DirectTileRange implements TileRange {
        private TileDraw draw;
        private Rectangle range;

        /**
         * List of GridCoverage2D defined by this TileRange.
         * <p>
         * List is used in order to provide the following mapping:
         * tiles.get( 
         * </p>
         */
        private List tiles;
        
        /** names of tiles already loaded */
        Set loaded;
        boolean isLoaded;

        /**
         * Tile range will be created with "placeholders"; to retrive content
         * use the load method.
         * 
         * @param draw Stratagy object used to produce GridCoverages
         * @param range Range of tiles to produce
         */
        DirectTileRange( TileDraw draw, Rectangle range, List tiles, Set loaded ){            
            this.draw = draw;
            this.range = range;
            this.isLoaded = false;
            this.tiles = tiles;
            this.loaded = loaded.isEmpty() ? new HashSet() : loaded;
        }
        
        public Rectangle getRange() {
            return range;
        }
        public Envelope getBounds() {
            Envelope bounds = new Envelope();
            double x,y;
            for( Iterator i=tiles.iterator();i.hasNext();){
                GridCoverage2D tile = (GridCoverage2D) i.next();                
                Envelope2D area = tile.getEnvelope2D();
                
                //System.out.println( tile.getName() + " --> "+ area ); 
                x = area.getMinX();
                y = area.getMinY();
                bounds.expandToInclude( x,y );
                x = area.getMaxX();
                y = area.getMaxY();
                bounds.expandToInclude( x, y );
            }
            //System.out.println( range + " --> "+bounds );
            return bounds;
        }
        public Envelope2D getEnvelope2D() {
            Envelope2D bounds = null;
            for( Iterator i=tiles.iterator();i.hasNext();){
                GridCoverage2D tile = (GridCoverage2D) i.next();
                if( bounds == null){
                    bounds = new Envelope2D( tile.getEnvelope() );                    
                }
                else {
                    bounds.add( tile.getEnvelope2D() );
                }
            }
            return bounds;
        }
        public String toString() {
            return "DirectTileRange("+range+")";
        }
        public boolean equals( Object obj ) {
            if( obj == this ) return true;
            if( obj == null || !(obj instanceof DirectTileRange)){
                return false;
            }            
            DirectTileRange other = (DirectTileRange) obj;
            return this.draw == other.draw && this.range.equals( other.range );
        }
        public int hashCode() {
            return draw.hashCode() | range.hashCode() << 3;
        }
        /**
         * Set up available GridCoverage2d.
         * <p>
         * An entry is provided for every tile, even if just a placeholder.
         */
        public Set getTiles() {
            HashSet set = new HashSet( tiles );
            return Collections.unmodifiableSet( set);
        }

        public boolean isLoaded() {
            return isLoaded;
        }
        /**
         * Load tiles; this will replace existing placeholders.
         */
        public void load( IProgressMonitor monitor ) {
            fetchTiles(monitor, "Loading " ); 
            this.isLoaded = true;          
        }

        /**
         * Refresh tiles; this will update existing contents.
         */
        public void refresh( IProgressMonitor monitor ) {            
            if( isLoaded ){
                loaded.clear();
                fetchTiles(monitor, "Refresh" );
            }
            else {
                if( monitor == null ) monitor = new NullProgressMonitor();
                monitor.setTaskName( "Load already in progress" );
                monitor.isCanceled();
            }
        }

        private void fetchTiles( IProgressMonitor monitor, String message ) {
            if( monitor == null ) monitor = new NullProgressMonitor();
            
            int count =0;
            int total = range.width * range.height;
            monitor.beginTask("Fetch Tiles", total );
            try {
                for( int col = (int)range.x; col<range.getMaxX(); col++){
                    for( int row = (int)range.y; row<range.getMaxY(); row++){
                        if( monitor.isCanceled() ) {
                            return;
                        }
                        String name = draw.name(row, col);
                        
                        if( loaded.contains( name )){
                            count++;
                            monitor.worked( 1 );    
                        }
                        else {
                            monitor.setTaskName(name);                                                       
                            GridCoverage2D tile = draw.drawTile(row, col );                            
                            tiles.set( count, tile );
                            loaded.add(name);
                            count++;
                            monitor.worked( 1 );
                        }                        
                    }
                }
            } finally{
                monitor.done();
            }
        }                
    }
}