/*
 * uDig Spatial Operations - Tutorial - http://www.axios.es (C) 2009,
 * Axios Engineering S.L. This product is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This product is distributed as part of tutorial, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.tutorial.ui.centroid;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureStore;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.geotools.util.FeatureUtil;
import es.axios.udig.spatialoperations.tutorial.process.centroid.CentroidTask;
import es.axios.udig.spatialoperations.ui.common.ProjectionValidator;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.SOFeatureStore;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.util.DialogUtil;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * 
 * Checks the centroid parameters and executes the {@link CentroidTask} that
 * creates the centroid features in the target layer.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public final class CentroidCommand extends SpatialOperationCommand  {

	private static final InfoMessage		DEFAULT_MESSAGE		= new InfoMessage("Calculate the polygon's centroid", //$NON-NLS-1$
																			InfoMessage.Type.IMPORTANT_INFO);

	private ProjectionValidator				projectionValidator	= new ProjectionValidator();

	// Parameters
	private ILayer							sourceLayer			= null;
	private IMap							map					= null;
	private Filter							sourceFilter		= null;

	public CentroidCommand() {
		super(DEFAULT_MESSAGE);
	}

	public String getOperationID() {
		return "Centroid"; //$NON-NLS-1$
	}

	public String getOperationName() {
		return "Centroid"; //$NON-NLS-1$
	}

	public String getToolTipText() {
		return "Makes the centroid of polygons' layer or selected polygons"; //$NON-NLS-1$
	}

	/**
	 * Checks all centroid parameters. It sets the error message if an invalid
	 * parameter is found.
	 * 
	 * @return true if all parameter are OK, false in other case.
	 */
	@Override
	protected boolean validateParameters() {

		this.message = DEFAULT_MESSAGE;

		if (!checkLayers())
			return false;

		if (!checkFilter(this.sourceFilter)) {
			return false;
		}

		if (!checkSourceCRS())
			return false;

		if (getTargetLayer() != null) {
			// if an existent layer is set its crs is checked.
			if (!checkTargetCRS())
				return false;
		}

		return true;
	}

	/**
	 * @return true if is possible project from source CRS to map CRS, false in
	 *         other case.
	 */
	private boolean checkSourceCRS() {

		assert sourceLayer != null;
		assert map != null;

		CoordinateReferenceSystem sourceCrs = sourceLayer.getCRS();
		if (sourceCrs == null) {
			this.message = new InfoMessage("The source layer doesn't have a CRS", InfoMessage.Type.ERROR); //$NON-NLS-1$
			return false;
		}

		CoordinateReferenceSystem mapCrs = MapUtil.getCRS(map);
		if (mapCrs == null) {

			this.message = new InfoMessage("The Map doesn't have a CRS", InfoMessage.Type.ERROR); //$NON-NLS-1$
			return false;
		}
		if (!canProjectFromCrsToCrs(sourceCrs, mapCrs))
			return false;

		return true;
	}

	/**
	 * @return true if is possible project from map CRS to target CRS, false in
	 *         other case.
	 */
	private boolean checkTargetCRS() {

		assert getTargetLayer() != null;
		assert this.map != null;

		CoordinateReferenceSystem targetCrs = getTargetLayer().getCRS();
		if (targetCrs == null) {

			this.message = new InfoMessage("The target layer doesn't have the CRS", InfoMessage.Type.ERROR); //$NON-NLS-1$
			return false;

		}
		CoordinateReferenceSystem mapCrs = MapUtil.getCRS(map);
		if (mapCrs == null) {

			this.message = new InfoMessage("The map doesn't have the CRS", InfoMessage.Type.ERROR); //$NON-NLS-1$
			return false;
		}

		if (!canProjectFromCrsToCrs(targetCrs, mapCrs))
			return false;

		return true;
	}

	/**
	 * Checks if there are some null parameter and sets a human message.
	 * 
	 * @return false if found any null parameter, true in other case.
	 */
	private boolean checkLayers() {

		if (this.sourceLayer == null) {

			this.message = new InfoMessage("Must select the source layer", InfoMessage.Type.ERROR); //$NON-NLS-1$

			return false;
		}
		if ((getTargetLayer() == null) && (getTargetLayerName() == null) && (getTargetLayerGeometryClass() == null)) {

			this.message = new InfoMessage("Must specify the target layer for the centroid result", //$NON-NLS-1$
						InfoMessage.Type.ERROR);

			return false;
		}

		if (this.sourceLayer.equals(getTargetLayer())) {

			this.message = new InfoMessage("Source and Target layer should be differents", InfoMessage.Type.ERROR); //$NON-NLS-1$

			return false;
		}
		return true;
	}

	private boolean canProjectFromCrsToCrs(CoordinateReferenceSystem fromCrs, CoordinateReferenceSystem toCrs) {

		assert fromCrs != null;
		assert toCrs != null;

		this.message = InfoMessage.NULL;

		try {

			this.projectionValidator.setSourceCrs(fromCrs);
			this.projectionValidator.setTargetCrs(toCrs);

			if (!this.projectionValidator.validate()) {
				this.message = this.projectionValidator.getMessage();

				return false;
			}

		} catch (Exception e) {
			final String msg = MessageFormat.format("CRS error: ", e.getMessage()); //$NON-NLS-1$

			this.message = new InfoMessage(msg, InfoMessage.Type.FAIL);
			return false;
		}
		return true;
	}

	@Override
	public void executeOperation() throws SOCommandException {

		final NullProgressMonitor progress = new NullProgressMonitor();

		try {

			final SimpleFeatureStore source = getFeatureStore(sourceLayer);
			final SimpleFeatureStore target = getTargetStore();
			final CoordinateReferenceSystem sourceCRS = getSourceCRS();
			final CoordinateReferenceSystem mapCrs = getMapCRS();
			final CoordinateReferenceSystem targetCRS =  
					(getTargetLayer() != null) ?
								getTargetLayer().getCRS():
								getMapCRS();
			
			progress.setTaskName("Centroid Spatial Operation"); //$NON-NLS-1$
			String msg = MessageFormat.format("Doing the ceentoid of {0}", sourceLayer.getName()); //$NON-NLS-1$
			progress.beginTask(msg, IProgressMonitor.UNKNOWN);

			IRunnableWithProgress runner = new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					CentroidTask task = new CentroidTask(source, sourceCRS, sourceFilter, target, targetCRS, mapCrs);

					task.run();

				}
			};
			DialogUtil.runInProgressDialog("executing centroid task", true, runner, true, true); //$NON-NLS-1$

		} catch (Exception e) {

			throw new SOCommandException(e.getMessage());

		} finally{
			progress.done();
		}
		
	}

	private CoordinateReferenceSystem getMapCRS() {

		return MapUtil.getCRS(map);
	}

	private final CoordinateReferenceSystem getSourceCRS() {
		if (this.sourceLayer != null) {
			return this.sourceLayer.getCRS();
		} else {
			return null;
		}

	}
	/**
	 * 
	 * Analyzes what is the target specified, that is new layer o existent layer
	 * As lateral effect if the targetLayer is null this method create a new one
	 * 
	 * @return the target FeatureStore
	 * @throws SOCommandException
	 */
	private final SimpleFeatureStore getTargetStore() throws SOCommandException {

		SimpleFeatureStore targetStore = null;
		if (getTargetLayer() != null) {

			targetStore = this.getFeatureStore(getTargetLayer());

		} else { // create a new layer

			ILayer targetLayer = createNewLayer();
			IGeoResource targetGeoResource = targetLayer.getGeoResource();
			try {
				targetStore = (SimpleFeatureStore) targetGeoResource.resolve(FeatureStore.class, null);
			} catch (IOException e) {
				throw new SOCommandException(e.getMessage());
			}
		}
		Transaction transaction = ((net.refractions.udig.project.internal.Map) this.map).getEditManagerInternal()
					.getTransaction();

		SOFeatureStore soStore = new SOFeatureStore( targetStore, transaction);

		return soStore;
	}

	/**
	 * Creates a new layer
	 * 
	 * @return {@link ILayer}
	 * @throws SOCommandException
	 */
	private final ILayer createNewLayer() throws SOCommandException {

		SimpleFeatureStore targetStore;
		try {
			SimpleFeatureType type = FeatureUtil.createFeatureType(this.sourceLayer.getSchema(), getTargetLayerName(),
						getMapCRS(), getTargetLayerGeometryClass());

			IGeoResource targetGeoResource = AppGISMediator.createTempGeoResource(type);
			assert targetGeoResource != null;

			targetStore = (SimpleFeatureStore) targetGeoResource.resolve(FeatureStore.class, null);

			assert targetStore != null;

			ILayer newLayer = MapUtil.addLayerToMap((IMap) this.map, targetGeoResource);

			return newLayer;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SOCommandException(e);
		}
	}
	
	/**
	 * @param layer
	 * @return the feature store associated to the layer
	 * @throws SOCommandException
	 */
	private final SimpleFeatureStore getFeatureStore(final ILayer layer)
		throws SOCommandException {

		assert layer != null;

		IGeoResource resource = layer.getGeoResource();

		if (resource == null) {
			throw new SOCommandException("The layer does not have GeoResource"); //$NON-NLS-1$
		}
		try {
			SimpleFeatureStore store = (SimpleFeatureStore) resource.resolve(FeatureStore.class, new NullProgressMonitor());

			Transaction transaction = ((net.refractions.udig.project.internal.Map) this.map)
					.getEditManagerInternal().getTransaction();
			
			SOFeatureStore soStore = new SOFeatureStore(store, transaction);

			return soStore;

		} catch (IOException e) {
			throw new SOCommandException(e.getMessage());
		}

	}

	@Override
	protected final Object[] getValidTargetLayerGeometries() {
		Object[] geometryClasses = new Object[] { Point.class, MultiPoint.class, Geometry.class };

		return geometryClasses;
	}

	@Override
	protected final Object[] getSourceGeomertyClass() {
		Object[] geometryClasses = new Object[] {
				LineString.class,
				MultiLineString.class,
				Polygon.class,
				MultiPolygon.class };

		return geometryClasses;
	}

	@Override
	public final void initParameters() {

		setMap(null);
		setSourceLayer(null);
		setSourceFilter(null);
	}

	public final void setMap(final IMap map) {

		this.map = map;
	}

	/**
	 * 
	 * @param sourceLayer
	 *            the source layer to set
	 */
	public final void setSourceLayer(final ILayer sourceLayer) {

		this.sourceLayer = sourceLayer;
	}

	public final ILayer getSourceLayer() {

		return this.sourceLayer;
	}

	public final void setSourceFilter(final Filter filter) {

		this.sourceFilter = filter;

	}
}
