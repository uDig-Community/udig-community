package es.axios.udig.spatialoperations.ui.common;

import com.vividsolutions.jts.geom.Geometry;

import net.refractions.udig.project.ILayer;
/**
 * 
 * Validates the target layer
 * <p>
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
public interface ISOTargetLayerValidator extends ISOValidator {

	void setSourceLayer(ILayer sourceLayer);

	void setTargetLayer(ILayer targetLayer);

	void setTargetLayerName(String targetLayerName,
			Class<? extends Geometry> targetGeometryClass);


	boolean validGeometryCollectionCompatible();

	boolean validGeometryCompatible(ILayer sourceLayer);
}