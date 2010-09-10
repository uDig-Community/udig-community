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


/**
 * Thrown when it is unable to generate TIN.
 *
 * @since 2.4
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/referencing/src/main/java/org/geotools/referencing/operation/builder/algorithm/TriangulationException.java $
 * @version $Id: TriangulationException.java 24173 2007-02-05 13:33:12Z jezekjan $
 * @author Jan Jezek
 */
public class TriangulationException extends Exception {
    private static final long serialVersionUID = -3134565178815225915L;

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param  message The cause for this exception. The cause is saved
     *         for later retrieval by the {@link #getCause()} method.
     */
    public TriangulationException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with no detail message.
     */
    public TriangulationException() {
        super();
    }
}
