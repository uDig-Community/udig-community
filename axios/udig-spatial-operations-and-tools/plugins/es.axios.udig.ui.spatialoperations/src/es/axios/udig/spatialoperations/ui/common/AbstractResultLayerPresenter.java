/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2011, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
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
package es.axios.udig.spatialoperations.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import net.refractions.udig.project.ILayer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.ISOTopParamsPresenter;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * 
 * Presents the available target layers and its geometries. It can be an
 * existent layer or a new layer.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 * @since 1.3.1
 */
public abstract class AbstractResultLayerPresenter extends AggregatedPresenter implements Observer {

	protected static int				GRID_DATA_1_WIDTH_HINT	= 110;
	protected static final int			GRID_DATA_2_WIDTH_HINT	= 150;
	protected static final int			GRID_DATA_3_WIDTH_HINT	= 135;
	protected static final String		DEMO_LEGEND				= "ResultLegend";							//$NON-NLS-1$

	protected final static Class<?>[]	geometryClasses			= new Class[] {
			Point.class,
			MultiPoint.class,
			LineString.class,
			MultiLineString.class,
			Polygon.class,
			MultiPolygon.class,
			Geometry.class										};

	// controls
	protected CCombo					comboTargetLayer		= null;
	protected CLabel					resultLegend			= null;
	protected CCombo					comboGeometryList		= null;

	// listeners
	protected List<TargetLayerListener>	listeners				= new ArrayList<TargetLayerListener>(1);

	// data
	protected ILayer					currentTargetLayer		= null;
	protected String					currentNewLayerName		= null;
	protected String					lastNameGenerated		= null;
	protected static final String		SEPARATOR				= "_";										//$NON-NLS-1$
	protected ImageRegistry				imagesRegistry			= null;

	protected boolean					modifyNewLayerName		= false;
	protected CLabel					targetLabel;

	public AbstractResultLayerPresenter(AggregatedPresenter parentPresenter,
										Composite parentComposite,
										int style,
										int width) {
		super(parentComposite, style);
		// Change the width to have the same as the one that called him.
		GRID_DATA_1_WIDTH_HINT = width;
		super.initialize();
		parentPresenter.addPresenter(this);
	}

	@Override
	protected void initState() {

		getCommand().addObserver(this);
	}

	@Override
	protected void createContents() {

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		setLayout(gridLayout);

		this.imagesRegistry = CreateImageRegistry();

		createGroupTargetInputs();
	}

	private final ImageRegistry CreateImageRegistry() {

		ImageRegistry registry = new ImageRegistry();

		String imgFile = "images/" + DEMO_LEGEND + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(DEMO_LEGEND, ImageDescriptor.createFromFile(ResultLayerComposite.class, imgFile));

		return registry;
	}

	/**
	 * changes the list of layers
	 */
	@Override
	public final void changedLayerListActions() {

		// Saves the last generated and test if this name was generated to
		// restore the value.
		// after the combobox initialization.
		if (this.comboTargetLayer.isDisposed()) {
			return;
		}

		String presentedName = this.comboTargetLayer.getText();

		loadComboWithLayerList(this.comboTargetLayer, ParameterName.TARGET_GEOMETRY_CLASS);

		if (presentedName.equals(this.lastNameGenerated)) {

			this.comboTargetLayer.setText(this.lastNameGenerated);

		}
		selectedTargetLayerActions(this.comboTargetLayer);

	}

	/**
	 * Adds the listeners
	 * 
	 * @param listeners
	 */
	public void addSpecifiedLayerListener(TargetLayerListener listener) {

		assert listener != null;

		this.listeners.add(listener);
	}

	/**
	 * Removes the listeners
	 * 
	 * @param listeners
	 */
	public void removeSpecifiedLayerListener(TargetLayerListener listener) {

		assert listener != null;

		this.listeners.add(listener);
	}

	/**
	 * Announces that a layer selected was selected to the listeners
	 * 
	 * @param layer
	 */
	private void dispatchEvent(final ILayer layer) {

		if (layer == null) {
			return;
		}
		for (TargetLayerListener listener : this.listeners) {

			listener.targetLayerSelected(layer);
			listener.validateTargetLayer();
		}
	}

	/**
	 * Announces that a name for a new feature Type or Layer was typed
	 * 
	 * @param text
	 */
	private void dispatchEvent(final String text) {

		for (TargetLayerListener listener : this.listeners) {

			listener.newTargetLayerName(text);
			listener.validateTargetLayer();
		}

	}

	/**
	 * Announces that a target geometry for a new layer has changed to the
	 * listeners.
	 * 
	 * @param targetClass
	 */
	private void dispatchEvent(final Class<? extends Geometry> targetClass) {

		for (TargetLayerListener listener : this.listeners) {

			listener.newGeometrySelected(targetClass);
			listener.validateTargetLayer();
		}
	}

	/**
	 * This method initializes groups widget for target
	 */
	private void createGroupTargetInputs() {

		GridData gridData1 = new GridData();
		gridData1.grabExcessHorizontalSpace = false;
		gridData1.horizontalAlignment = GridData.BEGINNING;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.grabExcessVerticalSpace = false;
		gridData1.widthHint = GRID_DATA_1_WIDTH_HINT - 5;

		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = false;
		gridData2.horizontalAlignment = GridData.BEGINNING;
		gridData2.verticalAlignment = GridData.CENTER;
		gridData2.grabExcessVerticalSpace = false;
		gridData2.widthHint = GRID_DATA_2_WIDTH_HINT;

		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.BEGINNING;
		gridData3.grabExcessHorizontalSpace = false;
		gridData3.verticalAlignment = GridData.CENTER;
		gridData3.widthHint = GRID_DATA_3_WIDTH_HINT;

		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.BEGINNING;
		gridData4.grabExcessHorizontalSpace = false;
		gridData4.verticalAlignment = GridData.CENTER;

		targetLabel = new CLabel(this, SWT.NONE);
		targetLabel.setLayoutData(gridData1);
		targetLabel.setText(Messages.ResultLayerComposite_target_label + ":"); //$NON-NLS-1$
		targetLabel.setToolTipText(Messages.ResultLayerComposite_target_label_tooltip);

		comboTargetLayer = new CCombo(this, SWT.BORDER);
		comboTargetLayer.setLayoutData(gridData2);
		comboTargetLayer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedTargetLayerActions(comboTargetLayer);
			}

		});

		comboTargetLayer.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				modifyTextActions();
			}
		});

		resultLegend = new CLabel(this, SWT.NONE);
		resultLegend.setText(""); //$NON-NLS-1$
		resultLegend.setLayoutData(gridData4);
		resultLegend.setImage(this.imagesRegistry.get(DEMO_LEGEND));

		comboGeometryList = new CCombo(this, SWT.BORDER);
		comboGeometryList.setEditable(false);
		comboGeometryList.setLayoutData(gridData3);
		comboGeometryList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				selectedGeometryClassActions();
			}

		});
		addGeometryTypes();
	}

	/**
	 * Add geometry type classes to the comboGeometryList.
	 */
	private void addGeometryTypes() {

		for (Class<?> classes : AbstractResultLayerPresenter.geometryClasses) {
			comboGeometryList.add(classes.getSimpleName());
			comboGeometryList.setData(classes.getSimpleName(), classes);
		}
	}

	@Override
	protected void setDefaultValues() {

		this.lastNameGenerated = makeNextLayerNameUsing(this.lastNameGenerated);

		populate();
	}

	/**
	 * Populate with default values maintaining the input set by user
	 */
	@Override
	protected final void populate() {

		populateComboTargetLayer();

		String nextLayerName = null;
		if ((this.lastNameGenerated == null)) {
			// generate a new layer name
			nextLayerName = makeInitialLayerName();
		} else {
			nextLayerName = this.lastNameGenerated;
		}

		this.lastNameGenerated = nextLayerName;

		this.comboTargetLayer.setText(this.lastNameGenerated);
		this.currentNewLayerName = this.lastNameGenerated;

		dispatchEvent(this.currentNewLayerName);
	}

	private void populateComboTargetLayer() {

		String layerName = this.comboTargetLayer.getText();

		// updates the combo's list filtering the invalid layers
		loadComboWithLayerList(this.comboTargetLayer, ParameterName.TARGET_GEOMETRY_CLASS);

		// restore the layer selected by the user
		if (!"".equals(layerName.trim())) { //$NON-NLS-1$
			int index = this.comboTargetLayer.indexOf(layerName);
			if (index >= 0) {
				this.comboTargetLayer.select(index);
			} else {
				// this.comboTargetLayer.setText(layerName);
			}

		}
	}

	/**
	 * Clears the values of widgets
	 */
	@Override
	public void clear() {

		Display.findDisplay(uiThread).syncExec(new Runnable() {

			public void run() {
				clearInputs();
				setDefaultValues();
			}
		});

	}

	/**
	 * Gets the target layer selected and validate all inputs
	 * 
	 * @param comboTargetLayer
	 * 
	 * @return the name of layer selected
	 */
	private String selectedTargetLayerActions(final CCombo comboTargetLayer) {

		assert comboTargetLayer != null;

		ILayer selectedLayer = null;
		ISOCommand cmd = getCommand();
		Class<? extends Geometry> currentClass = cmd.getTargetLayerGeometryClass();
		String geomName = ""; //$NON-NLS-1$
		if (currentClass != null) {
			geomName = currentClass.getSimpleName();
		} else {
			geomName = Geometry.class.getSimpleName();
		}
		String layerName = ""; //$NON-NLS-1$
		int index = comboTargetLayer.getSelectionIndex();
		if (index != -1) {
			layerName = comboTargetLayer.getItem(index);
			if (!(layerName.equals(this.lastNameGenerated))) {

				selectedLayer = (ILayer) comboTargetLayer.getData(layerName);

				setCurrentTarget(selectedLayer);
				dispatchEvent(selectedLayer);
				// displays the layer's geometry
				SimpleFeatureType schema = selectedLayer.getSchema();
				GeometryDescriptor geomAttrType = schema.getGeometryDescriptor();
				geomName = geomAttrType.getType().getBinding().getSimpleName();
				setGeometryComboEnabled(false);
			}
		} else {

			setCurrentTarget(selectedLayer);
			dispatchEvent(selectedLayer);
			setGeometryComboEnabled(true);
		}

		this.comboGeometryList.select(this.comboGeometryList.indexOf(geomName));

		return layerName;
	}

	/**
	 * When a targetGeometry was selected.
	 */
	private void selectedGeometryClassActions() {

		Class<? extends Geometry> targetClass;
		targetClass = getTargetClass();
		dispatchEvent(targetClass);
	}

	/**
	 * Combo's text was modified, this method assures that there is not any
	 * layer with the new typed name.
	 */
	private void modifyTextActions() {

		final String text = this.comboTargetLayer.getText();
		if (text.length() == 0) {
			return;
		}
		// if it was modified by the selection of one item it was handled by
		// selectedTargetLayerActions
		if (this.comboTargetLayer.getSelectionIndex() != -1) {
			return;
		}

		if (text.equals(this.currentNewLayerName)) {
			return;
		}

		// check the new name with the layer list.
		if (this.comboTargetLayer.indexOf(text) != -1) {
			InfoMessage msg = new InfoMessage(Messages.ResultLayerComposite__duplicated_layer_name,
						InfoMessage.Type.ERROR);
			this.setMessage(msg);

			return;
		}
		// boolean used to control the back write of the result layer name
		modifyNewLayerName = true;
		// if the new name is correct, notifies to all listeners,
		// saves the current name and sets Geometry as default for the new layer
		setCurrentTarget(text);
		setGeometryComboEnabled(true);
		int index = comboGeometryList.getSelectionIndex();
		if (index == -1) {

			this.comboGeometryList.select(this.comboGeometryList.indexOf(Geometry.class.getSimpleName()));
		}

		dispatchEvent(text);
		modifyNewLayerName = false;
	}

	/**
	 * Makes the new name taking the last generated as base. Format:
	 * Prefix-Number
	 * 
	 * @param lastNameGenerated
	 * @return new layer's name using the last generated
	 */
	protected final String makeNextLayerNameUsing(String lastNameGenerated) {

		if (lastNameGenerated == null) {
			return null;
		}

		StringBuffer defaultName = new StringBuffer();

		int separatorPosition = lastNameGenerated.indexOf(SEPARATOR);
		String prefix = lastNameGenerated.substring(0, separatorPosition);

		defaultName.append(prefix);

		String nextNumber = computeNextNumberFor(lastNameGenerated, SEPARATOR);
		defaultName.append(SEPARATOR);
		defaultName.append(nextNumber);

		return defaultName.toString();
	}

	/**
	 * Makes the new name using the map's layer list
	 * 
	 * @return a new layer name
	 */
	private String makeInitialLayerName() {

		StringBuffer defaultName = new StringBuffer();

		final String opName = getPrefix();

		defaultName.append(opName);

		// checks if there is a layer with this name, then adds an integer value
		// following the sequence.
		String prefix = opName;
		SortedSet<String> layerList = new TreeSet<String>();
		for (int i = 0; i < this.comboTargetLayer.getItemCount(); i++) {

			String item = this.comboTargetLayer.getItem(i);
			boolean match = Pattern.matches(prefix + "_" + "\\p{Digit}", item); //$NON-NLS-1$ //$NON-NLS-2$
			if (match) {
				layerList.add(item);
			}
		}

		String nextNumber = null;

		// gets the last number generated, 1 for first
		// format: opName-Integer

		if (layerList.isEmpty()) {
			nextNumber = "1"; //$NON-NLS-1$
		} else {
			String lastLayer = layerList.last();
			nextNumber = computeNextNumberFor(lastLayer, SEPARATOR);
		}

		defaultName.append(SEPARATOR);
		defaultName.append(nextNumber);

		return defaultName.toString();
	}

	/**
	 * gets the name from the first presenter (this method takes into account
	 * i18n )
	 * 
	 * @return the prefix
	 */
	private String getPrefix() {

		ISOTopParamsPresenter firstPresenter = (ISOTopParamsPresenter) getTopPresenter();
		final String opName = firstPresenter.getOperationName();

		return opName;
	}

	/**
	 * Computes the next integer for the layer name
	 * 
	 * @param lastLayer
	 * @param separator
	 * 
	 * @return the next number
	 */
	private String computeNextNumberFor(final String lastLayer, final String separator) {

		int numberPosition = lastLayer.indexOf(separator);

		String strLastNumber = lastLayer.substring(numberPosition + 1);

		int lastNumber = Integer.parseInt(strLastNumber);

		final String nextNumber = String.valueOf(lastNumber + 1);

		return nextNumber;
	}

	/**
	 * Adds the new layer in combobox
	 */
	@Override
	protected void addedLayerActions(ILayer layer) {

		super.addedLayerActions(layer);

		changedLayerListActions();

	}

	/**
	 * Remove the layer from combobox
	 */
	@Override
	protected final void removedLayerActions(ILayer layer) {

		super.removedLayerActions(layer);

		changedLayerListActions();
	}

	/**
	 * Sets the current name of the new layer
	 * 
	 * @param newLayerName
	 *            name for the new layer
	 */
	private void setCurrentTarget(String newLayerName) {

		this.currentNewLayerName = newLayerName;
		this.currentTargetLayer = null;
	}

	@Override
	public void setEnabled(boolean enabled) {

		comboTargetLayer.setEnabled(enabled);
		setGeometryComboEnabled(enabled);
		super.setEnabled(enabled);
	}

	/**
	 * Called by the SpatialJoinGeomComposite.
	 * 
	 * Set the comboTargetLayer enabled or disabled. If the target layer is an
	 * existent one(is a layer selected), have to take into account that this
	 * layer have a geometry, so the comboGeometryList is set enable(false).
	 * 
	 * @param enabled
	 *            True of false
	 */
	public void setComboTargetEnabled(boolean enabled) {

		comboTargetLayer.setEnabled(enabled);
		if (isLayerSelected()) {
			comboGeometryList.setEnabled(false);
		} else {
			comboGeometryList.setEnabled(enabled);
		}
	}

	/**
	 * Set the comboGeometryList enabled or disabled.
	 * 
	 * @param enabled
	 *            True of false
	 */
	private void setGeometryComboEnabled(boolean enabled) {

		this.comboGeometryList.setEnabled(enabled);
	}

	/**
	 * Sets the current target layer
	 * 
	 * @param layer
	 *            the target layer
	 */
	private void setCurrentTarget(ILayer layer) {

		if (layer != null) {
			this.currentNewLayerName = null;
			this.currentTargetLayer = layer;
		}
	}

	/**
	 * @return Returns the currentTargetLayer.
	 */
	public ILayer getCurrentTargetLayer() {

		return this.currentTargetLayer;
	}

	/**
	 * @return true if a Layer was selected, false in other case.
	 */
	public boolean isLayerSelected() {

		return this.currentTargetLayer != null;
	}

	/**
	 * @return true if a feature type was specified, false in other case.
	 */
	public boolean isNewFeatureType() {

		return this.currentNewLayerName != null;
	}

	/**
	 * @return the name of new layer
	 */
	public String getNewLayerName() {

		return this.currentNewLayerName;
	}

	/**
	 * Called by the command (its an observable) when it validates.
	 */
	@Override
	public void update(Observable o, Object arg) {

		if (!getEnabled())
			return;

		if (!(arg instanceof ParameterName))
			return;
		ParameterName param = (ParameterName) arg;

		switch (param) {

		case SOURCE_GEOMETRY_CLASS:
		case REFERENCE_GEOMETRY_CLASS:

			filterResultLayers();

			break;
		default:
			break;
		}
	}

	/**
	 * if it was a new target layer, maintain the last user selection or written
	 * name.
	 */
	private void restoreSelectedTargetLayer() {

		if (currentNewLayerName != null) {
			this.comboTargetLayer.setText(currentNewLayerName);
		} else if (currentTargetLayer != null) {
			int index = this.comboTargetLayer.indexOf(this.currentTargetLayer.getName());
			if (index != -1) {
				this.comboTargetLayer.select(index);
			}else{
				currentTargetLayer=null;
				validateParameters();
			}
		}
	}

	/**
	 * Filter the result layer combo with the layers that have valid geometry.
	 * It depends on the source and reference geometry classes and the selected
	 * Spatial Operation.
	 * 
	 * @param command
	 */
	private void filterResultLayers() {

		if (this.modifyNewLayerName) {
			return;
		}

		loadComboWithLayerList(comboTargetLayer, ParameterName.TARGET_GEOMETRY_CLASS);
		loadResultLayersGeometry();
		restoreSelectedTargetLayer();
	}

	/**
	 * Fill the combo of the geometry list with the valid geometry classes.
	 * 
	 */
	private void loadResultLayersGeometry() {

		// preserve the user selection
		String geomSelected = ""; //$NON-NLS-1$

		int indexSelection = this.comboGeometryList.getSelectionIndex();
		if (indexSelection >= 0) {
			// keep the user selection
			geomSelected = this.comboGeometryList.getItem(indexSelection);
		}
		// load the valid geometries from command
		List<?> domain = getCommand().getDomainValues(ParameterName.TARGET_GEOMETRY_CLASS);
		this.comboGeometryList.removeAll();
		for (Object obj : domain) {
			Class<?> geometryClass = (Class<?>) obj;
			String geomName = geometryClass.getSimpleName();
			this.comboGeometryList.add(geomName);
			this.comboGeometryList.setData(geomName, geometryClass);
		}
		if (this.comboGeometryList.getItemCount() == 0)
			return;

		// if the option selected by the user is not valid right now then put
		// geometry as default or the first in the geometry list
		int indexOfSelected = this.comboGeometryList.indexOf(geomSelected);
		if (indexOfSelected != -1) {
			// keeps the user selection
			this.comboGeometryList.select(indexOfSelected);
		} else {
			// if Geometry class exists then it is the default value, in other
			// case select the first geometry of list
			indexOfSelected = this.comboGeometryList.indexOf(Geometry.class.getSimpleName());
			if (indexOfSelected != -1) {

				this.comboGeometryList.select(indexOfSelected);
			} else {
				this.comboGeometryList.select(0);
			}

		}

	}

	/**
	 * Get the target geometry class if the combo has any value selected.
	 * 
	 * @return The target class or null if nothing selected.
	 */
	public Class<? extends Geometry> getTargetClass() {

		int index = comboGeometryList.getSelectionIndex();
		String geomName;
		Class<? extends Geometry> geomClass;

		if (index != -1) {
			geomName = comboGeometryList.getItem(index);
			geomClass = (Class<? extends Geometry>) comboGeometryList.getData(geomName);
			return geomClass;
		}

		return null;
	}

}
