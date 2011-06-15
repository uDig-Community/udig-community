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

import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseMotionListener;
import net.refractions.udig.project.ui.render.displayAdapter.ViewportPane;
import net.refractions.udig.tools.edit.Activator;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.activator.DrawGeomsActivator;
import net.refractions.udig.tools.edit.commands.DrawEditGeomsCommand;
import net.refractions.udig.tools.edit.commands.StyleStrategy;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

/**
 * {@link Activator} that draws the currently being edited shape with the end line (the one from the
 * last edit shape point to the mouse location) specified by an {@link IEditPointProvider} instance
 * acting as a strategy object.
 * <p>
 * TODO: this is meant to replace {@link DrawGeomsActivator}
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 * @see DrawGeomsActivator
 */
public class DrawEditGeomWithCustomEndlineActivator implements Activator, MapMouseMotionListener {

    /**
     * handler in use set on activate
     */
    private EditToolHandler                        handler;

    /**
     * Draw command for the edit shape
     */
    private DrawEditGeomsCommandWithCustomEndpoint command;

    private IEditPointProvider                     endpointStrategy;

    /**
     * Creates an activator that will use the provided strategy to get the target screen location
     * upon a mouse movement event.
     * 
     * @param endpointStrategy
     */
    public DrawEditGeomWithCustomEndlineActivator( final IEditPointProvider endpointStrategy ) {
        assert endpointStrategy != null;
        this.endpointStrategy = endpointStrategy;
    }

    /**
     * Sets up itself as a mouse motion listener on the {@link ViewportPane} to draw the edit shape
     * 
     * @see Activator#activate(EditToolHandler)
     */
    public void activate( final EditToolHandler handler ) {
        this.handler = handler;
        command = new DrawEditGeomsCommandWithCustomEndpoint(handler, endpointStrategy);

        StyleStrategy colorizationStrategy = command.getColorizationStrategy();
        colorizationStrategy.setFill(PreferenceUtil.instance().getDrawGeomsFill());
        // colorizationStrategy.setModifyShapeColor(PreferenceUtil.instance().getDrawGeomsLine());
        colorizationStrategy.setLine(PreferenceUtil.instance().getDrawGeomsLine());

        ViewportPane viewportPane = handler.getContext().getViewportPane();

        viewportPane.addDrawCommand(command);
        viewportPane.addMouseMotionListener(this);

    }

    /**
     * Invalidates the draw command and deregisters itself as a mouse listener
     * 
     * @see Activator#deactivate(EditToolHandler)
     */
    public void deactivate( EditToolHandler handler ) {
        command.setValid(false);
        ViewportPane viewportPane = handler.getContext().getViewportPane();
        viewportPane.removeMouseMotionListener(this);
    }

    /**
     * @see Activator#handleActivateError(EditToolHandler, Throwable)
     */
    public void handleActivateError( EditToolHandler handler, Throwable error ) {
        deactivate(handler);
    }

    /**
     * @see Activator#handleDeactivateError(EditToolHandler, Throwable)
     */
    public void handleDeactivateError( EditToolHandler handler, Throwable error ) {
        // TODO
    }

    /**
     * @see MapMouseMotionListener#mouseMoved(MapMouseEvent)
     */
    public void mouseMoved( MapMouseEvent event ) {
        boolean legalState = handler.getCurrentState() == EditState.CREATING;
        if (!legalState) {
            return;
        }
        boolean change = command.setCurrentLocation(event, handler);

        if (change) {
            handler.repaint();
        }
    }
    /**
     * Empty method
     * 
     * @see MapMouseMotionListener#mouseDragged(MapMouseEvent)
     */
    public void mouseDragged( MapMouseEvent event ) {
    }
    /**
     * Empty method
     * 
     * @see MapMouseMotionListener#mouseHovered(MapMouseEvent)
     */
    public void mouseHovered( MapMouseEvent event ) {
    }

    /**
     * Draw command that uses an {@link IEditPointProvider} to obtain the location of the coordinate
     * relative to the mouse location.
     * <p>
     * TODO: this command extends and is meant to replace {@link DrawEditGeomsCommand} by using a
     * strategy object to get the dynamic point location.
     * </p>
     * 
     * @author Aritz Davila (www.axios.es)
     * @author Mauricio Pazos (www.axios.es)
     * @since 0.2.0
     */
    private static class DrawEditGeomsCommandWithCustomEndpoint extends DrawEditGeomsCommand {
        /**
         * Strategy object to obtain the mouse location possibly with a custom constraint (like
         * parallel, ortho, etc)
         */
        private IEditPointProvider endpointStrategy;

        /**
         * @param handler the edit tool handler holding the edit state
         * @param endpointStrategy strategy object to obtain the current shape endpoint for feedbak
         */
        public DrawEditGeomsCommandWithCustomEndpoint( final EditToolHandler handler,
                                                       final IEditPointProvider endpointStrategy ) {
            super(handler);
            this.endpointStrategy = endpointStrategy;
        }

        /**
         * @param event the mouse event that originates the feedback action, from where to obtain
         *        the mouse location to ask the {@link IEditPointProvider} for the constrained
         *        point.
         * @param handler needed as the field in the super class is private. Could be removed if we
         *        merge.
         * @return {@link DrawEditGeomsCommand#setCurrentLocation(Point, PrimitiveShape)} where the
         *         point passed in is the one obtained from the {@link IEditPointProvider}.
         */
        public boolean setCurrentLocation( final MapMouseEvent event, final EditToolHandler handler ) {
            Point location = Point.valueOf(event.x, event.y);
            PrimitiveShape currentShape = handler.getCurrentShape();
            if (currentShape != null) {
                location = endpointStrategy.getPoint(event, handler);
            }
            return super.setCurrentLocation(location, currentShape);
        }
    }
}