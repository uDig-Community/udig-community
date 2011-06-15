/**
 * 
 */
package es.axios.udig.spatialoperations.ui.common;

import net.refractions.udig.project.ILayer;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author mauro
 *
 */
public abstract class TargetLayerListenerAdapter implements TargetLayerListener {

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.common.TargetLayerListener#validateTargetLayer()
	 */
	public void validateTargetLayer() {
		
		// null implementation

	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.common.TargetLayerListener#targetLayerSelected(net.refractions.udig.project.ILayer)
	 */
	public void targetLayerSelected(ILayer selectedLayer) {
		// null implementation

	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.common.TargetLayerListener#newTargetLayerName(java.lang.String)
	 */
	public void newTargetLayerName(String text) {
		// null implementation

	}

	/* (non-Javadoc)
	 * @see es.axios.udig.spatialoperations.ui.common.TargetLayerListener#newGeometrySelected(java.lang.Class)
	 */
	public void newGeometrySelected(Class<? extends Geometry> targetClass) {
		// null implementation

	}

}
