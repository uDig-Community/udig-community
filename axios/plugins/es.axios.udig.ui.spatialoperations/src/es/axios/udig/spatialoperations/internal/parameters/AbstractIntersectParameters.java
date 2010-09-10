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

import net.refractions.udig.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *  Common parameter for intersect operation
 * <p>
 *
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
class AbstractIntersectParameters implements IIntersectParameters{

    private ILayer            firstLayer            = null;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer  = null;
    private ILayer            secondLayer           = null;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer = null;

    /**
     * Initialization of common parameters
     * 
     * @param firstLayer
     * @param featuresInFirstLayer
     * @param secondLayer
     * @param featuresInSecondLayer
     */
    public AbstractIntersectParameters( final ILayer firstLayer,
                                        final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer,
                                        final ILayer secondLayer,
                                        final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer ) {
    
        assert firstLayer != null;
        assert featuresInFirstLayer != null;
        assert secondLayer != null;
        assert featuresInSecondLayer != null;

        this.firstLayer            = firstLayer;
        this.featuresInFirstLayer  = featuresInFirstLayer;
        this.secondLayer           = secondLayer;
        this.featuresInSecondLayer = featuresInSecondLayer;
    
    }


    /**
     * @return Returns the featuresInFirstLayer.
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeaturesInFirstLayer() {
        return featuresInFirstLayer;
    }


    /**
     * @return Returns the featuresInSecondLayer.
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeaturesInSecondLayer() {
        return featuresInSecondLayer;
    }


    /**
     * @return Returns the firstLayer.
     */
    public ILayer getFirstLayer() {
        return firstLayer;
    }


    /**
     * @return Returns the secondLayer.
     */
    public ILayer getSecondLayer() {
        return secondLayer;
    }
    
}
