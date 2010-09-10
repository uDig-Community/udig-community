package net.refractions.wkt;

import java.util.ArrayList;
import java.util.Collection;

import net.refractions.udig.project.ILayer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * Super class for pasting WKT into a new layer.  This class
 * provides the functionality to parse features.
 * 
 * @author Emily
 *
 */
public abstract class PasteWKT {

	protected static WKTReader reader = new WKTReader();
	protected String errorString = ""; //$NON-NLS-1$
	
	/**
	 * Parses data string into features.
	 * <p>
	 * This function updates the errorString if errors occur parsing features.
	 * </p>
	 * 
	 * @param data the wkt data string; features must be separated by line feed
	 * @param featureBuilder the builder to use for building features
	 * @return
	 */
	protected Collection<SimpleFeature> getFeatures(String data, SimpleFeatureBuilder featureBuilder){
		
		Collection<SimpleFeature> featuresToAdd = new ArrayList<SimpleFeature>();
		String features[] = data.split("[\r\n]+"); //$NON-NLS-1$
		
		for (int i = 0; i < features.length; i++) {
			String feature = features[i];
			try {
				Geometry g = reader.read(feature);
				SimpleFeature f = featureBuilder.buildFeature(null);
				f.setDefaultGeometry(g);
				featuresToAdd.add(f);
			} catch (ParseException e) {
				//error += e.getMessage() + "; ";
				//e.printStackTrace();
				errorString += "Invalid Feature: '" + feature + "'\n";  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return featuresToAdd;
	}
	
	/**
	 * Adds collection of features to a given layer using AddFeaturesCommand.
	 * 
	 * @param layer
	 * @param features
	 * @throws Exception
	 */
	protected void addFeatures(ILayer layer, Collection<SimpleFeature> features) throws Exception{
		if (features.size() == 0) return;
		AddFeaturesCommand afc = new AddFeaturesCommand(features, layer);
		afc.run(new NullProgressMonitor());
		layer.refresh(null);
		
	}
}
