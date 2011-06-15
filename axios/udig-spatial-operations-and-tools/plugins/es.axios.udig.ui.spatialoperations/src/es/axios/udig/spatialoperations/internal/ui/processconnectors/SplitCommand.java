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
import org.opengis.feature.type.FeatureType;
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
import es.axios.udig.spatialoperations.internal.parameters.ISplitParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager;
import es.axios.udig.spatialoperations.ui.taskmanager.SOTaskManager;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.message.InfoMessage.Type;
import es.axios.udig.ui.commons.util.LayerUtil;

/**
 * <pre>
 * Command for split operation.
 * 
 * This class is responsible to check the parameters needed for split operation
 * and execute the split task.
 * </pre>
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.2.0
 */
public final class SplitCommand extends SpatialOperationCommand {

	private static final InfoMessage		INITIAL_MESSAGE		= new InfoMessage(
																			Messages.SplitComposite_operation_description,
																			InfoMessage.Type.IMPORTANT_INFO);
	private ILayer							firstLayer			= null;
	private CoordinateReferenceSystem		firstCRS			= null;
	private ILayer							secondLayer			= null;
	private CoordinateReferenceSystem		secondCRS			= null;
	private CoordinateReferenceSystem		targetCRS			= null;
	private Filter							filterInFirstLayer	= null;
	private Filter							filterInSecondLayer	= null;

	// collaborators
	private GeometryCompatibilityValidator	geomValidator		= new GeometryCompatibilityValidator();

	public SplitCommand() {

		super(INITIAL_MESSAGE);
	}

	public String getOperationID() {

		return "Split"; //$NON-NLS-1$
	}

	/**
	 * Sets the input parameters.
	 * 
	 * @param currentFirstLayer
	 * @param firstLayerCRS
	 * @param currentSecondLayer
	 * @param secondLayerCRS
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 */
	public void setInputParams(	final ILayer currentFirstLayer,
								final CoordinateReferenceSystem firstLayerCRS,
								final ILayer currentSecondLayer,
								final CoordinateReferenceSystem secondLayerCRS,
								final Filter filterInFirstLayer,
								final Filter filterInSecondLayer) {

		this.firstLayer = currentFirstLayer;
		this.firstCRS = firstLayerCRS;
		this.secondLayer = currentSecondLayer;
		this.secondCRS = secondLayerCRS;

		this.filterInFirstLayer = filterInFirstLayer;
		this.filterInSecondLayer = filterInSecondLayer;

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
	 * 
	 * Set the output parameters.
	 * 
	 * @param layerName
	 * @param targetCRS
	 * @param targetClass
	 */
	public void setOutputParams(final String layerName,
								final CoordinateReferenceSystem targetCRS,
								final Class<? extends Geometry> targetClass) {

		setTargetLayerToCreate(layerName, targetCRS, targetClass);

	}

	/**
	 * Evaluates the compatibility of layer's geometries and not null inputs.
	 * Additionally first, second and target layer must be different.
	 * 
	 * @return true if all parameters are ok
	 */
	@Override
	protected boolean validateParameters() {

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

		this.message = new InfoMessage(Messages.IntersectCommand_parameters_ok, InfoMessage.Type.INFORMATION);

		return true;
	}

	protected boolean checkLayersCompatibility(ILayer firstLayer, ILayer secondLayer) {

		FeatureType firstType = firstLayer.getSchema();
		FeatureType secondType = secondLayer.getSchema();

		GeometryDescriptor firstGeomAttr = firstType.getGeometryDescriptor();
		Class<? extends Geometry> firstGeomClass = (Class<? extends Geometry>) firstGeomAttr.getType().getBinding();

		GeometryDescriptor secondGeomAttr = secondType.getGeometryDescriptor();
		Class<? extends Geometry> secondGeomClass = (Class<? extends Geometry>) secondGeomAttr.getType().getBinding();

		if (!firstGeomClass.equals(MultiPolygon.class) && !firstGeomClass.equals(Polygon.class)
					&& !secondGeomClass.equals(MultiLineString.class) && !secondGeomClass.equals(LineString.class)) {

			this.message = new InfoMessage(Messages.SplitCommand_source_polygon_line, Type.ERROR);
			return false;
		}
		if (!secondGeomClass.equals(MultiLineString.class) && !secondGeomClass.equals(LineString.class)) {

			this.message = new InfoMessage(Messages.SpatialOperationCommand_reference_line, Type.ERROR);
			return false;
		}
		return true;
	}

	/**
	 * Check the target layer geometry. It must be Polygon, MultiPolygon,
	 * LineString or MultiLineString.
	 * 
	 * @return True if its correct, false otherwise.
	 */
	private boolean checkTargetCompatibility() {

		this.message = InfoMessage.NULL;

		List<Class<? extends Geometry>> expectedGeometry = new ArrayList<Class<? extends Geometry>>();
		// The expected geometries are M.Polygon and Polygon.
		expectedGeometry.add(MultiPolygon.class);
		expectedGeometry.add(Polygon.class);
		expectedGeometry.add(LineString.class);
		expectedGeometry.add(MultiLineString.class);

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
			this.message = new InfoMessage(Messages.SplitCommand_faild_validating_geometry_compatibility,
						InfoMessage.Type.FAIL);
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

			this.message = new InfoMessage(Messages.SplitCommand_first_and_second_must_be_differents,
						InfoMessage.Type.ERROR);

			return false;
		}

		return true;
	}

	@Override
	public void executeOperation() throws SOCommandException {

		ISplitParameters params = null;

		if (getTargetLayer() != null) {
			params = ParametersFactory.createSplitParameters(firstLayer, firstCRS, secondLayer, secondCRS,
						getTargetLayer(), targetCRS, filterInFirstLayer, filterInSecondLayer);
		} else {
			params = ParametersFactory.createSplitParameters(firstLayer, firstCRS, secondLayer, secondCRS,
						getTargetLayerName(), getTargetLayerGeometryClass(), filterInFirstLayer, filterInSecondLayer);
		}

		ISOTaskManager taskManager = SOTaskManager.getInstance();
		taskManager.splitOperation(params);

	}

	@Override
	public void initParameters() {

		firstLayer = null;
		secondLayer = null;

		this.filterInFirstLayer = null;
		this.filterInSecondLayer = null;

		initTargetLayer();
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
	protected Object[] getSourceGeomertyClass() {

		Object[] obj = new Object[] { Polygon.class, MultiPolygon.class, LineString.class, MultiLineString.class };

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
			if (sourceGeom.getSuperclass().equals(GeometryCollection.class)
						&& sourceGeom.equals(MultiPolygon.class)) {

				obj[0] = MultiPolygon.class;
			} else if (sourceGeom.getSuperclass().equals(GeometryCollection.class)
						&& sourceGeom.equals(MultiLineString.class)) {

				obj[0] = MultiLineString.class;
			} else if (sourceGeom.equals(Polygon.class)) {

				obj[0] = Polygon.class;
			} else {

				obj[0] = LineString.class;
			}
			obj[1] = Geometry.class;

		} else {

			obj = new Object[5];
			obj[0] = Polygon.class;
			obj[1] = MultiPolygon.class;
			obj[2] = LineString.class;
			obj[3] = MultiLineString.class;
			obj[4] = Geometry.class;
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
		this.secondLayer = null;
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

		return Messages.SplitComposite_operation_name;
	}

	public String getToolTipText() {

		return Messages.SplitComposite_operation_description;
	}

}
