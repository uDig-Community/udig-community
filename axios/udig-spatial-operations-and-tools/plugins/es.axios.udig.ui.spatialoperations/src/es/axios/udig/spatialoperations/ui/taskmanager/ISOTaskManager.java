package es.axios.udig.spatialoperations.ui.taskmanager;

import es.axios.udig.spatialoperations.descriptor.ISODescriptor;
import es.axios.udig.spatialoperations.internal.parameters.IBufferParameters;
import es.axios.udig.spatialoperations.internal.parameters.IClipParameters;
import es.axios.udig.spatialoperations.internal.parameters.IDissolveParameters;
import es.axios.udig.spatialoperations.internal.parameters.IFillParameters;
import es.axios.udig.spatialoperations.internal.parameters.IHoleParameters;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectParameters;
import es.axios.udig.spatialoperations.internal.parameters.IPolygonToLineParameters;
import es.axios.udig.spatialoperations.internal.parameters.ISpatialJoinGeomParameters;
import es.axios.udig.spatialoperations.internal.parameters.ISplitParameters;

public interface ISOTaskManager {

	/**
	 * Creates a monitor for dissolve task and execute the process.
	 * 
	 * @param params
	 */
	public  Integer dissolveOperation(final IDissolveParameters params);

	public  boolean isDone(final Integer pid);

	public  boolean isCanceled(final Integer pid);

	/**
	 * Creates a monitor for spatial joint task and execute the process.
	 * 
	 * @param params
	 */
	public  Integer spatialJoinOperation(
			final ISpatialJoinGeomParameters params);

	/**
	 * Create a monitor for buffer task and execute the process.
	 * 
	 * @param params
	 */
	public  Integer bufferOperation(final IBufferParameters params);

	/**
	 * Creates a monitor for clip task and execute the process.
	 * 
	 * @param params
	 */
	public  Integer clipOperation(final IClipParameters params);

	/**
	 * Creates a monitor for intersect task and execute the process.
	 * 
	 * @param params
	 */
	public  Integer intersectOperation(final IIntersectParameters params);

	/**
	 * Creates a monitor for polygon to line task and execute the process.
	 * 
	 * @param params
	 */
	public  Integer polygonToLineOperation(
			final IPolygonToLineParameters params);

	/**
	 * Creates a monitor for split task and execute the process.
	 * 
	 * @param params
	 * @return
	 */
	public  Integer splitOperation(final ISplitParameters params);

	/**
	 * Creates a monitor for fill task and execute the process.
	 * 
	 * @param params
	 * @return
	 */
	public  Integer fillOperation(final IFillParameters params);

	/**
	 * Creates a monitor for hole task and execute the process.
	 * 
	 * @param params
	 * @return
	 */
	public  Integer holeOperation(final IHoleParameters params);

	public ISODescriptor getSpatialOperationDescriptor(String operationID);

}