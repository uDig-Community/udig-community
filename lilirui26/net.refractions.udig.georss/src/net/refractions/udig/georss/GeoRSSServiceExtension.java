package net.refractions.udig.georss;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.IService;

import net.refractions.udig.catalog.ServiceExtension2;

public class GeoRSSServiceExtension implements ServiceExtension2 {
	

	public IService createService(URL id, Map<String, Serializable> params) {
		
		try {
			 id = new URL ("http://earthquake.usgs.gov/eqcenter/catalogs/eqs1day-M2.5.xml");
			 } catch (MalformedURLException e) {
			e.printStackTrace();
		}return new GeoRSSService(id, params);
		
	}
	
	public Map<String, Serializable> createParams(URL url) {
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		
		return params;
	}

	
	protected String doOtherChecks(Map<String, Serializable> params) {
		// TODO Auto-generated method stub
		return null;
	}

	/*@Override
	protected DataStoreFactorySpi getDataStoreFactory() {
		// TODO Auto-generated method stub
		return null;
	}*/
	

	
	public String reasonForFailure(URL url) {
	
	return reasonForFailure(createParams(url));
	}

	
	public String reasonForFailure(Map<String, Serializable> params) {
		
		return null;
	}
	
	/*private String processURL(URL url) {
		String PATH = url.getPath();
		String QUERY = url.getQuery();
		String PROTOCOL = url.getProtocol();
		
		if (PROTOCOL.indexOf("http")==-1){
			return "GeoRSSSeriveExtension_protocol'"+PROTOCOL+"'";
		}
		if(QUERY!=null && QUERY.toUpperCase().indexOf("Service=")!=-1){
			int indexOf = QUERY.toUpperCase().indexOf( "SERVICE=" ); 
			if( QUERY.toUpperCase().indexOf( "SERVICE=WMS") == -1 ){ 
                int endOfExp = QUERY.indexOf('&', indexOf);
                if( endOfExp == -1 )
                	endOfExp=QUERY.length();
                if( endOfExp>indexOf+8)
                	return "GeoRSSServiceExtension_badService"+QUERY.substring(indexOf+8, endOfExp );
                else{
                	return"GeoRSSServiceExtension_badService"+""; 
            }
        } 
			else if (PATH != null && PATH.toUpperCase().indexOf("GEOSERVER") != -1) { 
            return null;
        }
		
	}return null;
	}*/
}
