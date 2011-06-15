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
package es.axios.udig.ui.editingtools.precisionparallels.internal.command;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import net.refractions.udig.project.ui.commands.AbstractDrawCommand;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.commands.StyleStrategy;
import net.refractions.udig.tools.edit.support.CurrentEditGeomPathIterator;
import net.refractions.udig.tools.edit.support.EditGeom;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;

import es.axios.udig.ui.editingtools.precisionparallels.internal.ParallelContext;

/**
 * Purpose of this class is to highlight the reference line.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class DrawGeomColorCommnad extends AbstractDrawCommand {

	private ParallelContext	parallelContext	= null;
	private StyleStrategy	colorStrategy	= new StyleStrategy();
	private EditToolHandler	handler			= null;

	public DrawGeomColorCommnad(EditToolHandler handler, ParallelContext parallelContext) {

		this.handler = handler;
		this.parallelContext = parallelContext;
	}

	public Rectangle getValidArea() {

		return null;
	}

	public void run(IProgressMonitor monitor) throws Exception {
		// When there is no reference line to draw, return.
		if (parallelContext.getReferenceLine() == null) {
			return;
		}
		List<EditGeom> geoms = new LinkedList<EditGeom>();
		geoms.add(parallelContext.getReferenceLine());

		for (EditGeom geom : geoms) {

			if (geom.getShell().getNumPoints() > 0) {
				CurrentEditGeomPathIterator pathIterator = CurrentEditGeomPathIterator.getPathIterator(geom);
				pathIterator.setLocation(null, null);
				fillPath(geom, pathIterator);
			}
		}
	}

	private void fillPath(EditGeom geom, CurrentEditGeomPathIterator pathIterator) {
		Path shape = pathIterator.toPath(Display.getCurrent());
		try {

			if (graphics != null) {
				colorStrategy.setLineColor2(graphics, geom, handler);
				graphics.drawPath(shape);

				colorStrategy.setLineColor(graphics, geom, handler);
				graphics.drawPath(shape);
			}
		}
		finally {
			if (shape != null) {
				shape.dispose();
			}
		}
	}

}
