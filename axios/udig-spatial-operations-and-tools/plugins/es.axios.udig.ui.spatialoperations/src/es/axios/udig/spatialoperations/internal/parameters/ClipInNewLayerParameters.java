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
package es.axios.udig.spatialoperations.internal.parameters;

import net.refractions.udig.project.ILayer;


import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * TODO Purpose of
 * <p>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
final class ClipInNewLayerParameters extends AbstractClipParameters implements IClipInNewLayerParameters {

	private SimpleFeatureType	targetFeatureType	= null;

	/**
	 * 
	 * @param usingLayer
	 * @param clipSourceLayer
	 * @param targetFeatureType
	 * @param usingFilter
	 * @param clipSourceFilter
	 */
	public ClipInNewLayerParameters(final ILayer usingLayer,
									final CoordinateReferenceSystem usingCRS,
									final ILayer clipSourceLayer,
									final CoordinateReferenceSystem clipSourceCRS,
									final SimpleFeatureType targetFeatureType,
									final Filter usingFilter,
									final Filter clipSourceFilter) {

		super(usingLayer, usingCRS, clipSourceLayer, clipSourceCRS, usingFilter, clipSourceFilter);

		assert targetFeatureType != null;
		
		this.targetFeatureType = targetFeatureType;
	}

	/**
	 * @return Returns the targetFeatureType.
	 */
	public SimpleFeatureType getTargetFeatureType() {
		return this.targetFeatureType;
	}

}
