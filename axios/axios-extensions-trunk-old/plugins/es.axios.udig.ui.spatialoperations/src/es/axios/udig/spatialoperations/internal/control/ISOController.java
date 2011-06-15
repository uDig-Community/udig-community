/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
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
package es.axios.udig.spatialoperations.internal.control;

import java.util.List;

import es.axios.udig.spatialoperations.internal.modelconnection.ISOCommand;
import es.axios.udig.spatialoperations.internal.ui.parameters.ISOParamsPresenter;
import es.axios.udig.spatialoperations.ui.view.ISOPresenter;
import es.axios.udig.spatialoperations.ui.view.Message;

/**
 * Interface for spatial operation controllers.
 * <p>
 * This interface is an intermediate what controls the communication between 
 * Presenter and Command.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * 
 * @since 1.1.0
 */
public interface ISOController {

    /**
     * stop the controller 
     */
    void stop();

    /**
     * initialize the control to run
     */
    void run();

    /**
     * @return operation's id
     */
    String getOperationID();

    /**
     * @return true if inputs values are OK
     */
    boolean validate();

    /**
     * Dispatch user the message to the spatial operation informatio area presenter
     * @param msg
     */
    void setMessage( final Message msg );

    /**
     * @return Message indicating the state of current transaction 
     */
    Message getMessage();

   /**
    * @return the default message
    */
   Message getDefaultMessage();

   /**
     * @return Operation's presenter
     */
    ISOPresenter getOperationPresenter();
    
    /**
     * @return Parameter's presenter
     */
    List<ISOParamsPresenter> getParamsPresenter();

    /**
     * @return Command associate to this controller
     */
    ISOCommand getCommand();


    /**
     * Executes the command associated
     */
    void executeCommand();

    /**
     * Resets input's command and clears input widgets of presenter
     */
    void reset();

    /**
     * @return true if this controller is in running state, false in other case.
     */
    boolean isRunning();
    
    /**
     * @return true if this controller is in stopped state, false in other case.
     */
    boolean isStopped();

    /**
     * Initializes this controller (it will be stopped)
     */
    void shutUp();

    /**
     * Sets the gener spatial operation presenter (the top view)
     * @param soPresenter
     */
    void setSpatialOperationPresenter( ISOPresenter soPresenter );

    /**
     * Adds a parameter presenter
     * @param bufferContent
     */
    void addParamsPresenter( ISOParamsPresenter soParamsPresenter );

    /**
     * Sets the command
     * @param command
     */
    void setCommand( ISOCommand command );


    
    

}