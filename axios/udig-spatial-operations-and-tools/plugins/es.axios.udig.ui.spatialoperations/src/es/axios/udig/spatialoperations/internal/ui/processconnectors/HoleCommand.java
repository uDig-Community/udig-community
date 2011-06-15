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

import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.geotools.util.FeatureUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IHoleParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager;
import es.axios.udig.spatialoperations.ui.taskmanager.SOTaskManager;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.message.InfoMessage.Type;
import es.axios.udig.ui.commons.util.LayerUtil;

/**
 * 
 * Command executing hole operation
 * <p>
 * Validates data input and executes the hole operation. The Client module must
 * set the input data and evaluate precondition before execute the command.
 * </p>
 * 
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
public final class HoleCommand extends SpatialOperationCommand {

	private static final Logger				LOGGER			= Logger.getLogger(HoleCommand.class.getName());
	private static final InfoMessage		INITIAL_MESSAGE	= new InfoMessage(Messages.HoleCommand_description,
																		InfoMessage.Type.IMPORTANT_INFO);

	// Collaborators
	private GeometryCompatibilityValidator	geomValidator	= new GeometryCompatibilityValidator();

	// inputs values
	private ILayer							usingLayer		= null;
	private CoordinateReferenceSystem		usingCRS		= null;
	private ILayer							sourceLayer		= null;
	private CoordinateReferenceSystem		sourceCRS		= null;
	private SimpleFeatureType 				targetFeatureType = null;

	private Filter							usingFilter		= null;
	private Filter							sourceFilter	= null;

	/**
	 * New instance of ClipCommand
	 * 
	 */
	public HoleCommand() {
		super(INITIAL_MESSAGE);
	}

	public String getOperationID() {

		return "hole"; //$NON-NLS-1$
	}

	/**
	 * Evaluates inputs data, if they have errors message error will be set.
	 * 
	 * @return true if inputs data are ok
	 */
	@Override
	protected boolean validateParameters() {

		this.message = InfoMessage.NULL;

		// clipping and clipped layer must be different
		if (this.sourceLayer == null) {
			this.message = new InfoMessage(Messages.HoleCommand_select_hole_layer, InfoMessage.Type.INFORMATION);
			return false;
		} else if (this.usingLayer == null) {
			this.message = new InfoMessage(Messages.HoleCommand_select_using_layer, InfoMessage.Type.INFORMATION);

			return false;

		} else if (this.sourceLayer.equals(this.usingLayer)) {
			this.message = new InfoMessage(Messages.HoleCommand_source_using_different, InfoMessage.Type.ERROR);

			return false;
		} else if (!validTarget()) {

			return false;

		} else if (!checkLayersCompatibility()) {

			return false;

		} else if (!checkFilter(this.usingFilter)) {

			return false;

		} else if (!checkFilter(this.sourceFilter)) {

			return false;
		}
		this.message = new InfoMessage(Messages.HoleCommand_params_ok, InfoMessage.Type.INFORMATION);

		return true;
	}

	/**
	 * Check that the first layer is Polygon or MultiPolygon and check that the
	 * second layer is LineString or MultiLineString.
	 * 
	 * @return
	 */
	private boolean checkLayersCompatibility() {

		SimpleFeatureType sourceType = sourceLayer.getSchema();
		SimpleFeatureType usingType = usingLayer.getSchema();

		GeometryDescriptor sourceGeomAttr = sourceType.getGeometryDescriptor();
		Class<? extends Geometry> sourceGeomClass = (Class<? extends Geometry>) sourceGeomAttr.getType().getBinding();

		GeometryDescriptor usingGeomAttr = usingType.getGeometryDescriptor();
		Class<? extends Geometry> usingGeomClass = (Class<? extends Geometry>) usingGeomAttr.getType().getBinding();

		if (!sourceGeomClass.equals(MultiPolygon.class) && !sourceGeomClass.equals(Polygon.class)
					&& !usingGeomClass.equals(MultiLineString.class) && !usingGeomClass.equals(LineString.class)) {

			this.message = new InfoMessage(Messages.HoleCommand_source_polygon, Type.ERROR);
			return false;
		}
		if (!usingGeomClass.equals(MultiLineString.class) && !usingGeomClass.equals(LineString.class)) {

			this.message = new InfoMessage(Messages.HoleCommand_using_line, Type.ERROR);
			return false;
		}
		return true;
	}

	/**
	 * Valid the target layer parameter
	 * 
	 * @return
	 */
	private boolean validTarget() {

		this.message = InfoMessage.NULL;

		if ((getTargetLayerName() == null) && (getTargetLayer() == null)) {

			this.message = new InfoMessage(Messages.HoleCommand_select_targer_layer, InfoMessage.Type.INFORMATION);
			return false;

		} else if ((getTargetLayer() != null) && (this.usingLayer != null)) {

			if (getTargetLayer().equals(this.usingLayer)) {

				this.message = new InfoMessage(Messages.HoleCommand_using_target_not_equal, InfoMessage.Type.ERROR);
				return false;
			}

		} else if (getTargetLayerName() != null) {

			try {
				if (getTargetLayerGeometryClass() == null) {
					return false;
				}
				SimpleFeatureType type = FeatureUtil.createFeatureType(this.sourceLayer.getSchema(),
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
		// equal to the layer to clip or MulyGeimetry compatible or Geometry
		SimpleFeatureType featureType = this.sourceLayer.getSchema();
		GeometryDescriptor geomType = featureType.getGeometryDescriptor();
		Class<?> expectedGeometry = geomType.getType().getBinding();

		SimpleFeatureType targetType = (getTargetLayer() != null) ? getTargetLayer().getSchema()
					: this.getTargetFeatureType();
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
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param usingFilter
	 * @param sourceFilter
	 */
	public void setInputParams(	final ILayer usingLayer,
	                           	final CoordinateReferenceSystem usingCRS,
	                           	final ILayer sourceLayer,
	                           	final CoordinateReferenceSystem sourceCRS,
								final Filter usingFilter,
								final Filter sourceFilter) {

		this.usingLayer = usingLayer;
		this.usingCRS = usingCRS;
		this.sourceLayer = sourceLayer;
		this.sourceCRS = sourceCRS;

		this.usingFilter = usingFilter;
		this.sourceFilter = sourceFilter;
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

		IHoleParameters params = null;

		if (getTargetLayer() != null) {

			params = ParametersFactory.createHoleParameters(sourceLayer, sourceCRS, usingLayer, usingCRS, sourceFilter,
						usingFilter, getTargetLayer(), getTargetLayerCRS());
		} else {
			assert getTargetFeatureType() != null;

			params = ParametersFactory.createHoleParameters(sourceLayer, sourceCRS, usingLayer, usingCRS, sourceFilter,
						usingFilter, getTargetFeatureType());
		}

		ISOTaskManager taskManager = SOTaskManager.getInstance();
		taskManager.holeOperation(params);

	}

	@Override
	public void initParameters() {

		this.sourceCRS = null;
		this.usingLayer = null;
		this.usingCRS = null;

		this.usingFilter = null;
		this.sourceFilter = null;
	}

	/**
	 * Get the compatible geometry types for this spatial operation.
	 * 
	 * @return The compatible geometries are:
	 * 
	 *         <pre>
	 * 
	 * {@link Polygon}
	 * {@link MultiPolygon}
	 * 
	 * </pre>
	 */
	protected Object[] getSourceGeomertyClass() {

		Object[] obj  = {Polygon.class,
						 MultiPolygon.class};

		return obj;
	}
	
	/**
	 * @return The list of valid geometry for the reference layer
	 */
	@Override
	protected Object[] getReferenceGeomertyClass() {

		Object[] obj  = new Object[]{
					LineString.class,
				 	MultiLineString.class
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

		Object[] obj;
		if (sourceLayer != null) {

			obj = new Object[2];
			Class<? extends Geometry> sourceGeom = LayerUtil.getGeometryClass(this.sourceLayer);

			// is a geometry collection
			if (sourceGeom.getSuperclass().equals(GeometryCollection.class)) {

				obj[0] = MultiPolygon.class;
			} else {

				obj[0] = Polygon.class;
			}
			obj[1] = Geometry.class;
		} else {

			obj = new Object[]{Polygon.class, MultiPolygon.class, Geometry.class};
		}

		return obj;
	}

	/**
	 * 
	 * @return The clip layer.
	 */
	public ILayer getSourceLayer() {

		return this.sourceLayer;
	}

	/**
	 * Set the default values for the clip layer.
	 * 
	 * @param clipLayer
	 */
	public void setDefaultValues(ILayer clipLayer) {

		this.sourceLayer = clipLayer;
	}

	/**
	 * 
	 * @return The using layer.
	 */
	public ILayer getReferenceLayer() {

		return this.usingLayer;
	}

	public String getOperationName() {

		return Messages.HoleSash_operation_name;
	}

	public String getToolTipText() {

		return Messages.HoleSash_tooltip_text;
	}
	

	private SimpleFeatureType getTargetFeatureType() {
		
		return this.targetFeatureType;
	}
	
	private void setTargetFeatureType(SimpleFeatureType type) {
		
		this.targetFeatureType = type;
	}
	
}
