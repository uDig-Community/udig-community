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
package es.axios.udig.spatialoperations.internal.processmanager;

import java.io.IOException;
import java.text.MessageFormat;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.core.jts.JTSProgressMonitor;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.render.IViewportModel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.buffer.BufferOp;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IBufferInExistentLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IBufferInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.parameters.IBufferParameters;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.GeometryUtil;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Implements the buffer operation workflow.
 * <p>
 * A BufferRunnableProcess takes a {@link BufferInNewLayerParameters} object with the operation inputs uses
 * uses it to perform the operation as follows:
 * <ul>
 * <li> Obtain the FeatureCollection<SimpleFeatureType, SimpleFeature> with the selected Features in the source Layer
 * <li> Obtain the FeatureStore<SimpleFeatureType, SimpleFeature> where the newly created Features holding the buffered Geometries
 * will be stored. If the target layer is an already existing one, it will be used, else a new
 * temporary FeatureStore<SimpleFeatureType, SimpleFeature> will be created through {@link ICatalog#createTemporaryResource(Object)}
 * <li> For each selected SimpleFeature create a new one that conforms to the target SimpleFeatureType, and
 * holds the buffered geometry and the matching attributes from the source SimpleFeatureType </li>
 * <li>If the target Layer is a new one, add it to the Map
 * </p>
 * <p>
 * Note the buffer computation is made with a slightly {@link BufferOp modified version} of the JTS
 * <code>BufferOp</code> in order to allow the operation to be cancelled while inside the buffer
 * computation.
 * </p>
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
final class BufferProcess extends AbstractProcess {
    
    

    /**
     * Monitor tasks names
     */
    private static final String[] mainSteps = {
            Messages.BufferProcess_subtaskGetTargetLayer,
            Messages.BufferProcess_subtaskBufferringFeatures,
            Messages.BufferProcess_subtaskAddLayerToMap};

    private IBufferInNewLayerParameters      paramsBufferInNewLayer     = null;

    private IBufferInExistentLayerParameters paramsBufferInExistntLayer = null;

    private ILayer                           targetLayer                = null;

    private FeatureStore<SimpleFeatureType, SimpleFeature>                     targetStore                = null;

    private IGeoResource                     targetGeoResource          = null;

    private ILayer                           sourceLayer                = null;

    private FeatureCollection<SimpleFeatureType, SimpleFeature>                selectedFeatures           = null;

    private IMap                             map                        = null;

    


    /**
     * @param params
     */
    public BufferProcess( final IBufferParameters  params ) {
        
        if(params instanceof IBufferInNewLayerParameters){
            this.paramsBufferInNewLayer = (IBufferInNewLayerParameters) params;
        }else if (params instanceof IBufferInExistentLayerParameters) {
            this.paramsBufferInExistntLayer = (IBufferInExistentLayerParameters) params;
            
        }else{
            assert false; // illegal parameter
        }

    }
    

    /**
     * Initializes the buffer process with the common parameters
     *
     * @param params
     */
   protected void init(IBufferParameters params){
   
       this.sourceLayer = params.getSourceLayer();
       assert this.sourceLayer != null;
       
       this.selectedFeatures  = params.getSelectedFeatures();
       assert this.selectedFeatures != null;
       
       this.map = this.sourceLayer.getMap();
       assert this.map != null;
   }
   
   /**
    * Initialzie the buffer process taking into account if the target is an
    * existnet layer or a new layer
    * 
    * @param monitor
    */
   @Override
   protected void init( IProgressMonitor monitor ) throws  SOProcessException{

        super.init(monitor);


        if (this.paramsBufferInExistntLayer != null) {

            init(this.paramsBufferInExistntLayer);

            this.targetLayer = this.paramsBufferInExistntLayer.getTargetLayer();
            
            this.targetStore = getFeatureStore(this.targetLayer);

        } else if(this.paramsBufferInNewLayer != null) {
            
            init(this.paramsBufferInNewLayer);

            // create new layer (store and resource) with the feature type required
            SimpleFeatureType type = this.paramsBufferInNewLayer.getTargetFeatureType();

            this.targetGeoResource = AppGISMediator.createTempGeoResource(type);
            assert this.targetGeoResource != null;
            
            try {
                this.targetStore =  this.targetGeoResource.resolve(FeatureStore.class,
                                                                  monitor);
            } catch (IOException e) {
                throw new SOProcessException(Messages.BufferProcess_failed_creating_temporal_store);
            }
                
            this.targetLayer = addLayerToMap(this.map, this.targetGeoResource);
            
        }
        assert this.targetLayer != null;
        assert this.targetStore != null;

   }
   
   

    /**
     * Performs the Buffer operation as stated in thi class' comments
     * <p>
     * Operation cancelations works by checking <code>monitor.isCanceled()</code> periodically.
     * </p>
     * 
     * @param monitor
     * @throws SOProcessException 
     */
    @Override
    public void run( IProgressMonitor monitor ) throws SOProcessException {
        
        
        try {
            monitor.subTask(mainSteps[0]);
            monitor.worked(1);
            init(monitor);

            final int featureCount = this.selectedFeatures.size();
            final int ticks = mainSteps.length + featureCount;

            monitor.beginTask(Messages.BufferProcess_taskBuffering, ticks);

            checkCancelation();

            monitor.subTask(mainSteps[1]);
            
            IBufferParameters params = (this.paramsBufferInExistntLayer != null)
                    ? this.paramsBufferInExistntLayer
                    : this.paramsBufferInNewLayer;
            performBuffer(params, this.selectedFeatures);

            checkCancelation();

            monitor.subTask(mainSteps[2]);
            

        } catch (SOProcessException e) {
            
            throwException(e);
            
        
        } catch (InterruptedException e) {

            final String msg = Messages.BufferProcess_canceled;
            
            throwException(new SOProcessException(msg, e));
        } finally{
            endProcess((Map) map, this.targetLayer);
            
            monitor.done();
        }
        
    }



    /**
     * Traverses the FeatureCollection<SimpleFeatureType, SimpleFeature> <code>selection</code> creating a buffered geometry on each
     * SimpleFeature's default geometry and stores the result in a new SimpleFeature on the <code>target</code>
     * FeatureStore.
     * <p>
     * Note the buffer computation is made with a slightly {@link BufferOp modified version} of the
     * JTS <code>BufferOp</code> in order to allow the operation to be cancelled while inside the
     * buffer computation.
     * </p>
     * 
     * @param params buffer parameters
     * @param selection source layer's selected features in its native CRS
     * @throws SOProcessException if
     *         {@link #createBufferedFeature(SimpleFeature, SimpleFeatureType, Geometry)} fails
     * @throws IOException if it is thrown while adding the resulting features to the target
     *         FeatureStore<SimpleFeatureType, SimpleFeature> or while commiting the transaction
     * @throws InterruptedException if the user cancelled the operation
     */
    private void performBuffer(IBufferParameters params, 
    		FeatureCollection<SimpleFeatureType, SimpleFeature> selection ) 
            throws SOProcessException, InterruptedException{
        
        assert selection != null;
        
        final int featureCount = selection.size();
        
        final ILayer sourceLayer = this.sourceLayer;

        final CoordinateReferenceSystem sourceCrs = LayerUtil.getCrs(sourceLayer);

        final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(sourceLayer.getMap());

        final CoordinateReferenceSystem targetCrs = this.targetStore.getSchema().getDefaultGeometry()
                .getCRS();

        final Unit sourceUnits = GeoToolsUtils.getDefaultCRSUnit(mapCrs);
        final Unit targetUnits = params.getUnitsOfMeasure();
        final int quadSegments = params.getQuadrantSegments().intValue();

        final Double width = params.getWidth().doubleValue();
        
        
        SimpleFeature sourceFeature = null;
        // the one to use if params.isMergeGeometry() == true
        Geometry mergedGeometry = null;
        
        FeatureIterator<SimpleFeature> iterator = null;

        try {
            int processingCount = 0;
            Geometry geometry;
            
                        
            String subTaskName;
            
            iterator = selection.features();
            while( iterator.hasNext() ) {

                processingCount++;
                subTaskName = MessageFormat.format(
                        Messages.BufferProcess_subTask_BufferingFeatureN, 
                        processingCount, featureCount);
                
                getMonitor().subTask(subTaskName);

                checkCancelation();

                sourceFeature = iterator.next();

                geometry = (Geometry) sourceFeature.getDefaultGeometry();
                geometry = GeoToolsUtils.reproject(geometry, sourceCrs, mapCrs);
                geometry = makeBufferGeometry(geometry, width, sourceUnits, targetUnits, quadSegments, getMonitor());
                geometry = GeoToolsUtils.reproject(geometry, mapCrs, targetCrs);

                checkCancelation();

                if (params.isMergeGeometries()) {
                    if (mergedGeometry == null) {
                        mergedGeometry = geometry;
                    } else {
                        mergedGeometry = mergedGeometry.union(geometry);
                    }
                } else {
                    createAndStoreBufferedFeature(sourceFeature, geometry, this.targetStore);
                }
                getMonitor().worked(1);
            }
            checkCancelation();
            if (params.isMergeGeometries()) {
                createAndStoreBufferedFeature(null, mergedGeometry, this.targetStore);
            }

            getMonitor().subTask(Messages.BufferProcess_subtastCommittingTransaction);
           
            
        } catch (OperationNotFoundException e) {
            String message = MessageFormat.format(Messages.BufferProcess_failed_transforming, 
                                                  sourceFeature.getID(),
                                                  e.getMessage());
            throw new SOProcessException(message, e);
        } catch (TransformException e) {
            String message = MessageFormat.format( Messages.BufferProcess_failed_transforming_feature_to_crs,
                                                   sourceFeature.getID(), 
                                                   e.getMessage());
            throw new SOProcessException(message, e);
        } finally {

            if(iterator != null) iterator.close();
            
            getMonitor().done();

        }
    }

    /**
     * @param sourceFeature feature containing source attributes to match over <code>target</code>,
     *        or <code>null</code>
     * @param bufferedGeometry
     * @param target
     * @param monitor
     * @throws SOProcessException
     * @throws InterruptedException
     * @throws IOException
     */
    private void createAndStoreBufferedFeature( final SimpleFeature sourceFeature, 
                                                final Geometry bufferedGeometry,
                                                final FeatureStore<SimpleFeatureType, SimpleFeature> target ) 
            throws SOProcessException, InterruptedException{

        SimpleFeature newFeature;

        final SimpleFeatureType targetType = target.getSchema();

        newFeature = createBufferedFeature(sourceFeature, targetType, bufferedGeometry);

        checkCancelation();

        try {
            target.addFeatures(DataUtilities.collection(new SimpleFeature[]{newFeature}));
        } catch (IOException e) {
            final String msg = Messages.BufferProcess_adding_feature_to_store;
            throw new SOProcessException(msg, e);
        }
    }

    /**
     * Does the buffer for the source geomety
     *
     * @param sourceGeometry
     * @param width
     * @param sourceUnits
     * @param targetUnits
     * @param quadrantSegments
     * @param monitor
     * @return
     * @throws InterruptedException
     */
    private Geometry makeBufferGeometry(    
                                     final Geometry sourceGeometry, 
                                     final double width,
                                     final Unit sourceUnits,
                                     final Unit targetUnits, 
                                     final int quadrantSegments,
                                     final IProgressMonitor monitor ) 
            throws InterruptedException {

        double bufferWidth = getBufferWidth(width, sourceUnits, targetUnits);
        BufferOp bufOp = new BufferOp(sourceGeometry);
        bufOp.setQuadrantSegments(quadrantSegments);
        Geometry bufferedGeometry = bufOp.getResultGeometry(bufferWidth, new JTSProgressMonitor( monitor ));
 
        return bufferedGeometry;
    }

    /**
     * @param bufferWidth
     * @param bufferUnits
     * @param geometryUnits
     * @return the converted distance <code>bufferWidth</code> from <code>targetUnits</code> to
     *         <code>sourceUnits</code>
     */
    private double getBufferWidth( double bufferWidth, Unit sourceUnits, Unit targetUnits ) {
        assert sourceUnits != null;
        assert targetUnits != null;

        double convertedWidth;
        if (GeoToolsUtils.PIXEL_UNITS.equals(targetUnits)) {
            
            IViewportModel viewportModel = this.sourceLayer.getMap().getViewportModel();
            Coordinate origin = viewportModel.pixelToWorld(0, 0);
            int fixedBufferWidth = (int) Math.round(bufferWidth);
            Coordinate originPlusWidth = viewportModel.pixelToWorld(fixedBufferWidth,
                    fixedBufferWidth);
            convertedWidth = Math.abs(originPlusWidth.x - origin.x);
        } else {
            UnitConverter converter = targetUnits.getConverterTo(sourceUnits);
            convertedWidth = converter.convert(bufferWidth);
        }
        return convertedWidth;
    }

    /**
     * Creates a new SimpleFeature for <code>targetType</code> that holds the common attributes from
     * <code>sourceFeature</code> and the buffered geometry.
     * 
     * @param sourceFeature the original SimpleFeature from which to extract matching attributes for the
     *        new SimpleFeature, or <code>null</code> if the new SimpleFeature has to have empty attributes
     *        other than the default geometry.
     * @param targetType
     * @param bufferedGeometry the product geometry of running {@link BufferOp} over the default
     *        geometry of <code>sourceFeature</code> with the parameters provided to this
     *        operation.
     * @return a new SimpleFeature of type <code>targetType</code> holding the common attributes with
     *         <code>sourceFeature</code> and <code>bufferedGeometry</code> as the feature's
     *         default geometry
     * @throws SOProcessException
     */
    @SuppressWarnings("unchecked")
    private SimpleFeature createBufferedFeature( SimpleFeature sourceFeature, SimpleFeatureType targetType,
            Geometry bufferedGeometry ) throws SOProcessException {

        SimpleFeature newFeature;
        try {
            newFeature = DataUtilities.template(targetType);
            final GeometryDescriptor targetGeometryType = targetType.getDefaultGeometry();

            if (sourceFeature != null) {
                GeoToolsUtils.match(sourceFeature, newFeature);
            }

            final String attName = targetGeometryType.getLocalName();
            final Class geomClass = targetGeometryType.getType().getBinding();
            bufferedGeometry = GeometryUtil.adapt(bufferedGeometry, geomClass);

            newFeature.setAttribute(attName, bufferedGeometry);

        } catch (IllegalAttributeException e) {
            throw new SOProcessException(e.getMessage(), e);
        }
        return newFeature;
    }


}