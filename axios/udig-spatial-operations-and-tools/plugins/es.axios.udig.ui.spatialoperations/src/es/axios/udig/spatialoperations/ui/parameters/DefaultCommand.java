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

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.descriptor.ISODescriptor;
import es.axios.udig.spatialoperations.descriptor.ISOParameterType;
import es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Provide default implementation for spatial operation command
 * 
 * TODO WARNNING THIS IS A WORK IN PROGRESS
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
public class DefaultCommand extends SpatialOperationCommand {

	private static InfoMessage	INITIAL_MESSAGE	= null;

	private ISOTaskManager		taskManager;

	protected DefaultCommand(final ISOTaskManager taskManager) {
		super(INITIAL_MESSAGE);

		assert taskManager != null : "taskManager can not be null"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand#initParameters()
	 */
	@Override
	public void initParameters() {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns the set of domain values for the parameter name
	 * 
	 * @param parameterName
	 * 
	 * @return the set of domain values
	 */
	@Override
	public List<?> getDomainValues(final ParameterName parameterName) {

		ISODescriptor descriptor = this.taskManager.getSpatialOperationDescriptor(this.getOperationID());

		ISOParameterType type = descriptor.getType(parameterName);
		assert !type.isEnum() : "the type for the parameter " + parameterName + " should be defined by enumeration"; //$NON-NLS-1$ //$NON-NLS-2$

		return type.getDomainValues();
	}

	public Class<?> getDomainType(final String parameterName) {

		return null;
	}

	public Object getParameterValue(final String parameterName) {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand#executeOperation()
	 */
	@Override
	public void executeOperation() throws SOCommandException {
		// TODO Auto-generated method stub

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see es.axios.udig.spatialoperations.ui.parameters.ISOCommand#getOperationID()
	 */
	public String getOperationID() {
		// TODO Auto-generated method stub
		return "id undefined";
	}

	public String getOperationName() {
		return "operation name undefined";
	}

	public String getToolTipText() {
		return "tool tip undefined";
	}

	@Override
	protected Object[] getValidTargetLayerGeometries() {
		// TODO Auto-generated method stub
		return new Object[0];
	}

	@Override
	protected Object[] getSourceGeomertyClass() {
		// TODO Auto-generated method stub
		return new Object[0];
	}

	@Override
	protected boolean validateParameters() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setTargetLayerGeometry(Class<? extends Geometry> targetClass) {
		// TODO Auto-generated method stub
		
	}

}
