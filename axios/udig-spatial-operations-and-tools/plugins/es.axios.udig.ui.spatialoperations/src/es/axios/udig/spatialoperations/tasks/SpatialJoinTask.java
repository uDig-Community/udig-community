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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.geotools.util.FeatureUtil;
import es.axios.geotools.util.GeoToolsUtils;

/**
 * Spatial Join Process
 * <p>
 * Generates a new layer with the features of layer A and B which fulfill with
 * the spatial relation.
 * </p>
 * <p>
 * <b> select A.*, B.* from A join B on A.Geometry <Relation> B.Geometry </b>
 * <p>
 * <p>
 * 
 * <pre>
 * Relation is one of: &lt;b&gt;intersect&lt;/b&gt;, 
 *                     &lt;b&gt;overlaps&lt;/b&gt;, 
 *                     &lt;b&gt;contains&lt;/b&gt;, 
 *                     &lt;b&gt;covers&lt;/b&gt;, 
 *                     &lt;b&gt;is-cover-by&lt;/b&gt;, 
 *                     &lt;b&gt;crosses&lt;/b&gt;, 
 *                     &lt;b&gt;disjoint&lt;/b&gt;, 
 *                     &lt;b&gt;equals&lt;/b&gt;, 
 *                     &lt;b&gt;overlap&lt;/b&gt;, 
 *                     &lt;b&gt;within&lt;/b&gt;, 
 *                     &lt;b&gt;is-within-distance&lt;/b&gt;.  
 *                     &lt;b&gt;touches&lt;/b&gt;,
 * 
 * </pre>
 * 
 * The A source is only used as reference to the spatial relation, the B
 * sources' features that accomplish the relation will be copied in the result
 * without changes in its geometries.
 * </p>
 * <p>
 * </p>
 * Note: the alfanumeric data manipulation is not solved in this algorithm it
 * require a mapping between the source and target. This mapping should be a new
 * parameter. </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
final class SpatialJoinTask extends AbstractSpatialOperationTask<Object> implements ISpatialJoinTask<Object> {

	private static final Logger	LOGGER	= Logger.getLogger(SpatialJoinTask.class.getName());

	/**
	 * two mode to execute the join task: copy into target layer or select into
	 * source layer
	 */
	private enum Mode {
		copy, selection
	};

	private final FeatureCollection<SimpleFeatureType, SimpleFeature>	firstSource;
	private final FeatureCollection<SimpleFeatureType, SimpleFeature>	referenceFeatures;
	private final CoordinateReferenceSystem								firstSourceCrs;
	private final CoordinateReferenceSystem								secondSourceCrs;
	private final CoordinateReferenceSystem								mapCrs;
	private CoordinateReferenceSystem									targetCrs;
	private SpatialRelation												spatialRelation	= null;
	private Double														distance		= null;
	private Mode														processMode		= null;
	private Set<FeatureId>												featureIds		= new HashSet<FeatureId>();

	/**
	 * new instance of SpatialJoinGeometryProcess
	 * 
	 * @param firstSource
	 * @param secondSource
	 * @param crs
	 *            coordinate reference used to do the spatial relation
	 * @param target
	 * @throws SpatialDataProcessException
	 */
	protected SpatialJoinTask(	final FeatureCollection<SimpleFeatureType, SimpleFeature> firstSource,
								final FeatureCollection<SimpleFeatureType, SimpleFeature> secondSource,
								final CoordinateReferenceSystem crs,
								final SimpleFeatureStore 		target,
								final CoordinateReferenceSystem sourceCrs,
								final CoordinateReferenceSystem secondCrs,
								final CoordinateReferenceSystem targetCrs) throws SpatialOperationException {

		assert firstSource != null : "firstSource cannot be null"; //$NON-NLS-1$
		assert secondSource != null : "secondSource cannot be null"; //$NON-NLS-1$
		assert target != null : "target cannot be null"; //$NON-NLS-1$
		assert crs != null : "crs cannot be null"; //$NON-NLS-1$

		this.firstSource = firstSource;
		this.referenceFeatures = secondSource;
		this.targetStore = target;
		this.mapCrs = crs;

		// initialize the coordinate reference of sources
		this.firstSourceCrs = sourceCrs;
		this.secondSourceCrs = secondCrs;
		this.targetCrs = targetCrs;

		assert this.firstSourceCrs != null;
		assert this.secondSourceCrs != null;
		assert this.targetCrs != null;
	}

	/**
	 * new instance of SpatialJoinGeometryProcess
	 * 
	 * @param firstSource
	 * @param secondSource
	 * @param mapCrs
	 * @param firstCRS
	 * @param secondCRS
	 * @throws SpatialDataProcessException
	 */
	protected SpatialJoinTask(	final FeatureCollection<SimpleFeatureType, SimpleFeature> firstSource,
								final FeatureCollection<SimpleFeatureType, SimpleFeature> secondSource,
								final CoordinateReferenceSystem mapCrs,
								final CoordinateReferenceSystem firstCRS,
								final CoordinateReferenceSystem secondCRS) throws SpatialOperationException {

		assert firstSource != null : "firstSource cannot be null"; //$NON-NLS-1$
		assert secondSource != null : "secondSource cannot be null"; //$NON-NLS-1$
		assert mapCrs != null : "mapCrs cannot be null"; //$NON-NLS-1$
		assert firstCRS != null : "firstCRS cannot be null";//$NON-NLS-1$
		assert secondCRS != null : "secondCRS cannot be null";//$NON-NLS-1$

		this.firstSource = firstSource;
		this.referenceFeatures = secondSource;
		this.mapCrs = mapCrs;

		// initialize the coordinate reference of sources
		this.firstSourceCrs = firstCRS;
		this.secondSourceCrs = secondCRS;
		this.targetStore = null;

		assert this.firstSourceCrs != null;
		assert this.secondSourceCrs != null;

	}

	/**
	 * Creates an instance of SpatialJoinGeometryProcess
	 * 
	 * @param firstSource
	 * @param secondSource
	 * @param spatialRelation
	 * @param mapCrs
	 *            coordinate reference used to do the spatial relation
	 * @param target
	 * @return SpatialJoinGeometryProcess
	 * @throws SpatialDataProcessException
	 */
	public static SpatialJoinTask createProcess(final FeatureCollection<SimpleFeatureType, SimpleFeature> firstSource,
												final FeatureCollection<SimpleFeatureType, SimpleFeature> secondSource,
												final SpatialRelation spatialRelation,
												final CoordinateReferenceSystem mapCrs,
												final SimpleFeatureStore target,
												final CoordinateReferenceSystem sourceCrs,
												final CoordinateReferenceSystem secondCrs,
												final CoordinateReferenceSystem targetCrs)
		throws SpatialOperationException {

		assert spatialRelation != null : "spatialRelation cannot be null"; //$NON-NLS-1$

		SpatialJoinTask process = new SpatialJoinTask(firstSource, secondSource, mapCrs, target, sourceCrs, secondCrs,
					targetCrs);
		process.spatialRelation = spatialRelation;

		process.processMode = Mode.copy;

		return process;
	}

	/**
	 * 
	 * @param firstSource
	 * @param secondSource
	 * @param spatialRelation
	 * @param mapCrs
	 * @param target
	 * @return
	 * @throws SpatialDataProcessException
	 */
	public static SpatialJoinTask createSelectionProcess(	final FeatureCollection<SimpleFeatureType, SimpleFeature> firstSource,
															final FeatureCollection<SimpleFeatureType, SimpleFeature> secondSource,
															final SpatialRelation spatialRelation,
															final CoordinateReferenceSystem mapCrs,
															final CoordinateReferenceSystem firstCRS,
															final CoordinateReferenceSystem secondCRS)
		throws SpatialOperationException {

		assert spatialRelation != null : "spatialRelation cannot be null"; //$NON-NLS-1$

		SpatialJoinTask process = new SpatialJoinTask(firstSource, secondSource, mapCrs, firstCRS, secondCRS);
		process.spatialRelation = spatialRelation;

		process.processMode = Mode.selection;

		return process;
	}

	/**
	 * Creates a process that add in target those features which are in the
	 * specified distance.
	 * 
	 * @param firstSource
	 * @param secondSource
	 * @param distance
	 * @param crs
	 *            coordinate reference used to do the spatial relation
	 * @param target
	 * @return SpatialJoinGeometryProcess
	 * @throws SpatialDataProcessException
	 */
	public static SpatialJoinTask createDistanceRelationProcess(final FeatureCollection<SimpleFeatureType, SimpleFeature> firstSource,
																final FeatureCollection<SimpleFeatureType, SimpleFeature> secondSource,
																final Double distance,
																final CoordinateReferenceSystem crs,
																final SimpleFeatureStore target,
																final CoordinateReferenceSystem sourceCrs,
																final CoordinateReferenceSystem secondCrs,
																final CoordinateReferenceSystem targetCrs)
		throws SpatialOperationException {

		assert distance != null : "distance cannot be null"; //$NON-NLS-1$

		SpatialJoinTask process = new SpatialJoinTask(firstSource, secondSource, crs, target, sourceCrs, secondCrs, targetCrs);
		
		process.distance = distance;

		return process;
	}

	@Override
	public Object call() throws Exception {

		perform();

		Object result = null;
		if (Mode.copy.equals(this.processMode)) {

			result = getResult();

		} else if (Mode.selection.equals(this.processMode)) {

			result = getSelectionFilter();

		} else {
			assert false : "must set the process Mode"; //$NON-NLS-1$
		}

		return result;
	}

	/**
	 * Evaluates the spatial relation for each features present in sources and
	 * adds in the target those features which accomplish it.
	 */
	@Override
	protected void perform() throws SpatialOperationException {

		FeatureIterator<SimpleFeature> iterFirstSource = null;

		try {

			iterFirstSource = this.firstSource.features();
			while (iterFirstSource.hasNext()) {

				SimpleFeature featureInFirst = iterFirstSource.next();
				Geometry firstGeom = (Geometry) featureInFirst.getDefaultGeometry();

				boolean existRelation = false;

				if (SpatialRelation.Disjoint.equals(this.spatialRelation)) {

					existRelation = existDisjoint(firstGeom);
				} else {

					existRelation = existeRelation(this.spatialRelation, firstGeom);
				}

				if (existRelation) {
					if (Mode.selection.equals(this.processMode)) {

						addToFeatureList(featureInFirst);
					} else if (Mode.copy.equals(this.processMode)) {

						insertIntoStore(this.targetStore, featureInFirst);
					}
				}

			}
		} catch (Exception e) {
			throw new SpatialOperationException(e);
		} finally {
			if (iterFirstSource != null) {
				iterFirstSource.close();
			}

		}

	}

	/**
	 * Stores the feature Id
	 * 
	 * @param featureInFirst
	 *            The feature of the source layer.
	 */
	private void addToFeatureList(SimpleFeature featureInFirst) {

		FeatureId fid = FILTER_FACTORY.featureId(featureInFirst.getID());
		featureIds.add(fid);
	}

	/**
	 * Feature from source must fulfill geometry disjoint with all features of
	 * reference features.
	 * 
	 * @param firstGeom
	 * @return true if the spatial relation is true, false in other case
	 * @throws SpatialOperationException
	 */
	private boolean existDisjoint(final Geometry firstGeom) throws SpatialOperationException {

		FeatureIterator<SimpleFeature> iterSecondSource = null;
		try {
			iterSecondSource = this.referenceFeatures.features();
			while (iterSecondSource.hasNext()) {

				SimpleFeature featureInSecond = iterSecondSource.next();
				Geometry secondGeom = (Geometry) featureInSecond.getDefaultGeometry();

				if (!existGeometryRelation(SpatialRelation.Disjoint, firstGeom, this.firstSourceCrs, secondGeom,
							this.secondSourceCrs, this.mapCrs)) {

					return false;
				}

			}
			return true;

		} finally {
			if (iterSecondSource != null) {
				iterSecondSource.close();
			}
		}

	}

	/**
	 * Feature from source must fulfill geometry relation at least with one
	 * feature from reference features. This method do not considerate the
	 * disjoint spatial relation. To disjoint this task use the method
	 * {@link #existDisjoint(Geometry)}.
	 * 
	 * 
	 * @param relation
	 * @param firstGeom
	 * 
	 * @return True if exist relation, false in other case
	 * @throws SpatialDataProcessException
	 */
	private boolean existeRelation(final SpatialRelation relation, final Geometry firstGeom)
		throws SpatialOperationException {

		assert !relation.equals(SpatialRelation.Disjoint) : "illegal argument: disjoint is not handed by this method"; //$NON-NLS-1$

		FeatureIterator<SimpleFeature> iterSecondSource = null;
		try {
			iterSecondSource = this.referenceFeatures.features();

			while (iterSecondSource.hasNext()) {

				SimpleFeature featureInSecond = iterSecondSource.next();
				Geometry referenceGeom = (Geometry) featureInSecond.getDefaultGeometry();

				if (existGeometryRelation(relation, firstGeom, this.firstSourceCrs, referenceGeom,
							this.secondSourceCrs, this.mapCrs)) {

					return true;
				}
			}
			return false;

		} catch (Exception e) {
			throw makeException(e);
		} finally {
			if (iterSecondSource != null) {
				iterSecondSource.close();
			}
		}
	}

	/**
	 * Inserts a new feature using the geometry presents in the second feature.
	 * This method do not copies alphanumeric data to the target layer. TODO a
	 * mapping to specify what alphanumeric data, this process, must copy.
	 * 
	 * @param target
	 * @param featureInFirst
	 * 
	 * @throws SpatialDataProcessException
	 *             if adds features fail
	 * @throws SpatialOperationException
	 */
	private void insertIntoStore(	final FeatureStore<SimpleFeatureType, SimpleFeature> target,
									final SimpleFeature featureInFirst) throws SpatialOperationException {

		// creates the new features
		SimpleFeature newSecond = createFeatureUsing(featureInFirst, this.firstSourceCrs, this.targetCrs);

		insert(target, newSecond);
	}

	/**
	 * Creates the new feature projecting the featue's geometry on target.
	 * 
	 * @param feature
	 * @param sourceCrs
	 * @param targetCrs
	 * @param geomOnTargetCrs
	 * @return Feature
	 * @throws SpatialDataProcessException
	 */
	private SimpleFeature createFeatureUsing(	final SimpleFeature feature,
												final CoordinateReferenceSystem sourceCrs,
												final CoordinateReferenceSystem targetCrs)
		throws SpatialOperationException {

		try {
			// projects the geometry on target
			Geometry firstGeom = (Geometry) feature.getDefaultGeometry();

			Geometry geomOnTargetCrs = GeoToolsUtils.reproject(firstGeom, sourceCrs, targetCrs);

			// create the new feature using the projected geometry
			final SimpleFeatureType targetType = this.targetStore.getSchema();
			SimpleFeature newFeature = FeatureUtil.createFeatureUsing(feature, targetType, geomOnTargetCrs);
			return newFeature;

		} catch (Exception e) {
			throw createException(e);
		}
	}

	/**
	 * Evaluates the geometry relation between the first and second geometry,
	 * using the CRS specified.
	 * 
	 * @param relation
	 * @param firstGeom
	 * @param firstCrs
	 * @param secondGeom
	 * @param secondCrs
	 * @param crs
	 * @return
	 * @throws SpatialOperationException
	 * @throws SpatialDataProcessException
	 */
	private boolean existGeometryRelation(	final SpatialRelation relation,
											final Geometry firstGeom,
											final CoordinateReferenceSystem firstCrs,
											final Geometry secondGeom,
											final CoordinateReferenceSystem secondCrs,
											final CoordinateReferenceSystem crs) throws SpatialOperationException {

		try {

			Geometry firstGeomOnCrs = GeoToolsUtils.reproject(firstGeom, firstCrs, crs);
			Geometry secondGeomOnCrs = GeoToolsUtils.reproject(secondGeom, secondCrs, crs);

			boolean exist = false;
			switch (relation) {
			case Intersects:

				exist = firstGeomOnCrs.intersects(secondGeomOnCrs);
				break;

			case Disjoint: // inverse intersects

				exist = firstGeomOnCrs.disjoint(secondGeomOnCrs);
				break;

			case Contains:

				exist = firstGeomOnCrs.contains(secondGeomOnCrs);
				break;

			case Within: // inverse contains

				exist = firstGeomOnCrs.within(secondGeomOnCrs);
				break;

			case IsCoverBy:

				exist = firstGeomOnCrs.coveredBy(secondGeomOnCrs);
				break;

			case Covers: // inverse coveredBy

				exist = firstGeomOnCrs.covers(secondGeomOnCrs);
				break;

			case Crosses:

				// do overlaps when geometries dimension is the same and also is
				// 2.
				if ((firstGeomOnCrs.getDimension() == secondGeomOnCrs.getDimension())
							&& firstGeomOnCrs.getDimension() == 2) {

					exist = firstGeomOnCrs.overlaps(secondGeomOnCrs);
				} else {

					exist = firstGeomOnCrs.crosses(secondGeomOnCrs);
				}

				break;

			case Equals:

				exist = firstGeomOnCrs.equals(secondGeomOnCrs);
				break;

			case Overlaps:

				exist = firstGeomOnCrs.overlaps(secondGeomOnCrs);
				break;

			case Touches:

				exist = firstGeomOnCrs.touches(secondGeomOnCrs);
				break;
			case IsWithinDistance:
				assert this.distance != null: "must set the distance for within distance relation";		 //$NON-NLS-1$
				exist = firstGeomOnCrs.isWithinDistance(secondGeomOnCrs, this.distance);
				break;

			default:
				assert false : "unsupported spatial relation"; //$NON-NLS-1$
				break;
			}
			return exist;

		} catch (Exception e) {
			throw makeException(e);
		}

	}

	/**
	 * Returns the target with the join result.
	 * 
	 * @return {@link FeatureStore} or {@link Id}
	 */
	@Override
	protected Object getResult() {

		Object result = null;
		if (Mode.copy.equals(this.processMode)) {
			result = this.targetStore;
		} else if (Mode.selection.equals(this.processMode)) {

			result = getSelectionFilter();
		}
		assert result != null : "spatial join without result !!"; //$NON-NLS-1$

		return result;
	}

	/**
	 * @return the selected features
	 */
	protected Id getSelectionFilter() {

		Id filter = null;
		filter = FILTER_FACTORY.id(featureIds);

		assert filter != null;

		return filter;
	}

}
