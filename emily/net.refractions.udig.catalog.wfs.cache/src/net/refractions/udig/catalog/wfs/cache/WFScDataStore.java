package net.refractions.udig.catalog.wfs.cache;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import net.refractions.udig.catalog.URLUtils;

import org.geotools.caching.featurecache.FeatureCache;
import org.geotools.caching.featurecache.FeatureCacheException;
import org.geotools.caching.grid.featurecache.StreamingGridFeatureCache;
import org.geotools.caching.grid.spatialindex.store.BufferedDiskStorage;
import org.geotools.caching.grid.spatialindex.store.DiskStorage;
import org.geotools.caching.grid.spatialindex.store.MemoryStorage;
import org.geotools.caching.spatialindex.Storage;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.WFSServiceInfo;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class WFScDataStore implements WFSDataStore {

    public static final String PAGE_SIZE_DEFAULT = "1000"; //$NON-NLS-1$
    
    private WFSDataStore parent;
    
    //cache from typeName to cache structure
    protected HashMap<String, FeatureSource<SimpleFeatureType, SimpleFeature>> nameToCache = new HashMap<String, FeatureSource<SimpleFeatureType, SimpleFeature>>();
    protected Map<String, Serializable> params;
    
    
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
     */
    public WFScDataStore(WFSDataStore parent, Map<String, Serializable> param){
        this.parent = parent;
        this.params = param;
        try {
            saveCapabilities();
        } catch (Exception e) {
            //each up exception; we don't care do we?
        }
    }
    
    public static String createCapbilitiesDocumentFileName(URL url) {
        String serverURL = url.getHost() + "_" + url.getPort() + "_" + url.getPath(); //$NON-NLS-1$ //$NON-NLS-2$
        serverURL = serverURL.replace('\\', '_');
        serverURL = serverURL.replace('/', '_'); 
        return serverURL + ".cache"; //$NON-NLS-1$
    }   
    
    protected void saveCapabilities() throws Exception{
        if (getCacheType() == WFScServiceImpl.CACHE_DISK){
            String[] types = parent.getTypeNames();
            
            URL url = (URL)params.get(WFSDataStoreFactory.URL.key);
            String dir = getCacheLocation();
            String cachefile = dir + File.separator + createCapbilitiesDocumentFileName(url);
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(cachefile)));
            for( int i = 0; i < types.length; i++ ) {
                bw.write(types[i] + "\r\n"); //$NON-NLS-1$
            }
            
            bw.close();
        }
    }
    /**
     * Gets the cache type from the parameters.
     *
     * @return
     */
    protected String getCacheType(){
        return (String)this.params.get(WFScServiceImpl.CACHE_TYPE_KEY);
    }
    /**
     * Gets the cache location from the parameters
     *
     * @return
     */
    protected String getCacheLocation(){
        return (String)this.params.get(WFScServiceImpl.CACHE_DIR_KEY);
    }
    /**
     * Get the page index size from the parameters
     *
     * @return
     */
    protected Integer getPageSize(){
        return (Integer)this.params.get(WFScServiceImpl.CACHE_PAGE_SIZE_KEY);
    }
    /**
     * Gets the cache size from the parameters. 
     *
     * @return the cache size; null if not specified or not an integer
     */
    protected Integer getCacheSize(){
        try{
            return (Integer)this.params.get(WFScServiceImpl.GRID_CACHE_SIZE_KEY);
        }catch (Exception ex){
            return null;
        }
    }
    
    /**
     * Gets the maximum number of features allowed in the cache.
     * 
     * @return max features or null if not specified
     */
    protected Integer getCacheFeatureSize(){
        try{
            return (Integer)this.params.get(WFScServiceImpl.CACHE_FEATURE_SIZE_KEY);
        }catch (Exception ex){
            return null;
        }
        
    }
    
    public URL getCapabilitiesURL() {
        return parent.getCapabilitiesURL();
    }

    public URL getDescribeFeatureTypeURL( String typeName ) {
        return parent.getDescribeFeatureTypeURL(typeName);
    }

    public String getFeatureTypeAbstract( String typeName ) {
        return parent.getFeatureTypeAbstract(typeName);
    }

    public ReferencedEnvelope getFeatureTypeBounds( String typeName ) {
        return parent.getFeatureTypeBounds(typeName);
    }

    public CoordinateReferenceSystem getFeatureTypeCRS( String typeName ) {
        return parent.getFeatureTypeCRS(typeName);
    }

    public Set<String> getFeatureTypeKeywords( String typeName ) {
        return parent.getFeatureTypeKeywords(typeName);
    }

    public QName getFeatureTypeName( String typeName ) {
        return parent.getFeatureTypeName(typeName);
    }

    public String getFeatureTypeTitle( String typeName ) {
        return parent.getFeatureTypeTitle(typeName);
    }

    public ReferencedEnvelope getFeatureTypeWGS84Bounds( String typeName ) {
        return parent.getFeatureTypeWGS84Bounds(typeName);
    }

    public WFSServiceInfo getInfo() {
        return parent.getInfo();
    }

    public Integer getMaxFeatures() {
        return parent.getMaxFeatures();
    }

    public String getServiceAbstract() {
        return parent.getServiceAbstract();
    }

    public Set<String> getServiceKeywords() {
        return parent.getServiceKeywords();
    }

    public URI getServiceProviderUri() {
        return parent.getServiceProviderUri();
    }

    public String getServiceTitle() {
        return parent.getServiceTitle();
    }

    public String getServiceVersion() {
        return parent.getServiceVersion();
    }

    public boolean isPreferPostOverGet() {
        return parent.isPreferPostOverGet();
    }

    public void setMaxFeatures( Integer maxFeatures ) {
        parent.setMaxFeatures(maxFeatures);
    }

    public void setPreferPostOverGet( Boolean booleanValue ) {
        parent.setPreferPostOverGet(booleanValue);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader( Query arg0,
            Transaction arg1 ) throws IOException {
        return parent.getFeatureReader(arg0, arg1);
    }
    
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter( String arg0,
            Transaction arg1 ) throws IOException {
        return parent.getFeatureWriter(arg0, arg1);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter( String arg0,
            Filter arg1, Transaction arg2 ) throws IOException {
        return parent.getFeatureWriter(arg0, arg1, arg2);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend( String arg0,
            Transaction arg1 ) throws IOException {
        return parent.getFeatureWriterAppend(arg0, arg1);
    }

    public LockingManager getLockingManager() {
        return parent.getLockingManager();
    }



    public String[] getTypeNames() throws IOException {
        return parent.getTypeNames();
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getView( Query arg0 )
            throws IOException, SchemaException {
        return parent.getView(arg0);
    }

    public void updateSchema( String arg0, SimpleFeatureType arg1 ) throws IOException {
        parent.updateSchema(arg0, arg1);
    }

    public void createSchema( SimpleFeatureType arg0 ) throws IOException {
        parent.createSchema(arg0);
    }

    public void dispose() {
        for( Iterator iterator = nameToCache.values().iterator(); iterator.hasNext(); ) {
            FeatureCache type = (FeatureCache) iterator.next();
            if (type != null) type.dispose();
        }
        parent.dispose();
    }

    public List<Name> getNames() throws IOException {
        return parent.getNames();
    }

    public SimpleFeatureType getSchema( Name typeName ) throws IOException {
        //check the cache
        return getSchema(typeName.getLocalPart());
    }

    public SimpleFeatureType getSchema( String typeName ) throws IOException {
        //check the cache
        FeatureSource<SimpleFeatureType, SimpleFeature> cache = nameToCache.get(typeName) ;
        SimpleFeatureType type = null;
        if (cache != null){
            type = cache.getSchema();
        }
        
        if (type == null){
          //pass along to parent
            type = parent.getSchema(typeName);
        }
        return type;
    }
    
    public void updateSchema( Name arg0, SimpleFeatureType arg1 ) throws IOException {
        parent.updateSchema(arg0, arg1);
    }
    
    protected String getCacheFileName(String typeName, String extension){
        String filename = URLUtils.cleanFilename(typeName);
        return getCacheLocation() + File.separator + filename + "." + extension; //$NON-NLS-1$
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource( Name typeName )
            throws IOException {
        return getFeatureSource(typeName.getLocalPart());
    }

    
    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource( String typeName )
            throws IOException {
        try{
            FeatureSource<SimpleFeatureType, SimpleFeature> cachingFS =  nameToCache.get(typeName) ;
            if (cachingFS != null){
                return cachingFS;
            }
           
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = parent.getFeatureSource(typeName);
            cachingFS = makeFeatureCache(fs, typeName);
            
            nameToCache.put(typeName, cachingFS);
           
            return cachingFS;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    protected FeatureSource makeFeatureCache(FeatureSource sourceFeatureSource, String typeName) throws FeatureCacheException{
        FeatureSource cachingFS;
        
        //max out the cache size
        
        int featurecapacity = Integer.MAX_VALUE;
        if (getCacheFeatureSize() != null){
            featurecapacity = getCacheFeatureSize();
        }
        int indexcapacity = 1000;    //size of the number of tiles in the grid feature cache.
        Integer cacheSize = getCacheSize();
        if (cacheSize != null){
            indexcapacity = cacheSize;
        }
        
        if (this.getCacheType().equals(WFScServiceImpl.CACHE_MEMORY)){
            Storage storage = MemoryStorage.createInstance();
            ReferencedEnvelope bnds = getExpandedBounds(sourceFeatureSource);

            if (bnds != null){
                cachingFS = new StreamingGridFeatureCache(sourceFeatureSource, bnds, indexcapacity, featurecapacity, storage);
            }else{
                cachingFS = new StreamingGridFeatureCache(sourceFeatureSource, indexcapacity, featurecapacity, storage);
            }
        }else if (this.getCacheType().equals(WFScServiceImpl.CACHE_DISK)){
            Properties prop = new Properties();
            
            String filename = getCacheFileName(typeName, "cache");             //$NON-NLS-1$
            
            prop.put(DiskStorage.DATA_FILE_PROPERTY, filename);
            if (this.getPageSize() != null){
                prop.put(DiskStorage.PAGE_SIZE_PROPERTY, this.getPageSize().toString());
            }else{
                prop.put(DiskStorage.PAGE_SIZE_PROPERTY, PAGE_SIZE_DEFAULT);
            }
            prop.put( BufferedDiskStorage.BUFFER_SIZE_PROPERTY, "100"); //$NON-NLS-1$

            Storage storage = BufferedDiskStorage.createInstance(prop);
            //Storage storage = DiskStorage.createInstance(prop);
            ReferencedEnvelope bnds = getExpandedBounds(sourceFeatureSource);
            if (bnds != null){
                cachingFS = new StreamingGridFeatureCache(sourceFeatureSource, bnds, indexcapacity, featurecapacity, storage);
            }else{
                cachingFS = new StreamingGridFeatureCache(sourceFeatureSource, indexcapacity, featurecapacity, storage);
            }        
        }else{
            cachingFS = sourceFeatureSource;
        }
        
        return cachingFS;
    }
    
    /**
     * Gets the bounds of a given feature source and expands them by 0.0001%.
     * 
     * <p>This is used when creating grid feature cache to ensure the grid is
     * large enough so that rounding errors don't cause issues.</p>
     *
     * @param fs
     * @return
     */
    private ReferencedEnvelope getExpandedBounds(FeatureSource fs){
        ReferencedEnvelope bnds = null;
        try{
            bnds = fs.getBounds();
            bnds.expandBy(bnds.getWidth() * 0.0001);
        }catch (Exception ex){
            //gobble error
        }
        return bnds;
    }
    
    public void clearCache(Name typeName){
        clearCache(typeName.getLocalPart());
    }
    public void clearCache(String typeName){
        FeatureSource<SimpleFeatureType, SimpleFeature> cache = nameToCache.get(typeName) ;
        
        if (cache instanceof FeatureCache){
            FeatureCache fc = (FeatureCache) cache;
            fc.clear();
        }else{
            //cannot clear do we want to log this?
        }
        
    }
    
}
