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

import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;

/**
 * The contents of an MITAB file.
 * 
 * @author Lucas Reed (Refractions Research Inc)
 * @since 1.1.0
 */
@SuppressWarnings("nls")
public class MITABGeoResource extends IGeoResource {
    private URL url;
    private MITABService service;
    private MITABGeoResourceInfo info;

    public MITABGeoResource( MITABService service ) {
        this.service = service;
        File file = this.service.getFile();

        try {
            this.url = new URL(this.service.getIdentifier() + "#" + file.getName());
        } catch (MalformedURLException e) {
            this.service.setMessage(e);
        }
    }

    @Override
    public <T> boolean canResolve( Class<T> adaptee ) {
        return adaptee.isAssignableFrom(FeatureSource.class) || super.canResolve(adaptee);
    }

    @Override
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (null == adaptee) {
            return null;
        }

        if (adaptee.isAssignableFrom(FeatureSource.class)) {
            DataStore ds = this.service.resolve(DataStore.class, monitor);
            String[] a = ds.getTypeNames();
            String featureName = a[0];

            FeatureSource<SimpleFeatureType, SimpleFeature> fs = ds.getFeatureSource(featureName);

            return adaptee.cast(fs);
        }

        return super.resolve(adaptee, monitor);
    }

    public MITABService service( IProgressMonitor monitor ) throws IOException {
        return this.service;
    }
    @Override
    public MITABService parent( IProgressMonitor monitor ) throws IOException {
        return service;
    }
    public URL getIdentifier() {
        return this.url;
    }

    public MITABGeoResourceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        return new MITABGeoResourceInfo(this, monitor);
    }

    public Throwable getMessage() {
        return this.service.getMessage();
    }

    public Status getStatus() {
        return service.getStatus();
    }
}