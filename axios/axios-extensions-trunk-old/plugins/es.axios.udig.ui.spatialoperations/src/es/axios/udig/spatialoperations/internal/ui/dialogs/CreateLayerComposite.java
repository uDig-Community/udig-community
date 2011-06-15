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

import net.refractions.udig.internal.ui.UiPlugin;
import net.refractions.udig.ui.FeatureTypeEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;

/**
 * Presents the widget required to create a new layer.
 * <p>
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class CreateLayerComposite extends Composite {

    private FeatureTypeEditor editor;
    private SimpleFeatureTypeBuilder defaultBuilder;

    /**
     * @param parent
     * @param style
     * @param editor
     * @param defaultBuilder
     */
    public CreateLayerComposite( Composite parent, int style, FeatureTypeEditor editor,
            SimpleFeatureTypeBuilder defaultBuilder ) {
        super(parent, style);
        this.editor = editor;
        this.defaultBuilder = defaultBuilder;
        initialize();
    }

    private void initialize() {
        GridLayout gridLayout = new GridLayout(8, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;

        setLayout(gridLayout);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        editor.createFeatureTypeNameText(this, new GridData(SWT.FILL, SWT.FILL, true, false, 8, 1));

        Composite buttons = new Composite(this, SWT.NULL);
        gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        buttons.setLayout(new GridLayout(1, true));
        GridData gridData = new GridData(SWT.NULL, SWT.FILL, false, true, 1, 1);
        gridData.widthHint = 32;
        buttons.setLayoutData(gridData);
        createButton(buttons, editor.getCreateAttributeAction());
        createButton(buttons, editor.getDeleteAction());

        editor.createTable(this, new GridData(SWT.FILL, SWT.FILL, true, true, 7, 1),
                defaultBuilder, true);
        editor.createContextMenu();
    }

    private void createButton( Composite composite, final IAction action ) {
        final Button button = new Button(composite, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        button.setToolTipText(action.getToolTipText());

        button.addPaintListener(new PaintListener(){
            ImageRegistry images = UiPlugin.getDefault().getImageRegistry();
            public void paintControl( PaintEvent e ) {
                Image image = images.get(action.getId());
                if (image == null || image.isDisposed()) {
                    images.put(action.getId(), action.getImageDescriptor());
                    image = images.get(action.getId());
                }

                Point size = button.getSize();
                Rectangle imageBounds = image.getBounds();
                e.gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height, 2, 2,
                        size.x - 4, size.y - 4);
            }
        });

        button.addListener(SWT.Selection, new Listener(){
            public void handleEvent( Event event ) {
                action.runWithEvent(event);
            }
        });
    }
}
