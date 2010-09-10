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
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.DirectedEdgeStar;
import com.vividsolutions.jts.geomgraph.EdgeEnd;
import com.vividsolutions.jts.util.Assert;

/**
 * A {@link DirectedEdgeStar} for the {@link SplitGraphNode nodes} in a {@link SplitGraph}
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
class SplitEdgeStar extends DirectedEdgeStar {

    /**
     * Adds a DirectedEdge to the list of incident edges on this star
     * 
     * @param de non null directed edge to insert on this node star
     */
    public void insert( DirectedEdge de ) {
        if (de == null) {
            throw new NullPointerException();
        }
        insertEdgeEnd(de, de);
    }

    /**
     * Overrides {@link DirectedEdgeStar#insert(EdgeEnd)} just to delegate to
     * {@link #insert(DirectedEdge)} forcing the argument type
     */
    @Override
    public void insert( EdgeEnd ee ) {
        insert((DirectedEdge) ee);
    }

    /**
     * Removes the given edge from this edge star
     * 
     * @param edge
     * @throws IllegalArgumentException if <code>edge</code> is not one of this star's edges
     */
    public void remove( DirectedEdge edge ) {
        if (edge == null) {
            throw new NullPointerException("edge");
        }
        int degree = getDegree();
        Object removed = edgeMap.remove(edge);
        int afterDegree = getDegree();
        Assert.isTrue(afterDegree == degree - 1);
        if (edge != removed) {
            throw new IllegalArgumentException(
                                               "Tried to remove an edge not registered in this edge star: "
                                                       + edge);
        }
        edgeList = null; // edge list has changed - clear the cache
    }

    /**
     * Returns the list of Directed edges whose direction is outgoing from this star's node. That
     * is, for all the DirectedEdges in the star, if the edge's start point is coincident whith the
     * edge star node, returns the same DirectedNode, otherwise returns the edge's
     * {@link DirectedEdge#getSym() symmetric edge}.
     * 
     * @return
     */
    private List getOutgoingEdges() {
        final Coordinate nodeCoord = getCoordinate();
        final List edges = getEdges();
        final List outgoingEdges = new ArrayList(edges.size());
        for( Iterator it = edges.iterator(); it.hasNext(); ) {
            DirectedEdge edge = (DirectedEdge) it.next();
            if (!nodeCoord.equals2D(edge.getCoordinate())) {
                edge = edge.getSym();
            }
            assert nodeCoord.equals2D(edge.getCoordinate());
            outgoingEdges.add(edge);
        }
        return outgoingEdges;
    }

    /**
     * Finds the first edge to the passed in in the <code>searchDrirection</code> direction.
     * 
     * @param searchDirection one of {@link CGAlgorithms#CLOCKWISE},
     *        {@link CGAlgorithms#COUNTERCLOCKWISE}
     * @return the edge forming the acutest angle with <code>edge</code> in the
     *         <code>prefferredDirection</code> or <code>null</code> if there are no edges in
     *         the prefferred direction.
     */
    public DirectedEdge findClosestEdgeInDirection( DirectedEdge edge, final int searchDirection ) {
        if (edge == null) {
            throw new NullPointerException("edge");
        }
        if (CGAlgorithms.CLOCKWISE != searchDirection
                && CGAlgorithms.COUNTERCLOCKWISE != searchDirection) {
            throw new IllegalArgumentException("Allowed values for for searchDirection "
                    + "are CGAlgorithms.CLOCKWISE and CGAlgorithms.COUNTERCLOCKWISE: "
                    + searchDirection);
        }

        // ensure we're using the node's outgoing edge
        if (super.findIndex(edge) == -1) {
            edge = edge.getSym();
            if (super.findIndex(edge) == -1) {
                throw new IllegalArgumentException("Edge does not belongs to this edgestar");
            }
        }
        final int degree = getDegree();
        if (degree < 2) {
            throw new IllegalStateException("there must be at least two edges in the edge star");
        }
        final Coordinate nodeCoord = getCoordinate();

        assert nodeCoord.equals2D(edge.getCoordinate());

        double acutestAngle = Double.MAX_VALUE;
        DirectedEdge acutest = null;
        DirectedEdge adjacentEdge = null;

        final Coordinate tip1 = edge.getDirectedCoordinate();
        final Coordinate tail = nodeCoord;

        // ensure we're using outgoing edges
        final List outgoingEdges = getOutgoingEdges();
        for( Iterator it = outgoingEdges.iterator(); it.hasNext(); ) {
            adjacentEdge = (DirectedEdge) it.next();

            if (adjacentEdge == edge) {
                continue;
            }

            Coordinate tip2 = adjacentEdge.getDirectedCoordinate();

            double angle = computeAngleInDirection(tip1, tail, tip2, searchDirection);

            if (angle < acutestAngle) {
                acutestAngle = angle;
                acutest = adjacentEdge;
            }
        }

        return acutest;
    }

    /**
     * Computes the angle comprised between the vector <code>tail:tip1</code> looking in the
     * specified <code>direction</code> to the vector <code>tail:tip2</code>
     * 
     * @param tip1
     * @param tail
     * @param tip2
     * @param direction one of {@link CGAlgorithms#CLOCKWISE},
     *        {@link CGAlgorithms#COUNTERCLOCKWISE}
     * @return the angle in radians defined by the vectors tail-tip1:tail-tip2 calculated in the
     *         specified <code>direction</code> from tail-tip1
     */
    public double computeAngleInDirection( Coordinate tip1, Coordinate tail, Coordinate tip2,
                                           int direction ) {
        final int orientation = CGAlgorithms.computeOrientation(tail, tip1, tip2);

        // minimal angle (non oriented)
        double angle = Angle.angleBetween(tip1, tail, tip2);
        if (orientation != direction) {
            angle = Angle.PI_TIMES_2 - angle;
        }
        return angle;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SplitEdgeStar[degree: ");
        sb.append(getDegree()).append(", edges: ");
        for( Iterator it = getEdges().iterator(); it.hasNext(); ) {
            DirectedEdge de = (DirectedEdge) it.next();
            sb.append("DirectedEdge[");
            sb.append(de.getEdge()).append(" ");
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }

}