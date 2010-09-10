package net.refractions.udig.sld.editor.internal.ui;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class SLDFigure extends Figure {
    
    Label label;

    public SLDFigure(String name, Rectangle bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("Bounds must not be null.");
        }
        this.bounds = bounds;
        this.label = new Label();
        setFigureName(name);

        setOpaque(true);
        setLayoutManager(new XYLayout());
        label.setBounds(new Rectangle(30, 20, 200, 250));
        add(label);
        
    }
    
    public void setFigureName( String name ) {
        this.label.setText(name);
    }

    public Border getBorder() {
        return new LineBorder(ColorConstants.blue, 5);
    }
    public Rectangle getBounds() {
        return this.bounds;
    }
}
