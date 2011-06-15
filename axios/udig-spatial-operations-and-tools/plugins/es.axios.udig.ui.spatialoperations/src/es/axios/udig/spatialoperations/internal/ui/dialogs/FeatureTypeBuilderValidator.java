package es.axios.udig.spatialoperations.internal.ui.dialogs;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;

/**
 * Validates the feature type to determine whether it is acceptable and can be
 * created. This is used to determine if the dialog can close.
 * 
 * @since 1.1.0
 */
public interface FeatureTypeBuilderValidator {
	/**
	 * Returns true if the feature type builder is ok and the dialog may close.
	 * Changes to the builder will be reflected in the dialog.
	 * 
	 * @param builder
	 *            builder to validate.
	 * @return true if the feature type builder is ok and the dialog may close
	 */
	boolean validate(SimpleFeatureTypeBuilder builder);
}