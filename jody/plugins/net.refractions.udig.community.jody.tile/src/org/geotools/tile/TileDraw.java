package org.geotools.tile;

import org.geotools.coverage.grid.GridCoverage2D;

/**
 * Please note implementations should be threadsafe as TileCache
 * implementations will often have a pool of worker threads assigned
 * to tile creation.
 * <p>
 * This construct is captured as an abstract class to allow
 * for the addition of scheduling or event notification as
 * future needs dictate. Any additional methods will not be abstract;
 * allowing your code to function without modification.
 * </p>
 * @author Jody Garnett, Refractions Research, Inc.
 */
public abstract class TileDraw {
     public abstract String name( int row, int col );
     public abstract GridCoverage2D drawPlaceholder( int row, int col );
     public abstract GridCoverage2D drawTile( int row, int col );
}