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

import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Wizard Dialog
 * <p>
 * Displays the merge wizard with the selected features, when the user define the values for the
 * result feature, this method retrieve the new feature and return.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public class FeatureMergeWizardDialog extends WizardDialog {

    private FeatureMergeWizardDialog( Shell parent, FeatureMergeWizard wizard ) {
        super(parent, wizard);
    }

    public static boolean open( final Display display, final MergeFeatureBuilder builder ) {

        final FeatureMergeWizard wizard = new FeatureMergeWizard(builder);

        PlatformGIS.syncInDisplayThread(new Runnable(){
            public void run() {
                Shell parent = display.getActiveShell();

                final FeatureMergeWizardDialog dialog = new FeatureMergeWizardDialog(parent, wizard);
                dialog.setBlockOnOpen(true);
                dialog.open();
            }
        });
        boolean succeeded = !wizard.isCancel();
        return succeeded;
    }

}
