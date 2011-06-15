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

import java.util.Set;

import org.eclipse.swt.widgets.Composite;

/**
 * This interface is used by the spatial operation framework to link the different implementations
 * of spatial operations.
 * <p>
 * The <b>{@link ISOBuilder#build(Composite)}</b> method will be called when the framework is constructing the spatial
 * operation presentation. The implementation class must make a set of collaborations {@link ISOParamsPresenter}}-
 * {@link ISOCommand} in the <b> {@link ISOBuilder#build(Composite)} </b> method.
 * </p>
 * <p>
 * That list of spatial operations will be provide to the spatial operation framework by 
 * the <b> {@link ISOBuilder#getResult()}</b> method.
 * </p>
 * 
 * 
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
public interface ISOBuilder {

	/**
	 * Makes the spatial operation parameter presenter and command to execute the spatial operation
	 * 
	 * @param parentComposite container provided to display the spatial operations composite
	 */
	public void build(final Composite parentComposite);

	/**
	 * Provides the list of spatial operation made in the {@link ISOBuilder#build(Composite)}.
	 * 
	 * @return a set of {@link ISOParamsPresenter}
	 */
	public Set<Object[]> getResult();

}