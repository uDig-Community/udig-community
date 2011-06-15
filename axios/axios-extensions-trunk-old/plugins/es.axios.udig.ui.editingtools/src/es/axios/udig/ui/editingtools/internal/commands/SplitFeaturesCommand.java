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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.command.AbstractCommand;
import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.command.factory.EditCommandFactory;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.GeometryCreationUtil;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.IllegalFilterException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.GeometryUtil;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;
import es.axios.udig.ui.editingtools.internal.geometryoperations.split.SplitStrategy;
import es.axios.udig.ui.editingtools.internal.i18n.Messages;

/**
 * Undoable map command that splits a collection of Features with a given Line.
 * <p>
 * The splitting line is taken from the EditToolHandler's {@link EditToolHandler#getCurrentShape()}
 * </p>
 * <p>
 * That line will then be used to cut the selected layer's geometries that intersect it. If a
 * selection is set on the layer, it will be respected, and thus the command will apply to those
 * features that are either selected and intersect the splitting line.
 * </p>
 * <p>
 * For those SimpleFeature geometries that were splitted, the original SimpleFeature will be deleted and as many
 * new Features as geometries resulted of the split operation will be created, maintaining the
 * original SimpleFeature's attributes other than the default geometry.
 * </p>
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 * @see SplitStrategy
 */
final class SplitFeaturesCommand extends AbstractCommand implements UndoableMapCommand {

    private EditToolHandler   handler;
    private ILayer            selectedLayer;

    /**
     * Composite command used to aggregate the set of feature delete and create commands
     */
    private UndoableComposite composite;

    /**
     * Creates a new split command to split the features of the <code>handler</code>'s selected
     * layer with the line present in the handler's
     * {@link EditToolHandler#getCurrentShape() current shape}.
     * 
     * @param handler an EditToolHandler containing the context for the command to run. For
     *        instance, the selected layer and the line shape drawn by the user.
     */
    public SplitFeaturesCommand( EditToolHandler handler ) {
        final ILayer selectedLayer = handler.getContext().getSelectedLayer();

        assert selectedLayer.getSchema() != null;
        Class<?> geometryBinding = selectedLayer.getSchema().getDefaultGeometry().getType().getBinding();
		assert geometryBinding != Point.class;
        assert geometryBinding != MultiPoint.class;

        assert selectedLayer.hasResource(FeatureStore.class);

        this.handler = handler;
        this.selectedLayer = selectedLayer;
    }

    public String getName() {
        return "Split Features Command"; //$NON-NLS-1$
    }

    /**
     * Runs the split operation over the provided features and builds this command's state as an
     * {@link UndoableComposite} with the list of commands needed to remove the splitted features
     * and create the new ones, then delegates the execution to that {@link UndoableComposite} which
     * is maintained to be reused by {@link #rollback(IProgressMonitor)}.
     */
    public void run( IProgressMonitor monitor ) throws Exception {
        if( monitor == null ) {
            monitor = new NullProgressMonitor();
        }
        IProgressMonitor prepMonitor = SubMonitor.convert( monitor, 80 );
        
        final List<UndoableMapCommand> undoableCommands = new ArrayList<UndoableMapCommand>();

            LineString splitter;
            FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToSplit;

            monitor.beginTask("Splitting features", 3); //$NON-NLS-1$
            try {
                splitter = getSplittingLineInMapCRS(handler);
                assert splitter.getUserData() instanceof CoordinateReferenceSystem;

                prepMonitor.worked(1);

                featuresToSplit = getFeaturesToSplit(splitter);

                prepMonitor.worked(2);
            } catch (OperationNotFoundException e) {
            	throw new IllegalStateException( e.getMessage(), e );
            } catch (IOException e) {
            	throw new IllegalStateException( e.getMessage(), e );
            } catch (TransformException e) {
            	throw new IllegalStateException( e.getMessage(), e );
            }
            try {
                List<UndoableMapCommand> commands = buildCommandList(featuresToSplit, splitter);
                undoableCommands.addAll(commands);
            } catch (Exception e) {
                throw (RuntimeException) new RuntimeException("Difficulity splitting "+featuresToSplit.size()+" feature(s)").initCause(e); //$NON-NLS-1$ //$NON-NLS-2$
            }

            prepMonitor.worked(3);
            prepMonitor.done();

            if (undoableCommands.size() == 0) {
                throw new IllegalArgumentException(
                                                   Messages.SplitFeaturesCommand_did_not_apply_to_any_feature);
            }
            
            IProgressMonitor splitMonitor = SubMonitor.convert( monitor, 20 );            
            composite = new UndoableComposite(undoableCommands);

            // cascade setMap on the aggregate commands
            Map map = getMap();
            composite.setMap(map);

            try {
                composite.run(splitMonitor);
            } catch (Exception e) {
            	throw new IllegalStateException( e.getLocalizedMessage(), e );
            }
            handler.setCurrentShape(null);
            handler.setCurrentState(EditState.NONE);

        final ILayer selectedLayer = handler.getContext().getSelectedLayer();
        EditBlackboard editBlackboard = handler.getEditBlackboard(selectedLayer);
        editBlackboard.clear();
        
        handler.repaint();
    }

    /**
     * Returns the line drawn as the splitting line, transformed to JTS LineString in the current
     * {@link IMap map}'s CRS.
     * 
     * @param handler the {@link EditToolHandler} from where to grab the current shape (the one
     *        drawn as the cutting line)
     * @param layerCrs the {@link CoordinateReferenceSystem} of the Layer being edited, which is the
     *        CRS the editing line is being stored in the {@link EditToolHandler}. Expected as
     *        argument as I don't know how to obtain the layer's CRS from the handler itself.
     * @return
     * @throws TransformException
     * @throws OperationNotFoundException
     */
    public static LineString getSplittingLineInMapCRS( EditToolHandler handler ) throws OperationNotFoundException, TransformException {

        final ILayer selectedLayer = handler.getContext().getSelectedLayer();
        final CoordinateReferenceSystem layerCrs = LayerUtil.getCrs(selectedLayer);

        assert handler.getCurrentShape() != null;
        assert layerCrs != null;

        final PrimitiveShape currentShape = handler.getCurrentShape();
        final LineString lineInLayerCrs = GeometryCreationUtil.createGeom(LineString.class,currentShape,true);
        final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(handler.getContext().getMap());

        LineString splittingLine = (LineString) GeoToolsUtils.reproject(lineInLayerCrs, layerCrs,
                                                                        mapCrs);
        splittingLine.setUserData(mapCrs);

        return splittingLine;
    }

    /**
     * Returns the features to be splitted by <code>splittingLine</code>.
     * <p>
     * To aquire the featuers, <code>splittingLine</code> is transformed to the layer's CRS and an
     * Intersects filter is made with the result.
     * </p>
     * 
     * @param splittingLine
     * @return
     * @throws IOException
     * @throws OperationNotFoundException
     * @throws TransformException
     */
    FeatureCollection<SimpleFeatureType, SimpleFeature> getFeaturesToSplit( LineString splittingLine ) throws IOException, OperationNotFoundException, TransformException {

        Filter extraFilter = createSplittingLineFilter(splittingLine);
        FeatureCollection<SimpleFeatureType, SimpleFeature> selection = LayerUtil.getSelectedFeatures(selectedLayer, extraFilter);
        return selection;
    }

    Filter createSplittingLineFilter( LineString splittingLine ) throws OperationNotFoundException, TransformException {
        Filter filter = selectedLayer.getFilter();
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        Intersects intersectsFilter;
        try {

            final CoordinateReferenceSystem geomCrs;
            geomCrs = (CoordinateReferenceSystem) splittingLine.getUserData();
            final CoordinateReferenceSystem layerCrs = LayerUtil.getCrs(selectedLayer);
            final Geometry literal = GeoToolsUtils.reproject(splittingLine, geomCrs, layerCrs);

            SimpleFeatureType schema = selectedLayer.getSchema();
			String geomName = schema.getDefaultGeometry().getLocalName();
            
			intersectsFilter = ff.intersects( ff.property(geomName), ff.literal(literal));
			
        } catch (IllegalFilterException e) {
        	throw new IllegalStateException( e.getMessage(), e );
        }

        if (Filter.EXCLUDE.equals(filter)) {
            filter = intersectsFilter;
        } else {
        	filter = ff.and( filter, intersectsFilter );
        }
        return filter;
    }

    /**
     * Creates the list of commands that are going to be executed on {@link #run(IProgressMonitor)}
     * <p>
     * NOTE: visible only to be accessed by unit tests, framework uses
     * {@link #run(IProgressMonitor)} and you should never see this class from client code.
     * </p>
     * 
     * @return
     * @throws OperationNotFoundException
     * @throws TransformException
     * @throws IllegalAttributeException
     */
    List<UndoableMapCommand> buildCommandList( FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToSplit,
                                               LineString splitterInMapCrs ) throws OperationNotFoundException, TransformException, IllegalAttributeException {
        
        EditPlugin.trace("Splitter in map crs:" + splitterInMapCrs, null); //$NON-NLS-1$
        final EditCommandFactory cmdFac = AppGISMediator.getEditCommandFactory();

        final FeatureIterator<SimpleFeature> iterator = featuresToSplit.features();
        final List<UndoableMapCommand> undoableCommands = new ArrayList<UndoableMapCommand>();
        final SplitStrategy splitOp = new SplitStrategy(splitterInMapCrs);

        final CoordinateReferenceSystem layerCrs = LayerUtil.getCrs(selectedLayer);
        final CoordinateReferenceSystem splitterCrs;
        splitterCrs = (CoordinateReferenceSystem) splitterInMapCrs.getUserData();
        try {
            Geometry originalGeometry;
            Geometry splitted;
            UndoableMapCommand command;
            while( iterator.hasNext() ) {
                final SimpleFeature feature = iterator.next();
                final SimpleFeatureType featureType = feature.getFeatureType();

                originalGeometry = (Geometry) feature.getDefaultGeometry();
                EditPlugin.trace("originalGeometry=" + originalGeometry,null); //$NON-NLS-1$

                originalGeometry = GeoToolsUtils.reproject(originalGeometry, layerCrs, splitterCrs);
                EditPlugin.trace("originalGeometry projected to Map CRS =" + originalGeometry, null); //$NON-NLS-1$

                splitted = splitOp.split(originalGeometry);

                EditPlugin.trace("split result =" + splitted,null); //$NON-NLS-1$

                splitted = GeoToolsUtils.reproject(splitted, splitterCrs, layerCrs);

                EditPlugin.trace("splitted back projected to layerCrs=" + splitted,null); //$NON-NLS-1$

                final int numGeometries = splitted.getNumGeometries();
                switch( numGeometries ) {
                case 0:
                    throw new IllegalStateException(
                                                    Messages.SplitFeaturesCommand_no_geometry_were_created);
                case 1:
                    // do nothing, same as input
                    break;
                default:
                    command = cmdFac.createDeleteFeature(feature, selectedLayer);
                    undoableCommands.add(command);
                    for( int i = 0; i < numGeometries; i++ ) {
                        Geometry splittedPart = splitted.getGeometryN(i);
                        Class<?> geometryBinding =
                        	featureType.getDefaultGeometry().getType().getBinding();
                        
                        splittedPart = GeometryUtil.adapt(splittedPart, (Class<? extends Geometry>) geometryBinding);
                        
                        SimpleFeature newFeature = SimpleFeatureBuilder.template( featureType, null );
                        GeoToolsUtils.match(feature, newFeature);
                        newFeature.setDefaultGeometry(splittedPart);
                        command = cmdFac.createAddFeatureCommand(newFeature, selectedLayer);
                        undoableCommands.add(command);
                    }
                }

            }
        } finally {
            featuresToSplit.close(iterator);
        }
        return undoableCommands;
    }

    /**
     * Rolls back the split operation by delegating to the {@link UndoableComposite} created in
     * {@link #run(IProgressMonitor)}, if any. Exits gracefully otherwise.
     */
    public void rollback( IProgressMonitor monitor ) throws Exception {
        if (composite != null) {
            // handler.setCurrentState(EditState.NONE);
            // handler.setCurrentShape(currentShape);
            composite.rollback(monitor);
        }
    }

}
