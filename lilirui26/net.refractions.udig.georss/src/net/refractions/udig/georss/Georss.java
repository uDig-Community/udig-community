/**
 * 
 */
package net.refractions.udig.georss;

import java.io.*;
import java.util.*;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.vividsolutions.jts.geom.*;


/**
 * @author RUI LI
 * 
 */
public class Georss {

	/**
	 * 
	 */
	List <EarthQuakeItem> items = new ArrayList<EarthQuakeItem>();
	
	public Georss()throws JDOMException, IOException{
		
		// TODO Auto-generated constructor stub
		SAXBuilder bldr = new SAXBuilder();
		Document xmlDoc = bldr.build("http://earthquake.usgs.gov/eqcenter/catalogs/eqs1day-M2.5.xml");
		Element root = xmlDoc.getRootElement().getChild("channel");
		List quakeList = root.getChildren("item");
		
		

		for(int i=0; i<quakeList.size(); i++){
			Element quake = (Element)quakeList.get(i);
			String quakeTime = quake.getChildText("pubDate");
			String quakeName = quake.getChildText("title");
			String quakeDetl = quake.getChildText("link");
			
			String lat = quake.getChildText("lat", quake.getNamespace("geo:,http://www.w3.org/2003/01/geo/wgs84_pos#"));  
			double quakeLat = Double.valueOf(lat.trim()).doubleValue();
			String longt = quake.getChildText("long", quake.getNamespace("geo:,http://www.w3.org/2003/01/geo/wgs84_pos#"));
			double quakeLong = Double.valueOf(longt.trim()).doubleValue();
			
	
			GeometryFactory gf = new GeometryFactory();
		    Point point = gf.createPoint(new Coordinate(quakeLat, quakeLong));
			String time = quakeTime;
			String name = quakeName;
			String link = quakeDetl;
						
			EarthQuakeItem item = new EarthQuakeItem(point, name, time, link);
			items.add(item);
			System.out.println("------Earthquake Report--------");
			System.out.println("Earthquake Time:"+ time);
			System.out.println("Earthquake Description:"+ name	);
			System.out.println("Details link:"+ link);
			System.out.println("Earthquake Location:"+point);
						
		}
		}
	 public List <EarthQuakeItem> getItems(){
			
		   return items;
		}

}
