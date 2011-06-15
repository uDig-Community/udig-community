/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
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
import net.refractions.udig.project.IMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.preferences.Preferences;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.SpatialJoinGeomCommand;
import es.axios.udig.spatialoperations.tasks.SpatialRelation;
import es.axios.udig.spatialoperations.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.ui.common.TargetLayerListenerAdapter;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Spatial Join Geometry Composite
 * <p>
 * This composite presents the widgets required to get the Spatial Join
 * Geometries parameters.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
final class SpatialJoinGeomComposite extends AggregatedPresenter {

	private static final int		GRID_DATA_1_WIDTH_HINT				= 135;
	private static final int		GRID_DATA_2_WIDTH_HINT				= 150;
	// private static final int GRID_DATA_3_WIDTH_HINT = 210;
	private static final int		GRID_DATA_4_WIDTH_HINT				= 45;
	private static final String		SOURCE_LEGEND						= "SourceLegend";		//$NON-NLS-1$
	private static final String		REFERENCE_LEGEND					= "ReferenceLegend";	//$NON-NLS-1$

	// widgets controls
	private Group					groupSourceInputs					= null;
	private CCombo					cComboFirstLayer					= null;
	private CCombo					cComboRelations						= null;
	private CCombo					cComboSecondLayer					= null;
	private Group					groupTargetInputs					= null;
	private ResultLayerComposite	resultComposite						= null;
	private CLabel					cLabelCountFeaturesInFirstLayer		= null;
	private CLabel					cLabelCountFeaturesInSecondLayer	= null;
	private CLabel					sourceLegend						= null;
	private CLabel					referenceLegend						= null;
	private Button					checkBoxSelection					= null;

	private TabFolder				tabFolder							= null;
	private Composite				basicComposite						= null;
	private Composite				advancedComposite					= null;

	private ImageRegistry			imagesRegistry						= null;

	// parameters
	private ILayer					currentFirstLayer					= null;
	private ILayer					currentSecondLayer					= null;
	private SpatialRelation			currentRelation						= null;
	private Boolean					currentSelection					= null;
	private Boolean					defaultSelection					= null;

	// list of Spatial join
	private final String[]			spatialJoinOperations				= { "Disjoint", //$NON-NLS-1$
			"Touches", //$NON-NLS-1$
			"Crosses", //$NON-NLS-1$
			"Within", //$NON-NLS-1$
			"Contains", //$NON-NLS-1$
			"Intersects", //$NON-NLS-1$
			"Equals"													};						//$NON-NLS-1$

	public SpatialJoinGeomComposite(Composite parent, int style) {

		super(parent, style);
		super.initialize();
	}

	@Override
	protected void initState() {

		ISOCommand cmd = getCommand();
		cmd.addObserver(this.resultComposite);
	}

	@Override
	protected void createContents() {

		GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		this.imagesRegistry = CreateImageRegistry();

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

		advancedComposite = new Composite(tabFolder, SWT.NONE);
		advancedComposite.setLayoutData(gridData);
		advancedComposite.setLayout(gridLayout);

		createSourceInput();
		createGroupResult();

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.Composite_tab_folder_basic);
		tabItem.setControl(basicComposite);

		// TODO add advanced options
		// TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
		// tabItem1.setText("Advanced");
		// tabItem1.setControl(advancedComposite);

	}

	private ImageRegistry CreateImageRegistry() {

		ImageRegistry registry = new ImageRegistry();

		String imgFile = "images/" + SOURCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(SOURCE_LEGEND, ImageDescriptor.createFromFile(SpatialJoinGeomComposite.class, imgFile));

		imgFile = "images/" + REFERENCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(REFERENCE_LEGEND, ImageDescriptor.createFromFile(SpatialJoinGeomComposite.class, imgFile));

		return registry;
	}

	/**
	 * This method initializes groupSourceInputs
	 * 
	 */
	private void createSourceInput() {

		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.BEGINNING;
		gridData11.grabExcessHorizontalSpace = false;
		gridData11.grabExcessVerticalSpace = true;
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

		GridData gridData21 = new GridData();
		gridData21.horizontalAlignment = GridData.BEGINNING;
		gridData21.grabExcessHorizontalSpace = false;
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
		// gridData23.widthHint = GRID_DATA_3_WIDTH_HINT;

		GridData gridData24 = new GridData();
		gridData24.horizontalAlignment = GridData.BEGINNING;
		gridData24.grabExcessHorizontalSpace = false;
		gridData24.verticalAlignment = GridData.CENTER;
		gridData24.widthHint = GRID_DATA_4_WIDTH_HINT;

		GridData gridData31 = new GridData();
		gridData31.horizontalAlignment = GridData.BEGINNING;
		gridData31.grabExcessHorizontalSpace = false;
		gridData31.grabExcessVerticalSpace = true;
		gridData31.verticalAlignment = GridData.CENTER;
		gridData31.widthHint = GRID_DATA_1_WIDTH_HINT;

		GridData gridData32 = new GridData();
		gridData32.horizontalAlignment = GridData.BEGINNING;
		gridData32.grabExcessHorizontalSpace = false;
		gridData32.grabExcessVerticalSpace = true;
		gridData32.verticalAlignment = GridData.CENTER;
		gridData32.widthHint = GRID_DATA_2_WIDTH_HINT;

		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.BEGINNING;
		gridData5.grabExcessHorizontalSpace = false;
		gridData5.verticalAlignment = GridData.CENTER;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;

		groupSourceInputs = new Group(basicComposite, SWT.NONE);
		groupSourceInputs.setText(Messages.Composite_source_group); //$NON-NLS-1$
		groupSourceInputs.setLayout(gridLayout);
		groupSourceInputs.setLayoutData(gridData);

		CLabel cLabel = new CLabel(groupSourceInputs, SWT.NONE);
		cLabel.setText(Messages.SpatialJoinGeomComposite_First_Layer + ":"); //$NON-NLS-1$
		cLabel.setLayoutData(gridData11);

		// first source layer
		cComboFirstLayer = new CCombo(groupSourceInputs, SWT.BORDER | SWT.READ_ONLY);
		cComboFirstLayer.setLayoutData(gridData12);
		cComboFirstLayer.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedFirstLayerActions();

			}
		});

		sourceLegend = new CLabel(groupSourceInputs, SWT.NONE);
		sourceLegend.setText(""); //$NON-NLS-1$
		sourceLegend.setLayoutData(gridData5);
		sourceLegend.setImage(this.imagesRegistry.get(SOURCE_LEGEND));

		CLabel cLabelSelectedFeaturesInSecond = new CLabel(groupSourceInputs, SWT.NONE);
		cLabelSelectedFeaturesInSecond.setText(Messages.SpatialJoinGeomComposite_First_Selected_Features + ":"); //$NON-NLS-1$
		cLabelSelectedFeaturesInSecond.setLayoutData(gridData13);

		cLabelCountFeaturesInFirstLayer = new CLabel(groupSourceInputs, SWT.NONE);
		cLabelCountFeaturesInFirstLayer.setText(""); //$NON-NLS-1$
		cLabelCountFeaturesInFirstLayer.setLayoutData(gridData14);

		// Relation controls initialization
		CLabel cLabelRelation = new CLabel(groupSourceInputs, SWT.NONE);
		cLabelRelation.setText(Messages.SpatialJoinGeomComposite_Relation + ":"); //$NON-NLS-1$
		cLabelRelation.setLayoutData(gridData31);

		cComboRelations = new CCombo(groupSourceInputs, SWT.BORDER | SWT.READ_ONLY);
		cComboRelations.setLayoutData(gridData32);
		cComboRelations.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedRelationActions();

			}
		});

		// fill labels
		new Label(groupSourceInputs, SWT.NONE);
		new Label(groupSourceInputs, SWT.NONE);
		new Label(groupSourceInputs, SWT.NONE);

		// second layer initialization
		CLabel cLabelSecondLayer = new CLabel(groupSourceInputs, SWT.NONE);
		cLabelSecondLayer.setText(Messages.SpatialJoinGeomComposite_Second_Layer + ":"); //$NON-NLS-1$
		cLabelSecondLayer.setLayoutData(gridData21);

		cComboSecondLayer = new CCombo(groupSourceInputs, SWT.BORDER | SWT.READ_ONLY);
		cComboSecondLayer.setLayoutData(gridData22);
		cComboSecondLayer.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedSecondLayerActions();

			}
		});

		referenceLegend = new CLabel(groupSourceInputs, SWT.NONE);
		referenceLegend.setText(""); //$NON-NLS-1$
		referenceLegend.setLayoutData(gridData5);
		referenceLegend.setImage(this.imagesRegistry.get(REFERENCE_LEGEND));

		CLabel cLabelFeaturesInSecond = new CLabel(groupSourceInputs, SWT.NONE);
		cLabelFeaturesInSecond.setText(Messages.SpatialJoinGeomComposite_Second_Selected_Features + ":"); //$NON-NLS-1$
		cLabelFeaturesInSecond.setLayoutData(gridData23);

		cLabelCountFeaturesInSecondLayer = new CLabel(groupSourceInputs, SWT.NONE);
		cLabelCountFeaturesInSecondLayer.setText(""); //$NON-NLS-1$
		cLabelCountFeaturesInSecondLayer.setLayoutData(gridData24);
	}

	protected void selectedSecondLayerActions() {

		ILayer selectedLayer = getSelecedLayer(this.cComboSecondLayer);
		if (selectedLayer == null) {
			return;
		}

		setCurrentSecondLayer(selectedLayer);

		validateParameters();
	}

	protected void selectedRelationActions() {
		int index = cComboRelations.getSelectionIndex();
		String relation = null;
		if (index == -1) {
			// sets the first relation as default
			relation = this.cComboRelations.getItem(0);
		} else {
			relation = cComboRelations.getItem(index);
		}
		this.currentRelation = (SpatialRelation) cComboRelations.getData(relation);
		this.cComboRelations.setToolTipText(getToolTip(this.currentRelation));

		SpatialJoinGeomCommand cmd = (SpatialJoinGeomCommand) getCommand();
		cmd.setSpatialRelation(this.currentRelation);
		populate();
		validateParameters();
	}

	/**
	 * Reinitialize parameter values
	 */
	@Override
	protected final void clearInputs() {

		SpatialJoinGeomCommand cmd = (SpatialJoinGeomCommand) getCommand();

		this.currentFirstLayer = cmd.getFirstLayer();
		this.currentSecondLayer = cmd.getSecondLayer();

		tabFolder.setSelection(tabFolder.getItem(0));

	}

	/**
	 * Retrieves the description for the relation.
	 * 
	 * @param relation
	 * @return relation's description
	 */
	private String getToolTip(SpatialRelation relation) {

		String description = ""; //$NON-NLS-1$

		switch (relation) {
		case Contains:
			description = Messages.SpatialJoinGeomComposite_contains;
			break;
		case Crosses:
			description = Messages.SpatialJoinGeomComposite_crosses;
			break;
		case Covers:
			description = Messages.SpatialJoinGeomComposite_covers;
			break;
		case Disjoint:
			description = Messages.SpatialJoinGeomComposite_disjoint;
			break;
		case Equals:
			description = Messages.SpatialJoinGeomComposite_equals;
			break;
		case Intersects:
			description = Messages.SpatialJoinGeomComposite_intersects;
			break;
		case IsCoverBy:
			description = Messages.SpatialJoinGeomComposite_is_cover_by;
			break;
		case Overlaps:
			description = Messages.SpatialJoinGeomComposite_overlaps;
			break;
		case Touches:
			description = Messages.SpatialJoinGeomComposite_touches;
			break;
		case Within:
			description = Messages.SpatialJoinGeomComposite_within;
			break;

		default:
			break;
		}

		return description;
	}

	protected void selectedFirstLayerActions() {

		ILayer selectedLayer = getSelecedLayer(this.cComboFirstLayer);
		if (selectedLayer == null) {
			return;
		}

		setCurrentFirstLayer(selectedLayer);

		validateParameters();
	}

	/**
	 * Maintains the consistency between the presented layers and features in
	 * map model and this view
	 */
	@Override
	protected final void changedLayerListActions() {

		// change the list of layers
		this.cComboFirstLayer.removeAll();
		this.cComboSecondLayer.removeAll();

		populate();

		// update the selection

		selectedFirstLayerActions();
		selectedSecondLayerActions();

	}

	/**
	 * Target layer widgets
	 * 
	 */
	private void createGroupResult() {

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.verticalAlignment = GridData.BEGINNING;

		groupTargetInputs = new Group(basicComposite, SWT.NONE);
		groupTargetInputs.setText(Messages.IntersectComposite_result);
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

		checkBoxSelection = new Button(groupTargetInputs, SWT.CHECK);
		checkBoxSelection.setText(Messages.SpatialJoinGeomComposite_checkSelection_text);
		checkBoxSelection.setToolTipText(Messages.SpatialJoinGeomComposite_checkSelection_tooltip);
		checkBoxSelection.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {

				validateParameters();

				setEnableResultComposite(currentSelection);
				populate();
			}
		});
		checkBoxSelection.setLayoutData(gridData);

	}

	/**
	 * Populate the composite widgets with the layers, relations, and options.
	 */
	@Override
	protected void populate() {


		this.currentSelection = this.defaultSelection;
		if (this.currentSelection == null) {
			this.currentSelection = Preferences.spatialJoinResultSelection();
		}

		this.checkBoxSelection.setSelection(this.currentSelection);

		loadComboWithLayerList(this.cComboFirstLayer);
		loadComboWithLayerList(this.cComboSecondLayer);

		loadRelations();

		selectDefaultLayer();
		
		setEnableResultComposite(this.currentSelection);
	}

	/**
	 * Sets the selected layer in map has default for first layer.
	 */
	private void selectDefaultLayer() {

		SpatialJoinGeomCommand cmd = (SpatialJoinGeomCommand) getCommand();
		this.currentFirstLayer = cmd.getFirstLayer();
		if (this.currentFirstLayer == null) {
			ILayer selected = MapUtil.getSelectedLayer(getCurrentMap());
			if (LayerUtil.isCompatible(selected, cmd.getDomainValues(ParameterName.SOURCE_GEOMETRY_CLASS))) {
				this.currentFirstLayer = selected;
			}
		}

		setCurrentFirstLayer(this.currentFirstLayer);
		changeSelectedLayer(this.currentFirstLayer, this.cComboFirstLayer);

		this.currentSecondLayer = cmd.getSecondLayer();
		setCurrentSecondLayer(this.currentSecondLayer);
		changeSelectedLayer(this.currentSecondLayer, this.cComboSecondLayer);

	}

	/**
	 * Load the combo with the spatial relations.
	 */
	private void loadRelations() {

		if (cComboRelations.getItemCount() == 0) {
			// the relation set do not change then only load the relations the
			// first time

			for (SpatialRelation r : SpatialRelation.values()) {
				// Filter
				for (int i = 0; i < spatialJoinOperations.length; i++) {
					if (spatialJoinOperations[i].equals(r.toString())) {
						cComboRelations.add(r.toString());
						cComboRelations.setData(r.toString(), r);
						break;
					}
				}
			}
			cComboRelations.select(0);
			selectedRelationActions();
		}

	}

	private void setCurrentFirstLayer(final ILayer selectedLayer) {

		if (selectedLayer == null) {
			return;
		}

		this.currentFirstLayer = selectedLayer;

		presentSelectionAllOrBBox(this.currentFirstLayer, this.currentFirstLayer.getFilter(),
					this.cLabelCountFeaturesInFirstLayer);
	}

	private void setCurrentSecondLayer(final ILayer selectedLayer) {

		if (selectedLayer == null) {
			return;
		}

		this.currentSecondLayer = selectedLayer;

		presentSelectionAllOrBBox(this.currentSecondLayer, this.currentSecondLayer.getFilter(),
					this.cLabelCountFeaturesInSecondLayer);
	}

	/**
	 * Sets the spatial join command's parameters
	 */
	@Override
	protected void setParametersOnCommand(ISOCommand command) {

		SpatialJoinGeomCommand cmd = (SpatialJoinGeomCommand) command;

		IMap map = getCurrentMap();
		assert map != null;
		CoordinateReferenceSystem mapCrs = MapUtil.getCRS(map);

		Filter filterInFirstLayer = null;
		CoordinateReferenceSystem firstLayerCRS = null;
		Filter filterInSecondLayer = null;
		CoordinateReferenceSystem secondLayerCRS = null;

		if (this.currentFirstLayer != null) {
			firstLayerCRS = this.currentFirstLayer.getCRS();
			filterInFirstLayer = getFilter(this.currentFirstLayer);
		}
		if (this.currentSecondLayer != null) {
			secondLayerCRS = this.currentSecondLayer.getCRS();
			filterInSecondLayer = getFilter(this.currentSecondLayer);
		}

		Boolean selection = getSelectionOption();

		setEnableResultComposite(selection);

		// Start setting the parameters on the command.
		cmd.setInputParams(this.currentFirstLayer, firstLayerCRS, this.currentSecondLayer, secondLayerCRS,
					this.currentRelation, mapCrs, filterInFirstLayer, filterInSecondLayer, selection);

		if (this.resultComposite.isLayerSelected()) {

			ILayer targetLayer = this.resultComposite.getCurrentTargetLayer();

			cmd.setOutputParams(targetLayer);

		} else {
			// requires create a new layer
			final String layerName = this.resultComposite.getNewLayerName();
			if (layerName != null) {
				final Class<? extends Geometry> targetGeomClass = this.resultComposite.getTargetClass();

				cmd.setOutputParams(layerName, mapCrs, targetGeomClass);

			}
		}
	}

	/**
	 * Gets the selection option and set the current value.
	 * 
	 * @return true if selection was selected.
	 */
	private Boolean getSelectionOption() {

		this.currentSelection = this.checkBoxSelection.getSelection();

		this.defaultSelection = this.currentSelection;

		return this.currentSelection;
	}

	/**
	 * If the checkboxSelection is checked, enable / disable the result
	 * composite.
	 */
	private void setEnableResultComposite(boolean selected) {
		this.resultComposite.setEnabled(!selected);
	}

	@Override
	public void setEnabled(boolean enabled) {

		cComboFirstLayer.setEnabled(enabled);
		cComboRelations.setEnabled(enabled);
		cComboSecondLayer.setEnabled(enabled);
		checkBoxSelection.setEnabled(enabled);
		
		resultComposite.setEnabled(!this.currentSelection);

	}

	/**
	 * Changes the count of features selected of the selected layer
	 */
	@Override
	protected final void changedFilterSelectionActions(final ILayer layer, final Filter newFilter) {

		if (layer.equals(this.currentFirstLayer)) {

			presentSelectionAllOrBBox(this.currentFirstLayer, newFilter, this.cLabelCountFeaturesInFirstLayer);

		}
		if (layer.equals(this.currentSecondLayer)) {

			presentSelectionAllOrBBox(this.currentSecondLayer, newFilter, this.cLabelCountFeaturesInSecondLayer);

		}
	}

	@Override
	protected void removeLayerListActions(ILayer layer) {

		if (layer.equals(this.currentFirstLayer)) {
			currentFirstLayer = null;
		}
		validateParameters();
	}

} // @jve:decl-index=0:visual-constraint="10,10"
