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
package es.axios.udig.ui.editingtools.precisionsegmentcopy.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.commands.DeselectEditGeomCommand;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.EditGeom;
import net.refractions.udig.tools.edit.support.EditUtils;
import net.refractions.udig.tools.edit.support.PrimitiveShape;
import net.refractions.udig.tools.edit.support.ShapeType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.command.DrawGeomColorCommnad;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsMode;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.command.AddCustomVertexCommand;

/**
 * 
 * This class will draw the parallel on the blackboard.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public class SegmentCopyPreview implements Observer {

	private SegmentCopyContext	parallelContext	= null;
	private IToolContext		context			= null;
	private EditToolHandler		handler			= null;

	public SegmentCopyPreview(	IToolContext toolContext,
								EditToolHandler editToolHandler,
								SegmentCopyContext parallelContext) {

		this.context = toolContext;
		this.handler = editToolHandler;
		this.parallelContext = parallelContext;
	}

	public void update(Observable o, Object arg) {

		redraw();
	}

	/**
	 * Deletes the previous parallel preview and draws a new one.
	 */
	private void redraw() {

		UndoableComposite composite = new UndoableComposite();

		delete(composite);
		draw(composite);

		composite.setMap(handler.getContext().getMap());
		context.sendASyncCommand(composite);

		handler.repaint();
	}

	/**
	 * Deletes the parallel preview.
	 * 
	 * @param composite
	 */
	private void delete(UndoableComposite composite) {

		List<EditGeom> list = new LinkedList<EditGeom>();
		list.add(handler.getCurrentGeom());

		// Deselects the selected painted geometry
		composite.addCommand(new DeselectEditGeomCommand(handler, list));
		// Select the current geometry.
		composite.addCommand(new DrawGeomColorCommnad(handler, parallelContext));
	}

	/**
	 * Draw the parallel preview. The context have a state which is used to know
	 * when the parallel tool is ready for drawing the preview.
	 * 
	 * @param composite
	 * 
	 */
	public void draw(UndoableComposite composite) {

		// check if it's ready.
		if (!(parallelContext.getMode() == PrecisionToolsMode.READY)) {
			return;
		}

		parallelContext.calculateDistanceCoordinate();

		double distanceCoorX = parallelContext.getDistanceCoorX();
		double distanceCoorY = parallelContext.getDistanceCoorY();

		// EditGeom editGeom = parallelContext.getReferenceLine();
		// PrimitiveShape shape = editGeom.getShell();
		EditBlackboard bb = handler.getEditBlackboard(handler.getEditLayer());
		// Iterator<Coordinate> coorIt = shape.coordIterator();

		List<LineSegment> segmentList = parallelContext.getSegmentList();
		List<Coordinate> coorList = segmentToCoordinate(segmentList);
		Iterator<Coordinate> coorIt = coorList.iterator();

		EditGeom newEditGeom = bb.newGeom("", ShapeType.LINE); //$NON-NLS-1$
		PrimitiveShape shape = newEditGeom.getShell();
		handler.setCurrentShape(shape);
		handler.setCurrentState(EditState.MODIFYING);

		Coordinate coor, newCoor;

		while (coorIt.hasNext()) {

			coor = coorIt.next();
			System.out.println("\nCOOR:" + coor.toString());
			newCoor = new Coordinate(coor.x + distanceCoorX, coor.y + distanceCoorY);
			composite.addFinalizerCommand(new AddCustomVertexCommand(handler, bb,
						new EditUtils.EditToolHandlerShapeProvider(handler), newCoor, shape));
			System.out.println("\nNEW COOR:" + newCoor.toString());
		}
	}

	/**
	 * Get the coordinates of each segment, add those coordinates to the list
	 * and return the entire list.
	 * 
	 * @param segmentList
	 * @return
	 */
	private List<Coordinate> segmentToCoordinate(List<LineSegment> segmentList) {

		List<Coordinate> coorList = new ArrayList<Coordinate>();

		for (LineSegment seg : segmentList) {

			coorList.add(seg.p0);
			coorList.add(seg.p1);
		}

		return coorList;
	}

}
