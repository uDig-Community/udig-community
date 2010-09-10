package net.refractions.udig.catalog.wfs.cache;

import java.io.IOException;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * An empty feature source.
 * 
 * <p>This feature source throws exception when you perform queries against it.  The
 * purpose of it is to act as an empty feature source when a feature source to the original
 * wfs dataset cannot be acquired.</p>
 * 
 * @author Emily Gouge
 * @since 1.2.0
 */
public class EmptyFeatureSource implements FeatureSource {
 
    private DataAccess parent;
    
    public EmptyFeatureSource( DataAccess parent  ){
        this.parent = parent;
        
    }
    
    public void addFeatureListener( FeatureListener listener ) {
    }

    public ReferencedEnvelope getBounds() throws IOException {
        return null;
    }

    public ReferencedEnvelope getBounds( Query query ) throws IOException {
        return null;
    }

    public int getCount( Query query ) throws IOException {
        return 0;
    }

    public DataAccess getDataStore() {
        return parent;
    }

    public FeatureCollection getFeatures() throws IOException {
        throw new IOException("Error loading features - connection to wfs server broken."); //$NON-NLS-1$
    }

    public FeatureCollection getFeatures( Query query ) throws IOException {
        throw new IOException("Error loading features - connection to wfs server broken."); //$NON-NLS-1$
    }

    public FeatureCollection getFeatures( Filter filter ) throws IOException {
        throw new IOException("Error loading features - connection to wfs server broken."); //$NON-NLS-1$
    }

    public ResourceInfo getInfo() {
        return null;
    }

    public Name getName() {
        return null;
    }

    public QueryCapabilities getQueryCapabilities() {
        return null;
    }

    public FeatureType getSchema() {
        return null;
    }

    public Set getSupportedHints() {
        return null;
    }

    public void removeFeatureListener( FeatureListener listener ) {
    }

}
