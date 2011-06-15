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
package es.axios.udig.spatialoperations.ui.parameters;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;

/**
 * TODO WARNNING THIS IS A WORK IN PROGRESS
 * <p>
 * The implementation class must provide the composite required to present the
 * spatial operation parameters.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
public interface ISOParametersPresenterFactory {

	/**
	 * Provides the composite for the spatial operation parameters presentation
	 */
	public ISOAggregatedPresenter createDataComposite(final ScrolledComposite dataParent, final int style);

	/**
	 * Provides the composite for the spatial operation demo image
	 */
	public IImageOperation createDemoImages();

	/**
	 * Provides spatial operation icon
	 */
	public Image createIcon();

}