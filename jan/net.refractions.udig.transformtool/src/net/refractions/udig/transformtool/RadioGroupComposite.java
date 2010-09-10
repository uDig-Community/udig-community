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

import java.io.IOException;
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
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.builder.AdvancedAffineBuilder;
import org.geotools.referencing.operation.builder.AffineTransformBuilder;
import org.geotools.referencing.operation.builder.IDWGridBuilder;
import org.geotools.referencing.operation.builder.MappedPosition;
import org.geotools.referencing.operation.builder.ProjectiveTransformBuilder;
import org.geotools.referencing.operation.builder.RSGridBuilder;
import org.geotools.referencing.operation.builder.SimilarTransformBuilder;
import org.geotools.referencing.operation.builder.TPSGridBuilder;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;


/**
 * A composite of radioboxes for choosing the transformation method.
 *
 * @author jezekjan
 */
class RadioGroupComposite {
    private Button radioAffine;
    private Button radioAffine5;
    private Button radioLinear;
    private Button radioRubber;
    private Button radioProjective;
    private Button radioIDW;
    private Button radioTPS;
    private InterpolationSettings settings = null;
    Listener listener = new Listener() {
            public void handleEvent(Event event) {
                Button button = (Button) event.widget;

                try {
                    IMap map = ApplicationGIS.getActiveMap();
                    IBlackboard blackboard = map.getBlackboard();
                    blackboard.put(TransformTool.BLACKBOARD_CALCULATOR, (button.getData()));
                  } catch (Exception e) {
                    e.printStackTrace();
                }

                if (settings != null) {
                    settings.refresh();
                }
            }
        };

    Listener GridListener = new Listener() {
            public void handleEvent(Event event) {
                Button button = (Button) event.widget;

                try {
                    IMap map = ApplicationGIS.getActiveMap();
                    IBlackboard blackboard = map.getBlackboard();
                    blackboard.put(TransformTool.BLACKBOARD_CALCULATOR, (button.getData()));
                        } catch (Exception e) {
                    e.printStackTrace();
                }

                if (settings != null) {
                    settings.refresh();
                }
            }
        };

    protected void addRadios(Composite parent, GridData gridData, InterpolationSettings interp) {
    
        this.settings = interp;

        Group groupRadio = new Group(parent, SWT.NULL);
        groupRadio.setText("Select Transformation:");

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;

        groupRadio.setLayout(layout);
        groupRadio.setLayoutData(gridData);

        GridData thisGridData = new GridData();

        radioLinear = new Button(groupRadio, SWT.RADIO);
        radioLinear.setText("Similar");
        radioLinear.setEnabled(false);
        radioLinear.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        radioAffine = new Button(groupRadio, SWT.RADIO);
        radioAffine.setText("Affine");
        radioAffine.setEnabled(false);
        radioAffine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        radioAffine5 = new Button(groupRadio, SWT.RADIO);
        radioAffine5.setText("Affine5");
        radioAffine5.setEnabled(false);
        radioAffine5.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        radioProjective = new Button(groupRadio, SWT.RADIO);
        radioProjective.setText("Projective");
        radioProjective.setEnabled(false);
        radioProjective.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
        radioIDW = new Button(groupRadio, SWT.RADIO);
        radioIDW.setText("IDW");
        radioIDW.setEnabled(false);
        radioIDW.setLayoutData(thisGridData);

        radioTPS = new Button(groupRadio, SWT.RADIO);
        radioTPS.setText("TPS");
        radioTPS.setEnabled(false);
        radioTPS.setLayoutData(thisGridData);

        radioRubber = new Button(groupRadio, SWT.RADIO);
        radioRubber.setText("RubberSheet");
        radioRubber.setEnabled(false);
        radioRubber.setLayoutData(thisGridData);

        radioAffine.addListener(SWT.Selection, listener);
        radioAffine5.addListener(SWT.Selection, listener);
        radioLinear.addListener(SWT.Selection, listener);     
        radioProjective.addListener(SWT.Selection, listener);
        radioTPS.addListener(SWT.Selection, GridListener);
        radioIDW.addListener(SWT.Selection, GridListener);
        radioRubber.addListener(SWT.Selection, GridListener);

        refresh();
        gridData = new GridData();
    }

    protected void refresh() {
        IMap map = ApplicationGIS.getActiveMap();
        IBlackboard blackboard = map.getBlackboard();

        LayerImpl sourceLayer = (LayerImpl) blackboard.get(TransformTool.BLACKBOARD_SOURCELAYER);

        if (blackboard.get(TransformTool.BLACKBOARD_VECTORLAYER) != null) {
              List<MappedPosition> pts = (List<MappedPosition>) blackboard.get(VectorLayerReader.BLACKBOARD_PTS);

            try {
                radioAffine.setEnabled(true);
                radioAffine.setData(new AffineTransformBuilder(pts));
            } catch (Exception e) {
                radioAffine.setEnabled(false);
            }
            
            try {
                radioAffine5.setEnabled(true);
                AdvancedAffineBuilder ab = new AdvancedAffineBuilder(pts);
                ab.setConstrain(ab.SXY, 0);
                radioAffine5.setData(ab);
            } catch (Exception e) {
                radioAffine5.setEnabled(false);
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
                Envelope env = getLayerEnvelope(sourceLayer, 0.05);                
                RSGridBuilder builder = new RSGridBuilder(pts, 1, 1, env,
                        getWorldToGrid(sourceLayer));
                radioRubber.setData(builder);
                radioRubber.setEnabled(true);
                                    
            } catch (Exception e) {
                 e.printStackTrace();
                radioRubber.setEnabled(false);
            }

            try {
            	Envelope env = getLayerEnvelope(sourceLayer, 0.05);
                radioTPS.setEnabled(true);
            
                IDWGridBuilder builder = new IDWGridBuilder(pts, 1, 1, env,
                        getWorldToGrid(sourceLayer));
                radioTPS.setData(builder);
                radioTPS.setEnabled(true);

                //(new GridCoverageFactory()).create("DY", builder.getDyGrid(), env)
                // .show();
            } catch (Exception e) {
                radioTPS.setEnabled(false);
            }

            try {
                MathTransform worldToGrid = null;
                Envelope env = getLayerEnvelope(sourceLayer, 0.05);
                radioIDW.setEnabled(true);
                radioIDW.setData(new TPSGridBuilder(pts, 1, 1, env, getWorldToGrid(sourceLayer)));
                radioIDW.setEnabled(true);
            } catch (Exception e) {
                radioIDW.setEnabled(false);
            }
        }
    }

    private MathTransform getWorldToGrid(LayerImpl layer) {
        try {
            if (layer.getGeoResource().canResolve(GridCoverageReader.class)) {
                GridCoverage2D csource;

                GridCoverageReader reader = (GridCoverageReader) layer.getResource(GridCoverageReader.class,
                        null);

                csource = (GridCoverage2D) reader.read(null);

                return csource.getGridGeometry().getGridToCRS().inverse();
            } else if (layer.getGeoResource().canResolve(FeatureSource.class)) {
                FeatureSource source = (FeatureSource) layer.getResource(FeatureSource.class, null);

                if (DefaultGeographicCRS.class.isAssignableFrom(layer.getCRS().getClass())) {
                    GeneralMatrix M = new GeneralMatrix(3, 3);
                    double[] m0 = { 1000, 0, 0 };
                    double[] m1 = { 0, 1000, 0 };
                    double[] m2 = { 0, 0, 1 };
                    M.setRow(0, m0);
                    M.setRow(1, m1);
                    M.setRow(2, m2);

                    return (MathTransform) ProjectiveTransform.create(M);
                }
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoninvertibleTransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return IdentityTransform.create(2);
    }

    private Envelope getLayerEnvelope(LayerImpl layer, double scale) {
    	Envelope env = null;
        try {
            if (layer.getGeoResource().canResolve(GridCoverageReader.class)) {
                GridCoverageReader reader = (GridCoverageReader) layer.getResource(GridCoverageReader.class,
                        null);

                GridCoverage2D csource = (GridCoverage2D) reader.read(null);

                env = csource.getEnvelope();
            } else if (layer.getGeoResource().canResolve(FeatureSource.class)) {
                FeatureSource source = (FeatureSource) layer.getResource(FeatureSource.class, null);

                
                env = (ReferencedEnvelope) source.getBounds();
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return enlargeEnvelope(env, scale);
       
    }

    public Envelope enlargeEnvelope(Envelope env, double scale) {
		
    	double minx = env.getMinimum(0)  - scale * env.getSpan(0);
    	double miny = env.getMinimum(1)  - scale * env.getSpan(1);
    	double maxx = env.getMaximum(0)  + scale * env.getSpan(0);
    	double maxy = env.getMaximum(1)  + scale * env.getSpan(1);
	
    	ReferencedEnvelope envTarget = new ReferencedEnvelope(minx, maxx, miny, maxy, env.getCoordinateReferenceSystem());
	
		return  envTarget;

	}
    public static void main(String[] args) {
        try {
            final DefaultMathTransformFactory factory = new DefaultMathTransformFactory();
            ParameterValueGroup WarpGridParameters = factory.getDefaultParameters("Warp Grid");
        } catch (NoSuchIdentifierException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
