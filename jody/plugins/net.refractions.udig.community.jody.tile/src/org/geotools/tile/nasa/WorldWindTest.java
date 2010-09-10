package org.geotools.tile.nasa;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import junit.framework.TestCase;

import org.geotools.catalog.GeoResource;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.tile.TileMap;
import org.geotools.tile.TileMapInfo;
import org.geotools.tile.TileResource;
import org.geotools.tile.TileServer;
import org.geotools.tile.TileService;
import org.geotools.tile.TileServiceInfo;
import org.geotools.tile.TileSet;
import org.geotools.tile.ZoomLevel;
import org.geotools.tile.cache.TileRange;

import com.vividsolutions.jts.geom.Envelope;

public class WorldWindTest extends TestCase {
    WorldWindTileProtocol ww;
    private URL url;

    @Override
    protected void setUp() throws Exception {
        super.setUp();                
        url = WorldWindTest.class.getResource("earthimages.xml");
        ww = new WorldWindTileProtocol( url );
    }
    
    public void testServiceInfo() throws Exception {
        TileServiceInfo info = ww.getInfo( null );
        
        assertEquals( "Images", info.getTitle() );
        
        // example does not include abstract or description
        String description1 = info.getDescription();
        String description2 = info.getAbstract();        
    }
    public void testTileMapInfo() throws Exception {
        TileServiceInfo info = ww.getInfo( null );
        
        List ids = ww.getTileMapIds(info, null);
        URI id = (URI) ids.get(0);
        
        TileMapInfo tileInfo = ww.getTileMapInfo( info, id, null );
        assertNotNull( tileInfo );
        assertFalse( tileInfo.getZoomLevels().isEmpty() );
        
        ZoomLevel zero = (ZoomLevel) tileInfo.getZoomLevels().iterator().next();
        assertEquals( "160x80", 160, zero.getNumberOfColumns() );
        assertEquals( "160x80", 80, zero.getNumberOfRows() );
        
        Envelope bbox = tileInfo.getBounds();
        assertNotNull( bbox );
        assertEquals( 180.0, bbox.getHeight(), 0.001 );
        assertEquals( 360.0, bbox.getWidth(), 0.001 );
        
        assertEquals( "105", tileInfo.getName() );
        assertEquals( "NLT Landsat7 (Visible Color)", tileInfo.getTitle() );
                
        assertEquals( id , tileInfo.getIdentifier());
    }
    
    public void testServiceInfoInternal() throws Exception {
        WorldWindTileServiceInfo info = (WorldWindTileServiceInfo) ww.getInfo( null );
              
        List ids = info.childrenIds();
        assertFalse( ids.isEmpty() );
        
        URI id = (URI) ids.get(0);
        QuadTileMapInfo quadInfo = info.getInfo( id );
        assertNotNull( quadInfo );
    }
 
    public void testStratagy(){
        TileServiceInfo info = ww.getInfo( null );
        
        List ids = ww.getTileMapIds(info, null);
        URI id = (URI) ids.get(0);

        TileServer server = new TileServer( info );
        
        TileMapInfo tileInfo = ww.getTileMapInfo( info, id, null );        
        TileMap tileMap = new TileMap( server, tileInfo );  

        ZoomLevel zero = (ZoomLevel) tileInfo.getZoomLevels().iterator().next();
        TileSet tileSet = tileMap.getTileSet(zero);
        
        assertNotNull( tileSet );
        assertEquals( zero, tileSet.getZoomLevel() );
        
        Envelope request = new Envelope(-140,-130,40,50);        
        TileRange range = tileSet.getTileRange( request );
        
        Envelope bounds = range.getBounds();
        assertTrue( bounds.intersects( request ));        
    }
    public void testAccessorFailover() throws IOException {
        String good = "http://worldwind25.arc.nasa.gov/tile/tile.aspx?T=bmng.topo.bathy.200411&L=1&X=3&Y=7";             
        String bad = "http://worldwind25.arc.nasa.gov/tile/tile.aspx?T=105&L=0&X=63&Y=22";
        
        ImageInputStream inputGood = Accessor.openImageInput( new URL(good));
        ImageInputStream inputBad = Accessor.openImageInput( new URL(bad));
        assertNotNull( inputGood );
        //assertNull( inputBad );
        
        ImageReader jpeg = ImageIO.getImageReadersByFormatName("jpeg").next();
        jpeg.setInput( inputGood );
        
        BufferedImage image = jpeg.read(0);
        assertNotNull( image );
        assertEquals( 512, image.getWidth() );
        assertEquals( 512, image.getHeight() );
        jpeg.dispose();
        
        jpeg = ImageIO.getImageReadersByFormatName("jpeg").next();
        jpeg.setInput( inputBad );
        try {
	        image = jpeg.read(0);
	        assertNotNull( image );
	        assertEquals( 512, image.getWidth() );
	        assertEquals( 512, image.getHeight() );
	        fail("This was supposed to be a bad image");
        }
        catch( IIOException expected){
        	// expected
        }
        finally {
	        jpeg.dispose();
        }
        
    }
    
    public void testTileService() throws IOException {
        TileService service = new TileService(null,Collections.singletonMap("url",url));
        TileResource topo = null;
        for( Iterator i=service.members(null).iterator(); i.hasNext();){
            GeoResource handle = (GeoResource) i.next();
            if( handle.getInfo(null).getName().equals("bmng.topo.bathy.200411")){
                topo = (TileResource) handle;
                break;
            }
        }
        assertNotNull( topo );
        TileMapInfo info = (TileMapInfo) topo.getInfo(null);
        assertEquals( new Envelope(-180, 180, -90, 90), info.getBounds() );
        
        TileMap tileMap = topo.getTileMap( null );
        assertEquals( info, tileMap.getInfo() );
        
        ZoomLevel zero = (ZoomLevel) info.getZoomLevels().iterator().next();
        assertEquals( 10, zero.getNumberOfColumns() );
        assertEquals( 5, zero.getNumberOfRows() );
        TileSet set = tileMap.getTileSet( zero );
    }
    
    public void testInterestingRange() throws Exception {
        TileService service = new TileService(null,Collections.singletonMap("url",url));
        TileResource topo = null;
        for( Iterator i=service.members(null).iterator(); i.hasNext();){
            GeoResource handle = (GeoResource) i.next();
            if( handle.getInfo(null).getName().equals("bmng.topo.bathy.200411")){
                topo = (TileResource) handle;
                break;
            }
        }
        TileMapInfo info = (TileMapInfo) topo.getInfo(null);        
        TileMap tileMap = topo.getTileMap( null );        
        ZoomLevel zero = (ZoomLevel) info.getZoomLevels().iterator().next();
        TileSet set = tileMap.getTileSet( zero );
        
        // start of test 
        Envelope requestBounds = new Envelope(-167.87629932873148,-74.33033309983843,38.48575622428183,95.87699629341508);
        Rectangle requestRange = new Rectangle( 0,3, 2, 2 );
        Envelope expectedBounds = new Envelope( -180, -180+36*2, -90+36*3, -90+36*5);        
        Envelope expectedCoverage = info.getBounds().intersection( requestBounds );
        
        // try with expected requestRange
        TileRange range = set.getTileRange( requestRange );                        
        Envelope actualBounds = range.getBounds();
        
        // did we get what we expected?
        assertEquals( requestRange, range.getRange() );
        assertEquals( expectedBounds, actualBounds );

        // try with expected requestBounds
        TileRange range2 = set.getTileRange( requestBounds );
        actualBounds = range2.getBounds();        
        assertEquals( requestRange, range2.getRange() );        
        assertTrue( actualBounds+ " must contain "+expectedCoverage, actualBounds.contains( expectedCoverage ) );
        
        assertEquals( range, range2 );
    }
    
    public void testLevel0TwoTiles() throws IOException {
        TileService service = new TileService(null,Collections.singletonMap("url",url));
        TileResource topo = null;
        for( Iterator i=service.members(null).iterator(); i.hasNext();){
            GeoResource handle = (GeoResource) i.next();
            if( handle.getInfo(null).getName().equals("bmng.topo.bathy.200411")){
                topo = (TileResource) handle;
                break;
            }
        }
        TileMapInfo info = (TileMapInfo) topo.getInfo(null);        
        TileMap tileMap = topo.getTileMap( null );        
        ZoomLevel zero = (ZoomLevel) info.getZoomLevels().iterator().next();
        TileSet set = tileMap.getTileSet( zero );
        
       // here is our set up:
        Rectangle requestRange = new Rectangle(1,3,2,1);            
        Envelope requestBounds = new Envelope(-180+36, -180+(36*3), -90+36*3, -90+36*4);
        Envelope2D requestBounds2D =
            new Envelope2D( info.getCRS(), requestBounds.getMinX(), requestBounds.getMinY(), 36*2, 36 );
        
        TileRange tileRange = set.getTileRange( requestRange );
        assertEquals( 2, tileRange.getTiles().size() );
        assertEquals( requestBounds, tileRange.getBounds() );
        assertEquals( requestBounds2D, tileRange.getEnvelope2D() );
        
        Iterator iterator = tileRange.getTiles().iterator();
        GridCoverage2D place1 = (GridCoverage2D) iterator.next();
        String name = place1.getName().toString();
                    
        tileRange.load(null); // load both
        
        Iterator iterator2 = tileRange.getTiles().iterator();
        GridCoverage2D tile1 = (GridCoverage2D) iterator2.next();
        GridCoverage2D tile2 = (GridCoverage2D) iterator2.next();
        
        assertFalse( tileRange.getTiles().contains( place1 ) );
        assertTrue( tile1.getName().equals( place1.getName()) ||
                    tile2.getName().equals( place1.getName()) );
        
        
        // Test zoom level two
        ZoomLevel one = zero.zoom(1);
        set = tileMap.getTileSet( one );
        assertEquals( one, set.getZoomLevel() );
        
        TileRange again = set.getTileRange( requestBounds );        
        assertEquals( tileRange.getRange().width*2, again.getRange().width );        
        assertTrue( tileRange.getBounds().contains( again.getBounds() ));  
    }
    
    public void testLevel0SingleTile() throws IOException {
        TileService service = new TileService(null,Collections.singletonMap("url",url));
        TileResource topo = null;
        for( Iterator i=service.members(null).iterator(); i.hasNext();){
            GeoResource handle = (GeoResource) i.next();
            if( handle.getInfo(null).getName().equals("bmng.topo.bathy.200411")){
                topo = (TileResource) handle;
                break;
            }
        }
        TileMapInfo info = (TileMapInfo) topo.getInfo(null);        
        TileMap tileMap = topo.getTileMap( null );        
        ZoomLevel zero = (ZoomLevel) info.getZoomLevels().iterator().next();
        TileSet set = tileMap.getTileSet( zero );
        
        // here is our request information
        Rectangle requestRange = new Rectangle(0,0,1,1);            
        Envelope requestBounds = new Envelope(-180, -180+36, -90, -90+36);
        ReferencedEnvelope requestBounds2D = new ReferencedEnvelope( requestBounds, info.getCRS());
        
        // request 
        TileRange single = set.getTileRange( requestRange );
        assertNotNull( single );
        
        
        Envelope bounds = single.getBounds();
        assertEquals( requestBounds, bounds);
        
        Envelope2D envelope2D = single.getEnvelope2D();
        assertEquals( requestBounds2D.getMinX(), envelope2D.getMinX() );
        assertEquals( requestBounds2D.getMinY(), envelope2D.getMinY() );
        assertEquals( requestBounds2D.getMaxX(), envelope2D.getMaxX() );
        assertEquals( requestBounds2D.getMaxY(), envelope2D.getMaxY() );
        assertEquals( requestBounds2D.getCoordinateReferenceSystem(), envelope2D.getCoordinateReferenceSystem() );            
        
        Set tiles = single.getTiles();
        
        assertEquals( 1, tiles.size());
        GridCoverage2D placeholder = (GridCoverage2D) tiles.iterator().next();
        
        assertFalse( single.isLoaded() );
        single.load(null);
        assertTrue( single.isLoaded() );
        Set loaded = single.getTiles();
        
        assertEquals( 1, tiles.size());
        GridCoverage2D coverage = (GridCoverage2D) loaded.iterator().next();
        
        assertNotSame( placeholder, coverage );
        assertEquals( placeholder.getEnvelope(), coverage.getEnvelope() );
        assertEquals( placeholder.getName(), coverage.getName() );
        
        TileRange again = tileMap.getTileRange( requestBounds, zero.getScaleDenominator() );
        assertEquals( single, again );
        assertTrue( again.isLoaded() );

        // Test zoom level two
        ZoomLevel one = zero.zoom(1);
        set = tileMap.getTileSet( one );
        assertEquals( one, set.getZoomLevel() );
        
        TileRange detailed = set.getTileRange( requestBounds );        
        assertEquals( (again.getRange().width)*2, detailed.getRange().width );
        assertEquals( again.getRange().x*2, detailed.getRange().x );
        assertTrue( again.getBounds().contains( detailed.getBounds() ));   
        
        ZoomLevel two = zero.zoom(2);
        set = tileMap.getTileSet( two );
        assertEquals( two, set.getZoomLevel() );
        
        TileRange finer = set.getTileRange( requestBounds );        
        assertEquals( (again.getRange().width)*8, finer.getRange().width * 2 );
        assertEquals( again.getRange().x*8, finer.getRange().x );
        assertTrue( again.getBounds().contains( finer.getBounds() ));   
        
        
    }
}
