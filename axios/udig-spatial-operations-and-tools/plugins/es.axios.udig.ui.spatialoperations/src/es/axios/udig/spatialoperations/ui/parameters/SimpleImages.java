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

import org.eclipse.swt.graphics.Image;

/**
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.3.0
 */
public class SimpleImages extends AbstractImagesOperation implements IImageOperation {

	private String				IMAGE_SOURCE	= "";	//$NON-NLS-1$
	private String				IMAGE_RESULT	= "";	//$NON-NLS-1$
	private String				IMAGE_FINAL		= "";	//$NON-NLS-1$

	protected Image				currentImage	= null;
	protected LayersCheckState	currentState	= null;

	/**
	 * 
	 * @param clazz
	 */
	public SimpleImages(Class<?> clazz, String source_image, String result_image, String final_image) {

		super(clazz, source_image, result_image, final_image);

		assert !source_image.equals("") : "Redefine the variable and set a value"; //$NON-NLS-1$ //$NON-NLS-2$
		assert !result_image.equals("") : "Redefine the variable and set a value"; //$NON-NLS-1$ //$NON-NLS-2$
		assert !final_image.equals("") : "Redefine the variable and set a value"; //$NON-NLS-1$//$NON-NLS-2$

		this.IMAGE_SOURCE = source_image;
		this.IMAGE_RESULT = result_image;
		this.IMAGE_FINAL = final_image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.axios.udig.spatialoperations.ui.parameters.AbstractImagesOperation
	 * #createImageRegistry()
	 */
	@Override
	protected void createImageRegistry(Class<?> clazz, String source_image, String result_image, String final_image) {

		addImageToRegistry(source_image, clazz);
		addImageToRegistry(result_image, clazz);
		addImageToRegistry(final_image, clazz);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.axios.udig.spatialoperations.ui.parameters.AbstractImagesOperation
	 * #getDefaultImage()
	 */
	@Override
	public Image getDefaultImage() {

		this.currentImage = getImage(IMAGE_FINAL);
		return currentImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.axios.udig.spatialoperations.ui.parameters.AbstractImagesOperation
	 * #getImage(java.lang.Boolean, java.lang.Boolean)
	 */
	@Override
	public Image getImage(Boolean chkSource, Boolean chkResult) {
		this.currentState = (setCurrentLayerCheckState(chkSource, chkResult));
		if (this.currentState == null) {
			// no check selected = no image will display
			return null;
		}
		// correspondent image.
		switch (this.currentState) {
		case Source:
			this.currentImage = getImage(IMAGE_SOURCE);
			break;
		case Result:
			this.currentImage = getImage(IMAGE_RESULT);
			break;
		case Final:
			this.currentImage = getImage(IMAGE_FINAL);
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}
		return currentImage;
	}

}
