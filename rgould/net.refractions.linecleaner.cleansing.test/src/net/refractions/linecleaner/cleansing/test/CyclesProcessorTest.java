package net.refractions.linecleaner.cleansing.test;

import net.refractions.linecleaner.cleansing.CyclesProcessor;

import org.geotools.data.FeatureStore;

public class CyclesProcessorTest extends ProcessorTestCase {

	@Override
	protected String getFilename() {
		return "1092features.shp";
	}
	
	public void testCyclesProcessor() throws Exception {
		CyclesProcessor processor = new CyclesProcessor((FeatureStore) source, 100);
		processor.run();
	}

}
