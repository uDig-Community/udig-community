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
package es.axios.udig.spatialoperations.internal.ui.parameters.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.internal.control.BufferController;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.preferences.Preferences;
import es.axios.udig.spatialoperations.internal.ui.parameters.AbstractParamsPresenter;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Composite that contains the input widgets to set 
 * the buffer options.
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
final class BufferOptionsComposite extends AbstractParamsPresenter {
    
    private static final int GRID_DATA_1_WIDTH_HINT    = 125;
    private static final int GRID_DATA_2_WIDTH_HINT    = 150;

    // controls
    private Group            bufferOptionsGroup        = null;
    private Label            labelBufferWidth          = null;
    private Text             textBufferWidth           = null;
    private Label            labelQuadrantSegments     = null;
    private Spinner          spinnerQuadrantSegments   = null;
    private Composite        groupUnits                = null;
    private Button           radioLayerUnits           = null;
    private Button           radioMapUnits             = null;
    private Button           radioSpecifyUnits         = null;
    private CCombo           comboWidthUnits           = null;
    private Button           checkBoxMergeResult       = null;
    private ExpandBar        expandBar                 = null;
    private ExpandItem       expandItemAdvancedOptions = null;
    private Composite        advancedOptionsControls   = null;
    private Composite        advancedOptionsComposite  = null;

    // data
    private List<Unit>       bufferUnits               = null;

    private Unit             currentUnits              = null;
    private Boolean          currentAggregateOption    = null;
    private Double           currentWidth              = null;
    private Double           defaultWidth              = null;
    private Integer          currentQuadrantSegments   = null;

    /**
     * New instance of BufferOptionsComposite
     * @param parent
     * @param style
     */
    public BufferOptionsComposite( Composite parent, int style ) {
        super(parent, style);

        super.initialize(); 

    }

    /**
     * Cerate the widget required for common and advanced options 
     */
    @Override
    protected final void createContents() {

        
        setLayout(new FillLayout());

        createBufferOptionsGroup();

        // adds the listeners
        this.addPaintListener(new PaintListener(){

            public void paintControl( @SuppressWarnings("unused")
                                      PaintEvent e ) {
                resize();
            }});
        
        BufferLayersComposite layersComposite = getLayersComposite();
        layersComposite.addSourceLayerSelectedListener(new SourceLayerSelectedListener(){

            public void layerSelected( @SuppressWarnings("unused")
                                       ILayer selectedLayer ) {
                selectedSourceLayerActions(selectedLayer);
            }});
        
    }
        
    

    /**
     * @return ths Buffer Layers Composite (source and target container)
     */
    private BufferLayersComposite getLayersComposite() {
        
        
        BufferComposite bufferComposite = (BufferComposite)this.getParent();

        BufferLayersComposite layersComposite = bufferComposite.getLayersComposite();
     
        return layersComposite;
    }


    
    /**
     * Resizes the options widgets composites
     */
    protected final void resize() {

      // resizes the expand bar's content
      Composite parent = expandBar.getParent();
      
      Point parentSize = expandBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
      
      int xParent = parentSize.x;
      int yItems =  expandItemAdvancedOptions.getHeight(); 
      int yParent = parentSize.y + yItems;
      expandBar.setSize(xParent, yParent);
      
      // only close the item if the size change (this approach solves a layout problem at expand item)
      expandItemAdvancedOptions.setExpanded(false);
      parent.layout(true);
    }
    
    

    /**
     * This method initializes bufferOptionsGroup
     */
    private void createBufferOptionsGroup() {
        
        // group
        bufferOptionsGroup = new Group(this, SWT.NONE);
        bufferOptionsGroup.setText(Messages.BufferOptionsComposite_options);
        bufferOptionsGroup.setLayout(new GridLayout());
        
        createWidthComposite(bufferOptionsGroup);

        createUnitOptions(bufferOptionsGroup);
        
        createAdvancedOptionsComposite( bufferOptionsGroup);
    }

    /**
     *
     * @param bufferOptionsGroup
     */
    private void createWidthComposite( final Group parent ) {
        
        // width composite: text width + unit label + options
        Composite widthComposite = new Composite(parent, SWT.NONE);
        widthComposite.setLayout(new GridLayout(3,false));

        // width label
        labelBufferWidth = new Label(widthComposite, SWT.NONE);
        labelBufferWidth.setText(Messages.BufferOptionsComposite_labelBufferWidth_text);
        GridData gridDataLabel = new GridData();
        gridDataLabel.horizontalAlignment = GridData.BEGINNING;
        gridDataLabel.grabExcessHorizontalSpace = false;
        gridDataLabel.widthHint = GRID_DATA_1_WIDTH_HINT;
        labelBufferWidth.setLayoutData(gridDataLabel);
        
        // width
        textBufferWidth = new Text(widthComposite, SWT.BORDER | SWT.RIGHT);
        textBufferWidth.setTextLimit(20);
        
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.BEGINNING;
        gridData2.verticalAlignment = GridData.CENTER;
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.widthHint = GRID_DATA_2_WIDTH_HINT;
        textBufferWidth.setLayoutData(gridData2);

        textBufferWidth.addModifyListener(new ModifyListener(){

            public void modifyText( @SuppressWarnings("unused")
                                    ModifyEvent e ) {
                validate();
            }});

        textBufferWidth.addKeyListener(
          new KeyListener(){
            
            private String prevVal = String.valueOf(Preferences.bufferWidth());
            
            public void keyPressed( KeyEvent event ) {
                
                this.prevVal = textBufferWidth.getText();
                
                if (Character.isLetter(event.character)) {
                    event.doit = false;
                    textBufferWidth.setText(prevVal);
                } else {
                    prevVal = textBufferWidth.getText();
                }
            }

            public void keyReleased( @SuppressWarnings("unused")
                                     KeyEvent event ) {
                
                this.prevVal = textBufferWidth.getText();

                try {
                    Double.parseDouble(textBufferWidth.getText());
                } catch (NumberFormatException e) {
                    textBufferWidth.setText(prevVal);
                }
                validate();
            }
        });
        
        comboWidthUnits = new CCombo(widthComposite, SWT.BORDER);
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.BEGINNING;
        gridData3.verticalAlignment = GridData.CENTER;
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.widthHint = GRID_DATA_2_WIDTH_HINT;
        comboWidthUnits.setLayoutData(gridData3);
        comboWidthUnits.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected( @SuppressWarnings("unused")
                                        SelectionEvent e ) {
                validate();
            }});
        
    }

    /**
     * Create units option buttons
     * 
     * @param widthComposite
     */
    private void createUnitOptions( Composite widthComposite ) {
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;

        groupUnits = new Composite(widthComposite, SWT.NONE);
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = GridData.FILL;
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.verticalAlignment = GridData.BEGINNING;
        groupUnits.setLayoutData(gridData4);
        groupUnits.setLayout(gridLayout);

        radioMapUnits = new Button(groupUnits, SWT.RADIO);
        radioMapUnits.setText(Messages.BufferOptionsComposite_radioMapUnits_text); 
        radioMapUnits.setToolTipText(Messages.BufferOptionsComposite_radioMapUnits_tooltip);
        GridData gridData7 = new GridData();
        gridData7.horizontalAlignment = GridData.BEGINNING;
        gridData7.grabExcessHorizontalSpace = true;
        radioMapUnits.setLayoutData(gridData7);

        radioLayerUnits = new Button(groupUnits, SWT.RADIO);
        radioLayerUnits.setText(Messages.BufferOptionsComposite_radioLayerUnits_text); 
        radioLayerUnits.setToolTipText(Messages.BufferOptionsComposite_radioLayerUnits_tooltip);
        GridData gridData5 = new GridData();
        gridData5.horizontalAlignment = GridData.CENTER;
        gridData5.grabExcessHorizontalSpace = true;
        radioLayerUnits.setLayoutData(gridData5);
        
        radioSpecifyUnits = new Button(groupUnits, SWT.RADIO);
        radioSpecifyUnits.setText(Messages.BufferOptionsComposite_radioSpecifyUnits_text); 
        GridData gridData6 = new GridData();
        gridData6.horizontalAlignment = GridData.END;
        gridData6.grabExcessHorizontalSpace = true;
        radioSpecifyUnits.setLayoutData(gridData6);
            
            
        radioSpecifyUnits.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( @SuppressWarnings("unused")
                                        SelectionEvent e ) {
                selectedSpecifiyUnitsActions();
            }});

        radioMapUnits.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected( @SuppressWarnings("unused")
                                        SelectionEvent e ) {
                selectedMapUnitsActions();
            }});
        
        radioLayerUnits.addSelectionListener(new SelectionAdapter(){


            @Override
            public void widgetSelected( @SuppressWarnings("unused")
                                        SelectionEvent e ) {
                
                selectedLayerUnitsActions();
            }});
    }

    
    /**
     * Create the advanced option widgets
     */
    private Composite createAdvancedOptionsComposite(final Composite parent) {
        
        advancedOptionsComposite = new Composite(parent, SWT.NONE);
        GridData gridDataComposite = new GridData();
        gridDataComposite.horizontalAlignment = GridData.FILL;
        gridDataComposite.grabExcessHorizontalSpace = true;
        gridDataComposite.grabExcessVerticalSpace = true;
        gridDataComposite.verticalAlignment = GridData.FILL;
        advancedOptionsComposite.setLayoutData(gridDataComposite);
        
        advancedOptionsComposite.setLayout(new GridLayout());
        
        expandBar = new ExpandBar(advancedOptionsComposite, SWT.NONE); //SWT.V_SCROLL
        expandBar.setLayout(new GridLayout());
        GridData expandDataLayout = new GridData();
        expandDataLayout.horizontalAlignment = GridData.FILL;
        expandDataLayout.grabExcessHorizontalSpace = true;
        expandDataLayout.grabExcessVerticalSpace = true;
        expandDataLayout.verticalAlignment = GridData.FILL;
        
        expandBar.setLayoutData(expandDataLayout);
        
        GridLayout layoutAdvancedOptions = new GridLayout ();
        layoutAdvancedOptions.marginLeft = layoutAdvancedOptions.marginTop = layoutAdvancedOptions.marginRight = layoutAdvancedOptions.marginBottom = 10;
        layoutAdvancedOptions.verticalSpacing = 10;
        layoutAdvancedOptions.numColumns = 2;

        GridData gridDataAdvancedOptions = new GridData();
        gridDataAdvancedOptions.horizontalAlignment = GridData.FILL;
        gridDataAdvancedOptions.grabExcessHorizontalSpace = true;
        gridDataAdvancedOptions.grabExcessVerticalSpace = true;
        gridDataAdvancedOptions.verticalAlignment = GridData.FILL;
        gridDataAdvancedOptions.horizontalSpan = 2;

        advancedOptionsControls = new Composite(expandBar, SWT.NONE);
        advancedOptionsControls.setLayout(layoutAdvancedOptions);   
        advancedOptionsControls.setLayoutData(gridDataAdvancedOptions);
        
        createAdvancedOptionItems(advancedOptionsControls);
        
        // item 0 in expand bar
        expandItemAdvancedOptions = new ExpandItem (expandBar, SWT.NONE);
        expandItemAdvancedOptions.setText(Messages.BufferOptionsComposite_advanced_options);

        advancedOptionsControls.pack(true);
        Point computeSize = advancedOptionsControls.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        expandItemAdvancedOptions.setHeight(computeSize.y);
        expandItemAdvancedOptions.setControl(advancedOptionsControls);
        
        return advancedOptionsComposite;
    }


    /**
     * This method initializes groupUnits
     */
    private void createAdvancedOptionItems(Composite parent) {
        
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.BEGINNING;
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.horizontalSpan =  2;
        gridData1.verticalAlignment = GridData.CENTER;
        
        checkBoxMergeResult = new Button(parent, SWT.CHECK);
        checkBoxMergeResult.setText(Messages.BufferOptionsComposite_checkMergeResults_text);
        checkBoxMergeResult
                .setToolTipText(Messages.BufferOptionsComposite_chekMergeResults_tooltip);
        checkBoxMergeResult.setLayoutData(gridData1);
        checkBoxMergeResult.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseUp( @SuppressWarnings("unused")
                                 MouseEvent e ) {
                validate();
            }});
        
        GridData gridData21 = new GridData();
        gridData21.horizontalAlignment = GridData.BEGINNING;
        gridData21.grabExcessHorizontalSpace =false;
        gridData21.verticalAlignment = GridData.CENTER;
        //gridData21.widthHint = GRID_DATA_1_WIDTH_HINT;

        labelQuadrantSegments = new Label(parent, SWT.NONE);
        labelQuadrantSegments.setText(Messages.BufferOptionsComposite_labelQuadrantSegments_text);
        labelQuadrantSegments.setLayoutData(gridData21);

        GridData gridData31 = new GridData();
        gridData31.horizontalAlignment = GridData.BEGINNING;
        gridData31.grabExcessHorizontalSpace = false;
        gridData31.verticalAlignment = GridData.CENTER;
        gridData31.widthHint = 20;

        spinnerQuadrantSegments = new Spinner(parent, SWT.BORDER);
        spinnerQuadrantSegments.setMinimum(3);
        spinnerQuadrantSegments.setLayoutData(gridData31);
        
    }
    /**
     * @return the Unit of measure to use for the buffer width, from the selected option
     */
    private Unit getBufferUnitOfMeasure() {
        
        this.currentUnits = null;
        
        int index = comboWidthUnits.getSelectionIndex();
        if( index == -1) return this.currentUnits;
        
        Unit unit = this.bufferUnits.get(index);

        // display de selection
        this.currentUnits = unit;
        
        return  this.currentUnits ;
    }

    /**
     * @return the selected source layer
     */
    private ILayer getSourceLayer(){
        BufferComposite bufferComposite = (BufferComposite)this.getTopPresenter();
        
        ILayer layer = bufferComposite.getSourceLayer();

        return layer;
    }
    /**
     * @return layer's units
     */
    private Unit getLayerUnits(final ILayer layer){

        
        CoordinateReferenceSystem crs = LayerUtil.getCrs(layer);

        Unit units = GeoToolsUtils.getDefaultCRSUnit(crs);
        
        return units;
    }
    
    /**
     * @return map's units
     */
    private Unit getMapUnits(){
        IMap map = getCurrentMap();
        assert map != null;
        
        CoordinateReferenceSystem crs = MapUtil.getCRS(map);
        Unit mapUnits = GeoToolsUtils.getDefaultCRSUnit(crs);
        
        return mapUnits;
    }

    /**
     * @return the number of line segments to use per circle quadrant when approximating curves
     *         generated by the buffer algorithm
     */
    private Integer getQuadrantSegments() {
        
        this.currentQuadrantSegments = spinnerQuadrantSegments.getSelection();
        
        return this.currentQuadrantSegments;
    }

    /**
     * @return distance around the geometries edges on which to perform the buffer, in
     *         {@link #getBufferUnitOfMeasure()} units
     */
    private Double getBufferWidth() {
        String text = textBufferWidth.getText();
        Double bufferWidth;
        try{
            bufferWidth = Double.parseDouble(text);
        
        } catch(NumberFormatException e){ 
            bufferWidth = new Double(0);
        }
        // set the current and default width
        this.currentWidth  = bufferWidth;
        this.defaultWidth = bufferWidth;
        
        return this.currentWidth;
    }

    private void populateUnitCombo() {
        
        if(comboWidthUnits.getItemCount() != 0){
            return; // this collection is inmutable, only populates the first time 
        }
        
        final Unit defaultUnit = Preferences.bufferUnits();
        final Set<Unit> commonLengthUnits = GeoToolsUtils.getCommonLengthUnits();

        SortedMap<String, Unit> units = new TreeMap<String, Unit>();
        for( Unit unit : commonLengthUnits ) {
            units.put(GeoToolsUtils.getUnitName(unit), unit);
        }
        units.put(GeoToolsUtils.getUnitName(GeoToolsUtils.DEGREES), GeoToolsUtils.DEGREES);

        bufferUnits = new ArrayList<Unit>(units.values());

        int index = 0;
        Set<Entry<String, Unit>> unitset = units.entrySet();
        for( Entry<String, Unit> entry : unitset ) {

            String unitName = entry.getKey();
            comboWidthUnits.add(unitName, index);
            if (defaultUnit.equals(units.get(unitName))) {
                comboWidthUnits.select(index);
            }
            index++;

        }
    }

    /**
     * Clears the user's inputs 
     */
    @Override
    protected final void clearInputs() {
        
        this.currentAggregateOption = null;
        this.currentQuadrantSegments = null;
        this.currentUnits = null;
        this.currentWidth = null;
    }
    
    /**
     * Populates the default options
     */
    @Override
    protected final void populate() {
        
        
        IMap map = getCurrentMap(); 
        if(map == null ) return;

        ILayer sourceLayer = getSourceLayer();
        if(sourceLayer == null) return;
        

        // sets default values
        if(this.currentAggregateOption == null){
         
            boolean mergeGeometries = Preferences.bufferMergeGeometries();
            checkBoxMergeResult.setSelection(mergeGeometries);
            
            this.currentAggregateOption = mergeGeometries;
        }
        
        if (this.currentQuadrantSegments == null) {
            int quadrantSegments = Preferences.bufferQuadrantSegments();
            spinnerQuadrantSegments.setSelection(quadrantSegments);
            
            this.currentQuadrantSegments = quadrantSegments;
        }


        // set default width
            
        //This implementation maintains the last value typed by the user as default
        if(this.defaultWidth == null){
            this.defaultWidth = Preferences.bufferWidth();
        }
        textBufferWidth.setText(String.valueOf(this.defaultWidth));
        
        this.currentWidth = defaultWidth;

        // sets the units 
        populateUnitCombo();
        
        if(this.currentUnits == null){
            
            // sets the maps units as default
            Unit unit = getMapUnits();

            this.currentUnits = unit;
            
            String unitName = GeoToolsUtils.getUnitName(this.currentUnits);
            
            comboWidthUnits.setText(unitName);

            setSelectionUnitOption(this.radioMapUnits);
            this.comboWidthUnits.setEnabled(false);

        
        }
        
        validate(); 

    }

    
    /**
     * Sets enable/disable the composite's widgets
     */
    @Override
    public void setEnabled( boolean enabled ) {
        
        this.textBufferWidth.setEnabled(enabled);
        this.radioLayerUnits.setEnabled(enabled);
        this.radioMapUnits.setEnabled(enabled);
        this.radioSpecifyUnits.setEnabled(enabled);
        
        this.checkBoxMergeResult.setEnabled(enabled);
        this.spinnerQuadrantSegments.setEnabled(enabled);
    }
    
    /**
     * If  layer units is selected get the units for the selected layer.
     * @param selectedLayer
     */
    private void selectedSourceLayerActions(final ILayer selectedLayer ) {
        
        if(!this.radioLayerUnits.getSelection()){
            return;
        }
        selectedLayerUnitsActions(selectedLayer);
    }

    private void selectedLayerUnitsActions(final ILayer layer) {
        
        if(layer == null) {
            return;
        }
        
        // gets the units from layer and sets the combo with this units
        this.currentUnits = getLayerUnits(layer);
        
        final String unitName = GeoToolsUtils.getUnitName(this.currentUnits);
        this.comboWidthUnits.setText(unitName);
        
        this.comboWidthUnits.setEnabled(false);
        
        validate();
    }

    /**
     * Sets the unit's layer in units combobox
     */
    private void selectedLayerUnitsActions() {
        
        ILayer layer = getSourceLayer();

        selectedLayerUnitsActions(layer);
    }

    /**
     * Sets the unit's map in units combobox
     */
    private void selectedMapUnitsActions() {
     
        // gets the units from map and sets the combo with this units
        this.currentUnits = getMapUnits();
        final String unitName = GeoToolsUtils.getUnitName(this.currentUnits);
        this.comboWidthUnits.setText(unitName);
        
        this.comboWidthUnits.setEnabled(false);
        
        validate();
    }

    /**
     * Enables the combo to specify the units 
     */
    private void selectedSpecifiyUnitsActions() {
        
        this.comboWidthUnits.setEnabled(true);
    }

    /**
     * Validate the currente option's values
     *
     */
    protected final void validate() {

        BufferController ctrl = (BufferController)getController(); 
        if(ctrl == null) return; 
        
        if (!ctrl.isRunning()) {
            return;
        }

        // gets the options values and sends it to controller which does the
        // validation.
        Boolean aggregate           = getAggregateOption();

        Double width                = getBufferWidth();

        Unit units                  = getBufferUnitOfMeasure();

        Integer cuadrantSegments    = getQuadrantSegments();

        ctrl.setOptions(aggregate , width , units , cuadrantSegments );
        
        ctrl.validate();

    }
    
    /**
     * if the layer deleted is the current source layer,
     * changes the units option to map units 
     */
    @Override
    protected final void removedLayerActions( ILayer layer ) {
        super.removedLayerActions(layer);

        //
        ILayer sourceLayer = getSourceLayer();
        
        if( sourceLayer.equals(layer) ){

            this.currentUnits = null;
            setSelectionUnitOption(this.radioMapUnits);
        }
        
        populate();
    }
    

    /**
     * @param selectedRadio
     */
    private void setSelectionUnitOption( final Button selectedRadio) {
        
        this.radioMapUnits.setSelection(this.radioMapUnits == selectedRadio);
        this.radioLayerUnits.setSelection(this.radioLayerUnits == selectedRadio);
        
        this.radioSpecifyUnits.setSelection(this.radioSpecifyUnits == selectedRadio);
        
        
    }

    @Override
    public final void changedLayerListActions() {

        
        this.currentUnits = null;
        populate();
    }

    /**
     * Gets the aggregate option and set de current value.
     *
     * @return true if aggregate was selected.
     */
    private Boolean getAggregateOption() {
        
        this.currentAggregateOption =  this.checkBoxMergeResult.getSelection();
        
        return this.currentAggregateOption;
    }




    

    

} // @jve:decl-index=0:visual-constraint="10,10"
