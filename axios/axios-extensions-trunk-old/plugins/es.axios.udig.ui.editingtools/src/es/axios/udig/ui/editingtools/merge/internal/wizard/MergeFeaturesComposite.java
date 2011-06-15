/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.ui.editingtools.merge.internal.wizard;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.ui.editingtools.internal.i18n.Messages;

/**
 * Merge Controls for composite
 * <p>
 * Presents the source features in treevew and result feature in table. The user can especify the
 * merge using this ui.
 * </p>
 * <p>
 * Implementation note:
 * <ul>
 * <li> The event management is handled by a {@link MergeUIController}.
 * <li> The building of the merged feature is handled by a {@link MergeFeatureBuilder}.
 * <li> This class only sets up the UI controls (does not populates the feature views) and registers
 * itself as a {@link MergeFeatureBuilder} change listener to update the wizzard page messages
 * </ul>
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class MergeFeaturesComposite extends Composite {

    private MergeUIController   controller;

    private Table               tableMergeFeature       = null;

    private SashForm            sashForm                = null;
    private Tree                treeFeatures            = null;
    private Composite           compositeMergeFeature   = null;
    private Composite           compositeMergeControls  = null;
    private Composite           compositeSourceFeatures = null;
    private CLabel              cLabelSources           = null;
    private CLabel              cLabelTarget            = null;

    private static final String UNION                   = "Union"; //$NON-NLS-1$
    private Label               labelResultGeometry     = null;

    /**
     * Label showing what the result type of the geometry merge will be
     */
    private Label               labelResult             = null;

    /**
     * This composite's parent wizard page
     */
    private WizardPage          wizardPage              = null;

    /**
     * New instance of MergeFeaturesComposite
     * 
     * @param parent parent containter
     * @param style SWT style
     * @param featureSet source features
     * @param featureType feature type
     */
    public MergeFeaturesComposite( WizardPage wizardPage, Composite parent, int style,
                                    final MergeFeatureBuilder mergeBuilder ) {
        super(parent, style);
        this.wizardPage = wizardPage;

        createControls();
        controller = new MergeUIController(mergeBuilder, treeFeatures, tableMergeFeature);
        controller.initialize();

        mergeBuilder.addChangeListener(new MergeFeatureBuilder.ChangeListener(){

            public void attributeChanged( MergeFeatureBuilder builder, int attributeIndex,
                                          Object oldValue ) {
                if (attributeIndex == builder.getDefaultGeometryIndex()) {
                    mergeGeometryChanged(builder);
                }
            }

        });
        //set up initial feedback
        mergeGeometryChanged(mergeBuilder);
    }

    /**
     * Callback function to report a change in the merged geometry attribute
     * 
     * @param builder
     */
    private void mergeGeometryChanged( MergeFeatureBuilder builder ) {
        Geometry mergedGeometry = builder.getMergedGeometry();
        String geomName;
        if (builder.mergeGeomIsUnion()) {
            geomName = UNION;
        } else {
            geomName = mergedGeometry == null ? "null" : mergedGeometry.getClass().getSimpleName();
        }
        labelResultGeometry.setText(geomName);
        final String msg = MessageFormat.format(Messages.MergeFeaturesComposite_result_will_be,
                                                geomName);

        setMessage(msg, IMessageProvider.INFORMATION);
    }

    /**
     * Presents the message in the standard information area
     * 
     * @param usrMessage
     * @param type defined use the value defined in IMessageProvider (ERROR, WARNING, INFORMATION)
     */
    private void setMessage( String usrMessage, final int type ) {

        this.wizardPage.setMessage(usrMessage, type);
        this.wizardPage.setTitle(Messages.MergeFeaturesComposite_merge_result_title);

    }

    /**
     * Creates controls
     */
    private void createControls() {

        this.setLayout(new FillLayout());

        createSashForm();
        setSize(new Point(560, 260));

    }

    /**
     * This method initializes sashForm
     */
    private void createSashForm() {
        sashForm = new SashForm(this, SWT.V_SCROLL);
        sashForm.setOrientation(SWT.HORIZONTAL);

        sashForm.setLayout(new FillLayout());

        createCompositeSourceFeatures();
        createCompositeMergeFeature();
    }

    /**
     * This method initializes composite
     */
    private void createCompositeMergeFeature() {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = GridData.FILL;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 1;
        gridLayout1.makeColumnsEqualWidth = true;
        compositeMergeFeature = new Composite(sashForm, SWT.BORDER);
        compositeMergeFeature.setLayout(gridLayout1);
        cLabelTarget = new CLabel(compositeMergeFeature, SWT.NONE);
        cLabelTarget.setText(Messages.MergeFeaturesComposite_merge_feature);
        tableMergeFeature = new Table(compositeMergeFeature, SWT.MULTI);
        tableMergeFeature.setHeaderVisible(true);
        tableMergeFeature.setLayoutData(gridData);
        tableMergeFeature.setLinesVisible(true);
        createCompositeMergeControls();
        TableColumn tableColumnName = new TableColumn(tableMergeFeature, SWT.NONE);
        tableColumnName.setWidth(150);
        tableColumnName.setText(Messages.MergeFeaturesComposite_property);
        TableColumn tableColumnValue = new TableColumn(tableMergeFeature, SWT.NONE);
        tableColumnValue.setWidth(60);
        tableColumnValue.setText(Messages.MergeFeaturesComposite_value);
    }

    /**
     * This method initializes compositeMergeControls
     */
    private void createCompositeMergeControls() {
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.FILL;
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.verticalAlignment = GridData.CENTER;
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.FILL;
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.verticalAlignment = GridData.CENTER;
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.FILL;
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.verticalAlignment = GridData.CENTER;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.makeColumnsEqualWidth = true;
        compositeMergeControls = new Composite(compositeMergeFeature, SWT.NONE);
        compositeMergeControls.setLayout(gridLayout);
        compositeMergeControls.setLayoutData(gridData1);
        labelResult = new Label(compositeMergeControls, SWT.NONE);
        labelResult.setText(Messages.MergeFeaturesComposite_result_geometry);
        labelResult.setLayoutData(gridData2);
        labelResultGeometry = new Label(compositeMergeControls, SWT.NONE);
        labelResultGeometry.setText(Messages.MergeFeaturesComposite_result);
        labelResultGeometry.setLayoutData(gridData3);
    }

    /**
     * This method initializes composite1
     */
    private void createCompositeSourceFeatures() {
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = GridData.FILL;
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.grabExcessVerticalSpace = true;
        gridData4.verticalAlignment = GridData.FILL;
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.makeColumnsEqualWidth = true;
        compositeSourceFeatures = new Composite(sashForm, SWT.BORDER);
        compositeSourceFeatures.setLayout(gridLayout2);
        cLabelSources = new CLabel(compositeSourceFeatures, SWT.NONE);
        cLabelSources.setText(Messages.MergeFeaturesComposite_source);
        treeFeatures = new Tree(compositeSourceFeatures, SWT.SINGLE | SWT.CHECK);
        sashForm.setLayout(new FillLayout());
        treeFeatures.setHeaderVisible(true);
        treeFeatures.setLayoutData(gridData4);
        treeFeatures.setLinesVisible(true);
        TreeColumn treeColumnFeature = new TreeColumn(treeFeatures, SWT.NONE);
        treeColumnFeature.setWidth(150);
        treeColumnFeature.setText(Messages.MergeFeaturesComposite_feature);
        TreeColumn treeColumnValue = new TreeColumn(treeFeatures, SWT.NONE);
        treeColumnValue.setWidth(60);
        treeColumnValue.setText(Messages.MergeFeaturesComposite_value);
    }

} // @jve:decl-index=0:visual-constraint="10,10"
