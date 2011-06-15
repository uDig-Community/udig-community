/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * Abstract class implemented by all operations images.
 * 
 * Set the options and return the image. The logical must implements on
 * subclasses.
 * 
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @author Iratxe Lejarreta (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public abstract class AbstractImagesOperation implements IImageOperation {

	private ImageRegistry	registry	= new ImageRegistry();
	private ISOCommand		cmd;

	public AbstractImagesOperation() {
		createImageRegistry();
	}

	public AbstractImagesOperation(Class<?> clazz, String source_image, String result_image, String final_image) {

		assert clazz != null : "can't be null"; //$NON-NLS-1$

		createImageRegistry(clazz, source_image, result_image, final_image);
	}

	protected void createImageRegistry() {
		throw new UnsupportedOperationException("should be implemented by the subclass"); //$NON-NLS-1$
	}

	protected void createImageRegistry(Class<?> clazz, String source_image, String result_image, String final_image) {
		throw new UnsupportedOperationException("should be implemented by the subclass"); //$NON-NLS-1$
	}

	protected ImageRegistry getRegistry() {

		return registry;
	}

	public void setCommand(ISOCommand cmd) {

		assert cmd != null : "The command can not be null"; //$NON-NLS-1$

		this.cmd = cmd;
	}

	public ISOCommand getCommand() {
		return cmd;
	}

	public LayersCheckState setCurrentLayerCheckState(Boolean chkSource, Boolean chkResult) {

		assert chkSource != null;
		assert chkResult != null;

		LayersCheckState state = null;

		if (chkSource && chkResult) {

			state = LayersCheckState.Final;
		} else if (chkSource) {

			state = LayersCheckState.Source;
		} else if (chkResult) {

			state = LayersCheckState.Result;
		}

		return state;
	}

	public void addImageToRegistry(String name, Class<?> imageClass) {

		assert name != null;
		assert registry != null;
		assert imageClass != null;

		String imgFile = "images/" + name + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		this.registry.put(name, ImageDescriptor.createFromFile(imageClass, imgFile));
	}

	protected Image getImage(String key) {
		return this.getRegistry().get(key);
	}

	public abstract Image getImage(Boolean chkSource, Boolean chkResult);

	public abstract Image getDefaultImage();

}
