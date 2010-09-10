package net.refractions.linecleaner;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;

import org.geotools.data.FeatureStore;
import org.geotools.feature.Feature;
import org.geotools.filter.Filter;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

/**
 * <p>
 * Useful functions for working with Geometries.
 * </p>
 * @author myronwu
 * @author rgould
 */
public class GeometryUtil {
    /** GeometryUtil PRECISION_TOLERANCE field see crudeEquals */
    public static final double PRECISION_TOLERANCE = 0.00001;
    
    /**
     * Test whether two geometries have the same end points irrespective of direction.
     * @param g
     * @param h
     * @return Do g and h have equal end points?
     */
    public static boolean identicalEndPoints(Geometry g, Geometry h) {
        LineString gline = extractLine(g);
        LineString hline = extractLine(h);

        if (gline == null || hline == null) {
            return false;
        }
        return identicalEndPoints(gline, hline);
    }
    
    /**
     *
     * @param g
     * @param geoms
     * @return Do g's nodes connect with geometries in geoms?
     */
    public static boolean connects(Geometry g, Collection<Geometry> geoms) {
        LineString gline = extractLine(g);
        if (gline == null) {
            return false;
        }
        
        Point start = gline.getStartPoint();
        Point end = gline.getEndPoint();
        
        boolean startConnects = false;
        boolean endConnects = false;
        for (Geometry h: geoms) {
            LineString hline = extractLine(h);
            if (hline == null || hline.equals(gline)) {
                continue;
            }
            Point hstart = hline.getStartPoint();
            Point hend = hline.getEndPoint();
            if (start.equals(hstart) || start.equals(hend)) {
                startConnects = true;
            }
            if (end.equals(hstart) || end.equals(hend)) {
                endConnects = true;
            }
            if (startConnects && endConnects) {
                return true;
            }
        }
        return false;
    }
    
    /**
     *
     * @param g
     * @param h
     * @return Do g and h have an end point in common?
     */
    public static boolean hasCommonEndPoint(Geometry g, Geometry h) {
        LineString gline = extractLine(g);
        LineString hline = extractLine(h);
        
        if (gline == null || hline == null) {
            return false;
        }
        return hasCommonEndPoint(gline,hline);
    }
    
    /**
     *
     * @param m
     * @param n
     * @return Do m and n have at least one common end point?
     */
    public static boolean hasCommonEndPoint(LineString m, LineString n) {
        Point mstart = m.getStartPoint();
        Point mend = m.getEndPoint();
        Point nstart = n.getStartPoint();
        Point nend = n.getEndPoint();
        return mstart.equals(nstart) || mstart.equals(nend) ||
        mend.equals(nstart) || mend.equals(nend);
    }
    
    /**
     * Test whether two linestrings have the same end points irrespective of direction.
     * @param m
     * @param n
     * @return Do m and n have the same end points?
     */
    public static boolean identicalEndPoints(LineString m, LineString n) {
        return (m.getStartPoint().equals(n.getStartPoint()) &&
                m.getEndPoint().equals(n.getEndPoint()))
                || (m.getStartPoint().equals(n.getEndPoint()) &&
                        m.getEndPoint().equals(n.getStartPoint()));        
    }

    /**
     * @param g
     * @return Is g a single linestring multistring?  This occurs in
     * shapefiles.
     */
    public static boolean oneLineMultiString(Geometry g) {
        return g instanceof MultiLineString && g.getNumGeometries() == 1;
    }

    /**
     *
     * @param f
     * @param g
     * @return Do f and g have identical geometries?
     */
    public static boolean identicalGeometries(Feature f, Feature g) {
        return f.getDefaultGeometry().equals(g.getDefaultGeometry());
    }

    /**
     * This is an interface for a function object that's used as a function argument in
     * linestring subdivision.
     */
    public interface CoordinateFunction {
        public void run(Coordinate c);
    }
    
    /**
     * Subdivide g into intervals of length intervalLength.  However, in the interest of
     * saving memory for long linestrings, run a function f on each subdivision point instead
     * of returning an array of coordinates as in the function below.
     * @param g
     * @param intervalLength
     * @param f Closure fulfilling CoordinateFunction interface.
     */
    public static void subdivide(LineString g, double intervalLength, CoordinateFunction f) {
        // the linestring is too short to have any intervals.  fallback to nodes
        if (g.getLength() <= intervalLength) {
            f.run(g.getStartPoint().getCoordinate());
            f.run(g.getEndPoint().getCoordinate());
        } else {
            double intervalOffset = 0;
            int limit = g.getNumPoints() - 2;
            for (int i = 0; i <= limit; i++) {
                Line line = new Line(g.getCoordinateN(i), g.getCoordinateN(i+1));
                
                if (intervalOffset < line.getLength()) {
                    line = new Line(line.along(intervalOffset), line.getEnd());
                } else {
                    intervalOffset -= line.getLength();
                    continue;
                }
                
                if (line.getLength() < intervalLength) {
                    // interval's too long, but there's a vertex in the current line
                    if (intervalOffset < intervalLength) {
                        f.run(line.start);
                    }
                    intervalOffset = intervalLength - line.getLength();
                } else if (line.getLength() > intervalLength) {
                    int numPoints = line.subdivide(intervalLength, 0, f);
                    intervalOffset = intervalLength - (line.getLength() - (intervalLength * (numPoints-1)));
                } else {
                    f.run(line.getStart());
                    f.run(line.getEnd());
                }
            }
        }       
    }
    
    /**
     * Subdivide a LineString g into intervals of length intervalLength.
     * @param g
     * @param intervalLength
     * @return Coordinates that form subdivisions of g of length intervalLength.
     */
    public static Coordinate[] subdivide(LineString g, double intervalLength) {
        Stack<Coordinate> result = new Stack<Coordinate>();
        Coordinate[] coordinates = g.getCoordinates();
        
        // the linestring is too short to have any intervals.  fallback to nodes
        if (g.getLength() <= intervalLength) {
            result.push(g.getStartPoint().getCoordinate());
            result.push(g.getEndPoint().getCoordinate());
        } else {
            double intervalOffset = 0;
            int limit = coordinates.length - 2;
            for (int i = 0; i <= limit; i++) {
                Line line = new Line(coordinates[i], coordinates[i+1]);
                
                if (intervalOffset < line.getLength()) {
                    line = new Line(line.along(intervalOffset), line.getEnd());
                } else {
                    intervalOffset -= line.getLength();
                    continue;
                }
                
                if (line.getLength() < intervalLength) {
                    // interval's too long, but there's a vertex in the current line
                    if (intervalOffset < intervalLength) {
                        result.push(line.start);
                    }
                    intervalOffset = intervalLength - line.getLength();
                } else if (line.getLength() > intervalLength) {
                    Collection<Coordinate> subPoints = line.subdivide(intervalLength, 0);
                    result.addAll(subPoints);
                    intervalOffset = intervalLength - (line.getLength() - (intervalLength * (subPoints.size()-1)));
                } else {
                    result.push(line.getStart());
                    result.push(line.getEnd());
                }
            }
        }

        return result.toArray(new Coordinate[result.size()]);       
    }
    
    /**
     * Extracts a LineString from a either a linestring, or single-linestring
     * multistring (common in shapefiles).
     * @param g
     * @return The LineString in g
     */
    public static LineString extractLine(Geometry g) {
        LineString line;
        if (g instanceof LineString) {
            line = (LineString)g;
        } else if (oneLineMultiString(g)) {
            line = (LineString)((MultiLineString)g).getGeometryN(0);
        } else {
            line = null;
        }
        return line;
    }
    
    /**
     * Wraps a linestring in a multilinestring.  Useful for writing out
     * to shapefiles where such a thing is expected.
     * @param line
     * @return A multilinestring that wraps the given line.
     */
    public static MultiLineString wrapInMultiLineString(LineString line) {
        GeometryFactory gf = line.getFactory();
        return gf.createMultiLineString(new LineString[] {line});
    }

    /**
     *
     * @param ls
     * @param points
     * @return LineStrings that result from subdividing ls at points.
     */
    public static Collection<LineString> subdivide(LineString ls, Collection<Coordinate> points){
        LineString newLineString = addVertices(ls, points);
        return makeLines(ls.getFactory(), divideIntoCoordinateVectors(newLineString, points));
    }
    
    protected static Vector<LineString> makeLines(GeometryFactory gf, Vector<Vector<Coordinate>> coordinateVectors) {
        Vector<LineString> lines = new Vector<LineString>(coordinateVectors.size());
        for (Vector<Coordinate> coordinates: coordinateVectors) {
            lines.add(gf.createLineString(coordinates.toArray(new Coordinate[coordinates.size()])));
        }
        return lines;
    }
    
    protected static Vector<Vector<Coordinate>> divideIntoCoordinateVectors(LineString ls, Collection<Coordinate> points) {
        Collection<Coordinate> pointsCopy = new HashSet<Coordinate>(points);
        Vector<Vector<Coordinate>> result = new Vector<Vector<Coordinate>>(pointsCopy.size()+1);
        result.add(new Vector<Coordinate>());
        
        Coordinate[] coords = ls.getCoordinates();
        for (int i = 0, j = 0; i < coords.length; i++) {
            Coordinate c = coords[i];
            if (i != 0 && i != coords.length-1 && pointsCopy.contains(c)) {
                pointsCopy.remove(c);
                result.get(j).add(c); // add the current point as end node to the current line
                // move to the next line
                j++;
                Vector<Coordinate> nextPoints = new Vector<Coordinate>();
                nextPoints.add(c);
                result.add(nextPoints);
            } else {
                result.get(j).add(c);
            }
        }
        return result;
    }
    
    /**
     *
     * @param ls
     * @param vertices Vertices to add to ls.
     * @return ls with vertices added in.
     */
    public static LineString addVertices(LineString ls, Collection<Coordinate> vertices) {
        LineString result = ls;
        for (Coordinate vertex: vertices) {
            // this is ugly--is there a way to write a destructive addVertex?
            result = addVertex(result, vertex);
        }
        return result;
    }
    
    /**
     * Add a vertex to a LineString.  It's up to the caller to call this
     * with a vertex that's actually on the LineString, otherwise the
     * vertex will be appended as the last coordinate in the LineString. 
     * @param ls
     * @param vertex
     * @return ls with vertex added in.
     */
    public static LineString addVertex(LineString ls, Coordinate vertex) {
        if (ls.isCoordinate(vertex)) {
            return ls;
        }
        Coordinate[] coords = ls.getCoordinates();
        int index = findInsertionIndex(ls, vertex);
        Coordinate[] newCoords = insertCoordinate(coords, vertex, index);

        return ls.getFactory().createLineString(newCoords);
    }
    
    /**
     *
     * @param coords
     * @param c
     * @param index
     * @return Coordinate array with c inserted at index.
     */
    public static Coordinate[] insertCoordinate(Coordinate[] coords, Coordinate c, int index) {
        Vector<Coordinate> vector = new Vector<Coordinate>(Arrays.asList(coords));
        vector.insertElementAt(c, index);
        return vector.toArray(new Coordinate[coords.length+1]);
    }
    
    /**
     * Find the position in which to insert a vertex into a LineString's
     * coordinate array.  It's up to the caller to call this with a vertex
     * on the LineString, otherwise the returned index will be the end
     * of the array.
     * @param ls
     * @param vertex
     * @return Position in ls's coordinate array in which to insert vertex.
     */
    public static int findInsertionIndex(LineString ls, Coordinate vertex) {
        Collection<LineString> lines = foldCoordinates(ls);
        int i = 0;
        for (LineString line: lines) {
            // Use a crude intersect here instead of line.intersects().  We're
            // calling this method with approximations of projected points on the line,
            // which means line.intersects() never returns true.  What we're doing 
            // instead is projecting a second point and testing for rough equality.
            if (!line.isCoordinate(vertex) && crudeIntersects(line, vertex)) {
                break;
            }
            i++;
        }
        return i+1;
    }

    protected static boolean crudeIntersects(LineString ls, Coordinate c) {
        return crudeEquals(GeometryUtil.getClosestPoint(c, ls), c);
    }
    
    protected static boolean crudeEquals(Coordinate c, Coordinate d) {
        return Math.max(c.x, d.x) - Math.min(c.x,d.x) < PRECISION_TOLERANCE &&
        Math.max(c.y, d.y) - Math.min(c.y, d.y) < PRECISION_TOLERANCE;
    }
    
    /**
     * Build the collection of lines (represented as one-line linestrings) that make
     * up the LineString ls.
     * @param ls
     * @return Collection of LineStrings made by folding together the coordinates of ls.
     */
    public static Collection<LineString> foldCoordinates(LineString ls) {
        GeometryFactory gf = ls.getFactory();
        Coordinate[] coords = ls.getCoordinates();
        Collection<LineString> lines = new LinkedList<LineString>();
        int limit = coords.length-1;
        for (int i = 0; i < limit; i++) {
            Coordinate start = coords[i];
            Coordinate end = coords[i+1];
            lines.add(makeLine(gf, start, end));
        }
        return lines;
    }
    
    // convenience costructor for making a line masquerading as linestring
    protected static LineString makeLine(GeometryFactory gf, Coordinate start, Coordinate end) {
        return gf.createLineString(new Coordinate[] {start, end});
    }
    
    /**
     *
     * @param store
     * @param endNode
     * @param distanceTolerance
     * @return BBoxFilter to fetch features with distanceTolerance of endNode.
     */
    public static Filter getBBoxFilter(FeatureStore store, Point endNode, double distanceTolerance) {
    	
    	Coordinate coord =endNode.getCoordinate();
    	
    	double minx = coord.x - distanceTolerance;
    	double maxx = coord.x + distanceTolerance;
    	double miny = coord.y - distanceTolerance;
    	double maxy = coord.y + distanceTolerance;
    	
    	Envelope bounds = new Envelope(minx, maxx, miny, maxy);
    	
    	try {
    		return FeatureUtil.withinBbox(store.getSchema().getDefaultGeometry().getName(), bounds);
    	} catch (IllegalFilterException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	return null;
    }

    /**
     * 
     * @param c
     * @param g
     * @return An approximation of the closest point on Geometry g from Coordinate c.
     */
    public static Coordinate getClosestPoint(Coordinate c, Geometry g) {
        Point p = g.getFactory().createPoint(c);
        Coordinate coordinate = DistanceOp.closestPoints(g,p)[0];
        return coordinate;
    }
}
