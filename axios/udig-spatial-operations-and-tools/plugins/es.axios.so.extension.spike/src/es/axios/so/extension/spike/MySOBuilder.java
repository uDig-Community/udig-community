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
package es.axios.so.extension.spike;

import org.eclipse.swt.widgets.Composite;

import es.axios.udig.spatialoperations.ui.parameters.AbstractSpatialOperationBuilder;

/**
 * Test for extension point
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
public class MySOBuilder extends AbstractSpatialOperationBuilder {

	@Override
	public void build(Composite container) {
		
		addCollaboration(container, 
						new SO1ParametersFactory(),
						new SO1Command());

	}

}
