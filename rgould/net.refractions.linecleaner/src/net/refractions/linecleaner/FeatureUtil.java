package net.refractions.linecleaner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.function.FilterFunction_envelope;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 * Some useful functions on features and feature collections.
 * </p>
 * @author myronwu
 * @author rgould
 */
public class FeatureUtil {
    /** FeatureUtil MERGE_SOURCE_NAME field: the name of the attribute
     * added to merged featurestores used to indicate the originating file. */
    public static final String MERGE_SOURCE_NAME = "mrgsrc";
    
    /**
     * Returns a fresh copy of a feature. 
     * @param f
     * @return Copy of f.
     * @throws IllegalAttributeException
     */
    public static Feature copy(Feature f) 
    throws IllegalAttributeException { 
        FeatureType type = f.getFeatureType();
        // this may be deprecated, but so is FeatureFactory.create()
        return type.create(f.getAttributes(null));
    }
    
    /**
    * Get a collection of nearby features, where nearby means 
    * @param features FeatureCollection in which to look for nearby features.
    * @param f Look for features near this feature f.
    * @return FeatureCollection of features inside f's bounding box.
    */
   public static FeatureCollection nearbyFeatures(FeatureCollection features, Feature f) {
       FeatureCollection nearbyFeatures;
       try {
           Filter bboxFilter = FeatureUtil.nearish(f);
           nearbyFeatures = features.subCollection( bboxFilter );
       } catch (IllegalFilterException e) {
           nearbyFeatures = new MemoryFeatureCollection(f.getFeatureType());
       }
   
       return nearbyFeatures;
   }

   public static FeatureCollection nearbyFeatureFids(FeatureSource source, Feature f) {
       FeatureCollection nearbyFeatures = null;
       
       String typename = source.getSchema().getTypeName();
       String geomName = source.getSchema().getDefaultGeometry().getName();
       try {
           nearbyFeatures = source.getFeatures(
                   new DefaultQuery(typename, nearish(f), new String[] {geomName}));
       } catch (Exception e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       return nearbyFeatures;
   }
   
   /**
    * Get a filter to find features near f.
    * @param f The feature that you want to find features near.
    * @return The filter that'll return features near f.
    * @throws IllegalFilterException
    */
   public static Filter nearish( Feature f )
   throws IllegalFilterException {
       String geomName = f.getFeatureType().getDefaultGeometry().getName();
       Envelope bounds = f.getBounds();
       
       FilterFactory ff = FilterFactoryFinder.createFilterFactory();

       GeometryFilter bboxFilter = ff.createGeometryFilter(FilterType.GEOMETRY_BBOX);                                                           
       AttributeExpression geomExpr = ff.createAttributeExpression( geomName );
       
       bboxFilter.addLeftGeometry(ff.createBBoxExpression(bounds));
       FilterFunction_envelope ff_env = new FilterFunction_envelope();
       ff_env.setArgs(new Expression[]{geomExpr});
       bboxFilter.addRightGeometry(ff_env);
       
       Filter fidFilter = ff.createFidFilter(f.getID());
       return bboxFilter.and(fidFilter.not());
   }

    /**
     * Get a filter that will fetch all features strictly within an envelope.
     * @param geomName The name of the geometry attribute to be used in the filter.
     * @param bounds The envelope inside which to find features.
     * @return A filter that returns features inside bounds.
     * @throws IllegalFilterException
     */
    public static Filter withinBbox(String geomName, Envelope bounds)
    throws IllegalFilterException {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();            
        GeometryFilter bboxFilter = ff.createGeometryFilter(FilterType.GEOMETRY_BBOX);                                                           
        AttributeExpression geomExpr = ff.createAttributeExpression( geomName );
        
        bboxFilter.addLeftGeometry(ff.createBBoxExpression(bounds));
        bboxFilter.addRightGeometry(geomExpr);
        return bboxFilter;
    }

    public static Filter intersectsGeom(String geomName, Geometry g)
    throws IllegalFilterException {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryFilter intersectFilter = ff.createGeometryFilter(FilterType.GEOMETRY_INTERSECTS);
        AttributeExpression geomExpr = ff.createAttributeExpression(geomName);

        intersectFilter.addLeftGeometry(ff.createLiteralExpression(g));
        intersectFilter.addRightGeometry(geomExpr);
        return intersectFilter;
    }
    
    public static Filter distanceToGeom(String geomName, Geometry g, double distance) 
    throws IllegalFilterException {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        GeometryDistanceFilter distanceFilter = ff.createGeometryDistanceFilter(FilterType.GEOMETRY_DWITHIN);
        AttributeExpression geomExpr = ff.createAttributeExpression(geomName);

        distanceFilter.addLeftGeometry(ff.createLiteralExpression(g));
        distanceFilter.addRightGeometry(geomExpr);
        distanceFilter.setDistance(distance);
        return distanceFilter;
    }
    
    /**
     * Retype all the features in a FeatureCollection.
     * @param ft The type we want to expand fc's features to.
     * @param fc The FeatureCollection to retype.
     * @return The retyped features.
     * @see org.geotools.data.DataUtilities#reType(org.geotools.feature.FeatureType, org.geotools.feature.Feature) reType
     * @throws IllegalAttributeException
     */
    @SuppressWarnings("unchecked")
    public static FeatureCollection reType(FeatureType ft, FeatureCollection fc)
    throws IllegalAttributeException {
        FeatureCollection out = new MemoryFeatureCollection(ft);
        
        FeatureIterator i = fc.features();
        try {
            while (i.hasNext()) {
                Feature f = i.next();
                out.add(DataUtilities.reType(ft, f));
            }
        } finally {
            i.close();
        }
        
        return out;
    }
    
    /**
     * Merge two feature types to produce a new FeatureType.  Attributes are unioned.
     * @param t First FeatureType to merge.
     * @param s Second FeatureType to merge.
     * @param name Name of the new FeatureType
     * @return FeatureType that comprises t and s.
     * @throws SchemaException
     */
    public static FeatureType mergeFeatureTypes(FeatureType t, FeatureType s, String name)
    throws SchemaException {
        FeatureTypeBuilder builder = FeatureTypeFactory.newInstance(name);
        builder.addTypes(mergeAttributeTypes(t,s));
        FeatureType ft = builder.getFeatureType();
        
        return ft;
    }
    
    // generalize an attribute type to have values of type string 
    private static AttributeType generalizeType(AttributeType t) {
        return AttributeTypeFactory.newAttributeType(t.getName(), String.class, true);
    }
    
    /**
     * Merge the two attribute type sets of two feature types.  In general the attribute
     * types are unioned, and in cases where two attribute types share a name but differ
     * in class type, we generalize them to be String.
     * @param t
     * @param s
     * @return
     */
    public static AttributeType[] mergeAttributeTypes(FeatureType t, FeatureType s) {
        Set<String> done = new HashSet<String>();
        Vector<AttributeType> types = new Vector<AttributeType>();
        
        // add in all attribute types of t
        for (AttributeType type: t.getAttributeTypes()) {
            AttributeType ttype = s.getAttributeType(type.getName());
            
            if (ttype != null && !ttype.getType().equals(type.getType())) {
                types.add(generalizeType(type));
                done.add(type.getName());
            } else {
                types.add(type);
                done.add(type.getName());
            }
        }
        
        // add in all attribute types of s not found in t
        for (AttributeType type: s.getAttributeTypes()) {
            if (!done.contains(type.getName())) {
                types.add(type);
            }
        }
        return types.toArray(new AttributeType[types.size()]);
    }
    
    /**
     * Add an attribute to a FeatureType.
     * @param schema
     * @param type
     * @return New FeatureType with attribute type added to it.
     * @throws SchemaException
     */
    public static FeatureType addAttribute( FeatureType schema, AttributeType type )
    throws SchemaException {
        try {
            FeatureType newFeatureType;
            if (schema.find(type.getName()) == -1) {
                FeatureTypeBuilder builder = FeatureTypeFactory.newInstance(schema.getTypeName());
                builder.importType(schema);
                builder.addType(type);
                newFeatureType = builder.getFeatureType();
            } else {
                newFeatureType = schema;
            }
            return newFeatureType;
        } catch (FactoryConfigurationError e) {
            throw new SchemaException("Unable to add attribute to Feature.");
        }
    }

    /**
     * Adds a source file attribute to each feature in a FeatureCollection.
     * @param fc
     * @param source The value to set the attribute to.
     * @return The features with the source file attribute added in.
     * @throws SchemaException
     * @throws IllegalAttributeException
     */
    public static FeatureCollection addMergeSource(FeatureCollection fc, String source)
    throws SchemaException, IllegalAttributeException {
        FeatureType schema = fc.getSchema();
        AttributeType mergeSourceAttribute = 
            AttributeTypeFactory.newAttributeType(MERGE_SOURCE_NAME, 
                    String.class, true, 50, "");
        FeatureType newType = addAttribute(schema, mergeSourceAttribute);
        
        FeatureCollection result = new MemoryFeatureCollection(newType);
        FeatureIterator i = fc.features();
        try {
            while (i.hasNext()) {
                Feature f = i.next();
                Feature newFeature = DataUtilities.reType(newType, f);
                if (f.getAttribute(MERGE_SOURCE_NAME) == null) {
                    newFeature.setAttribute(MERGE_SOURCE_NAME, source);
                    result.add(newFeature);
                } else {
                    result.add(f);
                }
            }
        } finally {
            i.close();
        }
        return result;
    }
    
    /**
     *
     * @param store
     * @param fids
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    private static void removeFeatures( FeatureStore store, Collection<String> fids ) throws FactoryConfigurationError, IOException {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        FidFilter filter = ff.createFidFilter();
        for (String fid: fids) {
            filter.addFid(fid);
        }
        store.removeFeatures(filter);
    }

    public static void dumpFeature(Feature f) {
        FeatureType ft = f.getFeatureType();
        System.out.println(f.getDefaultGeometry());
        /*
        for (AttributeType at: ft.getAttributeTypes()) {
            String name = at.getName();
            System.out.print(name + ": " + f.getAttribute(name));
            System.out.println("");
        }*/
    }
    
    public static void dumpFeatureCollection( FeatureCollection fc ) {
        FeatureIterator k = fc.features();
        try {
            while (k.hasNext()) {
                Feature f = k.next();
                dumpFeature(f);
            }
        } finally {
            k.close();
        }
    }
    
    public static void removeFeature(FeatureStore store, Feature f)
    throws IOException {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        FidFilter filter = ff.createFidFilter();
        filter.addFid(f.getID());
        store.removeFeatures(filter);
    }
    
    public static void removeFeaturesByFid(FeatureStore store, Collection<String> fids)
    throws IOException {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        FidFilter filter = ff.createFidFilter();
        for (String fid: fids) {
            filter.addFid(fid);
        }
        store.removeFeatures(filter);
    }
    
    public static void removeFeatures(FeatureStore store, FeatureCollection features)
    throws IOException {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        FidFilter filter = ff.createFidFilter();
        filter.addAllFids(getFids(features));
        store.removeFeatures(filter);
    }
    
    public static FeatureStore copyFeatureStore(FeatureStore source, URL destination)
    throws IOException {
        IndexedShapefileDataStore ds = makeShapefileDataStore(destination, false);
        ds.createSchema(source.getSchema());
        FeatureStore store = (FeatureStore)ds.getFeatureSource();
        store.addFeatures(source.getFeatures().reader());
        return store;
    }
    
    public static void copyShapeFile(File source, File destination)
    throws IOException {
        String sourceNoExt = removeExtension(source.getAbsolutePath());
        String destNoExt = removeExtension(destination.getAbsolutePath());
        File dbfInFile = new File(sourceNoExt + ".dbf");
        File shxInFile = new File(sourceNoExt + ".shx");
        File dbfOutFile = new File(destNoExt + ".dbf");
        File shxOutFile = new File(destNoExt + ".shx");
        copyFile(source, destination);
        copyFile(dbfInFile, dbfOutFile);
        copyFile(shxInFile, shxOutFile);
    }
    
    protected static String removeExtension(String filename) {
        int dotPlace = filename.lastIndexOf ( '.' );
        return dotPlace >= 0 ? filename.substring(0, dotPlace) : filename;
    }
    
    protected static void copyFile(File source, File destination)
    throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);
    
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    
    /**
     * Merge together two FeatureStores.  Each feature will have a new attribute added to
     * it indicating its source.
     * @param one FeatureStore number one.
     * @param labelOne The value for the source file attribute for features in FeatureStore one.
     * @param two FeatureStore number two.
     * @param labelTwo The value for the source file attribute for features in FeatureStore two.
     * @param url The URL of where to store the new file.
     * @return The new FeatureStore consisting of features from FeatureStores one and two. 
     * @throws IOException
     * @throws MalformedURLException
     */
    public static FeatureStore mergeFeatureStores(FeatureStore one, String labelOne, FeatureStore two, String labelTwo, URL url)
    throws IOException {
        try {
            FeatureType type =
                mergeFeatureTypes(one.getSchema(), two.getSchema(), one.getSchema().getTypeName());
            type = addMergeSource(type);
            
            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            // Work on a temporary file.  This is in case the destination is also
            // a featurestore to be merged in; we can't overwrite before we read
            // the features in.
            File tmp = File.createTempFile("udigLineCleaner", ".shp");
            IndexedShapefileDataStore ds = makeShapefileDataStore(tmp.toURL(), false);
            ds.createSchema(type);
            FeatureStore store = (FeatureStore)ds.getFeatureSource();
            store.addFeatures(makeMergeFeatureReader(one, type, labelOne));
            store.addFeatures(makeMergeFeatureReader(two, type, labelTwo));

            FeatureStore copy = copyFeatureStore(store, url);
            tmp.delete();
            return copy;

//            String file = url.getFile();
//            copyShapeFile(tmp, new File(file));
//            return openShapefileFeatureStore(file);
        } catch (Exception e) {
            // TODO Handle Exception
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
    }
    
    public static FeatureType mergeFeatureTypes(List<FeatureStore> stores)
    throws SchemaException {
        FeatureType type = stores.get(0).getSchema();
        int size = stores.size();
        for (int i = 1; i < size; i++) {
            type = mergeFeatureTypes(type, stores.get(i).getSchema(), type.getTypeName());
        }
        return type;
    }
    
    public static FeatureStore mergeFeatureStores(List<FeatureStore> storesToMerge, URL shpFileURL) 
    throws Exception {
        try {
            FeatureType type = addMergeSource(mergeFeatureTypes(storesToMerge));

            ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
            // Work on a temporary file.  This is in case the destination is also
            // a featurestore to be merged in; we can't overwrite before we read
            // the features in.
            File tmp = File.createTempFile("udigLineCleaner", ".shp");
            IndexedShapefileDataStore ds = makeShapefileDataStore(tmp.toURL(), false);
            ds.createSchema(type);
            FeatureStore store = (FeatureStore)ds.getFeatureSource();
            for (FeatureStore fs: storesToMerge) {
                store.addFeatures(makeMergeFeatureReader(fs, type, fs.getSchema().getTypeName()));
            }

//            String file = shpFileURL.getFile();
//            copyShapeFile(tmp, new File(file));
//            return openShapefileFeatureStore(file);
            FeatureStore copy = copyFeatureStore(store, shpFileURL);
            tmp.delete();
            return copy;
        } catch (Exception e) {
            // TODO Handle Exception
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
    }
    
    public static FeatureStore openShapefileFeatureStore(String filename) throws Exception {
        IndexedShapefileDataStore ds = openShapefileDataStore(filename);
        return (FeatureStore) ds.getFeatureSource();
    }
    
    public static IndexedShapefileDataStore openShapefileDataStore(String filename) throws Exception {
        File shp = new File(filename);
        return makeShapefileDataStore(shp.toURL(), true);
    }

    public static IndexedShapefileDataStore makeShapefileDataStore(URL url, boolean spatial_index) throws IOException {
        Map params = new HashMap();
        params.put( IndexedShapefileDataStoreFactory.URLP.key, url);
        params.put( IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, new Boolean(spatial_index));
//        params.put( IndexedShapefileDataStoreFactory.MEMORY_MAPPED.key, new Boolean(false));
//        params.put( IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key, IndexedShapefileDataStoreFactory.TREE_GRX);
        IndexedShapefileDataStoreFactory fac = new IndexedShapefileDataStoreFactory();
        return (IndexedShapefileDataStore) fac.createDataStore(params);  
    }
    
    /**
     *
     * @param type
     * @return
     * @throws SchemaException
     */
    private static FeatureType addMergeSource( FeatureType type ) throws SchemaException {
        if (type.getAttributeType(MERGE_SOURCE_NAME) == null) {
            AttributeType mergeSourceAttribute = 
                AttributeTypeFactory.newAttributeType(MERGE_SOURCE_NAME, 
                        String.class, true, 50, "");
            type = addAttribute(type, mergeSourceAttribute);
        }
        return type;
    }
    
    private static FeatureReader makeMergeFeatureReader(FeatureStore source, final FeatureType type, final String label)
    throws IOException {
        final FeatureReader reader = source.getFeatures().reader();
        return new FeatureReader() {
            public FeatureType getFeatureType() {
                return reader.getFeatureType();
            }

            public Feature next()
                throws IOException, IllegalAttributeException,
                    NoSuchElementException {
                Feature f = reader.next();
                f = DataUtilities.reType(type, f);
                if (f.getAttribute(MERGE_SOURCE_NAME) == null) {
                    f.setAttribute(MERGE_SOURCE_NAME, label);
                }
                return f;
            }

            public boolean hasNext() throws IOException {
                return reader.hasNext();
            }
            
            public void close() throws IOException {
                reader.close();
            }
        };
    }
    
    /**
     *
     * @param one
     * @param label
     * @param type
     * @param store
     * @throws IOException
     * @throws IllegalAttributeException
     */
    private static void copyFeatures( FeatureSource source, FeatureStore destination, String label )
    throws IOException, IllegalAttributeException {
        FeatureIterator i = source.getFeatures().features();
        FeatureType type = destination.getSchema();
        try {
            while (i.hasNext()) {
                Feature f = i.next();
                f = DataUtilities.reType(type, f);
                if (f.getAttribute(MERGE_SOURCE_NAME) == null) {
                    f.setAttribute(MERGE_SOURCE_NAME, label);
                }
                destination.addFeatures(makeReader(f));
            }
        } finally {
            i.close();
        }
    }
    
    private static void copyFeatures( FeatureIterator i, FeatureStore destination, String label )
    throws IOException, IllegalAttributeException {
        FeatureType type = destination.getSchema();
        try {
            while (i.hasNext()) {
                Feature f = i.next();
                f = DataUtilities.reType(type, f);
                if (f.getAttribute(MERGE_SOURCE_NAME) == null) {
                    f.setAttribute(MERGE_SOURCE_NAME, label);
                }
                destination.addFeatures(makeReader(f));
            }
        } finally {
            i.close();
        }
    }
    
    public static FeatureReader makeReader(final Feature f) {
        return new FeatureReader() {
            public FeatureType getFeatureType() {
                return f.getFeatureType();
            }

            boolean hasNext = true;

            public Feature next()
                throws IOException, IllegalAttributeException,
                    NoSuchElementException {
                hasNext = false;
                return f;
            }

            public boolean hasNext() throws IOException {
                return hasNext;
            }

            public void close() throws IOException {
                //do nothing.
            }
        };
    }
    
    /**
     * Extracts a collection of geometries from a collection of features.
     * @param features
     * @return Geometries
     */
    public static Collection<Geometry> extractGeometries(Collection<Feature> features) {
        List<Geometry> result = new LinkedList();
        for (Feature f: features) {
            result.add(f.getDefaultGeometry());
        }
        return result;
    }

    /**
     * 
     * @param store
     * @return Does store have a merge source attribute?
     */
    public static boolean hasMergeSourceAttribute(FeatureStore store) {
        return store.getSchema().getAttributeType(FeatureUtil.MERGE_SOURCE_NAME) != null;
    }
    
    /**
     * Filters a set of features to ensure they connect with one another.  Any
     * feature that doesn't connect to another through its two end points is dropped.
     * @param f Feature guaranteed to be in the result set.
     * @param features
     * @return A collection of connected features.
     */
    public static Collection<Feature> filterByConnectivity(Feature f, Collection<Feature> features) {
        List<Feature> result = new LinkedList<Feature>();
        Collection<Geometry> geometries = extractGeometries(features);
        
        geometries.add(f.getDefaultGeometry());
        for (Feature g: features) {
            if (GeometryUtil.connects(g.getDefaultGeometry(), geometries)) {
                result.add(g);
            }
        }
        return result;
    }
    
    public static Collection<Feature> filterZeroLengthLines(Collection<Feature> features) {
        List<Feature> result = new LinkedList<Feature>();
        
        for (Feature f: features) {
            if (f.getDefaultGeometry().getLength() != 0) {
                result.add(f);
            }
        }
        return result;
    }
    
    /**
     *
     * @param features
     * @return fids of features in the feature collection.
     */
    public static Collection<String> getFids(FeatureCollection features) {
        Collection<String> fids = new HashSet<String>();
        FeatureIterator i = features.features();
        try {
            while (i.hasNext()) {
                fids.add(i.next().getID());
            }
        } finally {
            i.close();
        }
        return fids;
    }
    
    public static Filter fidsToFidFilter(Collection<String> fids) {
    	FilterFactory ff = FilterFactoryFinder.createFilterFactory();
    	FidFilter filter = ff.createFidFilter();
    	filter.addAllFids(fids);
    	return filter;
    }
}
