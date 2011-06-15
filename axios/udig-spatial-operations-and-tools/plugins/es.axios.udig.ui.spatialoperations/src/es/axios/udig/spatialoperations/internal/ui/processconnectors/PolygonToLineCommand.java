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
package es.axios.udig.spatialoperations.internal.ui.processconnectors;

import java.util.logging.Logger;

import net.refractions.udig.project.ILayer;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IPolygonToLineParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.ui.common.LayerValidator;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager;
import es.axios.udig.spatialoperations.ui.taskmanager.SOTaskManager;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.util.LayerUtil;

/**
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
public final class PolygonToLineCommand extends SpatialOperationCommand {

	private static final Logger					LOGGER			= Logger
																			.getLogger(PolygonToLineCommand.class
																						.getName());
	private static final InfoMessage			INITIAL_MESSAGE	= new InfoMessage(
																			Messages.PolygonToLineCommand_initial_message,
																			InfoMessage.Type.IMPORTANT_INFO);

	// validating collaborations
	private final PolygonToLineTargetValidator	targetValidator	= new PolygonToLineTargetValidator();
	private final LayerValidator				layerValidator	= new LayerValidator();

	// data
	private ILayer								sourceLayer		= null;
	private CoordinateReferenceSystem			sourceCRS		= null;
	private CoordinateReferenceSystem			mapCrs			= null;
	private Filter								filter			= null;
	private Boolean								explode			= null;

	public PolygonToLineCommand() {

		super(INITIAL_MESSAGE);
	}

	public String getOperationID() {

		return "polygontoline"; //$NON-NLS-1$
	}

	/**
	 * Sets the input parameters.
	 * 
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param filter
	 * @param mapCrs
	 */
	public void setInputParams(	final ILayer sourceLayer,
								final CoordinateReferenceSystem sourceCRS,
								final Filter filter,
								final CoordinateReferenceSystem mapCrs) {

		this.sourceLayer = sourceLayer;
		this.sourceCRS = sourceCRS;
		this.filter = filter;
		this.mapCrs = mapCrs;
	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param targetLayer
	 */
	public void setOutputParams(final ILayer targetLayer) {

		setTargetLayer(targetLayer);
	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param layerName
	 * @param targetCRS
	 * @param targetGeomClass
	 */
	public void setOutputParams(final String layerName,
								final CoordinateReferenceSystem targetCRS,
								final Class<? extends Geometry> targetGeomClass) {

		setTargetLayerToCreate(layerName, targetCRS, targetGeomClass);
	}

	/**
	 * Set the advanced options.
	 * 
	 * @param explode
	 */
	public void setAdvancedOptions(Boolean explode) {

		this.explode = explode;
	}

	/**
	 * checks the dissolve parameters and set the flag to indeed that this
	 * command can not be executed
	 */
	@Override
	protected boolean validateParameters() {

		if (!checkSource(this.sourceLayer)) {
			return false;
		}
		if (!checkCRS(this.mapCrs)) {
			return false;
		}
		if (!checkTarget(this.targetValidator, this.sourceLayer)) {
			return false;
		}

		return true;
	}

	private boolean checkSource(ILayer sourceLayer) {

		try {
			this.message = INITIAL_MESSAGE;

			this.layerValidator.setParameters(sourceLayer);
			if (!this.layerValidator.validate()) {

				this.message = this.layerValidator.getMessage();

				return false;
			}

			if (!this.layerValidator.validatePolygon()) {

				this.message = this.layerValidator.getMessage();

				return false;
			}

		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			this.message = new InfoMessage(Messages.PolygonToLineCommand_internal_fail, InfoMessage.Type.FAIL);
		}
		if (!checkFilter(this.filter)) {
			return false;
		}

		return true;
	}

	@Override
	public void executeOperation() throws SOCommandException {

		IPolygonToLineParameters params = null;

		if (getTargetLayer() != null) {
			params = ParametersFactory.createPolygonToLineParameters(this.filter, this.mapCrs, this.sourceLayer,
						this.sourceCRS, getTargetLayer(), getTargetLayerCRS(), this.explode);

		} else {

			params = ParametersFactory.createPolygonToLineParameters(this.filter, this.mapCrs, this.sourceLayer,
						this.sourceCRS, getTargetLayerName(), getTargetLayerGeometryClass(), this.explode);
		}

		ISOTaskManager taskManager = SOTaskManager.getInstance();
		taskManager.polygonToLineOperation(params);

	}

	@Override
	public void initParameters() {

		sourceLayer = null;
		sourceCRS = null;
		filter = null;
		mapCrs = null;

		explode = null;
	}

	/**
	 * Get the compatible geometry types for this spatial operation.
	 * 
	 * @return The compatible geometries are:
	 * 
	 *         <pre>
	 * {@link Polygon}
	 * {@link MultiPolygon}
	 * 
	 * </pre>
	 */
	protected Object[] getSourceGeomertyClass() {

		Object[] obj = new Object[]{
					Polygon.class,
					MultiPolygon.class
		};

		return obj;
	}

	/**
	 * Get the result layer geometry. If the source layer isn't unknown,
	 * automatically return the correct geometry type.
	 * 
	 * @return The PolygonToLine operation valid geometries are:
	 *         <p>
	 *         {@link Geometry}
	 *         {@link MultiLineString} and 
	 *         {@link LineString}
	 *         </p>
	 */
	protected Object[] getValidTargetLayerGeometries() {

		Object[] obj;
		if (this.sourceLayer != null) {

			obj = new Object[2];
			Class<? extends Geometry> sourceGeom = LayerUtil.getGeometryClass(this.sourceLayer);

			// is a geometry collection
			if (sourceGeom.getSuperclass().equals(GeometryCollection.class)) {

				obj[0] = MultiLineString.class;
			} else {

				obj[0] = LineString.class;
			}
			obj[1] = Geometry.class;
			

		} else {

			obj = new Object[] {
						LineString.class,	
						MultiLineString.class,
						Geometry.class
					};
		}

		return obj;
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
	 * The explode option.
	 * 
	 * @return
	 */
	public Boolean getExplodeOption() {

		return this.explode;
	}

	/**
	 * Set the default values for explode the polygon.
	 * 
	 * @param polygonToLineExplode
	 */
	public void setDefaultValues(Boolean polygonToLineExplode) {

		this.explode = polygonToLineExplode;
	}

	public String getOperationName() {

		return Messages.PolygonToLineSash_operation_name;
	}

	public String getToolTipText() {

		return Messages.PolygonToLineSash_tooltip_text;
	}

}
