/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Wien Government 
 *
 *      http://wien.gov.at
 *      http://www.axios.es 
 *
 * (C) 2010, Vienna City - Municipal Department of Automated Data Processing, 
 * Information and Communications Technologies.
 * Vienna City agrees to license under Lesser General Public License (LGPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.lib.geometry.split;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

import es.axios.lib.geometry.util.GeometryUtil;

/**
 * Test case that rotates each polygon and check the split operation doesn't
 * fail.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class TestcatalogWithRotation {
    private static Logger LOGGER = Logger.getLogger(TestcatalogWithRotation.class.getName());

	public void loopTest(List<Polygon> splitee, LineString splitter, Coordinate center, double increase) { 

		for (double degree = 0; degree < 360;) {

		    
			String msg = "Test using rotation (degree):"+  degree; //$NON-NLS-1$
            LOGGER.fine(msg);
			makeSplit(degree, splitee, splitter, center);
			degree = degree + increase;
		}
	}

	private void makeSplit(double degree, List<Polygon> splitee, LineString splitter, Coordinate center) {

		// convert the degree to radiant
		double radiant = SplitTestUtil.convert(degree);
		// rotate
		LineString rotatedSplitLine = GeometryUtil.rotation(splitter, radiant, center);
		List<Geometry> splitGeometries = new ArrayList<Geometry>();
		for (Polygon poly : splitee) {

			splitGeometries.add(GeometryUtil.rotation(poly, radiant, center));
		}

		SplitBuilder builder = SplitBuilder.newInstansceUsingSplitLine(rotatedSplitLine);
		builder.buildSplit(splitGeometries);

	}

	@Test
	public void loop12() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((50 180, 240 180, 240 20, 50 20, 50 180), (100 140, 190 140, 190 80, 100 80, 100 140))");
		LineString splitter = (LineString) SplitTestUtil
					.read("LINESTRING (80 10, 80 100, 150 160, 200 110, 160 70, 160 10)");
		Coordinate center = new Coordinate(145, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop12_reversed() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((50 180, 240 180, 240 20, 50 20, 50 180), (100 140, 190 140, 190 80, 100 80, 100 140))");
		LineString splitter = (LineString) SplitTestUtil
					.read("LINESTRING (160 10, 160 70, 200 110, 150 160, 80 100, 80 10)");
		Coordinate center = new Coordinate(145, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop13() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((40 185, 40 45, 195 45, 195 15, 265 15, 265 185, 40 185),  (110 150, 185 150, 185 90, 110 90, 110 150))");
		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (95 25, 95 130, 195 130, 195 45, 185 30)");

		Coordinate center = new Coordinate(140, 110);

		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop13_reversedLine() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((40 185, 40 45, 195 45, 195 15, 265 15, 265 185, 40 185),  (110 150, 185 150, 185 90, 110 90, 110 150))");
		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (185 30, 195 45, 195 130, 95 130, 95 25)");

		Coordinate center = new Coordinate(140, 110);

		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop2() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((40 185, 40 45, 195 45, 195 15, 265 15, 265 185, 40 185),  (110 150, 185 150, 185 90, 110 90, 110 150))");
		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (195 45, 195 130, 95 130, 95 40)");

		Coordinate center = new Coordinate(140, 110);

		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 1);
	}

	@Test
	public void loop2degree48() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((40 185, 40 45, 195 45, 195 15, 265 15, 265 185, 40 185),  (110 150, 185 150, 185 90, 110 90, 110 150))");
		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (195 45, 195 130, 95 130, 95 45)");

		Coordinate center = new Coordinate(140, 110);

		int degree = 48;
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);

		makeSplit(degree, splitGeometries, splitter, center);
	}

	@Ignore
	public void loop2degree71() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((40 185, 40 45, 195 45, 195 15, 265 15, 265 185, 40 185),  (110 150, 185 150, 185 90, 110 90, 110 150))");
		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (195 45, 195 130, 95 130, 95 45)");

		Coordinate center = new Coordinate(140, 110);

		int degree = 71;
		// FAIL: falla porque el final de la linea esta "tocandose" con uno de
		// los segmentos, pero realmente no hay interseccion en dicho boundary.
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);

		makeSplit(degree, splitGeometries, splitter, center);
	}

	@Test
	public void loop37() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 160, 60 130, 100 130, 100 100, 60 100, 60 70, 140 70, 140 160, 60 160))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (156 79, 100 100, 100 130, 156 144)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop15() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((45 195, 255 195, 255 15, 45 15, 45 195),  (60 180, 240 180, 240 120, 195 120, 195 150, 105 150, 105 120, 60 120, 60 180),  (60 60, 105 60, 105 30, 60 30, 60 60),  (195 60, 240 60, 240 30, 195 30, 195 60))");

		LineString splitter = (LineString) SplitTestUtil
					.read("LINESTRING (105 120, 195 120, 195 60, 105 60, 105 120, 105 120)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop19() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((30 40, 30 190, 60 190, 60 140, 90 140, 90 160, 150 160, 150 140, 180 140, 180 190, 210 190, 210 20, 150 20, 150 40, 30 40),   (50 70, 50 110, 80 110, 80 90, 70 90, 70 70, 50 70))");

		LineString splitter = (LineString) SplitTestUtil
					.read("LINESTRING (180 10, 150 40, 70 70, 80 90, 150 140, 110 180)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop21() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((30 40, 30 190, 60 190, 60 140, 90 140, 90 160, 150 160, 150 140, 180 140, 180 190, 210 190, 210 20, 150 20, 150 40, 30 40),   (50 70, 50 110, 80 110, 80 90, 70 90, 70 70, 50 70))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (150 40, 70 70, 80 90, 150 140, 90 160)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop25() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((20 180, 220 180, 220 20, 20 20, 20 180),  (40 100, 40 160, 200 160, 200 100, 170 100, 170 130, 70 130, 70 100, 40 100),  (40 60, 70 60, 70 30, 40 30, 40 60),  (170 60, 200 60, 200 30, 170 30, 170 60))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (50 120, 50 40, 190 40, 190 120)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop28() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil.read("POLYGON ((90 70, 90 130, 160 130, 160 70, 90 70))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (120 130, 120 100, 160 100)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop11() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 270, 90 270, 90 230, 110 230, 110 250, 150 250, 150 230, 170 230, 170 270, 200 270, 200 170, 60 170, 60 170, 60 270),  (90 210, 110 210, 110 190, 90 190, 90 210), (150 210, 170 210, 170 190, 150 190, 150 210))");
		LineString splitter = (LineString) SplitTestUtil
					.read("LINESTRING (100 200, 110 210, 110 230, 150 230, 150 210, 110 190)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop11_reversedLine() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 270, 90 270, 90 230, 110 230, 110 250, 150 250, 150 230, 170 230, 170 270, 200 270, 200 170, 60 170, 60 170, 60 270),  (90 210, 110 210, 110 190, 90 190, 90 210), (150 210, 170 210, 170 190, 150 190, 150 210))");
		LineString splitter = (LineString) SplitTestUtil
					.read("LINESTRING (110 190, 150 210, 150 230, 110 230, 110 210, 100 200)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop11degree45() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 270, 90 270, 90 230, 110 230, 110 250, 150 250, 150 230, 170 230, 170 270, 200 270, 200 170, 60 170, 60 170, 60 270),  (90 210, 110 210, 110 190, 90 190, 90 210), (150 210, 170 210, 170 190, 150 190, 150 210))");
		LineString splitter = (LineString) SplitTestUtil
					.read("LINESTRING (110 190, 150 210, 150 230, 110 230, 110 210, 100 200)");

		Coordinate center = new Coordinate(110, 110);
		int degree = 45;
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		makeSplit(degree, splitGeometries, splitter, center);
	}

	@Test
	public void loop36() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 60, 60 160, 160 160, 160 60, 60 60), (80 140, 140 140, 140 80, 80 80, 80 140))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (70 120, 110 150, 150 110, 110 70, 70 100)");

		Coordinate center = new Coordinate(110, 110);

		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 1);
	}

	@Test
	public void loop36degree3() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 60, 60 160, 160 160, 160 60, 60 60), (80 140, 140 140, 140 80, 80 80, 80 140))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (70 120, 110 150, 150 110, 110 70, 70 100)");

		Coordinate center = new Coordinate(110, 110);

		int degree = 3;
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		makeSplit(degree, splitGeometries, splitter, center);

	}

	@Test
	public void loop36_catalog_original() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 60, 60 160, 160 160, 160 60, 60 60), (80 140, 140 140, 140 80, 80 80, 80 140))");

		Polygon splitee2 = (Polygon) SplitTestUtil.read("POLYGON ((80 140, 140 140, 140 80, 80 80, 80 140))");
		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (70 120, 110 150, 150 110, 110 70, 70 100)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		splitGeometries.add(splitee2);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop36_with_reversedLine() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 60, 60 160, 160 160, 160 60, 60 60), (80 140, 140 140, 140 80, 80 80, 80 140))");

		Polygon splitee2 = (Polygon) SplitTestUtil.read("POLYGON ((80 140, 140 140, 140 80, 80 80, 80 140))");
		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (70 100, 110 70, 150 110, 110 150, 70 120)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		splitGeometries.add(splitee2);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	/**
	 * degree =89.99999999999916
	 * 
	 * @throws Exception
	 */
	@Test
	public void loop36degree89() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 60, 60 160, 160 160, 160 60, 60 60), (80 140, 140 140, 140 80, 80 80, 80 140))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (70 120, 110 150, 150 110, 110 70, 70 100)");

		Coordinate center = new Coordinate(110, 110);

		double degree = 89.99999999999916;
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		makeSplit(degree, splitGeometries, splitter, center);

	}

	@Test
	public void loop9() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 270, 90 270, 90 230, 110 230, 110 250, 150 250, 150 230, 170 230, 170 270, 200 270, 200 170, 60 170, 60 170, 60 270),  (90 210, 110 210, 110 190, 90 190, 90 210),  (150 210, 170 210, 170 190, 150 190, 150 210))");

		LineString splitter = (LineString) SplitTestUtil
					.read("LINESTRING (110 210, 110 230, 150 230, 150 210, 110 210, 110 210, 110 210)");
		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop9_reversedLine() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((60 270, 90 270, 90 230, 110 230, 110 250, 150 250, 150 230, 170 230, 170 270, 200 270, 200 170, 60 170, 60 170, 60 270),  (90 210, 110 210, 110 190, 90 190, 90 210),  (150 210, 170 210, 170 190, 150 190, 150 210))");

		LineString splitter = (LineString) SplitTestUtil
					.read("LINESTRING (110 210, 150 210, 150 230, 110 230, 110 210)");
		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop22() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((20 180, 220 180, 220 20, 20 20, 20 180),  (40 100, 40 160, 200 160, 200 100, 170 100, 170 130, 70 130, 70 100, 40 100),  (40 60, 70 60, 70 30, 40 30, 40 60),  (170 60, 200 60, 200 30, 170 30, 170 60))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (70 100, 170 100, 190 40, 60 50, 70 100)");
		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop22_reversedLine() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((20 180, 220 180, 220 20, 20 20, 20 180),  (40 100, 40 160, 200 160, 200 100, 170 100, 170 130, 70 130, 70 100, 40 100),  (40 60, 70 60, 70 30, 40 30, 40 60),  (170 60, 200 60, 200 30, 170 30, 170 60))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (70 100, 60 50, 190 40, 170 100, 70 100)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop27() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((20 180, 220 180, 220 20, 20 20, 20 180),  (40 100, 40 160, 200 160, 200 100, 170 100, 170 130, 70 130, 70 100, 40 100),  (40 60, 70 60, 70 30, 40 30, 40 60),  (170 60, 200 60, 200 30, 170 30, 170 60))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (50 40, 190 40, 170 100, 70 100, 50 40)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

	@Test
	public void loop27_reversedLine() throws Exception {

		Polygon splitee = (Polygon) SplitTestUtil
					.read("POLYGON ((20 180, 220 180, 220 20, 20 20, 20 180),  (40 100, 40 160, 200 160, 200 100, 170 100, 170 130, 70 130, 70 100, 40 100),  (40 60, 70 60, 70 30, 40 30, 40 60),  (170 60, 200 60, 200 30, 170 30, 170 60))");

		LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (50 40, 70 100, 170 100, 190 40, 50 40)");

		Coordinate center = new Coordinate(110, 110);
		List<Polygon> splitGeometries = new ArrayList<Polygon>();
		splitGeometries.add(splitee);
		loopTest(splitGeometries, splitter, center, 0.1);
	}

}
