package es.axios.udig.spatialoperations.internal.parameters;

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.tasks.IBufferTask.CapStyle;

/**
 * Encapsulates the parameters for a buffer operation
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
final class BufferInNewLayerParameters extends AbstractBufferParameters implements IBufferInNewLayerParameters {

	private SimpleFeatureType	targetFeatureType	= null;

	/**
	 * New instance of BufferInNewLayerParameters
	 * 
	 * @param sourceLayer
	 * @param newFeatureType
	 * @param mergeGeometry
	 * @param width
	 * @param quadrantSegments
	 * @param unit
	 */
	public BufferInNewLayerParameters(	final ILayer sourceLayer,
										final CoordinateReferenceSystem sourceCRS,
										final SimpleFeatureType newFeatureType,
										final Boolean mergeGeometry,
										final Double width,
										final Integer quadrantSegments,
										final Unit<?> unit,
										final Filter filter,
										final CapStyle endCapStyle) {

		super(sourceLayer, sourceCRS, filter, mergeGeometry, width, unit, quadrantSegments, endCapStyle);

		assert newFeatureType != null;

		this.targetFeatureType = newFeatureType;
	}

	/**
	 * @return the Feature Type of the new layer
	 */
	public final SimpleFeatureType getTargetFeatureType() {
		return targetFeatureType;
	}
}
