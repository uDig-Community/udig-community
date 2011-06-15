/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */package es.axios.udig.spatialoperations.ui.parameters;

import org.eclipse.swt.widgets.Composite;

import es.axios.udig.spatialoperations.internal.ui.parameters.buffer.BufferParametersPresenterFactory;
import es.axios.udig.spatialoperations.internal.ui.parameters.clip.ClipParametersPresenterFactory;
import es.axios.udig.spatialoperations.internal.ui.parameters.dissolve.DissolveParametersPresenterFactory;
import es.axios.udig.spatialoperations.internal.ui.parameters.fill.FillParametersPresenterFactory;
import es.axios.udig.spatialoperations.internal.ui.parameters.hole.HoleParametersPresenterFactory;
import es.axios.udig.spatialoperations.internal.ui.parameters.intersect.IntersectParametersPresenterFactory;
import es.axios.udig.spatialoperations.internal.ui.parameters.polygontoline.PolygonToLineParametersFactory;
import es.axios.udig.spatialoperations.internal.ui.parameters.spatialjoingeom.SpatialJoinParameterPresenterFactory;
import es.axios.udig.spatialoperations.internal.ui.parameters.split.SplitParametersPresenterFactory;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.BufferCommand;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.ClipCommand;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.DissolveCommand;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.FillCommand;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.HoleCommand;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.IntersectCommand;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.PolygonToLineCommand;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.SpatialJoinGeomCommand;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.SplitCommand;

/**
 * Builds the collaborations required to present and execute the default spatial operation.
 * 
 * <p>
 * This builder creates the parameter presenter and the command that execute the default spatial operation.
 * We understand as <b>default</b> those operations provided by this framework.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0 
 *
 */
final public class SpatialOperationDefaultBuilder extends AbstractSpatialOperationBuilder {
	
	
	/**
	 * Creates the following collaborations: 
	 * buffer, Intersect, JoinGeometries, Dissolve, Split, PolygonToLine, Fill, Hole. 
	 * @see es.axios.udig.spatialoperations.ui.parameters.ISOBuilder#build(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void build(final Composite container){
		
		addCollaboration(
				 container, new BufferParametersPresenterFactory(), 
				 new BufferCommand());

		addCollaboration(
				container,new ClipParametersPresenterFactory(), 
				new ClipCommand());

		addCollaboration(
				container, new IntersectParametersPresenterFactory(),
				new IntersectCommand());

		addCollaboration(
				container, new SpatialJoinParameterPresenterFactory(),  
				new SpatialJoinGeomCommand());

		addCollaboration(
				container, new DissolveParametersPresenterFactory(),
				new DissolveCommand());

		addCollaboration(
				container, new SplitParametersPresenterFactory(),
				new SplitCommand());

		addCollaboration(
				container, new PolygonToLineParametersFactory() , 
				new PolygonToLineCommand());

		addCollaboration(
				container, new FillParametersPresenterFactory(),
				new FillCommand());

		addCollaboration(
				container, new HoleParametersPresenterFactory(), 
				new  HoleCommand());
	}

}
