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
package es.axios.udig.ui.editingtools.parallel;

import java.util.Set;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.tool.edit.internal.Messages;
import net.refractions.udig.tools.edit.AbstractEditTool;
import net.refractions.udig.tools.edit.Activator;
import net.refractions.udig.tools.edit.EditToolConfigurationHelper;
import net.refractions.udig.tools.edit.EditToolHandler;
import net.refractions.udig.tools.edit.activator.AdvancedBehaviourCommandHandlerActivator;
import net.refractions.udig.tools.edit.activator.DeleteGlobalActionSetterActivator;
import net.refractions.udig.tools.edit.activator.DrawCurrentGeomVerticesActivator;
import net.refractions.udig.tools.edit.activator.DrawGeomsActivator;
import net.refractions.udig.tools.edit.activator.EditStateListenerActivator;
import net.refractions.udig.tools.edit.activator.GridActivator;
import net.refractions.udig.tools.edit.activator.SetRenderingFilter;
import net.refractions.udig.tools.edit.behaviour.CursorControlBehaviour;
import net.refractions.udig.tools.edit.behaviour.InsertVertexOnEdgeBehaviour;
import net.refractions.udig.tools.edit.behaviour.MoveGeometryBehaviour;
import net.refractions.udig.tools.edit.behaviour.MoveVertexBehaviour;
import net.refractions.udig.tools.edit.behaviour.SelectFeatureBehaviour;
import net.refractions.udig.tools.edit.behaviour.SelectVertexBehaviour;
import net.refractions.udig.tools.edit.behaviour.SelectVertexOnMouseDownBehaviour;
import net.refractions.udig.tools.edit.behaviour.SelectVerticesWithBoxBehaviour;
import net.refractions.udig.tools.edit.behaviour.SetSnapSizeBehaviour;
import net.refractions.udig.tools.edit.behaviour.StartEditingBehaviour;
import net.refractions.udig.tools.edit.behaviour.StartExtendLineBehaviour;
import net.refractions.udig.tools.edit.impl.ConditionalProvider;
import net.refractions.udig.tools.edit.impl.LineTool;
import net.refractions.udig.tools.edit.support.ShapeType;

import org.eclipse.swt.SWT;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;

import es.axios.udig.ui.editingtools.internal.commons.behaviour.AddVertexWithProviderWhileCreatingBehaviour;
import es.axios.udig.ui.editingtools.internal.commons.behaviour.DoubleClickRunAcceptWithProviderBehaviour;

/**
 * Temporal replacement for {@link LineTool} while we define the framework for
 * adding dynamic behaviour modifiers
 * <p>
 * TODO: move changes to {@link LineTool} and eliminate this class once the
 * approach is accepted
 * </p>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public class LineTool2 extends LineTool {

	/**
	 * Overrides {@link AbstractEditTool#setActive(boolean)} so we can set and
	 * remove the {@link EditToolHandler} from the map's blackboard as
	 * appropriate.
	 * <p>
	 * TODO: If the approach we're using to add modes to edit tools is accepted
	 * and incorporated into uDig, put this code directly in
	 * {@link AbstractEditTool}
	 * </p>
	 */
	@Override
	public void setActive(boolean active) {
		super.setActive(active);

		final String key = EditToolHandler.class.getName();
		final IBlackboard blackboard = getContext().getMap().getBlackboard();
		if (active) {
			blackboard.put(key, handler);
		} else {
			blackboard.put(key, null);
		}
	}

	@Override
	protected void initActivators(Set<Activator> activators) {
		activators.add(new EditStateListenerActivator());
		activators.add(new DeleteGlobalActionSetterActivator());

		activators.add(new DrawGeomsActivator(DrawGeomsActivator.DrawType.LINE));

		activators.add(new DrawCurrentGeomVerticesActivator());
		// /activators.add(new SetSnapBehaviourCommandHandlerActivator());
		activators.add(new AdvancedBehaviourCommandHandlerActivator());
		activators.add(new SetRenderingFilter());
		activators.add(new GridActivator());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initEventBehaviours(EditToolConfigurationHelper helper) {
		// //helper.add(new DrawCreateVertexSnapAreaBehaviour());

		helper.startAdvancedFeatures();
		helper.add(new CursorControlBehaviour(handler, new ConditionalProvider(handler,
					Messages.LineTool_select_or_create_feature, Messages.LineTool_add_vertex_or_finish),
					new CursorControlBehaviour.SystemCursorProvider(SWT.CURSOR_SIZEALL), new ConditionalProvider(
								handler, Messages.LineTool_move_vertex, null),
					new CursorControlBehaviour.SystemCursorProvider(SWT.CURSOR_CROSS), new ConditionalProvider(handler,
								Messages.LineTool_add_vertex, null)));
		helper.stopAdvancedFeatures();

		// vertex selection OR geometry selection should not both happen so make
		// them a mutual
		// exclusion behaviour
		helper.startMutualExclusiveList();

		helper.add(new AddVertexWithProviderWhileCreatingBehaviour());

		helper.startAdvancedFeatures();
		helper.add(new SelectVertexOnMouseDownBehaviour());
		helper.add(new SelectVertexBehaviour());
		helper.stopAdvancedFeatures();

		helper.startAdvancedFeatures();
		SelectFeatureBehaviour selectGeometryBehaviour = new SelectFeatureBehaviour(new Class[] {
				LineString.class,
				LinearRing.class,
				MultiLineString.class }, Intersects.class);

		selectGeometryBehaviour.initDefaultStrategies(ShapeType.LINE);
		helper.add(selectGeometryBehaviour);
		helper.add(new InsertVertexOnEdgeBehaviour());

		helper.startElseFeatures();
		helper.add(new StartEditingBehaviour(ShapeType.LINE));
		helper.stopElseFeatures();

		helper.stopAdvancedFeatures();
		helper.stopMutualExclusiveList();

		helper.startAdvancedFeatures();
		helper.startMutualExclusiveList();
		helper.add(new MoveVertexBehaviour());
		helper.add(new MoveGeometryBehaviour());
		helper.stopMutualExclusiveList();

		helper.add(new SelectVerticesWithBoxBehaviour());
		helper.stopAdvancedFeatures();

		// /helper.add(new DoubleClickRunAcceptBehaviour());
		helper.add(new DoubleClickRunAcceptWithProviderBehaviour());

		helper.add(new SetSnapSizeBehaviour());
		helper.add(new StartExtendLineBehaviour());

		helper.done();
	}
}
