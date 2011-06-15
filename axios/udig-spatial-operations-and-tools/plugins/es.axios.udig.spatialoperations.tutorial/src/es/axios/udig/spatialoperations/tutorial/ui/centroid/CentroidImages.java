/*
 * uDig Spatial Operations - Tutorial - http://www.axios.es (C) 2009,
 * Axios Engineering S.L. This product is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This product is distributed as part of tutorial, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.tutorial.ui.centroid;

import org.eclipse.swt.graphics.Image;

import es.axios.udig.spatialoperations.ui.parameters.AbstractImagesOperation;
import es.axios.udig.spatialoperations.ui.parameters.LayersCheckState;

/**
 * Provides the images required by the demo presentation.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public class CentroidImages extends AbstractImagesOperation {

	private static final String	CENTROID_SOURCE	= "CentroidSource"; //$NON-NLS-1$
	private static final String	CENTROID_RESULT	= "CentroidResult"; //$NON-NLS-1$
	private static final String	CENTROID_FINAL	= "CentroidFinal";	//$NON-NLS-1$

	private Image				currentImage	= null;

	private LayersCheckState	currentState	= null;

	@Override
	protected void createImageRegistry() {

		addImageToRegistry(CENTROID_SOURCE, CentroidImages.class);
		addImageToRegistry(CENTROID_RESULT, CentroidImages.class);
		addImageToRegistry(CENTROID_FINAL, CentroidImages.class);
	}

	@Override
	public Image getDefaultImage() {

		this.currentImage = getImage(CENTROID_FINAL);
		return currentImage;
	}

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
			this.currentImage = getImage(CENTROID_SOURCE);
			break;
		case Result:
			this.currentImage = getImage(CENTROID_RESULT);
			break;
		case Final:
			this.currentImage = getImage(CENTROID_FINAL);
			break;
		default:
			assert false : "Invalid image option"; //$NON-NLS-1$
			break;
		}

		return currentImage;
	}
}
