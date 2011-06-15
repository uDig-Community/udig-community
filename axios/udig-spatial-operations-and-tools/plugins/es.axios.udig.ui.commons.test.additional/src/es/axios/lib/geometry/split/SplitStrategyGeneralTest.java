/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Wien Government 
 *
 *      http://wien.gov.at
 *      http://www.axios.es 
 *
 * (C) 2010, Vienna City - Municipal Department of Automated Data Processing, 
 * Information and Communications Technologies.
 * Vienna City agrees to license under Lesser General Public License (LGPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.lib.geometry.split;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class SplitStrategyGeneralTest {

    @Test
    public void testCreatingAPolygonWithHoleInBoundary() throws Exception {
        Polygon polygon = (Polygon) SplitTestUtil
                .read("POLYGON ((20 25, 10 15, 10 5, 20 -5, 30 5, 30 15, 20 25), (15 15, 25 15, 25 5, 15 5, 15 15))"); //$NON-NLS-1$

        LineString splitLine = (LineString) SplitTestUtil
                .read("LINESTRING (17 10, 15 15, 20 25, 25 15, 23 10)"); //$NON-NLS-1$

        
        List<Geometry> expectedParts = new ArrayList<Geometry>();
        expectedParts.add( SplitTestUtil.read("POLYGON ((20 25, 15 15, 25 15, 20 25))")); //$NON-NLS-1$

        expectedParts.add( SplitTestUtil
                .read("POLYGON ((10 5, 10 15, 20 25, 30 15, 30 5, 20 -5, 10 5),  (20 25, 15 15, 15 5, 25 5, 25 15, 20 25))")); //$NON-NLS-1$

        SplitTestUtil.testSplitStrategy(polygon, splitLine, expectedParts);
    }

    @Test
    public void testSplitPolygon_IntersectsVertexAndEdge() throws Exception {
        // FIXME if the split line is reversed, the test fails.
        Polygon geomToSplit = (Polygon) SplitTestUtil
                .read("POLYGON((10 10, 15.5 10, 15.7 10, 15.8 10, 20 10, 20 20, 15.5 20, 15.5 30, 10 30, 10 10))");
        LineString splitter = (LineString) SplitTestUtil.read("LINESTRING(15.5 0, 15.5 40)");

        Geometry expectedLeft = SplitTestUtil
                .read("POLYGON ((15.5 10, 10 10, 10 30, 15.5 30, 15.5 10))");
        Geometry expectedRight = SplitTestUtil
                .read("POLYGON ((15.5 20, 20 20, 20 10, 15.8 10, 15.7 10, 15.5 10, 15.5 20))");

        Geometry[] expectedParts = new Geometry[]{expectedLeft, expectedRight};
        testSplitResults(geomToSplit, splitter, expectedParts);
    }
    

    private List<Geometry> testSplitResults( Geometry geomToSplit, LineString splitter,
            Geometry[] expectedParts ) {

        List<Geometry> expectedPartsList = new ArrayList<Geometry>();

        for( int i = 0; i < expectedParts.length; i++ ) {

            expectedPartsList.add(expectedParts[i]);
        }

        return SplitTestUtil.testSplitStrategy(geomToSplit, splitter, expectedPartsList);
    }

    @SuppressWarnings("nls")
    @Test
    public void testcatalog_17_non_closed_lines() throws Exception {
        Polygon geomToSplit = (Polygon) SplitTestUtil
                .read("POLYGON ((60 180, 240 180, 240 30, 60 30, 60 180),  (75 165, 120 165, 120 120, 75 120, 75 165),  (225 165, 180 165, 180 120, 225 120, 225 120, 225 120, 225 120, 225 165),  (225 45, 180 45, 180 90, 225 90, 225 45),  (75 45, 75 90, 120 90, 120 45, 120 45, 75 45))");

        LineString splitter = (LineString) SplitTestUtil
                .read("LINESTRING (105 75, 120 90, 120 120, 180 120, 180 90, 120 45)");

        assertTrue( geomToSplit.isValid());

        Geometry partA = SplitTestUtil
                .read(" POLYGON ((60 180, 60 180, 240 180, 240 30, 60 30, 60 180),  (75 165, 120 165, 120 120, 75 120, 75 165),  (180 165, 225 165, 225 120, 180 120, 180 165),  (180 90, 225 90, 225 45, 180 45, 180 90),   (120 90, 120 120, 180 120, 180 90, 120 45, 75 45, 75 90, 120 90))");

        Geometry partB = SplitTestUtil
                .read("POLYGON ((120 90, 120 120, 180 120, 180 90, 120 45, 120 90))");

        assertTrue( partA.isValid());
        assertTrue( partB.isValid());

        List<Geometry> expectedParts = new ArrayList<Geometry>();
        expectedParts.add(partA);
        expectedParts.add(partB);

        SplitTestUtil.testSplitStrategy(geomToSplit, splitter, expectedParts);
    }

    /**
     * Test catalog 11 with reversed lineString
     * 
     * @throws Exception
     */
    @SuppressWarnings("nls")
    @Test
    public void testcatalog_11a() throws Exception {
        Polygon geomToSplit = (Polygon) SplitTestUtil
                .read("POLYGON ((60 270, 90 270, 90 230, 110 230, 110 250, 150 250, 150 230, 170 230, 170 270, 200 270, 200 170, 60 170, 60 170, 60 270),  (90 210, 110 210, 110 190, 90 190, 90 210), (150 210, 170 210, 170 190, 150 190, 150 210))");
        LineString splitter = (LineString) SplitTestUtil
                .read("LINESTRING (110 190, 150 210, 150 230, 110 230, 110 210, 100 200)");

        assertFalse( SplitUtil.isClosedLine(splitter));

        Geometry partA = SplitTestUtil
                .read("POLYGON ((60 270, 90 270, 90 230, 110 230, 110 210, 90 210, 90 190, 110 190, 150 210, 150 230, 170 230, 170 270, 200 270, 200 170, 60 170, 60 170, 60 170, 60 270),  (170 210, 170 190, 150 190, 150 210, 170 210))");

        Geometry partB = SplitTestUtil
                .read("POLYGON ((110 210, 110 230, 150 230, 150 210, 110 190, 110 190, 110 190, 110 210))");

        Geometry partC = SplitTestUtil
                .read("POLYGON ((110 250, 150 250, 150 230, 110 230, 110 230, 110 230, 110 250))");

        assertTrue( partA.isValid());
        assertTrue( partB.isValid());
        assertTrue( partC.isValid());

        List<Geometry> expectedParts = new ArrayList<Geometry>();
        expectedParts.add(partA);
        expectedParts.add(partB);
        expectedParts.add(partC);

        SplitTestUtil.testSplitStrategy(geomToSplit, splitter, expectedParts);
    }

    /**
     * This test is a variant of test case 36 from wien testcatalog
     * @throws Exception
     */
    @Test
    public void testcatalog_36_variant() throws Exception {
	
        Polygon geomToSplit = (Polygon) SplitTestUtil.read("POLYGON ((-143.8705655746711 -45.58070672851018, -142.96460374772022 59.05788428431726, 30.98006702685001 63.134712505596255, 34.15093342117814 -49.65753494978917, -143.8705655746711 -45.58070672851018), (-98.57247422712679 34.14393404316787, -97.89465714526831 24.460832873760975, -95.40160783279867 -11.154157304376469, -70.10259497986135 -10.562367530038754, -55.992268360435105 -17.495890093032685, -45.413325640482235 -9.984840761866144, -17.941871628497864 -9.342233650474697, -17.941871628497864 9.519891586642764, -10.694177012890776 14.665754763723797, -17.941871628497864 19.16695457762715, -17.941871628497864 35.04989587011876, -43.06163755218221 34.76765130917848, -53.72736379305789 41.39162865877496, -71.85115977760633 34.444173531364726, -98.57247422712679 34.14393404316787))"); //$NON-NLS-1$
        LineString splitter = (LineString) SplitTestUtil.read("LINESTRING (-104.0082451888321 1.0763273594604925, -66.4108293703703 -18.40185191998357, -51.462459225680675 -26.555508362541545, 1.0833267374707418 15.571716590674683, -58.25717292781232 48.186342360906615, -113.52084437181641 21.9134493793309)"); //$NON-NLS-1$

        assertFalse( SplitUtil.isClosedLine(splitter));

        List<Geometry> expectedParts = new ArrayList<Geometry>();
        expectedParts.add( SplitTestUtil.read("POLYGON ((-81.04862613404471 -10.818415042417312, -66.4108293703703 -18.40185191998357, -51.462459225680675 -26.555508362541545, -30.354293894800104 -9.632582709335567, -45.413325640482235 -9.984840761866144, -55.992268360435105 -17.495890093032685, -70.10259497986135 -10.562367530038754, -81.04862613404471 -10.818415042417312))")); //$NON-NLS-1$

        expectedParts.add( SplitTestUtil.read("POLYGON ((-17.941871628497864 0.3187558317515756, 1.0833267374707418 15.571716590674683, -17.941871628497864 26.02831416586354, -17.941871628497864 19.16695457762715, -10.694177012890776 14.665754763723797, -17.941871628497864 9.519891586642764, -17.941871628497864 0.3187558317515756))")); //$NON-NLS-1$

        expectedParts.add( SplitTestUtil.read("POLYGON ((-34.027300759849986 34.86916071133952, -58.25717292781232 48.186342360906615, -87.53376077612401 34.26796453138138, -71.85115977760633 34.444173531364726, -53.72736379305789 41.39162865877496, -43.06163755218221 34.76765130917848, -34.027300759849986 34.86916071133952))") ); //$NON-NLS-1$
        
        expectedParts.add( SplitTestUtil.read("POLYGON ((-143.8705655746711 -45.58070672851018, -142.96460374772022 59.05788428431726, 30.98006702685001 63.134712505596255, 34.15093342117814 -49.65753494978917, -143.8705655746711 -45.58070672851018), (-98.57247422712679 34.14393404316787, -97.89465714526831 24.460832873760975, -95.40160783279867 -11.154157304376469, -81.04862613404471 -10.818415042417312, -66.4108293703703 -18.40185191998357, -51.462459225680675 -26.555508362541545, -30.354293894800104 -9.632582709335567, -17.941871628497864 -9.342233650474697, -17.941871628497864 0.3187558317515756, 1.0833267374707418 15.571716590674683, -17.941871628497864 26.02831416586354, -17.941871628497864 35.04989587011876, -34.027300759849986 34.86916071133952, -58.25717292781232 48.186342360906615, -87.53376077612401 34.26796453138138, -98.57247422712679 34.14393404316787))") ); //$NON-NLS-1$

        SplitTestUtil.testSplitStrategy(geomToSplit, splitter, expectedParts);
    }
    
    @Test
    public void testcatalog_36_fails_en_segmentsIntersectThePolygon() throws Exception {
	
        Polygon geomToSplit = (Polygon) SplitTestUtil
                .read("POLYGON ((-143.8705655746711 -45.58070672851018, -142.96460374772022 59.05788428431726, 30.98006702685001 63.134712505596255, 34.15093342117814 -49.65753494978917, -143.8705655746711 -45.58070672851018), (-98.57247422712679 34.14393404316787, -97.89465714526831 24.460832873760975, -95.40160783279867 -11.154157304376469, -70.10259497986135 -10.562367530038754, -55.992268360435105 -17.495890093032685, -45.413325640482235 -9.984840761866144, -17.941871628497864 -9.342233650474697, -17.941871628497864 9.519891586642764, -10.694177012890776 14.665754763723797, -17.941871628497864 19.16695457762715, -17.941871628497864 35.04989587011876, -43.06163755218221 34.76765130917848, -53.72736379305789 41.39162865877496, -71.85115977760633 34.444173531364726, -98.57247422712679 34.14393404316787))"); //$NON-NLS-1$
        LineString splitter = (LineString) SplitTestUtil
                .read("LINESTRING (-80 0, -80 -60, -60 -60, -20 -60, -40 0, -40 0)"); //$NON-NLS-1$

        assertFalse( SplitUtil.isClosedLine(splitter));

        Geometry partA = SplitTestUtil
                .read("POLYGON ((-80 -10.793885776123869, -80 -47.04339143632707, -23.890553132926037 -48.328340601221896, -36.73935306050836 -9.781940818474942, -45.413325640482235 -9.984840761866144, -55.992268360435105 -17.495890093032685, -70.10259497986135 -10.562367530038754, -80 -10.793885776123869))"); //$NON-NLS-1$

        Geometry partB = SplitTestUtil
                .read("POLYGON ((-143.8705655746711 -45.58070672851018, -142.96460374772022 59.05788428431726, 30.98006702685001 63.134712505596255, 34.15093342117814 -49.65753494978917, -23.890553132926037 -48.328340601221896, -36.73935306050836 -9.781940818474942, -17.941871628497864 -9.342233650474697, -17.941871628497864 9.519891586642764, -10.694177012890776 14.665754763723797, -17.941871628497864 19.16695457762715, -17.941871628497864 35.04989587011876, -43.06163755218221 34.76765130917848, -53.72736379305789 41.39162865877496, -71.85115977760633 34.444173531364726, -98.57247422712679 34.14393404316787, -97.89465714526831 24.460832873760975, -95.40160783279867 -11.154157304376469, -80 -10.793885776123869, -80 -47.04339143632707, -143.8705655746711 -45.58070672851018))"); //$NON-NLS-1$

        List<Geometry> expectedParts = new ArrayList<Geometry>();
        expectedParts.add(partA);
        expectedParts.add(partB);

        SplitTestUtil.testSplitStrategy(geomToSplit, splitter, expectedParts);
    }
}
