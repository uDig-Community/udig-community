package net.refractions.wkt;

import java.util.Collection;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.geotools.data.FeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * 
 * Pastes WKT features into an existing layer.
 * 
 * @author Emily
 *
 */
public class PasteWKTLayer extends PasteWKT implements IOp {

	public static final String ID = "net.refractions.wkt.pastelayer"; //$NON-NLS-1$
	
	public void op(Display display, Object target, IProgressMonitor monitor)
			throws Exception {

		final Display mydisplay = display;
		final ILayer destinationLayer = (ILayer) target;

		display.asyncExec(new Runnable() {

			public void run() {
				Clipboard cb = new Clipboard(mydisplay);
				TextTransfer transfer = TextTransfer.getInstance();
				String data = (String) cb.getContents(transfer);

				errorString = ""; //$NON-NLS-1$
				if (data != null) {
					try{				
						FeatureSource<SimpleFeatureType, SimpleFeature> destinationFs = destinationLayer.getResource(FeatureSource.class, new NullProgressMonitor());
						SimpleFeatureBuilder builder = new SimpleFeatureBuilder((SimpleFeatureType) destinationFs.getSchema());					
						Collection<SimpleFeature> featuresToAdd = PasteWKTLayer.this.getFeatures(data, builder);
						PasteWKTLayer.this.addFeatures(destinationLayer, featuresToAdd);
					}catch (Exception ex){
						errorString = ex.getMessage() + "\n"; //$NON-NLS-1$
					}
				} else {
					errorString = "No Data."; //$NON-NLS-1$
				}
				
				if (errorString.length() > 0){
					Status status = new Status(IStatus.ERROR,ID,errorString);
					ErrorDialog.openError(mydisplay.getActiveShell(), "Translation Errors", "Error Creating All Features", status); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
		});

	}

}
