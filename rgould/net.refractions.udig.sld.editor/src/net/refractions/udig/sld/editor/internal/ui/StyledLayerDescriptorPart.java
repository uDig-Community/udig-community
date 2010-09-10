package net.refractions.udig.sld.editor.internal.ui;

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

public class StyledLayerDescriptorPart extends AbstractGraphicalEditPart  {

    protected IFigure createFigure() {       
        return new SLDFigure("SLD",new Rectangle(10,10, 300,300));
    }

    @Override
    protected void createEditPolicies() {
    }

    protected void refreshVisuals() {
        Rectangle rectangle = new Rectangle(5,150, 300,250);
        
        
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);
    }

    @Override
    public List getModelChildren() {
        return Collections.singletonList(new Integer(5));
    }

    
}
