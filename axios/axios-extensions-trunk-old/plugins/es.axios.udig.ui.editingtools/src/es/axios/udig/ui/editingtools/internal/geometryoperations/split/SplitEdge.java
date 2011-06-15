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
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.Label;
import com.vividsolutions.jts.geomgraph.Position;

/**
 * A custom edge class that knows if its an edge from the shell of the polygin being splitted, one
 * of its holes or the splitting line, by inspecing its label.
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
final class SplitEdge extends Edge {
    SplitEdge( Coordinate[] pts, Label label ) {
        super(pts, label);
    }

    private int[] getLabelLocations() {
        int[] locations = new int[3];
        Label label = getLabel();
        locations[Position.ON] = label.getLocation(0, Position.ON);
        locations[Position.LEFT] = label.getLocation(0, Position.LEFT);
        locations[Position.RIGHT] = label.getLocation(0, Position.RIGHT);
        return locations;
    }

    public boolean isShellEdge() {
        int[] loc = getLabelLocations();
        return (loc[Position.LEFT] == Location.EXTERIOR && loc[Position.RIGHT] == Location.INTERIOR);
    }

    public boolean isHoleEdge() {
        int[] loc = getLabelLocations();
        return (loc[Position.LEFT] == Location.INTERIOR && loc[Position.RIGHT] == Location.EXTERIOR);
    }

    public boolean isInteriorEdge() {
        int[] loc = getLabelLocations();
        return (loc[Position.LEFT] == Location.INTERIOR && loc[Position.RIGHT] == Location.INTERIOR);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Edge[label=");
        sb.append(getLabel()).append(", ");
        Coordinate[] coords = getCoordinates();
        for( int i = 0; i < coords.length; i++ ) {
            sb.append(coords[i].x).append(",").append(coords[i].y).append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}