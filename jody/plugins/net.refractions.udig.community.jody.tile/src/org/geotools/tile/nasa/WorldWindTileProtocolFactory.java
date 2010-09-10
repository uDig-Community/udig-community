package org.geotools.tile.nasa;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.geotools.tile.TileProtocol;
import org.geotools.tile.TileProtocolFactory;
import org.geotools.util.SimpleInternationalString;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.opengis.util.InternationalString;

public class WorldWindTileProtocolFactory implements TileProtocolFactory {
	
	public boolean canTile(URL url) {
		try {
			SAXBuilder builder = new SAXBuilder(false); 
			URLConnection connection = url.openConnection();
            Document dom = builder.build(connection.getInputStream());
            return "LayerSet".equals( dom.getRootElement().getName());
		}
		catch( Throwable t ){
			return false;
		}
	}

	public TileProtocol createTileStratagy(URL url) throws IOException {
		return new WorldWindTileProtocol(url);
	}

	public InternationalString getDescription() {
		return new SimpleInternationalString("WorldWind LayerSet File");
	}

	public InternationalString getName() {
		return new SimpleInternationalString(
				"Used to connect to a world wind layerset file (for example"+
				"http://worldwind25.arc.nasa.gov/layerConfig/earthimages.xml)."+
				"Currently QuadTileMap layers are supported using the ImageTile"+
				"accessor.");
	}
}
