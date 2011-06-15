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
package es.axios.udig.ui.editingtools.internal.commands;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.command.Command;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.tools.edit.EditToolHandler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.LineString;

/**
 * Mock implementation of {@link EditingToolsCommandFactory} to support unit
 * tests.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class MockEdittingToolsCommandFactory {

	public UndoableMapCommand createMergeFeaturesCommand(	ILayer layer,
															FeatureCollection sourceFeatures,
															SimpleFeature mergedFeature) {
		throw new UnsupportedOperationException();
	}

	public UndoableMapCommand createSplitFeaturesCommand(EditToolHandler handler) {
		MockSplitCommand mock = new MockSplitCommand(handler);
		return mock;
	}

	public UndoableMapCommand createTrimFeaturesCommand(EditToolHandler handler,
														ILayer selectedLayer,
														FeatureCollection featuresToTrim,
														LineString trimmingLine) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Base class for mock commands. Extend as needed to provide specific test
	 * behaviour.
	 */
	public static abstract class MockUndoableMapCommand implements UndoableMapCommand {

		public IMap	map;

		public Map getMap() {
			return (Map) map;
		}

		public void setMap(IMap map) {
			this.map = map;
		}

		public Command copy() {
			throw new UnsupportedOperationException();
		}

		public String getName() {
			return null;
		}

		public void run(IProgressMonitor monitor) throws Exception {
			throw new UnsupportedOperationException();
		}

		public void rollback(IProgressMonitor monitor) throws Exception {
			throw new UnsupportedOperationException();
		}
	}

	public static class MockSplitCommand extends MockUndoableMapCommand {

		public EditToolHandler	handler;

		public MockSplitCommand(EditToolHandler handler) {
			this.handler = handler;
		}
	}
}
