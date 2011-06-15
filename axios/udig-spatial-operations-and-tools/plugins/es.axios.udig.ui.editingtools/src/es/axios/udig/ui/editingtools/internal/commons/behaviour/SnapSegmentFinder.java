/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
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
package es.axios.udig.ui.editingtools.internal.commons.behaviour;

import java.io.IOException;
import java.util.List;

import net.refractions.udig.mapgraphic.grid.GridMapGraphic;
import net.refractions.udig.mapgraphic.grid.GridStyle;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.project.ui.AnimationUpdater;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.animation.SearchBoxAnimation;
import net.refractions.udig.tools.edit.support.ClosestEdge;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.IsBusyStateProvider;
import net.refractions.udig.tools.edit.support.Point;
import net.refractions.udig.tools.edit.support.SnapBehaviour;
import net.refractions.udig.ui.ProgressManager;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.geotools.util.GeoToolsUtils;

/**
 * Utility class to find out the closest segment to a given (screen) point from
 * the segments in the edit shape, the current layer, all the active layers,
 * etc, as defined by a {@link SnapBehaviour}.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public class SnapSegmentFinder {

	/**
	 * Which CRS to perform the search and return the snapping segment on
	 */
	private CoordinateReferenceSystem		calculationCrs;

	private static final GeometryFactory	gfac	= new GeometryFactory();

	/**
	 * @param calculationCrs
	 *            which CRS to use to perform the snapping calculation. Needed
	 *            as different CRS's may lead to different segments being the
	 *            closest. This is also going to be the CRS the returned
	 *            LineSegment is on.
	 */
	public SnapSegmentFinder(CoordinateReferenceSystem calculationCrs) {
		assert calculationCrs != null;
		this.calculationCrs = calculationCrs;
	}

	public CoordinateReferenceSystem getCalculationCrs() {
		return calculationCrs;
	}

	/**
	 * @param handler
	 *            the edit tool handler in use
	 * @param editBlackboard
	 *            the EditBlackboard of the cureent edit layer
	 * @param centerPoint
	 *            the snapping point in screen coordinates
	 * @param includeSegmentsInCurrent
	 *            whether to include the current edit feature segments in the
	 *            lookup
	 * @param snapBehaviour
	 *            the snap behaviour to use
	 * @param snappingRadius
	 *            snap search distance in pixels
	 * @return the closest line segment inside the snapping distance with
	 *         coordinates in the calculation crs provided at the constructor
	 */
	public LineSegment getClosestSnapSegment(	EditToolHandler handler,
												final EditBlackboard editBlackboard,
												final Point centerPoint,
												final boolean includeSegmentsInCurrent,
												final SnapBehaviour snapBehaviour,
												final int snappingRadius) {

		final IToolContext context = handler.getContext();
		Coordinate centerCoordInTargetCrs = getCenterCoordInTargetCrs(centerPoint, context);

		MinSegFinder minFinder = new MinSegFinder(centerCoordInTargetCrs);
		SearchBoxAnimation anim = null;

		final EditState previousState = handler.getCurrentState();
		try {
			handler.setCurrentState(EditState.BUSY);
			if (snapBehaviour != SnapBehaviour.OFF && snapBehaviour != SnapBehaviour.GRID) {
				anim = new SearchBoxAnimation(centerPoint, new IsBusyStateProvider(handler));
				AnimationUpdater.runTimer(context.getMapDisplay(), anim);
			}
			switch (snapBehaviour) {
			case OFF:
				return null;
			case SELECTED:
				searchEditBlackboard(context, editBlackboard, centerPoint, includeSegmentsInCurrent, snappingRadius,
							minFinder);
				break;

			case CURRENT_LAYER:
				searchEditBlackboard(context, editBlackboard, centerPoint, includeSegmentsInCurrent, snappingRadius,
							minFinder);
				searchSegmentInLayer(handler.getEditLayer(), context, centerPoint, snappingRadius, minFinder);
				break;
			case ALL_LAYERS:
				searchEditBlackboard(context, editBlackboard, centerPoint, includeSegmentsInCurrent, snappingRadius,
							minFinder);
				for (ILayer layer : context.getMapLayers()) {
					searchSegmentInLayer(layer, context, centerPoint, snappingRadius, minFinder);
				}
				break;
			case GRID:
				findClosestGridSegment(centerPoint, context.getMap(), snappingRadius, minFinder);
			default:
				break;
			}

			LineSegment min = minFinder.getMinSegment();

			return min;

		} finally {
			handler.setCurrentState(previousState);
			if (anim != null) {
				anim.setValid(false);
			}
		}

	}

	private Coordinate getCenterCoordInTargetCrs(Point centerPoint, final IToolContext context) {
		Coordinate centerCoordInMapCrs = context.pixelToWorld(centerPoint.getX(), centerPoint.getY());
		CoordinateReferenceSystem mapCrs = context.getCRS();
		Coordinate centerCoordInTargetCrs;
		try {
			com.vividsolutions.jts.geom.Point pointInCalculationCrs;
			com.vividsolutions.jts.geom.Point point = gfac.createPoint(centerCoordInMapCrs);
			pointInCalculationCrs = (com.vividsolutions.jts.geom.Point) GeoToolsUtils.reproject(point, mapCrs,
						calculationCrs);
			centerCoordInTargetCrs = pointInCalculationCrs.getCoordinate();
		} catch (OperationNotFoundException e) {
			throw new RuntimeException(e);
		} catch (TransformException e) {
			throw new RuntimeException(e);
		}
		return centerCoordInTargetCrs;
	}

	/**
	 * Searches the editblackboard and adds the closest segment to the
	 * minFinder.
	 * <p>
	 * As a difference with the other search methods, this one makes the search
	 * in screen coordinates and then adds the result to minFinder in the
	 * calculation CRS
	 * </p>
	 * 
	 * @param snappingRadius
	 */
	private void searchEditBlackboard(	final IToolContext context,
										final EditBlackboard editBlackboard,
										final Point centerPoint,
										final boolean includeVerticesInCurrent,
										final int snappingRadius,
										MinSegFinder minFinder) {

		final boolean treatUnknownAsPolygon = true;
		final Coordinate centerPointInMapCrs = context.pixelToWorld(centerPoint.getX(), centerPoint.getY());
		MinSegFinder screenFinder = new MinSegFinder(centerPointInMapCrs);

		List<ClosestEdge> candidates;
		candidates = editBlackboard.getCandidates(centerPoint.getX(), centerPoint.getY(), treatUnknownAsPolygon);

		Point closestPoint;
		int indexOfPreviousPoint;
		Point previousPoint;
		Coordinate p0;
		Coordinate p1;
		for (ClosestEdge edge : candidates) {
			if (snappingRadius >= edge.getDistanceToEdge()) {
				closestPoint = edge.getPointOnLine();
				indexOfPreviousPoint = edge.getIndexOfPrevious();
				previousPoint = edge.getPart().getPoint(indexOfPreviousPoint);
				p0 = context.pixelToWorld(closestPoint.getX(), closestPoint.getY());
				p1 = context.pixelToWorld(previousPoint.getX(), previousPoint.getY());
				screenFinder.add(p0, p1);
			}
		}

		final LineSegment screenSegment = screenFinder.getMinSegment();
		if (screenSegment != null) {
			CoordinateReferenceSystem mapCrs = context.getCRS();
			LineSegment segment = GeoToolsUtils.reproject(screenSegment, mapCrs, calculationCrs);
			minFinder.add(segment.p0, segment.p1);
		}
	}

	// /**
	// * Roughly stolen from EditUtils as its private
	// * <p>
	// * TODO: consider asking for the EditUtils version to become public
	// * </p>
	// *
	// * @param p
	// * @param editBlackboard
	// * @param currentShape
	// * @return
	// */
	// private boolean containsNonCurrentShape( Point p, EditBlackboard
	// editBlackboard,
	// PrimitiveShape currentShape ) {
	// if (p == null || currentShape == null)
	// return false;
	// List<EditGeom> geoms = editBlackboard.getGeoms(p.getX(), p.getY());
	// if (geoms.isEmpty())
	// return false;
	// if (geoms.size() > 1 || geoms.get(0) != currentShape.getEditGeom())
	// return true;
	//
	// return false;
	// }

	/**
	 * Searches the layer for line segments within snapping distance
	 * 
	 * @param layer
	 *            the layer to search that can resolve to {@link FeatureSource}
	 * @param context
	 *            the context to use for convenience methods
	 * @param centerPoint
	 *            the current centerPoint.
	 * @param snappingRadius
	 *            snap search distance in pixels
	 * @param minFinder
	 * @return the closest vertex in the layer within the snapping radius or
	 *         null if no segment is found on the snapping radious distance.
	 */
	private void searchSegmentInLayer(	final ILayer layer,
										final IToolContext context,
										final Point centerPoint,
										final int snappingRadius,
										MinSegFinder minFinder) {
		if (!layer.hasResource(FeatureSource.class) || !layer.isApplicable(EditPlugin.ID) || !layer.isVisible())
			return;

		final ILayer editLayer = context.getEditManager().getEditLayer();
		SimpleFeatureType schema = layer.getSchema();
		Class<?> geomType = schema.getGeometryDescriptor().getType().getBinding();
		if (Point.class == geomType || MultiPoint.class == geomType) {
			return;
		}

		SimpleFeature editFeature = context.getEditManager().getEditFeature();
		String editFeatureID = null;
		if (editFeature != null) {
			editFeatureID = editFeature.getID();
		}

		int snapBoxSideLength = snappingRadius * 2;
		java.awt.Point screenLocation = new java.awt.Point(centerPoint.getX(), centerPoint.getY());
		Envelope bbox = context.getBoundingBox(screenLocation, snapBoxSideLength);

		try {
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = context.getFeaturesInBbox(layer, bbox);
			FeatureIterator<SimpleFeature> iter = null;
			try {
				final CoordinateReferenceSystem layerCrs = layer.getCRS();
				for (iter = features.features(); iter.hasNext();) {
					SimpleFeature feature = iter.next();
					if (feature.getID().equals(editFeatureID) && layer == editLayer) {
						continue;
					}
					Geometry geometry = (Geometry) feature.getDefaultGeometry();
					if (geometry != null) {
						geometry = GeoToolsUtils.reproject(geometry, layerCrs, this.calculationCrs);
						searchGeometry(geometry, minFinder);
					}
				}
			} finally {
				if (iter != null) {
					features.close(iter);
				}
			}
		} catch (Exception e) {
			EditPlugin.log("", e); //$NON-NLS-1$
		}
	}

	/**
	 * @param geom
	 *            any geometry except point or multipoint
	 * @param minFinder
	 */
	private void searchGeometry(Geometry geom, MinSegFinder minFinder) {
		final int numParts = geom.getNumGeometries();
		Geometry partN;
		for (int i = 0; i < numParts; i++) {
			partN = geom.getGeometryN(i);
			if (partN instanceof Polygon) {
				Polygon polygon = (Polygon) partN;
				searchLinearGeom(polygon.getExteriorRing(), minFinder);
				int numInteriorRing = polygon.getNumInteriorRing();
				for (int ringN = 0; ringN < numInteriorRing; ringN++) {
					searchLinearGeom(polygon.getInteriorRingN(ringN), minFinder);
				}
			} else {
				searchLinearGeom((LineString) partN, minFinder);
			}
		}
	}

	private void searchLinearGeom(LineString line, MinSegFinder minFinder) {
		final int numPoints = line.getNumPoints();
		Coordinate coordinate1;
		Coordinate coordinate2;
		for (int i = 1; i < numPoints; i++) {
			coordinate1 = line.getCoordinateN(i - 1);
			coordinate2 = line.getCoordinateN(i);
			minFinder.add(coordinate1, coordinate2);
		}
	}

	/**
	 * @param map
	 * @param minFinder
	 * @param screenPoint
	 * @param snappingRadius
	 */
	public void findClosestGridSegment(	final Point screenPoint,
										final IMap map,
										final int snappingRadius,
										MinSegFinder minFinder) {
		List<ILayer> layers = map.getMapLayers();

		// by default choose something that will work
		ILayer found = layers.get(0);
		GridMapGraphic graphic = new GridMapGraphic();
		for (ILayer layer : layers) {
			if (layer.hasResource(GridMapGraphic.class)) {
				found = layer;
				try {
					graphic = layer.getResource(GridMapGraphic.class, ProgressManager.instance().get());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				break;
			}
		}

		final IViewportModel viewport = map.getViewportModel();
		final Coordinate screenPointInMapCrs = viewport.pixelToWorld(screenPoint.getX(), screenPoint.getY());

		LineSegment closestInMapCrs = findClosestGridSegmentInMapCrs(screenPoint, found, graphic, viewport,
					screenPointInMapCrs);

		if (closestInMapCrs != null) {
			final CoordinateReferenceSystem mapCrs = viewport.getCRS();
			Coordinate coord2 = viewport.pixelToWorld(screenPoint.getX() + snappingRadius, screenPoint.getY()
						+ snappingRadius);
			final double snapRadiusInMapCrs = screenPointInMapCrs.distance(coord2);
			if (closestInMapCrs.distance(screenPointInMapCrs) <= snapRadiusInMapCrs) {
				LineSegment gridSegmentInDestinationCrs;
				gridSegmentInDestinationCrs = GeoToolsUtils.reproject(closestInMapCrs, mapCrs, calculationCrs);

				minFinder.add(gridSegmentInDestinationCrs.p0, gridSegmentInDestinationCrs.p1);
			}
		}
	}

	private LineSegment findClosestGridSegmentInMapCrs(	final Point screenPoint,
														ILayer found,
														GridMapGraphic graphic,
														final IViewportModel viewport,
														final Coordinate screenPointInMapCrs) {
		double[] closestPointInMapCrs;
		try {
			closestPointInMapCrs = graphic.closest(screenPoint.getX(), screenPoint.getY(), found);
		} catch (FactoryException e) {
			EditPlugin.log(null, e);
			throw new RuntimeException(e);
		}

		final GridStyle style = getStyle(found);
		double dx = style.getGridSize()[0];
		double dy = style.getGridSize()[1];
		if (GridStyle.Type.SCREEN == style.getType()) {
			Coordinate coordinate = viewport.pixelToWorld((int) dx, (int) dy);
			dx = coordinate.x;
			dy = coordinate.y;
		}

		MinSegFinder mapCrsFinder = new MinSegFinder(screenPointInMapCrs);
		double x = closestPointInMapCrs[0];
		double y = closestPointInMapCrs[1];
		mapCrsFinder.add(x, y, x + dx, y);
		mapCrsFinder.add(x, y, x - dx, y);
		mapCrsFinder.add(x, y, x, y + dy);
		mapCrsFinder.add(x, y, x, y - dy);

		LineSegment closestInMapCrs = mapCrsFinder.getMinSegment();
		return closestInMapCrs;
	}

	/**
	 * Stolen from {@link GridMapGraphic#getStyle(ILayer layer )} as its
	 * private.
	 * <p>
	 * TODO: ask for {@link GridMapGraphic#getStyle(ILayer layer )} to become
	 * public?
	 * </p>
	 * 
	 * @param layer
	 * @return
	 */
	private GridStyle getStyle(ILayer layer) {
		GridStyle gridStyle = (GridStyle) layer.getStyleBlackboard().get(GridStyle.ID);
		if (gridStyle == null) {
			return GridStyle.DEFAULT_STYLE;
		}
		return gridStyle;
	}

	/**
	 * Keeps track of the line segment that is at the minimum distance to the
	 * center point.
	 * <p>
	 * Adapted from the original
	 * {@link net.refractions.udig.tools.edit.support.EditUtils.MinFinder} by
	 * Jesse
	 * </p>
	 */
	private static class MinSegFinder {
		private Coordinate	centerCoord;
		private LineSegment	testingSegment;
		private LineSegment	minDistanceSegment;
		private double		minDistance	= Double.MAX_VALUE;

		public MinSegFinder(Coordinate coord) {
			this.centerCoord = coord;
			this.testingSegment = new LineSegment();
		}

		public LineSegment getMinSegment() {
			return minDistanceSegment;
		}

		public void add(double x1, double y1, double x2, double y2) {
			add(new Coordinate(x1, y1), new Coordinate(x2, y2));
		}

		public void add(Coordinate start, Coordinate end) {
			testingSegment.setCoordinates(start, end);
			double distance = testingSegment.distance(centerCoord);
			if (distance < minDistance) {
				minDistance = distance;
				minDistanceSegment = new LineSegment(start, end);
			}
		}
	}
}
