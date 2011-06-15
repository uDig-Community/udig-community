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
package es.axios.udig.ui.editingtools.internal.behaviour;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

import net.refractions.udig.core.IProvider;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.render.displayAdapter.IMapDisplay;
import net.refractions.udig.project.ui.AnimationUpdater;
import net.refractions.udig.project.ui.IAnimation;
import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.EventType;
import net.refractions.udig.tools.edit.LockingBehaviour;
import net.refractions.udig.tools.edit.preferences.PreferenceUtil;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.SnapBehaviour;

import org.eclipse.core.runtime.IProgressMonitor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineSegment;

import es.axios.udig.ui.commons.util.GeoToolsUtils;

/**
 * Locking event behaviour to find out the closest line to a mouse click.
 * <p>
 * On mouse release, searches for the closest point over the closest line segment following the
 * {@link SnapBehaviour} defined as a preference.
 * </p>
 * <p>
 * Requirements:
 * <ul>
 * <li>EventType == RELEASED
 * <li>EditState == CREATING
 * </ul>
 * </p>
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public class SelectLineSegmentBehaviour implements LockingBehaviour {

    public static final String SELECTED_SEGMENT_BBOARD_KEY = "SELECTED_LINE_SEGMENT";

    /**
     * Used together with lastEditLayer to avoid repeating snap calculations between isValid and
     * getCommand
     */
    private Point              lastPoint;
    /**
     * Used together with lastPoint to avoid repeating snap calculations between isValid and
     * getCommand
     */
    private ILayer             lastEditLayer;
    private LineSegment        lastSelectedSegment;

    /**
     * @author gabriel
     */
    public static class BlackboardSegmentProvider implements IProvider<LineSegment> {
        private IBlackboard blackboard;

        public BlackboardSegmentProvider( IBlackboard blackboard ) {
            this.blackboard = blackboard;
        }

        /**
         * @return the selected line segment stored in the blackboard, if any, or
         *         <code>null<code> otherwise.
         */
        public LineSegment get( Object...params) {
            LineSegment segment = (LineSegment) blackboard.get(SELECTED_SEGMENT_BBOARD_KEY);
            return segment;
        }
    }

    /**
     * @return <code>this</code>
     * @see LockingBehaviour#getKey(EditToolHandler)
     */
    public Object getKey( EditToolHandler handler ) {
        return this;
    }

    /**
     * @see EventBehaviour#isValid(EditToolHandler, MapMouseEvent, EventType)
     */
    public boolean isValid( EditToolHandler handler, MapMouseEvent e, EventType eventType ) {
        boolean validMode = EventType.RELEASED == eventType;
        boolean validState = EditState.CREATING == handler.getCurrentState();
        boolean isValid = validMode && validState;
        if (isValid) {
            LineSegment closestSegment = getSnapSegmentInLayerCrs(handler, e);
            isValid = closestSegment != null;
        }
        return isValid;
    }

    /**
     * Finds out the closest segment to the point indicated by the mouse event following the
     * snapping behaviour set as preference.
     * <p>
     * The snap segment lookup is made in the current <b>map</b>'s CRS and the resulting
     * {@link LineSegment} is stored in the <b>edit layer</b>'s CRS.
     * </p>
     * <p>
     * If the lookup is successful (i.e. a snap segment is found) two objects are stored in the
     * map's {@link IBlackboard blackboard}:
     * <ul>
     * <li>the snapping segment in layer CRS with the {@link #SELECTED_SEGMENT_BBOARD_KEY} key
     * <li>a new {@link IEditPointProvider} which returns a point parallel to the given segment
     * taking in count the mouse location and the last edit shape point, with the
     * {@link IEditPointProvider#BLACKBOARD_KEY} key
     * </ul>
     * If the lookup fails (i.e. no snap segment is found) <b>no modification</b> is made to the
     * map blackboard.
     * </p>
     * <p>
     * Shall be called only if isValid(...) == true
     * </p>
     * 
     * @see EventBehaviour#getCommand(EditToolHandler, MapMouseEvent, EventType)
     * @return <code>null</code>
     */
    public UndoableMapCommand getCommand( EditToolHandler handler, MapMouseEvent e,
                                          EventType eventType ) {

        handler.lock(this);

        try {
            LineSegment snapSegmentInLayerCrs = getSnapSegmentInLayerCrs(handler, e);

            final IBlackboard mapBlackboard = handler.getContext().getMap().getBlackboard();
            mapBlackboard.put(SELECTED_SEGMENT_BBOARD_KEY, snapSegmentInLayerCrs);

            if (snapSegmentInLayerCrs != null) {
                runAnimation(handler, snapSegmentInLayerCrs);

                IProvider<LineSegment> lineProvider = new BlackboardSegmentProvider(mapBlackboard);
                IEditPointProvider provider = new ParallelEditPointProvider(lineProvider);

                mapBlackboard.put(IEditPointProvider.BLACKBOARD_KEY, provider);
            }
        } finally {
            handler.unlock(this);
        }

        return null;
    }

    /**
     * Performs the lookup of the closest segment to the point given by the mouse location and
     * following the snap behaviour set as a preference.
     * <p>
     * The lookup is made in the <b>map</b>'s CRS, but the returned segment is transformed to the
     * <b>edit layer</code>'s CRS
     * </p>
     * 
     * @param handler
     * @param e
     * @return
     */
    private LineSegment getSnapSegmentInLayerCrs( EditToolHandler handler, MapMouseEvent e ) {
        final ILayer editLayer = handler.getEditLayer();
        final Point mouseLocation = Point.valueOf(e.x, e.y);
        if (lastPoint != null && lastEditLayer == editLayer && lastPoint.equals(mouseLocation)) {
            return lastSelectedSegment;
        }

        final EditBlackboard layerBlackboard = handler.getEditBlackboard(editLayer);
        final boolean includeSegmentsInCurrent = true;
        final SnapBehaviour snapBehaviour = PreferenceUtil.instance().getSnapBehaviour();

        final CoordinateReferenceSystem mapCrs = handler.getContext().getCRS();
        final int snappingRadius = PreferenceUtil.instance().getSnappingRadius();

        final SnapSegmentFinder segmentFinder = new SnapSegmentFinder(mapCrs);
        LineSegment closestSnapSegment;
        closestSnapSegment = segmentFinder.getClosestSnapSegment(handler, layerBlackboard,
                                                                 mouseLocation,
                                                                 includeSegmentsInCurrent,
                                                                 snapBehaviour, snappingRadius);

        if (closestSnapSegment != null) {
            CoordinateReferenceSystem layerCrs = editLayer.getCRS();
            closestSnapSegment = GeoToolsUtils.reproject(closestSnapSegment, mapCrs, layerCrs);
        }
        lastPoint = mouseLocation;
        lastEditLayer = editLayer;
        lastSelectedSegment = closestSnapSegment;

        return closestSnapSegment;
    }

    /**
     * @see EventBehaviour#handleError(EditToolHandler, Throwable, UndoableMapCommand)
     */
    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
    }

    private void runAnimation( EditToolHandler handler, LineSegment segmentInLayerCrs ) {
        EditBlackboard bboard = handler.getEditBlackboard(handler.getEditLayer());
        Point p1 = bboard.toPoint(segmentInLayerCrs.p0);
        Point p2 = bboard.toPoint(segmentInLayerCrs.p1);

        Line2D line = new Line2D.Float(p1.getX(), p1.getY(), p2.getX(), p2.getY());

        SegmentAnimation animation = new SegmentAnimation(line);

        IToolContext context = handler.getContext();
        IMapDisplay mapDisplay = context.getMapDisplay();

        AnimationUpdater.runTimer(mapDisplay, animation);
    }

    private class SegmentAnimation extends AbstractDrawCommand implements IAnimation {

        private int       runs    = 0;
        private final int maxRuns = 6;
        private Line2D    line;

        public SegmentAnimation( Line2D line ) {
            this.line = line;
        }

        public short getFrameInterval() {
            return 200;
        }

        public void nextFrame() {
            runs++;
        }

        public boolean hasNext() {
            return runs < maxRuns;
        }

        public void run( IProgressMonitor monitor ) throws Exception {
            if (runs < maxRuns && runs % 2 == 0) {
                graphics.setColor(Color.YELLOW);
                graphics.draw(line);
            }
        }

        /**
         * TODO
         */
        public Rectangle getValidArea() {
            return null;
        }
    }
}