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
package es.axios.udig.spatialoperations.internal.modelconnection;

import es.axios.udig.spatialoperations.ui.view.Message;

/**
 * SO Abstract Command
 * <p>
 * Abstracts the common behaviour of spatial operation commandss
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public abstract class SOAbstractCommand implements ISOCommand{

    
    protected boolean canExecute = false;

    protected Message message    = Message.NULL;
    protected Message defaultMessage = null;
    
    /**
     * @param initial_message
     */
    protected SOAbstractCommand( Message initialMessage ) {
        assert initialMessage != null;

        this.defaultMessage = initialMessage;
        this.message = this.defaultMessage;
    }

    public final Message getMessage(){
        return this.message;
    }    
    
    public Message getDefaultMessage() {
        
        return this.defaultMessage;
    }
    
    
    /**
     * Evaluates inputs data if they have errors a message error will be setted
     *
     * @return true if inputs data are ok
     */
    public boolean evalPrecondition(){
        return false;
    }
    
    


    /**
     * Execute the spatial operation if the precondition is true.
     * 
     * @throws SOCommandException 
     *
     */
    public abstract void execute() throws SOCommandException;

    /**
     * Resets the command
     *
     */
    public void reset(){
        
        message               = this.defaultMessage;
        canExecute            = false;
        
        initParameters();
    }
    
    /**
     * Initialize the parameters
     *
     */
    public abstract void initParameters();
    
    
}
