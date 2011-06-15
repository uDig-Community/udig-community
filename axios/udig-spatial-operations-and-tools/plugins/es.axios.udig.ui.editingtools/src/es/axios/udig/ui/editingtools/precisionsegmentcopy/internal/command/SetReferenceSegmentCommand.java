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

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.AbstractCommand;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.SnapBehaviour;

import org.eclipse.core.runtime.IProgressMonitor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineSegment;

import es.axios.geotools.util.GeoToolsUtils;
import es.axios.udig.ui.editingtools.internal.commons.behaviour.SnapSegmentFinder;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.SegmentCopyContext;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsMode;

/**
 * Find the segment under the snap area and store it as reference segment.
 * 
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class SetReferenceSegmentCommand extends AbstractCommand implements UndoableMapCommand {

	private EditToolHandler		handler;
	private Point				position;
	private SegmentCopyContext	segmentCopyContext;
	private MapMouseEvent		mapMouseEvent;

	public SetReferenceSegmentCommand(	EditToolHandler handler,
										Point cursorPosition,
										SegmentCopyContext context,
										MapMouseEvent e) {

		this.handler = handler;
		this.position = cursorPosition;
		this.segmentCopyContext = context;
		this.mapMouseEvent = e;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void run(IProgressMonitor monitor) throws Exception {

		final ILayer editLayer = handler.getEditLayer();
		final Point mouseLocation = position;

		final EditBlackboard layerBlackboard = handler.getEditBlackboard(editLayer);
		final boolean includeSegmentsInCurrent = true;
		final SnapBehaviour snapBehaviour = SnapBehaviour.CURRENT_LAYER;

		final CoordinateReferenceSystem mapCrs = handler.getContext().getCRS();
		final int snappingRadius = PreferenceUtil.instance().getSnappingRadius();

		final SnapSegmentFinder segmentFinder = new SnapSegmentFinder(mapCrs);
		List<LineSegment> linesList = new ArrayList<LineSegment>();
		LineSegment closestSnapSegment;
		closestSnapSegment = segmentFinder.getClosestSnapSegment(handler, layerBlackboard, mouseLocation,
					includeSegmentsInCurrent, snapBehaviour, snappingRadius);

		if (closestSnapSegment != null) {
			CoordinateReferenceSystem layerCrs = editLayer.getCRS();
			closestSnapSegment = GeoToolsUtils.reproject(closestSnapSegment, mapCrs, layerCrs);
		}
		linesList.add(closestSnapSegment);
		if (this.mapMouseEvent.isShiftDown()) {
			this.segmentCopyContext.addSegments(linesList);
		} else {
			this.segmentCopyContext.setReferenceLineSegment(closestSnapSegment);
		}
		this.segmentCopyContext.setMode(PrecisionToolsMode.WAITING);

		System.out.println("\nSegment: " + closestSnapSegment.toString());

	}

	public void rollback(IProgressMonitor monitor) throws Exception {
		// TODO Auto-generated method stub

	}

}
