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

package es.axios.udig.ui.editingtools.internal.geometryoperations.split;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.Node;

import es.axios.udig.ui.editingtools.internal.i18n.Messages;

/**
 * Performs a split of a LineString, MultiLineString, Polygon or MultiPolygon using a provided
 * LineString as cutting edge.
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class SplitStrategy {

    private final LineString splittingLine;

    private static final Map /* <Class, Class<? extends SpecificSplitOp> */strategies;
    static {
        Map knownStrategies = new HashMap();
        knownStrategies.put(LineString.class, LineStringSplitter.class);
        knownStrategies.put(MultiLineString.class, MultiLineStringSplitter.class);
        knownStrategies.put(Polygon.class, PolygonSplitter.class);
        knownStrategies.put(MultiPolygon.class, MultiPolygonSplitter.class);

        strategies = Collections.unmodifiableMap(knownStrategies);
    }

    public SplitStrategy( final LineString splittingLine ) {
        if (splittingLine == null) {
            throw new NullPointerException();
        }
        this.splittingLine = splittingLine;
    }

    public static Geometry splitOp( Geometry geom, LineString splitLine ) {
        SplitStrategy op = new SplitStrategy(splitLine);
        Geometry splittedGeometries = op.split(geom);
        return splittedGeometries;
    }

    /**
     * @param splitee
     * @return a <code>Geometry</code> containing all the splitted parts as aggregates. Use
     *         {@link Geometry#getGeometryN(int) getGeometryN(int)} to get each part.
     * @throws NullPointerException if geom is null
     * @throws IllegalArgumentException if geom is not of an acceptable geometry type to be splitted
     *         (i.e. not a linestring, multilinestring, polygon or multipolygon)
     */
    public Geometry split( final Geometry splitee ) {
        if (splitee == null) {
            throw new NullPointerException("Geometry for split operation was null");
        }
        Class spliteeClass = splitee.getClass();
        SpecificSplitOp splitOp = findSplitOp(spliteeClass);
        Geometry splitResult;

        splitOp.setSplitter(splittingLine);
        splitResult = splitOp.split(splitee);

        return splitResult;
    }

    private SpecificSplitOp findSplitOp( Class spliteeClass ) {
        if (!strategies.containsKey(spliteeClass)) {
            throw new IllegalArgumentException(Messages.SplitStrategy_illegal_geometry
                    + spliteeClass);
        }
        final Class splitOpClass = (Class) strategies.get(spliteeClass);
        SpecificSplitOp splitOp;
        try {
            splitOp = (SpecificSplitOp) splitOpClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Cannot instantiate " + splitOpClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Illegal access exception for "
                    + splitOpClass.getName(), e );
        }
        return splitOp;
    }

    /**
     * Strategy object for splitting geometry, subclasses are targeted towards specific kinds
     * of geometry.
     * <p>
     * The GeometryCollectionSplitter will hold onto a LineString provided by the user, and use
     * it to break provided geometry one by one.
     * 
     * @author Gabriel Roldán (www.axios.es)
     * @author Mauricio Pazos (www.axios.es)
     * @since 1.1.0
     */
    private static interface SpecificSplitOp {
        /**
         * LineString used for splitting; as provided by the user.
         * 
         * @param splitter LineString used by the split method to break up geometry one by one.
         */
        public void setSplitter( LineString splitter );
        
        /**
         * Split the provided Geometry using the LineString provided by the user. 
         *
         * @param splitee Geometry to split using the user supplied lineString
         * @return Original geometry, or a GeometryCollection if a split occurred
         */
        public Geometry split( Geometry splitee );
    }

    /**
     * Hold onto a LineString for use by subclasses.
     * 
     * @author Gabriel Roldán (www.axios.es)
     * @author Mauricio Pazos (www.axios.es)
     * @since 1.1.0
     */
    private static abstract class AbstractSplitter implements SpecificSplitOp {

        protected LineString splitter;

        public void setSplitter( LineString splitter ) {
            this.splitter = splitter;
        }
    }

    /**
     * User the lineString provided by the user to break up lineStrings one by one.
     * 
     * @author Gabriel Roldán (www.axios.es)
     * @author Mauricio Pazos (www.axios.es)
     * @since 1.1.0
     */
    private static class LineStringSplitter extends AbstractSplitter {
        /**
         * No-op default constructor required to reflectively instantiate the class
         */
        public LineStringSplitter() {
        }
        /**
         * @param splitee the {@link LineString} to be splitted
         */
        public Geometry split( Geometry splitee ) {
            LineString lineString = (LineString) splitee;
            Geometry splitted = lineString.difference(splitter);
            
            return splitted;
        }
    }

    /**
     * Strategy object for splitting (a geometry collection).
     * <p>
     * The GeometryCollectionSplitter will hold onto the SpecificcSplitOp in order to hold
     * onto the LineString provided by the user for spliting.
     * 
     * @author Gabriel Roldán (www.axios.es)
     * @author Mauricio Pazos (www.axios.es)
     * @since 1.1.0
     */
    private static abstract class AbstractGeometryCollectionSplitter implements SpecificSplitOp {
        /** Used to split a single geometry */
        private SpecificSplitOp singlePartSplitter;

        private AbstractGeometryCollectionSplitter( SpecificSplitOp singlePartSplitter ) {
            this.singlePartSplitter = singlePartSplitter;
        }
        /**
         * Update the singlePartSplitter with the provided LineString.
         * @param splitter LineString used by split method to split provided geometry one by one
         */
        public final void setSplitter( LineString splitter ) {
            singlePartSplitter.setSplitter(splitter);
        }
        
        /**
         * Actually forms the split, using the LineString held internally.
         * 
         */
        public final Geometry split( final Geometry splitee ) {
            final GeometryCollection coll = (GeometryCollection) splitee;
            final int numParts = coll.getNumGeometries();

            List splittedParts = new ArrayList();
            for( int partN = 0; partN < numParts; partN++ ) {
                Geometry simplePartN = coll.getGeometryN(partN);
                Geometry splittedPart = singlePartSplitter.split(simplePartN);
                if( splittedPart == null ) {
                	continue; // part was not split ... move on to the next
                }
                final int splittedPartsCount = splittedPart.getNumGeometries();
                for( int splittedPartN = 0; splittedPartN < splittedPartsCount; splittedPartN++ ) {
                    Geometry simpleSplittedPart = splittedPart.getGeometryN(splittedPartN);
                    splittedParts.add(simpleSplittedPart);
                }
            }
            GeometryFactory gf = splitee.getFactory();
            GeometryCollection splittedCollection = buildFromParts(gf, splittedParts);
            
            return splittedCollection;
        }

        protected abstract GeometryCollection buildFromParts( GeometryFactory gf, List parts );

    }

    /**
     * @author Gabriel Roldán (www.axios.es)
     * @author Mauricio Pazos (www.axios.es)
     * @since 1.1.0
     */
    private static class MultiLineStringSplitter extends AbstractGeometryCollectionSplitter {

        public MultiLineStringSplitter() {
            super(new LineStringSplitter());
        }

        @Override
        protected GeometryCollection buildFromParts( GeometryFactory gf, List parts ) {
            LineString[] lines = (LineString[]) parts.toArray(new LineString[parts.size()]);
            MultiLineString result = gf.createMultiLineString(lines);
            return result;
        }
    }

    /**
     * @author Gabriel Roldán (www.axios.es)
     * @author Mauricio Pazos (www.axios.es)
     * @since 1.1.0
     */
    private static class MultiPolygonSplitter extends AbstractGeometryCollectionSplitter {

        public MultiPolygonSplitter() {
            super(new PolygonSplitter());
        }

        @Override
        protected GeometryCollection buildFromParts( GeometryFactory gf, List parts ) {
            Polygon[] polygons = (Polygon[]) parts.toArray(new Polygon[parts.size()]);
            MultiPolygon result = gf.createMultiPolygon(polygons);
            return result;
        }
    }

    /**
     * Responsible for splitting a single polygon; polygon may be split into several parts (or a
     * hole may be formed).
     * 
     * Polygon Strategy:
     * <ul>
     * <li>Build a graph with all the edges and nodes from the intersection between the polygon and
     * the line
     * <li>Put weights on the nodes depending on the amount of incident edges. Nodes are only the
     * intersection points between the polygon boundary and the linestring, and the start point of
     * the polygon boundary.
     * <li>Classify the edges between shared (all the linestring ones) and non shared (the polygon
     * boundary ones). Store the coordinate list of an edge on the edge object itself.
     * <li>Start traveling the graph at any node, starting by its first edge.
     * <li>Alway travel to the next node, selecting the edge whose first segment has the lower
     * angle to the left (CCW) with the last segment in the linestring from the current edge.
     * <li>Remove the non shared edges used from the graph.
     * <li>Decrement in 1 the weight of the used nodes.
     * <li>Mark the remaining edges that have a node with weight < 3 as non-shared
     * <li>Remove the nodes with weight < 1 from the graph
     * </ul>
     * 
     * @author Gabriel Roldán (www.axios.es)
     * @author Mauricio Pazos (www.axios.es)
     * @since 1.1.0
     */
    private static class PolygonSplitter extends AbstractSplitter {

        /**
         * No-op default constructor required to reflectively instantiate the class
         */
        public PolygonSplitter() {
            // no-op
        }

        /**
         * Split the provided geometry, will be null if no split was needed.
         * @return split geometry or null
         */
        public Geometry split( Geometry splitee ) {
            assert splitee instanceof Polygon; // why? Class cast exception about to happen?
            final Polygon polygon = (Polygon) splitee;
            
            final Geometry splitted = splitPolygon(polygon);

            return splitted;
        }

        /**
         * Acutally split the provided polygon.
         * <p>
         * Depending on the topology we have three options:
         * <ul>
         * <li>single polygon:
         * <li>multipolgon: provided geom was split into several parts
         * <li>null: nothing see here, please move on
         * </ul>
         * 
         * @return Ruturn a single polygon, multipolygon or null depending on how the split went.
         */
        private Geometry splitPolygon( final Polygon geom ) {
            SplitGraph graph = new SplitGraph(geom, splitter);            
            if( !graph.isSplit() ){
                if( geom.contains( splitter )){
                    // possibility of a hole
                    LinearRing ring = null;
                    GeometryFactory factory = splitter.getFactory();
                    CoordinateList list = new CoordinateList( splitter.getCoordinates() );
                    list.closeRing();
                    ring = factory.createLinearRing( list.toCoordinateArray() );
                    Polygon hole = factory.createPolygon( ring, null );                    
                    return holePolygon( geom, hole );
                }
            	return null;
            }
            final GeometryFactory gf = geom.getFactory();

            // store unsplitted holes for later addition
            List<LinearRing> unsplittedHoles = findUnsplittedHoles(graph, gf);

            List<List<SplitEdge>> allRings = findRings(graph);

            List<Polygon> resultingPolygons = buildSimplePolygons(allRings, unsplittedHoles, gf);
            List<Polygon> cleanedPolygons = new ArrayList<Polygon>();

            for( Polygon poly : resultingPolygons ){
                if( poly.isValid() ){
                	cleanedPolygons.add( poly );
                }
                else {
                	Geometry geometry = poly.buffer(0.0); // fix up splinters? often makes the geometry valid
                	for( int i=0; i< geometry.getNumGeometries(); i++){
                		Geometry part = geometry.getGeometryN(i);
                		if( part instanceof Polygon ){
                			cleanedPolygons.add( (Polygon) part );
                		}
                		else {
                           	throw new IllegalStateException("Unexpected "+part.getGeometryType()+" during split, ensure polygon is valid prior to splitting");
                		}
                	}
                }
        	}
        	
            Geometry result;
            if (cleanedPolygons.size() == 1) {
                result = cleanedPolygons.get(0);
            } else {            	
                Polygon[] array = cleanedPolygons.toArray(new Polygon[cleanedPolygons.size()]);
                result = gf.createMultiPolygon(array);
            }
            return result;
        }
        /**
         * Drill a hole in the provided polygon.
         *
         * @param geom
         * @param hole
         * @return GeometryCollection of the (usually two) resulting polygons.
         */
        private GeometryCollection holePolygon( Polygon geom, Polygon hole ) {
            GeometryFactory factory = geom.getFactory();
            Geometry difference = geom.difference( hole );
            Geometry[] geometries = new Geometry[ difference.getNumGeometries()+1 ];
            for( int i =0; i<difference.getNumGeometries(); i++){
                geometries[i] = difference.getGeometryN(i);
            }            
            geometries[ geometries.length-1] = hole;            
            return factory.createGeometryCollection(geometries);
        }

        /**
         * Finds out and removes from the graph the edges that were originally holes in the polygon
         * and were not splitted by the splitting line.
         * 
         * @param graph
         * @param gf
         * @return
         */
        @SuppressWarnings("unchecked")
        private List<LinearRing> findUnsplittedHoles( SplitGraph graph, GeometryFactory gf ) {
            final List<LinearRing> unsplittedHoles = new ArrayList<LinearRing>(2);

            final List<SplitEdge> edges = new ArrayList<SplitEdge>();
            for( Iterator it = graph.getEdgeIterator(); it.hasNext(); ) {
                SplitEdge edge = (SplitEdge) it.next();
                edges.add(edge);
            }

            for( Iterator it = edges.iterator(); it.hasNext(); ) {
                SplitEdge edge = (SplitEdge) it.next();
                if (edge.isHoleEdge()) {
                    Coordinate[] coordinates = edge.getCoordinates();
                    Coordinate start = coordinates[0];
                    Coordinate end = coordinates[coordinates.length - 1];
                    boolean isLinearRing = start.equals2D(end);
                    if (isLinearRing) {
                        graph.remove(edge);
                        LinearRing ring = gf.createLinearRing(coordinates);
                        unsplittedHoles.add(ring);
                    }
                }
            }
            return unsplittedHoles;
        }

        private List<Polygon> buildSimplePolygons( List<List<SplitEdge>> allRings,
                                                   List<LinearRing> unsplittedHoles,
                                                   GeometryFactory gf ) {

            List<Polygon> polygons = new ArrayList<Polygon>(allRings.size());

            for( List<SplitEdge> edgeList : allRings ) {
                Polygon poly = buildPolygon(edgeList, gf);
                List<LinearRing> thisPolyHoles = new ArrayList<LinearRing>(unsplittedHoles.size());
                for( LinearRing holeRing : unsplittedHoles ) {
                    if (poly.covers(holeRing)) {
                        thisPolyHoles.add(holeRing);
                    }
                }
                unsplittedHoles.removeAll(thisPolyHoles);

                int numHoles = thisPolyHoles.size();
                if (numHoles > 0) {
                    LinearRing[] holes = thisPolyHoles.toArray(new LinearRing[numHoles]);
                    LinearRing shell = gf.createLinearRing(poly.getExteriorRing().getCoordinates());
                    poly = gf.createPolygon(shell, holes);
                }

                polygons.add(poly);
            }

            return polygons;
        }

        private Polygon buildPolygon( List<SplitEdge> edgeList, GeometryFactory gf ) {
            List<Coordinate> coords = new ArrayList<Coordinate>();
            Coordinate[] lastCoordinates = null;
            for( SplitEdge edge : edgeList ) {
                Coordinate[] coordinates = edge.getCoordinates();
                if (lastCoordinates != null) {
                    Coordinate endPoint = lastCoordinates[lastCoordinates.length - 1];
                    Coordinate startPoint = coordinates[0];
                    if (!endPoint.equals2D(startPoint)) {
                        coordinates = CoordinateArrays.copyDeep(coordinates);
                        CoordinateArrays.reverse(coordinates);
                    }
                }
                lastCoordinates = coordinates;
                for( int i = 0; i < coordinates.length; i++ ) {
                    Coordinate coord = coordinates[i];
                    coords.add(coord);
                }
            }
            Coordinate[] shellCoords = new Coordinate[coords.size()];
            coords.toArray(shellCoords);
            shellCoords = CoordinateArrays.removeRepeatedPoints(shellCoords);
            LinearRing shell = gf.createLinearRing(shellCoords);
            Polygon poly = gf.createPolygon(shell, (LinearRing[]) null);
            return poly;
        }

        /**
         * Builds a list of rings from the graph's edges
         * 
         * @param graph
         * @return
         */
        @SuppressWarnings("unchecked")
        private List<List<SplitEdge>> findRings( SplitGraph graph ) {
            final List<List<SplitEdge>> rings = new ArrayList<List<SplitEdge>>();

            DirectedEdge startEdge;
            // build each ring starting with the first edge belonging to the
            // shell found
            while( (startEdge = findShellEdge(graph)) != null ) {
                List<SplitEdge> ring = buildRing(graph, startEdge);
                rings.add(ring);
            }
            return rings;
        }

        private List<SplitEdge> buildRing( final SplitGraph graph, final DirectedEdge startEdge ) {
            // System.out.println("building ring edge list...");
            final List<SplitEdge> ring = new ArrayList<SplitEdge>();

            // follow this tessellation direction while possible,
            // switch to the opposite when not, and continue with
            // the same direction while possible.
            // Start travelling clockwise, as we start with a shell edge,
            // which is in clockwise order
            final int direction = CGAlgorithms.COUNTERCLOCKWISE;

            DirectedEdge currentDirectedEdge = startEdge;
            DirectedEdge nextDirectedEdge = null;

            while( nextDirectedEdge != startEdge ) {
                SplitEdge edge = (SplitEdge) currentDirectedEdge.getEdge();
                // System.out.println("adding " + edge);
                if (ring.contains(edge)) {
                    throw new IllegalStateException("trying to add edge twice: " + edge);
                }
                ring.add(edge);

                DirectedEdge sym = currentDirectedEdge.getSym();
                SplitGraphNode endNode = (SplitGraphNode) sym.getNode();
                SplitEdgeStar nodeEdges = (SplitEdgeStar) endNode.getEdges();
                nextDirectedEdge = nodeEdges.findClosestEdgeInDirection(sym, direction);

                assert nextDirectedEdge != null;

                currentDirectedEdge = nextDirectedEdge;
            }

            removeUnneededEdges(graph, ring);
            return ring;
        }

        /**
         * Removes from <code>graph</code> the edges in <code>ring</code> that are no more
         * needed
         * 
         * @param graph
         * @param ring
         */
        private void removeUnneededEdges( final SplitGraph graph, final List<SplitEdge> ring ) {
            for( SplitEdge edge : ring ) {
                if (!edge.isInteriorEdge()) {
                    graph.remove(edge);
                }
            }

            for( SplitEdge edge : ring ) {
                if (edge.isInteriorEdge()) {
                    Node node = graph.find(edge.getCoordinate());
                    int degree = node.getEdges().getDegree();
                    if (degree < 2) {
                        graph.remove(edge);
                    }
                }
            }
        }

        /**
         * Returns the first edge found that belongs to the shell (not an interior edge, not one of
         * a hole boundary)
         * <p>
         * This method relies on shell edges being labeled {@link Location#EXTERIOR exterior} to the
         * left and {@link Location#INTERIOR interior} to the right.
         * </p>
         * 
         * @param graph
         * @return the first shell edge found, or <code>null</code> if there are no more shell
         *         edges in <code>graph</code>
         */
        private DirectedEdge findShellEdge( SplitGraph graph ) {
            Iterator it = graph.getEdgeEnds().iterator();
            DirectedEdge firstShellEdge = null;
            while( it.hasNext() ) {
                DirectedEdge de = (DirectedEdge) it.next();
                SplitEdge edge = (SplitEdge) de.getEdge();
                if (edge.isShellEdge()) {
                    firstShellEdge = de;
                    break;
                }
            }
            return firstShellEdge;
        }
    }
}
