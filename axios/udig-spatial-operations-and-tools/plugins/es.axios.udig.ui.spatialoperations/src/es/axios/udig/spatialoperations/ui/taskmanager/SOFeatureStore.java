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
package es.axios.udig.spatialoperations.ui.taskmanager;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

/**
 * Spatial Operation Feature Store. This is a wrapped feature store, we use it,
 * for having a custom {@link SOTransaction} with custom implementation.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 * @see SOTransaction
 */
public class SOFeatureStore implements SimpleFeatureStore {

	private SimpleFeatureStore	soFeatureStore;
	private Transaction										soTransaction;

	/**
	 * Set the feature store and the transaction that will be used.
	 * 
	 * @param featureStore
	 * @param transaction
	 */
	public SOFeatureStore(SimpleFeatureStore featureStore, Transaction transaction) {

		this.soFeatureStore = featureStore;
		this.soTransaction = new SOTransaction(transaction);
	}

	public List<FeatureId> addFeatures(FeatureCollection<SimpleFeatureType, SimpleFeature> collection)
		throws IOException {

		List<FeatureId> features = this.soFeatureStore.addFeatures(collection);
		return features;
	}

	public Transaction getTransaction() {

		return this.soTransaction;
	}

	/**
	 * @deprecated
	 */
	public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter) throws IOException {

		this.soFeatureStore.modifyFeatures(type, value, filter);
	}

	/**
	 * @deprecated
	 */
	public void modifyFeatures(AttributeDescriptor type, Object value, Filter filter) throws IOException {

		this.soFeatureStore.modifyFeatures(type, value, filter);
	}

	public void removeFeatures(Filter filter) throws IOException {

		this.soFeatureStore.removeFeatures(filter);
	}

	public void setFeatures(FeatureReader<SimpleFeatureType, SimpleFeature> reader) throws IOException {

		this.soFeatureStore.setFeatures(reader);
	}

	public void setTransaction(Transaction transaction) {

		throw new UnsupportedOperationException("You should use the constructor to set the transaction"); //$NON-NLS-1$
	}

	public void addFeatureListener(FeatureListener listener) {

		this.soFeatureStore.addFeatureListener(listener);
	}

	public ReferencedEnvelope getBounds() throws IOException {

		return this.soFeatureStore.getBounds();
	}

	public ReferencedEnvelope getBounds(Query query) throws IOException {

		return this.soFeatureStore.getBounds(query);
	}

	public int getCount(Query query) throws IOException {

		return this.soFeatureStore.getCount(query);
	}

	public DataAccess<SimpleFeatureType, SimpleFeature> getDataStore() {

		return this.soFeatureStore.getDataStore();
	}

	public SimpleFeatureCollection getFeatures() throws IOException {

		return this.soFeatureStore.getFeatures();
	}

	public SimpleFeatureCollection getFeatures(Query query) throws IOException {

		return this.soFeatureStore.getFeatures(query);
	}

	public SimpleFeatureCollection getFeatures(Filter filter) throws IOException {

		return this.soFeatureStore.getFeatures(filter);
	}

	public SimpleFeatureType getSchema() {

		return this.soFeatureStore.getSchema();
	}

	public void removeFeatureListener(FeatureListener listener) {

		this.soFeatureStore.removeFeatureListener(listener);
	}

	public ResourceInfo getInfo() {

		return this.soFeatureStore.getInfo();
	}

	public Name getName() {

		return this.soFeatureStore.getName();
	}

	public QueryCapabilities getQueryCapabilities() {

		return this.soFeatureStore.getQueryCapabilities();
	}

	public Set<Key> getSupportedHints() {

		return this.soFeatureStore.getSupportedHints();
	}

	
	public void modifyFeatures(Name[] arg0, Object[] arg1, Filter arg2)
			throws IOException {
		this.soFeatureStore.modifyFeatures(arg0, arg1, arg2);
		
	}

	public void modifyFeatures(Name arg0, Object arg1, Filter arg2)
			throws IOException {
		
		this.soFeatureStore.modifyFeatures(arg0, arg1, arg2);
		
	}

	public void modifyFeatures(String arg0, Object arg1, Filter arg2)
			throws IOException {
	
		this.soFeatureStore.modifyFeatures(arg0, arg1, arg2);
		
	}

	public void modifyFeatures(String[] arg0, Object[] arg1, Filter arg2)
			throws IOException {
		this.soFeatureStore.modifyFeatures(arg0, arg1, arg2);
		
	}

}
