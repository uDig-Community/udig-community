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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.geotools.util.GeoToolsUtils;
import es.axios.geotools.util.UnitList;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.preferences.Preferences;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.BufferCommand;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Composite that contains the input widgets to set the buffer options.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
final class BufferOptionsComposite extends AggregatedPresenter {

	private static final int	GRID_DATA_1_WIDTH_HINT	= 65;
	private static final int	GRID_DATA_2_WIDTH_HINT	= 140;

	// controls
	private Group				bufferOptionsGroup		= null;
	private Label				labelBufferWidth		= null;
	private Text				textBufferWidth			= null;

	private Composite			groupUnits				= null;
	private Button				radioLayerUnits			= null;
	private Button				radioMapUnits			= null;
	private Button				radioSpecifyUnits		= null;
	private CCombo				comboWidthUnits			= null;
	// data
	private ArrayList<Unit<?>>	bufferUnits				= null;

	private Unit<?>				currentUnits			= null;
	private Unit<?>				defaultUnits			= null;
	private Button				defaultRadioUnitOption	= null;

	private Double				currentDistance			= null;
	private Double				defaultDistance			= null;

	/**
	 * New instance of BufferOptionsComposite
	 * 
	 * @param parent
	 * @param style
	 */
	public BufferOptionsComposite(Composite parent, int style) {

		super(parent, style);
		super.initialize();
	}

	/**
	 * Creates the widget required for common and advanced options
	 */
	@Override
	protected final void createContents() {

		setLayout(new FillLayout());

		createBufferOptionsGroup();

		BufferLayersComposite layersComposite = getLayersComposite();
		layersComposite.addSourceLayerSelectedListener(new SourceLayerSelectedListener() {

			public void layerSelected(ILayer selectedLayer) {

				selectedSourceLayerActions(selectedLayer);
			}
		});

	}

	/**
	 * @return this Buffer Layers Composite (source and target container)
	 */
	private BufferLayersComposite getLayersComposite() {

		BufferComposite bufferComposite = (BufferComposite) this.getParent().getParent().getParent();

		BufferLayersComposite layersComposite = bufferComposite.getLayersComposite();

		return layersComposite;
	}

	/**
	 * This method initializes bufferOptionsGroup
	 */
	private void createBufferOptionsGroup() {

		// group
		bufferOptionsGroup = new Group(this, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 1;
		gridLayout.marginHeight = 1;
		bufferOptionsGroup.setLayout(gridLayout);

		Composite distanceUnitsComposite = new Composite(bufferOptionsGroup, SWT.NONE);
		GridLayout gridDistanceUnits = new GridLayout(2, false);
		gridDistanceUnits.verticalSpacing = 1;
		gridDistanceUnits.marginHeight = 1;
		distanceUnitsComposite.setLayout(gridDistanceUnits);
		createDistanceComposite(distanceUnitsComposite);

		createUnitOptions(distanceUnitsComposite);

	}

	/**
	 * 
	 * @param bufferOptionsGroup
	 */
	private void createDistanceComposite(final Composite parent) {

		// width composite: text width + unit label + options
		Composite widthComposite = new Composite(parent, SWT.NONE);
		widthComposite.setLayout(new GridLayout(3, false));

		// width label
		labelBufferWidth = new Label(widthComposite, SWT.NONE);
		labelBufferWidth.setText(Messages.BufferOptionsComposite_labelBufferWidth_text + ":"); //$NON-NLS-1$
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

		textBufferWidth.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				validateParameters();
			}
		});

		textBufferWidth.addKeyListener(new KeyListener() {

			private String	prevVal	= String.valueOf(Preferences.bufferWidth());

			public void keyPressed(KeyEvent event) {

				this.prevVal = textBufferWidth.getText();

				if (Character.isLetter(event.character)) {
					event.doit = false;
					textBufferWidth.setText(prevVal);
				} else {
					prevVal = textBufferWidth.getText();
				}
			}

			public void keyReleased(KeyEvent event) {

				this.prevVal = textBufferWidth.getText();

				try {
					Double.parseDouble(textBufferWidth.getText());
				} catch (NumberFormatException e) {
					textBufferWidth.setText(prevVal);
				}
				validateParameters();
			}
		});

		comboWidthUnits = new CCombo(widthComposite, SWT.BORDER);
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.BEGINNING;
		gridData3.verticalAlignment = GridData.CENTER;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.widthHint = GRID_DATA_2_WIDTH_HINT;
		comboWidthUnits.setLayoutData(gridData3);
		comboWidthUnits.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				validateParameters();
			}
		});

	}

	/**
	 * Create units option buttons
	 * 
	 * @param bufferOptionsGroup
	 */
	private void createUnitOptions(Composite bufferOptionsGroup) {

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;

		groupUnits = new Composite(bufferOptionsGroup, SWT.NONE);
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.BEGINNING;
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
		gridData5.horizontalAlignment = GridData.BEGINNING;
		gridData5.grabExcessHorizontalSpace = true;
		radioLayerUnits.setLayoutData(gridData5);

		radioSpecifyUnits = new Button(groupUnits, SWT.RADIO);
		radioSpecifyUnits.setText(Messages.BufferOptionsComposite_radioSpecifyUnits_text);
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.BEGINNING;
		gridData6.grabExcessHorizontalSpace = true;
		radioSpecifyUnits.setLayoutData(gridData6);

		radioSpecifyUnits.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedSpecifiyUnitsActions();
				setSelectionUnitOption(radioSpecifyUnits);
			}
		});

		radioMapUnits.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedMapUnitsActions();
				setSelectionUnitOption(radioMapUnits);
			}
		});

		radioLayerUnits.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedLayerUnitsActions();
				setSelectionUnitOption(radioLayerUnits);
			}
		});
	}

	/**
	 * @return the Unit of measure to use for the buffer width, from the
	 *         selected option
	 */
	private Unit<?> getBufferUnitOfMeasure() {

		this.currentUnits = null;

		int index = comboWidthUnits.getSelectionIndex();
		if (index == -1) {
			return this.currentUnits;
		}

		Unit<?> unit = this.bufferUnits.get(index);

		// display the selection
		this.currentUnits = unit;
		this.defaultUnits = unit;

		return this.currentUnits;
	}

	/**
	 * @return the selected source layer
	 */
	private ILayer getSourceLayer() {

		BufferComposite bufferComposite = (BufferComposite) this.getParent().getParent().getParent();

		ILayer layer = bufferComposite.getSourceLayer();

		return layer;
	}

	/**
	 * @return layer's units
	 */
	private Unit<?> getLayerUnits(final ILayer layer) {

		CoordinateReferenceSystem crs = LayerUtil.getCrs(layer);

		Unit<?> units = GeoToolsUtils.getDefaultCRSUnit(crs);

		return units;
	}

	/**
	 * @return map's units
	 */
	private Unit<?> getMapUnits() {

		IMap map = getCurrentMap();
		assert map != null;

		CoordinateReferenceSystem crs = MapUtil.getCRS(map);
		Unit<?> mapUnits = GeoToolsUtils.getDefaultCRSUnit(crs);

		return mapUnits;
	}

	/**
	 * @return distance around the geometries edges on which to perform the
	 *         buffer, in {@link #getBufferUnitOfMeasure()} units
	 */
	private Double getBufferDistance() {

		String text = textBufferWidth.getText();
		Double bufferDistance;
		try {
			bufferDistance = Double.parseDouble(text);

		} catch (NumberFormatException e) {
			bufferDistance = null;
		}
		// set the current and default width
		this.currentDistance = bufferDistance;
		this.defaultDistance = bufferDistance;

		return this.currentDistance;
	}

	private void populateUnitCombo() {

		if (comboWidthUnits.getItemCount() != 0) {
			return; // this collection is inmutable, only populates the first
			// time
		}

		final Unit<?> defaultUnit = Preferences.bufferUnits();
		final Set<Unit<?>> commonLengthUnits = UnitList.getCommonLengthUnits();

		SortedMap<String, Unit<?>> units = new TreeMap<String, Unit<?>>();
		for (Unit<?> unit : commonLengthUnits) {
			units.put(UnitList.getUnitName(unit), unit);
		}

		bufferUnits = new ArrayList<Unit<?>>(units.values());

		int index = 0;
		Set<Entry<String, Unit<?>>> unitset = units.entrySet();
		for (Entry<String, Unit<?>> entry : unitset) {

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

		this.currentUnits = null;
		this.currentDistance = null;
	}

	/**
	 * Populates the default options
	 */
	@Override
	protected final void populate() {

		// set default distance
		if (this.defaultDistance == null) {

			this.defaultDistance = Preferences.bufferWidth();

		}
		if (this.currentDistance == null) {
			this.currentDistance = this.defaultDistance;
		}

		textBufferWidth.setText(String.valueOf(this.currentDistance));

		// sets the units
		populateUnitCombo();

		if (this.defaultUnits == null) {

			// sets the maps units as default

			this.defaultUnits = getMapUnits();

		}
		this.currentUnits = this.defaultUnits;

		String unitName = UnitList.getUnitName(this.currentUnits);

		comboWidthUnits.setText(unitName);

		if (this.defaultRadioUnitOption == null) {
			setSelectionUnitOption(this.radioMapUnits);
			this.defaultRadioUnitOption = this.radioMapUnits;
		} else {
			setSelectionUnitOption(this.defaultRadioUnitOption);
		}

	}

	/**
	 * Sets enable/disable the composite's widgets
	 */
	@Override
	public void setEnabled(boolean enabled) {

		this.textBufferWidth.setEnabled(enabled);
		this.radioLayerUnits.setEnabled(enabled);
		this.radioMapUnits.setEnabled(enabled);
		this.radioSpecifyUnits.setEnabled(enabled);
	}

	/**
	 * If layer units is selected get the units for the selected layer.
	 * 
	 * @param selectedLayer
	 */
	private void selectedSourceLayerActions(final ILayer selectedLayer) {

		if (!this.radioLayerUnits.getSelection()) {
			return;
		}
		selectedLayerUnitsActions(selectedLayer);
	}

	private void selectedLayerUnitsActions(final ILayer layer) {

		if (layer == null) {
			return;
		}

		// gets the units from layer and sets the combo with this units
		this.currentUnits = getLayerUnits(layer);

		final String unitName = UnitList.getUnitName(this.currentUnits);
		this.comboWidthUnits.setText(unitName);

		this.comboWidthUnits.setEnabled(false);

		validateParameters();
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
		final String unitName = UnitList.getUnitName(this.currentUnits);
		this.comboWidthUnits.setText(unitName);

		this.comboWidthUnits.setEnabled(false);

		validateParameters();
	}

	/**
	 * Enables the combo to specify the units
	 */
	private void selectedSpecifiyUnitsActions() {

		this.comboWidthUnits.setEnabled(true);
	}

	/**
	 * Validate the current option's values
	 * 
	 */
	@Override
	protected final void setParametersOnCommand(ISOCommand command) {

		BufferCommand cmd = (BufferCommand) command;

		if (comboWidthUnits.getItemCount() == 0) {
			return;
		}

		// gets the options values and sends it to controller which does the
		// validation.

		Double distance = getBufferDistance();

		Unit<?> units = getBufferUnitOfMeasure();

		cmd.setBasicOptions(distance, units);
	}

	/**
	 * if the layer deleted is the current source layer, changes the units
	 * option to map units
	 */
	@Override
	protected final void removedLayerActions(ILayer layer) {

		super.removedLayerActions(layer);

		ILayer sourceLayer = getSourceLayer();

		if (sourceLayer.equals(layer)) {

			this.currentUnits = null;
			setSelectionUnitOption(this.radioMapUnits);
		}

		populate();
	}

	/**
	 * Sets the unit options (radio buttons), enables the unit selection
	 * combobox.
	 * 
	 * @param selectedRadio
	 */
	private void setSelectionUnitOption(final Button selectedRadio) {

		BufferCommand cmd = (BufferCommand) getCommand();
		cmd.setRadioUnitOption(selectedRadio);
		// sets unit options
		this.defaultRadioUnitOption = selectedRadio;
		this.radioMapUnits.setSelection(selectedRadio.equals(this.radioMapUnits));
		this.radioLayerUnits.setSelection(selectedRadio.equals(this.radioLayerUnits));
		this.radioSpecifyUnits.setSelection(selectedRadio.equals(this.radioSpecifyUnits));
		// enables combo
		if (selectedRadio.equals(this.radioSpecifyUnits)) {
			this.comboWidthUnits.setEnabled(true);
		} else {
			this.comboWidthUnits.setEnabled(false);
		}
	}

	@Override
	public final void changedLayerListActions() {

		this.currentUnits = null;

		populate();
	}

} // @jve:decl-index=0:visual-constraint="10,10"
