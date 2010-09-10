package net.refractions.linecleaner.ui;

import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class OutputSelectionPage extends WizardPage {

	protected Text pathText;
	private Button browse;

	protected OutputSelectionPage() {
		super(NAME, TITLE, IMAGE);
	}

	private static final String NAME = "Output";
	private static final String TITLE = "Choose the destination for the output.";
	private static final ImageDescriptor IMAGE = null;
	private static final String DEFAULT_FILE_NAME = "new.shp";

	
	private GridData createEdgeGridData() {
        GridData edgeGridData = new GridData();
        edgeGridData.grabExcessHorizontalSpace = false;
        edgeGridData.grabExcessVerticalSpace = false;
        edgeGridData.horizontalAlignment = SWT.FILL;
        edgeGridData.verticalAlignment = SWT.CENTER;
        return edgeGridData;
	}
	
	private GridData createMiddleGridData() {

        GridData middleGridData = new GridData();
        middleGridData.grabExcessHorizontalSpace = true;
        middleGridData.grabExcessVerticalSpace = false;
        middleGridData.horizontalAlignment = SWT.FILL;
        middleGridData.verticalAlignment = SWT.CENTER;
        return middleGridData;
	}
	
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);


        GridLayout gridLayout = new GridLayout(3, false);
        composite.setLayout(gridLayout);        
                
        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(createEdgeGridData());
        label.setText("Output file location: ");
        label.setToolTipText("This is the location where the final dataset will be saved to.");
        
        pathText = new Text(composite, SWT.BORDER);
        pathText.setLayoutData(createMiddleGridData());
        pathText.setText(System.getProperty("user.home")+System.getProperty("file.separator")
        		+ DEFAULT_FILE_NAME);
        
        pathText.addMouseListener(new MouseListener() {
		
			public void mouseUp(MouseEvent e) {
			}
		
			public void mouseDown(MouseEvent e) {
			}
		
			public void mouseDoubleClick(MouseEvent e) {
				String text = pathText.getText();
				int fileNameBegin = text.lastIndexOf(System.getProperty("file.separator"))+1;
				int fileNameEnd = text.lastIndexOf(".");
				
				pathText.setSelection(fileNameBegin, fileNameEnd);
			}
		
		});
        
        pathText.addKeyListener(new KeyListener() {
		
			public void keyReleased(KeyEvent e) {
				updateButtons();
			}
		
			public void keyPressed(KeyEvent e) {
				updateButtons();
			}
		});
        
        pathText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				updateButtons();
			}
		
			public void focusGained(FocusEvent e) {
				updateButtons();
			}		
		});
        
        browse = new Button(composite, SWT.PUSH);
        browse.setLayoutData(createEdgeGridData());
        browse.setText("Browse...");
        browse.addSelectionListener(new SelectionListener() {
		
			public void widgetDefaultSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getWizard().getContainer().getShell());
				dialog.setMessage("Choose a directory to place the dataset output in.");
				String path = dialog.open();
				if (path == null) { //Cancel pressed
					return;
				}
				
				String filename = "";
				String previousPath =  pathText.getText();
				int index = previousPath.lastIndexOf(System.getProperty("file.separator"));
				if (index >= 0) {
					filename = previousPath.substring(index+1);
				}
				pathText.setText(path+System.getProperty("file.separator")+filename);
			}
		
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		});
        
		setControl(composite);
	}
	
	
	@Override
	public boolean isPageComplete() {
		String filename = pathText.getText();
		File file = new File(filename);
		
		boolean result = true;
		
		if (file.isDirectory()) {
			setErrorMessage("Target file is a directory.");
			result = false;
		} else if (file.exists() && !file.canWrite()) {
			setErrorMessage("Cannot write to " + filename);
			result = false;
		} else if (file.exists()) {
			setErrorMessage(null);
			setMessage("File exists and will be over-written.", IMessageProvider.WARNING);
			result =  true;
		} else {
			setErrorMessage(null);
			setMessage("File does not exist and will be created.", IMessageProvider.INFORMATION);
			result =  true;
		}
		return result;
	}

	public String getFileNoExtension() {
		String text = pathText.getText();
		int index = text.lastIndexOf(".");
		if (index >= 0) {
			return text.substring(0, index);
		}
		return text;
	}

	public String getOutputPath() {
		String text = pathText.getText();
		int index = text.lastIndexOf(System.getProperty("file.separator"));
		if (index >= 0) {
			return text.substring(0, index);
		}
		return text;
	}
	private void updateButtons() {
		getWizard().getContainer().updateButtons();
	}
}
