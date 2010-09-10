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
package es.axios.udig.ui.editingtools.internal.commands;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.AbstractCommand;
import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.command.factory.EditCommandFactory;
import net.refractions.udig.project.ui.render.displayAdapter.ViewportPane;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.mediator.PlatformGISMediator;
import es.axios.udig.ui.commons.util.DialogUtil;
import es.axios.udig.ui.editingtools.internal.geometryoperations.TrimGeometryStrategy;
import es.axios.udig.ui.editingtools.internal.i18n.Messages;

/**
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
final class TrimFeaturesCommand extends AbstractCommand implements UndoableMapCommand {

    private EditToolHandler   handler;
    private ILayer            selectedLayer;
    private LineString        trimmingLine;

    /** current shape to be restored on undo */
    private PrimitiveShape    currentShape;

    private FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToTrim;

    private UndoableComposite composite;

    /**
     * @param selectedLayer
     * @param selectionFilter
     * @param trimmingLine required to hold its CRS on {@link Geometry#getUserData()}
     */
    public TrimFeaturesCommand( EditToolHandler handler, ILayer selectedLayer,
                                FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToTrim, LineString trimmingLine ) {
        if (!selectedLayer.hasResource(FeatureStore.class)) {
            throw new IllegalArgumentException();
        }
        this.handler = handler;
        this.selectedLayer = selectedLayer;
        this.featuresToTrim = featuresToTrim;
        this.trimmingLine = trimmingLine;
    }

    /**
     * Opens an question dialog in a standardized way TODO this commons dialog should not be part of
     * mediator
     * 
     * @param title message dialog title
     * @param message message dialog content
     * @return wether the question was accepted or not
     */
    public static boolean openQuestion( final String title, final String message ) {
        final boolean[] confirm = {false};
        PlatformGISMediator.syncInDisplayThread(new Runnable(){
            public void run() {
                confirm[0] = MessageDialog.openQuestion(null, title, message);
            }
        });
        return confirm[0];
    }

    public String getName() {
        return "Trim Features Command"; //$NON-NLS-1$
    }

    public void run( final IProgressMonitor monitor ) throws Exception {

        //this may take a while so run it with a busy indicator
        Runnable runnable = new Runnable(){
            public void run() {
                final EditCommandFactory editCommandFactory;
                editCommandFactory = AppGISMediator.getEditCommandFactory();

                final FeatureIterator<SimpleFeature> iterator = featuresToTrim.features();
                final List<UndoableMapCommand> undoableCommands = new ArrayList<UndoableMapCommand>();
                final TrimGeometryStrategy trimOp = new TrimGeometryStrategy(trimmingLine);

                try {
                    SimpleFeature feature;
                    Geometry original;
                    Geometry trimmed;
                    UndoableMapCommand command;
                    while( iterator.hasNext() ) {
                        feature = iterator.next();
                        original = (Geometry) feature.getDefaultGeometry();
                        trimmed = trimOp.trim(original);
                        command = editCommandFactory.createSetGeomteryCommand(feature,
                                                                              selectedLayer,
                                                                              trimmed);
                        undoableCommands.add(command);
                    }
                } finally {
                    featuresToTrim.close(iterator);
                }

                if (undoableCommands.size() == 0) {
                    DialogUtil
                              .openInformation(
                                               Messages.TrimFeaturesCommand_no_features_modified,
                                               Messages.TrimFeaturesCommand_did_not_apply_to_any_feature);
                } else {
                    composite = new UndoableComposite(undoableCommands);
                    try {
                        composite.run(monitor);
                    } catch (Exception e) {
                    	throw new IllegalStateException( e.getMessage(), e );
                    }
                    currentShape = handler.getCurrentShape();
                    handler.setCurrentShape(null);
                    handler.setCurrentState(EditState.NONE);
                }

            }
        };
        
        ViewportPane pane = handler.getContext().getViewportPane();
        AppGISMediator.showWhile(pane, runnable);
    }

    public void rollback( IProgressMonitor monitor ) throws Exception {
        if (composite != null) {
            handler.setCurrentState(EditState.CREATING);
            handler.setCurrentShape(currentShape);
            composite.rollback(monitor);
        }
    }

}
