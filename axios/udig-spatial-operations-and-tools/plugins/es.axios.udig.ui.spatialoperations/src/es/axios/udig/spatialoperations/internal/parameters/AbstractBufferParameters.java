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

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.tasks.IBufferTask.CapStyle;

/**
 * Common parameter for buffer operation
 * <p>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
abstract class AbstractBufferParameters implements IBufferParameters {

	private ILayer						sourceLayer			= null;

	private Boolean						mergeGeometries		= null;

	private CapStyle					endCapStyle			= null;

	private Integer						quadrantSegments	= null;

	private Unit<?>						unitsOfMeasure		= null;

	private Filter						filter				= null;

	private Double						width				= null;

	private CoordinateReferenceSystem	sourceCRS			= null;

	/**
	 * Common initialization for Buffer parameters
	 * 
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param selectedFeatures
	 * @param mergeGeometry
	 * @param width
	 * @param quadrantSegments
	 * @param unit
	 */
	public AbstractBufferParameters(final ILayer sourceLayer,
									final CoordinateReferenceSystem sourceCRS,
									final Filter filter,
									final Boolean mergeGeometry,
									final Double width,
									final Unit<?> unit,
									final Integer quadrantSegments,
									final CapStyle endCapStyle) {

		assert sourceLayer != null;
		assert sourceCRS != null;
		assert mergeGeometry != null;
		assert width != null;
		assert quadrantSegments != null;
		assert unit != null;
		assert filter != null;
		assert endCapStyle != null;

		this.sourceLayer = sourceLayer;
		this.sourceCRS = sourceCRS;
		this.mergeGeometries = mergeGeometry;
		this.width = width;
		this.quadrantSegments = quadrantSegments;
		this.unitsOfMeasure = unit;
		this.filter = filter;

		this.endCapStyle = endCapStyle;

	}

	/**
	 * @return the width
	 */
	public Double getWidth() {
		return width;
	}

	/**
	 * @return the quadrant segments definition
	 */
	public Integer getQuadrantSegments() {
		return quadrantSegments;
	}

	/**
	 * @return the end cap style for buffer
	 */
	public CapStyle getCapStyle() {
		return endCapStyle;
	}

	/**
	 * @return the units
	 */
	public Unit<?> getUnitsOfMeasure() {
		return unitsOfMeasure;
	}

	/**
	 * @return true if merge geometries is required
	 */
	public boolean isMergeGeometries() {
		return mergeGeometries;
	}

	/**
	 * @return Returns the sourceLayer.
	 */
	public ILayer getSourceLayer() {
		return this.sourceLayer;
	}

	/**
	 * 
	 * @return The filter.
	 */
	public Filter getFilter() {
		return this.filter;
	}

	/**
	 * @return The source CRS.
	 */
	public CoordinateReferenceSystem getSourceCRS() {

		return this.sourceCRS;
	}

}
