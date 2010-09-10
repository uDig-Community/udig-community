package net.refractions.udig.georss;


import java.net.URL;
import net.refractions.udig.catalog.IServiceInfo;

public class IServiceGeoRSSInfo extends IServiceInfo{
	private URL url;
	
	IServiceGeoRSSInfo (GeoRSSDataStore resource){
		super();
	}
	
	public URL getSource(){
		return getIdentifier();
	}
	
	public String getTitle(){
		return "GeoRSS";
	}

	private URL getIdentifier() {
		
		return url;
	}
}
