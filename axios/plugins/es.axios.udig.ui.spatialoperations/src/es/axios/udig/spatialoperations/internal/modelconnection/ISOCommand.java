package es.axios.udig.spatialoperations.internal.modelconnection;

import es.axios.udig.spatialoperations.ui.view.Message;

/**
 * 
 * Interface of Spatial Operation Commands
 * <p>
 * This interfaces define the protocol required to a client module 
 * evaluates the parameters for a spatial operations and executes it.
 * The implementations class must add setters for each parameter. 
 * Additionally, it can add getDomains methods to inform about inputs valid.  
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * 
 * @since 1.1.0
 */
public interface ISOCommand {

    /**
     * Evaluates inputs data if they have errors a message error will be setted
     *
     * @return true if inputs data are ok
     */
    boolean evalPrecondition();

    /**
     * human message
     *
     * @return a message if the precondition is false.
     */
    Message getMessage();

    /**
     * Execute the spatial operation if the precondition is true.
     * @throws SOCommandException 
     *
     */
    void execute() throws SOCommandException;

    /**
     * Initialize the parameters
     *
     */
    void reset();

    /**
     *
     * @return the default message
     */
    Message getDefaultMessage();

}