package es.axios.udig.spatialoperations.internal.preferences;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.eclipse.jface.preference.IPreferenceStore;

import es.axios.udig.spatialoperations.Activator;

/**
 * Single access point for spatial operation preferences.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
public class Preferences {

	/**
	 * @return the default width preference for a buffer operation
	 */
	public static double bufferWidth() {
		return getDouble(PreferenceConstants.BUFFER_WIDTH);
	}

	/**
	 * @return the default width preference for a buffer operation
	 */
	public static Unit<?> bufferUnits() {
		String unitId = getString(PreferenceConstants.BUFFER_UNITS);
		Unit<?> unit = SI.METER;
		if (unitId != null && unitId.trim().length() > 0) {
			try {
				unit = Unit.valueOf(unitId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return unit;
	}

	/**
	 * @return wether to use the map, layer or user defined units or the user
	 *         defined units for a buffer operation by default
	 * @see PreferenceConstants#BUFFER_UNITS_LAYER
	 * @see PreferenceConstants#BUFFER_UNITS_MAP
	 * @see PreferenceConstants#BUFFER_UNITS_USER
	 */
	public static String bufferUseUnitsFrom() {
		return getString(PreferenceConstants.BUFFER_USE_UNITS_FROM);
	}

	/**
	 * @return the preference for the number of quadrant segments used to
	 *         approximate curves in a buffer operation by default
	 */
	public static int bufferQuadrantSegments() {
		return getInt(PreferenceConstants.BUFFER_QUADRANT_SEGMENTS);
	}

	public static boolean bufferMergeGeometries() {
		return getBoolean(PreferenceConstants.BUFFER_MERGE_GEOMETRIES);
	}

	public static boolean spatialJoinResultSelection() {
		return getBoolean(PreferenceConstants.SPATIAL_JOIN_RESULT_SELECTION);
	}

	/**
	 * @return whether to use the whole layer if no selection is set
	 */
	public static boolean selectionFallbackToWholeLayer() {
		return getBoolean(PreferenceConstants.SELECTION_FALLBACK_TO_WHOLE_LAYER);
	}

	public static boolean showDemo() {
		return getBoolean(PreferenceConstants.SHOW_DEMO);
	}

	public static void setDemoVisible(boolean value) {
		setBoolean(PreferenceConstants.SHOW_DEMO, value);
	}

	public static boolean isDemoVisible() {
		return getBoolean(PreferenceConstants.SHOW_DEMO);
	}

	/**
	 * The default option on Polygon To Line operation for the
	 * "explode into lines" check option.
	 * 
	 * @return
	 */
	public static boolean polygonToLineExplode() {

		return getBoolean(PreferenceConstants.POLYGONTOLINE_EXPLODE);
	}

	private static int getInt(final String preferenceName) {
		IPreferenceStore store = Preferences.getPreferenceStore();
		int value = store.getInt(preferenceName);
		return value;
	}

	private static boolean getBoolean(final String preferenceName) {
		IPreferenceStore store = Preferences.getPreferenceStore();
		boolean value = store.getBoolean(preferenceName);
		return value;
	}

	private static String getString(final String preferenceName) {
		IPreferenceStore store = Preferences.getPreferenceStore();
		String value = store.getString(preferenceName);
		return value;
	}

	private static double getDouble(final String preferenceName) {
		IPreferenceStore store = Preferences.getPreferenceStore();
		double value = store.getDouble(preferenceName);
		return value;
	}

	private static void setBoolean(final String preferenceName, final boolean value) {
		IPreferenceStore store = Preferences.getPreferenceStore();
		store.setValue(preferenceName, value);
	}

	/**
	 * Provides access to the plugin's preference store
	 * 
	 * @return this plugin's preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		Activator activator = Activator.getDefault();
		IPreferenceStore store = activator.getPreferenceStore();
		return store;
	}
}
