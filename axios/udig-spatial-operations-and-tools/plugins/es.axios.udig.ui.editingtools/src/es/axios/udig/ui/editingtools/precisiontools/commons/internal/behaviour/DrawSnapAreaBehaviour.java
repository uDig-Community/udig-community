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
package es.axios.udig.ui.editingtools.precisiontools.commons.internal.behaviour;

import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.behaviour.DrawCreateVertexSnapAreaBehaviour;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.SnapBehaviour;

/**
 * Always draw the snap area. Collaboration
 * {@link DrawCreateVertexSnapAreaBehaviour}
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class DrawSnapAreaBehaviour extends DrawCreateVertexSnapAreaBehaviour implements EventBehaviour {

	@Override
	public boolean isValid(EditToolHandler handler, MapMouseEvent e, EventType eventType) {
		// if snap is off, set it on.
		if (PreferenceUtil.instance().getSnapBehaviour() == SnapBehaviour.OFF) {
			PreferenceUtil.instance().setSnapBehaviour(SnapBehaviour.ALL_LAYERS);
		}

		boolean snapOn = PreferenceUtil.instance().getSnapBehaviour() != SnapBehaviour.OFF
					&& PreferenceUtil.instance().getSnapBehaviour() != SnapBehaviour.GRID;
		boolean mouseMoving = (eventType == EventType.MOVED || eventType == EventType.DRAGGED || eventType != EventType.EXITED);
		boolean shouldTurnOff = eventType != EventType.HOVERED;
		return shouldTurnOff || (snapOn && mouseMoving);
	}
}
