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
package es.axios.udig.ui.editingtools.parallel;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.core.IProvider;
import net.refractions.udig.project.BlackboardEvent;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IBlackboardListener;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.project.render.IViewportModelListener;
import net.refractions.udig.project.render.ViewportModelEvent;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tools.edit.Activator;
import net.refractions.udig.tools.edit.EditToolConfigurationHelper;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.EventBehaviour;
import net.refractions.udig.tools.edit.activator.DrawCurrentGeomVerticesActivator;
import net.refractions.udig.tools.edit.behaviour.DrawCreateVertexSnapAreaBehaviour;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineSegment;

import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.editingtools.internal.behaviour.DrawEditGeomWithCustomEndlineActivator;
import es.axios.udig.ui.editingtools.internal.behaviour.DrawOrthoAxesActivator;
import es.axios.udig.ui.editingtools.internal.behaviour.IEditPointProvider;
import es.axios.udig.ui.editingtools.internal.behaviour.ParallelEditPointProvider;
import es.axios.udig.ui.editingtools.internal.behaviour.SelectLineSegmentBehaviour;
import es.axios.udig.ui.editingtools.internal.behaviour.SelectLineSegmentBehaviour.BlackboardSegmentProvider;

/**
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public class ParallelAction extends AbstractEditModifierActionDelegate {

    @Override
    protected void activate() {
        super.removeRunningActivators();

        EditToolHandler handler = getHandler();
        IBlackboard blackboard = handler.getContext().getMap().getBlackboard();
        IProvider<LineSegment> lineProvider = new BlackboardSegmentProvider(blackboard);
        IEditPointProvider editPointProvider = new ParallelEditPointProvider(lineProvider);

        addActivator(new DrawParallelOrhoAxesActivator());
        addActivator(new DrawEditGeomWithCustomEndlineActivator(editPointProvider));
        addActivator(new DrawCurrentGeomVerticesActivator());
        addActivator(new ClearSelectedSegmentActivator());

        List<EventBehaviour> eventBehaviours = handler.getBehaviours();
        List<EventBehaviour> backup = new ArrayList<EventBehaviour>(eventBehaviours);
        eventBehaviours.clear();
        EditToolConfigurationHelper helper = new EditToolConfigurationHelper(eventBehaviours);

        helper.add(new DrawCreateVertexSnapAreaBehaviour());

        helper.startMutualExclusiveList();
        // either select a parallel reference segment
        helper.add(new SelectLineSegmentBehaviour());

        // or run the default behaviours to add the shape points
        helper.startOrderedList(true);
        for( EventBehaviour b : backup ) {
            helper.add(b);
        }
        helper.stopOrderedList();

        helper.stopMutualExclusiveList();

        helper.done();
    }

//    @Override
//    protected void deactivate() {
//        super.deactivate();
//        restoreOriginalActivators();
//    }

    /**
     * Activator that draws the orthogonal axes centered at the mouse location only if there is a
     * selected line segment for the parallel mode.
     * <p>
     * The axes rotation changes when:
     * <ul>
     * <li> The map's CRS changes
     * <li> The selected segment in the map's blackboard changes
     * </ul>
     * </p>
     */
    private static class DrawParallelOrhoAxesActivator extends DrawOrthoAxesActivator
            implements
                IViewportModelListener,
                IBlackboardListener {

        /**
         * Registers itself as a listener of CRS changes in the map and line segment selection
         * changes in the blackboard to update the ortho axes rotation angle accordingly
         */
        @Override
        public void activate( EditToolHandler handler ) {
            super.activate(handler);
            IViewportModel viewportModel = getViewportModel(handler);
            viewportModel.addViewportModelListener(this);

            IBlackboard blackboard = getBlackboard(handler);
            blackboard.addListener(this);
        }

        /**
         * Deregisters itself as a listener of CRS changes in the map and line segment selection
         * changes in the blackboard
         */
        @Override
        public void deactivate( EditToolHandler handler ) {
            super.deactivate(handler);
            super.setValid(false);
            IViewportModel viewportModel = getViewportModel(handler);
            viewportModel.removeViewportModelListener(this);

            IBlackboard blackboard = getBlackboard(handler);
            blackboard.removeListener(this);
        }

        private IViewportModel getViewportModel( EditToolHandler handler ) {
            final IToolContext context = handler.getContext();
            final IMap map = context.getMap();
            IViewportModel viewportModel = map.getViewportModel();
            return viewportModel;
        }

        private IBlackboard getBlackboard( EditToolHandler handler ) {
            IToolContext context = handler.getContext();
            IMap map = context.getMap();
            IBlackboard blackboard = map.getBlackboard();
            return blackboard;
        }

        /**
         * Returns the angle in radians of the <code>segmentInLayerCrs</code> inclination
         * calculated in the CRS of the map, so the visualized feedback is consistent with the
         * user's view regardless of the internal CRS of the data.
         * 
         * @param segmentInLayerCrs
         * @return
         */
        private double getAngleInMapCoordinates( LineSegment segmentInLayerCrs ) {
            CoordinateReferenceSystem mapCrs = handler.getContext().getCRS();
            CoordinateReferenceSystem layerCrs = handler.getEditLayer().getCRS();
            LineSegment segmentInMapCrs = GeoToolsUtils.reproject(segmentInLayerCrs, layerCrs,
                                                                  mapCrs);
            double angle = segmentInMapCrs.angle();
            return angle;
        }

        /**
         * Gets notified of changes in the map's CRS so to update the rotation angle of the axes
         * 
         * @see IViewportModelListener#changed(ViewportModelEvent)
         */
        public void changed( ViewportModelEvent event ) {
            if (ViewportModelEvent.EventType.CRS == event.getType()) {
                perfomAxesRotation(getBlackboard(handler));
            }
        }

        /**
         * Gets notified of changes in the selected segment and updates the axes rotation angle
         * accordingly
         * 
         * @see IBlackboardListener#blackBoardChanged(BlackboardEvent)
         */
        public void blackBoardChanged( BlackboardEvent event ) {
            if (SelectLineSegmentBehaviour.SELECTED_SEGMENT_BBOARD_KEY == event.getKey()) {
                perfomAxesRotation(event.getSource());
            }
        }

        /**
         * Gets notified when the map's blackboard is cleared (and this if there were a selected
         * segment it does no longer exists) and updates the axes rotation angle accordingly
         * 
         * @see IBlackboardListener#blackBoardCleared(IBlackboard)
         */
        public void blackBoardCleared( IBlackboard source ) {
            perfomAxesRotation(source);
        }

        private void perfomAxesRotation( IBlackboard bboard ) {
            LineSegment segmentInLayerCrs;
            segmentInLayerCrs = (LineSegment) bboard
                                                    .get(SelectLineSegmentBehaviour.SELECTED_SEGMENT_BBOARD_KEY);
            double angle = 0;
            if (segmentInLayerCrs != null) {
                angle = getAngleInMapCoordinates(segmentInLayerCrs);
            }
            super.setRotationAngle(angle);
            handler.repaint();
        }
    }

    /**
     * Activator meant to remove any selected line segment from the map's blackboard when this tool
     * mode is deactivated.
     */
    private static class ClearSelectedSegmentActivator implements Activator {

        /**
         * Empty method
         */
        public void activate( EditToolHandler handler ) {
        }

        /**
         * Removes the selected line segment from the map's blackboard by setting the value of the
         * {@link SelectLineSegmentBehaviour#SELECTED_SEGMENT_BBOARD_KEY} key to <code>null</code>
         */
        public void deactivate( EditToolHandler handler ) {
            final IToolContext context = handler.getContext();
            final IMap map = context.getMap();
            IBlackboard blackboard = map.getBlackboard();
            blackboard.put(SelectLineSegmentBehaviour.SELECTED_SEGMENT_BBOARD_KEY, null);
        }

        /**
         * Empty method
         */
        public void handleActivateError( EditToolHandler handler, Throwable error ) {
        }

        /**
         * Empty method
         */
        public void handleDeactivateError( EditToolHandler handler, Throwable error ) {
        }

    }
}
