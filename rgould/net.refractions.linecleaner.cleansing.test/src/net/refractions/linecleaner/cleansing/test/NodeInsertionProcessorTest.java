package net.refractions.linecleaner.cleansing.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import net.refractions.linecleaner.FeatureUtil;
import net.refractions.linecleaner.GeometryUtil;
import net.refractions.linecleaner.cleansing.NodeInsertionProcessor;

import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
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
import org.geotools.resources.TestData;

import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.NonRobustLineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class NodeInsertionProcessorTest extends TestCase {
    public void testProcessor() throws Exception {
        FeatureStore store = (FeatureStore)getDataStore("merged-set10.shp").getFeatureSource();
        FeatureStore copy = FeatureUtil.copyFeatureStore(store, new URL("file:///C:/merged.shp")); 
        NodeInsertionProcessor nip = new NodeInsertionProcessor(copy, 25);
        nip.go();
    }
    
    public void testCrossAndIntersect() throws Exception {
        GeometryFactory gf = new GeometryFactory();
        LineString one = gf.createLineString(new Coordinate[] {new Coordinate(1.0, 0.0), new Coordinate(1.0, 2.0)});
        LineString two = gf.createLineString(new Coordinate[] {new Coordinate(0.0, 1.0), new Coordinate(2.0, 1.0)});
        System.out.println("intersect? " + one.intersects(two));
        System.out.println("cross? " + one.crosses(two));
    }
    
    public void testIntersection() throws Exception {
        PrecisionModel pm = new PrecisionModel(5.5);
        GeometryFactory gf = new GeometryFactory(pm);
        Coordinate start = new Coordinate(1442463.152, 618439.412);
        Coordinate end = new Coordinate(1442412.507, 618448.975);
        pm.makePrecise(start);
        pm.makePrecise(end);
        LineString ls = gf.createLineString(new Coordinate[] {start, end});

        Coordinate node = new Coordinate(1442412.5071333393, 618448.9749748224);
        
        System.out.println(node);
        pm.makePrecise(node);
        System.out.println(node);
        
        assertTrue(ls.intersects(gf.createPoint(node)));
        /*
        System.out.println((int)ls.distance(gf.createPoint(node)) == 0);
        
        Coordinate closestPoint = GeometryUtil.getClosestPoint(node, ls);
        System.out.println(closestPoint);
        System.out.println(closestPoint.distance(node));
        PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
        pm.makePrecise(closestPoint);
        pm.makePrecise(node);
        System.out.println(closestPoint);
        System.out.println(closestPoint.equals2D(node));
        System.out.println(ls.intersects(gf.createPointFromInternalCoord(closestPoint,ls)));
        writeShp(ls, new URL("file:///C:/poop2.shp"));*/
    }
    
    public void testBrokenNodes() throws Exception {
        PrecisionModel pm = new PrecisionModel(5000);
        // nodes to be added in
        Coordinate node1 = new Coordinate(1442415.4605523588, 618448.4172979326);
        Coordinate node2 = new Coordinate(1442490.5728790257, 618365.0399557692);
        Coordinate node3 = new Coordinate(1442412.5071333393, 618448.9749748223);
        Coordinate node4 = new Coordinate(1442419.080023095, 618447.7338543813);
        Coordinate node5 = new Coordinate(1442412.5071333393, 618448.9749748224);
        Coordinate node6 = new Coordinate(1442322.6048408146, 618419.645562501);
        Coordinate node7 = new Coordinate(1442334.6411110885, 618416.4721226477);
        List<Coordinate> nodes = new LinkedList<Coordinate>();
        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        nodes.add(node4);
        nodes.add(node5);
        nodes.add(node6);
        nodes.add(node7);
        
        for (Coordinate c: nodes) {
            pm.makePrecise(c);
        }
        // the linestring we're add the nodes to
        GeometryFactory gf = new GeometryFactory();
        LineString ls = gf.createLineString(new Coordinate[] {
                new Coordinate(1442514.034, 618311.243),
                new Coordinate(1442504.252, 618320.387),
                new Coordinate(1442501.89, 618323.107),
                new Coordinate(1442496.92, 618338.619),
                new Coordinate(1442491.452, 618350.026),
                new Coordinate(1442489.96, 618353.868),
                new Coordinate(1442490.573, 618365.039),
                new Coordinate(1442490.07, 618369.013),
                new Coordinate(1442493.15, 618384.517),
                new Coordinate(1442496.239, 618400.034),
                new Coordinate(1442498.208, 618416.421),
                new Coordinate(1442496.477, 618430.106),
                new Coordinate(1442463.152, 618439.412),
                new Coordinate(1442412.507, 618448.975),
                new Coordinate(1442411.637, 618447.864),
                new Coordinate(1442395.67, 618430.721),
                new Coordinate(1442388.484, 618423.772),
                new Coordinate(1442380.308, 618416.692),
                new Coordinate(1442370.022, 618410.35),
                new Coordinate(1442365.415, 618406.971),
                new Coordinate(1442352.06, 618410.376),
                new Coordinate(1442328.881, 618418.488),
                new Coordinate(1442318.818, 618420.344),
                new Coordinate(1442306.212, 618422.661),
                new Coordinate(1442257.275, 618435.566),
                new Coordinate(1442230.268, 618451.551),
                new Coordinate(1442227.915, 618452.943),
                new Coordinate(1442181.087, 618481.148),
                new Coordinate(1442161.316, 618479.61),
                new Coordinate(1442148.965, 618475.592),
                new Coordinate(1442132.671, 618470.158),
                new Coordinate(1442105.129, 618458.19),
                new Coordinate(1442091.811, 618449.961),
                new Coordinate(1442079.907, 618445.316),
                new Coordinate(1442058.705, 618440.028),
                new Coordinate(1442040.103, 618433.617),
                new Coordinate(1442033.211, 618432.704),
                new Coordinate(1442013.347, 618433.577),
                new Coordinate(1441980.295, 618424.999),
                new Coordinate(1441973.441, 618416.37),
                new Coordinate(1441954.274, 618415.267),
                new Coordinate(1441935.663, 618419.941),
                new Coordinate(1441934.9, 618420.32),
                new Coordinate(1441921.578, 618427.932),
                new Coordinate(1441901.668, 618442.649),
                new Coordinate(1441881.602, 618452.475),
                new Coordinate(1441869.959, 618459.549)
        });
        ls = getPreciseGeometry(ls, pm);
        System.out.println(ls.getNumPoints() + " points");
        System.out.println(ls);
        for (Coordinate c: nodes) {
            System.out.println("Adding node at " + c);
            System.out.println("\tAt index " + GeometryUtil.findInsertionIndex(ls, c));
            System.out.println("\tResult: ");
            System.out.println(GeometryUtil.addVertex(ls,c) + "\n");
        }
        LineString newLine = GeometryUtil.addVertices(ls,nodes);
        System.out.println("total: "+newLine);
        writeShp(newLine, new URL("file:///C:/poop.shp"));
    }
    public LineString getPreciseGeometry(LineString ls, PrecisionModel pm) {
        Coordinate[] coords = ls.getCoordinates();
        for (int i = 0; i < coords.length; i++) {
            pm.makePrecise(coords[i]);
        }
        return ls.getFactory().createLineString(coords);
    }
    
    public void writeShp(LineString ls, URL url) throws IOException, IllegalAttributeException {
        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        ShapefileDataStore ds = (ShapefileDataStore)factory.createDataStore(url);
        
        FeatureTypeBuilder builder = FeatureTypeFactory.newInstance("test");
        AttributeType geom = AttributeTypeFactory.newAttributeType("the_geom", MultiLineString.class);
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
        fc.add(type.create(new Object[] {GeometryUtil.wrapInMultiLineString(ls)}));
        store.addFeatures(fc.reader());
    }
    
    public void testTest() throws Exception {
        Coordinate one = new Coordinate(1455850.041029902, 689795.8186576265);
        Coordinate two = new Coordinate(1456453.526474759, 690079.0873358246);
        Coordinate three = new Coordinate(1457451.1248631962, 690140.6674832591);
        Coordinate four = new Coordinate(1457993.0301606187, 689845.082775574);
        Coordinate five = new Coordinate(1459828.1185541635, 689487.9179204545);
        Coordinate six = new Coordinate(1461416.886357971, 689783.5026281396);
        Coordinate seven = new Coordinate(1462488.3809233294, 690079.0873358246);
        
        GeometryFactory gf = new GeometryFactory();
        LineString ls = gf.createLineString(new Coordinate[] {one,two,three, four, five, six, seven});
        for (LineString line: GeometryUtil.foldCoordinates(ls)) {
            System.out.println(line);
        }
        Coordinate c = new Coordinate(1458297.0723434542, 689785.906780257);
        Point p = gf.createPoint(c);
//        Point q = gf.createPoint(new Coordinate(1460381.3183500767, 689410.1329702612));
        System.out.println(GeometryUtil.getClosestPoint(c, ls));
        //        System.out.println(ls.intersects(q));
        System.out.println(GeometryUtil.findInsertionIndex(ls, c));
//        System.out.println(GeometryUtil.addVertex(ls, p));
    }
    
    public IndexedShapefileDataStore getDataStore(String filename)
    throws Exception {

        File shp = TestData.file( this, filename );
        Map params=new HashMap();
        params.put( IndexedShapefileDataStoreFactory.URLP.key, shp.toURL());
        params.put( IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, new Boolean(true));
        params.put( IndexedShapefileDataStoreFactory.MEMORY_MAPPED.key, new Boolean(false));
        params.put( IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key, IndexedShapefileDataStoreFactory.TREE_GRX);
//      params.put( IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key, IndexedShapefileDataStoreFactory.TREE_QIX);
        
        Logger.getLogger("org.geotools.data").setLevel(Level.WARNING);
        
        IndexedShapefileDataStoreFactory fac=new IndexedShapefileDataStoreFactory();
        return (IndexedShapefileDataStore) fac.createDataStore(params);
    }
}
