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
package es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.command;

import net.refractions.udig.project.command.AbstractCommand;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.EditUtils;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.SnapBehaviour;

import org.eclipse.core.runtime.IProgressMonitor;

import com.vividsolutions.jts.geom.Coordinate;

import es.axios.udig.ui.editingtools.internal.i18n.Messages;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.SegmentCopyContext;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsMode;

/**
 * Sets the initial point into the {@link SegmentCopyContext}.
 * 
 * <pre>
 * If there is a vertex under the snap area, this vertex will be the initial coordinate,
 * otherwise the initial point will be the position where the click was done.
 * </pre>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public class SetInitialPointCommand extends AbstractCommand implements UndoableMapCommand {

	private SegmentCopyContext	segmentCopyContext	= null;
	private Coordinate			coordinate			= null;
	private EditToolHandler		handler				= null;

	public SetInitialPointCommand(SegmentCopyContext parallelContext, Coordinate coor, EditToolHandler handler) {

		this.segmentCopyContext = parallelContext;
		this.coordinate = coor;
		this.handler = handler;
	}

	public String getName() {

		return Messages.SetInitialPointCommand;
	}

	/**
	 * If there is a vertex under the snap area, this vertex will be the initial
	 * coordinate, otherwise the initial point will be the position where the
	 * click was done.
	 */
	public void run(IProgressMonitor monitor) throws Exception {

		EditBlackboard board = handler.getEditBlackboard(handler.getEditLayer());
		SnapBehaviour snapBehaviour = PreferenceUtil.instance().getSnapBehaviour();

		EditUtils editUtils = EditUtils.instance;
		EditState currentState = handler.getCurrentState();

		Point point = board.toPoint(coordinate);
		Coordinate snapCoord;
		snapCoord = editUtils.getClosestSnapPoint(handler, board, point, false, snapBehaviour, currentState);
		if (snapCoord != null) {
			this.coordinate = snapCoord;
		}

		// set the state before setting the initial point because, when point is
		// set it will automatically update.
		this.segmentCopyContext.setMode(PrecisionToolsMode.READY);
		// this.parallelContext.previousMode = this.parallelContext.mode;
		// this.parallelContext.mode = Mode.READY;
		this.segmentCopyContext.setInitialCoordinate(this.coordinate);
	}

	public void rollback(IProgressMonitor monitor) throws Exception {

	}

}
