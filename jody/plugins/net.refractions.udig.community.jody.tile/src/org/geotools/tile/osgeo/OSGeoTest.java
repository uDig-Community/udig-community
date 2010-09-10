package org.geotools.tile.osgeo;

import java.net.URL;

import org.geotools.tile.TileServer;

import junit.framework.TestCase;

/**
 * This unit test documents how to connect the TileClient code to an
 * OSGeo Tile Server (http://labs.metacarta.com/wms-c/tilecache.py/).
 * 
 * @author Jody Garnett, Refranctions Research, Inc.
 */
public class OSGeoTest extends TestCase {

	TileServer server;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		URL url = new URL("http://labs.metacarta.com/wms-c/tilecache.py/");
		server = new TileServer(url, null );
	}
	public void testConnection() throws Exception {
		assertNotNull( server );
	}
}
