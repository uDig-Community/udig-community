/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
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

import org.eclipse.swt.graphics.Image;


/**
 * Provides the metaphor for the inputs and result of the spatial operation.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public interface IImageOperation {

	/**
	 * Depending on which layers check's are checked, {@link LayersCheckState}
	 * will set the current state.
	 * 
	 * @param chkSource
	 *            State of the source checkbox
	 * @param chkResult
	 *            State of the result checkbox
	 * @return The current layerCheckState
	 */
	LayersCheckState setCurrentLayerCheckState(Boolean chkSource, Boolean chkResult);

	/**
	 * Put the image on the image registrsy.
	 * 
	 * @param name
	 *            Image name.
	 * @param registry
	 *            The ImageRegistry that contains the images.
	 * @param imageClass
	 *            The spatial operation image class that call.
	 */
	void addImageToRegistry(String name, Class<?> imageClass);

	/**
	 * 
	 * Depending of the selected options, it will return the correspondent
	 * image.
	 * 
	 * 
	 * @param chkSource
	 *            Will show source layer
	 * @param chkResult
	 *            Will show result layer
	 * @return the image
	 */
	Image getImage(Boolean chkSource, Boolean chkResult);

	/**
	 * The default image operation.
	 * 
	 * @return The default image.
	 */
	Image getDefaultImage();

	/**
	 * Set the associated command for each ImageOperation class.
	 */
	void setCommand(ISOCommand cmd);
	
	
	ISOCommand getCommand();
	
}
