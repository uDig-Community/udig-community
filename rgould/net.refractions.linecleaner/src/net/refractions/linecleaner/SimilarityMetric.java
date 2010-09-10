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
package net.refractions.linecleaner;

import java.io.IOException;
import java.util.Iterator;


import net.refractions.linecleaner.GeometryUtil.CoordinateFunction;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Utility class dealing with the calculation of similarity between LineStrings.
 * Similarity is defined as the average distance between two lines.
 * </p>
 */
public class SimilarityMetric { 
    /**
     *
     * @param source 
     * @return All the LineString/MultiLineString features with similarity added as an 
     * attribute named "Similarity".
     * @throws IOException
     * @throws SchemaException
     * @throws FactoryConfigurationError
     * @throws IllegalFilterException
     * @throws IllegalAttributeException
     */
    public static FeatureCollection addSimilarityMetric(FeatureSource source, double intervalLength)
    throws IOException, SchemaException, FactoryConfigurationError, 
    IllegalFilterException, IllegalAttributeException {
        return addSimilarityMetric(source, "Similarity", intervalLength);
    }
    
    /**
     *
     * @param source 
     * @param attributeName Name of the attribute in which the similarity metric will be 
     * written.
     * @return All the LineString/MultiLineString features with similarity added as an 
     * attribute named "Similarity".
     * @throws IOException
     * @throws SchemaException
     * @throws FactoryConfigurationError
     * @throws IllegalFilterException
     * @throws IllegalAttributeException
     */
    public static FeatureCollection addSimilarityMetric(FeatureSource source, String attributeName, double intervalLength)
    throws IOException, SchemaException, FactoryConfigurationError,
    IllegalFilterException, IllegalAttributeException {
        FeatureCollection fc = source.getFeatures();
        AttributeType similarityType = AttributeTypeFactory.newAttributeType(attributeName, 
                Double.class, true, Double.SIZE, 0.0);
        FeatureType ft = FeatureUtil.addAttribute(fc.getSchema(), similarityType);
        
        FeatureCollection out = new MemoryFeatureCollection(ft);
        int count = fc.size();
        int n = 1;
        for (Iterator i = fc.iterator(); i.hasNext();) {
            System.out.println("on feature # " + n++ + " of " + count);
            Feature f = (Feature)i.next();
            Feature similarFeature = DataUtilities.reType(ft, f);
            
            double sim = similarityMetric(source.getFeatures(), f, intervalLength);            
            similarFeature.setAttribute(similarityType.getName(), sim);

            out.add(similarFeature);
        }
        return out;
    }

    /**
     *
     * @param source
     * @param f
     * @return Similarity metric for f from source. 
     * @throws FactoryConfigurationError
     */
    private static double similarityMetric( FeatureCollection features, Feature f, double intervalLength )
    throws FactoryConfigurationError {
        FeatureCollection nearbyFeatures = FeatureUtil.nearbyFeatures(features, f);
        double sim = 0;
        for (Iterator j = nearbyFeatures.iterator(); j.hasNext();) {
            Feature g = (Feature)j.next();
            
            double currentSim = similarity(f.getDefaultGeometry(),
                    g.getDefaultGeometry(), intervalLength);
            if (sim == 0) {
                sim = currentSim;
            } else {
                sim = Math.min(sim, currentSim);
            }
        }
        return sim;
    }

    /**
     *
     * @param g
     * @param h
     * @param d subdivision distance
     * @return similarity between g and h
     */
    public static double similarity(Geometry g, Geometry h, double d) {
        LineString line = GeometryUtil.extractLine(g);
        if (line == null) {
            return 0.0;
        }
        
        Coordinate[] gPoints = GeometryUtil.subdivide(line, d);
        
        double sumDifferences = 0;
        for (int i = 0; i < gPoints.length; i++) {
            Point p = g.getFactory().createPoint(gPoints[i]);
            sumDifferences += p.distance(h);
        }
        
        return sumDifferences / (gPoints.length - 1);
    }
    
    /**
    *
    * @param g
    * @param h
    * @param d subdivision distance
    * @return similarity between g and h
    */
   public double similarityF(Geometry g, Geometry h, double d) {
       LineString line = GeometryUtil.extractLine(g);
       if (line == null) {
           return 0.0;
       }
       
       SimilarityClosure similarityClosure = new SimilarityClosure(h);
       GeometryUtil.subdivide(line, d, similarityClosure); 

       return similarityClosure.getSimilarity();
   }
   
   private class SimilarityClosure implements CoordinateFunction {
       double sumDifferences = 0.0;
       Geometry target;
       int numPointsVisited = 0;
       
       public SimilarityClosure(Geometry g) {
           this.target = g;
       }
       
       public void run(Coordinate c) {
           Point p = this.target.getFactory().createPoint(c);
           this.sumDifferences += p.distance(this.target);
           this.numPointsVisited++;
       }
       
       public double getSimilarity() {
           return this.sumDifferences / (numPointsVisited-1);
       }
   }
}

