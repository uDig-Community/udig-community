package net.refractions.udig.catalog.wfs.cache.op;

import net.refractions.udig.catalog.wfs.cache.WFScDataStore;
import net.refractions.udig.catalog.wfs.cache.WFScGeoResourceImpl;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

/**
 * Operation to clean the cache of a wfsc layer.
 * 
 * @author Emily Gouge
 * @since 1.2.0
 */
public class ClearCache implements IOp {

    @Override
    public void op( Display display, Object target, IProgressMonitor monitor ) throws Exception {
        monitor.beginTask("Clear Cache", 4); //$NON-NLS-1$
        WFScGeoResourceImpl geo = (WFScGeoResourceImpl) target;
        monitor.worked(1);
        WFScDataStore datastore = geo.resolve(WFScDataStore.class, monitor);
        monitor.worked(1);
        String typename = geo.getInfo(monitor).getName();
        monitor.worked(1);
        datastore.clearCache(typename);
        monitor.worked(1);
    }
}
