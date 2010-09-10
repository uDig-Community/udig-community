package net.refractions.linecleaner.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.ContextModel;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.ui.internal.ApplicationGISInternal;
import net.refractions.udig.project.ui.internal.ProjectUIPlugin;
import net.refractions.udig.project.ui.internal.UDIGAdapterFactoryContentProvider;

import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ListSelectionDialog;

public class DataSelectionPage extends WizardPage {

	private static final String TITLE = "Select the data to be used in the operation.";
	private static final String NAME = "Data Selection";

	private static final ImageDescriptor IMAGE = null;
	
	private static final String PRIORITY_TIP = "The order of the layers determines the priority of the features when " +
		"a choice must be made between deleting two features. The feature with the highest " +
		"priority (at the top of the list) will be kept, and the others deleted.";
	
	protected List<ILayer> layers;
	protected org.eclipse.swt.widgets.List layersList;
	protected Button cleanse;
	protected Button clean;
	private Button up;
	private Button down;
	
	public DataSelectionPage(List<ILayer> incomingLayers) {
		super(NAME, TITLE, IMAGE);
		this.layers = new ArrayList<ILayer>(incomingLayers.size());
		for (ILayer layer : incomingLayers) {
			this.layers.add(layer);
		}
	}
	
	private GridData createLeftGridData() {
        GridData leftGridData = new GridData();
        leftGridData.horizontalAlignment = SWT.FILL;
        leftGridData.verticalAlignment = SWT.FILL;
        leftGridData.grabExcessHorizontalSpace = true;
        leftGridData.grabExcessVerticalSpace = true;
        return leftGridData;
	}
	
	private GridData createRightGridData() {
        GridData rightGridData = new GridData();
        rightGridData.horizontalAlignment = SWT.BEGINNING;
        rightGridData.verticalAlignment = SWT.BEGINNING;
        rightGridData.grabExcessHorizontalSpace = false;
        rightGridData.grabExcessVerticalSpace = true;
        return rightGridData;
	}
	
	private GridData createTopGridData() {
		GridData topGridData = new GridData();
		topGridData.horizontalAlignment = SWT.BEGINNING;
		topGridData.verticalAlignment = SWT.BEGINNING;
		topGridData.grabExcessHorizontalSpace = false;
		topGridData.grabExcessVerticalSpace = true;
		return topGridData;
	}
	
	private GridData createBottomGridData() {
		
		GridData bottomGridData = new GridData();
		bottomGridData.horizontalAlignment = SWT.BEGINNING;
		bottomGridData.verticalAlignment = SWT.END;
		bottomGridData.grabExcessHorizontalSpace = false;
		bottomGridData.grabExcessVerticalSpace = false;
		return bottomGridData;
	}

	public void createControl(Composite parent) {
		/*
		 * Grid Layouts and data configuration
		 */
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 2;
		mainLayout.makeColumnsEqualWidth = false;
		
		GridLayout columnGridLayout = new GridLayout();
        columnGridLayout.numColumns = 1;

        /*
         * Composite configurations
         */
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(mainLayout);
        
		Composite left = new Composite(composite, SWT.NULL);
        left.setLayoutData(createLeftGridData());
        left.setLayout(columnGridLayout);
        
        Composite right = new Composite(composite, SWT.NULL);
        right.setLayoutData(createRightGridData());
        right.setLayout(columnGridLayout);
        
        
        /*
         * Other widget configurations
         */
        
        /*
         * Left column
         */
        Label label = new Label(left, SWT.WRAP);
        label.setText("Choose the layers to process:");
        label.setToolTipText(PRIORITY_TIP);
        label.setLayoutData(new GridData());
        
        GridLayout layersGridLayout = new GridLayout(2, false);
        
        /*
         * LayersList sub-Composite
         */
        
        Composite layersComposite = new Composite(left, SWT.NULL);
        layersComposite.setLayoutData(createLeftGridData());
        layersComposite.setLayout(layersGridLayout);
        
        layersList = new org.eclipse.swt.widgets.List(layersComposite, SWT.SINGLE | SWT.BORDER);
        layersList.setToolTipText(PRIORITY_TIP);
       
		for (int i = 0; i < layers.size(); i++) {
			ILayer layer = layers.get(i);
			layersList.add(layer.getName());
		}
		
		layersList.select(0);
		
		layersList.setLayoutData(createLeftGridData());
		layersList.addSelectionListener(new SelectionListener() {
		
			public void widgetDefaultSelected(SelectionEvent e) {
				if (layersList.getSelectionCount() > 0) {
					up.setEnabled(true);
					down.setEnabled(true);
				} else {
					up.setEnabled(false);
					down.setEnabled(false);
				}
				updateButtons();
			}
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		});
		
		Composite layersRightColumn = new Composite(layersComposite, SWT.NULL);
		layersRightColumn.setLayout(new GridLayout(1, false));
		
		Composite layersRightTopRow = new Composite(layersRightColumn, SWT.NULL);
		layersRightTopRow.setLayout(new GridLayout(1, false));
		layersRightTopRow.setLayoutData(createRightGridData());
		
		Composite layersRightBottomRow = new Composite(layersRightColumn, SWT.NULL);
		layersRightBottomRow.setLayout(new GridLayout(1, false));
		layersRightBottomRow.setLayoutData(createRightGridData());
				

        
		/*
		 * Top of the layersList right column 
		 */
		
		up = new Button(layersRightTopRow, SWT.PUSH);
		up.setText("Up");
		up.setLayoutData(createTopGridData());
		up.addSelectionListener(new SelectionListener() {
		
			public void widgetDefaultSelected(SelectionEvent e) {
				String[] items = layersList.getItems();
				
				int index = layersList.getSelectionIndex();
				if (index <= 0) {
					return;
				}
					
				swap (items, index, index-1);
				layersList.setItems(items);
				layersList.setSelection(index-1);
			}
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		});
		
		down = new Button(layersRightTopRow, SWT.PUSH);
		down.setLayoutData(createTopGridData());
		down.setText("Down");
		down.addSelectionListener(new SelectionListener() {
		
			public void widgetDefaultSelected(SelectionEvent e) {
				String[] items = layersList.getItems();
				
				int index = layersList.getSelectionIndex();
				if (index >= items.length-1) {
					return;
				}
				swap (items, index, index+1);
				layersList.setItems(items);
				layersList.select(index+1);
			}
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		});
		
		/*
		 * Bottom of the layersList right column
		 */
		
		Button add = new Button(layersRightBottomRow, SWT.PUSH);
		add.setLayoutData(createBottomGridData());
		add.setText("Add...");
		add.addSelectionListener(new SelectionListener() {
		
			public void widgetDefaultSelected(SelectionEvent e) {
				String message = "Select one or more layers.";
				AdapterFactoryLabelProvider labelProvider = new AdapterFactoryLabelProvider(ProjectUIPlugin.getDefault().getAdapterFactory());
				
		        UDIGAdapterFactoryContentProvider contentProvider = 
		        	new UDIGAdapterFactoryContentProvider(ProjectUIPlugin.getDefault().getAdapterFactory());
				
		        ContextModel contextModel = ApplicationGISInternal.getActiveMap().getContextModel(); 
		        Object input = contextModel;
		        
				ListSelectionDialog dialog = new ListSelectionDialog(
						getWizard().getContainer().getShell(), input
						, contentProvider, labelProvider, message);
				
				int result = dialog.open();
				if (result == Dialog.CANCEL) {
					return;
				}
				
				Object[] chosenLayers = dialog.getResult();
				List<String> items = Arrays.asList(layersList.getItems());
				List<String> results = new ArrayList<String>();
				results.addAll(items);
				
				for (Object obj : chosenLayers) {
					ILayer layer = (ILayer) obj;
					String layerName = layer.getName();
					if (!results.contains(layerName)) {
						results.add(layerName);
						layers.add(layer);
					}
				}
				
				layersList.setItems(results.toArray(new String[results.size()]));
				layersList.setSelection(0);
				updateButtons();
			}
			
		
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		});
		
		Button remove = new Button(layersRightBottomRow, SWT.PUSH);
		remove.setLayoutData(createBottomGridData());
		remove.setText("Remove");
		remove.addSelectionListener(new SelectionListener() {
		
			public void widgetDefaultSelected(SelectionEvent e) {
				List<String> items = Arrays.asList(layersList.getItems());
				List<String> selected = Arrays.asList(layersList.getSelection());
				List<String> result = new ArrayList<String>();
				
				for (String string : items) {
					if (!selected.contains(string)) {
						result.add(string);
					}
				}
				
				layersList.setItems(result.toArray(new String[result.size()]));
				layersList.setSelection(0);
				updateButtons();
			}
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		});
		
		/*
		 * End of LayersList sub-Composite
		 */

		/*
		 * Right column
		 */
		
		label = new Label(right, SWT.WRAP);
		label.setText("Operations to be performed:");
		
		cleanse = new Button(right, SWT.CHECK);
		cleanse.setText("Data Preparation");
		cleanse.setSelection(true);
		cleanse.addSelectionListener(updateButtons);
		
		clean = new Button(right, SWT.CHECK);
		clean.setText("Light Conflation");
		clean.setSelection(true);
		clean.addSelectionListener(updateButtons);
		
		setControl(composite);
	}

	protected void swap(String[] items, int index, int i) {
		String temp = items[index];
		items[index] = items[i];
		items[i] = temp;
	}

	@Override
	public boolean isPageComplete() {
		if (layersList.getSelectionCount() < 1 ) {
			setErrorMessage("At least one or more layers must be chosen.");
			return false;
		}
		
		if (!cleanse.getSelection() && !clean.getSelection()) {
			setErrorMessage("At least one of the operations must be checked.");
			return false;
		}
		setErrorMessage(null);
		return true;
	}
	
	private void updateButtons() {
		getWizard().getContainer().updateButtons();
	}
	
	private UpdateSelectionListener updateButtons = new UpdateSelectionListener();
	
	private class UpdateSelectionListener extends SelectionAdapter {
			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
		
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateButtons();
			}
	}
	
	
}
