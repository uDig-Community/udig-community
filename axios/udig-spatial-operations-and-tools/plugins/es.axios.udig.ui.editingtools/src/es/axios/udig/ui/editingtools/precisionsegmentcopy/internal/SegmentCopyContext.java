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
import java.util.List;

import net.refractions.udig.project.ui.commands.SelectionBoxCommand;
import net.refractions.udig.tools.edit.support.EditGeom;
import net.refractions.udig.tools.edit.support.Point;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import es.axios.udig.ui.editingtools.internal.i18n.Messages;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsContext;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsMode;

/**
 * The context of the precision segment copy.
 * 
 * Stores the necessary parameters to draw a segment copy.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public class SegmentCopyContext extends PrecisionToolsContext {

	// Context parameters.


	private EditGeom			referenceLine			= null;
	private LineSegment			referenceLineSegment	= null;
	private Coordinate			startCoordinate			= null;
	private Coordinate			endCoordinate			= null;
	private List<LineSegment>	segmentList				= new ArrayList<LineSegment>();

	private java.awt.Point		bboxStartPoint			= null;
	private SelectionBoxCommand	shapeCommand			= new SelectionBoxCommand();


	/**
	 * Initializes its context. Sets the initial data to null. Set the precision
	 * copy state waiting.
	 * 
	 */
	@Override
	public synchronized void initContext() {

		initialCoordinate = null;
		referenceLine = null;
		reverse = false;
		distanceCoorX = 0;
		distanceCoorY = 0;
		referenceCoor = null;
		length = null;
		previousMode = PrecisionToolsMode.WAITING;
		mode = PrecisionToolsMode.WAITING;

		referenceLineSegment = null;
		startCoordinate = null;
		endCoordinate = null;
		segmentList.clear();
		bboxStartPoint = null;
		shapeCommand = new SelectionBoxCommand();
		update(UPDATE_LAYER);
	}

	/**
	 * Calculate the distances between coordinates.
	 */
	public synchronized void calculateDistanceCoordinate() {

		assert this.initialCoordinate != null;
		assert this.referenceCoor != null;

		// TODO need to improve this.
		if (previousMode == PrecisionToolsMode.EDITING) {
			// After editing manually the fields on the view, don't calculate it
			// because the values introduced by the user are valid.
			return;
		}
		double iniX = initialCoordinate.x;
		double iniY = initialCoordinate.y;

		double refX = referenceCoor.x;
		double refY = referenceCoor.y;

		this.distanceCoorX = (iniX - refX);
		this.distanceCoorY = (iniY - refY);
	}

	/**
	 * Get the referenceLine which segment copy will be based on.
	 * 
	 * @return
	 */
	public EditGeom getReferenceLine() {

		return referenceLine;
	}

	/**
	 * Set the referenceLine which segment copy will be based on. Also stores
	 * the coordinate where the reference line starts.
	 * 
	 * @param referenceLine
	 */
	public synchronized void setReferenceLine(EditGeom referenceLine) {

		assert referenceLine != null;

		this.referenceLine = referenceLine;

		Point referencePoint = this.referenceLine.getShell().getPoint(0);
		this.referenceCoor = this.bb.toCoord(referencePoint);

		update(UPDATE_LAYER);
	}

	/**
	 * Set the reference segment which copy will be based on. Also stores the
	 * coordinate where the reference line starts.
	 */
	public synchronized void setReferenceLineSegment(LineSegment lineSegment) {

		assert lineSegment != null;

		this.referenceLineSegment = lineSegment;

		this.referenceCoor = lineSegment.p0;
		this.startCoordinate = lineSegment.p0;
		this.endCoordinate = lineSegment.p1;

		// TODO now we assume that the list only will have 2 coordinate, not
		// multiple segments added.
		segmentList.clear();
		segmentList.add(lineSegment);

		update(UPDATE_LAYER);
	}

	/**
	 * Set the reference coordinate. TODO UPDATE THE COMMENT By default it take
	 * as reference coordinate the point(0) of the reference line. When called
	 * this function, instead of taking the first point, it takes as reference
	 * point the last point.
	 */
	public synchronized void changeReferenceCoor() {

		assert referenceLineSegment != null;

		if (reverse) {
			this.referenceCoor = this.startCoordinate;
			reverse = false;
		} else {
			this.referenceCoor = this.endCoordinate;
			reverse = true;
		}

		update(UPDATE_LAYER);
	}

	/**
	 * True if segment list is empty.
	 * 
	 * @return
	 */
	public boolean isLineSegmentEmpty() {

		return this.segmentList.isEmpty();
	}

	/**
	 * Return the first line segment.
	 * 
	 * @return
	 */
	public String getLineSegmentToString() {

		String msg = "";
		msg = this.segmentList.get(0).toString();
		return msg;
	}

	/**
	 * Get the coordinate list which contains the coordinates of reference line
	 * segment.
	 * 
	 * @return
	 */
	public List<LineSegment> getSegmentList() {

		return this.segmentList;
	}


	/**
	 * Set the start point of the bbox.
	 * 
	 * @param point
	 */
	public synchronized void setBBoxStartPoint(java.awt.Point point) {

		this.bboxStartPoint = point;
	}

	/**
	 * Get the start point of the bbox.
	 * 
	 * @return
	 */
	public java.awt.Point getBBoxStartPoint() {

		return this.bboxStartPoint;
	}

	/**
	 * The command uses to draw a bbox.
	 * 
	 * @return the shapeCommand
	 */
	public SelectionBoxCommand getShapeCommand() {

		return shapeCommand;
	}

	/**
	 * TODO UPDATE THE COMMENT Add the LineSegment to the coordinate
	 * list(contains all LineSegment coordinates), if the segment isn't next to
	 * anyone of the list, will return a message.
	 * 
	 * @param lineSegment
	 * @return Empty string if all goes well, or a string containing the message
	 *         if something goes wrong.
	 */
	public synchronized String addLineSegment(LineSegment lineSegment) {

		assert lineSegment != null;

		// TODO check if the line segments are contiguous.

		if (!(checkSegmentContiguous(lineSegment))) {
			return Messages.PrecisionSegmentCopy_Segment_not_contiguous;
		}

		// XXX
		this.referenceLineSegment = lineSegment;

		this.referenceCoor = lineSegment.p0;
		this.startCoordinate = lineSegment.p0;
		this.endCoordinate = lineSegment.p1;
		// XXX

		// TODO add in the correct position. sorted
		segmentList.add(lineSegment);

		update(UPDATE_LAYER);

		return "";
	}

	/**
	 * Before adding this segment to the coordinate list, must ensure this
	 * segment
	 * 
	 * @param lineSegment
	 * @return
	 */
	private boolean checkSegmentContiguous(LineSegment lineSegment) {

		// TODO
		if (segmentList.isEmpty()) {
			return true;
		}
		Coordinate start, end;
		start = lineSegment.p0;
		end = lineSegment.p1;

		for (LineSegment seg : segmentList) {

			Coordinate coor = seg.p0;

			if ((coor.equals2D(start)) || (coor.equals2D(end))) {
				return true;
			}
			coor = seg.p1;

			if ((coor.equals2D(start)) || (coor.equals2D(end))) {
				return true;
			}
		}

		System.out.println("NOT IN:" + start.toString() + "||" + end.toString());
		return false;
	}

	/**
	 * Recursive. TODO UPDATE THE COMMENT
	 * 
	 * @param segments
	 */
	public String addSegments(List<LineSegment> segments) {
		String msg = "";
		Coordinate coor1, coor2, lineCoor1, lineCoor2;
		List<LineSegment> removedSegment = new ArrayList<LineSegment>();
		for (LineSegment lineToAdd : segments) {

			coor1 = lineToAdd.p0;
			coor2 = lineToAdd.p1;
			for (LineSegment storedLine : segmentList) {

				lineCoor1 = storedLine.p0;
				lineCoor2 = storedLine.p1;
				// XXX know the correct order of adding segment.
				if ((coor1.equals2D(lineCoor1)) || (coor2.equals2D(lineCoor1)) || (coor1.equals2D(lineCoor2))
							|| (coor2.equals2D(lineCoor2))) {
					// segmentList.add(segmentList.indexOf(storedLine),
					// lineToAdd);
					if (!removedSegment.contains(lineToAdd)) {
						removedSegment.add(lineToAdd);
					}
				}

				// if ((coor1.equals2D(lineCoor2)) ||
				// (coor2.equals2D(lineCoor2))) {
				// // segmentList.add(segmentList.indexOf(storedLine) + 1,
				// // lineToAdd);
				//
				// if (!removedSegment.contains(lineToAdd)) {
				// removedSegment.add(lineToAdd);
				// }
				// }

			}
			if (segmentList.isEmpty()) {

				removedSegment.add(lineToAdd);
			}

		}
		segmentList.addAll(removedSegment);
		segments.removeAll(removedSegment);

		if (segments.isEmpty()) {
			// all the segment where added.
			msg = "";
		} else if (!removedSegment.isEmpty()) {
			// new segments were added but still left segments without been
			// added.
			msg = addSegments(segments);
		} else {
			// removedSegment is empty so on this iteration none was added
			// some segment aren't contiguous.
			msg = Messages.PrecisionSegmentCopy_Segment_not_contiguous;
		}
		// FIXME hack, set reference coordinate because now is null, but this
		// isn't the first coordinate.

		this.referenceCoor = segmentList.get(0).p0;
		;
		this.startCoordinate = segmentList.get(0).p0;
		;
		this.endCoordinate = segmentList.get(0).p1;

		return msg;
	}

	/**
	 * Find the header segment, then sort the list.
	 */
	public void sortSegmentList() {

		LineSegment header = findHeaderSegment();
		sortList(header);
	}

	private LineSegment findHeaderSegment() {

		boolean isHeaderP0, isHeaderP1;
		Coordinate p0, p1;
		for (LineSegment headerCandidate : segmentList) {

			// we assume it can be the header segment.
			isHeaderP0 = true;
			// isHeaderP1 = true;
			p0 = headerCandidate.p0;
			p1 = headerCandidate.p1;
			for (LineSegment line : segmentList) {

				if (headerCandidate.equalsTopo(line)) {
					continue; // if it is the same line, continue.
				}
				// one of the coordinate of the header segment isn't covered on
				// any segment.
				if (p0.equals2D(line.p0) || p0.equals2D(line.p1)) {
					isHeaderP0 = false;
				}
				// if (p1.equals2D(line.p0) || p1.equals2D(line.p1)) {
				// isHeaderP1 = false;
				// }

				if (!isHeaderP0) {// && !isHeaderP1) {
					break;// both coordinate are the same on other segment, so
					// this segment isn't a header segment.
				}
			}
			if (isHeaderP0) {// || isHeaderP1) {
				return headerCandidate;
			}
		}
		assert false : "Always returns a header, code should never reach here.";
		return null;
	}

	/**
	 * TODO UPDATE THE COMMENT
	 * 
	 * @param header
	 */
	private void sortList(LineSegment header) {

		List<LineSegment> newList = new ArrayList<LineSegment>();
		LineSegment compare;
		Coordinate p0, p1;
		// add the header segment to the list, remove from old list, and set the
		// header segment as the segment to be compared.
		newList.add(header);
		segmentList.remove(header);
		compare = header;

		while (newList.size() - 1 != segmentList.size()) {
			//
			for (LineSegment oldLine : segmentList) {

				p0 = compare.p0;
				p1 = compare.p1;
				if (compare.equalsTopo(oldLine)) {
					continue;// Don't compare the same LineSegment
				}
				if ((p0.equals2D(oldLine.p0) || p0.equals2D(oldLine.p1) || p1.equals2D(oldLine.p0) || p1
							.equals2D(oldLine.p1))
							&& (!newList.contains(oldLine))) {
					newList.add(oldLine);
					compare = oldLine;
				}
			}
		}
		segmentList.clear();
		segmentList.addAll(newList);
	}

}
