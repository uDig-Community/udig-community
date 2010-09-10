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

import java.util.List;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.operation.builder.AffineTransformBuilder;
import org.geotools.referencing.operation.builder.MappedPosition;
import org.geotools.referencing.operation.builder.MathTransformBuilder;
import org.geotools.referencing.operation.builder.ProjectiveTransformBuilder;
import org.geotools.referencing.operation.builder.RubberSheetBuilder;
import org.geotools.referencing.operation.builder.SimilarTransformBuilder;
import org.geotools.referencing.operation.builder.algorithm.Quadrilateral;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * A comosite of radioboxes for choosing the transformation method.
 *
 * @author jezekjan
 */
class RadioGroupComposite {
    private Button radioAffine;
    private Button radioLinear;
    private Button radioRubber;
    private Button radioProjective;
    Listener listener = new Listener() {
            public void handleEvent(Event event) {
                Button button = (Button) event.widget;

                try {
                    IMap map = ApplicationGIS
                        .getActiveMap();
                    IBlackboard blackboard = map.getBlackboard();
                    blackboard.put(TransformTool.BLACKBOARD_CALCULATOR,
                        (button.getData()));
                    blackboard.put(TransformTool.BLACKBOARD_MATHTRANSFORM,
                        ((MathTransformBuilder)button.getData())
                        .getMathTransform());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

    protected RadioGroupComposite() {
    }

    protected void addRadios(Composite parent, GridData gridData) {
        Group groupRadio = new Group(parent, SWT.NULL);
        groupRadio.setText("Transformation method:");      
        groupRadio.setLayoutData(gridData);

        GridLayout layout3 = new GridLayout();
        layout3.numColumns = 2;
        
        groupRadio.setLayout(layout3);

  
        
        radioAffine = new Button(groupRadio, SWT.RADIO);
        radioAffine.setText("Affine");
        radioAffine.setEnabled(false);        

        radioLinear = new Button(groupRadio, SWT.RADIO);
        radioLinear.setText("Similar");
        radioLinear.setEnabled(false);

        radioRubber = new Button(groupRadio, SWT.RADIO);
        radioRubber.setText("RubberSheet");
        radioRubber.setEnabled(false);
        
        radioProjective = new Button(groupRadio, SWT.RADIO);
        radioProjective.setText("Projective");
        radioProjective.setEnabled(false);

        radioAffine.addListener(SWT.Selection, listener);
        radioLinear.addListener(SWT.Selection, listener);
        radioRubber.addListener(SWT.Selection, listener);
        radioProjective.addListener(SWT.Selection, listener);
        refresh();
    }

    protected void refresh() {
        IMap map = ApplicationGIS.getActiveMap();
        IBlackboard blackboard = map.getBlackboard();

        LayerImpl sourceLayer = (LayerImpl) blackboard.get(TransformTool.BLACKBOARD_SOURCELAYER);

        if (blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER) != null) {
           // DirectPosition[] ptSrc = (DirectPosition[]) blackboard.get(VectorLayerReader.BLACKBOARD_PTSRC);
           // DirectPosition[] ptDst = (DirectPosition[]) blackboard.get(VectorLayerReader.BLACKBOARD_PTDST);
            List<MappedPosition> pts = ( List<MappedPosition>) blackboard.get(VectorLayerReader.BLACKBOARD_PTS);

            try {
                radioAffine.setEnabled(true);
                radioAffine.setData(new AffineTransformBuilder(pts));
            } catch (Exception e) {
                radioAffine.setEnabled(false);
            }
            
            try {
                radioProjective.setEnabled(true);
                radioProjective.setData(new ProjectiveTransformBuilder(pts));
            } catch (Exception e) {
                radioProjective.setEnabled(false);
            }
            
            try {
                radioLinear.setEnabled(true);
                radioLinear.setData(new SimilarTransformBuilder(pts));
                radioLinear.setEnabled(true);
            } catch (Exception e) {
                radioLinear.setEnabled(false);
            }

            try {
                radioRubber.setEnabled(true);
                CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;
                FeatureSource source = (FeatureSource) sourceLayer.getResource(FeatureSource.class,
                        null);
                double dX = source.getBounds().getMaxX() - source.getBounds().getMinX();
                double dY = source.getBounds().getMaxY() - source.getBounds().getMinY();
                
                double maxX = source.getBounds().getMaxX() + dX*0.05;
                double maxY = source.getBounds().getMaxY() + dY*0.05;
                double minX = source.getBounds().getMinX() - dX*0.05;
                double minY = source.getBounds().getMinY() - dY*0.05 ;

                Quadrilateral qaud = new Quadrilateral(new DirectPosition2D(crs,
                            minX, minY), new DirectPosition2D(crs,maxX, minY),
                        new DirectPosition2D(crs,maxX, maxY),
                        new DirectPosition2D(crs,minX, maxY));

                radioRubber.setData(new RubberSheetBuilder(pts, qaud));
            } catch (Exception e) {
               // e.printStackTrace();
                radioRubber.setEnabled(false);
            }
        }
    }
}
