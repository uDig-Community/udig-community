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
package es.axios.udig.spatialoperations.internal.ui.parameters;

import java.io.IOException;
import java.util.Collections;
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
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.internal.control.ISOController;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.view.Message;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.mediator.PlatformGISMediator;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Composite containing the spatial operation' parameters selected
 * <p>
 * The subclass of this are responsible of presenting the content 
 * for operation parameters. It must implement the <code>createContents()</code> method
 * to create the specific controls for the parameter content presenter.
 * 
 * <p>
 * Subclasses may override these methods:
 * <ul>
 *  <li><code>createContents()</code> - must be implemented to display the specific controls</li>  
 *  <li><code>populate()</code> - may be extended or reimplemented</li>
 *  <li><code>clearInputs()</code> - may be extended or reimplemented</li>
 *  <li><code>changeContent()</code> - must be implemented to Check the consistence between this presenter and its model</li>
 * </ul>
 * </p>
 * </p>
 * <p>
 * To create an instance have into account the following idiom:
 * </p>
 * <pre><code>
 *  public MyParamsPresenter(){
 * 
 *      super(parent, style);
 *   
 *      super.initialize();
 *   
 *      populate();
 *   
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
 * Override
 *   public String getOperationName(){
 *       return "The new operation name"; 
 *   };
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
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public abstract class AbstractParamsPresenter extends Composite implements ISOParamsPresenter {
    
    private boolean                 wasInitialized       = false;

    private ISOController           controller           = null;
    private ISOParamsPresenter      parentParamPresenter = null;

    // data
    private IToolContext            context              = null;

    // listeners
    /** Map Listener used to catch the map changes */
    private IMapCompositionListener mapListener          = null;

    /** Layer Listener used to catch the layers changes */
    private ILayerListener          layerListener        = null;
    
    /**
     * This method must be override by subclass. A typical implementation could have the following
     * idiom:
     * 
     * <pre><code>
     * public ANewParamsPresenter() {
     * 
     *     super(parent, style);
     * 
     *     super.initialize();
     * 
     *     populate();
     * 
     * }
     * </code> </pre>
     * 
     * @see initialize()
     * @param parent
     * @param style
     */
    public AbstractParamsPresenter( Composite parent, int style ) {
        super(parent, style);
        
    }
    
    /**
     * Creates the widgets for this presenter.
     * <p>
     * This is a template method which creates the component of the presenter.
     * The method call <Link>createContents</link> that requires an implementation. 
     * </p>
     */
    protected void initialize() {
        
        this.wasInitialized = false;

        createContents();
        
        initListeners();
        
        this.wasInitialized = true;
        
    }
    
    /**
     * create the default listeners for spatial operations.
     * 
     */
    private void initListeners() {

        this.mapListener = new IMapCompositionListener(){

            public void changed(MapCompositionEvent event ) {

                if( !wasInitialized()) return;
                if( getController().isStopped()) return;

                updatedMapLayersActions(event);

            }
        };
        
        this.layerListener = new ILayerListener(){

            public void refresh( LayerEvent event ) {
                
                if( !wasInitialized()) return;
                if( getController().isStopped()) return; 
                
                updateLayerActions(event);
            }};

    }

        
    /**
     * @return true if the presenter is ready to work, false in other case
     */
    public boolean wasInitialized(){
        return (!this.isDisposed())
            && (this.controller != null) 
            && (this.wasInitialized);
    }

    
    /**
     * Opens the presenter populating its widgets. 
     * @see clear()
     * @see initState();
     * @see populate();
     */
    public void open(){
        
        setEnabled(true);

        initState();

        clearInputs();
        
        populate();
        
    }
    
    /**
     * initializes the presenter state. 
     * Override this method if you want to do 
     * a specific initialization of your presenter.
     * (nothing by default)
     */
    protected void initState() {
        // nothing by default
    }

    public void close(){
        setEnabled(false);
    }
    
    /**
     * Must be override by the implementation class responsible to 
     * provide the name to show the operation name to user
     *@return the operation name
     */
    public String getOperationName(){
        // default 
        return ""; //$NON-NLS-1$
    };
    


    /**
     * Creates the widgets for this presenter.
     * This method must be called from subclass to assure the correct initialization.
     */
    protected abstract void createContents();

    /**
     * Sets the default values of widgets (nothing by default)
     */
    protected abstract void populate();
    

    public void setController( ISOController controller ) {
        
        assert controller != null;
        this.controller = controller;
    }

    public ISOController getController() {
        
        assert this.controller != null;
        return this.controller;
    }
    /**
     * Displays the message in the standard information area.
     *
     * @param message
     */
    protected void setMessage(final Message message){
        getController().setMessage(message);        
    }
    
    
    /**
     * Display operation's parameters
     */
    public void visible(final boolean present){
        displayPresenter(present);
    }
    /**
     * Display operation's parameters
     */
    protected void displayPresenter(final boolean present){
        
        
        setVisible(present);
        if(present ){
            
            refresh();
            
            this.getController().run();
        }else{
            this.getController().stop();
        }
        
    }
    

    /**
     * Clears the values of widgets (nothing by default)
     */
    public void clear(){
        
        clearInputs();
        
        populate();
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
    public boolean isTop(){
        return parentParamPresenter == null;
    }

    public void setParentPresenter( final ISOParamsPresenter presenter ){
        assert presenter != null;
        
        this.parentParamPresenter = presenter;
    }

    /**
     * @return the top presenter for the operation's parameters
     */
    public ISOParamsPresenter getTopPresenter() {
        
        ISOParamsPresenter top;
       
        if(isTop() ){
            top=  this;
        }else{
            top = this.parentParamPresenter.getTopPresenter();
        }
        return top;
    }
    
    

    /**
     * Updates the contents of this presenter
     */
    protected void refresh() {

        
    }


    /**
     * Sets the context. 
     * 
     * @param context
     */    
    public void setContext(final IToolContext context){
     
        
        IMap map;
        if (context == null){
            // initialize or reinitialize the Presenter
            map = getCurrentMap();
            if(map != null){
                removeListenerFrom(map);
            }
        }
        else{
            // sets maps and its layers as current
            map = context.getMap();
            if (map != null) {

                // add this presenter as listener of the map
                Set<ILayer> layerList =  getLayerListOf(map);
                addListenersTo(map, layerList);
            }
        }
        this.context = context;
            
        // notifies the change in current map
        changedMapActions(map);
        changedLayerListActions();

    }

    /**
     * @return the context
     */
    protected IToolContext getContext(){
        return this.context;
    }


    /**
     * @return the current Map; null if there is not any Map.
     */    
    public IMap getCurrentMap(){
     
        if(this.context == null)
            return null;

        return this.context.getMap();
    }

    /**
     * @return the current map's CRS or null if current map is null
     */
    protected CoordinateReferenceSystem getCurrentMapCrs() {
        IMap map = getCurrentMap(); 
        CoordinateReferenceSystem crs = (map != null)? MapUtil.getCRS(map) : null;
        return crs;
    }
    
    
    /**
     * gets the layer list from a map
     *
     * @param map
     * @return the Layer list of map
     */
    protected Set<ILayer> getLayerListOf(IMap map){
        
        assert map != null;
        
        return AppGISMediator.getMapLayers(map);
    }
    
    /**
     * @return the layer list of current  map
     */
    protected Set<ILayer> getCurrentLayerList(){
     
        if (getCurrentMap() == null){
            return Collections.emptySet();
        }
        return AppGISMediator.getMapLayers(this.getCurrentMap());
    }


    /**
     * This call back is called when the map is changed. 
     * Nothing by default you must be override if your 
     * presenter requieres specific actions.
     */
    protected void changedMapActions(@SuppressWarnings("unused")
                                     final IMap map) {
        
        // nothing by default
    }

    /**
     * Sets the map as current and add the listeners to listen the 
     * changes in the map and its layers.
     * Additionally it initializes the current layer list.
     * @param map
     */
    private void addListenersTo( final IMap map,  final Set<ILayer> layerList ) {

        assert map != null;
        assert layerList != null;
        assert this.mapListener != null;
        assert this.layerListener != null;
        
        map.addMapCompositionListener(this.mapListener);
        
        for( ILayer layer : layerList) {
            
            layer.addListener( this.layerListener );
        }
    }
    

    /**
     * Removes the listeners from map.
     * 
     * @param currentMap
     */
    private void removeListenerFrom( IMap map ) {

        assert map != null;
        assert this.mapListener != null;
        assert this.layerListener != null;

        for( ILayer layer : getCurrentLayerList()) {
            
            layer.removeListener( this.layerListener );
        }
        
        map.removeMapCompositionListener(this.mapListener);
    }
    

   /**
    * This method is called if the collection of layer is updated (added or removed).
    * This is a template method that calls a specific method by each event type.
    * 
    * @see changedLayerListActions()
    * @see addedLayerActions()
    * @see removedLayerActions()
    * @param event
    */
    private void updatedMapLayersActions(final MapCompositionEvent event ) {

        MapCompositionEvent.EventType eventType = event.getType();
        
        switch( eventType ) {
        
        case ADDED:{
            PlatformGISMediator.runInDisplayThread(new Runnable(){
                public void run() {
                    final ILayer layer = event.getLayer();
                    addedLayerActions(layer);
                }
            });
            break;
        }
        case REMOVED:{
            
            PlatformGISMediator.runInDisplayThread(new Runnable(){
                public void run() {
                    final ILayer layer = event.getLayer();
                    removedLayerActions(layer);
                }
            });
            break;
        }
        case MANY_ADDED:
        case MANY_REMOVED: 
            PlatformGISMediator.runInDisplayThread(new Runnable(){

                public void run() {
                    changedLayerListActions();
                }
            });
            break;
        default:
            break;
        }
    }
    
    /**
     * This is a callback method which should be used to updates the 
     * content of its widget, checking the list layer in map.
     */
    protected void changedLayerListActions(){
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
    protected void removedLayerActions( final ILayer layer ) {
        
        layer.removeListener(this.layerListener);
    }

    /**
     * This is a callback method, It should be used to implement the actions 
     * required when a new layer is added to map. 
     * The event occurs when a layer is created or added to the map.
     * <p>
     * This method provide a default implementation which add a {@link ILayerListener} 
     * Do not forget call this method to maintain the listener list.
     * </p>
     * 
     * @param layer
     */
    protected void addedLayerActions( final ILayer layer ) {
        
        layer.addListener( this.layerListener );
        
        
    }
    
    /**
     * This method is called when a layer is changed.
     *
     * @param event
     * 
     * @see changedFilterSelection
     * @see changedLayerActions
     */
    private void updateLayerActions(final LayerEvent event ) {

        final ILayer modifiedLayer = event.getSource();
        
        PlatformGISMediator.syncInDisplayThread(new Runnable(){
            
            public void run() {
                
                LayerEvent.EventType type = event.getType();
                switch( type ) {
                case ALL:
                    changedLayerActions(modifiedLayer);
                    break;
        
                case FILTER:
                    Filter newFilter = modifiedLayer.getFilter();
                    changedFilterSelectionActions(modifiedLayer, newFilter);
                default:
                    break;
                }
            }
            
        });
    }

    /**
     * This is a callback method, It should be used to implement the actions 
     * required when the features selected in layer are changed. 
     * The event occurs when a layer is created or added to the map.
     * @param layer 
     * @param newFilter the filter or Filter.ALL if there is no any feature selected
     */
    protected void changedFilterSelectionActions( @SuppressWarnings("unused")
                                                  final ILayer layer, 
                                                  @SuppressWarnings("unused")
                                                  final Filter newFilter ) {
        // nothing as default implementation
    }

    /**
     * This is a callback method, It should be used to implement the actions 
     * required when a layer is modified. 
     * The event occurs when a layer is created or added to the map.
     * 
     * @param modifiedLayer the modified layer
     */
    protected void changedLayerActions( @SuppressWarnings("unused")
                                        final ILayer modifiedLayer ) {
        // nothing by default implementation
    }
    
    /**
     * Gets the selected layer from combo box
     * 
     * @param comboLayer
     * @param textFeatures
     * @return the layer selected
     */
    protected ILayer getSelecedLayer( final CCombo comboLayer) {
        ILayer currentLayer;
        
        int index = comboLayer.getSelectionIndex();
        if(index == -1  ){
            currentLayer = null;
        } else {
            final String layerName = comboLayer.getItem(index);
            currentLayer = (ILayer)comboLayer.getData(layerName);
            
        }
        return currentLayer;
    }
    /**
     * Presents the features' sum in the layer 
     *
     * @param layer 
     * @param textSelectedFeatures the widget where the computed sum is presented
     */
    protected void presentFeaturesSumOfLayer( final ILayer layer, final CLabel textSelectedFeatures ) {
        
        presentSelectedFeaturesSum( layer, Filter.INCLUDE, textSelectedFeatures );
    }
    
    
    /**
     * Presents the sum of features in the layer and returns the selected features.
     * If any feature is selected returns all layer's features.
     *
     * @param layer
     * @param filter
     * @param textSelectedFeatures
     * @return FeatureCollection<SimpleFeatureType, SimpleFeature> the selected features. If any feature is selected returns all layer's features.
     */
    protected FeatureCollection<SimpleFeatureType, SimpleFeature> presentSelectedFeaturesSum(final ILayer layer, 
                                                           final Filter filter, 
                                                           final CLabel textSelectedFeatures ) {
        assert layer != null;
        assert filter!= null;
        
        int count = 0;
        FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures = null;
        try {
            // Particular semantic of uDig, when the filter is deleted filter ALL is setted, for 
            // spatial operation we need select all features (Filter.NONE)
            Filter adjustedFilter = null;
            if(Filter.EXCLUDE.equals(filter)){
                adjustedFilter = Filter.INCLUDE;
            }else{
                adjustedFilter = filter;
            }
            // Applies the the filter, if the feature collection is empty, will select all layer's features
            selectedFeatures = LayerUtil.getSelectedFeatures(layer, adjustedFilter);
            if(selectedFeatures.isEmpty()){
                selectedFeatures = LayerUtil.getSelectedFeatures(layer, Filter.INCLUDE);
                count = Integer.MAX_VALUE; // presents All as selected
            } else {
                count = GeoToolsUtils.computeCollectionSize(selectedFeatures);  
            }
            assert count >= 0 && count <= Integer.MAX_VALUE;
 
        } catch (IOException e) {
            
            final String text = Messages.AbstractParamsPresenter_failed_selecting_features;
            setMessage(new Message(text, Message.Type.FAIL));
            throw (RuntimeException) new RuntimeException( text ).initCause( e );
        
        } finally{

            String strCount;
            if(count == Integer.MAX_VALUE){
                strCount =Messages.AbstractParamsPresenter_all_features_selected; 
                
            }else {
                strCount = String.valueOf(count);
            }
            textSelectedFeatures.setText(strCount);
        }
        return selectedFeatures;
    }
    
    /**
     * Updates the widgets that contain the layers. The current selected layer 
     * is maintained only if the current layer have not been deleted.  
     */
    protected void changeSelectedLayer( final ILayer currentLayer, final CCombo comboToChange ) {

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
     * Does a copy of layer in combo box.
     * @param comboLayer
     */
    protected void loadComboWithLayerList( CCombo comboLayer ) {

        comboLayer.removeAll();

        Set<ILayer> list = getCurrentLayerList();
        for( ILayer layer : list ) {

            String name = layer.getName();
            comboLayer.add(name);
            comboLayer.setData(name, layer);

        }

    }
    
        
}
