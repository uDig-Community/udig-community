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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.ClipCommand;
import es.axios.udig.spatialoperations.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.ui.common.TargetLayerListenerAdapter;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Content for Clip operation.
 * <p>
 * This contains the widgets required to capture the inputs for clip operation.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * 
 * @since 1.1.0
 */
final class ClipComposite extends AggregatedPresenter {

	private static final int		GRID_DATA_1_WIDTH_HINT		= 100;
	private static final int		GRID_DATA_2_WIDTH_HINT		= 150;
	private static final int		GRID_DATA_4_WIDTH_HINT		= 45;
	private static final String		SOURCE_LEGEND				= "SourceLegend";		//$NON-NLS-1$
	private static final String		REFERENCE_LEGEND			= "ReferenceLegend";	//$NON-NLS-1$

	private Group					groupSource					= null;
	private CLabel					cLabelClippingLayer			= null;
	private CCombo					cComboUsingLayer			= null;
	private CLabel					cLabel						= null;
	private CLabel					textClippingFeatures		= null;
	private CLabel					cLabelSourceToClipLayer		= null;
	private CCombo					cComboSourceClipLayer		= null;
	private CLabel					cLabelSourceFeaturesToClip	= null;
	private CLabel					textSourceFeaturesToClip	= null;
	private Group					groupTargetInputs			= null;
	private ResultLayerComposite	resultComposite				= null;
	private TabFolder				tabFolder					= null;
	private Composite				basicComposite				= null;
	private Composite				advancedComposite			= null;
	private CLabel					sourceLegend				= null;
	private CLabel					referenceLegend				= null;

	// data
	private ILayer					currentUsingLayer			= null;
	private ILayer					currentSourceClipLayer		= null;

	private ImageRegistry			imagesRegistry				= null;

	public ClipComposite(Composite parent, int style) {

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

		GridLayout sourceLayout = new GridLayout();
		sourceLayout.numColumns = 5;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.verticalAlignment = GridData.CENTER;

		groupSource = new Group(basicComposite, SWT.NONE);
		groupSource.setLayoutData(gridData1);
		groupSource.setLayout(sourceLayout);
		groupSource.setText(Messages.ClipComposite_using_as_clip);

		createGroupToClipLayer(groupSource);
		createGroupClippingLayer(groupSource);

		createGroupResult();

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.Composite_tab_folder_basic);
		tabItem.setControl(basicComposite);
	}

	private ImageRegistry CreateImageRegistry() {

		ImageRegistry registry = new ImageRegistry();

		String imgFile = "images/" + SOURCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(SOURCE_LEGEND, ImageDescriptor.createFromFile(ClipComposite.class, imgFile));

		imgFile = "images/" + REFERENCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(REFERENCE_LEGEND, ImageDescriptor.createFromFile(ClipComposite.class, imgFile));

		return registry;
	}

	/**
	 * This method initializes groupSource
	 * 
	 * @param groupSource
	 */
	private void createGroupClippingLayer(final Group groupSource) {

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
		// gridData3.widthHint = GRID_DATA_3_WIDTH_HINT;
		gridData3.verticalAlignment = GridData.CENTER;

		GridData gridData4 = new GridData();
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.verticalAlignment = GridData.CENTER;
		gridData4.horizontalAlignment = GridData.BEGINNING;
		gridData4.widthHint = GRID_DATA_4_WIDTH_HINT;

		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.BEGINNING;
		gridData5.grabExcessHorizontalSpace = false;
		gridData5.verticalAlignment = GridData.CENTER;

		cLabelClippingLayer = new CLabel(groupSource, SWT.NONE);
		cLabelClippingLayer.setText(Messages.ClipComposite_clipping_layer + ":"); //$NON-NLS-1$
		cLabelClippingLayer.setLayoutData(gridData1);

		cComboUsingLayer = new CCombo(groupSource, SWT.BORDER | SWT.READ_ONLY);
		cComboUsingLayer.setLayoutData(gridData2);

		cComboUsingLayer.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedUsingLayerActions(cComboUsingLayer);
			}
		});

		referenceLegend = new CLabel(groupSource, SWT.NONE);
		referenceLegend.setText(""); //$NON-NLS-1$
		referenceLegend.setLayoutData(gridData5);
		referenceLegend.setImage(this.imagesRegistry.get(REFERENCE_LEGEND));

		cLabel = new CLabel(groupSource, SWT.NONE);
		cLabel.setText(Messages.ClipComposite_clipping_features + ":"); //$NON-NLS-1$
		cLabel.setLayoutData(gridData3);
		textClippingFeatures = new CLabel(groupSource, SWT.NONE);
		textClippingFeatures.setText(""); //$NON-NLS-1$
		textClippingFeatures.setLayoutData(gridData4);
		textClippingFeatures.setEnabled(false);
	}

	/**
	 * creates "to clip layer" widgets
	 * 
	 * @param groupSource
	 * 
	 */
	private void createGroupToClipLayer(final Group groupSource) {

		GridData gridData4 = new GridData();
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.verticalAlignment = GridData.CENTER;
		gridData4.horizontalAlignment = GridData.BEGINNING;
		gridData4.widthHint = GRID_DATA_4_WIDTH_HINT;

		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.BEGINNING;
		gridData3.grabExcessHorizontalSpace = false;
		gridData3.verticalAlignment = GridData.CENTER;
		// gridData3.widthHint = GRID_DATA_3_WIDTH_HINT;

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

		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.BEGINNING;
		gridData5.grabExcessHorizontalSpace = false;
		gridData5.verticalAlignment = GridData.CENTER;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;

		cLabelSourceToClipLayer = new CLabel(groupSource, SWT.NONE);
		cLabelSourceToClipLayer.setText(Messages.ClipComposite_layer_to_clip + ":"); //$NON-NLS-1$
		cLabelSourceToClipLayer.setLayoutData(gridData1);
		cComboSourceClipLayer = new CCombo(groupSource, SWT.BORDER | SWT.READ_ONLY);
		cComboSourceClipLayer.setLayoutData(gridData2);
		cComboSourceClipLayer.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedClipLayerActions(cComboSourceClipLayer);
			}
		});

		sourceLegend = new CLabel(groupSource, SWT.NONE);
		sourceLegend.setText(""); //$NON-NLS-1$
		sourceLegend.setLayoutData(gridData5);
		sourceLegend.setImage(this.imagesRegistry.get(SOURCE_LEGEND));

		cLabelSourceFeaturesToClip = new CLabel(groupSource, SWT.NONE);
		cLabelSourceFeaturesToClip.setText(Messages.ClipComposite_features_to_clip + ":"); //$NON-NLS-1$
		cLabelSourceFeaturesToClip.setLayoutData(gridData3);
		textSourceFeaturesToClip = new CLabel(groupSource, SWT.NONE);
		textSourceFeaturesToClip.setText(""); //$NON-NLS-1$
		textSourceFeaturesToClip.setLayoutData(gridData4);
	}

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
	}

	/**
	 * Reinitialize parameter values
	 */
	@Override
	protected final void clearInputs() {

		// initializes data using the values set by user in this session
		ClipCommand cmd = (ClipCommand) getCommand();

		this.currentUsingLayer = cmd.getCurrentUsingLayer();
		this.currentSourceClipLayer = cmd.getCurrentSourceClipLayer();

		// initializes widgets
		this.cComboUsingLayer.removeAll();
		this.cComboSourceClipLayer.removeAll();

		this.textClippingFeatures.setText(""); //$NON-NLS-1$
		this.textSourceFeaturesToClip.setText(""); //$NON-NLS-1$

		tabFolder.setSelection(tabFolder.getItem(0));
	}

	/**
	 * Populates layer comboboxs with the current layers. Sets the current layer
	 * current has default for clipping layer.
	 * 
	 */
	@Override
	protected void populate() {

		loadComboWithLayerList(this.cComboSourceClipLayer);
		loadComboWithLayerList(this.cComboUsingLayer);

		selectDefaultLayer();
	}

	/**
	 * Changes the count of features selected of the selected layer
	 */
	@Override
	protected final void changedFilterSelectionActions(final ILayer layer, final Filter newFilter) {

		assert layer != null;
		assert newFilter != null;

		if (layer.equals(this.currentSourceClipLayer)) {

			presentSelectionAllOrBBox(this.currentSourceClipLayer, newFilter, this.textSourceFeaturesToClip);

		}
		if (layer.equals(this.currentUsingLayer)) {

			presentSelectionAllOrBBox(this.currentUsingLayer, newFilter, this.textClippingFeatures);

		}

		validateParameters();
	}

	/**
	 * Sets the selected layer in map has default clipping layer.
	 */
	private void selectDefaultLayer() {

		ClipCommand cmd = (ClipCommand) getCommand();
		this.currentSourceClipLayer = cmd.getCurrentSourceClipLayer();
		if (this.currentSourceClipLayer == null) {
			ILayer selected = MapUtil.getSelectedLayer(getCurrentMap());
			if (LayerUtil.isCompatible(selected, cmd.getDomainValues(ParameterName.SOURCE_GEOMETRY_CLASS))) {
				this.currentSourceClipLayer = selected;
			}
		}

		setCurrentSourceClipLayer(this.currentSourceClipLayer);
		changeSelectedLayer(this.currentSourceClipLayer, this.cComboSourceClipLayer);

		this.currentUsingLayer = cmd.getCurrentUsingLayer();
		setCurrentUsingLayer(this.currentUsingLayer);
		changeSelectedLayer(this.currentUsingLayer, this.cComboUsingLayer);
	}

	@Override
	public void setEnabled(boolean enabled) {

		groupSource.setEnabled(enabled);
		cComboUsingLayer.setEnabled(enabled);
		cComboSourceClipLayer.setEnabled(enabled);
		groupTargetInputs.setEnabled(enabled);
		resultComposite.setEnabled(enabled);

	}

	/**
	 * @param selectedLayer
	 */
	private void setCurrentUsingLayer(ILayer selectedLayer) {

		if (selectedLayer == null) {
			return;
		}

		this.currentUsingLayer = selectedLayer;

		presentSelectionAllOrBBox(this.currentUsingLayer, this.currentUsingLayer.getFilter(), this.textClippingFeatures);

		validateParameters();

	}

	/**
	 * @param selectedLayer
	 */
	private void setCurrentSourceClipLayer(final ILayer selectedLayer) {

		if (selectedLayer == null) {
			return;
		}

		this.currentSourceClipLayer = selectedLayer;

		presentSelectionAllOrBBox(this.currentSourceClipLayer, this.currentSourceClipLayer.getFilter(),
					this.textSourceFeaturesToClip);
		validateParameters();

	}

	/**
	 * Actions associated with layer selection.
	 * 
	 * @param comboLayer
	 * @param textFeatures
	 */
	private void selectedClipLayerActions(final CCombo comboLayer) {

		ILayer selectedLayer = getSelecedLayer(comboLayer);

		if (selectedLayer == null) {
			return;
		}

		setCurrentSourceClipLayer(selectedLayer);
	}

	/**
	 * Actions associated with layer selection.
	 * 
	 * @param comboLayer
	 * @param textFeatures
	 */
	private void selectedUsingLayerActions(final CCombo comboLayer) {

		ILayer selectedLayer = getSelecedLayer(comboLayer);

		if (selectedLayer == null) {
			return;
		}

		setCurrentUsingLayer(selectedLayer);
	}

	/**
	 * Sets the clip command parameters
	 */
	@Override
	protected void setParametersOnCommand(ISOCommand command) {

		// Sets the parameters values in controller to do the validation
		ClipCommand cmd = (ClipCommand) command;

		Filter usingFilter = null;
		Filter clipSourceFilter = null;
		CoordinateReferenceSystem currentUsingCRS = null;
		CoordinateReferenceSystem currentSourceCRS = null;

		if (this.currentUsingLayer != null) {
			usingFilter = getFilter(this.currentUsingLayer);
			currentUsingCRS = this.currentUsingLayer.getCRS();
		}

		if (this.currentSourceClipLayer != null) {
			clipSourceFilter = getFilter(this.currentSourceClipLayer);
			currentSourceCRS = this.currentSourceClipLayer.getCRS();
		}

		// Start setting the parameters on the command.
		cmd.setInputParams(this.currentUsingLayer, currentUsingCRS, this.currentSourceClipLayer, currentSourceCRS,
					usingFilter, clipSourceFilter);

		if (this.resultComposite.isLayerSelected()) {

			final ILayer targetLayer = this.resultComposite.getCurrentTargetLayer();
			cmd.setOutputParams(targetLayer);

		} else {

			final String newFeatureTypeName = this.resultComposite.getNewLayerName();
			if (newFeatureTypeName != null) {
				final CoordinateReferenceSystem targetCrs = getCurrentMapCrs();
				final Class<? extends Geometry> targetClass = this.resultComposite.getTargetClass();

				cmd.setOutputParams(newFeatureTypeName, targetCrs, targetClass);
			}
		}
	}

	/**
	 * Initializes the widgets with default values.
	 */
	@Override
	protected final void changedLayerListActions() {

		// change the list of layers
		cComboUsingLayer.removeAll();
		cComboSourceClipLayer.removeAll();

		populate();

		// FIXME this has been already done inside populate()
//		changeSelectedLayer(this.currentSourceClipLayer, this.cComboSourceClipLayer);
	    selectedClipLayerActions(this.cComboSourceClipLayer);
//
//		changeSelectedLayer(this.currentUsingLayer, this.cComboUsingLayer);
		selectedUsingLayerActions(this.cComboUsingLayer);

	}

	@Override
	protected void removeLayerListActions(ILayer layer) {

		if (layer.equals(this.currentSourceClipLayer)) {
			currentSourceClipLayer = null;
		}
		validateParameters();
	}

} // @jve:decl-index=0:visual-constraint="10,10"
