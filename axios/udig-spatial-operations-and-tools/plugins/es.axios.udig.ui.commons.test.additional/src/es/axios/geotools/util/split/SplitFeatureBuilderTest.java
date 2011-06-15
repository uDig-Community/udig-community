/**
 * 
 */
package es.axios.geotools.util.split;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import es.axios.geotools.util.TestUtility;
import es.axios.geotools.util.split.SplitFeatureBuilder;
import es.axios.lib.geometry.split.SplitTestUtil;
import es.axios.lib.geometry.util.GeometryUtil;

/**
 * Test for {@link es.axios.geotools.util.split.SplitFeatureBuilder}
 * <p>
 * <ul>
 * <li></li>
 * </ul>
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.3.0
 */
public class SplitFeatureBuilderTest {

    static final Logger LOGGER = Logger.getLogger(SplitFeatureBuilderTest.class.getName());

    

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.N  using split line [1-2-3-4-1]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9N_SplitLine1_2_3_4_1() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (-25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436)", 0); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.N  using split line [2-3-4-1-2]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9N_SplitLine2_3_4_1_2() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714)", 0); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.N  using split line [3-4-1-2-3]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9N_SplitLine3_4_1_2_3() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (51.94285714285718 25.97142857142856, -25.19999999999999 25.86857142857142, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856)", 0); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.N  using split line [4-1-2-3-4]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9N_SplitLine4_1_2_3_4() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (-25.19999999999999 25.457142857142856, 51.94285714285718 25.97142857142856, 51.94285714285718 -39.34285714285714, -25.19999999999999 -38.828571428571436, -25.19999999999999 25.457142857142856)", 0); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.O  using split line [1-2-3-4-1]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9O_SplitLine1_2_3_4_1() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (-25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436)", 90); //$NON-NLS-1$
    }
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.O  using split line [2-3-4-1-2]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9O_SplitLine2_3_4_1_2() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714)", 90); //$NON-NLS-1$
    }
    
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.O  using split line [4-1-2-3-4]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9O_SplitLine4_1_2_3_4() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (-25.19999999999999 25.457142857142856, 51.94285714285718 25.97142857142856, 51.94285714285718 -39.34285714285714, -25.19999999999999 -38.828571428571436, -25.19999999999999 25.457142857142856)", 90); //$NON-NLS-1$
    }
    
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.O  using split line [3-4-1-2-3]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9O_SplitLine3_4_1_2_3() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (51.94285714285718 25.97142857142856, -25.19999999999999 25.86857142857142, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856)", 90); //$NON-NLS-1$
    }
    


    
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.SO  using split line [1-2-3-4-1]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9SO_SplitLine1_2_3_4_1() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (-25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436)", 135); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.SO  using split line [2-3-4-1-2]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9SO_SplitLine2_3_4_1_2() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714)", 135); //$NON-NLS-1$
    }
    
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.SO  using split line [3-4-1-2-3]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9SO_SplitLine3_4_1_2_3() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (51.94285714285718 25.97142857142856, -25.19999999999999 25.86857142857142, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856)", 135); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.SO  using split line [4-1-2-3-4]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9SO_SplitLine4_1_2_3_4() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (-25.19999999999999 25.457142857142856, 51.94285714285718 25.97142857142856, 51.94285714285718 -39.34285714285714, -25.19999999999999 -38.828571428571436, -25.19999999999999 25.457142857142856)", 135); //$NON-NLS-1$
    }
    
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.SW  using split line [1-2-3-4-1]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9SW_SplitLine1_2_3_4_1() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (-25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436)", 225); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.SW  using split line [2-3-4-1-2]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9SW_SplitLine2_3_4_1_2() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714)", 225); //$NON-NLS-1$
    }
    
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.SW  using split line [3-4-1-2-3]
     * the corners of inner features.
     * 
     * @throws Exception
     */
    @Ignore
    public void testBuildSplit_9SW_SplitLine3_4_1_2_3() throws Exception {
        
        // TODO this test require an specific example because the result of split line rotation does not thouch
        testBuildSplit_9_usingSplitLine( "LINESTRING (51.94285714285718 25.97142857142856, -25.19999999999999 25.86857142857142, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856)", 225); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.SW  using split line [4-1-2-3-4]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9SW_SplitLine4_1_2_3_4() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (-25.19999999999999 25.457142857142856, 51.94285714285718 25.97142857142856, 51.94285714285718 -39.34285714285714, -25.19999999999999 -38.828571428571436, -25.19999999999999 25.457142857142856)", 225); //$NON-NLS-1$
    }
    
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.S  using split line [1-2-3-4-1]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9S_SplitLine1_2_3_4_1() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (-25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436)", 180); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.S  using split line [2-3-4-1-2]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9S_SplitLine2_3_4_1_2() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714)", 180); //$NON-NLS-1$
    }
    
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.S  using split line [3-4-1-2-3]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9S_SplitLine3_4_1_2_3() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (51.94285714285718 25.97142857142856, -25.19999999999999 25.86857142857142, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856)", 180); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.S  using split line [4-1-2-3-4]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9S_SplitLine4_1_2_3_4() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (-25.19999999999999 25.457142857142856, 51.94285714285718 25.97142857142856, 51.94285714285718 -39.34285714285714, -25.19999999999999 -38.828571428571436, -25.19999999999999 25.457142857142856)", 180); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.W  using split line [1-2-3-4-1]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9W_SplitLine1_2_3_4_1() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (-25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436)", 270); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.W  using split line [2-3-4-1-2]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9W_SplitLine2_3_4_1_2() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714)", 270); //$NON-NLS-1$
    }
    
    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.W  using split line [3-4-1-2-3]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9W_SplitLine3_4_1_2_3() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (51.94285714285718 25.97142857142856, -25.19999999999999 25.86857142857142, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856)", 270); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.W  using split line [4-1-2-3-4]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9W_SplitLine4_1_2_3_4() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (-25.19999999999999 25.457142857142856, 51.94285714285718 25.97142857142856, 51.94285714285718 -39.34285714285714, -25.19999999999999 -38.828571428571436, -25.19999999999999 25.457142857142856)", 270); //$NON-NLS-1$
    }


    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.NW  using split line [1-2-3-4-1]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9NW_SplitLine1_2_3_4_1() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (-25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436)", 315); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.NW  using split line [2-3-4-1-2]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9NW_SplitLine2_3_4_1_2() throws Exception {
        
        // creates a list of features with this geometries
        testBuildSplit_9_usingSplitLine("LINESTRING (51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714)", 315); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.N  using split line [3-4-1-2-3]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9NW_SplitLine3_4_1_2_3() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (51.94285714285718 25.97142857142856, -25.19999999999999 25.86857142857142, -25.19999999999999 -38.828571428571436, 51.94285714285718 -39.34285714285714, 51.94285714285718 25.97142857142856)", 315); //$NON-NLS-1$
    }

    /**
     * This test correspond to bug report bagis_split_detail_20100809_en.pdf 9.N  using split line [4-1-2-3-4]
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSplit_9NW_SplitLine4_1_2_3_4() throws Exception {
        
        testBuildSplit_9_usingSplitLine( "LINESTRING (-25.19999999999999 25.457142857142856, 51.94285714285718 25.97142857142856, 51.94285714285718 -39.34285714285714, -25.19999999999999 -38.828571428571436, -25.19999999999999 25.457142857142856)", 315); //$NON-NLS-1$
    }
    /**
     * Create list of features for scenario 2. They will be rotated the angle indeed as parameter.
     * @param angle
     * @param x
     * @param y
     * @return the list of rotated features
     * @throws Exception
     */
    private List<SimpleFeature> createFeatureExample_9(final int angle, final double x, final double y) throws Exception{
        

        List<SimpleFeature> featureList = new ArrayList<SimpleFeature>(4);

        final String wkt1 = "POLYGON ((-77.14285714285712 52.19999999999999, -50.91428571428568 51.685714285714276, -50.91428571428568 25.97142857142856, -25.19999999999999 25.457142857142856, -25.19999999999999 51.171428571428564, 52.457142857142884 51.171428571428564, 51.94285714285718 25.97142857142856, 77.65714285714287 25.457142857142856, 77.1428571428572 51.171428571428564, 102.85714285714292 51.171428571428564, 103.3714285714286 -77.39999999999999, -76.6285714285714 -77.91428571428573, -77.14285714285712 52.19999999999999), (-49.88571428571427 -39.34285714285714, -49.88571428571427 -64.54285714285713, -25.19999999999999 -64.54285714285713, -25.19999999999999 -38.828571428571436, -34.97142857142856 -38.828571428571436, -49.88571428571427 -39.34285714285714), (51.94285714285718 -39.34285714285714, 51.94285714285718 -65.57142857142857, 77.65714285714287 -64.54285714285713, 77.1428571428572 -38.3142857142857, 51.94285714285718 -39.34285714285714))"; //$NON-NLS-1$
        featureList.add(createFeature(rotatePolygon(wkt1,angle, x, y)));
        
        final String wkt2 = "POLYGON ((-50.91428571428568 25.97142857142856, -50.91428571428568 51.685714285714276, -50.91428571428568 76.88571428571427, 77.65714285714287 76.88571428571427, 77.65714285714287 51.171428571428564, 77.1428571428572 51.171428571428564, 77.65714285714287 25.457142857142856, 51.94285714285718 25.97142857142856, 52.457142857142884 51.171428571428564, -25.19999999999999 51.171428571428564, -25.19999999999999 25.86857142857142, -50.91428571428568 25.97142857142856)))POLYGON ((-50.91428571428568 25.97142857142856, -50.91428571428568 51.685714285714276, -50.91428571428568 76.88571428571427, 77.65714285714287 76.88571428571427, 77.65714285714287 51.171428571428564, 77.1428571428572 51.171428571428564, 77.65714285714287 25.457142857142856, 51.94285714285718 25.97142857142856, 52.457142857142884 51.171428571428564, -25.19999999999999 51.171428571428564, -25.19999999999999 25.86857142857142, -50.91428571428568 25.97142857142856)))"; //$NON-NLS-1$
        featureList.add(createFeature(rotatePolygon(wkt2,angle, x, y)));

        final String wkt3 ="POLYGON ((77.65714285714287 -64.54285714285713, 51.94285714285718 -65.57142857142857, 51.94285714285718 -39.34285714285714, 77.1428571428572 -38.3142857142857, 77.65714285714287 -64.54285714285713))"; //$NON-NLS-1$
        featureList.add(createFeature(rotatePolygon(wkt3,angle, x, y)));
        
        final String wkt4 = "POLYGON ((-49.88571428571427 -64.54285714285713, -49.88571428571427 -39.34285714285714, -25.19999999999999 -38.828571428571436, -25.19999999999999 -64.54285714285713, -49.88571428571427 -64.54285714285713))"; //$NON-NLS-1$
        featureList.add(createFeature(rotatePolygon(wkt4, angle, x, y)));
        
        return featureList;        
    }

    /**
     * test for scenario 9 from bagis_split_detail_20100809_en.pdf using the orientation indeed by the angle parameter
     * @param wktSplitLine
     * @param angle degree (the orientation)
     * @throws Exception
     */
    private void testBuildSplit_9_usingSplitLine(final String wktSplitLine, final int angle) throws Exception{
        
        // creates a list of features with this geometries
        
        final double x = -77.14285714285712;
        final double y = 52.19999999999999;

        List<SimpleFeature> featureList =  createFeatureExample_9(angle, x, y);

        // creates the split line
        SimpleFeature firstFeature = featureList.get(0);
        CoordinateReferenceSystem crs = firstFeature.getFeatureType().getCoordinateReferenceSystem();
        LineString splitLine = createSplitLine(crs, wktSplitLine, angle,x, y); 
        
        // executes the split builder
        SplitFeatureBuilder builder = SplitFeatureBuilder.newInstance(featureList, splitLine, crs);
        builder.buildSplit();
        
        List<SimpleFeature>  splitList = builder.getSplitResult();
        LOGGER.fine("Split Result: "+ prettyPrint(splitList)); //$NON-NLS-1$
        Assert.assertEquals("Split Result", 3, splitList.size()); //$NON-NLS-1$
        
        List<SimpleFeature> faturesSufferedSplit = builder.getFeaturesThatSufferedSplit();
        LOGGER.fine("Original feature that was splitted: " +prettyPrint(faturesSufferedSplit)); //$NON-NLS-1$
        Assert.assertEquals("Features that suffers split",  1, faturesSufferedSplit.size()); //$NON-NLS-1$
        
    }    
    /**
     * Creates a lineString oriented in the direction indeed by the angle parameter 
     * @param crs
     * @param wktSplitLine
     * @param angle degree this is the orientation
     * @param px    pivot x
     * @param py    pivot y
     * @return
     * @throws ParseException
     */
    private LineString createSplitLine( 
            final CoordinateReferenceSystem crs, 
            final String wktSplitLine,
            final int angle, 
            final double px ,final double py ) throws ParseException {
        
        final double radAngle= SplitTestUtil.convert(angle);
        final Coordinate center = new Coordinate(px ,py);
        
        WKTReader reader = new WKTReader();
        LineString splitLine = (LineString)reader.read( wktSplitLine);
        LineString rotated = (LineString)GeometryUtil.rotation(splitLine, radAngle, center);
        rotated.setUserData(crs);

        return rotated;
    }

    /**
     * Rotates the Polygon 
     * @param wktGeom 
     * @param angle exagecimal (degree)
     * @param cx pivot x
     * @param cy pivot y
     * @return 
     * @throws ParseException
     */
    private String rotatePolygon(final String wktGeom, int angle, double cx, double cy ) throws ParseException{
        
        final double radAngle= SplitTestUtil.convert(angle);
        final Coordinate center = new Coordinate(cx ,cy);
        Polygon geom1 =  (Polygon)SplitTestUtil.read(wktGeom);
        Polygon p = GeometryUtil.rotation(geom1, radAngle , center);
        
        return p.toText();
    }
    
    /**
     * Create a feature with the provided geometry. The CRS is WGS84
     * @param wktGeometry
     * @return a new feature
     * @throws ParseException
     */
    private SimpleFeature createFeature( final String  wktGeometry ) throws ParseException {
        
        return TestUtility.createFeature(wktGeometry);
    }


    /**
     * Prints the features'attribute of the feature list 
     * @param featureList
     * @return the list of features ready to print
     */
    private static String prettyPrint( List<SimpleFeature> featureList ) {

        StringBuilder strBuilder = new StringBuilder("\n"); //$NON-NLS-1$
        for( SimpleFeature f : featureList ) {
            strBuilder.append("Feature Id -- Geometry: ").append(f.getID()) //$NON-NLS-1$
                    .append(" -- ").append(f.getDefaultGeometry()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return strBuilder.toString();
    }

}
