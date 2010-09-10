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

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;



/**
 * Factory for Parameters used by in spatial operations.
 * <p>
 * This factory produces the object used to transfer 
 * spatial operation parameters (object value).
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class ParametersFactory {
    
    private ParametersFactory(){
        // there is not instance of this class
    }
    
    /**
     * Parameters required to make the buffer on target layer
     *
     * @param sourceLayer
     * @param targetFeatureType
     * @param mergeGeometry
     * @param with
     * @param quadrantSegments
     * @param unit
     * @return buffer parameters to make buffer in new targset layer
     */
    public final static IBufferInNewLayerParameters createBufferParameters(
                final ILayer sourceLayer,
                final FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures,
                final SimpleFeatureType targetFeatureType,
                final Boolean mergeGeometry,
                final Double with,
                final Integer quadrantSegments,
                final Unit unit
            ){
        
        BufferInNewLayerParameters params = new BufferInNewLayerParameters(
                                        sourceLayer, selectedFeatures,
                                        targetFeatureType,
                                        mergeGeometry, 
                                        with, quadrantSegments, unit);
        
        
        return params; 
        
    }
    
    /**
     * Parameters required to create  buffer on new layer
     *
     * @param sourceLayer
     * @param targetLayer
     * @param mergeGeometry
     * @param with
     * @param quadrantSegments
     * @param unit
     * @return buffer paremeters into the target layer
     */
    public final static IBufferInExistentLayerParameters createBufferParameters(
            final ILayer sourceLayer,
            final FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures,
            final ILayer targetLayer,
            final Boolean mergeGeometry,
            final Double with,
            final Integer quadrantSegments,
            final Unit unit
        ){
    
        IBufferInExistentLayerParameters params = new BufferInExistentLayer(
                sourceLayer, selectedFeatures,
                targetLayer, mergeGeometry,
                with, quadrantSegments, unit);

        return params;
    }
    
    /**
     * New instance of intersect parameters
     * 
     * @param firstLayer
     * @param featuresInFirstLayer
     * @param secondLayer
     * @param featuresInSecondLayer
     * @param targetFeatureType
     * @return IIntersectParameters
     */
    public static IIntersectInNewLayerParameters createIntersectParameters( 
            final ILayer firstLayer, 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer, 
            final ILayer secondLayer, 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer, 
            final SimpleFeatureType targetFeatureType ) {

        
        
        IIntersectInNewLayerParameters params =  new IntersectInNewLayerParameters(
                firstLayer, featuresInFirstLayer,
                secondLayer, featuresInSecondLayer,
                targetFeatureType);
        
        return params;
    }

    /**
     * New instance of clip parameters
     *
     * @param clippingLayer
     * @param layerToClip
     * @param clippingFeatures
     * @param featuresToClip
     * @param targetLayer 
     * @return IClipParameters
     */
    public final static IClipInExistentLayerParameters createClipParameters(
            final ILayer clippingLayer,
            final ILayer layerToClip, 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> clippingFeatures,
            final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToClip, ILayer targetLayer){
        
        IClipInExistentLayerParameters params = new ClipInExistentLayerParameters(
                            clippingLayer,
                            layerToClip,
                            clippingFeatures,
                            featuresToClip,
                            targetLayer);
        
        return params; 
        
    }

    /**
     *
     * @param clippingLayer
     * @param layerToClip
     * @param clippingFeatures
     * @param featuresToClip
     * @param targetFeatureType
     * @return
     */
    public final static IClipInNewLayerParameters createClipParameters(
                                                              final ILayer clippingLayer,
                                                              final ILayer layerToClip,
                                                              final FeatureCollection<SimpleFeatureType, SimpleFeature> clippingFeatures,
                                                              final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToClip,
                                                              final SimpleFeatureType targetFeatureType ) {

        IClipInNewLayerParameters params = new ClipInNewLayerParameters(clippingLayer, layerToClip, clippingFeatures,
                                                    featuresToClip, targetFeatureType);

        return params;
    }

    /**
     * New instance of intersect parameters
     * 
     * @param firstLayer
     * @param featuresInFirstLayer
     * @param secondLayer
     * @param featuresInSecondLayer
     * @param targetLayer
     * @return IIntersectParameters
     */
    public static IIntersectInExistentLayerParameters createIntersectParameters( 
            final ILayer firstLayer, 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer, 
            final ILayer secondLayer, 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer, 
            final ILayer targetLayer ) {
    
        
        
        IIntersectInExistentLayerParameters params =  new IntersectInExistenLayerParameters(
                firstLayer, featuresInFirstLayer,
                secondLayer, featuresInSecondLayer,
                targetLayer);
        
        return params;
    }


}
