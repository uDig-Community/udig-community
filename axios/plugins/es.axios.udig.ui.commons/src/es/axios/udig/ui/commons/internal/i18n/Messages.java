package es.axios.udig.ui.commons.internal.i18n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String DialogUtil_title;

    public static String DialogUtil_message;

    public static String DialogUtil_runInBackground;

    public static String GeometryUtil_CannotGetDimension;

    public static String GeometryUtil_DonotKnowHowAdapt;

    public static String GeometryUtil_ExpectedSimpleGeometry;

    public static String GeoToolsUtils_FailCreatingFeature;


    public static String GeoToolsUtils_FeatureTypeName;

    public static String GeoToolsUtils_Geometry;


    public static String GeoToolsUtils_Name;


    public static String GeoToolsUtils_unitName_centimeters;
    public static String GeoToolsUtils_unitName_degrees;
    public static String GeoToolsUtils_unitName_feet;
    public static String GeoToolsUtils_unitName_inches;
    public static String GeoToolsUtils_unitName_kilometers;
    public static String GeoToolsUtils_unitName_meters;
    public static String GeoToolsUtils_unitName_pixels;
    public static String GeoToolsUtils_unitName_yards;

    public static String LayerUtil_CanNotResolveFeatureSource;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
