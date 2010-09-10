package net.refractions.udig.georss;

import java.io.IOException;


import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IService;


public class IGeoResourceGeoRSSInfo extends IGeoResourceInfo {
	CoordinateReferenceSystem crs = null;
	String typename = null;
	IService parent;
		
	IGeoResourceGeoRSSInfo(IService parent) throws IOException {
		
	this.parent=parent;
	
		//GeoRSSGeoResource geoRSSGeoResource = null;
		//IProgressMonitor monitor= null;
		//parent = geoRSSGeoResource.service(monitor);
		
		GeoRSSService service = (GeoRSSService) parent;
		FeatureType ft = service.getDataStore(null).getSchema(typename);
		bounds = new ReferencedEnvelope (-180, 180, -90,90, DefaultGeographicCRS.WGS84);
		GeometryAttributeType defaultGeom = ft.getDefaultGeometry();
		if (defaultGeom ==null){
			crs= null;
		}else{
			crs=DefaultGeographicCRS.WGS84;
		}
		name = typename;
		schema=ft.getNamespace();
		keywords = new String[]{
				"GeoRSS", typename,schema.toString()
		};
	}
		
	public CoordinateReferenceSystem getCRS(){
			if(crs != null)
				return crs;
			return super.getCRS();
		
		
	}	
}
