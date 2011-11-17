/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2010, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under General Public License (GPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package es.axios.udig.sextante.linetopolygon;

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
 * @since 1.3.0
 */
public final class LineToPolygonUIFactory implements ISOParametersPresenterFactory {

	private ISOCommand	cmd	= null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory
	 *      #createDataComposite(org.eclipse.swt.custom.ScrolledComposite, int)
	 */
	public ISOAggregatedPresenter createDataComposite(ScrolledComposite dataParent, int style) {
		return new SOLineToPolygonComposite(dataParent, style, cmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory
	 *      #createDemoImages()
	 */

	public IImageOperation createDemoImages() {
		return new LineToPolygonImages();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory
	 *      #createIcon()
	 */

	public Image createIcon() {

		final String file = "images/LineToPolygon.gif"; //$NON-NLS-1$
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(SOLineToPolygonComposite.class, file);
		Image image = descriptor.createImage();
		return image;
	}

	public void setCommand(ISOCommand cmd) {
		assert cmd != null : "Can't be null!"; //$NON-NLS-1$
		this.cmd = cmd;
	}

}
