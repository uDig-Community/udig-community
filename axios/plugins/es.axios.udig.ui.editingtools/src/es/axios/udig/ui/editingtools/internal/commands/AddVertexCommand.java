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
package es.axios.udig.ui.editingtools.internal.commands;

import net.refractions.udig.core.IBlockingProvider;
import net.refractions.udig.project.command.AbstractCommand;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.AnimationUpdater;
import net.refractions.udig.project.ui.IAnimation;
import net.refractions.udig.tool.edit.internal.Messages;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.animation.AddVertexAnimation;
import net.refractions.udig.tools.edit.animation.DeleteVertexAnimation;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.EditUtils;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

import org.eclipse.core.runtime.IProgressMonitor;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Command for adding a vertext to a {@link net.refractions.udig.tools.edit.support.EditGeom}
 * <p>
 * As a difference with {@link net.refractions.udig.tools.edit.commands.AddVertexCommand}, this one
 * does not implements any extra logic than just adding the provided point to the current edit shape
 * </p>
 * <p>
 * Note: This is a plain copy of {@link net.refractions.udig.tools.edit.commands.AddVertexCommand}
 * without the performSnapCalculation method, and is meant to replace that class when we integrate
 * with uDig's too.edit plugin.
 * </p>
 * 
 * @author jones
 * @since 1.1.0
 */
public class AddVertexCommand extends AbstractCommand implements UndoableMapCommand {

    private final Coordinate                  toAdd;
    private Point                             point;
    private Coordinate                        addedCoord;
    private final EditBlackboard              board;
    private IBlockingProvider<PrimitiveShape> shapeProvider;
    private EditToolHandler                   handler;
    private int                               index;
    private boolean                           showAnimation = true;

    public AddVertexCommand( EditToolHandler handler, EditBlackboard editBlackboard,
                             Coordinate coordToAdd ) {
        this(handler, editBlackboard, new EditUtils.EditToolHandlerShapeProvider(handler),
             coordToAdd, true);
    }

    /**
     * New instance is created. The animation is not shown.
     * 
     * @param useSnapping TODO
     */
    public AddVertexCommand( EditToolHandler handler, EditBlackboard bb,
                             IBlockingProvider<PrimitiveShape> provider, Coordinate coordToAdd,
                             boolean useSnapping ) {
        this.handler = handler;
        board = bb;
        shapeProvider = provider;
        toAdd = coordToAdd;
        point = board.toPoint(toAdd); 
    }

    public void rollback( IProgressMonitor monitor ) throws Exception {
        if (addedCoord == null)
            return;

        if (handler.getContext().getMapDisplay() != null && showAnimation) {
            IAnimation animation = new DeleteVertexAnimation(point);
            AnimationUpdater.runTimer(handler.getContext().getMapDisplay(), animation);
        }
        board.removeCoordinate(index, addedCoord, shapeProvider.get(monitor));
    }

    public void run( IProgressMonitor monitor ) throws Exception {
        PrimitiveShape shape = shapeProvider.get(monitor);
        boolean collapseVertices = board.isCollapseVertices();
        try {
            board.setCollapseVertices(false);
            board.addCoordinate(toAdd, shape);
            addedCoord = toAdd;
            index = shape.getNumPoints() - 1;
        } finally {
            board.setCollapseVertices(collapseVertices);
        }
        if (handler.getContext().getMapDisplay() != null && showAnimation) {
            IAnimation animation = new AddVertexAnimation(point.getX(), point.getY());
            AnimationUpdater.runTimer(handler.getContext().getMapDisplay(), animation);
        }
    }

    public String getName() {
        return Messages.AddVertexCommand_name + toAdd;
    }
}
