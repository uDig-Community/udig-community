package org.geotools.tile.nasa;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.geotools.tile.TileMapInfo;
import org.geotools.tile.TileSet;
import org.geotools.tile.ZoomLevel;
import org.jdom.Element;

/**
 * Captures the different ways of accessing content used
 * by WorldWind.
 */
public abstract class Accessor {
    
    /** Name of Layer using this accessor */    
    abstract String getName();

    /** Name of Layer using this accessor */
    abstract URI getIdentifier();

    /** Non null RenderedImage if data exists at that location */
    abstract RenderedImage tileImage( TileSet tileset, int row, int col ) throws IOException;

    /**
     * Create an accessor for the provided layer.
     * <p>
     * Currently supports ImageAccessor.
     * </p>
     * @param element
     * @return
     */
    static Accessor create( Element element ){
        if( element.getChild( "ImageAccessor") != null){
            Element imageAccessor = element.getChild("ImageAccessor");
            if( imageAccessor.getChild("ImageTileService") != null){
                return createImageTileAccessor( imageAccessor.getChild("ImageTileService") );
            }
            else if( imageAccessor.getChild("WMSAccessor") != null){
                return createWMSAccessor( imageAccessor.getChild("WMSAccessor") );
            }
        }
        return null;
    }

    private static Accessor createImageTileAccessor( final Element imageTileService ) {
        final String server = imageTileService.getChildText("ServerUrl");
        final String data = imageTileService.getChildText("DataSetName");
        try {
            final String name = data;
            final URI id = new URI( server + "#" + data ); 
            return new Accessor(){
                String getName() {                                
                    return name;
                }     
                URI getIdentifier() {
                    return id;
                }
                
                public RenderedImage tileImage( TileSet tileset, int row, int col ) throws IOException {
                    URL request;
                        
                    request = requestURL( tileset, row, col );
                    if( request == null ) return null; // no data here move on
                    
                    try {
                        //BufferedImage image = ImageIO.read( request.openStream() );
                        //return JAI.create( "tile"+row+"x"+col, request );                                                
                        ImageInputStream input = openImageInput( request );
                        if( input == null ){
                            return null; // request invalid
                        }
                        ImageReader jpeg = ImageIO.getImageReadersByFormatName("jpeg").next();                        
                        jpeg.setInput( input, true );                        
                        return jpeg.read(0,null);
                    }
                    catch( IOException io){
                        System.out.println( io.getMessage() + "for request:"+request);                
                        return null;
                    }
                    catch( NoClassDefFoundError format ){
                        System.out.println("Unknown format:"+request );
                        return null;
                    }
                }
                /**
                 * Set up the request url.
                 * 
                 * @param tileset
                 * @param row
                 * @param col
                 * @return
                 * @throws IOException
                 */
                private URL requestURL( TileSet tileset, int row, int col ) throws IOException {                    
                    int level = levelNumber( tileset );
                    String request =                        
                        MessageFormat.format("{0}?T={1}&L={2}&X={3}&Y={4}",
                                new Object[]{server, data,
                            new Integer( level ), new Integer(col), new Integer(row)});
                    
                    return new URL( request );                           
                }
                private int levelNumber( TileSet tileset ) {
                    TileMapInfo info = tileset.getTileMap().getInfo();
                    ZoomLevel zoomLevel = tileset.getZoomLevel();
                    Set set = info.getZoomLevels();
                    int level = 0;
                    for( Iterator i = set.iterator(); i.hasNext(); level++ ) {
                        if (zoomLevel.equals(i.next())) {
                            return level;
                        }
                    }
                    return -1;
                }
            };            
        } catch (URISyntaxException e) {
            return null;
        }        
    }
    /**
     * This method actually checks the connection header information first...
     * </p>
     * Sample invalid header: <b>http://worldwind25.arc.nasa.gov/tile/tile.aspx?T=105&L=0&X=63&Y=22</b>
     * <pre><code>
     * HTTP/1.1 204 No Content
     * Date: Wed, 22 Nov 2006 01:48:51 GMT
     * Server: Microsoft-IIS/6.0
     * X-Powered-By: ASP.NET
     * X-AspNet-Version: 1.1.4322
     * Set-Cookie: ASP.NET_SessionId=smi5d055xvtdcx45ltxrbcjv; path=/
     * Cache-Control: private
     * Content-Length: 24
     * 
     * No data for this region.
     * </code></pre>
     *
     * This method should be replaced with a peek at the connection,
     * and then pass the connection onto JAI.create ...
     * @param request
     * @return URL or null if could not connect
     *
    static URL checkRequest( String request ){
        URL url;
        try {
            url = new URL( request );
        } catch (MalformedURLException e) {
            throw new RuntimeException( e );
        }
        if( "http".equalsIgnoreCase(url.getProtocol())){
            // check connection header
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();  
                System.out.println( "HTTP "+connection.getResponseCode()+":" +request );                 
                if( connection.getResponseCode() == 200 ){
                    return url; // okay
                }
                if( connection.getResponseCode() == 204 ){
                    return null; // we must be "No data for this region.";                            
                }                
            } catch (IOException e) {
                System.out.println("No content:"+request);
                System.out.println( e );
                return null;
            }                                   
        }                    
        return url;
    }*/
    
    /**
     * This method actually checks the connection header information first...
     * </p>
     * Sample invalid header: <b>http://worldwind25.arc.nasa.gov/tile/tile.aspx?T=105&L=0&X=63&Y=22</b>
     * <pre><code>
     * HTTP/1.1 204 No Content
     * Date: Wed, 22 Nov 2006 01:48:51 GMT
     * Server: Microsoft-IIS/6.0
     * X-Powered-By: ASP.NET
     * X-AspNet-Version: 1.1.4322
     * Set-Cookie: ASP.NET_SessionId=smi5d055xvtdcx45ltxrbcjv; path=/
     * Cache-Control: private
     * Content-Length: 24
     * 
     * No data for this region.
     * </code></pre>
     * @return ImageInputStream (or null if No data for this region)
     */
    static ImageInputStream openImageInput( URL url ) throws IOException {
        if( "http".equalsIgnoreCase(url.getProtocol())){
            // check connection header
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) url.openConnection();  
            System.out.println( "HTTP "+connection.getResponseCode()+":" +url );                 
            if( connection.getResponseCode() == 204 ){
                connection.disconnect();
                return null; // we must be "No data for this region.";                            
            }
            return ImageIO.createImageInputStream(connection.getInputStream());                                          
        }
        return ImageIO.createImageInputStream( url.openStream() ); 
    }
    private static Accessor createWMSAccessor( final Element wmsAccessor ) {
        String server = wmsAccessor.getChildText("ServerGetMapUrl");
        String data = wmsAccessor.getChildText("global_mosaic");
        try {
            final String name = data;
            final URI id = new URI( server + "#" + data ); 
            return new Accessor(){
                String getName() {                                
                    return name;
                }
                URI getIdentifier() {
                    return id;
                }
                @Override
                RenderedImage tileImage( TileSet tileset, int row, int col ) throws IOException {
                    throw new IOException("No Content");
                }                            
            };            
        } catch (URISyntaxException e) {
            return null;
        }        
    }
        
}
