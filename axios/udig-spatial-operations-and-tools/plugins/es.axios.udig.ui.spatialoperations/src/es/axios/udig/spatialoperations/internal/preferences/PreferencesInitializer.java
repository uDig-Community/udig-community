package es.axios.udig.spatialoperations.internal.preferences;

import javax.measure.unit.SI;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for the <code>org.eclipse.core.runtime.preferences</code>
 * extension point that loads the system default values for this plug in
 * preferences.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

	public PreferencesInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Preferences.getPreferenceStore();
		store.setDefault(PreferenceConstants.BUFFER_MERGE_GEOMETRIES, false);
		store.setDefault(PreferenceConstants.BUFFER_WIDTH, 1D);
		store.setDefault(PreferenceConstants.BUFFER_UNITS, SI.METER.toString());
		store.setDefault(PreferenceConstants.BUFFER_USE_UNITS_FROM, PreferenceConstants.BUFFER_UNITS_LAYER);
		store.setDefault(PreferenceConstants.BUFFER_QUADRANT_SEGMENTS, 8);
		store.setDefault(PreferenceConstants.SELECTION_FALLBACK_TO_WHOLE_LAYER, true);
		store.setDefault(PreferenceConstants.SPATIAL_JOIN_RESULT_SELECTION, false);
		store.setDefault(PreferenceConstants.SHOW_DEMO, true);
		store.setDefault(PreferenceConstants.POLYGONTOLINE_EXPLODE, false);
	}

}
