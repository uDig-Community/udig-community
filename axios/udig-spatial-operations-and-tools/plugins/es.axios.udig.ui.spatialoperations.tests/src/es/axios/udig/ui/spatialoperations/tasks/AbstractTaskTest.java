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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import es.axios.udig.spatialoperations.tasks.SpatialOperationException;
import es.axios.udig.ui.spatialoperations.ShapefileUtil.ShapeReader;

/**
 * <p>
 * 
 * Common part for all the task. Initializes the task parameters and run it.
 * 
 * Before doing any test, assure the target shp is an empty shapefile. Empty
 * shapefile for each operation can be found here:
 * 
 * resources/es/axios/udig/spatialoperations/task/EmptyShapefiles
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
public abstract class AbstractTaskTest {

	protected static String			PATH	= "resources/es/axios/udig/spatialoperations/task/"; //$NON-NLS-1$
	protected ShapeReader			reader;
	protected Future<FeatureStore<SimpleFeatureType, SimpleFeature>>	future	= null;

	/**
	 * Initializes the shapeReader and for each subclass initializes the
	 * specific parameters for the task.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 * @throws SpatialOperationException
	 */
	protected abstract void initTaskParameters() throws IOException, SchemaException, SpatialOperationException;

	/**
	 * 
	 * @return The specific task.
	 */
	protected abstract Callable<FeatureStore<SimpleFeatureType,SimpleFeature>>  getCurrentTask();

	/**
	 * Get the current task and run it. Assert it finished.
	 * 
	 * @throws InterruptedException
	 */
	protected void runTask() throws InterruptedException {

		ExecutorService executor = Executors.newCachedThreadPool();

		// obtain the current task
		Callable<FeatureStore<SimpleFeatureType,SimpleFeature>>  task = getCurrentTask();

		this.future = executor.submit(task);

		assertNotNull(future);

		while (!future.isDone() && !future.isCancelled()) {

			Thread.sleep(100);
		}

		assertFalse(future.isCancelled());
		assertTrue(future.isDone());
	}
	/**
	 * Return the FeatureStore of the existent target shapefile.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws SchemaException
	 */
	protected SimpleFeatureStore createTargetStore(String path) throws IOException, SchemaException {

		SimpleFeatureStore store = null;

		File f = new File(path);
		URL shapeURL = f.toURI().toURL();

		ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

		DataStore dataStore = factory.createDataStore(shapeURL);
		String name = dataStore.getTypeNames()[0];

		store = (SimpleFeatureStore) dataStore.getFeatureSource(name);

		return store;
	}

	/**
	 * Creates a new FeatureStore. Creates a new indexedShapeFile and attach an
	 * existent featureType to it.
	 * 
	 * @param path
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws SchemaException
	 */
	protected SimpleFeatureStore createNewTargetStore(String path, SimpleFeatureType type) throws IOException, SchemaException {

		SimpleFeatureStore store = null;
		File f = new File(path);
		URL shapeURL = f.toURI().toURL();
		 
		Map<String, Serializable> connectParameters = new HashMap<String, Serializable>();
		connectParameters.put("url", shapeURL); //$NON-NLS-1$
		connectParameters.put("create spatial index", Boolean.TRUE); //$NON-NLS-1$

		ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
		DataStore dataStore = factory.createNewDataStore(connectParameters);

		dataStore.createSchema(type);
		String name = dataStore.getTypeNames()[0];

		store = (SimpleFeatureStore) dataStore.getFeatureSource(name);

		return store;
	}

	/**
	 * 
	 * @deprecated Don't use it, only for test when indexed shapeFile doesn't
	 *             work.
	 */
	protected SimpleFeatureStore createNewTargetStore2(String path, SimpleFeatureType type) throws IOException, SchemaException {

		SimpleFeatureStore store = null;
		File f = new File(path);
		URL shapeURL = f.toURL();

		ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

		Map<String, Serializable> connectParameters = new HashMap<String, Serializable>();

		connectParameters.put("url", shapeURL); //$NON-NLS-1$
		connectParameters.put("memory mapped buffer", true); //$NON-NLS-1$

		DataStore dataStore = factory.createNewDataStore(connectParameters);
		dataStore.createSchema(type);
		String name = dataStore.getTypeNames()[0];

		store = (SimpleFeatureStore) dataStore.getFeatureSource(name);

		return store;
	}

	/**
	 * Get the FeatureType from a MultiLineString shapefile.
	 * 
	 * @param task_path
	 * 
	 * @return
	 * @throws IOException
	 */
	protected SimpleFeatureType createMultiLineStringFeatureType(String task_path) throws IOException {

		File f = new File(PATH + task_path + "TargetMultiLineStringForPolygonToLine"); //$NON-NLS-1$
		URL shapeURL = f.toURI().toURL();

		ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
		DataStore dataStore = factory.createDataStore(shapeURL);
		String name = dataStore.getTypeNames()[0];

		return dataStore.getSchema(name);
	}

	/**
	 * Return a MultiPolygon FeatureType obtained from a MultiPolygon shapefile.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected SimpleFeatureType createMultiPolygonFeatureType() throws IOException {

		File f = new File(PATH + "MultiPolygon2.shp"); //$NON-NLS-1$
		URL shapeURL = f.toURI().toURL();

		ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();

		DataStore dataStore = factory.createDataStore(shapeURL);
		String name = dataStore.getTypeNames()[0];

		return dataStore.getSchema(name);
	}
}
