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
package net.refractions.linecleaner;

import java.util.Collection;
import java.util.Stack;

import net.refractions.linecleaner.GeometryUtil.CoordinateFunction;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * A simple line class.
 * </p>
 */
public class Line {
    Coordinate start;
    Coordinate end;
    
    /**
     * @param p starting coordinate
     * @param q ending coordinate
     */
    public Line(Coordinate p, Coordinate q) {
        this.start = p;
        this.end = q;
    }
    
    /**
     *
     * @return length of the line
     */
    public double getLength() {
        return this.start.distance(this.end);
    }
    
    /**
     *
     * @param distance distance along the line for the returned point
     * @return point that is some distance along the line
     */
    public Coordinate along(double distance) {
        Coordinate p;
        if (distance == 0) {
            p = this.start;
        } else {
            double percent = distance / this.getLength();        
            p = new Coordinate( this.start.x + (this.end.x - this.start.x) * percent,
                    this.start.y + (this.end.y - this.start.y) * percent);
        }
        return p;
    }
    
    /**
     * Construct a segment of a line starting at startDistance extending to 
     * the end of this line.
     * @param startDistance
     * @return A new line representing the segment.
     */
    public Line subLine(double startDistance) {
        return new Line(this.along(startDistance), this.end);
    }
    
    /**
     * Subdivide a line into intervals of intervalLength, starting at startDistance down
     * the line.
     * @param intervalLength length of subdivisions
     * @param startDistance distance along the line to start subdividing from; an offset
     * @return A collection of coordinates indicating the points that subdivide the line.
     */
    public Collection<Coordinate> subdivide(double intervalLength, double startDistance) {
        Stack<Coordinate> result = new Stack<Coordinate>();
        
        Line line = subLine(startDistance);
        result.push(line.start);
        int limit = (int) (line.getLength() / intervalLength);
        for (int i = 0; i < limit; i++) {
            Coordinate p = line.along(intervalLength);
            result.push(p);
            line = new Line(p, this.end);
        }
        return result;
    }
    
    public int subdivide(double intervalLength, double startDistance, CoordinateFunction f) {
        Line line = subLine(startDistance);
        f.run(line.start);
        int limit = (int) (line.getLength() / intervalLength);
        for (int i = 0; i < limit; i++) {
            Coordinate p = line.along(intervalLength);
            f.run(p);
            line = new Line(p, this.end);
        }
        return limit+1;
    }
    
    /**
     * @return Returns the end.
     */
    public Coordinate getEnd() {
        return this.end;
    }
    
    /**
     * @param end The end to set.
     */
    public void setEnd( Coordinate end ) {
        this.end = end;
    }
    
    /**
     * @return Returns the start.
     */
    public Coordinate getStart() {
        return this.start;
    }
    
    /**
     * @param start The start to set.
     */
    public void setStart( Coordinate start ) {
        this.start = start;
    }
}
