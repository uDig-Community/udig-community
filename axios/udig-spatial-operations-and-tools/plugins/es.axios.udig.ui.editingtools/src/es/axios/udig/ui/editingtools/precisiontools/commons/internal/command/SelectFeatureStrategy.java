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
package es.axios.udig.ui.editingtools.precisiontools.commons.internal.command;

import net.refractions.udig.project.command.UndoableComposite;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.tools.edit.EditPlugin;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.commands.SelectFeatureAsEditFeatureCommand;
import net.refractions.udig.tools.edit.commands.SelectionParameter;
import net.refractions.udig.tools.edit.commands.SelectionStrategy;
import net.refractions.udig.tools.edit.support.Point;

import org.eclipse.core.runtime.IProgressMonitor;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Strategy object used to select a feature from the blackboard. Copy of
 * net.refractions.udig.tools.edit.commands.selection.SelectFeatureStrategy but
 * only with the necessary code for this case.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class SelectFeatureStrategy implements SelectionStrategy {

	public void run(IProgressMonitor monitor,
					UndoableComposite commands,
					SelectionParameter parameters,
					SimpleFeature feature,
					boolean firstFeature) {

		EditToolHandler handler = parameters.handler;
		Class<? extends Geometry>[] acceptableClasses = parameters.acceptableClasses;
		MapMouseEvent event = parameters.event;

		for (Class<? extends Geometry> clazz : acceptableClasses) {
			if (clazz.isAssignableFrom(feature.getDefaultGeometry().getClass())) {

				commands.addCommand(new SelectFeatureAsEditFeatureCommand(handler, feature, handler.getEditLayer(),
							Point.valueOf(event.x, event.y)));

			} else {
				EditPlugin.trace(EditPlugin.SELECTION,
							"Feature is not one of the acceptable classes " + feature.getID(), null); //$NON-NLS-1$
			}
		}

	}

}
