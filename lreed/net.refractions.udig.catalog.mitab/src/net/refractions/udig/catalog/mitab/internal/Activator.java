/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2008, AmanziTel
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

package net.refractions.udig.catalog.mitab.internal;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * UI integration plug-in class.
 * <p>
 * This class is used to access the preferenceStore used to 
 * record the installation directory of OGR.
 * </p>
 *
 * @author Lucas Reed, (Refractions Research Inc)
 * @since 1.1.0
 */
@SuppressWarnings("nls")
public class Activator extends AbstractUIPlugin {
    private static Activator instance;

    private IPreferenceStore preferenceStore;

    public static Activator getInstance() {
        return instance;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        instance = new Activator();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        instance = null;
    }

    @Override
    public String toString() {
        return "MITAB Activator instance " + this.hashCode();
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        if (null == this.preferenceStore) {
            this.preferenceStore = new ScopedPreferenceStore(new InstanceScope(),
                    Plugin.PLUGIN_PREFERENCE_SCOPE);
        }

        return this.preferenceStore;
    }
}