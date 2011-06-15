package es.axios.so.extension.copy;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

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
import org.geotools.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.ui.common.SpecifiedLayerListener;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.ui.commons.util.MapUtil;

public class CopyComposite extends AggregatedPresenter {

	private static final int		GRID_DATA_1_WIDTH_HINT				= 130;
	private static final int		GRID_DATA_2_WIDTH_HINT				= 150;
	// private static final int GRID_DATA_3_WIDTH_HINT = 96;
	private static final int		GRID_DATA_4_WIDTH_HINT				= 45;
	private static final String		SOURCE_LEGEND						= "SourceLegend";	//$NON-NLS-1$

	// widgets
	private Group					groupSource							= null;
	private CCombo					cComboSourceLayer					= null;
	private CLabel					cLabelCountFeaturesInSourceLayer	= null;
	private CLabel					sourceLegend						= null;

	private TabFolder				tabFolder							= null;
	private Composite				basicComposite						= null;

	private ResultLayerComposite	resultComposite						= null;
//	private ImageRegistry			imagesRegistry						= null;

	// input data
	private ILayer					currentSourceLayer					= null;

	public CopyComposite(Composite parent, int style) {

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

//		this.imagesRegistry = createImageRegistry();

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

		createSourceGroup();
		createGroupResult();

		this.setLayout(gridLayout1);
		setSize(new Point(541, 202));

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("basic");
		tabItem.setControl(basicComposite);

		// TODO add advanced options
		// TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
		// tabItem1.setText(Messages.Composite_tab_folder_advanced);
		// tabItem1.setControl(advancedComposite);

	}

//	private ImageRegistry createImageRegistry() {
//
//		ImageRegistry registry = new ImageRegistry();
//
//		String imgFile = "images/" + SOURCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
//		// registry.put(SOURCE_LEGEND,
//		// ImageDescriptor.createFromFile(PolygonToLineComposite.class,
//		// imgFile));
//
//		return registry;
//	}

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
		groupSource.setText("source");
		groupSource.setLayout(gridLayout);
		groupSource.setLayoutData(gridData);

		CLabel cLabel = new CLabel(groupSource, SWT.NONE);
		cLabel.setText("source" + ":"); //$NON-NLS-1$
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
		sourceLegend.setText(""); //$NON-NLS-1$
		sourceLegend.setLayoutData(gridData5);
//		sourceLegend.setImage(this.imagesRegistry.get(SOURCE_LEGEND));

		CLabel cLabelSelected = new CLabel(groupSource, SWT.NONE);
		cLabelSelected.setText("selection" + ":"); //$NON-NLS-1$
		cLabelSelected.setLayoutData(gridData13);

		cLabelCountFeaturesInSourceLayer = new CLabel(groupSource, SWT.NONE);
		cLabelCountFeaturesInSourceLayer.setText(""); //$NON-NLS-1$
		cLabelCountFeaturesInSourceLayer.setLayoutData(gridData14);
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
		groupTargetInputs.setText("result");
		groupTargetInputs.setLayout(new GridLayout());
		groupTargetInputs.setLayoutData(gridData);

		this.resultComposite = new ResultLayerComposite(groupTargetInputs, SWT.NONE, GRID_DATA_1_WIDTH_HINT);

		GridData resultCompositeGridData = new GridData();
		resultCompositeGridData.horizontalAlignment = GridData.FILL;
		resultCompositeGridData.grabExcessHorizontalSpace = true;
		resultCompositeGridData.grabExcessVerticalSpace = true;
		resultCompositeGridData.verticalAlignment = GridData.FILL;

		this.resultComposite.setLayoutData(resultCompositeGridData);

		this.resultComposite.addSpecifiedLayerListener(new SpecifiedLayerListener() {

			public void layerSelected(ILayer selectedLayer) {

				CopyCommand cmd = (CopyCommand) getCommand();
				cmd.setTargetLayer(selectedLayer);
				validateParameters();
			}

			public void newFeatureTypeIsRequired(String layerName) {

				CopyCommand cmd = (CopyCommand) getCommand();
				cmd.setTargetLayerName(layerName);
				validateParameters();
			}

			public void newGeometryClassSelected(Class<? extends Geometry> targetClass) {

				CopyCommand cmd = (CopyCommand) getCommand();
				cmd.setTargetLayerGeometry(targetClass);
				validateParameters();
			}
		});

		this.addPresenter(this.resultComposite);

	}

	/**
	 * Reinitialize parameter values
	 */
	@Override
	protected final void clearInputs() {

		tabFolder.setSelection(tabFolder.getItem(0));

	}

	@Override
	protected void populate() {

		ILayer sourceLayer = getContext().getSelectedLayer();

		loadComboWithLayerList(this.cComboSourceLayer,
				SpatialOperationCommand.PARAMS_SOURCE_GEOMETRY_CLASS);

		CopyCommand cmd = (CopyCommand) getCommand();
		cmd.setDefaultValues(sourceLayer);
		selectDefaultLayer(cmd);

		validateParameters();
	}

	/**
	 * Remove the layer from combobox TODO could be default method in abstract
	 */
	@Override
	protected void removedLayerActions(ILayer layer) {

		super.removedLayerActions(layer);

		changedLayerListActions();
	}

	/**
	 * Synchronizes the content with map model
	 */
	@Override
	public final void changedLayerListActions() {

		if (this.cComboSourceLayer.isDisposed())
			return;

		this.cComboSourceLayer.removeAll();

		populate();

		changeSelectedLayer(this.currentSourceLayer, this.cComboSourceLayer);
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

		assert selectedLayer != null;

		this.currentSourceLayer = selectedLayer;

		setSelectedFeatures(this.currentSourceLayer, this.currentSourceLayer.getFilter());
		validateParameters();
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
	 * Presents the selected layer in map as first layer.
	 * 
	 * @param ctrl
	 */
	private ILayer selectDefaultLayer(CopyCommand cmd) {

		this.currentSourceLayer = cmd.getSourceLayer();

		presentSelectionAllOrBBox(this.currentSourceLayer, this.currentSourceLayer.getFilter(),
					this.cLabelCountFeaturesInSourceLayer);

		changeSelectedLayer(this.currentSourceLayer, this.cComboSourceLayer);

		validateParameters();

		return this.currentSourceLayer;
	}

	@Override
	protected void setParametersOnCommand(ISOCommand command) {

		CopyCommand cmd = (CopyCommand) command;
		IMap map = getCurrentMap();
		assert map != null;
		CoordinateReferenceSystem mapCrs = MapUtil.getCRS(map);

		Filter filter = null;
		if (this.currentSourceLayer != null) {

			filter = getFilter(this.currentSourceLayer);
		}
		// Start setting the parameters on the command.
		cmd.setInputParams(this.currentSourceLayer, filter, mapCrs);

		if (this.resultComposite.isLayerSelected()) {

			ILayer targetLayer = this.resultComposite.getCurrentTargetLayer();

			cmd.setOutputParams(targetLayer);

		} else {
			// requires create a new layer
			final String layerName = this.resultComposite.getNewLayerName();
			Class<? extends Geometry> targetGeomClass = this.resultComposite.getTargetClass();

			cmd.setOutputParams(layerName, targetGeomClass);
		}
	}

}
