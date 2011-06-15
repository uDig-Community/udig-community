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
 * TODO Purpose of 
 * <p>
 *
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
abstract class AbstractClipParameters {
    private ILayer            clippingLayer;
    private ILayer            layerToClip;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> clippingFeatures;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToClip;

    
    public AbstractClipParameters( 
            final ILayer clippingLayer, 
            final ILayer layerToClip,
            final FeatureCollection<SimpleFeatureType, SimpleFeature> clippingFeatures,
            final FeatureCollection<SimpleFeatureType, SimpleFeature> clippedFeatures) {

        assert clippingLayer != null;
        assert layerToClip != null;
        assert clippingFeatures != null;
        assert clippedFeatures != null;
        
        this.clippingLayer =  clippingLayer;
        this.layerToClip =layerToClip;        
        
        this.clippingFeatures = clippingFeatures;
        this.featuresToClip = clippedFeatures;
        
        
    }

    /**
     * @return Returns the clippedLayer.
     */
    public ILayer getLayerToClip() {
        return layerToClip;
    }

    /**
     * @return Returns the clippingLayer.
     */
    public ILayer getClippingLayer() {
        return clippingLayer;
    }

    /**
     * @return Returns the clippedFeatures.
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeaturesToClip() {
        return featuresToClip;
    }

    /**
     * @return Returns the clippingFeatures.
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getClippingFeatures() {
        return clippingFeatures;
    }
    

    
}
