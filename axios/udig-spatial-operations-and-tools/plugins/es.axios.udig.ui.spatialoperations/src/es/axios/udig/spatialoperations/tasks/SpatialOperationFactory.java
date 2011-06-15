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
package es.axios.udig.spatialoperations.tasks;

import java.util.List;

import javax.measure.unit.Unit;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.geotools.util.FeatureTypeUnionBuilder;

/**
 * Creates the spatial operation tasks
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
public class SpatialOperationFactory {

	/**
	 * A new instance of dissolve task
	 * 
	 * @param featureSource
	 * @param filter
	 * @param dissolveProperty
	 * @param mapCrs
	 * @param targetStore
	 * @param targetCrs
	 * @param sourceCrs
	 * @return {@link IDissolveTask} object is the return value of task
	 * 
	 * @throws SpatialDataProcessException
	 */
	public static IDissolveTask createDissolve(	final SimpleFeatureSource 		featureSource,
												final Filter 					filter,
												final List<String> 				dissolveProperty,
												final CoordinateReferenceSystem mapCrs,
												final SimpleFeatureStore 		targetStore,
												final CoordinateReferenceSystem sourceCrs,
												final CoordinateReferenceSystem targetCrs)
		throws SpatialOperationException {

		IDissolveTask task = DissolveTask.createProcess(featureSource, filter, dissolveProperty, mapCrs, targetStore,
					sourceCrs, targetCrs);

		return task;
	}

	/**
	 * New instance of {@link ISpatialJoinTask} to create the join result on the
	 * target layer.
	 * 
	 * @param firstSource
	 * @param secondSource
	 * @param spatialRelation
	 * @param mapCrs
	 * @param target
	 * @return {@link ISpatialJoinTask}
	 * @throws SpatialOperationException
	 */
	public static ISpatialJoinTask<Object> createSpatialJoin(	final FeatureCollection<SimpleFeatureType, SimpleFeature> firstSource,
																final FeatureCollection<SimpleFeatureType, SimpleFeature> secondSource,
																final SpatialRelation spatialRelation,
																final CoordinateReferenceSystem mapCrs,
																final SimpleFeatureStore target,
																final CoordinateReferenceSystem sourceCrs,
																final CoordinateReferenceSystem secondCrs,
																final CoordinateReferenceSystem targetCrs)
		throws SpatialOperationException {

		ISpatialJoinTask<Object> task = SpatialJoinTask.createProcess(firstSource, secondSource, spatialRelation,
					mapCrs, target, sourceCrs, secondCrs, targetCrs);

		return task;
	}

	/**
	 * New instance of {@link ISpatialJoinTask} for feature selection
	 * 
	 * @param firstSource
	 * @param secondSource
	 * @param spatialRelation
	 * @param mapCrs
	 * @return {@link ISpatialJoinTask}
	 * @throws SpatialOperationException
	 */
	public static ISpatialJoinTask<Object> createSpatialJoinSelection(	final FeatureCollection<SimpleFeatureType, SimpleFeature> firstSource,
																		final FeatureCollection<SimpleFeatureType, SimpleFeature> secondSource,
																		final SpatialRelation spatialRelation,
																		final CoordinateReferenceSystem mapCrs,
																		final CoordinateReferenceSystem firstCRS,
																		final CoordinateReferenceSystem secondCRS)
		throws SpatialOperationException {

		ISpatialJoinTask<Object> task = SpatialJoinTask.createSelectionProcess(firstSource, secondSource,
					spatialRelation, mapCrs, firstCRS, secondCRS);

		return task;
	}

	/**
	 * A new instance of buffer task.
	 * 
	 * @param source
	 * @param targetStore
	 * @param crs
	 * @param width
	 * @param unitsOfMeasure
	 * @param mergeGeometries
	 * @param quadrantSegments
	 * @param endCapStyle
	 * @param targetCRS
	 * @param sourceCRS
	 * @return
	 */
	public static IBufferTask createBuffer(	final FeatureCollection<SimpleFeatureType, SimpleFeature> source,
											final SimpleFeatureStore targetStore,
											final CoordinateReferenceSystem crs,
											final Double width,
											final Unit<?> unitsOfMeasure,
											final boolean mergeGeometries,
											final Integer quadrantSegments,
											final IBufferTask.CapStyle endCapStyle,
											final CoordinateReferenceSystem sourceCRS,
											final CoordinateReferenceSystem targetCRS) {

		IBufferTask task = BufferTask.createProcess(source, targetStore, crs, width, unitsOfMeasure, mergeGeometries,
					quadrantSegments, endCapStyle, sourceCRS, targetCRS);

		return task;
	}

	/**
	 * A new instance of of clip task.
	 * 
	 * @param usingFeatures
	 * @param clipSourceFeatures
	 * @param targetStore
	 * @param mapCrs
	 * @param usingCrs
	 * @param clipSourceCrs
	 * @param targetGeomAttrType
	 * @param isCreatingNewLayer
	 * @param clipSourceName
	 * @param targetLayerName
	 * @param targetCrs
	 * @param clipSourceFeatureType
	 * @return
	 */
	public static IClipTask createClip(	final FeatureCollection<SimpleFeatureType, SimpleFeature> usingFeatures,
										final FeatureCollection<SimpleFeatureType, SimpleFeature> clipSourceFeatures,
										final SimpleFeatureStore targetStore,
										final CoordinateReferenceSystem mapCrs,
										final CoordinateReferenceSystem usingCrs,
										final CoordinateReferenceSystem clipSourceCrs,
										final boolean isCreatingNewLayer,
										final String clipSourceName,
										final String targetLayerName,
										final CoordinateReferenceSystem targetCrs) {

		IClipTask task = ClipTask.createProcess(usingFeatures, clipSourceFeatures, targetStore, mapCrs, usingCrs,
					clipSourceCrs, isCreatingNewLayer, clipSourceName, targetLayerName, targetCrs);
		return task;
	}

	/**
	 * A new instance of of intersect task.
	 * 
	 * @param targetStore
	 * @param secondLayer
	 * @param featuresInFirstLayer
	 * @param featuresInSecondLayer
	 * @param firstLayerCrs
	 * @param mapCrs
	 * @param isCreatingNewLayer
	 * @param featureUnionBuilder
	 * @param secondLayerCrs
	 * @return
	 */
	public static IIntersectTask createIntersect(	final SimpleFeatureStore targetStore,
													final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer,
													final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer,
													final CoordinateReferenceSystem firstLayerCrs,
													final CoordinateReferenceSystem mapCrs,
													final boolean isCreatingNewLayer,
													final FeatureTypeUnionBuilder featureUnionBuilder,
													final CoordinateReferenceSystem secondLayerCrs) {

		IIntersectTask task = IntersectTask.createProcess(targetStore, featuresInFirstLayer, featuresInSecondLayer,
					firstLayerCrs, mapCrs, isCreatingNewLayer, featureUnionBuilder, secondLayerCrs);
		return task;
	}

	/**
	 * A new instance of polygon to line task.
	 * 
	 * @param targetStore
	 * @param featuresFromSource
	 * @param sourceLayerCrs
	 * @param explode
	 * @return
	 */
	public static IPolygonToLineTask createPolygonToLine(	final SimpleFeatureStore targetStore,
															final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresFromSource,
															final CoordinateReferenceSystem sourceLayerCrs,
															final Boolean explode) {

		IPolygonToLineTask task = PolygonToLineTask.createProcess(targetStore, featuresFromSource, sourceLayerCrs,
					explode);

		return task;
	}

	/**
	 * A new instance of split task.
	 * 
	 * @param targetStore
	 * @param featuresInFirstLayer
	 * @param featuresInSecondLayer
	 * @param firstLayerCrs
	 * @param mapCrs
	 * @param isCreatingNewLayer
	 * @param secondLayerCrs
	 * @param targetCrs
	 * @param layerToSplitName
	 * @param targetLayerName
	 * @return
	 */
	public static ISplitTask createSplit(	final SimpleFeatureStore targetStore,
											final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer,
											final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer,
											final CoordinateReferenceSystem firstLayerCrs,
											final CoordinateReferenceSystem mapCrs,
											final boolean isCreatingNewLayer,
											final CoordinateReferenceSystem secondLayerCrs,
											final CoordinateReferenceSystem targetCrs,
											final String layerToSplitName,
											final String targetLayerName) {

		ISplitTask task = SplitTask.createProccess(targetStore, featuresInFirstLayer, featuresInSecondLayer,
					 mapCrs, isCreatingNewLayer, layerToSplitName,
					targetLayerName);
		return task;
	}

	/**
	 * A new instance of Fill task.
	 * 
	 * @param targetStore
	 * @param featuresInFirstLayer
	 * @param featuresInSecondLayer
	 * @param firstLayerCrs
	 * @param mapCrs
	 * @param isCreatingNewLayer
	 * @param secondLayerCrs
	 * @param targetCrs
	 * @return
	 */
	public static IFillTask createFill(	final SimpleFeatureStore targetStore,
										final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInFirstLayer,
										final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresInSecondLayer,
										final CoordinateReferenceSystem firstLayerCrs,
										final CoordinateReferenceSystem mapCrs,
										final CoordinateReferenceSystem secondLayerCrs,
										final CoordinateReferenceSystem targetCrs,
										final boolean isCopySourceFeatures) {

		IFillTask task = FillTask.createProcess(targetStore, featuresInFirstLayer, featuresInSecondLayer,
					firstLayerCrs, mapCrs, secondLayerCrs, targetCrs,  isCopySourceFeatures);
		return task;
	}

	/**
	 * A new instance of hole task.
	 * 
	 * @param sourceFeatures
	 * @param usingFeatures
	 * @param targetStore
	 * @param mapCrs
	 * @param targetCrs
	 * @param sourceCrs
	 * @param usingCrs
	 * @param isCreatingNewLayer
	 * @param sourceName
	 * @param targetLayerName
	 * @return
	 */
	public static IHoleTask createHole(	final FeatureCollection<SimpleFeatureType, SimpleFeature> sourceFeatures,
										final FeatureCollection<SimpleFeatureType, SimpleFeature> usingFeatures,
										final SimpleFeatureStore		targetStore,
										final CoordinateReferenceSystem mapCrs,
										final CoordinateReferenceSystem targetCrs,
										final CoordinateReferenceSystem sourceCrs,
										final CoordinateReferenceSystem usingCrs,
										final boolean 					isCreatingNewLayer,
										final String 					sourceName,
										final String 					targetLayerName) {

		IHoleTask task = HoleTask.createProcess(sourceFeatures, usingFeatures, targetStore, mapCrs, targetCrs,
					sourceCrs, usingCrs, isCreatingNewLayer, sourceName, targetLayerName);

		return task;
	}
}
