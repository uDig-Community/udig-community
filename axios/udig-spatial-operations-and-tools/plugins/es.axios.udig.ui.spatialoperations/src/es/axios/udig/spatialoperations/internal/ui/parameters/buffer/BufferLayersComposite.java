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
package es.axios.udig.spatialoperations.internal.ui.parameters.buffer;

import java.util.LinkedList;
import java.util.List;

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
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.BufferCommand;
import es.axios.udig.spatialoperations.ui.common.ResultLayerComposite;
import es.axios.udig.spatialoperations.ui.common.TargetLayerListenerAdapter;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Buffer Layers Composite
 * <p>
 * Presents the widgets that allow to get the source and target layer.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
final class BufferLayersComposite extends AggregatedPresenter {

	private static final int					GRID_DATA_1_WIDTH_HINT	= 70;
	private static final int					GRID_DATA_2_WIDTH_HINT	= 150;
	// private static final int GRID_DATA_3_WIDTH_HINT = 85;
	private static final int					GRID_DATA_4_WIDTH_HINT	= 45;
	private static final String					SOURCE_LEGEND			= "SourceLegend";									//$NON-NLS-1$

	// controls
	private CCombo								comboSourceLayer		= null;
	private CLabel								labelSelectedFeatures	= null;
	private ResultLayerComposite				resultComposite			= null;
	private CLabel								labelSource;
	private CLabel								sourceLegend			= null;
	// data
	private ILayer								currentSourceLayer		= null;
	private ImageRegistry						imagesRegistry			= null;

	private List<SourceLayerSelectedListener>	sourceLayerListenerList	= new LinkedList<SourceLayerSelectedListener>();

	/**
	 * New instance of BufferLayerComposite
	 * 
	 * @param parent
	 * @param style
	 */
	public BufferLayersComposite(Composite parent, int style) {
		super(parent, style);
		super.initialize();
	}

	@Override
	protected void createContents() {

		GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		this.imagesRegistry = CreateImageRegistry();

		createSourceComposite(this);
		createTargetComposite(this);

	}

	private ImageRegistry CreateImageRegistry() {

		ImageRegistry registry = new ImageRegistry();

		String imgFile = "images/" + SOURCE_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(SOURCE_LEGEND, ImageDescriptor.createFromFile(BufferLayersComposite.class, imgFile));

		return registry;
	}

	/**
	 * Creates the widgets to get the source of buffer operation
	 */
	private void createSourceComposite(Composite parent) {

		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.BEGINNING;
		gridData1.grabExcessHorizontalSpace = false;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.widthHint = GRID_DATA_1_WIDTH_HINT;

		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.CENTER;
		gridData2.grabExcessHorizontalSpace = false;
		gridData2.widthHint = GRID_DATA_2_WIDTH_HINT;

		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.BEGINNING;
		gridData3.grabExcessHorizontalSpace = false;
		// gridData3.widthHint = GRID_DATA_3_WIDTH_HINT;
		gridData3.verticalAlignment = GridData.CENTER;

		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.BEGINNING;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.widthHint = GRID_DATA_4_WIDTH_HINT;
		gridData4.verticalAlignment = GridData.CENTER;

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

		Group sourceComposite = new Group(parent, SWT.NONE);
		sourceComposite.setText(Messages.BufferLayersComposite_source);
		sourceComposite.setLayout(gridLayout);
		sourceComposite.setLayoutData(gridData);

		labelSource = new CLabel(sourceComposite, SWT.NONE);
		labelSource.setText(Messages.BufferLayersComposite_layer + ":"); //$NON-NLS-1$
		labelSource.setLayoutData(gridData1);

		comboSourceLayer = new CCombo(sourceComposite, SWT.BORDER | SWT.READ_ONLY);
		comboSourceLayer.setLayoutData(gridData2);

		comboSourceLayer.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedSourceLayerActions(comboSourceLayer);
			}
		});

		sourceLegend = new CLabel(sourceComposite, SWT.NONE);
		sourceLegend.setText(""); //$NON-NLS-1$
		sourceLegend.setLayoutData(gridData5);
		sourceLegend.setImage(this.imagesRegistry.get(SOURCE_LEGEND));

		labelSelectedFeatures = new CLabel(sourceComposite, SWT.NONE);
		labelSelectedFeatures.setText(Messages.BufferLayersComposite_features_seleccionados + ":"); //$NON-NLS-1$
		labelSelectedFeatures.setLayoutData(gridData3);

		labelSelectedFeatures = new CLabel(sourceComposite, SWT.NONE);
		labelSelectedFeatures.setText(""); //$NON-NLS-1$
		labelSelectedFeatures.setLayoutData(gridData4);

	}

	/**
	 * Creates the widgets to get the target of buffer operation
	 */
	private void createTargetComposite(Composite parent) {

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;

		Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.BufferLayersComposite_result);
		group.setLayout(new GridLayout());
		group.setLayoutData(gridData);

		this.resultComposite = new ResultLayerComposite(this, group, SWT.NONE, GRID_DATA_1_WIDTH_HINT);
		this.resultComposite.addSpecifiedLayerListener(new TargetLayerListenerAdapter() {
			@Override
			public void validateTargetLayer() {
				validateParameters();
			}
		});

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
	 * Sets the selected layer in map has default source layer. Maintains as
	 * default the last selection done it.
	 * 
	 * @param ctrl
	 */
	private void selectDefaultLayer() {

		BufferCommand cmd = (BufferCommand) getCommand();
		if (this.currentSourceLayer == null) {
			ILayer selected = MapUtil.getSelectedLayer(getCurrentMap());
			if (LayerUtil.isCompatible(selected, cmd.getDomainValues(ParameterName.SOURCE_GEOMETRY_CLASS))) {
				this.currentSourceLayer = selected;
			}
		}
		setCurrentSourceLayer(this.currentSourceLayer);
		changeSelectedLayer(this.currentSourceLayer, this.comboSourceLayer);
	}

	/**
	 * Sets the current source layer and dispatch the event to its listeners.
	 * 
	 * @param selectedLayerInMap
	 */
	private void setCurrentSourceLayer(ILayer selectedLayer) {

		this.currentSourceLayer = selectedLayer;
		if (this.currentSourceLayer == null) {
			return;
		}

		setSelectedFeatures(this.currentSourceLayer, this.currentSourceLayer.getFilter());

		dispatchEventSourceLayerSelected(this.currentSourceLayer);

	}

	@Override
	public void setEnabled(boolean enabled) {

		comboSourceLayer.setEnabled(enabled);
		resultComposite.setEnabled(enabled);
	}

	/**
	 * Sends the event "new source layer selected" to all listeners
	 */
	private void dispatchEventSourceLayerSelected(ILayer selectedLayer) {

		for (SourceLayerSelectedListener listener : this.sourceLayerListenerList) {

			listener.layerSelected(this.currentSourceLayer);
		}

	}

	public void addSourceLayerSelectedListener(final SourceLayerSelectedListener listener) {

		this.sourceLayerListenerList.add(listener);
	}

	public void removeSourceLayerSelectedListener(final SourceLayerSelectedListener listener) {

		this.sourceLayerListenerList.remove(listener);
	}

	/**
	 * Populates layer comboboxs with the default values.
	 */
	@Override
	protected void populate() {

		loadComboWithLayerList(this.comboSourceLayer);

		selectDefaultLayer();
	}

	/**
	 * @return the source layer
	 */
	public ILayer getSourceLayer() {
		return this.currentSourceLayer;
	}

	/**
	 * Sychronizes the content with map model
	 */
	@Override
	public final void changedLayerListActions() {

		if (this.comboSourceLayer.isDisposed()) {
			return;
		}

		this.comboSourceLayer.removeAll();

		populate();

		selectedSourceLayerActions(this.comboSourceLayer);

		this.resultComposite.changedLayerListActions();

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
	 * Sets the selected features on layer and present the collection size
	 * 
	 * @param currentLayer
	 * @param filter
	 */
	private void setSelectedFeatures(final ILayer currentLayer, final Filter filter) {

		if (!currentLayer.equals(this.currentSourceLayer)) {
			return; // only presents the feature of current source layer
		}
		presentSelectionAllOrBBox(this.currentSourceLayer, filter, this.labelSelectedFeatures);

		validateParameters();
	}

	/**
	 * Clears the introduced value by user and sets default values
	 */
	@Override
	protected void clearInputs() {

		// clear controls
		this.labelSelectedFeatures.setText(""); //$NON-NLS-1$
		this.comboSourceLayer.removeAll();
	}

	/**
	 * Sets the parameters and calls to its controller to validate them
	 */
	@Override
	protected void setParametersOnCommand(ISOCommand command) {

		// Sets the parameters values in controller to do the validation
		BufferCommand cmd = (BufferCommand) command;

		Filter filter = null;
		if (this.currentSourceLayer != null) {
			filter = getFilter(this.currentSourceLayer);
		}

		// Start setting the parameters on the command.
		cmd.setInputParams(this.currentSourceLayer, filter);

		if (this.resultComposite.isLayerSelected()) {

			final ILayer targetLayer = this.resultComposite.getCurrentTargetLayer();

			cmd.setOutputParams(targetLayer);

		} else {

			final String newFeatureTypeName = this.resultComposite.getNewLayerName();
			if (newFeatureTypeName != null) {

				final CoordinateReferenceSystem crs = getCurrentMapCrs();
				final Class<? extends Geometry> targetClass = this.resultComposite.getTargetClass();
				cmd.setOutputParams(newFeatureTypeName, crs, targetClass);
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
