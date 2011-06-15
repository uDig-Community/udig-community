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

import es.axios.udig.spatialoperations.tasks.ISpatialJoinTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationException;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.spatialoperations.tasks.SpatialRelation;
import es.axios.udig.ui.spatialoperations.ShapefileUtil.ShapeReader;

/**
 * <p>
 * Test class for SpatialJoinTask operation.
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
public class SpatialJoinTaskTest extends AbstractTaskTest {

	private String							firstShp			= "";
	private String							secondShp			= "";
	private String							targetShp			= "";
	private String							newShp				= "";

	private SpatialRelation					spatialRelation		= null;

	private Boolean							isCreatingNewLayer	= null;

	private ISpatialJoinTask<FeatureStore<SimpleFeatureType,SimpleFeature>>	task				= null;
	private SimpleFeatureStore					targetStore			= null;

	private static final String				TASK_PATH			= "SpatialJoinTask/";

	@Override
	protected Callable<FeatureStore<SimpleFeatureType,SimpleFeature>> getCurrentTask() {

		return task;
	}

	@Override
	protected void initTaskParameters() throws IOException, SchemaException, SpatialOperationException {

		reader = new ShapeReader();

		SimpleFeatureCollection firstSource, secondSource;

		firstSource = reader.getFeatures(PATH + firstShp);
		secondSource = reader.getFeatures(PATH + secondShp);

		final CoordinateReferenceSystem mapCrs = reader.getCRS(PATH + firstShp);
		final CoordinateReferenceSystem sourceCrs = firstSource.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
		final CoordinateReferenceSystem secondCrs = secondSource.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();

		if (isCreatingNewLayer) {
			SimpleFeatureType type = (SimpleFeatureType) reader.getFeatureType(PATH + firstShp);
			targetStore = createNewTargetStore(PATH + newShp, type);
		} else {
			targetStore = createTargetStore(PATH + targetShp);
		}
		final CoordinateReferenceSystem targetCrs = targetStore.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
		ISpatialJoinTask<?> task = SpatialOperationFactory.createSpatialJoin(firstSource, secondSource,
					spatialRelation, mapCrs, targetStore, sourceCrs, secondCrs, targetCrs);

		this.task = (ISpatialJoinTask<FeatureStore<SimpleFeatureType, SimpleFeature>>) task;
	}

	/**
	 * Spatial Join between the shapefile MultiPolygon2 and
	 * UsingSpatialJoinMultiPolygon shapefile. Creates a new shapefile.
	 * 
	 * The relation = Contains.
	 * 
	 * Check that the featureStore isn't empty, there 4 features and the
	 * geometry of the resultant features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testSpatialJoinTaskContains()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingSpatialJoinMultiPolygon.shp";
		newShp = TASK_PATH + "newTargetJoinContains.shp";

		isCreatingNewLayer = true;

		spatialRelation = SpatialRelation.Contains;

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
			assertTrue(fc.size() == 4);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue(f.getDefaultGeometry().getClass() == MultiPolygon.class);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}

	/**
	 * Spatial Join between the shapefile MultiPolygon2 and
	 * UsingSpatialJoinMultiPolygon shapefile. Creates a new shapefile.
	 * 
	 * The relation = Crosses.
	 * 
	 * Check that the featureStore isn't empty, there 2 features and the
	 * geometry of the resultant features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testSpatialJoinTaskCrosses()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingSpatialJoinMultiPolygon.shp";
		newShp = TASK_PATH + "newTargetJoinCrosses.shp";

		isCreatingNewLayer = true;

		spatialRelation = SpatialRelation.Crosses;

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
			assertTrue(fc.size() == 2);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue(f.getDefaultGeometry().getClass() == MultiPolygon.class);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}

	/**
	 * Spatial Join between the shapefile MultiPolygon2 and
	 * UsingSpatialJoinMultiPolygon shapefile. Creates a new shapefile.
	 * 
	 * The relation = Touch.
	 * 
	 * Check that the featureStore isn't empty, there 2 features and the
	 * geometry of the resultant features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testSpatialJoinTaskTouches()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingSpatialJoinMultiPolygon.shp";
		newShp = TASK_PATH + "newTargetJoinTouch.shp";

		isCreatingNewLayer = true;

		spatialRelation = SpatialRelation.Touches;

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
			assertTrue(fc.size() == 2);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue(f.getDefaultGeometry().getClass() == MultiPolygon.class);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}

	/**
	 * Spatial Join between the shapefile MultiPolygon2 and
	 * UsingSpatialJoinMultiPolygon shapefile. Creates a new shapefile.
	 * 
	 * The relation = Disjoint.
	 * 
	 * Check that the featureStore isn't empty, there 2 features and the
	 * geometry of the resultant features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testSpatialJoinTaskDisjoint()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingSpatialJoinMultiPolygon.shp";
		newShp = TASK_PATH + "newTargetJoinDisjoint.shp";

		isCreatingNewLayer = true;

		spatialRelation = SpatialRelation.Disjoint;

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
			assertTrue(fc.size() == 2);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue(f.getDefaultGeometry().getClass() == MultiPolygon.class);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}

	/**
	 * Spatial Join between the shapefile MultiPolygon2 and
	 * UsingSpatialJoinMultiPolygon shapefile. Put the result on existent
	 * 
	 * The relation = Disjoint.
	 * 
	 * Check that the featureStore isn't empty, there 2 features and the
	 * geometry of the resultant features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testSpatialJoinTaskDisjointOnExistent()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiPolygon2.shp";
		secondShp = TASK_PATH + "UsingSpatialJoinMultiPolygon.shp";
		targetShp = TASK_PATH + "TargetMultiPolygonForSpatialJoin.shp";

		isCreatingNewLayer = false;

		spatialRelation = SpatialRelation.Disjoint;

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
			assertTrue(fc.size() == 2);

			it = fc.features();
			while (it.hasNext()) {

				SimpleFeature f = it.next();
				assertTrue(f.getDefaultGeometry().getClass() == MultiPolygon.class);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}
}
