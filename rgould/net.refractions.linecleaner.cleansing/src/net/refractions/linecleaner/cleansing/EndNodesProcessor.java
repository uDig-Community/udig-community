package net.refractions.linecleaner.cleansing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.refractions.linecleaner.GeometryUtil;
import net.refractions.linecleaner.LoggingSystem;
import net.refractions.udig.project.internal.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * See the file doc/end-nodes.txt for the algorithm that this is based on.
 * 
 * @author rgould
 *
 */
public class EndNodesProcessor extends AbstractProcessor {

	public static final double DEFAULT_DISTANCE_TOLERANCE = 25;
	public static final double DEFAULT_AREA_TOLERANCE = 3000;
	
    private double distanceTolerance;
    private ArrayList<NodeCollection> nodeCollections;
    private java.util.Map<Node, NodeCollection> collectionIndex;
	private double areaTolerance;
	private long startTime;
	
	LoggingSystem loggingSystem = LoggingSystem.getInstance();
	private String typename;
	private String defaultGeom;

    
    /**
     * 
     * @param dataStore - datastore to perform the operation on
     * @param distanceTolerance - minimum distance at which end-nodes are snapped together
     * @param areaTolerance - groups of end-nodes with area greater than this are flagged/ignored
     * @param level - sets the level of the logger. defaults to Level.WARNING
     */
    public EndNodesProcessor (Map map, FeatureStore featureStore, double distanceTolerance, 
    		double areaTolerance) {
    	super(map, featureStore);
        this.distanceTolerance = distanceTolerance;
        this.areaTolerance = areaTolerance;
        this.nodeCollections = new ArrayList<NodeCollection>();
        this.collectionIndex = new HashMap<Node, NodeCollection>();
        this.typename = featureStore.getSchema().getTypeName();
        this.defaultGeom = featureStore.getSchema().getDefaultGeometry().getName();
    }
    
    public void printNodeCollections() {
    	System.out.println("Node Collections: Size: " +nodeCollections.size());
    	for (int i = 0; i < nodeCollections.size(); i++) {
    		NodeCollection nc = nodeCollections.get(i);
    		System.out.print("NodeCollection["+i+"] (Flagged:"+ nc.isFlagged() +") (Centroid: " + nc.calculateAveragePoint() + ") contains EndNodes: ");
    		Iterator iter = nc.iterator();
    		while (iter.hasNext()) {
    			Point point = (Point) iter.next();
    			String fid = (String) point.getUserData();
    			
    			System.out.print(fid+" ");
    		}
    		System.out.println();
    	}
    }
    
    
    protected void runInternal(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	loggingSystem.setCurrentAction(LoggingSystem.END_NODES);
    	loggingSystem.begin();
    	
    	monitor.beginTask("End Nodes: ", 2);
    	
    	if (monitor.isCanceled()) {
    		return;
    	}
		pauseIfNecessary(pauseMonitor);
		
    	setupNodeCollections(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
    			pauseMonitor);
    	
    	if (isDebugging()) {
    		try {
				dumpNodeCollections();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	if (monitor.isCanceled()) {
    		return;
    	}
    	pauseIfNecessary(pauseMonitor);
		
    	processNodeCollections(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
    			pauseMonitor);
        
    	monitor.done();
        
        
        loggingSystem.fine("####################################################");
        int count = 0;
        loggingSystem.fine("FLAGGED NODE COLLECTIONS: ");
        for (NodeCollection collection : getNodeCollections()) {
        	if (collection.isFlagged()) {
        		count++;
        		Envelope bbox = collection.getEnvelope();
        		double area = bbox.getHeight() * bbox.getWidth();
        		loggingSystem.fine(collection.toString() + "Area size:"+ area);
        	}
        }
        loggingSystem.finish(count);
        loggingSystem.fine("TOTAL FLAGGED NODE COLLECTIONS: " + count);
        loggingSystem.fine("####################################################");
    }

    private void dumpNodeCollections() throws Exception {
    	AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom", Polygon.class);
    	AttributeType flagged = AttributeTypeFactory.newAttributeType("flagged", Boolean.class);

    	AttributeType size = AttributeTypeFactory.newAttributeType("size", Integer.class);
    	FeatureType ftNode = FeatureTypeFactory.newFeatureType(new AttributeType[] {geom, flagged, size}, "nodeCollection");

    	ArrayList<Feature> features = new ArrayList<Feature>(nodeCollections.size());
    	
    	GeometryFactory factory = new GeometryFactory();
		
		for (NodeCollection collection : nodeCollections) {
			Coordinate[] coords = new Coordinate[5];
			
			Envelope envelope = collection.getEnvelope();
			
			coords[0] = new Coordinate(envelope.getMinX(), envelope.getMinY());
			coords[1] = new Coordinate(envelope.getMinX(), envelope.getMaxY());
			coords[2] = new Coordinate(envelope.getMaxX(), envelope.getMaxY());
			coords[3] = new Coordinate(envelope.getMaxX(), envelope.getMinY());
			coords[4] = coords[0];				
				 
			LinearRing ring = factory.createLinearRing(coords);
			Polygon polygon = factory.createPolygon(ring, null);

			Feature newFeature = ftNode.create(new Object[] { polygon, collection.isFlagged() , collection.size()} );
			features.add(newFeature);
		}
		

		String typename = featureStore.getSchema().getTypeName();
		String tmpDir = System.getProperty("java.io.tmpdir");
		String separator = System.getProperty("file.separator");
		File file = new File(tmpDir + separator + typename + "-pre"+getName()+"NCDUMP"+".shp");
		

		ShapefileDataStoreFactory dsFactory = new ShapefileDataStoreFactory();
        ShapefileDataStore ds = (ShapefileDataStore)dsFactory.createDataStore(file.toURL());
		ds.createSchema(ftNode);
		
		FeatureStore newStore = (FeatureStore) ds.getFeatureSource();
		FeatureReader reader = DataUtilities.reader(features);
		newStore.addFeatures(reader);
			
	}

	/**
     * Run through the features and set up each end-node into a collection
     * that includes other end-nodes that are nearby.
     * @throws IOException 
     *
     */
	public void setupNodeCollections(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {
		loggingSystem.fine("******************************");
		loggingSystem.fine("Begin: Set up NodeCollections.");
		
		monitor.beginTask("", 2*featureStore.getCount(Query.FIDS));
		monitor.subTask("Setup");
		
		Query query = new DefaultQuery(this.typename, Filter.NONE, new String[] {this.defaultGeom});
        MemoryFeatureIterator iter = new MemoryFeatureIterator(featureStore, map, query);
        try {
            int count = 0;
	        while (iter.hasNext()) {
	        	Feature feature = (Feature) iter.next();
	        	Geometry geometry = feature.getDefaultGeometry();
	        	
	        	Coordinate start = geometry.getCoordinates()[0];
	        	Coordinate end = geometry.getCoordinates()[geometry.getCoordinates().length-1];
	        	
	        	Point startPoint = geometry.getFactory().createPoint(start);
	        	Point endPoint = geometry.getFactory().createPoint(end);
	        	startPoint.setUserData(feature.getID());
	        	endPoint.setUserData(feature.getID());
	        	
	        	processEndNode(startPoint);
	        	
	        	monitor.worked(1);
	        	if (monitor.isCanceled()) {
	        		break;
	        	}
	        	
	        	processEndNode(endPoint);
	        	
	        	monitor.worked(1);
	        	if (monitor.isCanceled()) {
	        		break;
	        	}
	        	
	        	if ((count%100) == 0) {
	        		double time = System.currentTimeMillis();
	        		time = time-startTime;
	        		loggingSystem.fine("[SetupNodeCollections]: Processing feature #"+count+". Time is at " + time + "ms.");
	        	}
	        	count++;
	        }
        } finally { 
        	iter.close();
        	monitor.done();
        }
        loggingSystem.fine("Finished setting up node collections. Total collections: " + getNodeCollections().size() );
	}
	
	/**
	 * Given an end-node (endNodeA), assign it to a NodeCollection for later
	 * processing.
	 * 
	 * See the file doc/end-nodes.txt for the algorithm that this is based on.
	 * 
	 * @param endNodeA
	 */
	private void processEndNode(Point endNodeA) {
		loggingSystem.finest("Begin processing endNode: " + endNodeToString(endNodeA));
        Collection<Point> nearby = findNearbyNodes(distanceTolerance, endNodeA);
        
        NodeCollection collectionA = findNodeCollection(endNodeA);
        
        Iterator nearbyIter = nearby.iterator();
        while (nearbyIter.hasNext()) {
            Point endNodeB = (Point) nearbyIter.next();
            NodeCollection collectionB = findNodeCollection(endNodeB);
            
            if (collectionA != null && collectionB != null) {
            	if (collectionA == collectionB) {
            		continue;
            	} else {
            		loggingSystem.fine("Merging nodecollections: "+ endNodeToString(endNodeA) + " and " + endNodeToString(endNodeB));
            		mergeCollections(collectionA, collectionB);
            		continue;
            	}
            }
            
            if (collectionA != null) {
            	loggingSystem.finest("Adding " + endNodeToString(endNodeB) + " to the collection of " + endNodeToString(endNodeA));
            	addToCollection(collectionA, endNodeB);
            } else {
		        if (collectionB != null) {
		        	loggingSystem.finest("Adding " + endNodeToString(endNodeA) + " to the collection of " + endNodeToString(endNodeB));
		            addToCollection(collectionB, endNodeA);
		            collectionA = collectionB; //This prevents us from having to do findNodeCollection(endNodeA) on every iteration. They ARE the same collections.
		        } else {
		        	loggingSystem.finest("Creating a new NodeCollection for " + endNodeToString(endNodeA) + " and " +endNodeToString(endNodeB));
	                collectionA = createNewNodeCollection();
	                addToCollection(collectionA, endNodeA);
	                addToCollection(collectionA, endNodeB);
		        }
            }
        }
    }

	private void mergeCollections(NodeCollection collectionA, NodeCollection collectionB) {
		collectionA.merge(collectionB);
		getNodeCollections().remove(collectionB);

		// update the index
		Iterator i = collectionB.iterator();
		while (i.hasNext()) {
			Point p = (Point) i.next();
			Node n = new Node(p, (String) p.getUserData());
			collectionIndex.put(n, collectionA);
		}
	}
	
	private void addToCollection(NodeCollection collectionA, Point endNodeB) {
		collectionA.add(endNodeB);
		collectionIndex.put(new Node(endNodeB, (String) endNodeB.getUserData()), collectionA);
	}
    
	/**
	 * Performs the real processing on the node collections.
	 * 
	 * In short:
	 * Run through each nodeCollection. If its collective area
	 * is too large, flag it. 
	 * 
	 * Calculate an average point. If the collection is not flagged,
	 * move every end-node inside the collection to that point.
	 * 
	 * See the file doc/end-nodes.txt for the algorithm that this is based on.
	 * @param monitor 
	 * @throws IOException 
	 *
	 */
    private void processNodeCollections(IProgressMonitor monitor, PauseMonitor pauseMonitor)
    throws IOException {
    	loggingSystem.fine("******************************");
    	loggingSystem.fine("Begin: Processing Node Collections");
    	int count = 0;
    	
    	monitor.beginTask("", getNodeCollections().size());
    	monitor.subTask("Processing");
    	
        for (NodeCollection collectionC : getNodeCollections()) {
        	
            Envelope bbox = collectionC.getEnvelope();
            
            if (!isTolerableAreaSize(bbox)) {
            	double area = bbox.getHeight() * bbox.getWidth();
            	loggingSystem.info("Node Collection has a total area ("+area+") greater than " + this.areaTolerance +". Collection: "+ collectionC);
                collectionC.setFlagged(true);
            }
            
            Point averagePoint = collectionC.calculateAveragePoint();
            if (!collectionC.isFlagged()) {
            	repositionNodes(collectionC, averagePoint);
            }
            
        
//    		double time = System.currentTimeMillis();
//    		time = time-startTime;
//    		LOGGER.fine("Finished processing NodeCollection #"+count+" Size: " +collectionC.size()+ 
//    				". Time is at " + time + "ms.");
//    		Runtime runtime = Runtime.getRuntime();
//    		LOGGER.finer("MEMORY: Total: "+runtime.totalMemory()+" Max: "+runtime.maxMemory()+" Free: " + runtime.freeMemory());
    	
        	count++;
        	
        	monitor.worked(1);
        	if (monitor.isCanceled()) {
        		break;
        	}
        	pauseIfNecessary(pauseMonitor);
        }
        
        monitor.done();
//        try {
//        	featureStore.getTransaction().commit();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }

    /**
     * For every node that is in collectionC, reposition it so that it is 
     * located at point "averagePoint".
     * 
     * Note that this method does not call commit() or close() on the 
     * dataStore.
     * 
     * @param collectionC
     * @param averagePoint
     * @throws IOException 
     */
    private void repositionNodes(NodeCollection collectionC, Point averagePoint) throws IOException {
    	loggingSystem.fine("Reposition Nodes to "+averagePoint+" for NodeCollection: " + collectionC);
    	    	
    	Iterator iter = collectionC.iterator();
    	while (iter.hasNext()) {
    		Point point = (Point) iter.next();
    		
    		String fid = (String) point.getUserData();
    		FidFilter fidFilter = FilterFactoryFinder.createFilterFactory().createFidFilter(fid);
    		
    		Feature feature = null;
    		
    		Query query = new DefaultQuery(this.typename, fidFilter, new String[] {this.defaultGeom});
            MemoryFeatureIterator iter2 = new MemoryFeatureIterator(featureStore, map, query);
    		try {
				feature = (Feature) iter2.next();
    		} finally { 
				iter2.close();
    		} 
			
    		Geometry geometry = feature.getDefaultGeometry();
    		
    		boolean beginning = false;
    		Coordinate[] coords = geometry.getCoordinates();
    		Coordinate start = coords[0];
    		if (start.equals2D(point.getCoordinate())) {
    			beginning = true;
    		} 
    		loggingSystem.finest("REPOSITIONING: BEFORE: " + geometry);

    		if (beginning) {
    			geometry.getCoordinates()[0].setCoordinate(averagePoint.getCoordinate());
    		} else {
    			geometry.getCoordinates()[geometry.getCoordinates().length-1].setCoordinate(averagePoint.getCoordinate());
    		}
    		
    		loggingSystem.finest("REPOSITIONING: AFTER: " + geometry);
    		
    		
    		String xpath = feature.getFeatureType().getDefaultGeometry().getName();
    		
    		try {
				feature.setAttribute(xpath, geometry);
			} catch (IllegalAttributeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			AttributeType attributeType = feature.getFeatureType().getAttributeType(xpath);
			this.featureStore.modifyFeatures(attributeType, geometry, fidFilter);
			
    	}
    }

    /**
     * Returns true of the area of bbox is less than this.areaTolerance.
     * 
     * @param bbox
     * @return
     */
	private boolean isTolerableAreaSize(Envelope bbox) {	
		if (areaTolerance <= 0) {
			return true;
		}
		
    	double area = bbox.getHeight() * bbox.getWidth();
    	
    	return area <= areaTolerance;
	}

	/**
     * Searches all known NodeCollections for "endNode" and returns the
     * containing NodeCollection, if it is found. Otherwise, it returns
     * null.
     *
     * @param endNode
     * @return
     */
    private NodeCollection findNodeCollection( Point endNode ) {
    	Node n = new Node(endNode, (String) endNode.getUserData());
    	NodeCollection nc = null;
    	if (collectionIndex.containsKey(n)) {
    		nc = collectionIndex.get(n);
    	}
    	return nc;
    }

    public List<NodeCollection> getNodeCollections() {
        return nodeCollections;
    }

    /**
     * Creates a new NodeCollection and adds it to the local pool
     *
     * @return
     */
    private NodeCollection createNewNodeCollection() {
        NodeCollection newNC = new NodeCollection();
        getNodeCollections().add(newNC);
        return newNC;
    }
    
    /**
     * Given a node (endNode), locate every other node that is within a 
     * certain distance (toleranceDistance). Return each of those endNodes.
     * 
     * @param toleranceDistance
     * @param endNode
     * @return
     */
    private Collection<Point> findNearbyNodes( double toleranceDistance, Point endNode ) {    
        ArrayList<Point> nearbyNodes = new ArrayList<Point>();
        
        FeatureCollection featureCollection = null;
        String geomName = featureStore.getSchema().getDefaultGeometry().getName();
        try {
			featureCollection = this.featureStore.getFeatures(
					new DefaultQuery(typename, getBBoxFilter(endNode), new String[] { geomName }));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FeatureIterator iter = featureCollection.features();
		try {
	        while (iter.hasNext()) {
	        	Feature feature = (Feature) iter.next();
	        	Geometry geometry = feature.getDefaultGeometry();
	        	
	        	Coordinate start = geometry.getCoordinates()[0];
	        	Coordinate end = geometry.getCoordinates()[geometry.getCoordinates().length-1];
	        	
	        	Point startPoint = geometry.getFactory().createPoint(start);
	        	Point endPoint = geometry.getFactory().createPoint(end);
	        	startPoint.setUserData(feature.getID());
	        	endPoint.setUserData(feature.getID());
	        	
	        	if (!endNode.equals(startPoint) && 
	        			endNode.isWithinDistance(startPoint, toleranceDistance)) {
	        		loggingSystem.finest(endNodeToString(endNode)+" is within distance of " + endNodeToString(startPoint));
	        		nearbyNodes.add(startPoint);
	        	}
	        	if (!endNode.equals(endPoint) &&
	        			endNode.isWithinDistance(endPoint, toleranceDistance)) {
	        		loggingSystem.finest(endNodeToString(endNode)+" is within distance of " + endNodeToString(endPoint));
	        		nearbyNodes.add(endPoint);
	        	}
	        }
        } finally {
        	featureCollection.close(iter);
        }
        
        return nearbyNodes;
    }

	public static String endNodeToString(Point endNode) {
		return "[FID: '"+endNode.getUserData()+"' Coord: ("
		+endNode.getCoordinate().x+", "
		+endNode.getCoordinate().y+")]";
	}
    
    private Filter getBBoxFilter(Point endNode) {
        return GeometryUtil.getBBoxFilter(this.featureStore, endNode, this.distanceTolerance);
    }
    
    // simple struct combining Point and fid to hash NodeCollection against.
    private class Node {
    	public final Coordinate c;  // use Coordinate.  Point doesn't have hashCode()
    	public final String fid;
    	
    	public Node(Point p, String fid) {
    		this.c = p.getCoordinate();
    		this.fid = fid;
    	}
    	
    	@Override
    	public boolean equals(Object obj) {
    		boolean equals = false;
    		
    		if (obj instanceof Node) {
    			Node q = (Node) obj;
    			equals = c.equals(q.c) && fid.equals(q.fid); 
    		}
    		return equals;
    	}
    	
    	@Override
    	public int hashCode() {
    		// TODO this is a valid hash, but is it a good one?
    		return c.hashCode() + fid.hashCode(); 
    	}
    }
}
