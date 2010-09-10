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
package es.axios.udig.spatialoperations.internal.parameters;

import net.refractions.udig.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * Parameters for Intersect Operation
 * <p>
 * Used this object to create the intersection in an existent layer
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class IntersectInExistenLayerParameters extends AbstractIntersectParameters implements IIntersectInExistentLayerParameters{

    private ILayer            targetLayer           = null;    
    
    

    public IntersectInExistenLayerParameters( 
            final ILayer firstLayer, final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer, 
            final ILayer secondLayer, final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer, 
            final ILayer targetLayer ) {
        
        super(firstLayer, featuresInFirstLayer, secondLayer, featuresInSecondLayer);
        
        assert targetLayer != null;    
        
        
        this.targetLayer = targetLayer;    
        
        
    }



    /**
     * @return Returns the targetLayer.
     */
    public ILayer getTargetLayer() {
        return targetLayer;
    }

}
