/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
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
package es.axios.udig.spatialoperations.internal.processmanager;

import java.util.LinkedList;
import java.util.List;

/**
 * Maintains the transaction to apply in the store
 * <p>
 * The process could require complex transaction, this 
 * class maintan the information about the modified features  (inserted, updated, delete)
 * in the process. The object of this class only maintains the
 * reference (or FID) to the changed  feature.
 * 
 * The process could use this FID to gets the feature if that is necessary.
 * 
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 * 
 */
final class FeatureTransaction {
    
    private String       oldFID          = null;
    private List<String> listInsertedFID = new LinkedList<String>();
    private String       updatedFID      = null;

    public enum Type {
        UPDATE, DELETE, SPLIT
    };
   
    private Type type;

    
    /**
     * use the factory method
     * 
     * @see createSplitTransaction
     * @see createDeleteTransaction
     * @see createUpdateTransaction
     *
     */
    private FeatureTransaction(){
        // nothing
    }
    
    
    /**
     * @param oldFID
     * @param insertList
     * @return new instance of split transaction
     */
    public static FeatureTransaction createSplitTransaction(final String oldFeature, 
                                                     final List<String> insertList ){
        FeatureTransaction tx = new FeatureTransaction();
        
        tx.oldFID = oldFeature;
        
        tx.listInsertedFID.addAll(insertList);
        tx.type = Type.SPLIT;
        
        return tx;
    }
    /**
     * @param oldFID
     * @return new instance of delete transaction
     */
    public static FeatureTransaction createDeleteTransaction(final String oldFeature ){
        
        FeatureTransaction tx = new FeatureTransaction();
        
        tx.oldFID = oldFeature;
        tx.type = Type.DELETE;
        
        return tx;
    }

    /**
     * @param oldFID
     * @param updatedFeature
     * @return new instance of update transaction
     */
    public static FeatureTransaction createUpdateTransaction(final String oldFeature, String updatedFeature ){

        FeatureTransaction tx = new FeatureTransaction();
        
        tx.oldFID = oldFeature;
        tx.updatedFID = updatedFeature;
        tx.type = Type.UPDATE;
        
        return tx;
    }

    /**
     * @return Returns the oldFID.
     */
    public String getOldFeature() {
        return this.oldFID;
    }

    /**
     * @return Returns the type.
     */
    public Type getType() {
        return this.type;
    }
    /**
     * @return Returns the listInsertedFID.
     */
    public List<String> getListInsertedFeatures() {
        return this.listInsertedFID;
    }


    /**
     * @return Returns the list of Updateed FID.
     */
    public String getUpdatedFeature() {
        return this.updatedFID;
    }
    
}
