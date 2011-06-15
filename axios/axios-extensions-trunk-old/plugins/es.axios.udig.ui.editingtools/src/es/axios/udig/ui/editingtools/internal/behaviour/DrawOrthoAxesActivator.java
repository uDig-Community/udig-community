/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
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
package es.axios.udig.ui.editingtools.internal.behaviour;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseMotionListener;
import net.refractions.udig.project.ui.render.displayAdapter.ViewportPane;
import net.refractions.udig.tools.edit.Activator;
import net.refractions.udig.tools.edit.EditToolHandler;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public class DrawOrthoAxesActivator implements Activator, MapMouseMotionListener {

    /**
     * handler in use set on activate
     */
    protected EditToolHandler    handler;

    private DrawOrthoAxesCommand drawAxesCommand;

    public void setRotationAngle( double angle ) {
        if (drawAxesCommand != null) {
            drawAxesCommand.setRotationAngle(angle);
        }
    }

    public void setValid( boolean valid ) {
        if (valid) {
            getCommand();
        } else {
            if (drawAxesCommand != null) {
                drawAxesCommand.setValid(false);
                drawAxesCommand = null;
            }
        }
    }

    private DrawOrthoAxesCommand getCommand() {
        if (drawAxesCommand == null) {
            drawAxesCommand = new DrawOrthoAxesCommand();
            ViewportPane viewportPane = handler.getContext().getViewportPane();
            viewportPane.addDrawCommand(drawAxesCommand);
        }
        return drawAxesCommand;
    }
    /**
     * @see Activator#activate(EditToolHandler)
     */
    public void activate( EditToolHandler handler ) {
        this.handler = handler;
        setValid(true);
        ViewportPane viewportPane = handler.getContext().getViewportPane();
        viewportPane.addMouseMotionListener(this);
    }

    /**
     * @see Activator#deactivate(EditToolHandler)
     */
    public void deactivate( EditToolHandler handler ) {
        setValid(false);
        ViewportPane viewportPane = handler.getContext().getViewportPane();
        viewportPane.removeMouseMotionListener(this);
    }

    /**
     * @see Activator#handleActivateError(EditToolHandler, Throwable)
     */
    public void handleActivateError( EditToolHandler handler, Throwable error ) {
    }

    /**
     * @see Activator#handleDeactivateError(EditToolHandler, Throwable)
     */
    public void handleDeactivateError( EditToolHandler handler, Throwable error ) {
    }

    /**
     * @see MapMouseMotionListener#mouseMoved(MapMouseEvent)
     */
    public void mouseMoved( MapMouseEvent event ) {
        getCommand().setEvent(event);
        handler.repaint();
    }

    /**
     * Empty method
     * 
     * @see MapMouseMotionListener#mouseDragged(MapMouseEvent)
     */
    public void mouseDragged( MapMouseEvent event ) {
    }

    /**
     * Empty method
     * 
     * @see MapMouseMotionListener#mouseHovered(MapMouseEvent)
     */
    public void mouseHovered( MapMouseEvent event ) {
    }

    /**
     * DrawCommand that draws a pair of orthogonal axes passing through the mouse location and for
     * which a rotation angle can be set.
     */
    private static class DrawOrthoAxesCommand extends AbstractDrawCommand {

        private MapMouseEvent         event;

        private final float[]         srcCoords = new float[8];
        private final float[]         dstCoords = new float[8];

        private final AffineTransform translate = new AffineTransform();
        private final AffineTransform rotate    = new AffineTransform();

        public Rectangle getValidArea() {
            return new Rectangle(display.getDisplaySize());
        }

        public void setEvent( MapMouseEvent event ) {
            this.event = event;
        }

        /**
         * @param angle angle in radians to rotate the axes around the event's location
         */
        public void setRotationAngle( double angle ) {
            rotate.setToIdentity();
            rotate.rotate(-angle);
        }

        public void run( IProgressMonitor monitor ) throws Exception {
            if (event != null) {
                final int displayW = display.getWidth();
                final int displayH = display.getHeight();
                final AffineTransform transform = translate;
                transform.setToTranslation(event.x, event.y);
                transform.concatenate(rotate);

                srcCoords[0] = -displayW;
                srcCoords[1] = 0;
                srcCoords[2] = displayW;
                srcCoords[3] = 0;
                srcCoords[4] = 0;
                srcCoords[5] = -displayH;
                srcCoords[6] = 0;
                srcCoords[7] = displayH;

                float[] dst = dstCoords;
                transform.transform(srcCoords, 0, dst, 0, 4);

                graphics.setColor(Color.LIGHT_GRAY);
                graphics.drawLine((int) dst[0], (int) dst[1], (int) dst[2], (int) dst[3]);
                graphics.drawLine((int) dst[4], (int) dst[5], (int) dst[6], (int) dst[7]);
            }
        }

    }
}