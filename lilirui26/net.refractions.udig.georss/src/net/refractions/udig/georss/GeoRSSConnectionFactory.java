package net.refractions.udig.georss;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.UDIGConnectionFactory;

public class GeoRSSConnectionFactory extends UDIGConnectionFactory {


	public boolean canProcess(Object context) {
		if (context instanceof IResolve){
			IResolve resolve = (IResolve)context;
			return resolve.canResolve(GeoRSSDataStore.class);
			
		}
		return toCapabilitiesURL(context)!=null;
	}

	

	
	public Map<String, Serializable> createConnectionParameters(Object data) {
		if (data == null)
			return null;
		if(data instanceof GeoRSSService){
			GeoRSSService georssService = (GeoRSSService) data;
			return georssService.getConnectionParams();
		}
		
		URL url = toCapabilitiesURL(data);
		if (url == null){
			url = CatalogPlugin.locateURL(data);
		}
			
		if (url!= null){
			List <IResolve> list = CatalogPlugin.getDefault().getLocalCatalog().find(url, null);
			for (IResolve resolve : list){
				Map params = createParams(resolve);
				if(!params.isEmpty())
					return params;
			}return createParams(url);
		}
			
		return Collections.emptyMap();
	}

	public Map<String, Serializable> createParams(IResolve handle) {
		
		if(handle instanceof GeoRSSService){
			GeoRSSService georssService = (GeoRSSService)handle;
			return georssService.getConnectionParams();
		}
		else if(handle instanceof GeoRSSGeoResource){
			GeoRSSGeoResource layer = (GeoRSSGeoResource) handle;
			GeoRSSService georssService;
			try {
				georssService= (GeoRSSService) layer.parent(null);
				return georssService.getConnectionParams();
			}catch (IOException e){
				checkedURL(layer.getIdentifier());
			}
		}
		else if(handle.canResolve(GeoRSSDataStore.class)){
			return createParams(handle.getIdentifier());
		}
		return Collections.emptyMap();
			
	}

	public Map<String, Serializable> createParams(URL url) {
		GeoRSSServiceExtension factory = new GeoRSSServiceExtension();
		Map params = factory.createParams(url);
		if(params!= null)
			return params;
		return params;
	}



	@Override
	public URL createConnectionURL(Object context) {
		// TODO Auto-generated method stub
		return null;
	}
	
	static URL toCapabilitiesURL(Object data) {
		if(data instanceof IResolve){
			return toCapabilitiesURL((IResolve)data);
		}
		else if (data instanceof URL){
			return toCapabilitiesURL((URL)data);
		}
		else if(CatalogPlugin.locateURL(data)!=null){
			return toCapabilitiesURL(CatalogPlugin.locateURL(data));
		}
		else {
			return null;
		}
	}
	
	static URL toCapabilitiesURL (IResolve resolve){
		if (resolve instanceof IService){
			return toCapabilitiesURL((IService) resolve);
		}
		return toCapabilitiesURL(resolve.getIdentifier());
	}
	
	static URL toCapabilitiesURL(IService resolve){
		if (resolve instanceof GeoRSSService){
			return toCapabilitiesURL((GeoRSSService) resolve);
		}
		return toCapabilitiesURL(resolve.getIdentifier());
	}
	
	static URL toCapabilitiesURL(GeoRSSService georssService){
		
		return georssService.getIdentifier();
		
	}
	
	 static URL toCapabilitiesURL( URL url ){
	        if (url == null) return null;
	        
	        String protocol = (url.getProtocol() != null ) ? url.getProtocol().toLowerCase() 
	        		: null;
	        
	        if( !"http".equals(protocol) && !"https".equals(protocol)){  
	            return null;
	        }
	            return null;
	        }
	
		 public static final URL checkedURL( URL url ){
		        String check = url.toExternalForm();
		        int hash = check.indexOf('#');
		        if ( hash == -1 ){
		            return url;            
		        }
		        try {
		            return new URL( check.substring(0, hash ));
		        } catch (MalformedURLException e) {
		            return null;
		        }
		    }

}
