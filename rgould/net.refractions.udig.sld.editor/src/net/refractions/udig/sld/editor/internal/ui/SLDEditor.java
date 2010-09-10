package net.refractions.udig.sld.editor.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.ui.IEditorInput;

public class SLDEditor extends GraphicalEditor{

    private String sld = "Default";

    public SLDEditor () {
        setEditDomain(new DefaultEditDomain(this));
    }
    
    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        
        getGraphicalViewer().setRootEditPart(new ScalableFreeformRootEditPart());

        getGraphicalViewer().setEditPartFactory(new SLDEditorPartFactory());
        
        ContextMenuProvider provider = new SLDEditorContextMenuProvider(getGraphicalViewer(), getActionRegistry());
        getGraphicalViewer().setContextMenu(provider);
        getSite().registerContextMenu("net.refractions.udig.sld.editor.contextmenu", //$NON-NLS-1$
                provider, getGraphicalViewer());
    }

    @Override
    public void doSave( IProgressMonitor monitor ) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    protected void initializeGraphicalViewer() {
        getGraphicalViewer().setContents(getSLD());
    }
    
    protected String getSLD() {
        return this.sld;
    }

    @Override
    protected void setInput( IEditorInput input ) {
        super.setInput(input);
    }
}
