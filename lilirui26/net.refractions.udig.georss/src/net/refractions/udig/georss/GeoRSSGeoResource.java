package net.refractions.udig.georss;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;

import net.refractions.udig.catalog.*;


public class GeoRSSGeoResource extends IGeoResource {
	IResolve parent;
	String typename = null;
	private URL identifier;
	
	public GeoRSSGeoResource(IResolve parent, String typename){
		this.parent = parent; 
		this.typename = typename;
		try{
			identifier = new URL(parent.getIdentifier().toString()+"#"+typename);
		}catch (MalformedURLException e){
			identifier=parent.getIdentifier();
		}
	}
	
	
	@Override
	public URL getIdentifier() {
		
		return identifier;
	}

	public Throwable getMessage() {
		
		return parent.getMessage();
	}

	public Status getStatus() {
		
		return parent.getStatus();
	}
	
	public <T> T resolve (Class<T> adaptee, IProgressMonitor monitor)throws IOException{
		if(adaptee == null)
			return null;
		if(adaptee.isAssignableFrom(IService.class))
			return adaptee.cast(parent);
		if(adaptee.isAssignableFrom(GeoRSSDataStore.class))
			return parent.resolve(adaptee, monitor);
		if(adaptee.isAssignableFrom(IGeoResource.class))
			return adaptee.cast(this);
		if(adaptee.isAssignableFrom(IGeoResourceInfo.class))
			return adaptee.cast(getInfo(monitor));
		if(adaptee.isAssignableFrom(FeatureStore.class)){
			FeatureSource fs = parent.resolve(GeoRSSDataStore.class, monitor).getFeatureSource(typename);  
			if (fs instanceof FeatureStore)
				return adaptee.cast(fs);
		if(adaptee.isAssignableFrom(FeatureSource.class))
				return adaptee.cast(parent.resolve(GeoRSSDataStore.class, monitor).getFeatureSource(typename));
			}
		
		return super.resolve(adaptee, monitor);
		} 
		
	 public <T> boolean canResolve (Class <T> adaptee){
		 if (adaptee == null)
			 return false;
		 return  adaptee.isAssignableFrom(IGeoResourceInfo.class) ||
				 adaptee.isAssignableFrom(GeoRSSDataStore.class) ||
				 adaptee.isAssignableFrom(IService.class) ||
				 adaptee.isAssignableFrom(IGeoResource.class) ||
				 adaptee.isAssignableFrom(FeatureStore.class) ||
				 adaptee.isAssignableFrom(FeatureSource.class) ||
				 super.canResolve(adaptee);
	 }
	
	
	@Override
	public IService service(IProgressMonitor monitor) throws IOException {
		// TODO Auto-generated method stub
		return (IService) parent;
	}
	
	
	private volatile IGeoResourceInfo info;
	public IGeoResourceInfo getInfo(IProgressMonitor monitor) throws IOException {
		if(info== null && getStatus()!=Status.BROKEN){
			
				if(info ==null)
					info = new IGeoResourceGeoRSSInfo(service(monitor));
				
		} 
		return info;
	}
}
