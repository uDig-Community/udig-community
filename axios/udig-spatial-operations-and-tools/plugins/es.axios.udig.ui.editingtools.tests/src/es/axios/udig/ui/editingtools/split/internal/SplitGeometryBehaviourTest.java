/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
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
package es.axios.udig.ui.editingtools.split.internal;

import junit.framework.TestCase;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.tools.edit.support.EditBlackboard;
import net.refractions.udig.tools.edit.support.PrimitiveShape;
import es.axios.udig.ui.editingtools.internal.commands.MockEdittingToolsCommandFactory;
import es.axios.udig.ui.editingtools.internal.commands.MockEdittingToolsCommandFactory.MockSplitCommand;
import es.axios.udig.ui.editingtools.support.TestHandler;

/**
 * Test suite for {@link SplitGeometryBehaviour}
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class SplitGeometryBehaviourTest extends TestCase {

    private MockEdittingToolsCommandFactory mockCommandFactory;
    private SplitGeometryBehaviour          behaviour;
    private TestHandler                     handler;

    protected void setUp() throws Exception {
        super.setUp();
        mockCommandFactory = new MockEdittingToolsCommandFactory();
//        behaviour = new SplitGeometryBehaviour(mockCommandFactory);
        handler = new TestHandler();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        behaviour = null;
        handler = null;
    }

    /**
     * Tests {@link SplitGeometryBehaviour#isValid(net.refractions.udig.tools.edit.EditToolHandler)}
     */
    public void testIsValid() {
        EditBlackboard bb = handler.getEditBlackboard();

        handler.setCurrentShape(null);
        assertFalse(behaviour.isValid(handler));

        PrimitiveShape splitter = bb.newGeom(null, null).getShell();
        handler.setCurrentShape(splitter);

        bb.addPoint(30, 0, splitter);

        // only one point, can't get a line
        assertFalse(behaviour.isValid(handler));

        // got enough as to build a line
        bb.addPoint(30, 60, splitter);

        assertTrue(behaviour.isValid(handler));

        // more points in a linestring still valid
        bb.addPoint(60, 60, splitter);
        assertTrue(behaviour.isValid(handler));
    }

    // Test commented out as behaviour.getSplittingLine is private and its
    // result is validated with assertions
    // /**
    // * Tests the linestring is built and contains the Map's CRS information
    // */
    // public void testGetSplittingLine() {
    // EditBlackboard bb = handler.getEditBlackboard();
    //
    // PrimitiveShape splitter = bb.newGeom(null, null).getShell();
    // handler.setCurrentShape(splitter);
    // bb.addPoint(30, 0, splitter);
    // bb.addPoint(30, 60, splitter);
    //        
    // LineString splittingLine = behaviour.getSplittingLine(handler);
    // assertNotNull(splittingLine);
    // Object userData = splittingLine.getUserData();
    // assertNotNull(userData);
    // assertTrue(userData instanceof CoordinateReferenceSystem);
    //        
    // IMap map = handler.getContext().getMap();
    // IViewportModel viewportModel = map.getViewportModel();
    // CoordinateReferenceSystem mapCrs = viewportModel.getCRS();
    // assertEquals(mapCrs, userData);
    // }

    /**
     *
     */
    public void testGetCommand() {
        EditBlackboard bb = handler.getEditBlackboard();
        PrimitiveShape splitter = bb.newGeom(null, null).getShell();
        handler.setCurrentShape(splitter);
        bb.addPoint(30, 0, splitter);
        bb.addPoint(30, 60, splitter);

        UndoableMapCommand command = behaviour.getCommand(handler);
        assertNotNull(command);
        assertTrue(command instanceof MockEdittingToolsCommandFactory.MockSplitCommand);

        MockEdittingToolsCommandFactory.MockSplitCommand split;
        split = (MockSplitCommand) command;

        assertSame(handler, split.handler);
    }
}
