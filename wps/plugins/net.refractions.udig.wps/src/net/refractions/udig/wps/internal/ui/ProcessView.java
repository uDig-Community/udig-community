/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.wps.internal.ui;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.refractions.udig.catalog.IProcess;
import net.refractions.udig.catalog.IProcessInfo;
import net.refractions.udig.catalog.util.GeoToolsAdapters;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.wps.WPSUtils;
import net.refractions.udig.wps.internal.Messages;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.PropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.geotools.data.Parameter;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * @author gdavis, Refractions Research Inc
 * @author Lucas Reed, Refractions Research Inc
 */
public class ProcessView extends ViewPart {

    public static final String VIEW_ID = "net.refractions.udig.wps.internal.ui.ProcessView"; //$NON-NLS-1$
    private IProcess process;
    private ProcessFactory processFactory;
    private Name processName;
    
    /** Page book used to switch between pages (one for when no processFactory is set) */
    private PageBook book;
    private ScrolledComposite processComp;
    private Composite noProcessComp;
    private PropertySheetPage inputSheetPage;
    private final int inputSheetPageMinHeight = 90;
    
    /**
     * PropertySource gathering up input parameters from the
     * user interface.
     */
    private InputParamPropertySource inputParamPropertySource;
    
    private Button addBtn;
    private Button remBtn;
    private Button importBtn;
    private Text processConsole;
    private PropertySheetEntry currentSelection;
    private ISelectionListener workbenchWatcher;
    private Object workbenchSelection;
    private Set<Class< ? >> adaptableInputTypes;
    private CoordinateReferenceSystem lastFoundCRS;
    private static int scratchLayerCount = 0;
    
    public ProcessView() {
        super();
        this.processFactory = null;
    }    
    
    @Override
    public void dispose() {
        // remove our workbench listener
        if (this.workbenchWatcher != null) {
            getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this.workbenchWatcher);
        }
    }

    public void createPartControl( Composite parent ) {
        // if we don't have a process info then
        // create a default view
        book = new PageBook(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        book.setLayout(gridLayout);
        
        if (this.processFactory != null) {
            createProcessPartControls(book);
            deleteProcessComp(noProcessComp);
            book.showPage(processComp);
        }
        else {
            createDefaultPartControls(book);
            book.showPage(noProcessComp);
        }
        
        // create a selection listener for watching what gets selected in the workbench
        this.workbenchWatcher = new ISelectionListener(){
            public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
                if (selection instanceof IStructuredSelection 
                        && adaptableInputTypes != null && !adaptableInputTypes.isEmpty()) {
                    IStructuredSelection structSel = (IStructuredSelection) selection;
                    for( Iterator itr = structSel.iterator(); itr.hasNext(); ) {
                        Object obj = itr.next();
                        // check if a selection matches one of our adaptable
                        // types, if it does, set it as the last selection and exit
                        Iterator<Class< ? >> iterator = adaptableInputTypes.iterator();
                        while (iterator.hasNext()) {
                            Class< ? > type = iterator.next();
                            if (obj.getClass() == type) {
                                workbenchSelection = obj;
                                return;
                            }
                            else if (obj instanceof IAdaptable) {
                                IAdaptable adaptable = (IAdaptable) obj;
                                Object adapter = adaptable.getAdapter(type);
                                if (adapter != null) {
                                    workbenchSelection = adapter;
                                    return;
                                }
                                // a layer or map can adapt to a feature/geom so
                                // do some magic to catch those cases
                                else if (type == Geometry.class || type == SimpleFeature.class) {
                                    ILayer layerAdapt = (ILayer) adaptable.getAdapter(ILayer.class);
                                    IMap mapAdapt = (IMap) adaptable.getAdapter(IMap.class);
                                    if (layerAdapt == null && mapAdapt != null) {
                                        // get layer
                                        layerAdapt = mapAdapt.getEditManager().getSelectedLayer();
                                    }
                                    if (layerAdapt != null) {
                                        // store the selection and adapt it to a feature/geom
                                        // when the user tries to import it
                                        workbenchSelection = layerAdapt;
                                        return;                                        
                                    }
                                }
                                
                            }
                        }
                    }
                }
            }
        };           
        
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(workbenchWatcher);
    }
    
    /**
     * Create the controls for this view based on the process that is set
     * 
     */    
    private void createProcessPartControls( Composite parent ) {
        // if there is already a composite setup for this, delete it first
        if (processComp != null) {
            deleteProcessComp(processComp);
        }
        processComp = new ScrolledComposite(parent, SWT.V_SCROLL);
        GridLayout gridLayout = new GridLayout(1, false);
        processComp.setLayout(gridLayout);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);        
        processComp.setLayoutData(layoutData);        
        
        Composite innerProcessComp = new Composite(processComp, SWT.FILL | SWT.TOP);
        gridLayout = new GridLayout(1, false);
        innerProcessComp.setLayout(gridLayout);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);        
        innerProcessComp.setLayoutData(layoutData);
        
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false); 

        // create title and desc labels
        Label title = new Label(innerProcessComp, SWT.BOLD);
        title.setText(this.processFactory.getTitle(processName).toString()+": "); //$NON-NLS-1$
        title.setLayoutData(layoutData);
        
        Label desc = new Label(innerProcessComp, SWT.NORMAL | SWT.WRAP);
        desc.setText(this.processFactory.getDescription(processName).toString()); 
        desc.setLayoutData(layoutData);   
        
        Label divider = new Label(innerProcessComp, SWT.NORMAL | SWT.WRAP);
        divider.setText("");  //$NON-NLS-1$
        divider.setLayoutData(layoutData);             
        
        // create and add the input param buttons
        Composite buttComp = new Composite(innerProcessComp, SWT.FILL | SWT.LEFT);
        gridLayout = new GridLayout(4, false);
        gridLayout.marginHeight = 0;
        buttComp.setLayout(gridLayout);
        layoutData = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);   
        buttComp.setLayoutData(layoutData);      
        
        // btn label
        Label inputs = new Label(buttComp, SWT.NORMAL | SWT.WRAP);
        inputs.setText(Messages.WPSProcessView_inputsLabel); 
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false); 
        inputs.setLayoutData(layoutData);                 
        
        // add btn
        addBtn = new Button(buttComp, SWT.PUSH | SWT.FLAT);
        addBtn.setText(Messages.WPSProcessView_addButton); 
        addBtn.setToolTipText(Messages.WPSProcessView_addToolTip);
        layoutData = new GridData(SWT.LEFT, SWT.TOP, false, false);
        addBtn.setLayoutData(layoutData);
        addBtn.addListener(SWT.MouseUp, new Listener(){
            public void handleEvent( Event event ) {
                // add new descriptor and refresh
                Point computeSize = inputSheetPage.getControl().getSize();
                inputParamPropertySource.addNewDescriptor(currentSelection);
                inputSheetPage.getControl().setSize(computeSize);
                inputSheetPage.refresh();
                updateInputButtonsStatus();
            }
        });    
        // button will only be active when a valid selection is selected in the property
        // sheet page
        addBtn.setEnabled(false);
        
        // remove btn
        remBtn = new Button(buttComp, SWT.PUSH | SWT.FLAT);
        remBtn.setText(Messages.WPSProcessView_remButton); 
        remBtn.setToolTipText(Messages.WPSProcessView_remToolTip);
        layoutData = new GridData(SWT.LEFT, SWT.TOP, false, false);
        remBtn.setLayoutData(layoutData);
        remBtn.addListener(SWT.MouseUp, new Listener(){
            public void handleEvent( Event event ) {
                // do remove, first lookup the item to remove
                if (currentSelection != null && inputParamPropertySource.deleteFromDisplayName(currentSelection.getDisplayName())) {
                    inputSheetPage.refresh();
                    updateInputButtonsStatus();
                }
            }
        });    
        // button will only be active when a valid selection is selected in the property
        // sheet page
        remBtn.setEnabled(false);
        
        // import btn
        importBtn = new Button(buttComp, SWT.PUSH | SWT.FLAT);
        importBtn.setText(Messages.WPSProcessView_importButton); 
        importBtn.setToolTipText(Messages.WPSProcessView_importToolTip);
        layoutData = new GridData(SWT.LEFT, SWT.TOP, false, false);
        importBtn.setLayoutData(layoutData);
        importBtn.addListener(SWT.MouseUp, new Listener(){
            public void handleEvent( Event event ) {
                // try import
                if (currentSelection != null) {
                    // try importing whatever the last saved workbench selection was
                    importSelectionToInput(workbenchSelection);
                }
            }
        });    
        // button will only be active when a valid selection is selected in the property
        // sheet page
        importBtn.setEnabled(false);        
        
        // create and add the input param composite
        createInputParamPropertyPage(innerProcessComp, this.processFactory.getParameterInfo(processName));
        
        // console label
        Label consoleLabel = new Label(innerProcessComp, SWT.NORMAL | SWT.WRAP);
        consoleLabel.setText(Messages.WPSProcessView_consoleLabel); 
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false); 
        consoleLabel.setLayoutData(layoutData);          
        
        // create the log console for showing error info and results
        processConsole = new Text(innerProcessComp, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL);
        Display display = PlatformUI.getWorkbench().getDisplay();
        Color color = display.getSystemColor(SWT.COLOR_WHITE);        
        processConsole.setBackground(color);
        color = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        processConsole.setForeground(color);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        layoutData.heightHint = 40;
        processConsole.setLayoutData(layoutData);
        //processConsole.setEnabled(false);
        addConsoleText(Messages.WPSProcessView_consoleDefaultText);

        // create the execute button       
        Button execButton = new Button(innerProcessComp, SWT.PUSH | SWT.FLAT);
        execButton.setText(Messages.WPSProcessView_execButton);
        layoutData = new GridData(SWT.LEFT, SWT.TOP, false, false);
        execButton.setLayoutData(layoutData);
        execButton.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                Map<String, Object> paramValues = inputParamPropertySource.getParamValues();
                if (paramValues != null) {
                    
                    // validate the inputs
                    Map<String, Parameter< ? >> parameterInfo = processFactory.getParameterInfo(processName);
                    Map<String, Object> errors = WPSUtils.checkProcessInputs(parameterInfo, paramValues);

                    StringBuilder str = new StringBuilder("");    //$NON-NLS-1$
                    for(String key : errors.keySet()) {
                        if (0 == str.length()) {
                            str.append("\""+key+"\""); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            str.append(", " + "\""+key+"\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                    }

                    Map<String,Object> input = WPSUtils.toInput( paramValues );

                    // if there are input errors, don't create the request job
                    if (0 != errors.size()) {
                        addConsoleText(Messages.bind(Messages.WPSExecute_invalidInput, str.toString()));
                    }   
                    else {
                        ExecuteJob job = new ExecuteJob(input);
                        job.schedule();
                    }
                }
            }
        });
        
        processComp.setContent(innerProcessComp);
        processComp.setExpandVertical(true);
        processComp.setExpandHorizontal(true);   
        
        // calculate the size of the inner composite and set the min size on the
        // scrollable composite so that the scrollbar works
        Point size = innerProcessComp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        size.y += 1;
        processComp.setMinSize(size);        
    }

    /**
     * Append the given text to the process console
     *
     * @param string
     */
    private void addConsoleText( final String string ) {
        if (processConsole == null || string == null) {
            return;
        }
        
        // ensure we are in the display thread and not blocking
        Display display = PlatformUI.getWorkbench().getDisplay();
        display.asyncExec( new Runnable(){
            public void run() {
                String prevText = processConsole.getText();
                String text = ""; //$NON-NLS-1$
                if (prevText != null && !prevText.equals("")) { //$NON-NLS-1$
                    text += Text.DELIMITER;
                }
                text += "* "+string; //$NON-NLS-1$
                processConsole.append(text);
                processConsole.setFocus();
            }           
        });         
    }

    private class ExecuteJob extends Job {
    	private Map<String, Object> inputs;

    	public ExecuteJob(Map<String, Object> inputs) {
    		super("Execute Request");	//$NON-NLS-1$
    		this.inputs = inputs;
    	}

    	protected IStatus run(IProgressMonitor monitor) {
    	    // Create and send the execute request
        	Process process = processFactory.create(processName);
        	addConsoleText(Messages.WPSExecute_sendingRequest);
        	Map<String, Object> results;
            try {
                results = process.execute(this.inputs, GeoToolsAdapters.progress(monitor));
            } catch (ProcessException e1) {
                addConsoleText( "Failure: "+e1);
                IStatus fail = new Status( IStatus.ERROR, processFactory.getClass().getCanonicalName(), e1.toString() );
                e1.printStackTrace();
                return fail;
            }            
        	addConsoleText(Messages.WPSExecute_gotResponse);
        	if (results == null || results.isEmpty()) {
        		addConsoleText(Messages.WPSExecute_noResults);
        	    return Status.OK_STATUS;
        	}

        	// go through the results
        	for(String key : results.keySet())
        	{
        	    Object obj = results.get(key);
        		if (obj instanceof Geometry)
        		{
        			try {
        			    addConsoleText(Messages.bind(Messages.WPSExecute_result, key, Messages.WPSExecute_creatingScratchLayer));
        			    scratchLayerCount++;
        				WPSUtils.createScratchLayer((Geometry)obj, lastFoundCRS, monitor, scratchLayerCount);
        			} catch(Exception e) {
        				addConsoleText(Messages.WPSExecute_layerCreationError);
        				return Status.OK_STATUS;
        			}
        		} else {
        			addConsoleText(Messages.bind(Messages.WPSExecute_result, key, obj.toString()));
        		}
        	}

    		return Status.OK_STATUS;
    	}
    }

    /**
     * Try to import the current workbench selection into the current input selection 
     * in the tree view
     */
    private void importSelectionToInput(Object obj) {
        if (currentSelection == null) {
            return;
        }
        // get the input parameter
        String category = currentSelection.getCategory();
        String displayName = currentSelection.getDisplayName();
        String lookupname = displayName;
        if (category != null && !category.equals("")) { //$NON-NLS-1$
            lookupname = category;
        }
        Parameter< ? > param = this.inputParamPropertySource.getParamFromName(lookupname);        
        
        // see if the object to import matches the type expected by the param
        String value = null;
        
        if (param.type == Geometry.class) {
            // If we have a layer, try getting a feature from it
            if (obj instanceof ILayer) {
                ILayer layer = (ILayer) obj;
                SimpleFeature feature = WPSUtils.getSelectedFeatureFromLayer(layer);
                if (feature != null) {
                    obj = feature;
                }    
            }
            // make sure we can turn the object into a geometry WKT String
            if (obj instanceof SimpleFeature) {
                SimpleFeature feature = (SimpleFeature) obj;
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                // store the CRS as the last found one for any results we try to display
                lastFoundCRS = feature.getFeatureType().getCoordinateReferenceSystem();
                if (geom != null) {
                    WKTWriter writer = new WKTWriter();
                    value = writer.write(geom);
                }
                else {
                    value = obj.toString();
                }
            } 
            else if (obj instanceof Geometry) {
                Geometry geom = (Geometry) obj;
                WKTWriter writer = new WKTWriter();
                value = writer.write(geom);              
            } 
            else if (obj instanceof String) {
                value = (String) obj;
            } 
        }
        else {
            if( obj != null ){
                // fall back on toString
                value = obj.toString();
            }
        }
        
        // try setting the value manually, and refreshing
        String id = this.inputParamPropertySource.getPropertyIdFromDisplayName(displayName);
        if (value != null) {
            this.inputParamPropertySource.setPropertyValue(id, value);
            // refresh the property sheet to show the new value
            this.inputSheetPage.selectionChanged(null, null);
            this.inputSheetPage.refresh();
        }
        else {
            addConsoleText(Messages.WPSProcessView_consoleBadInput);
        }
        
    }
    
    /**
     * Create the property page for input params (delete and recreate if it already exists)
     *
     * @param parent
     * @param parameterInfo
     */
    private void createInputParamPropertyPage( Composite parent,
            Map<String, Parameter< ? >> parameterInfo ) {
        if (this.inputSheetPage != null ) {
            this.inputSheetPage.dispose();
        }
        this.inputSheetPage = new PropertySheetPage();
        this.inputSheetPage.createControl(parent);
        
        // create each new entry in the sheet page for the inputs
        this.inputParamPropertySource = new InputParamPropertySource(parameterInfo, null);
        addInputSelection(this.inputParamPropertySource);

        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.minimumHeight = this.inputSheetPageMinHeight;
        this.inputSheetPage.getControl().setLayoutData(layoutData); 
        
        // add a new selection listener for when the sheet page selection changes
        inputSheetPage.getControl().addListener(SWT.Selection, 
                new Listener(){
                    public void handleEvent( Event event ) {
                        // remember the current selection
                        Widget item = event.item;
                        if( item == null ){
                            return; // no data
                        }
                        Object data = item.getData();
                        if (data instanceof PropertySheetEntry) {
                            currentSelection = (PropertySheetEntry) data;
                        }
                        else {
                            // selection is not a valid propertysheetentry
                            // (maybe a propertysheetcategory, etc)
                            currentSelection = null;
                        }
                        updateInputButtonsStatus();
                    }
                });     
    }
    
    /**
     * Updated the enabled status of the add/remove buttons based on the
     * current selection
     */
    private void updateInputButtonsStatus() {
        if (currentSelection == null) {
            this.addBtn.setEnabled(false);
            this.remBtn.setEnabled(false);
            this.importBtn.setEnabled(false);
            return;
        }
        // figure out what param we are refering to from the current selection
        String category = currentSelection.getCategory();
        String displayName = currentSelection.getDisplayName();
        String lookupname = displayName;
        if (category != null && !category.equals("")) { //$NON-NLS-1$
            lookupname = category;
        }
        Parameter< ? > param = this.inputParamPropertySource.getParamFromName(lookupname);
        if (param == null) {
            this.addBtn.setEnabled(false);
            this.remBtn.setEnabled(false);
            this.importBtn.setEnabled(false);
            return;
        }        
        
        // if there is a valid selected input value below, activate the import
        // button (it will resolve if it can import what is selected on the map
        // when clicked)
        if (currentSelection != null) {
            this.importBtn.setEnabled(true);
        }
        else {
            this.importBtn.setEnabled(false);
        }        
        
        // if the param allows more values than are currently on the form, activate the
        // add button
        int count = this.inputParamPropertySource.getCategoryCount(param.title.toString());
        if (count == 0) {
            this.addBtn.setEnabled(false);
            this.remBtn.setEnabled(false);
            return;
        }        
        
        if (count < param.maxOccurs) {
            this.addBtn.setEnabled(true);
        }
        else {
            this.addBtn.setEnabled(false);
        }
        
        // if the param has more values than the minimum, activate the remove button
        if (count > param.minOccurs) {
            this.remBtn.setEnabled(true);
        }
        else {
            this.remBtn.setEnabled(false);
        }
    }

    /**
     * Add a new input param selection to the property sheet page
     *
     * @param ipps
     */
    private void addInputSelection( InputParamPropertySource ipps ) {
        StructuredSelection selection;
        Object value = defaultSource;
        if( ipps!=null )
            value=ipps;
        else
            value=defaultSource;
        selection=new StructuredSelection(value);
        this.inputSheetPage.selectionChanged(null, selection);        
    }

    /**
     * Delete the given Composite
     * @param comp 
     */
    private void deleteProcessComp(Composite comp) {
        if (comp == null) return;
        comp.dispose();
        comp = null;
    }

    /**
     * Create the default controls for this view based on the fact that no
     * process is set
     * 
     */
    private void createDefaultPartControls(Composite parent) {
        GridData gridData;
        noProcessComp = new Composite(parent, SWT.FILL);

        GridLayout gridLayout = new GridLayout();
        int columns = 1;
        gridLayout.numColumns = columns;
        noProcessComp.setLayout(gridLayout);

        gridData = new GridData(GridData.FILL_HORIZONTAL);

        Label title = new Label(noProcessComp, SWT.NORMAL);
        //title.setAlignment(SWT.LEFT);
        title.setText(Messages.WPSProcessView_noProcessSet); 
        title.setLayoutData(gridData);
    }

    public void setFocus() {
    }

    /**
     * Set the process for this view, which also creates a job to
     * fetch the process info and build the view's widgets based on
     *
     * @param proc the IProcess to build this view based around
     * @throws IOException 
     */
    public void setProcess( IProcess proc ) {
        this.process = proc;
        GetProcessInfoJob job = new GetProcessInfoJob();
        job.setProcess(proc);
        job.schedule();
    }
    
    /**
     * Job for fetching the processInfo and setting this view's controls
     * 
     * @author GDavis, Refractions Research Inc.
     */
    private class GetProcessInfoJob extends Job {

        private IProcess proc;
        
        public GetProcessInfoJob() {
            super("Get ProcessInfo Job"); //$NON-NLS-1$
        }
        
        public void setProcess(IProcess proc) {
            this.proc = proc;
        }

        protected IStatus run( IProgressMonitor monitor ) {
            if (this.proc == null) return Status.CANCEL_STATUS;
            try {
                IProcessInfo info = this.proc.getInfo(monitor);
                setProcessFactory(info.getProcessFactory(), info.getProcessName());
            } catch (IOException e) {
                // TODO log this
                return Status.CANCEL_STATUS;
            }
            return Status.OK_STATUS;
        }
    }

    /**
     * Set the process factory, then build a new composite/page and
     * display it
     *
     * @param processFactory2
     */
    public void setProcessFactory( ProcessFactory pf, Name processName ) {
        if (pf == null) {
            return;
        }
        if (processName == null) {
            return;
        }
        
        this.processName = processName;
        this.processFactory = pf;
        this.adaptableInputTypes = new HashSet<Class< ? >>();
        
        // based on the process factory input params, build up a list of
        // acceptable adaptable input types
        Iterator<Entry<String, Parameter< ? >>> iterator = this.processFactory.getParameterInfo(processName).entrySet().iterator();
        while (iterator.hasNext()) {
            this.adaptableInputTypes.add(iterator.next().getValue().type);
        }
        
        // get the current display and update the UI in the display thread
        IWorkbench wb = PlatformUI.getWorkbench();
        Display display = wb.getDisplay();
        display.asyncExec( new Runnable(){
            public void run() {        
                createProcessPartControls(book);
                deleteProcessComp(noProcessComp);
                book.showPage(processComp); 
            }
        });     
    }
    
    /*
     * create a default property source for the tableview if there are no inputs
     */
    IAdaptable defaultSource=new IAdaptable (){

        public Object getAdapter(Class adapter) {
            if( IPropertySource.class.isAssignableFrom(adapter) )
                return new IPropertySource() {
                    
                    public void setPropertyValue(Object id, Object value) {
                        // TODO Auto-generated method stub
                
                    }
                
                    public void resetPropertyValue(Object id) {
                        // TODO Auto-generated method stub
                
                    }
                
                    public boolean isPropertySet(Object id) {
                        // TODO Auto-generated method stub
                        return false;
                    }
                
                    public Object getPropertyValue(Object id) {
                        return ""; //$NON-NLS-1$
                    }
                
                    public IPropertyDescriptor[] getPropertyDescriptors() {
                        return new PropertyDescriptor[]{new PropertyDescriptor("ID",Messages.WPSProcessView_noInputs)}; //$NON-NLS-1$
                    }
                
                    public Object getEditableValue() {
                        return null;
                    }
                
                };
            return null;
        }
        
    };

}
