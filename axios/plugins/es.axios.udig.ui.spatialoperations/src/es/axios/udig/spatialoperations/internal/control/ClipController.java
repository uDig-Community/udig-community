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
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.internal.modelconnection.ClipCommand;

/**
 * Clip user interface control
 * <p>
 * This class controls the status of widgets and validate the content data.
 *
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class ClipController extends AbstractController  {
   
    
    

    @Override
    public String getOperationID() {
        return "Clip";  //$NON-NLS-1$
    }

    /**
     * Sets parameters in clip command
     *
     * @param clippingLayer
     * @param layerToClip
     * @param clippingFeatures
     * @param featuresToClip
     * @param targetLayer
     */
    public void setParameters( 
            final ILayer clippingLayer , final ILayer layerToClip,
            final FeatureCollection<SimpleFeatureType, SimpleFeature> clippingFeatures, final FeatureCollection<SimpleFeatureType, SimpleFeature>  featuresToClip, 
            final ILayer targetLayer) {

        ClipCommand clipCmd = (ClipCommand)getCommand();
        clipCmd.setParameters(
                    clippingLayer ,layerToClip, 
                    clippingFeatures, featuresToClip,
                    targetLayer);
        
    }

    /**
     * Sets parameters in clip command
     *
     * @param clippingLayer
     * @param layerToClip
     * @param clippingFeatures
     * @param featuresToClip
     * @param targetLayerName
     * @param targetCrs
     */
    public void setParameters( final ILayer clippingLayer, final ILayer layerToClip,
                               final FeatureCollection<SimpleFeatureType, SimpleFeature>  clippingFeatures, final FeatureCollection<SimpleFeatureType, SimpleFeature>  featuresToClip,
                               final String targetLayerName,
                               final CoordinateReferenceSystem targetCrs) {

        ClipCommand clipCmd = (ClipCommand) getCommand();
        clipCmd.setParameters(clippingLayer, layerToClip, 
                              clippingFeatures, featuresToClip,
                              targetLayerName,
                              targetCrs);

    }

    
}
