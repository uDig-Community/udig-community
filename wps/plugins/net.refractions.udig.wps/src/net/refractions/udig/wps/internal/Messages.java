package net.refractions.udig.wps.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "net.refractions.udig.wps.internal.messages"; //$NON-NLS-1$
    public static String WPSServiceExtension_badService;
    public static String WPSServiceExtension_needsKey;
    public static String WPSServiceExtension_nullURL;
    public static String WPSServiceExtension_nullValue;
    public static String WPSServiceExtension_protocol;
    public static String WPSWizardPage_connectionProblem;
    public static String WPSWizardPage_serverConnectionError;
    public static String WPSWizardPage_title;
    public static String WPSServiceImpl_broken;
    public static String WPSWizardPage_error_invalidURL;
    public static String WPSServiceImpl_could_not_connect;
    public static String WPSWizardPage_label_url_text;
    public static String WPSServiceImpl_connecting_to;
    public static String WPSProcessImpl_acquiring_task;
    public static String WPSProcessImpl_downloading_icon;
    public static String WPSProcessView_noProcessSet;
    public static String WPSProcessView_noInputs;
    public static String WPSProcessView_inputsLabel;
    public static String WPSProcessView_addButton;
    public static String WPSProcessView_execButton;
    public static String WPSProcessView_remButton;
    public static String WPSProcessView_addToolTip;
    public static String WPSProcessView_remToolTip;
    public static String WPSProcessView_importButton;
    public static String WPSProcessView_importToolTip;
    public static String WPSProcessView_consoleLabel;
    public static String WPSProcessView_consoleDefaultText;
    public static String WPSProcessView_consoleBadInput;
    public static String WPSPropertySource_clickToAdd;
    public static String WPSPropertySource_categoryname;
    public static String WPSWizardPage_url_field_label;
    public static String WPSExecute_tooFewParameters;
    public static String WPSExecute_tooGreatParameters;
    public static String WPSExecute_layerCreationError;
    public static String WPSExecute_invalidInput;
    public static String WPSExecute_result;
    public static String WPSExecute_noResults;
    public static String WPSExecute_sendingRequest;
    public static String WPSExecute_gotResponse;
    public static String WPSExecute_creatingScratchLayer;
    public static String WPSExecute_scratchLayerBase;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
