/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
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
package es.axios.udig.ui.editingtools.parallel;

import net.refractions.udig.tools.edit.activator.DrawCurrentGeomVerticesActivator;
import es.axios.udig.ui.editingtools.internal.commons.behaviour.DrawEditGeomWithCustomEndlineActivator;
import es.axios.udig.ui.editingtools.internal.commons.behaviour.DrawOrthoAxesActivator;
import es.axios.udig.ui.editingtools.internal.commons.behaviour.OrthoEditPointProvider;

/**
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 0.2.0
 */
public class OrthoAction extends AbstractEditModifierActionDelegate {

    @Override
    protected void activate() {
        removeRunningActivators();
        addActivator(new DrawEditGeomWithCustomEndlineActivator(new OrthoEditPointProvider()));
        addActivator(new DrawCurrentGeomVerticesActivator());
        addActivator(new DrawOrthoAxesActivator());

        OrthoEditPointProvider editPointProvider = new OrthoEditPointProvider();
        setEditPointProvider(editPointProvider);
    }

//    @Override
//    protected void deactivate() {
//        super.deactivate();
//        restoreOriginalActivators();
//    }

}
