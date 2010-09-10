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
package es.axios.udig.spatialoperations.internal.control;

import net.refractions.udig.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import es.axios.udig.spatialoperations.internal.modelconnection.IntersectCommand;

/**
 * Intersect Controller
 * <p>
 * Manages the collaborations between  the intersect 
 * presentation and its command
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class IntersectController extends AbstractController{


    

    @Override
    public String getOperationID() {
        return "Intersect";  //$NON-NLS-1$
    }

    /**
     * Sets Intersect command's parameters 
     *
     * @param firstLayer
     * @param featuresInFirstLayer
     * @param secondLayer
     * @param featuresInSecondLayer
     * @param targetLayer
     */
    public void setParameters( 
            ILayer firstLayer, FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer,
            ILayer secondLayer, FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer,
            ILayer targetLayer ) {
        
        IntersectCommand cmd = (IntersectCommand)getCommand();
        cmd.setParameters(
                firstLayer, featuresInFirstLayer,
                secondLayer, featuresInSecondLayer,
                targetLayer );
        
    }
    
    /**
     * Sets intersect command's parameters 
     *
     * @param firstLayer
     * @param featuresInFirstLayer
     * @param secondLayer
     * @param featuresInSecondLayer
     * @param targetLayer
     * @param targetCrs
     */
    public void setParameters( 
            ILayer firstLayer, FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer,
            ILayer secondLayer, FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer,
            SimpleFeatureType targetType) {
        
        IntersectCommand cmd = (IntersectCommand)getCommand();
        cmd.setParameters(
                firstLayer, featuresInFirstLayer,
                secondLayer, featuresInSecondLayer,
                targetType);
        
    }


}
