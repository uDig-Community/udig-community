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
package es.axios.udig.spatialoperations.tasks;

/**
 * This exception is produced if the spatial operation cannot reply its result.
 *  
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila   (www.axios.es)
 * 
 * @since 1.1.0
 */
public class SpatialOperationException extends Exception {

    /**
     * serial
     */
    private static final long serialVersionUID = 628919543099079360L;

    /**
     * 
     */
    public SpatialOperationException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public SpatialOperationException( String message ) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public SpatialOperationException( Throwable cause ) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public SpatialOperationException( String message, Throwable cause ) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }
}
