package net.refractions.udig.sld.editor.internal.ui;

import net.refractions.udig.project.ui.UDIGEditorInput;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;

public class SLDEditorInput extends UDIGEditorInput {

    public boolean exists() {
        return false;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return null;
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return null;
    }

    public Object getAdapter( Class adapter ) {
        return null;
    }

}
