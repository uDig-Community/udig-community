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

import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.Point;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public class DefaultEditPointProvider implements IEditPointProvider {

    public Coordinate getCoordinate( MapMouseEvent e, EditToolHandler handler ) {
        EditBlackboard board = handler.getEditBlackboard(handler.getEditLayer());
        Point point = getPoint(e, handler);
        Coordinate coord = board.toCoord(point);
        return coord;
    }

    public Point getPoint( MapMouseEvent e, EditToolHandler handler ) {
        Point point = Point.valueOf(e.x, e.y);
        return point;
    }

}
