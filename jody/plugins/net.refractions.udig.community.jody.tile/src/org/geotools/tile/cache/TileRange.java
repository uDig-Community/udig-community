package org.geotools.tile.cache;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.geometry.Envelope2D;

import com.vividsolutions.jts.geom.Envelope;

public interface TileRange {
    public TileRange EMPTY = new TileRange(){
        public Envelope getBounds() {
            return new Envelope(); // empty!
        }

        public Envelope2D getEnvelope2D() {
            return new Envelope2D( null, 0, 0, 0, 0 );
        }

        public Set getTiles() {
            return Collections.EMPTY_SET;
        }

        public boolean isLoaded() {
            return true; // as loaded as we will ever be
        }

        public void load( IProgressMonitor monitor ) {
            if( monitor != null ) monitor.done();
        }

        public void refresh( IProgressMonitor monitor ) {
            if( monitor != null ) monitor.done();
        }

        public Rectangle getRange() {
            return new Rectangle(0,0,0,0);
        }
        
    };
    /**
     * Bounds of this tile range.
     * 
     * @return bounds of tiles in this range
     */
    Envelope getBounds();
    
    /**
     * Envelope2D for this tile range.
     * 
     * @return bounds of tiles in this range
     */
    Envelope2D getEnvelope2D();
    
    /**
     * Range in row/col.
     *
     * @return Range in row col;
     */
    Rectangle getRange();

    void load( IProgressMonitor monitor ); // monitor advances as each tile is available
    boolean isLoaded();
    void refresh( IProgressMonitor monitor ); // leaves tiles as is, but redraws

    /**
     * Tiles in range
     * 
     * @return Set of GridCoverage2d
     */
    Set getTiles();
}
