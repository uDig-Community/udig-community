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
package es.axios.so.extension.copy;

import org.eclipse.swt.custom.ScrolledComposite;

import es.axios.udig.spatialoperations.ui.parameters.IImageOperation;
import es.axios.udig.spatialoperations.ui.parameters.ISOAggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory;

/**
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public class CopyFactory implements ISOParametersPresenterFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory#createDataComposite(org.eclipse.swt.custom.ScrolledComposite,
	 *      int)
	 */
	public ISOAggregatedPresenter createDataComposite(ScrolledComposite dataParent, int style) {

		return new CopyComposite(dataParent, style);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory#demoImages()
	 */
	public IImageOperation createDemoImages() {

		return new CopyImages();
	}

}
