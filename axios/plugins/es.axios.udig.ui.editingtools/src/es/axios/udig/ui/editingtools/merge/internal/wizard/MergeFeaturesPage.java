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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import es.axios.udig.ui.editingtools.internal.i18n.Messages;

/**
 * {@link WizardPage} wrapping a {@link MergeFeaturesComposite} to build the merged SimpleFeature
 * <p>
 * Presents the selected features in tree in this control. the user can select the properties for
 * the result festure
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class MergeFeaturesPage extends WizardPage {

    // data
    private MergeFeatureBuilder     builder;

    // controls
    private MergeFeaturesComposite pageControls = null;

    /**
     * New instance of MergeFeaturesPage
     * 
     * @param pageName
     * @param featureSet
     * @param featureType
     */
    protected MergeFeaturesPage( final String pageName, final MergeFeatureBuilder builder ) {
        super(pageName);

        this.builder = builder;
    }

    /**
     * create controls for merge interactions
     * 
     * @throws OperationFailedException
     */
    public void createControl( Composite parent ) {

        this.pageControls = new MergeFeaturesComposite(this, parent, SWT.NONE, this.builder);

        setControl(this.pageControls);
        setPageComplete(true); // TODO require test in next button
    }

    /**
     * Returns whether the {@link MergeFeatureBuilder merge builder} is able to create the merged
     * feature from its current state.
     */
    @Override
    public boolean isPageComplete() {
        try {
            builder.buildMergedFeature();
            return true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            final String usrMessage = Messages.MergeFeaturesComposite_failed_creating_merge_feature;
            setMessage(usrMessage, IMessageProvider.ERROR);
            return false;
        }
    }
}
