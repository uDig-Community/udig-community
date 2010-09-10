package net.refractions.udig.sld.editor.internal.ui;

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

public class DiagramContentsEditPart extends AbstractGraphicalEditPart {
    protected IFigure createFigure() {
        Figure f = new Figure() { 
            public Border getBorder() {
                return new LineBorder(ColorConstants.gray, 10);
            }
            public Rectangle getBounds() {
                return new Rectangle(0,0,612,792);
            }
        };
        f.setOpaque(true);
        f.setLayoutManager(new XYLayout());
        return f;
    }

    protected void createEditPolicies() {
        
    }

    protected List getModelChildren() {
//        return ((Parent)getModel()).getChildren();
        return Collections.singletonList("SLD");
//        return Collections.singletonList(new Integer(5));
    }

    @Override
    protected void refreshVisuals() {
        Rectangle rectangle = new Rectangle(3,3, 400,400);
        
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);
    }
    
    
}
