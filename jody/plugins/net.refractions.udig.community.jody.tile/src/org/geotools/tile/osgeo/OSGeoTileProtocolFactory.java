package org.geotools.tile.osgeo;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.geotools.tile.TileProtocol;
import org.geotools.tile.TileProtocolFactory;
import org.geotools.tile.nasa.LayerSetParser;
import org.geotools.util.SimpleInternationalString;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.opengis.util.InternationalString;

/**
 * Allows connection to servers following OSGeo WMS Tile specification.
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class OSGeoTileProtocolFactory implements TileProtocolFactory {

	public boolean canTile(URL url) {
		try {
			SAXBuilder builder = new SAXBuilder(false); 
			URLConnection connection = url.openConnection();
            Document dom = builder.build(connection.getInputStream());
            return "Services".equals( dom.getRootElement().getName() );
		}
		catch( Throwable t ){
			return false;
		}
	}

	public TileProtocol createTileStratagy(URL url) throws IOException {
		return new OSGeoTileProtocol( url );
	}

	public InternationalString getDescription() {
		return new SimpleInternationalString(
				"Allows connection to WMS Tile Servers, as defined by the OSGeo foundation."
		);
	}

	public InternationalString getName() {
		return new SimpleInternationalString("WMS Tile");
	}

}
