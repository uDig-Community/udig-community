package es.axios.udig.spatialoperations.ui.view;

import net.refractions.udig.project.ui.tool.IToolContext;

/**
 * Interface for spatial operation presentation.
 * <p>
 * The implementation class of this interface are responsible to 
 * provide the presentation for spatial operations.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public interface ISOPresenter {

    /**
     * Sets the new context. Map deleted, added or layer list changes
     * 
     * @param newContext
     */
    void setContext(final IToolContext newContext);    
    
    
    /**
     * @param enabled    True to enable the executing of operation; false in other case.
     */
    void setPerformEnabled(final boolean enabled );

    /**
     * Sets a message to display on inforation area
     * @param message
     * @param type 
     */
    void displayMessage( final Message message);

    /**
     * Initializes the inputs values of current operation
     *
     */
    void initializeInputs();
    

}
