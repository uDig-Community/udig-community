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
package es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.behaviour;

import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.support.Point;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.SegmentCopyContext;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.command.SetReferenceSegmentCommand;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsMode;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsUtil;

/**
 * <p>
 * Select the segment that is under the cursor or under the snap area.
 * If shift is down, and is a segment under cursor/snap, add this segment to the segment list.
 * </p>
 * <p>
 * Requirements:
 * <ul>
 * <li>state==MODIFYING or NONE</li>
 * <li>event type == RELEASED</li>
 * <li>button1 must be the button that was released</li>
 * <li>Mouse over a geometry</li>
 * </ul>
 * Optional:
 * <ul>
 * <li>Shift is down</li>
*  </ul>
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class SetReferenceSegmentEventBehaviour implements EventBehaviour {

	private SegmentCopyContext	segmentCopyContext	= null;

	public SetReferenceSegmentEventBehaviour(SegmentCopyContext context) {

		this.segmentCopyContext = context;
	}

	public UndoableMapCommand getCommand(EditToolHandler handler, MapMouseEvent e, EventType eventType) {

		if (!isValid(handler, e, eventType)) {
			throw new IllegalArgumentException("Behaviour is not valid for the current state"); //$NON-NLS-1$
		}

		SetReferenceSegmentCommand setReferenceSegmentCommand = new SetReferenceSegmentCommand(handler, Point.valueOf(
					e.x, e.y), segmentCopyContext,e);

		return setReferenceSegmentCommand;
	}

	public void handleError(EditToolHandler handler, Throwable error, UndoableMapCommand command) {

		EditPlugin.log("", error); //$NON-NLS-1$
	}

	public boolean isValid(EditToolHandler handler, MapMouseEvent e, EventType eventType) {
		
		boolean legalState = handler.getCurrentState() == EditState.NONE
					|| handler.getCurrentState() == EditState.MODIFYING;
		boolean releaseButtonState = eventType == EventType.RELEASED;
		boolean legalButton = e.button == MapMouseEvent.BUTTON1;

		if (!(legalState && releaseButtonState && legalButton)) {
			return false;
		}
		//if it was doing DnD, change Mode because DnD has end and return.  
		//TODO change mode only 1 time on all the events that run on EventType.RELEASED
		//now it change on SetInitialPointEventBehaviour.
		if(segmentCopyContext.getMode()==PrecisionToolsMode.POST_DRAG || segmentCopyContext.getMode()==PrecisionToolsMode.DRAG ){
			return false;
		}

//		if ((PrecisionParallelUtil.isFeatureUnderCursor(handler, e))
//					&& parallelContext.getReferenceLineSegment() == null) {
//			
//			return true;
//		}

		if ((PrecisionToolsUtil.isFeatureUnderCursor(handler, e))
					&& segmentCopyContext.isLineSegmentEmpty()) {
			return true;
		}
		//TODO when SHIFT is down, and is isFeatureUnderCursor->add this segment
		if((PrecisionToolsUtil.isFeatureUnderCursor(handler, e))
					&& e.isShiftDown()){
			return true;
		}
		
		
		return false;

	}

}
