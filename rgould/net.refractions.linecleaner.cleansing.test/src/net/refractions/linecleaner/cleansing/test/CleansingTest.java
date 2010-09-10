package net.refractions.linecleaner.cleansing.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.refractions.linecleaner.cleansing.PerformCleansingAction;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.resources.TestData;

import junit.framework.TestCase;

public class CleansingTest extends TestCase {
	
	private IndexedShapefileDataStore ds;
	private String typeName;
	private FeatureSource source;

	protected void setUp() throws Exception {
        super.setUp();
        long before = System.currentTimeMillis();

		ds = getDataStore("ftenroads.shp");
//        ds = getDataStore("4000features.shp");
//        ds = getDataStore("smallset.shp");
//        ds = getDataStore("new.shp");
//        ds = getDataStore("newBigger.shp");
//        ds = getDataStore("1092features.shp");
		
        typeName = ds.getTypeNames()[0];
        source = ds.getFeatureSource( typeName );
        System.out.println(source.getFeatures().getCount());
        long after = System.currentTimeMillis();
        after = after-before;
        System.out.println("Startup time: " + after);
    }
	
	public void testCleansing() throws Exception {
//		PerformCleansingAction action = new PerformCleansingAction(this.ds, 3, 3, (2*3)^2);
//		PerformCleansingAction action = new PerformCleansingAction(this.ds, 10, 35, (2*35)^2);
//		PerformCleansingAction action = new PerformCleansingAction((FeatureStore) this.source, 10, -1, 25, 2500, 5);
		long before = System.currentTimeMillis();
		action.run(null);
		long after = System.currentTimeMillis();
		after = after - before;
		System.out.println("Total running time for cleansing: " + after);
	}
	
	public IndexedShapefileDataStore getDataStore(String filename) throws Exception {

        File shp = TestData.file( this, filename );
		Map params=new HashMap();
		params.put( IndexedShapefileDataStoreFactory.URLP.key, shp.toURL());
		params.put( IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, new Boolean(true));
		params.put( IndexedShapefileDataStoreFactory.MEMORY_MAPPED.key, new Boolean(false));
//		params.put( IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key, IndexedShapefileDataStoreFactory.TREE_GRX);
		params.put( IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key, IndexedShapefileDataStoreFactory.TREE_QIX);
        
		Logger.getLogger("org.geotools.data").setLevel(Level.WARNING);
		
		IndexedShapefileDataStoreFactory fac=new IndexedShapefileDataStoreFactory();
		return (IndexedShapefileDataStore) fac.createDataStore(params);
	}
}
