package es.axios.geotools.util.split;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import es.axios.geotools.util.TestUtility;
import es.axios.lib.geometry.split.SplitTestUtil;


public class SplitFeatureBuilderModifiedNeighbourTest {
    
    private static final Logger LOGGER = Logger.getLogger(SplitFeatureBuilderModifiedNeighbourTest.class.getName());
    
    
    /**
     * The inputs are grabbed from Split_modifiedpolygon_01.log
     * 
     * @throws Exception 
     */
    @SuppressWarnings({"nls"})
    @Test
    public void split_modifiedpolygon_01() throws Exception{

        // Creates the neighbor and the feature to split
        final String neghbourGeom = 
                "MULTIPOLYGON (((3331.90000000037 344759.1501, 3330.18290000036 344761.539100001, 3329.76719999965 344762.1175, 3327.75999999978 344764.91, 3320.46600000001 344759.625399999,3321.88999999967 344757.43,  3323.59999999963 344758.550000001, 3325.25999999978 344758.199999999,  3326.41999999993 344756.41,  3326.0700000003 344754.75,  3324.37000000011 344753.640000001, 3328.16579999961 344747.7939, 3334.34269999992 344751.981899999, 3330.12000000011 344757.869999999, 3331.90000000037 344759.1501), (3328.75 344759.683, 3327.502 344758.915,  3326.97695543392 344759.73388619, 3326.471 344760.523, 3327.574 344761.339, 3328.14163402196 344760.539678622,3328.75 344759.683)))";
        
        
        SimpleFeature neighbourFeature = TestUtility.createFeature(neghbourGeom);
        
        final String geomToSplit = 
            "MULTIPOLYGON (((3326.471 344760.523, 3326.97695543392 344759.73388619, 3327.502 344758.915, 3328.75 344759.683, 3328.14163402196 344760.539678622, 3327.574 344761.339, 3326.471 344760.523)))";
        
        SimpleFeature featureToSplit = TestUtility.createFeature(geomToSplit);
        
       
        List<SimpleFeature> featureList = new ArrayList<SimpleFeature>(2);
        featureList.add(featureToSplit);
        featureList.add(neighbourFeature);
       
        // creates the split line
        LineString splitLine = (LineString) SplitTestUtil.read("LINESTRING (3327.574 344761.339, 3327.502 344758.915)");
        CoordinateReferenceSystem crs = featureToSplit.getType().getCoordinateReferenceSystem();
        splitLine.setUserData(crs);
        
        // executes the split builder
        SplitFeatureBuilder builder = SplitFeatureBuilder.newInstance(featureList, splitLine, crs);
        builder.buildSplit();
        
        List<SimpleFeature> faturesSufferedSplit = builder.getFeaturesThatSufferedSplit();
        LOGGER.fine("Original feature that was splitted: " +TestUtility.prettyPrint(faturesSufferedSplit)); //$NON-NLS-1$
        Assert.assertEquals("Features that suffers split",  1, faturesSufferedSplit.size()); //$NON-NLS-1$
                  
        List<SimpleFeature>  splitList = builder.getSplitResult();
        LOGGER.fine("Split Result: "+ TestUtility.prettyPrint(splitList)); //$NON-NLS-1$
        Assert.assertEquals("Split Result", 2, splitList.size()); //$NON-NLS-1$

        builder.buildNeighbours();
        List<SimpleFeature> neighbourList = builder.getNeighbourResult();
        LOGGER.fine("Neghbour features: " +TestUtility.prettyPrint(neighbourList)); //$NON-NLS-1$
        Assert.assertEquals("Neighbour features",  1, neighbourList.size()); //$NON-NLS-1$
    }
    
    /**
     * This test asserts that an new vertex must be added in the neighbor feature of an split feature. The new vertex is the
     * intersection between the neighbor's boundary and the split line. 
     * @throws Exception 
     */
    @Test
    public void neighborWithNewVertex() throws Exception{
    	
    	// prepares the split input
        List<SimpleFeature> featureList = new ArrayList<SimpleFeature>(2);
    	
        final String geomToSplit = 
            "POLYGON ((-73.9627659574468 5.9840425531915, -73.00531914893617 46.1968085106383, -42.293601292545716 46.91661439789745, -44.79653519726947 -1.1953373262367697, -73.9627659574468 5.9840425531915))"; //$NON-NLS-1$
        
        SimpleFeature featureToSplit = TestUtility.createFeature(geomToSplit);
        featureList.add(featureToSplit);

        final String neghborGeom = "POLYGON ((-42.293601292545716 46.91661439789745, -11.728723404255305 47.63297872340426, -11.728723404255305 -9.335106382978722, -44.79653519726947 -1.1953373262367697, -42.293601292545716 46.91661439789745))"; //$NON-NLS-1$
        final Geometry originalGeom = SplitTestUtil.read(neghborGeom); 

        SimpleFeature neighborFeature = TestUtility.createFeature(neghborGeom);
        featureList.add(neighborFeature);
        
        // prepares the split line
        LineString splitLine = (LineString) SplitTestUtil.read("LINESTRING (-83.66364306217145 32.93933431654321, -30.988645743155033 25.51534140580935)" ); //$NON-NLS-1$
        CoordinateReferenceSystem crs = featureToSplit.getType().getCoordinateReferenceSystem();
        splitLine.setUserData(crs);
        
        SplitFeatureBuilder builder = SplitFeatureBuilder.newInstance(featureList, splitLine, crs);
        builder.buildSplit();
        builder.buildNeighbours();

        // computes the expected new,  that is the intersection between the neighbor's boundary and the split line.
        Geometry originalBoundary = originalGeom.getBoundary();
		Geometry intersection = originalBoundary.intersection(splitLine);
        Coordinate[] coordList = intersection.getCoordinates();
        Assert.assertTrue(coordList.length == 1);
        Coordinate expectedNewVertex = coordList[0];
        
        // search the expected new vertex in the neighbor
        List<SimpleFeature> neighborList = builder.getNeighbourResult();
        Assert.assertEquals("Neighbour features",  1, neighborList.size()); //$NON-NLS-1$
        Geometry resultGeometry = (Geometry) neighborList.get(0).getDefaultGeometry();
        Geometry resultBoundary = resultGeometry.getBoundary();
        
        // asserts there is a new coordinate
		Assert.assertEquals(originalBoundary.getCoordinates().length + 1, resultBoundary.getCoordinates().length);

		// asserts that the new vertex is that expected 
		boolean exist = false;
		int onlyOne = 0;
        Coordinate[] coordinates = resultBoundary.getCoordinates();
        for(int i = 0; i < coordinates.length; i++){
        	
        	if(coordinates[i].equals2D(expectedNewVertex)){
        		exist = true;
        		onlyOne++;
        	}
        }
        Assert.assertTrue(exist);
        Assert.assertEquals(onlyOne, 1);
    }
    
    
    /**
     * Test that the split line can divide two separated features
     */
    @Test
    public void splitSeparatedFeature() throws Exception{

        List<SimpleFeature> featureList = new ArrayList<SimpleFeature>(2);
    	
    	final String geom1 = "POLYGON ((-105.12820512820512 -3.076923076923066, -105.12820512820512 49.230769230769226, -57.43589743589743 49.230769230769226, -57.43589743589743 -3.076923076923066, -105.12820512820512 -3.076923076923066))"; //$NON-NLS-1$
        SimpleFeature feature1 = TestUtility.createFeature(geom1);
        featureList.add(feature1);

        final String geom2 = "POLYGON ((-20.162365355033984 -4.220029958030366, -20.162365355033984 47.82700619101084, 13.128982091650045 47.82700619101084, 13.128982091650045 -4.220029958030366, -20.162365355033984 -4.220029958030366))"; //$NON-NLS-1$
        SimpleFeature feature2 = TestUtility.createFeature(geom2);
        featureList.add(feature2);
        
        // prepares the split line
        LineString splitLine = (LineString) SplitTestUtil.read("LINESTRING (-119 23, 29 23)" ); //$NON-NLS-1$
        CoordinateReferenceSystem crs = feature1.getType().getCoordinateReferenceSystem();
        splitLine.setUserData(crs);
        
        // execute the split builder
        SplitFeatureBuilder builder = SplitFeatureBuilder.newInstance(featureList, splitLine, crs);
        builder.buildSplit();
        builder.buildNeighbours();

        List<SimpleFeature> splitFeatures =  builder.getFeaturesThatSufferedSplit();
        Assert.assertEquals( "unexpected size of feature that suffered split", 2, splitFeatures.size()); //$NON-NLS-1$
        
        List<SimpleFeature> splitResult =  builder.getSplitResult();
        Assert.assertEquals("unexpected size of split fragement", 4, splitResult.size()); //$NON-NLS-1$
        
        List<SimpleFeature> neighbour =  builder.getNeighbourResult();
        Assert.assertEquals("unexpected size of neighbour error",0, neighbour.size()); //$NON-NLS-1$
    }
    
    

}
