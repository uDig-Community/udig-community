package net.refractions.linecleaner.cleansing;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

import net.refractions.linecleaner.FeatureUtil;
import net.refractions.linecleaner.GeometryUtil;
import net.refractions.linecleaner.LoggingSystem;
import net.refractions.udig.project.internal.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

public class PseudoNodeProcessor extends AbstractProcessor {

	private LoggingSystem loggingSystem;
	private double intersectionDistanceTolerance = 0.0;
    
	public PseudoNodeProcessor(Map map, FeatureStore featureStore) {
		this(map, featureStore, Level.WARNING);
	}
	
	public PseudoNodeProcessor(Map map, FeatureStore featureStore, Level loggingLevel) {
		super(map, featureStore);
    	loggingSystem = LoggingSystem.getInstance();
	}

    public PseudoNodeProcessor(Map map, FeatureStore featureStore, double intersectionDistanceTolerance) {
        this(map, featureStore, Level.WARNING);
        this.intersectionDistanceTolerance = intersectionDistanceTolerance;
    }
    
    public PseudoNodeProcessor(Map map, FeatureStore featureStore, Level loggingLevel, double intersectionDistanceTolerance) {
        this(map, featureStore, loggingLevel);
        this.intersectionDistanceTolerance = intersectionDistanceTolerance;
    }

	protected void runInternal(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {
		if (monitor == null) monitor = new NullProgressMonitor();

		loggingSystem.setCurrentAction(LoggingSystem.PSEUDO_NODES);
		loggingSystem.begin();
		long start=System.currentTimeMillis();
		monitor.beginTask("", featureStore.getCount(Query.ALL));
		monitor.subTask("Removing pseudo nodes");
		
		MemoryFeatureIterator iter = MemoryFeatureIterator.createDefault(this.featureStore, map);
		try {
			while (iter.hasNext()) {
				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
				if (pauseMonitor != null && pauseMonitor.isPaused()) {
					while (pauseMonitor.isPaused()) {
						synchronized (this) {
							try {
								wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}

				Feature feature = iter.next();
				Geometry geom = feature.getDefaultGeometry();
				GeometryFactory gf = geom.getFactory();
                Coordinate[] coordinates = geom.getCoordinates();
                Point startPoint = gf.createPoint(coordinates[0]);
                Point endPoint = gf.createPoint(coordinates[coordinates.length-1]);
                
				try {
					processNode(startPoint);
					processNode(endPoint);
				} catch (IllegalFilterException e) {
					throw (IOException) new IOException().initCause(e);
				} catch (IllegalAttributeException e) {
					throw (IOException) new IOException().initCause(e);
				}
				
			}
		} finally {
			iter.close();
			monitor.done();
		}
        
        System.out.println("Pseudo Nodes done in: "+(System.currentTimeMillis()-start)/1000.00+" seconds");
		loggingSystem.finish();
	}

    protected FeatureCollection getConnectedFeatures(Point p)
    throws IllegalFilterException, IOException {
        String typename = featureStore.getSchema().getTypeName();
        String geomName = featureStore.getSchema().getDefaultGeometry().getName();
        
        // if we're just doing straight pseudo-noding, use an intersection filter
        if (this.intersectionDistanceTolerance == 0.0) {
            Filter intersectsFilter = FeatureUtil.intersectsGeom(geomName, p);
            FeatureCollection connectedFeatures = this.featureStore.getFeatures(
                    new DefaultQuery(typename, intersectsFilter, new String[] { geomName }));
            return connectedFeatures;
        }
        
        // pseudo-noding with a certain tolerance on what's considered an intersection
        Filter bboxFilter = 
            GeometryUtil.getBBoxFilter(featureStore, p, this.intersectionDistanceTolerance);
        Filter distanceFilter =
            FeatureUtil.distanceToGeom(geomName, p, this.intersectionDistanceTolerance);
        FeatureCollection connectedFeatures = this.featureStore.getFeatures(
                new DefaultQuery(typename, bboxFilter.and(distanceFilter), new String[] {geomName}));
        
        return connectedFeatures;
    }
    
	private void processNode(Point node) throws IllegalFilterException, IOException, IllegalAttributeException {
		FeatureCollection connectedFeatures = getConnectedFeatures(node);
        
        Feature first = null;
        Feature second = null;
        // We'll attempt to get two features off connectedFeatures, otherwise simply return.
        // NOTE: we can't use connectedFeatures.size() == 2 here cause size() is borken.
        FeatureIterator iter = connectedFeatures.features();
        try {
            if (iter.hasNext()) first = iter.next(); else return;
            if (iter.hasNext()) second = iter.next(); else return;
            if (iter.hasNext()) return;
        } finally {
            iter.close();
        }
        
        if (first.getID().equals(second.getID())) {
			loggingSystem.info("Found a feature that is a cycle. "+LoggingSystem.featureToString(first));
			return;
		}
		
		Geometry firstGeom = first.getDefaultGeometry();
		Geometry secondGeom = second.getDefaultGeometry();
        
        // there's no point in merging zero-length lines.
        if (firstGeom.getLength() == 0 || secondGeom.getLength() == 0) {
            return;
        }        
        
        LineMerger merger = new LineMerger();        
        merger.add(firstGeom);
        merger.add(secondGeom);
		
		Collection merged = merger.getMergedLineStrings();
		if (merged.size() != 1) {
			loggingSystem.warning("Lines did not merge properly! Lines might not share end-nodes. " 
					+ LoggingSystem.featureToString(first)+ " and " 
					+ LoggingSystem.featureToString(second));
			return;
		}
		
		LineString result = (LineString) merged.iterator().next();
        
        // don't allow self-intersecting lines
        if (!result.isSimple()) {
            return;
        }
        
		GeometryFactory factory = new GeometryFactory();
		Geometry mergedLine = factory.createMultiLineString(new LineString[] { result });
		
		modifyFeature(first, mergedLine);
		loggingSystem.modify(first, "Merged features '"+first.getID()+"' and '"+second.getID()+"'.");
		deleteFeature(second);		
		loggingSystem.delete(second);
	}


	private void modifyFeature(Feature feature, Geometry newGeometry) throws IllegalAttributeException, IOException {
		String fid = feature.getID();
		FidFilter fidFilter = FilterFactoryFinder.createFilterFactory().createFidFilter(fid);
		
		String xpath = feature.getFeatureType().getDefaultGeometry().getName();
		
		feature.setAttribute(xpath, newGeometry);
		
		AttributeType attributeType = feature.getFeatureType().getAttributeType(xpath);
		featureStore.modifyFeatures(attributeType, newGeometry, fidFilter);
	}
	
	private void deleteFeature(Feature feature) throws IOException {
		String fid = feature.getID();
		FidFilter fidFilter = FilterFactoryFinder.createFilterFactory().createFidFilter(fid);
        
		featureStore.removeFeatures(fidFilter);
	}
}
