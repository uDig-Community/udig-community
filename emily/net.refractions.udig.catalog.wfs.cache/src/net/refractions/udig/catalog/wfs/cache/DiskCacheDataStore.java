package net.refractions.udig.catalog.wfs.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.caching.featurecache.FeatureCacheException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.WFSServiceInfo;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * An extension of the WFScDataStore to be used when you can't connect
 * to the WFS Service to create a WFScDataStore.  
 * <p>
 * This datastore checks for cached features on disk and creates the
 * feature sources necessary to use these cached features.
 * </p> 
 * 
 * @author Emily Gouge
 * @since 1.2.0
 */
public class DiskCacheDataStore extends WFScDataStore {

    public static final String PAGE_SIZE_DEFAULT = "1000"; //$NON-NLS-1$
    
    /**
     * The connection parameters for a WFScDataStore include:
     * <li>All parameters for a WFSDataStore</li>
     * <li>WFScServiceImpl.CACHE_TYPE_KEY - the type of cache</li>
     * <li>WFScServiceImpl.CACHE_DIR_KEY - the disk location (for disk cache)</li>
     * <li>WFScServiceImpl.CACHE_PAGE_SIZE_KEY - the page size of the disk cache (optional)</li>
     * <li>WFScServiceImpl.GRID_CACHE_SIZE_KEY - the size of the grid cache (optional)</li>
     * 
     * @param parent
     * @param param
     * @throws IOException 
     */
    public DiskCacheDataStore(Map<String, Serializable> param) throws IOException{
        super(null, param);
        computeInfoFromCache();
    }
    
    /**
     * Reads the cached features types.
     *
     * @throws IOException
     */
    private void computeInfoFromCache() throws IOException{
        //read the directory from the list of files and associated types
        String cacheDir = getCacheLocation();
        
        URL url = (URL)super.params.get(WFSDataStoreFactory.URL.key);
        String capFileName = createCapbilitiesDocumentFileName(url);
        String capFile = cacheDir + File.separator + capFileName;
        
        //read the capfile
        HashSet<String> types = readCapFile(capFile);
        //init types
        for( Iterator<String> iterator = types.iterator(); iterator.hasNext(); ) {
            String typeName = (String) iterator.next();
            File myfile = new File(getCacheFileName(typeName, "cache")); //$NON-NLS-1$
            if (myfile.exists()){
                //we have cached stuff for this layer so show it; otherwise there is no point??
                nameToCache.put(typeName, null);
            }
        }
    }

    private HashSet<String> readCapFile(String fileName) throws IOException{
       BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
       String line = null;
       HashSet<String> types = new HashSet<String>();
       
       while ((line = reader.readLine()) != null){
           types.add(line.trim());
       }
       reader.close();
       
       return types;
       
    }
    
    
    @Override
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader( Query query,
            Transaction transaction ) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource( String typeName )
            throws IOException {
        FeatureSource fs = this.nameToCache.get(typeName);
        if (fs == null){
            try{
                fs = makeFeatureCache(typeName);
                this.nameToCache.put(typeName, fs);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return fs;
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter( String typeName,
            Transaction transaction ) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter( String typeName,
            Filter filter, Transaction transaction ) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend( String typeName,
            Transaction transaction ) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockingManager getLockingManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleFeatureType getSchema( String typeName ) throws IOException {
        return getFeatureSource(typeName).getSchema();
    }

    @Override
    public String[] getTypeNames() throws IOException {
        Set<String> t = nameToCache.keySet();
        String[] types = new String[t.size()];
        t.toArray(types);
        return types;
    }

    @Override
    public FeatureSource<SimpleFeatureType, SimpleFeature> getView( Query query )
            throws IOException, SchemaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSchema( String typeName, SimpleFeatureType featureType ) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createSchema( SimpleFeatureType featureType ) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
    }

    @Override
    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource( Name typeName )
            throws IOException {
        return getFeatureSource(typeName.getLocalPart());
    }

    @Override
    public WFSServiceInfo getInfo() {
        return new WFSServiceInfo(){

            @Override
            public String getVersion() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public Set<String> getKeywords() {
                return null;
            }

            @Override
            public URI getPublisher() {
                return null;
            }

            @Override
            public URI getSchema() {
                return null;
            }

            @Override
            public URI getSource() {
                return null;
            }

            @Override
            public String getTitle() {
                return null;
            }
            
        };
    }

    @Override
    public List<Name> getNames() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleFeatureType getSchema( Name name ) throws IOException {
        return getSchema(name.getLocalPart());
    }

    @Override
    public void updateSchema( Name typeName, SimpleFeatureType featureType ) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    private FeatureSource makeFeatureCache(String typeName) throws FeatureCacheException{
        return super.makeFeatureCache(new EmptyFeatureSource(this), typeName);
    }
}