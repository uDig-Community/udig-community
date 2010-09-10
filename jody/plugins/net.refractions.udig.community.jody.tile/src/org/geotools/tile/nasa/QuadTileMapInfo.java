package org.geotools.tile.nasa;

import java.net.URI;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Icon;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.tile.TileMapInfo;
import org.geotools.tile.ZoomLevel;
import org.jdom.Element;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class QuadTileMapInfo implements TileMapInfo {
    Element quad;
    
    /** Accessor used to grab content (& name) */
    Accessor accessor;
    
    QuadTileMapInfo( Element quadTileSet ){
        quad = quadTileSet;
        accessor = Accessor.create( quad );
        if( accessor == null ){
            throw new NullPointerException("Could not access this QuadTileMap");
        }
    }
    QuadTileMapInfo( Element quadTileSet, Accessor accessor ){
        quad = quadTileSet;
        this.accessor = accessor;
    }
       
    /**
     * Unique identifier for data source
     */
    public URI getIdentifier() {
        return accessor.getIdentifier();
    }
    
    /**
     * Grab the set of zoom levels.
     * <p>
     * Zoom levels are defined by:
     * <ul>
     * <ll>BoundingBox
     * <li>LevelZeroTileSizeDegrees
     * <li>NumberLevels
     * </ul>
     */
    public SortedSet getZoomLevels() {        
        double angle = getLevelZeroTileSizeDegrees();        
        Envelope bbox = getBounds();
        int tileSize = getTextureSizePixels();
        
        int rows = (int) (bbox.getHeight() / angle);
        int cols = (int) (bbox.getWidth() / angle);
        
        double scale;
        try {
            int midx = cols/2;
            int midy = rows/2;
            double x1 = bbox.getMinX() + ((double)midx)*angle;
            double x2 = x1 + angle;
            double y1 = bbox.getMinY() + ((double)midy)*angle;
            double y2 = y1 + angle; 
            Envelope centerTile = new Envelope( x1, x2, y1, y2 );
            CoordinateReferenceSystem crs = getCRS();
			scale = RendererUtilities.calculateScale( centerTile, crs, 512, 512, 72.0 );            
        } catch (Exception e) {
            // Backup HACK
            double miles = 49.0 * angle; // 49 miles per deegree
            double inches = 63360 * miles;
            double pixels = inches / 72; // 72 DPI
            scale = pixels * 512;            
        }        
        ZoomLevel levelZero = new ZoomLevel(scale, rows, cols);
        int numberOfLevels = getNumberLevels();
        return levels( levelZero, numberOfLevels );
    }
    
    /** Create a set of zoom levels, starting from levelZero */
    private static SortedSet levels( ZoomLevel levelZero, int numberOfLevels ){
        SortedSet set = new TreeSet();
        set.add( levelZero );
        for( int zoom = 1; zoom <= numberOfLevels; zoom++ ){
            set.add(levelZero.zoom( zoom ));
        }
        return set;
    }
    
    private double getLevelZeroTileSizeDegrees(){
        Element imageAccessor = quad.getChild("ImageAccessor");
        String text = imageAccessor.getChildText("LevelZeroTileSizeDegrees");        
        return Double.parseDouble( text );
    }
    private int getNumberLevels(){
        Element imageAccessor = quad.getChild("ImageAccessor");
        String text = imageAccessor.getChildText("NumberLevels");        
        return Integer.parseInt( text );
    }
    private int getTextureSizePixels(){
        Element imageAccessor = quad.getChild("ImageAccessor");
        String text = imageAccessor.getChildText("TextureSizePixels");        
        return Integer.parseInt( text );
    }
    private int getImageTileService(){
        Element imageAccessor = quad.getChild("ImageAccessor");
        String text = imageAccessor.getChildText("ImageTileService");        
        return Integer.parseInt( text );
    }
    
    public Envelope getBounds() {
        Element boundingBox = quad.getChild("BoundingBox");
        double north = bound( boundingBox.getChild("North"));
        double south = bound( boundingBox.getChild("South"));
        double east  = bound( boundingBox.getChild("East"));
        double west  = bound( boundingBox.getChild("West"));
        return new ReferencedEnvelope( west, east, south, north, getCRS() );        
    }
    
    static double bound( Element direction ){
        String value = direction.getChildText("Value");
        return Double.parseDouble( value );
    }

    public CoordinateReferenceSystem getCRS() {
        // TODO: account for <DistanceAboveSurface>0</DistanceAboveSurface>
        // TODO: model as perfect sphere
        return DefaultGeographicCRS.WGS84; 
    }

    public String getDescription() {
        String basicDescription = quad.getChildText("Description");
        String extendedDescription = LayerSetParser.description( quad );
        
        if( extendedDescription != null ){
            return basicDescription + "\n" + extendedDescription;
        }
        else {
            return basicDescription;
        }
    }
    
    /**
     * Icon (if avialable) will be in ExtendedInformation/ToolBarImage
     */
    public Icon getIcon() {
        return LayerSetParser.infoIcon( quad );
    }

    public String[] getKeywords() {
        return new String[]{ "TileMap" };
    }
    
    public String getName() {
         return accessor.getName();
    }

    /**
     * Currently world wind schema; suspect this should be
     * "TileMap" to allow geotools recognizers to function.
     */
    public URI getSchema() {
        return WorldWindTileServiceInfo.SCHEMA;
    }

    public String getTitle() {
        return quad.getChildText("Name");
    }

}
