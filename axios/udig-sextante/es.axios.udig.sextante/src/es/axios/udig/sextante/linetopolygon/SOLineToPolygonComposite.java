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

import org.eclipse.swt.widgets.Composite;

import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.SimplePresenter;

/**
 * Right now it only extend the SimplePresenter.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.3.0
 */
public class SOLineToPolygonComposite extends SimplePresenter {

	public SOLineToPolygonComposite(Composite parent, int style, ISOCommand cmd) {
		super(parent, style, cmd);
	}

}
