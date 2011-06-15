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

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.ILayer;

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

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IFillParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
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
public final class FillCommand extends SpatialOperationCommand {

	private static final InfoMessage		INITIAL_MESSAGE		= new InfoMessage(Messages.FillCommand_initial_message,
																			InfoMessage.Type.IMPORTANT_INFO);

	private ILayer							firstLayer			= null;
	private CoordinateReferenceSystem		firstLayerCRS		= null;
	private ILayer							secondLayer			= null;
	private CoordinateReferenceSystem		secondLayerCRS		= null;
	private Filter							filterInFirstLayer	= null;
	private Filter							filterInSecondLayer	= null;
	private boolean							copyFeatures;

	// collaborators
	private GeometryCompatibilityValidator	geomValidator		= new GeometryCompatibilityValidator();

	public FillCommand() {

		super(INITIAL_MESSAGE);
	}

	public String getOperationID() {

		return "fill"; //$NON-NLS-1$
	}

	/**
	 * Sets the input parameters.
	 * 
	 * @param currentFirstLayer
	 * @param currentFirstCRS
	 * @param currentSecondLayer
	 * @param currentSecondCRS
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 */
	public void setInputParams(	final ILayer currentFirstLayer,
								final CoordinateReferenceSystem currentFirstCRS,
								final ILayer currentSecondLayer,
								final CoordinateReferenceSystem currentSecondCRS,
								final Filter filterInFirstLayer,
								final Filter filterInSecondLayer) {

		this.firstLayer = currentFirstLayer;
		this.firstLayerCRS = currentFirstCRS;
		this.secondLayer = currentSecondLayer;
		this.secondLayerCRS = currentSecondCRS;

		this.filterInFirstLayer = filterInFirstLayer;
		this.filterInSecondLayer = filterInSecondLayer;

	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param targetLayer
	 */
	public void setOutputParams(final ILayer targetLayer, final boolean copyFeatures) {

		setTargetLayer(targetLayer);
		setCopyFeaturesOption(copyFeatures);

	}

	/**
	 * 
	 * Set the output parameters.
	 * 
	 * @param layerName
	 * @param targetCRS
	 * @param targetClass
	 */
	public void setOutputParams(final String layerName,
								final CoordinateReferenceSystem targetCRS,
								final Class<? extends Geometry> targetClass,
								final boolean copyFeatures) {

		setTargetLayerToCreate(layerName, targetCRS, targetClass);
		setCopyFeaturesOption(copyFeatures);

	}

	/**
	 * Evaluates the compatibility of layer's geometries and not null inputs.
	 * Additionally first, second and target layer must be different.
	 * 
	 * @return true if all parameters are ok
	 */
	@Override
	protected boolean validateParameters() {

		this.message = new InfoMessage(Messages.FillCommand_params_ok, InfoMessage.Type.INFORMATION);

		if (hasNullParameters(firstLayer, secondLayer)) {

			return false;

		} else if (!checkInterLayerPredicate()) {

			return false;

		} else if (!checkTargetCompatibility()) {

			return false;

		} else if (!checkLayersCompatibility(firstLayer, secondLayer)) {

			return false;

		} else if (!checkFilter(this.filterInFirstLayer)) {

			return false;

		} else if (!checkFilter(this.filterInSecondLayer)) {

			return false;
		}

		return true;
	}

	/**
	 * Check the target layer geometry. It must be Polygon or MultiPolygon.
	 * 
	 * @return True if its correct, false otherwise.
	 */
	private boolean checkTargetCompatibility() {

		this.message = InfoMessage.NULL;

		List<Class<? extends Geometry>> expectedGeometry = new ArrayList<Class<? extends Geometry>>();
		// The expected geometries are M.Polygon and Polygon.
		expectedGeometry.add(MultiPolygon.class);
		expectedGeometry.add(Polygon.class);

		Class<? extends Geometry> targetGeometry;
		if (getTargetLayer() != null) {
			SimpleFeatureType targetType = getTargetLayer().getSchema();
			GeometryDescriptor geomAttr = targetType.getGeometryDescriptor();
			targetGeometry = (Class<? extends Geometry>) geomAttr.getType().getBinding();
		} else {
			// its new layer
			targetGeometry = getTargetLayerGeometryClass();
		}

		// The expected geometries are 2, so we validate both.
		this.geomValidator.setExpected(expectedGeometry.get(0));
		this.geomValidator.setExpectedList(expectedGeometry);
		this.geomValidator.setTarget(targetGeometry);

		try {
			if (!this.geomValidator.validateBoth()) {

				this.message = this.geomValidator.getMessage();

				return false;
			}
		} catch (Exception e) {
			this.message = new InfoMessage(Messages.FillCommand_fail_validating, InfoMessage.Type.FAIL);
			return false;
		}

		return true;
	}

	/**
	 * The layers can not be equals
	 * 
	 * @return true if the layers are different
	 */
	private boolean checkInterLayerPredicate() {

		this.message = InfoMessage.NULL;

		List<ILayer> layerList = new ArrayList<ILayer>(3);

		layerList.add(this.firstLayer);
		if (layerList.contains(this.secondLayer)) {

			this.message = new InfoMessage(Messages.FillCommand_firt_second_different, InfoMessage.Type.ERROR);

			return false;
		}

		return true;
	}

	@Override
	public void executeOperation() throws SOCommandException {

		IFillParameters params = null;

		if (getTargetLayer() != null) {
			params = ParametersFactory.createFillParameters(firstLayer, firstLayerCRS, secondLayer, secondLayerCRS,
						filterInFirstLayer, filterInSecondLayer, getTargetLayer(), getTargetLayerCRS(), copyFeatures);
		} else {
			params = ParametersFactory.createFillParameters(firstLayer, firstLayerCRS, secondLayer, secondLayerCRS,
						filterInFirstLayer, filterInSecondLayer, getTargetLayerName(), getTargetLayerGeometryClass(),
						copyFeatures);
		}

		ISOTaskManager taskManager = SOTaskManager.getInstance();
		taskManager.fillOperation(params);

	}

	@Override
	public void initParameters() {

		firstLayer = null;
		firstLayerCRS = null;
		secondLayer = null;
		secondLayerCRS = null;

		this.filterInFirstLayer = null;
		this.filterInSecondLayer = null;
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
	 * 
	 * </pre>
	 */
	@Override
	protected Object[] getSourceGeomertyClass() {

		Object[] obj = { Polygon.class, MultiPolygon.class };

		return obj;
	}

	/**
	 * @return The list of valid geometry for the reference layer
	 */
	@Override
	protected Object[] getReferenceGeomertyClass() {
		Object[] obj = new Object[] { LineString.class, MultiLineString.class };

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

		Object[] obj;
		if (firstLayer != null) {

			obj = new Object[2];
			Class<? extends Geometry> sourceGeom = LayerUtil.getGeometryClass(this.firstLayer);

			// is a geometry collection
			if (sourceGeom.getSuperclass().equals(GeometryCollection.class)) {

				obj[0] = MultiPolygon.class;
			} else {

				obj[0] = Polygon.class;
			}
			obj[1] = Geometry.class;

		} else {

			obj = new Object[3];
			obj[0] = Polygon.class;
			obj[1] = MultiPolygon.class;
			obj[2] = Geometry.class;

		}

		return obj;
	}

	/**
	 * Set the default value for the first layer. Second layers default value
	 * will be null.
	 * 
	 * @param firstLayer
	 */
	public void setDefaultValues(ILayer firstLayer) {

		this.firstLayer = firstLayer;
	}

	private void setCopyFeaturesOption(boolean bool) {

		this.copyFeatures = bool;

	}

	/**
	 * 
	 * @return The first layer.
	 */
	public ILayer getFirstLayer() {

		return this.firstLayer;
	}

	/**
	 * 
	 * @return The second layer.
	 */
	public ILayer getSecondLayer() {

		return this.secondLayer;
	}

	public String getOperationName() {

		return Messages.FillSash_operation_name;
	}

	public String getToolTipText() {

		return Messages.FillSash_tool_tip_text;
	}

}
