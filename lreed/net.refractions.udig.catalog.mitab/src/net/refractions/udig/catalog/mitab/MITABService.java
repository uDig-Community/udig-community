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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.URLUtils;

/**
 * Represents the file in the catalogue
 * 
 * @author Lucas Reed, (Refractions Research Inc)
 * @since 1.1.0
 */
@SuppressWarnings("nls")
public class MITABService extends IService {
    private URL                       url;
    private Map<String, Serializable> params;
    private File                      file;
    private Throwable                 message;
    private MITABServiceInfo          info;
    private List<MITABGeoResource>    members;
    private DataStore                 dataStore;

    public MITABService(Map<String, Serializable> params) {
        this.params = params;
        this.url    = (URL)this.params.get(MITABServiceExtension.KEY);
    }

    public Map<String, Serializable> getConnectionParams() {
        return this.params;
    }

    public MITABServiceInfo createInfo(IProgressMonitor monitor) throws IOException {
        return new MITABServiceInfo(this);
    }

    public List<? extends IGeoResource> resources(IProgressMonitor monitor) throws IOException {
        if (null == this.members) {
            synchronized(this) {
                if (null == this.members) {
                    MITABGeoResource dataHandle = new MITABGeoResource(this);
                    this.members = Collections.singletonList(dataHandle);
                }
            }
        }

        return this.members;
    }

    public URL getIdentifier() {
        return this.url;
    }

    public Throwable getMessage() {
        return this.message;
    }

    public void setMessage(Throwable msg) {
        this.message = msg;
    }

    public Status getStatus() {
        if (null != this.message) {
            return Status.BROKEN;
        }

        if (null == this.file) {
            return Status.NOTCONNECTED;
        }

        return Status.CONNECTED;
    }

    public File getFile() {
        if (null == this.file) {
            synchronized(this) {
                if (null == this.file) {
                    try {
                        this.file = URLUtils.urlToFile(this.url);
                    } catch(Throwable t){
                        this.message = t;
                    }

                    if (null == file || false == file.exists()) {
                        this.message = new FileNotFoundException(this.url.toString());
                    }
                }
            }
        }

        return this.file;
    }

    @Override
    public <T> boolean canResolve(Class<T> adaptee) {
        if (null == adaptee) {
            return false;
        }

        return adaptee.isAssignableFrom(File.class)
            || adaptee.isAssignableFrom(DataStore.class)
            || super.canResolve(adaptee);
    }

    @SuppressWarnings("deprecation")
    private synchronized DataStore getDS() throws IOException {
        if (null == this.dataStore) {
            MITABReader reader = null;

            try {
                reader = new MITABReader(this.file);
            } catch(IOException e) {
                this.message = e;
                throw e;
            } catch(Throwable t) {
                this.message = t;
                throw new IOException("Could not connect to to datastore.", t);
            }

            Map<String, Object> connect = new HashMap<String, Object>();
            connect.put("url", reader.getShapeFile().toURL());
            this.dataStore = DataStoreFinder.getDataStore(connect);

            this.message = null;
        }

        return this.dataStore;
    }

    @Override
    public <T> T resolve(Class<T> adaptee, IProgressMonitor monitor) throws IOException {
        if (null == adaptee) {
            throw new NullPointerException("No adaptee.");
        }

        if (null == monitor) {
            monitor = new NullProgressMonitor();
        }

        if (adaptee.isAssignableFrom(DataStore.class)) {
            return adaptee.cast(this.getDS());
        }

        if (adaptee.isAssignableFrom(File.class)) {
            return adaptee.cast(this.getFile());
        }

        return super.resolve(adaptee, monitor);
    }
}