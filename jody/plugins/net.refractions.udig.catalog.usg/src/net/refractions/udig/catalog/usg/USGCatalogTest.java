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
package net.refractions.udig.catalog.usg;

import java.io.IOException;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.geotools.feature.Feature;

import junit.framework.TestCase;

/**
 * TODO Purpose of 
 * <p>
 *
 * </p>
 * @author Jody Garnett
 * @since 1.0.0
 */
public class USGCatalogTest extends TestCase {
    USGCatalog cat;
    AddressSeeker seeker;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cat = new USGCatalog();        
        seeker = new AddressSeeker();
    }
    public void testStuff() throws Throwable {
        List<Feature> stuff = seeker.geocode("1600 Pennsylvania Ave, Washington DC");
        
        System.out.println( stuff );
        
        for( Feature feature :
            stuff ){
            System.out.println( feature );
        }
    }
}
