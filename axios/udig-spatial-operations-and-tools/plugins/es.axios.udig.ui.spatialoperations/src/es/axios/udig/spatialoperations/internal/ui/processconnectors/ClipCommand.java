/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
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

import net.refractions.udig.project.ILayer;

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
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IClipParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager;
import es.axios.udig.spatialoperations.ui.taskmanager.SOTaskManager;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Command executing clip operation
 * <p>
 * Validates data input and executes the clip operation The Client module must
 * set the input data and evaluate precondition before execute the command
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
public final class ClipCommand extends SpatialOperationCommand {

	private static final Logger				LOGGER				= Logger.getLogger(ClipCommand.class.getName());
	private static final InfoMessage		INITIAL_MESSAGE		= new InfoMessage(
																			Messages.ClipCommand_clip_description,
																			InfoMessage.Type.IMPORTANT_INFO);

	// Collaborators
	private GeometryCompatibilityValidator	geomValidator		= new GeometryCompatibilityValidator();

	// inputs values
	private ILayer							usingLayer			= null;
	private CoordinateReferenceSystem		usingCRS			= null;
	private ILayer							clipSourceLayer		= null;
	private CoordinateReferenceSystem		clipSourceCRS		= null;

	private Filter							usingFilter			= null;
	private Filter							clipSourceFilter	= null;
	private SimpleFeatureType 				targetFeatureType 	= null;


	/**
	 * New instance of ClipCommand
	 * 
	 */
	public ClipCommand() {
		super(INITIAL_MESSAGE);
	}

	public String getOperationID() {
		return "Clip"; //$NON-NLS-1$
	}

	/**
	 * Evaluates inputs data, if they have errors message error will be set.
	 * 
	 * @return true if inputs data are ok
	 */
	@Override
	protected boolean validateParameters() {

		this.message = InfoMessage.NULL;
		boolean ok = true;

		// clipping and clipped layer must be different
		if (this.clipSourceLayer == null) {
			this.message = new InfoMessage(Messages.ClipCommand_must_select_clipped_layer, InfoMessage.Type.INFORMATION);
			ok = false;
		} else if (this.usingLayer == null) {
			this.message = new InfoMessage(Messages.ClipCommand_must_select_clipping_layer,
						InfoMessage.Type.INFORMATION);

			ok = false;

		} else if (this.clipSourceLayer.equals(this.usingLayer)) {
			this.message = new InfoMessage(Messages.ClipCommand_clipping_and_clipped_must_be_differents,
						InfoMessage.Type.ERROR);

			ok = false;
		} else if (!validTarget()) {

			ok = false;

		} else if (!checkFilter(this.usingFilter)) {

			ok = false;

		} else if (!checkFilter(this.clipSourceFilter)) {

			ok = false;

		} else if ((this.clipSourceLayer != null) && (getTargetLayer() != null)) {
			if (this.clipSourceLayer.equals(getTargetLayer())) {

				final String msg = MessageFormat.format(Messages.ClipCommand_will_clip_existent_layer,
							this.clipSourceLayer.getName());
				this.message = new InfoMessage(msg, InfoMessage.Type.WARNING);
				ok = true; // this warning allow to execute the
				// operation
			}
		}
		if (ok) {

			// if there is a message set it will be maintained else
			// set de ok message as default.
			if (InfoMessage.NULL.equals(this.message)) {

				this.message = new InfoMessage(Messages.ClipCommand_parameters_ok, InfoMessage.Type.INFORMATION);
			}
		}
		return ok;
	}

	/**
	 * Valid the target layer parameter
	 * 
	 * @return
	 */
	private boolean validTarget() {

		this.message = InfoMessage.NULL;
		
		if ((getTargetLayerName() == null) && (getTargetLayer() == null)) {

			this.message = new InfoMessage(Messages.ClipCommand_must_select_result, InfoMessage.Type.INFORMATION);
			return false;

		}
		if ((getTargetLayer() != null) && (this.usingLayer != null)) {

			if (getTargetLayer().equals(this.usingLayer)) {

				this.message = new InfoMessage(Messages.ClipCommand_clipping_and_result_must_be_differents,
							InfoMessage.Type.ERROR);
				return false;
			}
		}
		if ((getTargetLayerName() != null)) {
			try {
				SimpleFeatureType type = FeatureUtil.createFeatureType(this.clipSourceLayer.getSchema(),
						getTargetLayerName(), getTargetLayerCRS(), getTargetLayerGeometryClass());
				setTargetFeatureType(type);

			} catch (SchemaException e) {

				LOGGER.severe(e.getMessage());
				this.message = new InfoMessage(Messages.BufferCommand_can_not_create_targetFeatureType,
							InfoMessage.Type.ERROR);
				return false;
			}
		}

		// The geometry dimension of target layer must be
		// equal to the layer to clip or MultyGeometry compatible or Geometry
		SimpleFeatureType featureType = this.clipSourceLayer.getSchema();
		GeometryDescriptor geomType = featureType.getGeometryDescriptor();
		Class<?> expectedGeometry = geomType.getType().getBinding();

		SimpleFeatureType targetType = (getTargetLayer() != null) ? 
												getTargetLayer().getSchema()
											: 	getTargetFeatureType();
		GeometryDescriptor geomAttr = targetType.getGeometryDescriptor();
		Class<?> targetGeometry = geomAttr.getType().getBinding();

		this.geomValidator.setExpected((Class<? extends Geometry>) expectedGeometry);
		this.geomValidator.setTarget((Class<? extends Geometry>) targetGeometry);

		try {
			if (!this.geomValidator.validate()) {

				this.message = this.geomValidator.getMessage();

				return false;
			}
		} catch (Exception e) {
			this.message = new InfoMessage(Messages.ClipCommand_failed_validating_geometry, InfoMessage.Type.FAIL);
			return false;
		}

		return true;
	}

	/**
	 * Set the parameters.
	 * 
	 * @param usingLayer
	 * @param usingCRS
	 * @param clipSourceLayer
	 * @param sourceCRS
	 * @param usingFilter
	 * @param clipSourceFilter
	 */
	public void setInputParams(	final ILayer usingLayer,
								final CoordinateReferenceSystem usingCRS,
								final ILayer clipSourceLayer,
								final CoordinateReferenceSystem sourceCRS,
								final Filter usingFilter,
								final Filter clipSourceFilter) {

		this.usingLayer = usingLayer;
		this.usingCRS = usingCRS;
		this.clipSourceLayer = clipSourceLayer;
		this.clipSourceCRS = sourceCRS;

		this.usingFilter = usingFilter;
		this.clipSourceFilter = clipSourceFilter;
	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param targetLayer
	 * @param targetCrs
	 */
	public void setOutputParams(final ILayer targetLayer) {

		setTargetLayer(targetLayer);
	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param targetLayerName
	 * @param targetCrs
	 */
	public void setOutputParams(final String targetLayerName,
								final CoordinateReferenceSystem targetCrs,
								final Class<? extends Geometry> targetClass) {

		setTargetLayerToCreate(targetLayerName, targetCrs, targetClass);
	}

	/**
	 * Executes the clip transaction
	 * 
	 * @throws SOCommandException
	 */
	@Override
	public void executeOperation() throws SOCommandException {

		IClipParameters params;
		if (getTargetLayer() != null) {
			params = ParametersFactory.createClipParameters(
						this.usingLayer, this.usingCRS, 
						this.clipSourceLayer, this.clipSourceCRS, 
						getTargetLayer(), getTargetLayerCRS(), 
						this.usingFilter, this.clipSourceFilter);
		} else {
			params = ParametersFactory.createClipParameters(
						this.usingLayer, usingCRS, 
						this.clipSourceLayer, this.clipSourceCRS, 
						getTargetFeatureType(), 
						this.usingFilter, this.clipSourceFilter);
		}

		ISOTaskManager taskManager = SOTaskManager.getInstance();
		taskManager.clipOperation(params);
	}

	@Override
	public void initParameters() {

		this.clipSourceLayer = null;
		this.clipSourceCRS = null;
		this.usingLayer = null;
		this.usingCRS = null;
		this.usingFilter = null;
		this.clipSourceFilter = null;

		super.initTargetLayer();
	}

	/**
	 * Get the compatible geometry types for this spatial operation.
	 * 
	 * @return The compatible geometries are:
	 * 
	 * <pre>
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
					Geometry.class,
		};

		return obj;
	}

	/**
	 * Get the result layer geometry.
	 * 
	 * @return The clips operation valid geometries are:
	 *         <p>
	 *         Same geometry class of the source layer and {@link Geometry}.
	 *         </p>
	 */
	protected Object[] getValidTargetLayerGeometries() {

		Object[] validGeometrys = null;
		Class<?> geomClass = null;

		if (clipSourceLayer != null) {

			geomClass = clipSourceLayer.getSchema().getGeometryDescriptor().getType().getBinding();
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
	 * 
	 * @return The clip layer.
	 */
	public ILayer getCurrentSourceClipLayer() {

		return this.clipSourceLayer;
	}
	/**
	 * 
	 * @return The using layer.
	 */
	public ILayer getCurrentUsingLayer() {

		return this.usingLayer;
	}

	public String getOperationName() {

		return Messages.ClipComposite_operation_name;
	}

	public String getToolTipText() {

		return Messages.ClipCommand_clip_description;
	}

	private SimpleFeatureType getTargetFeatureType() {
		
		return this.targetFeatureType;
	}
	private void setTargetFeatureType(SimpleFeatureType type) {
		this.targetFeatureType = type;
	}
	
}
