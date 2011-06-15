/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
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

import java.util.TreeMap;

import junit.framework.TestCase;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.DirectedEdgeStar;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeEnd;
import com.vividsolutions.jts.geomgraph.Label;
import com.vividsolutions.jts.geomgraph.Node;
import com.vividsolutions.jts.geomgraph.NodeMap;

/**
 * Test suite for {@link SplitEdgeStar}
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class SplitEdgeStarTest extends TestCase {

    /**
     * {@link DirectedEdge} subclass only to support testing
     */
    private static class TestDirectedEdge extends DirectedEdge {

        public TestDirectedEdge( Edge edge, boolean isForward ) {
            super(edge, isForward);
        }

        public int hashCode() {
            return getCoordinate().hashCode() * getDirectedCoordinate().hashCode();
        }

        public boolean equals( Object o ) {
            if (!(o instanceof TestDirectedEdge)) {
                return false;
            }
            TestDirectedEdge edge = (TestDirectedEdge) o;
            if (getNode() != edge.getNode()) {
                return false;
            }
            if (isForward() != edge.isForward()) {
                return false;
            }
            boolean equals = getEdge().equals(edge.getEdge());
            return equals;
        }

        /**
         * Override {@link DirectedEdge#compareTo(Object)} as it seems to be broken, since it
         * compares the orientation of both vectors and often returns <code>0</code> avoiding two
         * different DirecteEdge's to be inserted in the {@link DirectedEdgeStar}'s internal
         * {@link TreeMap} through {@link DirectedEdgeStar#insert(EdgeEnd)}
         */
        @Override
        public int compareTo( Object o ) {
            TestDirectedEdge e = (TestDirectedEdge) o;
            Coordinate c1 = getCoordinate();
            Coordinate d1 = getDirectedCoordinate();
            Coordinate c2 = e.getCoordinate();
            Coordinate d2 = e.getDirectedCoordinate();
            int comp = c1.compareTo(c2);
            if (0 == comp) {
                comp = d1.compareTo(d2);
            }
            return comp;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer("DirectedEdge[");
            sb.append("forward=").append(isForward()).append(" ");
            sb.append(getCoordinate().x).append(",").append(getCoordinate().y);
            sb.append(":");
            sb.append(getDirectedCoordinate().x).append(",").append(getDirectedCoordinate().y);
            sb.append(", edge=").append(getEdge());
            sb.append("]");
            return sb.toString();
        }
    }

    NodeMap      nodeMap;

    Coordinate[] v1;

    DirectedEdge edge1;

    Coordinate[] v2;

    DirectedEdge edge2;

    Coordinate[] v3;

    DirectedEdge edge3;

    Coordinate[] v4;

    DirectedEdge edge4;

    /**
     * Simple test, rectangle splitted by center vertical line
     * 
     * <pre>
     * <code>
     *            v1    v2
     *      0,20------o-------20,20
     *          |     |10,20 |
     *          |     |      |
     *        v1|   v4|      |v2
     *          |     |      |
     *          |     |10,0  |
     *          o_____o______|20,0
     *        0,0  v3    v2 
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
//    public void setUp() throws Exception {
//        super.setUp();
//        nodeMap = new NodeMap(new SplitGraphNodeFactory());
//        // build boundary vectors clockwise
//        v1 = new Coordinate[]{c(0, 0), c(0, 20), c(10, 20)};
//        v2 = new Coordinate[]{c(10, 20), c(20, 20), c(20, 0), c(10, 0)};
//        v3 = new Coordinate[]{c(10, 0), c(0, 0)};
//        v4 = new Coordinate[]{c(10, 0), c(10, 20)};
//
//        edge1 = directedEdge(nodeMap, v1, Location.EXTERIOR, Location.INTERIOR);
//        edge2 = directedEdge(nodeMap, v2, Location.EXTERIOR, Location.INTERIOR);
//        edge3 = directedEdge(nodeMap, v3, Location.EXTERIOR, Location.INTERIOR);
//        edge4 = directedEdge(nodeMap, v4, Location.INTERIOR, Location.INTERIOR);
//    }
//
//    public void tearDown() throws Exception {
//        super.tearDown();
//        nodeMap = null;
//        v1 = null;
//        v2 = null;
//        v3 = null;
//        v4 = null;
//        edge1 = null;
//        edge2 = null;
//        edge3 = null;
//        edge4 = null;
//    }
//
//    private Coordinate c( double x, double y ) {
//        return new Coordinate(x, y);
//    }
//
//    private DirectedEdge directedEdge( NodeMap nodeMap, Coordinate[] coordinates, int leftLoc,
//                                       int rightLoc ) {
//        Label label = new Label(Location.BOUNDARY, leftLoc, rightLoc);
//        SplitEdge edge = new SplitEdge(coordinates, label);
//        Node node = nodeMap.addNode(coordinates[0]);
//
//        boolean isForward = true;
//        DirectedEdge de1 = new TestDirectedEdge(edge, isForward);
//        isForward = false;
//        DirectedEdge de2 = new TestDirectedEdge(edge, isForward);
//
//        de1.setNode(node);
//        de2.setNode(node);
//
//        de1.setSym(de2);
//        de2.setSym(de1);
//
//        return de1;
//    }
//
//    public void testInsert() {
//        SplitEdgeStar star = new SplitEdgeStar();
//        try {
//            star.insert(null);
//            fail("insert should raise NPE on null argument");
//        } catch (NullPointerException e) {
//            assertTrue(true);
//        }
//
//        Coordinate p0 = new Coordinate(0, 0);
//        Coordinate p1 = new Coordinate(10, 10);
//        Label label = new Label(Location.BOUNDARY, Location.EXTERIOR, Location.INTERIOR);
//        SplitEdge edge = new SplitEdge(new Coordinate[]{p0, p1}, label);
//        EdgeEnd nonValidEdgeClass = new EdgeEnd(edge, p0, p1);
//
//        try {
//            star.insert(nonValidEdgeClass);
//            fail("insert should only allow " + DirectedEdge.class.getName());
//        } catch (ClassCastException e) {
//            assertTrue(true);
//        }
//
//        DirectedEdge de = new TestDirectedEdge(edge, true);
//        star.insert(de);
//        assertEquals(1, star.getEdges().size());
//        assertSame(de, star.getEdges().get(0));
//    }
//
//    /**
//     * Verifies that {@link SplitEdgeStar#findClosestEdgeInDirection(DirectedEdge, int)} returns the
//     * correct edge by entering to the node through the given edge and looking towards the specified
//     * direction.
//     * 
//     * <pre>
//     * <code>
//     *            v1  N2  v2
//     *      0,20------o-------20,20
//     *          |     |10,20 |
//     *          |     |      |
//     *        v1|   v4|      |v2
//     *          &circ;     &circ;      |
//     *          |     |10,0  |
//     *          o__/__o__/___|20,0
//     *        0,0  v3 N1  v2 
//     * </code>
//     * </pre>
//     * 
//     * @throws Exception
//     */
//    public void testFindClosestEdgeInDirection() {
//        SplitEdgeStar starN1 = new SplitEdgeStar();
//        starN1.insert(edge2.getSym());
//        starN1.insert(edge3);
//        starN1.insert(edge4);
//        assertEquals(3, starN1.getDegree());
//
//        try {
//            starN1.findClosestEdgeInDirection(null, CGAlgorithms.COUNTERCLOCKWISE);
//            fail("Expected NPE");
//        } catch (NullPointerException e) {
//            assertTrue(true);
//        }
//
//        DirectedEdge edge;
//        edge = starN1.findClosestEdgeInDirection(edge2.getSym(), CGAlgorithms.COUNTERCLOCKWISE);
//        assertSame(edge4, edge);
//        edge = starN1.findClosestEdgeInDirection(edge2.getSym(), CGAlgorithms.CLOCKWISE);
//        assertSame(edge3, edge);
//
//        edge = starN1.findClosestEdgeInDirection(edge3, CGAlgorithms.COUNTERCLOCKWISE);
//        assertSame(edge2.getSym(), edge);
//        edge = starN1.findClosestEdgeInDirection(edge3, CGAlgorithms.CLOCKWISE);
//        assertSame(edge4, edge);
//
//        edge = starN1.findClosestEdgeInDirection(edge4, CGAlgorithms.COUNTERCLOCKWISE);
//        assertSame(edge3, edge);
//        edge = starN1.findClosestEdgeInDirection(edge4, CGAlgorithms.CLOCKWISE);
//        assertSame(edge2.getSym(), edge);
//
//        // try with another node
//        SplitEdgeStar starN2 = new SplitEdgeStar();
//        starN2.insert(edge1.getSym());
//        starN2.insert(edge2);
//        starN2.insert(edge4.getSym());
//
//        edge = starN2.findClosestEdgeInDirection(edge1, CGAlgorithms.COUNTERCLOCKWISE);
//        assertSame(edge4.getSym(), edge);
//        edge = starN2.findClosestEdgeInDirection(edge1, CGAlgorithms.CLOCKWISE);
//        assertSame(edge2, edge);
//    }
//
//    /**
//     * Write down our understanding of the angle computation
//     */
//    public void testAngle() {
//        SplitEdgeStar star = new SplitEdgeStar();
//
//        Coordinate p0;
//        Coordinate p1;
//        Coordinate p2;
//        double angle;
//        double degrees;
//
//        p0 = new Coordinate(0, 10);
//        p1 = new Coordinate(0, 0);
//        p2 = new Coordinate(10, 0);
//
//        angle = star.computeAngleInDirection(p0, p1, p2, CGAlgorithms.CLOCKWISE);
//        degrees = Angle.toDegrees(angle);
//        assertEquals(90.0, degrees);
//
//        angle = star.computeAngleInDirection(p0, p1, p2, CGAlgorithms.COUNTERCLOCKWISE);
//        degrees = Angle.toDegrees(angle);
//        assertEquals(270.0, degrees);
//
//        p0 = new Coordinate(0, 10);
//        p1 = new Coordinate(10, 0);
//        p2 = new Coordinate(0, 0);
//
//        angle = star.computeAngleInDirection(p0, p1, p2, CGAlgorithms.COUNTERCLOCKWISE);
//        degrees = Angle.toDegrees(angle);
//        assertEquals(45.0, degrees);
//
//        angle = star.computeAngleInDirection(p0, p1, p2, CGAlgorithms.CLOCKWISE);
//        degrees = Angle.toDegrees(angle);
//        assertEquals(315.0, degrees);
//
//        p0 = new Coordinate(-10, 10);
//        p1 = new Coordinate(0, 0);
//        p2 = new Coordinate(10, -10);
//
//        angle = star.computeAngleInDirection(p0, p1, p2, CGAlgorithms.COUNTERCLOCKWISE);
//        degrees = Angle.toDegrees(angle);
//        assertEquals(180.0, degrees);
//
//        angle = star.computeAngleInDirection(p0, p1, p2, CGAlgorithms.CLOCKWISE);
//        degrees = Angle.toDegrees(angle);
//        assertEquals(180.0, degrees);
//    }

}
