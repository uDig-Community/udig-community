/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
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
package es.axios.udig.ui.editingtools.internal.geometryoperations;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import es.axios.udig.ui.editingtools.internal.geometryoperations.split.SplitStrategy;

/**
 * Test suite for {@link SplitStrategy}
 * <p>
 * To create a test case:
 * <ul>
 * <li>Open JTS testBuilder (cd <jts dir>/bin; sh testBuilder.sh)
 * <li>Set precision model to double precision (Edit/Precision Model...)
 * <li>Set geometry A to be the splitting line, either drwaing it or through the A field in the WKT
 * pane
 * <li>Set geometry B to be the one to be splitted, either drwaing it or through the A field in the
 * WKT pane
 * <li>Go to the geometric funcions tab and make a buffer of A with <code>0.000001</code> width
 * and <code>1</code> quadrant segment
 * <li>Copy the resulting polygon WKT from the result pane and paste it as geometry A in the WKT
 * pane
 * <li>Go to the Geometric Funcions tab and calculate <code>B - A</code> (B difference A)
 * <li>Copy the resulting geometry WKT from the result pane and paste it as geometry A in the WKT
 * pane
 * <li>Set the precision model to Fixed with a Scale of <code>7</code>
 * <li>Now you will see the WKT for the geometry A changed to adjust the coordinates to the
 * precision model. The parts of the resulting geometry (i.e. each polygon inside the MULTIPOLYGON)
 * are the expected operation results.
 * <li>Copy the parts in the fields A and B ensuring they're properly formatted as POLYGON or
 * LINESTRING depending on what kind of geometry you used as input and go to the Predicates tab.
 * Press Run and ensure the predicates are <code>true</code> for <code>Intersects</code> and
 * <code>Touches</code>, but <code>false</code> for the rest.
 * </ul>
 * Now you have your input and expected results.
 * </p>
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class SplitOpTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests the validity of splitting a <code>geometry</code> (polygon, multipolygon, linestring
     * or multilinestring) with the given <code>splitter</code> <code>LineString</code>.
     * <p>
     * To test the validity the following assertiong are ran:
     * <ul>
     * <li>The resulting geometry is not <code>null</code>
     * <li>The resulting geometry is an instance of GeometryCollection
     * <li>The number of parts of the resulting geometry is equal to the length of the passed
     * <code>expectedParts</code> array
     * <li>The parts of the resulting geometry collection corresponds with the ones passed as
     * <code>expectedParts</code>, in the given order.
     * <li>The union of the resulting parts is equal to the original <code>splitee</code>
     * geometry.
     * </ul>
     * </p>
     * 
     * @param splitee the JTS polygon to be splitted by <code>splitter</code>
     * @param splitter the splitting line
     * @param expectedParts the parts expected, in order, non <code>null</code>
     */
    private Geometry testSplitResults( Geometry splitee, LineString splitter,
                                       Geometry[] expectedParts ) {

        final Geometry splitted = testSplitResults(splitee, splitter);

        assertEquals(expectedParts.length, splitted.getNumGeometries());

        final int numGeoms = splitted.getNumGeometries();

        for( int expectedPartN = 0; expectedPartN < numGeoms; expectedPartN++ ) {
            boolean found = false;
            Geometry expectedPart = expectedParts[expectedPartN];
            expectedPart.normalize();

            for( int splittedPartN = 0; splittedPartN < numGeoms; splittedPartN++ ) {
                Geometry splittedPart = splitted.getGeometryN(splittedPartN);
                splittedPart.normalize();
                if (expectedPart.equals(splittedPart)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail(expectedPart + " not found in " + splitted);
            }
        }

        return splitted;
    }

    private Geometry testSplitResults( Geometry splitee, LineString splitter, int expectedPartCount ) {

        final Geometry splitted = testSplitResults(splitee, splitter);

        assertEquals(splitted.toString(), expectedPartCount, splitted.getNumGeometries());

        return splitted;
    }

    private Geometry testSplitResults( Geometry splitee, LineString splitter ) {

        final Geometry splitted = SplitStrategy.splitOp(splitee, splitter);

        assertNotNull(splitted);

        Geometry union = null;
        for( int i = 0; i < splitted.getNumGeometries(); i++ ) {
            Geometry part = splitted.getGeometryN(i);
            if (union == null) {
                union = part;
            } else {
                union = union.union(part);
            }
        }

        Geometry symDifference = splitee.symDifference(union);
        if (splitee instanceof Polygon) {
            double maxTolerableAreaDiff = splitee.getArea() / 10000;
            double area = symDifference.getArea();
            assertTrue("difference area greater than maximum allowed: " + area + " > "
                    + maxTolerableAreaDiff, area < maxTolerableAreaDiff);
        }

        return splitted;
    }

    /**
     * Simple test, rectangle splitted by center vertical line
     * 
     * <pre>
     * <code>
     *                   .
     *                  /|\
     *                   |
     *             +-----|------+
     *             |     |      |
     *             |     |      |
     *             |     |      |
     *             |     |      |
     *             |     |      |
     *             |_____|______|
     *                   |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitPolygon_SplitPolygonOnce() throws Exception {
        LineString splitter = (LineString) read("LINESTRING(30 0, 30 60)");
        Polygon splitee = (Polygon) read("POLYGON((20 20, 40 20, 40 40, 20 40, 20 20))");
        Geometry expectedLeft = read("POLYGON ((30 20, 20 20, 20 40, 30 40, 30 20))");
        Geometry expectedRight = read("POLYGON ((30 40, 40 40, 40 20, 30 20, 30 40))");

        testSplitResults(splitee, splitter, new Geometry[]{expectedLeft, expectedRight});
    }

    /**
     * <pre>
     * <code>
     *              +--------------+
     *              |              |
     *              |              |
     *              |   +------+   |
     *              |   |      |   |
     *              |   |      |   |
     *              |___|______|___|
     *                  |      |
     *                  |     \|/
     *                         .
     *                                 
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitPolygon_CutoutBlock() throws Exception {
        LineString splitter = (LineString) read("LINESTRING(20 0, 20 30, 30 30, 30 0)");
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 10))");
        final Geometry expectedLeft = read("POLYGON ((20 10, 10 10, 10 40, 40 40, 40 10, 30 10, 30 30, 30 30, 20 30, 20 30, 20 10))");
        final Geometry expectedRight = read("POLYGON ((30 10, 20 10, 20 30, 30 30, 30 10))");
        testSplitResults(splitee, splitter, new Geometry[]{expectedLeft, expectedRight});
    }

    /**
     * <pre>
     * <code>
     *                             .
     *                            /|\
     *              10,40          |20,40       40,40
     *                  +----------|-----------+
     *                  |          |           |
     *                  |15,30     |20,30      |
     *                  |    +-----------+30,30|
     *                  |    |     |     |     |
     *                  |    |     |     |     |
     *                  |    |     |     |     |
     *                  |    |_____|_____|30,15|
     *                  | 15,15    |20,15      |
     *                  |          |           |
     *                  |__________|___________| 
     *                10,10        |20,10      40,10
     *                             |
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitPolygon_SplitGeomWithHole() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 10), (15 15, 15 30, 30 30, 30 15, 15 15))");
        LineString splitter = (LineString) read("LINESTRING(20 0, 20 22, 20 60)");
        Geometry expectedLeft = read("POLYGON ((20 10, 10 10, 10 40, 20 40, 20 30, 15 30, 15 15, 20 15, 20 10))");
        Geometry expectedRight = read("POLYGON ((20 40, 40 40, 40 10, 20 10, 20 15, 30 15, 30 30, 20 30, 20 40))");

        testSplitResults(splitee, splitter, new Geometry[]{expectedLeft, expectedRight});
    }

    /**
     * <pre>
     * <code>
     *                 15,60
     *                   .
     *                  /|\
     *                   |15,40
     *            10,40+-|------------+40,40
     *                 | | 30,30      |
     *                 | |  +-----+30,30
     *                 | |  |     |   |
     *                 | |  |_____|   | 
     *                 | | 20,20  30,20
     *                 |_|____________|
     *             10,10 |15,10       10,40
     *                  15,0
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitPolygon_SplitGeomWithHoleNoHoleBisection() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 10), (20 20, 20 30, 30 30, 30 20, 20 20))");
        LineString splitter = (LineString) read("LINESTRING(15 0, 15 60)");
        Geometry expectedLeft = read("POLYGON ((15 10, 10 10, 10 40, 15 40, 15 10))");
        Geometry expectedRight = read("POLYGON ((15 40, 40 40, 40 10, 15 10, 15 40), (20 20, 30 20, 30 30, 20 30, 20 20))");

        testSplitResults(splitee, splitter, new Geometry[]{expectedLeft, expectedRight});
    }

    /**
     * <pre>
     * <code>
     *                      .
     *                     /|\
     *                      |
     *                   +--|-------+
     *                   |  |       |
     *                   |  |       |
     *                   +--|--+    |
     *                      | /     |
     *                       /      |
     *                      /       |
     *                     /        |
     *                    |         |
     *                    o_\_______|
     *                      /
     *                            
     * </code>
     * </pre> *
     * 
     * @throws Exception
     */
    public void testSplitPolygon2() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON ((10 10, 40 10, 40 40, 10 40, 10 30, 20 30, 10 20, 10 10))");
        LineString splitter = (LineString) read("LINESTRING (15 30, 15 40)");

        Geometry expectedLeft = read("POLYGON ((10 10, 40 10, 40 40, 15 40, 15 30, 20 30, 10 20, 10 10))");
        Geometry expectedRight = read("POLYGON ((15 30, 10 30, 10 40, 15 40, 15 30))");

        Geometry[] expectedParts = new Geometry[]{expectedLeft, expectedRight};
        testSplitResults(splitee, splitter, expectedParts);
    }

    /**
     * <pre>
     * <code>
     *                    .
     *                   /|\
     *                    |
     *                 +--|-------+
     *                 |  |       |
     *                 |  |       |
     *                 +--|--+    |
     *                    | /     |
     *                    |/      |
     *                    /       |
     *                   /|       |
     *                  | |       |
     *                  |_|_______|
     *                    |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitPolygon_DoubleIntersection() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 30, 20 30, 10 20, 10 10))");
        LineString splitter = (LineString) read("LINESTRING(15 0, 15 60)");

        Geometry expectedLeft = read("POLYGON ((15 10, 10 10, 10 20, 15 25, 15 10))");
        Geometry expectedMiddle = read("POLYGON ((15 40, 40 40, 40 10, 15 10, 15 25, 20 30, 15 30, 15 40))");
        Geometry expectedRight = read("POLYGON ((15 30, 10 30, 10 40, 15 40, 15 30))");

        Geometry[] expectedParts = new Geometry[]{expectedLeft, expectedMiddle, expectedRight};
        testSplitResults(splitee, splitter, expectedParts);
    }

    /**
     * <pre>
     * <code>
     *                    |
     *                  +-|-------+
     *                  | |       |
     *                  |_|___    |
     *                    |   |   |
     *                   _|___|   |
     *                  | |       |
     *                  |_|_______|
     *                    |     
     *                   \|/
     *                    . 
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitPolygon_DoubleIntersectionReversedCut() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 30, 20 30, 20 20, 10 20, 10 10))");
        LineString splitter = (LineString) read("LINESTRING(15 60, 15 0)");

        Geometry expectedLeft = read("POLYGON ((15 10, 10 10, 10 20, 15 20, 15 10))");
        Geometry expectedMiddle = read("POLYGON ((15 40, 40 40, 40 10, 15 10, 15 20, 20 20, 20 30, 15 30, 15 40))");
        Geometry expectedRight = read("POLYGON ((15 30, 10 30, 10 40, 15 40, 15 30))");

        Geometry[] expectedParts = new Geometry[]{expectedLeft, expectedMiddle, expectedRight};
        testSplitResults(splitee, splitter, expectedParts);
    }

    /**
     * <pre>
     * <code>
     *                                     5,4 
     *          |                          /|\
     *    0,3   |             3,3   4,3     |      6,3
     *      ----|---------------      ------|-------
     *      |   |              |      |     |      |
     *      | C |              |      |     |      |
     *      |___|____ 2,2      |      |     |      |
     *     0,2  |    |         |      |     |      |
     *          |    |         |      |     |      |
     *          |    |   B     |      |  D  |  E   |
     *     0,1  |    |2,1      |      |     |      |
     *      ----|----+         |      |     |      |
     *      |   |              |      |     |      |
     *      | A |              |      |     |      |
     *      |___|______________|      |_____|______|
     *     0,0  |              3,0    4,0   |      6,0
     *          |___________________________|
     *          1,-1                       5,-1
     * 
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitMultiPolygon() throws Exception {
        MultiPolygon splitee = (MultiPolygon) read("MULTIPOLYGON(((0 0, 3 0, 3 3, 0 3, 0 2, 2 2, 2 1, 0 1, 0 0)), ((4 0, 6 0, 6 3, 4 3, 4 0)))");
        LineString splitter = (LineString) read("LINESTRING(1 5, 1 -1, 5 -1, 5 4)");

        Geometry partA = read("POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))");
        Geometry partB = read("POLYGON((1 0, 3 0, 3 3, 1 3, 1 2, 2 2, 2 1, 1 1, 1 0))");
        Geometry partC = read("POLYGON((0 2, 1 2, 1 3, 0 3, 0 2))");
        Geometry partD = read("POLYGON((4 0, 5 0, 5 3, 4 3, 4 0))");
        Geometry partE = read("POLYGON((5 0, 6 0, 6 3, 5 3, 5 0))");
        
        Geometry[] expectedParts = {partA, partB, partC, partD, partE};
        
        testSplitResults(splitee, splitter, expectedParts);
    }

    /**
     * <pre>
     * <code>
     *                 .
     *                /|\
     *                 |
     *                 | +------------+
     *                 | |            |
     *                 | |            |
     *                 | |            |
     *                 | |            |
     *                 | |            |
     *                 | |____________|
     *                 |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitPolygon_LineRidesShapeLine() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10.5 10, 20 10, 20 20, 10.5 20, 10.5 10))");

        LineString splitter = (LineString) read("LINESTRING(10 0, 10 30)");

        Geometry expected = read("POLYGON((10.5 10, 20 10, 20 20, 10.5 20, 10.5 10))");

        assertTrue(splitee.equals(expected));

        Geometry[] expectedParts = new Geometry[]{expected};
        testSplitResults(splitee, splitter, expectedParts);
    }

    /**
     * <pre>
     * <code>
     *                     .
     *                    /|\
     *                     |
     *                     |
     *             +-------+
     *             |       |
     *             |       |
     *             |       |
     *             |       +-----+
     *             |       |     |
     *             |       |     |
     *             |       |     |
     *             |       |     |
     *             +-------+++---+
     *                     |     
     *                     |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitPolygon_IntersectsVertexAndEdge() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 15.5 10, 15.7 10, 15.8 10, 20 10, 20 20, 15.5 20, 15.5 30, 10 30, 10 10))");
        LineString splitter = (LineString) read("LINESTRING(15.5 0, 15.5 40)");

        Geometry expectedLeft = read("POLYGON ((15.5 10, 10 10, 10 30, 15.5 30, 15.5 10))");
        Geometry expectedRight = read("POLYGON ((15.5 20, 20 20, 20 10, 15.8 10, 15.7 10, 15.5 10, 15.5 20))");

        Geometry[] expectedParts = new Geometry[]{expectedLeft, expectedRight};
        testSplitResults(splitee, splitter, expectedParts);
    }

    /**
     * <pre>
     * <code>
     *                    .
     *                   /|\
     *                    |      
     *                    |      
     *              +-----+-----+
     *                    |     
     *                    |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitLine_simple() throws Exception {
        LineString splitee = (LineString) read("LINESTRING (15.5 10, 10 10)");
        LineString splitter = (LineString) read("LINESTRING (12.5 0, 12.5 40)");

        Geometry expectedLeft = read("LINESTRING (15.5 10, 12.5 10)");
        Geometry expectedRight = read("LINESTRING (12.5 10, 10 10)");

        Geometry[] expectedParts = new Geometry[]{expectedLeft, expectedRight};
        testSplitResults(splitee, splitter, expectedParts);
    }

    /**
     * <pre>
     * <code>
     *                         +
     *                         |
     *                         | 
     *                         |          \
     *                 --------+-----------}
     *                 |       |          /  
     *                 |       |      
     *                 |       |
     *           +-----+-------+
     *                 |     
     *                 |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitLineMultipleIntersections() throws Exception {
        LineString splitee = (LineString) read("LINESTRING (10 10, 20 10, 20 20)");
        LineString splitter = (LineString) read("LINESTRING (15 5, 15 15, 25 15)");

        Geometry expectedLeft = read("LINESTRING (10 10, 15 10)");
        Geometry expectedMiddle = read("LINESTRING (15 10, 20 10, 20 15)");
        Geometry expectedRight = read("LINESTRING  (20 15, 20 20)");

        Geometry[] expectedParts = new Geometry[]{expectedLeft, expectedMiddle, expectedRight};
        testSplitResults(splitee, splitter, expectedParts);
    }

    /**
     * <pre>
     * <code>
     * 
     *           +-------------+
     *          / \     
     *           |     
     *           |     
     *           |     
     *           |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitLineSharesVertex() throws Exception {
        LineString splitee = (LineString) read("LINESTRING (15.5 10, 10 10)");
        LineString splitter = (LineString) read("LINESTRING (10.5 0, 15.5 10)");

        Geometry expected = read("LINESTRING (15.5 10, 10 10)");

        Geometry[] expectedParts = new Geometry[]{expected};
        testSplitResults(splitee, splitter, expectedParts);
    }

    /**
     * <pre>
     * <code>
     * 
     *            +     /
     *            |    /
     *            |   / 
     *            |  /  
     *            | /   
     *            |/    
     *            +------+
     *           /
     *          /
     *        |/_
     *        
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testSplitLineCutsOnVertex() throws Exception {
        LineString splitee = (LineString) read("LINESTRING (20 10, 10 10, 10 20)");
        LineString splitter = (LineString) read("LINESTRING (20 20, 0 0 )");

        Geometry expectedLeft = read("LINESTRING (20 10, 10 10)");
        Geometry expectedRight = read("LINESTRING (10 10, 10 20)");

        Geometry[] expectedParts = new Geometry[]{expectedLeft, expectedRight};
        testSplitResults(splitee, splitter, expectedParts);
    }

    public void testSplitPolygon() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON ((484931.33221207117 4823429.360774391, 576441.460505905 4823429.360773954, 576441.4605056487 4770404.800438415, 484931.3322121216 4770404.800438844, 484931.33221207117 4823429.360774391))");
        LineString splitter = (LineString) read("LINESTRING (518738.74733960454 4831558.988908185, 519056.4924999358 4763720.397139888, 526682.3763498047 4760066.327794093)");

        testSplitResults(splitee, splitter, 2);
    }

    public void testSplitPolygon3() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON ((-2.05630239088747 43.383785592776206, -2.060964497966142 43.08091005152631,-2.4598370614233303 43.08348812503837, -2.5916373277716898 43.08403436779535, -3.1851208791748506 43.08461326221585,-3.1857959859927067 43.30753769431183, -3.185956334129664 43.36011198917018, -3.1860512944056665 43.39118019719231,-3.1860873520587085 43.402964186987994, -2.5261988548074474 43.561229494969545, -2.0535830120584198 43.55829389231849,-2.05630239088747 43.383785592776206))");
        LineString splitter = (LineString) read("LINESTRING(-2.3129794071592786 43.629039402056485, -2.9465587625515224 42.99163177261353)");

        testSplitResults(splitee, splitter, 2);
    }

    private Geometry read( final String wkt ) {

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
