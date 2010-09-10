package org.geotools.tile.osgeo;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.geotools.tile.TileDraw;
import org.geotools.tile.TileMapInfo;
import org.geotools.tile.TileProtocol;
import org.geotools.tile.TileServiceInfo;
import org.geotools.tile.TileSet;
import org.geotools.util.ProgressListener;

public class OSGeoTileProtocol extends TileProtocol {
	URL server;
	TileServiceInfo info;
	
	public OSGeoTileProtocol(URL url) {
		server = url;
	}

	@Override
	public synchronized TileServiceInfo getInfo(ProgressListener monitor) {
		if( info == null ){
			info = new OSGeoServiceInfo( this, monitor );
		}
		return info;
	}

	@Override
	public TileDraw getTileDraw(TileSet tileset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List getTileMapIds(TileServiceInfo info, ProgressListener monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TileMapInfo getTileMapInfo(TileServiceInfo info, URI id,
			ProgressListener monitor) {
		// TODO Auto-generated method stub
		return null;
	}

}
