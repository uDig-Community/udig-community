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

import es.axios.udig.spatialoperations.tasks.ISplitTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.spatialoperations.ShapefileUtil.ShapeReader;

/**
 * <p>
 * Test class for SplitTask.
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
public class SplitTaskTest extends AbstractTaskTest {

	private String				firstShp			= "";
	private String				secondShp			= "";
	private String				targetShp			= "";
	private ISplitTask			task				= null;
	private SimpleFeatureStore		targetStore			= null;
	private Boolean				isCreatingNewLayer	= null;
	private String				layerToSplitName	= "";
	private String				targetLayerName		= "";
	private String				newShp				= "";

	private static final String	TASK_PATH			= "SplitTask/";

	@Override
	protected Callable<FeatureStore<SimpleFeatureType,SimpleFeature>> getCurrentTask() {

		return task;
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

		ISplitTask task = SpatialOperationFactory.createSplit(targetStore, featuresInFirstLayer, featuresInSecondLayer,
					firstLayerCrs, mapCrs, isCreatingNewLayer, secondLayerCrs, targetCrs, layerToSplitName,
					targetLayerName);

		this.task = task;
	}

	/**
	 * Splits the shapefile MultiPolygon2 using the
	 * UsingIntersectMultiLineString and puts the result on the existent
	 * shapefile TargetMultiPolygonForSplit.
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
	public void testSplitTask() throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingIntersectMultiLineString.shp";
		targetShp = TASK_PATH + "TargetMultiPolygonForSplit.shp";

		isCreatingNewLayer = false;
		layerToSplitName = "MultiPolygon";
		targetLayerName = "TargetMultiPolygonForSplit";

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.
		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull(resultStore);
		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse(fc.isEmpty());
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
				it.close();
			}
		}

	}

	/**
	 * Splits the shapefile MultiPolygon2 using the
	 * UsingIntersectMultiLineString and puts the result on the new shapefile
	 * newTargetMultiPolygonForSplit.
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
	public void testSplitTaskInNewShapefile()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingIntersectMultiLineString.shp";
		// targetShp = "TargetMultiPolygonForSplit.shp";
		newShp = TASK_PATH + "newTargetMultiPolygonForSplit.shp";

		isCreatingNewLayer = true;

		layerToSplitName = "MultiPolygon2";
		targetLayerName = "newTargetMultiPolygonForSplit";

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.
		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull(resultStore);
		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse(fc.isEmpty());
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
				it.close();
			}
		}

	}

	/**
	 * Splits the shapefile MultiPolygon2 using the
	 * UsingIntersectMultiLineString and puts the result on the new shapefile
	 * newTargetMultiPolygonForSplit.
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
	public void testSplitTaskInNewShapefile2()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = "SourcePolygon.shp";
		secondShp = TASK_PATH + "UsingLineString1.shp";
		// targetShp = "TargetMultiPolygonForSplit.shp";
		newShp = TASK_PATH + "newTestForSplit2.shp";

		isCreatingNewLayer = true;

		layerToSplitName = "SourcePolygon";
		targetLayerName = "UsingLineString1";

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.
		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();

		assertNotNull(resultStore);
		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse(fc.isEmpty());
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
				it.close();
			}
		}
	}
}
