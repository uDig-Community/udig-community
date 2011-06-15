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
package es.axios.udig.ui.editingtools.internal.behaviour;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public class OrthoEditPointProvider implements IEditPointProvider {
    private Coordinate tail = new Coordinate();
    private Coordinate toX  = new Coordinate();
    private Coordinate toY  = new Coordinate();

    public Coordinate getCoordinate( MapMouseEvent e, EditToolHandler handler ) {
        Point orthoPoint = getPoint(e, handler);
        ILayer editLayer = handler.getEditLayer();
        EditBlackboard editBlackboard = handler.getEditBlackboard(editLayer);
        Coordinate toCoord = editBlackboard.toCoord(orthoPoint);
        return toCoord;
    }

    public Point getPoint( MapMouseEvent e, EditToolHandler handler ) {
        PrimitiveShape currentShape = handler.getCurrentShape();
        Point orthoPoint = Point.valueOf(e.x, e.y);
        if (currentShape != null) {
            orthoPoint = getOrthoLocation(orthoPoint, currentShape);
        }
        return orthoPoint;
    }

    /**
     * Returns the location corresponding to the orthogonal segment between the last edit shape
     * point and the current mouse point given by the mouse event.
     * 
     * @param eventLocation
     * @param currentShape
     * @return
     */
    public Point getOrthoLocation( Point eventLocation, PrimitiveShape currentShape ) {
        if (currentShape.getNumPoints() == 0) {
            return eventLocation;
        }
        Point lastPoint = currentShape.getPoint(currentShape.getNumCoords() - 1);

        tail.x = lastPoint.getX();
        tail.y = lastPoint.getY();

        toX.x = tail.x;
        toX.y = eventLocation.getY();

        toY.x = eventLocation.getX();
        toY.y = tail.y;

        Point location;
        if (tail.distance(toX) < tail.distance(toY)) {
            location = Point.valueOf(eventLocation.getX(), lastPoint.getY());
        } else {
            location = Point.valueOf(lastPoint.getX(), eventLocation.getY());
        }
        return location;
    }
}
