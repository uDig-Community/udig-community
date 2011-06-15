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
package es.axios.udig.ui.spatialoperations.tasks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;

import es.axios.geotools.util.FeatureTypeUnionBuilder;
import es.axios.udig.spatialoperations.tasks.IIntersectTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.spatialoperations.ShapefileUtil.ShapeReader;

/**
 * <p>
 * Test class for IntersectTask operation.
 * 
 * <pre>
 * 
 * Common schema of any test:
 * 
 * public void Test (){
 * 
 * 		-initialize the parameters
 * 
 * 		 initTaskParameters();
 * 
 * 		-execute
 * 
 * 		 runTask();
 * 
 * 		-obtain the result and check the data.
 * }
 * 
 * </pre>
 * 
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class IntersectTaskTest extends AbstractTaskTest {

	private String						firstShp			= "";
	private String						secondShp			= "";
	private String						targetShp			= "";
	private String						newShp				= "";
	private String						newTargetName		= "";
	private Class<? extends Geometry>	newTargetGeom		= null;

	private Boolean						isCreatingNewLayer	= null;

	private IIntersectTask				task				= null;
	private SimpleFeatureStore				targetStore			= null;
	private FeatureTypeUnionBuilder		featureUnionBuilder;

	private static final String			TASK_PATH			= "IntersectTask/";

	@Override
	protected Callable<FeatureStore<SimpleFeatureType,SimpleFeature>>   getCurrentTask() {

		return task;
	}

	/**
	 * Return the feature type used when creating a new layer.
	 */
	private SimpleFeatureType buildTargetFeatureType(	
			final SimpleFeatureType firstType,
			final SimpleFeatureType secondType,
			final String layerName,
			final Class<? extends Geometry> geometryClass,
			final CoordinateReferenceSystem crs) throws SchemaException {

		this.featureUnionBuilder = new FeatureTypeUnionBuilder(layerName);

		featureUnionBuilder.add(firstType).add(secondType).setGeometryClass("the_geom", geometryClass, crs); //$NON-NLS-1$
		SimpleFeatureType newFeatureType = featureUnionBuilder.getFeatureType();

		return newFeatureType;

	}

	@Override
	protected void initTaskParameters() throws IOException, SchemaException {

		reader = new ShapeReader();

		SimpleFeatureCollection featuresInFirstLayer, featuresInSecondLayer;

		featuresInFirstLayer = reader.getFeatures(PATH + firstShp);
		featuresInSecondLayer = reader.getFeatures(PATH + secondShp);

		final CoordinateReferenceSystem firstLayerCrs = reader.getCRS(PATH + firstShp);
		final CoordinateReferenceSystem secondLayerCrs = reader.getCRS(PATH + secondShp);
		final CoordinateReferenceSystem mapCrs = firstLayerCrs;

		SimpleFeatureType firstLayerType, secondLayerType;

		firstLayerType = featuresInFirstLayer.getSchema();
		secondLayerType = featuresInSecondLayer.getSchema();

		if (isCreatingNewLayer) {
			SimpleFeatureType type = buildTargetFeatureType(firstLayerType, secondLayerType, newTargetName, newTargetGeom,
						firstLayerCrs);
			targetStore = createNewTargetStore(PATH + newShp, type);
		} else {
			targetStore = createTargetStore(PATH + targetShp);
		}

		IIntersectTask task = SpatialOperationFactory.createIntersect(targetStore, featuresInFirstLayer,
					featuresInSecondLayer, firstLayerCrs, mapCrs, isCreatingNewLayer, featureUnionBuilder,
					secondLayerCrs);

		this.task = task;
	}

	/**
	 * 
	 * Intersects the MultiPolygon shapefile MultiPolygon2 whit the MultiPolygon
	 * shapefile UsingIntersectMultiPolygon and puts the result on the existent
	 * shapefile TargetMultiPolygonForIntersect.
	 * 
	 * Check that the featureStore isn't empty and the geometry of the resultant
	 * features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testIntersectTaskPolygon()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters
		// intersect POLYGON with POLYGON layer and result POLYGON layer.
		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingIntersectMultiPolygon.shp";
		targetShp = TASK_PATH + "TargetMultiPolygonForIntersect.shp";

		isCreatingNewLayer = false;

		initTaskParameters();

		assertNotNull("Task must have a value.", task);
		assertNotNull("TargetStore must be created.", targetStore);

		// execute

		runTask();

		// obtain the result and check the data.

		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull(resultStore);

		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse("Collecction is empty.", fc.isEmpty());
			assertTrue(fc.size() > 0);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue(f.getDefaultGeometry().getClass() == MultiPolygon.class);
			}
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			if (it != null) {
				fc.close(it);
			}
		}
	}

	/**
	 * Intersects the MultiPolygon shapefile MultiPolygon2 whit the MultiPolygon
	 * shapefile UsingIntersectMultiPolygon and puts the result on the existent
	 * shapefile TargetMultiLineStringForIntersect.
	 * 
	 * Check that the featureStore isn't empty and the geometry of the resultant
	 * features is MultiLineString.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testIntersectTaskLineString()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters
		// intersect POLYGON with POLYGON layer and result LINESTRING layer.
		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingIntersectMultiPolygon.shp";
		targetShp = TASK_PATH + "TargetMultiLineStringForIntersect.shp";

		isCreatingNewLayer = false;

		initTaskParameters();

		assertNotNull("Task must have a value.", task);
		assertNotNull("TargetStore must be created.", targetStore);

		// execute

		runTask();

		// obtain the result and check the data.

		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull(resultStore);

		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse("Collecction is empty.", fc.isEmpty());
			assertTrue(fc.size() > 0);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue(f.getDefaultGeometry().getClass() == MultiLineString.class);
			}
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			if (it != null) {
				it.close();
			}
		}
	}

	/**
	 * Intersects the MultiPolygon shapefile MultiPolygon2 whit the
	 * MultiLineString shapefile UsingIntersectMultiLineString and puts the
	 * result on the existent shapefile TargetMultiLineStringForIntersect.
	 * 
	 * Check that the featureStore isn't empty and the geometry of the resultant
	 * features is MultiLineString.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testIntersectTaskNewLayerLineString1()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters
		// intersect POLYGON with LINESTRING layer and result LINESTRING layer.
		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingIntersectMultiLineString.shp";
		targetShp = TASK_PATH + "TargetMultiLineStringForIntersect.shp";

		// newShp = "newTargetIntersectMultiLineString2";
		// newTargetGeom = MultiLineString.class;
		// newTargetName = "newTargetIntersectMultiLineString2";

		isCreatingNewLayer = false;

		initTaskParameters();

		assertNotNull("Task must have a value.", task);
		assertNotNull("TargetStore must be created.", targetStore);

		// execute

		runTask();

		// obtain the result and check the data.

		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull(resultStore);

		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse("Collecction is empty.", fc.isEmpty());
			assertTrue("Size greater than 0.", fc.size() > 0);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue("Result class must be MultiLineString.",
							f.getDefaultGeometry().getClass() == MultiLineString.class);
			}
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			if (it != null) {
				it.close();
			}
		}
	}

	/**
	 * Intersects the MultiPolygon shapefile MultiPolygon2 whit the
	 * MultiLineString shapefile UsingIntersectMultiLineString and puts the
	 * result on the new shapefile newTargetIntersectMultiPoint1.
	 * 
	 * This intersect is not possible, so check the featureStore is empty.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testIntersectTaskNewLayerPoint1()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// CAN'T do intersect operation under those condition, so this must
		// return nothing.
		// initialize the parameters
		// intersect POLYGON with LINESTRING layer and result POINT layer.
		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingIntersectMultiLineString.shp";

		newShp = TASK_PATH + "newTargetIntersectMultiPoint1";
		newTargetGeom = MultiPoint.class;
		newTargetName = "newTargetIntersectMultiPoint1";

		isCreatingNewLayer = true;

		initTaskParameters();

		assertNotNull("Task must have a value.", task);
		assertNotNull("TargetStore must be created.", targetStore);

		// execute

		runTask();

		// obtain the result and check the data.

		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull(resultStore);

		SimpleFeatureCollection fc = null;

		try {
			fc = resultStore.getFeatures();

			assertTrue("Collecction is empty.", fc.isEmpty());
			assertTrue(fc.size() == 0);

		}
		catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Intersects the MultiPolygon shapefile MultiPolygon2 whit the MultiPoint
	 * shapefile UsingIntersectMultiPoint and puts the result on the existent
	 * shapefile TargetMultiPointForIntersect.
	 * 
	 * Check that the featureStore isn't empty and the geometry of the resultant
	 * features is MultiPoint.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testIntersectTaskPolygonPoint()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters
		// intersect POLYGON with POINT layer and result POINT layer.
		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingIntersectMultiPoint.shp";
		targetShp = TASK_PATH + "TargetMultiPointForIntersect.shp";

		// newShp = "newTargetIntersectMultiPoint2";
		// newTargetGeom = MultiPoint.class;
		// newTargetName = "newTargetIntersectMultiPoint2";

		isCreatingNewLayer = false;

		initTaskParameters();

		assertNotNull("Task must have a value.", task);
		assertNotNull("TargetStore must be created.", targetStore);

		// execute

		runTask();

		// obtain the result and check the data.

		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull(resultStore);

		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse("Collecction is empty.", fc.isEmpty());
			assertTrue(fc.size() > 0);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue(f.getDefaultGeometry().getClass() == MultiPoint.class);
			}
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			if (it != null) {
				it.close();
			}
		}
	}
}
