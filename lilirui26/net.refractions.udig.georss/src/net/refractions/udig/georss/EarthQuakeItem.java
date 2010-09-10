package net.refractions.udig.georss;

import com.vividsolutions.jts.geom.Point;
/**
 * @author RUI LI
 * 
 */
public class EarthQuakeItem {
	
		public final Point point;
		public final String time;
		public final String name;
		public final String link;
		
		public EarthQuakeItem(
				final Point point,
				final String time,
				final String name,
				final String link){
			this.point = point;
			this.name = name;
			this.time = time;
			this.link = link;
		}
	
}
