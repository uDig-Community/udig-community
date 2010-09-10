package net.refractions.linecleaner.cleansing.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import net.refractions.linecleaner.cleansing.MinimumLengthProcessor;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.resources.TestData;

public class MinimumLengthTest extends ProcessorTestCase {
	private IndexedShapefileDataStore ds;

	@Override
	protected String getFilename() {
		return "minimumLength.shp";
	}

	
	public void testMinimumLength() throws Exception {
		int featuresBefore = this.source.getCount(Query.ALL);
		System.out.println("Features before: " + featuresBefore);
		assertEquals(featuresBefore, 60);

		MinimumLengthProcessor mlp = new MinimumLengthProcessor((FeatureStore) this.ds.getFeatureSource(), 5);
		long before = System.currentTimeMillis();
		mlp.run();
		long after = System.currentTimeMillis();
		after = after-before;
		System.out.println("Done. Time to complete: " + after);
		int featuresAfter = this.source.getCount(Query.ALL);
		
		System.out.println("Features after: " + featuresAfter);
		assertEquals(featuresAfter, 35);
	}



}
