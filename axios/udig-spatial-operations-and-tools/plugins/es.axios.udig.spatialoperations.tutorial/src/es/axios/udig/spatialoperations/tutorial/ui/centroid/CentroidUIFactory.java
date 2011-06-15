/*
 * uDig Spatial Operations - Tutorial - http://www.axios.es (C) 2009,
 * Axios Engineering S.L. This product is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This product is distributed as part of tutorial, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.tutorial.ui.centroid;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;

import es.axios.udig.spatialoperations.ui.parameters.IImageOperation;
import es.axios.udig.spatialoperations.ui.parameters.ISOAggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory;

/**
 * 
 * This factory provides the centroid components (or products) to the spatial operation framework.
 *
 * @author Mauricio Pazos (www.axios.es)
 *
 */
public final class CentroidUIFactory implements ISOParametersPresenterFactory {

	/**
	 * Provides the composite for the centroid parameters presentation
	 */
	public ISOAggregatedPresenter createDataComposite(
			ScrolledComposite dataParent, int style) {

		return  new SOCentroidComposite(dataParent, style);
	}

	/**
	 * Provides the composite for the centroid demo image presentation
	 */
	public IImageOperation createDemoImages() {
		
		return  new CentroidImages();
	}

	/**
	 * Provides the the centroid icon
	 */
	public Image createIcon() {

		final String file =  "images/Centroid.gif"; //$NON-NLS-1$
		
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(SOCentroidComposite.class, file);
		
		Image image = descriptor.createImage(); 
			
		return image;
	}

}
