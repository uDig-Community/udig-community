package net.refractions.udig.sld.editor.internal.ui;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

public class SLDEditorPartFactory implements EditPartFactory {

    public EditPart createEditPart( EditPart context, Object model ) {
        if (model instanceof String) {
            if (context == null) {
                DiagramContentsEditPart part = new DiagramContentsEditPart();
//                part.setModel("SLD");
                return part;
            } else {
                return new StyledLayerDescriptorPart();
            }
        }
        if (model instanceof Integer) {
            return new NamedLayerEditPart();
        }
        return null;
    }

}
