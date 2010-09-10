package net.refractions.udig.wps.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

import net.refractions.udig.ui.IDropAction;
import net.refractions.udig.ui.ViewerDropLocation;
/**
 * This action is used for drag/dropping map objects into the
 * input parameters (PropertySheetPage) of a WPS Process View
 *
 * @author GDavis
 * @since 1.1.0
 */
public class WPSDropAction extends IDropAction {

    public WPSDropAction() {
    }

    @Override
    public boolean accept() {
        if( getViewerLocation()==ViewerDropLocation.NONE ) {
            return false;
        }
        String geomWKT = null;
        // make sure we can turn the object into a geometry WKT String
        if (getData() instanceof SimpleFeature) {
            Geometry geom = (Geometry) ((SimpleFeature)getData()).getDefaultGeometry();
            WKTWriter writer = new WKTWriter();
            geomWKT = writer.write(geom);
        } 
        else if (getData() instanceof Geometry) {
            Geometry geom = (Geometry) getData();
            WKTWriter writer = new WKTWriter();
            geomWKT = writer.write(geom);              
        } 
        else if (getData() instanceof String) {
            geomWKT = (String) getData();
        }

        if( geomWKT == null ) {
            return false;
        }

        return geomWKT != null && (getDestination() instanceof TextPropertyDescriptor );
    }

    @Override
    public void perform( IProgressMonitor monitor ) {
        if ( !accept() ) {
            throw new IllegalStateException("Data is not acceptable for this action!  Programatic Error!!!"); //$NON-NLS-1$
        }
        // grab the actual target
        Object target = getDestination();
        if ( target instanceof TextPropertyDescriptor ) {
            TextPropertyDescriptor tpd = (TextPropertyDescriptor) target;
            //dropOnLayer(monitor, (Layer) target);
            System.out.println("YES");
        }  
    }
}