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

import com.vividsolutions.jts.geom.MultiLineString;

import es.axios.udig.spatialoperations.tasks.IPolygonToLineTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.spatialoperations.ShapefileUtil.ShapeReader;

/**
 * <p>
 * Test class for PolygonToLineTask.
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
public class PolygonToLineTaskTest extends AbstractTaskTest {

	private String				firstShp		= "";
	private String				targetShp		= "";
	private String				newShp			= "";
	private IPolygonToLineTask	task			= null;
	private SimpleFeatureStore		targetStore		= null;
	private Boolean				explode			= null;
	private Boolean				newShapefile	= null;

	private static final String	TASK_PATH		= "PolygonToLineTask/";

	@Override
	protected Callable<FeatureStore<SimpleFeatureType,SimpleFeature>>   getCurrentTask() {

		return task;
	}

	@Override
	protected void initTaskParameters() throws IOException, SchemaException {

		reader = new ShapeReader();

		final SimpleFeatureCollection featuresFromSource = reader.getFeatures(PATH + firstShp);
		final CoordinateReferenceSystem sourceLayerCrs = 
			featuresFromSource.getSchema()
				.getGeometryDescriptor()
					.getCoordinateReferenceSystem();

		if (newShapefile) {
			SimpleFeatureType type = createMultiLineStringFeatureType(TASK_PATH);
			targetStore = createNewTargetStore(PATH + newShp, type);
		} else {
			targetStore = createTargetStore(PATH + targetShp);
		}
		IPolygonToLineTask task = SpatialOperationFactory.createPolygonToLine(targetStore, featuresFromSource,
					sourceLayerCrs, explode);

		this.task = task;
	}

	/**
	 * 
	 * Before doing any test, assure the target shp is an empty shapefile. Empty
	 * shapefile for each operation can be found here:
	 * 
	 * resources/es/axios/udig/spatialoperations/task/EmptyShapefiles
	 * 
	 * Converts the shapefile MultiPolygon into lines and puts the result on the
	 * existent shapefile TargetMultiLineStringForPolygonToLine.
	 * 
	 * Checks the result featureStore isn't empty and the geometry of the
	 * features is MultiLineString.
	 * 
	 * @throws SchemaException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * 
	 */
	@Test
	public void testPolygonToLineWithoutExplode()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = "MultiPolygon.shp";
		targetShp = TASK_PATH + "TargetMultiLineStringForPolygonToLine.shp";

		newShapefile = false;
		explode = false;

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
				assertTrue(f.getDefaultGeometry().getClass() == MultiLineString.class);
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
	 * Converts the shapefile MultiPolygon into lines with explode(each side of
	 * the polygon feature will be converted into a single feature of 1
	 * lineString ) and puts the result on the existent shapefile
	 * TargetMultiLineStringForPolygonToLineExplode.
	 * 
	 * Checks the result featureStore isn't empty, count the features that must
	 * be 43 lines and the geometry of the features is MultiLineString.
	 * 
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testExplode() throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = "MultiPolygon.shp";
		targetShp = TASK_PATH + "TargetMultiLineStringForPolygonToLineExplode.shp";

		newShapefile = false;
		explode = true;

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
			assertTrue(fc.size() == 43);

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
	 * Converts the shapefile MultiPolygon into lines and puts the result on the
	 * new shapefile TargetNewForPolygonToLine.
	 * 
	 * Checks the result featureStore isn't empty and the geometry of the
	 * features is MultiLineString.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testPolygonToLineInNewShapefile()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = "MultiPolygon.shp";

		newShp = TASK_PATH + "TargetNewForPolygonToLine.shp";

		newShapefile = true;
		explode = false;

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

}
