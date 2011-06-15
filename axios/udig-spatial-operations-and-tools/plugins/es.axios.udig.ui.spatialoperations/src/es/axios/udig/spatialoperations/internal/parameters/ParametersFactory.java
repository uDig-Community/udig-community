/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.internal.parameters;

import java.util.List;

import javax.measure.unit.Unit;

import net.refractions.udig.project.ILayer;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.tasks.SpatialRelation;
import es.axios.udig.spatialoperations.tasks.IBufferTask.CapStyle;

/**
 * Factory for Parameters used by in spatial operations.
 * <p>
 * This factory produces the object used to transfer spatial operation
 * parameters (object value).
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
public final class ParametersFactory {

	private ParametersFactory() {

		// there is not instance of this class
	}

	/**
	 * Parameters required to make the buffer on target layer
	 * 
	 * @param sourceLayer
	 * @param targetFeatureType
	 * @param mergeGeometry
	 * @param with
	 * @param quadrantSegments
	 * @param unit
	 * @return buffer parameters to make buffer in new target layer
	 */
	public final static IBufferInNewLayerParameters createBufferParameters(	final ILayer sourceLayer,
																			final CoordinateReferenceSystem sourceCRS,
																			final SimpleFeatureType targetFeatureType,
																			final Boolean mergeGeometry,
																			final Double with,
																			final Integer quadrantSegments,
																			final Unit<?> unit,
																			final Filter filter,
																			final CapStyle endCapStyle) {

		BufferInNewLayerParameters params = new BufferInNewLayerParameters(sourceLayer, sourceCRS, targetFeatureType,
					mergeGeometry, with, quadrantSegments, unit, filter, endCapStyle);

		return params;

	}

	/**
	 * Parameters required to create buffer on new layer
	 * 
	 * @param sourceLayer
	 * @param targetLayer
	 * @param mergeGeometry
	 * @param with
	 * @param quadrantSegments
	 * @param unit
	 * @param filter
	 * @return buffer parameters into the target layer
	 */
	public final static IBufferInExistentLayerParameters createBufferParameters(final ILayer sourceLayer,
																				final CoordinateReferenceSystem sourceCRS,
																				final ILayer targetLayer,
																				final CoordinateReferenceSystem targetCRS,
																				final Boolean mergeGeometry,
																				final Double with,
																				final Integer quadrantSegments,
																				final Unit<?> unit,
																				final Filter filter,
																				final CapStyle endCapStyle) {

		IBufferInExistentLayerParameters params = new BufferInExistentLayer(sourceLayer, sourceCRS, targetLayer,
					targetCRS, mergeGeometry, with, quadrantSegments, unit, filter, endCapStyle);

		return params;
	}

	/**
	 * New instance of intersect parameters
	 * 
	 * @param firstLayer
	 * @param firstCRS
	 * @param secondLayer
	 * @param secondCRS
	 * @param targetLayer
	 * @param targetCRS
	 * @return IIntersectParameters
	 */
	public static IIntersectInExistentLayerParameters createIntersectParameters(final ILayer firstLayer,
																				final CoordinateReferenceSystem firstCRS,
																				final ILayer secondLayer,
																				final CoordinateReferenceSystem secondCRS,
																				final ILayer targetLayer,
																				final CoordinateReferenceSystem targetCRS,
																				final Filter filterInFirstLayer,
																				final Filter filterInSecondLayer) {

		IIntersectInExistentLayerParameters params = new IntersectInExistenLayerParameters(firstLayer, firstCRS,
					secondLayer, secondCRS, targetLayer, targetCRS, filterInFirstLayer, filterInSecondLayer);

		return params;
	}

	/**
	 * Parameters required to make an intersect in new layer.
	 * 
	 * @param firstLayer
	 * @param firstCRS
	 * @param secondLayer
	 * @param secondCRS
	 * @param targetName
	 * @param targetGeometryClass
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 * @return
	 */
	public static IIntersectInNewLayerParameters createIntersectParameters(	final ILayer firstLayer,
																			final CoordinateReferenceSystem firstCRS,
																			final ILayer secondLayer,
																			final CoordinateReferenceSystem secondCRS,
																			final String targetName,
																			final Class<? extends Geometry> targetGeometryClass,
																			final Filter filterInFirstLayer,
																			final Filter filterInSecondLayer) {

		IIntersectInNewLayerParameters params = new IntersectInNewLayerParameters(firstLayer, firstCRS, secondLayer,
					secondCRS, targetName, targetGeometryClass, filterInFirstLayer, filterInSecondLayer);

		return params;
	}

	/**
	 * New instance of clip parameters
	 * 
	 * @param usingLayer
	 * @param clipSourceLayer
	 * @param targetLayer
	 * @param usingFilter
	 * @param clipSourceFilter
	 * 
	 * @return IClipParameters
	 */
	public final static IClipInExistentLayerParameters createClipParameters(final ILayer usingLayer,
																			final CoordinateReferenceSystem usingCRS,
																			final ILayer clipSourceLayer,
																			final CoordinateReferenceSystem clipSourceCRS,
																			final ILayer targetLayer,
																			final CoordinateReferenceSystem targetCRS,
																			final Filter usingFilter,
																			final Filter clipSourceFilter) {

		IClipInExistentLayerParameters params = new ClipInExistentLayerParameters(usingLayer, usingCRS,
					clipSourceLayer, clipSourceCRS, targetLayer, targetCRS, usingFilter, clipSourceFilter);

		return params;

	}

	/**
	 * Parameters required to make a clip on new layer.
	 * 
	 * @param usingLayer
	 * @param clipSourceLayer
	 * @param targetFeatureType
	 * @param usingFilter
	 * @param clipSourceFilter
	 * @return
	 */
	public final static IClipInNewLayerParameters createClipParameters(	final ILayer usingLayer,
																		final CoordinateReferenceSystem usingCRS,
																		final ILayer clipSourceLayer,
																		final CoordinateReferenceSystem clipSourceCRS,
																		final SimpleFeatureType targetFeatureType,
																		final Filter usingFilter,
																		final Filter clipSourceFilter) {

		IClipInNewLayerParameters params = new ClipInNewLayerParameters(usingLayer, usingCRS, clipSourceLayer,
					clipSourceCRS, targetFeatureType, usingFilter, clipSourceFilter);

		return params;
	}

	/**
	 * Parameters required to make a spatial join on existent layer.
	 * 
	 * @param firstLayer
	 * @param firstCRS
	 * @param secondLayer
	 * @param secondCRS
	 * @param spatialRelation
	 * @param mapCrs
	 * @param targetLayer
	 * @param targetCRS
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 * @param selection
	 * @return
	 */
	public static ISpatialJoinInExistentLayerParameters createSpatialJoinParameters(final ILayer firstLayer,
																					final CoordinateReferenceSystem firstCRS,
																					final ILayer secondLayer,
																					final CoordinateReferenceSystem secondCRS,
																					final SpatialRelation spatialRelation,
																					final CoordinateReferenceSystem mapCrs,
																					final ILayer targetLayer,
																					final CoordinateReferenceSystem targetCRS,
																					final Filter filterInFirstLayer,
																					final Filter filterInSecondLayer,
																					final Boolean selection) {

		ISpatialJoinInExistentLayerParameters params = new SpatialJoinInExistentLayerParameters(firstLayer, firstCRS,
					secondLayer, secondCRS, spatialRelation, mapCrs, targetLayer, targetCRS, filterInFirstLayer,
					filterInSecondLayer, selection);
		return params;
	}

	/**
	 * Parameters required to make spatial join on new layer.
	 * 
	 * @param firstLayer
	 * @param firstCRS
	 * @param secondLayer
	 * @param secondCRS
	 * @param spatialRelation
	 * @param mapCrs
	 * @param targetLayerName
	 * @param targetGeometryClass
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 * @param selection
	 * @return
	 */
	public static ISpatialJoinInNewLayerParameters createSpatialJoinParameters(	final ILayer firstLayer,
																				final CoordinateReferenceSystem firstCRS,
																				final ILayer secondLayer,
																				final CoordinateReferenceSystem secondCRS,
																				final SpatialRelation spatialRelation,
																				final CoordinateReferenceSystem mapCrs,
																				final String targetLayerName,
																				final Class<? extends Geometry> targetGeometryClass,
																				final Filter filterInFirstLayer,
																				final Filter filterInSecondLayer,
																				final Boolean selection) {

		ISpatialJoinInNewLayerParameters params = new SpatialJoinInNewLayerParameters(firstLayer, firstCRS,
					secondLayer, secondCRS, spatialRelation, mapCrs, targetLayerName, targetGeometryClass,
					filterInFirstLayer, filterInSecondLayer, selection);
		return params;
	}

	/**
	 * Parameters required to make dissolve operation on existent layer.
	 * 
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param filter
	 * @param propDissolve
	 * @param mapCrs
	 * @param targetLayer
	 * @param targetCRS
	 * @return
	 */
	public static IDissolveInExistentLayerParameters createDissolveParameters(	final ILayer sourceLayer,
																				final CoordinateReferenceSystem sourceCRS,
																				final Filter filter,
																				final List<String> propDissolve,
																				final CoordinateReferenceSystem mapCrs,
																				final ILayer targetLayer,
																				final CoordinateReferenceSystem targetCRS) {

		IDissolveInExistentLayerParameters params = new DissolveInExistentLayerParameters(sourceLayer, sourceCRS,
					filter, propDissolve, mapCrs, targetLayer, targetCRS);
		return params;
	}

	/**
	 * Parameters required to make dissolve operation on new layer.
	 * 
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param filter
	 * @param propDissolve
	 * @param mapCrs
	 * @param targetLayerName
	 * @param targetGeomClass
	 * @return
	 */
	public static IDissolveInNewLayerParameters createDissolveParameters(	final ILayer sourceLayer,
																			final CoordinateReferenceSystem sourceCRS,
																			final Filter filter,
																			final List<String> propDissolve,
																			final CoordinateReferenceSystem mapCrs,
																			final String targetLayerName,
																			final Class<? extends Geometry> targetGeomClass) {

		IDissolveInNewLayerParameters params = new DissolveInNewLayerParameters(sourceLayer, sourceCRS, filter,
					propDissolve, mapCrs, targetLayerName, targetGeomClass);
		return params;
	}

	/**
	 * Parameters required to make polygon to line operation on new layer.
	 * 
	 * @param filter
	 * @param mapCrs
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param layerName
	 * @param targetClass
	 * @param boolean1
	 * @return
	 */
	public static IPolygonToLineInNewLayerParameters createPolygonToLineParameters(	final Filter filter,
																					final CoordinateReferenceSystem mapCrs,
																					final ILayer sourceLayer,
																					final CoordinateReferenceSystem sourceCRS,
																					final String layerName,
																					final Class<? extends Geometry> targetClass,
																					final Boolean explode) {

		IPolygonToLineInNewLayerParameters params = new PolygonToLineInNewLayerParameters(filter, mapCrs, sourceLayer,
					sourceCRS, layerName, targetClass, explode);

		return params;
	}

	/**
	 * Parameters required to make polygon to line operation on existent layer.
	 * 
	 * @param filter
	 * @param mapCrs
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param targetLayer
	 * @param targetCRS
	 * @return
	 */
	public static IPolygonToLineInExistentLayerParameters createPolygonToLineParameters(final Filter filter,
																						final CoordinateReferenceSystem mapCrs,
																						final ILayer sourceLayer,
																						final CoordinateReferenceSystem sourceCRS,
																						final ILayer targetLayer,
																						final CoordinateReferenceSystem targetCRS,
																						final Boolean explode) {

		IPolygonToLineInExistentLayerParameters params = new PolygonToLineInExistentLayerParameters(filter, mapCrs,
					sourceLayer, sourceCRS, targetLayer, targetCRS, explode);

		return params;
	}

	/**
	 * Parameters required to make the split operation on existent layer.
	 * 
	 * @param firstLayer
	 * @param firstCRS
	 * @param secondLayer
	 * @param secondCRS
	 * @param targetLayer
	 * @param targetCRS
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 * @return
	 */
	public static ISplitInExistentLayerParameters createSplitParameters(final ILayer firstLayer,
																		final CoordinateReferenceSystem firstCRS,
																		final ILayer secondLayer,
																		final CoordinateReferenceSystem secondCRS,
																		final ILayer targetLayer,
																		final CoordinateReferenceSystem targetCRS,
																		final Filter filterInFirstLayer,
																		final Filter filterInSecondLayer) {

		ISplitInExistentLayerParameters params = new SplitInExistentLayerParameters(firstLayer, firstCRS, secondLayer,
					secondCRS, targetLayer, targetCRS, filterInFirstLayer, filterInSecondLayer);

		return params;

	}

	/**
	 * Parameters required to make the split operation on new layer.
	 * 
	 * @param firstLayer
	 * @param firstCRS
	 * @param secondLayer
	 * @param secondCRS
	 * @param targetName
	 * @param targetGeometryClass
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 * @return
	 */
	public static ISplitInNewLayerParameters createSplitParameters(	final ILayer firstLayer,
																	final CoordinateReferenceSystem firstCRS,
																	final ILayer secondLayer,
																	final CoordinateReferenceSystem secondCRS,
																	final String targetName,
																	final Class<? extends Geometry> targetGeometryClass,
																	final Filter filterInFirstLayer,
																	final Filter filterInSecondLayer) {

		ISplitInNewLayerParameters params = new SplitInNewLayerParameters(firstLayer, firstCRS, secondLayer, secondCRS,
					targetName, targetGeometryClass, filterInFirstLayer, filterInSecondLayer);

		return params;
	}

	/**
	 * Parameters required for doing fill operation on existent layer.
	 * 
	 * @param firstLayer
	 * @param firstLayerCRS
	 * @param secondLayer
	 * @param secondLayerCRS
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 * @param targetLayer
	 * @param targetCRS
	 * @return
	 */
	public static IFillInExistentLayerParameters createFillParameters(	final ILayer firstLayer,
																		final CoordinateReferenceSystem firstLayerCRS,
																		final ILayer secondLayer,
																		final CoordinateReferenceSystem secondLayerCRS,
																		final Filter filterInFirstLayer,
																		final Filter filterInSecondLayer,
																		final ILayer targetLayer,
																		final CoordinateReferenceSystem targetCRS,
																		final boolean copySourceFeatures) {

		IFillInExistentLayerParameters params = new FillInExistentLayerParameters(firstLayer, firstLayerCRS,
					secondLayer, secondLayerCRS, filterInFirstLayer, filterInSecondLayer, targetLayer, targetCRS, copySourceFeatures);

		return params;

	}

	/**
	 * Parameters required for doing fill operation on a new layer.
	 * 
	 * @param firstLayer
	 * @param firstLayerCRS
	 * @param secondLayer
	 * @param secondLayerCRS
	 * @param filterInFirstLayer
	 * @param filterInSecondLayer
	 * @param targetName
	 * @param targetGeometryClass
	 * @return
	 */
	public static IFillInNewLayerParameters createFillParameters(	final ILayer firstLayer,
																	final CoordinateReferenceSystem firstLayerCRS,
																	final ILayer secondLayer,
																	final CoordinateReferenceSystem secondLayerCRS,
																	final Filter filterInFirstLayer,
																	final Filter filterInSecondLayer,
																	final String targetName,
																	final Class<? extends Geometry> targetGeometryClass,
																	final boolean copySourceFeatures) {

		IFillInNewLayerParameters params = new FillInNewLayerParameters(firstLayer, firstLayerCRS, secondLayer,
					secondLayerCRS, filterInFirstLayer, filterInSecondLayer, targetName, targetGeometryClass, copySourceFeatures);

		return params;
	}

	/**
	 * Parameters required for doing hole operation on existent layer.
	 * 
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param usingLayer
	 * @param usingCRS
	 * @param sourceFilter
	 * @param usingFilter
	 * @param targetLayer
	 * @param targetCRS
	 * @return
	 */
	public static IHoleInExistentLayer createHoleParameters(final ILayer sourceLayer,
															final CoordinateReferenceSystem sourceCRS,
															final ILayer usingLayer,
															final CoordinateReferenceSystem usingCRS,
															final Filter sourceFilter,
															final Filter usingFilter,
															final ILayer targetLayer,
															final CoordinateReferenceSystem targetCRS) {

		IHoleInExistentLayer params = new HoleInExistentLayerParameters(sourceLayer, sourceCRS, usingLayer, usingCRS,
					sourceFilter, usingFilter, targetLayer, targetCRS);

		return params;
	}

	/**
	 * Parameters required for doing hole operation on new layer.
	 * 
	 * @param sourceLayer
	 * @param sourceCRS
	 * @param usingLayer
	 * @param usingCRS
	 * @param sourceFilter
	 * @param usingFilter
	 * @param targetFeatureType
	 * @return
	 */
	public static IHoleInNewLayer createHoleParameters(	final ILayer sourceLayer,
														final CoordinateReferenceSystem sourceCRS,
														final ILayer usingLayer,
														final CoordinateReferenceSystem usingCRS,
														final Filter sourceFilter,
														final Filter usingFilter,
														final SimpleFeatureType targetFeatureType) {

		IHoleInNewLayer params = new HoleInNewLayerParameters(sourceLayer, sourceCRS, usingLayer, usingCRS,
					sourceFilter, usingFilter, targetFeatureType);

		return params;
	}

}
