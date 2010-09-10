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
package es.axios.udig.ui.editingtools.internal.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * TODO Purpose of 
 * <p>
 *
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "es.axios.udig.ui.editingtools.internal.i18n.messages"; //$NON-NLS-1$
    public static String        FeatureMergeWizard_feature_merge;
    public static String        MergeFeaturesComposite_failed_creating_merge_feature;
    public static String        MergeFeaturesComposite_feature;
    public static String        MergeFeaturesComposite_merge_feature;
    public static String MergeFeaturesComposite_merge_result_title;
    public static String        MergeFeaturesComposite_property;
    public static String        MergeFeaturesComposite_result;
    public static String        MergeFeaturesComposite_result_geometry;
    public static String        MergeFeaturesComposite_result_will_be;
    public static String        MergeFeaturesComposite_source;
    public static String        MergeFeaturesComposite_value;
    public static String        MergeTool_failed_executing;
    public static String        MergeTool_failed_getting_selection;
    public static String        MergeTool_failed_rollback;
    public static String MergeTool_select_features_to_merge;
    public static String MergeTool_successful;
    public static String        MergeTool_title_tool;
    public static String        SplitFeaturesCommand_did_not_apply_to_any_feature;
    public static String        SplitFeaturesCommand_no_geometry_were_created;
    public static String        SplitFeaturesCommand_splitter_line_contain_crs_user_data;
    public static String        SplitGeometryBehaviour_transaction_failed;
    public static String        SplitStrategy_illegal_geometry;
    public static String        SplitStrategy_multigeoms_not_implemented;
    public static String        SplitStrategy_valid_geometries;
    public static String        SplitTool_draw_line_to_split;
    public static String        TrimFeaturesCommand_did_not_apply_to_any_feature;
    public static String        TrimFeaturesCommand_no_features_modified;
    public static String        TrimGeometryBehaviour_operation_failed;
    public static String        TrimGeometryStrategy_defined_for_line_geometries;
    public static String        TrimGeometryStrategy_difference_unknown_type;
    public static String        TrimGeometryStrategy_point_not_on_line;
    public static String        TrimGeometryStrategy_trimming_line_intersect_one_point;

    public static String        MergeFeatureBehaviour_select_two_or_more;
    public static String        MergeFeatureBehaviour_must_intersect;
    public static String        MergeFeatureBehaviour_dialog_error_title;
    public static String        TrimTool_draw_line_to_trim;
    
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    private Messages() {
    }
}
