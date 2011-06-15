/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.internal.ui.parameters.dissolve;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * TODO Purpose of 
 * <p>
 *
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public class DissolveComposite extends Composite {

    private Group group = null;
    private CLabel cLabel = null;
    private CCombo cCombo = null;
    private CLabel cLabel1 = null;
    private CCombo cCombo1 = null;
    private Composite composite = null;
    private Table table = null;
    private Group group1;
    private CLabel cLabel7;
    private CCombo cCombo3;
    private CLabel cLabel8;
    private CLabel cLabel9;
    public DissolveComposite( Composite parent, int style ) {
        super(parent, style);
        initialize();
    }

    private void initialize() {
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 1;
        createGroup();
        createResultGroup();
        this.setLayout(gridLayout1);
        setSize(new Point(541, 202));
    }

    /**
     *
     */
    private void createResultGroup() {
        GridData gridData10 = new GridData();
        gridData10.widthHint = 100;
        GridData gridData7 = new GridData();
        gridData7.widthHint = 160;
        GridData gridData6 = new GridData();
        gridData6.horizontalAlignment = GridData.FILL;
        gridData6.grabExcessHorizontalSpace = true;
        gridData6.verticalAlignment = GridData.CENTER;
        GridData gridData5 = new GridData();
        gridData5.horizontalSpan = 2;
        gridData5.verticalAlignment = GridData.CENTER;
        gridData5.widthHint = 120;
        gridData5.horizontalAlignment = GridData.END;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 5;
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.FILL;
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.verticalAlignment = GridData.CENTER;

        group1 = new Group(this, SWT.NONE);
        group1.setText("Result");
        group1.setLayout(gridLayout1);
        group1.setLayoutData(gridData1);
        cLabel7 = new CLabel(group1, SWT.NONE);
        cLabel7.setText("Layer");
        cLabel7.setLayoutData(gridData5);
        cCombo3 = new CCombo(group1, SWT.BORDER);
        cCombo3.setLayoutData(gridData6);
        cLabel8 = new CLabel(group1, SWT.NONE);
        cLabel8.setText("Geometry");
        cLabel8.setLayoutData(gridData7);
        cLabel9 = new CLabel(group1, SWT.NONE);
        cLabel9.setText(" ");
        cLabel9.setLayoutData(gridData10);
        
    
    }

    /**
     * This method initializes group	
     *
     */
    private void createGroup() {
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.FILL;
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.verticalAlignment = GridData.CENTER;
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.FILL;
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.verticalAlignment = GridData.CENTER;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 5;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalSpan = 2;
        gridData.verticalAlignment = GridData.CENTER;
        group = new Group(this, SWT.NONE);
        group.setText("Source");
        group.setLayout(gridLayout);
        group.setLayoutData(gridData);
        cLabel = new CLabel(group, SWT.NONE);
        cLabel.setText("Layer");
        cCombo = new CCombo(group, SWT.BORDER);
        cCombo.setLayoutData(gridData1);
        cLabel1 = new CLabel(group, SWT.NONE);
        cLabel1.setText("Dissolve property");
        cCombo1 = new CCombo(group, SWT.BORDER);
        cCombo1.setLayoutData(gridData2);
        Label filler = new Label(group, SWT.NONE);
        createComposite();
    }

    /**
     * This method initializes composite	
     *
     */
    private void createComposite() {
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = GridData.FILL;
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.verticalSpan = 2;
        gridData4.verticalAlignment = GridData.CENTER;
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 1;
        gridLayout2.makeColumnsEqualWidth = true;
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.FILL;
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.horizontalSpan = 5;
        gridData3.verticalAlignment = GridData.CENTER;
        composite = new Composite(group, SWT.NONE);
        composite.setLayoutData(gridData3);
        composite.setLayout(gridLayout2);
        table = new Table(composite, SWT.NONE);
        table.setHeaderVisible(true);
        table.setLayoutData(gridData4);
        table.setLinesVisible(true);
        TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setWidth(60);
        tableColumn.setText("Source property");
        TableColumn tableColumn1 = new TableColumn(table, SWT.NONE);
        tableColumn1.setWidth(60);
        tableColumn1.setText("Statistic");
        TableColumn tableColumn2 = new TableColumn(table, SWT.NONE);
        tableColumn2.setWidth(60);
        tableColumn2.setText("Result property name");

    
        
    
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
