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

import java.util.Date;
import net.refractions.udig.catalog.IServiceInfo;

/**
 * Description of an MITAB file.
 *
 * @author Lucas Reed, (Refractions Research Inc)
 * @since 1.1.0
 */
public class MITABServiceInfo extends IServiceInfo {
    public MITABService handle;

    @SuppressWarnings("nls")
    public MITABServiceInfo(MITABService service) {
        this.handle      = service;        
        this.title       = service.getFile().getName();
        this.description = "Property File Service (" + handle.getFile()+ ")";
        this.keywords    = new String[]{"MITAB", "File"};
    }

    public Date getTimestamp() {
        return new Date(this.handle.getFile().lastModified());
    }
}