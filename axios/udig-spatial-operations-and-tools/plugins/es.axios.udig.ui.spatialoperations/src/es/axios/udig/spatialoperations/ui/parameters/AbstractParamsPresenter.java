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
package es.axios.udig.spatialoperations.ui.parameters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ILayerListener;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.IMapCompositionListener;
import net.refractions.udig.project.LayerEvent;
import net.refractions.udig.project.MapCompositionEvent;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand.CmdStatus;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand.ParameterName;
import es.axios.udig.spatialoperations.ui.view.ISOPresenter;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.mediator.PlatformGISMediator;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Composite containing the spatial operation' parameters selected
 * <p>
 * The subclass of this are responsible of presenting the content for operation
 * parameters. It must implement the <code>createContents()</code> method to
 * create the specific controls for the parameter content presenter.
 * 
 * <p>
 * Subclasses may override these methods:
 * <ul>
 * <li><code>createContents()</code> - must be implemented to display the
 * specific controls</li>
 * <li><code>populate()</code> - may be extended or reimplemented</li>
 * <li><code>clearInputs()</code> - may be extended or reimplemented</li>
 * <li><code>changeContent()</code> - must be implemented to Check the
 * consistency between this presenter and its command.</li>
 * </ul>
 * </p>
 * </p>
 * <p>
 * To create an instance have into account the following idiom:
 * </p>
 * 
 * <pre><code>
 *  public MyParamsPresenter(){
 * 
 *      super(parent, style);
 *   
 *      super.initialize();
 *  }
 *  
 *  initState() {
 *  	//set the resultComposite as observer (if that is necessary )
 *  	ISOCommand cmd = getCommand();
 *	 	cmd.addObserver(this.aComposite1);
 *	 	cmd.addObserver(this.aComposite2);
 *	 	...
 *  }
 * Override
 *   protected void createContents() {
 *      // create the parameter widgets required by this implementation
 *   }
 * 
 * 
 * Override
 *   protected void populate() {
 *      // fill widgets with default values
 *   }
 * 
 * </code> </pre>
 * 
 * @see createContents()
 * @see populate()
 * @see clearInputs()
 * @see changeContent()
 * 
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
public abstract class AbstractParamsPresenter extends Composite implements ISOParamsPresenter {

	private ISOPresenter			soPresenter				= null;

	private boolean					wasInitialized			= false;

	private ISOParamsPresenter		parentParamPresenter	= null;

	// data
	private IToolContext			context					= null;

	// listeners
	/** Map Listener used to catch the map changes */
	private IMapCompositionListener	mapListener				= null;

	/** Layer Listener used to catch the layers changes */
	private ILayerListener			layerListener			= null;

	/**
	 * LF is LayerFilter list. Stores each layer and its filter, until the user
	 * clean all the filters. TODO not implemented yet
	 */
	protected List<LF>				LFList					= new LinkedList<LF>();

	protected Thread				uiThread;

	private ISOCommand				command					= null;

	/**
	 * This method must be override by subclass. A typical implementation could
	 * have the following idiom:
	 * 
	 * <pre><code>
	 * public ANewParamsPresenter() {
	 * 
	 * 	super(parent, style);
	 * 
	 * 	super.initialize();
	 * 
	 * }
	 * </code> </pre>
	 * 
	 * @see initialize()
	 * @param parent
	 * @param style
	 */
	public AbstractParamsPresenter(Composite parent, int style) {

		super(parent, style);

	}

	public ISOCommand getCommand() {

		if (this.command == null) {
			Runnable runnable = new Runnable() {

				public void run() {

					// returns the command instance maintained in the top
					// presenter
					Composite composite = getParent();
					while (!(composite instanceof ISOTopParamsPresenter)) {
						composite = composite.getParent();
					}
					command = ((ISOTopParamsPresenter) composite).getCommand();
				}

			};

			Display.findDisplay(uiThread).syncExec(runnable);
		}

		return this.command;
	}

	/**
	 * Creates the widgets for this presenter.
	 * <p>
	 * This is a template method which creates the component of the presenter.
	 * The method call {@link #createContents()} that requires an
	 * implementation.
	 * </p>
	 */
	protected void initialize() {

		this.wasInitialized = false;

		this.uiThread = Thread.currentThread();

		createContents();

		initListeners();

		this.wasInitialized = true;

	}

	/**
	 * create the default listeners for spatial operations.
	 * 
	 */
	private void initListeners() {

		this.mapListener = new IMapCompositionListener() {

			public void changed(MapCompositionEvent event) {

				if (!wasInitialized()) {
					return;
				}
//				if (getCommand().isStopped()) {
//					return;
//				}

				updatedMapLayersActions(event);
			}
		};

		this.layerListener = new ILayerListener() {

			public void refresh(LayerEvent event) {

				if (!wasInitialized()) {
					return;
				}
//				if (getCommand().isStopped()) {
//					return;
//				}

				updateLayerActions(event);
			}
		};

	}

	/**
	 * @return true if the presenter is ready to work, false in other case
	 */
	public boolean wasInitialized() {

		return (!this.isDisposed()) && (this.getCommand() != null) && (this.wasInitialized);
	}

	/**
	 * Opens the presenter populating its widgets.
	 * 
	 * @see clear()
	 * @see initState();
	 * @see populate();
	 */
	public void open() {

		Display.findDisplay(uiThread).syncExec(new Runnable() {

			public void run() {
				IMap map = getCurrentMap();

				setEnabled(map != null);

				initState();

				clearInputs();

				setDefaultParameters();
			}
		});
	}

	/**
	 * initializes the presenter state. Override this method if you want to do a
	 * specific initialization of your presenter. (nothing by default)
	 */
	protected void initState() {

		// nothing by default
	}

	/**
	 * Closes the spatial operation presenter. This message is sent when the
	 * framework need to present other spatial operation selected by the user
	 */
	public void close() {

		getCommand().setStatus(CmdStatus.STOPPED);

		setEnabled(false);
	}

	/**
	 * Creates the widgets for this presenter. This method must be called from
	 * subclass to assure the correct initialization.
	 */
	protected abstract void createContents();

	/**
	 * Sets as Ready its command an populate the widgets with the default
	 * values.
	 */
	private void setDefaultParameters() {
		ISOCommand cmd = getCommand();

		cmd.setStatus(CmdStatus.STOPPED);

		populate();

		cmd.setStatus(CmdStatus.READY);

		validateParameters();
	};

	/**
	 * Sets the parameters on command an validate
	 */
	public boolean validateParameters() {

		ISOCommand cmd = getCommand();

		setParametersOnCommand(cmd);

		if (!cmd.isReady()) {
			return false;
		}

		return validateData();

	}

	/**
	 * Should be override in subclass if the presenter has values to set in the
	 * command
	 */
	protected void setParametersOnCommand(ISOCommand command) {
		// null implementation by default
	}

	/**
	 * Sets the default values of widgets (nothing by default)
	 */
	protected abstract void populate();

	/**
	 * Display operation's parameters
	 */
	public void visible(final boolean present) {

		displayPresenter(present);
	}

	/**
	 * Display operation's parameters
	 */
	protected void displayPresenter(final boolean present) {

		Display.findDisplay(uiThread).syncExec(new Runnable() {

			public void run() {
				setVisible(present);

				refresh();
			}
		});

	}

	/**
	 * Clears the values of widgets
	 */
	public void clear() {

		Display.findDisplay(uiThread).syncExec(new Runnable() {

			public void run() {

				IMap map = getCurrentMap();

				setEnabled(map != null);

				setDefaultValues();

				clearInputs();

				setDefaultParameters();
			}
		});

	}

	/**
	 * Set the default parameter's values
	 */
	protected void setDefaultValues() {
		// nothing

	}

	/**
	 * clears the input values of this content
	 */
	protected void clearInputs() {

		// nothing
	}

	/**
	 * @return true if this presenter is the top
	 */
	public boolean isTop() {

		return parentParamPresenter == null;
	}

	public void setParentPresenter(final ISOParamsPresenter presenter) {

		assert presenter != null;

		this.parentParamPresenter = presenter;
	}

	/**
	 * @return the top presenter for the operation's parameters
	 */
	public ISOParamsPresenter getTopPresenter() {

		ISOParamsPresenter top;

		if (isTop()) {
			top = this;
		} else {
			top = this.parentParamPresenter.getTopPresenter();
		}
		return top;
	}

	/**
	 * Updates the contents of this presenter
	 */
	private void refresh() {

		// setDefaultParameters();
	}

	/**
	 * Sets the context.
	 * 
	 * @param context
	 */
	public void setContext(final IToolContext context) {

		IMap map;
		if (context == null) {
			// initialize or reinitialize the Presenter
			map = getCurrentMap();
			if (map != null) {
				removeListenerFrom(map);
			}
		} else {
			// sets maps and its layers as current
			map = context.getMap();
			if (map != null) {

				// add this presenter as listener of the map
				List<ILayer> layerList = getLayerListOf(map);
				addListenersTo(map, layerList);
			}
		}
		this.context = context;

		// notifies the change in current map
		changedMapActions(map);
		if (map != null) {
			changedLayerListActions();
			validateParameters();
		}
	}

	/**
	 * @return the context
	 */
	protected IToolContext getContext() {

		return this.context;
	}

	/**
	 * @return the current Map; null if there is not any Map.
	 */
	public IMap getCurrentMap() {

		if (this.context == null) {
			return null;
		}

		return this.context.getMap();
	}

	/**
	 * @return the current map's CRS or null if current map is null
	 */
	protected CoordinateReferenceSystem getCurrentMapCrs() {

		IMap map = getCurrentMap();
		CoordinateReferenceSystem crs = (map != null) ? MapUtil.getCRS(map) : null;
		return crs;
	}

	/**
	 * gets the layer list from a map
	 * 
	 * @param map
	 * @return the Layer list of map
	 */
	protected List<ILayer> getLayerListOf(IMap map) {

		assert map != null;

		return AppGISMediator.getMapLayers(map);
	}

	/**
	 * @return the layer list of current map
	 */
	protected List<ILayer> getCurrentLayerList() {

		if (getCurrentMap() == null) {
			return Collections.emptyList();
		}
		return AppGISMediator.getMapLayers(this.getCurrentMap());
	}

	/**
	 * This call back is called when the map is changed. Nothing by default you
	 * must be override if your presenter requires specific actions.
	 */
	protected void changedMapActions(final IMap map) {

		// nothing by default
	}

	/**
	 * Sets the map as current and add the listeners to listen the changes in
	 * the map and its layers. Additionally it initializes the current layer
	 * list.
	 * 
	 * @param map
	 */

	private void addListenersTo(final IMap map, final List<ILayer> layerList) {

		assert map != null;
		assert layerList != null;
		assert this.mapListener != null;
		assert this.layerListener != null;

		map.addMapCompositionListener(this.mapListener);

		for (ILayer layer : layerList) {

			layer.addListener(this.layerListener);
		}
	}

	/**
	 * Removes the listeners from map.
	 * 
	 * @param currentMap
	 */
	private void removeListenerFrom(IMap map) {

		assert map != null;
		assert this.mapListener != null;
		assert this.layerListener != null;

		for (ILayer layer : getCurrentLayerList()) {

			layer.removeListener(this.layerListener);
		}

		map.removeMapCompositionListener(this.mapListener);
	}

	/**
	 * This method is called if the collection of layer is updated (added or
	 * removed). This is a template method that calls a specific method by each
	 * event type.
	 * 
	 * @see changedLayerListActions()
	 * @see addedLayerActions()
	 * @see removedLayerActions()
	 * @param event
	 */
	private void updatedMapLayersActions(final MapCompositionEvent event) {

		MapCompositionEvent.EventType eventType = event.getType();

		switch (eventType) {

		case ADDED: {
			Display.findDisplay(uiThread).asyncExec(new Runnable() {
				public void run() {

					final ILayer layer = event.getLayer();
					addedLayerActions(layer);
					validateParameters();
				}
			});
			break;
		}
		case REMOVED: {

			Display.findDisplay(uiThread).asyncExec(new Runnable() {
				public void run() {

					final ILayer layer = event.getLayer();
					removedLayerActions(layer);
					validateParameters();
				}
			});
			break;
		}
		case MANY_ADDED: 
		case MANY_REMOVED:
			Display.findDisplay(uiThread).asyncExec(new Runnable() {

				public void run() {

					changedLayerListActions();
					validateParameters();
				}
			});
			break;
		default:
			break;
		}
	}

	/**
	 * This is a callback method which should be used to updates the content of
	 * its widget, checking the list layer in map.
	 */
	protected void changedLayerListActions() {

		// nothing
	}

	/**
	 * This is a callback method, It should be used to implement the actions
	 * required when a layer is deleted from map.
	 * <p>
	 * This method provide a default implementation which remove the listener.
	 * You can override this method to provide specific actions, Do not forget
	 * call this method to maintain the listener list.
	 * </p>
	 * 
	 * @param layer
	 */
	protected void removedLayerActions(final ILayer layer) {

		layer.removeListener(this.layerListener);
		// TODO implement removeLayerListActions(layer).
		removeLayerListActions(layer);
		changedLayerListActions();

	}

	/**
	 * Should be redefined if the subclass requires an specific behaviour.
	 */
	protected void removeLayerListActions(ILayer layer) {
		// null implementation.

	}

	/**
	 * This is a callback method, It should be used to implement the actions
	 * required when a new layer is added to map. The event occurs when a layer
	 * is created or added to the map.
	 * <p>
	 * This method provide a default implementation which add a
	 * {@link ILayerListener} Do not forget call this method to maintain the
	 * listener list.
	 * </p>
	 * 
	 * @param layer
	 */
	protected void addedLayerActions(final ILayer layer) {

		layer.addListener(this.layerListener);

		changedLayerListActions();

	}

	/**
	 * This method is called when a layer is changed.
	 * 
	 * @param event
	 * 
	 * @see {@link #changedFilterSelectionActions(ILayer, Filter)}
	 * @see {@link  #changedLayerActions(ILayer)}
	 */
	private void updateLayerActions(final LayerEvent event) {

		final ILayer modifiedLayer = event.getSource();

		PlatformGISMediator.syncInDisplayThread(new Runnable() {

			public void run() {

				LayerEvent.EventType type = event.getType();
				switch (type) {
				case ALL:
					changedLayerActions(modifiedLayer);
					break;

				case FILTER:
					Filter newFilter = modifiedLayer.getFilter();
					changedFilterSelectionActions(modifiedLayer, newFilter);
					break;

				default:
					break;
				}
			}

		});
	}

	/**
	 * This is a callback method, It should be used to implement the actions
	 * required when the features selected in layer are changed. The event
	 * occurs when a layer is created or added to the map.
	 * 
	 * @param layer
	 * @param newFilter
	 *            the filter or Filter.ALL if there is no any feature selected
	 */
	protected void changedFilterSelectionActions(final ILayer layer, final Filter newFilter) {

		// nothing as default implementation
	}

	/**
	 * This is a callback method, It should be used to implement the actions
	 * required when a layer is modified. The event occurs when a layer is
	 * created or added to the map.
	 * 
	 * @param modifiedLayer
	 *            the modified layer
	 */
	protected void changedLayerActions(final ILayer modifiedLayer) {

		// nothing by default implementation
	}

	/**
	 * Gets the selected layer from combo box
	 * 
	 * @param comboLayer
	 * @param textFeatures
	 * @return the layer selected
	 */
	protected ILayer getSelecedLayer(final CCombo comboLayer) {

		ILayer currentLayer;

		int index = comboLayer.getSelectionIndex();
		if (index == -1) {
			currentLayer = null;
		} else {
			final String layerName = comboLayer.getItem(index);
			currentLayer = (ILayer) comboLayer.getData(layerName);

		}
		return currentLayer;
	}

	/**
	 * Look at the layer filter, if the filter is EXCLUDE or INCLUDE, return All
	 * that means all the features will be processed, if the filter is different
	 * from those, return BBox, that means only the selected features will be
	 * processed.
	 * 
	 * @param layer
	 * @param filter
	 * @param textSelectedFeatures
	 * @return FeatureCollection the selected features. If any feature is
	 *         selected returns all layer's features.
	 */
	protected void presentSelectionAllOrBBox(final ILayer layer, final Filter filter, final CLabel textSelectedFeatures) {

		assert layer != null : "illegal argument, layer cannot be null"; //$NON-NLS-1$
		assert filter != null : "illegal argument, filter cannot be null"; //$NON-NLS-1$

		String strCount = ""; //$NON-NLS-1$

		LF layerFilter = getLayerFromLF(layer);
		if (layerFilter != null) {
			// FIXME uncomment that when LF(layerFilter) works, for now on, next
			// IF is a patch.
			if (Filter.EXCLUDE.equals(filter) || Filter.INCLUDE.equals(filter)) {
				// if (layerFilter.getFilter().equals(Filter.ALL)
				// || layerFilter.getFilter().equals(Filter.NONE)) {

				strCount = Messages.AbstractParamsPresenter_all_features_selected;
			} else {
				strCount = "BBox."; //$NON-NLS-1$
			}
		} else {
			if (Filter.EXCLUDE.equals(filter) || Filter.INCLUDE.equals(filter)) {

				strCount = Messages.AbstractParamsPresenter_all_features_selected;
			} else {
				strCount = "BBox."; //$NON-NLS-1$
			}
		}

		textSelectedFeatures.setText(strCount);
		textSelectedFeatures.setToolTipText(filter.toString());

	}

	/**
	 * Adjusts the layer's filter
	 * <p>
	 * Note: filter ALL has not sense in spatial operation context
	 * </p>
	 * 
	 * @param currentSourceLayer
	 * @return Filter
	 */
	protected Filter getFilter(ILayer layer) {

		assert layer != null;

		Filter filter = layer.getFilter();
		if (Filter.EXCLUDE.equals(filter)) {
			filter = Filter.INCLUDE;

		}
		assert (filter != null) && (!Filter.EXCLUDE.equals(filter)) : "illegal postcondition: filter cannot be null"; //$NON-NLS-1$

		return filter;
	}

	/**
	 * Updates the widgets that contain the layers. The current selected layer
	 * is maintained only if the current layer have not been deleted.
	 */
	protected void changeSelectedLayer(final ILayer currentLayer, final CCombo comboToChange) {

		if (currentLayer == null) {
			return;
		}

		// maintains the selection if the current layer have not been deleted,
		// otherwise delete the selection.
		String currentLayerName;
		int index;
		currentLayerName = currentLayer.getName();
		index = comboToChange.indexOf(currentLayerName);
		if (index != -1) {
			comboToChange.select(index);
		} else {
			comboToChange.clearSelection();
		}
	}

	/**
	 * Load an specific comboBox and filter the compatible layers.
	 * 
	 * @param comboLayer
	 * @param domain
	 *            Get domain for a certain parameter.
	 */
	protected void loadComboWithLayerList(CCombo comboLayer, ParameterName domain) {

		comboLayer.removeAll();

		List<ILayer> list = getCurrentLayerList();
		// get the compatible geometries
		ISOCommand cmd = getCommand();
		List<?> geometries = cmd.getDomainValues(domain);

		for (ILayer layer : list) {
			Class<?> type = layer.getSchema().getGeometryDescriptor().getType().getBinding();
			if (geometries.contains(type)) {
				String name = layer.getName();
				comboLayer.add(name);
				comboLayer.setData(name, layer);
			}
		}
	}

	/**
	 * load the list of layers in the specified combobox
	 * 
	 * @param comboLayer combobox to be loaded
	 */
	protected void loadComboWithLayerList(CCombo comboLayer) {

		comboLayer.removeAll();
		for (ILayer layer : getCurrentLayerList()) {
			String name = layer.getName();
			comboLayer.add(name);
			comboLayer.setData(name, layer);
		}
	}

	/**
	 * Return the number of layer contained on the map.
	 * 
	 * @return
	 */
	protected int getLayerCount() {

		List<ILayer> layerList = getCurrentLayerList();
		return layerList.size();
	}

	/**
	 * Update the LayerFilter list. If it is empty, add the layers, and if the
	 * user have added a new layer, also add it to the list.
	 */
	private void updateLFList() {

		if (this.LFList.size() == 0) {
			addLF();
			return;
		}

		removeLF();

		List<ILayer> layerList = getCurrentLayerList();
		Iterator<ILayer> layerIterator = layerList.iterator();
		ILayer layer = null;

		while (layerIterator.hasNext()) {

			layer = layerIterator.next();
			if (!layerFilterContains(layer)) {
				this.LFList.add(new LF(layer));
			}
		}
	}

	/**
	 * Look at the <code>layer</code> in the <code>LayerFilterList</code>.
	 * If the list contains the <code>layer</code> , return true.
	 * 
	 * @param layer
	 *            The layer
	 * @return If it find, return true.
	 */
	private boolean layerFilterContains(ILayer layer) {

		assert layer != null;

		Iterator<LF> iterator = this.LFList.iterator();
		LF layerFilter = null;

		while (iterator.hasNext()) {

			layerFilter = iterator.next();
			if (layer.equals(layerFilter.getLayer())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Update the LayerFilter list and stores the filter of each layer until all
	 * the filters become Filter.EXCLUDE, that means, the users clear are the
	 * filters.
	 */
	protected void setLayerAndFilter() {

		updateLFList();

		storeFilters();

	}

	/**
	 * If the filters are distinct of <code>Filter.EXCLUDE</code>, stores it.
	 * When the user select features from layer 1 on a bbox, we store the filter
	 * because when the user try to select whit a bbox another features from
	 * layer 2, the layer 1 filter become Filter.EXCLUDE and we lost the
	 * features selected from the layer 1.
	 * 
	 * This function will store the filters, so we wont loose that filters.
	 */
	private void storeFilters() {

		List<ILayer> layerList = getCurrentLayerList();
		Iterator<ILayer> layerIterator = layerList.iterator();
		ILayer currentLayer = null;
		LF LFlayer = null;
		Filter filter = null;

		while (layerIterator.hasNext()) {

			currentLayer = layerIterator.next();
			filter = currentLayer.getFilter();
			if (!Filter.EXCLUDE.equals(filter) && !Filter.INCLUDE.equals(filter)) {

				LFlayer = getLayerFromLF(currentLayer);
				if (LFlayer != null) {

					LFlayer.setFilter(filter);
					this.LFList.set(this.LFList.indexOf(LFlayer), LFlayer);
				}
			}
		}

	}

	/**
	 * Look at the <code>layer</code> on the <code>LFList</code> and if it
	 * contains, return the current <code>LFList</code> registry. If not, return
	 * null.
	 * 
	 * @param layer
	 *            The layer to retrieve
	 * @return The LayerFilter registry that contains the correspondent
	 *         <code>layer</code>
	 */
	private LF getLayerFromLF(ILayer layer) {

		assert layer != null;

		Iterator<LF> iterator = this.LFList.iterator();
		LF layerFilter = null;

		while (iterator.hasNext()) {

			layerFilter = iterator.next();
			if (layer.equals(layerFilter.getLayer())) {
				return layerFilter;
			}
		}
		// Doesn't contains.
		return null;
	}

	/**
	 * Set all the filter of the <code>LFList</code> to
	 * <code>Filter.EXCLUDE</code>.
	 */
	protected void resetLF() {

		Iterator<LF> iterator = this.LFList.iterator();
		LF layerFilter = null;

		while (iterator.hasNext()) {

			layerFilter = iterator.next();
			layerFilter.setFilter(Filter.EXCLUDE);
		}
	}

	/**
	 * Add current layers and filters to the list.
	 */
	private void addLF() {

		List<ILayer> layerList = getCurrentLayerList();
		Iterator<ILayer> layerIterator = layerList.iterator();
		ILayer layer = null;

		while (layerIterator.hasNext()) {
			layer = layerIterator.next();
			this.LFList.add(new LF(layer));
		}

	}

	/**
	 * When a layer is removed from the map, we assure that the {@link #LFList}
	 * contains only the actual layers.
	 */
	protected void removeLF() {

		List<ILayer> layerList = getCurrentLayerList();
		Set<LF> layerFilterToRemove = new HashSet<LF>();
		Iterator<LF> iterator = this.LFList.iterator();
		LF layerFilter = null;

		while (iterator.hasNext()) {

			layerFilter = iterator.next();
			if (!layerList.contains(layerFilter.getLayer())) {

				layerFilterToRemove.add(layerFilter);
			}
		}
		this.LFList.removeAll(layerFilterToRemove);
	}

	/**
	 * Check if the layers have some feature selected, its filter is distinct of
	 * <code>Filter.EXCLUDE</code>
	 * 
	 * @return True if all the layers have <code>Filter.EXCLUDE</code>
	 */
	protected boolean layersHaveNotSelection() {

		List<ILayer> layerList = getCurrentLayerList();
		Iterator<ILayer> layerIterator = layerList.iterator();
		ILayer layer = null;
		Filter filter = null;

		while (layerIterator.hasNext()) {

			layer = layerIterator.next();
			filter = layer.getFilter();

			if (!Filter.EXCLUDE.equals(filter) && !Filter.INCLUDE.equals(filter)) {

				return false;
			}
		}

		return true;
	}

	/**
	 * LayerAndFilter class.
	 * 
	 * A class that will store a layer and its filter.
	 * 
	 */
	protected class LF {

		private ILayer	layer	= null;
		private Filter	filter	= null;

		/**
		 * LayerAndFilter constructor. Stores the Layer and its filter.
		 */
		public LF(ILayer layer) {

			this.layer = layer;
			this.filter = layer.getFilter();
		}

		public ILayer getLayer() {

			return layer;
		}

		public void setLayer(ILayer layer) {

			this.layer = layer;
		}

		public Filter getFilter() {

			return filter;
		}

		public void setFilter(Filter filter) {

			this.filter = filter;
		}

		@Override
		public int hashCode() {

			final int prime = 31;
			int result = 1;
			result = prime * result + ((filter == null) ? 0 : filter.hashCode());
			result = prime * result + ((layer == null) ? 0 : layer.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof LF)) {
				return false;
			}
			final LF other = (LF) obj;
			if (filter == null) {
				if (other.filter != null) {
					return false;
				}
			} else if (!filter.equals(other.filter)) {
				return false;
			}
			if (layer == null) {
				if (other.layer != null) {
					return false;
				}
			} else if (!layer.equals(other.layer)) {
				return false;
			}
			return true;
		}

	}

	public InfoMessage getDefaultMessage() {

		return getCommand().getDefaultMessage();
	}

	public InfoMessage getMessage() {

		return getCommand().getMessage();
	}

	public String getOperationID() {

		return this.getCommand().getOperationID();
	}

	public final ISOPresenter getOperationPresenter() {

		return this.soPresenter;
	}

	public final void setMessage(final InfoMessage message) {

		this.soPresenter.displayMessage(message);
	}

	public void setSpatialOperationPresenter(ISOPresenter soPresenter) {

		assert soPresenter != null;

		this.soPresenter = soPresenter;
	}

	public void executeCommand() {

		try {
			getCommand().execute();
			reset();
		} catch (SOCommandException e) {
			final InfoMessage msg = new InfoMessage(e.getMessage(), InfoMessage.Type.FAIL);
			setMessage(msg);
			return;
		}
	}

	public void reset() {

		getCommand().reset();

		this.soPresenter.initializeInputs();

	}

	private boolean validateData() {

		ISOCommand cmd = getCommand();

		boolean result = cmd.evalPrecondition();

		ISOPresenter soPresenter = getOperationPresenter();

		soPresenter.displayMessage(cmd.getMessage());

		return result;

	}

	/**
	 * Redefine this method to retrieve:
	 * <ul>
	 * <li>the domain values for the parameters from command</li>
	 * <li>parameters values present in the command</li>
	 * </ul>
	 */
	public void update(Observable o, Object arg) {

		// will implement the get domain.
	}

}
