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
package es.axios.udig.ui.editingtools.arc.internal;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.command.factory.EditCommandFactory;
import net.refractions.udig.project.internal.commands.edit.CreateFeatureCommand;
import net.refractions.udig.tools.edit.Behaviour;
import net.refractions.udig.tools.edit.EditState;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.DialogUtil;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.GeometryUtil;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.editingtools.internal.i18n.Messages;

/**
 * Acceptance behaviour for the arc creation tool.
 * <p>
 * This behaviour uses an ArcBuilder to create an arc's approximation as a JTS LineString and
 * returns {@link CreateFeatureCommand} with the arc approximation as default geometry.
 * </p>
 * <p>
 * The geometry is created in the current map's {@link CoordinateReferenceSystem} and then projected
 * back to the current layer's CRS.
 * </p>
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 * @see ArcBuilder
 */
public class CreateArcBehaviour implements Behaviour {

    /**
     * Valid if current shape is not null and has exactly three points.
     */
    public boolean isValid( EditToolHandler handler ) {
        PrimitiveShape currentShape = handler.getCurrentShape();
        if (currentShape == null) {
            return false;
        }
        int nCoords = currentShape.getNumCoords();
        return nCoords == 3;
    }

    public UndoableMapCommand getCommand( EditToolHandler handler ) {
        final PrimitiveShape currentShape = handler.getCurrentShape();
        final ILayer editLayer = handler.getEditLayer();

        // need to use map coordinates in order to avoid
        // possible inconsistencies between what the user
        // drawn and the projected result
        GeometryFactory gf = new GeometryFactory();
        CoordinateReferenceSystem layerCrs = LayerUtil.getCrs(editLayer);
        CoordinateReferenceSystem mapCrs = editLayer.getMap().getViewportModel().getCRS();
        Point p1 = gf.createPoint(currentShape.getCoord(0));
        Point p2 = gf.createPoint(currentShape.getCoord(1));
        Point p3 = gf.createPoint(currentShape.getCoord(2));

        try {
            p1 = (Point) GeoToolsUtils.reproject(p1, layerCrs, mapCrs);
            p2 = (Point) GeoToolsUtils.reproject(p2, layerCrs, mapCrs);
            p3 = (Point) GeoToolsUtils.reproject(p3, layerCrs, mapCrs);
        } catch (OperationNotFoundException onfe) {
            throw new IllegalStateException( "Could not reproject to map crs:"+ onfe.getLocalizedMessage(), onfe );
        } catch (TransformException te) {
        	throw new IllegalStateException( "Could not reproject to map crs:"+te.getLocalizedMessage(), te );
        }

        ArcBuilder builder = new ArcBuilder();
        builder.setPoints(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());

        // TODO: especificar la cantidad de segmentos por cuadrante
        // mediante una preferencia
        Geometry geom = builder.getGeometry(15);
        if (geom == null) {
            throw new IllegalStateException("null geom");
        }

        // backproject resulting geom from map crs to layer's crs
        try {
            geom = GeoToolsUtils.reproject(geom, mapCrs, layerCrs);
        } catch (OperationNotFoundException onfe) {
        	throw new IllegalStateException( "Could not reproject back to data crs:"+ onfe.getLocalizedMessage(), onfe );
        } catch (TransformException te) {
            throw new IllegalStateException( "Could not reproject back to data crs:"+te.getLocalizedMessage(), te );
        }

        EditCommandFactory editCmdFac = AppGISMediator.getEditCommandFactory();

        ILayer layer = editLayer;
        SimpleFeatureType schema = layer.getSchema();
        SimpleFeature feature;
        try {
        	feature = SimpleFeatureBuilder.build( schema, (Object[]) null, null );
            Class type = schema.getDefaultGeometry().getType().getBinding();
            geom = GeometryUtil.adapt(geom, type);
            feature.setDefaultGeometry(geom);
        } catch (IllegalAttributeException e) {
        	// consider using the bubble feedback
            throw new IllegalStateException("Could not create Arc:"+e,e );
        }
        UndoableMapCommand command = editCmdFac.createAddFeatureCommand(feature, layer);

        handler.setCurrentShape(null);
        handler.setCurrentState(EditState.NONE);

        return command;
    }

    public void handleError( EditToolHandler handler, Throwable error, UndoableMapCommand command ) {
        error.printStackTrace();
        DialogUtil.openError("Create arc failed to execute", error.getMessage());
    }
}
