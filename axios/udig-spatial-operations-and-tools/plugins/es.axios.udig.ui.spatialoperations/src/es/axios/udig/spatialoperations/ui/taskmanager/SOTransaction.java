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

import java.io.IOException;
import java.util.Set;

import net.refractions.udig.project.internal.impl.UDIGTransaction;

import org.geotools.data.Transaction;

/**
 * Transaction used with {@link SOFeatureStore}.
 * 
 * This is a wrapped transaction with custom commit() method.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 * @see SOFeatureStore
 */
class SOTransaction implements Transaction {

	Transaction	transaction;

	/**
	 * Set the wrapped transaction.
	 * 
	 * @param transaction
	 */
	public SOTransaction(Transaction transaction) {

		this.transaction = transaction;
	}

	public void addAuthorization(String authID) throws IOException {

		this.transaction.addAuthorization(authID);
	}

	public void close() throws IOException {

		if (!(this.transaction instanceof UDIGTransaction)) {
			this.transaction.close();
		}
	}

	/**
	 * Custom commit method.
	 * 
	 * If the transaction object is an object that belong to
	 * {@link UDIGTransaction}, it must commit throw the editManager.
	 */
	public void commit() throws IOException {

		if (this.transaction instanceof UDIGTransaction) {
			((UDIGTransaction) this.transaction).commitInternal();
		} else {
			this.transaction.commit();
		}
	}

	public Set<String> getAuthorizations() {

		return this.transaction.getAuthorizations();
	}

	public Object getProperty(Object key) {

		return this.transaction.getProperty(key);
	}

	public State getState(Object key) {

		return this.transaction.getState(key);
	}

	public void putProperty(Object key, Object value) throws IOException {

		this.transaction.putProperty(key, value);
	}

	public void putState(Object key, State state) {

		this.transaction.putState(key, state);
	}

	public void removeState(Object key) {

		this.transaction.removeState(key);
	}

	public void rollback() throws IOException {

		this.transaction.rollback();
	}

}
