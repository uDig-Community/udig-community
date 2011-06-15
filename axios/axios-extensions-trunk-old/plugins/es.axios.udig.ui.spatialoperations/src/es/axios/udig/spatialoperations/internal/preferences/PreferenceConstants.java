package es.axios.udig.spatialoperations.internal.preferences;


public interface PreferenceConstants {

    /** value for {@link #BUFFER_USE_UNITS_FROM} to use the map's units */
    String BUFFER_UNITS_MAP = "map"; //$NON-NLS-1$
    /** value for {@link #BUFFER_USE_UNITS_FROM} to use the layer's units */
    String BUFFER_UNITS_LAYER = "layer"; //$NON-NLS-1$
    /** value for {@link #BUFFER_USE_UNITS_FROM} to use the user defined units */
    String BUFFER_UNITS_USER = "user"; //$NON-NLS-1$

    String BUFFER_WIDTH = "BUFFER_WIDTH"; //$NON-NLS-1$
    String BUFFER_UNITS = "BUFFER_UNITS"; //$NON-NLS-1$
    String BUFFER_USE_UNITS_FROM = "BUFFER_USE_UNITS_FROM"; //$NON-NLS-1$
    String SELECTION_FALLBACK_TO_WHOLE_LAYER = "SELECTION_FALLBACK_TO_WHOLE_LAYER"; //$NON-NLS-1$
    String BUFFER_QUADRANT_SEGMENTS = "BUFFER_QUADRANT_SEGMENTS"; //$NON-NLS-1$
    String BUFFER_MERGE_GEOMETRIES = "BUFFER_MERGE_GEOMETRIES"; //$NON-NLS-1$
}
