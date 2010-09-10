/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.ui.view;

import net.refractions.udig.project.ui.IUDIGView;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.opengis.feature.simple.SimpleFeature;

/**
 * View for spatial operations
 * <p>
 * This view presents the spatial operaion and its parameters. 
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class SOView extends ViewPart implements IUDIGView{

    private ISOPresenter soComposite = null;
    private IToolContext context     = null;
    
    public static final String id = "es.axios.udig.spatialoperations.ui.view.SOView"; //$NON-NLS-1$
    
    
    /**
     * New instance of SOView
     *
     */
    public SOView() {
        
    }

    @Override
    public void createPartControl( Composite parent ) {
        
        
        SOComposite composite = new SOComposite(parent, SWT.NONE);
        composite.setLayout( new FillLayout());
        
        composite.open();

        this.soComposite = composite;
        
        
    }

    @Override
    public void setFocus() {
        
    }
    
    

    @Override
    public void init( IViewSite site ) throws PartInitException {
        super.init(site);
    }

    @Override
    public void setInitializationData( IConfigurationElement cfig, String propertyName, Object data ) {
        super.setInitializationData(cfig, propertyName, data);
    }

    public void editFeatureChanged( @SuppressWarnings("unused")
                                    SimpleFeature feature ) {
        // this is not important for spatial operations
    }


    public IToolContext getContext() {
        return this.context;
    }

    /**
     * Called by the uDig framework when a new map is created or 
     * its layer list is modified.
     * 
     * @param newContext it could be null if the map is deleted
     */
    public void setContext( IToolContext newContext ) {
        
        this.context = newContext;
       
        this.soComposite.setContext(this.context);
        
    }

    /**
     * Enable / Desable the perform button
     *
     * @param ok
     */
    public void setPerformEnabled( boolean ok ) {
        
        this.soComposite.setPerformEnabled(ok);
        
        
    }

    
    

}
