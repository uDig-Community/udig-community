package es.axios.udig.spatialoperations.ui.view;

import net.refractions.udig.project.ui.tool.IToolContext;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Interface for spatial operation presentation.
 * <p>
 * The implementation class of this interface are responsible to provide the
 * presentation for spatial operations.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
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
	 * Sets a message to display on inforation area
	 * 
	 * @param message
	 * @param type
	 */
	void displayMessage(final InfoMessage message);

	/**
	 * Initializes the inputs values of current operation
	 * 
	 */
	void initializeInputs();

	/**
	 * Enables / disables the widgets
	 * 
	 * @param boolValue
	 *            true to enable the presenter, false in other case
	 */
	void setEnabled(boolean boolValue);

	void open();

	/**
	 * Execute the run button action.
	 */
	void executeOperation();

	/**
	 * Execute the show/hide operation. Will show/hide the demo composite.
	 */
	void switchShowHide();

}
