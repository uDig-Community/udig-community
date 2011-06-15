/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
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

package es.axios.udig.spatialoperations.ui.parameters;

import java.util.List;
import java.util.Observer;

import net.refractions.udig.project.ILayer;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * 
 * Interface of Spatial Operation Commands
 * <p>
 * This interfaces define the protocol required to a client module evaluates the
 * parameters for a spatial operations and executes it. The implementations
 * class must add setters for each parameter. Additionally, it can add
 * getDomains methods to inform about inputs valid.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 * @since 1.1.0
 */
public interface ISOCommand {

	public enum CmdStatus {
		STOPPED, 
		READY, 
		EXECUTING,
	}
	

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
	InfoMessage getMessage();

	/**
	 * Execute the spatial operation if the precondition is true.
	 * 
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
	InfoMessage getDefaultMessage();

	/**
	 * 
	 * @return true if the command can be executed
	 */
	boolean canExecute();

	/**
	 * Add the observable class.
	 * 
	 * @param o
	 */
	public void addObserver(Observer o);

	/**
	 * Returns the range of valid values for the asked parameter.
	 * 
	 * @param parameterName
	 */
	public List<?> getDomainValues(final ParameterName parameterName);


	/**
	 * 
	 * @return True if the command state is stopped.
	 */
	public boolean isStopped();

	/**
	 * 
	 * @return True if the command state if activated
	 */
	public boolean isReady();

	/**
	 * 
	 * @return Target geometry class.
	 */
	public Class<? extends Geometry> getTargetLayerGeometryClass();



	/**
	 * 
	 * @return True if an existent layer is selected.
	 */
	public boolean isLayerSelected();

	/**
	 * 
	 * @return If the target layer is a new target layer.
	 */
	public boolean isNewTargetLayer();


	/**
	 * Set the command state = Active.
	 */
	public void setStatus(CmdStatus status);


	/**
	 * Only SpatialJoinGeomController will use this method, because is the only
	 * one that have selection option.
	 * 
	 * @return True if the result of the spatial operation will be shown the
	 *         result as a selected features.
	 */
	public boolean isSelection();

	/**
	 * 
	 * @return The operation ID of this spatial operation.
	 */
	public abstract String getOperationID();

	public String getOperationName();

	public String getToolTipText();
}