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

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.tools.edit.EditToolHandler;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author Gabriel Roldán (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public interface EditingToolsCommandFactory {

    /**
     * Creates an {@link UndoableMapCommand}} that splits a collection of Features with a given
     * {@link Linestring}.
     * <p>
     * The splitting line is taken from the EditToolHandler's
     * {@link EditToolHandler#getCurrentShape()}
     * </p>
     * <p>
     * That line will then be used to cut the selected layer's geometries that intersect it. If a
     * selection is set on the layer, it will be respected, and thus the command will apply to those
     * features that are either selected and intersect the splitting line.
     * </p>
     * <p>
     * For those SimpleFeature geometries that were splitted, the original SimpleFeature will be deleted and as
     * many new Features as geometries resulted of the split operation will be created, maintaining
     * the original SimpleFeature's attributes other than the default geometry.
     * </p>
     * 
     * @param handler used to restore the state in the command's rollbak
     * @param selectedLayer used to build the delete and create feature commands against. It's
     *        {@link IGeoResource georesource} has to be resolvable to {@link FeatureStore} by
     *        testing through {@link ILayer#hasResource(Class)}.
     * @param featuresToSplit features from selectedLayer to split with splitter
     * @param splitter line to apply as splitter
     * @return
     * @throws IllegalArgumentException if <code>selectedLayer</code> has no
     *         <code>FeatureStore</code>, or if the <code>splitter</code> has no
     *         {@link CoordinateReferenceSystem CRS} information.
     */
    public UndoableMapCommand createSplitFeaturesCommand( final EditToolHandler handler ) throws IllegalArgumentException;

    public UndoableMapCommand createTrimFeaturesCommand( final EditToolHandler handler,
                                                         final ILayer selectedLayer,
                                                         final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToTrim,
                                                         final LineString trimmingLine );

    public UndoableMapCommand createMergeFeaturesCommand( final ILayer layer,
                                                          final FeatureCollection<SimpleFeatureType, SimpleFeature> sourceFeatures,
                                                          final SimpleFeature mergedFeature );
}
