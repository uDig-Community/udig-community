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
package es.axios.udig.spatialoperations.internal.modelconnection;

import java.text.MessageFormat;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;

import com.vividsolutions.jts.geom.GeometryFactory;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.preferences.PreferenceConstants;
import es.axios.udig.spatialoperations.ui.view.Message;
import es.axios.udig.ui.commons.util.GeoToolsUtils;

/**
 * Valids that exist a projection from layer to map
 * <p>
 *  
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class ProjectionValidator implements ISOValidator {

    
    private Message                   messages  = null;
    private CoordinateReferenceSystem sourceCrs = null;
    private CoordinateReferenceSystem targetCrs = null;


    /**
     * @param sourceCrs The sourceCrs to set.
     */
    public void setSourceCrs( CoordinateReferenceSystem sourceCrs ) {
        this.sourceCrs = sourceCrs;
    }

    /**
     * @param targetCrs The targetCrs to set.
     */
    public void setTargetCrs( CoordinateReferenceSystem targetCrs ) {
        this.targetCrs = targetCrs;
    }

    /**
     * @return the error message.
     */
    public final Message getMessage() {
        assert this.messages != null;
        return this.messages;
    }

    /**
     * Checks the following subjects:
     * <p>
     * <ul>
     * <li> The <code>layer</code> shall resolve to a <code>FeatureSource</code>
     * <li> There has to be possible to transform geometries from the source layer's CRS to the
     * Map's CRS
     * <li> If the preference {@link PreferenceConstants#SELECTION_FALLBACK_TO_WHOLE_LAYER} is set
     * to <code>false</code>, <code>sourceLayer</code> must have at least one selected SimpleFeature.
     * </ul>
     * </p>
     * 
     * @param layer the layer over which the operation is meant to be ran.
     * 
     * @return true if exist the projection
     * @throws Exception if the preference fallback to whole layer is set to false and an
     *         Exception is thrown while trying to obtain the layer's selection count.
     */
    public boolean validate() throws Exception {

        if( this.sourceCrs == null){
            final String msg = Messages.ProjectionValidator_crs_source_can_not_be_null;
            this.messages=new Message(msg, Message.Type.ERROR);
            return false;
        }

        if( this.targetCrs == null) {
            final String msg = Messages.ProjectionValidator_crs_target_can_not_be_null;
            this.messages=new Message( msg, Message.Type.ERROR);
            return false;
        }
        
        GeometryFactory gFactory = new GeometryFactory();
        try {
            GeoToolsUtils.getTransformer(gFactory, sourceCrs, targetCrs);
        } catch (OperationNotFoundException e) {
            String msg = MessageFormat.format(Messages.ProjectionValidator_impossible_reproject,
                                              e.getMessage());
            this.messages=new Message(msg, Message.Type.ERROR);
            return false;
        }

        return true;
    }

    

}
