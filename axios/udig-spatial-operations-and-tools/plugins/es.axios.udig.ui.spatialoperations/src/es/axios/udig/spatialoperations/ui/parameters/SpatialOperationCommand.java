/**
 * 
 */
package es.axios.udig.spatialoperations.ui.parameters;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.common.ISOTargetLayerValidator;
import es.axios.udig.spatialoperations.ui.common.ISOValidator;
import es.axios.udig.spatialoperations.ui.common.ResultLayerComposite;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.message.InfoMessage.Type;

/**
 * Abstract Spatial Operation Command
 * <p>
 * Abstracts the common behaviour of spatial operation commands. This
 * implementation work as observable object for all spatial operation
 * presenters. This class inform to observers when the parameters are validated.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 * @since 1.1.0
 */
public abstract class SpatialOperationCommand extends Observable implements ISOCommand {

	/* common parameters */
	public enum ParameterName {
		TARGET_GEOMETRY_CLASS, 
		SELECTED_LAYER, 
		TARGET_NEW_LAYER, 
		SOURCE_GEOMETRY_CLASS, 
		REFERENCE_GEOMETRY_CLASS
	};

	private boolean						canExecute			= false;

	protected InfoMessage				message				= InfoMessage.NULL;
	protected InfoMessage				defaultMessage		= null;

	/* Result layer parameters */
	private ILayer						targetLayer			= null;
	private String						nameOfNewTargetlayer= null;
	private Class<? extends Geometry>	targetGeometryClass	= null;

	protected IMap						map					= null;
	/**
	 * The state of the command. Until all the default values are set, the
	 * status will be stopped, once they are set, the state will change to
	 * Running.
	 */
	private CmdStatus					status				= CmdStatus.STOPPED;

	private CoordinateReferenceSystem targetCRS;


	/**
	 * Initialize the generic attribute of command
	 * 
	 * @param initialMessage
	 */
	protected SpatialOperationCommand(final InfoMessage initialMessage) {
		assert initialMessage != null;

		this.defaultMessage = initialMessage;
		this.message = this.defaultMessage;
	}

	public final InfoMessage getMessage() {
		return this.message;
	}

	public InfoMessage getDefaultMessage() {

		return this.defaultMessage;
	}

	/**
	 * <p>
	 * Evaluates input data, if they have errors a message error will be setted.
	 * 
	 * Also, set the observer as modified and notify it.
	 * </p>
	 * 
	 * @return true if inputs data are okay
	 */
	public boolean evalPrecondition() {

		boolean result = validateParameters();
		this.canExecute = result;

		setChanged();
		notifyObservers(""); //$NON-NLS-1$
		//TODO workaround, this should be improved. See track ticket: #196   
		setChanged();
		notifyObservers(ParameterName.SOURCE_GEOMETRY_CLASS); 
		setChanged();
		notifyObservers(ParameterName.REFERENCE_GEOMETRY_CLASS); 

		return result;
	}

	/**
	 * Checks the parameters of spatial operation
	 * 
	 * @return true if inputs data are okay
	 */
	abstract protected boolean validateParameters();

	public void execute() throws SOCommandException {

		if (!this.canExecute) {
			throw new SOCommandException("the precondition is false."); //$NON-NLS-1$
		}

		setStatus(CmdStatus.EXECUTING);

		executeOperation();
		reset();
	}

	/**
	 * Execute the spatial operation if the precondition is true.
	 * 
	 * @throws SOCommandException
	 * 
	 */
	public abstract void executeOperation() throws SOCommandException;

	/**
	 * Resets the command
	 * 
	 */
	public void reset() {

		message = this.defaultMessage;
		canExecute = false;

		initParameters();
		initTargetLayer();


		setStatus(CmdStatus.STOPPED);
	}

	/**
	 * Initialize the parameters. If you want maintain the parameters values in
	 * the user session you should not set that parameters in this method. So
	 * same parameters' values will live between the executing of one operation
	 * and other.
	 * 
	 */
	public abstract void initParameters();

	/**
	 * @return True if the operations can be executed
	 */
	public boolean canExecute() {
		return canExecute;
	}

	/**
	 * Get the domain(range of valid values) of the given parameter. The
	 * subclass should provide an implementation.
	 * 
	 * @param parameterName
	 *            Desired parameter.
	 * @return The domain of the given parameter.
	 */
	public List<?> getDomainValues(ParameterName parameterName) {

		Object[] geomClasses = null;
		switch (parameterName) {
		case TARGET_GEOMETRY_CLASS:
			geomClasses = getValidTargetLayerGeometries();
			break;
		case SOURCE_GEOMETRY_CLASS:
			geomClasses = getSourceGeomertyClass();
			break;
		case REFERENCE_GEOMETRY_CLASS:
			geomClasses = getReferenceGeomertyClass();
			break;
		default:
			assert false : parameterName + " is not parameter name for this command"; //$NON-NLS-1$
		}
		return Arrays.asList(geomClasses);
	}

	/**
	 * Get the valid geometry classes for the source layer.
	 * 
	 * @return The compatible classes.
	 */
	protected abstract Object[] getSourceGeomertyClass();

	/**
	 * Get the valid geometry classes for the reference layer.
	 * 
	 * @return The compatible classes.
	 */
	protected Object[] getReferenceGeomertyClass() {
		// null implementation by default
		return null;
	}

	/**
	 * Gets the valid geometry classes for the target layer.
	 * 
	 * @return The compatible classes.
	 */
	protected abstract Object[] getValidTargetLayerGeometries();

	/**
	 * @return True if the command is deactivated.
	 */
	public boolean isStopped() {

		return CmdStatus.STOPPED.equals(this.status);
	}

	public void setStatus(CmdStatus status) {
		this.status = status;

		setChanged();
		notifyObservers(); 
	}

	/**
	 * 
	 * @return True if the command is activated.
	 */
	public boolean isReady() {

		return CmdStatus.READY.equals(this.status);

	}

	/**
	 * True if {@link ResultLayerComposite} has called the
	 * setDefaultValuesForResult. Set the values for target layer or for a new
	 * layer(name, geometry type).
	 * 
	 * @return
	 */
	protected boolean isTargetLayerSelected() {

		if (targetLayer == null && nameOfNewTargetlayer == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 
	 * @return The target layer.
	 */
	public ILayer getTargetLayer() {

		return targetLayer;
	}

	/**
	 * 
	 * @return True if target layer is an existent one.
	 */
	public boolean isLayerSelected() {

		return this.targetLayer != null;
	}

	/**
	 * 
	 * @return True if target layer is a new one.
	 */
	public boolean isNewTargetLayer() {

		return this.nameOfNewTargetlayer != null;
	}

	/**
	 * Set an existent layer as target layer.
	 * 
	 * @param targetLayer
	 */
	public void setTargetLayer(ILayer targetLayer) {
		
		this.targetLayer = targetLayer;
		this.nameOfNewTargetlayer = null;
		
		setChanged();
		notifyObservers(ParameterName.SELECTED_LAYER); 
	}
	

	/**
	 * Set the geometry class of the new target layer.
	 * 
	 * @param targetClass
	 */
	public void setTargetLayerGeometry(Class<? extends Geometry> targetClass){
		this.targetGeometryClass = targetClass;
		
		setChanged();
		notifyObservers(ParameterName.TARGET_GEOMETRY_CLASS); 
		
	}

	

	/**
	 * Sets layer's name will be created. If the parameter is "" the feature type name will be set to null
	 * 
	 * @param nameOfNewLayer
	 */
	public void setTargetLayerName(String nameOfNewLayer ) {

		this.nameOfNewTargetlayer= nameOfNewLayer;
		if( this.nameOfNewTargetlayer != null){
			// if the name is the null string ("") it is set with null value
			this.nameOfNewTargetlayer = (nameOfNewLayer.length() > 0)? nameOfNewLayer : null; 
		}
		this.targetLayer = null;

		setChanged();
		notifyObservers(ParameterName.TARGET_NEW_LAYER); 
	}
	
	protected CoordinateReferenceSystem getTargetLayerCRS() {
		
		return this.targetCRS;
		
	}
	

	protected void setTargetLayerCRS(CoordinateReferenceSystem targetCRS) {
		this.targetCRS = targetCRS;
		
		setChanged();
		notifyObservers("");  //$NON-NLS-1$
	}
	
	

	/**
	 * True by default, only SpatialJoinGeomController will override this
	 * method, because is the only one that have selection option.
	 * 
	 * @return
	 */
	public boolean isSelection() {

		return false;
	}

	/**
	 * Check if the filter is null or {@link Filter#ALL}.
	 * 
	 * @param filter
	 * @return False if the conditions are fulfilled.
	 */
	protected boolean checkFilter(final Filter filter) {

		this.message = getDefaultMessage();

		if (filter == null) {
			this.message = new InfoMessage(Messages.SpatialJoinGeomCommand_filter_null, InfoMessage.Type.INFORMATION);
			return false;
		} else {
			if (Filter.EXCLUDE.equals(filter)) {
				this.message = new InfoMessage(Messages.SpatialJoinGeomCommand_filter_empty, InfoMessage.Type.ERROR);
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if there are some null parameter and sets a human message.
	 * 
	 * @return false if found any null parameter, true in other case.
	 */
	protected boolean hasNullParameters(ILayer firstLayer, ILayer secondLayer) {

		this.message = InfoMessage.NULL;

		if (firstLayer == null) {

			this.message = new InfoMessage(Messages.IntersectCommand_must_select_the_first_layer,
						InfoMessage.Type.INFORMATION);

			return true;
		}

		if (secondLayer == null) {

			this.message = new InfoMessage(Messages.IntersectCommand_must_select_second_layer,
						InfoMessage.Type.INFORMATION);

			return true;
		}
		if ((this.targetLayer == null) && (this.nameOfNewTargetlayer == null) && (this.targetGeometryClass == null)) {

			this.message = new InfoMessage(Messages.IntersectCommand_must_select_target_layer,
						InfoMessage.Type.INFORMATION);

			return true;
		}

		return false;
	}

	/**
	 * Check if the CRS is null.
	 * 
	 * @param mapCrs
	 * @return False if its null.
	 */
	protected boolean checkCRS(CoordinateReferenceSystem mapCrs) {
		this.message = getDefaultMessage();

		if (mapCrs == null) {
			this.message = new InfoMessage(Messages.PolygonToLineCommand_map_crs, InfoMessage.Type.INFORMATION);
			return false;
		}

		return true;
	}

	/**
	 * @return true if the target has a correct value, false in other case
	 */
	protected boolean checkTarget(final ISOTargetLayerValidator targetValidator, final ILayer sourceLayer) {

		try {

			this.message = getDefaultMessage();

			targetValidator.setSourceLayer(sourceLayer);
			if (this.targetLayer != null) {
				targetValidator.setTargetLayer(this.targetLayer);
			} else {
				targetValidator.setTargetLayerName(this.nameOfNewTargetlayer, this.targetGeometryClass);
			}
			if (!targetValidator.validate()) {
				this.message = targetValidator.getMessage();
				return false;
			}
			if (!targetValidator.validGeometryCollectionCompatible()) {
				this.message = targetValidator.getMessage();
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Check a layer, and if it isn't OK, return false and change the message of
	 * the cmd.
	 * 
	 * @param layer
	 * @param layerValidator
	 * @return False if it isn't OK.
	 */
	protected boolean checkLayer(final ILayer layer, ISOValidator layerValidator) {

		try {
			this.message = getDefaultMessage();

			layerValidator.setParameters(layer);
			if (!layerValidator.validate()) {

				this.message = layerValidator.getMessage();

				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Check that the first layer is Polygon or MultiPolygon and check that the
	 * second layer is LineString or MultiLineString.
	 * TODO move to defaultCommandImplementation class. 
	 * @return
	 */
	protected boolean checkLayersCompatibility(ILayer firstLayer, ILayer secondLayer) {

		FeatureType firstType = firstLayer.getSchema();
		FeatureType secondType = secondLayer.getSchema();

		GeometryDescriptor firstGeomAttr = firstType.getGeometryDescriptor();
		Class<? extends Geometry> firstGeomClass = (Class<? extends Geometry>) firstGeomAttr.getType().getBinding();

		GeometryDescriptor secondGeomAttr = secondType.getGeometryDescriptor();
		Class<? extends Geometry> secondGeomClass = (Class<? extends Geometry>) secondGeomAttr.getType().getBinding();

		if (!firstGeomClass.equals(MultiPolygon.class) && !firstGeomClass.equals(Polygon.class)) {

			this.message = new InfoMessage(Messages.SpatialOperationCommand_source_polygon, Type.ERROR);
			return false;
		}
		if (!secondGeomClass.equals(MultiLineString.class) && !secondGeomClass.equals(LineString.class)) {

			this.message = new InfoMessage(Messages.SpatialOperationCommand_reference_line, Type.ERROR);
			return false;
		}
		return true;
	}

	public String getTargetLayerName() {
		
		return this.nameOfNewTargetlayer;
	}

	public Class<? extends Geometry> getTargetLayerGeometryClass() {
		return this.targetGeometryClass;
	}

	/** 
	 * Set all values required to create a new layer. 
	 * 
	 * It is a convenient method.
	 * @see setTargetLayerName, setTargetLayerGeomety, setTargetLayerCRS
	 * 
	 * 
	 *
	 * @param layerName
	 * @param targetCRS
	 * @param targetClass
	 */
	public void setTargetLayerToCreate(
			final String layerName,
			final CoordinateReferenceSystem targetCRS,
			final Class<? extends Geometry> targetClass) {

		setTargetLayerName(layerName);
		setTargetLayerGeometry(targetClass);
		setTargetLayerCRS(targetCRS);
	
	}

	public void initTargetLayer() {

		setTargetLayer(null);
		setTargetLayerCRS(null);
		setTargetLayerName(null);
		setTargetLayerGeometry(null);
	}
	
}
