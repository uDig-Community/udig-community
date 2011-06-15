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
package es.axios.udig.spatialoperations.internal.processmanager;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IClipInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IClipInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IClipParameters;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Clip Process
 * <p>
 * Uses the feature's geometries of clipping collection to intersect the features contained in
 * clipped collection.
 * </p>
 * <p>
 * This transaction must:
 * <ul>
 * <li>create new features using the data of original features, for those features that were
 * broken.
 * <li>delete the features included in clip area  
 * <li>clip geometry of features that intersect.
 * <ul>
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class ClipProcess extends AbstractProcess {

    private static final Logger                       LOGGER                    = Logger
                                                                                        .getLogger(ClipProcess.class
                                                                                                                    .getName());

    private static final FilterFactory                FILTER_FACTORY            = FilterFactoryFinder
                                                                                                     .createFilterFactory();

    private IClipInExistentLayerParameters            paramsClipInExistentLayer = null;

    private IClipInNewLayerParameters                 paramsClipInNewLayer      = null;

    private GeometryDescriptor                     geomAttrType              = null;

    private ILayer                                    targetLayer;

    private FeatureStore<SimpleFeatureType, SimpleFeature>                              targetStore;

    private ILayer                                    clippingLayer;

    private ILayer                                    layerToClip;

    private IMap                                      map;

    private FeatureCollection<SimpleFeatureType, SimpleFeature>                         clippingFeatures;

    private FeatureCollection<SimpleFeatureType, SimpleFeature>                         featuresToClip;

    private IGeoResource                              targetGeoResource;

    private java.util.Map<String, FeatureTransaction> transactionTrack          = new HashMap<String, FeatureTransaction>();           

    

    /**
     * new instance of clip process
     * 
     * @param paramsClipInExistentLayer
     */
    public ClipProcess( final IClipParameters params ) {
        
        
        if(params instanceof IClipInExistentLayerParameters){
            this.paramsClipInExistentLayer = (IClipInExistentLayerParameters)params;
        } else if (params instanceof IClipInNewLayerParameters){
            this.paramsClipInNewLayer = (IClipInNewLayerParameters) params;
            
        } else{
            assert false; //illegal parameter
        }
        
    }

    /**
     * @return the FeatureStore<SimpleFeatureType, SimpleFeature> of target layer
     */
    private FeatureStore<SimpleFeatureType, SimpleFeature> getTargetStore() {
        assert this.targetStore != null;
        return this.targetStore;
    }

    /**
     * Runs the clip process
     * @throws SOProcessException 
     */
    @Override
    public final void run( IProgressMonitor monitor ) throws SOProcessException {


        // initalization
        init(monitor); 

        final String msg = MessageFormat.format(Messages.ClipProcess_clipping_with, 
                                                layerToClip.getName(),
                                                clippingLayer.getName());
        getMonitor().subTask(msg);
        getMonitor().worked(1);
        int count = computeCount();
        getMonitor().beginTask(msg, count);

        // gets the crs of layers and map
        final CoordinateReferenceSystem clippingCrs = LayerUtil.getCrs(clippingLayer);
        final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(clippingLayer.getMap());
        final CoordinateReferenceSystem featureToClipCrs = LayerUtil.getCrs(layerToClip);

        FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToClip = this.featuresToClip;
        // save the name of geometry attribute

        FeatureCollection<SimpleFeatureType, SimpleFeature> clipping = this.clippingFeatures;
        FeatureIterator<SimpleFeature> iter = null;
        try {
            iter = clipping.features();
            while( iter.hasNext() ) {

                checkCancelation();

                SimpleFeature clippingFeature = iter.next();

                clipFeatureCollectionUsingClippingFeature(
                        featuresToClip,  featureToClipCrs,
                        clippingFeature, clippingCrs, mapCrs);

                getMonitor().worked(1);
            }

        } catch (InterruptedException e) {

            final String cancelMsg = Messages.ClipProcess_clip_was_canceled;
            throw new SOProcessException(cancelMsg);
            
        } finally {

            if (iter != null) {
                clipping.close(iter);
            }
            endProcess((Map) this.map, this.targetLayer);
            
            final String endMsg = Messages.ClipProcess_successful;
            monitor.subTask(endMsg);
            monitor.done();
        }
    }
    

    /**
     * Compute the count of features to process.
     * 
     * If overflow occur retruns MaxInteger.
     *
     * @return the count or Integer.MAX_VALUE by overflow
     */
    private int computeCount() {
        
        int count;
        try{
            count = this.clippingFeatures.size() * this.featuresToClip.size();
        }catch(ArithmeticException e ){
            
            count = Integer.MAX_VALUE;
        }
        return count;
    }

    /**
     * @return true if a new layer is required.
     */
    private final boolean isRequiredCreateNewLayer() {
        
        return (this.paramsClipInNewLayer != null) ;
    }
    
    /**
     * Initializes the clip process
     *
     * @param params
     */
   protected void init(final IClipParameters params){
   
       this.clippingLayer = params.getClippingLayer();
       assert this.clippingLayer != null;

       this.layerToClip = params.getLayerToClip();
       assert this.layerToClip != null;
       
       this.map = this.clippingLayer.getMap();
       assert this.map != null;
       
       this.clippingFeatures = params.getClippingFeatures();
       assert this.clippingFeatures != null;

       this.featuresToClip = params.getFeaturesToClip();
       assert this.featuresToClip  != null;
   }
    

    
    /**
     * Initialzie the process taking into account if the required target is an
     * existnet layer or a new layer
     * 
     * @param monitor
     */
    @Override
    protected void init( final IProgressMonitor monitor ) throws  SOProcessException{

         try {
            super.init(monitor);

            if (this.paramsClipInExistentLayer != null) {

                init(this.paramsClipInExistentLayer);

                this.targetLayer = this.paramsClipInExistentLayer.getTargetLayer();

                this.targetStore = getFeatureStore(targetLayer);
                
                setGeomAttrTypeToClip(this.targetLayer.getSchema());

            } else if (this.paramsClipInNewLayer != null) {

                init(this.paramsClipInNewLayer);

                // create new layer (store and resource) with the feature type required
                SimpleFeatureType type = this.paramsClipInNewLayer.getTargetFeatureType();

                this.targetGeoResource = AppGISMediator.createTempGeoResource(type);
                assert this.targetGeoResource != null;

                this.targetStore = this.targetGeoResource.resolve(FeatureStore.class, monitor);

                setGeomAttrTypeToClip(type);
                
                this.targetLayer = addLayerToMap(this.map, this.targetGeoResource);
            }
            assert this.targetLayer != null;
            assert this.targetStore != null;
            

        } catch (IOException e) {

            final String msg = MessageFormat
                                            .format(
                                                    Messages.ClipProcess_failed_creating_temporal_store,
                                                    e.getMessage());
            LOGGER.severe(msg);

            throw new SOProcessException(msg);
        }

    }
    
    /**
     * Extracts the attribute type of geometry and sets the instance variable 
     * for this process
     * 
     * @param featureType
     */
    private final void setGeomAttrTypeToClip( SimpleFeatureType featureType ) {

        GeometryDescriptor type = featureType.getDefaultGeometry();
        assert type != null;
        this.geomAttrType =  type;
    }
    
    private final GeometryDescriptor getGeomAttrTypeToClip(){
        assert this.geomAttrType != null;
        return this.geomAttrType;
    }

    /**
     * Clips the feature collection using the clippingFeature
     *
     * @param featureCollectionToClip feature collection to clip
     * @param featureToClipCrs crs
     * @param clippingFeature  feature used to clip the feature collection
     * @param clippingCrs crs
     * @param mapCrs crs
     * @throws SOProcessException 
     */
    private final void clipFeatureCollectionUsingClippingFeature( 
            final FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollectionToClip,
            final CoordinateReferenceSystem featureToClipCrs, 
            final SimpleFeature clippingFeature,
            final CoordinateReferenceSystem clippingCrs, 
            final CoordinateReferenceSystem mapCrs ) throws SOProcessException {

        FeatureIterator<SimpleFeature> iter = null;
        try {
            Geometry clippingFeatureGeometry = (Geometry) clippingFeature.getDefaultGeometry();

            // iterate for each clipping feature'geometry (general case is collection, particular case only one geometry)
            for(int i=0; i < clippingFeatureGeometry.getNumGeometries(); i++){

                Geometry clippingGeometry = clippingFeatureGeometry.getGeometryN(i);
                
                Geometry clippingGeometryOnMap = GeoToolsUtils.reproject(clippingGeometry, 
                                                                         clippingCrs,
                                                                         mapCrs);

                // iterates in the collection to clip and does the geometry clip using the clipping geometry                
                iter = featureCollectionToClip.features();
                while( iter.hasNext() ) {

                    checkCancelation();

                    SimpleFeature featureToClip = iter.next();

                    // The feature selected to clip could have been changed in previous iteration
                    // then is necessary check the "transaction trak"
                    String fid = featureToClip.getID();
                    if( this.transactionTrack.containsKey(fid) ){

                        // the feature was changed before and requires more changes (clipping)
                        clipChangedFeatureUsing( featureToClip, featureToClipCrs, clippingGeometryOnMap, mapCrs);

                    } else {
                        // first time that this feature will be precessed
                        clipFeatureUsing( featureToClip, featureToClipCrs, clippingGeometryOnMap, mapCrs);
                    }
                    
                    getMonitor().worked(1);
                }
            }

        } catch (Exception e) {

            final String emsg = e.getMessage();
            LOGGER.severe(emsg);
            throw new SOProcessException(emsg);

        } finally {
            if (iter != null) {
                featureCollectionToClip.close(iter);
            }

        }

    }

    /**
     * The feature to clip, was changed before and requires more precessing 
     * 
     * @param featureToClip 
     * @param featureToClipCrs
     * @param clippingGeometryOnMap
     * 
     * @param mapCrs
     * @throws SOProcessException 
     */
    private void clipChangedFeatureUsing(SimpleFeature                    featureToClip, 
                                         CoordinateReferenceSystem  featureToClipCrs, 
                                         Geometry                   clippingGeometryOnMap, 
                                         CoordinateReferenceSystem  mapCrs ) 
            throws SOProcessException {

        String fid = featureToClip.getID();
        FeatureTransaction tx = this.transactionTrack.get(fid);
        
        assert tx != null;
        
        switch( tx.getType() ) {
        case DELETE :
            // does not process
            break;
        case SPLIT :
            // precesses the fragments
            List<String> fidList = tx.getListInsertedFeatures();

            FeatureCollection<SimpleFeatureType, SimpleFeature> fragmentCollection = findFeatures(this.targetStore, fidList);
            
            FeatureIterator<SimpleFeature> fragmentIterator = null;
            try{
                fragmentIterator = fragmentCollection.features();
                
                while( fragmentIterator.hasNext() ) {

                    SimpleFeature fragment = fragmentIterator.next();

                    clipFeatureUsing( fragment, featureToClipCrs, clippingGeometryOnMap, mapCrs);
                    
                }
            } finally {
                if (fragmentIterator != null)
                    fragmentCollection.close(fragmentIterator);
            }
            
            break;
        case UPDATE :
            // the feature require more changes
            
            // retrieves the changed feature and applies it the clip 
            fid = tx.getUpdatedFeature();
            
            SimpleFeature updatedFeature = findFeature(this.targetStore, fid);
            
            clipFeatureUsing( updatedFeature, featureToClipCrs, clippingGeometryOnMap, mapCrs);
            
           break;

        default:
            assert false; // impossible case
        }
        
    
    }

    /**
     * Retrieves the features from sotre
     * 
     * @param store target feature sotore 
     * @param fidList 
     * 
     * @return the feature collection
     * @throws SOProcessException 
     */
    private FeatureCollection<SimpleFeatureType, SimpleFeature> findFeatures( FeatureStore<SimpleFeatureType, SimpleFeature> store, List<String> fidList ) throws SOProcessException {
        

        try {
            FidFilter filter = FILTER_FACTORY.createFidFilter();
            filter.addAllFids(fidList);
            
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = store.getFeatures(filter);
            return features;
        
        } catch (IOException e) {
            final String msg = e.getMessage();
            LOGGER.severe(msg);
            throw new SOProcessException(msg);
        }
    }
    /**
     * Retrieve the feature from store
     *
     * @param store
     * @param fid
     * @return
     * @throws SOProcessException
     */
    private SimpleFeature findFeature( FeatureStore<SimpleFeatureType, SimpleFeature> store, String fid) throws SOProcessException {
        
        try {
            FidFilter filter = FILTER_FACTORY.createFidFilter(fid);
            
            FeatureCollection<SimpleFeatureType, SimpleFeature> featuresCollection = store.getFeatures(filter);
            FeatureIterator<SimpleFeature> iter = featuresCollection.features();
            
            assert iter.hasNext();
            
            SimpleFeature feature =  iter.next();
            
            return feature;
        
        } catch (IOException e) {
            final String msg = e.getMessage();
            LOGGER.severe(msg);
            throw new SOProcessException(msg);
        }
    }

    /**
     * 
     *
     * @param featureCollection
     * @return feature list
     */
    private List<SimpleFeature> toList( final FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) {
        
        List<SimpleFeature> list = new LinkedList<SimpleFeature> ();
        
        FeatureIterator<SimpleFeature> iterator = featureCollection.features();
        while( iterator.hasNext() ) {
            SimpleFeature feature = iterator.next();
            
            list.add(feature);
            
        }
        featureCollection.close(iterator);
        
        return list;
    }

    /**
     * Clips the feature using the clipping geometry.
     *
     * @param featureToClip
     * @param featureToClipCrs
     * @param clippingGeometryOnMap
     * @param mapCrs
     * 
     * @return the list of features resultant of process. 
     * @throws SOProcessException 
     */
    private void clipFeatureUsing(final SimpleFeature                   featureToClip,
                                  final CoordinateReferenceSystem featureToClipCrs,
                                  final Geometry                  clippingGeometryOnMap,
                                  final CoordinateReferenceSystem mapCrs) 
            throws SOProcessException {

        try {
            Geometry featureGeometryToClip = (Geometry) featureToClip.getDefaultGeometry();

            // iterate in the feature's geometries to clip and applies the delete, split or difference operation
            // using the clipping geometry
            int resultGeomSize = featureGeometryToClip.getNumGeometries();
            List<Geometry> resultClipGeom = new ArrayList<Geometry>(resultGeomSize);
            
            for( int i = 0; i < featureGeometryToClip.getNumGeometries(); i++ ) {
                
                Geometry simpleGeomToClip = featureGeometryToClip.getGeometryN(i);

                Geometry featureGeometryOnMapCrs = GeoToolsUtils.reproject(simpleGeomToClip,
                                                                  featureToClipCrs, 
                                                                  mapCrs);
                // Analyses the geometry's positon
                if (clippingGeometryOnMap.contains(featureGeometryOnMapCrs)) {
                    
                    continue;   // it will be deleted from target (if "to clip layer" is equal to target layer) 
                                // or does not add this geometry to result

                } else if (splits(clippingGeometryOnMap, featureGeometryOnMapCrs)) {

                    Geometry splitGeom = computeGeometrySplit(featureGeometryOnMapCrs,
                                                              clippingGeometryOnMap, mapCrs);
                    
                    resultClipGeom.add(splitGeom);

                } else if (clippingGeometryOnMap.intersects(featureGeometryOnMapCrs)) {

                    Geometry diffGeom = computeGeometryDifference(featureGeometryOnMapCrs,
                                                                  clippingGeometryOnMap, mapCrs);
                    
                    resultClipGeom.add(diffGeom);
                }
            }
            //postcondition: {resultClipGeom is a Collection with Geometries modified with difference, deleted and splited geom (new geom) }
            GeometryFactory factory = featureGeometryToClip.getFactory();
            
            GeometryCollection resultCollection =  factory.createGeometryCollection(resultClipGeom.toArray(new Geometry[]{}));

            updateTargetWith(featureToClip, resultCollection);
            
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            throw new SOProcessException(Messages.ClipProcess_failed_executing_reproject);
        }

    }


    /**
     * Analyses the geometry result to apply clip method to a feature and update the target store.
     * <p>
     * Theses are the different situations to analyse:
     * 
     * <ul>
     * <li>resultCollection is empty: the feature must be deleted.</li>
     * 
     * <li>resultCollection is a GeometryCollection (not empty) and the feature to clip is GeometryCollection 
     * compatible, the method adds the new geometry.</li>
     * 
     * <li>resultCollection is a GeometryCollection (not empty) and the feature to clip is "simple geometry", 
     * then if the "to Clip" layer and Target layer are equals requires to create a new feature and delete the source feature, 
     * esle only add the new features in the target.
     * </li>
     *  </ul>
     * </p>
     *
     * @param featureToClip feature that contains the source geometry (to clip geometry)
     * @param resultCollection resultClipGeom is a Collection with geometries modified with difference, deleted or splited geom (new geom)
     * @throws SOProcessException 
     */
    private void updateTargetWith(final SimpleFeature             featureToClip, 
                                  final GeometryCollection  resultCollection) 
            throws  SOProcessException{
        
        final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(this.map);
        final CoordinateReferenceSystem targetCrs = LayerUtil.getCrs(this.targetLayer);
        
        Geometry geomResultOnTarget;
        try {
            geomResultOnTarget = GeoToolsUtils.reproject(resultCollection, mapCrs, targetCrs);
        } catch (Exception e) {
            throw new SOProcessException(e.getMessage());
        }
        
        
        String fidToClip = featureToClip.getID();
        
        FeatureTransaction tx;
        
        if( geomResultOnTarget.isEmpty() ){
            // the feature must be deleted form target (or no apeare in the result)
            
            if(this.layerToClip.equals(this.targetLayer)){
                
                deleteFeature(featureToClip);
            } // else does not add it in target layer
            
            //registers the delete transaction in track 
            tx = FeatureTransaction.createDeleteTransaction(fidToClip);
            this.transactionTrack.put(fidToClip, tx);
            
        } else { // resultCollection is not empty

            Geometry geometryToClip = (Geometry) featureToClip.getDefaultGeometry();

            if(geometryToClip instanceof GeometryCollection){ // target must be GeometryCollection compatible too
                
                // modified geometries case
                if(this.layerToClip.equals(this.targetLayer)){
                    
                    // updates the feature
                    modifyFeatureInStore(featureToClip, geomResultOnTarget, this.targetStore);

                    tx = FeatureTransaction.createUpdateTransaction(fidToClip, fidToClip); 
                    this.transactionTrack.put(fidToClip, tx);
                
                } else { 
                    // creates a new feature in the target layer
                    String newFid = createFeatureInStore(geomResultOnTarget, this.targetStore);

                    tx = FeatureTransaction.createUpdateTransaction(fidToClip, newFid);
                    this.transactionTrack.put(fidToClip, tx);
                }
            
            } else { // geometryToClip is "simple geometry" 
                
                // creates new features for each new fragment. if toClip layer and target are equals 
                // inserts the fragments and delete the original feature
                // else only inserts the new fragments
                List<SimpleFeature> featureList = createFeatureFragments(featureToClip, (GeometryCollection)geomResultOnTarget, true);
                
                assert ! featureList.isEmpty();
                List<String> newFidList = new LinkedList<String>();
                for( SimpleFeature feature : featureList ) {

                    String newFid = insertFeatureInStore(feature, this.targetStore);
                    newFidList.add(newFid);
                    
                }
                if( this.layerToClip.equals(this.targetLayer) ){

                    deleteFeature(featureToClip);
                } 
                tx = FeatureTransaction.createSplitTransaction(fidToClip, newFidList);
                this.transactionTrack.put(fidToClip, tx);
            }
           
            
        }
    }
    /**
     * Updates the target store with the clipped geometry
     *
     * @param targetStore
     * @param featureToClip
     * @param clippedGeometry
     * 
     * @throws SOProcessException 
     */
//    private void updateTarget(final FeatureStore<SimpleFeatureType, SimpleFeature> targetStore, 
//                              final SimpleFeature featureToClip, 
//                              final Geometry clippedGeometry ) 
//        throws SOProcessException {
//
//        // if the feature have some geometry updates the store, else the featue
//        // have not geometry as result of this process then deletes the feature.
//        try {
//            if (clippedGeometry.isEmpty()) {
//
//                deleteFeature(featureToClip);
//
//            } else {
//                // creates the new geometry using the result geometry of clip process
//                // and sets it in the feature
//
//                Geometry featureGeometry = featureToClip.getDefaultGeometry();
//                GeometryFactory geomFac = featureGeometry.getFactory();
//                Geometry newClippedGeomery = geomFac.createGeometryCollection(
//                                                                clippedGeometry.toArray(new Geometry[]{}));
//                // applies the change in the store
//                if (requireCreateFeatureForFragment(this.targetStore)) {
//
//                    final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(clippingLayer.getMap());
//                    SimpleFeatureType featureType = featureToClip.getFeatureType();
//                    final CoordinateReferenceSystem targetLayerCrs = featureType
//                                                                                .getDefaultGeometry()
//                                                                                .getCoordinateSystem();
//                    updateStoreWithMultiGeom(this.targetStore, targetLayerCrs, featureToClip,
//                                                 mapCrs, (GeometryCollection) newClippedGeomery);
//
//                } else {
//                    featureToClip.setDefaultGeometry(newClippedGeomery);
//                    if (this.targetLayer.equals(this.layerToClip)) {
//                        // modifies the feature's geometry in the store
//                        modifyFeatureInStore(featureToClip, newClippedGeomery, this.targetStore);
//
//                    } else { // create a feature in the new layer with the clipped geometry
//
//                        createFeatureInStore(newClippedGeomery, this.targetStore);
//                    }
//                }
//
//            }
//        } catch (Exception e) {
//
//            LOGGER.severe(e.getMessage());
//            throw new SOProcessException(Messages.ClipProcess_failed);
//        }
//    
//    }

    

    /**
     * Updates the feature in store using the Geometry Collection
     *
     * @param store
     * @param targetLayerCrs
     * @param feature
     * @param mapCrs
     * @param resultGeometry
     * 
     * @throws OperationNotFoundException
     * @throws TransformException
     * @throws SOProcessException
     */
    private void updateStoreWithMultiGeom( final FeatureStore<SimpleFeatureType, SimpleFeature> store,
                                           final CoordinateReferenceSystem targetLayerCrs,
                                           final SimpleFeature feature,
                                           final CoordinateReferenceSystem mapCrs, 
                                           final GeometryCollection resultGeometry ) 
            throws  SOProcessException {

        Geometry adjustedGeom;
        if( requiresAdjust(feature, store) ){
            adjustedGeom = adjustGeometryAttribute(resultGeometry, feature);
        } else{
            adjustedGeom = resultGeometry;
        }
        Geometry finalGeometry;
        try {
        
            finalGeometry = GeoToolsUtils.reproject(adjustedGeom, mapCrs, targetLayerCrs);
        
        } catch (Exception e) {
            throw new SOProcessException(e.getMessage());
        }

        // create the clip geometry and set the feature 
        if (this.layerToClip.equals(this.targetLayer)) {

            // modifies the feature's geometry in the store
            modifyFeatureInStore(feature, finalGeometry, store);

        } else {

            createFeatureInStore(finalGeometry, store);
        }
    }
    
    
    /**
     * Analyses the target store, then if it was defined as simple geometry then require 
     * a feature for each geometry fragment to store the clip result.
     * 
     * @return true if the strore requires to create fragments, false in other case
     */
    private final boolean requireCreateFeatureForFragment(FeatureStore<SimpleFeatureType, SimpleFeature> store) {
        
        final SimpleFeatureType featureType = store.getSchema();
        
        GeometryDescriptor geomAttr = featureType.getDefaultGeometry();
        assert geomAttr != null;
        
        Class targetGeom = geomAttr.getType().getBinding();
        
        return 
            Point.class.equals(targetGeom) || 
            LineString.class.equals(targetGeom) || 
            Polygon.class.equals(targetGeom);
        
    }

    /**
     * Evaluates if clippingGeometry divides or not the feature's geometry. The params featureGeometry cannot be
     * instance of Geometry Collection (Muli ....)
     * 
     * @param clippingGeometry
     * @param featureGeometry
     * @return true if clippingGeometry divides the feature's geometry, false in other case.
     */
    private final boolean splits( final Geometry clippingGeometry, final Geometry featureGeometry ) {

        assert ! (featureGeometry instanceof GeometryCollection);
        
        if(featureGeometry instanceof Point){
            return false; // cannot split a point
        }
        
        // If the result of difference is a multygeometry then the geometry is splitted.
        Geometry geoDiff = featureGeometry.difference(clippingGeometry);

        boolean isGeometryCollection = geoDiff instanceof GeometryCollection;
        boolean intersects = featureGeometry.intersects(clippingGeometry);

        return isGeometryCollection && intersects;
    }

    /**
     * Makes the geometry difference between the feature's geometry and the clipping geometry. If the
     * features type is geometry or multigeometry (geometry collection ) then modifies the feature's geometry. 
     * If the feature type has a simple geometry (point, polygon, line) then makes new geometries for each.
     *
     * @param featureCrs
     * @param featureGeometryOnMapCrs
     * @param clippingGeometryOnMapCrs
     * @param mapCrs
     * 
     * @return splited geometry. 
     * 
     * @throws SOProcessException 
     */
    private GeometryCollection computeGeometrySplit( 
            final Geometry featureGeometryOnMapCrs,
            final Geometry clippingGeometryOnMapCrs,
            final CoordinateReferenceSystem mapCrs ) 
        throws SOProcessException {

        try {
            // does the difference 
            Geometry geoDiff = featureGeometryOnMapCrs.difference(clippingGeometryOnMapCrs);
            assert geoDiff instanceof GeometryCollection;
            
            GeometryCollection geoCollection = (GeometryCollection) geoDiff;

            return geoCollection;
            
        } catch (Exception e) {
            final String msg = e.getMessage();
            LOGGER.severe(msg);
            throw new SOProcessException(msg);
        }
    }

    /**
     * Creates new features for each geometry fragmens.
     * 
     * @param featurePrototype prototype used to create the new features
     * @param geomFragmentCollection geometry fragment (split result)
     * @param requiresCopyData true to copy the prototype data to the new features
     * 
     * @throws SOProcessException 
     * 
     * @return list of new SimpleFeature
     */
    private final List<SimpleFeature> createFeatureFragments( final SimpleFeature               featurePrototype,
                                                        final GeometryCollection    geomFragmentCollection,
                                                        final boolean               requiresCopyData) 
            throws SOProcessException {

        List<SimpleFeature> featureList = new LinkedList<SimpleFeature>();

        // creates a new feature for each geometry fragment
        GeometryCollection geomList = geomFragmentCollection;
        for( int i = 0; i < geomList.getNumGeometries(); i++ ) {

            Geometry geomFragment = geomList.getGeometryN(i);

            SimpleFeature newFeature = createFeature(featurePrototype, geomFragment, requiresCopyData);

            featureList.add(newFeature);
        }

        return featureList;

    }

    /**
     * @param featurePrototype
     * @param newGeometry
     * @param requiresCopyData
     * @return
     * @throws SOProcessException
     */
    private final SimpleFeature createFeature( final SimpleFeature  featurePrototype,
                                         final Geometry newGeometry,
                                         final boolean  requiresCopyData) 
            throws SOProcessException {
        
        try {

            // create the new feature and set the geometry fragment
            SimpleFeature newFeature = DataUtilities.template(featurePrototype.getFeatureType());

            // copies the data in the new feature
            if (requiresCopyData) {
                GeoToolsUtils.match(featurePrototype, newFeature);
            }
            newFeature.setDefaultGeometry(newGeometry);
            
            return newFeature;

                
        } catch (Exception e) {
            final String msg = e.getMessage();
            LOGGER.severe(msg);
            throw new SOProcessException(msg);
        }
    }
    

    /**
     * Test if the feature geometry requires adjust to the target store geometry. The feature's
     * geometry only requires adjust if it es a simple geometry (point, linestring, ploygon) and the
     * target store is a GeometryCollection (Mulipoint, MultiLinestring, MultiPolygon).
     * 
     * @param feature
     * @param targetStore
     * @return true if the feature requires adjust its geometry
     */
    private boolean requiresAdjust( final SimpleFeature feature, final FeatureStore<SimpleFeatureType, SimpleFeature> targetStore ) {
        
        Geometry geom = (Geometry) feature.getDefaultGeometry();

        boolean featureGeomIsSimple =   (geom instanceof Point)
                                    ||  (geom instanceof LineString)
                                    ||  (geom instanceof Polygon);
        
        GeometryDescriptor geomAttType = targetStore.getSchema().getDefaultGeometry();
        Class targetGeom = geomAttType.getClass();
        boolean targetStoreIsMulty =   MultiPoint.class.equals(targetGeom)
                                    || MultiLineString.class.equals(targetGeom)
                                    || MultiPolygon.class.equals(targetGeom);
        
        return featureGeomIsSimple && targetStoreIsMulty;
    }

    /**
     * Deletes the feature if the target layer is equal to the layer to clip
     * 
     * @param feature
     */
    private void deleteFeature(final SimpleFeature feature ) {


        try {
            //Deletes only if the target is equal to the layer to clip
            if(this.layerToClip.equals(this.targetLayer)){
                FidFilter filter = FILTER_FACTORY.createFidFilter(feature.getID());
                FeatureStore<SimpleFeatureType, SimpleFeature> store = getTargetStore();
                store.removeFeatures(filter);
            }

        } catch (IOException e) {
            final String msg = Messages.ClipProcess_failed_deleting;
            LOGGER.severe(msg);
            throw (RuntimeException) new RuntimeException(msg).initCause( e );
        }

    }

    /**
     * Modifies the geometry of feature doing the difference with the clipping geometry.
     * precondition: this method supposes that clipping does not contain featrue's geometry but
     * intersects it.
     * 
     * <p>
     * Note: Geometry could contain clipping then feature with hole is required. 
     * This process does not produce hole geometry.
     * In this case this method returns th original geometry without modifications.
     * </p>
     * 
     * @param targetLayerCrs
     * @param featureGeometryOnMap
     * @param clippingGeometryOnMap
     * @param mapCrs
     * @return Modify Geometry or the original if it require hole
     * @throws SOProcessException 
     */
    private final Geometry computeGeometryDifference( 
            Geometry                    featureGeometryOnMap, 
            Geometry                    clippingGeometryOnMap,
            CoordinateReferenceSystem   mapCrs ) throws SOProcessException {


        // assert: clipping does not contains featrue's geometry but intersect
        FeatureStore<SimpleFeatureType, SimpleFeature> store = getTargetStore();
        
        final SimpleFeatureType featureType = store.getSchema();
        final CoordinateReferenceSystem targetLayerCrs = featureType.getDefaultGeometry()
                                                                            .getCRS();
        Geometry resultGeometry = null;
        try {
            
            if (!featureGeometryOnMap.contains(clippingGeometryOnMap)) {

                // clipping does not contain the feature and features does not contain clipping area
                // then constructs the following difference: feature'geometry - clipping area

                Geometry diffGeometry = featureGeometryOnMap.difference(clippingGeometryOnMap);

                Geometry adjustedGeom = GeoToolsUtils.reproject(diffGeometry, mapCrs, targetLayerCrs);
                
                resultGeometry = adjustedGeom;
            } else {
                // Note: if the condition is false, feature's geometry could contain clipping
                // then feature with hole is required. This process does not produce hole geometry.
                resultGeometry = featureGeometryOnMap;
            }
            return resultGeometry;

        } catch (OperationNotFoundException e) {

            final String msg = MessageFormat.format(Messages.ClipProcess_failed_executing_reproject,
                                                    mapCrs.getName(), targetLayerCrs.getName());
            
            LOGGER.severe(msg);
            throw new SOProcessException(msg);

        } catch (TransformException e) {

            final String msg = MessageFormat.format(Messages.ClipProcess_failed_transforming,
                                                    mapCrs.getName(), targetLayerCrs.getName());
            LOGGER.severe(msg);
            throw new SOProcessException(msg);
        }

    }
    
    /**
     * Creates a new feature in the strore using the geometry
     *
     * @param finalGeometry
     * @param store
     * @return the feature id 
     * @throws SOProcessException 
     */
    private String createFeatureInStore( final Geometry finalGeometry, final FeatureStore<SimpleFeatureType, SimpleFeature> store ) throws SOProcessException{
        
        SimpleFeature newFeature;
        try {
            newFeature = DataUtilities.template(store.getSchema());

            newFeature.setDefaultGeometry(finalGeometry);
        
            Set fidSet =  store.addFeatures(DataUtilities.collection(new SimpleFeature[]{newFeature}));
            
            Iterator iter = fidSet.iterator();
            assert iter.hasNext();
            String fid = (String)iter.next();
            
            return fid;

        } catch (Exception e) {
            
            final String msg = Messages.ClipProcess_failed_creating_new_feature ;
            LOGGER.severe(msg);
            throw new SOProcessException(msg);
            
        }
    }
    private String insertFeatureInStore( final SimpleFeature feature, final FeatureStore<SimpleFeatureType, SimpleFeature> store ) throws SOProcessException{
        
        try {
        
            Set fidSet =  store.addFeatures(DataUtilities.collection(new SimpleFeature[]{feature}));
            
            Iterator iter = fidSet.iterator();
            assert iter.hasNext();
            String fid = (String)iter.next();
            
            return fid;

        } catch (Exception e) {
            
            final String msg = Messages.ClipProcess_failed_creating_new_feature ;
            LOGGER.severe(msg);
            throw new SOProcessException(msg);
            
        }
    }

    /**
     * Sets the new geometry in the feature and register the modification in the store
     *
     * @param featureToModify
     * @param finalGeometry
     * @param store
     * @throws SOProcessException
     */
    private final void modifyFeatureInStore(SimpleFeature featureToModify, Geometry finalGeometry, FeatureStore<SimpleFeatureType, SimpleFeature> store ) throws SOProcessException{
        // modifies the feature's geometry in the store
        
        FidFilter filter = FILTER_FACTORY.createFidFilter(featureToModify.getID());
        
        GeometryDescriptor geomAttr =  getGeomAttrTypeToClip();
        try {
            store.modifyFeatures(geomAttr, finalGeometry, filter);
        } catch (IOException e) {
            final String msg = e.getMessage();
            LOGGER.severe(msg);
            throw new SOProcessException(msg);
        }
        
    }

}
