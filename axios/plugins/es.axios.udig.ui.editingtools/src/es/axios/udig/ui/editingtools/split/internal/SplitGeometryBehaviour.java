/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
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
package es.axios.udig.ui.editingtools.split.internal;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.UndoableCommand;
import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.tools.edit.Behaviour;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.commands.SetEditStateCommand;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.PrimitiveShape;
import es.axios.udig.ui.commons.util.DialogUtil;
import es.axios.udig.ui.editingtools.internal.commands.EditingToolsCommandFactory;
import es.axios.udig.ui.editingtools.internal.i18n.Messages;

/**
 * Edit tool {@link Behaviour} that takes the current shape from the {@link EditToolHandler} to use
 * it as a splitting line and splits the crossing Features from the current {@link ILayer layer}.
 * <p>
 * This behaviour will either:
 * <ul>
 * <li>Split one or more Simple Features using a line
 * <li>Split one polygon feature by creating a hole
 * </ul> 
 * @see SplitFeaturesCommand
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public class SplitGeometryBehaviour implements Behaviour {

    private EditingToolsCommandFactory commandFactory;

    public SplitGeometryBehaviour( EditingToolsCommandFactory commandFactory ) {
        this.commandFactory = commandFactory;
    }

    /**
     * Returns <code>true</code> if there's a linestring in the {@link EditBlackboard}'s
     * {@link EditToolHandler} to use as splitting line
     */
    public boolean isValid( EditToolHandler handler ) {
        PrimitiveShape currentShape = handler.getCurrentShape();
        if (currentShape == null) {
            return false;
        }
        int nCoords = currentShape.getNumCoords();
        return nCoords > 1;
    }

    /**
     * Returns an {@link UndoableMapCommand} that's responsible of using the
     * {@link EditToolHandler handler}'s current shape (as a LineString) to split the features of
     * the current layer that intersects the trimming line.
     * <p>
     * When a feature's geometry is splitted, the original SimpleFeature will be deleted and as many new
     * Features as geometries result from the split will be created, with the same attributes than
     * the original one except the geometry, which will be a splitted part for each one.
     * </p>
     * 
     * @return the command that splits the geometries under the handler's current shape
     * @see SplitFeaturesCommand
     */
    public UndoableMapCommand getCommand( EditToolHandler handler ) {
        assert handler != null;

        UndoableMapCommand splitCommand;
        
        UndoableComposite commands = new UndoableComposite();
        commands.add( new SetEditStateCommand(handler, EditState.BUSY ));
        commands.add( commandFactory.createSplitFeaturesCommand(handler) );
        commands.add( new SetEditStateCommand(handler, EditState.NONE ));
        
        return commands;
    }

    /**
     * TODO: rollback as per
     * {@link Behaviour#handleError(EditToolHandler, Throwable, UndoableMapCommand)}'s contract
     * states (strange I don't see any Behaviour in udig that actually does it? investigate)
     */
    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
        // TODO: log through this plugin
        assert error != null;

        String message = error.getMessage();
        DialogUtil.openError(Messages.SplitGeometryBehaviour_transaction_failed, message);
        handler.setCurrentState( EditState.NONE ); // start again
        handler.setCurrentShape( null ); // stop drawing this line
        handler.getEditBlackboard( handler.getEditLayer() ).clear();
    }

}
