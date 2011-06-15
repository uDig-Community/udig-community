package es.axios.udig.spatialoperations.internal.parameters;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.refractions.udig.project.ILayer;

/**
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @since
 */
public interface ISpatialJoinInExistentLayerParameters extends ISpatialJoinGeomParameters {

	/**
	 * 
	 * @return The target layer.
	 */
	public ILayer getTargetLayer();

	/**
	 * 
	 * @return The target CRS.
	 */
	public CoordinateReferenceSystem getTargetCRS();

}
