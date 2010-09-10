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

import org.eclipse.osgi.util.NLS;

/**
 * Localization strings for English
 *
 * @author Lucas Reed, (Refractions Research Inc)
 * @since 1.1.0
 */
public class Messages extends NLS {
    private final static String BUNDLE_NAME = "net.refractions.udig.catalog.mitab.internal.messages"; //$NON-NLS-1$

    public static String Ogr2OgrPreference_selectExecutable;
    public static String Ogr2OgrPreference_fileNotFound;
    public static String Ogr2OgrPreference_browseButton;
    public static String Ogr2OgrPreference_execuables;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}