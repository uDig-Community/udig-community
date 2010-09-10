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
package es.axios.udig.spatialoperations.internal.processmanager;

import java.io.IOException;
import java.text.MessageFormat;
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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectParameters;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Makes an intersection on the target layer
 * <p>
 * This process creates new features that result from intersection of other 
 * two layer.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * 
 * @since 1.1.0
 */
final class IntersectProcess extends AbstractProcess {

    private static final Logger                 LOGGER                        = Logger
                                                                                      .getLogger(IntersectProcess.class
                                                                                                                       .getName());
    private IIntersectInExistentLayerParameters paramsIntersectInExistenLayer = null;
    private IIntersectInNewLayerParameters      paramsIntersectInNewLayer     = null;

    private ILayer                              firstLayer                    = null;
    private IMap                                map                           = null;
    private ILayer                              secondLayer                   = null;
    private ILayer                              targetLayer                   = null;
    private FeatureStore<SimpleFeatureType, SimpleFeature>                        targetStore                   = null;
    private IGeoResource                        targetGeoResource             = null;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer;

 
    /**
     * New instance of Intersect process
     * 
     * @param params parameters required to leave the intersect into an existent layer
     */
    public IntersectProcess( final IIntersectParameters params ) {

        if (params instanceof IIntersectInExistentLayerParameters ) {
            this.paramsIntersectInExistenLayer  = (IIntersectInExistentLayerParameters) params;
        } else if (params instanceof IIntersectInNewLayerParameters) {
            this.paramsIntersectInNewLayer  = (IIntersectInNewLayerParameters) params;
        } else{
            assert false; // illegal parameter 
        }
    }

    /**
     * Initializes the intersect process
     *
     * @param params
     */
   protected void init(final IIntersectParameters params){
   
       this.firstLayer = params.getFirstLayer();
       assert this.firstLayer != null;

       this.secondLayer = params.getSecondLayer();
       assert this.secondLayer != null;
       
       this.map = this.firstLayer.getMap();
       assert this.map != null;
       
       this.featuresInFirstLayer = params.getFeaturesInFirstLayer();
       assert this.featuresInFirstLayer != null;

       this.featuresInSecondLayer = params.getFeaturesInSecondLayer();
       assert this.featuresInSecondLayer  != null;
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

            if (this.paramsIntersectInExistenLayer != null) {

                init(this.paramsIntersectInExistenLayer);

                this.targetLayer = this.paramsIntersectInExistenLayer.getTargetLayer();

                this.targetStore = getFeatureStore(targetLayer);

            } else if (this.paramsIntersectInNewLayer != null) {

                init(this.paramsIntersectInNewLayer);

                // create new layer (store and resource) with the feature type required
                SimpleFeatureType type = this.paramsIntersectInNewLayer.getTargetFeatureType();

                this.targetGeoResource = AppGISMediator.createTempGeoResource(type);
                assert this.targetGeoResource != null;

                this.targetStore = this.targetGeoResource.resolve(FeatureStore.class, monitor);

                this.targetLayer = addLayerToMap(this.map, this.targetGeoResource);

            }
            assert this.targetLayer != null;
            assert this.targetStore != null;

         } catch (IOException e) {

            final String msg = MessageFormat
                                            .format(
                                                    Messages.IntersectProcess_failed_creating_temporal_store,
                                                    e.getMessage());
            LOGGER.severe(msg);

            throw new SOProcessException(msg);
        }

    }
    

    /**
     * Intersect strategy
     * <p>
     * Traverses the first layer doing the intersection of each feature in first layer with each
     * feature in second layer.
     * </p>
     * 
     * @throws SOProcessException
     */
    @Override
    public final void run( IProgressMonitor monitor ) throws SOProcessException {
        
        FeatureIterator<SimpleFeature> iter = null;
        try {
            init(monitor);

            // Traverses the first layer doing the intersection of each feature in first layer 
            // with each feature in second layer.
    
            // gets the crs of map
            final CoordinateReferenceSystem firstLayerCrs = LayerUtil.getCrs(this.firstLayer);
            final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(this.map);

            final int ticks =computeCount(this.featuresInFirstLayer, this.featuresInSecondLayer);         
            final String msg = MessageFormat.format(
                                    Messages.IntersectProcess_intersectin_with,
                                    this.firstLayer.getName(), this.secondLayer.getName());
            monitor.beginTask(msg, ticks);


            iter = featuresInFirstLayer.features();
            while( iter.hasNext() ) {

                checkCancelation();

                SimpleFeature featureInFirstLayer = iter.next();

                Geometry featureGeometry = (Geometry) featureInFirstLayer.getDefaultGeometry();
                
                createIntersectionFeaturesUsingGeomety(
                                      this.featuresInSecondLayer, 
                                      featureGeometry,
                                      firstLayerCrs, 
                                      this.targetStore, 
                                      mapCrs);
                monitor.worked(1);
            }
            
        } catch (Exception e) {
        
            final String exMessage = (e.getMessage()!= null)? e.getMessage():""; //$NON-NLS-1$
            final String emsg = MessageFormat.format(Messages.IntersectProcess_intersection_fail, exMessage);
            monitor.subTask(emsg);
            
            throwException( new SOProcessException(emsg) );

        } finally {

            if (iter != null) {
                featuresInFirstLayer.close(iter);
            }
            endProcess((Map) map, this.targetLayer);

            final String  success= Messages.IntersectProcess_successful;
            monitor.subTask(success);
            monitor.done();
        }
    }
    
    private int computeCount(FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirst, FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecond) {
        int count;
        try{
            count = featuresInFirst.size() * featuresInSecond.size();
        }catch(ArithmeticException e ){
            
            count = Integer.MAX_VALUE;
        }
        return count;
    }
    

    /**
     * Creates new features using the intersection between the baseGeometry 
     * and the geometries of featuresInSecondLayer.
     *
     * @param featuresInSecondLayer features used to intersect
     * @param baseGeometry          geometry used to interscect the features on second layer
     * @param targetFeatureType     feature type used to create the new intersection features
     * 
     * @return list of new intersection features
     * @throws SOProcessException 
     */
    private final void createIntersectionFeaturesUsingGeomety( 
            final FeatureCollection<SimpleFeatureType, SimpleFeature>         featuresInSecondLayer, 
            final Geometry                  baseGeometry, final CoordinateReferenceSystem baseGeomCrs,
            final FeatureStore<SimpleFeatureType, SimpleFeature>              store,
            final CoordinateReferenceSystem mapCrs) throws SOProcessException {

        assert featuresInSecondLayer != null;
        assert baseGeometry != null;
        assert store != null;
        
        // gets the crs of each layer and map to make the intersect geometries
        final CoordinateReferenceSystem secondLayerCrs = LayerUtil.getCrs(this.secondLayer);
        
        final SimpleFeatureType featureType = this.targetStore.getSchema();
        final CoordinateReferenceSystem targetLayerCrs = featureType.getDefaultGeometry()
                                                                        .getCRS();

        IProgressMonitor monitor = getMonitor();
        
        FeatureIterator<SimpleFeature> iter = null;
        try {

            // project the base geometry to map
            Geometry baseGeomOnMapCrs = GeoToolsUtils.reproject(baseGeometry, baseGeomCrs, mapCrs);
            
            // First does the graphic operations and creates a command 
            // for each feature intersected finally executes all command.
            iter = featuresInSecondLayer.features();
            while( iter.hasNext() ) {

                checkCancelation();

                SimpleFeature featureInSecondLayer = iter.next();

                Geometry featureGeometry = (Geometry) featureInSecondLayer.getDefaultGeometry();
                Geometry featureGeomOnMapCrs = GeoToolsUtils.reproject(featureGeometry, secondLayerCrs, mapCrs);

                if (baseGeomOnMapCrs.intersects(featureGeomOnMapCrs)) {

                    // makes the intersection on map crs 
                    Geometry intersectionOnMapCrs = baseGeomOnMapCrs.intersection(featureGeomOnMapCrs);
                    
                    Geometry intersectionGeometry = GeoToolsUtils.reproject(intersectionOnMapCrs, mapCrs, targetLayerCrs);
                    
                    // adds the feature in the store associate to layer                    
                    SimpleFeature newFeature = DataUtilities.template(featureType);

                    Geometry finalIntersection = adjustGeometryAttribute(intersectionGeometry, newFeature);
                    newFeature.setDefaultGeometry(finalIntersection);
                    
                    store.addFeatures(DataUtilities.collection(new SimpleFeature[]{newFeature}));
                }
                monitor.worked(1);
            }

        } catch (Exception e) {
            
            final String emsg = MessageFormat.format(
                                    Messages.IntersectProcess_intersection_fail, e.getMessage());
            getMonitor().subTask(emsg);
            
            throwException( new SOProcessException(emsg) );
            
        } finally {
            if (iter != null) {
                featuresInSecondLayer.close(iter);
            }
        }
    }


 }
