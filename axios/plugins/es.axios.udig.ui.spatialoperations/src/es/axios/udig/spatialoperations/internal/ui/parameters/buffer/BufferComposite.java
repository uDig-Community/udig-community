/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
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
package es.axios.udig.spatialoperations.internal.ui.parameters.buffer;

import net.refractions.udig.project.ILayer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.ui.parameters.AggregatedPresenter;

/**
 * This class presents the buffer parameters.
 * <p>
 *
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * 
 * @since 1.1.0
 */
public final class BufferComposite extends AggregatedPresenter{

    private BufferLayersComposite  layersComposite;
    private BufferOptionsComposite optionsComposite;
    
    
    /**
     * Creates a new instance of BufferComposite
     * 
     * @param parent
     * @param style
     */
    public BufferComposite( Composite parent, int style ) {
        super(parent, style ); 
        
		super.initialize();
        
        
    }


    @Override
    public final String getOperationName() {
        return Messages.BufferComposite_operation_name;
    }
    
    
    @Override
    public String getToolTipText() {
        return Messages.BufferCommand_create_buffer_in_target;
    }


    @Override
    protected final void createContents() {
        
        GridLayout gridLayout = new GridLayout();
        setLayout(gridLayout);

        
        layersComposite = new BufferLayersComposite(this, SWT.NONE); 
        GridData gridDataLayers = new GridData();
        gridDataLayers.horizontalAlignment = SWT.FILL;
        gridDataLayers.grabExcessHorizontalSpace = true;
        gridDataLayers.verticalAlignment = SWT.BEGINNING;
        layersComposite.setLayoutData(gridDataLayers);
        
        
        optionsComposite = new BufferOptionsComposite(this, SWT.NONE); 
        GridData gridDataOptions = new GridData();
        gridDataOptions.horizontalAlignment = GridData.FILL;
        gridDataOptions.grabExcessHorizontalSpace = true;
        gridDataOptions.grabExcessVerticalSpace = true;
        gridDataOptions.verticalAlignment = GridData.FILL;
        optionsComposite.setLayoutData(gridDataOptions);
        
        
        super.addPresenter(this.layersComposite);
        super.addPresenter(this.optionsComposite);
        
        
    }


    public ILayer getSourceLayer() {

        return this.layersComposite.getSourceLayer();
    }
    /**
     * @return Returns the BufferLayersComposite
     */
    public BufferLayersComposite getLayersComposite() {
        
        assert this.layersComposite != null;
        
        return this.layersComposite;
    }


    @Override
    protected void populate() {
        //nothing
    }

    
    
}  //  @jve:decl-index=0:visual-constraint="10,10"
