package net.refractions.udig.sld.editor.internal.ui;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

public class NamedLayerEditPart extends AbstractGraphicalEditPart  {

    @Override
    protected IFigure createFigure() {
        return new SLDFigure("NamedLayer", new Rectangle(5,5,300,250));
    }

    @Override
    protected void createEditPolicies() {
    }

    protected void refreshVisuals() {
        Rectangle rectangle = new Rectangle(5,5, 300,250);
        
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);
    }

}
