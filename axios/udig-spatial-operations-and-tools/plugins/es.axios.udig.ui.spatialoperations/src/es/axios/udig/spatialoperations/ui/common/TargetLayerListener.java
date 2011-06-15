/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
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
package es.axios.udig.spatialoperations.ui.common;

import com.vividsolutions.jts.geom.Geometry;

import net.refractions.udig.project.ILayer;


/**
 * Specified layer listener
 * <p>
 * Listen if an existent layer was selected or a new feature type was selected.
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
public interface TargetLayerListener {

	public void validateTargetLayer();

    /**
     * Called when an existent layer is selected
     * 
     * @param selectedLayer the layer selected
     */
    void targetLayerSelected( ILayer selectedLayer );


    /**
     * Called when a new name for the layer was especified
     * 
     * @param text
     */
    void newTargetLayerName( String text );

    /**
     * Called when creating a new layer, user select the desired geometry class.
     * @param tragetClass
     */
    void newGeometrySelected (Class < ? extends Geometry> targetClass);
}
