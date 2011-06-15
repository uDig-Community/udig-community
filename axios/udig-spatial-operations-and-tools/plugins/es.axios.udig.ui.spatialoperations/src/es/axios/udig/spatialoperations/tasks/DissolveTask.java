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

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.geotools.util.FeatureUtil;
import es.axios.geotools.util.GeoToolsUtils;
import es.axios.lib.geometry.util.GeometryUtil;

/**
 * Dissolve Process.
 * <p>
 * The dissolve process does the geometry union of that features which have
 * equals values in the property specified has "dissolve property". That
 * features, which have not equals values, will be in the resultant data set
 * without changes.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
final class DissolveTask extends AbstractSpatialOperationTask<SimpleFeatureStore> implements
			IDissolveTask {

	private final List<String>									dissolveProperties;
	private final CoordinateReferenceSystem						mapCrs;
	private CoordinateReferenceSystem							sourceCrs;
	private final CoordinateReferenceSystem						targetCrs;
	private final Class<? extends Geometry>						targetGeometryClass;
	private FeatureCollection<SimpleFeatureType, SimpleFeature>	sortedSource;

	/**
	 * new instance of DissolveProcess
	 * 
	 * @param source
	 * @param dissolveProperty
	 * @param mapCrs
	 *            coordinate reference used to do the spatial relation
	 * @param target
	 * @param targetCrs2
	 * @param sourceCrs2
	 */
	private DissolveTask(	final FeatureSource<SimpleFeatureType, SimpleFeature> source,
							final List<String> dissolveProperty,
							final CoordinateReferenceSystem mapCrs,
							final SimpleFeatureStore target,
							final CoordinateReferenceSystem sourceCrs,
							final CoordinateReferenceSystem targetCrs) {

		assert source != null : "illegal argument: source cannot be null"; //$NON-NLS-1$
		assert dissolveProperty != null : "illegal argument: dissolveProperty cannot be null"; //$NON-NLS-1$
		assert mapCrs != null : "illegal argumet: crs cannot be null"; //$NON-NLS-1$
		assert source != null : "illegal argument: source cannot be null"; //$NON-NLS-1$
		assert FeatureUtil.hasProperty(source.getSchema(), dissolveProperty) : "illegal argument: dissolveProperty " //$NON-NLS-1$
					+ dissolveProperty + " does not exist in source collection " + source.getSchema().getTypeName(); //$NON-NLS-1$

		assert sourceCrs != null : "can't be null"; //$NON-NLS-1$
		assert targetCrs != null : "can't be null"; //$NON-NLS-1$
		this.dissolveProperties = dissolveProperty;

		this.targetStore = target;
		this.targetGeometryClass = (Class<? extends Geometry>) this.targetStore.getSchema().getGeometryDescriptor()
					.getType().getBinding();

		this.mapCrs = mapCrs;

		this.sourceCrs = sourceCrs;
		this.targetCrs = targetCrs;

	}

	/**
	 * Creates the dissolve process using the filter to reduce the filter
	 * collection to dissolve.
	 * 
	 * @param source
	 * @param filter
	 * @param dissolveProperty
	 * @param mapCrs
	 * @param target
	 * @param targetCrs2
	 * @param sourceCrs2
	 * @return a new instance of DissolveProcess
	 * @throws SpatialDataProcessException
	 */
	public static IDissolveTask createProcess(	final FeatureSource<SimpleFeatureType, SimpleFeature> source,
												final Filter filter,
												final List<String> dissolveProperty,
												final CoordinateReferenceSystem mapCrs,
												final SimpleFeatureStore target,
												final CoordinateReferenceSystem sourceCrs,
												final CoordinateReferenceSystem targetCrs)
		throws SpatialOperationException {

		DissolveTask newProcess = new DissolveTask(source, dissolveProperty, mapCrs, target, sourceCrs, targetCrs);

		newProcess.sortedSource = newProcess
					.createFeaturesCollectionSorted(source, filter, dissolveProperty, sourceCrs);
		assert dissolveProperty.size() >= 1;
		assert checkSorted(newProcess.sortedSource, dissolveProperty.get(0));

		return newProcess;
	}

	/**
	 * Checks if the collection is order by the indeed property 
	 * @param sortedSource
	 * @param sortProperty
	 */
	private static boolean checkSorted(
			final FeatureCollection<SimpleFeatureType, SimpleFeature> sortedSource, 
			final String sortProperty) {
	
		Stack<Object> valueList = new Stack<Object>();
		
		FeatureIterator<SimpleFeature> iter = sortedSource.features();
		try{
			while(iter.hasNext()){
				
				SimpleFeature f = iter.next();
				Property property = f.getProperty(sortProperty);
				Object value = property.getValue();
				if( value == null) continue;
				
				// if new value is different of of group values
				if( valueList.isEmpty()||(!value.equals(valueList.peek()))){
					
					// add a the first of element of group values
					if(!valueList.contains(value)){

						valueList.push(value);

					} else {
						return false;
					}
					
				}
			}
			return true;
			
		} finally{
			iter.close();
		}
	}

	/**
	 * Does the union of that features with equals values in their dissolve
	 * properties
	 */
	@Override
	protected void perform() throws SpatialOperationException {

		if (this.sortedSource.isEmpty()) {
			return;
		}

		FeatureIterator<SimpleFeature> iter = null;
		try {
			SimpleFeature currentFeature, dissolveFeature;

			// insert the feature to dissolve
			iter = this.sortedSource.features();
			currentFeature = iter.next(); 
			dissolveFeature = insertIntoStore(this.targetStore, currentFeature);
			do {
				// iterates while the features set if it have equal property value
				StringBuffer groupPropValue = getGroupValues(currentFeature, this.dissolveProperties);

				boolean endOfGroup = false;
				while ( iter.hasNext()  && !endOfGroup) {

					currentFeature = iter.next();
					
					StringBuffer currentPropValue = getGroupValues(currentFeature, this.dissolveProperties);

					// if the current feature has not got the attribute value of this merge group then finish this merge feature and save it.
					if (currentPropValue.toString().equals(groupPropValue.toString())) {
						// dissolve the current feature within the same attributes
						dissolveFeature = makeGeometryUnion(
											dissolveFeature, currentFeature, 
											this.sourceCrs, this.mapCrs, this.targetCrs);
					} else {
						endOfGroup = true;
					}
				} 
				
				modifyGeometry(this.targetStore, dissolveFeature);

				if( endOfGroup ){

					// initialize the next iteration inserting the feature not processed because it is not part of processed group 
					dissolveFeature = insertIntoStore(this.targetStore, currentFeature);
				} 
				
			}while (iter.hasNext() );
			
		} catch (Exception e) {
			throw new SpatialOperationException(e);
		} finally {
			if (iter != null) {
				iter.close();
			}
		}
	}

	/**
	 * Makes an string using the values of dissolve properties
	 * @param dissolveFeature
	 * @param dissolveProperties
	 * @return values of dissolve properties in an StringBuffer
	 */
	private StringBuffer getGroupValues(	final SimpleFeature dissolveFeature, final List<String> dissolveProperties) {

		StringBuffer groupValue = new StringBuffer(50);
		for (String property : dissolveProperties) {
			
			Object value = dissolveFeature.getAttribute(property);
			if (value != null) {
				groupValue = groupValue.append(value.toString());
			} else {
				groupValue = groupValue.append(""); //$NON-NLS-1$
			}
		}
		return groupValue;
	}

	/**
	 * Returns the target with the features that result of this spatial
	 * operation
	 * 
	 * @return {@link FeatureStore}
	 */
	@Override
	protected SimpleFeatureStore getResult() {

		return this.targetStore;
	}

	/**
	 * Retrieves the feature collection to dissolve ordered by the dissolve
	 * property.
	 * 
	 * @param source
	 * @param filter
	 * @param dissolveProperty
	 * @param sourceCrs2
	 * @return a FeaturerCollection sorted
	 * @throws SpatialOperationException
	 */
	private FeatureCollection<SimpleFeatureType, SimpleFeature> createFeaturesCollectionSorted(	
			final FeatureSource<SimpleFeatureType, SimpleFeature> source,
			final Filter filter,
			final List<String> dissolveProperty,
			final CoordinateReferenceSystem sourceCrs)
		throws SpatialOperationException {

		try {
			FeatureCollection<SimpleFeatureType, SimpleFeature> ordered;

			FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
			SortBy[] sortBy = new SortBy[dissolveProperty.size()];
			int i = 0;
			for(String propName: dissolveProperty){
				sortBy[i++]  = ff.sort(propName, org.opengis.filter.sort.SortOrder.ASCENDING);
			}
			
			QueryCapabilities queryCapabilities = source.getQueryCapabilities();
			if( queryCapabilities.supportsSorting(sortBy) ){
				// if the source support the sort, then create a query an retrieve the features sorted by the dissolve property 
				Query query = new Query();
				query.setFilter(filter);
				query.setCoordinateSystem(sourceCrs);
				query.setSortBy(sortBy );
				ordered = source.getFeatures(query);
			} else {
				// it the source does not support the sort (likes shp), creates the OrderFeatureCollection  
				ordered = new OrderedFeatureCollection(
						(SimpleFeatureSource) source, filter, dissolveProperty,
						sourceCrs);
				return ordered;
			}
			return ordered;

		} catch( IOException e){

			throw createException(e);
		} 
	}

	/**
	 * Does the geometry union of the last dissolve feature with the current
	 * feature.
	 * 
	 * @param dissolveFeature
	 * @param currentFeature
	 * @param sourceCrs
	 * @param crs
	 * @param targetCrs
	 * @return new dissolve feature projected on the target's CRS
	 * @throws SpatialDataProcessException
	 */
	private SimpleFeature makeGeometryUnion(final SimpleFeature dissolveFeature,
											final SimpleFeature currentFeature,
											final CoordinateReferenceSystem sourceCrs,
											final CoordinateReferenceSystem crs,
											final CoordinateReferenceSystem targetCrs) throws SpatialOperationException {

		try {
			Geometry dissolveOnCrs = GeoToolsUtils.reproject((Geometry) dissolveFeature.getDefaultGeometry(),
						sourceCrs, crs);
			Geometry currentOnCrs = GeoToolsUtils.reproject((Geometry) currentFeature.getDefaultGeometry(), sourceCrs,
						crs);

			Geometry union = dissolveOnCrs.union(currentOnCrs);

			Geometry unionOnTargetCrs = GeoToolsUtils.reproject(union, crs, targetCrs);

			dissolveFeature.setDefaultGeometry(unionOnTargetCrs);

			return dissolveFeature;

		} catch (Exception e) {
			e.printStackTrace();
			throw createException(e);
		}
	}

	/**
	 * Creates the a new feature with the geometry and  the dissolve properties 
	 * @param store
	 * @param feature
	 * @return the inserted feature
	 * 
	 * @throws SpatialOperationException
	 */
	private SimpleFeature insertIntoStore(FeatureStore<SimpleFeatureType, SimpleFeature> store, SimpleFeature feature)
		throws SpatialOperationException{

		// creates the new features 
		SimpleFeature newFeature = createDissoveFeature(
				(Geometry) feature.getDefaultGeometry(),
				feature, 
				this.targetStore.getSchema(), 
				this.sourceCrs,
				this.targetCrs, 
				this.dissolveProperties);

		// inserts the second feature in target layer
		SimpleFeature insertedFeature = insertFeature(store, newFeature);

		return insertedFeature;
	}

	/**
	 * Creates the new feature on target feature type projecting the featue's
	 * geometry.
	 * 
	 * @param feature
	 * @param sourceCrs
	 * @param targetCrs
	 * @param geomOnTargetCrs
	 * @return Feature
	 * @throws SpatialDataProcessException
	 */
	private SimpleFeature createFeatureUsing(	final SimpleFeature feature,
												final SimpleFeatureType targetType,
												final CoordinateReferenceSystem sourceCrs,
												final CoordinateReferenceSystem targetCrs)
		throws SpatialOperationException {

		assert feature != null;
		assert targetType != null;
		assert sourceCrs != null;
		assert targetCrs != null;

		try {

			// projects the geometry on target
			Geometry firstGeom = (Geometry) feature.getDefaultGeometry();

			Geometry geomOnTargetCrs = GeoToolsUtils.reproject(firstGeom, sourceCrs, targetCrs);

			// create the new feature using the projected geometry
			SimpleFeature newFeature = FeatureUtil.createFeatureUsing(feature, targetType, geomOnTargetCrs);
			return newFeature;

		} catch (Exception e) {
			throw createException(e);
		}
	}
	/**
	 * Create a new feature using the geometry provided as parameter and 
	 * the source features' values indeed in the dissolve properties list
	 * @param geometry		geometry used to create the new feature
	 * @param sourceFeature	feature that contains the value to copy 
	 * @param targetType	target feature type
	 * @param sourceCrs
	 * @param targetCrs
	 * @param dissolveProperties properties name list 
	 * @return
	 * @throws SpatialOperationException
	 */
	private SimpleFeature createDissoveFeature(
			final Geometry geometry,
			final SimpleFeature sourceFeature,
			final SimpleFeatureType targetType,
			final CoordinateReferenceSystem sourceCrs,
			final CoordinateReferenceSystem targetCrs,
			final List<String> dissolveProperties
			) throws SpatialOperationException{
		
		try {
			Geometry geomOnTargetCrs = GeoToolsUtils.reproject(geometry, sourceCrs, targetCrs);

			// create the new feature using the projected geometry
			SimpleFeature newFeature = FeatureUtil.createFeatureWithGeometry(targetType, geomOnTargetCrs);
			
			// copy in the dissolve features only the values present in the dissolve property
			newFeature = FeatureUtil.copyAttributesInPropertyList(sourceFeature, newFeature, dissolveProperties);

			return newFeature;
		} catch (Exception e) {
			e.printStackTrace();
			throw createException(e);
		}
	}

	private void modifyGeometry(final FeatureStore<SimpleFeatureType, SimpleFeature> store,
								final SimpleFeature dissolveFeature) throws SpatialOperationException {

		// inserts the second feature in target layer
		Transaction transaction = store.getTransaction();

		try {
			Class<? extends Geometry> geomClass = this.targetGeometryClass;

			Geometry adaptedGeom = GeometryUtil.adapt((Geometry) dissolveFeature.getDefaultGeometry(), geomClass);

			// modifies the feature's geometry in the store
			GeometryDescriptor geomAttr = store.getSchema().getGeometryDescriptor();

			String fid = dissolveFeature.getID();
			Id filter = getFilterId(fid);
			store.modifyFeatures(geomAttr.getName(), adaptedGeom, filter);

			transaction.commit();
		} catch (IOException e) {
			try {
				store.getTransaction().rollback();
			} catch (IOException e1) {
				throw createException(e1);
			}
			throw createException(e);
		} finally {
			try {
				transaction.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw createException(e);
			}
		}
	}
}
