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

package net.refractions.udig.catalog.mitab;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.Serializable;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.catalog.URLUtils;

/**
 * Contribute MITAB support to the uDig catalogue as a ServiceExtension
 *
 * @author Lucas Reed, (Refractions Research Inc)
 * @since 1.1.0
 */
@SuppressWarnings("nls")
public class MITABServiceExtension implements ServiceExtension {
    public static final String KEY = "net.refractions.udig.catalog.mitab.url";

    /**
     * Returns either parameters Map or null
     */
    public Map<String, Serializable> createParams(URL url) {
        Map<String, Serializable> params = null;

        try {
            File file = URLUtils.urlToFile(url);

            if (file.exists()) {
                String name  = file.getName();
                int    start = name.lastIndexOf(".");

                if (".tab".equalsIgnoreCase(name.substring(start))) {
                    params = new HashMap<String, Serializable>();

                    params.put(MITABServiceExtension.KEY, url);
                }
            }
        } catch(Throwable t) {
            // Intentionally left blank
        }

        return params;
    }

    public IService createService(URL id, Map<String, Serializable> params) {
        if (null == params) {
            return null;
        }

        if (params.containsKey(MITABServiceExtension.KEY)) {
            return new MITABService(params);
        }

        return null;
    }
}