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

import java.io.File;
import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import net.refractions.udig.catalog.IGeoResourceInfo;

/**
 * Description of the contents of an MITAB file.
 * 
 * @author Lucas Reed (Refractions Research Inc)
 * @since 1.1.0
 */
@SuppressWarnings("nls")
public class MITABGeoResourceInfo extends IGeoResourceInfo {
    public MITABGeoResource handle;

    public MITABGeoResourceInfo( MITABGeoResource resource, IProgressMonitor monitor )
            throws IOException {
        this.handle = resource;
        File file = handle.service(null).getFile();
        String fileName = file.getName();
        int split = fileName.lastIndexOf(".");
        this.title = split == -1 ? fileName : fileName.substring(0,split);        
        this.bounds = this.setBounds();
    }

    @SuppressWarnings("unchecked")
    private ReferencedEnvelope setBounds() {
        FeatureSource fs;
        ReferencedEnvelope env0;
        ReferencedEnvelope bounds = null;

        try {
            fs = this.handle.resolve(FeatureSource.class, new NullProgressMonitor());

            env0 = fs.getBounds();
            bounds = (ReferencedEnvelope) env0;

            if (null == bounds) {
                bounds = new ReferencedEnvelope(new Envelope(), this.getCRS());

                FeatureIterator<SimpleFeature> iter = fs.getFeatures().features();

                while( iter.hasNext() ) {
                    SimpleFeature element = iter.next();
                    if (bounds.isNull()) {
                        bounds.init((Coordinate) element.getBounds());
                    } else {
                        bounds.include(element.getBounds());
                    }
                }

                iter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bounds;
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        CoordinateReferenceSystem crs;

        try {
            FeatureSource< ? , ? > fs = this.handle.resolve(FeatureSource.class,
                    new NullProgressMonitor());

            FeatureType ft = fs.getSchema();

            crs = ft.getCoordinateReferenceSystem();
        } catch (Exception e) {
            crs = null;
        }

        return crs;
    }
}