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

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.Label;
import com.vividsolutions.jts.geomgraph.NodeFactory;
import com.vividsolutions.jts.geomgraph.PlanarGraph;
import com.vividsolutions.jts.geomgraph.Position;
import com.vividsolutions.jts.geomgraph.Quadrant;

/**
 * A {@link PlanarGraph} that builds itself from a {@link Polygon} and a
 * {@link LineString splitting line}.
 * <p>
 * The resulting graph will have the following characteristics:
 * <ul>
 * <li>It will contain as many edges as linestrings in the boundary of the intersection geometry
 * between the polygon and the splitting line string.
 * <li>All edges will be labelled {@link Location#BOUNDARY} at {@link Position#ON}</li>
 * <li>The edges from the polygon's exterior ring will be labelled {@link Location#EXTERIOR} at the
 * {@link Position#LEFT}, {@link Location#INTERIOR} at {@link Position#RIGHT}</li>
 * <li>The edges from the polygon's holes will be labelled {@link Location#INTERIOR} at the
 * {@link Position#LEFT}, {@link Location#EXTERIOR} at {@link Position#RIGHT}</li>
 * </ul>
 * <p>
 * Note the provided polygon may result modified as the result of {@link Polygon#normalize()},
 * which is called in order to ensure propper orientation of the shell and holes.
 * </p>
 * </p>
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
class SplitGraph extends PlanarGraph {

    private static final NodeFactory NODE_FACTORY = new SplitGraphNodeFactory();

    private final Polygon            polygon;

    /**
     * LineString used to split polygon, may be null if
     * polygon is to be untouched.
     */
    private final LineString         splitter;

    public SplitGraph( Polygon polygon, LineString splitter ) {
        super(NODE_FACTORY);
        this.polygon = polygon;
        // after normalize() we know the shell is oriented CW and the holes CCW
        this.polygon.normalize();

        this.splitter = normalizeSplitter(splitter);
        buildGraph();
    }

    /** True if out provided linestring actually does something */
    public boolean isSplit(){
    	return splitter != null;
    }
    
    /**
     * This method will take care of lines dangling into a polygon.
     * 
     * @param original
     * @return linestring clipped to the edges of the polygon, may be null if it does not bisect completely
     */
    private LineString normalizeSplitter( final LineString original ) {
        LineString splitter = original;        
        if( !original.isClosed()){
            // ensure the splitter has no endpoints lying inside the polygon
            splitter = removeInteriorEndPoints(polygon, original);
        }
        // endure the splitter is directed clockwise, as its going
        // to become part of the shell boundary when the tesselation
        // process eliminates other shell edges from the graph
        Coordinate[] splitterCoords = splitter.getCoordinates();
        if( splitterCoords.length == 0 ){
        	return null; // empty linestring? must of drawn a line within a polygon
        }
        Coordinate coord0 = splitterCoords[0];
        Coordinate coord1 = splitterCoords[1];

        // determine the orientation of the coordinate sequence given the
        // quadrant of its first vector
        // 1 | 0
        // --+--
        // 2 | 3
        final int quadrant = Quadrant.quadrant(coord0, coord1);
        boolean isCounterClockWise = 1 == quadrant || 2 == quadrant;
        if (isCounterClockWise) {
            CoordinateArrays.reverse(splitterCoords);
            GeometryFactory gf = original.getFactory();
            splitter = gf.createLineString(splitterCoords);
        }
        return splitter;
    }

    /**
     * Removes the given edge and its related {@link DirectedEdge}'s from this graph
     * 
     * @param edge the edge to remove
     * @throws IllegalArgumentException if no enclosing DirectedEdge is found for <code>edge</code>
     * @see #remove(DirectedEdge)
     */
    public void remove( final SplitEdge edge ) {
        DirectedEdge edgeEnd = (DirectedEdge) findEdgeEnd(edge);
        if (edgeEnd == null) {
            throw new IllegalArgumentException("No enclosing edge end found for " + edge);
        }
        remove(edgeEnd);
    }

    /**
     * Removes a DirectedEdge, its sym and its {@link SplitEdge} from this graph. May lead to the
     * graph containing dangling nodes.
     * 
     * @param edgeEnd
     */
    public void remove( final DirectedEdge edgeEnd ) {
        if (edgeEnd == null) {
            throw new NullPointerException();
        }
        if (edgeEndList.remove(edgeEnd)) {
            DirectedEdge sym = edgeEnd.getSym();
            edgeEndList.remove(sym);

            // shared edge between both ends
            Edge edge = edgeEnd.getEdge();
            edges.remove(edge);

            // node of directed edge end
            SplitGraphNode node = (SplitGraphNode) edgeEnd.getNode();
            // node of symetric directed edge end
            SplitGraphNode endNode = (SplitGraphNode) sym.getNode();
            node.remove(edgeEnd);
            endNode.remove(sym);
        }
    }

    /**
     * Builds a linestrnig from splitter such that it contains no endpoints lying inside the polygon
     * 
     * @param polygon
     * @param splitter
     * @return
     */
    private LineString removeInteriorEndPoints( Polygon polygon, LineString splitter ) {
        final Coordinate[] coords = splitter.getCoordinates();
        final GeometryFactory gf = splitter.getFactory();
        int useFrom;
        int useTo;

        for( useFrom = 0; useFrom < coords.length; useFrom++ ) {
            Point p = gf.createPoint(coords[useFrom]);
            if (!polygon.contains(p)) {
                break;
            }
        }
        for( useTo = coords.length - 1; useTo >= useFrom; useTo-- ) {
            Point p = gf.createPoint(coords[useTo]);
            if (!polygon.contains(p)) {
                break;
            }
        }

        if (useFrom == useTo) {
            throw new IllegalArgumentException("Line lies completely inside polygon");
        }

        int length = 1 + (useTo - useFrom);
        Coordinate[] crossingLineCoords = new Coordinate[length];
        System.arraycopy(coords, useFrom, crossingLineCoords, 0, length);
        LineString surelyCrossingLine = gf.createLineString(crossingLineCoords);
        return surelyCrossingLine;
    }

    /**
     * <pre>
     * <code>
     *                             
     *                  +----------o-----------+
     *                  |          |           |
     *                  |          |           |
     *                  |    +-----------+     |
     *                  |    |     |     |     |
     *                  |    |     |     |     |
     *                  |    |     |     |     |
     *                  |    o__\__o_____|     |
     *                  |       /  |           |
     *                 /|\        /|\          |
     *                  o__________o___________| 
     *                                        
     *                             
     * </code>
     * </pre>
     * 
     * @throws Exception
     */
    private void buildGraph() {
    	if( splitter == null ) return; // no graph!
    	
        Geometry intersectingLineStrings = polygon.intersection(splitter);
        Geometry nodedShell = polygon.getExteriorRing().difference(splitter);

        LineString[] interiorRings = new LineString[polygon.getNumInteriorRing()];
        for( int i = 0; i < polygon.getNumInteriorRing(); i++ ) {
            LineString interiorRingN = polygon.getInteriorRingN(i);
            interiorRings[i] = interiorRingN;
        }
        GeometryFactory factory = polygon.getFactory();
        Geometry interiorRingCollection = factory.createMultiLineString(interiorRings);
        Geometry nodedHoles = interiorRingCollection.difference(splitter);

        // shell segments oriented CW means exterior at the left, interior at
        // the right
        addEdges(nodedShell, Location.BOUNDARY, Location.EXTERIOR, Location.INTERIOR);
        // hole segments oriented CCW means interior at the left, exterior at
        // the right
        addEdges(nodedHoles, Location.BOUNDARY, Location.INTERIOR, Location.EXTERIOR);
        // splitter intersection segments have interior location at both left
        // and right
        addEdges(intersectingLineStrings, Location.BOUNDARY, Location.INTERIOR, Location.INTERIOR);
    }

    private void addEdges( Geometry linearGeom, int onLoc, int leftLoc, int rightLoc ) {
        final int nParts = linearGeom.getNumGeometries();
        Geometry currGeom;
        Coordinate[] coords;
        List edges = new ArrayList();
        for( int i = 0; i < nParts; i++ ) {
            currGeom = linearGeom.getGeometryN(i);
            coords = currGeom.getCoordinates();
            Label label = new Label(onLoc, leftLoc, rightLoc);
            Edge edge = new SplitEdge(coords, label);
            edges.add(edge);
        }
        // for each edge in the list, adds two DirectedEdge, one reflecting
        // the given edge and other the opposite
        super.addEdges(edges);
    }

}
