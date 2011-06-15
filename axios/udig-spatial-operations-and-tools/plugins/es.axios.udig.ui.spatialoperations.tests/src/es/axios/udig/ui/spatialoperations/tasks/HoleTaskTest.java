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
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.MultiPolygon;

import es.axios.udig.spatialoperations.tasks.IHoleTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.spatialoperations.ShapefileUtil.ShapeReader;

/**
 * <p>
 * Test class for HoleTask operation.
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
public class HoleTaskTest extends AbstractTaskTest {

	private String				firstShp			= "";
	private String				secondShp			= "";
	private String				targetShp			= "";
	private String				newShp				= "";
	private String				sourceName			= "";
	private String				targetLayerName		= "";

	private Boolean				isCreatingNewLayer	= null;

	private IHoleTask			task				= null;
	private SimpleFeatureStore		targetStore			= null;

	private static final String	TASK_PATH			= "HoleTask/";

	@Override
	protected Callable<FeatureStore<SimpleFeatureType,SimpleFeature>> getCurrentTask() {

		return task;
	}

	@Override
	protected void initTaskParameters() throws IOException, SchemaException {

		reader = new ShapeReader();

		SimpleFeatureCollection usingFeatures, sourceFeatures;

		sourceFeatures = reader.getFeatures(PATH + firstShp);
		usingFeatures = reader.getFeatures(PATH + secondShp);

		final CoordinateReferenceSystem sourceCrs = reader.getCRS(PATH + firstShp);
		final CoordinateReferenceSystem usingCrs = reader.getCRS(PATH + secondShp);
		// Use as mapCrs the sourceCrs
		final CoordinateReferenceSystem mapCrs = sourceCrs;

		if (isCreatingNewLayer) {
			SimpleFeatureType type = sourceFeatures.getSchema();
			targetStore = createNewTargetStore(PATH + newShp, type);
		} else {
			targetStore = createTargetStore(PATH + targetShp);
		}

		GeometryDescriptor targetGeomAttrType = targetStore.getSchema().getGeometryDescriptor();

		final CoordinateReferenceSystem targetCrs = targetGeomAttrType.getCoordinateReferenceSystem();

		IHoleTask task = SpatialOperationFactory.createHole(sourceFeatures, usingFeatures, targetStore, mapCrs,
					targetCrs, sourceCrs, usingCrs, isCreatingNewLayer, sourceName, targetLayerName);

		this.task = task;
	}

	@Test
	public void testHoleTask() throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = "MultiPolygon4.shp";
		secondShp = TASK_PATH + "UsingHoleMultiLineString.shp";
		targetShp = TASK_PATH + "TargetMultiPolygonForHole.shp";

		sourceName = "MultiPolygon4";
		targetLayerName = "TargetMultiPolygonForHole";

		isCreatingNewLayer = false;

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

	@Test
	public void testHoleInNewShapefile() throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = "MultiPolygon4.shp";
		secondShp = TASK_PATH + "UsingHoleMultiLineString.shp";
		newShp = TASK_PATH + "newTargetMultiPolygonForHole.shp";

		sourceName = "MultiPolygon4";
		targetLayerName = "newTargetMultiPolygonForHole";

		isCreatingNewLayer = true;

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

	@Test
	public void testHoleSameSourceAsTarget()
		throws IOException, SchemaException, InterruptedException, ExecutionException {

		// initialize the parameters

		firstShp = TASK_PATH + "MultiPolygon4AsTarget.shp";
		secondShp = TASK_PATH + "UsingHoleMultiLineString.shp";
		targetShp = TASK_PATH + "MultiPolygon4AsTarget.shp";

		sourceName = "MultiPolygon4AsTarget";
		targetLayerName = "MultiPolygon4AsTarget";

		isCreatingNewLayer = false;

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
