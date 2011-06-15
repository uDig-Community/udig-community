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

import java.util.LinkedList;
import java.util.List;

import es.axios.udig.spatialoperations.internal.modelconnection.ISOCommand;
import es.axios.udig.spatialoperations.internal.modelconnection.SOCommandException;
import es.axios.udig.spatialoperations.internal.ui.parameters.ISOParamsPresenter;
import es.axios.udig.spatialoperations.ui.view.ISOPresenter;
import es.axios.udig.spatialoperations.ui.view.Message;


/**
 * Abstract behavior for controller.
 *  
 * <p>
 * This class abstracts common behaviour for controller implementations.
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public abstract class AbstractController implements ISOController {    

    // associatied objects
    private ISOPresenter             soPresenter         = null;
    private List<ISOParamsPresenter> listParamsPresenter = new LinkedList<ISOParamsPresenter>();;
    private ISOCommand               cmd                 = null;

    /** controller's state */
    private enum State {
        STOPPED, RUNNING, READY
    };
    private State state = State.READY;

    /**
     * Sets the gener spatial operation presenter (the top view)
     * @param soPresenter
     */
    public void setSpatialOperationPresenter( ISOPresenter soPresenter ){

        assert soPresenter != null;
        
        this.soPresenter = soPresenter;
        
    }

    /**
     * Adds a parameter presenter
     * @param soParamsPresenter
     */
    public void addParamsPresenter( ISOParamsPresenter soParamsPresenter ){

        assert soParamsPresenter!= null;
        
        this.listParamsPresenter.add(soParamsPresenter);

        soParamsPresenter.setController(this);
        
    }

    /**
     * Sets the command
     * @param command
     */
    public void setCommand( ISOCommand command ){
        
        assert command != null;
        this.cmd = command;
        
        
    }
    
    
    
    /**
     * Initializes the internal state of this controller and 
     * opens all associated presenters to this controller
     */
    public void shutUp(){
        
        this.state = State.READY;
        
    }
    
    public Message getMessage() {
        return this.cmd.getMessage();
    }
    public Message getDefaultMessage() {
        return getCommand().getDefaultMessage();
    }
    
    
    public void setMessage(final Message message){
        this.soPresenter.displayMessage(message);
    }

    public ISOCommand getCommand() {
        return this.cmd;
    }

    public List<ISOParamsPresenter> getParamsPresenter() {
        return this.listParamsPresenter;
    }
    
    public ISOPresenter getOperationPresenter(){
        return this.soPresenter;
    }


    
    /**
     * Validates Input values
     * @return true if inputs are OK
     */
    public boolean validate() {

        ISOCommand cmd = getCommand();
        
        boolean result = cmd.evalPrecondition();
        
        ISOPresenter soPresenter= getOperationPresenter();
        
        soPresenter.setPerformEnabled(result);
     
        soPresenter.displayMessage(cmd.getMessage());
        
        return result;
        
    }

    public void executeCommand() {

        soPresenter.setPerformEnabled(false); 

        try {
            this.cmd.execute();
            reset(); 

        } catch (SOCommandException e) {
            final Message msg = new Message(e.getMessage(), Message.Type.FAIL);
            setMessage(msg);
            return;
        }
        
        
    }


    /**
     * Resets input's command and clears input widgets of presenter
     */
    public  void reset(){
        
        this.cmd.reset(); 

        this.soPresenter.initializeInputs();
    }
    
    /**
     * Resets this control and hide the parameters presenter
     */
    public void stop(){
        
        this.state = State.STOPPED;

        for( ISOParamsPresenter presenter: this.listParamsPresenter){
            presenter.close();
        }
    }
    /**
     * Reset this control and shows the parameters presenter
     */
    public void run(){
        // To preserve the last state of ui
        if(this.state != State.RUNNING){
        
            this.state = State.RUNNING;
            
            for( ISOParamsPresenter presenter: this.listParamsPresenter){
                presenter.open();
            }
        }
    }

    public boolean isRunning() {
        return this.state == State.RUNNING;
    }
    
    public boolean isStopped(){
        return this.state == State.STOPPED;
    }
    
    public boolean isReady(){
        return this.state == State.READY;
    }
    public abstract String getOperationID();

}