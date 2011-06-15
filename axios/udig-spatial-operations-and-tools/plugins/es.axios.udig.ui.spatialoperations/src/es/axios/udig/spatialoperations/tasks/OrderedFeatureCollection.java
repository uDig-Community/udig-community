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
package es.axios.udig.spatialoperations.tasks;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * <p>
 * This class is used to order the contained of shape file. This kind of store does not
 * support order query. 
 * </p>
 * <p>
 * Implements an index with duplicate. Each entry in the index have the following
 * structure which associate FeatueId and Sort Properties.
 * <p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
final class OrderedFeatureCollection implements FeatureCollection<SimpleFeatureType, SimpleFeature> {

	private final SimpleFeatureCollection	sortedCollection;
	private final List<Association>										sortedIndex;

	private SimpleFeatureSource											source;
	private CoordinateReferenceSystem									crs;

	public OrderedFeatureCollection(final SimpleFeatureSource source,
									final Filter filter,
									final List<String> dissolveProperty,
									final CoordinateReferenceSystem sourceCrs) throws IOException {

		assert source != null;
		assert filter != null;
		assert dissolveProperty != null;

		this.source = source;
		this.crs = sourceCrs;

		// Creates index for that DataStores which don't support the
		// Query.sortBy
		this.sortedCollection = retrieveFeatures(this.source, filter, dissolveProperty,  this.crs);
		this.sortedIndex = sort(this.sortedCollection, dissolveProperty);
		
		assert this.sortedCollection.size() == this.sortedIndex.size();
	}

	/**
	 * Retrieve the features without order
	 */
	private SimpleFeatureCollection retrieveFeatures(	final SimpleFeatureSource source,
														final Filter filter,
														final List<String> sortByProperties,
														final CoordinateReferenceSystem crs)
		throws IOException {

		Query query = new Query();
		query.setFilter(filter);
		query.setCoordinateSystem(crs);

		SimpleFeatureCollection features = source.getFeatures(query);

		return features;
	}

	/**
	 * Implements the association (property, featureId) used to sort the feature
	 * collection.
	 * 
	 * @author Mauricio Pazos (www.axios.es)
	 * @since 1.1
	 */
	private class Association implements Comparable<Association> {

		private final Object	sortProperty;
		private final String	featureId;

		/**
		 * @return the sortProperty
		 */
		public final Object getSortProperty() {
			return sortProperty;
		}

		/**
		 * @return the featureId
		 */
		public final String getFeatureId() {
			return featureId;
		}

		/**
		 * 
		 * @param sortProperty
		 *            could be null
		 * @param featureId
		 *            a not null value
		 */
		public Association(Object sortProperty, String featureId) {
			assert featureId != null;

			this.sortProperty = sortProperty;
			this.featureId = featureId;
		}

		/**
		 * @param o
		 *            an association
		 */
		public int compareTo(final Association o) {

			assert o != null;

			if ((this.sortProperty == null) && (o.getSortProperty() != null)) {
				return -1;
			}

			if ((this.sortProperty != null) && (o.getSortProperty() == null)) {
				return 1;
			}

			if ((this.sortProperty == null) && (o.getSortProperty() == null)) {
				return 0;
			}

			return this.sortProperty.toString().compareTo(o.getSortProperty().toString());
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
			result = prime * result + ((featureId == null) ? 0 : featureId.hashCode());
			result = prime * result + ((sortProperty == null) ? 0 : sortProperty.hashCode());
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
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			final Association other = (Association) obj;
			if (featureId == null) {
				if (other.featureId != null) {
					return false;
				}
			} else if (!featureId.equals(other.featureId)) {
				return false;
			}
			if (sortProperty == null) {
				if (other.sortProperty != null) {
					return false;
				}
			} else if (!sortProperty.equals(other.sortProperty)) {
				return false;
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[" + this.sortProperty + ", " + this.featureId + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

	}

	/**
	 * Create an order list of features using the ordering by insertion algorithm.
	 * The List contains {@link Association} witch relate the dissolve property values and the feature id.
	 * 
	 * @param original
	 * @param dissolveProperties
	 * @return
	 */
	private List<Association> sort(	final FeatureCollection<SimpleFeatureType, SimpleFeature> original,
									final List<String> dissolveProperties) {

		// inserts in order the features <sortProp, IDs>
		List<Association> indexList = new LinkedList<Association>();
		FeatureIterator<SimpleFeature> iter = null;
		try {
			iter = original.features();
			while (iter.hasNext()) {
				SimpleFeature feature = iter.next();
				// searches the value for each attribute position.
				StringBuffer stringBuffer = new StringBuffer(50);
				for (String  property: dissolveProperties) {

					Object attribute = feature.getAttribute(property);
					if (attribute != null) {
						stringBuffer = stringBuffer.append(attribute.toString());
					} else {
						stringBuffer = stringBuffer.append(""); //$NON-NLS-1$
					}
				}
				Association currentMap = new Association(stringBuffer.toString(), feature.getID());

				Association map = null;
				int posFound = -1;
				for (int i = 0; i < indexList.size() && (posFound == -1); i++) {
					map = indexList.get(i);
					if (currentMap.compareTo(map) <= 0) {
						posFound = i;
					}
				}
				if (posFound >= 0) {
					indexList.add(posFound, currentMap);
				} else {
					indexList.add(currentMap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return indexList;
	}

	/**
	 * @deprecated
	 */
	public void close(FeatureIterator<SimpleFeature> iter) {
		this.sortedCollection.close(iter);
	}


	/**
	 * @deprecated
	 */
	public void close(Iterator<SimpleFeature> iter) {

		this.sortedCollection.close(iter);
	}

	public Iterator<SimpleFeature> iterator() {
		throw new UnsupportedOperationException();
	}

	public FeatureIterator<SimpleFeature> features() {

		FeatureIterator<SimpleFeature> iterator;
		iterator = new FeatureIterator<SimpleFeature>() {

			Iterator<Association>	sortedIter	= sortedIndex.iterator();

			public void close() {
				sortedIter = null;
			}

			public boolean hasNext() {

				return sortedIter.hasNext();
			}

			public SimpleFeature next() {
				Association map = sortedIter.next();

				SimpleFeature feature = null;
				FeatureIterator<SimpleFeature> fIter = sortedCollection.features();
				while (fIter.hasNext()) {
					feature = fIter.next();

					if (feature.getID().equals(map.getFeatureId())) {
						break;
					}
				}
				fIter.close();
				assert feature != null;
				return feature;
			}
		};

		return iterator;
	}

	public SimpleFeatureType getFeatureType() {

		return this.sortedCollection.getSchema();
	}

	public SimpleFeatureType getSchema() {
		return this.sortedCollection.getSchema();
	}

	@Deprecated
	public void purge() {
		this.sortedCollection.purge();
	}

	public boolean contains(Object o) {
		return this.sortedCollection.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return this.sortedCollection.containsAll(c);
	}

	public boolean isEmpty() {
		return this.sortedCollection.isEmpty();
	}

	public int size() {
		return this.sortedIndex.size();
	}

	public Object[] toArray() {
		return this.sortedIndex.toArray();
	}

	public ReferencedEnvelope getBounds() {
		return this.sortedCollection.getBounds();
	}

	public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void addListener(CollectionListener listener) throws NullPointerException {
		throw new UnsupportedOperationException();
	}

	public void removeListener(CollectionListener listener) throws NullPointerException {
		throw new UnsupportedOperationException();
	}

	public FeatureCollection<SimpleFeatureType, SimpleFeature> sort(SortBy order) {
		throw new UnsupportedOperationException();
	}

	public FeatureCollection<SimpleFeatureType, SimpleFeature> subCollection(Filter filter) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends SimpleFeature> c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public String getID() {
		throw new UnsupportedOperationException();
	}

	
	public boolean add(SimpleFeature obj) {
		throw new UnsupportedOperationException();
	}

	
	public boolean addAll(FeatureCollection<? extends SimpleFeatureType, ? extends SimpleFeature> resource) {
		throw new UnsupportedOperationException();
	}

	
	public <O> O[] toArray(O[] a) {
		throw new UnsupportedOperationException();
	}

}
