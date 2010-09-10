package net.refractions.linecleaner.cleansing.views;


import java.io.File;
import java.net.MalformedURLException;

import net.refractions.linecleaner.cleansing.PerformCleansingAction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;


public class CleansingView extends ViewPart implements ModifyListener {

    private Text filename;
    private double nodeDistanceTolerance = 3.0;
    
    public CleansingView() {
	}

    public void createPartControl(Composite parent) {
        GridData gridData;
        final Composite composite = new Composite(parent, SWT.NULL);

        GridLayout gridLayout = new GridLayout();
        int columns = 3;
        gridLayout.numColumns = columns;
        composite.setLayout(gridLayout);

        gridData = new GridData();

        Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText("Filename:"); //$NON-NLS-1$
        urlLabel.setLayoutData(gridData);

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = 400;

        filename = new Text(composite, SWT.BORDER);
        filename.setLayoutData(gridData);
        
        filename.addModifyListener(this);

        gridData = new GridData();
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        
        Button browse = new Button(composite, SWT.PUSH);
        browse.setText("Browse");
        browse.addSelectionListener(new SelectionListener(){

            public void widgetSelected( SelectionEvent e ) {
                widgetDefaultSelected(e);
            }

            public void widgetDefaultSelected( SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(composite.getShell(), SWT.OPEN);
                fileDialog.setFilterExtensions(new String[]{"*.shp"}); //$NON-NLS-1$
                fileDialog.setFilterNames(new String[]{"Shapefiles (*.shp)"}); //$NON-NLS-1$
                String result = fileDialog.open();
                if (result != null)
                    filename.setText(result);
            }

        });
        
        Button go = new Button(composite, SWT.PUSH);
        go.setText("Perform Cleansing");
        go.addSelectionListener(new SelectionListener(){
        

            public void widgetDefaultSelected( SelectionEvent e ) {
                widgetSelected(e);
            }
        
            public void widgetSelected( SelectionEvent e ) {
                System.out.println("Performing Cleansing on file '"+filename.getText()+"'!");
                File file = new File(filename.getText());
//                try {
//                    PerformCleansingAction action = new PerformCleansingAction(file.toURL(), nodeDistanceTolerance);
//                    action.run();
//                } catch (MalformedURLException e1) {
//                    // TODO Handle MalformedURLException
//                    throw (RuntimeException) new RuntimeException( ).initCause( e1 );
//                } catch (Exception e1) {
//                    // TODO Handle Exception
//                    throw (RuntimeException) new RuntimeException( ).initCause( e1 );
//                }
            }
        
        });
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		
	}

    public void modifyText( ModifyEvent e ) {
    }
}