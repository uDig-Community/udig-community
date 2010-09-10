package net.refractions.udig.georss;

import java.io.*;
import java.util.*;


import org.geotools.data.*;
import org.geotools.feature.*;
import org.jdom.JDOMException;

import com.vividsolutions.jts.geom.Point;
import net.refractions.udig.georss.Georss;
/**
 * @author RUI LI
 * 
 * 
 * 
 */

public class GeoRSSDataStore extends AbstractDataStore{
	

	public static DefaultFeatureType createEarthQuakeFeatureType(){
		AttributeType[] types = new AttributeType[4];
		types[0] = AttributeTypeFactory.newAttributeType("point", Point.class);
		types[1] = AttributeTypeFactory.newAttributeType("time", String.class);
		types[2] = AttributeTypeFactory.newAttributeType("place", String.class);
		types[3] = AttributeTypeFactory.newAttributeType("link", String.class);

		DefaultFeatureTypeFactory factory = new DefaultFeatureTypeFactory();
		factory.addTypes(types);
		
		
	    DefaultFeatureType ftEarthQuake = null;
	  	  try{
	  		factory.setName("earthquake");
			ftEarthQuake= (DefaultFeatureType)factory.getFeatureType();
			 } 
		     catch (SchemaException e){
				e.printStackTrace();
			}
		     return ftEarthQuake;
	}

	
		
	public static DefaultFeature createEarthQuakeFeature(EarthQuakeItem item){
		Object[] featureAttributes = {item.point, item.time,item.name, item.link};
		DefaultFeature fEarthQuake = null;
		
		try {
			DefaultFeatureType ftEarthQuake = createEarthQuakeFeatureType();
			fEarthQuake = (DefaultFeature)ftEarthQuake.create(featureAttributes);
			} catch (IllegalAttributeException e){
			e.printStackTrace();
			}
			return fEarthQuake;
    }
		
		public static FeatureCollection createFeatureCollection()throws IOException,JDOMException {
			Georss georss = new Georss();
			List items = georss.getItems();
			FeatureCollection fcEarthQuake = FeatureCollections.newCollection();
			
			for (int i=0; i<items.size();i++){
			EarthQuakeItem item = (EarthQuakeItem)items.get(i);
		    DefaultFeature fEarthQuake = createEarthQuakeFeature (item);
			fcEarthQuake.add(fEarthQuake);
			}
			
			return fcEarthQuake; 
		}
	
	 
		
	 public CollectionFeatureReader getFeatureReader(String typeNames)throws IOException {
		 	
		 DefaultFeatureType ftEarthQuake = null;
		 FeatureCollection fcEarthQuake = null;
		 try{
				ftEarthQuake = createEarthQuakeFeatureType(); 
			    fcEarthQuake = createFeatureCollection();	
				
			} catch (IOException e){
				e.printStackTrace();
				throw new IOException("Unable to parse GeoRSS feed.");
			} catch (JDOMException ee){
				ee.printStackTrace();
			} 
				
			    return new CollectionFeatureReader(fcEarthQuake,ftEarthQuake);
	 }
	 
	 public DefaultFeatureType getSchema(String typeName){
		 return createEarthQuakeFeatureType(); 
		 }
	 
     public String[] getTypeNames(){
		 String[] typeName = new String []{"GeoRSSFeatures"};
		 return typeName;
	 }
     
     
	 
}
	