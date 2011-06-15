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
package es.axios.udig.ui.editingtools.merge.internal;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.ui.tool.IToolContext;
import net.refractions.udig.tools.edit.Behaviour;

import org.eclipse.swt.widgets.Display;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.ui.commons.util.DialogUtil;
import es.axios.udig.ui.commons.util.GeometryUtil;
import es.axios.udig.ui.editingtools.internal.commands.EditingToolsCommandFactory;
import es.axios.udig.ui.editingtools.internal.i18n.Messages;
import es.axios.udig.ui.editingtools.merge.internal.wizard.FeatureMergeWizardDialog;
import es.axios.udig.ui.editingtools.merge.internal.wizard.MergeFeatureBuilder;

/**
 * Merge Features Behaviour
 * <p>
 * This is class is responsible to get the inputs required by merge tool. It presents the merge
 * dialog and makes the command which produce the merge in the current map.
 * </p>
 * <p>
 * This class follow the design idea of {@link Behaviour} interface but not implement it, because
 * the signature required here is similar but not equal.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class MergeFeatureBehaviour {

    private EditingToolsCommandFactory commandFactory;
    private String                      message;
    private SimpleFeature[]                   sourceFeatures = null;
    private IToolContext                toolContext;

    /**
     * a new instance of MergeFeatureBehaviour
     * 
     * @param toolContext the context of tool
     */
    public MergeFeatureBehaviour( final IToolContext toolContext,
                                  final EditingToolsCommandFactory commandFactory ) {
        assert toolContext != null;
        assert commandFactory != null;

        this.toolContext = toolContext;
        this.commandFactory = commandFactory;
    }

    /**
     * Set the features to merge
     * 
     * @param features
     */
    public void setSourceFeatures( final SimpleFeature[] features ) {
        assert features != null : "got null argument";
        this.sourceFeatures = new SimpleFeature[features.length];
        System.arraycopy(features, 0, sourceFeatures, 0, features.length);
    }

    /**
     * Displays the merge dialog to get the desired feature as result, and creates the command.
     * 
     * @return MergeFeaturesCommand or null if the user cancels the merge dialog.
     */
    public UndoableMapCommand getCommand() {

        assert this.sourceFeatures != null;

        MergeFeatureBuilder builder = createMergeBuilder();
        Display display = Display.getDefault();
        boolean success = FeatureMergeWizardDialog.open(display, builder);

        if (!success) return null;
        
        final ILayer layer = this.toolContext.getSelectedLayer();

        SimpleFeature mergedFeature = builder.buildMergedFeature();
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = DataUtilities.collection(sourceFeatures);
        UndoableMapCommand cmd = commandFactory.createMergeFeaturesCommand(layer, features,
                                                                           mergedFeature);

        return cmd;
    }

    private MergeFeatureBuilder createMergeBuilder() {
        SimpleFeatureType type = sourceFeatures[0].getFeatureType();
        final Class<?> expectedGeometryType = type.getDefaultGeometry().getType().getBinding();
        Geometry union;
        union = GeometryUtil.geometryUnion(DataUtilities.collection(sourceFeatures));
        try {
            union = GeometryUtil.adapt(union, (Class<? extends Geometry>) expectedGeometryType);
        } catch (IllegalArgumentException iae) {
            union = null;
        }
        MergeFeatureBuilder mergeBuilder = new MergeFeatureBuilder(sourceFeatures, union);
        return mergeBuilder;
    }

    /**
     * Valid the merge data:
     * <ul>
     * <li> Must select two or more feature </li>
     * <li> The features selected are polygons they must intersect </li>
     * </ul>
     * 
     * @return true if the input are OK
     */
    public boolean isValid() {

        this.message = ""; //$NON-NLS-1$
        boolean valid = true;

        // Must select two or more feature
        int selectionCount = sourceFeatures.length;
        if (selectionCount < 2) {
            this.message = Messages.MergeFeatureBehaviour_select_two_or_more;
            valid = false;
        }

        // REVISIT GR: commented out this block of code as it seems to make no sense
        // since for other geometry types than Polygon, if the geometries
        // does not intersect, it is still possible to merge the feature but
        // selecting the geometry from one of those features.
        // // if the gemetry type is poligon they must intersct
        // SimpleFeatureType type = sourceFeatures.getSchema();
        // GeometryAttributeType attrType = type.getDefaultGeometry();
        // Class geometryClass = attrType.getType();
        //
        // if (Polygon.class.equals(geometryClass)) {
        //
        // if (!GeometryUtil.intersects(sourceFeatures, geometryClass)) {
        // this.message = Messages.MergeFeatureBehaviour_must_intersect;
        // return false;
        // }
        // }
        return valid;
    }

    /**
     * displays the error message
     */
    public void handleError() {

        DialogUtil.openError(Messages.MergeFeatureBehaviour_dialog_error_title, this.message);
    }

}
