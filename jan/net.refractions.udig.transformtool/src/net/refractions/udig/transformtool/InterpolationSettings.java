/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package net.refractions.udig.transformtool;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.impl.LayerImpl;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.image.WorldImageWriter;
import org.geotools.referencing.operation.builder.MathTransformBuilder;
import org.geotools.referencing.operation.builder.WarpGridBuilder;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


public class InterpolationSettings {
    Button coverageButton;
    Text rowsNum;
    Text cellsNum;
    Listener listener = new Listener() {
            public void handleEvent(Event event) {
                //System.out.print(rowsNum.getText());
                IMap map = ApplicationGIS.getActiveMap();
                IBlackboard blackboard = map.getBlackboard();

                WarpGridBuilder builder = (WarpGridBuilder) blackboard.get(TransformTool.BLACKBOARD_CALCULATOR);
                builder.setHeight(Integer.parseInt(rowsNum.getText()));
                builder.setWidth(Integer.parseInt(cellsNum.getText()));

              
                    blackboard.put(TransformTool.BLACKBOARD_CALCULATOR, builder);
                  //  blackboard.put(TransformTool.BLACKBOARD_MATHTRANSFORM,
                  //      builder.getMathTransform());
            
            }
        };

    public boolean generateCoverage() {
        return coverageButton.getSelection();
    }

    protected void addWidgets(Composite parent, GridData gridData) {
        Group groupRadio = new Group(parent, SWT.NULL);
        groupRadio.setText("Interpolation Settings:");

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;

        groupRadio.setLayout(layout);
        groupRadio.setLayoutData(gridData);

    
   

        (new Label(groupRadio, SWT.NULL)).setText("Number of grid cells");
        cellsNum = new Text(groupRadio, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 1;
        cellsNum.setLayoutData(gridData);
        cellsNum.addListener(SWT.Modify, listener);

        (new Label(groupRadio, SWT.NULL)).setText("Number of grid rows");
        rowsNum = new Text(groupRadio, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 1;
        rowsNum.setLayoutData(gridData);        
        rowsNum.addListener(SWT.Modify, listener);
        
        coverageButton = new Button(groupRadio, SWT.BUTTON1);
        coverageButton.setText("Generate coverage");

        coverageButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    IMap map = ApplicationGIS.getActiveMap();
                    IBlackboard blackboard = map.getBlackboard();

                    WarpGridBuilder builder = (WarpGridBuilder) blackboard.get(TransformTool.BLACKBOARD_CALCULATOR);

                    try {
                        Shell shell = Display.getDefault().getActiveShell();
                     /*   GridCoverage coverageDy = (new GridCoverageFactory()).create("DY",
                                builder.getDyGrid(), builder.getEnvelope());
                        GridCoverage coverageDx = (new GridCoverageFactory()).create("DX",
                                builder.getDxGrid(), builder.getEnvelope());
                     */
                        
                        Color[] colors = new Color[] {
                                Color.BLUE,  Color.WHITE, Color.RED
                            };
                        GridCoverage2D coverageDx = (new GridCoverageFactory()).create("Interpolated Coverage",
                                builder.getDxRaster(),builder.getEnvelope(), null, null, null,
                                new Color[][] { colors }, null);
                        
                        GridCoverage2D coverageDy = (new GridCoverageFactory()).create("Interpolated Coverage",
                                builder.getDyRaster(),builder.getEnvelope(), null, null, null,
                                new Color[][] { colors }, null);


                         
                       // ((GridCoverage2D)coverage).show();
                        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
                        dialog.setText("Directory for generated images (File names will be generated automatically)");                          

                        String dir = dialog.open();
                        String xPath = dir+"/dx.png";
                        String yPath = dir+"/dy.png";
                        
                        WorldImageWriter writer = new WorldImageWriter((Object) (new File(xPath)));
                        writer.write(coverageDx, null);
                        DialogUtility.addURLToMap(new URL("file://"+xPath));
                        
                        writer = new WorldImageWriter((Object) (new File(yPath)));
                        writer.write(coverageDy, null);
                        DialogUtility.addURLToMap(new URL("file://"+yPath));

                        
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (FactoryException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });

        
        GridData gridDat = new GridData();
        gridDat.horizontalSpan = 2;
        gridDat.horizontalAlignment= SWT.FILL;
        coverageButton.setLayoutData(gridDat);
        
        refresh();
    }

    protected void refresh() {
        IMap map = ApplicationGIS.getActiveMap();
        IBlackboard blackboard = map.getBlackboard();

        LayerImpl sourceLayer = (LayerImpl) blackboard.get(TransformTool.BLACKBOARD_SOURCELAYER);

        MathTransformBuilder builder = (MathTransformBuilder) blackboard.get(TransformTool.BLACKBOARD_CALCULATOR);

        if ((builder != null) && WarpGridBuilder.class.isAssignableFrom(builder.getClass())) {
            coverageButton.setEnabled(true);
            rowsNum.setEnabled(true);
            cellsNum.setEnabled(true);
        } else {
            coverageButton.setEnabled(false);
            rowsNum.setEnabled(false);
            cellsNum.setEnabled(false);
        }
    }
}
