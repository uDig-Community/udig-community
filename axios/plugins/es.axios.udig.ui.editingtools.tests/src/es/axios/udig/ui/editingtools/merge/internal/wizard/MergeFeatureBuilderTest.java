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
package es.axios.udig.ui.editingtools.merge.internal.wizard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureFactoryImpl;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.Attribute;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

import es.axios.udig.ui.editingtools.merge.internal.wizard.MergeFeatureBuilder.ChangeListener;

/**
 * Test suite for {@link MergeFeatureBuilder}
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class MergeFeatureBuilderTest extends TestCase {

    private static final String ATTNAME_NAME   = "name";
    private static final String ATTNAME_GEOM   = "geom";
    private static final String ATTNAME_INTATT = "intval";

    private MergeFeatureBuilder builder;

    private static SimpleFeature[]    features;
    private static SimpleFeatureType  fType;
    private static Geometry     mergedGeometry = null;

    public MergeFeatureBuilderTest( String name ) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(MergeFeatureBuilderTest.class);
        TestSetup wrapper = new TestSetup(suite){
            protected void setUp() throws Exception {
                oneTimeSetUp();
            }
            protected void tearDown() {
                oneTimeTearDown();
            }
        };
        return wrapper;
    }

    private static void oneTimeSetUp() throws Exception {
        String identification = "TestFeatureType";
        String typeSpec = "name:string:nillable,geom:LineString,intval:Integer:nillable";
        fType = DataUtilities.createType(identification, typeSpec);
        features = new SimpleFeature[3];
        features[0] = SimpleFeature(fType, "name1", "LINESTRING(0 0, 10 10)", 1);
        features[1] = SimpleFeature(fType, "name2", "LINESTRING(10 10, 10 20)", 1);
        features[2] = SimpleFeature(fType, "name3", "LINESTRING(10 20, 20 20)", 1);

        mergedGeometry = new WKTReader().read("LINESTRING(0 0, 10 10, 10 20, 20 20)");
    }

    private static SimpleFeature SimpleFeature( SimpleFeatureType ft, String name, String geom, int intAtt ) throws Exception {
        Object[] values = new Object[3];
        values[0] = name;
        values[1] = new WKTReader().read(geom);
        values[2] = new Integer(intAtt);
        SimpleFeature f = SimpleFeatureBuilder.build( ft, values, null);
        return f;
    }

    private static void oneTimeTearDown() {
    }

    protected void setUp() throws Exception {
        super.setUp();
        builder = new MergeFeatureBuilder(features, mergedGeometry);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        builder = null;
    }

    public void testMergeFeatureBuilderPreConditions() throws SchemaException, IllegalAttributeException {
        SimpleFeature[] empty = new SimpleFeature[0];
        try {
            builder = new MergeFeatureBuilder(empty, mergedGeometry);
            fail("shouldn't accept empty collection");
        } catch (RuntimeException e) {
            assertTrue(true);
        }

        SimpleFeatureType anotherSchema = DataUtilities.createSubType(fType, new String[]{ATTNAME_NAME,
                ATTNAME_INTATT});
        SimpleFeature[] mixedFeatures = new SimpleFeature[4];
        mixedFeatures[0] = features[0];
        mixedFeatures[1] = features[1];
        mixedFeatures[2] = features[2];
        SimpleFeature nonGeometryFeature = SimpleFeatureBuilder.build( anotherSchema, new Object[]{"a name", new Integer(1000)}, null);
        
        mixedFeatures[3] = nonGeometryFeature;

        try {
            builder = new MergeFeatureBuilder(mixedFeatures, mergedGeometry);
            fail("shouldn't accept features of mixed schemas");
        } catch (IllegalArgumentException iae) {
            assertTrue(true);
        }

        SimpleFeature[] nonGeomCollection = {nonGeometryFeature};
        try {
            builder = new MergeFeatureBuilder(nonGeomCollection, mergedGeometry);
            fail("shouldn't accept schema with no default geometry");
        } catch (IllegalArgumentException iae) {
            assertTrue(true);
        }

        Geometry illegalGeomType = new GeometryFactory().createPoint(new Coordinate(0, 0));
        try {
            builder = new MergeFeatureBuilder(features, illegalGeomType);
            fail("failure expected as the merge geometry passed is not assignable "
                    + "to the features schema default geom");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
    public void testAddChangeListener() {
        class TestListener implements ChangeListener {
            MergeFeatureBuilder callbackBuilder;
            int                 changedAttributeIndex;
            Object              oldAttributeValue;

            public void attributeChanged( MergeFeatureBuilder builder, int attributeIndex,
                                          Object oldValue ) {
                this.callbackBuilder = builder;
                this.changedAttributeIndex = attributeIndex;
                this.oldAttributeValue = oldValue;
            }

        }

        TestListener listener = new TestListener();
        builder.addChangeListener(listener);

        int featureIndex = 1;
        int attributeIndex = 0;
        Object expectedOldValue = builder.getMergeAttribute(attributeIndex);
        builder.setMergeAttribute(featureIndex, attributeIndex);
        assertSame(builder, listener.callbackBuilder);
        assertSame(attributeIndex, listener.changedAttributeIndex);
        assertSame(expectedOldValue, listener.oldAttributeValue);
    }

    /**
     * This test is a kind of fun; they want to force failure and ensure
     * that the merge fails. 
     * <p>
     * I am going to force failure by putting an impossible restriction on the FeatureType.
     * </p>
     * @throws SchemaException
     * @throws IllegalAttributeException
     */
    public void testBuildMergedFeatureFailsIfCreateFails() throws SchemaException, IllegalAttributeException {
        /**
         * DefaultFeatureType subclass with the sole pourpose of allowing us to force when to fail
         * on a call to create(Object[])
         */
//        class FailOnDemandType extends DefaultFeatureType {
//            // flag to indicate if fail on #create(...) or not
//            boolean failOnCreate = false;
//            public FailOnDemandType( String typeName, String namespace, Collection types,
//                                     Collection superTypes, GeometryAttributeType defaultGeom ) throws SchemaException, NullPointerException {
//                super(typeName, namespace, types, superTypes, defaultGeom);
//            }
//
//            public SimpleFeature create( Object[] attributes ) throws IllegalAttributeException {
//                if (failOnCreate) {
//                    throw new IllegalAttributeException("Forced failure for unit test purposes");
//                }
//                return super.create(attributes);
//            }
//        }
        class FailFeatureFactory extends FeatureFactoryImpl {
            // flag to indicate if fail on #create(...) or not
            boolean failOnCreate = false;
            
            @Override
            public SimpleFeature createSimpleFeature( List<Attribute> value,
                    SimpleFeatureType type, String id ) {
                if( failOnCreate ){
                    throw new IllegalAttributeException("Forced failure for unit test purposes");
                }
                return super.createSimpleFeature(value, type, id);
            }  
        };
        
        FailFeatureFactory factory = new FailFeatureFactory();
        SimpleFeatureBuilder create = new SimpleFeatureBuilder( fType, factory );
        create.addAll( features[0].getAttributes() );
        
        SimpleFeature f = create.buildFeature(features[0].getID()+"_copy");        
        SimpleFeature[] collection = {f};
                
        builder = new MergeFeatureBuilder(collection, null);
        
        // now force our SimpleFeature type to fail on create
        factory.failOnCreate = true;
        try {
            builder.setFactory(factory);
            builder.buildMergedFeature();
            fail("cleared a non null attribute shouldn't allow to build the merge SimpleFeature");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    public void testBuildMergedFeatureNullProvidedGeometry() {
        builder = new MergeFeatureBuilder(features, null);

        Geometry assignedGeometry = (Geometry) features[0].getDefaultGeometry();

        SimpleFeature merged = builder.buildMergedFeature();
        assertSame(assignedGeometry, merged.getDefaultGeometry());
    }

    public void testGetMergedGeometry() {
        assertSame(mergedGeometry, builder.getMergedGeometry());

        builder = new MergeFeatureBuilder(features, null);

        assertNull(builder.getMergedGeometry());
    }

    public void testMergeGeomIsUnion() throws ArrayIndexOutOfBoundsException, IllegalAttributeException {
        assertTrue(builder.mergeGeomIsUnion());
        final int geomAttIndex = fType.indexOf(ATTNAME_GEOM);
        builder.setMergeAttribute(2, geomAttIndex);
        assertFalse(builder.mergeGeomIsUnion());
        builder = new MergeFeatureBuilder(features, null);
        assertFalse(builder.mergeGeomIsUnion());

        // say both the provided geometry is null and we assign
        // de value of a SimpleFeature where the geom is also null
        features[1].setAttribute(geomAttIndex, null);
        builder = new MergeFeatureBuilder(features, null);
        builder.setMergeAttribute(1, geomAttIndex);
        // now both the assigned value and the provided geom are null
        // but the assigned value is NOT the provided geom
        assertFalse("expected false even if the assigned "
                + "value and the provided geom are both null", builder.mergeGeomIsUnion());
    }

    public void testSetAndGetMergeAttribute() {
        // attribute index 1 is the geometry one
        assertSame(features[0].getAttribute(0), builder.getMergeAttribute(0));
        assertSame(mergedGeometry, builder.getMergeAttribute(1));
        assertSame(features[0].getAttribute(2), builder.getMergeAttribute(2));

        builder.setMergeAttribute(2, 1);
        assertSame(features[2].getAttribute(1), builder.getMergeAttribute(1));

        builder.setMergeAttribute(1, 2);
        assertSame(features[1].getAttribute(2), builder.getMergeAttribute(2));
    }

    public void testClearMergeAttribute() {
        builder.clearMergeAttribute(0);
        assertNull(builder.getMergeAttribute(0));

        int geomAttIndex = 1;
        builder.setMergeAttribute(1, geomAttIndex);
        assertNotSame(mergedGeometry, builder.getMergeAttribute(geomAttIndex));
        builder.clearMergeAttribute(geomAttIndex);
        assertSame(mergedGeometry, builder.getMergeAttribute(1));

        builder.clearMergeAttribute(2);
        assertNull(builder.getMergeAttribute(2));
    }

    public void testGetAttribute() {
        // not much could be wronng here, lets check it so
        assertSame(features[0].getAttribute(0), builder.getAttribute(0, 0));
        assertSame(features[0].getAttribute(1), builder.getAttribute(0, 1));
        assertSame(features[0].getAttribute(2), builder.getAttribute(0, 2));

        assertSame(features[1].getAttribute(0), builder.getAttribute(1, 0));
        assertSame(features[1].getAttribute(1), builder.getAttribute(1, 1));
        assertSame(features[1].getAttribute(2), builder.getAttribute(1, 2));

        assertSame(features[2].getAttribute(0), builder.getAttribute(2, 0));
        assertSame(features[2].getAttribute(1), builder.getAttribute(2, 1));
        assertSame(features[2].getAttribute(2), builder.getAttribute(2, 2));
    }

}
