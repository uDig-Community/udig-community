/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
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
package es.axios.udig.ui.commons.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.unit.Unit;

import org.geotools.data.DataUtilities;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.CoordinateSequenceTransformer;
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.CRSUtilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.ui.commons.internal.i18n.Messages;

/**
 * GeoTools Utils
 * <p>
 * This class has util and convenint methods to work with GeoTools
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public class GeoToolsUtils {

    private static final String[][]  COMMON_LENGTH_UNITS = {
            {"km", Messages.GeoToolsUtils_unitName_kilometers}, //$NON-NLS-1$ 
            {"pixel", Messages.GeoToolsUtils_unitName_pixels}, //$NON-NLS-1$ 
            {"ft", Messages.GeoToolsUtils_unitName_feet}, //$NON-NLS-1$ 
            {"yd", Messages.GeoToolsUtils_unitName_yards}, //$NON-NLS-1$
            {"in", Messages.GeoToolsUtils_unitName_inches}, //$NON-NLS-1$ 
            {"cm", Messages.GeoToolsUtils_unitName_centimeters}, //$NON-NLS-1$ 
            {"m", Messages.GeoToolsUtils_unitName_meters}};                            //$NON-NLS-1$ 

    public static final Unit         DEGREES             = Unit.valueOf("\u00B0");     //$NON-NLS-1$

    public static final Unit         PIXEL_UNITS         = Unit.valueOf("pixel");      //$NON-NLS-1$

    /**
     * Commonly used units of length measure
     */
    private static Map<Unit, String> commonLengthUnits   = new HashMap<Unit, String>();

    private GeoToolsUtils() {
        // util class
    }
    /**
     * Returns a set of the most commonly used units of measure for measuring lengths at a GIS
     * application scale
     * 
     * @return a set of the most commont units to use in operations like buffer, etc
     */
    public static Set<Unit> getCommonLengthUnits() {
        if (commonLengthUnits.isEmpty()) {
            synchronized (commonLengthUnits) {
                if (commonLengthUnits.isEmpty()) {
                    for( int i = 0; i < COMMON_LENGTH_UNITS.length; i++ ) {
                        Unit unit = Unit.valueOf(COMMON_LENGTH_UNITS[i][0]);
                        String unitName = COMMON_LENGTH_UNITS[i][1];
                        commonLengthUnits.put(unit, unitName);
                    }
                    commonLengthUnits = Collections.unmodifiableMap(commonLengthUnits);
                }
            }
        }
        return commonLengthUnits.keySet();
    }

    /**
     * Returns the localized unit name
     * 
     * @param unit the unit
     * @return the localized
     */
    public static String getUnitName( Unit unit ) {

        assert unit != null;

        String unitName = commonLengthUnits.get(unit);
        if (unitName == null) {
            if (DEGREES.equals(unit)) {
                unitName = Messages.GeoToolsUtils_unitName_degrees;
            } else {
                unitName = unit.toString();
            }
        }

        return unitName;
    }

    /**
     * Adds the matching attributes from <code>source</code> to <code>target</code>
     * <p>
     * Two attributes match if they have the same name and type.
     * </p>
     * 
     * @param source
     * @param target
     * @throws IllegalAttributeException
     */
    public static void match( SimpleFeature source, SimpleFeature target ) throws IllegalAttributeException {
        Map<String, Class<?>> sourceTypes = new HashMap<String, Class<?>>();
        for( AttributeDescriptor att : source.getFeatureType().getAttributes() ) {
            sourceTypes.put(att.getLocalName(), att.getType().getBinding());
        }
        for( AttributeDescriptor att : target.getFeatureType().getAttributes() ) {
            String name = att.getLocalName();
            Class<?> sourceBinding = sourceTypes.get(name);
            if (sourceBinding != null && sourceBinding == att.getType().getBinding()) {
                Object attribute = source.getAttribute(name);
                target.setAttribute(name, attribute);
            }
        }
    }

    /**
     * Returns a representative (first axis with a length) unit of the given crs.
     * 
     * @param crs
     * @return a representative unit of the given crs.
     */
    public static Unit getDefaultCRSUnit( CoordinateReferenceSystem crs ) {
        assert crs != null;
        CoordinateSystem coordinateSystem = crs.getCoordinateSystem();
        Unit unit = CRSUtilities.getUnit(coordinateSystem);
        if (unit == null) {
            CoordinateSystemAxis axis = coordinateSystem.getAxis(0);
            unit = axis.getUnit();
        }
        return unit;
    }

    /**
     * Returns the first feature of feature collection
     * 
     * @param featureSet
     * @return SimpleFeature first feature in collection
     */
    public static SimpleFeature firstFeature( FeatureCollection<SimpleFeatureType, SimpleFeature> featureSet ) {    	
        Iterator iter = null;
        try {
            iter = featureSet.iterator();
            if (!iter.hasNext()) {
                return null;
            }
           return (SimpleFeature) iter.next();
        } finally {
            if (iter != null) {
                featureSet.close(iter);
            }
        }
    }

    /**
     * @param gFactory
     * @param geomCrs
     * @param reprojectCrs
     * @return a GeometryCoordinateSequenceTransformer configured to transform geometries from
     *         <code>geomCrs</code> to <code>reprojectCrs</code>
     * @throws OperationNotFoundException
     */
    public static GeometryCoordinateSequenceTransformer getTransformer(
                                                                        final GeometryFactory gFactory,
                                                                        final CoordinateReferenceSystem geomCrs,
                                                                        final CoordinateReferenceSystem reprojectCrs ) throws OperationNotFoundException {

        assert geomCrs != null;
        assert reprojectCrs != null;

        CoordinateSequenceFactory csFactory;
        CoordinateSequenceTransformer csTransformer;
        GeometryCoordinateSequenceTransformer transformer;

        csFactory = gFactory.getCoordinateSequenceFactory();
        csTransformer = new CoordSeqFactoryPreservingCoordinateSequenceTransformer(csFactory);
        transformer = new GeometryCoordinateSequenceTransformer(csTransformer);
        // MathTransform mathTransform = findMathTransform(geomCrs, reprojectCrs);
        MathTransform mathTransform;
        try {
            mathTransform = CRS.findMathTransform(geomCrs, reprojectCrs, true);
        } catch (FactoryException e) {
            throw new OperationNotFoundException(e.getMessage());
        }

        transformer.setMathTransform(mathTransform);

        return transformer;
    }

    // private static MathTransform findMathTransform( final CoordinateReferenceSystem geomCrs,
    // final CoordinateReferenceSystem reprojectCrs ) throws OperationNotFoundException {
    // MathTransform mathTransform;
    //
    // CoordinateOperation reprojectOperation;
    // try {
    // CoordinateOperationFactory coordinateOperationFactory;
    // Hints hints = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
    // coordinateOperationFactory = FactoryFinder.getCoordinateOperationFactory(hints);
    // reprojectOperation = coordinateOperationFactory.createOperation(geomCrs, reprojectCrs);
    // } catch (OperationNotFoundException e) {
    // throw e;
    // } catch (FactoryException e) {
    // // should not happen
    // throw (RuntimeException) new RuntimeException().initCause(e);
    // }
    // mathTransform = (MathTransform2D) reprojectOperation.getMathTransform();
    // return mathTransform;
    // }

    /**
     * Reprojects <code>geom</code> from <code>geomCrs</code> to <code>reprojectCrs</code>
     * 
     * @param geom the geometry to reproject
     * @param geomCrs the original CRS of <code>geom</code>
     * @param reprojectCrs the CRS to reproject <code>geom</code> onto
     * @return <code>geom</code> reprojected from <code>geomCrs</code> to
     *         <code>reprojectCrs</code>
     * @throws OperationNotFoundException if there isn't a transformation in GeoTools to convert
     *         from <code>geomCrs</code> to <code>reprojectCrs</code>
     * @throws TransformException
     */
    public static Geometry reproject( final Geometry geom, final CoordinateReferenceSystem geomCrs,
                                      final CoordinateReferenceSystem reprojectCrs ) throws OperationNotFoundException, TransformException {

        assert geom != null;
        assert geomCrs != null;
        assert reprojectCrs != null;

        if (geomCrs.equals(reprojectCrs)) {
            return geom;
        }
        if (CRS.equalsIgnoreMetadata(geomCrs, reprojectCrs)) {
            return geom;
        }

        GeometryFactory gFactory = geom.getFactory();

        GeometryCoordinateSequenceTransformer transformer;
        transformer = getTransformer(gFactory, geomCrs, reprojectCrs);
        Geometry geometry;
        try {
            geometry = transformer.transform(geom);
        } catch (TransformException e) {
            throw e;
        }
        return geometry;
    }

    /**
     * Returns the aSimpleFeaturebuilder for a type with the default geometry attribute. The default
     * geometry type is <code>Geometry<code>
     *
     * @return FeatureTypeBuilder
     */
    public static SimpleFeatureTypeBuilder createDefaultFeatureType() {
        return createDefaultFeatureType(Messages.GeoToolsUtils_FeatureTypeName);
    }

    /**
     * Returns the aSimpleFeaturebuilder for a type with the default geometry attribute The default
     * geometry type is <code>Geometry<code>. The Crs will be WGS84
     *
     * @return FeatureTypeBuilder
     */
    public static SimpleFeatureTypeBuilder createDefaultFeatureType( final String typeName ) {

        return createDefaultFeatureType(typeName,
                                        DefaultGeographicCRS.WGS84);

    }

    /**
     * Returns the aSimpleFeaturebuilder for a type with the default geometry attribute The default
     * geometry type is <code>Geometry<code>. 
     *
     * @param typeName
     * @param crs 
     * @return FeatureTypeBuilder
     */
    public static SimpleFeatureTypeBuilder createDefaultFeatureType( final String typeName,
                                                               final CoordinateReferenceSystem crs ) {

        assert typeName != null;

        SimpleFeatureTypeBuilder builder;

        builder = new SimpleFeatureTypeBuilder();
        builder.setName(typeName);
        builder.crs( crs ).add( Messages.GeoToolsUtils_Geometry, Geometry.class );

        return builder;

    }

    /**
     * Returns the aSimpleFeaturebuilder for a type with the following attributes present in de
     * prototype. The default geometry type is <code>Geometry<code>
     *
     * @param prototype
     * @return FeatureTypeBuilder
     */
    public static SimpleFeatureTypeBuilder createDefaultFeatureType( final SimpleFeatureType prototype ) {

        assert prototype != null;
        final String newTypeName = prototype.getTypeName() + "2"; //$NON-NLS-1$

        return createDefaultFeatureType(prototype, newTypeName);
    }

    /**
     * Returns the aSimpleFeaturebuilder for a type with the attributes present in de prototype. The
     * default geometry type is <code>Geometry<code>
     * 
     * @param prototype
     * @param typeName
     * @return FeatureTypeBuilder
     */
    public static SimpleFeatureTypeBuilder createDefaultFeatureType( final SimpleFeatureType prototype,
                                                               final String typeName ) {

        assert prototype != null;
        assert typeName != null;

        SimpleFeatureTypeBuilder builder;

        builder = new SimpleFeatureTypeBuilder();
        builder.setName(typeName);
        
        List<AttributeDescriptor> attributes = prototype.getAttributes();
        GeometryDescriptor defaultGeometry = prototype.getDefaultGeometry();
        for( int i = 0; i < attributes.size(); i++ ) {
            AttributeDescriptor att = attributes.get(i);
            if (att == defaultGeometry) {
                if (att.getType().getBinding() != MultiPolygon.class && att.getType().getBinding() != Polygon.class) {
                    Class<?> targetGeomType = Polygon.class;
                    final Class sourceGeomClass = defaultGeometry.getType().getBinding();
                    if (GeometryCollection.class.isAssignableFrom(sourceGeomClass)) {
                        targetGeomType = MultiPolygon.class;
                    }
                    final String geomTypeName = att.getLocalName();
                    CoordinateReferenceSystem crs = defaultGeometry.getCRS();
                    
                    AttributeTypeBuilder build = new AttributeTypeBuilder();
                    build.setName( geomTypeName );
                    build.setBinding( targetGeomType );
                    build.setNillable(true);
                    build.setCRS(crs);
                    
                    GeometryType type = build.buildGeometryType();
                    att = build.buildDescriptor( geomTypeName, type );
                }
                builder.add( att );
                builder.setDefaultGeometry( att.getLocalName() );
            } else {
                builder.add(att);
            }
        }
        return builder;

    }

    /**
     * Create a newSimpleFeatureofSimpleFeatureType whith the geometry provides. The geometry will be
     * adapted to geometry class ofSimpleFeaturetype.
     * 
     * @param type
     * @param geometry
     * @return a newSimpleFeature
     */
    @SuppressWarnings("unchecked")
    public static SimpleFeature createFeatureWithGeometry( final SimpleFeatureType type, Geometry geometry ) {

       SimpleFeature newFeature;
        try {
            newFeature = DataUtilities.template(type);
            final GeometryDescriptor targetGeometryType = type.getDefaultGeometry();

            final String attName = targetGeometryType.getLocalName();
            final Class geomClass = targetGeometryType.getType().getBinding();
            Geometry geoAdapted = GeometryUtil.adapt(geometry, geomClass);

            newFeature.setAttribute(attName, geoAdapted);

            return newFeature;

        } catch (IllegalAttributeException e) {
            final String msg = Messages.GeoToolsUtils_FailCreatingFeature;
            throw (RuntimeException) new RuntimeException(msg).initCause(e);
        } finally {
        }
    }

    /**
     * @param featureType
     * @return the dimension of default geometry
     */
    public static int getDimensionOf( final SimpleFeatureType featureType ) {

        GeometryDescriptor geomAttr = featureType.getDefaultGeometry();

        Class geomClass = geomAttr.getType().getBinding();

        int dim = GeometryUtil.getDimension(geomClass);

        return dim;
    }
    /**
     * Computes the sum of features in theSimpleFeaturecollection.
     * 
     * @param selectedFeatures
     * @return the count of features in the collection or Integer.MAX_VALUE if theSimpleFeature
     *         collection has more than Integer.MAX_VALUE features
     */
    public static int computeCollectionSize( FeatureCollection<SimpleFeatureType, SimpleFeature> features ) {

        Iterator iter = features.iterator();
        int count = 0;
        try {
            while( iter.hasNext() ) {
                iter.next();
                count++;
            }
        } catch (ArithmeticException e) {
            count = Integer.MAX_VALUE;
        } finally {
            features.close(iter);
        }

        return count;
    }

    /**
     * Utility method to easily reproject a line segment as LineSegment is not a Geometry
     * 
     * @param segment the segment to reproject
     * @param segmentCrs the CRS the segment coordinates are in
     * @param reprojectCrs the CRS to reproject the segment to
     * @return a new line segment built from the reprojected coordinates of <code>segment</code>
     *         from <code>segmentCrs</code> to <code>reprojectCrs</code>
     */
    public static LineSegment reproject( LineSegment segment, CoordinateReferenceSystem segmentCrs,
                                         CoordinateReferenceSystem reprojectCrs ) {

        assert segment != null;
        assert segmentCrs != null;
        assert reprojectCrs != null;

        if (segmentCrs.equals(reprojectCrs)) {
            return segment;
        }
        if (CRS.equalsIgnoreMetadata(segmentCrs, reprojectCrs)) {
            return segment;
        }

        MathTransform mathTransform;
        try {
            mathTransform = CRS.findMathTransform(segmentCrs, reprojectCrs, true);
        } catch (FactoryException e) {
            throw new RuntimeException(e.getMessage());
        }
        double[] src = {segment.p0.x, segment.p0.y, segment.p1.x, segment.p1.y};
        double[] dst = new double[4];
        try {
            mathTransform.transform(src, 0, dst, 0, 2);
        } catch (TransformException e) {
            throw new RuntimeException(e.getMessage());
        }
        Coordinate p0 = new Coordinate(dst[0], dst[1]);
        Coordinate p1 = new Coordinate(dst[2], dst[3]);
        LineSegment lineSegment = new LineSegment(p0, p1);
        return lineSegment;
    }

}
