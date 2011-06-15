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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This class abstracts the common behavior of monitor for a task which produce
 * new features into a result (or target) layer.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
abstract class SOTaskMonitor<V> extends AbstractMonitor {

	private static final Logger	LOGGER	= Logger.getLogger(SOTaskMonitor.class.getName());

	/**
	 * This method does the monitoring of the associated task. It checks if the
	 * task was canceled or done and produce the target (or result layer).
	 */
	@Override
	public void monitoringTask(IProgressMonitor progress) throws InterruptedException {

		try {

			progress.beginTask(getBeginMessage(), IProgressMonitor.UNKNOWN);

			// Checks the finalization of task
			Future<V> future = getFuture();
			while (!future.isDone() && !future.isCancelled()) {

				// if the user has pressed the cancel button the task must be
				// canceled. In other case this monitor will go to sleep some
				// milliseconds
				// to the next check of task state.
				if (progress.isCanceled()) {
					future.cancel(true);
				} else {

					progress.worked(1);
					Thread.sleep(250);
				}
			}
			// final actions: makes the cancel message or presents the features
			// in the target layer
			String msg = null;
			if (future.isCancelled()) {

				msg = getCancelMessage();

			} else if (future.isDone()) {

				msg = getDoneMessage();

				deliveryResult(future, getTarget());
			} else {
				assert false : "illegal state: only cancel or done state is expected"; //$NON-NLS-1$
			}
			progress.subTask(msg);
			LOGGER.finest(msg);

		}
		catch (Exception e) {

			throw makeTaskInterrumptedException(progress, e);

		}
		finally {
			progress.done();
			//FIXME test if it refresh w/o this line.
			//getMap().getRenderManager().refresh(getTarget(), getMap().getBounds(new NullProgressMonitor()));
		}
	}

	@Override
	public boolean isCancelled() {

		Future<V> future = getFuture();

		if (future == null) {
			return false;
		}
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {

		Future<V> future = getFuture();

		if (future == null) {
			return false;
		}
		return future.isDone();
	}

	/**
	 * Prepares the result of this process
	 * 
	 * @param future
	 * @param targetLayer
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	protected abstract void deliveryResult(Future<V> future, ILayer target) throws InterruptedException;

	/**
	 * @return the begin message
	 */
	protected abstract String getBeginMessage();

	/**
	 * @return the done message
	 */
	protected abstract String getDoneMessage();

	/**
	 * @return the cancel message
	 */
	protected abstract String getCancelMessage();

	/**
	 * @return the target layer used to create the result of this operation
	 */
	protected abstract ILayer getTarget();

	/**
	 * @return the map where this operation will create the target layer
	 */
	protected abstract IMap getMap();

	/**
	 * @return {@link Future} to check the progress of the spatial operation
	 */
	protected abstract Future<V> getFuture();

}
