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
package es.axios.udig.spatialoperations.internal.ui.parameters.clip;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.internal.control.ClipController;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.internal.ui.common.SpecifiedLayerListener;
import es.axios.udig.spatialoperations.internal.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.view.Message;
import es.axios.udig.ui.commons.util.GeoToolsUtils;

/**
 * Content for Clip operation.
 * <p>
 * This contains the widgets required to capture the inputs 
 * for clip operation.
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * 
 * @since 1.1.0
 */
public final class ClipComposite extends AggregatedPresenter {

    private static final int        GRID_DATA_1_WIDTH_HINT = 125;
    private static final int        GRID_DATA_2_WIDTH_HINT = 150;
    private static final int        GRID_DATA_3_WIDTH_HINT = 170;
    private static final int        GRID_DATA_4_WIDTH_HINT = 150;

    private Group                groupSource              = null;
    private Group                groupResult              = null;
    private CLabel               cLabelLayer              = null;
    private CCombo               cComboClippingLayer      = null;
    private CLabel               cLabel                   = null;
    private CLabel               textClippingFeatures     = null;
    private CLabel               cLabelClippedLayer       = null;
    private CCombo               cComboLayerToClip        = null;
    private CLabel               cLabelFeaturesToClip     = null;
    private CLabel               textFeaturesToClip       = null;
    private Group                groupTargetInputs        = null;
    private ResultLayerComposite resultComposite          = null;

    // data
    private ILayer               currentClippingLayer     = null;
    private ILayer               currentLayerToClip       = null;
    private SimpleFeatureType          currentTargetFeatureType = null;
    private ILayer               currentTargetLayer       = null;
    private FeatureCollection<SimpleFeatureType, SimpleFeature>    featuresToClip           = null;
    private FeatureCollection<SimpleFeatureType, SimpleFeature>    clippingFeatures         = null;

    public ClipComposite( Composite parent, int style ) {
        super(parent, style);

        super.initialize();
            
    }

    /**
     * Initializes the content for inputs parameters
     */
    @Override
    protected final void createContents() {
       

        GridLayout gridLayout = new GridLayout();
        setLayout(gridLayout);

        
        createGroupClippingLayer();
        createGroupToClipLayer();
        
        createGroupResult();

        
        
    }


    /**
     * This method initializes groupSource
     */
    private void createGroupClippingLayer() {
        

        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.BEGINNING;
        gridData1.verticalAlignment = GridData.CENTER;
        gridData1.widthHint = GRID_DATA_1_WIDTH_HINT;

        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.BEGINNING;
        gridData2.grabExcessHorizontalSpace = false;
        gridData2.heightHint = -1;
        gridData2.widthHint = GRID_DATA_2_WIDTH_HINT;
        gridData2.verticalAlignment = GridData.CENTER;

        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.BEGINNING;
        gridData3.grabExcessHorizontalSpace = false;
        gridData3.heightHint = -1;
        gridData3.widthHint = GRID_DATA_3_WIDTH_HINT;
        gridData3.verticalAlignment = GridData.CENTER;

        GridData gridData4 = new GridData();
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.verticalAlignment = GridData.CENTER;
        gridData4.horizontalAlignment = GridData.BEGINNING;
        gridData4.widthHint = GRID_DATA_4_WIDTH_HINT;

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.CENTER;
        
        groupSource = new Group(this, SWT.NONE);
        groupSource.setLayoutData(gridData);
        groupSource.setLayout(gridLayout);
        groupSource.setText(Messages.ClipComposite_using_as_clip);
        
        cLabelLayer = new CLabel(groupSource, SWT.NONE);
        cLabelLayer.setText(Messages.ClipComposite_clipping_layer);
        cLabelLayer.setLayoutData(gridData1);
        
        cComboClippingLayer = new CCombo(groupSource, SWT.BORDER | SWT.READ_ONLY);
        cComboClippingLayer.setLayoutData(gridData2);
        
        cComboClippingLayer.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected( @SuppressWarnings("unused")
                                        SelectionEvent e ) {
                selectedClippingLayerActions(cComboClippingLayer);
            }});
        
        cLabel = new CLabel(groupSource, SWT.NONE);
        cLabel.setText(Messages.ClipComposite_clipping_features);
        cLabel.setLayoutData(gridData3);
        textClippingFeatures = new CLabel(groupSource, SWT.BORDER);
        textClippingFeatures.setText(""); //$NON-NLS-1$
        textClippingFeatures.setLayoutData(gridData4);
        textClippingFeatures.setEnabled(false);
    }


    /**
     * creates "to clip layer" widgets	
     *
     */
    private void createGroupToClipLayer() {
        
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = GridData.FILL;
        gridData4.grabExcessHorizontalSpace = false;
        gridData4.verticalAlignment = GridData.CENTER;
        gridData4.widthHint = GRID_DATA_4_WIDTH_HINT;
  
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.BEGINNING;
        gridData3.grabExcessHorizontalSpace = false;
        gridData3.verticalAlignment = GridData.CENTER;
        gridData3.widthHint = GRID_DATA_3_WIDTH_HINT;
        
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.BEGINNING;
        gridData2.grabExcessHorizontalSpace = false;
        gridData2.verticalAlignment = GridData.CENTER;
        gridData2.widthHint = GRID_DATA_2_WIDTH_HINT;
        
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.BEGINNING;
        gridData1.grabExcessHorizontalSpace = false;
        gridData1.verticalAlignment = GridData.CENTER;
        gridData1.widthHint = GRID_DATA_1_WIDTH_HINT;

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = false;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.CENTER;
        
        groupResult = new Group(this, SWT.NONE);
        groupResult.setLayoutData(gridData);
        groupResult.setLayout(gridLayout);
        groupResult.setText(Messages.ClipComposite_apply_to);
        cLabelClippedLayer = new CLabel(groupResult, SWT.NONE);
        cLabelClippedLayer.setText(Messages.ClipComposite_layer_to_clip);
        cLabelClippedLayer.setLayoutData(gridData1);
        cComboLayerToClip = new CCombo(groupResult, SWT.BORDER | SWT.READ_ONLY);        
        cComboLayerToClip.setLayoutData(gridData2);
        cComboLayerToClip.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected( @SuppressWarnings("unused")
                                        SelectionEvent e ) {
                selectedLayerToClipActions(cComboLayerToClip, textFeaturesToClip);
            }});

        cLabelFeaturesToClip = new CLabel(groupResult, SWT.NONE);
        cLabelFeaturesToClip.setText(Messages.ClipComposite_features_to_clip);
        cLabelFeaturesToClip.setLayoutData(gridData3);
        textFeaturesToClip = new CLabel(groupResult, SWT.BORDER);
        textFeaturesToClip.setText(""); //$NON-NLS-1$
        textFeaturesToClip.setLayoutData(gridData4);
    }
    
    
    private void createGroupResult() {

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = false;
        gridData.verticalAlignment = GridData.BEGINNING;

        groupTargetInputs = new Group(this, SWT.NONE);
        groupTargetInputs.setText(Messages.IntersectComposite_result);
        groupTargetInputs.setLayout(new GridLayout());
        groupTargetInputs.setLayoutData(gridData);

        this.resultComposite = new ResultLayerComposite(groupTargetInputs, SWT.NONE);

        GridData resultCompositeGridData = new GridData();
        resultCompositeGridData.horizontalAlignment = GridData.FILL;
        resultCompositeGridData.grabExcessHorizontalSpace = true;
        resultCompositeGridData.grabExcessVerticalSpace = true;
        resultCompositeGridData.verticalAlignment = GridData.FILL;
        
        this.resultComposite.setLayoutData(resultCompositeGridData);
        
        this.resultComposite.addSpecifiedLayerListener(new SpecifiedLayerListener(){


            public void layerSelected( @SuppressWarnings("unused")
                                       ILayer selectedLayer ) {
                
                selectedTargetLayerActions(selectedLayer);
            }


            public void newFeatureTypeIsRequired( @SuppressWarnings("unused")
                                                  String layerName ) {
                requiredFeatureTypeActions(layerName);
            }});
        
        this.addPresenter(this.resultComposite);
        
    }
    /**
     * Create the a new feature type with the specified layer name
     * @param layerName 
     */
    private void requiredFeatureTypeActions( final String layerName ) {

        SimpleFeatureTypeBuilder typeBuilder = GeoToolsUtils.createDefaultFeatureType(layerName);
        SimpleFeatureType newFeatureType = null;
        try {
            
            newFeatureType = typeBuilder.buildFeatureType();
            
        } catch (IllegalArgumentException e) {
            Message message = new Message(Messages.IntersectComposite_can_not_create_targetFeatureType,
                                       Message.Type.ERROR);
            
            this.getController().setMessage(message);
        }
        setCurrentTargetFeatureType(newFeatureType);
        validate();
    }
    /**
     * Sets the current layer and validate inputs
     * 
     * @param selectedLayer
     */
    private void selectedTargetLayerActions( final ILayer selectedLayer ) {
        setCurrentTargetLayer(selectedLayer);
        
        validate();
    }
    
    /**
     * Sets the created feature type and unsets the current target layer
     * 
     * @param type a feature type or null
     */
    private void setCurrentTargetFeatureType( SimpleFeatureType type ) {
    
        this.currentTargetFeatureType = type;
        this.currentTargetLayer = null;
    }


    /**
     * Sets the current target layer and unsets the feature ype
     * @param layer a layer or null
     */
    private void setCurrentTargetLayer(final ILayer layer ) {
       
        this.currentTargetLayer = layer;
        this.currentTargetFeatureType = null;
    
    }
    

 
    @Override
    public String getOperationName() {
        return Messages.ClipComposite_operation_name; 
    }
    
    @Override
    public String getToolTipText() {
        return Messages.ClipCommand_clip_description;
    }
    

    /**
     * Reinitialize parameter values
     */
    @Override
    protected final void clearInputs() {
        
        // initializes data
        
        this.currentClippingLayer = null;
        this.currentLayerToClip = null;
        this.currentTargetFeatureType = null;
        this.currentTargetLayer = null;
        
        // initializes widgets
        this.cComboClippingLayer.removeAll();
        this.cComboLayerToClip.removeAll();
        
        this.textClippingFeatures.setText(""); //$NON-NLS-1$
        this.textFeaturesToClip.setText(""); //$NON-NLS-1$
        
        
        // sets default values
        populate(); 
        
    }

   
    
    /**
     * Populates layer comboboxs with the current layers.
     * Sets the current layer current has default for 
     * clipping layer.
     *
     */
    @Override
    protected void populate() {
        
        loadComboWithLayerList(this.cComboClippingLayer);
        loadComboWithLayerList(this.cComboLayerToClip);
        
        selectDefaultLayer();
        
        validate();
        
    }
    
    

    /**
     * Changes the count of features selected of the selected layer
     */
    @Override
    protected final void changedFilterSelectionActions( final ILayer layer, final Filter newFilter ) {

        
        if(layer.equals(this.currentLayerToClip)){

            this.featuresToClip =presentSelectedFeaturesSum(this.currentLayerToClip,newFilter, this.textFeaturesToClip  );
            

        } 
        if(layer.equals(this.currentClippingLayer) ){
            
            this.clippingFeatures=  presentSelectedFeaturesSum(this.currentClippingLayer, newFilter, this.textClippingFeatures );
            
        }
        
        validate();
    }
    
    
    @Override
    protected void addedLayerActions( ILayer layer ) {
        super.addedLayerActions(layer);
        
        changedLayerListActions();

    }


    @Override
    protected void removedLayerActions( ILayer layer ) {
        super.removedLayerActions(layer);

        changedLayerListActions();
    }


    /**
     * Actions associated with layer selection.
     *
     * @param comboLayer 
     * @param textFeatures
     */
    private void selectedClippingLayerActions(final CCombo comboLayer) {

        ILayer selectedLayer = getSelecedLayer(comboLayer);

        if (selectedLayer == null)
            return;

        setCurrentClippingLayer(selectedLayer);
    }
    

    /**
     * Sets the selected layer in map has default clipping layer.
     */
    private void selectDefaultLayer(){
        
        // gets the selected layer from map in the current context
        IToolContext context = getContext();
        if (context == null)
            return;

        ILayer selectedLayerInMap = context.getSelectedLayer();
        if (selectedLayerInMap == null)
            return;

        setCurrentClippingLayer(selectedLayerInMap);

        this.clippingFeatures = presentSelectedFeaturesSum(this.currentClippingLayer,
                                                           this.currentClippingLayer.getFilter(),
                                                           this.textClippingFeatures);

        changeSelectedLayer(this.currentClippingLayer, this.cComboClippingLayer);

        validate();
    }
    
    @Override
    public void setEnabled(boolean enabled){

        groupSource.setEnabled(enabled);
        groupResult.setEnabled(enabled);
        cComboClippingLayer.setEnabled(enabled);
        cComboLayerToClip.setEnabled(enabled);
        groupTargetInputs.setEnabled(enabled);
        resultComposite.setEnabled(enabled);
        
    }

    /**
     * @param selectedLayer
     */
    private void setCurrentClippingLayer( ILayer selectedLayer ) {

        this.currentClippingLayer = selectedLayer;

        this.clippingFeatures = presentSelectedFeaturesSum(this.currentClippingLayer, 
                                                           this.currentClippingLayer.getFilter(),
                                                           this.textClippingFeatures);

        validate();
        
    }

    /**
     * Actions associated with layer selection.
     *
     * @param comboLayer 
     * @param textFeatures
     */
    private void selectedLayerToClipActions(final CCombo comboLayer, final CLabel textFeatures ) {

        ILayer selectedLayer = getSelecedLayer(comboLayer);

        if (selectedLayer == null)
            return;

        this.currentLayerToClip = selectedLayer;

        this.featuresToClip = presentSelectedFeaturesSum(this.currentLayerToClip,
                                                         this.currentClippingLayer.getFilter(),
                                                         textFeatures);

        validate();
    }
    

    /**
     * Validate parameters, if they are ok enable operation
     */
    private void validate(){

        // Sets the parameters values in controller to do the validation
        ClipController ctrl = (ClipController) getController();
        if (!ctrl.isRunning()) {
            return;
        }

        if (this.resultComposite.isLayerSelected() ) {
            final ILayer targetLayer = this.resultComposite.getCurrentTargetLayer();

            ctrl.setParameters(this.currentClippingLayer, this.currentLayerToClip,
                               this.clippingFeatures, this.featuresToClip, targetLayer);

        } else {
            final String newFeatureTypeName = this.resultComposite.getNewLayerName();
            final CoordinateReferenceSystem targetCrs = getCurrentMapCrs();
            
            ctrl.setParameters(this.currentClippingLayer, this.currentLayerToClip,
                               clippingFeatures, featuresToClip, newFeatureTypeName, targetCrs);
        }

        ctrl.validate();
            
        
    }
    
    /**
     * Initializes the widghets with default values.
     */
    @Override
    protected final void changedLayerListActions() {
        
        // change the list of layers
        cComboClippingLayer.removeAll();
        cComboLayerToClip.removeAll();
        
        populate();
        
        changeSelectedLayer(this.currentLayerToClip, this.cComboLayerToClip);
        selectedLayerToClipActions(this.cComboLayerToClip, this.textFeaturesToClip);

        changeSelectedLayer(this.currentClippingLayer, this.cComboClippingLayer);
        
        selectedClippingLayerActions(this.cComboClippingLayer);
        
    }
    

}  //  @jve:decl-index=0:visual-constraint="10,10"
