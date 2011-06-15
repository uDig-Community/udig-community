package es.axios.so.extension.copy;

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
import java.io.IOException;

import net.refractions.udig.project.ILayer;

import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.spatialoperations.ui.common.ISOTargetLayerValidator;
import es.axios.udig.spatialoperations.ui.common.ISOValidator;
import es.axios.udig.spatialoperations.ui.common.LayerValidator;
import es.axios.udig.spatialoperations.ui.common.TargetLayerValidator;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public class CopyCommand extends SpatialOperationCommand {

	private static final InfoMessage	INITIAL_MESSAGE	= new InfoMessage("copy features",
																	InfoMessage.Type.IMPORTANT_INFO);

	// validating collaborations
	private final ISOTargetLayerValidator	targetValidator;
	private final ISOValidator		layerValidator;

	// data
	private ILayer						sourceLayer		= null;
	private CoordinateReferenceSystem	mapCrs			= null;
	private Filter						filter			= null;
	private Boolean						explode			= null;

	public CopyCommand() {

		super(INITIAL_MESSAGE);

		reset();

		this.targetValidator = new TargetLayerValidator();
		this.layerValidator = new LayerValidator();
	}

	public String getOperationID() {

		return "copy";
	}

	/**
	 * Sets the input parameters.
	 * 
	 * @param sourceLayer
	 * @param filter
	 * @param mapCrs
	 */
	public void setInputParams(final ILayer sourceLayer, final Filter filter, final CoordinateReferenceSystem mapCrs) {

		this.sourceLayer = sourceLayer;
		this.filter = filter;
		this.mapCrs = mapCrs;

		setChanged();
		notifyObservers(PARAMS_TARGET_GEOMETRY_CLASS);
	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param targetLayer
	 */
	public void setOutputParams(final ILayer targetLayer) {

		// sets target layer
		this.targetLayer = targetLayer;
		this.targetLayerName = null;
		this.targetGeometryClass = null;

		setChanged();
		notifyObservers(PARAMS_TARGET_SELECTED_LAYER);
	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param layerName
	 * @param targetGeomClass
	 */
	public void setOutputParams(final String layerName, final Class<? extends Geometry> targetGeomClass) {

		// sets target layer
		this.targetLayerName = layerName;
		this.targetLayer = null;
		this.targetGeometryClass = targetGeomClass;

		setChanged();
		notifyObservers(PARAMS_TARGET_NEW_LAYER);
	}

	/**
	 * checks the dissolve parameters and set the flag to indeed that this
	 * command can not be executed
	 */
	@Override
	public boolean validateParameters() {

		boolean canExecute = false;

		if (!checkSource(this.sourceLayer)) {
			return false;
		}

		if (!checkCRS(this.mapCrs)) {
			return false;
		}
		if (!checkTarget()) {
			return false;
		}

		return true;

	}

	/**
	 * @return true if the target has a correct value, false in other case
	 */
	private boolean checkTarget() {

		try {

			this.message = INITIAL_MESSAGE;

			this.targetValidator.setSourceLayer(this.sourceLayer);
			if (this.targetLayer != null) {
				this.targetValidator.setTargetLayer(this.targetLayer);
			} else {
				this.targetValidator.setTargetLayerName(this.targetLayerName, this.targetGeometryClass);
			}
			if (!this.targetValidator.validate()) {
				this.message = this.targetValidator.getMessage();
				return false;
			}
			if (!this.targetValidator.validGeometryCompatible()) {
				this.message = this.targetValidator.getMessage();
				return false;
			}

		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			//
			// if (!this.layerValidator.validatePolygon()) {
			//
			// this.message = this.layerValidator.getMessage();
			//
			// return false;
			// }

		}
		catch (Exception e) {

			this.message = new InfoMessage("fail", InfoMessage.Type.FAIL);
		}
		if (!checkFilter(this.filter)) {
			return false;
		}

		return true;
	}

	@Override
	public void executeOperation() throws SOCommandException {

		// NOTE
		CopyTask task = new CopyTask();

		try {
			// set the parameters, and convert to geotools class objects. The
			// same task the monitor do.
			if (this.targetLayer != null) {

				task.setParameters(this.filter, this.mapCrs, this.sourceLayer, this.targetLayer);
			} else {

				task.setParameters(this.filter, this.mapCrs, this.sourceLayer, this.targetLayerName,
							this.targetGeometryClass);

			}
			task.convertToGeotools();

			task.execute();

			task.refreshTargetLayer();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (SchemaException e) {

			e.printStackTrace();
		}
	}

	@Override
	public void initParameters() {

		sourceLayer = null;
		filter = null;
		mapCrs = null;

		targetLayer = null;
		targetLayerName = null;
		targetGeometryClass = null;

		explode = null;
	}

	/**
	 * Get the compatible geometry types for this spatial operation.
	 * 
	 * @return The compatible geometries are:
	 * 
	 * <pre>
	 * All
	 * 
	 * </pre>
	 */
	protected Object[] getSourceGeomertyClass() {

		Object[] obj = new Object[7];

		obj[0] = Polygon.class;
		obj[1] = MultiPolygon.class;
		obj[2] = Geometry.class;
		obj[3] = LineString.class;
		obj[4] = MultiLineString.class;
		obj[5] = Point.class;
		obj[6] = MultiPoint.class;

		return obj;
	}

	/**
	 * Get the result layer geometry. If the source layer isn't unknown,
	 * automatically return the correct geometry type.
	 * 
	 * @return The PolygonToLine operation valid geometries are:
	 *         <p>
	 *         All
	 *         </p>
	 */
	protected Object[] getResultLayerGeometry() {

		Object[] obj = new Object[7];

		obj[0] = Polygon.class;
		obj[1] = MultiPolygon.class;
		obj[2] = Geometry.class;
		obj[3] = LineString.class;
		obj[4] = MultiLineString.class;
		obj[5] = Point.class;
		obj[6] = MultiPoint.class;

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
	 * Until all default values are not set, won't be running.
	 */
//FIXME	@Override
//	public void setReady() {
//
//		if (sourceLayer != null && isResultSelected()) {
//			setStatus( CmdStatus.READY);
//		}
//	}

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

		// default
		return "Copy";
	}

	public String getToolTipText() {

		return "Copy features";
	}

}
