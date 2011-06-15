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
package es.axios.udig.spatialoperations.internal.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.refractions.udig.project.ILayer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.ui.parameters.AbstractParamsPresenter;
import es.axios.udig.spatialoperations.internal.ui.parameters.ISOParamsPresenter;
import es.axios.udig.spatialoperations.ui.view.Message;

/**
 * Common solution to define the result (or target layer)
 * <p>
 * Common solution to user interface which need capture an 
 * existen layer or define a new layer
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class ResultLayerComposite extends AbstractParamsPresenter {

    private static final int             GRID_DATA_1_WIDTH_HINT = 125;
    private static final int             GRID_DATA_2_WIDTH_HINT = 150;
    private static final int             GRID_DATA_3_WIDTH_HINT = 170;

    // controls
    private CCombo                       comboTargetLayer       = null;
    private CLabel geometryLabel;


    // listeners
    private List<SpecifiedLayerListener> listeners              = new ArrayList<SpecifiedLayerListener>(
                                                                                                          1);


    // data
    private ILayer                       currentTargetLayer     = null;
    private String                       currentNewLayerName    = null;
    private String                       lastNameGenerated      = null;
    private static final String          SEPARATOR              = "-"; //$NON-NLS-1$
    
    public ResultLayerComposite( Composite parent, int style ) {
        super(parent, style);
        
        super.initialize();
    }



    @Override
    protected final void createContents() {
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        setLayout(gridLayout);

        createGroupTargetInputs();

    }



    /**
     * changes the list of layers
     */
    @Override
    public final void changedLayerListActions() {
        
        // Saves the last generated and test if this name was generated to restore de value.
        // after the combobox initialization
        if(this.comboTargetLayer.isDisposed()) return;
        
        String presentedName = this.comboTargetLayer.getText();
        
        loadComboWithLayerList(this.comboTargetLayer);

        if(presentedName.equals(this.lastNameGenerated)){
            
                this.comboTargetLayer.setText(this.lastNameGenerated);
                
        }
        selectedTargetLayerActions(this.comboTargetLayer);
        
    }
    
    
    /**
     * Adds the listeners
     *
     * @param listeners
     */
    public void addSpecifiedLayerListener(SpecifiedLayerListener listener){
     
        assert listener != null;
        
        this.listeners.add(listener);
    }

    /**
     * Removes the listeners
     *
     * @param listeners
     */
    public void removeSpecifiedLayerListener(SpecifiedLayerListener listener){
        
        assert listener != null;
        
        this.listeners.add(listener);
    }

    /**
     * Announces that a layer selected was selected to the listeners
     *
     * @param layer
     */
   private void dispatchEvent(final ILayer layer){

        for( SpecifiedLayerListener listener : this.listeners ) {

            listener.layerSelected(layer);
        }
    }

    /**
     * Announces that a name for e new feature Type or Layer was typed
     *
     * @param text
     */
    private void dispatchEvent(final String text){

        for( SpecifiedLayerListener listener : this.listeners ) {

            listener.newFeatureTypeIsRequired(text);
        }
        
    }
    
    
    /**
     * This method initializes groups widget for taget
     */
    private void createGroupTargetInputs() {
        
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = false;
        gridData1.horizontalAlignment = GridData.BEGINNING;
        gridData1.verticalAlignment = GridData.CENTER;
        gridData1.grabExcessVerticalSpace = false;
        gridData1.widthHint = GRID_DATA_1_WIDTH_HINT - 5;

        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = false;
        gridData2.horizontalAlignment = GridData.BEGINNING;
        gridData2.verticalAlignment = GridData.CENTER;
        gridData2.grabExcessVerticalSpace = false;
        gridData2.widthHint = GRID_DATA_2_WIDTH_HINT;

        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.BEGINNING;
        gridData3.grabExcessHorizontalSpace = false;
        gridData3.verticalAlignment = GridData.CENTER;
        gridData3.widthHint = GRID_DATA_3_WIDTH_HINT;

        CLabel targetLabel = new CLabel(this, SWT.NONE);
        targetLabel.setLayoutData(gridData1);
        targetLabel.setText(Messages.ResultLayerComposite_target_label);

        comboTargetLayer = new CCombo(this, SWT.BORDER );
        comboTargetLayer.setLayoutData(gridData2);
        comboTargetLayer.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( @SuppressWarnings("unused")
                                        SelectionEvent e ) {
                selectedTargetLayerActions(comboTargetLayer);
            }

        });
        
        comboTargetLayer.addModifyListener(new ModifyListener(){

            public void modifyText( @SuppressWarnings("unused")
                                    ModifyEvent e ) {
                
                modifyTextActions();
            }});
        
        geometryLabel = new CLabel(this, SWT.NONE);
        geometryLabel.setText(""); //$NON-NLS-1$
        geometryLabel.setLayoutData(gridData3);
    }

    @Override
    protected final void clearInputs() {

        this.currentNewLayerName = null;
        this.currentTargetLayer = null;
    }

    
    /**
     * Initializes the last layer name generated
     */
    @Override
    protected void initState() {

        this.lastNameGenerated =null;
        
    }


    /**
     * Populate with defalut values maintaining the input setted by user 
     */
    @Override
    protected  final void populate() {

        loadComboWithLayerList(this.comboTargetLayer);
        
        String nextLayerName;
        if ((this.lastNameGenerated == null)) {
            // generate a new layer name
            nextLayerName = makeInitialLayerName();

        } else {

            // generates a new layer name using the last name
            nextLayerName = makeNextLayerNameUsing(this.lastNameGenerated);
        }
        assert !nextLayerName.equals(this.lastNameGenerated);
        
        this.lastNameGenerated = nextLayerName;
            
        this.comboTargetLayer.setText(this.lastNameGenerated);
    }
    
    /**
     * Gets the target layer selected and validate all inputs
     * 
     * @param comboTargetLayer
     * 
     * @return the name of layer selected
     */
    private String selectedTargetLayerActions( final CCombo comboTargetLayer ) {

        assert comboTargetLayer != null;
        
        ILayer selectedLayer = null;
        String layerName = ""; //$NON-NLS-1$
        int index = comboTargetLayer.getSelectionIndex();
        if (index != -1) {
            layerName = comboTargetLayer.getItem(index);
       
            selectedLayer = (ILayer) comboTargetLayer.getData(layerName);

            // displays the layer's geometry
            SimpleFeatureType schema = selectedLayer.getSchema();
            GeometryDescriptor geomAttrType = schema.getDefaultGeometry();
            String geomName = geomAttrType.getLocalName();
            this.geometryLabel.setText(geomName);
            
            
        }
        setCurrentTarget(selectedLayer);
        dispatchEvent(selectedLayer);
        
        
        return layerName;
    }
    
    
    /**
     * Combo's text was modified, this method assures that 
     * there is not any layer with the new typed name.
     */
    private void modifyTextActions() {
        
        final String text = this.comboTargetLayer.getText();
        if(text.length() == 0 ){
            return;
        }
        // if it was modified by the selection of one item it was handled by
        // selectedTargetLayerActions
        if(this.comboTargetLayer.getSelectionIndex()!= -1){
            return;
        }
        
        if(text.equals(this.currentNewLayerName)){
            return;
        }
        
        // check the new name with the layer list.
        if( this.comboTargetLayer.indexOf(text) != -1){
            Message msg = new Message(Messages.ResultLayerComposite__duplicated_layer_name, Message.Type.ERROR);
            this.getController().setMessage(msg);
            
            return;
        }
        
        // if the new name is correct, notifies to all listeners,
        // saves the current name and sets Geometry as default for the new layer        
        setCurrentTarget(text);
        geometryLabel.setText("Geometry"); //$NON-NLS-1$
        
        dispatchEvent(text);
        
    }


    /**
     * Makes the new name taking the last generated as base.
     * Format: Prefix-Number
     * 
     * @param lastNameGenerated
     * @return new layer's name using the last generated
     */
    private String makeNextLayerNameUsing( String lastNameGenerated ) {

        StringBuffer defaultName = new StringBuffer();
        
        int separatorPosition = lastNameGenerated.indexOf(SEPARATOR);
        String prefix = lastNameGenerated.substring(0, separatorPosition);
        
        defaultName.append(prefix);
        
        String nextNumber = computeNextNumberFor(lastNameGenerated, SEPARATOR);
        defaultName.append(SEPARATOR);
        defaultName.append(nextNumber);
        
        return defaultName.toString();
    }



    /**
     * Makes the new name using the map's layer list
     * 
     * @return a new layer name
     */
    private String makeInitialLayerName() {
        StringBuffer defaultName = new StringBuffer();
        
        final String opName = getPrefix();
        
        defaultName.append(opName);
        
        // checks if there is a layer with this name, then adds an integer value following the sequence.
        String prefix = opName;
        SortedSet<String> layerList = new TreeSet<String>();
        for( int i = 0; i < this.comboTargetLayer.getItemCount(); i++ ) {

            String item = this.comboTargetLayer.getItem(i);
            if (item.startsWith(prefix)) {
                layerList.add(item);
            }
        }
        String nextNumber = null;

        // gets the last number generated, 1 for first
        // format: opName-Integer
        
        if(layerList.isEmpty() ){
            nextNumber = "1"; //$NON-NLS-1$
        }else{
            String lastLayer = layerList.last();
            nextNumber = computeNextNumberFor(lastLayer, SEPARATOR);
        }
        
        defaultName.append(SEPARATOR);
        defaultName.append(nextNumber);
        
        return defaultName.toString();
    }



    /**
     * gets the name from the first presenter (this method takes into account i18n )
     * @return the prefix
     */
    private String getPrefix() {
        
        List<ISOParamsPresenter> presenterlist = this.getController().getParamsPresenter();
        ISOParamsPresenter firstPresenter = presenterlist.get(0);
        final String opName = firstPresenter.getOperationName();

        return opName;
    }



    /**
     * Computes the next integer for the layer name
     * 
     * @param lastLayer
     * @param separator
     * 
     * @return the next number
     */
    private String computeNextNumberFor( final String lastLayer, final String separator ) {
        
        int numberPosition = lastLayer.indexOf(separator);

        String strLastNumber = lastLayer.substring(numberPosition + 1);

        int lastNumber = Integer.parseInt(strLastNumber);

        final String nextNumber = String.valueOf(lastNumber + 1);
        
        return nextNumber;
    }



    /**
     * Adds the new layer in combobox
     */
    @Override
    protected void addedLayerActions( ILayer layer ) {
        super.addedLayerActions(layer);
        
        changedLayerListActions();

    }


    /**
     * Remove the layer from combobox
     */
    @Override
    protected final void removedLayerActions( ILayer layer ) {
        super.removedLayerActions(layer);

        changedLayerListActions();
    }
    

    /**
     * Sets the current name of the new layer
     * 
     * @param newLayerName name for the new layer
     */
    private void setCurrentTarget( String newLayerName ) {
    
        this.currentNewLayerName = newLayerName;
        this.currentTargetLayer = null;


    }
    
    @Override
    public void setEnabled(boolean enabled){
        comboTargetLayer.setEnabled(enabled);

    }
    /**
     * Sets the current target layer
     * 
     * @param layer the target layer
     */
    private void setCurrentTarget( ILayer layer ) {
    
        this.currentNewLayerName = null;
        this.currentTargetLayer = layer;
    }

    /**
     * @return Returns the currentTargetLayer.
     */
    public ILayer getCurrentTargetLayer() {
        return this.currentTargetLayer;
    }


    /**
     * @return true if a Layer was selected, false in other case.
     */
    public boolean isLayerSelected() {
        return this.currentTargetLayer != null;
    }


    /**
     * @return true if a feature type was specified, false in other case. 
     */
    public boolean isNewFeatureType() {
        return this.currentNewLayerName != null;
    }



    /**
     * @return the name of new layer
     */
    public String getNewLayerName() {
        
        return this.comboTargetLayer.getText();
    }


}
