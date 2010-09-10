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
package es.axios.udig.spatialoperations.internal.modelconnection;

import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IBufferParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.internal.processmanager.SOProcessException;
import es.axios.udig.spatialoperations.internal.processmanager.SOProcessManager;
import es.axios.udig.spatialoperations.ui.view.Message;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Buffer Command
 * <p>
 * Validates the inputs values for buffer operation and executes it. 
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class BufferCommand extends SOAbstractCommand {
    
    private static final Logger LOGGER              = Logger.getLogger(BufferCommand.class
                                                            .getName());

    private static final Message INITIAL_MESSAGE     = new Message(Messages.BufferCommand_create_buffer_in_target,
                                                                   Message.Type.IMPORTANT_INFO);
    private ProjectionValidator projectionValidator = new ProjectionValidator();

    // command's parameters
    private ILayer              sourceLayer;

    /** must have got a value if targetLayer is null */
    private ILayer              targetLayer;
    
    /** setted if targetLayer is not setted */
    private SimpleFeatureType               targetFeatureType;

    private Boolean                   aggregate;
    private Double                    width;
    private Unit                      unit;
    private Integer                   quadrantSegments;
    private FeatureCollection<SimpleFeatureType, SimpleFeature>         sourceFeatures;

    private String                    newLayerName;
    private CoordinateReferenceSystem targetCRS;

    
    public BufferCommand(){
        super(INITIAL_MESSAGE);
        reset();
    }
    
    /**
     * Sets the source and target layers in command
     *
     * @param sourceLayer
     * @param sourceFeatures
     * @param targetLayer
     */
    public void setLayers( 
            final ILayer            sourceLayer, 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> sourceFeatures,
            final ILayer            targetLayer ) {
    
        this.sourceLayer = sourceLayer;
        this.targetLayer = targetLayer;
        this.newLayerName = null;
        this.targetCRS = null;
        this.targetFeatureType = null;
    
        this.sourceFeatures = sourceFeatures;
    }
    

    /**
     * Sets the source and feature type of the target layer to be created.
     * 
     * @param sourceLayer       source layer
     * @param selectedFeatures  selected features
     * @param newLayer          new layer's name
     * @param targetCRS         the target CRS
     */
    public void setLayers(final ILayer sourceLayer, 
                          final FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures,  
                          final String newLayer, 
                          final CoordinateReferenceSystem targetCRS) {
        
        this.sourceLayer = sourceLayer;
        
        this.newLayerName = newLayer;
        this.targetCRS = targetCRS;
        this.targetLayer = null;
        this.targetFeatureType = null;
        
        this.sourceFeatures = selectedFeatures;
    }
    
    /**
     * Sets the options in command.
     *
     * @param aggregate
     * @param width
     * @param unit
     * @param quadrantSegments
     */
    public void setOptions(
            final Boolean           aggregate,
            final Double            width,
            final Unit              unit,
            final Integer           quadrantSegments){
        
        this.aggregate = aggregate;
        this.width = width;
        this.unit = unit;
        this.quadrantSegments = quadrantSegments;
        
    }

    /**
     * Checks buffer's parameters
     */
    @Override
    public boolean evalPrecondition() {
    
        this.canExecute = false;
        this.message = Message.NULL;

        // source and target layer must exist and be differents
        if (this.sourceLayer == null) {
            this.message = new Message (Messages.BufferCommand_select_source_layer, 
                                        Message.Type.INFORMATION);
            return this.canExecute;
        }
        
        if( (this.sourceFeatures == null)|| (this.sourceFeatures.size() == 0) ){
            this.message = new Message(Messages.BufferCommand_there_is_not_source_features, 
                                       Message.Type.ERROR);
            
            return this.canExecute;
        }

        if ((this.targetLayer == null) && 
            ((this.newLayerName == null) || (this.newLayerName.length() == 0))) {
            this.message = new Message(Messages.BufferCommand_must_select_target,
                                       Message.Type.INFORMATION);

            return this.canExecute;
        }
                
        SimpleFeatureType targetType = null;
        if((this.newLayerName != null) && this.newLayerName.length() != 0){
            
            SimpleFeatureTypeBuilder typeBuilder = GeoToolsUtils.createDefaultFeatureType(this.newLayerName, this.targetCRS);
            try {
                
                this.targetFeatureType = typeBuilder.buildFeatureType();
                targetType = this.targetFeatureType;
                
            } catch (IllegalArgumentException e) {
                this.message = new Message(Messages.BufferCommand_can_not_create_targetFeatureType,
                                           Message.Type.ERROR);
            }
            
        } else if(this.targetLayer != null ){

            if (this.sourceLayer.equals(this.targetLayer)) {
    
                this.message = new Message(Messages.BufferCommand_source_and_target_must_be_differents,
                                           Message.Type.ERROR);
    
                return this.canExecute;
            }
            
            targetType = this.targetLayer.getSchema();

        } 
        
        if (!checkTargetCompatibility(targetType) ){
            
            
            return this.canExecute;
        }
        
        if((this.width == null)||(this.width <= 0) ){
            this.message = new Message(Messages.BufferCommand_width_must_be_greater_than_cero, 
                                       Message.Type.ERROR);
            return this.canExecute;
        }
        if(this.unit == null){
            this.message = new Message(Messages.BufferCommand_must_specify_the_units, 
                                       Message.Type.INFORMATION);
            return this.canExecute;
        }
        
        boolean ok;
        try {
            CoordinateReferenceSystem sourceCrs = this.sourceLayer.getCRS();
            IMap map = this.sourceLayer.getMap();
            if(map == null)throw new IllegalStateException();
            
            CoordinateReferenceSystem mapCrs = MapUtil.getCRS(map);

            this.projectionValidator.setSourceCrs(sourceCrs);
            this.projectionValidator.setTargetCrs(mapCrs);
            ok = this.projectionValidator.validate();
            if (!ok) {
                this.message = this.projectionValidator.getMessage();
                
                return this.canExecute;
            }

        } catch (Exception e) {


            final String msg = MessageFormat.format(Messages.BufferCommand_crs_error, e.getMessage());
            
            LOGGER.info(msg);
            
            this.message = new Message(msg,
                                       Message.Type.FAIL);

            return this.canExecute;
        }

        this.canExecute = true;
        this.message = new Message(Messages.BufferCommand_parameters_ok, 
                                   Message.Type.INFORMATION);
        
        return this.canExecute;
    }
    /**
     * @return true if the geometry of target is Polygon, MultiPolygon or Geometry 
     */
    private boolean checkTargetCompatibility(final SimpleFeatureType targetType) {
        
        assert targetType != null;

        this.message = Message.NULL;
                
        GeometryDescriptor geomAttr= targetType.getDefaultGeometry();
        Class targetGeometry = geomAttr.getType().getBinding();
        
        if (  !(Polygon.class.equals(targetGeometry) || 
                MultiPolygon.class.equals(targetGeometry) || 
                Geometry.class.equals(targetGeometry))) {

            String text = MessageFormat.format(Messages.BufferCommand_geometry_type_error,
                                               Polygon.class.getSimpleName(),
                                               MultiPolygon.class.getSimpleName(),
                                               Geometry.class.getSimpleName());
            this.message = new Message(text, Message.Type.ERROR);

            return false;
        }
        
        return true;
    }

    /**
     * Exceute the buffer operation.
     * @throws SOCommandException 
     */
    @Override
    public void execute() throws SOCommandException {
        
        if(! this.canExecute ) {
            throw new SOCommandException("the precondition is false."); //$NON-NLS-1$
        }

        IBufferParameters params = null;
        if (this.targetLayer != null) { 
            // use the existent layer

            params = ParametersFactory.createBufferParameters(this.sourceLayer, this.sourceFeatures,
                                                              this.targetLayer,
                                                              this.aggregate, this.width,
                                                              this.quadrantSegments, this.unit);

        } else {// create new layer

            params = ParametersFactory.createBufferParameters(this.sourceLayer, this.sourceFeatures,
                                                              this.targetFeatureType,
                                                              this.aggregate, this.width,
                                                              this.quadrantSegments, this.unit);
        } 
        
        try {
            SOProcessManager.bufferOperation(params);
            
        } catch (SOProcessException e) {
            throw new SOCommandException(e.getMessage());
        }
        
        reset();
    }


    @Override
    public void initParameters() {


        this.sourceLayer = null;
        this.sourceFeatures = null;

        this.targetLayer = null;
        this.targetFeatureType = null;

        this.aggregate = null;
        this.width = null;
        this.unit = null;
        this.quadrantSegments = null;
    }


}
