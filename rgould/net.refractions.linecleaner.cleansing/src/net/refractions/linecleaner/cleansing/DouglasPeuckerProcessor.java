package net.refractions.linecleaner.cleansing;

import java.io.IOException;

import net.refractions.linecleaner.LoggingSystem;
import net.refractions.udig.project.internal.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.simplify.DouglasPeuckerLineSimplifier;

/**
 * Runs over every Feature in a FeatureStore and processes it 
 * using an implementation of the Douglas Peucker algorithm.
 * 
 * @author rgould
 */
public class DouglasPeuckerProcessor extends AbstractProcessor {

	public static final double DEFAULT_DISTANCE_TOLERANCE = 5;
	
	double distanceTolerance;
	LoggingSystem loggingSystem = LoggingSystem.getInstance();

	
	public DouglasPeuckerProcessor(Map map, FeatureStore featureStore, double distanceTolerance) {
		super(map, featureStore);
		this.distanceTolerance = distanceTolerance;
		
	}
	
	protected void runInternal(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {
		if (monitor == null) monitor = new NullProgressMonitor();
		
		loggingSystem.setCurrentAction(LoggingSystem.DOUGLAS_PEUCKER);
		loggingSystem.begin();
		int start = featureStore.getCount(Query.ALL);
		
		monitor.beginTask("", start);
		monitor.subTask("Performing Douglas-Peucker vertex removal");
		
		MemoryFeatureIterator iter = new MemoryFeatureIterator(featureStore, map, 
				new DefaultQuery(featureStore.getSchema().getTypeName(), Filter.NONE, 
						new String[] { featureStore.getSchema().getDefaultGeometry().getName() }));
		
//		FeatureIterator iter = featureStore.getFeatures().features();
		try {
			while (iter.hasNext()) {
				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
				pauseIfNecessary(pauseMonitor);
				
				Feature feature = iter.next();
	//			System.out.println("[[DPP]] begin processing feature " + feature.getID());
				
				Geometry geom = feature.getDefaultGeometry();
				Coordinate[] points = geom.getCoordinates();
				
	//			System.out.println("BEFORE Geom: " + geom);
				int before = points.length;
	//			System.out.println("BEFORE Coord count: " + points.length);
				points = DouglasPeuckerLineSimplifier.simplify(points, distanceTolerance);
	//			System.out.println("AFTER Coord count: " + points.length);
				int after = before - points.length;
				if (after > 0) {
//					loggingSystem.modify(feature, "Removed "+after+" coordinates.");
				}
				
				GeometryFactory factory = new GeometryFactory();
				geom = factory.createMultiLineString(new LineString[] { factory.createLineString(points) });
				
	//			System.out.println("AFTER Geom: " + geom);
				
				String xpath = feature.getFeatureType().getDefaultGeometry().getName();
	    					
				AttributeType attributeType = feature.getFeatureType().getAttributeType(xpath);
				String fid = feature.getID();
				FidFilter fidFilter = FilterFactoryFinder.createFilterFactory().createFidFilter(fid);
				this.featureStore.modifyFeatures(attributeType, geom, fidFilter);
				
			}
		} finally {
			iter.close();
			monitor.done();
		}

		int finish = start - featureStore.getCount(Query.ALL);
		loggingSystem.finish(finish);
	}

}
