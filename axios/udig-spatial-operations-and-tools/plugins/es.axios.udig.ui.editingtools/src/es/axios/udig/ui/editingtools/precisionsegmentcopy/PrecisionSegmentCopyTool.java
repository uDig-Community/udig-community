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
package es.axios.udig.ui.editingtools.precisionsegmentcopy;

import java.util.List;
import java.util.Set;

import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.tools.edit.AbstractEditTool;
import net.refractions.udig.tools.edit.Activator;
import net.refractions.udig.tools.edit.Behaviour;
import net.refractions.udig.tools.edit.EditToolConfigurationHelper;
import net.refractions.udig.tools.edit.EnablementBehaviour;
import net.refractions.udig.tools.edit.activator.DrawCurrentGeomVerticesActivator;
import net.refractions.udig.tools.edit.activator.DrawGeomsActivator;
import net.refractions.udig.tools.edit.activator.EditStateListenerActivator;
import net.refractions.udig.tools.edit.activator.GridActivator;
import net.refractions.udig.tools.edit.activator.ResetAllStateActivator;
import net.refractions.udig.tools.edit.activator.SetRenderingFilter;
import net.refractions.udig.tools.edit.activator.SetSnapBehaviourCommandHandlerActivator;
import net.refractions.udig.tools.edit.behaviour.AcceptOnDoubleClickBehaviour;
import net.refractions.udig.tools.edit.behaviour.DefaultCancelBehaviour;
import net.refractions.udig.tools.edit.behaviour.SetSnapSizeBehaviour;
import net.refractions.udig.tools.edit.behaviour.accept.AcceptChangesBehaviour;
import net.refractions.udig.tools.edit.enablement.ValidToolDetectionActivator;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import es.axios.udig.ui.editingtools.internal.commons.behaviour.DrawOrthoAxesActivator;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.SegmentCopyContext;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.SegmentCopyPreview;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.behaviour.CreateBBoxEventBehaviour;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.behaviour.SetInitialPointEventBehaviour;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.behaviour.SetReferenceSegmentEventBehaviour;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.view.SegmentCopyParametersView;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.behaviour.DrawSnapAreaBehaviour;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.behaviour.PrecisionToolAcceptBehaviour;

/**
 * Creates a precision parallel line.
 * 
 * With a reference line and an initial point, create a parallel line which
 * could change the distance between the reference line.
 * 
 * FIXME If it doesn't use the snap, the map doesn't repaint correctly.
 * 
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public class PrecisionSegmentCopyTool extends AbstractEditTool {

	private SegmentCopyContext			segmentCopyContext	= new SegmentCopyContext();
	private SegmentCopyPreview			segmentCopyPreview	= null;
	private SegmentCopyParametersView	view				= null;

	@Override
	public void setActive(boolean active) {
		super.setActive(active);

		if (active) {

			segmentCopyContext.setHandler(getHandler());
			segmentCopyContext.initContext();
			segmentCopyContext.setEditBlackBoard(getHandler().getEditBlackboard(getHandler().getEditLayer()));
			segmentCopyPreview = new SegmentCopyPreview(getContext(), getHandler(), segmentCopyContext);
			segmentCopyContext.addObserver(segmentCopyPreview);

			Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
					// run the view, set the parallelcontext and add the
					// parameters view as observer.
					ApplicationGIS.getView(true, SegmentCopyParametersView.id);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewPart viewPart = page.findView(SegmentCopyParametersView.id);
					view = (SegmentCopyParametersView) viewPart;
					assert view != null;
					view.setSegmentCopyContext(segmentCopyContext);
				}

			});
		} else {
			Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
					// When the tool is deactivated, hide the view.
					ApplicationGIS.getView(false, SegmentCopyParametersView.id);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewPart viewPart = page.findView(SegmentCopyParametersView.id);
					page.hideView(viewPart);
				}
			});
		}
	}

	@Override
	protected void initActivators(Set<Activator> activators) {
		activators.add(new EditStateListenerActivator());
		activators.add(new DrawGeomsActivator(DrawGeomsActivator.DrawType.LINE));
		activators.add(new SetSnapBehaviourCommandHandlerActivator());
		activators.add(new DrawCurrentGeomVerticesActivator());
		activators.add(new DrawOrthoAxesActivator());
		activators.add(new ResetAllStateActivator());
		activators.add(new SetRenderingFilter());
		activators.add(new GridActivator());
	}

	@Override
	protected void initAcceptBehaviours(List<Behaviour> acceptBehaviours) {
		acceptBehaviours.add(new AcceptChangesBehaviour(LineString.class, false));
		acceptBehaviours.add(new PrecisionToolAcceptBehaviour(segmentCopyContext));
		// acceptBehaviours.add(new DeselectEditShapeAcceptBehaviour());
	}

	@Override
	protected void initCancelBehaviours(List<Behaviour> cancelBehaviours) {
		cancelBehaviours.add(new DefaultCancelBehaviour());
	}

	@Override
	protected void initEventBehaviours(EditToolConfigurationHelper helper) {

		// helper.add(new DrawCreateVertexSnapAreaBehaviour());
		helper.add(new DrawSnapAreaBehaviour());
		helper.startMutualExclusiveList();
		helper.add(new SetReferenceSegmentEventBehaviour(segmentCopyContext));
		helper.add(new SetInitialPointEventBehaviour(segmentCopyContext));
		helper.stopMutualExclusiveList();
		helper.add(new CreateBBoxEventBehaviour(segmentCopyContext));

		helper.add(new SetSnapSizeBehaviour());
		helper.add(new AcceptOnDoubleClickBehaviour());
		helper.done();

	}

	@Override
	protected void initEnablementBehaviours(List<EnablementBehaviour> enablementBehaviours) {
		enablementBehaviours.add(new ValidToolDetectionActivator(new Class[] {
				Geometry.class,
				LineString.class,
				MultiLineString.class }));
		// enablementBehaviours.add(new WithinLegalLayerBoundsBehaviour());
	}

}
