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
package es.axios.udig.ui.editingtools.internal.geometryoperations.split;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import es.axios.udig.ui.editingtools.internal.geometryoperations.split.SplitGraph;
import es.axios.udig.ui.editingtools.internal.geometryoperations.split.SplitGraphNode;

/**
 * Test suite for {@link SplitGraph}
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class SplitGraphTest extends TestCase {

    public SplitGraphTest( String name ) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Simple test, rectangle splitted by center vertical line
     * 
     * <pre>
     * <code>
     *                                      .
     *                                     /|\
     *                                      |
     *                                ------o-------
     *                                |     |      |
     *                                |     |      |
     *                                |     |      |
     *                                |     |      |
     *                                |     |      |
     *                                o_____o______|
     *                                      |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testBuildGraph_SplitPolygonOnce() throws Exception {
        LineString splitter = (LineString) read("LINESTRING(30 0, 30 60)");
        Polygon splitee = (Polygon) read("POLYGON((20 20, 40 20, 40 40, 20 40, 20 20))");
        // Geometry expectedLeft = read("POLYGON ((30 20, 20 20, 20 40, 30 40,
        // 30 20))");
        // Geometry expectedRight = read("POLYGON ((30 40, 40 40, 40 20, 30 20,
        // 30 40))");

        SplitGraph graph = new SplitGraph(splitee, splitter);

        System.out.println("Nodes: " + graph.getNodes());
        System.out.println("EdgeEnds: " + graph.getEdgeEnds());
        assertEquals(3, graph.getNodes().size());
        assertEquals(8, graph.getEdgeEnds().size());
    }

    /**
     * <pre>
     * <code>
     *               +--------------+
     *               |              |
     *               |              |
     *               |   +------+   |
     *               |   |      |   |
     *               |   |      |   |
     *               o___o______o___|
     *                   |      |
     *                   |     \|/
     *                          .
     *                
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testBuildGraph_CutoutBlock() throws Exception {
        LineString splitter = (LineString) read("LINESTRING(20 0, 20 30, 30 30, 30 0)");
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 10))");
        // final Geometry expectedLeft = read("POLYGON ((20 10, 10 10, 10 40, 40
        // 40, 40 10, 30 10, 30 30, 30 30, 20 30, 20 30, 20 10))");
        // final Geometry expectedRight = read("POLYGON ((30 10, 20 10, 20 30,
        // 30 30, 30 10))");

        SplitGraph graph = new SplitGraph(splitee, splitter);

        System.out.println("Nodes: " + graph.getNodes());
        System.out.println("EdgeEnds: " + graph.getEdgeEnds());
        assertEquals(3, graph.getNodes().size());
        assertEquals(8, graph.getEdgeEnds().size());
    }

    /**
     * <pre>
     * <code>
     *                      .
     *                     /|\
     *                      |
     *               +------o------+
     *               |      |      |
     *               |  +---o--+   |
     *               |  |   |  |   |
     *               |  o___o__|   |  
     *               |      |      |
     *               o______o______|
     *                      |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testBuildGraph_SplitGeomWithHole() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 10), (15 15, 15 30, 30 30, 30 15, 15 15))");
        LineString splitter = (LineString) read("LINESTRING(20 0, 20 22, 20 60)");
        // Geometry expectedLeft = read("POLYGON ((20 10, 10 10, 10 40, 20 40,
        // 20 30, 15 30, 15 15, 20 15, 20 10))");
        // Geometry expectedRight = read("POLYGON ((20 40, 40 40, 40 10, 20 10,
        // 20 15, 30 15, 30 30, 20 30, 20 40))");

        SplitGraph graph = new SplitGraph(splitee, splitter);

        System.out.println("Nodes: " + graph.getNodes());
        System.out.println("EdgeEnds: " + graph.getEdgeEnds());
        assertEquals(6, graph.getNodes().size());
        assertEquals(16, graph.getEdgeEnds().size());
    }

    /**
     * <pre>
     * <code>
     *                .
     *               /|\
     *                |
     *             +--o-------+
     *             |  |       |
     *             |  |       |
     *             +--o--+    |
     *                | /     |
     *                |/      |
     *                o       |
     *               /|       |
     *              | |       |
     *              o_o_______|
     *                |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testBuildGraph_DoubleIntersection() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 30, 20 30, 10 20, 10 10))");
        LineString splitter = (LineString) read("LINESTRING(15 0, 15 60)");

        // Geometry expectedLeft = read("POLYGON ((15 10, 10 10, 10 20, 15 25,
        // 15 10))");
        // Geometry expectedMiddle = read("POLYGON ((15 40, 40 40, 40 10, 15 10,
        // 15 25, 20 30, 15 30, 15 40))");
        // Geometry expectedRight = read("POLYGON ((15 30, 10 30, 10 40, 15 40,
        // 15 30))");

        SplitGraph graph = new SplitGraph(splitee, splitter);

        System.out.println("Nodes: " + graph.getNodes());
        System.out.println("EdgeEnds: " + graph.getEdgeEnds());
        assertEquals(5, graph.getNodes().size());
        assertEquals(14, graph.getEdgeEnds().size());
    }

    /**
     * <pre>
     * <code>
     *                 |
     *               +-o-------+
     *               | |       |
     *               |_o___    |
     *                 |   |   |
     *                _o___|   |
     *               | |       |
     *               o_o_______|
     *                 |     
     *                \|/
     *                 . 
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testBuildGraph_DoubleIntersectionReversedCut() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 30, 20 30, 20 20, 10 20, 10 10))");
        LineString splitter = (LineString) read("LINESTRING(15 60, 15 0)");

        // Geometry expectedLeft = read("POLYGON ((15 10, 10 10, 10 20, 15 20,
        // 15 10))");
        // Geometry expectedMiddle = read("POLYGON ((15 40, 40 40, 40 10, 15 10,
        // 15 20, 20 20, 20 30, 15 30, 15 40))");
        // Geometry expectedRight = read("POLYGON ((15 30, 10 30, 10 40, 15 40,
        // 15 30))");
        //
        // Geometry[] expectedParts = new Geometry[]{expectedLeft,
        // expectedMiddle, expectedRight};

        SplitGraph graph = new SplitGraph(splitee, splitter);

        System.out.println("Nodes: " + graph.getNodes());
        System.out.println("EdgeEnds: " + graph.getEdgeEnds());
        assertEquals(5, graph.getNodes().size());
        assertEquals(14, graph.getEdgeEnds().size());
    }

    /**
     * <pre>
     * <code>
     *                     .
     *                    /|\
     *                     |
     *                     |
     *             +-------o
     *             |       |
     *             |       |
     *             |       |
     *             |       o-----+
     *             |       |     |
     *             |       |     |
     *             |       |     |
     *             |       |     |
     *             o-------o-++--+
     *                     |     
     *                     |     
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testBuildGraph_IntersectsVertexAndEdge() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 15.5 10, 15.7 10, 15.8 10, 20 10, 20 20, 15.5 20, 15.5 30, 10 30, 10 10))");
        LineString splitter = (LineString) read("LINESTRING(15.5 0, 15.5 40)");

        // Geometry expectedLeft = read("POLYGON ((15.5 10, 10 10, 10 30, 15.5
        // 30, 15.5 10))");
        // Geometry expectedRight = read("POLYGON ((15.5 20, 20 20, 20 10, 15.8
        // 10, 15.7 10, 15.5 10, 15.5 20))");
        //
        // Geometry[] expectedParts = new Geometry[]{expectedLeft,
        // expectedRight};

        SplitGraph graph = new SplitGraph(splitee, splitter);

        System.out.println("Nodes: " + graph.getNodes());
        System.out.println("EdgeEnds: " + graph.getEdgeEnds());
        assertEquals(4, graph.getNodes().size());
        assertEquals(10, graph.getEdgeEnds().size());
    }

    public void testBuildGraph() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON ((484931.33221207117 4823429.360774391, 576441.460505905 4823429.360773954, 576441.4605056487 4770404.800438415, 484931.3322121216 4770404.800438844, 484931.33221207117 4823429.360774391))");
        LineString splitter = (LineString) read("LINESTRING (518738.74733960454 4831558.988908185, 519056.4924999358 4763720.397139888, 526682.3763498047 4760066.327794093)");

        SplitGraph graph = new SplitGraph(splitee, splitter);

        System.out.println("Nodes: " + graph.getNodes());
        System.out.println("EdgeEnds: " + graph.getEdgeEnds());

        assertEquals(3, graph.getNodes().size());
        assertEquals(8, graph.getEdgeEnds().size());
    }

    public void testBuildGraph3() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON ((-2.05630239088747 43.383785592776206, -2.060964497966142 43.08091005152631, -2.4598370614233303 43.08348812503837, -2.5916373277716898 43.08403436779535, -3.1851208791748506 43.08461326221585, -3.1857959859927067 43.30753769431183, -3.185956334129664 43.36011198917018, -3.1860512944056665 43.39118019719231, -3.1860873520587085 43.402964186987994, -2.5261988548074474 43.561229494969545, -2.0535830120584198 43.55829389231849, -2.05630239088747 43.383785592776206))");
        LineString splitter = (LineString) read("LINESTRING (-2.3129794071592786 43.629039402056485, -2.9465587625515224 42.99163177261353)");

        SplitGraph graph = new SplitGraph(splitee, splitter);

        System.out.println("Nodes: " + graph.getNodes());
        System.out.println("EdgeEnds: " + graph.getEdgeEnds());

        assertEquals(3, graph.getNodes().size());
        assertEquals(8, graph.getEdgeEnds().size());
    }

    /**
     * <pre>
     * <code>
     *              c3     c4      c5
     *               +------o------+
     *               |      |      |
     *               |      |      |
     *               |      |      |
     *               |      |      |  
     *               |      |      |
     *               o______o______|
     *               c2     c1      c6
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    public void testRemoveEdge() throws Exception {
        Polygon splitee = (Polygon) read("POLYGON((10 10, 40 10, 40 40, 10 40, 10 10))");
        LineString splitter = (LineString) read("LINESTRING(20 0, 20 60)");

        SplitGraph graph = new SplitGraph(splitee, splitter);
        assertEquals(3, graph.getNodes().size());
        assertEquals(8, graph.getEdgeEnds().size());

        try {
            graph.remove((DirectedEdge) null);
            fail("Expected NPE");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        Coordinate c1 = new Coordinate(20, 10);
        Coordinate c2 = new Coordinate(10, 10);
        Coordinate c3 = new Coordinate(10, 40);
        Coordinate c4 = new Coordinate(20, 40);
        Coordinate c5 = new Coordinate(40, 40);
        Coordinate c6 = new Coordinate(10, 40);

        // the following calls relies in the nodes degree being decremented
        // after removing each edge
        testRemoveEdge(graph, new Coordinate[]{c1, c2}, c2, 3, 2);
        testRemoveEdge(graph, new Coordinate[]{c2, c3}, c4, 1, 3);
        testRemoveEdge(graph, new Coordinate[]{c4, c5}, c1, 2, 2);
        testRemoveEdge(graph, new Coordinate[]{c1, c4}, c4, 1, 1);
    }

    /**
     * Tests {@link SplitGraph#remove(DirectedEdge)} by removing the edge defined from the initial
     * vector <code>coordNode1[0]:coordNode1[1]</code> and ending at the node in
     * <code>coordNode2</code>
     * 
     * @param graph the graph being tested
     * @param coordNode1 the initial vector of the edge
     * @param coordNode2 the coordinate of the end node
     * @param initialDegree1 the expected initial degree of the first node. It will be checked that
     *        the node has degree = <code>initialDegree1 - 1</code> after removing the edge
     * @param initialDegree2 the expected initial degree of the end node. It will be checked that
     *        the end node has degree = <code>initialDegree2 - 1</code> after removing the edge
     */
    private void testRemoveEdge( SplitGraph graph, final Coordinate[] coordNode1,
                                 final Coordinate coordNode2, final int initialDegree1,
                                 final int initialDegree2 ) {

        final SplitGraphNode node1 = (SplitGraphNode) graph.find(coordNode1[0]);
        assertNotNull(node1);
        final SplitGraphNode node2 = (SplitGraphNode) graph.find(coordNode2);
        assertNotNull(node2);

        assertEquals(initialDegree1, node1.getEdges().getDegree());
        assertEquals(initialDegree2, node2.getEdges().getDegree());

        final Edge edge = graph.findEdge(coordNode1[0], coordNode1[1]);
        assertNotNull(edge);

        final DirectedEdge end = (DirectedEdge) graph.findEdgeEnd(edge);
        assertNotNull(end);

        // remove the edge
        graph.remove(end);

        assertNull(graph.findEdge(coordNode1[0], coordNode1[1]));
        assertNull(graph.findEdgeEnd(edge));

        int expectedDegree1 = initialDegree1 - 1;
        int expectedDegree2 = initialDegree2 - 1;
        assertEquals(expectedDegree1, node1.getEdges().getDegree());
        assertEquals(expectedDegree2, node2.getEdges().getDegree());
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
