/*
 * uDig Spatial Operations - Tutorial - http://www.axios.es (C) 2009,
 * Axios Engineering S.L. This product is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This product is distributed as part of tutorial, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.tutorial.ui.centroid;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.ui.common.TargetLayerListenerAdapter;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
/**
 * 
 * Displays the widgets required to sets the centroid parameters.
 *
 * @author Mauricio Pazos (www.axios.es)
 *
 */
public final class SOCentroidComposite extends AggregatedPresenter {

	private static final int		GRID_DATA_1_WIDTH_HINT				= 130;
	private static final int		GRID_DATA_2_WIDTH_HINT				= 150;
	private static final int		GRID_DATA_4_WIDTH_HINT				= 45;
	
	private static final String		SOURCE_LEGEND				= "SourceLegend";		//$NON-NLS-1$
	
	private TabFolder 				tabFolder;
	private Composite 				basicComposite;
	private Group 					groupSource;
	private CCombo 					cComboSourceLayer;
	private CLabel 					sourceLegend;
	private CLabel 					cLabelCountFeaturesInSourceLayer;
	private ILayer 					currentSourceLayer;
	private ResultLayerComposite 	resultComposite;


	public SOCentroidComposite(Composite parent, int style) {
		super(parent, style);
		
		super.initialize();
	}

	@Override
	protected void createContents() {

		GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(gridData);

		basicComposite = new Composite(tabFolder, SWT.NONE);
		basicComposite.setLayoutData(gridData);
		basicComposite.setLayout(gridLayout);

		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;

		createSourceGroup(basicComposite);
		createGroupResult(basicComposite);

		this.setLayout(gridLayout1);
		setSize(new Point(541, 202));

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("basic"); //$NON-NLS-1$
		tabItem.setControl(basicComposite);
		

	}

	/**
	 * This method initializes source group
	 */
	private void createSourceGroup(Composite parentComposite) {

		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.BEGINNING;
		gridData11.grabExcessHorizontalSpace = false;
		gridData11.verticalAlignment = GridData.CENTER;
		gridData11.widthHint = GRID_DATA_1_WIDTH_HINT;

		GridData gridData12 = new GridData();
		gridData12.horizontalAlignment = GridData.BEGINNING;
		gridData12.grabExcessHorizontalSpace = false;
		gridData12.verticalAlignment = GridData.CENTER;
		gridData12.widthHint = GRID_DATA_2_WIDTH_HINT;

		GridData gridData13 = new GridData();
		gridData13.horizontalAlignment = GridData.BEGINNING;
		gridData13.grabExcessHorizontalSpace = false;
		gridData13.verticalAlignment = GridData.CENTER;
		// gridData13.widthHint = GRID_DATA_3_WIDTH_HINT;

		GridData gridData14 = new GridData();
		gridData14.horizontalAlignment = GridData.BEGINNING;
		gridData14.grabExcessHorizontalSpace = false;
		gridData14.verticalAlignment = GridData.CENTER;
		gridData14.widthHint = GRID_DATA_4_WIDTH_HINT;

		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.BEGINNING;
		gridData5.grabExcessHorizontalSpace = false;
		gridData5.verticalAlignment = GridData.CENTER;

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;

		groupSource = new Group(parentComposite,SWT.NONE);
		groupSource.setText("source"); //$NON-NLS-1$
		groupSource.setLayout(gridLayout);
		groupSource.setLayoutData(gridData);

		CLabel cLabel = new CLabel(groupSource, SWT.NONE);
		cLabel.setText("source:"); //$NON-NLS-1$
		cLabel.setLayoutData(gridData11);

		cComboSourceLayer = new CCombo(groupSource, SWT.BORDER);
		cComboSourceLayer.setLayoutData(gridData12);
		cComboSourceLayer.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedSourceLayerActions(cComboSourceLayer);

			}
		});
		sourceLegend = new CLabel(groupSource, SWT.NONE);
		sourceLegend.setLayoutData(gridData5);
		ImageRegistry imagesRegistry = CreateImageRegistry();
		sourceLegend.setImage(imagesRegistry.get(SOURCE_LEGEND));		
		

		CLabel cLabelSelected = new CLabel(groupSource, SWT.NONE);
		cLabelSelected.setText("selection:"); //$NON-NLS-1$
		cLabelSelected.setLayoutData(gridData13);

		 cLabelCountFeaturesInSourceLayer = new CLabel(groupSource, SWT.NONE);
		cLabelCountFeaturesInSourceLayer.setText(""); //$NON-NLS-1$
		cLabelCountFeaturesInSourceLayer.setLayoutData(gridData14);
	}

	private ImageRegistry CreateImageRegistry() {

		ImageRegistry registry = new ImageRegistry();

		String imgFile = "images/" + SOURCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(SOURCE_LEGEND, ImageDescriptor.createFromFile(SOCentroidComposite.class, imgFile));

		return registry;
	}
	

	/**
	 * Target layer widgets
	 */
	private void createGroupResult(Composite basicComposite) {

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.verticalAlignment = GridData.BEGINNING;

		Group groupTargetInputs = new Group(basicComposite, SWT.NONE);
		groupTargetInputs.setText("result"); //$NON-NLS-1$
		groupTargetInputs.setLayout(new GridLayout());
		groupTargetInputs.setLayoutData(gridData);

		this.resultComposite = new ResultLayerComposite(this, groupTargetInputs, SWT.NONE, GRID_DATA_1_WIDTH_HINT);

		GridData resultCompositeGridData = new GridData();
		resultCompositeGridData.horizontalAlignment = GridData.FILL;
		resultCompositeGridData.grabExcessHorizontalSpace = true;
		resultCompositeGridData.grabExcessVerticalSpace = true;
		resultCompositeGridData.verticalAlignment = GridData.FILL;

		this.resultComposite.setLayoutData(resultCompositeGridData);

		this.resultComposite.addSpecifiedLayerListener(new TargetLayerListenerAdapter() {
			
			@Override
			public void validateTargetLayer() {
				validateParameters();
			}
		});
	}

	/**
	 * populates the widgets and sets the default parameters for the centroid
	 */
	@Override
	protected void populate() {

		loadComboWithLayerList(this.cComboSourceLayer,
				ParameterName.SOURCE_GEOMETRY_CLASS); 

		selectDefaultLayer();
	}
	
	/**
	 * Actions associated with layer selection.
	 * 
	 * @param comboLayer
	 * @param textFeatures
	 */
	private void selectedSourceLayerActions(final CCombo comboLayer) {

		ILayer selectedLayer = getSelecedLayer(comboLayer);

		if (selectedLayer == null)
			return;

		setCurrentSourceLayer(selectedLayer);
	}

	/**
	 * Sets the current source layer and dispatch the event to its listeners.
	 * 
	 * @param selectedLayerInMap
	 */
	private void setCurrentSourceLayer(ILayer selectedLayer) {

		if( selectedLayer == null) return;

		this.currentSourceLayer = selectedLayer;
		

		presentSelectionAllOrBBox(
					this.currentSourceLayer, 
					this.currentSourceLayer.getFilter(),
					this.cLabelCountFeaturesInSourceLayer);

		validateParameters();

	}

	/**
	 * Presents the selected layer in map as source layer.
	 * 
	 * @param ctrl
	 */
	private ILayer selectDefaultLayer() {

		if (this.currentSourceLayer == null) {
			
			this.currentSourceLayer = getContext().getSelectedLayer();

		}
		setCurrentSourceLayer(this.currentSourceLayer);
		changeSelectedLayer(this.currentSourceLayer, this.cComboSourceLayer);

		return this.currentSourceLayer;
	}
	
	/**
	 * Sets the selected features on layer and presents All or BBox.
	 * 
	 * @param currentLayer
	 * @param filter
	 */
	private void setSelectedFeatures(final ILayer currentLayer, final Filter filter) {

		if (!currentLayer.equals(this.currentSourceLayer)) {
			return; // only presents the feature of current source layer
		}

		presentSelectionAllOrBBox(this.currentSourceLayer, filter, this.cLabelCountFeaturesInSourceLayer);
	}

	/**
	 * Changes the count of features selected of the selected layer
	 */
	@Override
	protected final void changedFilterSelectionActions(final ILayer layer, final Filter newFilter) {

		assert layer != null;
		assert newFilter != null;

		setSelectedFeatures(layer, newFilter);
	}
	
	/**
	 * Synchronizes the content with map model
	 */
	@Override
	protected final void changedLayerListActions() {

		populate();

		selectedSourceLayerActions(this.cComboSourceLayer);
	}

	/**
	 * This method grabs the data from each composite's widget and sets 
	 * the command centroid's  parameters
	 */
	@Override
	protected void setParametersOnCommand( ISOCommand command) {

		CentroidCommand cmd = (CentroidCommand) command;

		// sets the source layer.
		cmd.setSourceLayer(this.currentSourceLayer);
		
		// sets the filter
		Filter filter = null;
		if (this.currentSourceLayer != null) {
			filter = getFilter(this.currentSourceLayer);
		}
		cmd.setSourceFilter(filter);
		
		// sets the map data
		IMap map = getCurrentMap();
		cmd.setMap(map);

		// sets the target or result layer
		if (this.resultComposite.isLayerSelected()) {
			// an existent layer has been selected
			ILayer targetLayer = this.resultComposite.getCurrentTargetLayer();

			cmd.setTargetLayer(targetLayer);

		} else {
			// requires create a new layer
			final String layerName = this.resultComposite.getNewLayerName();
			Class<? extends Geometry> targetGeomClass = this.resultComposite.getTargetClass();

			cmd.setTargetLayerName(layerName);
			cmd.setTargetLayerGeometry(targetGeomClass);
		}
	}

}
