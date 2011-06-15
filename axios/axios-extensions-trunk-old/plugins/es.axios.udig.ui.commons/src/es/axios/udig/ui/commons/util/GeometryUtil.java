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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import es.axios.udig.ui.commons.internal.i18n.Messages;

/**
 * Geometry util methods
 * <p>
 * Collection of method which gets feature or feature collection to applay geometry operations
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public class GeometryUtil {

    /**
     * unused
     */
    private GeometryUtil() {
        // util class
    }

    /**
     * Returns a geometry which is the union of all the non null default geometries from the
     * features in <code>featureCollection</code>
     * 
     * @param featureCollection
     * @param expectedGeometryClass
     * @return Gemetry Union
     */
    public static Geometry geometryUnion( final FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection ) {

        Geometry resultGeom = null;

        SimpleFeature currFeature;
        Geometry featureGeom;
        try {
            for( Iterator<SimpleFeature> iterator = featureCollection.iterator(); iterator.hasNext(); ) {
                currFeature = iterator.next();
                featureGeom = (Geometry) currFeature.getDefaultGeometry();
                if (featureGeom != null) {
                    featureGeom.normalize();
                    if (resultGeom == null) {
                        resultGeom = featureGeom;
                    } else {
                        resultGeom = resultGeom.union(featureGeom);
                    }
                }
            }
        } finally {
            // ask feature collection to close potentially still open iterators
            featureCollection.purge();
        }

        return resultGeom;

        // // TODO: iterar sobre featurecollection y crear la unión incrementalmente. Obtener el
        // // array de geometries es potencialmente demasiado costoso
        // Geometry[] geometries = extractGeometries(featureCollection);
        // assert geometries.length >= 2;
        //
        // final GeometryFactory geomFactory = geometries[0].getFactory();
        //
        // if (Polygon.class.equals(expectedGeometryClass)) {
        // // does buffer of collection for efficient union
        //
        // GeometryCollection geometryCollection = geomFactory
        // .createGeometryCollection(geometries);
        //
        // resultGeom = geometryCollection.buffer(0);
        //
        // } else { // other geometries
        // Geometry unionGeom = geometries[0];
        // for( int i = 1; i < geometries.length; i++ ) {
        // Geometry geom = geometries[i];
        //
        // if (GeometryCollection.class.equals(unionGeom.getClass())) {
        // break; // cannot do the union
        // }
        // unionGeom = unionGeom.union(geom);
        //
        // }
        // // if could not do union because exists a geometry collection in union geometry
        // if (GeometryCollection.class.equals(unionGeom.getClass())) {
        // // makes a geometry collection
        // resultGeom = geomFactory.createGeometryCollection(geometries);
        // } else {
        // // the result is the union
        // resultGeom = unionGeom;
        // }
        // }
        // assert !resultGeom.isEmpty();
        //
        // return resultGeom;
    }

    /**
     * Extracts the geometries and makes a geometry array.
     * <p>
     * Note the resulting array size might be lower than the featureCollection size, as it will not
     * contain null geometries.
     * </p>
     * 
     * @param featureCollection
     * @return Geometry[] geometries present in features
     */
    public static Geometry[] extractGeometries( final FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection ) {

        Iterator iter = null;
        try {
            ArrayList<Geometry> geometries = new ArrayList<Geometry>(featureCollection.size());

            int finalSize = 0;
            iter = featureCollection.iterator();
            while( iter.hasNext() ) {

            	SimpleFeature feature = (SimpleFeature) iter.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                if (geometry == null) {
                    continue;
                } else if (geometry instanceof GeometryCollection) {

                    GeometryCollection geomSet = (GeometryCollection) geometry;
                    final int size = geomSet.getNumGeometries();
                    for( int j = 0; j < size; j++ ) {
                        geometries.add(geomSet.getGeometryN(j));
                    }
                    finalSize += size;

                } else {
                    geometries.add(geometry);
                    finalSize++;
                }
            }

            return geometries.toArray(new Geometry[finalSize]);

        } finally {
            if (iter != null) {
                featureCollection.close(iter);
            }

        }

    }

    /**
     * Existe intersection between features.
     * 
     * @param features
     * @param expectedGeometryClass
     * @return boolean true
     */
    // public static boolean intersects( final FeatureCollection<SimpleFeatureType, SimpleFeature> features,
    // final Class expectedGeometryClass ) {
    // // TODO: search an option to optimize this implementation.
    // Geometry result = geometryUnion(features, expectedGeometryClass);
    //
    // return !(result instanceof GeometryCollection);
    // }
    /**
     * Adapts a Geometry <code>geom</code> to another type of geometry given the desired geometry
     * class.
     * <p>
     * Currently implemented adaptations:
     * <ul>
     * <li>Point -> MultiPoint. Wraps the Point on a single part MultiPoint.
     * <li>Polygon -> MultiPolygon. Wraps the Polygon on a single part MultiPolygon.
     * <li>LineString -> MultiLineString. Wraps the LineString on a single part MultiLineString.
     * <li>MultiLineString -> String. Succeeds if merging the parts result in a single LineString,
     * fails otherwise.
     * <li>MultiPolygon -> Polygon. Succeeds if merging the parts result in a single Polygon, fails
     * otherwise.
     * <li>* -> GeometryCollection
     * </ul>
     * </p>
     * TODO: add more adaptations on an as needed basis
     * 
     * @param inputGeom
     * @param adaptTo
     * @return Geometry adapted
     * @throws IllegalArgumentException if <code>geom</code> cannot be adapted as
     *         <code>adapTo</code>
     */
    public static Geometry adapt( final Geometry inputGeom, final Class< ? extends Geometry> adaptTo ) {

        assert inputGeom != null : "inputGeom can't be null";
        assert adaptTo != null : "adaptTo can't be null";;

        final Class geomClass = inputGeom.getClass();

        if (Geometry.class.equals(adaptTo)) {
            return inputGeom;
        }

        final GeometryFactory gf = inputGeom.getFactory();

        if (MultiPoint.class.equals(adaptTo) && Point.class.equals(geomClass)) {
            return gf.createMultiPoint(new Point[]{(Point) inputGeom});
        }

        if (Polygon.class.equals(adaptTo)) {
            if (adaptTo.equals(geomClass)) {
                return inputGeom;
            }
            Polygonizer polygonnizer = new Polygonizer();
            polygonnizer.add(inputGeom);
            Collection polys = polygonnizer.getPolygons();
            Polygon[] polygons = new ArrayList<Polygon>(polys).toArray(new Polygon[polys.size()]);

            if (polygons.length == 1) {
                return polygons[0];
            }
        }

        if (MultiPolygon.class.equals(adaptTo)) {
            if (adaptTo.equals(geomClass)) {
                return inputGeom;
            }
            if (Polygon.class.equals(geomClass)) {
                return gf.createMultiPolygon(new Polygon[]{(Polygon) inputGeom});
            }
            /*
             * Polygonizer polygonnizer = new Polygonizer(); polygonnizer.add(inputGeom); Collection
             * polys = polygonnizer.getPolygons(); Polygon[] polygons = new ArrayList<Polygon>(polys).toArray(new
             * Polygon[polys.size()]); if (MultiPolygon.class.equals(adaptTo)) { return
             * gf.createMultiPolygon(polygons); } if (polygons.length == 1) { return polygons[0]; }
             */
        }

        if (GeometryCollection.class.equals(adaptTo)) {
            return gf.createGeometryCollection(new Geometry[]{inputGeom});
        }

        if (MultiLineString.class.equals(adaptTo) || LineString.class.equals(adaptTo)) {
            LineMerger merger = new LineMerger();
            merger.add(inputGeom);
            Collection mergedLineStrings = merger.getMergedLineStrings();
            ArrayList<LineString> lineList = new ArrayList<LineString>(mergedLineStrings);
            LineString[] lineStrings = lineList.toArray(new LineString[mergedLineStrings.size()]);

            if (MultiLineString.class.equals(adaptTo)) {
                MultiLineString line = gf.createMultiLineString(lineStrings);
                return line;
            }
            if (lineStrings.length == 1) {
                Geometry mergedResult = (Geometry) lineStrings[0];
                return mergedResult;
            }
        }

        final String msg = MessageFormat.format(Messages.GeometryUtil_DonotKnowHowAdapt,
                                                geomClass.getSimpleName(), adaptTo.getSimpleName());

        throw new IllegalArgumentException(msg);
    }
    /**
     * @param geomClass
     * @return the geometry class's dimension
     */
    public static int getDimension( final Class geomClass ) {

        if ((Point.class.equals(geomClass)) || (MultiPoint.class.equals(geomClass))) {
            return 0;
        } else if ((LineString.class.equals(geomClass))
                || (MultiLineString.class.equals(geomClass))) {
            return 1;
        } else if ((Polygon.class.equals(geomClass)) || (MultiPolygon.class.equals(geomClass))) {
            return 2;
        } else {
            final String msg = MessageFormat.format(Messages.GeometryUtil_CannotGetDimension,
                                                    geomClass.getName());
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * @param simpleGeometry Point, LineString or Polygon class
     * @return a compatible Geometry collection for the simple geometry
     */
    public static Class getCompatibleCollection( final Class simpleGeometry ) {

        if (Point.class.equals(simpleGeometry)) {
            return MultiPoint.class;
        } else if (LineString.class.equals(simpleGeometry)) {
            return MultiLineString.class;
        } else if (Polygon.class.equals(simpleGeometry)) {
            return MultiPolygon.class;
        } else {
            throw new IllegalArgumentException(Messages.GeometryUtil_ExpectedSimpleGeometry);
        }
    }

}
