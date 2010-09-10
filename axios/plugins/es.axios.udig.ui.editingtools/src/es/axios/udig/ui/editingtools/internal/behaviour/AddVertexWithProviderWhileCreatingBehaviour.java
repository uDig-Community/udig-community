/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.ui.editingtools.internal.behaviour;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.command.UndoRedoCommand;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.behaviour.AddVertexWhileCreatingBehaviour;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.Point;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.vividsolutions.jts.geom.Coordinate;

import es.axios.udig.ui.editingtools.internal.commands.AddVertexCommand;

/**
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class AddVertexWithProviderWhileCreatingBehaviour extends AddVertexWhileCreatingBehaviour {

    @Override
    public boolean isValid( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        boolean legalState = handler.getCurrentState() == EditState.CREATING;
        boolean legalEventType = eventType == EventType.RELEASED || eventType==EventType.DOUBLE_CLICK;
        boolean shapeAndGeomNotNull = handler.getCurrentShape() != null;
        boolean button1Released = e.button == MapMouseEvent.BUTTON1;

        boolean valid = legalState && legalEventType && shapeAndGeomNotNull && button1Released
                && !e.buttonsDown() && !e.modifiersDown();
        if (valid) {
            IEditPointProvider provider = getEditPointProvider(handler);
            Coordinate coord = provider.getCoordinate(e, handler);
            EditBlackboard editBlackboard = handler.getEditBlackboard(handler.getEditLayer());
            Point point = editBlackboard.toPoint(coord);
            valid = valid & isNotDuplicated(handler, point);
        }
        return valid;
    }

    private IEditPointProvider getEditPointProvider( EditToolHandler handler ) {
        IBlackboard blackboard = handler.getContext().getMap().getBlackboard();
        IEditPointProvider provider;
        provider = (IEditPointProvider) blackboard.get(IEditPointProvider.BLACKBOARD_KEY);

        // TODO: this won't be needed once AbstractEditTool sets a default provider on the bboard
        if (provider == null) {
            return new DefaultEditPointProvider();
        }
        return provider;
    }

    private boolean overShapeVertex( EditToolHandler handler, Point point ) {
        Point vertexOver = overVertex(handler, point);
        return handler.getCurrentShape().hasVertex(vertexOver);
    }

    private Point overVertex( EditToolHandler handler, Point point ) {
        EditBlackboard editBlackboard = handler.getEditBlackboard(handler.getEditLayer());
        int vertexRadius = PreferenceUtil.instance().getVertexRadius();
        Point vertexOver = editBlackboard.overVertex(point, vertexRadius);
        return vertexOver;
    }

    @Override
    public UndoableMapCommand getCommand( EditToolHandler handler, MapMouseEvent e,
                                          EventType eventType ) {

        // TODO: restore
        // String reasonForFaliure = validator.isValid(handler, e, eventType);
        // if (reasonForFaliure != null) {
        // AnimationUpdater.runTimer(handler.getContext().getMapDisplay(),
        // new MessageBubble(e.x, e.y,
        // Messages.AddVertexWhileCreatingBehaviour_illegal
        // + "\n" + reasonForFaliure, PreferenceUtil.instance().getMessageDisplayDelay()));
        // //$NON-NLS-1$
        // return null;
        // }

        IEditPointProvider provider = getEditPointProvider(handler);
        Coordinate toAdd = provider.getCoordinate(e, handler);
        EditBlackboard editBlackboard = handler.getEditBlackboard(handler.getEditLayer());
        Point pointToAdd = editBlackboard.toPoint(toAdd);
        Point destination = overVertex(handler, pointToAdd);
        if (destination == null)
            destination = pointToAdd;

        AddVertexCommand addVertexCommand = new AddVertexCommand(handler, editBlackboard,
                                                                 toAdd);
        try {
            addVertexCommand.setMap(handler.getContext().getMap());
            addVertexCommand.run(new NullProgressMonitor());
        } catch (Exception e1) {
        	// consider bubble feedback
        	throw new IllegalStateException( e1.getLocalizedMessage(), e1 );
        }
        return new UndoRedoCommand(addVertexCommand);
    }
}
