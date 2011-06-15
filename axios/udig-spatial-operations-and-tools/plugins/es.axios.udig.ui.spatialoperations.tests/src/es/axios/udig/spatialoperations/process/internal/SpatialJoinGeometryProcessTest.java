/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
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
package es.axios.udig.spatialoperations.process.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.tasks.ISpatialJoinTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.spatialoperations.tasks.SpatialRelation;




/**
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class SpatialJoinGeometryProcessTest extends TestCase {

    private final static Logger LOGGER       = Logger
                                                     .getLogger(SpatialJoinGeometryProcessTest.class
                                                                                                    .getName());
    private SimpleFeatureCollection   firstSource  = null;
    private SimpleFeatureCollection   secondSource = null;
    private SimpleFeatureStore        target       = null;

    protected void setUp() throws Exception {
        super.setUp();

        // open the first layer
        SimpleFeatureSource firstSource = (SimpleFeatureSource) StoreManager.openLayer("join_source1.shp");
        this.firstSource = firstSource.getFeatures();

        // open the second layer
        SimpleFeatureSource secondSource = (SimpleFeatureSource) StoreManager.openLayer("join_source2.shp");
        this.secondSource = secondSource.getFeatures();

        // create a new target
        this.target = (SimpleFeatureStore) StoreManager.createTarget("join_target2.shp");
    }
    
    /**
     * Test intersects relation
     * @throws Exception
     */
    public void testIntersectsRelation()throws Exception{

        CoordinateReferenceSystem crs = this.target.getSchema().getCoordinateReferenceSystem();
        
        ISpatialJoinTask<Object> process = SpatialOperationFactory.createSpatialJoin(
        		this.firstSource, this.secondSource, SpatialRelation.Intersects, crs, 
        		this.target, crs, crs, crs);
		ExecutorService executor = Executors.newCachedThreadPool();
		Future<Object>  futureDissolve = executor.submit(process);

		while( !futureDissolve.isDone() ){
			//wait it is done
			Thread.sleep(5000);
		}
		SimpleFeatureCollection features = this.target.getFeatures();
        
        assertTrue(features.size() > 0); 
    }

    public void testCancel()throws Exception{

        CoordinateReferenceSystem crs = this.target.getSchema().getCoordinateReferenceSystem();
        
//// TODO       SpatialDataProcess process = SpatialJoinGeometryProcess.createProcess(this.firstSource,
//                                                                                     this.secondSource,
//                                                                                     SpatialRelation.Intersects,
//                                                                                     crs,
//                                                                                     this.target);
//        process.cancel();
//        assertTrue( process.getStatus() == Status.CANCELED);
    }

    public void testFailed(){
// TODO
//        SpatialDataProcess process = null;
        try {
            CoordinateReferenceSystem crs = this.target.getSchema().getCoordinateReferenceSystem();
//
////            process = SpatialJoinGeometryProcess.createProcess(this.firstSource,
//                                                                      this.secondSource,
//                                                                      SpatialRelation.Intersects,
//                                                                      crs, this.target);
            fail("an exception was expected");
        } catch (Exception e) {

//            assertNotNull(process);
//            assertTrue(process.getStatus() == Status.FAILED);
        }
    }
}
