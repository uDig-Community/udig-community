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
package es.axios.udig.spatialoperations.ui.parameters;

import java.util.logging.Logger;

import net.refractions.udig.project.ILayer;

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
import es.axios.udig.spatialoperations.ui.common.ISOTargetLayerValidator;
import es.axios.udig.spatialoperations.ui.common.LayerValidator;
import es.axios.udig.spatialoperations.ui.common.TargetLayerValidator;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.3.0
 */
public abstract class SimpleCommand extends SpatialOperationCommand {

	protected static InfoMessage			INITIAL_MESSAGE;

	private static final Logger				LOGGER			= Logger.getLogger(SimpleCommand.class.getName());

	// validating collaborations
	private final ISOTargetLayerValidator	targetValidator	= new TargetLayerValidator();
	private final LayerValidator			layerValidator	= new LayerValidator();

	// data
	protected ILayer						sourceLayer		= null;
	protected CoordinateReferenceSystem		sourceCRS		= null;
	protected CoordinateReferenceSystem		mapCrs			= null;
	protected Filter						sourceFilter	= null;

	protected String						tabItemBasicText;
	protected String						tabItemAdvancedText;
	protected String						groupSourceText;
	protected String						labelSourceLayerText;
	protected String						labelSelectedText;
	protected String						groupTargetInputsText;
	protected String						operationID;
	protected String						operationName;
	protected String						toolTipText;
	protected String						targetLabelText;
	protected String						targetLabelToolTipText;

	public SimpleCommand(InfoMessage initialMessage) {
		super(initialMessage);
		setValues();
	}

	private void setValues() {

		setGroupSourceText();
		setGroupTargetInputsText();
		setLabelSourceLayerText();
		setOperationID();
		setOperationName();
		setTabItemAdvancedText();
		setTabItemBasicText();
		setTargetLabelText();
		setTargetLabelToolTipText();
		setToolTipText();
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
								final Filter sourceFilter,
								final CoordinateReferenceSystem mapCrs) {

		this.sourceLayer = sourceLayer;
		this.sourceCRS = sourceCRS;
		this.sourceFilter = sourceFilter;
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

	@Override
	protected boolean validateParameters() {

		if (!checkSource(this.sourceLayer)) {
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

	private boolean checkSource(ILayer sourceLayer) {

		try {
			this.message = INITIAL_MESSAGE;

			this.layerValidator.setParameters(sourceLayer);
			if (!this.layerValidator.validate()) {

				this.message = this.layerValidator.getMessage();

				return false;
			}
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
			this.message = new InfoMessage(Messages.PolygonToLineCommand_internal_fail, InfoMessage.Type.FAIL);
		}
		if (!checkFilter(this.sourceFilter)) {
			return false;
		}

		return true;
	}

	@Override
	protected Object[] getValidTargetLayerGeometries() {

		Object[] obj = new Object[] {
				Point.class,
				MultiPoint.class,
				LineString.class,
				MultiLineString.class,
				Polygon.class,
				MultiPolygon.class,
				Geometry.class };

		return obj;
	}

	@Override
	protected Object[] getSourceGeomertyClass() {

		Object[] obj = new Object[] {
				Point.class,
				MultiPoint.class,
				LineString.class,
				MultiLineString.class,
				Polygon.class,
				MultiPolygon.class,
				Geometry.class };

		return obj;
	}

	@Override
	public void initParameters() {

		sourceLayer = null;
		sourceCRS = null;
		sourceFilter = null;
		mapCrs = null;

		initTargetLayer();
	}

	public String getOperationID() {

		return this.operationID;
	}

	public abstract void setOperationID();

	public String getOperationName() {

		return this.operationName;
	}

	public abstract void setOperationName();

	public String getToolTipText() {

		return this.toolTipText;
	}

	public abstract void setToolTipText();

	/**
	 * 
	 * @return
	 */
	public String getTabItemBasicText() {

		return this.tabItemBasicText;
	}

	public abstract void setTabItemBasicText();

	/**
	 * 
	 * @return
	 */
	public String getTabItemAdvancedText() {

		return this.tabItemAdvancedText;
	}

	public abstract void setTabItemAdvancedText();

	/**
	 * 
	 * @return
	 */
	public String getGroupSourceText() {

		return this.groupSourceText;
	}

	public abstract void setGroupSourceText();

	/**
	 * 
	 * @return
	 */
	public String getLabelSourceLayerText() {

		return this.labelSourceLayerText;
	}

	public abstract void setLabelSourceLayerText();

	/**
	 * 
	 * @return
	 */
	public String getGroupTargetInputsText() {

		return this.groupTargetInputsText;
	}

	public abstract void setGroupTargetInputsText();

	/**
	 * 
	 * @return
	 */
	public String getTargetLabelText() {

		return this.targetLabelText;
	}

	public abstract void setTargetLabelText();

	/**
	 * 
	 * @return
	 */
	public String getTargetLabelToolTipText() {

		return this.targetLabelToolTipText;
	}

	public abstract void setTargetLabelToolTipText();

	
	public ILayer getSourceLayer(){
		
		return this.sourceLayer;
	}
}
