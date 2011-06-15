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
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.GeometryUtil;

public abstract class AbstractProcess implements ISOProcess {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractProcess.class.getName());
    
    private IProgressMonitor   monitor   = null;


   /**
    * initialize the process
    * @throws SOProcessException 
    */
   protected void init(IProgressMonitor monitor) throws SOProcessException {
       if(monitor == null) throw new SOProcessException("the monitor value can not be null"); //$NON-NLS-1$
       
       this.monitor = monitor;
   }

   /**
     * @return Returns the monitor.
     */
    protected final IProgressMonitor getMonitor() {
        assert this.monitor != null;
        return this.monitor;
    }

    /**
     * @param monitor The monitor to set.
     */
    protected final void setMonitor( IProgressMonitor monitor ) {
        this.monitor = monitor;
    }
    
 


    /**
     * log the exception and throw to caller
     * @param exception
     * @throws SOProcessException 
     */
    protected void throwException( SOProcessException exception ) throws SOProcessException {
    
        LOGGER.severe(exception.getMessage());
        
        throw exception;
    }
    

    /**
     * Adds a new layer to map using the georesource of the feature sotre
     * 
     * @param map 
     * @param geoResource 
     * 
     * @return the new layer
     */
    protected ILayer addLayerToMap(IMap map, IGeoResource geoResource) {
        
        int index = map.getMapLayers().size();
        List< ? extends ILayer> listLayer = AppGISMediator.addLayersToMap(map, Collections.singletonList(geoResource), index);

        assert listLayer.size() == 1; // creates only one layer
        
        ILayer layer = listLayer.get(0);
        
        return layer;
    }

    /**
     * Gets the store required to save the new features.
     *
     * @param layer
     * @return a feature store
     * @throws SOProcessException if can get the store
     */
    public static FeatureStore<SimpleFeatureType, SimpleFeature> getFeatureStore( final ILayer layer ) throws SOProcessException {
        
        SimpleFeatureType featureType = layer.getSchema();

        IGeoResource resource = layer.getGeoResource();
        FeatureStore<SimpleFeatureType, SimpleFeature> store = getTargetFeatureStore(resource, featureType);
        
        return store;
    }
    
    
    /**
     * Returns the target FeatureStore<SimpleFeatureType, SimpleFeature> for the operation.
     * <p>
     * If it comes from an existing layer, it is returned. If it has to be created because the user
     * have selected the "create new layer" option, a new temporary IGeoResource is created
     * </p>
     * @param resource  
     * @param featureType
     * @return the FeatureStore<SimpleFeatureType, SimpleFeature> for the generated features, either if it comes from an existing
     *         layer or a new one had to be created
     * @throws SOProcessException 
     */
    private static FeatureStore<SimpleFeatureType, SimpleFeature> getTargetFeatureStore(IGeoResource resource, SimpleFeatureType featureType) throws SOProcessException {
        
        try {
            if (resource == null) {
                // new  resourece is required because new layer was selected
                final ICatalog catalog = AppGISMediator.getCatalog();
                assert catalog != null;
                
                resource = catalog.createTemporaryResource(featureType);
            }
            final FeatureStore<SimpleFeatureType, SimpleFeature> targetStore;

            targetStore = resource.resolve(FeatureStore.class, new NullProgressMonitor());
            
            return targetStore;

        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            final String msg = Messages.AbstractProcess_failed_getting_feature_store;
            throw new SOProcessException(msg);
        }
        
    }

    /**
     * Returns the muli geometry required by the feature
     *
     * @param geometry must be simple geometry (point, polygon, linestring)
     * @param feature
     * @return adjusted geometry
     */
    public static Geometry adjustGeometryAttribute( final Geometry geometry, final SimpleFeature feature ) {
        
        GeometryDescriptor geoAttr = feature.getFeatureType().getDefaultGeometry();
        Class geomClass = geoAttr.getType().getBinding();
        Geometry adjustedGeom = GeometryUtil.adapt(geometry, geomClass);
        
        return adjustedGeom;
    }
    
    /**
     * If the user cancel this process throw the InterruptedException
     *
     * @throws InterruptedException
     */
    protected void checkCancelation() throws InterruptedException{
        
        if (this.monitor.isCanceled()) {
            throw new InterruptedException();
        }
        
    }

    public abstract void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException ;
    
    /**
     * Presents the result of porcess
     */
    protected void endProcess(final Map map, final ILayer targetLayer) {
        
        assert map != null;
        assert targetLayer != null;
        
        // FIXME Hack to solve the target layer refresh
        // Note: we need confirm the transaction before refresh, the following sentences are a
        // possible solution
        try {
            map.getEditManagerInternal().commitTransaction();

          
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
        targetLayer.refresh(null); // I think this don't work for this strategy
        
    }
    


}