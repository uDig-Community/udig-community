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
package es.axios.udig.spatialoperations.internal.control;

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.internal.modelconnection.BufferCommand;

/**
 * Buffer Controller
 * <p>
 * Manages the communication between UI Content and Commands.
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)s
 * @since 1.1.0
 */
public final class BufferController extends AbstractController {



    @Override
    public String getOperationID() {
        return "Buffer"; //$NON-NLS-1$
    }

    /**
     * Sets the buffer parameters required to execute the buffer operation.
     * 
     * @param sourceLayer
     * @param sourceFeatures
     * @param targetLayer
     */
    public void setLayers( 
            final ILayer            sourceLayer, 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> sourceFeatures,
            final ILayer            targetLayer) {

        BufferCommand cmd = (BufferCommand) getCommand();
        cmd.setLayers(sourceLayer, sourceFeatures, targetLayer);
        
        
    }

    /**
     * Sets the buffer parameters required to create buffer in a new layer
     * 
     * @param sourceLayer
     * @param sourceFeatures
     * @param targetFeatureType
     * @param targetCRS
     */
    public void setLayers( final ILayer                 sourceLayer, 
                           final FeatureCollection<SimpleFeatureType, SimpleFeature>      sourceFeatures, 
                           final String                 targetFeatureType,
                           final CoordinateReferenceSystem targetCRS) {

        BufferCommand cmd = (BufferCommand) getCommand();
        cmd.setLayers(sourceLayer,sourceFeatures, targetFeatureType, targetCRS);
    }
    
    /**
     * Sets buffer options in command
     *
     * @param aggregate
     * @param with
     * @param units
     * @param cuadrantSegments
     */
    public final void setOptions(
            final Boolean   aggregate,
            final Double    with,
            final Unit    units,
            final Integer   cuadrantSegments){
        
        BufferCommand cmd = (BufferCommand) getCommand();
        cmd.setOptions(aggregate, with, units, cuadrantSegments);
    }


    

}
