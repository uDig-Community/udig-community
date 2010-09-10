package net.refractions.linecleaner.cleansing.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.resources.TestData;

public abstract class ProcessorTestCase extends TestCase {

	protected IndexedShapefileDataStore ds;
	protected String typeName;
	protected FeatureSource source;

	protected void setUp() throws Exception {
        super.setUp();
        long before = System.currentTimeMillis();

		ds = getDataStore(getFilename());
		
        typeName = ds.getTypeNames()[0];
        source = ds.getFeatureSource( typeName );
        System.out.println(source.getFeatures().getCount());
        long after = System.currentTimeMillis();
        after = after-before;
        System.out.println("Startup time: " + after);
    }
	
	
	public IndexedShapefileDataStore getDataStore(String filename) throws Exception {

        File shp = TestData.file( this, filename );
		Map params=new HashMap();
		params.put( IndexedShapefileDataStoreFactory.URLP.key, shp.toURL());
		params.put( IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, new Boolean(true));
		params.put( IndexedShapefileDataStoreFactory.MEMORY_MAPPED.key, new Boolean(false));
		params.put( IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key, IndexedShapefileDataStoreFactory.TREE_GRX);
//		params.put( IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key, IndexedShapefileDataStoreFactory.TREE_QIX);
        
		Logger.getLogger("org.geotools.data").setLevel(Level.WARNING);
		
		IndexedShapefileDataStoreFactory fac=new IndexedShapefileDataStoreFactory();
		return (IndexedShapefileDataStore) fac.createDataStore(params);
	}
	
	protected abstract String getFilename();
	
}
