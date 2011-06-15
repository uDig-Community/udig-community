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

import es.axios.udig.spatialoperations.ui.view.ISOPresenter;
import es.axios.udig.spatialoperations.ui.view.SOComposite;
import es.axios.udig.spatialoperations.ui.view.SOView;

/**
 * Maintains the reference to the spatial operation context. This class is instanced when a new
 * spatial operation is added in the spatial operation view.
 * 
 * <p>
 * The associations between a specific spatial operation and the spatial operation 
 * framework are established when its context is created. Thus this class constructor
 * must be set with the parameters presenter ({@link ISOTopParamsPresenter}) the associated
 * command ({@link ISOCommand}) and the component shared by all spatial operation to display its
 * Name, icon, messages, etc ({@link SOView} and {@link SOComposite). 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 * @since 1.2.0
 */
public final class SOContext {

	private ISOTopParamsPresenter topPresenter;
	private ISOCommand command;
	private SOView spatialOperationView;
	private SOComposite soComposite;
	
	/**
	 * New instance of SOContext.
	 * @param soComposite 
	 * @param presenter spatial operation parameters presenter
	 * @param command	spatial operation command
	 * @param soView	spatial operation view
	 */
	public SOContext(final SOComposite soComposite, final ISOTopParamsPresenter presenter, final ISOCommand command, final SOView soView){
		
		assert soComposite != null;
		assert presenter != null;
		assert command != null;
		assert soView != null;
		
		this.soComposite = soComposite;
		this.topPresenter = presenter;
		this.command = command;
		this.spatialOperationView = soView;
		
		// associates the spatial operation components to the framework
		this.command.addObserver(this.soComposite);
		this.command.addObserver(this.spatialOperationView);
		this.command.addObserver(this.topPresenter);
		
		this.topPresenter.setCommand(this.command);
		
		ISOPresenter spatialOperationComposite = this.spatialOperationView.getSpatialOperationPresenter();
		this.topPresenter.setSpatialOperationPresenter(spatialOperationComposite);
	}

	
	public ISOTopParamsPresenter getTopPresenter() {
		return topPresenter;
	}

	public ISOCommand getCommand() {
		return command;
	}

	public SOView getSpatialOperationView() {
		return spatialOperationView;
	}
	
	public ISOPresenter getSpatialOperationComposite() {
		return this.spatialOperationView.getSpatialOperationPresenter();
	}
	
}
