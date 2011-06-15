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

package es.axios.udig.ui.editingtools.internal.commands;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.tools.edit.EditToolHandler;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.LineString;

/**
 * Default implementation of {@link EditingToolsCommandFactory}
 * 
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class DefaultEdittingToolsCommandFactory implements EditingToolsCommandFactory {

    public UndoableMapCommand createSplitFeaturesCommand( EditToolHandler handler ) {
        return new SplitFeaturesCommand(handler);
    }

    public UndoableMapCommand createTrimFeaturesCommand( EditToolHandler handler,
                                                         ILayer selectedLayer,
                                                         FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToTrim,
                                                         LineString trimmingLine ) {
        return new TrimFeaturesCommand(handler, selectedLayer, featuresToTrim, trimmingLine);

    }

    public UndoableMapCommand createMergeFeaturesCommand( ILayer layer,
                                                          FeatureCollection<SimpleFeatureType, SimpleFeature> sourceFeatures,
                                                          SimpleFeature mergedFeature ) {
        return new MergeFeaturesCommand(layer, sourceFeatures, mergedFeature);
    }

}
