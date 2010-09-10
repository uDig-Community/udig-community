package net.refractions.udig.georss;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.SubProgressMonitor;

//import net.refractions.udig.catalog.CatalogPlugin;
//import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
//import net.refractions.udig.ui.ErrorManager;

public class GeoRSSService extends IService {
 
	private URL url = null;
	private Map <String, Serializable> params = null;
	public GeoRSSService(URL arg1, Map <String, Serializable> arg2 ){
		url = arg1; 
		params = arg2;
	}
	
	@Override
	public <T> T resolve(Class <T> adaptee, IProgressMonitor monitor)throws IOException{
		if (adaptee == null)
			return null;
		if(adaptee.isAssignableFrom(GeoRSSDataStore.class)){
			return adaptee.cast(getDataStore(monitor));
		}
		return super.resolve(adaptee, monitor);
		
	}
	
	public <T> boolean canResolve (Class<T> adaptee){
		if(adaptee == null)
			return false;
		return adaptee.isAssignableFrom(GeoRSSDataStore.class)||
		super.canResolve(adaptee);
	}
	
	/*public void dispose(IProgressMonitor monitor){
		if (members ==null)
			return;
		
		int steps = (int)((double)99/(double) members.size());
		for (IResolve resolve: members){
			try {
				SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, steps);
				resolve.dispose(subProgressMonitor);
				subProgressMonitor.done();
			}catch (Throwable e){
				ErrorManager.get().displayException(e, 
						"Error disposing members of service:"+ getIdentifier(), CatalogPlugin.ID);
			}
		}
	}*/
	
	
	private volatile GeoRSSDataStore ds = null;
	GeoRSSDataStore getDataStore(IProgressMonitor monitor) throws IOException{
	
		if(this.ds == null)
		{
			this.ds = new GeoRSSDataStore();
		}

	return ds;
	
	}


	@Override
	public Map<String, Serializable> getConnectionParams() {
		
		return params;
	}

	
	public IServiceInfo getInfo(IProgressMonitor monitor) throws IOException {
		getDataStore(monitor);
		if(info ==null){
			info = new IServiceGeoRSSInfo(ds);
			}
		return info;
	}
	private volatile IServiceInfo info = null;

	
	public List <GeoRSSGeoResource> members (IProgressMonitor monitor) throws IOException {
		
		if (members == null){
			getDataStore(monitor);
			members = new LinkedList<GeoRSSGeoResource>();
			String[] typenames = ds.getTypeNames();
			
			if (typenames!= null)
				for (int i=0; i<typenames.length; i++){
					try{
						members.add(new GeoRSSGeoResource(this, typenames[i]));
					} catch (Exception e){
						e.printStackTrace();
					}
		}
	
		}		return members;
	}
	
	private volatile List<GeoRSSGeoResource> members = null;	

	public URL getIdentifier() {
		
		return url;
	}

	public Throwable getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
