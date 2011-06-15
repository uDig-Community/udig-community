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
package es.axios.udig.spatialoperations.ui.parameters;

import java.util.Observable;
import java.util.Observer;

import net.refractions.udig.project.ui.tool.IToolContext;
import es.axios.udig.spatialoperations.ui.view.ISOPresenter;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Interface for spatial operation's parameters presenters.
 * <p>
 * The implementation class of this interface are responsible to provide the
 * presentation for spatial operation's parameters.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */

public interface ISOParamsPresenter extends Observer {

	/**
	 * Sets the context
	 * 
	 * @param context
	 */
	void setContext(final IToolContext context);

	/**
	 * @return true if the presenter is ready to work, false in other case
	 */
	boolean wasInitialized();

	/**
	 * Opens the presenter populating its widgets.
	 */
	void open();

	/**
	 * closes the presentation
	 */
	void close();

	/**
	 * Clears inputs values
	 * 
	 */
	void clear();

	/**
	 * Shows or hides parameters's widgets
	 * 
	 * @param present
	 */
	void visible(boolean present);


	/**
	 * 
	 * @return true if this presenter is the top container for parameters
	 */
	boolean isTop();

	/**
	 * 
	 * @return the top container
	 */
	ISOParamsPresenter getTopPresenter();

	/**
	 * Set the paramsPresenter that belong this paramsPresenter.
	 * 
	 * @param presenter
	 */
	void setParentPresenter(ISOParamsPresenter presenter);

	/**
	 * Enable / Disable this presenter
	 * 
	 * @param b
	 */
	void setEnabled(boolean b);



	/**
	 * 
	 * @return The command associated to the paramsPresenter.
	 */
	public ISOCommand getCommand();

	/**
	 * 
	 * @return Default message for each paramsPresenter
	 */
	public InfoMessage getDefaultMessage();

	/**
	 * 
	 * @return Actual message, could be an error message or an info message for
	 *         show on the paramsPresenter
	 */
	public InfoMessage getMessage();

	/**
	 * 
	 * @return The spatial operation ID.
	 */
	public String getOperationID();


	/**
	 * Set message for this paramsPresenter
	 * 
	 * @param msg
	 */
	public void setMessage(final InfoMessage msg);

	/**
	 * Set the container (father) for this paramsPresenter.
	 * 
	 * @param soPresenter
	 */
	public void setSpatialOperationPresenter(ISOPresenter soPresenter);

	/**
	 * Execute the command associated for this paramsPresenter.
	 */
	public void executeCommand();

	/**
	 * Reset the values for this command and initializes the the data of this
	 * paramsPresenter.
	 */
	public void reset();

	/**
	 * After setting all the data into the command, this paramsPresenter must
	 * validate that data.
	 * 
	 * @return
	 */
	public boolean validateParameters();


	public void update(Observable o, Object arg);

}