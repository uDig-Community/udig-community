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
package es.axios.udig.spatialoperations.internal.ui.parameters.hole;

import org.eclipse.swt.graphics.Image;

import es.axios.udig.spatialoperations.ui.parameters.AbstractImagesOperation;
import es.axios.udig.spatialoperations.ui.parameters.LayersCheckState;

/**
 * A class that stores the different hole images and show the correspondent
 * image.
 * 
 * TODO for each option add getters on the command and implement the logical
 * here
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
final class HoleImages extends AbstractImagesOperation {

	private static final String	HOLE_SOURCE		= "HoleSource"; //$NON-NLS-1$
	private static final String	HOLE_RESULT		= "HoleResult"; //$NON-NLS-1$
	private static final String	HOLE_FINAL		= "HoleFinal";	//$NON-NLS-1$

	private Image				currentImage	= null;

	private LayersCheckState	currentState	= null;

	/**
	 * 
	 */
	public HoleImages() {
		super();
	}


	@Override
	public Image getImage(Boolean chkSource, Boolean chkResult) {

		this.currentState = (setCurrentLayerCheckState(chkSource, chkResult));

		if (this.currentState == null) {
			// no check selected = no image will display
			return null;
		}

		// TODO implement check for each option, and finally will return the
		// correspondent image.

		switch (this.currentState) {
		case Source:

			this.currentImage = getImage(HOLE_SOURCE);
			break;
		case Result:

			this.currentImage = getImage(HOLE_RESULT);
			break;
		case Final:

			this.currentImage = getImage(HOLE_FINAL);
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		assert currentImage != null : "currentImage couldn't be null"; //$NON-NLS-1$

		return currentImage;
	}

	@Override
	public Image getDefaultImage() {

		this.currentImage = getImage(HOLE_FINAL);
		return currentImage;
	}

	/**
	 * Creates the image registry where saves all the images.
	 * 
	 * @return
	 */
	@Override
	protected void createImageRegistry() {

		addImageToRegistry(HOLE_SOURCE,  HoleImages.class);
		addImageToRegistry(HOLE_RESULT, HoleImages.class);
		addImageToRegistry(HOLE_FINAL, HoleImages.class);
	}

}
