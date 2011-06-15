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
package es.axios.udig.spatialoperations.internal.ui.parameters.buffer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;

import es.axios.udig.spatialoperations.ui.parameters.IImageOperation;
import es.axios.udig.spatialoperations.ui.parameters.ISOAggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory;

/**
 * <p>
 * Values of parameters required by the buffer operation
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
public final class BufferParametersPresenterFactory implements ISOParametersPresenterFactory {

	public ISOAggregatedPresenter createDataComposite(ScrolledComposite dataParent, int style) {

		ISOAggregatedPresenter p = new BufferComposite(dataParent, SWT.BORDER);

		return p;
	}

	public IImageOperation createDemoImages() {

		IImageOperation img = new BufferImages();

		return img;

	}

	public Image createIcon() {

		final String file = "images/Buffer.gif"; //$NON-NLS-1$

		ImageDescriptor descriptor = ImageDescriptor.createFromFile(BufferComposite.class, file);

		Image image = descriptor.createImage();

		return image;
	}


}
