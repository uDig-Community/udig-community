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
package es.axios.udig.spatialoperations.tasks;

import java.util.List;
import java.util.logging.Logger;

import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.util.LineStringExtracter;

import es.axios.geotools.util.FeatureUtil;
import es.axios.geotools.util.GeoToolsUtils;

/**
 * Convert the features from polygon to lineString.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
final class PolygonToLineTask extends AbstractSpatialOperationTask<SimpleFeatureStore>
			implements IPolygonToLineTask {

	private static final Logger									LOGGER				= Logger
																								.getLogger(PolygonToLineTask.class
																											.getName());

	private CoordinateReferenceSystem							sourceLayerCrs		= null;
	private FeatureCollection<SimpleFeatureType, SimpleFeature>	featuresFromSource	= null;
	private Boolean												explode				= null;

	// private String newAttributeName = null;

	/**
	 * To create an instance must use
	 * {@link #createProcess(FeatureStore, FeatureCollection, CoordinateReferenceSystem, String)}
	 */
	private PolygonToLineTask() {

	}

	/**
	 * Create the polygon to line process implementation.
	 * 
	 * @param targetStore
	 * @param featuresFromSource
	 * @param sourceLayerCrs
	 * @param explode
	 * @return
	 */
	public static IPolygonToLineTask createProcess(	final SimpleFeatureStore targetStore,
													final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresFromSource,
													final CoordinateReferenceSystem sourceLayerCrs,
													final Boolean explode) {
		assert targetStore != null;
		assert featuresFromSource != null;
		assert sourceLayerCrs != null;
		assert explode != null;
		// assert newAttributeName != null;

		PolygonToLineTask task = new PolygonToLineTask();
		task.targetStore = targetStore;
		task.featuresFromSource = featuresFromSource;
		task.sourceLayerCrs = sourceLayerCrs;
		task.explode = explode;
		// task.newAttributeName = newAttributeName;

		return task;
	}

	@Override
	protected SimpleFeatureStore getResult() {

		return this.targetStore;
	}

	@Override
	protected void perform() throws SpatialOperationException {

		FeatureIterator<SimpleFeature> iter = null;
		SimpleFeature featureToTransform = null;

		try {

			iter = featuresFromSource.features();

			SimpleFeatureType featureType = this.targetStore.getSchema();
			final CoordinateReferenceSystem targetLayerCrs = featureType.getGeometryDescriptor()
						.getCoordinateReferenceSystem();

			while (iter.hasNext()) {

				featureToTransform = iter.next();
				Geometry defaultGeometry = (Geometry) featureToTransform.getDefaultGeometry();
				if (defaultGeometry.isValid()) {

					if (explode) {

						createFeatureExplode(featureToTransform, targetLayerCrs, featureType);
					} else {
						createFeature(featureToTransform, targetLayerCrs, featureType);
					}
				} else {
					LOGGER.severe("Not a valid geometry!! \nFID: " + featureToTransform.getID() + "\nGeometry: " //$NON-NLS-1$
								+ defaultGeometry.toText());
				}

			}
		} catch (Exception e) {

			throw makeException(e, e.getMessage());
		} finally {

			if (iter != null) {
				iter.close();
			}
		}

	}

	/**
	 * Convert each segment of the polygon feature in one lineString feature and
	 * inserts it.
	 * 
	 * @param featureToTransform
	 *            Feature to explode
	 * @param targetLayerCrs
	 *            Target layer CRS
	 * @param featureType
	 *            Target featureType
	 * @throws OperationNotFoundException
	 * @throws TransformException
	 * @throws IllegalAttributeException
	 * @throws SpatialOperationException
	 */
	private void createFeatureExplode(	SimpleFeature featureToTransform,
										CoordinateReferenceSystem targetLayerCrs,
										SimpleFeatureType featureType)
		throws OperationNotFoundException, TransformException, IllegalAttributeException, SpatialOperationException {

		SimpleFeature transformedFeature;
		Coordinate[] newCoor;
		Geometry lines = convertToLine(featureToTransform, targetLayerCrs);

		// get the geometries of that feature.
		List<? extends Geometry> linesList =  LineStringExtracter.getLines(lines);

		for (Geometry lineSegment : linesList) {

			// get the coordinates list of each geometry.
			Coordinate[] coorList = lineSegment.getCoordinates();

			Coordinate[] coordFiltered = CoordinateArrays.removeRepeatedPoints(coorList);
			GeometryFactory gfac = lineSegment.getFactory();
			CoordinateSequenceFactory coorFac = gfac.getCoordinateSequenceFactory();
			CoordinateSequence coordinates;
			for (int i = 0; i < coordFiltered.length - 1; i++) {
				// for each geometry, create lineStrings as sides has the
				// geometry.
				newCoor = new Coordinate[2];
				newCoor[0] = coordFiltered[i];
				newCoor[1] = coordFiltered[i + 1];
				coordinates = coorFac.create(newCoor);
				LineString lineString = gfac.createLineString(coordinates);

				transformedFeature = FeatureUtil.createFeatureUsing(featureToTransform, featureType, lineString);
				insert(targetStore, transformedFeature);
			}
		}
	}

	/**
	 * Convert the polygon feature into a lineString feature and inserts it.
	 * 
	 * @param featureToTransform
	 *            The polygon feature.
	 * @param targetLayerCrs
	 *            Target CRS.
	 * @param featureType
	 *            Target feature type.
	 * @throws OperationNotFoundException
	 * @throws TransformException
	 * @throws IllegalAttributeException
	 * @throws SpatialOperationException
	 */
	private void createFeature(	SimpleFeature featureToTransform,
								CoordinateReferenceSystem targetLayerCrs,
								SimpleFeatureType featureType)
		throws OperationNotFoundException, TransformException, IllegalAttributeException, SpatialOperationException {

		Geometry mlineString;
		mlineString = convertToLine(featureToTransform, targetLayerCrs);
		// the new feature will belong to this ID

		SimpleFeature transformedFeature = FeatureUtil.createFeatureUsing(featureToTransform, featureType, mlineString);
		insert(targetStore, transformedFeature);
	}

	/**
	 * Convert the geometry of this feature( a polygon geometry ) to LineString
	 * geometry.
	 * 
	 * @param featureToTransform
	 *            The feature to be converted.
	 * @param targetLayerCrs
	 *            Target layer CRS.
	 * 
	 * @return The converted geometry which one will be a lineString geometry.
	 * 
	 * @throws OperationNotFoundException
	 * @throws TransformException
	 */
	private Geometry convertToLine(SimpleFeature featureToTransform, CoordinateReferenceSystem targetLayerCrs)
		throws OperationNotFoundException, TransformException {

		// project the base geometry to map
		Geometry baseGeometry = (Geometry) featureToTransform.getDefaultGeometry();
		Geometry baseGeomOnMapCrs = GeoToolsUtils.reproject(baseGeometry, sourceLayerCrs, targetLayerCrs);

		Geometry boundary = baseGeomOnMapCrs.getBoundary();

		return boundary;
	}
}
