/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
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

import net.refractions.udig.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IClipParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.internal.processmanager.SOProcessException;
import es.axios.udig.spatialoperations.internal.processmanager.SOProcessManager;
import es.axios.udig.spatialoperations.ui.view.Message;
import es.axios.udig.ui.commons.util.GeoToolsUtils;

/**
 * Command executing clip operation
 * <p>
 * Validates data input and executes the clip operation
 * The Client module must set the input data and  evaluate 
 * precondition before execute the command
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class ClipCommand extends SOAbstractCommand {

    private static final Message INITIAL_MESSAGE  = new Message(Messages.ClipCommand_clip_description,
                                                                Message.Type.IMPORTANT_INFO);

    // Collaborators
    private GeometryCompatibilityValidator geomValidator = new GeometryCompatibilityValidator();

    // inputs values
    private ILayer                         clippingLayer    = null;
    private ILayer                         layerToClip      = null;

    private FeatureCollection<SimpleFeatureType, SimpleFeature>              featuresToClip   = null;
    private FeatureCollection<SimpleFeatureType, SimpleFeature>              clippingFeatures = null;

    private ILayer                         targetLayer      = null;
    private String                         targetLayerName  = null;
    private CoordinateReferenceSystem      targetCrs        = null;
    
    private SimpleFeatureType targetFeatureType;

    /**
     * New instnce of ClipCommand
     *
     */
    public ClipCommand(){
        super(INITIAL_MESSAGE);
        reset();
    }
    
    /**
     * Evaluates inputs data, if they have errors message error will be setted.
     * 
     * @return true if inputs data are ok
     */
    @Override
    public boolean evalPrecondition(){
     
        this.message = Message.NULL;
        this.canExecute = true;
        
        // clipping and cliped layer must be differents
        if(this.layerToClip == null){
            this.message =  new Message(Messages.ClipCommand_must_select_clipped_layer, 
                                        Message.Type.INFORMATION);
            this.canExecute = false;
        }else if(this.clippingLayer== null){
            this.message = new Message(Messages.ClipCommand_must_select_clipping_layer, 
                                       Message.Type.INFORMATION);
            
            this.canExecute = false;
            
        }else  if(this.layerToClip.equals(this.clippingLayer)){
            this.message = new Message(Messages.ClipCommand_clipping_and_clipped_must_be_differents,
                                       Message.Type.ERROR);
            
            this.canExecute = false;
        } else if( (this.clippingFeatures.size() == 0) ){
            
            this.message = new Message(Messages.ClipCommand_there_are_not_features_in_clipping_layer, 
                                       Message.Type.ERROR);
            
            this.canExecute = false;
        } else if( (this.featuresToClip.size() == 0) ){
            
            this.message = new Message(Messages.ClipCommand_there_are_not_features_to_clip, 
                                       Message.Type.ERROR);
            
            this.canExecute = false;
        } else if(!validTarget()){
            
            this.canExecute = false;
            
        }else if((this.layerToClip!= null) && (this.targetLayer!=null) ){
            if(this.layerToClip.equals(this.targetLayer)){
            
                final String msg = MessageFormat.format(Messages.ClipCommand_will_clip_existent_layer, this.layerToClip.getName());
                this.message = new Message(msg, Message.Type.WARNING);
                this.canExecute = true; // this warning allow to execute the operation
            }
        }
        if(this.canExecute){

            // if there is a message setted it will be maintained else
            // set de ok message as default.
            if( this.message == Message.NULL ){

                this.message = new Message(Messages.ClipCommand_parameters_ok,Message.Type.INFORMATION);
            }
        }
        return this.canExecute;
    }
    
    /**
     * Valid the target layer parameter
     * @return
     */
    private boolean validTarget() {
        
        this.message = Message.NULL;
        
        if ((this.targetLayerName == null) && (this.targetLayer == null)) {

            this.message = new Message(Messages.ClipCommand_must_select_result,
                                       Message.Type.INFORMATION);
            return false;

        } else if( (this.targetLayer != null) && (this.clippingLayer!= null) ){
            
            if( this.targetLayer.equals(this.clippingLayer) ){
            
                this.message = new Message(Messages.ClipCommand_clipping_and_result_must_be_differents,
                                           Message.Type.ERROR);
                return false;
            }
            
        } else if((this.targetLayerName != null) && this.targetLayerName.length() > 0){
            
            //FIXME HACK then map's crs should be a command's parameter
            this.targetCrs = this.layerToClip.getMap().getViewportModel().getCRS();
            assert this.targetCrs != null; 
            //end hack
            
            SimpleFeatureTypeBuilder typeBuilder = GeoToolsUtils.createDefaultFeatureType(this.targetLayerName, this.targetCrs);
            try {
                
                this.targetFeatureType = typeBuilder.buildFeatureType();
                
            } catch (IllegalArgumentException e) {
                
                this.message = new Message(Messages.BufferCommand_can_not_create_targetFeatureType,
                                           Message.Type.ERROR);
                return false;
            }
        }
        
        // The geometry dimension of target layer must be 
        // equal to the layer to clip or MulyGeimetry compatible or Geometry
        SimpleFeatureType featureType = this.layerToClip.getSchema();
        GeometryDescriptor geomType = featureType.getDefaultGeometry();
        Class expectedGeometry = geomType.getType().getBinding();

        SimpleFeatureType targetType = (this.targetLayer != null)
                ? this.targetLayer.getSchema()
                : this.targetFeatureType;
        GeometryDescriptor geomAttr= targetType.getDefaultGeometry();
        Class targetGeometry = geomAttr.getType().getBinding();

        this.geomValidator .setExpected(expectedGeometry);
        this.geomValidator.setTarget(targetGeometry);
        
        try {
            if(!this.geomValidator.validate() ){
                
                this.message = this.geomValidator.getMessage();
                
                return false;
            }
        } catch (Exception e) {
            this.message = new Message( Messages.ClipCommand_failed_validating_geometry, Message.Type.FAIL);
            return false;
        }
        
        return true;
    }

    /**
     * Sets parameters
     *
     * @param clippingLayer
     * @param layerToClip
     * @param clippingFeatures
     * @param featuresToClip
     * @param targetLayer
     */
    public void setParameters(
                    final ILayer clippingLayer, final ILayer layerToClip,
                    final FeatureCollection<SimpleFeatureType, SimpleFeature> clippingFeatures, final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToClip,
                    final ILayer targetLayer ) {
    
        this.clippingLayer = clippingLayer;
        this.layerToClip = layerToClip;

        this.clippingFeatures = clippingFeatures;
        this.featuresToClip = featuresToClip;

        setTarget(targetLayer);
    }
    
    /**
     * sets parameters
     * 
     * @param clippingLayer
     * @param layerToClip
     * @param clippingFeatures
     * @param featuresToClip
     * @param targetLayerName
     * @param targetCrs
     */
    public void setParameters( final ILayer clippingLayer, final ILayer layerToClip,
                               final FeatureCollection<SimpleFeatureType, SimpleFeature> clippingFeatures,
                               final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToClip,
                               final String targetLayerName, 
                               final CoordinateReferenceSystem targetCrs) {
        
        this.clippingLayer = clippingLayer;
        this.layerToClip = layerToClip;

        this.clippingFeatures = clippingFeatures;
        this.featuresToClip = featuresToClip;

        setTarget(targetLayerName, targetCrs);
    }

    /**
     * sets the target layer parameter
     * 
     * @param targetLayer
     */
    private void setTarget( final ILayer targetLayer ) {
        
        this.targetLayer = targetLayer;
        this.targetLayerName = null;
        this.targetCrs = null;
        
    }
    /**
     * sets the feature type for the new layer
     *
     * @param targetLayerName
     * @param targetCrs
     */
    private void setTarget( final String  targetLayerName, final CoordinateReferenceSystem targetCrs ) {
        
        this.targetLayer = null;
        
        this.targetLayerName = targetLayerName;
        this.targetCrs = targetCrs;
    }


    /**
     * Executes the clip transaction
     * 
     * @throws SOCommandException
     */
    @Override
    public void execute() throws SOCommandException{

        if(! this.canExecute ) {
            throw new SOCommandException("The precondition is false."); //$NON-NLS-1$
        }
        
        IClipParameters params;
        if(this.targetLayer != null){
            params = ParametersFactory.createClipParameters(this.clippingLayer, this.layerToClip,
                                                            this.clippingFeatures,
                                                            this.featuresToClip, this.targetLayer);
        }else{
            assert this.targetFeatureType != null;
            
            params = ParametersFactory.createClipParameters(this.clippingLayer, this.layerToClip,
                                                            this.clippingFeatures,
                                                            this.featuresToClip, this.targetFeatureType);
            
        }

        try {
            SOProcessManager.clipOperation(params);
        } catch (SOProcessException e) {
            throw  new SOCommandException( e.getMessage());
        }
        
        
        reset();
    }

    @Override
    public void initParameters() {
        
        this.layerToClip = null;
        this.clippingLayer = null;
        
        this.featuresToClip  = null;
        this.clippingFeatures = null;
    }
    
}
