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
import es.axios.udig.spatialoperations.internal.parameters.ISpatialJoinGeomParameters;
import es.axios.udig.spatialoperations.internal.parameters.ParametersFactory;
import es.axios.udig.spatialoperations.tasks.SpatialRelation;
import es.axios.udig.spatialoperations.ui.common.LayerValidator;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.ISOTaskManager;
import es.axios.udig.spatialoperations.ui.taskmanager.SOTaskManager;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Spatial Join Geometries Command
 * <p>
 * This class is responsible to check the parameters and execute the spatial
 * join geometries process.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
public final class SpatialJoinGeomCommand extends SpatialOperationCommand {

	private static final InfoMessage	INITIAL_MESSAGE		= new InfoMessage(
																		Messages.SpatialJoinGeomCommand_initial_message,
																		InfoMessage.Type.IMPORTANT_INFO);
	private LayerValidator				layerValidator		= new LayerValidator();							;

	private ILayer						firstLayer			= null;
	private CoordinateReferenceSystem	firstCRS			= null;
	private ILayer						secondLayer			= null;
	private CoordinateReferenceSystem	secondCRS			= null;
	private SpatialRelation				relation			= SpatialRelation.Contains;					// Default
	private CoordinateReferenceSystem	mapCrs				= null;

	private Filter						filterInFirsLayer	= null;
	private Filter						filterInSecondLayer	= null;
	private boolean						selection			= false;

	/* a mirror of "boolean selection" but this allows null */
	private Boolean						selectionNull		= null;

	/**
	 * @param initialMessage
	 */
	public SpatialJoinGeomCommand() {

		super(INITIAL_MESSAGE);
	}

	public String getOperationID() {
		return "spatialJoinGeom"; //$NON-NLS-1$
	}

	/**
	 * Sets the output parameters.
	 * 
	 * @param firstLayer
	 * @param firstCRS
	 * @param secondLayer
	 * @param secondCRS
	 * @param relation
	 * @param mapCrs
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 * @param selection
	 */
	public void setInputParams(	final ILayer firstLayer,
								final CoordinateReferenceSystem firstCRS,
								final ILayer secondLayer,
								final CoordinateReferenceSystem secondCRS,
								final SpatialRelation relation,
								final CoordinateReferenceSystem mapCrs,
								final Filter filterInFirstLayer,
								final Filter filterInSecondLayer,
								final Boolean selection) {

		this.firstLayer = firstLayer;
		this.firstCRS = firstCRS;
		this.secondLayer = secondLayer;
		this.secondCRS = secondCRS;
		this.relation = relation;
		this.mapCrs = mapCrs;
		this.filterInFirsLayer = filterInFirstLayer;
		this.filterInSecondLayer = filterInSecondLayer;
		this.selection = selection;

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
	 * @param targetClass
	 */
	public void setOutputParams(final String layerName,
								final CoordinateReferenceSystem targetCRS,
								final Class<? extends Geometry> targetClass) {

		setTargetLayerToCreate(layerName, targetCRS, targetClass);
	}

	@Override
	protected boolean validateParameters() {

		if (!checkLayer(this.firstLayer, layerValidator)) {
			return false;
		}
		if (!checkLayer(this.secondLayer, layerValidator)) {
			return false;
		}
		if (!checkRelation(this.relation)) {
			return false;
		}
		if (!checkFilter(this.filterInFirsLayer)) {
			return false;
		}
		if (!checkFilter(this.filterInSecondLayer)) {
			return false;
		}
		if (!this.selection) {
			if (!checkTarget()) {
				return false;
			}
		}

		return true;
	}

	private boolean checkRelation(final SpatialRelation relation) {

		this.message = INITIAL_MESSAGE;

		if (relation == null) {
			this.message = new InfoMessage(Messages.SpatialJoinGeomCommand_Relation_required, InfoMessage.Type.ERROR);
			return false;
		}
		return true;
	}

	/**
	 * @return true if the target has a correct value, false in other case
	 */
	private boolean checkTarget() {

		assert this.firstLayer != null;
		assert this.secondLayer != null;

		this.message = INITIAL_MESSAGE;

		// target check (create new layer or existent layer selected)
		if (getTargetLayer() == null) {

			if ((getTargetLayerName() == null) || (getTargetLayerGeometryClass() == null)) {
				this.message = new InfoMessage(Messages.SpatialJoinGeomCommand_Target_Layer,
							InfoMessage.Type.INFORMATION);
				return false;
			} else {

				if (getTargetLayerName() != null) {
					// target and source must have different name
					if (getTargetLayerName().equals(this.firstLayer.getName())
								|| getTargetLayerName().equals(this.secondLayer.getName())) {
						this.message = new InfoMessage(
									Messages.SpatialJoinGeomCommand_Source_and_result_must_be_different_names,
									InfoMessage.Type.ERROR);
						return false;
					}
				}
			}
		} else { // An existent layer was selected then the sources and
			// target layers must be different

			if (getTargetLayer().equals(this.firstLayer) || getTargetLayer().equals(this.secondLayer)) {
				this.message = new InfoMessage(Messages.SpatialJoinGeomCommand_Source_and_result_must_be_different,
							InfoMessage.Type.ERROR);
				return false;
			}
		}

		Geometry SecondLayerGeom = null; // TODO set the geometries
		Geometry targetGeometry = null;
		if (!isTargetGeometryCompatible(SecondLayerGeom, targetGeometry)) {

			this.message = INITIAL_MESSAGE;
			return false;
		}

		return true;
	}

	/**
	 * Returns true if the target geometry is equal to second layer or it is
	 * {@link Geometry}
	 * 
	 * @param secondLayerGeom
	 * @param targetGeometry
	 * @return true if the geometries are compatible
	 */
	private boolean isTargetGeometryCompatible(final Geometry secondLayerGeom, final Geometry targetGeometry) {

		// TODO the target geometry must be equal to second lager or geometry

		return true;
	}

	@Override
	public void executeOperation() throws SOCommandException {

		ISpatialJoinGeomParameters params = null;
		if (getTargetLayer() != null) {

			params = ParametersFactory.createSpatialJoinParameters(this.firstLayer, this.firstCRS, this.secondLayer,
						this.secondCRS, this.relation, this.mapCrs, getTargetLayer(), getTargetLayerCRS(),
						this.filterInFirsLayer, this.filterInSecondLayer, this.selection);

		} else {

			params = ParametersFactory.createSpatialJoinParameters(this.firstLayer, this.firstCRS, this.secondLayer,
						this.secondCRS, this.relation, this.mapCrs, getTargetLayerName(), getTargetLayerGeometryClass(),
						this.filterInFirsLayer, this.filterInSecondLayer, this.selection);
		}

		ISOTaskManager taskManager = SOTaskManager.getInstance();
		taskManager.spatialJoinOperation(params);
	}

	@Override
	public void initParameters() {

		this.mapCrs = null;

		this.firstLayer = null;
		this.firstCRS = null;
		this.secondLayer = null;
		this.secondCRS = null;

		this.filterInFirsLayer = null;
		this.filterInSecondLayer = null;
		// this.relation = null;
		this.selectionNull = null;

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
	 * @return The spatial join geometry operation valid geometries are:
	 *         <p>
	 *         The same geometry of the source layer and the {@link Geometry}.
	 *         </p>
	 * 
	 */
	protected Object[] getValidTargetLayerGeometries() {

		Object[] validGeometries = null;
		Class<?> geomClass = null;

		if (firstLayer != null) {

			geomClass = firstLayer.getSchema().getGeometryDescriptor().getType().getBinding();
			if ((geomClass.equals(Geometry.class))) {
				validGeometries = new Object[]{
							Geometry.class
						};
			} else {

				validGeometries = new Object[]
				        { 	geomClass,
							Geometry.class
						};
			}
		} else {
			validGeometries = new Object[] {Geometry.class};			
		}
		return validGeometries;
	}

	/**
	 * 
	 * @return The spatial relation.
	 */
	public SpatialRelation getRelation() {

		return this.relation;
	}

	/**
	 * 
	 * @return True if selection is checked.
	 */
	public Boolean getSelection() {

		if (this.selectionNull == null) {
			return false;
		}
		// Return true or false
		return this.selection;
	}

	/**
	 * Set the default values for the first layer and the selection.
	 * 
	 * @param firstLayer
	 * @param selection
	 */
	public void setDefaultValues(ILayer firstLayer, Boolean selection) {

		this.firstLayer = firstLayer;
		this.secondLayer = null;
		this.selection = selection;
		this.selectionNull = selection;
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

	/**
	 * Set the spatial relation.
	 * 
	 * @param currentRelation
	 */
	public void setSpatialRelation(SpatialRelation currentRelation) {

		this.relation = currentRelation;
	}

	public String getOperationName() {

		return Messages.SpatialJoinGeomComposite_Operation_Name;
	}

	public String getToolTipText() {

		return Messages.SpatialJoinGeomComposite_Description;
	}

}
