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
package es.axios.udig.ui.editingtools.trim.internal;

import java.io.IOException;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.tools.edit.Behaviour;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.support.GeometryCreationUtil;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.IllegalFilterException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.LineString;

import es.axios.udig.ui.commons.util.DialogUtil;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.editingtools.internal.commands.EditingToolsCommandFactory;
import es.axios.udig.ui.editingtools.internal.i18n.Messages;
import es.axios.udig.ui.editingtools.trim.TrimTool;

/**
 * {@link Behaviour} to use the blackboard's linestring as a trimming line for the {@link TrimTool}
 * <p>
 * Requirements:
 * <ul>
 * <li>Current Shape is not null</li>
 * </ul>
 * </p>
 * <p>
 * Action: {@link #getCommand(EditToolHandler)} returns the command initialized to trim the lines at
 * the right of the one in the blackboard.
 * </p>
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class TrimGeometryBehaviour implements Behaviour {

    private EditingToolsCommandFactory commandFactory;

    public TrimGeometryBehaviour( EditingToolsCommandFactory commandFactory ) {
        this.commandFactory = commandFactory;
    }

    /**
     * Valid if current shape is not null and has at least two coordinates
     */
    public boolean isValid( EditToolHandler handler ) {
        PrimitiveShape currentShape = handler.getCurrentShape();
        if (currentShape == null) {
            return false;
        }
        int nCoords = currentShape.getNumCoords();
        return nCoords > 1;
    }

    public UndoableMapCommand getCommand( EditToolHandler handler ) {
        final ILayer selectedLayer = handler.getContext().getSelectedLayer();
        LineString trimmingLineInLayerCrs;
        try {
            trimmingLineInLayerCrs = getTrimmingLineInLayerCrs(handler);
        } catch (OperationNotFoundException e) {
            throw (RuntimeException) new RuntimeException("Cannot reproject trim line from "
                    + "layer's crs to map's crs").initCause(e);
        } catch (TransformException e) {
            throw (RuntimeException) new RuntimeException("Cannot reproject trim line from "
                    + "layer's crs to map's crs").initCause(e);
        }

        FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToTrim;
        try {
            featuresToTrim = getFeaturesToTrim(selectedLayer, trimmingLineInLayerCrs);
        } catch (OperationNotFoundException e) {
        	throw new IllegalStateException( e.getMessage(), e );
        } catch (IOException e) {
        	throw new IllegalStateException( e.getMessage(), e );
        } catch (TransformException e) {
        	throw new IllegalStateException( e.getMessage(), e );
        }

        featuresToTrim.size();
        UndoableMapCommand trimCommand = commandFactory
                                                       .createTrimFeaturesCommand(handler,
                                                                                  selectedLayer,
                                                                                  featuresToTrim,
                                                                                  trimmingLineInLayerCrs);

        return trimCommand;
    }

    private FeatureCollection<SimpleFeatureType, SimpleFeature> getFeaturesToTrim( ILayer selectedLayer, LineString trimmingLine ) throws IOException, OperationNotFoundException, TransformException {
        Filter extraFilter = createTrimmingLineFilter(selectedLayer, trimmingLine);
        FeatureCollection<SimpleFeatureType, SimpleFeature> selection = LayerUtil.getSelectedFeatures(selectedLayer,
                                                                    extraFilter);
        return selection;
    }

    /**
     * Returns the line drawn as the trimming line, transformed to JTS LineString, in the selected
     * layer's CRS
     * 
     * @param handler the {@link EditToolHandler} from where to grab the current shape (the one
     *        drawn as the cutting line)
     * @return
     * @throws TransformException
     * @throws OperationNotFoundException
     */
    private LineString getTrimmingLineInLayerCrs( EditToolHandler handler ) throws OperationNotFoundException, TransformException {

        final PrimitiveShape currentShape = handler.getCurrentShape();
        final LineString line = GeometryCreationUtil.createGeom(LineString.class, currentShape,true);
        return line;
    }

    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
        // TODO: log through this plugin
        // EditPlugin.log("", error); //$NON-NLS-1$
        DialogUtil.openError(Messages.TrimGeometryBehaviour_operation_failed, error.getMessage());
    }

    private Filter createTrimmingLineFilter( ILayer selectedLayer, LineString trimmingLineInLayerCrs ) throws OperationNotFoundException, TransformException {
        Filter filter = selectedLayer.getFilter();
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        Intersects intersectsFilter;
        try {

            // final CoordinateReferenceSystem geomCrs;
            // geomCrs = (CoordinateReferenceSystem) trimmingLineInLayerCrs.getUserData();
            // final CoordinateReferenceSystem reprojectCrs = LayerUtil.getLayerCrs(selectedLayer);
            // final Geometry literal = GeoToolsUtils.reproject(trimmingLineInLayerCrs, geomCrs,
            // reprojectCrs);

            SimpleFeatureType schema = selectedLayer.getSchema();
			String typeName = schema.getDefaultGeometry().getLocalName();
			
			intersectsFilter = ff.intersects( ff.property(typeName), ff.literal(trimmingLineInLayerCrs));
        } catch (IllegalFilterException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }

        if (Filter.EXCLUDE.equals(filter)) {
            filter = intersectsFilter;
        } else {
            filter = ff.and( filter, intersectsFilter);
        }
        return filter;
    }

}
