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
package es.axios.udig.spatialoperations.ui.taskmanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import es.axios.udig.spatialoperations.descriptor.ISODescriptor;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IBufferParameters;
import es.axios.udig.spatialoperations.internal.parameters.IClipParameters;
import es.axios.udig.spatialoperations.internal.parameters.IDissolveParameters;
import es.axios.udig.spatialoperations.internal.parameters.IFillParameters;
import es.axios.udig.spatialoperations.internal.parameters.IHoleParameters;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectParameters;
import es.axios.udig.spatialoperations.internal.parameters.IPolygonToLineParameters;
import es.axios.udig.spatialoperations.internal.parameters.ISpatialJoinGeomParameters;
import es.axios.udig.spatialoperations.internal.parameters.ISplitParameters;
import es.axios.udig.ui.commons.util.DialogUtil;

/**
 * Facade for spatial operation processes.
 * <p>
 * This class implement the methods to execute the spatial operations.
 * 
 * </p>
 * TODO The process fail exception require to be improved. Now, if the process
 * fail the user is not informed. Thinking that this processes are asynchronous
 * an special mechanism will be required like an ProcessMonitor with its view
 * (or panel in process). Then the user could inspect the status (running,
 * finished, progress ) of all process.
 * 
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 * @since 1.1.0
 */
public final class SOTaskManager implements ISOTaskManager {

	private static Sequencer					SEQUENCER		= Sequencer.getInstance();

	private static Map<Integer, ISOTaskMonitor>	PID_MONITOR_MAP	= Collections
																			.synchronizedMap(new HashMap<Integer, ISOTaskMonitor>());

	private static SOTaskManager THIS = new SOTaskManager();
	
	public static ISOTaskManager getInstance(){
		
		return THIS;
	}
	
	
	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#dissolveOperation(es.axios.udig.spatialoperations.internal.parameters.IDissolveParameters)
	 */
	public Integer dissolveOperation(final IDissolveParameters params) {

		final ISOTaskMonitor monitor = DissolveMonitor.newInstance(params);

		DialogUtil.runInProgressDialog(Messages.SOTaskManager_dissolve_operation, true, monitor, true, true);

		Integer pid = SEQUENCER.next();
		PID_MONITOR_MAP.put(pid, monitor);

		return pid;
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#isDone(java.lang.Integer)
	 */
	public boolean isDone(final Integer pid) {

		ISOTaskMonitor monitor = PID_MONITOR_MAP.get(pid);

		return monitor.isDone();
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#isCanceled(java.lang.Integer)
	 */
	public boolean isCanceled(final Integer pid) {

		ISOTaskMonitor monitor = PID_MONITOR_MAP.get(pid);

		return monitor.isCancelled();
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#spatialJoinOperation(es.axios.udig.spatialoperations.internal.parameters.ISpatialJoinGeomParameters)
	 */
	public Integer spatialJoinOperation(final ISpatialJoinGeomParameters params) {

		final ISOTaskMonitor monitor = SpatialJoinMonitor.newInstance(params);

		DialogUtil.runInProgressDialog(Messages.SOTaskManager_spatial_join_operation, true, monitor, true, true);

		Integer pid = SEQUENCER.next();
		PID_MONITOR_MAP.put(pid, monitor);

		return pid;
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#bufferOperation(es.axios.udig.spatialoperations.internal.parameters.IBufferParameters)
	 */
	public Integer bufferOperation(final IBufferParameters params) {

		final ISOTaskMonitor monitor = BufferMonitor.newInstance(params);

		DialogUtil.runInProgressDialog(Messages.SOTaskManager_buffer_operation, true, monitor, true, true);

		Integer pid = SEQUENCER.next();
		PID_MONITOR_MAP.put(pid, monitor);

		return pid;
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#clipOperation(es.axios.udig.spatialoperations.internal.parameters.IClipParameters)
	 */
	public Integer clipOperation(final IClipParameters params) {

		final ISOTaskMonitor monitor = ClipMonitor.newInstance(params);

		DialogUtil.runInProgressDialog(Messages.SOTaskManager_clip_operation, true, monitor, true, true);

		Integer pid = SEQUENCER.next();
		PID_MONITOR_MAP.put(pid, monitor);

		return pid;
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#intersectOperation(es.axios.udig.spatialoperations.internal.parameters.IIntersectParameters)
	 */
	public Integer intersectOperation(final IIntersectParameters params) {

		final ISOTaskMonitor monitor = IntersectMonitor.newInstance(params);

		DialogUtil.runInProgressDialog(Messages.SOTaskManager_intersect_operation, true, monitor, true, true);

		Integer pid = SEQUENCER.next();
		PID_MONITOR_MAP.put(pid, monitor);

		return pid;
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#polygonToLineOperation(es.axios.udig.spatialoperations.internal.parameters.IPolygonToLineParameters)
	 */
	public Integer polygonToLineOperation(final IPolygonToLineParameters params) {

		final ISOTaskMonitor monitor = PolygonToLineMonitor.newInstance(params);

		DialogUtil.runInProgressDialog(Messages.SOTaskManager_polygonToLine_operation, true, monitor, true, true);

		Integer pid = SEQUENCER.next();
		PID_MONITOR_MAP.put(pid, monitor);

		return pid;
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#splitOperation(es.axios.udig.spatialoperations.internal.parameters.ISplitParameters)
	 */
	public Integer splitOperation(final ISplitParameters params) {

		final ISOTaskMonitor monitor = SplitMonitor.newInstance(params);

		DialogUtil.runInProgressDialog(Messages.SOTaskManager_split_operation, true, monitor, true, true);

		Integer pid = SEQUENCER.next();
		PID_MONITOR_MAP.put(pid, monitor);

		return pid;
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#fillOperation(es.axios.udig.spatialoperations.internal.parameters.IFillParameters)
	 */
	public Integer fillOperation(final IFillParameters params) {

		final ISOTaskMonitor monitor = FillMonitor.newInstance(params);

		DialogUtil.runInProgressDialog(Messages.SOTaskManager_fill_operation, true, monitor, true, true);

		Integer pid = SEQUENCER.next();
		PID_MONITOR_MAP.put(pid, monitor);

		return pid;
	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager#holeOperation(es.axios.udig.spatialoperations.internal.parameters.IHoleParameters)
	 */
	public Integer holeOperation(final IHoleParameters params) {

		final ISOTaskMonitor monitor = HoleMonitor.newInstance(params);

		DialogUtil.runInProgressDialog(Messages.SOTaskManager_hole_operation, true, monitor, true, true);

		Integer pid = SEQUENCER.next();
		PID_MONITOR_MAP.put(pid, monitor);

		return pid;
	}


	
	public ISODescriptor getSpatialOperationDescriptor(final String operationID) {
		// TODO Auto-generated method stub
		
		return null;
		
	}

}
