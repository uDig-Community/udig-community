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

import org.eclipse.jface.wizard.Wizard;

import es.axios.udig.ui.editingtools.internal.i18n.Messages;

/**
 * This wizard capture the property values of the merge feature.
 * <p>
 * This wizard presents the features selected in first page. Here the user can pick up the values in
 * the selected features to build the merge feature. Additionally he can select a target layer.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class FeatureMergeWizard extends Wizard {


    private boolean cancelled = false;
    
    // controls
    private MergeFeaturesPage mergePage;

    // data
    private MergeFeatureBuilder builder;

    public FeatureMergeWizard( final MergeFeatureBuilder builder) {
        this.builder = builder;
    }

    /**
     * Adds merge and layer selection page
     */
    @Override
    public void addPages() {
        setHelpAvailable(false);

        final String title = Messages.FeatureMergeWizard_feature_merge;
        this.mergePage = new MergeFeaturesPage(title, this.builder );
        setWindowTitle(this.mergePage.getTitle());
        setHelpAvailable(false);
        setWindowTitle(title);

        addPage(this.mergePage);
    }

    @Override
    public boolean performFinish() {
        boolean ready = this.mergePage.isPageComplete();
        return ready;
    }

    @Override
    public boolean performCancel() {
        cancelled = true;
        return true;
    }

    public boolean isCancel() {
        return cancelled;
    }
}
