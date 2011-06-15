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
package es.axios.udig.spatialoperations.internal.ui.parameters.fill;

import net.refractions.udig.project.ILayer;

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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.FillCommand;
import es.axios.udig.spatialoperations.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.ui.common.TargetLayerListenerAdapter;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Input data for fill operation.
 * <p>
 * This contains the widgets required to capture the inputs for fill operation.
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
final class FillComposite extends AggregatedPresenter {

	private static final int		GRID_DATA_1_WIDTH_HINT		= 105;
	private static final int		GRID_DATA_2_WIDTH_HINT		= 150;
	private static final int		GRID_DATA_4_WIDTH_HINT		= 45;
	private static final String		SOURCE_LEGEND				= "SourceLegend";		//$NON-NLS-1$
	private static final String		REFERENCE_LEGEND			= "ReferenceLegend";	//$NON-NLS-1$

	// widgets
	private Group					groupSourceInputs			= null;
	private Group					groupTargetInputs			= null;

	private Button					checkButtonCopy				= null;
	private ResultLayerComposite	resultComposite				= null;
	private CLabel					cLabel						= null;
	private CLabel					cLabel1						= null;
	private CLabel					cLabelFeaturesInFirstLayer	= null;
	private CLabel					cLabel3						= null;
	private CCombo					comboSecondLayer			= null;
	private CLabel					cLabel4						= null;
	private CLabel					cLabelFeaturesInSecondLayer	= null;
	private CCombo					comboFirstLayer				= null;
	private CLabel					sourceLegend				= null;
	private CLabel					referenceLegend				= null;

	private TabFolder				tabFolder					= null;
	private Composite				basicComposite				= null;
	private Composite				advancedComposite			= null;

	private ImageRegistry			imagesRegistry				= null;

	// parameters
	private ILayer					currentFirstLayer			= null;
	private ILayer					currentSecondLayer			= null;

	public FillComposite(Composite parent, int style) {

		super(parent, style);

		super.initialize();
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

		createGroupSourceInputs(basicComposite);

		createGroupTargetInputs(basicComposite);

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.Composite_tab_folder_basic);
		tabItem.setControl(basicComposite);

	}

	private ImageRegistry CreateImageRegistry() {

		ImageRegistry registry = new ImageRegistry();

		String imgFile = "images/" + SOURCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(SOURCE_LEGEND, ImageDescriptor.createFromFile(FillComposite.class, imgFile));

		imgFile = "images/" + REFERENCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(REFERENCE_LEGEND, ImageDescriptor.createFromFile(FillComposite.class, imgFile));

		return registry;
	}

	/**
	 * This method initializes group for source inputs
	 */
	private void createGroupSourceInputs(Composite basicComposite) {

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
		// gridData13.widthHint = GRID_DATA_3_WIDTH_HINT;

		GridData gridData14 = new GridData();
		gridData14.horizontalAlignment = GridData.BEGINNING;
		gridData14.grabExcessHorizontalSpace = true;
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
		// gridData23.widthHint = GRID_DATA_3_WIDTH_HINT;

		GridData gridData24 = new GridData();
		gridData24.horizontalAlignment = GridData.BEGINNING;
		gridData24.grabExcessHorizontalSpace = true;
		gridData24.verticalAlignment = GridData.CENTER;
		gridData24.widthHint = GRID_DATA_4_WIDTH_HINT;

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;

		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.BEGINNING;
		gridData5.grabExcessHorizontalSpace = false;
		gridData5.verticalAlignment = GridData.CENTER;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;

		groupSourceInputs = new Group(basicComposite, SWT.NONE);
		groupSourceInputs.setText(Messages.Composite_source_group);
		groupSourceInputs.setLayout(gridLayout);
		groupSourceInputs.setLayoutData(gridData);

		cLabel = new CLabel(groupSourceInputs, SWT.NONE);
		cLabel.setText(Messages.FillComposite_Source_layer + ":"); //$NON-NLS-1$
		cLabel.setLayoutData(gridData11);

		comboFirstLayer = new CCombo(groupSourceInputs, SWT.BORDER | SWT.READ_ONLY);
		comboFirstLayer.setLayoutData(gridData12);

		this.comboFirstLayer.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedFirstLayerActions();

			}
		});

		sourceLegend = new CLabel(groupSourceInputs, SWT.NONE);
		sourceLegend.setText(""); //$NON-NLS-1$
		sourceLegend.setLayoutData(gridData5);
		sourceLegend.setImage(this.imagesRegistry.get(SOURCE_LEGEND));

		cLabel1 = new CLabel(groupSourceInputs, SWT.NONE);
		cLabel1.setText(Messages.DissolveComposite_selected_features + ":"); //$NON-NLS-1$
		cLabel1.setLayoutData(gridData13);

		cLabelFeaturesInFirstLayer = new CLabel(groupSourceInputs, SWT.NONE);
		cLabelFeaturesInFirstLayer.setText(""); //$NON-NLS-1$
		cLabelFeaturesInFirstLayer.setLayoutData(gridData14);

		cLabel3 = new CLabel(groupSourceInputs, SWT.NONE);
		cLabel3.setText(Messages.FillComposite_Reference_layer + ":"); //$NON-NLS-1$
		cLabel3.setLayoutData(gridData21);
		comboSecondLayer = new CCombo(groupSourceInputs, SWT.BORDER | SWT.READ_ONLY);
		comboSecondLayer.setLayoutData(gridData22);
		this.comboSecondLayer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedSecondLayerActions();
			}
		});

		referenceLegend = new CLabel(groupSourceInputs, SWT.NONE);
		referenceLegend.setText(""); //$NON-NLS-1$
		referenceLegend.setLayoutData(gridData5);
		referenceLegend.setImage(this.imagesRegistry.get(REFERENCE_LEGEND));

		cLabel4 = new CLabel(groupSourceInputs, SWT.NONE);
		cLabel4.setText(Messages.DissolveComposite_selected_features + ":"); //$NON-NLS-1$
		cLabel4.setLayoutData(gridData23);

		cLabelFeaturesInSecondLayer = new CLabel(groupSourceInputs, SWT.NONE);
		cLabelFeaturesInSecondLayer.setText(""); //$NON-NLS-1$
		cLabelFeaturesInSecondLayer.setLayoutData(gridData24);

	}

	private void createGroupTargetInputs(Composite basicComposite) {

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.verticalAlignment = GridData.BEGINNING;

		groupTargetInputs = new Group(basicComposite, SWT.NONE);
		groupTargetInputs.setText(Messages.Composite_result_group);
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
				enableCopyFeatures();
			}

		});

		// copy check box
		checkButtonCopy = new Button(groupTargetInputs, SWT.CHECK);
		checkButtonCopy.setText(Messages.FillComposite_CopyText);
		checkButtonCopy.setToolTipText(Messages.FillComposite_ToolTip);
		checkButtonCopy.setLayoutData(gridData);
		checkButtonCopy.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				validateParameters();
			}
		});

	}

	/**
	 * Copy Features check button behavior
	 */
	private void enableCopyFeatures() {

		FillCommand cmd = (FillCommand) getCommand();
		ILayer firstLayer = cmd.getFirstLayer();
		ILayer targetLayer = cmd.getTargetLayer();

		boolean enabled = (firstLayer != null) && !firstLayer.equals(targetLayer);
		checkButtonCopy.setEnabled(enabled);
		if (!enabled) {
			checkButtonCopy.setSelection(false);
		}

	}

	/**
	 * Populates layers comboBox with the current layers.
	 */
	@Override
	protected void populate() {

		loadComboWithLayerList(this.comboFirstLayer, ParameterName.SOURCE_GEOMETRY_CLASS);
		loadComboWithLayerList(this.comboSecondLayer, ParameterName.REFERENCE_GEOMETRY_CLASS);

		selectDefaultLayer();
	}

	/**
	 * Sets the selected First layer and its features has current.
	 */
	private void selectedFirstLayerActions() {

		ILayer selectedLayer = getSelecedLayer(this.comboFirstLayer);
		if (selectedLayer == null) {
			return;
		}

		setCurrentFirstLayer(selectedLayer);

		validateParameters();
	}

	/**
	 * Sets the selected layer in map has default for first layer.
	 */
	private void selectDefaultLayer() {

		// synchronizes the command values with the default widget values
		FillCommand cmd = (FillCommand) getCommand();

		// sets the first layer as current
		this.currentFirstLayer = cmd.getFirstLayer();
		if (this.currentFirstLayer == null) {
			ILayer selected = MapUtil.getSelectedLayer(getCurrentMap());
			if (LayerUtil.isCompatible(selected, cmd.getDomainValues(ParameterName.SOURCE_GEOMETRY_CLASS))) {
				this.currentFirstLayer = selected;
			}
		}
		setCurrentFirstLayer(this.currentFirstLayer);
		changeSelectedLayer(this.currentFirstLayer, this.comboFirstLayer);

		// sets the second layer as current
		this.currentSecondLayer = cmd.getSecondLayer();
		setCurrentSecondLayer(this.currentSecondLayer);
		changeSelectedLayer(this.currentSecondLayer, this.comboSecondLayer);
	}

	private void setCurrentFirstLayer(final ILayer selectedLayer) {

		if (selectedLayer == null) {
			return;
		}

		this.currentFirstLayer = selectedLayer;

		presentSelectionAllOrBBox(this.currentFirstLayer, this.currentFirstLayer.getFilter(),
					this.cLabelFeaturesInFirstLayer);
	}

	private void setCurrentSecondLayer(final ILayer selectedLayer) {

		if (selectedLayer == null) {
			return;
		}

		this.currentSecondLayer = selectedLayer;

		presentSelectionAllOrBBox(this.currentSecondLayer, this.currentSecondLayer.getFilter(),
					this.cLabelFeaturesInSecondLayer);
	}

	/**
	 * Sets the selected Second layer and its features has current.
	 */
	private void selectedSecondLayerActions() {

		ILayer selectedLayer = getSelecedLayer(this.comboSecondLayer);
		if (selectedLayer == null) {
			return;
		}

		setCurrentSecondLayer(selectedLayer);

		validateParameters();
	}

	@Override
	public void setEnabled(boolean enabled) {

		groupSourceInputs.setEnabled(enabled);
		groupTargetInputs.setEnabled(enabled);
		resultComposite.setEnabled(enabled);
		comboSecondLayer.setEnabled(enabled);
		comboFirstLayer.setEnabled(enabled);
	}

	/**
	 * sets the fill command parameters
	 */
	@Override
	protected void setParametersOnCommand(ISOCommand command) {

		// Sets the parameters values in controller to do the validation
		FillCommand cmd = (FillCommand) command;

		Filter filterInFirstLayer = null;
		Filter filterInSecondLayer = null;
		CoordinateReferenceSystem currentFirstCRS = null;
		CoordinateReferenceSystem currentSecondCRS = null;

		if (this.currentFirstLayer != null) {
			currentFirstCRS = this.currentFirstLayer.getCRS();
			filterInFirstLayer = getFilter(this.currentFirstLayer);
		}
		if (this.currentSecondLayer != null) {
			currentSecondCRS = this.currentSecondLayer.getCRS();
			filterInSecondLayer = getFilter(this.currentSecondLayer);
		}

		// Start setting the parameters on the command.
		cmd.setInputParams(this.currentFirstLayer, currentFirstCRS, this.currentSecondLayer, currentSecondCRS,
					filterInFirstLayer, filterInSecondLayer);

		if (this.resultComposite.isLayerSelected()) {

			final ILayer targetLayer = this.resultComposite.getCurrentTargetLayer();

			cmd.setOutputParams(targetLayer, checkButtonCopy.getSelection());
		} else {
			// requires create a new layer
			final String layerName = this.resultComposite.getNewLayerName();
			if (layerName != null) {

				final Class<? extends Geometry> targetClass = this.resultComposite.getTargetClass();
				final CoordinateReferenceSystem targetCRS = getCurrentMapCrs();

				cmd.setOutputParams(layerName, targetCRS, targetClass, checkButtonCopy.getSelection());
			}
		}

		;
	}

	/**
	 * Changes the count of features selected of the selected layer
	 */
	@Override
	protected final void changedFilterSelectionActions(final ILayer layer, final Filter newFilter) {

		assert layer != null;
		assert newFilter != null;

		if (layer.equals(this.currentFirstLayer)) {

			presentSelectionAllOrBBox(this.currentFirstLayer, newFilter, this.cLabelFeaturesInFirstLayer);
		}
		if (layer.equals(this.currentSecondLayer)) {

			presentSelectionAllOrBBox(this.currentSecondLayer, newFilter, this.cLabelFeaturesInSecondLayer);
		}

		validateParameters();
	}

	@Override
	protected void removeLayerListActions(ILayer layer) {

		if (layer.equals(this.currentFirstLayer)) {
			currentFirstLayer = null;
		}
		validateParameters();
	}
	
	protected final void changedLayerListActions() {

		// change the list of layers
		comboFirstLayer.removeAll();
		comboSecondLayer.removeAll();

		populate();
		
		selectedFirstLayerActions();
		selectedSecondLayerActions();
	}

}
