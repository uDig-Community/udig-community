package es.axios.udig.spatialoperations.internal.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String	BUNDLE_NAME											= Messages.class.getPackage()
																								.getName()
																								+ ".messages";	//$NON-NLS-1$

	public static String		AbstractCommonTask_failed_creating_layer;

	public static String		AbstractCommonTask_procces_failed;

	public static String		AbstractMonitor_failed;

	public static String		AbstractMonitor_failed_initializating;

	public static String		AbstractSpatialOperationTask_failed_inserting;

	public static String		BufferMonitor_successful;

	public static String		Composite_source_group;

	public static String		Composite_result_group;

	public static String		DissolveCommand_initial_message;

	public static String		DissolveCommand_internal_fail;

	public static String		DissolveCommand_map_crs_value;

	public static String		DissolveCommand_select_dissolve_property;

	public static String		DissolveComposite_operation_name;

	public static String		DissolveComposite_operation_tool_tip;

	public static String		DissolveComposite_source_layer;

	public static String		DissolveComposite_selected_features;

	public static String		DissolveComposite_property;

	public static String		DissolveMonitor_canceled;

	public static String		DissolveMonitor_dissolve_using;

	public static String		DissolveMonitor_successful;

	public static String		DissolveTask_failed_inserting;

	public static String		FillCommand_fail_validating;

	public static String		FillCommand_fill_all;

	public static String		FillCommand_filter_null;

	public static String		FillCommand_firt_second_different;

	public static String		FillCommand_initial_message;

	public static String		FillCommand_params_ok;

	public static String		SpatialOperationCommand_reference_line;

	public static String		FillCommand_select_source_layer;

	public static String		FillCommand_select_target_layer;

	public static String		FillCommand_select_using_layer;

	public static String		SpatialOperationCommand_source_polygon;

	public static String		FillComposite_Reference_layer;

	public static String		FillComposite_CopyText;

	public static String		FillComposite_ToolTip;

	public static String		FillComposite_Source_layer;

	public static String		FillMonitor_canceled;

	public static String		FillMonitor_filling_layer;

	public static String		FillMonitor_success;

	public static String		FillSash_operation_name;

	public static String		FillSash_tool_tip_text;

	public static String		AbstractTask_failed_inserting_feature;

	public static String		HoleCommand_description;

	public static String		HoleCommand_params_ok;

	public static String		HoleCommand_select_hole_layer;

	public static String		HoleCommand_select_targer_layer;

	public static String		HoleCommand_select_using_layer;

	public static String		HoleCommand_source_polygon;

	public static String		HoleCommand_source_using_different;

	public static String		HoleCommand_using_line;

	public static String		HoleCommand_using_target_not_equal;

	public static String		HoleComposite_Hole_layer;

	public static String		HoleComposite_Using_layer;

	public static String		HoleMonitor_cutting_layers;

	public static String		HoleMonitor_finished_success;

	public static String		HoleMonitor_process_canceled;

	public static String		HoleSash_operation_name;

	public static String		HoleSash_tooltip_text;

	public static String		HoleTask_failed_doing_a_hole;

	public static String		LayerValidator_layer_null;

	public static String		LayerValidator_source_polygon;

	public static String		PolygonToLineAdvancedOptComposite_addvanced_tool_tip;

	public static String		PolygonToLineAdvancedOptComposite_advanced_text;

	public static String		PolygonToLineCommand_initial_message;

	public static String		PolygonToLineCommand_internal_fail;

	public static String		PolygonToLineCommand_map_crs;

	public static String		PolygonToLineSash_operation_name;

	public static String		PolygonToLineSash_tooltip_text;

	public static String		PolygonToLineTargetValidator_target_geometry_collection;

	public static String		PolygonToLineTargetValidator_target_geometry_simple;

	public static String		PolygonToLineTask_failed_inserting;

	public static String		SOTaskManager_buffer_operation;

	public static String		SOTaskManager_clip_operation;

	public static String		SOTaskManager_dissolve_operation;

	public static String		SOTaskManager_fill_operation;

	public static String		SOTaskManager_hole_operation;

	public static String		SOTaskManager_intersect_operation;

	public static String		SOTaskManager_polygonToLine_operation;

	public static String		SOTaskManager_spatial_join_operation;

	public static String		SOTaskManager_split_operation;

	public static String		SpatialJoinGeomCommand_filter_empty;

	public static String		SpatialJoinGeomCommand_filter_null;

	public static String		SpatialJoinGeomCommand_initial_message;

	public static String		SpatialJoinGeomComposite_checkSelection_text;
	public static String		SpatialJoinGeomComposite_checkSelection_tooltip;

	public static String		SpatialJoinGeomCommand_Source_and_result_must_be_different;
	public static String		SpatialJoinGeomCommand_Source_and_result_must_be_different_names;
	public static String		SpatialJoinGeomCommand_Target_Layer;
	public static String		SpatialJoinGeomCommand_Empty_Layer;
	public static String		SpatialJoinGeomCommand_Relation_required;

	public static String		SpatialJoinGeomComposite_contains;

	public static String		SpatialJoinGeomComposite_covers;

	public static String		SpatialJoinGeomComposite_crosses;

	public static String		SpatialJoinGeomComposite_disjoint;

	public static String		SpatialJoinGeomComposite_equals;

	public static String		SpatialJoinGeomComposite_intersects;

	public static String		SpatialJoinGeomComposite_is_cover_by;

	public static String		SpatialJoinGeomComposite_Operation_Name;
	public static String		SpatialJoinGeomComposite_Description;

	public static String		SpatialJoinGeomComposite_overlaps;
	public static String		SpatialJoinGeomComposite_Second_Selected_Features;
	public static String		SpatialJoinGeomComposite_Second_Layer;
	public static String		SpatialJoinGeomComposite_Relation;
	public static String		SpatialJoinGeomComposite_First_Selected_Features;
	public static String		SpatialJoinGeomComposite_First_Layer;

	public static String		ClipCommand_failed_validating_geometry;

	public static String		AbstractParamsPresenter_failed_selecting_features;
	public static String		AbstractParamsPresenter_all_features_selected;

	public static String		AbstractProcess_failed_getting_feature_store;

	public static String		BufferCommand_can_not_create_targetFeatureType;
	public static String		BufferCommand_create_buffer_in_target;
	public static String		BufferCommand_crs_error;
	public static String		BufferCommand_geometry_type_error;
	public static String		BufferCommand_must_select_target;
	public static String		BufferCommand_must_specify_the_units;
	public static String		BufferCommand_parameters_ok;
	public static String		BufferCommand_select_source_layer;
	public static String		BufferCommand_source_and_target_must_be_differents;
	public static String		BufferCommand_target_required;
	public static String		BufferCommand_there_is_not_source_features;
	public static String		BufferCommand_width_must_be_not_zero;
	public static String		BufferComposite_operation_name;

	public static String		BufferLayersComposite_failed_getting_selected_features;

	public static String		BufferLayersComposite_failed_validating_buffer_parameters;

	public static String		BufferLayersComposite_features_seleccionados;

	public static String		BufferLayersComposite_layer;

	public static String		BufferLayersComposite_result;

	public static String		BufferLayersComposite_source;

	public static String		BufferOperation_dialog_title;
	public static String		BufferOperation_operationPrecondition_message;
	public static String		BufferOperation_operationPrecondition_noFeaturesSelected;
	public static String		BufferOperation_operationPrecondition_notAFeatureSourceLayer;
	public static String		BufferOperation_operationPrecondition_title;
	public static String		BufferOptionsComposite_advanced_options;
	public static String		BufferOptionsComposite_bufferOptionsGroup_text;
	public static String		BufferOptionsComposite_checkMergeResults_text;
	public static String		BufferOptionsComposite_chekMergeResults_tooltip;
	public static String		BufferOptionsComposite_groupUnits_text;
	public static String		BufferOptionsComposite_labelBufferWidth_text;
	public static String		BufferOptionsComposite_labelQuadrantSegments_text;
	public static String		BufferOptionsComposite_Circular_Approximation_tooltip;
	// TODO public static String BufferOptionsComposite_layer_crs_units;
	public static String		BufferOptionsComposite_operationDescription;
	public static String		BufferOptionsComposite_options;
	public static String		BufferOptionsComposite_radioLayerUnits_text;
	public static String		BufferOptionsComposite_radioMapUnits_text;
	public static String		BufferOptionsComposite_radioLayerUnits_tooltip;
	public static String		BufferOptionsComposite_radioMapUnits_tooltip;
	public static String		BufferOptionsComposite_radioSpecifyUnits_text;
	// TODO public static String BufferOptionsComposite_units;
	public static String		BufferOptionsPage_messageIncompatibleUnits;
	public static String		BufferPage_description;
	public static String		BufferPage_title;
	public static String		BufferProcess_adding_feature_to_store;
	public static String		BufferProcess_canceled;
	public static String		BufferProcess_error_getting_selected_features;
	public static String		BufferProcess_failed_creating_temporal_store;
	public static String		BufferProcess_failed_transforming;
	public static String		BufferProcess_failed_transforming_feature_to_crs;
	public static String		BufferProcess_subTask_BufferingFeatureN;
	public static String		BufferProcess_subtaskAddLayerToMap;
	public static String		BufferProcess_subtaskBufferringFeatures;
	public static String		BufferProcess_subtaskGetTargetLayer;
	public static String		BufferProcess_subtastCommittingTransaction;
	public static String		BufferProcess_taskBuffering;
	public static String		BufferWizard_bufferOptionsPage_title;
	public static String		BufferWizard_layerCreationPage_description;
	public static String		BufferWizard_layerCreationPage_title;
	public static String		BufferProcess_failed;

	public static String		ClipProcess_failed_creating_temporal_store;

	public static String		ClipCommand_will_clip_existent_layer;
	public static String		ClipCommand_clip_description;
	public static String		ClipCommand_clipping_and_clipped_must_be_differents;
	public static String		ClipCommand_must_select_clipped_layer;
	public static String		ClipCommand_must_select_clipping_layer;
	public static String		ClipCommand_must_select_result;
	public static String		ClipCommand_parameters_ok;
	public static String		ClipCommand_there_are_not_features_in_clipping_layer;
	public static String		ClipCommand_there_are_not_features_to_clip;
	public static String		ClipComposite_apply_to;
	public static String		ClipComposite_clipping_features;
	public static String		ClipComposite_clipping_layer;
	public static String		ClipComposite_failed_validating_parameters;
	public static String		ClipComposite_features_to_clip;
	public static String		ClipComposite_layer_to_clip;
	public static String		ClipComposite_operation_name;
	public static String		ClipCommand_clipping_and_result_must_be_differents;

	public static String		ClipComposite_using_as_clip;
	public static String		ClipProcess_clip_was_canceled;
	public static String		ClipProcess_clipping_with;
	public static String		ClipProcess_failed;
	public static String		ClipProcess_failed_clipping_feature_collection;
	public static String		ClipProcess_failed_deleting;
	public static String		ClipProcess_failed_executing_reproject;
	public static String		ClipProcess_failed_modifying_feature;
	public static String		ClipProcess_failed_transforming;
	public static String		ClipProcess_failed_creating_new_feature;
	// TODO public static String ClipProcess_reporjecting;
	public static String		ClipProcess_successful;
	public static String		CreateLayerPage_defaultGeometryAttributeTypeName;
	public static String		CreateLayerPage_defaultStringAttributeTypeName;
	public static String		CreateLayerPage_title;
	public static String		CreateNewLayerDialog_create_new_layer;
	// TODO public static String
	// CreateNewLayerDialog_failed_creatin_the_feature_type;
	public static String		CreateNewLayerDialog_failed_getting_new_feature_type;
	public static String		CreateNewLayerDialog_must_set_the_feature_name;
	public static String		CreateNewLayerDialog_must_set_the_geometry;
	public static String		CreateNewLayerDialog_specific_attributes;
	public static String		CreateNewLayerDialog_title;
	public static String		FeatureTypeEditor_newFeatureTypeName;
	public static String		IntersectCommand_description;

	public static String		IntersectCommand_expected_geometries;

	public static String		IntersectCommand_faild_validating_geometry_compatibility;

	public static String		IntersectCommand_first_and_second_must_be_differents;

	public static String		IntersectCommand_first_sectond_and_target_must_be_differents;

	public static String		IntersectCommand_must_select_second_layer;

	public static String		IntersectCommand_must_select_target_layer;

	public static String		IntersectCommand_must_select_the_first_layer;

	public static String		IntersectCommand_must_set_target_layer;

	public static String		IntersectCommand_parameters_ok;

	public static String		IntersectCommand_there_is_not_features_to_intersect_in_first_layer;

	public static String		IntersectCommand_there_is_not_to_intersect_in_second_layer;

	public static String		IntersectComposite_can_not_create_targetFeatureType	= null;
	public static String		IntersectComposite_create;
	public static String		IntersectComposite_failed_validating_parameters;
	public static String		IntersectComposite_first_layer;

	public static String		IntersectComposite_operation_name;
	public static String		IntersectComposite_result;
	public static String		IntersectComposite_second_layer;
	public static String		IntersectComposite_selected_features;
	public static String		IntersectComposite_source;
	public static String		IntersectComposite_target_layer;
	public static String		IntersectProcess_failed_creating_temporal_store;
	public static String		IntersectProcess_intersectin_with;
	public static String		IntersectProcess_intersection_fail;
	public static String		IntersectProcess_successful;
	public static String		LayerSelectionComposite_layerColumn_text;
	public static String		ProjectionValidator_crs_source_can_not_be_null;
	public static String		ProjectionValidator_crs_target_can_not_be_null;
	public static String		ProjectionValidator_impossible_reproject;

	public static String		ResultLayerComposite__duplicated_layer_name;
	public static String		ResultLayerComposite_button_new_layer;
	public static String		ResultLayerComposite_target_label;
	public static String		SOComposite_operation;
	public static String		SOComposite_perform;

	public static String		SpatialJoinGeomComposite_touches;

	public static String		SpatialJoinGeomComposite_within;

	public static String		SpatialJoinMonitor_canceled;

	public static String		SpatialJoinMonitor_successful;

	public static String		SpatialJoinTask_crs_not_found;

	public static String		SpatialJoinTask_failed_inserting;
	// TODO public static String SpatialOperationProcessManager_;
	public static String		SpatialOperationProcessManager_buffer_process;
	public static String		SpatialOperationProcessManager_clip_process;
	public static String		SpatialOperationProcessManager_intersect_process;

	public static String		GeometryCompatibilityValidator_expected_geometry_type;

	public static String		DemoComposite_checkSource_text;

	public static String		DemoComposite_checkSource_tooltip;

	public static String		DemoComposite_checkResult_text;

	public static String		DemoComposite_checkResult_tooltip;

	public static String		SOComposite_show_hide_demo_tooltip;

	public static String		ProjectionValidator_crs_source_unknown;

	public static String		IntersectProcess_canceled;

	public static String		Commit_Error_message;

	public static String		Commit_error_shell_title;

	public static String		PolygonToLayerBegin;

	public static String		PolygonToLayerCanceled;

	public static String		PolygonToLayerSuccessful;

	public static String		SplitCommand_filter_empty;

	public static String		SplitCommand_filter_null;

	public static String		SplitCommand_reference_layer_error;

	public static String		SplitCommand_source_layer_error;

	public static String		SplitComposite_operation_name;

	public static String		SplitComposite_operation_description;

	public static String		SplitComposite_source;

	public static String		SplitComposite_First_Layer;

	public static String		SplitComposite_selected_features;

	public static String		SplitComposite_second_layer;

	public static String		SplitComposite_result;

	public static String		SplitCommand_faild_validating_geometry_compatibility;

	public static String		SplitCommand_first_and_second_must_be_differents;

	public static String		SplitCommand_first_second_and_target_must_be_differents;

	public static String		SplitCommand_must_select_target_layer;

	public static String		SplitCommand_must_select_second_layer;

	public static String		SplitCommand_must_select_the_first_layer;

	public static String		SplitProccess_being_message;

	public static String		SplitProccess_canceled;

	public static String		SplitProccess_done;

	public static String		Composite_tab_folder_basic;

	public static String		Composite_tab_folder_advanced;

	public static String		TargetLayerValidator_source_result_different;

	public static String		TargetLayerValidator_source_result_different_names;

	public static String		TargetLayerValidator_target_collection;

	public static String		SplitProcess_failed_executing_reproject;

	public static String		SplitProcess_failed_deleting;

	public static String		SplitProcess_failed_creating_new_feature;

	public static String		DissolveCommand_postgis_nopk;

	public static String		ResultLayerComposite_target_label_tooltip;

	public static String		SplitCommand_source_polygon_line;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {

	}
}
