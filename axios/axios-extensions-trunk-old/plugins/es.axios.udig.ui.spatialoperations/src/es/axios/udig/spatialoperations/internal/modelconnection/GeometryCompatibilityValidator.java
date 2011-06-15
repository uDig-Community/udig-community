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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.view.Message;
import es.axios.udig.ui.commons.util.GeometryUtil;

/**
 * Geometry Compatibility Validator
 * <p>
 * Valids the compatibliy of the setted geometries
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public class GeometryCompatibilityValidator implements ISOValidator {

    private Class   expectedGeometry = null;
    private Class   targetGeometry   = null;
    private Message message          = null;

    
    public Message getMessage() {
        return this.message;
    }

    public boolean validate() throws Exception {
        
        this.message = Message.NULL;
        
        if(Geometry.class.equals( targetGeometry )){
            return true;
        }
        
        if( expectedGeometry.equals( targetGeometry) ) {
            return true;
        }
        
        // if it is multigeometry the expected geometry must have same multigeometry
        if( (expectedGeometry.equals(MultiPolygon.class) )||
            (expectedGeometry.equals(MultiLineString.class) ) ||
            (expectedGeometry.equals(MultiPolygon.class) ) ){
            
            if(!expectedGeometry.equals( targetGeometry) ){
                
                String msg = MessageFormat.format(Messages.GeometryCompatibilityValidator_expected_geometry_type, expectedGeometry.getSimpleName());
                this.message = new Message(msg, Message.Type.ERROR);
                return false;
            }
            
            return true; 
        } 
        if(Geometry.class.equals(this.expectedGeometry)){
            
            if(! Geometry.class.equals(this.targetGeometry)){
                
                String msg = MessageFormat.format(Messages.GeometryCompatibilityValidator_expected_geometry_type, Geometry.class.getSimpleName());
                this.message = new Message(msg, Message.Type.ERROR);
                return false;
            }
        }
        // test simple vs mutigeometry compatibility
        if( isCompatibleGeometryCollection( expectedGeometry, targetGeometry )){
                return true;
        }
        // it is not compatible geometry
        final String typeExpectedName = expectedGeometry.getSimpleName();
        String text = MessageFormat.format(Messages.IntersectCommand_expected_geometries,
                                           typeExpectedName);
        this.message = new Message(text, Message.Type.ERROR);
        
        return false;
    }
    
    

    /**
     * Analyses if simple Geometry has a correspondent multygeometry
     * @param simpleGeometry
     * @param targetGeometry
     * @return true if simpleGeometry is compatible to target Geometry
     */
    private boolean isCompatibleGeometryCollection( Class simpleGeometry, Class targetGeometry ) {
        
        Class compatible = GeometryUtil.getCompatibleCollection(simpleGeometry);
        
        boolean result = compatible.equals(targetGeometry);
            
        return result;
    }

    

    /**
     *
     * @param expectedGeometry
     */
    public void setExpected( Class expectedGeometry ) {
        this.expectedGeometry = expectedGeometry;
    }

    /**
     *
     * @param targetGeometry
     */
    public void setTarget( Class targetGeometry ) {
        
        this.targetGeometry = targetGeometry;
    }
    

}
