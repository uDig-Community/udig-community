/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.transformtool;

import net.refractions.udig.project.command.UndoRedoCommand;
import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.behaviour.AddVertexWhileCreatingBehaviour;
import net.refractions.udig.tools.edit.commands.AddVertexCommand;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.Point;

import org.eclipse.core.runtime.NullProgressMonitor;

class VertexBehaviour extends AddVertexWhileCreatingBehaviour {
	@Override
	public UndoableMapCommand getCommand(EditToolHandler handler,
			MapMouseEvent e, EventType eventType) {
		Point valueOf = Point.valueOf(e.x, e.y);
		EditBlackboard editBlackboard = handler.getEditBlackboard(handler
				.getContext().getSelectedLayer());
		Point destination = handler.getEditBlackboard(
				handler.getContext().getSelectedLayer()).overVertex(valueOf,
				PreferenceUtil.instance().getVertexRadius());
		if (destination == null)
			destination = valueOf;

		UndoableComposite composite = new UndoableComposite();
		composite.getCommands().add(
				new AddVertexCommand(handler, editBlackboard, destination));
		composite.getCommands().add(
				handler.getCommand(handler.getAcceptBehaviours()));
		try {
			composite.setMap(handler.getContext().getMap());
			composite.run(new NullProgressMonitor());
		} catch (Exception e1) {
			throw (RuntimeException) new RuntimeException().initCause(e1);
		}
		return new UndoRedoCommand(composite);
	}
}
