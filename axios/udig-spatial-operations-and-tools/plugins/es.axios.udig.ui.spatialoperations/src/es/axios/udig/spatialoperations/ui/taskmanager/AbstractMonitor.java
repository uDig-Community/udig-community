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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.command.MapCommand;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.lib.geometry.util.GeometryUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Implements common behavior for the operation monitors.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
abstract class AbstractMonitor implements ISOTaskMonitor {

	private static final Logger	LOGGER			= Logger.getLogger(AbstractMonitor.class.getName());

	private IProgressMonitor	progressMonitor	= null;

	/**
	 * Initializes and runs the spatial operation
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		commitChanges(monitor);
		initTask(monitor);
		monitoringTask(monitor);

	}

	public abstract boolean isDone();

	public abstract boolean isCancelled();

	/**
	 * Checks the status of this spatial operation and produces the result when
	 * the task will have finished.
	 * 
	 * @param progress
	 * 
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	protected abstract void monitoringTask(IProgressMonitor progress) throws InterruptedException;

	/**
	 * initializes the process
	 * 
	 * @throws InvocationTargetException
	 */
	protected void initTask(IProgressMonitor monitor) throws InvocationTargetException {

		this.progressMonitor = monitor;

		initTaskMonitor(monitor);
	}

	/**
	 * Initializes the specifics parameters for the spatial operation
	 * 
	 * @param progress
	 * @throws InvocationTargetException
	 */
	protected abstract void initTaskMonitor(IProgressMonitor progress) throws InvocationTargetException;

	/**
	 * The progress monitor is the user interface which allows inform the
	 * progress of this operation and capture a cancel event required by the
	 * user.
	 * 
	 * @param progress
	 *            The monitor to set.
	 */
	protected final void setMonitor(IProgressMonitor progress) {
		this.progressMonitor = progress;
	}

	/**
	 * @return Returns the monitor.
	 */
	protected final IProgressMonitor getMonitor() {
		if (this.progressMonitor == null) {
			this.progressMonitor = new NullProgressMonitor();
		}
		return this.progressMonitor;
	}

	/**
	 * Logs the exception message and presents a message to user
	 * 
	 * @param monitor
	 * @param e
	 */
	protected InvocationTargetException makeInitializeExcepion(IProgressMonitor monitor, Exception e) {

		final String msg = e.getMessage();
		LOGGER.severe(msg);
		monitor.subTask(Messages.AbstractMonitor_failed_initializating);
		return new InvocationTargetException(e);
	}

	/**
	 * Logs the exception that caused the spatial operation interruption and
	 * produce the exception.
	 * 
	 * @param progress
	 * @param e
	 * @return {@link InterruptedException}
	 */
	protected InterruptedException makeTaskInterrumptedException(IProgressMonitor progress, Exception e) {

		LOGGER.severe(e.getMessage());

		final String msg = Messages.AbstractMonitor_failed;

		progress.subTask(msg);

		InterruptedException ex = new InterruptedException();

		return ex;
	}

	/**
	 * Adds a new layer to map using the georesource of the feature store
	 * 
	 * @param map
	 * @param geoResource
	 * 
	 * @return the new layer
	 */
	protected ILayer addLayerToMap(final IMap map, final IGeoResource geoResource) {

		return MapUtil.addLayerToMap(map, geoResource);
	}

	/**
	 * Gets the store required to save the new features.
	 * 
	 * @param layer
	 * @return a feature store
	 * @throws IOException
	 *             if can get the store
	 */
	public static SimpleFeatureStore getFeatureStore(final ILayer layer) throws IOException {

		assert layer != null;

		SimpleFeatureType featureType = layer.getSchema();

		IGeoResource resource = layer.getGeoResource();
		SimpleFeatureStore store = getTargetFeatureStore(resource, featureType);

		return store;
	}

	/**
	 * Returns the target FeatureStore for the operation.
	 * <p>
	 * If it comes from an existing layer, it is returned. If it has to be
	 * created because the user have selected the "create new layer" option, a
	 * new temporary IGeoResource is created
	 * </p>
	 * 
	 * @param resource
	 * @param featureType
	 * @return the FeatureStore for the generated features, either if it comes
	 *         from an existing layer or a new one had to be created
	 * @throws IOException
	 */
	private static SimpleFeatureStore getTargetFeatureStore(IGeoResource resource,
																						SimpleFeatureType featureType)
		throws IOException {

		if (resource == null) {
			// new resource is required because new layer was selected
			final ICatalog catalog = AppGISMediator.getCatalog();
			assert catalog != null;

			resource = catalog.createTemporaryResource(featureType);
		}
		final SimpleFeatureStore targetStore;

		targetStore = resource.resolve(SimpleFeatureStore.class, new NullProgressMonitor());

		IMap map = ApplicationGIS.getActiveMap();
		Transaction transaction = ((net.refractions.udig.project.internal.Map) map).getEditManagerInternal()
					.getTransaction();

		SimpleFeatureStore store = new SOFeatureStore(targetStore, transaction);

		return store;
	}

	/**
	 * Returns the multi geometry required by the feature
	 * 
	 * @param geometry
	 *            must be simple geometry (point, polygon, linestring)
	 * @param feature
	 * @return adjusted geometry
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public static Geometry adjustGeometryAttribute(final Geometry geometry, final SimpleFeature feature) {

		Class<? extends Geometry> geomClass = (Class<? extends Geometry>) feature.getFeatureType()
					.getGeometryDescriptor().getType().getBinding();
		Geometry adjustedGeom = GeometryUtil.adapt(geometry, geomClass);

		return adjustedGeom;
	}

	/**
	 * Presents the result of process into target layer
	 */
	protected void presentFeaturesOnTargetLayer(final ILayer targetLayer) {

		assert targetLayer != null;

		targetLayer.refresh(null);

	}

	protected void editorMapCommit(final IMap map) {

		try {
			assert map instanceof Map;
			((Map) map).getEditManagerInternal().commitTransaction();

		} catch (IOException e) {

			LOGGER.severe(e.getMessage());
			throw (RuntimeException) new RuntimeException().initCause(e);
		}

	}

	protected void editorMapRollback(final IMap map) {

		try {
			assert map instanceof Map;
			((Map) map).getEditManagerInternal().rollbackTransaction();

		} catch (IOException e) {

			LOGGER.severe(e.getMessage());
			throw (RuntimeException) new RuntimeException().initCause(e);
		}

	}

	/**
	 * Commit the changes. We do this to assure that all data is OK and
	 * committed.
	 * 
	 * @param monitor
	 */
	private void commitChanges(IProgressMonitor monitor) {

		try {
			MapCommand command = getContext().getEditFactory().createCommitCommand();
			command.setMap(getContext().getMap());
			command.run(monitor);
		} catch (Exception e) {

			Display display = Display.getDefault();
			display.asyncExec(new Runnable() {
				public void run() {

					Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					String message = Messages.Commit_Error_message;
					MessageDialog.openError(parent, Messages.Commit_error_shell_title, message);
				}
			});
		}
	}

	/**
	 * Return the IToolContext
	 * 
	 * @return
	 */
	private IToolContext getContext() {
		// TODO maybe get IToolContext in a properly way?
		return ApplicationGIS.getToolManager().getActiveTool().getContext();
	}

}
