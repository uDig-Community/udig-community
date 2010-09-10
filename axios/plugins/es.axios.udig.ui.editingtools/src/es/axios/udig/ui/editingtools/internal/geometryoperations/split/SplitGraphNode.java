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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.EdgeEnd;
import com.vividsolutions.jts.geomgraph.Node;

/**
 * Custom node type. Does nothing special by now but to force the use of {@link SplitEdgeStar}
 * instances as the node's list of incident edges and allow to remove edges from its list of
 * incident edges.
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
class SplitGraphNode extends Node {
    public SplitGraphNode( Coordinate coord, SplitEdgeStar incidentEdges ) {
        super(coord, incidentEdges);
    }

    public void add( DirectedEdge edge ) {
        super.add(edge);
    }

    @Override
    public void add( EdgeEnd edge ) {
        add((DirectedEdge) edge);
    }

    /**
     * Removes the given <code>edge</code> from this node's {@link #getEdges() edge list}.
     * 
     * @param edge
     */
    public void remove( DirectedEdge edge ) {
        SplitEdgeStar edges = (SplitEdgeStar) getEdges();
        edges.remove(edge);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Node[");
        sb.append(coord.x).append(":").append(coord.y);
        sb.append(", ").append(label);
        sb.append(", ").append(getEdges());
        sb.append("]");
        return sb.toString();
    }
}