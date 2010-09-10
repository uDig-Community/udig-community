/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
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
package es.axios.udig.spatialoperations.internal.parameters;

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Parameters to create buffer in existent layer 
 * <p>
 *
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class BufferInExistentLayer extends AbstractBufferParameters
        implements
            IBufferInExistentLayerParameters {

    private ILayer       targetLayer       = null;
    
    /**
     * New instance of BufferInExistentLayer
     * 
     * @param sourceLayer
     * @param selectedFeatures
     * @param targetLayer
     * @param mergeGeometry
     * @param width
     * @param quadrantSegments
     * @param unit
     */
    public BufferInExistentLayer( 
            final ILayer    sourceLayer, 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures,
            final ILayer    targetLayer, 
            final Boolean   mergeGeometry, 
            final Double    width, 
            final Integer   quadrantSegments, 
            final Unit      unit ) {

        super(sourceLayer, selectedFeatures, mergeGeometry, width, quadrantSegments, unit );

        assert targetLayer != null;
        
        this.targetLayer = targetLayer;
    }
    
    

    /**
     * @return Returns the targetLayer.
     */
    public ILayer getTargetLayer() {
        return this.targetLayer;
    }





    

}
