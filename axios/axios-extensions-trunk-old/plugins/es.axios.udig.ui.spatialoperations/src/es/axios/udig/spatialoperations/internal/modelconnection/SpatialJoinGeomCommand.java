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

import es.axios.udig.spatialoperations.ui.view.Message;

/**
 * Spatial Join Geometries Command
 * <p>
 * This class is responsible to check the parameters and execute the 
 * spatial join geometries process.
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class SpatialJoinGeomCommand extends SOAbstractCommand {

    private static final Message INITIAL_MESSAGE       = new Message("Generates a new layer with the features of First and Second layer which fulfil with the spatial relation.",
                                                                     Message.Type.IMPORTANT_INFO);

    /**
     * @param initialMessage
     */
    public SpatialJoinGeomCommand() {
        super(INITIAL_MESSAGE);
    }

    @Override
    public void execute() throws SOCommandException {

    }

    @Override
    public void initParameters() {
        
        //TODO
    }

}
