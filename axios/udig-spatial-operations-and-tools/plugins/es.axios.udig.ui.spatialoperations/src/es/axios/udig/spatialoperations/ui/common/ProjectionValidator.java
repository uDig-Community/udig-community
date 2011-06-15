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
package es.axios.udig.spatialoperations.ui.common;

import java.text.MessageFormat;
import java.util.ArrayList;

import net.refractions.udig.project.ILayer;

import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;

import com.vividsolutions.jts.geom.GeometryFactory;

import es.axios.geotools.util.GeoToolsUtils;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.preferences.PreferenceConstants;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Valids that exist a projection from layer to map
 * <p>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
public final class ProjectionValidator implements ISOValidator {

	private InfoMessage					messages		= null;
	private CoordinateReferenceSystem	sourceCrs		= null;
	private CoordinateReferenceSystem	targetCrs		= null;
	private ArrayList<Transformation>	transformations	= new ArrayList<Transformation>();

	class Transformation {
		private CoordinateReferenceSystem	sourceCrs	= null;
		private CoordinateReferenceSystem	targetCrs	= null;

		public Transformation(final CoordinateReferenceSystem sourceCrs, final CoordinateReferenceSystem targetCrs) {

			this.sourceCrs = sourceCrs;
			this.targetCrs = targetCrs;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sourceCrs == null) ? 0 : sourceCrs.hashCode());
			result = prime * result + ((targetCrs == null) ? 0 : targetCrs.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Transformation)) {
				return false;
			}
			final Transformation other = (Transformation) obj;
			if (sourceCrs == null) {
				if (other.sourceCrs != null) {
					return false;
				}
			} else if (!sourceCrs.equals(other.sourceCrs)) {
				return false;
			}
			if (targetCrs == null) {
				if (other.targetCrs != null) {
					return false;
				}
			} else if (!targetCrs.equals(other.targetCrs)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * @param sourceCrs
	 *            The sourceCrs to set.
	 */
	public void setSourceCrs(CoordinateReferenceSystem sourceCrs) {
		this.sourceCrs = sourceCrs;
	}

	/**
	 * @param targetCrs
	 *            The targetCrs to set.
	 */
	public void setTargetCrs(CoordinateReferenceSystem targetCrs) {
		this.targetCrs = targetCrs;
	}

	/**
	 * @return the error message.
	 */
	public final InfoMessage getMessage() {
		assert this.messages != null;
		return this.messages;
	}

	/**
	 * Checks the following subjects:
	 * <p>
	 * <ul>
	 * <li> The <code>layer</code> shall resolve to a
	 * <code>FeatureSource</code>
	 * <li> There has to be possible to transform geometries from the source
	 * layer's CRS to the Map's CRS
	 * <li> If the preference
	 * {@link PreferenceConstants#SELECTION_FALLBACK_TO_WHOLE_LAYER} is set to
	 * <code>false</code>, <code>sourceLayer</code> must have at least one
	 * selected Feature.
	 * </ul>
	 * </p>
	 * 
	 * @param layer
	 *            the layer over which the operation is meant to be ran.
	 * 
	 * @return true if exist the projection
	 * @throws Exception
	 *             if the preference fallback to whole layer is set to false and
	 *             an Exception is thrown while trying to obtain the layer's
	 *             selection count.
	 */
	public boolean validate() throws Exception {

		if (this.transformations.contains(new Transformation(sourceCrs, targetCrs))) {
			return true;
		}

		if (this.sourceCrs == null) {
			final String msg = Messages.ProjectionValidator_crs_source_can_not_be_null;
			this.messages = new InfoMessage(msg, InfoMessage.Type.ERROR);
			return false;
		}
		if (DefaultEngineeringCRS.GENERIC_2D.equals(sourceCrs)) {
			final String msg = Messages.ProjectionValidator_crs_source_unknown;
			this.messages = new InfoMessage(msg, InfoMessage.Type.ERROR);
			return false;
		}
		if (this.targetCrs == null) {
			final String msg = Messages.ProjectionValidator_crs_target_can_not_be_null;
			this.messages = new InfoMessage(msg, InfoMessage.Type.ERROR);
			return false;
		}

		GeometryFactory gFactory = new GeometryFactory();
		try {
			GeoToolsUtils.getTransformer(gFactory, sourceCrs, targetCrs);
			this.transformations.add(new Transformation(sourceCrs, targetCrs));

		}
		catch (OperationNotFoundException e) {
			String msg = MessageFormat.format(Messages.ProjectionValidator_impossible_reproject, e.getMessage());
			this.messages = new InfoMessage(msg, InfoMessage.Type.ERROR);
			return false;
		}

		return true;
	}

	public void setParameters(ILayer layer) {
		// nothing by default.
	}

}
