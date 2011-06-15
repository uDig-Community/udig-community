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

import java.awt.Point;

import junit.framework.TestCase;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.command.MapCommand;
import net.refractions.udig.project.command.NavCommand;
import net.refractions.udig.project.command.factory.BasicCommandFactory;
import net.refractions.udig.project.command.factory.NavigationCommandFactory;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.PrimitiveShape;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.editingtools.support.TestHandler;
import es.axios.udig.ui.testsupport.TestWorkBenchBuilder;

/**
 * Test suite for {@link SplitFeaturesCommand}
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class SplitFeaturesCommandTest extends TestCase {

    private TestWorkBenchBuilder testData;

    protected void setUp() throws Exception {
        super.setUp();
        testData = new TestWorkBenchBuilder();
        testData.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        testData.tearDown();
    }

    public void testRollback() {
    }

    /**
     * Test method for
     * {@link es.axios.udig.ui.editingtools.internal.commands.SplitFeaturesCommand#getSplittingLineInMapCRS(org.opengis.referencing.crs.CoordinateReferenceSystem)}.
     * 
     * @throws Exception
     */
    public void testGetSplittingLineInMapCRS() throws Exception {
        final TestHandler handler = new TestHandler();
        final IMap map = handler.getContext().getMap();
        final ILayer selectedLayer = handler.getContext().getSelectedLayer();
        final CoordinateReferenceSystem layerCrs = DefaultGeographicCRS.WGS84;
        final CoordinateReferenceSystem mapCrs = CRS.decode("EPSG:23030");
        final Envelope approxSpainEnvelope = new Envelope(-12580, 1048000, 4056000, 4833000);

        // the one drawn by the user
        final LineString splitLineInLayerCrs;
        // the one expected
        final LineString splitLineInMapCrs;

        // set up the edit blackboard with a line crossing the whole vieport at middle height
        PrimitiveShape splitter = setUpMapState(map, handler, selectedLayer, layerCrs, mapCrs,
                                                approxSpainEnvelope);
        Coordinate coord1InLayerCrs = splitter.getCoord(0);
        Coordinate coord2InLayerCrs = splitter.getCoord(1);

        GeometryFactory geometryFactory = new GeometryFactory();
        splitLineInLayerCrs = geometryFactory.createLineString(new Coordinate[]{coord1InLayerCrs,
                coord2InLayerCrs});

        splitLineInMapCrs = (LineString) GeoToolsUtils.reproject(splitLineInLayerCrs, layerCrs,
                                                                 mapCrs);

        final LineString commandCreatedSplitLine = SplitFeaturesCommand
                                                                       .getSplittingLineInMapCRS(handler);

        assertNotNull(commandCreatedSplitLine);
        // finally, verify the command projected the splitting line to the map's crs
        assertSame(mapCrs, commandCreatedSplitLine.getUserData());

        splitLineInMapCrs.normalize();
        commandCreatedSplitLine.normalize();
        assertTrue(splitLineInMapCrs.equalsExact(commandCreatedSplitLine));
    }

    private PrimitiveShape setUpMapState( final IMap map, final TestHandler handler,
                                          final ILayer selectedLayer,
                                          final CoordinateReferenceSystem layerCrs,
                                          final CoordinateReferenceSystem mapCrs,
                                          final Envelope approxSpainEnvelope ) {
        PrimitiveShape splitter;
        // force selectedLayer to declare it's in WGS84
        ((Layer) selectedLayer).setCRS(layerCrs);
        // verification check
        assertSame(layerCrs, selectedLayer.getCRS());

        // set map in a different CRS than the layer (EPSG:23030 - ED50/UTM zone 30M, for instance)
        BasicCommandFactory basicCommandFactory = BasicCommandFactory.getInstance();
        MapCommand setMapCrsCommand = basicCommandFactory.createChangeCRS(map, mapCrs);
        map.sendCommandSync(setMapCrsCommand);
        // just a verification check before continuing
        assertSame(mapCrs, map.getViewportModel().getCRS());

        // set the map's viewport to about the extent of spain
        NavCommand setBboxCommand = NavigationCommandFactory
                                                            .getInstance()
                                                            .createSetViewportBBoxCommand(
                                                                                          approxSpainEnvelope);
        map.sendCommandSync(setBboxCommand);
        // check point befor continuing
        assertTrue(map.getViewportModel().getBounds().intersects(approxSpainEnvelope));

        // // finished setting up test environment ////

        // create user defined split line in the edit blackboard
        // as a line crossing the viewport at the mid height
        IViewportModel viewport = map.getViewportModel();
        Envelope bounds = viewport.getBounds();
        double midY = bounds.getMinY() + (bounds.getHeight() / 2);
        Point p1 = viewport.worldToPixel(new Coordinate(bounds.getMinX(), midY));
        Point p2 = viewport.worldToPixel(new Coordinate(bounds.getMaxX(), midY));

        EditBlackboard bb = handler.getEditBlackboard();
        splitter = bb.newGeom(null, null).getShell();
        handler.setCurrentShape(splitter);
        bb.addPoint(p1.x, p1.y, splitter);
        bb.addPoint(p2.x, p2.y, splitter);

        return splitter;
    }

}
