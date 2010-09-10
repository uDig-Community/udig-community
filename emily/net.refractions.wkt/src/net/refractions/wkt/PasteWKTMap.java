package net.refractions.wkt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * Pastes well known text features into a map; creating a new temporary layer to put
 * the features into.
 * 
 * @author Emily
 *
 */
public class PasteWKTMap extends PasteWKT implements IOp {

	public static final String ID = "net.refractions.wkt.pastemap"; //$NON-NLS-1$
	
	public void op(Display display, Object target, IProgressMonitor monitor)
			throws Exception {

		final Display mydisplay = display;
		final IMap myMap = (IMap) target;
		
		mydisplay.asyncExec(new Runnable() {
			public void run() {
				errorString = ""; //$NON-NLS-1$
				
				Clipboard cb = new Clipboard(mydisplay);
				TextTransfer transfer = TextTransfer.getInstance();
				String data = (String) cb.getContents(transfer);
				createLayerWithFeature(data, myMap);
				
				if (errorString.length() > 0){
					Status status = new Status(IStatus.ERROR,ID,errorString);
					ErrorDialog.openError(mydisplay.getActiveShell(), "Translation Errors", "Error Creating All Features", status); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		});

	}

	
	private void createLayerWithFeature(String data, IMap map){
			
		//build feature type
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName( "PastedFeatures_" + UUID.randomUUID());		 //$NON-NLS-1$
		builder.setCRS( map.getViewportModel().getCRS() );
		builder.add("the_geom", Geometry.class); //$NON-NLS-1$
		SimpleFeatureType myFeatureType = builder.buildFeatureType();
		
		//create a resource and add it to the map
		IGeoResource layer = CatalogPlugin.getDefault().getLocalCatalog().createTemporaryResource(myFeatureType);
		List<? extends ILayer> layers = ApplicationGIS.addLayersToMap(map, Collections.singletonList(layer), 0);
		ILayer newLayer = (ILayer) layers.get(0);
				
		if (data != null) {
			try{				
				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(myFeatureType); 
				Collection<SimpleFeature> featuresToAdd = super.getFeatures(data, featureBuilder);
				super.addFeatures(newLayer, featuresToAdd);
			}catch (Exception ex){
				errorString += ex.getMessage() + "\n"; //$NON-NLS-1$
			}
		}else{
			errorString += "No Data."; //$NON-NLS-1$
		}
		
	}
}
