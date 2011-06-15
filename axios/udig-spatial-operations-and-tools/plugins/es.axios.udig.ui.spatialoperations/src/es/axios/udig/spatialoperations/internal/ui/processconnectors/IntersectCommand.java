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

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.ILayer;

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

import es.axios.geotools.util.GeoToolsUtils;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager;
import es.axios.udig.spatialoperations.ui.taskmanager.SOTaskManager;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Intersect Command
 * <p>
 * Evaluates predicate and executes the associated intersection operation.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
public final class IntersectCommand extends SpatialOperationCommand {

	private static final InfoMessage		INITIAL_MESSAGE		= new InfoMessage(
																			Messages.IntersectCommand_description,
																			InfoMessage.Type.IMPORTANT_INFO);
	// collaborators
	private GeometryCompatibilityValidator	geomValidator		= new GeometryCompatibilityValidator();

	// inputs parameters
	private ILayer							firstLayer			= null;
	private CoordinateReferenceSystem		firstCRS			= null;

	private ILayer							secondLayer			= null;
	private CoordinateReferenceSystem		secondCRS			= null;

	private Filter							filterInFirsLayer	= null;
	private Filter							filterInSecondLayer	= null;

	public IntersectCommand() {
		super(INITIAL_MESSAGE);

	}

	public String getOperationID() {
		return "Intersect"; //$NON-NLS-1$
	}

	/**
	 * Set the input parameters.
	 * 
	 * @param firstLayer
	 * @param firstCRS
	 * @param secondLayer
	 * @param secondCRS
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 */
	public void setInputParams(	final ILayer firstLayer,
								final CoordinateReferenceSystem firstCRS,
								final ILayer secondLayer,
								final CoordinateReferenceSystem secondCRS,
								final Filter filterInFirstLayer,
								final Filter filterInSecondLayer) {

		this.firstLayer = firstLayer;
		this.firstCRS = firstCRS;

		this.secondLayer = secondLayer;
		this.secondCRS = secondCRS;

		this.filterInFirsLayer = filterInFirstLayer;
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
	 * Set the output parameters.
	 * 
	 * @param targetTypeName
	 * @param targetCRS
	 * @param geometryClass
	 */
	public void setOutputParams(final String targetTypeName,
	                            final CoordinateReferenceSystem targetCRS,
								final Class<? extends Geometry> targetClass) {

		setTargetLayerToCreate(targetTypeName, targetCRS, targetClass);

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

		} else if (!checkFilter(this.filterInFirsLayer)) {

			return false;

		} else if (!checkFilter(this.filterInSecondLayer)) {

			return false;
		}
		this.message = new InfoMessage(Messages.IntersectCommand_parameters_ok, InfoMessage.Type.INFORMATION);

		return true;
	}

	/**
	 * @return true if the geometry of target is compatible with the
	 *         intersection geometry type
	 */
	private boolean checkTargetCompatibility() {

		this.message = InfoMessage.NULL;

		// The geometry dimension of target layer must be
		// equal to the minor of the source layers' dimension.
		List<Class<? extends Geometry>> expectedGeometry = getGeometryExpected(firstLayer, secondLayer);

		Class<? extends Geometry> targetGeometry;
		if (getTargetLayer() != null) {
			SimpleFeatureType targetType = getTargetLayer().getSchema();
			GeometryDescriptor geomAttr = targetType.getGeometryDescriptor();
			targetGeometry =  (Class<? extends Geometry>) geomAttr.getType().getBinding();
		} else {
			// its new layer
			targetGeometry = getTargetLayerGeometryClass();
		}
		// when the expected geometry is only one.
		if (expectedGeometry.size() == 1) {
			this.geomValidator.setExpected(expectedGeometry.get(0));
			this.geomValidator.setTarget(targetGeometry);

			try {
				if (!this.geomValidator.validate()) {

					this.message = this.geomValidator.getMessage();

					return false;
				}
			} catch (Exception e) {
				this.message = new InfoMessage(Messages.IntersectCommand_faild_validating_geometry_compatibility,
							InfoMessage.Type.FAIL);
				return false;
			}
		} else {
			// When the expected geometry are 2 geometries.
			this.geomValidator.setExpected(expectedGeometry.get(0));
			this.geomValidator.setExpectedList(expectedGeometry);
			this.geomValidator.setTarget(targetGeometry);

			try {
				if (!this.geomValidator.validateBoth()) {

					this.message = this.geomValidator.getMessage();

					return false;
				}
			} catch (Exception e) {
				this.message = new InfoMessage(Messages.IntersectCommand_faild_validating_geometry_compatibility,
							InfoMessage.Type.FAIL);
				return false;
			}
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

			this.message = new InfoMessage(Messages.IntersectCommand_first_and_second_must_be_differents,
						InfoMessage.Type.ERROR);

			return false;
		}
		layerList.add(this.secondLayer);

		if (layerList.contains(getTargetLayer())) {

			layerList.add(this.firstLayer);
			this.message = new InfoMessage(Messages.IntersectCommand_first_sectond_and_target_must_be_differents,
						InfoMessage.Type.ERROR);

			return false;
		}
		return true;
	}

	/**
	 * Analyzes the source layers and produces the target geometry required for
	 * target layer.
	 * <p>
	 * The result will be the layer's geometry that has the minimum dimension.
	 * If someone of they has Geometry type the result must be Geometry
	 * </p>
	 * 
	 * @param firstLayer
	 * @param secondLayer
	 * @return the geometry expected
	 */
	private List<Class<? extends Geometry>> getGeometryExpected(final ILayer firstLayer, final ILayer secondLayer) {

		List<Class<? extends Geometry>> list = new ArrayList<Class<? extends Geometry>>();

		SimpleFeatureType firstType = firstLayer.getSchema();
		SimpleFeatureType secondType = secondLayer.getSchema();

		// first checks if some of layers have got Geometry class
		// If that is true returns Geometry class.
		GeometryDescriptor firstGeomAttr = firstType.getGeometryDescriptor();
		Class<? extends Geometry> firstGeomClass = (Class<? extends Geometry>) firstGeomAttr.getType().getBinding();

		GeometryDescriptor secondGeomAttr = secondType.getGeometryDescriptor();
		Class<? extends Geometry> secondGeomClass = (Class<? extends Geometry>) secondGeomAttr.getType().getBinding();

		if (Geometry.class.equals(firstGeomClass) || Geometry.class.equals(secondGeomClass)) {
			list.add(Geometry.class);
			return list;
		}

		// if they have not got Geometry class checks the dimension and
		// return the class of minimum
		int firstLayerDim = GeoToolsUtils.getDimensionOf(firstType);
		int secondLayerDim = GeoToolsUtils.getDimensionOf(secondType);
		int lessDim;
		SimpleFeatureType featureTypeExpected;

		if (firstLayerDim <= secondLayerDim) {
			featureTypeExpected = firstType;
			lessDim = firstLayerDim;
		} else {
			featureTypeExpected = secondType;
			lessDim = secondLayerDim;

		}
		GeometryDescriptor geomType = featureTypeExpected.getGeometryDescriptor();
		Class<? extends Geometry> geomClass = (Class<? extends Geometry>) geomType.getType().getBinding();
		list.add(geomClass);

		// another valid geometry will be a geometry with one dimension less
		// than the geometry
		// with less dimension from first layer and second layer.
		switch (lessDim) {
		case 1:
			list.add(MultiPoint.class);

			break;
		case 2:
			list.add(MultiLineString.class);
			list.add(MultiPoint.class);

			break;
		default:
			break;
		}

		return list;
	}

	/**
	 * Executes the intersect operation.
	 */
	@Override
	public void executeOperation() throws SOCommandException {

		// Creates the required parameters to create new layer or use an
		// existent layer.
		IIntersectParameters params = null;
		if (getTargetLayer() != null) {
			params = ParametersFactory.createIntersectParameters(this.firstLayer, this.firstCRS, this.secondLayer,
						this.secondCRS, getTargetLayer(), getTargetLayerCRS(), this.filterInFirsLayer,
						this.filterInSecondLayer);

		} else {

			params = ParametersFactory.createIntersectParameters(this.firstLayer, this.firstCRS, this.secondLayer,
						this.secondCRS, getTargetLayerName(), getTargetLayerGeometryClass(), this.filterInFirsLayer,
						this.filterInSecondLayer);

		}

		ISOTaskManager taskManager = SOTaskManager.getInstance();
		taskManager.intersectOperation(params);

	}

	@Override
	public void initParameters() {

		//firstLayer = null;
		//firstCRS = null;
		secondLayer = null;
		secondCRS = null;

		this.filterInFirsLayer = null;
		this.filterInSecondLayer = null;

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
					Geometry.class,
		};

		return obj;
	}

	/**
	 * Get the result layer geometry.
	 * 
	 * @return The intersect operation valid geometries are:
	 *         <p>
	 *         The geometry with the less dimension between source layer and
	 *         second layer, if this resultant geometry hasn't dimension 0,
	 *         another valid geometry is a geometry with one dimension less than
	 *         the resultant geometry. And the last one is {@link Geometry}.
	 *         </p>
	 * 
	 *         <pre>
	 * i.e. First layer geometry is {@link MultiPolygon} and second layer geometry is {@link MultiLineString}
	 * the result layer geometry will be one of this:
	 * 
	 * {@link MultiLineString}
	 * {@link MultiPoint}
	 * {@link Geometry}
	 * 
	 * </pre>
	 */
	@Override
	protected Object[] getValidTargetLayerGeometries() {

		Object[] validGeometrys = new Object[4];
		int arraySize = 0;

		if ((firstLayer == null) || (secondLayer == null)) {
			return new Object[]{Geometry.class};
			
		}

		if (firstLayer.getSchema().getGeometryDescriptor().getType()
				.getBinding().equals(Geometry.class)
				|| secondLayer.getSchema().getGeometryDescriptor().getType()
						.getBinding().equals(Geometry.class)) {

			validGeometrys = new Object[1];
			validGeometrys[0] = Geometry.class;
			return validGeometrys;
		}

		int lessDimensionLayer;

		int firstLayerGeomDimension = GeoToolsUtils.getDimensionOf(firstLayer
				.getSchema());
		int secondLayerGeomDimension = GeoToolsUtils.getDimensionOf(secondLayer
				.getSchema());

		if (firstLayerGeomDimension > secondLayerGeomDimension) {

			validGeometrys[0] = secondLayer.getSchema().getGeometryDescriptor()
					.getType().getBinding();
			lessDimensionLayer = secondLayerGeomDimension;
		} else {

			validGeometrys[0] = firstLayer.getSchema().getGeometryDescriptor()
					.getType().getBinding();
			lessDimensionLayer = firstLayerGeomDimension;
		}
		arraySize++;
		validGeometrys[1] = Geometry.class;
		arraySize++;

		// another valid geometry will be a geometry with one dimension less
		// than the geometry
		// with less dimension from first layer and second layer.
		switch (lessDimensionLayer) {
		case 1:
			validGeometrys[2] = MultiPoint.class;
			arraySize++;
			break;
		case 2:
			validGeometrys[2] = MultiLineString.class;
			arraySize++;
			validGeometrys[3] = MultiPoint.class;
			arraySize++;
			break;
		default:
			break;
		}

		Object[] geometries = new Object[arraySize];

		geometries[0] = validGeometrys[0];

		// Last item of the geometries array will be the Geometry.Class. All
		// the commands show the
		// Geometry.Class as the last item, so here is maintained the same
		// order.
		if (arraySize == 4) {
			geometries[1] = validGeometrys[3];
			geometries[2] = validGeometrys[2];
			geometries[3] = validGeometrys[1];
		} else if (arraySize == 3) {
			geometries[1] = validGeometrys[2];
			geometries[2] = validGeometrys[1];
		} else {
			geometries[1] = validGeometrys[1];
		}

		return geometries;

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

	public final String getOperationName() {

		return Messages.IntersectComposite_operation_name;
	}

	public String getToolTipText() {

		return Messages.IntersectCommand_description;
	}
}
