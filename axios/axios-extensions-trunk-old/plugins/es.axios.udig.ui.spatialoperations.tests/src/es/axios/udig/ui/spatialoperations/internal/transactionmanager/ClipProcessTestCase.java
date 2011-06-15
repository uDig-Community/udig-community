/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
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
package es.axios.udig.ui.spatialoperations.internal.transactionmanager;

import junit.framework.TestCase;
import net.refractions.udig.project.ILayer;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.ui.testsupport.TestWorkBenchBuilder;

/**
 * Test for Cip Process
 * 
 * <p>
 *
 * </p>
 * 
 * TODO this test must be implemented 
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public class ClipProcessTestCase extends TestCase {
    
    private TestWorkBenchBuilder      testData;
    private ILayer                    layerToClip;

    private CoordinateReferenceSystem sourceCrs;
    private CoordinateReferenceSystem mapCrs;
    private ILayer                    clippingLayer;     

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        testData.tearDown();
    }
    
    /**
     * Tests that the process delete all features included in clipping area
     * @throws Exception 
     *
     */
    public void testDeleteIncluedInClippingArea() throws Exception{
    }
    
    

}
