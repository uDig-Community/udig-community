/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.wps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.opengis.ows11.KeywordsType;
import net.opengis.ows11.LanguageStringType;
import net.opengis.wps10.WPSCapabilitiesType;
import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.internal.AddReshapedToMap;
import net.refractions.udig.wps.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.EList;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Parameter;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Provides miscellaneous utility methods for use with WPS.
 *
 * @author gdavis, Refractions Research Inc
 * @author Lucas Reed, Refractions Research Inc
 */
public class WPSUtils {
    /**
     * Utility method to go through a WPS capabilities document and
     * get all the keywords within the ELists.
     * 
     * @param caps WPS capabilities doc
     * @param keywords String List to add the keywords to (creates one if null is passed)
     * @return the String List with keywords added
     */
    public static List<String> getKeywords( WPSCapabilitiesType caps, List<String> keywords ) {
        if (keywords == null) {
            keywords = new ArrayList<String>();
        }
        if (caps.getServiceIdentification().getKeywords() != null) {
            // do the painful process of converting an EList of ELists into a 
            // single List of Strings (or just add each one manually)
            EList<KeywordsType> eKeywords = caps.getServiceIdentification().getKeywords();
            Iterator<KeywordsType> iterator = eKeywords.iterator();
            while (iterator.hasNext()) {
                KeywordsType next = iterator.next();
                EList<LanguageStringType> eKeywords2 = next.getKeyword();
                Iterator<LanguageStringType> iterator2 = eKeywords2.iterator();
                while (iterator2.hasNext()) {
                    LanguageStringType next2 = iterator2.next();
                    if (next2.getValue() != null && next2.getValue().length() != 0) {
                        keywords.add(next2.getValue());
                    }
                }
            }
            //keywordsFromWPS.addAll(Arrays.asList(caps.getServiceIdentification().getKeywords()));
        }
        return keywords;
    }

    /**
     * Creates a new scratch layer and adds a JTS Geometry to the layer
     *
     * @param geom
     * @param crs
     * @param monitor
     * @throws Exception
     */
    public static void createScratchLayer(final Geometry geom, CoordinateReferenceSystem crs,
    		final IProgressMonitor monitor, final int layerCount) throws Exception {
    	if (null == crs) {
    		crs = DefaultGeographicCRS.WGS84;
    	}

    	// Create SimpleFeatureType
	    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
	    builder.setCRS(crs);
	    builder.add("geometry", Geometry.class);	//$NON-NLS-1$
	    builder.setName(new String(Messages.WPSExecute_scratchLayerBase+layerCount));
	    SimpleFeatureType featureType = builder.buildFeatureType();

	    // Create SimpleFeature from SimpleFeatureType
	    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
	    featureBuilder.add(geom);
	    SimpleFeature feature = featureBuilder.buildFeature("simple_feature");	//$NON-NLS-1$

	    WPSUtils.createScratchLayer(feature, monitor);
    }

    /**
     * Creates a new scratch layer and adds a Feature to the layer
     *
     * @param feature
     * @param monitor
     * @throws Exception
     */
    private static void createScratchLayer(final SimpleFeature feature,
    		final IProgressMonitor monitor)
    	throws Exception {
    	// Add FeatureType to catalogue and get IGeoResource
	    ICatalog     catalogue   = CatalogPlugin.getDefault().getLocalCatalog();
	    IGeoResource geoResource = catalogue.createTemporaryResource(feature.getType());

    	// Add Feature to IGeoResource
	    FeatureStore<SimpleFeatureType, SimpleFeature> store = geoResource.resolve(
                FeatureStore.class, SubMonitor.convert(monitor,
                "Create scratch space", 10));//$NON-NLS-1$
	    store.addFeatures(DataUtilities.collection(feature));

	    // Add IGeoResource to Map
	    AddReshapedToMap adder = new AddReshapedToMap();
	    adder.execute(null, geoResource);

	    return;
    } 

    /**
     * Checks a map of values to Java type correctness against the process parameter data.
     * Returns subset of input map where elements are invalid.  This is intended to be
     * called with data provided from a generated GUI form, so number and existence should
     * be correct.
     *
     * 0: Required input key does not exist.
     * 1: Input value is null but required.
     * 2: Minimum input check.
     * 3: Maximum input check.
     *
     * @param processParameters
     * @param inputs
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object>       checkProcessInputs(
    		final Map<String, Parameter<?>> processParameters,
    		final Map<String, Object>       inputs) {
        
    	Map<String, Object> invalid = WPSUtils.deepCopy(inputs);
    	
    	LOOP: for(String processInputName : processParameters.keySet()) {
            if (false == invalid.containsKey(processInputName)) {
                invalid.put(processInputName, null);	// if missing input
                continue;
            }

            Parameter<?> param = processParameters.get(processInputName);

            boolean isValid = true;
            
            if (invalid == null) {
                isValid = false;
            }
            
            if (invalid.get(processInputName) instanceof Map) {
	            Map<String, Object> values = (Map<String, Object>)invalid.get(processInputName);
	            
	            // count number of non-null values
	            int count = 0;
	            List<String> nonNulls = new ArrayList<String>();
	            for(String valueKey : values.keySet()) {
	            	if (values.get(valueKey) != null) {
	            	    count++;
	            	    nonNulls.add(valueKey);
	            	}
	            }
	            
	            // check that the required number of params is valid
                if (values.size() < param.minOccurs || count < param.minOccurs) {
                    //System.err.println(Messages.bind(Messages.WPSExecute_tooFewParameters, processInputName, param.minOccurs));
                    isValid = false;
                }
                if (values.size() > param.maxOccurs || count > param.maxOccurs) {
                    //System.err.println(Messages.bind(Messages.WPSExecute_tooGreatParameters, processInputName, param.maxOccurs));
                    isValid = false;
                }	
                
                // now remove all the non-null values (since they are valid)
                for (String valueKey : nonNulls) {
                    values.remove(valueKey);
                }
            }

            // only remove the entry if it is valid
            if (isValid) {
                invalid.remove(processInputName);
            }
        }

    	return invalid;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopy(final Map<String, Object> inputs) {
    	Map<String, Object> copy = new HashMap<String, Object>();

    	for(String key : inputs.keySet()) {
    		if (inputs.get(key) instanceof Map) {
    			copy.put(key, WPSUtils.deepCopy((Map<String, Object>)inputs.get(key)));
    		} else {
    			copy.put(key, inputs.get(key));
    		}
    	}

    	return copy;
    }
    
    /**
     * Get a feature from the given layer.  Try and get the first selected
     * feature, return null if there isn't one
     *
     * @param layer
     * @return first matching selected feature
     */
    public static SimpleFeature getSelectedFeatureFromLayer(ILayer layer) {
        IGeoResource geoResource = layer.getGeoResource(FeatureSource.class);
        if (geoResource != null) {
            FeatureSource featuresource;
            try {
                featuresource = geoResource.resolve(FeatureSource.class, null);
                if (featuresource != null) {
                    Query query = layer.getQuery(true);
                    FeatureCollection features = featuresource.getFeatures(query);
                    if (features != null) {
                        FeatureIterator<SimpleFeature> iter = features.features();
                        try {
                            if (iter.hasNext()) {
                                return (SimpleFeature) iter.next();
                            }
                        }
                        finally {
                            iter.close();
                        }
                    }
                }
            } catch (IOException e) {
                return null;
            }
        }
        
        return null;
    }
    /**
     * Unpack the gathered input parameters into a flat map of values.
     * <p>
     * This is a temporary workaround (I hope); the map is not being created
     * in the manner expected by the process api (which expects key/value pairs).
     * This method goes through and "unpacks" values which are themselves a map;
     * if and only if the nested map contains a single entry which is the key/value pair
     * being collected.
     * 
     * @param paramValues
     * @return
     */
    public static Map<String, Object> toInput( Map<String, Object> paramValues ) {
        Map<String,Object> input = new HashMap<String, Object>();
        for( Entry<String,Object> entry : paramValues.entrySet() ){
            String key = entry.getKey();
            Object value = entry.getValue();
            if( value instanceof Map ){
                Map<?,?> map = (Map<?,?>) entry.getValue();
                if( map.size() == 1 && map.containsKey( key ) ){
                    value = map.get(key);
                }
            }
            input.put(key,value);
        }
        return input;
    }
}
