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

import net.refractions.udig.core.IProvider;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.LayerUtil;

/**
 * {@link IEditPointProvider} that returns the target point tha is on the parallel line to the
 * reference segment (returned by the passed in <code>IProvider&lt;LineSegment&gt;</code>),
 * defined by the last edit shape point and the normal to the (infinite) line defined by the
 * reference segment and that passes through the mouse location.
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public class ParallelEditPointProvider implements IEditPointProvider {

    private IProvider<LineSegment>    provider;

    private LineSegment               lastSegmentInLayerCrs;
    private LineSegment               lastSegmentInMapCrs;
    private CoordinateReferenceSystem lastMapCrs;

    /**
     * @param provider a provider for the line segment to calculate the parallel segment from, which
     *        shall provide the line segment in the edit layer's CRS
     */
    public ParallelEditPointProvider( IProvider<LineSegment> provider ) {
        assert provider != null;
        this.provider = provider;
    }

    /**
     * Performs the calculation in map coordinates using the segment in map coordinates provided by
     * the <code>IProvider&lt;LineSegment&gt;</code> settled on the constructor, and returns the
     * coordinate in the edit layer's crs as per
     * {@link IEditPointProvider#getCoordinate(MapMouseEvent, EditToolHandler)}
     */
    public Coordinate getCoordinate( MapMouseEvent e, EditToolHandler handler ) {
        return getCoordinateInLayerCrs(e, handler);
    }

    /**
     * Performs the calculation in map coordinates
     * 
     * @see IEditPointProvider#getPoint(MapMouseEvent, EditToolHandler)
     */
    public Point getPoint( MapMouseEvent e, EditToolHandler handler ) {
        Coordinate coordInMapCrs = getCoordinateInMapCrs(e, handler);
        java.awt.Point point = handler.getContext().worldToPixel(coordInMapCrs);
        return Point.valueOf(point.x, point.y);
    }

    private Coordinate getCoordinateInLayerCrs( MapMouseEvent e, EditToolHandler handler ) {
        Coordinate coordInMapCrs = getCoordinateInMapCrs(e, handler);
        Coordinate coordInLayerCrs = null;
        ILayer layer = handler.getEditLayer();
        IMap map = handler.getContext().getMap();
        coordInLayerCrs = LayerUtil.mapToLayer(map, layer, coordInMapCrs);

        return coordInLayerCrs;
    }

    /**
     * Returns the coordinate which defines a segment parallel to the one provided by the
     * {@link IProvider} set on the constructor, and whose starting point is the last point of the
     * current edit layer.
     * 
     * @param e
     * @param handler
     * @return the coordinate of the mouse event if there's no current edit shape or there's no
     *         selected segment to calculate the parallel segment, a coordinate defining a parallel
     *         segment between the last edit shape point and the current mouse location otherwise
     */
    private Coordinate getCoordinateInMapCrs( MapMouseEvent e, EditToolHandler handler ) {
        final LineSegment segmentInLayerCrs = provider.get();
        final PrimitiveShape currentShape = handler.getCurrentShape();

        if (segmentInLayerCrs == null || currentShape == null || currentShape.getNumPoints() == 0) {
            return handler.getContext().pixelToWorld(e.x, e.y);
        }

        final IToolContext context = handler.getContext();
        final CoordinateReferenceSystem mapCrs = context.getCRS();

        final LineSegment segmentInMapCrs;
        // fast check for repeated state to avoid expensive calculation
        if (lastMapCrs == mapCrs && lastSegmentInLayerCrs == segmentInLayerCrs) {
            segmentInMapCrs = lastSegmentInMapCrs;
        } else {
            CoordinateReferenceSystem layerCrs = handler.getEditLayer().getCRS();
            segmentInMapCrs = GeoToolsUtils.reproject(segmentInLayerCrs, layerCrs, mapCrs);
            lastMapCrs = mapCrs;
            lastSegmentInLayerCrs = segmentInLayerCrs;
            lastSegmentInMapCrs = segmentInMapCrs;
        }

        final Point lastShapePoint = currentShape.getPoint(currentShape.getNumCoords() - 1);

        final Coordinate lastShapeCoord = context.pixelToWorld(lastShapePoint.getX(),
                                                               lastShapePoint.getY());

        final Coordinate mouseCoordinate = context.pixelToWorld(e.x, e.y);
        Coordinate parallelToSegment = calculateEndPoint(segmentInMapCrs, lastShapeCoord,
                                                         mouseCoordinate);
        return parallelToSegment;
    }

    LineSegment normal = new LineSegment();

    /**
     * Calculates the coordinate where the rect parallel to the one where <code>segment</code> is
     * contained and that passes through <code>lastShapePoint</code> intersects it over the normal
     * that passes through the <code>mouseCoordinate</code>.
     * <p>
     * In order to avoid inconsistencies this calculation must be performed in the map's CRS space
     * </p>
     * 
     * @param segment
     * @param lastShapePoint
     * @param mouseCoordinate
     * @return
     */
    private Coordinate calculateEndPoint( final LineSegment segment,
                                          final Coordinate lastShapePoint,
                                          final Coordinate mouseCoordinate ) {
        Coordinate projectedMouse = segment.project(mouseCoordinate);
        normal.setCoordinates(mouseCoordinate, projectedMouse);
        Coordinate projected = normal.project(lastShapePoint);
        return projected;
    }

}
