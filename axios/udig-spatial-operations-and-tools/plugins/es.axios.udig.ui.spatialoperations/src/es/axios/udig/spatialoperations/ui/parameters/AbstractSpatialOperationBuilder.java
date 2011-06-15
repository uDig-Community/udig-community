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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract Spatial Operation Builder
 * <p>
 * Provide Default methods to build the list of spatial operation components
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
public abstract class AbstractSpatialOperationBuilder implements ISOBuilder {

	private Set<Object[]>	soList	= null;

	/**
	 * Subclass should create the list of spatial operation components.
	 * 
	 * @see es.axios.udig.spatialoperations.ui.parameters.ISOBuilder#build(org.eclipse.swt.widgets.Composite)
	 */
	public abstract void build(Composite parentComposite);

	/**
	 * Associates the spatial operation presenter and its command.
	 * <p>
	 * This method should be called by the build method in order to create a new
	 * collaboration of spatial operation components. The collaboration is added
	 * to the list of spatial operation collaborations
	 * </p>
	 * 
	 * @param soPresenter
	 *            spatial operation presenter
	 * @param soCommand
	 *            spatial operation command
	 * @return {@link ISOParamsPresenter}
	 */
	protected void addCollaboration(Composite parent,
									ISOParametersPresenterFactory soPresenterFactory,
									ISOCommand soCommand) {

		//soPresenterFactory.setCommand(soCommand);		
		ISOTopParamsPresenter soPresenter = new TopParametersPresenter(parent, soPresenterFactory, SWT.NONE);

		if (soList == null) {
			soList = new LinkedHashSet<Object[]>();
		}
		Object[] components = new Object[2];

		components[0] = soPresenter;
		components[1] = soCommand;

		soList.add(components);
	}

	/**
	 * @returns a Set of spatial operation components
	 * @see es.axios.udig.spatialoperations.ui.parameters.ISOBuilder#getResult()
	 */
	public Set<Object[]> getResult() {

		assert soList != null : "the spatial operation components was not built"; //$NON-NLS-1$

		return soList;

	}

}
