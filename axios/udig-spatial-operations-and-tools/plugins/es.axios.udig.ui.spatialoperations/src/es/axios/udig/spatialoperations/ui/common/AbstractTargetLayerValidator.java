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
package es.axios.udig.spatialoperations.ui.common;

import net.refractions.udig.project.ILayer;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.util.LayerUtil;

/**
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
public abstract class AbstractTargetLayerValidator implements ISOTargetLayerValidator {

	protected InfoMessage				message;
	protected ILayer					targetLayer;
	protected String					targetLayerName;
	protected Class<? extends Geometry>	targetGeometryClass;
	protected ILayer					sourceLayer;

	/**
	 * Validate the target layer.
	 */
	public boolean validate() throws Exception {

		// target check (create new layer or existent layer selected)
		if (this.targetLayer == null) {

			if ((this.targetLayerName == null) || (this.targetGeometryClass == null)) {
				this.message = new InfoMessage(Messages.SpatialJoinGeomCommand_Target_Layer,
							InfoMessage.Type.INFORMATION);
				return false;
			}
			if (this.targetLayerName != null) {
				// target and source must have different name

				// FIXME if sourceLayer is null, NPE is thrown from here.

				if (this.sourceLayer != null && this.targetLayerName.equals(this.sourceLayer.getName())) {
					this.message = new InfoMessage(Messages.TargetLayerValidator_source_result_different_names,
								InfoMessage.Type.ERROR);
					return false;
				}
			}

		} else { // An existent layer was selected then the sources and
			// target layers must be different

			if (this.targetLayer.equals(this.sourceLayer)) {
				this.message = new InfoMessage(Messages.TargetLayerValidator_source_result_different,
							InfoMessage.Type.ERROR);
				return false;
			}

			this.targetGeometryClass = LayerUtil.getGeometryClass(this.targetLayer);
		}
		return true;
	}

	/**
	 * @return The error message.
	 */
	public InfoMessage getMessage() {
		return this.message;
	}

	/**
	 * @param targetLayer
	 *            the targetLayer to set
	 */
	public final void setTargetLayer(ILayer targetLayer) {
		assert targetLayer != null;

		this.targetLayer = targetLayer;

		this.targetLayerName = null;
		this.targetGeometryClass = null;
	}

	/**
	 * @param targetLayerName
	 *            the targetLayerName to set
	 */
	public final void setTargetLayerName(String targetLayerName, Class<? extends Geometry> targetGeometryClass) {
		//		assert targetLayerName != null : "illegal argument"; //$NON-NLS-1$
		//		assert targetGeometryClass != null : "illegal argument"; //$NON-NLS-1$

		this.targetLayerName = targetLayerName;
		this.targetGeometryClass = targetGeometryClass;

		this.targetLayer = null;
	}

	/**
	 * @param sourceLayer
	 *            the sourceLayer to set
	 */
	public final void setSourceLayer(ILayer sourceLayer) {

		this.sourceLayer = sourceLayer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seees.axios.udig.spatialoperations.internal.ui.processconnectors.
	 * ISOTargetLayerValidator#isTargetGeometryCompatible()
	 */
	public abstract boolean validGeometryCollectionCompatible();

	/*
	 * (non-Javadoc)
	 * 
	 * @seees.axios.udig.spatialoperations.internal.ui.processconnectors.
	 * ISOTargetLayerValidator
	 * #isTargetGeometryCompatible(net.refractions.udig.project.ILayer)
	 */
	public abstract boolean validGeometryCompatible(ILayer sourceLayer2);

	public void setParameters(ILayer layer) {
		// implemented when needed.
	}

}
