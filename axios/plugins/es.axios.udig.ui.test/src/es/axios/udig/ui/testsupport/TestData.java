package es.axios.udig.ui.testsupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility class that provides test data for unit tests based on a MemoryDataStore
 */
public class TestData {

    public static final String FTYPE_LINES = "Lines";

    public static final String FTYPE_POLYGONS = "Polygons";

    public static final String FTYPE_POINTS = "Points";

    public static final String INTVAL_ATT_NAME = "intVal";
    public static final String NAME_ATT_NAME = "name";
    public static final String GEOM_ATT_NAME = "geom";
    
    private static final Map<String, String> TYPE_DEFS = new HashMap<String, String>();
    static {
        TYPE_DEFS.put(FTYPE_LINES, "name:String,intVal:int,geom:LineString");
        TYPE_DEFS.put(FTYPE_POINTS, "name:String,intVal:int,geom:Point");
        TYPE_DEFS.put(FTYPE_POLYGONS, "name:String,intVal:int,geom:Polygon");
    }

    /**
     * Returns the FeatureStore<SimpleFeatureType, SimpleFeature> for one of the prescribed feature types. Each invocation creates a
     * new one.
     * 
     * @param typeName
     * @return
     * @throws Exception
     */
    public static FeatureStore<SimpleFeatureType, SimpleFeature> featureStore( final String typeName ) throws Exception {
        DataStore store = dataStore();
        FeatureStore<SimpleFeatureType, SimpleFeature> fstore = (FeatureStore<SimpleFeatureType, SimpleFeature>) store.getFeatureSource(typeName);
        return fstore;
    }

    /**
     * Returns a MemoryDataStore holding the test data for the prescribed test FeatureTypes
     * <p>
     * Each invocation creates a new datastore
     * </p>
     * 
     * @return
     * @throws IOException
     * @throws SchemaException
     */
    public static DataStore dataStore() throws Exception {
        MemoryDataStore ds = new MemoryDataStore();
        Collection<SimpleFeature> features = createPointFeatures();
        ds.addFeatures(DataUtilities.reader(features));

        features = createLineFeatures();
        ds.addFeatures(DataUtilities.reader(features));

        features = createPolygontFeatures();
        ds.addFeatures(DataUtilities.reader(features));
        return ds;
    }

    /**
     * Creates a Collection of test Polygos with the following schema
     * <code>"name:String,intVal:int,the_geom:Polygon"</code>.
     * <p>
     * The polygons created are in WGS84 and one is created for every 30 degrees
     * </p>
     * 
     * @return
     * @throws SchemaException
     * @throws IllegalAttributeException
     */
    public static List<SimpleFeature> createPolygontFeatures() throws SchemaException,
            IllegalAttributeException {
    	SimpleFeatureType polygonsType = createType(FTYPE_POLYGONS);

        GeometryFactory gfac = new GeometryFactory();

        List<SimpleFeature> features = new ArrayList<SimpleFeature>();

        int featureCount = 0;
        for( double westBoundLong = -180; westBoundLong < 180; westBoundLong += 30 ) {
            for( double southBoundLat = -90; southBoundLat < 90; southBoundLat += 30 ) {
                Coordinate[] coords = new Coordinate[5];
                coords[0] = new Coordinate(westBoundLong, southBoundLat);
                coords[1] = new Coordinate(westBoundLong, southBoundLat + 30);
                coords[2] = new Coordinate(westBoundLong + 30, southBoundLat + 30);
                coords[3] = new Coordinate(westBoundLong + 30, southBoundLat);
                coords[4] = new Coordinate(westBoundLong, southBoundLat);

                LinearRing shell = gfac.createLinearRing(coords);
                Polygon polygon = gfac.createPolygon(shell, null);
                featureCount++;
                SimpleFeature feature = feature(polygonsType, polygon, featureCount);
                features.add(feature);
            }
        }

        return features;
    }
    
    public static void createIntersectionFeatures(){
//      TODO do it!        
//        inputs
//        
//        clipping
//        POLYGON ((161.4256989363388 107.06583049631936, 217.6781885017735 107.06583049631936, 217.6781885017735 57.84490212656402, 161.4256989363388 57.84490212656402, 161.4256989363388 107.06583049631936))
//        
//        to clip
//        
//        POLYGON ((217.6781885017735 76.83011735489822, 241.58549656708323 76.83011735489822, 241.58549656708323 36.75021853952602, 195.8803487951675 36.75021853952602, 195.8803487951675 57.84490212656402, 217.6781885017735 76.83011735489822))
//        
//        result
//        POLYGON ((217.6781885017735 76.83011735489822, 241.58549656708323 76.83011735489822, 241.58549656708323 36.75021853952602, 195.8803487951675 36.75021853952602, 195.8803487951675 57.84490212656402, 217.6781885017735 57.84490212656402, 217.6781885017735 76.83011735489822))        
    }

    public static SimpleFeatureType createType( final String typeName ) throws SchemaException {
        String typeDef = TYPE_DEFS.get(typeName);
        SimpleFeatureType ftype = DataUtilities.createType(typeName, typeDef);
        String[] properties = {"name", "intVal", "geom"};
        ftype = DataUtilities.createSubType(ftype, properties, DefaultGeographicCRS.WGS84);
        return ftype;
    }

    public static List<SimpleFeature> createLineFeatures() throws SchemaException,
            IllegalAttributeException {
        SimpleFeatureType linesType = createType(FTYPE_LINES);

        GeometryFactory gfac = new GeometryFactory();

        List<SimpleFeature> features = new ArrayList<SimpleFeature>();

        int featureCount = 0;
        for( double westBoundLong = -180; westBoundLong < 180; westBoundLong += 30 ) {
            for( double southBoundLat = -90; southBoundLat < 90; southBoundLat += 30 ) {
                Coordinate[] coords = new Coordinate[2];
                coords[0] = new Coordinate(westBoundLong, southBoundLat);
                coords[1] = new Coordinate(westBoundLong, southBoundLat + 30);

                LineString line = gfac.createLineString(coords);
                featureCount++;
                SimpleFeature feature = feature(linesType, line, featureCount);
                features.add(feature);
            }
        }

        return features;
    }

    public static List<SimpleFeature> createPointFeatures() throws SchemaException,
            IllegalAttributeException {
    	SimpleFeatureType polygonsType = createType(FTYPE_POINTS);

        GeometryFactory gfac = new GeometryFactory();

        List<SimpleFeature> features = new ArrayList<SimpleFeature>();

        int featureCount = 0;
        for( double westBoundLong = -180; westBoundLong < 180; westBoundLong += 30 ) {
            for( double southBoundLat = -90; southBoundLat < 90; southBoundLat += 30 ) {
                Coordinate coord = new Coordinate(westBoundLong, southBoundLat);
                Point point = gfac.createPoint(coord);
                featureCount++;
                SimpleFeature feature = feature(polygonsType, point, featureCount);
                features.add(feature);
            }
        }

        return features;
    }

    /**
     * Creates a Feature for the given FeatureType and geometry.
     * <p>
     * The feature type is required to be <code>name:String,intVal:int,<geometryType>geom</code>
     * name will be <code>featureType.getName() + featureCount</code>. intVal will be
     * <code>featureCount</code>
     * </p>
     * 
     * @param featureType
     * @param geometry
     * @param featureCount
     * @return
     * @throws IllegalAttributeException
     */
    private static SimpleFeature feature( SimpleFeatureType featureType, Geometry geometry, int featureCount )
            throws IllegalAttributeException {
    	SimpleFeature feature = DataUtilities.template(featureType);
        String typeName = featureType.getTypeName();
        feature.setAttribute("name", typeName + featureCount);
        feature.setAttribute("intVal", featureCount);
        feature.setAttribute("geom", geometry);
        return feature;
    }
}
