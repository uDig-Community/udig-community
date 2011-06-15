/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
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
package es.axios.udig.spatialoperations.internal.ui.processconnectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCState;
import org.geotools.jdbc.NullPrimaryKey;
import org.opengis.feature.simple.SimpleFeature;
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

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IDissolveParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.ui.common.ISOTargetLayerValidator;
import es.axios.udig.spatialoperations.ui.common.ISOValidator;
import es.axios.udig.spatialoperations.ui.common.LayerValidator;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager;
import es.axios.udig.spatialoperations.ui.taskmanager.SOTaskManager;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
public final class DissolveCommand extends SpatialOperationCommand {

	private static final Logger					LOGGER			= Logger.getLogger(DissolveCommand.class.getName());
	private static final InfoMessage			INITIAL_MESSAGE	= new InfoMessage(
																			Messages.DissolveCommand_initial_message,
																			InfoMessage.Type.IMPORTANT_INFO);
	// validating collaborations
	private final TargetLayerDissolveValidator	targetValidator	= new TargetLayerDissolveValidator();
	private final LayerValidator				layerValidator	= new LayerValidator();

	// data
	private ILayer								sourceLayer		= null;
	private CoordinateReferenceSystem			sourceCRS		= null;
	private List<String>						propDissolve	= null;
	private CoordinateReferenceSystem			mapCrs			= null;
	private Filter								filter			= null;
	private CoordinateReferenceSystem			targetCRS		= null;

	public DissolveCommand() {
		super(INITIAL_MESSAGE);
	}

	public String getOperationID() {
		return "dissolve"; //$NON-NLS-1$
	}

	/**
	 * Sets the input parameters.
	 * 
	 * @param sourceLayer
	 * @param filter
	 * @param list
	 * @param mapCrs
	 * @param targetLayer
	 */
	public void setInputParams(	final ILayer sourceLayer,
								final CoordinateReferenceSystem sourceCRS,
								final Filter filter,
								final List<String> list,
								final CoordinateReferenceSystem mapCrs) {

		this.sourceLayer = sourceLayer;
		this.sourceCRS = sourceCRS;
		this.filter = filter;
		this.propDissolve = list;
		this.mapCrs = mapCrs;
	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param layerName
	 * @param mapCRS
	 * @param targetGeomClass
	 */
	public void setOutputParams(final String layerName,
								final CoordinateReferenceSystem mapCRS,
								final Class<? extends Geometry> targetClass) {

		setTargetLayerToCreate(layerName, mapCRS, targetClass);
	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param targetLayerName
	 * @param targetCrs
	 */
	public void setOutputParams(final ILayer targetLayer) {

		setTargetLayer(targetLayer);

	}

	/**
	 * checks the dissolve parameters and set the flag to indeed that this
	 * command can not be executed
	 */
	@Override
	protected boolean validateParameters() {

		if (!checkLayer(this.sourceLayer, layerValidator)) {
			return false;
		}
		if (!checkDissolveProperty(this.propDissolve)) {
			return false;
		}
		if (!checkCRS(this.mapCrs)) {
			return false;
		}
		if (!checkTarget(targetValidator, sourceLayer)) {
			return false;
		}

		return true;
	}

	/**
	 * @return true if the target has a correct value, false in other case
	 */
	@Override
	protected boolean checkTarget(ISOTargetLayerValidator targetValidator, ILayer sourceLayer) {

		try {
			this.message = INITIAL_MESSAGE;

			targetValidator.setSourceLayer(sourceLayer);
			if (getTargetLayer() != null) {
				targetValidator.setTargetLayer(getTargetLayer());
			} else {
				targetValidator.setTargetLayerName(getTargetLayerName(), getTargetLayerGeometryClass());
			}
			if (!targetValidator.validate()) {
				this.message = targetValidator.getMessage();
				return false;
			}
			if (!targetValidator.validGeometryCompatible(sourceLayer)) {
				this.message = targetValidator.getMessage();
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean checkDissolveProperty(List<String> list) {

		this.message = INITIAL_MESSAGE;

		if (list == null || list.isEmpty()) {

			this.message = new InfoMessage(Messages.DissolveCommand_select_dissolve_property,
						InfoMessage.Type.INFORMATION);

			return false;
		}

		return true;
	}

	@Override
	protected boolean checkLayer(ILayer sourceLayer, ISOValidator layerValidator) {

		if (!super.checkLayer(sourceLayer, layerValidator)) {
			return false;
		}

		if (!checkFilter(this.filter)) {
			return false;
		}
		if (!checkDataStore(sourceLayer)) {
			return false;
		}

		return true;
	}

	/**
	 * Check if the dataStore is a PostGis dataStore, and on that case, check
	 * the table has a primary key.
	 * 
	 * @param layer
	 * @return False if the selected layer hasn't a primary key.
	 */
	private boolean checkDataStore(ILayer layer) {

		SimpleFeatureType featureType = layer.getSchema();
		IGeoResource resource = layer.getGeoResource();

		if (resource == null) {
			// new resource is required because new layer was selected
			final ICatalog catalog = AppGISMediator.getCatalog();
			assert catalog != null;

			resource = catalog.createTemporaryResource(featureType);
		}
		final FeatureSource<SimpleFeatureType, SimpleFeature> source;

		try {
			source = resource.resolve(FeatureSource.class, new NullProgressMonitor());
			DataStore store = (DataStore) source.getDataStore();

			if (!(store instanceof JDBCDataStore)) {
				return true;
			}
			JDBCDataStore jdbcStore = (JDBCDataStore) store;
			ContentEntry entry = jdbcStore.getEntry(featureType.getName());
			ContentState state = entry.getState(Transaction.AUTO_COMMIT);

			if (!(state instanceof JDBCState)) {
				return true;
			}
			JDBCState jdbcState = (JDBCState) state;

			if (jdbcState.getPrimaryKey() instanceof NullPrimaryKey) {
				this.message = new InfoMessage(Messages.DissolveCommand_postgis_nopk, InfoMessage.Type.FAIL);
				return false;
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
			this.message = new InfoMessage(Messages.DissolveCommand_internal_fail, InfoMessage.Type.FAIL);
		}

		return true;
	}

	/**
	 * Sends the message to execute the dissolve process to the process manager
	 */
	@Override
	public void executeOperation() throws SOCommandException {

		IDissolveParameters params = null;
		List<String> propertiesList = new ArrayList<String>();
		propertiesList.addAll(propDissolve);
		if (getTargetLayer() != null) {
			params = ParametersFactory.createDissolveParameters(this.sourceLayer, this.sourceCRS, this.filter,
						propertiesList, this.mapCrs, getTargetLayer(), this.targetCRS);

		} else {

			params = ParametersFactory.createDissolveParameters(this.sourceLayer, this.sourceCRS, this.filter,
						propertiesList, this.mapCrs, getTargetLayerName(), getTargetLayerGeometryClass());
		}

		ISOTaskManager taskManager = SOTaskManager.getInstance();
		taskManager.dissolveOperation(params);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seees.axios.udig.spatialoperations.internal.ui.processconnectors.
	 * SOAbstractCommand#initParameters()
	 */
	@Override
	public void initParameters() {

		sourceLayer = null;
		sourceCRS = null;
		filter = null;
		propDissolve = null;
		mapCrs = null;
		
	}

	/**
	 * Get the compatible geometry types for this spatial operation.
	 * 
	 * @return The compatible geometries are:
	 * 
	 *         <pre>
	 * {@link Point}
	 * {@link MultiPoint}
	 * {@link LineString}
	 * {@link MultiLineString}
	 * {@link Polygon}
	 * {@link MultiPolygon}
	 * {@link Geometry}
	 * 
	 * </pre>
	 */
	@Override
	protected Object[] getSourceGeomertyClass() {

		Object[] obj = new Object[] {
					Point.class,
					MultiPoint.class,
					LineString.class,
					MultiLineString.class,
					Polygon.class,
					MultiPolygon.class,
					Geometry.class,
		};

		return obj;
	}

	/**
	 * Get the result layer geometry.
	 * 
	 * @return The dissolve operation valid geometries are:
	 *         <p>
	 *         Same geometry class of the source layer and {@link Geometry}.
	 *         </p>
	 */
	@Override
	protected Object[] getValidTargetLayerGeometries() {

		Object[] validGeometrys = null;
		Class<?> geomClass = null;

		if (sourceLayer != null) {

			geomClass = sourceLayer.getSchema().getGeometryDescriptor().getType().getBinding();
			if ((geomClass.equals(Geometry.class))) {
				validGeometrys = new Object[1];
				validGeometrys[0] = geomClass;
			} else {

				validGeometrys = new Object[2];
				validGeometrys[0] = geomClass;
				validGeometrys[1] = Geometry.class;
			}
			return validGeometrys;
		}
		return new Object[0];
	}

	/**
	 * Set the default values for the source layer.
	 * 
	 * @param sourceLayer
	 */
	public void setDefaultValues(ILayer sourceLayer) {

		this.sourceLayer = sourceLayer;
	}

	/**
	 * 
	 * @return The source layer.
	 */
	public ILayer getSourceLayer() {

		return this.sourceLayer;
	}

	/**
	 * Reset the dissolve property.
	 */
	public void resetDissolveProperty() {

		this.propDissolve = null;
	}

	/**
	 * 
	 * @return The dissolve property.
	 */
	public List<String> getDissolveProperty() {

		return this.propDissolve;
	}

	public String getOperationName() {

		return Messages.DissolveComposite_operation_name;
	}

	public String getToolTipText() {

		return Messages.DissolveComposite_operation_tool_tip;
	}

}
