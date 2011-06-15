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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import es.axios.geotools.util.FeatureUtil;

/**
 * This class Maintains convenient methods to handle the persistent geotools resources.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1
 */
public final class StoreManager {

    private final static Logger LOGGER = Logger.getLogger(StoreManager.class.getName());
    
    final static String PATH = "resources/es/axios/udig/spatialoperations/internal/process/";
    
    private StoreManager(){
        // utility class
    }
    public static FeatureStore createTarget(final String fileName) throws Exception {
        
        final File f = new File( PATH + fileName );
        final Map url = Collections.singletonMap("url", f.toURL());
        
        try {
            SimpleFeatureType type = DataUtilities.createType("target", "geom:MultyPolygon, name:String" );
            IndexedShapefileDataStoreFactory storeFactory = new IndexedShapefileDataStoreFactory();
            DataStore dataStore = storeFactory.createDataStore(url);
            dataStore.createSchema(type);
            
            return (FeatureStore) dataStore.getFeatureSource("target");

        } catch (Exception e) {
            
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            throw e;
        } 
    }
    
    public static DataStore createMemoryStore() throws Exception{
        
        //TODO this is an idiom to make same samples in memory
        SimpleFeatureType dataType = DataUtilities.createType("memory.test1",
                                            "id:0,foo:int,bar:double,geom:Point,group:String");

        int iVal[] = new int[]{4, 90, 20, 43, 29, 61, 8, 12};
        double dVal[] = new double[]{2.5, 80.433, 24.5, 9.75, 18, 53, 43.2, 16};

        SimpleFeature[] testFeatures = new SimpleFeature[iVal.length];
        GeometryFactory fac = new GeometryFactory();

        for( int i = 0; i < iVal.length; i++ ) {
        	
        	SimpleFeature f =  FeatureUtil.createFeatureUsing(testFeatures[i], dataType, fac.createPoint(new Coordinate(iVal[i], iVal[i])) );

        	testFeatures[i] = DataUtilities.template(dataType);
        	testFeatures[i].setAttribute(0, new Integer(i + 1));
        	testFeatures[i].setAttribute(1, new Integer(iVal[i]));
        	testFeatures[i].setAttribute(2, new Double(dVal[i]));
        	testFeatures[i].setAttribute(3, fac.createPoint(new Coordinate(iVal[i], iVal[i])));
        	testFeatures[i].setAttribute(3, "Group" + (i % 4)); //$NON-NLS-1$

//        	
//            testFeatures[i] = dataType.create(new Object[]{new Integer(i + 1),
//                    new Integer(iVal[i]), new Double(dVal[i]),
//                    fac.createPoint(new Coordinate(iVal[i], iVal[i])), "Group" + (i % 4)},
//                                              "classification.t" + (i + 1));
        }

        MemoryDataStore store = new MemoryDataStore();
        store.createSchema(dataType);
        store.addFeatures(testFeatures);
        
        return store;
    }


    public static FeatureSource<SimpleFeatureType, SimpleFeature> openLayer(final String fileName) throws IOException {
    
        final String path = "resources/es/axios/udig/spatialoperations/internal/process/"; //$NON-NLS-1$
        final File f = new File( path + fileName );
        final URL url = f.toURL();
        final Map<String,URL> map = Collections.singletonMap("url", url); //$NON-NLS-1$
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        assert dataStore != null;
        
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
        return featureSource;
    }

}
