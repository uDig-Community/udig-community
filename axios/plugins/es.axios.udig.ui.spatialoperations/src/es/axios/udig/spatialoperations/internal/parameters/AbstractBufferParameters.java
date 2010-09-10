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
 * Common parameter for buffer operation
 * <p>
 *
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
abstract class AbstractBufferParameters implements IBufferParameters {

    private ILayer            sourceLayer      = null;

    private Boolean           mergeGeometries  = null;

    private Double            width            = null;

    private Integer           quadrantSegments = null;

    private Unit              unitsOfMeasure   = null;

    private FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures = null;
    
    /**
     * Common initialization for Buffer parameters
     * 
     * @param sourceLayer
     * @param selectedFeatures
     * @param mergeGeometry
     * @param width
     * @param quadrantSegments
     * @param unit
     */
    public AbstractBufferParameters(final ILayer sourceLayer,
                                    final FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures,
                                    final Boolean mergeGeometry,  
                                    final Double width, 
                                    final Integer quadrantSegments, 
                                    final Unit unit ) {
                                   
        assert sourceLayer != null;
        assert selectedFeatures != null;
        assert mergeGeometry != null;
        assert width != null;
        assert quadrantSegments != null;
        assert unit != null;

        this.sourceLayer = sourceLayer;
        this.selectedFeatures = selectedFeatures;
        this.mergeGeometries = mergeGeometry;
        this.width = width;
        this.quadrantSegments = quadrantSegments;
        this.unitsOfMeasure = unit;
        
    }
    
    /**
     * @return the width
     */
    public Double getWidth() {
        return width;
    }

    /**
     * @return the quedrant segments definition
     */
    public Integer getQuadrantSegments() {
        return quadrantSegments;
    }

    /**
     * @return the units
     */
    public Unit getUnitsOfMeasure() {
        return unitsOfMeasure;
    }
    
    /**
     * @return true if merge geometries is required
     */
    public boolean isMergeGeometries() {
        return mergeGeometries;
    }


    /**
     * @return Returns the sourceLayer.
     */
    public ILayer getSourceLayer() {
        return this.sourceLayer;
    }
    
    /**
     * @return selected features
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getSelectedFeatures() {
        return this.selectedFeatures ;
    }


    
}
