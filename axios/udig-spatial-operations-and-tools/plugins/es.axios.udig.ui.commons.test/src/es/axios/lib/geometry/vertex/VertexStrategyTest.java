/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Wien Government 
 *
 *      http://wien.gov.at
 *      http://www.axios.es 
 *
 * (C) 2009, Vienna City - Municipal Department of Automated Data Processing, 
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
package es.axios.lib.geometry.vertex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import es.axios.lib.geometry.split.VertexStrategy;

/**
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 */
public class VertexStrategyTest {

	/**
	 * 
	 * <pre>
	 * 
	 *  		+---------------------------+
	 * 			|							|
	 * 			|							|
	 * 			|			o---------------x---------&gt;
	 * 			|							|
	 * 			|							|
	 * 			|							|
	 * 			+---------------------------+
	 * 
	 * 
	 * </pre>
	 */
	@Test
	public void testSimplePolygon() {

		LineString line = (LineString) read("LINESTRING(25 55, 40 55)");
		Polygon polygon = (Polygon) read("POLYGON((10 60, 30 60, 30 50, 10 50, 10 60))");
		Polygon neighbor = (Polygon) read("POLYGON((30 60, 30 50, 40 50, 40 60, 30 60))");

		List<Geometry> neighborList = new ArrayList<Geometry>(1);
		neighborList.add(neighbor);
		Geometry result = VertexStrategy.addIntersectionVertex(polygon, line, neighborList);

		assertNotNull(result);

		System.out.println(result.toText());

		assertEquals(result.toText(), "POLYGON ((10 60, 30 60, 30 55, 30 50, 10 50, 10 60))");
	}

	/**
	 * <pre>
	 * 
	 *  		+---------------------------+---------------+-----------+
	 * 			|							|				|			|
	 * 			|							|				|			|
	 * 			|			o---------------x---------------x-------&gt;	|
	 * 			|							|				|			|
	 * 			|							|				|			|
	 * 			|							|				|			|
	 * 			+---------------------------+---------------+-----------+
	 * 
	 * 
	 * </pre>
	 */
	@Test
	public void testSplittingPolygonBetweenNeighbours() {

		LineString line = (LineString) read("LINESTRING(15 20, 35 20)");
		Polygon polygon = (Polygon) read("POLYGON((10 30, 20 30, 20 10, 10 10, 10 30))");
		Polygon neighbour = (Polygon) read("POLYGON((20 30, 30 30, 30 10, 20 10, 20 30))");

		List<Geometry> neighborList = new ArrayList<Geometry>(1);
		neighborList.add(neighbour);
		Geometry result = VertexStrategy.addIntersectionVertex(polygon, line, neighborList);
		assertNotNull(result);
		System.out.println(result.toText());

		assertEquals(result.toText(), "POLYGON ((10 30, 20 30, 20 20, 20 10, 10 10, 10 30))");

		Polygon polygon2 = (Polygon) read("POLYGON((30 30, 40 30, 40 10, 30 10, 30 30))");

		Geometry result2 = VertexStrategy.addIntersectionVertex(polygon2, line, neighborList);
		assertNotNull(result2);
		System.out.println(result2.toText());

		assertEquals(result2.toText(), "POLYGON ((30 30, 40 30, 40 10, 30 10, 30 20, 30 30))");
	}

	/**
	 * 
	 * 
	 * <pre>
	 * 
	 * 			+-------------------------------+
	 * 			|								|
	 * 			|								|
	 * 			|		o---------------o		|
	 *  		|		|				|		|	
	 *  		|		|				|		|
	 *  		|	o---x---------------x---&gt;	|
	 *  		|		|				|		|
	 *  		|		|				|		|
	 *  		|		o---------------o		|
	 *  		|								|
	 *  		|								|
	 *  		+-------------------------------+
	 * 
	 * 
	 * 
	 * </pre>
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	@Test
	public void splitFeaturesWithHolesCase1() {

		Polygon polygon = (Polygon) read("POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 20 30, 20 10, 10 10, 10 30))");
		Polygon neighbour = (Polygon) read("POLYGON ((10 30, 20 30, 20 10, 10 10, 10 30))");
		LineString line = (LineString) read("LINESTRING(8 28, 25 28)");

		List<Geometry> neighbourList = new ArrayList<Geometry>(1);
		neighbourList.add(neighbour);
		Geometry result = VertexStrategy.addIntersectionVertex(polygon, line, neighbourList);

		assertNotNull(result);
		System.out.println(result.toText());

		assertEquals(result.toText(),
					"POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 20 30, 20 28, 20 10, 10 10, 10 28, 10 30))");
	}

	/**
	 * <pre>
	 * 
	 * 
	 * 
	 * 			+-------------------------------+
	 * 			|				o				|
	 * 			|				|				|
	 * 			|		o-------x-------o		|
	 *  		|		|		|		|		|	
	 *  		|		|		|		|		|
	 *  		|		|		|		|		|
	 *  		|		|		|		|		|
	 *  		|		|		|		|		|
	 *  		|		o-------x-------o		|
	 *  		|				|				|
	 *  		|			    V				|
	 *  		+-------------------------------+
	 * 
	 * 
	 * 
	 * </pre>
	 */

	@Test
	public void splitFeaturesWithHolesCase2() {

		Polygon polygon = (Polygon) read("POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 20 30, 20 10, 10 10, 10 30))");
		Polygon neighbour = (Polygon) read("POLYGON ((10 30, 20 30, 20 10, 10 10, 10 30))");
		LineString line = (LineString) read("LINESTRING(18 5, 18 35)");

		List<Geometry> neighbourList = new ArrayList<Geometry>(1);
		neighbourList.add(neighbour);
		Geometry result = VertexStrategy.addIntersectionVertex(polygon, line, neighbourList);

		assertNotNull(result);
		System.out.println(result.toText());

		assertEquals(result.toText(),
					"POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 18 30, 20 30, 20 10, 18 10, 10 10, 10 30))");
	}

	/**
	 * 
	 * 
	 * <pre>
	 * 
	 * 
	 * 
	 * 			+-------------------------------+
	 * 			|								|
	 * 			|								|
	 * 			|		o---------------o		|
	 *  		|		|				|		|	
	 *  		|		|				|		|
	 *  		|		|				|		|
	 *  		|	O---x-------o		|		|
	 *  		|		|		|		|		|
	 *  		|		o-------x-------o		|
	 *  		|				|				|
	 *  		|			    V				|
	 *  		+-------------------------------+
	 * 
	 * 
	 * 
	 * </pre>
	 */
	@Test
	public void splitFeaturesWithHolesCase3() {

		Polygon polygon = (Polygon) read("POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 20 30, 20 10, 10 10, 10 30))");
		Polygon neighbour = (Polygon) read("POLYGON ((10 30, 20 30, 20 10, 10 10, 10 30))");
		LineString line = (LineString) read("LINESTRING(13 5, 13 13, 8 13)");

		List<Geometry> neighbourList = new ArrayList<Geometry>(1);
		neighbourList.add(neighbour);
		Geometry result = VertexStrategy.addIntersectionVertex(polygon, line, neighbourList);

		assertNotNull(result);
		System.out.println(result.toText());

		assertEquals(result.toText(),
					"POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 20 30, 20 10, 13 10, 10 10, 10 13, 10 30))");
	}

	/**
	 * <pre>
	 * 
	 * 			+-------------------------------+
	 * 			|								|
	 * 			|								|
	 * 			|		o---------------o		|
	 *  		|		|				|		|	
	 *  		|		|				|		|
	 *    o-------------x---------------x---&gt;	|
	 *  		|		|				|		|
	 *  		|		|				|		|
	 *  		|		o---------------o		|
	 *  		|								|
	 *  		|								|
	 *  		+-------------------------------+
	 * 
	 * 
	 * 
	 * </pre>
	 */
	@Test
	public void splittingPolygonInsideOtherPolygon() {

		Polygon polygon = (Polygon) read("POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 20 30, 20 10, 10 10, 10 30))");
		Polygon neighbour = (Polygon) read("POLYGON ((10 30, 20 30, 20 10, 10 10, 10 30))");
		LineString line = (LineString) read("LINESTRING(2 20, 25 20)");

		List<Geometry> neighbourList = new ArrayList<Geometry>(1);
		neighbourList.add(neighbour);
		Geometry result = VertexStrategy.addIntersectionVertex(polygon, line, neighbourList);

		assertNotNull(result);
		System.out.println(result.toText());

		assertEquals(result.toText(),
					"POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 20 30, 20 20, 20 10, 10 10, 10 20, 10 30))");
	}

	/**
	 * <pre>
	 * 
	 * 			+---------------------------------------------------+
	 * 			|													|
	 * 			|													|
	 * 			|		o---------------o		o-----------o		|
	 *  		|		|				|		|			|		|
	 *  		|		|				|		|			|		|
	 *    o-------------x---------------x----------------------&gt;	|
	 *  		|		|				|		|			|		|
	 *  		|		|				|		|			|		|
	 *  		|		o---------------o		o-----------o		|
	 *  		|													|
	 *  		|													|
	 *  		+---------------------------------------------------+
	 * 
	 * 
	 * 
	 * </pre>
	 */
	@Test
	public void splitPolygonWithMoreThanOneHole() {

		Polygon polygon = (Polygon) read("POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 14 30, 14 10, 10 10, 10 30), (16 20, 20 20, 20 10, 16 10, 16 20))");
		LineString line = (LineString) read("LINESTRING(2 13, 25 13)");
		Polygon neighbour = (Polygon) read("POLYGON ((10 30, 14 30, 14 10, 10 10, 10 30))");

		List<Geometry> neighbourList = new ArrayList<Geometry>(1);
		neighbourList.add(neighbour);
		Geometry result = VertexStrategy.addIntersectionVertex(polygon, line, neighbourList);

		assertNotNull(result);
		System.out.println(result.toText());

		assertEquals(
					result.toText(),
					"POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 14 30, 14 13, 14 10, 10 10, 10 13, 10 30), (16 20, 20 20, 20 10, 16 10, 16 20))");
	}

	/**
	 * <pre>
	 * 
	 * 	o-------------------------------------------o
	 * 		|											|
	 * 		|											|
	 * 		|	+-------------------------------+		|
	 * 		|	|								|		|
	 * 		|	|								|		|
	 * 		|	|		o---------------o		|		|
	 *  	|	|		|				|		|		|
	 *  	|	|		|				|		|		|
	 *    	o-----------x--&gt;	   o----x---------------o
	 *  		|		|				|		|
	 *  		|		|				|		|
	 *  		|		o---------------o		|
	 *  		|								|
	 *  		|								|
	 *  		+-------------------------------+
	 * 
	 * 
	 * 
	 * </pre>
	 */
	@Test
	public void splitPolygonWithTheLineGoingOutside() {

		Polygon polygon = (Polygon) read("POLYGON ((10 30, 20 30, 20 10, 10 10, 10 30))");
		Polygon neighbor = (Polygon) read("POLYGON ((5 40, 30 40, 30 0, 5 0, 5 40), (10 30, 20 30, 20 10, 10 10, 10 30))");
		LineString line = (LineString) read("LINESTRING(18 20, 35 20, 35 50, 0 50, 0 20, 12 20)");

		List<Geometry> neighbourList = new ArrayList<Geometry>(1);
		neighbourList.add(neighbor);
		Geometry result = VertexStrategy.addIntersectionVertex(polygon, line, neighbourList);

		assertNotNull(result);
		System.out.println(result.toText());

		assertEquals(result.toText(), "POLYGON ((10 30, 20 30, 20 20, 20 10, 10 10, 10 20, 10 30))");
	}

	private Geometry read(final String wkt) {

		WKTReader reader = new WKTReader();
		Geometry geometry;
		try {
			geometry = reader.read(wkt);
		} catch (ParseException e) {
			throw (RuntimeException) new RuntimeException().initCause(e);
		}
		return geometry;
	}

}
