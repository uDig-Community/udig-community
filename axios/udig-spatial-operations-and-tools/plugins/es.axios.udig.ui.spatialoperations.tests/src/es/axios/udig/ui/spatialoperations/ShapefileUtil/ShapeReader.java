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
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;


import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Class used as demo, for looking code an get oriented
 * 
 * Also has public method for read a shapefile, and get properties of it.
 * 
 * @author Aritz Davila (www.axios.es)
 * 
 */
public class ShapeReader {

	public static void main(String[] args) {
		try {
			// get the shapefile URL by either loading it from the file system
			// or from the classpath
			URL shapeURL = null;

			File f = new File("resources/es/axios/udig/spatialoperations/task/TargetMultiLineString.shp");
			shapeURL = f.toURL();
			// get feature results
			ShapefileDataStore store = new ShapefileDataStore(shapeURL);
			String name = store.getTypeNames()[0];
			SimpleFeatureSource source = store.getFeatureSource(name);
			SimpleFeatureCollection fsShape = source.getFeatures();

			// print out a feature type header and wait for user input
			FeatureType ft = source.getSchema();

			printFeatureType(ft);

			// now print out the feature contents (every non geometric
			// attribute)

			SimpleFeatureIterator iter = fsShape.features();
			while (iter.hasNext()) {
				Feature feature = iter.next();
				System.out.print(feature.getIdentifier().getID() + "\t"); //$NON-NLS-1$

				for (Property prop: feature.getProperties()) {
					
					Object propValue = prop.getValue();

					if (!(propValue instanceof Geometry)) {
						System.out.print(propValue + "\t"); //$NON-NLS-1$
					}
				}

				System.out.println();
			}
			iter.close();
			
			System.out.println();
			System.out.println();
			System.out.println();

			// and finally print out every geometry in wkt format
			SimpleFeatureIterator geomIter = fsShape.features();
			while (iter.hasNext()) {

				Feature feature = geomIter.next();
				System.out.print(feature.getIdentifier().getID() + "\t"); //$NON-NLS-1$
				System.out.println(feature.getDefaultGeometryProperty().getValue());
				System.out.println();
			}
			geomIter.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	private static void printFeatureType(FeatureType ft) {
		
		System.out.println("FID\t"); //$NON-NLS-1$
		
		for (PropertyDescriptor propDesc: ft.getDescriptors()) {
			PropertyType at = propDesc.getType();

			if (!Geometry.class.isAssignableFrom(at.getClass())) {
				System.out.print(at.getClass().getName() + "\t"); //$NON-NLS-1$
			}
		}
		System.out.println();
	}

	private FeatureType	sourceFeatureType;

	/**
	 * Return the feature collection from a shapefile.
	 * 
	 * @return
	 * @throws IOException
	 */
	public SimpleFeatureCollection getFeatures(String path) throws IOException {

		File f = new File(path);
		URL shapeURL = f.toURL();

		// get feature results
		ShapefileDataStore store = new ShapefileDataStore(shapeURL);
		String name = store.getTypeNames()[0];
		SimpleFeatureSource source = store.getFeatureSource(name);

		return source.getFeatures();
	}

	/**
	 * Get the featureSource from a shapefile.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public SimpleFeatureSource getFeatureSource(String path) throws IOException {

		File f = new File(path);
		URL shapeURL = f.toURL();

		// get feature results
		ShapefileDataStore store = new ShapefileDataStore(shapeURL);
		String name = store.getTypeNames()[0];
		return store.getFeatureSource(name);
	}

	/**
	 * Get the CRS from a shapefile.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public CoordinateReferenceSystem getCRS(String path) throws IOException {

		File f = new File(path);
		URL shapeURL = f.toURL();
		// get feature type
		ShapefileDataStore store = new ShapefileDataStore(shapeURL);
		String name = store.getTypeNames()[0];
		FeatureType featureType = store.getFeatureSource(name).getSchema();

		return featureType.getGeometryDescriptor().getCoordinateReferenceSystem();
	}

	/**
	 * Get the feature type from an existent shapefile.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public FeatureType getFeatureType(String path) throws IOException {

		File f = new File(path);
		URL shapeURL = f.toURL();
		// get feature type
		ShapefileDataStore store = new ShapefileDataStore(shapeURL);
		String name = store.getTypeNames()[0];
		return store.getFeatureSource(name).getSchema();
	}

	/**
	 * 
	 * @return
	 * @deprecated
	 */
	public FeatureType getSourceFeatureType() {

		return this.sourceFeatureType;
	}
}
