package es.axios.udig.spatialoperations.internal.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$


    public static String ClipCommand_failed_validating_geometry;


    public static String AbstractParamsPresenter_failed_selecting_features;
    public static String AbstractParamsPresenter_all_features_selected ;

    public static String AbstractProcess_failed_getting_feature_store;

    public static String BufferCommand_can_not_create_targetFeatureType;
    public static String BufferCommand_create_buffer_in_target;
    public static String BufferCommand_crs_error;
    public static String BufferCommand_geometry_type_error;
    public static String BufferCommand_must_select_target;
    public static String BufferCommand_must_specify_the_units;
    public static String BufferCommand_parameters_ok;
    public static String BufferCommand_select_source_layer;
    public static String BufferCommand_source_and_target_must_be_differents;
    public static String BufferCommand_target_required;
    public static String BufferCommand_there_is_not_source_features;
    public static String BufferCommand_width_must_be_greater_than_cero;
    public static String BufferComposite_operation_name;

    public static String BufferLayersComposite_failed_getting_selected_features;

    public static String BufferLayersComposite_failed_validating_buffer_parameters;

    public static String BufferLayersComposite_features_seleccionados;

    public static String BufferLayersComposite_layer;

    public static String BufferLayersComposite_result;

    public static String BufferLayersComposite_source;

    public static String BufferOperation_dialog_title;
    public static String BufferOperation_operationPrecondition_message;
    public static String BufferOperation_operationPrecondition_noFeaturesSelected;
    public static String BufferOperation_operationPrecondition_notAFeatureSourceLayer;
    public static String BufferOperation_operationPrecondition_title;
    public static String BufferOptionsComposite_advanced_options;
    public static String BufferOptionsComposite_bufferOptionsGroup_text;
    public static String BufferOptionsComposite_checkMergeResults_text;
    public static String BufferOptionsComposite_chekMergeResults_tooltip;
    public static String BufferOptionsComposite_groupUnits_text;
    public static String BufferOptionsComposite_labelBufferWidth_text;
    public static String BufferOptionsComposite_labelQuadrantSegments_text;
    public static String BufferOptionsComposite_layer_crs_units;
    public static String BufferOptionsComposite_operationDescription;
    public static String BufferOptionsComposite_options;
    public static String BufferOptionsComposite_radioLayerUnits_text;
    public static String BufferOptionsComposite_radioMapUnits_text;
    public static String BufferOptionsComposite_radioLayerUnits_tooltip;
    public static String BufferOptionsComposite_radioMapUnits_tooltip;
    public static String BufferOptionsComposite_radioSpecifyUnits_text;
    public static String BufferOptionsComposite_units;
    public static String BufferOptionsPage_messageIncompatibleUnits;
    public static String BufferPage_description;
    public static String BufferPage_title;
    public static String BufferProcess_adding_feature_to_store;
    public static String BufferProcess_canceled;
    public static String BufferProcess_error_getting_selected_features;
    public static String BufferProcess_failed_creating_temporal_store;
    public static String BufferProcess_failed_transforming;
    public static String BufferProcess_failed_transforming_feature_to_crs;
    public static String BufferProcess_subTask_BufferingFeatureN;
    public static String BufferProcess_subtaskAddLayerToMap;
    public static String BufferProcess_subtaskBufferringFeatures;
    public static String BufferProcess_subtaskGetTargetLayer;
    public static String BufferProcess_subtastCommittingTransaction;
    public static String BufferProcess_taskBuffering;
    public static String BufferWizard_bufferOptionsPage_title;
    public static String BufferWizard_layerCreationPage_description;
    public static String BufferWizard_layerCreationPage_title;

    public static String ClipProcess_failed_creating_temporal_store;

    public static String ClipCommand_will_clip_existent_layer;
    public static String ClipCommand_clip_description;
    public static String ClipCommand_clipping_and_clipped_must_be_differents;
    public static String ClipCommand_must_select_clipped_layer;
    public static String ClipCommand_must_select_clipping_layer;
    public static String ClipCommand_must_select_result;
    public static String ClipCommand_parameters_ok;
    public static String ClipCommand_there_are_not_features_in_clipping_layer;
    public static String ClipCommand_there_are_not_features_to_clip;
    public static String ClipComposite_apply_to;
    public static String ClipComposite_clipping_features;
    public static String ClipComposite_clipping_layer;
    public static String ClipComposite_failed_validating_parameters;
    public static String ClipComposite_features_to_clip;
    public static String ClipComposite_layer_to_clip;
    public static String ClipComposite_operation_name;
    public static String ClipCommand_clipping_and_result_must_be_differents;

    public static String ClipComposite_using_as_clip;
    public static String ClipProcess_clip_was_canceled;
    public static String ClipProcess_clipping_with;
    public static String ClipProcess_failed;
    public static String ClipProcess_failed_clipping_feature_collection;
    public static String ClipProcess_failed_deleting;
    public static String ClipProcess_failed_executing_reproject;
    public static String ClipProcess_failed_modifying_feature;
    public static String ClipProcess_failed_transforming;
    public static String ClipProcess_failed_creating_new_feature;
    public static String ClipProcess_reporjecting;
    public static String ClipProcess_successful;
    public static String CreateLayerPage_defaultGeometryAttributeTypeName;
    public static String CreateLayerPage_defaultStringAttributeTypeName;
    public static String CreateLayerPage_title;
    public static String CreateNewLayerDialog_create_new_layer;
    public static String CreateNewLayerDialog_failed_creatin_the_feature_type;
    public static String CreateNewLayerDialog_failed_getting_new_feature_type;
    public static String CreateNewLayerDialog_must_set_the_feature_name;
    public static String CreateNewLayerDialog_must_set_the_geometry;
    public static String CreateNewLayerDialog_specific_attributes;
    public static String CreateNewLayerDialog_title;
    public static String FeatureTypeEditor_newFeatureTypeName;
    public static String IntersectCommand_description;

    public static String IntersectCommand_expected_geometries;

    public static String IntersectCommand_faild_validating_geometry_compatibility;

    public static String IntersectCommand_first_and_second_must_be_differents;

    public static String IntersectCommand_first_sectond_and_target_must_be_differents;

    public static String IntersectCommand_must_select_second_layer;

    public static String IntersectCommand_must_select_target_layer;

    public static String IntersectCommand_must_select_the_first_layer;

    public static String IntersectCommand_must_set_target_layer;

    public static String IntersectCommand_parameters_ok;

    public static String IntersectCommand_there_is_not_features_to_intersect_in_first_layer;

    public static String IntersectCommand_there_is_not_to_intersect_in_second_layer;

    public static String IntersectComposite_can_not_create_targetFeatureType = null;
    public static String IntersectComposite_create;
    public static String IntersectComposite_failed_validating_parameters;
    public static String IntersectComposite_first_layer;

    public static String IntersectComposite_operation_name;
    public static String IntersectComposite_result;
    public static String IntersectComposite_second_layer;
    public static String IntersectComposite_selected_features;
    public static String IntersectComposite_source;
    public static String IntersectComposite_target_layer;
    public static String IntersectProcess_failed_creating_temporal_store;
    public static String IntersectProcess_intersectin_with;
    public static String IntersectProcess_intersection_fail;
    public static String IntersectProcess_successful;
    public static String LayerSelectionComposite_layerColumn_text;
    public static String ProjectionValidator_crs_source_can_not_be_null;
    public static String ProjectionValidator_crs_target_can_not_be_null;
    public static String ProjectionValidator_impossible_reproject;

    public static String ResultLayerComposite__duplicated_layer_name;
    public static String ResultLayerComposite_button_new_layer;
    public static String ResultLayerComposite_target_label;
    public static String SOComposite_operation;
    public static String SOComposite_perform;
    public static String SpatialOperationProcessManager_;
    public static String SpatialOperationProcessManager_buffer_process;
    public static String SpatialOperationProcessManager_clip_process;
    public static String SpatialOperationProcessManager_intersect_process;
    
    public static String GeometryCompatibilityValidator_expected_geometry_type;
    

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
