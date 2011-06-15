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
package es.axios.udig.spatialoperations.internal.ui.parameters.dissolve;

import java.util.LinkedList;
import java.util.List;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.DissolveCommand;
import es.axios.udig.spatialoperations.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.ui.common.TargetLayerListenerAdapter;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Dissolve Parameters
 * <p>
 * Implements the user interface to grab the input for dissolve operation.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
class DissolveComposite extends AggregatedPresenter {

	private static final int			GRID_DATA_1_WIDTH_HINT				= 130;
	private static final int			GRID_DATA_2_WIDTH_HINT				= 150;
	private static final int			GRID_DATA_3_WIDTH_HINT				= 55;
	private static final int			GRID_DATA_4_WIDTH_HINT				= 45;
	private static final String			SOURCE_LEGEND						= "SourceLegend";			//$NON-NLS-1$

	// widgets
	private Group						groupSource							= null;
	private CCombo						cComboSourceLayer					= null;
	private CLabel						cLabelDissolveProp					= null;
	private Composite					composite							= null;
	private CLabel						cLabelCountFeaturesInSourceLayer	= null;
	private CLabel						sourceLegend						= null;
	private Table						table								= null;
	private Tree						treeDissolveProperty				= null;

	private TabFolder					tabFolder							= null;
	private Composite					basicComposite						= null;
	private Composite					advancedComposite					= null;

	private ResultLayerComposite		resultComposite						= null;
	private ImageRegistry				imagesRegistry						= null;

	// input data
	private ILayer						currentSourceLayer					= null;

	private ILayer						currentTargetLayer					= null;
	private String						newTargetLayerName					= null;
	private Class<? extends Geometry>	newTargetGeometry					= null;

	private List<String>				currentPropDissolve					= new LinkedList<String>();

	public DissolveComposite(Composite parent, int style) {

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

		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;

		createSourceGroup();
		createGroupResult();

		this.setLayout(gridLayout1);
		setSize(new Point(541, 202));

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.Composite_tab_folder_basic);
		tabItem.setControl(basicComposite);
	}

	private ImageRegistry CreateImageRegistry() {

		ImageRegistry registry = new ImageRegistry();

		String imgFile = "images/" + SOURCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(SOURCE_LEGEND, ImageDescriptor.createFromFile(DissolveComposite.class, imgFile));

		return registry;
	}

	/**
	 * This method initializes source group
	 */
	private void createSourceGroup() {

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

		GridData gridData15 = new GridData();
		gridData15.horizontalAlignment = GridData.BEGINNING;
		gridData15.grabExcessHorizontalSpace = false;
		gridData15.verticalAlignment = GridData.CENTER;
		gridData15.widthHint = GRID_DATA_1_WIDTH_HINT;

		GridData gridData16 = new GridData();
		gridData16.horizontalAlignment = GridData.BEGINNING;
		gridData16.grabExcessHorizontalSpace = false;
		gridData16.verticalAlignment = GridData.CENTER;
		gridData16.widthHint = GRID_DATA_2_WIDTH_HINT;
		gridData16.heightHint = GRID_DATA_3_WIDTH_HINT;

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

		groupSource = new Group(basicComposite, SWT.NONE);
		groupSource.setText(Messages.Composite_source_group);
		groupSource.setLayout(gridLayout);
		groupSource.setLayoutData(gridData);

		CLabel cLabel = new CLabel(groupSource, SWT.NONE);
		cLabel.setText(Messages.DissolveComposite_source_layer + ":"); //$NON-NLS-1$
		cLabel.setLayoutData(gridData11);

		cComboSourceLayer = new CCombo(groupSource, SWT.BORDER);
		cComboSourceLayer.setLayoutData(gridData12);
		cComboSourceLayer.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				DissolveCommand cmd = (DissolveCommand) getCommand();
				cmd.resetDissolveProperty();
				if (cmd.getDissolveProperty() == null) {
					currentPropDissolve.clear();
				}

				selectedSourceLayerActions(cComboSourceLayer);

			}
		});

		sourceLegend = new CLabel(groupSource, SWT.NONE);
		sourceLegend.setText(""); //$NON-NLS-1$
		sourceLegend.setLayoutData(gridData5);
		sourceLegend.setImage(this.imagesRegistry.get(SOURCE_LEGEND));

		CLabel cLabelSelected = new CLabel(groupSource, SWT.NONE);
		cLabelSelected.setText(Messages.DissolveComposite_selected_features + ":"); //$NON-NLS-1$
		cLabelSelected.setLayoutData(gridData13);

		cLabelCountFeaturesInSourceLayer = new CLabel(groupSource, SWT.NONE);
		cLabelCountFeaturesInSourceLayer.setText(""); //$NON-NLS-1$
		cLabelCountFeaturesInSourceLayer.setLayoutData(gridData14);

		cLabelDissolveProp = new CLabel(groupSource, SWT.NONE);
		cLabelDissolveProp.setText(Messages.DissolveComposite_property + ":"); //$NON-NLS-1$
		cLabelDissolveProp.setLayoutData(gridData15);

		treeDissolveProperty = new Tree(groupSource, SWT.CHECK | SWT.BORDER);
		treeDissolveProperty.setHeaderVisible(false);
		treeDissolveProperty.setLayoutData(gridData16);
		treeDissolveProperty.setLinesVisible(true);
		treeDissolveProperty.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {

				if (event.detail == SWT.CHECK) {
					validateParameters();
				}
			}

		});
		TreeColumn treeColumnFeature = new TreeColumn(treeDissolveProperty, SWT.NONE);
		treeColumnFeature.setWidth(GRID_DATA_2_WIDTH_HINT);

		// TODO createSourceComposite();
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

		Group groupTargetInputs = new Group(basicComposite, SWT.NONE);
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

			public void validateTargetLayer() {

				validateParameters();
			}
		});

	}

	/**
	 * TODO This method initializes the widgets used to select the aggregated
	 * functions
	 * 
	 */
	private void createSourceComposite() {

		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.verticalSpan = 2;
		gridData4.verticalAlignment = GridData.CENTER;

		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 1;
		gridLayout2.makeColumnsEqualWidth = true;

		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalSpan = 5;
		gridData3.verticalAlignment = GridData.CENTER;

		composite = new Composite(groupSource, SWT.NONE);
		composite.setLayoutData(gridData3);
		composite.setLayout(gridLayout2);

		table = new Table(composite, SWT.NONE);
		table.setHeaderVisible(true);
		table.setLayoutData(gridData4);
		table.setLinesVisible(true);
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(60);
		tableColumn.setText("Source property"); //$NON-NLS-1$
		TableColumn tableColumn1 = new TableColumn(table, SWT.NONE);
		tableColumn1.setWidth(60);
		tableColumn1.setText("Statistic"); //$NON-NLS-1$
		TableColumn tableColumn2 = new TableColumn(table, SWT.NONE);
		tableColumn2.setWidth(60);
		tableColumn2.setText("Result property name"); //$NON-NLS-1$
	}

	@Override
	protected void populate() {

		loadComboWithLayerList(this.cComboSourceLayer);

		ILayer layer = selectDefaultLayer();

		loadDissolveProperties(layer);

	}

	/**
	 * Sets the default layer to intersect.
	 */
	private ILayer selectDefaultLayer() {

		DissolveCommand cmd = (DissolveCommand) getCommand();
		this.currentSourceLayer = cmd.getSourceLayer();
		if (this.currentSourceLayer == null) {
			ILayer selected = MapUtil.getSelectedLayer(getCurrentMap());
			if (LayerUtil.isCompatible(selected, cmd.getDomainValues(ParameterName.SOURCE_GEOMETRY_CLASS))) {
				this.currentSourceLayer = selected;
			}
		}

		setCurrentSourceLayer(this.currentSourceLayer);
		changeSelectedLayer(this.currentSourceLayer, this.cComboSourceLayer);

		return this.currentSourceLayer;
	}

	/**
	 * Load in dissolve property combobox the properties of the layer selected.
	 * That properties will not be geometry type.
	 */
	private void loadDissolveProperties(final ILayer currentLayer) {

		treeDissolveProperty.removeAll();
		if (currentPropDissolve != null) {
			currentPropDissolve.clear();
		}
		if (currentLayer == null || currentLayer.getSchema() == null) {
			return;
		}

		SimpleFeatureType type = currentLayer.getSchema();
		for (int i = 0; i < type.getAttributeCount(); i++) {

			AttributeDescriptor attrType = type.getDescriptor(i);
			if (!(attrType instanceof GeometryDescriptor)) {

				String attrName = attrType.getLocalName();
				TreeItem featureItem = new TreeItem(this.treeDissolveProperty, SWT.NONE);
				featureItem.setData(attrName, attrType);
				featureItem.setText(attrName);
			}
		}
		// if the feature has only one attribute puts it as checked
		if (this.treeDissolveProperty.getItemCount() == 1) {
			TreeItem firstItem = this.treeDissolveProperty.getItem(0);
			firstItem.setChecked(true);
			validateParameters();
		}
	}

	/**
	 * Synchronizes the content with map model
	 */
	@Override
	public final void changedLayerListActions() {

		if (this.cComboSourceLayer.isDisposed()) {
			return;
		}

		this.cComboSourceLayer.removeAll();

		populate();

		selectedSourceLayerActions(this.cComboSourceLayer);
		this.resultComposite.changedLayerListActions();
	}

	/**
	 * Actions associated with layer selection.
	 * 
	 * @param comboLayer
	 * @param textFeatures
	 */
	private void selectedSourceLayerActions(final CCombo comboLayer) {

		ILayer selectedLayer = getSelecedLayer(comboLayer);
		loadDissolveProperties(selectedLayer);

		if (selectedLayer == null) {
			return;
		}

		setCurrentSourceLayer(selectedLayer);
		validateParameters();
	}

	public void setEnabled(boolean enabled) {

		cComboSourceLayer.setEnabled(enabled);
		treeDissolveProperty.setEnabled(enabled);
		resultComposite.setEnabled(enabled);
	}

	/**
	 * Sets the current source layer and dispatch the event to its listeners.
	 * 
	 * @param selectedLayerInMap
	 */
	private void setCurrentSourceLayer(ILayer selectedLayer) {

		if (selectedLayer == null) {
			return;
		}

		this.currentSourceLayer = selectedLayer;

		presentSelectionAllOrBBox(this.currentSourceLayer, this.currentSourceLayer.getFilter(),
					this.cLabelCountFeaturesInSourceLayer);
	}

	/**
	 * Sets the selected features on layer and presents the collection size
	 * 
	 * @param currentLayer
	 * @param filter
	 */
	private void setSelectedFeatures(final ILayer currentLayer, final Filter filter) {

		if (!currentLayer.equals(this.currentSourceLayer)) {
			return; // only presents the feature of current source layer
		}

		presentSelectionAllOrBBox(this.currentSourceLayer, filter, this.cLabelCountFeaturesInSourceLayer);

		validateParameters();
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
	 * Sets the parameters on dissolve command
	 */
	@Override
	protected void setParametersOnCommand(ISOCommand command) {

		IMap map = getCurrentMap();
		assert map != null;
		CoordinateReferenceSystem mapCrs = MapUtil.getCRS(map);
		CoordinateReferenceSystem currentSourceCRS = null;

		Filter filter = null;
		if (this.currentSourceLayer != null) {
			currentSourceCRS = this.currentSourceLayer.getCRS();
			filter = getFilter(this.currentSourceLayer);
		}
		List<String> dissolveProperties = new LinkedList<String>();
		for (TreeItem item : this.treeDissolveProperty.getItems()) {

			if (item.getChecked()) {
				dissolveProperties.add(item.getText());
			}
		}

		// Start setting the parameters on the command.
		DissolveCommand cmd = (DissolveCommand) command;

		cmd.setInputParams(this.currentSourceLayer, currentSourceCRS, filter, dissolveProperties, mapCrs);

		if (this.resultComposite.isLayerSelected()) {

			ILayer targetLayer = this.resultComposite.getCurrentTargetLayer();

			cmd.setOutputParams(targetLayer);

		} else {
			// requires create a new layer
			final String layerName = this.resultComposite.getNewLayerName();
			if (layerName != null) {

				Class<? extends Geometry> targetGeomClass = this.resultComposite.getTargetClass();

				cmd.setOutputParams(layerName, mapCrs, targetGeomClass);
			}
		}
	}

	@Override
	protected void removeLayerListActions(ILayer layer) {

		if (layer.equals(this.currentSourceLayer)) {
			currentSourceLayer = null;
		}
		validateParameters();
	}

} // @jve:decl-index=0:visual-constraint="10,10"
