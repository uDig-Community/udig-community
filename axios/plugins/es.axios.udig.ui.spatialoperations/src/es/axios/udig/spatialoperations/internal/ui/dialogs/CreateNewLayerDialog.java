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
package es.axios.udig.spatialoperations.internal.ui.dialogs;

import net.refractions.udig.ui.FeatureTypeEditor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.ui.commons.util.GeoToolsUtils;

/**
 * This dialog is used to create new layers.
 * <p>
 *
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public class CreateNewLayerDialog extends TitleAreaDialog{


    private SimpleFeatureTypeBuilder          defaultBuilder = null;
    private Button                      okButton            = null;
    private Button                      cancelButton        = null;
    private FeatureTypeBuilderValidator validateFeatureType = null;
    private SimpleFeatureType                 newFeatureType         = null;

    public CreateNewLayerDialog( Shell parentShell ) {
        super(parentShell);
    }
    
    @Override
    protected Control createContents( Composite parent ) {
        Control ctrl = super.createContents(parent);
        
        
        setTitle(Messages.CreateNewLayerDialog_title);
        
        setMessage(Messages.CreateNewLayerDialog_specific_attributes,
                   IMessageProvider.INFORMATION);
        
        
        return ctrl;
    }


    @Override
    protected Control createDialogArea( Composite parent ) {
        
        final Composite compositeDialog = (Composite)super.createDialogArea(parent);
        
        FeatureTypeEditor editor = new FeatureTypeEditor();
        this.defaultBuilder = GeoToolsUtils.createDefaultFeatureType();
        
        FeatureTypeBuilderValidator validator = new FeatureTypeBuilderValidator(){
            public boolean validate( SimpleFeatureTypeBuilder builder ) {
                
                final String name = builder.getName();
                if(name == null || "".equals(name)) { //$NON-NLS-1$
                    setMessage(Messages.CreateNewLayerDialog_must_set_the_feature_name);
                    return false;
                }
                SimpleFeatureType type = null; 
                try {
                    type = builder.buildFeatureType();
                } catch (IllegalArgumentException e) {
                    final String msg = Messages.CreateNewLayerDialog_failed_creatin_the_feature_type;
                    setMessage(msg);
                    throw (RuntimeException) new RuntimeException( msg).initCause( e );
                }
                assert type != null;
                
                GeometryDescriptor geoAttr =  type.getDefaultGeometry(); // builder.getDefaultGeometry();
                if(geoAttr == null || "".equals(geoAttr.getName())) { //$NON-NLS-1$
                    
                    setMessage(Messages.CreateNewLayerDialog_must_set_the_geometry);
                    return false;
                }

                //TODO validar consistencia enter sources layer y el nuevo (podrian filtrarse para que solo pueda seleccionar un tipo de geometria válida con LayerFilter )
                return true;
            }
        };
        this.validateFeatureType = validator;
        
        
        new CreateLayerComposite(
                                 compositeDialog, SWT.NONE, 
                                 editor,
                                 this.defaultBuilder);
        
        return compositeDialog;
    }
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
      this.okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
      this.cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
      
    }

    @Override
    protected void buttonPressed( int buttonId ) {
        
        assert this.defaultBuilder != null;
        
        this.newFeatureType = null;
        
        if (IDialogConstants.OK_ID == buttonId) {

            //validates input
            if(!this.validateFeatureType.validate(this.defaultBuilder)){
                return;
            }
            // the feature type is ok then sets this value in dialog state
            try {
                this.newFeatureType = this.defaultBuilder.buildFeatureType();
                
            } catch (IllegalArgumentException e) {
                final String msg = Messages.CreateNewLayerDialog_failed_getting_new_feature_type;
                throw (RuntimeException) new RuntimeException(msg ).initCause( e );
            }
        }
        
        setReturnCode(buttonId);
        close();
    }


    @Override
    public int open() {
        
        
        int code = super.open();
        
//        this.okButton.setEnabled(false);
//        this.cancelButton.setEnabled(true);
        
        return code;
    }

    /**
     * @return a new SimpleFeature Type
     */
    public SimpleFeatureType getNewLayer() {
        
        assert this.getReturnCode() == IDialogConstants.OK_ID;
        assert this.newFeatureType!= null;

        return this.newFeatureType;
    }
    

}
