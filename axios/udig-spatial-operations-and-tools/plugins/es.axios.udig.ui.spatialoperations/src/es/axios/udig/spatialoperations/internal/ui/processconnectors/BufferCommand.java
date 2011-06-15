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

import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.measure.converter.ConversionException;
import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.swt.widgets.Button;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
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
import es.axios.geotools.util.GeoToolsUtils;
import es.axios.geotools.util.UnitList;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IBufferParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.tasks.IBufferTask.CapStyle;
import es.axios.udig.spatialoperations.ui.common.ProjectionValidator;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager;
import es.axios.udig.spatialoperations.ui.taskmanager.SOTaskManager;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.util.MapUtil;

/**
 * Buffer Command
 * <p>
 * Validates the inputs values for buffer operation and executes it.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
public final class BufferCommand extends SpatialOperationCommand {

	public static final String			paramsDistance		= "paramsDistance";								//$NON-NLS-1$
	public static final String			paramsUnits			= "paramsResultGeometry";							//$NON-NLS-1$

	private static final Logger			LOGGER				= Logger.getLogger(BufferCommand.class.getName());

	private static final InfoMessage	INITIAL_MESSAGE		= new InfoMessage(
																		Messages.BufferCommand_create_buffer_in_target,
																		InfoMessage.Type.IMPORTANT_INFO);
	private ProjectionValidator			projectionValidator	= new ProjectionValidator();

	// command's parameters
	private ILayer						sourceLayer;

	private Boolean						aggregate;
	private Double						distance;
	private Unit<?>						unit;
	private Integer						quadrantSegments;
	private SimpleFeatureType			targetFeatureType;

	private CoordinateReferenceSystem	sourceCRS;

	private Filter						filter				= null;
	private Button						radioUnitOption		= null;
	private CapStyle					capStyle			= CapStyle.capRound;								// default

	// cap

	public BufferCommand() {
		super(INITIAL_MESSAGE);
	}

	public String getOperationID() {
		return "Buffer"; //$NON-NLS-1$
	}

	/**
	 * Sets the input parameters.
	 * 
	 * @param sourceLayer
	 * @param filter
	 */
	public void setInputParams(final ILayer sourceLayer, final Filter filter) {

		this.sourceLayer = sourceLayer;
		if (this.sourceLayer != null) {
			this.sourceCRS = sourceLayer.getCRS();
		}
		this.filter = filter;
	}

	/**
	 * Sets output parameters.
	 * 
	 * @param targetLayer
	 */
	public void setOutputParams(final ILayer targetLayer) {

		setTargetLayer(targetLayer);
	}

	/**
	 * Sets output parameters.
	 * 
	 * @param newLayer
	 * @param targetCRS
	 */
	public void setOutputParams(final String newLayer,
								final CoordinateReferenceSystem targetCRS,
								final Class<? extends Geometry> targetClass) {

		setTargetLayerToCreate(newLayer, targetCRS, targetClass);
	}

	/**
	 * Set basic options in command.
	 * 
	 * @param aggregate
	 * @param distance
	 * @param unit
	 * @param quadrantSegments
	 */
	public void setBasicOptions(final Double distance, final Unit<?> unit) {

		this.distance = distance;
		this.unit = unit;

	}

	/**
	 * Set advanced options in the command
	 * 
	 * @param aggregate
	 * @param quadrantSegments
	 * @param cap
	 */
	public void setAdvancedOptions(final Boolean aggregate, final Integer quadrantSegments, CapStyle cap) {

		this.aggregate = aggregate;
		this.quadrantSegments = quadrantSegments;
		this.capStyle = cap;
	}

	/**
	 * Checks buffer's parameters
	 */
	@Override
	protected boolean validateParameters() {

		// source and target layer must exist and be different
		if (this.sourceLayer == null) {
			return false;
		}

		if ((getTargetLayer() == null) && ((getTargetLayerName() == null))) {
			this.message = new InfoMessage(Messages.BufferCommand_must_select_target, InfoMessage.Type.INFORMATION);
			return false;
		}

		if (!checkFilter(this.filter)) {
			return false;
		}

		if (!checkTargetFeatureType()) {
			return false;
		}

		if (this.distance == null) {

			this.message = new InfoMessage(Messages.BufferCommand_width_must_be_not_zero, InfoMessage.Type.ERROR);
			return false;
		}

		if (!checkLayerCRS(this.sourceLayer)) {
			return false;
		}

		if (!checkUnit(this.sourceLayer.getMap())) {
			return false;
		}
		this.message = INITIAL_MESSAGE;
		return true;
	}

	private boolean checkUnit(IMap map) {

		this.message = InfoMessage.NULL;

		if (this.unit == null) {
			this.message = new InfoMessage(Messages.BufferCommand_must_specify_the_units, InfoMessage.Type.INFORMATION);
			return false;
		} else {
			if (UnitList.PIXEL_UNITS.equals(this.unit)) {
				return true;
			} else {
				try {
					final CoordinateReferenceSystem mapCrs = MapUtil.getCRS(map);
					final Unit<?> sourceUnits = GeoToolsUtils.getDefaultCRSUnit(mapCrs);

					this.unit.getConverterTo(sourceUnits);
				} catch (ConversionException e) {
					// TODO i18n
					this.message = new InfoMessage(e.getMessage(), InfoMessage.Type.ERROR);
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkLayerCRS(ILayer sourceLayer) {

		this.message = InfoMessage.NULL;

		try {
			if (sourceLayer.getMap() == null) {
				return false;
			}

			CoordinateReferenceSystem sourceCrs = sourceLayer.getCRS();

			IMap map = this.sourceLayer.getMap();
			assert (map != null) : "illegal state, map can not be null value"; //$NON-NLS-1$

			CoordinateReferenceSystem mapCrs = MapUtil.getCRS(map);

			this.projectionValidator.setSourceCrs(sourceCrs);
			this.projectionValidator.setTargetCrs(mapCrs);

			if (!this.projectionValidator.validate()) {
				this.message = this.projectionValidator.getMessage();

				return false;
			}

		} catch (Exception e) {
			final String msg = MessageFormat.format(Messages.BufferCommand_crs_error, e.getMessage());

			LOGGER.info(msg);
			this.message = new InfoMessage(msg, InfoMessage.Type.FAIL);
			return false;
		}
		return true;
	}

	/**
	 * Checks and sets the target feature type If this check fails set the error
	 * message
	 * 
	 * @return true if the target is a feature type valid
	 */
	private boolean checkTargetFeatureType() {

		this.message = InfoMessage.NULL;

		SimpleFeatureType targetType = null;
		if ((isNewTargetLayer())) {

			try {
				if (super.getTargetLayerGeometryClass() == null || this.sourceLayer.getSchema() == null
							|| getTargetLayerCRS() == null) {
					return false;
				}
				targetType = FeatureUtil.createFeatureType(this.sourceLayer.getSchema(), getTargetLayerName(),
							super.getTargetLayerCRS(), getTargetLayerGeometryClass());
			} catch (SchemaException e) {
				this.message = new InfoMessage(Messages.BufferCommand_can_not_create_targetFeatureType,
							InfoMessage.Type.ERROR);
				return false;
			}
		} else if (getTargetLayer() != null) {

			if (this.sourceLayer.equals(getTargetLayer())) {

				this.message = new InfoMessage(Messages.BufferCommand_source_and_target_must_be_differents,
							InfoMessage.Type.ERROR);
				return false;
			}

			targetType = getTargetLayer().getSchema();
		}
		if (targetType != null) {
			if (!checkTargetCompatibility(targetType)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if the geometry of target is Polygon, MultiPolygon or
	 *         Geometry
	 */
	private boolean checkTargetCompatibility(final SimpleFeatureType targetType) {

		assert targetType != null;

		this.message = InfoMessage.NULL;

		GeometryDescriptor geomAttr = targetType.getGeometryDescriptor();
		Class<?> targetGeometry = geomAttr.getType().getBinding();

		if (!(Polygon.class.equals(targetGeometry) || MultiPolygon.class.equals(targetGeometry) || Geometry.class
					.equals(targetGeometry))) {

			String text = MessageFormat.format(Messages.BufferCommand_geometry_type_error,
						Polygon.class.getSimpleName(), MultiPolygon.class.getSimpleName(),
						Geometry.class.getSimpleName());
			this.message = new InfoMessage(text, InfoMessage.Type.ERROR);

			return false;
		}
		setTargetFeatureType(targetType);

		return true;
	}

	/**
	 * Executes the buffer operation.
	 * 
	 * @throws SOCommandException
	 */
	@Override
	public void executeOperation() throws SOCommandException {

		IBufferParameters params = null;
		if (getTargetLayer() != null) {
			// use the existent layer

			params = ParametersFactory.createBufferParameters(this.sourceLayer, this.sourceCRS, getTargetLayer(),
						getTargetLayerCRS(), this.aggregate, this.distance, this.quadrantSegments, this.unit,
						this.filter, this.capStyle);

		} else {// create new layer

			params = ParametersFactory.createBufferParameters(this.sourceLayer, this.sourceCRS,
						this.getTargetFeatureType(), this.aggregate, this.distance, this.quadrantSegments, this.unit,
						this.filter, this.capStyle);
		}
		ISOTaskManager taskManager = SOTaskManager.getInstance();
		taskManager.bufferOperation(params);
	}

	@Override
	public void initParameters() {

		// this.sourceLayer = null;

		initTargetLayer();

		// this.aggregate = null;
		// this.distance = null;
		this.unit = null;
		// this.quadrantSegments = null;
		this.filter = null;
		this.capStyle = CapStyle.capRound;
	}

	/**
	 * @return the aggregate
	 */
	public Boolean getAggregate() {
		return aggregate;
	}

	/**
	 * @return the width. Purpose: If its negative, change the Demo.
	 */
	public Double getWidth() {
		return distance;
	}

	/**
	 * @return the quadrantSegments
	 */
	public Integer getQuadrantSegments() {
		return quadrantSegments;
	}

	/**
	 * @return The cap style.
	 */
	public CapStyle getCapStyle() {
		return capStyle;
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

	/**
	 * Get the result layer geometry.
	 * 
	 * @return The buffers operation valid target geometries are:
	 *         <p>
	 *         {@link Polygon}, {@link MultiPolygon}, {@link Geometry}
	 *         </p>
	 */
	protected Object[] getValidTargetLayerGeometries() {

		Object[] obj = new Object[] { 
					Polygon.class, 
					MultiPolygon.class, 
					Geometry.class };

		return obj;

	}

	/**
	 * 
	 * @return The source layer.
	 */
	public ILayer getSourceLayer() {

		return this.sourceLayer;
	}

	/**
	 * 
	 * @return The map units.
	 */
	public Unit<?> getUnits() {

		return this.unit;
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
	 * Set the default values for the distance and the map units.
	 * 
	 * @param width
	 * @param unit
	 */
	public void setDefaultValues(Double width, Unit<?> unit) {

		this.distance = width;
		this.unit = unit;
		this.radioUnitOption = null;

	}

	/**
	 * Set the default values for the merge geometries and the segment per
	 * quadrant.
	 * 
	 * @param aggregate
	 * @param quadrantSegments
	 * @param cap
	 */
	public void setDefaultValues(Boolean aggregate, Integer quadrantSegments, CapStyle cap) {

		this.aggregate = aggregate;
		this.quadrantSegments = quadrantSegments;
		this.capStyle = cap;
	}

	/**
	 * Set the selected radio button of the current units.
	 * 
	 * @param defaultRadioUnitOption
	 */
	public void setRadioUnitOption(Button defaultRadioUnitOption) {

		this.radioUnitOption = defaultRadioUnitOption;
	}

	/**
	 * 
	 * @return The radio button.
	 */
	public Button getRadioUnitOption() {

		return this.radioUnitOption;
	}

	public String getOperationName() {
		return Messages.BufferComposite_operation_name;
	}

	public String getToolTipText() {
		return Messages.BufferCommand_create_buffer_in_target;
	}

	public void setCapStyle(CapStyle cap) {

		this.capStyle = cap;
	}

	private SimpleFeatureType getTargetFeatureType() {

		return this.targetFeatureType;
	}

	private void setTargetFeatureType(SimpleFeatureType type) {
		this.targetFeatureType = type;
	}

}
