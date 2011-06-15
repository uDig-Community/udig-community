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
package es.axios.geotools.util;

import java.util.List;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Utility Functions used in the test cases
 * <p>
 * <ul>
 * <li></li>
 * </ul>
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.3.0
 */
public class TestUtility {
    
    private TestUtility(){
        //utility class
    }
    
    /**
     * Create a new Feature with the specified geometry 
     * @param wktGeometry
     * @return a new feature
     * @throws ParseException
     */
    static public SimpleFeature createFeature( final String  wktGeometry ) throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry geom1 = reader.read( wktGeometry);
        assert geom1.isValid();
        
        SimpleFeatureTypeBuilder sfb = FeatureUtil.createDefaultFeatureType();
        SimpleFeatureType featureType = sfb.buildFeatureType();
        SimpleFeature newFeature = FeatureUtil.createFeatureWithGeometry(featureType , geom1);
        
        return newFeature;
    }
    
    /**
     * Prints the features'attribute of the feature list 
     * @param featureList
     * @return the list of features ready to print
     */
    public static String prettyPrint( List<SimpleFeature> featureList ) {

        StringBuilder strBuilder = new StringBuilder("\n"); //$NON-NLS-1$
        for( SimpleFeature f : featureList ) {
            strBuilder.append("Feature Id -- Geometry: ").append(f.getID()) //$NON-NLS-1$
                    .append(" -- ").append(f.getDefaultGeometry()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return strBuilder.toString();
    }    

}
