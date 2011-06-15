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
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

import es.axios.geotools.util.FeatureUtil;
import es.axios.udig.spatialoperations.tasks.IDissolveTask;
import es.axios.udig.spatialoperations.tasks.SpatialOperationException;
import es.axios.udig.spatialoperations.tasks.SpatialOperationFactory;
import es.axios.udig.ui.spatialoperations.ShapefileUtil.ShapeReader;

/**
 * <p>
 * Test class for DissolveTask operation.
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
public class DissolveTaskTest extends AbstractTaskTest {

	private String 				firstShp = "";
	private String 				targetShp = "";
	private String 				newShp = "";
	private List<String> 		dissolveProperties = new LinkedList<String>();

	private Boolean 			isCreatingNewLayer = null;

	private IDissolveTask 		task = null;
	private SimpleFeatureStore 	targetStore = null;
	private org.opengis.filter.Filter filter = null;

	private static final String TASK_PATH = "DissolveTask/";

	@Override
	protected Callable<FeatureStore<SimpleFeatureType, SimpleFeature>> getCurrentTask() {

		return task;
	}

	@Override
	protected void initTaskParameters() throws IOException, SchemaException, SpatialOperationException {

		reader = new ShapeReader();

		SimpleFeatureSource featureSource = reader.getFeatureSource(PATH + firstShp);
		// use the source CRS because haven't got a map.
		final CoordinateReferenceSystem mapCrs = reader.getCRS(PATH + firstShp);

		if (isCreatingNewLayer) {
			SimpleFeatureType schema = featureSource.getSchema();
			SimpleFeatureTypeBuilder ftbuilder = FeatureUtil.createDefaultFeatureType(newShp, mapCrs, MultiPolygon.class);
			ftbuilder.add(schema.getDescriptor(this.dissolveProperties.get(0)));
			
			targetStore = createNewTargetStore(PATH + newShp, ftbuilder.buildFeatureType()); // FIXME
		} else {
			targetStore = createTargetStore(PATH + targetShp);
		}

		IDissolveTask task = SpatialOperationFactory.createDissolve(
					featureSource, this.filter, this.dissolveProperties, 
					mapCrs,
					targetStore, 
					featureSource.getSchema().getCoordinateReferenceSystem(), 
					mapCrs);

		this.task = task;
	}

	/**
	 * Dissolve using the property "name" from the source shapefile
	 * MultiPolygon3 and put the result on the existent shapefile
	 * TargetMultiPolygonForDissolve1.
	 * 
	 * Check that the featureStore isn't empty, its has 2 features and the
	 * geometry of the resultant features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testDissolvePolygonsByName()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiPolygon3.shp";
		targetShp = TASK_PATH + "TargetMultiPolygonForDissolve1.shp";
		newShp = TASK_PATH + "newTargetDissolve.shp";
		isCreatingNewLayer = true;
		
		filter = Filter.INCLUDE;
		dissolveProperties.add("name");

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.
		SimpleFeatureStore resultStore  = this.targetStore;

		assertNotNull(resultStore);

		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse(fc.isEmpty());
			assertEquals(2, fc.size());

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
	 * Dissolve using the property "AtributoA" from the source shapefile
	 * MultiPolygon3 and put the result on the existent shapefile
	 * TargetMultiPolygonForDissolve2.
	 * 
	 * Check that the featureStore isn't empty, its has 3 features and the
	 * geometry of the resultant features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testDissolvePolygonsByAtributoA()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiPolygon3.shp";
		targetShp = TASK_PATH + "TargetMultiPolygonForDissolve2.shp";

		isCreatingNewLayer = false;
		filter = Filter.INCLUDE;
		dissolveProperties.add("AtributoA");

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.

		SimpleFeatureStore resultStore = (SimpleFeatureStore) future.get();
		assertNotNull(resultStore);

		SimpleFeatureIterator it = null;
		try {
			SimpleFeatureCollection fc = resultStore.getFeatures();

			assertFalse(fc.isEmpty());
			assertEquals(3, fc.size() );

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
	 * Dissolve using the property "AtributoB" from the source shapefile
	 * MultiPolygon3 and put the result on the existent shapefile
	 * TargetMultiPolygonForDissolve3.
	 * 
	 * Check that the featureStore isn't empty, its has 4 features and the
	 * geometry of the resultant features is MultiPolygon.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testDissolvePolygonsByAtributoB()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiPolygon3.shp";
		targetShp = TASK_PATH + "TargetMultiPolygonForDissolve3.shp";

		isCreatingNewLayer = false;
		filter = Filter.INCLUDE;
		dissolveProperties.add("AtributoB");

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
			assertEquals(4, fc.size());

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
	 * Dissolve using the property "name" from the source shapefile
	 * MultiLineString1 and put the result on the existent shapefile
	 * TargetMultiLineStringForDissolve1.
	 * 
	 * Check that the featureStore isn't empty, its has 2 features and the
	 * geometry of the resultant features is MultiLineString.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testDissolveLinesByName()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiLineString1.shp";
		targetShp = TASK_PATH + "TargetMultiLineStringForDissolve1.shp";

		isCreatingNewLayer = false;
		filter = Filter.INCLUDE;
		dissolveProperties.add("name");

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
			assertEquals(2, fc.size());

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
	 * Dissolve using the property "AtributoA" from the source shapefile
	 * MultiLineString1 and put the result on the existent shapefile
	 * TargetMultiLineStringForDissolve2.
	 * 
	 * Check that the featureStore isn't empty, its has 3 features and the
	 * geometry of the resultant features is MultiLineString.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testDissolveLinesByAtributoA()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiLineString1.shp";
		targetShp = TASK_PATH + "TargetMultiLineStringForDissolve2.shp";

		isCreatingNewLayer = false;
		filter = Filter.INCLUDE;
		dissolveProperties.add("AtributoA");

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
			assertEquals(3, fc.size());

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
	 * Dissolve using the property "AtributoB" from the source shapefile
	 * MultiLineString1 and put the result on the new shapefile
	 * newTargetDissolveMultiLineString1.
	 * 
	 * Check that the featureStore isn't empty, its has 4 features and the
	 * geometry of the resultant features is MultiLineString.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws SpatialOperationException
	 */
	@Test
	public void testDissolveLinesByAtributoBnewShape()
		throws IOException, SchemaException, InterruptedException, ExecutionException, SpatialOperationException {

		// initialize the parameters

		firstShp = "MultiLineString1.shp";
		newShp = TASK_PATH + "newTargetDissolveMultiLineString1";

		isCreatingNewLayer = true;
		filter = Filter.INCLUDE;
		dissolveProperties.add("AtributoB");

		initTaskParameters();

		assertNotNull(task);
		assertNotNull(targetStore);

		// execute

		runTask();

		// obtain the result and check the data.

		SimpleFeatureStore resultStore = this.targetStore;//(SimpleFeatureStore) future.get();

		assertNotNull(resultStore);

		SimpleFeatureCollection fc = null;
		SimpleFeatureIterator it = null;

		try {
			fc = resultStore.getFeatures();

			assertFalse(fc.isEmpty());
			assertEquals(4, fc.size());

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

}
