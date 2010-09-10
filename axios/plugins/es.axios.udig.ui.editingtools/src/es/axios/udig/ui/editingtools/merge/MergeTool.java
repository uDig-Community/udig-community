/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
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
package es.axios.udig.ui.editingtools.merge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tool.select.BBoxSelection;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.DialogUtil;
import es.axios.udig.ui.editingtools.internal.commands.DefaultEdittingToolsCommandFactory;
import es.axios.udig.ui.editingtools.internal.i18n.Messages;
import es.axios.udig.ui.editingtools.internal.presentation.StatusBar;
import es.axios.udig.ui.editingtools.merge.internal.MergeFeatureBehaviour;

/**
 * Merge the features in bounding box
 * <p>
 * This implementation is based in {@link BBoxSelection}. The extension add behaviour object which
 * displays the merge dialog.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class MergeTool extends BBoxSelection {

    private static final String EXTENSION_ID = "es.axios.udig.ui.editingtools.merge.MergeTool"; //$NON-NLS-1$

    /**
     * A new instance of MergeTool
     */
    public MergeTool() {
        super();

    }

    public String getExtensionID() {
        return EXTENSION_ID;
    }

    @Override
    public void setActive( final boolean active ) {
        super.setActive(active);
        IToolContext context = getContext();
        if (active && context.getMapLayers().size() > 0) {

            String message = Messages.MergeTool_select_features_to_merge;
            StatusBar.setStatusBarMessage(context, message);
        } else {
            StatusBar.setStatusBarMessage(context, "");//$NON-NLS-1$
        }
    }

    /**
     * Returns an in memory feature collection for the bbox filter on the current layer
     */
    private SimpleFeature[] getFeaturesInBBox( Envelope bbox ) throws IOException {

        ILayer selectedLayer = getContext().getSelectedLayer();

        FeatureSource<SimpleFeatureType, SimpleFeature> source = selectedLayer.getResource(FeatureSource.class, null);

        String typename = source.getSchema().getTypeName();

        Filter filter = selectedLayer.createBBoxFilter(bbox, null);
        Query query = new DefaultQuery(typename, filter);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(query);

        List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
        try {
            for( Iterator it = features.iterator(); it.hasNext(); ) {
                SimpleFeature f = (SimpleFeature) it.next();
                featureList.add(f);
            }
        } finally {
            features.purge();
        }

        SimpleFeature[] featureArray = featureList.toArray(new SimpleFeature[featureList.size()]);
        return featureArray;
    }

    /**
     * Validates the features in bounding box and creates the command to delete the source features
     * and insert the the new merged feature
     */
    @Override
    protected void sendSelectionCommand( MapMouseEvent e, final Envelope bounds ) {

        super.sendSelectionCommand(e, bounds);

        // this may take a while, so run it with a busy indicator
        Runnable runnable = new Runnable(){

            public void run() {
                final String dlgTitle = Messages.MergeTool_title_tool;

                SimpleFeature[] features = null;
                try {
                    // gets the features in bounding box
                    features = getFeaturesInBBox(bounds);
                } catch (IOException e1) {
                    final String msg = Messages.MergeTool_failed_getting_selection;
                    DialogUtil.openError(dlgTitle, msg);
                    throw new IllegalStateException( e1.getMessage(), e1 );
                }
                assert features != null;

                IToolContext context = getContext();
                DefaultEdittingToolsCommandFactory commandFactory = new DefaultEdittingToolsCommandFactory();
                final MergeFeatureBehaviour behaviour = new MergeFeatureBehaviour(context,
                                                                                  commandFactory);

                behaviour.setSourceFeatures(features);
                if (!behaviour.isValid()) {
                    behaviour.handleError();
                    return;
                }

                // execute command
                UndoableMapCommand cmd = null;
                try {

                    cmd = behaviour.getCommand();
                    if (cmd == null) {
                        return;
                    }

                    IMap map = context.getMap();
                    assert map != null;
                    cmd.setMap(map);
                    map.sendCommandASync(cmd);

                    StatusBar.setStatusBarMessage(context, Messages.MergeTool_successful);

                } catch (Exception e1) {

                    final String msg = Messages.MergeTool_failed_executing + ": " + e1.getMessage(); //$NON-NLS-1$
                    DialogUtil.openError(dlgTitle, msg);

                    if (cmd != null) {
                        try {
                            cmd.rollback(new NullProgressMonitor());

                        } catch (Exception e2) {
                            final String msg2 = Messages.MergeTool_failed_rollback;
                            DialogUtil.openError(dlgTitle, msg2);
                            throw new IllegalStateException(msg2, e2 );
                        }
                    }
                    throw new IllegalStateException( e1.getMessage(), e1 );

                } finally {
                    unselect(context);
                    context.getViewportPane().repaint();
                }
            }
        };

        AppGISMediator.showWhile(getContext().getViewportPane(), runnable);
    }
    /**
     * Unselects the merged features
     * 
     * @param context
     */
    private void unselect( final IToolContext context ) {

        UndoableMapCommand clearSelectionCommand = context.getSelectionFactory()
                                                          .createNoSelectCommand();
        context.sendASyncCommand(clearSelectionCommand);
    }

}
