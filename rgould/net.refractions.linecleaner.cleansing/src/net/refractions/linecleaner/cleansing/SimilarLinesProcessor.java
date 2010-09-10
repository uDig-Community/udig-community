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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.refractions.linecleaner.FeatureUtil;
import net.refractions.linecleaner.GeometryUtil;
import net.refractions.linecleaner.LoggingSystem;
import net.refractions.linecleaner.SimilarityMetric;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

/**
 * <p>
 * A line cleaner class that attempts to clean out duplicate and similar sets of features
 * in a given feature source.
 * </p>
 */
public class SimilarLinesProcessor extends AbstractProcessor {
    public static final double DEFAULT_SAMPLING_DISTANCE = 1;
    public static final double DEFAULT_VERY_SIMILAR_TOLERANCE = 5;
    public static final double DEFAULT_SIMILAR_TOLERANCE = 30;
    
    public enum SieveCategory { IDENTICAL, VERY_SIMILAR, SIMILAR }
    
    LoggingSystem loggingSystem;
    
    private FeatureStore store;
    private double samplingDistance;
    private double verySimilarTolerance;
    private double similarTolerance;
    
    private List<String> featureStorePriorityList = null;    
    
    private Map<String, SievedFeature> flaggedFeatures = 
        new HashMap<String, SievedFeature>();
    private Map<String, SievedFeature> deletedFeatures =
        new HashMap<String, SievedFeature>();
    
    SimilarityMetric similarityMetric = new SimilarityMetric();
    SimilarityCache similarityCache = new SimilarityCache();
    SimilarityIndex similarityIndex = new SimilarityIndex();
    
    /**
     * @param source
     * @param samplingDistance
     * @param verySimilarTolerance
     * @param similarTolerance
     * @throws IOException
     */
    public SimilarLinesProcessor(net.refractions.udig.project.internal.Map map, FeatureStore source, double samplingDistance,
            double verySimilarTolerance, double similarTolerance)
    throws IOException {
        super(map, source);
        this.store = source;
        this.samplingDistance = samplingDistance;
        this.verySimilarTolerance = verySimilarTolerance;
        this.similarTolerance = similarTolerance;
        this.loggingSystem = LoggingSystem.getInstance();
    }

    /**
     * @param source
     * @param featureStorePriorityList 
     * @param samplingDistance
     * @param verySimilarTolerance
     * @param similarTolerance
     * @throws IOException
     */
    public SimilarLinesProcessor(net.refractions.udig.project.internal.Map map, FeatureStore source, List<String> featureStorePriorityList, 
            double samplingDistance, double verySimilarTolerance, double similarTolerance)
    throws IOException {
        this(map, source, samplingDistance, verySimilarTolerance, similarTolerance);
        this.featureStorePriorityList = featureStorePriorityList;
    }

    /**
     *
     * @throws IOException
     */
    public void runInternal(IProgressMonitor monitor, PauseMonitor pauseMonitor)
    throws IOException {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("", 100);

        this.loggingSystem.setCurrentAction(LoggingSystem.SIMILAR_FEATURES);
        this.loggingSystem.begin();
        
        if (monitor.isCanceled()) {
        	return;
        }
        pauseIfNecessary(pauseMonitor);
        
        cleanFeatures(new SubProgressMonitor(monitor, 87, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
        		pauseMonitor);
        
        if (monitor.isCanceled()) {
        	return;
        }
        pauseIfNecessary(pauseMonitor);
        
        cleanFeaturesByAggregation(new SubProgressMonitor(monitor, 13, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
        		pauseMonitor);
        
        if (monitor.isCanceled()) {
        	return;
        }
        pauseIfNecessary(pauseMonitor);
        
        outputHistograms();
        
        if (monitor.isCanceled()) {
        	return;
        }
        pauseIfNecessary(pauseMonitor);
        
        outputTSV();
        
        monitor.done();       
        this.loggingSystem.finish();
    }

    protected void cleanFeatures(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {
        monitor.beginTask("", this.store.getCount(Query.FIDS));
        monitor.subTask("Cleaning Similar Features");
        
        MemoryFeatureIterator i = MemoryFeatureIterator.createDefault(this.featureStore, map);      
        try {
            while (i.hasNext()) {
                Feature f = i.next();
                FeatureCollection nearbyFeatures =
                    FeatureUtil.nearbyFeatureFids(this.store, f);

                FeaturePredicate removalPredicate;
                if (this.featureStorePriorityList == null
                		|| !FeatureUtil.hasMergeSourceAttribute(this.store)) {
                	removalPredicate = new LengthPredicate();
                } else {
                	removalPredicate = new PrecedenceLengthPredicate();
                }
                sieve(f, nearbyFeatures, removalPredicate);
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
    
    protected void cleanFeaturesByAggregation(IProgressMonitor monitor, PauseMonitor pauseMonitor)
    throws IOException {
        List<String> keys = new LinkedList<String>(this.similarityIndex.getKeys());
        final Map<String, Double> lengthIndex = this.similarityIndex.lengthIndex;
        
        monitor.beginTask("", keys.size());
        monitor.subTask("Cleaning Similar Features By Aggregation");
        
        // Sort the keys by length in descending order so that we sieve long features
        // first.  This ensures that in cycles of features involving linestrings shorter
        // than the tolerance, we pick the right combination of lines to merge together.
        Collections.sort(keys, new Comparator<String>() {
            public int compare(String s, String r) {
            	double slength = lengthIndex.get(s);
            	double rlength = lengthIndex.get(r);
                if (slength < rlength) {
                    return 1;
                } else if (slength > rlength) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for (String ffid: keys) {
            Feature f = getFeature(ffid);

            Collection<Feature> similarFeatures = 
                this.similarityIndex.getFeaturesSimilarTo(f);
            // Filter the similar features by connectivity to avoid sending
            // useless extraneous linestrings to the merger.
            // Note: This suffices for now, but would be more correct if it
            // was a cycle finder.
            similarFeatures = FeatureUtil.filterByConnectivity(f, similarFeatures);
            
            LineMerger merger = new LineMerger();
            merger.add(FeatureUtil.extractGeometries(similarFeatures));
            Collection<LineString> mergedLines = merger.getMergedLineStrings();
            aggregatedSieve(f, mergedLines);
            monitor.worked(1);
            if (monitor.isCanceled()) {
                break;
            }
            pauseIfNecessary(pauseMonitor);
        }
        monitor.done();
    }   
    
    /**
     * Compare f against a collection of linestrings, which should be a
     * collection of linestrings merged by LineMerger.  We delete f if it
     * has the same end points as any of the linestrings and is within
     * distance under the given tolerances.
     * @param f The feature in question.
     * @param lines Collection of linestrings to compare f against.
     * @throws IOException 
     */
    protected void aggregatedSieve(Feature f, Collection<LineString> lines)
    throws IOException {
        Geometry fgeom = f.getDefaultGeometry();
        for (LineString line: lines) {
            double similarity = 
                similarityMetric.similarityF(fgeom, line, this.samplingDistance);
            double reverseSimilarity = 
                similarityMetric.similarityF(line, fgeom, this.samplingDistance);
            
            if (similarity < this.verySimilarTolerance 
                    && reverseSimilarity < this.verySimilarTolerance) {
                if (GeometryUtil.identicalEndPoints(fgeom, line)) {
                    deleteFeature(f, SieveCategory.VERY_SIMILAR, similarity);
                } else {
                    flagFeature(f, SieveCategory.VERY_SIMILAR, similarity);
                }
            } else if (similarity < this.similarTolerance
                    && reverseSimilarity < this.similarTolerance) {
                if (GeometryUtil.identicalEndPoints(fgeom, line)) {
                    deleteFeature(f, SieveCategory.SIMILAR, similarity);
                } else {
                    flagFeature(f, SieveCategory.VERY_SIMILAR, similarity);
                }
            }
        }
    }
    
    /**
     * Compare f against each feature g in candidate set.  We remove f or
     * g provided g and f have identical end points.  Which one is removed
     * is determined by whether or not removalPredicate.test(f,g) returns
     * true (remove f) or false (remove g).
     * @param f
     * @param candidateSet
     * @param removalPredicate
     * @throws IOException 
     */
    protected void sieve( Feature f, Collection<Feature> candidateSet, 
            FeaturePredicate removalPredicate ) throws IOException { 
        for (Feature g: candidateSet) {
            Geometry fgeom = f.getDefaultGeometry();
            Geometry ggeom = g.getDefaultGeometry();
            
            if (fgeom.equals(ggeom)) {
            	deleteFeature(g, SieveCategory.IDENTICAL, 0.0);
            } else {
            	double similarity = getSimilarity(f,g);
            	double reverseSimilarity = getSimilarity(g,f);
            	
            	// run the features through a simple sieve by similarity metric
            	if (similarity < this.verySimilarTolerance 
            			&& reverseSimilarity < this.verySimilarTolerance) {
            		if (GeometryUtil.identicalEndPoints(fgeom, ggeom)) {
            			if (removalPredicate.test(f,g)) {
            				deleteFeature(f, SieveCategory.VERY_SIMILAR, reverseSimilarity);
            				break;  // this feature's now out of consideration
            			}
            			deleteFeature(g, SieveCategory.VERY_SIMILAR, similarity);
            		} else {
            			flagFeature(g, SieveCategory.VERY_SIMILAR, similarity);
            		}
            	} else if (similarity < this.similarTolerance 
            			&& reverseSimilarity < this.similarTolerance) {
            		if (GeometryUtil.identicalEndPoints(fgeom, ggeom)) {
            			if (removalPredicate.test(f,g)) {
            				deleteFeature(f, SieveCategory.SIMILAR, reverseSimilarity);
            				break;
            			}
            			deleteFeature(g, SieveCategory.SIMILAR, similarity);
            		} else {
            			flagFeature(g, SieveCategory.SIMILAR, similarity);
            		}
            	}
            }
        }
    }
    
    private double getSimilarity( Feature f, Feature g ) {
        return this.similarityCache.getSimilarity(f,g);
    }
    
    public void outputHistograms() {
        String s = "";
        if (this.deletedFeatures.size() > 0) {
            s += "Deleted features:\n";
            s += formatHistogram(buildHistogram(this.deletedFeatures)) + "\n";
        }
        if (this.flaggedFeatures.size() > 0) {
            s += "Flagged features:\n";
            s += formatHistogram(buildHistogram(this.flaggedFeatures)) + "\n";
        }
        this.loggingSystem.info(s); 
    }
    
    protected String formatHistogram(Map<SieveCategory, Integer> histogram) {
        String s = "";
        
        for (Map.Entry<SieveCategory, Integer> e: histogram.entrySet()) {
            SieveCategory category = e.getKey();
            Integer count = e.getValue();
            s += category + ":\t" + count + "\n";
        }
        
        return s + "\n";
    }
    
    public void outputTSV() {
        String s = "";
        if (deletedFeatures.size() > 0) {
            s += "Deleted Features: \n";
            s += histogramTSV(deletedFeatures) + "\n";
        }
        if (flaggedFeatures.size() > 0) {
            s += "Flagged Features: \n";
            s += histogramTSV(flaggedFeatures) + "\n";
        }
        loggingSystem.info(s);
    }
    
    public String histogramTSV(Map<String, SievedFeature> map) {
        Map<SieveCategory, List<SievedFeature>> histogram = transpose(map);
        String s = "";
        for (Map.Entry<SieveCategory, List<SievedFeature>> e: histogram.entrySet()) {
            SieveCategory category = e.getKey();
            List<SievedFeature> features = e.getValue();
            s += category + "\n";
            for (SievedFeature sf: features) {
                s += sf.fid + "," + sf.similarityMetric + "\n";
            }
            s += "\n";
        }
        return s;
    }
    
    protected Map<SieveCategory, List<SievedFeature>>
    transpose(Map<String, SievedFeature> map) {
        Map<SieveCategory, List<SievedFeature>> result = 
            new LinkedHashMap<SieveCategory, List<SievedFeature>>();
        
        for (Map.Entry<String, SievedFeature> e: map.entrySet()) {
            SievedFeature sf = e.getValue();
            SieveCategory category = sf.category;
            List<SievedFeature> features;
            if (result.containsKey(category)) {
                features = result.get(category);
            } else {
                features = new LinkedList<SievedFeature>();
            }
            
            features.add(sf);
            result.put(category, features);
        }
        return result;
    }
    
    protected Map<SieveCategory, Integer>
    buildHistogram(Map<String, SievedFeature> map) {
        int numCategories = SieveCategory.values().length;
        Map<SieveCategory, Integer> histogram = 
            new LinkedHashMap<SieveCategory, Integer>(numCategories);
        
        for (Map.Entry<String, SievedFeature> e: map.entrySet()) {
            SievedFeature sf = e.getValue();
            
            int count = 1;
            if (histogram.containsKey(sf.category)) {
                count = histogram.get(sf.category) + 1;
            }
            histogram.put(sf.category, count);
        }
        
        return histogram;
    }

    protected void deleteFeature(Feature f, SieveCategory category,
            double metric)
    throws IOException {
    	String fid = f.getID();
    	// keep track of deleted features and the category they fall under
    	// for logging purposes
    	SievedFeature sf = new SievedFeature(fid, category, metric);
    	deletedFeatures.put(fid, sf);
    	
    	this.similarityIndex.removeFeature(f);
    	this.similarityCache.removeFeature(f);
    	FeatureUtil.removeFeature(this.store, f);
    	loggingSystem.delete(f);
    }
    
    protected void flagFeature(Feature f, SieveCategory category,
            double metric) {
        String fid = f.getID();
        SievedFeature sf = new SievedFeature(fid, category, metric);
        flaggedFeatures.put(fid, sf);
    }
    
    protected Feature getFeature(String fid) {
        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        FidFilter filter = ff.createFidFilter(fid);
        try {
            FeatureIterator i = this.store.getFeatures(filter).features();
            try {
                Feature f = i.next();
                return f;
            } finally {
                i.close();
            }
        } catch (IOException e) {
            // TODO Handle IOException
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }
    }
    
    protected Collection<Feature> getFeatures(Collection<String> fids) {
        List<Feature> features = new LinkedList<Feature>(); 
        for (String fid: fids) {
            Feature f = getFeature(fid);
            features.add(f);
        }
        return features;
    }
    
    protected Collection<Feature> getFeatures(FeatureCollection fids) {
        List<Feature> features = new LinkedList<Feature>();
        FeatureIterator i = fids.features();
        try {
            while (i.hasNext()) {
                Feature f = getFeature(i.next().getID());
                features.add(f);
            }
        } finally {
            i.close();
        }
        return features;
    }
    
    private interface FeaturePredicate {
    	// given f and g, determine whether or not to remove f
        public abstract boolean test(Feature f, Feature g);
    }
    
    private class LengthPredicate implements FeaturePredicate {
        public boolean test(Feature f, Feature g) {
            return f.getDefaultGeometry().getLength() >
            g.getDefaultGeometry().getLength();
        }
    }

    private class PrecedenceLengthPredicate implements FeaturePredicate {
        public boolean test(Feature f, Feature g) {
            // fetch the full feature g, instead of just g with fid and geom
            g = getFeature(g.getID());
            int fpriority = 
                featureStorePriorityList.indexOf(f.getAttribute(FeatureUtil.MERGE_SOURCE_NAME));
            int gpriority =
                featureStorePriorityList.indexOf(g.getAttribute(FeatureUtil.MERGE_SOURCE_NAME));
            
            if (fpriority == gpriority) {
                return f.getDefaultGeometry().getLength() >
                g.getDefaultGeometry().getLength();
            } else if (fpriority > gpriority) {
                return false;
            } else {
                return true;
            }
        }
    }
    
    private class SimilarityCache {
        Map<String, Map<String, Double>> cache = 
            new HashMap<String, Map<String, Double>>();
        
        public double getSimilarity(Feature f, Feature g) {
            double similarity = 0;
            if (cached(f,g)) {
                similarity = getNearbyFeatures(f).get(g.getID());
            } else {
                similarity = calculateSimilarity(f, g);
                cacheSimilarity(f, g, similarity);
                indexSimilarity(f, g, similarity);
            }
            return similarity;
        }
        
        private void cacheSimilarity(Feature f, Feature g, double similarityMetric) {
            if (!cached(f, g)) {
                getNearbyFeatures(f).put(g.getID(), similarityMetric);
            }
        }
        
        private void indexSimilarity(Feature f, Feature g, double similarityMetric) {
            if (similarityMetric < similarTolerance) {
                similarityIndex.addSimilarFeatures(f,g);
            }
        }
        
        private Map<String, Double> getNearbyFeatures(Feature f) {
            if (cache.containsKey(f.getID())) {
                return cache.get(f.getID());
            }
            Map<String, Double> features = new HashMap<String, Double>();
            cache.put(f.getID(), features);
            return features;
        }
        
        private boolean cached(Feature f, Feature g) {
            return getNearbyFeatures(f).containsKey(g.getID());
        }
        
        private double calculateSimilarity(Feature f, Feature g) {
            Geometry fgeom = f.getDefaultGeometry();
            Geometry ggeom = g.getDefaultGeometry();
            return similarityMetric.similarityF(fgeom, ggeom, samplingDistance);
        }
        
        public void removeFeature(Feature f) {
            cache.remove(f.getID());
        }
    }
    
    /**
     * 
     * <p>
     * A class that keeps track of similar features.  Keep in mind similarity
     * is one-way, ie if A is similar to B, B is not necessarily similar to A.
     * </p>
     */
    private class SimilarityIndex {
        Map<String, Set<String>> similarityIndex = new HashMap<String, Set<String>>();
        Map<String, Set<String>> transposeIndex = new HashMap<String, Set<String>>();
        Map<String, Double> lengthIndex = new HashMap<String, Double>();
        
        /**
         * Add f is similar to g to the index.
         * @param f
         * @param g
         */
        public void addSimilarFeatures(Feature f, Feature g) {
            getSimilarFeatures(f).add(g.getID());
            getFidsOfFeaturesSimilarTo(g).add(f.getID());
            indexLength(f);
            indexLength(g);
        }

        /**
         * Remove feature f
         * @param f
         */
        public void removeFeature(Feature f) {
            String fid = f.getID();
            Collection<String> similar = getSimilarFeatures(f);
            Collection<String> transpose = getFidsOfFeaturesSimilarTo(f);
            
            for (String sfid: similar) {
                this.transposeIndex.get(sfid).remove(fid);
            }
            for (String tfid: transpose) {
                this.similarityIndex.get(tfid).remove(fid);
            }
            this.similarityIndex.remove(fid);
            this.transposeIndex.remove(fid);
            this.lengthIndex.remove(fid);
        }
        
        public Set<String> getKeys() {
            return transposeIndex.keySet();
        }       
        
        public Set<String> getSimilarFeatures(Feature f) {
            return getValue(similarityIndex, f);
        }
        
        public Set<String> getFidsOfFeaturesSimilarTo(Feature f) {
            return getValue(transposeIndex, f);
        }
        
        public Collection<Feature> getFeaturesSimilarTo(Feature f) {
            return getFeatures(getFidsOfFeaturesSimilarTo(f));
        }
        
        private Set<String> getValue(Map<String, Set<String>> index, Feature f) {
            Set<String> similarFeatures;
            if (index.containsKey(f.getID())) {
                similarFeatures = index.get(f.getID());
            } else {
                similarFeatures = new HashSet<String>();
                index.put(f.getID(), similarFeatures);
            }            
            return similarFeatures;
        }

		private void indexLength(Feature g) {
			if (!this.lengthIndex.containsKey(g.getID())) {
            	this.lengthIndex.put(g.getID(), g.getDefaultGeometry().getLength());
            }
		}
    }
    
    private class SievedFeature {
        /** SievedFeature fid field */
        public String fid;
        /** SievedFeature similarityMetric field */
        public double similarityMetric;
        /** SievedFeature category field */
        public SieveCategory category;
        
        /**
         * @param fid
         * @param category
         * @param similarityMetric
         */
        public SievedFeature( String fid, SieveCategory category, double similarityMetric ) {
            super();
            this.fid = fid;
            this.similarityMetric = similarityMetric;
            this.category = category;
        }
    }
}

