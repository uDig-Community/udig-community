/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
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
package es.axios.udig.spatialoperations.internal.ui.parameters.intersect;

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

import es.axios.udig.spatialoperations.internal.control.IntersectController;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.internal.ui.common.SpecifiedLayerListener;
import es.axios.udig.spatialoperations.internal.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.view.Message;
import es.axios.udig.ui.commons.util.GeoToolsUtils;

/**
 * Input data for intersect operation.
 * <p>
 * This contains the widgets required to capture the inputs for intersect operation.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class IntersectComposite extends AggregatedPresenter {
    
    private static final int     GRID_DATA_1_WIDTH_HINT      = 125;
    private static final int     GRID_DATA_2_WIDTH_HINT      = 150;
    private static final int     GRID_DATA_3_WIDTH_HINT      = 170;
    private static final int     GRID_DATA_4_WIDTH_HINT      = 150;

    // widgets
    private Group                groupSourceInputs           = null;
    private Group                groupTargetInputs           = null;
    private ResultLayerComposite resultComposite             = null;
    private CLabel               cLabel                      = null;
    private CLabel               cLabel1                     = null;
    private CLabel               cLabelFeaturesInFirstLayer  = null;
    private CLabel               cLabel3                     = null;
    private CCombo               comboSecondLayer            = null;
    private CLabel               cLabel4                     = null;
    private CLabel               cLabelFeaturesInSecondLayer = null;
    private CCombo               comboFirstLayer             = null;

    // data
    private ILayer               currentFirstLayer           = null;
    private ILayer               currentSecondLayer          = null;

    private FeatureCollection<SimpleFeatureType, SimpleFeature>    featuresInFirstLayer        = null;
    private FeatureCollection<SimpleFeatureType, SimpleFeature>    featuresInSecondLayer       = null;


    public IntersectComposite( Composite parent, int style ) {
        super(parent, style);

        super.initialize();

        
    }

    @Override
    public final String getOperationName() {
        return Messages.IntersectComposite_operation_name; 
    }
    
    
    @Override
    public String getToolTipText() {
        return Messages.IntersectCommand_description;
    }
    
    

    @Override
    protected final void createContents() {

        GridLayout gridLayout = new GridLayout();
        setLayout(gridLayout);
       
        createGroupSourceInputs();

        createGroupTargetInputs();
    }

    /**
     * This method initializes group for source inputs
     */
    private void createGroupSourceInputs() {

        GridData gridData11 = new GridData();
        gridData11.horizontalAlignment = GridData.BEGINNING;
        gridData11.grabExcessHorizontalSpace = false;
        gridData11.grabExcessVerticalSpace = true;
        gridData11.verticalAlignment = GridData.CENTER;
        gridData11.widthHint = GRID_DATA_1_WIDTH_HINT;

        GridData gridData12 = new GridData();
        gridData12.horizontalAlignment = GridData.BEGINNING;
        gridData12.grabExcessHorizontalSpace = false;
        gridData12.grabExcessVerticalSpace = true;
        gridData12.verticalAlignment = GridData.CENTER;
        gridData12.widthHint = GRID_DATA_2_WIDTH_HINT;

        GridData gridData13 = new GridData();
        gridData13.horizontalAlignment = GridData.BEGINNING;
        gridData13.grabExcessHorizontalSpace = false;
        gridData13.grabExcessVerticalSpace = true;
        gridData13.verticalAlignment = GridData.CENTER;
        gridData13.widthHint = GRID_DATA_3_WIDTH_HINT;

        GridData gridData14 = new GridData();
        gridData14.horizontalAlignment = GridData.BEGINNING;
        gridData14.grabExcessHorizontalSpace = false;
        gridData14.grabExcessVerticalSpace = true;
        gridData14.verticalAlignment = GridData.CENTER;
        gridData14.widthHint = GRID_DATA_4_WIDTH_HINT;

        GridData gridData21 = new GridData();
        gridData21.horizontalAlignment = GridData.BEGINNING;
        gridData21.grabExcessHorizontalSpace = false;
        gridData21.grabExcessVerticalSpace = true;
        gridData21.verticalAlignment = GridData.CENTER;
        gridData21.widthHint = GRID_DATA_1_WIDTH_HINT;

        GridData gridData22 = new GridData();
        gridData22.horizontalAlignment = GridData.BEGINNING;
        gridData22.grabExcessHorizontalSpace = false;
        gridData22.grabExcessVerticalSpace = true;
        gridData22.verticalAlignment = GridData.CENTER;
        gridData22.widthHint = GRID_DATA_2_WIDTH_HINT;

        GridData gridData23 = new GridData();
        gridData23.horizontalAlignment = GridData.BEGINNING;
        gridData23.grabExcessHorizontalSpace = false;
        gridData23.grabExcessVerticalSpace = true;
        gridData23.verticalAlignment = GridData.CENTER;
        gridData23.widthHint = GRID_DATA_3_WIDTH_HINT;

        GridData gridData24 = new GridData();
        gridData24.horizontalAlignment = GridData.BEGINNING;
        gridData24.grabExcessHorizontalSpace = false;
        gridData24.grabExcessVerticalSpace = true;
        gridData24.verticalAlignment = GridData.CENTER;
        gridData24.widthHint = GRID_DATA_4_WIDTH_HINT;

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = false;
        gridData.verticalAlignment = GridData.CENTER;

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;

        groupSourceInputs = new Group(this, SWT.NONE);
        groupSourceInputs.setText(Messages.IntersectComposite_source);
        groupSourceInputs.setLayout(gridLayout);
        groupSourceInputs.setLayoutData(gridData);

        cLabel = new CLabel(groupSourceInputs, SWT.NONE);
        cLabel.setText(Messages.IntersectComposite_first_layer);
        cLabel.setLayoutData(gridData11);

        comboFirstLayer = new CCombo(groupSourceInputs, SWT.BORDER | SWT.READ_ONLY);
        comboFirstLayer.setLayoutData(gridData12);

        this.comboFirstLayer.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected( @SuppressWarnings("unused")
                                        SelectionEvent e ) {
                
                selectedFirstLayerActions();

            }
        });

        cLabel1 = new CLabel(groupSourceInputs, SWT.NONE);
        cLabel1.setText(Messages.IntersectComposite_selected_features);
        cLabel1.setLayoutData(gridData13);

        cLabelFeaturesInFirstLayer = new CLabel(groupSourceInputs, SWT.BORDER);
        cLabelFeaturesInFirstLayer.setText(""); //$NON-NLS-1$
        cLabelFeaturesInFirstLayer.setLayoutData(gridData14);

        cLabel3 = new CLabel(groupSourceInputs, SWT.NONE);
        cLabel3.setText(Messages.IntersectComposite_second_layer);
        cLabel3.setLayoutData(gridData21);

        comboSecondLayer = new CCombo(groupSourceInputs, SWT.BORDER | SWT.READ_ONLY);
        comboSecondLayer.setLayoutData(gridData22);
        this.comboSecondLayer.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( @SuppressWarnings("unused")
                                        SelectionEvent e ) {
                
                selectedSecondLayerActions();
            }
        });

        cLabel4 = new CLabel(groupSourceInputs, SWT.NONE);
        cLabel4.setText(Messages.IntersectComposite_selected_features);
        cLabel4.setLayoutData(gridData23);

        cLabelFeaturesInSecondLayer = new CLabel(groupSourceInputs, SWT.BORDER);
        cLabelFeaturesInSecondLayer.setText(""); //$NON-NLS-1$
        cLabelFeaturesInSecondLayer.setLayoutData(gridData24);
    }

    private void createGroupTargetInputs() {

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
    private void requiredFeatureTypeActions( @SuppressWarnings("unused")
                                             final String layerName ) {
        
        validate();
    }
    /**
     * Sets the current layer and validate inputs
     * 
     * @param selectedLayer
     */
    private void selectedTargetLayerActions( @SuppressWarnings("unused")
                                             final ILayer selectedLayer ) {
        validate();
    }



    /**
     * Populates layer comboboxs with the current layers. clipping layer.
     */
    @Override
    protected void populate() {

        
        loadComboWithLayerList(this.comboFirstLayer);
        loadComboWithLayerList(this.comboSecondLayer);
        
        selectDefaultLayer();
        
        // validat is required to set the initial values in the associated command
        validate();
        
    }

    /**
     * Sets the selected First layer and its features has current. 
     */
    private void selectedFirstLayerActions(){
        
        ILayer selectedLayer = getSelecedLayer(this.comboFirstLayer);
        if (selectedLayer == null)
            return;

        this.currentFirstLayer = selectedLayer;

        this.featuresInFirstLayer = presentSelectedFeaturesSum(this.currentFirstLayer, this.currentFirstLayer.getFilter(),
                                                               this.cLabelFeaturesInFirstLayer);

        validate();
    }
    

    /**
     * Sets the selected layer in map has default for first layer.
     */
    private void selectDefaultLayer(){
        
        // gets the selected layer from map in the current context
        IToolContext context = getContext();
        if (context == null)
            return;

        ILayer selectedLayerInMap = context.getSelectedLayer();
        if (selectedLayerInMap == null)
            return;

        this.currentFirstLayer = selectedLayerInMap;

        this.featuresInFirstLayer = presentSelectedFeaturesSum(this.currentFirstLayer, this.currentFirstLayer.getFilter(),
                                                               this.cLabelFeaturesInFirstLayer);

        changeSelectedLayer(this.currentFirstLayer, this.comboFirstLayer);

        validate();
    }
    
        
    /**
     * Sets the selected Second layer and its features has current. 
     */
    private void selectedSecondLayerActions(){
        
        ILayer selectedLayer = getSelecedLayer(this.comboSecondLayer);
        if (selectedLayer == null)
            return;

        this.currentSecondLayer = selectedLayer;

        this.featuresInSecondLayer = presentSelectedFeaturesSum(this.currentSecondLayer,
                                                                this.currentSecondLayer.getFilter(),
                                                                this.cLabelFeaturesInSecondLayer);

        validate();
    }
    
    
    @Override
    public void setEnabled(boolean enabled){
        groupSourceInputs.setEnabled(enabled);
        groupTargetInputs.setEnabled(enabled);
        resultComposite.setEnabled(enabled);
        comboSecondLayer.setEnabled(enabled);
        comboFirstLayer.setEnabled(enabled);
    }

    /**
     * Validate parameters, if they are ok enable operation
     */
    private void validate() {

        // Sets the parameters values in controller to do the validation
        IntersectController ctrl = (IntersectController) getController();
        if (!ctrl.isRunning()) {
            return;
        }

        if (this.resultComposite.isLayerSelected() ) {
            
            ILayer targetLayer = this.resultComposite.getCurrentTargetLayer();
            
            ctrl.setParameters(this.currentFirstLayer, this.featuresInFirstLayer,
                               this.currentSecondLayer, this.featuresInSecondLayer,
                               targetLayer);
        } else {
            final String layerName = this.resultComposite.getNewLayerName();
            final SimpleFeatureType featureType = buildFeatureType(layerName);

            ctrl.setParameters(this.currentFirstLayer, this.featuresInFirstLayer,
                               this.currentSecondLayer, this.featuresInSecondLayer,
                               featureType);
        }

        ctrl.validate();
    }

    /**
     * @return the target feature type
     */
    private SimpleFeatureType buildFeatureType(final String layerName) {

        final CoordinateReferenceSystem crs = getCurrentMapCrs();
        assert crs != null;
        SimpleFeatureTypeBuilder typeBuilder = GeoToolsUtils.createDefaultFeatureType(layerName, crs);
        SimpleFeatureType newFeatureType = null;
        try {
            newFeatureType = typeBuilder.buildFeatureType();
            
        } catch (IllegalArgumentException e) {
            Message message = new Message(Messages.IntersectComposite_can_not_create_targetFeatureType,
                                       Message.Type.ERROR);
            
            this.getController().setMessage(message);
        }
        return newFeatureType;
    }
    


    /**
     * Reinitialize parameter values
     */
    @Override
    protected void clearInputs() {
        
        // initializes data
        this.currentFirstLayer = null;
        this.currentSecondLayer = null;

        // initializes widgets
        this.comboFirstLayer.removeAll();
        this.comboSecondLayer.removeAll();

        this.cLabelFeaturesInFirstLayer.setText(""); //$NON-NLS-1$
        this.cLabelFeaturesInSecondLayer.setText(""); //$NON-NLS-1$

    }
    
    /**
     * Maintains the consistence between the presented layers and features 
     * in map model and this view
     */
    @Override
    protected final void changedLayerListActions() {

        // change the list of layers
        this.comboFirstLayer.removeAll();
        this.comboSecondLayer.removeAll();

        populate();

        // update the selection 
        changeSelectedLayer(this.currentFirstLayer, this.comboFirstLayer);
        selectedFirstLayerActions();

        changeSelectedLayer(this.currentSecondLayer, this.comboSecondLayer);
        selectedSecondLayerActions();

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
     * Changes the count of features selected of the selected layer
     */
    @Override
    protected void changedFilterSelectionActions( final ILayer layer, final Filter newFilter ) {

        if (layer.equals(this.currentFirstLayer)) {

            this.featuresInFirstLayer = presentSelectedFeaturesSum(this.currentFirstLayer,
                                                                   newFilter,
                                                                   this.cLabelFeaturesInFirstLayer);

        } 
        if (layer.equals(this.currentSecondLayer)) {

            this.featuresInSecondLayer = presentSelectedFeaturesSum(this.currentSecondLayer,
                                                                    newFilter,
                                                                    this.cLabelFeaturesInSecondLayer);

        } 
    }


} // @jve:decl-index=0:visual-constraint="10,10"
