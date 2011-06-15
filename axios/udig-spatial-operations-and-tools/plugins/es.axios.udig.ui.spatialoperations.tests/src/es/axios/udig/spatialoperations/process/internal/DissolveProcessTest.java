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
package es.axios.udig.spatialoperations.process.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.IncludeFilter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.tasks.IDissolveTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;

/**
 *
 * Dissolve Process Test
 *
 * FIXME it does not work
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1
 */
public class DissolveProcessTest extends TestCase {

    /**
     * In Multipolygon geometry the union is ever possible.
     * F1.geom = Polygon union F2.geom = Polygon => Fdissolve.geom = UnionGeom
     * @throws Exception 
     */
    public void testMultipolygonUnion() throws Exception {
        //TODO input source with F1 and F2 
        SimpleFeatureSource source = (SimpleFeatureSource) StoreManager.openLayer("dissolve_source.shp");
        List<String> dissolveProperty = new LinkedList<String>();
        dissolveProperty.add("name");
        SimpleFeatureStore target = (SimpleFeatureStore) StoreManager.createTarget("dissolve_target.shp");
        CoordinateReferenceSystem crs = target.getSchema().getCoordinateReferenceSystem();
        IncludeFilter filter = org.opengis.filter.Filter.INCLUDE;
        IDissolveTask process = SpatialOperationFactory.createDissolve(source, filter, dissolveProperty, crs, target, source.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem(),crs);
        
		ExecutorService executor = Executors.newCachedThreadPool();
		Future<FeatureStore<SimpleFeatureType, SimpleFeature>>  futureDissolve = executor.submit(process);
        
		while(!futureDissolve.isDone()){
			Thread.sleep(5000);
		}
        FeatureStore<SimpleFeatureType, SimpleFeature> featureStore = futureDissolve.get();
		
        assertTrue(featureStore != null);

        assertTrue(featureStore == target);
        
        Query query = Query.ALL;
        assertTrue((featureStore.getCount(query ) == 1));
    }

    /**
     * Sample: Two Polygons with intersection
     */
    public void testPolygonUnion() {
        fail("Not yet implemented");
    }

    /**
     * Sample: Polygon without intersection
     */
    public void testPolygonWhihoutUnion() {
        fail("Not yet implemented");
    }

}
