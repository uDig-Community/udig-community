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
package es.axios.udig.spatialoperations.ui.parameters;

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
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.common.TargetLayerListenerAdapter;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.3.0
 */
public class SimplePresenter extends AggregatedPresenter {

	private static final int			GRID_DATA_1_WIDTH_HINT				= 130;
	private static final int			GRID_DATA_2_WIDTH_HINT				= 150;
	private static final int			GRID_DATA_4_WIDTH_HINT				= 45;
	private static final String			SOURCE_LEGEND						= "SourceLegend";	//$NON-NLS-1$

	// widgets
	private Group						groupSource							= null;
	private CCombo						cComboSourceLayer					= null;
	private CLabel						cLabelCountFeaturesInSourceLayer	= null;
	private CLabel						sourceLegend						= null;

	private TabFolder					tabFolder							= null;
	private Composite					basicComposite						= null;
	private AdvancedComposite			advancedComposite					= null;

	private SimpleResultLayerPresenter	simpleResultPresenter				= null;
	private ImageRegistry				imagesRegistry						= null;

	// input data
	private ILayer						currentSourceLayer					= null;
	private TabItem						tabItemBasic;
	private TabItem						tabItemAdvanced;
	private CLabel						cLabelSourceLayer;
	private CLabel						cLabelSelected;
	private Group						groupTargetInputs;

	private SimpleCommand				cmd									= null;

	public SimplePresenter(Composite parent, int style, ISOCommand cmd) {

		super(parent, style);
		
		this.cmd = (SimpleCommand) cmd;
		super.initialize();
	}

	@Override
	protected void initState() {

		ISOCommand cmd = getCommand();
		cmd.addObserver(this.simpleResultPresenter);
	}

	@Override
	protected void createContents() {
		
		GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		this.imagesRegistry = createImageRegistry();

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

		advancedComposite = createAdvancedLayout(gridLayout);

		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;

		createSourceGroup();
		createGroupResult();

		this.setLayout(gridLayout1);
		setSize(new Point(541, 202));

		tabItemBasic = new TabItem(tabFolder, SWT.NONE);
		tabItemBasic.setText(cmd.getTabItemBasicText());
		tabItemBasic.setControl(basicComposite);

		if (advancedComposite != null) {

			tabItemAdvanced = new TabItem(tabFolder, SWT.NONE);
			tabItemAdvanced.setText(cmd.getTabItemAdvancedText());
			tabItemAdvanced.setControl(advancedComposite);
			// XXX look if necessary.
			this.addPresenter(advancedComposite);
		}
	}

	private AdvancedComposite createAdvancedLayout(GridLayout gridLayout) {

		AdvancedComposite advancedComposite = createAdvancedComposite(tabFolder, SWT.NONE);

		if (advancedComposite == null) {
			return advancedComposite;
		}
		GridData gridDataOptions = new GridData();
		gridDataOptions.horizontalAlignment = GridData.FILL;
		gridDataOptions.grabExcessHorizontalSpace = true;
		gridDataOptions.grabExcessVerticalSpace = true;
		gridDataOptions.verticalAlignment = GridData.BEGINNING;
		advancedComposite.setLayoutData(gridDataOptions);
		advancedComposite.setLayout(gridLayout);

		return advancedComposite;
	}

	/**
	 * Should be implemented by subclass to provide specific options for this
	 * kind of simple presenter
	 * 
	 * @param tabFolder
	 * @param style
	 * @return
	 */
	protected AdvancedComposite createAdvancedComposite(TabFolder tabFolder, int style) {
		// null implementation
		return null;
	}

	private ImageRegistry createImageRegistry() {

		ImageRegistry registry = new ImageRegistry();

		String imgFile = "images/" + SOURCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(SOURCE_LEGEND, ImageDescriptor.createFromFile(SimplePresenter.class, imgFile));

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
		groupSource.setText(cmd.getGroupSourceText());
		groupSource.setLayout(gridLayout);
		groupSource.setLayoutData(gridData);

		cLabelSourceLayer = new CLabel(groupSource, SWT.NONE);
		cLabelSourceLayer.setText(cmd.getLabelSourceLayerText() + ":"); //$NON-NLS-1$
		cLabelSourceLayer.setLayoutData(gridData11);

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
		sourceLegend.setImage(this.imagesRegistry.get(SOURCE_LEGEND));

		cLabelSelected = new CLabel(groupSource, SWT.NONE);
		cLabelSelected.setText(Messages.DissolveComposite_selected_features + ":"); //$NON-NLS-1$
		cLabelSelected.setLayoutData(gridData13);

		cLabelCountFeaturesInSourceLayer = new CLabel(groupSource, SWT.NONE);
		cLabelCountFeaturesInSourceLayer.setText("ALL"); //$NON-NLS-1$
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

		groupTargetInputs = new Group(basicComposite, SWT.NONE);
		groupTargetInputs.setText(cmd.getGroupTargetInputsText());
		groupTargetInputs.setLayout(new GridLayout());
		groupTargetInputs.setLayoutData(gridData);

		this.simpleResultPresenter = new SimpleResultLayerPresenter(this, groupTargetInputs, SWT.NONE,
					GRID_DATA_1_WIDTH_HINT);

		GridData resultCompositeGridData = new GridData();
		resultCompositeGridData.horizontalAlignment = GridData.FILL;
		resultCompositeGridData.grabExcessHorizontalSpace = true;
		resultCompositeGridData.grabExcessVerticalSpace = true;
		resultCompositeGridData.verticalAlignment = GridData.FILL;

		this.simpleResultPresenter.setLayoutData(resultCompositeGridData);
		this.simpleResultPresenter.addSpecifiedLayerListener(new TargetLayerListenerAdapter() {

			@Override
			public void validateTargetLayer() {
				validateParameters();
			}
		});

	}

	@Override
	protected void populate() {

		tabFolder.setSelection(tabFolder.getItem(0));

		//FIXME test it with raster layers.
		loadComboWithLayerList(cComboSourceLayer,ParameterName.SOURCE_GEOMETRY_CLASS);
		selectDefaultLayer();

	}

	/**
	 * Presents the selected layer in map as source layer.
	 * 
	 */
	private void selectDefaultLayer() {
		SimpleCommand cmd = (SimpleCommand) getCommand();
		this.currentSourceLayer=cmd.getSourceLayer();
		if (this.currentSourceLayer == null) {
			ILayer selected = MapUtil.getSelectedLayer(getCurrentMap());
			if (LayerUtil.isCompatible(selected, cmd.getDomainValues(ParameterName.SOURCE_GEOMETRY_CLASS))) {
				this.currentSourceLayer = selected;
			}
		}

		setCurrentSourceLayer(this.currentSourceLayer);
		changeSelectedLayer(this.currentSourceLayer, this.cComboSourceLayer);
	}

	/**
	 * Synchronizes the content with map model
	 */
	@Override
	public final void changedLayerListActions() {

		this.cComboSourceLayer.removeAll();

		populate();
		
		selectedSourceLayerActions(this.cComboSourceLayer);
//FIXME testing
//		this.simpleResultPresenter.changedLayerListActions();
	}

	/**
	 * Actions associated with layer selection.
	 * 
	 * @param comboLayer
	 * @param textFeatures
	 */
	private void selectedSourceLayerActions(final CCombo comboLayer) {

		ILayer selectedLayer = getSelecedLayer(comboLayer);

		if (selectedLayer == null) {
			return;
		}

		setCurrentSourceLayer(selectedLayer);
	}

	/**
	 * Sets the current source layer and dispatch the event to its listeners.
	 * 
	 * @param selectedLayerInMap
	 */
	private void setCurrentSourceLayer(ILayer selectedLayer) {

		if (selectedLayer == null)
			return;

		this.currentSourceLayer = selectedLayer;

		setSelectedFeatures(this.currentSourceLayer, this.currentSourceLayer.getFilter());
		validateParameters();
	}

	@Override
	public void setEnabled(boolean enabled) {

		cComboSourceLayer.setEnabled(enabled);
		simpleResultPresenter.setEnabled(enabled);
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

	@Override
	protected void setParametersOnCommand(ISOCommand command) {

		SimpleCommand cmd = (SimpleCommand) getCommand();

		IMap map = getCurrentMap();
		assert map != null;
		CoordinateReferenceSystem mapCrs = MapUtil.getCRS(map);
		CoordinateReferenceSystem sourceCRS = null;

		Filter filter = null;
		if (this.currentSourceLayer != null) {
			sourceCRS = this.currentSourceLayer.getCRS();
			filter = getFilter(this.currentSourceLayer);
		}
		// Start setting the parameters on the command.
		cmd.setInputParams(this.currentSourceLayer, sourceCRS, filter, mapCrs);

		if (this.simpleResultPresenter.isLayerSelected()) {

			ILayer targetLayer = this.simpleResultPresenter.getCurrentTargetLayer();

			cmd.setOutputParams(targetLayer);

		} else {
			// requires create a new layer
			final String layerName = this.simpleResultPresenter.getNewLayerName();
			Class<? extends Geometry> targetGeomClass = this.simpleResultPresenter.getTargetClass();

			cmd.setOutputParams(layerName, mapCrs, targetGeomClass);
		}
	}

	@Override
	protected void removeLayerListActions(ILayer layer) {

		if (layer.equals(this.currentSourceLayer)) {
			currentSourceLayer = null;
		}
		validateParameters();
	}

}
