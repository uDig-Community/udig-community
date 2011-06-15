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

import com.vividsolutions.jts.geom.MultiPolygon;

import es.axios.udig.spatialoperations.tasks.IFillTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.spatialoperations.ShapefileUtil.ShapeReader;

/**
 * <p>
 * Test class for FillTask.
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
 * 
 */
public class FillTaskTest extends AbstractTaskTest {

	private String				firstShp			= "";
	private String				secondShp			= "";
	private String				targetShp			= "";
	private String				newShp				= "";
	private IFillTask			task				= null;
	private SimpleFeatureStore		targetStore			= null;
	private Boolean				isCreatingNewLayer	= null;

	private static final String	TASK_PATH			= "FillTask/";


	@Override
	protected Callable<FeatureStore<SimpleFeatureType, SimpleFeature>> getCurrentTask() {
		return this.task;
	}

	@Override
	protected void initTaskParameters() throws IOException, SchemaException {

		reader = new ShapeReader();

		SimpleFeatureCollection featuresInFirstLayer, featuresInSecondLayer;

		featuresInFirstLayer = reader.getFeatures(PATH + firstShp);
		featuresInSecondLayer = reader.getFeatures(PATH + secondShp);

		final CoordinateReferenceSystem firstLayerCrs = reader.getCRS(PATH + firstShp);
		final CoordinateReferenceSystem secondLayerCrs = reader.getCRS(PATH + secondShp);
		CoordinateReferenceSystem targetCrs = null;
		// Don't have a Map, so the mapCrs will be the same as the targetCrs.

		if (isCreatingNewLayer) {
			SimpleFeatureType type = featuresInFirstLayer.getSchema();
			targetStore = createNewTargetStore(PATH + newShp, type);
			targetCrs = reader.getCRS(PATH + newShp);
		} else {
			targetStore = createTargetStore(PATH + targetShp);
			targetCrs = reader.getCRS(PATH + targetShp);
		}

		final CoordinateReferenceSystem mapCrs = targetCrs;

		IFillTask task = SpatialOperationFactory.createFill(targetStore, featuresInFirstLayer, featuresInSecondLayer,
					firstLayerCrs, mapCrs, secondLayerCrs, targetCrs, false);

		this.task = task;
	}

	/**
	 * Fill operation, using the source shapefile MultiPolygonForFill and the
	 * reference shapefile UsingMultiLineStringForFill, do the fill operation
	 * and put the result on the new shapefile newTargetFill_3;
	 * 
	 * Checks the result featureStore isn't empty and the geometry of the
	 * features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testFillTask() throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = TASK_PATH + "MultiPolygonForFill.shp";
		secondShp = TASK_PATH + "UsingMultiLineStringForFill.shp";
		newShp = TASK_PATH + "newTargetFill_3.shp";

		isCreatingNewLayer = true;

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.
		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull("Result store can not be null.", resultStore);
		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse("Collection is empty.", fc.isEmpty());
			assertTrue("Collection size 0.", fc.size() > 0);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue("Geometry isn't m.polygon.", f.getDefaultGeometry().getClass() == MultiPolygon.class);
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
	 * Fill operation, using the source shapefile MultiPolygonForFillAsTarget
	 * and the reference shapefile UsingMultiLineStringForFill, do the fill
	 * operation and put the result on the source shapefile
	 * MultiPolygonForFillAsTarget.
	 * 
	 * Checks the result featureStore isn't empty and the geometry of the
	 * features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testFillTaskTargetSameAsSource()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = TASK_PATH + "MultiPolygonForFillAsTarget.shp";
		secondShp = TASK_PATH + "UsingMultiLineStringForFill.shp";
		targetShp = TASK_PATH + "MultiPolygonForFillAsTarget.shp";

		isCreatingNewLayer = false;

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.
		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull("Result store can not be null.", resultStore);
		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse("Collection is empty.", fc.isEmpty());
			assertTrue("Collection size 0.", fc.size() > 0);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue("Geometry isn't m.polygon.", f.getDefaultGeometry().getClass() == MultiPolygon.class);
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
	 * Fill operation, using the source shapefile MultiPolygonForFill3 and the
	 * reference shapefile UsingMultiLineStringForFill3, do the fill operation
	 * and put the result on the new shapefile newTargetFill.
	 * 
	 * Checks the result featureStore isn't empty and the geometry of the
	 * features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testFillTask2() throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = TASK_PATH + "MultiPolygonForFill3.shp";
		secondShp = TASK_PATH + "UsingMultiLineStringForFill3.shp";
		newShp = TASK_PATH + "newTargetFill.shp";

		isCreatingNewLayer = true;

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.
		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull("Result store can not be null.", resultStore);
		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse("Collection is empty.", fc.isEmpty());
			assertTrue("Collection size 0.", fc.size() > 0);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue("Geometry isn't m.polygon.", f.getDefaultGeometry().getClass() == MultiPolygon.class);
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
	 * Fill operation, using the source shapefile MultiPolygonForFill2 and the
	 * reference shapefile UsingMultiLineStringForFill2, do the fill operation
	 * and put the result on the existent shapefile TargetMultiPolygonForFill.
	 * 
	 * Checks the result featureStore isn't empty and the geometry of the
	 * features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testFillTaskInExistentLayer()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = TASK_PATH + "MultiPolygonForFill2.shp";
		secondShp = TASK_PATH + "UsingMultiLineStringForFill2.shp";
		targetShp = TASK_PATH + "TargetMultiPolygonForFill.shp";

		isCreatingNewLayer = false;

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.
		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull("Result store can not be null.", resultStore);
		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse("Collection is empty.", fc.isEmpty());
			assertTrue("Collection size 0.", fc.size() > 0);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue("Geometry isn't m.polygon.", f.getDefaultGeometry().getClass() == MultiPolygon.class);
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
	 * Fill operation, using the source shapefile MultiPolygonForFill2AsTarget
	 * and the reference shapefile UsingMultiLineStringForFill2, do the fill
	 * operation and put the result on the source shapefile
	 * MultiPolygonForFill2AsTarget.
	 * 
	 * Checks the result featureStore isn't empty and the geometry of the
	 * features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testFillTaskTargetSameAsSource2()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = TASK_PATH + "MultiPolygonForFill2AsTarget.shp";
		secondShp = TASK_PATH + "UsingMultiLineStringForFill2.shp";
		targetShp = TASK_PATH + "MultiPolygonForFill2AsTarget.shp";

		isCreatingNewLayer = false;

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.
		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull("Result store can not be null.", resultStore);
		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse("Collection is empty.", fc.isEmpty());
			assertTrue("Collection size 0.", fc.size() > 0);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue("Geometry isn't m.polygon.", f.getDefaultGeometry().getClass() == MultiPolygon.class);
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
