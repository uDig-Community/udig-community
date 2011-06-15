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
package es.axios.udig.ui.spatialoperations.ShapefileUtil;

import java.io.File;
import java.net.URL;

import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Class used as demo, for looking code an get oriented.
 * 
 * @author Aritz Davila (www.axios.es)
 * 
 */
public class ShapeReaderWriter {

	public static void main(String[] args) {
		try {

			String newStoreURLString; // url for output shapefile
			boolean createOutput; // decide if an output shapefile should be
									// created

			// get the shapefile URL by either loading it from the file system
			// or from the classpath
			URL shapeURL = null;

			File f = new File("resources/es/axios/udig/spatialoperations/task/MultiLineStringTest1.shp");
			shapeURL = f.toURL();

			// generate new shapefile filename by prepending "new_"
			newStoreURLString = shapeURL.toString().substring(0, shapeURL.toString().lastIndexOf("/") + 1) + "new_" //$NON-NLS-1$ //$NON-NLS-2$
						+ shapeURL.toString().substring(shapeURL.toString().lastIndexOf("/") + 1); //$NON-NLS-1$
			createOutput = true;

			if (shapeURL == null) {
				System.err.println("Please specify a shape file."); //$NON-NLS-1$
				System.exit(-1);
			}

			// get feature results
			ShapefileDataStore store = new ShapefileDataStore(shapeURL);
			String name = store.getTypeNames()[0];
			SimpleFeatureSource source = store.getFeatureSource(name);
			SimpleFeatureCollection fsShape = source.getFeatures();
			
			// get feature type to create new shapefile
			SimpleFeatureType ft = source.getSchema();


			if (createOutput) {
				// now print out the feature contents (including geometric
				// attribute)

				// create new shapefile data store
				ShapefileDataStore newShapefileDataStore = new ShapefileDataStore(new URL(newStoreURLString));

				// create the schema using from the original shapefile
				newShapefileDataStore.createSchema(ft);

				// grab the data source from the new shapefile data store
				SimpleFeatureSource newFeatureSource = newShapefileDataStore.getFeatureSource(name);

				// downcast FeatureSource to specific implementation of
				// FeatureStore
				SimpleFeatureStore newFeatureStore = (SimpleFeatureStore) newFeatureSource;

				// accquire a transaction to create the shapefile from
				// FeatureStore
				Transaction t = newFeatureStore.getTransaction();

				// add features got from the query (FeatureReader)
				newFeatureStore.addFeatures(fsShape);

				// filteredReader is now exhausted and closed, commit the
				// changes
				t.commit();
				t.close();
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

}
