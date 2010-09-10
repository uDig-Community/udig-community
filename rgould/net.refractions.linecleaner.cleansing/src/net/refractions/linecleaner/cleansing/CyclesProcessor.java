package net.refractions.linecleaner.cleansing;

import java.io.IOException;

import net.refractions.linecleaner.LoggingSystem;
import net.refractions.udig.project.internal.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class CyclesProcessor extends AbstractProcessor {

	/**
	 * Default length tolerance is -1, which means that it is
	 * turned off, and all cycles that are encountered will be
	 * removed.
	 */
	public static final double DEFAULT_LENGTH_TOLERANCE = -1;
	double totalLengthTolerance;
	
	LoggingSystem loggingSystem;
	
	
	public CyclesProcessor(Map map, FeatureStore featureStore, double totalLengthTolerance) {
		super(map, featureStore);
		this.totalLengthTolerance = totalLengthTolerance;
		loggingSystem = LoggingSystem.getInstance();
	}

	protected void runInternal(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {

		if (monitor == null) monitor = new NullProgressMonitor();
		
		loggingSystem.setCurrentAction(LoggingSystem.CYCLES);
		loggingSystem.begin();
		
		int begin = featureStore.getCount(Query.ALL);
		monitor.beginTask("", begin);
		monitor.subTask("Checking for cycles");
		
		MemoryFeatureIterator iter = MemoryFeatureIterator.createDefault(featureStore, map);
		try {
			while (iter.hasNext()) {
				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
				pauseIfNecessary(pauseMonitor);
				
				Feature feature = iter.next();
				Geometry geometry = feature.getDefaultGeometry();
				
				Coordinate start = geometry.getCoordinates()[0];
				Coordinate end = geometry.getCoordinates()[geometry.getCoordinates().length-1];
				if (start.equals2D(end)) {
					if (totalLengthTolerance > 0 && geometry.getLength() >= totalLengthTolerance) {
						loggingSystem.info("Found a cycle with length " + geometry.getLength()+", fid: "+
								LoggingSystem.featureToString(feature)+". Not removing it.");
					} else {
						loggingSystem.delete(feature);
						FidFilter fidFilter = FilterFactoryFinder.createFilterFactory().createFidFilter(feature.getID());
						featureStore.removeFeatures(fidFilter);
					}
				}
			}
		} finally {
			iter.close();
			monitor.done();
		}
		
		int finish = begin - featureStore.getCount(Query.ALL);
		
		
		loggingSystem.finish(finish);
	}

}
