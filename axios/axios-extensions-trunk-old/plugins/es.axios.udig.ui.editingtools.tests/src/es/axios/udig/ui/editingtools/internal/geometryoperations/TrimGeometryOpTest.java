/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.ui.editingtools.internal.geometryoperations;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import es.axios.udig.ui.editingtools.internal.geometryoperations.TrimGeometryStrategy;

/**
 * Test suite for {@link TrimGeometryStrategy}
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class TrimGeometryOpTest extends TestCase {

    private static final GeometryFactory GF = new GeometryFactory();

    /**
     * Line used as cutting edge digitized clockwise. <code>LINESTRING(1 0, 1 10)</code>
     */
    private LineString                   clockwiseTrimLine;
    /**
     * Line used as cutting edge digitized counter clockwise. <code>LINESTRING(1 10, 1 0)</code>
     */
    private LineString                   coutnerClockwiseTrimLine;

    private TrimGeometryStrategy         trimOp;

    protected void setUp() throws Exception {
        super.setUp();
        Coordinate[] clockwiseCoords = {new Coordinate(1, 0), new Coordinate(1, 10)};
        Coordinate[] counterClockwiseCoords = {new Coordinate(1, 10), new Coordinate(1, 0)};
        clockwiseTrimLine = GF.createLineString(clockwiseCoords);
        coutnerClockwiseTrimLine = GF.createLineString(counterClockwiseCoords);
        trimOp = new TrimGeometryStrategy(clockwiseTrimLine);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        clockwiseTrimLine = null;
        trimOp = null;
    }

    /**
     * Result of <code>null</code> and empty input geometries is identity
     */
    public void testNullAndEmptyInputs() {
        Geometry input = null;
        Geometry trimmed = trimOp.trim(input);
        assertNull(trimmed);

        input = GF.createLinearRing(new Coordinate[0]);
        assertTrue(input.isEmpty());

        trimmed = trimOp.trim(input);
        assertSame(input, trimmed);
    }

    /**
     * Only LineString and MultiLineString are valid inputs
     */
    public void testValidatePreconditionInputGeometryType() {
        Geometry input = GF.createPoint(new Coordinate(0, 0));
        try {
            trimOp.trim(input);
            fail("Expected IAE on non line input");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    /**
     * The trimming line should intersect the input linestring at a single point
     */
    public void testValidatePreconditionIntersection() {
        Coordinate[] coordinates = {new Coordinate(2, 1), new Coordinate(-1, 1),
                new Coordinate(2, 2)};
        LineString intersectsAtTwoPoints = GF.createLineString(coordinates);

        try {
            trimOp.trim(intersectsAtTwoPoints);
            fail("Expected IAE on input with more than one intersection point");
        } catch (IllegalArgumentException e) {
            // OK
        }
    }

    public void testTrimLineString() throws ParseException {
        WKTReader reader = new WKTReader();
        LineString input = (LineString) reader.read("LINESTRING(-1 1, 2 1)");

        LineString leftPart = (LineString) reader.read("LINESTRING(-1 1, 1 1)");
        LineString rightPart = (LineString) reader.read("LINESTRING(1 1, 2 1)");

        Geometry cwResult = TrimGeometryStrategy.trim(input, clockwiseTrimLine);
        Geometry ccwResult = TrimGeometryStrategy.trim(input, coutnerClockwiseTrimLine);

        assertTrue(cwResult instanceof LineString);
        assertTrue(ccwResult instanceof LineString);

        assertTrue("expected " + leftPart + ", got " + cwResult, leftPart.equalsExact(cwResult));
        assertTrue("expected " + rightPart + ", got " + ccwResult, rightPart.equalsExact(ccwResult));
    }

    /**
     * {@link TrimGeometryStrategy} cuts the input geometries at the right of the trimming line, and
     * returns the remaining at the left.
     * 
     * @throws ParseException
     */
    public void testTrimMultiLineString() throws ParseException {
        WKTReader reader = new WKTReader();
        String inputWkt = "MULTILINESTRING((2 1, -1 1),(-1 -1, -2 -2))";
        MultiLineString input = (MultiLineString) reader.read(inputWkt);

        String leftPartWkt = "MULTILINESTRING((1 1, -1 1),(-1 -1, -2 -2))";
        final MultiLineString leftPart = (MultiLineString) reader.read(leftPartWkt);

        String rightPartWkt = "MULTILINESTRING((2 1, 1 1),(-1 -1, -2 -2))";
        final MultiLineString rightPart = (MultiLineString) reader.read(rightPartWkt);

        Geometry cwResult = TrimGeometryStrategy.trim(input, clockwiseTrimLine);
        Geometry ccwResult = TrimGeometryStrategy.trim(input, coutnerClockwiseTrimLine);

        assertTrue(cwResult instanceof MultiLineString);
        assertTrue(ccwResult instanceof MultiLineString);

        assertTrue("expected " + leftPart + ", was " + cwResult, leftPart.equalsExact(cwResult));
        assertTrue("expected " + rightPart + ", was " + ccwResult, rightPart.equalsExact(ccwResult));
    }

    /**
     * If the trimming line touches the input line but does not crosses it, trimOp has to return an
     * empty geom or the input geom depending on the direction
     * 
     * @throws ParseException
     */
    public void testTrimAndInputTouchButNotCross() throws ParseException {
        WKTReader reader = new WKTReader();
        // line at the right of the clockwise trimming line, touches it at 1 1
        LineString input = (LineString) reader.read("LINESTRING(1 1, 2 1)");

        final Geometry leftPart = reader.read("LINESTRING EMPTY");
        final LineString rightPart = (LineString) reader.read("LINESTRING(1 1, 2 1)");

        Geometry cwResult = TrimGeometryStrategy.trim(input, clockwiseTrimLine);
        Geometry ccwResult = TrimGeometryStrategy.trim(input, coutnerClockwiseTrimLine);

        assertNotNull(cwResult);
        assertTrue(cwResult.toString(), cwResult.isEmpty());
        assertTrue(ccwResult instanceof LineString);

        assertTrue("expected " + leftPart + ", got " + cwResult, leftPart.equalsExact(cwResult));
        assertTrue("expected " + rightPart + ", got " + ccwResult, rightPart.equalsExact(ccwResult));
    }
}
