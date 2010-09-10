/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.operation.builder.algorithm;

import org.geotools.geometry.DirectPosition2D;
import org.opengis.spatialschema.geometry.DirectPosition;



/**
 * DirectPosition associated with another DirectPosition.
 *
 * @since 2.4
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/operation/builder/algorithm/ExtendedPosition.java $
 * @version $Id: ExtendedPosition.java 24925 2007-03-27 20:12:08Z jgarnett $
 * @author Jan Jezek
 */
class ExtendedPosition extends DirectPosition2D {
    /**  */
    private static final long serialVersionUID = 4400395722009854165L;

    /** Coordinate associated with original coordinate. */
    private DirectPosition mappedposition;

    /**
     * Creates a MappedPosition
     * @param c the original DirectPosition.
     * @param mappedposition the associated DirectPosition.
     */
    public ExtendedPosition(DirectPosition c, DirectPosition mappedposition) {
        super(c);
        this.mappedposition = mappedposition;
    }

    /**
     * Returns the mapped DirectPosition.
     *
     * @return this coordinate's associated coordinate
     */
    public DirectPosition getMappedposition() {
        return mappedposition;
    }

    /**
     * Sets the mapped DirectPosition.
     *
     * @param mappedCoordinate Coordinate to be mapped to the existing one.
     */
    public void setMappedposition(DirectPosition mappedCoordinate) {
        this.mappedposition = mappedCoordinate;
    }
}
