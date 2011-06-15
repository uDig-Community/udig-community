package es.axios.udig.spatialoperations.internal.parameters;

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Encapsulates the parameters for a buffer operation
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class BufferInNewLayerParameters extends AbstractBufferParameters
        implements
            IBufferInNewLayerParameters {



    private SimpleFeatureType  targetFeatureType = null;

    /**
     * New instance of BufferInNewLayerParameters
     * 
     * @param sourceLayer
     * @param selectedFeatures
     * @param newFeatureType
     * @param mergeGeometry
     * @param width
     * @param quadrantSegments
     * @param unit
     */
    public BufferInNewLayerParameters( 
            final ILayer sourceLayer, 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures,
            final SimpleFeatureType newFeatureType,
            final Boolean mergeGeometry,  
            final Double width, 
            final Integer quadrantSegments, 
            final Unit unit ) {

        super(sourceLayer, selectedFeatures, mergeGeometry, width, quadrantSegments, unit );
        
        assert newFeatureType != null;
        
        
        this.targetFeatureType = newFeatureType;
    }
    


    /**
     * @return the SimpleFeature Type of the new layer
     */
    public final SimpleFeatureType getTargetFeatureType() {
        return targetFeatureType;
    }




}
