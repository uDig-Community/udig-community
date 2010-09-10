package org.geotools.tile.osgeo;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.swing.Icon;

import org.geotools.tile.TileProtocol;
import org.geotools.tile.TileServiceInfo;
import org.geotools.util.ProgressListener;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class OSGeoServiceInfo implements TileServiceInfo {
	TileProtocol protocol;
	
	private Document dom;
	
	public OSGeoServiceInfo(OSGeoTileProtocol protocol, ProgressListener monitor) {
		this.protocol = protocol;
		try {			
			URL tileMapService = tileMapService( protocol.server );
			
			SAXBuilder builder = new SAXBuilder(false); 			
			URLConnection connection = tileMapService.openConnection();
			dom = builder.build(connection.getInputStream());
		}
		catch( Throwable t ){
			t.printStackTrace();
		}
	}

	/**
	 * URL to supported TileMapService, currently must be version="1.0.0".
	 * 
	 * @param server
	 * @return URL to service
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	private URL tileMapService(URL server) throws IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder(false); 			
		URLConnection connection = server.openConnection();
		Document dom = builder.build(connection.getInputStream());
				
		for( Iterator i=dom.getRootElement().getChildren().iterator(); i.hasNext(); ){
			Element child = (org.jdom.Element) i.next();
			if( "TileMapService".equals( child.getName() ) &&
					"1.0.0".equals(child.getAttributeValue("version")) ){
				String href = child.getAttributeValue("href");
				return new URL( server, href );
			}
		}
		throw new IOException( server+"does not support TileMapService version 1.0.0");		
	}

	public TileProtocol getTileStratagy() {
		return protocol;
	}

	public String getAbstract() {
		return null;
	}

	public String getDescription() {
		return null;
	}

	public Icon getIcon() {
		return null;
	}

	public String[] getKeywords() {
		return null;
	}

	public URI getPublisher() {
		return null;
	}

	public URI getSchema() {
		return null;
	}

	public URI getSource() {
		return null;
	}

	public String getTitle() {
		return null;
	}
}