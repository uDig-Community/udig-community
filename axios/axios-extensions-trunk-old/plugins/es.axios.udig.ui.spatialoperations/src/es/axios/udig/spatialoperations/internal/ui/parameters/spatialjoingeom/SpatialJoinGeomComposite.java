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
package es.axios.udig.spatialoperations.internal.ui.parameters.spatialjoingeom;

import net.refractions.udig.project.ILayer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import es.axios.udig.spatialoperations.internal.control.SpatialJoinGeomController;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.internal.ui.common.SpecifiedLayerListener;
import es.axios.udig.spatialoperations.internal.ui.parameters.AggregatedPresenter;

/**
 * Spatial Join Geom Composite 
 * <p>
 * This composite present the widgets required to get the 
 * Spatial Join Geometries parameters.
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class SpatialJoinGeomComposite extends AggregatedPresenter {

    private CLabel               cLabel            = null;
    private CLabel               cLabel1           = null;
    private CLabel               cLabel2           = null;
    private CLabel               cLabel3           = null;
    private CLabel               cLabel4           = null;
    private CLabel               cLabel5           = null;
    private CLabel               cLabel6           = null;

    private Group                groupSourceInputs = null;
    private CCombo               cComboFirstLayer  = null;
    private CCombo               cComboRelations   = null;
    private CCombo               cComboSecondLayer = null;
    private Group                groupTargetInputs;
    private ResultLayerComposite resultComposite;

    // Spatial filter names
    private final String[]       filterNames       = new String[]{
            "intersect", "overlaps",
            "contains", "covers", "is-cover-by", 
            "crosses", "disjoint", "equals", "overlap",
            "within", "is-within-distance"         };
    
    
    public SpatialJoinGeomComposite( Composite parent, int style ) {
        super(parent, style);
        super.initialize();
    }

    @Override
    protected void createContents() {

        GridLayout gridLayout = new GridLayout();
        setLayout(gridLayout);

        createSourceInput();
        createGroupTargetInputs();
    }

    /**
     * This method initializes groupSourceInputs	
     *
     */
    private void createSourceInput() {
        GridData gridData9 = new GridData();
        gridData9.widthHint = 100;
        GridData gridData8 = new GridData();
        gridData8.widthHint = 100;
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = GridData.FILL;
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.verticalAlignment = GridData.CENTER;
        GridData gridData3 = new GridData();
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.verticalAlignment = GridData.CENTER;
        gridData3.horizontalAlignment = GridData.FILL;
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.verticalAlignment = GridData.CENTER;
        gridData2.horizontalAlignment = GridData.FILL;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.CENTER;
        groupSourceInputs = new Group(this, SWT.NONE);
        groupSourceInputs.setText("Source");
        groupSourceInputs.setLayout(gridLayout);
        groupSourceInputs.setLayoutData(gridData);
        cLabel = new CLabel(groupSourceInputs, SWT.NONE);
        cLabel.setText("First Layer");
        cComboFirstLayer = new CCombo(groupSourceInputs, SWT.NONE);
        cComboFirstLayer.setLayoutData(gridData2);
        cLabel1 = new CLabel(groupSourceInputs, SWT.NONE);
        cLabel1.setText("Selected Features");
        cLabel2 = new CLabel(groupSourceInputs, SWT.BORDER);
        cLabel2.setText("0");
        cLabel2.setLayoutData(gridData8);
        cLabel3 = new CLabel(groupSourceInputs, SWT.NONE);
        cLabel3.setText("Relation");
        cComboRelations = new CCombo(groupSourceInputs, SWT.NONE);
        cComboRelations.setLayoutData(gridData3);
        Label filler = new Label(groupSourceInputs, SWT.NONE);
        Label filler1 = new Label(groupSourceInputs, SWT.NONE);
        cLabel4 = new CLabel(groupSourceInputs, SWT.NONE);
        cLabel4.setText("Second Layer");
        cComboSecondLayer = new CCombo(groupSourceInputs, SWT.NONE);
        cComboSecondLayer.setLayoutData(gridData4);
        cLabel5 = new CLabel(groupSourceInputs, SWT.NONE);
        cLabel5.setText("Selected Features");
        cLabel6 = new CLabel(groupSourceInputs, SWT.BORDER);
        cLabel6.setText("0");
        cLabel6.setLayoutData(gridData9);
    }

    /**
     * Target layer widgets
     *
     */
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

    @Override
    protected void populate() {
        
        
        loadComboWithLayerList(this.cComboFirstLayer);
        loadComboWithLayerList(this.cComboSecondLayer);
        
        if(cComboRelations.getItemCount() == 0){
            // only load the relations the first time

            for( int i = 0; i < filterNames.length; i++ ) {
                cComboRelations.add(filterNames[i]);
            }
        }
        selectDefaultLayer();
        
        // validat is required to set the initial values in the associated command
        validate();
        
    }

    @Override
    public String getOperationName(){
        // default 
        return "Spatial Join Geometries"; //$NON-NLS-1$
    };


    /**
     *
     */
    private void selectDefaultLayer() {
    }

    /**
     *
     * @param layerName
     */
    protected void requiredFeatureTypeActions( String layerName ) {
    }

    /**
     *
     * @param selectedLayer
     */
    protected void selectedTargetLayerActions( ILayer selectedLayer ) {
    }

    /**
     *
     */
    private void validate() {
        
        SpatialJoinGeomController ctrl = (SpatialJoinGeomController) getController();
        // TODO sets parameters

        ctrl.validate();
    }
    
}  //  @jve:decl-index=0:visual-constraint="10,10"
