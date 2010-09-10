/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.transformtool;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.operation.builder.MappedPosition;
import org.geotools.referencing.operation.transform.GeocentricTransform;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;


public class VectorLayerReader implements IRunnableWithProgress {

	public static final String BLACKBOARD_PTS = "net.refractions.udig.transformtool.PTS";	  
  /*  public static final String BLACKBOARD_PTSRC = "net.refractions.udig.transformtool.PTSRC";
    public static final String BLACKBOARD_PTDST = "net.refractions.udig.transformtool.PTDST";
    public static final String BLACKBOARD_PTSRC3D = "net.refractions.udig.transformtool.PTSRC3D";
    public static final String BLACKBOARD_PTDST3D = "net.refractions.udig.transformtool.PTDST3D";
*/
    public VectorLayerReader() {
        super();
      
    }

    public void run(IProgressMonitor monitor)
        throws InvocationTargetException, InterruptedException {
       
        IMap map = ApplicationGIS.getActiveMap();
        List<MappedPosition> pts = new ArrayList();
        
        if (map == null) {
            return;
        }

        IBlackboard blackboard = map.getBlackboard();

        LayerImpl vectorLayer = (LayerImpl) blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER);

        CoordinateReferenceSystem CRS = vectorLayer.getCRS();

        DirectPosition2D[] ptSrc;
        DirectPosition2D[] ptDst;

        try {
            FeatureSource ft = (FeatureSource) vectorLayer.getResource(FeatureSource.class,
                    null);
           
            //System.out.print("............"+vectorLayer.getCRS());
            int i = 0;

            int size = ft.getFeatures().size();
            monitor.beginTask("Reading...", size);

            for (Iterator j = ft.getFeatures().iterator(); j.hasNext();) {
                Feature feature = (Feature) j.next();

                // System.out.println(feature.getDefaultGeometry().getCoordinates());
                Coordinate[] coords = feature.getDefaultGeometry()
                                             .getCoordinates();

                if (feature.getDefaultGeometry().getCoordinates().length > 2) {
                    throw new InterruptedException("Layer reading error");
                }

                CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

               
                pts.add(new MappedPosition(
                		new DirectPosition2D(crs, coords[0].x, coords[0].y),
                        new DirectPosition2D(crs, coords[1].x, coords[1].y)));
                //   ptSrc[i] = new DirectPosition2D(crs,coords[0].x, coords[0].y);

                //   ptDst[i] = new DirectPosition2D(crs,coords[1].x, coords[1].y);
                
                i++;
                monitor.worked(1);

                monitor.subTask("Reading " + i + ". of " + size + " vectors.");

                if (monitor.isCanceled()) {
                    throw new InterruptedException(
                        "The long running operation was cancelled");
                }

                monitor.done();
            }
            
            blackboard.put(this.BLACKBOARD_PTS, pts);
          
        } catch (Exception e) {
            final Display finalDisplay = Display.getDefault();
            Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        Status status = new Status(IStatus.ERROR,
                                "My Plug-in ID", 0,
                                "Vector layer has to contain just Line Strings formed by two points.",
                                null);
                        ErrorDialog.openError(finalDisplay.getActiveShell(),
                            "Error", "Unable to read selected vector layer.",
                            status);
                    }
                });
            monitor.done();
        }
    }

    private DirectPosition[] directPositionToGeocentric(DirectPosition[] point) {
        Envelope[] ptsE = new Envelope[point.length];
        DirectPosition[] DstPoint3D = new DirectPosition[point.length];

        for (int i = 0; i < ptsE.length; i++) {
            //	CoordinateReferenceSystem s= sourceLayer.getCRS().toWKT();
            ptsE[i] = new Envelope(new Coordinate(
                        point[i].getCoordinates()[0],
                        point[i].getCoordinates()[1]));

            try {
                IMap map = ApplicationGIS.getActiveMap();
                IBlackboard blackboard = map.getBlackboard();
                LayerImpl vectorLayer = (LayerImpl) blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER);

                CoordinateReferenceSystem CRS = vectorLayer.getCRS();
                // convert tp Geegraphic
                ptsE[i] = JTS.toGeographic(ptsE[i], CRS);

                DefaultEllipsoid ellipsoid = DefaultEllipsoid.WGS84;
                GeocentricTransform geocentric = new GeocentricTransform(ellipsoid,
                        false);

                double[] point3D = { ptsE[i].getMinX(), ptsE[i].getMinY(), 0 };
                double[] Dst = { 0, 0, 0 };
                geocentric.transform(point3D, 0, Dst, 0, 1);

                DstPoint3D[i] = new GeneralDirectPosition(Dst[0], Dst[1], Dst[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return DstPoint3D;
    }
}
