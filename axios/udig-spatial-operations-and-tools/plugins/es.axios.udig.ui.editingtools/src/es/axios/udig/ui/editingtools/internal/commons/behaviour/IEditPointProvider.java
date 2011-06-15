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
package es.axios.udig.ui.editingtools.internal.commons.behaviour;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.support.Point;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Interface for strategy objects that return an edit point meant to be added to the current edit
 * shape.
 * <p>
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public interface IEditPointProvider {

    /**
     * Key to be used to store the IEditPointProvider to use on the map's {@link IBlackboard}
     */
    String BLACKBOARD_KEY = IEditPointProvider.class.getName();

    /**
     * Returns a Coordinate to be added to the current edit shape, given the map mouse event that
     * originated the query for a behaviours command and the current edit tool handler.
     * 
     * @param e
     * @param handler
     * @return the coordinate to be added in the edit layer's CRS
     */
    public Coordinate getCoordinate( MapMouseEvent e, EditToolHandler handler );

    /**
     * Returns the Point location in screen coordinates relative to the coordinate to be added to
     * the current edit shape, given the map mouse event that originated the query for a behaviours
     * command and the current edit tool handler.
     * 
     * @param e
     * @param handler
     * @return the Point location to add in screen coordinates
     */
    public Point getPoint( MapMouseEvent e, EditToolHandler handler );
}
