/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.linecleaner.cleansing;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.refractions.linecleaner.FeatureUtil;
import net.refractions.linecleaner.GeometryUtil;
import net.refractions.linecleaner.LoggingSystem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Runs through all the nodes in a featurestore, adding nodes at the
 * closest point on all nearby features.  Basically we're adding nodes
 * at strategic points so that they'll be collapsed later by
 * the EndNodesProcessor.
 * 
 * @author myronwu
 */
public class NodeInsertionProcessor extends AbstractProcessor {
    double distanceTolerance;
    NewNodes newNodes;
        
    LoggingSystem loggingSystem = LoggingSystem.getInstance();
    
    /**
     * @param store
     * @param distanceTolerance
     */
    public NodeInsertionProcessor(net.refractions.udig.project.internal.Map map, FeatureStore store, double distanceTolerance) {
    	super(map, store);
        this.distanceTolerance = distanceTolerance;
        this.newNodes = new NewNodes();
    }
    
    /**
     *
     * @param monitor
     * @throws IOException
     */
    protected void runInternal(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {
        if (monitor == null) monitor = new NullProgressMonitor();
        
        this.loggingSystem.setCurrentAction(LoggingSystem.NODE_INSERTION);
        this.loggingSystem.begin();
        
        monitor.beginTask("Node Insertion: ", 2);
        if (monitor.isCanceled()) {
            return;
        }
        pauseIfNecessary(pauseMonitor);
        
        insertNodes(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
        		pauseMonitor);
        
        if (monitor.isCanceled()) {
            return;
        }
        pauseIfNecessary(pauseMonitor);
        
        this.newNodes.commitChanges(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
        		pauseMonitor);
        monitor.done();
        this.loggingSystem.finish();
    }
    
    // debugging method
    /*
    public void go() throws IOException {
        insertNodes();
        this.newNodes.dump();
        this.newNodes.commitChanges();
        
        // dump the nodes we're adding in for debugging purposes
        try {
            this.newNodes.writeShp(new URL("file:///C:/nodes.shp"));
        } catch (Exception e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
    }*/
    
    protected void insertNodes(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {
        monitor.beginTask("", 2 * this.featureStore.getCount(Query.FIDS));
        monitor.subTask("Calculating Nodes");
        
    	String typename = this.featureStore.getSchema().getTypeName();
    	String defaultGeom = this.featureStore.getSchema().getDefaultGeometry().getName();
    	DefaultQuery query = new DefaultQuery(typename, Filter.NONE, new String[] {defaultGeom});
        FeatureIterator i = this.featureStore.getFeatures(query).features();
        try {
            while (i.hasNext()) {
                Feature f = i.next();
                String fid = f.getID();
                Geometry geom = f.getDefaultGeometry();
                processNode(getStartPoint(geom), fid);
                
                monitor.worked(1);
                if (monitor.isCanceled()) {
                    break;
                }
                pauseIfNecessary(pauseMonitor);
                
                processNode(getEndPoint(geom), fid);
                
                monitor.worked(1);
                if (monitor.isCanceled()) {
                    break;
                }
                pauseIfNecessary(pauseMonitor);
            }
        } finally {
            i.close();
            monitor.done();
        }
    }
    
    /**
     *
     * @param node
     * @param fid Fid of the feature that node belongs to
     */
    protected void processNode(Point node, String fid) {
        FeatureIterator i = getNearbyFeatures(node).features();
        try {
            while (i.hasNext()) {
                Feature f = i.next();
                // getNearbyFeatures also returns f itself--exclude.
                if (!f.getID().equals(fid)) {
                    nodeClosestPoint(f, node);
                }
            }
        } finally {
            i.close();
        }
    }
    
    /**
     * Add a node to f at the closest point to node.
     * @param f
     * @param node
     */
    protected void nodeClosestPoint(Feature f, Point node) {
        Coordinate closestPoint = 
            GeometryUtil.getClosestPoint(node.getCoordinate(), f.getDefaultGeometry());

        // The bbox filter seems to bring back some features outside
        // the bbox?  Make a sanity check here to make sure the closest point
        // found is within tolerance.
        Point p = node.getFactory().createPoint(closestPoint);
        if (p.distance(node) < this.distanceTolerance) {
            this.newNodes.insertNode(f, closestPoint);
        }
    }
    
    protected FeatureCollection getNearbyFeatures(Point p) {
        FeatureCollection nearbyFeatures = null;
        String typename = this.featureStore.getSchema().getTypeName();
        String geomName = this.featureStore.getSchema().getDefaultGeometry().getName();
        try {
            nearbyFeatures = this.featureStore.getFeatures(
                    new DefaultQuery(typename, getBBoxFilter(p), new String[] { geomName }));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return nearbyFeatures;
    }
    
    protected Filter getBBoxFilter(Point node) {
        return GeometryUtil.getBBoxFilter(this.featureStore, node, this.distanceTolerance);
    }
    
    protected Point getStartPoint(Geometry g) {
        Coordinate start = g.getCoordinates()[0];
        return g.getFactory().createPoint(start);
    }
    
    protected Point getEndPoint(Geometry g) {
        Coordinate[] coords = g.getCoordinates();
        return g.getFactory().createPoint(coords[coords.length - 1]);
    }
    
    /**
     * Grab a feature and all its attributes from the FeatureStore by fid.  This is
     * necessary since the filters we work with are optimized when bringing back only 
     * fids and geometries.
     * @param fid
     * @return The feature identified by fid.
     * @throws NoSuchElementException
     * @throws IOException
     */
    protected Feature getFeature(String fid)
    throws NoSuchElementException, IOException {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        FidFilter filter = ff.createFidFilter(fid);
        FeatureIterator features = this.featureStore.getFeatures(filter).features();
        try{
            return features.next();
        }finally{
            if( features!=null)
                features.close();
        }
    }
    
    /**
     * Subdivide the feature f at a set of coordinates.
     * @param f
     * @param points
     * @return A FeatureCollection containing all subdivisions of f at the given points.
     * @throws IllegalAttributeException
     */
    protected FeatureCollection subdivide(Feature f, Collection<Coordinate> points)
    throws IllegalAttributeException {
        FeatureCollection fc = FeatureCollections.newCollection();
        LineString line = GeometryUtil.extractLine(f.getDefaultGeometry());
        if (line != null) {
            Collection<LineString> lines = GeometryUtil.subdivide(line, points);
            for (LineString ls: lines) {
                Feature fcopy = FeatureUtil.copy(f);
                // the following is expected by shapefiles
                MultiLineString mls = GeometryUtil.wrapInMultiLineString(ls);
                fcopy.setDefaultGeometry(mls);
                fc.add(fcopy);
            }
        }
        return fc;
    }
    
    /**
     * A class to cache all the nodes we're going to add to each feature.
     * This is necessary because we can't modify a FeatureStore as we traverse it.
     */
    private class NewNodes {
        Map<String, Set<Coordinate>> nodeIndex = 
            new HashMap<String, Set<Coordinate>>();
        
        /**
         * Divide the features at the nodes we've calculated.
         * @param pauseMonitor 
         * @throws IOException
         */
        public void commitChanges(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {
            monitor.beginTask("", nodeIndex.size());
            monitor.subTask("Inserting Nodes");
            
    		String typename = featureStore.getSchema().getTypeName();
    		String defaultGeom = featureStore.getSchema().getDefaultGeometry().getName();
    		Filter fidFilter = FeatureUtil.fidsToFidFilter(this.nodeIndex.keySet());
    		Query query = new DefaultQuery(typename, fidFilter);
            MemoryFeatureIterator iter = new MemoryFeatureIterator(featureStore, map, query);
            try {
            	while (iter.hasNext()) {
            		Feature f = iter.next();
            		String fid = f.getID();
            		Collection<Coordinate> newNodes = this.nodeIndex.get(fid);
            		
                    FeatureCollection fc;
                    try {
                        fc = subdivide(f, newNodes);
                        
                        // Here we pop the first subdivided feature and replace
                        // the original feature with it, then add the rest of
                        // the subdivisions.  Modifying is faster than removing
                        // features.
                        Feature first = pop(fc);
                        modifyGeometry(fid, first.getDefaultGeometry());
                        featureStore.addFeatures(fc.reader());
//                        System.out.print("Replacing " + f.getID() + " ");
//                        FeatureUtil.dumpFeature(f);
//                        System.out.print("with ");
//                        FeatureUtil.dumpFeatureCollection(fc);
                    } catch (IllegalAttributeException e1) {
                        // TODO Handle IllegalAttributeException
                        throw (RuntimeException) new RuntimeException( ).initCause( e1 );
                    }
                    monitor.worked(1);
                    if (monitor.isCanceled()) {
                        break;
                    }
                    pauseIfNecessary(pauseMonitor);
            	}
            } finally {
            	iter.close();
            	monitor.done();
            }
        }
        
        protected void modifyGeometry(String fid, Geometry newGeom)
        throws IOException {
            FilterFactory ff = FilterFactoryFinder.createFilterFactory();
            Filter filter = ff.createFidFilter(fid);
            String geomName = featureStore.getSchema().getDefaultGeometry().getName();
            AttributeType geomType = featureStore.getSchema().getAttributeType(geomName);
            featureStore.modifyFeatures(geomType, newGeom, filter);
        }
        
        protected Feature pop(FeatureCollection fc) {
            FeatureIterator i = fc.features();
            Feature f = null;
            try {
                if (i.hasNext()) {
                    f = i.next();
                    fc.remove(f);
                }
            } finally {
                i.close();
            }
            return f;
        }
        
        /**
         * Record a new node for f.
         * @param f
         * @param node
         */
        public void insertNode(Feature f, Coordinate node) {
            LineString line = GeometryUtil.extractLine(f.getDefaultGeometry());
            if (line != null) {
                getNodes(f.getID()).add(node);
            }
        }
        
        protected Set<Coordinate> getNodes(String fid) {
            if (hasNodes(fid)) {
                return this.nodeIndex.get(fid);
            }
            Set<Coordinate> nodes = new HashSet<Coordinate>();
            this.nodeIndex.put(fid, nodes);
            return nodes;
        }
        
        protected boolean hasNodes(String fid) {
            return this.nodeIndex.containsKey(fid);
        }
        
        // debugging method
        public void dump() {
            System.out.println("Dump:");
            for (Map.Entry<String, Set<Coordinate>> e: nodeIndex.entrySet()) {
                String fid = e.getKey();
                Set<Coordinate> nodes = e.getValue();
                System.out.println(fid);
                for (Coordinate c: nodes) {
                    System.out.println(c);
                }
            }
        }
        
        // debugging method
        public void writeShp(URL url) throws IOException, IllegalAttributeException {
            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            ShapefileDataStore ds = (ShapefileDataStore)factory.createDataStore(url);
            
            FeatureTypeBuilder builder = FeatureTypeFactory.newInstance("test");
            AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom", Point.class);
            FeatureType type;
            try {
                builder.addType(geom);
                type = builder.getFeatureType();
            } catch (SchemaException e1) {
                // TODO Handle SchemaException
                throw (RuntimeException) new RuntimeException( ).initCause( e1 );
            }
                        
            ds.createSchema(type);
            FeatureStore store = (FeatureStore)ds.getFeatureSource();
            FeatureCollection fc = FeatureCollections.newCollection();
            int count = 0;
            GeometryFactory gf = new GeometryFactory();
            for (Map.Entry<String, Set<Coordinate>> e: nodeIndex.entrySet()) {
                Set<Coordinate> nodes = e.getValue();
                for (Coordinate node: nodes) {
                    Feature f = type.create(new Object[] {gf.createPoint(node)}, Integer.toString(count));
                    count++;
                    fc.add(f);
                }
            }
            store.addFeatures(fc.reader());
        }
    }
}
