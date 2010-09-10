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
package es.axios.udig.spatialoperations.internal.modelconnection;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.internal.processmanager.SOProcessException;
import es.axios.udig.spatialoperations.internal.processmanager.SOProcessManager;
import es.axios.udig.spatialoperations.ui.view.Message;
import es.axios.udig.ui.commons.util.GeoToolsUtils;

/**
 * Intersect Command
 * <p>
 * Evaluates predicate and executes the associated intersection operation.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class IntersectCommand extends SOAbstractCommand {

    private static final Message INITIAL_MESSAGE       = new Message(Messages.IntersectCommand_description,
                                                                     Message.Type.IMPORTANT_INFO);
    // collaborators
    private GeometryCompatibilityValidator geomValidator = new GeometryCompatibilityValidator();

    // inputs parameters
    private ILayer                         firstLayer            = null;
    private FeatureCollection<SimpleFeatureType, SimpleFeature>              featuresInFirstLayer  = null;
    private ILayer                         secondLayer           = null;
    private FeatureCollection<SimpleFeatureType, SimpleFeature>              featuresInSecondLayer = null;
    private ILayer                         targetLayer           = null;
    private SimpleFeatureType                    targetFeatureType     = null;



    
    public IntersectCommand(){
        super(INITIAL_MESSAGE);
        
    }
    /**
     * Sets the parameters to execute the operation
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
        
        this.firstLayer = firstLayer;
        this.featuresInFirstLayer = featuresInFirstLayer;
        
        this.secondLayer = secondLayer;
        this.featuresInSecondLayer = featuresInSecondLayer;
        
        setTarget( targetLayer );
    }

    /**
     * sets the target layer parameter
     * 
     * @param targetLayer
     */
    private void setTarget( final ILayer targetLayer ) {
        
        this.targetLayer = targetLayer;
        this.targetFeatureType = null;
        
    }
    

    /**
     * Sets the parameters to execute the operation
     *
     * @param firstLayer
     * @param featuresInFirstLayer
     * @param secondLayer
     * @param featuresInSecondLayer
     * @param targetFeaureType 
     * @param targetCrs
     */
    public void setParameters( 
            ILayer firstLayer, FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer, 
            ILayer secondLayer, FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer, 
            SimpleFeatureType targetFeatureType) {
        
        this.firstLayer = firstLayer;
        this.featuresInFirstLayer = featuresInFirstLayer;
        
        this.secondLayer = secondLayer;
        this.featuresInSecondLayer = featuresInSecondLayer;
        
        setTarget( targetFeatureType);
        
    }

    /**
     * sets the feature type for the new layer
     *
     * @param featureType
     * @param targetCrs
     */
    private void setTarget( final SimpleFeatureType featureType ) {
        
        this.targetLayer = null;
        this.targetFeatureType = featureType;
    }
    /**
     * Evaluates de compatibility of layer's geometries and not null inputs.
     * Additionaly first,second and target layer must be differens.
     * 
     * @return true if all parameters are ok
     */
    @Override
    public boolean evalPrecondition() {

        this.canExecute = true;

        if(hasNullParameters()){
            
            this.canExecute = false;
            
        }else if( !checkInterLayerPredicate() ){
            
            this.canExecute = false;
            
        } else if(!checkNoEmptyLayers() ){
        
            this.canExecute = false;
            
        } else if(!checkTargetCompatibility() ){
        
            this.canExecute = false;
            
        }
        // if can, set the ok message
        if(this.canExecute){
            this.message = new Message(Messages.IntersectCommand_parameters_ok, 
                                       Message.Type.INFORMATION);
        }
        
        return this.canExecute;
    }



    /**
     * @return true if the geometry of target is compatible with 
     * the intersection geometry type 
     */
    private boolean checkTargetCompatibility() {
        
        this.message = Message.NULL;
        
        //The geometry dimension of target layer must be 
        // equal to the minor of the source layers' dimension.
        Class expectedGeometry = getGeometryExpected(firstLayer, secondLayer);

        SimpleFeatureType targetType = (this.targetLayer != null)
                ? this.targetLayer.getSchema()
                : this.targetFeatureType;
        GeometryDescriptor geomAttr= targetType.getDefaultGeometry();
        Class targetGeometry = geomAttr.getType().getBinding();

        this.geomValidator.setExpected(expectedGeometry);
        this.geomValidator.setTarget(targetGeometry);
        
        try {
            if(!this.geomValidator.validate() ){
                
                this.message = this.geomValidator.getMessage();
                
                return false;
            }
        } catch (Exception e) {
            this.message = new Message( Messages.IntersectCommand_faild_validating_geometry_compatibility, Message.Type.FAIL);
            return false;
        }
        
        return true;
    }

    /**
     * first and second layer must have one or more features
     *
     * @return true if first and second layer have features.
     */
    private boolean checkNoEmptyLayers() {
        
        this.message = Message.NULL;
        
        //first and second layer must have one or more features
        if( (this.featuresInFirstLayer.size() == 0) ){
            
            this.message = new Message(Messages.IntersectCommand_there_is_not_features_to_intersect_in_first_layer,
                                       Message.Type.ERROR);
            
            return false;
        }
        if( (this.featuresInSecondLayer.size() == 0) ){
            
            this.message = new Message(Messages.IntersectCommand_there_is_not_to_intersect_in_second_layer,
                                       Message.Type.ERROR);
            
            return false;
        }
        return true;
        
    }

    /**
     * The layers can not be equals
     *
     * @return true if the layers are differents
     */
    private boolean checkInterLayerPredicate() {

        this.message = Message.NULL;
        
        List<ILayer> layerList = new ArrayList<ILayer>(3);

        layerList.add(this.firstLayer);
        if( layerList.contains(this.secondLayer) ){
            
            this.message = new Message(Messages.IntersectCommand_first_and_second_must_be_differents,
                                       Message.Type.ERROR);
            
            return false;
        }
        layerList.add(this.secondLayer);
        
        if( layerList.contains(this.targetLayer) ){
            
            layerList.add(this.firstLayer);
            this.message = new Message(Messages.IntersectCommand_first_sectond_and_target_must_be_differents, 
                                       Message.Type.ERROR);
            
            return false;
        }
        return true;
    }

    /**
     * Checks if there are some null parameter and sets a human message. 
     * 
     * @return false if found any null parameter, true in other case.
     */
    private boolean hasNullParameters() {
        
        this.message = Message.NULL;

        if(this.firstLayer == null){

            this.message =  new Message(Messages.IntersectCommand_must_select_the_first_layer,
                                        Message.Type.INFORMATION);
            
            return true;
        }
        
        if(this.secondLayer== null){
            
            this.message = new Message(Messages.IntersectCommand_must_select_second_layer,
                                       Message.Type.INFORMATION);
            
            return true;
        }
        if ((this.targetLayer == null) && (this.targetFeatureType == null)) {
            this.message = new Message(Messages.IntersectCommand_must_select_target_layer,
                                       Message.Type.INFORMATION);

            return true;
        }
        
        return false;
    }
    /**
     * Analyses the source layers and produces the target geometry required 
     * for target layer.
     * <p>
     * The result will be the layer's geometry that has the minimum dimension.
     * If someone of they has Geometry type the result must be Geometry
     * </p>
     * @param firstLayer
     * @param secondLayer
     * @return the geometry expected
     */
    private Class getGeometryExpected( final ILayer firstLayer, final ILayer secondLayer){

        SimpleFeatureType firstType = firstLayer.getSchema();
        SimpleFeatureType secondType = secondLayer.getSchema();

        // first checks if some of layers have got Geometry class
        // If that is true returns Geometry class. 
        GeometryDescriptor firstGeomAttr = firstType.getDefaultGeometry();
        Class firstGeomClass = firstGeomAttr.getType().getBinding();
        
        GeometryDescriptor secondGeomAttr = secondType.getDefaultGeometry();
        Class secondGeomClass = secondGeomAttr.getType().getBinding();
        
        if(Geometry.class.equals(firstGeomClass) || Geometry.class.equals(secondGeomClass)){
            return Geometry.class;
        }
        
        // if they have not got Geometry class checks the dimension and 
        // return the class of minimum
        int firstLayerDim = GeoToolsUtils.getDimensionOf(firstType);
        int secondLayerDim = GeoToolsUtils.getDimensionOf(secondType);
        
        SimpleFeatureType featureTypeExpected;
        
        if(firstLayerDim <= secondLayerDim){
            featureTypeExpected = firstType;
        }else{
            featureTypeExpected = secondType;
            
        }
        GeometryDescriptor geomType = featureTypeExpected.getDefaultGeometry();
        Class geomClass = geomType.getType().getBinding();
        
        return geomClass;
    }
    
    /**
     * Executes the intersect operation.
     */
    @Override
    public void execute() throws SOCommandException {
        
        if(! this.canExecute ) {
            throw new SOCommandException("the precondition is false."); //$NON-NLS-1$
        }

        // Creates the required parameters to create new layer or use an existent layer.
        IIntersectParameters params = null; 
        if (this.targetLayer != null) {
            params = ParametersFactory.createIntersectParameters(this.firstLayer,
                                                                 this.featuresInFirstLayer,
                                                                 this.secondLayer,
                                                                 this.featuresInSecondLayer,
                                                                 this.targetLayer);
            
        } else{
            params = ParametersFactory.createIntersectParameters(this.firstLayer,
                                                                 this.featuresInFirstLayer,
                                                                 this.secondLayer,
                                                                 this.featuresInSecondLayer,
                                                                 this.targetFeatureType);
            
        }
        
        try {
            SOProcessManager.intersectOperation(params);
        } catch (SOProcessException e) {
            throw new SOCommandException(e.getMessage());
        }

        reset();
    }

    @Override
    public void initParameters() {

        firstLayer            = null;
        featuresInFirstLayer  = null;
        secondLayer           = null;
        featuresInSecondLayer = null;
        targetLayer           = null;
        targetFeatureType     = null;
        
    }


}
