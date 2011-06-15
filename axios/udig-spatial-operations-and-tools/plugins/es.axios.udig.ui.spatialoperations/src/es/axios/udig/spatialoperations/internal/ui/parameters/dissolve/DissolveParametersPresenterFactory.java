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
package es.axios.udig.spatialoperations.internal.ui.parameters.dissolve;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;

import es.axios.udig.spatialoperations.ui.parameters.IImageOperation;
import es.axios.udig.spatialoperations.ui.parameters.ISOAggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory;

/**
 * TODO WARNNING THIS IS A WORK IN PROGRESS
 * <p>
 * TODO COMMENT ME!
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
public final class DissolveParametersPresenterFactory implements ISOParametersPresenterFactory {

	public ISOAggregatedPresenter createDataComposite(ScrolledComposite dataParent, int style) {
		ISOAggregatedPresenter p = new DissolveComposite(dataParent, style);
		return p;
	}

	public IImageOperation createDemoImages() {
		IImageOperation img = new DissolveImages();
		return img;
	}

	public Image createIcon() {

		final String file = "images/dissolve.gif"; //$NON-NLS-1$

		ImageDescriptor descriptor = ImageDescriptor.createFromFile(DissolveComposite.class, file);

		Image image = descriptor.createImage();

		return image;
	}

}
