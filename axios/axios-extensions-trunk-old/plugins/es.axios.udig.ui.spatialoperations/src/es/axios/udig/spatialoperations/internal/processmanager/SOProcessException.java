/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
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
package es.axios.udig.spatialoperations.internal.processmanager;

import java.lang.reflect.InvocationTargetException;

/**
 * Spatial Operation Exception 
 * <p>
 * This exception is produced if the spatial operation process fail.
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class SOProcessException extends InvocationTargetException {

    /** long serialVersionUID field */
    private static final long serialVersionUID = 122066540027328422L;
    private String            message              = ""; //$NON-NLS-1$
    private Throwable         cause            = null;
    
    SOProcessException(String msg){
        
        this.message = msg;
    }
    

    public SOProcessException( String msg, Throwable cause ) {
        
        this.message = msg;
        this.cause = cause;
    }


    /**
     * @return Returns the message.
     */
    @Override
    public String getMessage() {
        return this.message;
    }


    @Override
    public Throwable getCause() {
        return this.cause;
    }
    
    
    
    
    
    
    

}
