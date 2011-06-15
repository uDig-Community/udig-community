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

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.AnimationUpdater;
import net.refractions.udig.project.ui.commands.SelectionBoxCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.animation.MessageBubble;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.util.LineStringExtracter;

import es.axios.udig.ui.editingtools.internal.i18n.Messages;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.SegmentCopyContext;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsMode;

/**
 * <p>
 * Create a bbox and set the segments that are under the bbox as reference
 * segments.
 * </p>
 * <p>
 * If the reference segments list isn't empty and Shift key is down, add the
 * segments under the bbox to the reference segments list.
 * </p>
 * 
 * <p>
 * Requirements:
 * <ul>
 * <li>state==MODIFYING or NONE</li>
 * <li>event type == RELEASED or DRAGGED or PRESSED</li>
 * <li>button1 must be the button that was released</li>
 * <li>when dragging doesn't take in account the released button</li>
 * </ul>
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class CreateBBoxEventBehaviour implements EventBehaviour {

	private SegmentCopyContext	segmentCopyContext	= null;

	public CreateBBoxEventBehaviour(SegmentCopyContext context) {

		this.segmentCopyContext = context;
	}

	public boolean isValid(EditToolHandler handler, MapMouseEvent e, EventType eventType) {

		boolean legalState = handler.getCurrentState() == EditState.NONE
					|| handler.getCurrentState() == EditState.MODIFYING;
		boolean releaseButtonState = eventType == EventType.RELEASED || eventType == EventType.DRAGGED
					|| eventType == EventType.PRESSED;

		boolean legalButton = e.button == MapMouseEvent.BUTTON1;

		boolean legal = true;
		if (!(legalState && releaseButtonState && legalButton)) {

			legal = false;
		}
		// additional if because when is dragging it says no buttons are
		// clicked.(e.button=NONE)
		if (legalState && eventType == EventType.DRAGGED && segmentCopyContext.isLineSegmentEmpty()) {

			legal = true;
		}

		if (legalState && eventType == EventType.DRAGGED && e.isShiftDown()) {

			legal = true;
		}

		return legal;
	}

	public UndoableMapCommand getCommand(EditToolHandler handler, MapMouseEvent e, EventType eventType) {

		if (!isValid(handler, e, eventType)) {
			throw new IllegalArgumentException("Behaviour is not valid for the current state"); //$NON-NLS-1$
		}

		SelectionBoxCommand shapeCommand = this.segmentCopyContext.getShapeCommand();
		Point start = this.segmentCopyContext.getBBoxStartPoint();
		switch (eventType) {
		case PRESSED:

			this.segmentCopyContext.setMode(PrecisionToolsMode.PRE_DRAG);
			this.segmentCopyContext.setBBoxStartPoint(e.getPoint());
			shapeCommand.setValid(true);
			shapeCommand.setShape(new Rectangle(e.getPoint().x, e.getPoint().y, 0, 0));
			handler.getContext().sendASyncCommand(shapeCommand);
			break;
		case DRAGGED:

			this.segmentCopyContext.setMode(PrecisionToolsMode.DRAG);
			shapeCommand.setShape(new Rectangle(Math.min(start.x, e.x), Math.min(start.y, e.y),
						Math.abs(e.x - start.x), Math.abs(start.y - e.y)));
			handler.getContext().getViewportPane().repaint();
			break;
		case RELEASED:

			this.segmentCopyContext.setMode(PrecisionToolsMode.POST_DRAG);
			Coordinate c1 = handler.getContext().getMap().getViewportModel().pixelToWorld(start.x, start.y);
			Coordinate c2 = handler.getContext().getMap().getViewportModel().pixelToWorld(e.getPoint().x,
						e.getPoint().y);
			// When a click is done (PRESS -> RELEASE) on same point, change
			// ParallelContext Mode and return.
			if (c1.equals2D(c2)) {
				this.segmentCopyContext.setMode(PrecisionToolsMode.WAITING);
				shapeCommand.setValid(false);
				break;
			}

			Envelope bounds = new Envelope(c1, c2);
			int count = countFeatures(handler, bounds);

			if (count > 1) {
				showBubble(handler.getContext(), e, Messages.PrecisionSegmentCopy_bbox_feature_count_advise);
				shapeCommand.setValid(false);
				break;
			} else if (count == 0) {
				// When the bbox is empty, return
				this.segmentCopyContext.setMode(PrecisionToolsMode.WAITING);
				shapeCommand.setValid(false);
				break;
			}

			getSegments(handler, bounds, e);

			shapeCommand.setValid(false);
			break;
		default:
			break;
		}

		return null;
	}

	/**
	 * <p>
	 * Get the feature contained on the bbox and split it on LineStrings. Get
	 * the Envelope of each LineStrings and if the bbox intersects that
	 * envelope, this LineSegment is contained on the bbox so we add to the
	 * list. At the end, we check the list with the possible segments and if any
	 * segment is contiguous, it's added.
	 * </p>
	 * 
	 * @param handler
	 * @param bbox
	 * @param e
	 */
	private void getSegments(EditToolHandler handler, Envelope bbox, MapMouseEvent e) {

		assert handler != null : "Handler cannot be null";
		assert bbox != null : "bbox cannot be null";

		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = null;
		FeatureIterator<SimpleFeature> iterator = null;
		Geometry geom, bboxGeometry;
		List<LineSegment> segments = new ArrayList<LineSegment>(); // List of
		// possible
		// segment
		// to add.
		try {
			collection = handler.getContext().getFeaturesInBbox(handler.getEditLayer(), bbox);
			iterator = collection.features();
			while (iterator.hasNext()) {

				Coordinate[] newCoor = null;
				LineString lineString;
				geom = (Geometry) iterator.next().getDefaultGeometry();
				System.out.println("Geometry:" + geom.toString());

				List<?> linesList = LineStringExtracter.getLines(geom);
				for (Object obj : linesList) {

					LineString line = (LineString) obj;

					Coordinate[] coorList = line.getCoordinates();
					GeometryFactory gfac = line.getFactory();
					CoordinateSequenceFactory coorFac = gfac.getCoordinateSequenceFactory();
					CoordinateSequence coordinates;

					bboxGeometry = gfac.toGeometry(bbox);

					for (int i = 0; i < coorList.length - 1; i++) {

						newCoor = new Coordinate[2];
						newCoor[0] = coorList[i];
						newCoor[1] = coorList[i + 1];
						coordinates = coorFac.create(newCoor);
						lineString = gfac.createLineString(coordinates);

						if (isLineContainedOnBBox(lineString, bboxGeometry)) {

							LineSegment lineSeg = new LineSegment(coorList[i], coorList[i + 1]);
							segments.add(lineSeg);
							// TODO insertar todos de golpe, no de uno en uno
							// por lo siguiente:
							// hay 4 segmentos, 1 enlaza con 2, 2 enlaza con 3,
							// etc
							// 3 esta en la lista, si vas a insertar 1 y 2, el
							// resultado seria
							// 1,2,3, todos enlazados, pero al insertar uno x
							// uno, si insertas primero 1, no
							// va a enlazar con 3.
							// TODO los segmentos a agregar que sean del mismo
							// feature.???

							System.out.println("To add:" + lineSeg.toString());
							// String result =
							// parallelContext.addLineSegment(lineSeg);
						}
					}
				}

			}
			if (!segments.isEmpty()) {
				String result = segmentCopyContext.addSegments(segments);
				if (!(result.equals(""))) {
					showBubble(handler.getContext(), e, result);
				}
				segmentCopyContext.sortSegmentList();
			}
		} catch (IOException e1) {
			// TODO
			e1.printStackTrace();
		} finally {
			if (iterator != null) {
				collection.close(iterator);
			}
		}

	}

	/**
	 * Search if the provided LineString is contained on the bbox.
	 * 
	 * @param line
	 * @param bboxGeometry
	 * @return True if the line is contained, false otherwise.
	 */
	private boolean isLineContainedOnBBox(LineString line, Geometry bboxGeometry) {

		assert line != null : "There must be a linestring";
		assert bboxGeometry != null : "There must be a bounding box";

		return bboxGeometry.intersects(line);
	}

	/**
	 * Show the message bubble advising the user something did wrong.
	 * 
	 * @param context
	 * @param e
	 */
	private void showBubble(IToolContext context, MapMouseEvent e, String message) {
		AnimationUpdater.runTimer(context.getMapDisplay(), new MessageBubble(e.x, e.y, message, PreferenceUtil
					.instance().getMessageDisplayDelay()));

	}

	/**
	 * Count how many features are under the bbox.
	 * 
	 * @param bounds
	 * @param handler
	 * @return
	 */
	private int countFeatures(EditToolHandler handler, Envelope bounds) {

		assert handler != null;
		assert bounds != null;

		int count = 0;

		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = null;
		FeatureIterator<SimpleFeature> iterator = null;
		try {
			collection = handler.getContext().getFeaturesInBbox(handler.getEditLayer(), bounds);
			iterator = collection.features();
			while (iterator.hasNext()) {

				iterator.next();
				count++;
			}
		} catch (IOException e1) {
			// TODO log... do something
			e1.printStackTrace();
		} finally {
			if (iterator != null) {
				collection.close(iterator);
			}
		}

		return count;
	}

	public void handleError(EditToolHandler handler, Throwable error, UndoableMapCommand command) {

		EditPlugin.log("", error); //$NON-NLS-1$
	}

}
