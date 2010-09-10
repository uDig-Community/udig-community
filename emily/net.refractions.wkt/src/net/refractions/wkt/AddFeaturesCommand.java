package net.refractions.wkt;


import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.AbstractCommand;
import net.refractions.udig.project.command.UndoableMapCommand;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;

/**
 * Adds a collection of features
 * 
 */
public class AddFeaturesCommand extends AbstractCommand implements UndoableMapCommand {

    private Collection<SimpleFeature> features;
    private FeatureStore<SimpleFeatureType, SimpleFeature> resource;
    
    private ILayer layer;
    private Set<FeatureId> addedFeatures = null;

    public AddFeaturesCommand( Collection<SimpleFeature> features, ILayer layer ) {
        this.features=features;
        this.layer=layer;
    }

    public Set getAddedFeatures(){
    	return addedFeatures;
    }
    public void run( IProgressMonitor monitor ) throws Exception {
        resource = layer.getResource(FeatureStore.class, monitor);
        
        if( resource == null )
            return;
        FeatureCollection<SimpleFeatureType, SimpleFeature> c=new org.geotools.feature.collection.AdaptorFeatureCollection("addFeatureCollection",resource.getSchema()){

            @Override
            public int size() {
                return 1;
            }

            @Override
            protected Iterator openIterator() {
                return features.iterator();
            }

            @Override
            protected void closeIterator( Iterator close ) {
            }
            
        };
        addedFeatures = new HashSet<FeatureId>();
        List<FeatureId> features = resource.addFeatures(c);
        addedFeatures.addAll(features);
        
    }

    public String getName() {
        return "Add Features Command"; 
    }

    public void rollback( IProgressMonitor monitor ) throws Exception {
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
        Set<FeatureId> fids = new HashSet<FeatureId>();
        for (Iterator<FeatureId> iterator = addedFeatures.iterator(); iterator.hasNext();) {
        	FeatureId fId = (FeatureId) iterator.next();
        	fids.add(fId);
		}
        
		resource.removeFeatures(filterFactory.id(fids));
    }
    
    public Filter getAddFeaturesAsFilter(){
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
        Set<FeatureId> fids = new HashSet<FeatureId>();
        for (Iterator<FeatureId> iterator = addedFeatures.iterator(); iterator.hasNext();) {
        	FeatureId fId = (FeatureId) iterator.next();
        	fids.add(fId);
		}
		return filterFactory.id(fids);
    }

}
