/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
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
package es.axios.udig.spatialoperations.ui.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.render.displayAdapter.ViewportPane;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import es.axios.udig.spatialoperations.internal.control.BufferController;
import es.axios.udig.spatialoperations.internal.control.ClipController;
import es.axios.udig.spatialoperations.internal.control.ISOController;
import es.axios.udig.spatialoperations.internal.control.IntersectController;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.modelconnection.BufferCommand;
import es.axios.udig.spatialoperations.internal.modelconnection.ClipCommand;
import es.axios.udig.spatialoperations.internal.modelconnection.IntersectCommand;
import es.axios.udig.spatialoperations.internal.ui.parameters.ISOParamsPresenter;
import es.axios.udig.spatialoperations.internal.ui.parameters.buffer.BufferComposite;
import es.axios.udig.spatialoperations.internal.ui.parameters.clip.ClipComposite;
import es.axios.udig.spatialoperations.internal.ui.parameters.intersect.IntersectComposite;

/**
 * Generic frame continer for operation presentation.
 * <p>
 * This composite defines the widgets below as content for
 * all operations:
 * 
 * <ul> 
 * <li>Operation selection
 * <li>Demostration area
 * <li>Information area
 * <li>Parameters area
 * <li>Perform Command
 * </ul>
 *
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
final class SOComposite  extends Composite implements ISOPresenter{
    
    //private static final  Logger LOGGER = Logger.getLogger(SOComposite.class.getName()); 
    private static final int GRID_DATA_1_WIDTH_HINT      = 125;
    private static final int GRID_DATA_2_WIDTH_HINT      = 150;
//    private static final int GRID_DATA_3_WIDTH_HINT      = 170;
//    private static final int GRID_DATA_4_WIDTH_HINT      = 150;
    
    private Composite           compositeSOSelection   = null;
    private Composite           compositeSOParameters  = null;
    private Composite           compositeCommand       = null;
    private CLabel              labelOperation         = null;
    private CCombo              comboOperations        = null;
    private Button              buttonPerform          = null;
    private CLabel              labelImageDemo         = null;
    private ISOParamsPresenter  currentOpPresenter    = null;
    private Composite           compositeInformation   = null;
    private CLabel              messageImage = null;
    private CLabel              messageText = null;
    
    private ImageRegistry       imagesRegistry         = null;

    /** maintains key= operation name, value = operation . */
    private Map<String, String> mapOperationsNames     = new HashMap<String, String>();
    private StackLayout         stackSOParamsLayout;
    private IToolContext        context             = null;
    
    
    public SOComposite( Composite parent, int style ) {
        super(parent, style);

        createContent();
        
        
    }
    /**
     * Opens this widget populating its widgets
     */
    public void open() {
        displayDefaultOperation();
    }

    private void createContent() {

        ViewForm viewForm = new ViewForm(this, SWT.NONE);
        viewForm.setLayout(new FillLayout());
        
        Composite soComposite = createCompositeSOSelection(viewForm);
        viewForm.setTopLeft(soComposite);
        
        Composite paramsComposite = createCompositeSOParameters(viewForm);
        viewForm.setContent(paramsComposite);
    }


    /**
     * This method initializes compositeSOSelection	
     *
     */
    private Composite createCompositeSOSelection(final Composite parent) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = false;
        gridData.minimumHeight = 300;
        gridData.minimumWidth = 500;
        gridData.verticalAlignment = SWT.CENTER;
        
        compositeSOSelection = new Composite(parent, SWT.BORDER);
        compositeSOSelection.setLayoutData(gridData); 
        compositeSOSelection.setLayout(layout);

        createCompositeCommand(compositeSOSelection);
        createCompositeInformation(compositeSOSelection);
        
        return compositeSOSelection;

    }
    

    /**
     * This method initializes compositeCommand	
     *
     */
    private void createCompositeCommand(final Composite parent) {
        

        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = SWT.END;
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.verticalAlignment = SWT.CENTER;
        
        GridData gridData3 = new GridData();
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.verticalAlignment = SWT.CENTER;
        gridData3.horizontalAlignment = SWT.FILL;
        
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = false;
        gridData2.verticalAlignment = SWT.CENTER;
        gridData2.widthHint = GRID_DATA_2_WIDTH_HINT;
        gridData2.horizontalAlignment = SWT.BEGINNING;

        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = SWT.BEGINNING;
        gridData1.grabExcessHorizontalSpace = false;
        gridData1.verticalAlignment = SWT.CENTER;
        gridData1.widthHint = GRID_DATA_1_WIDTH_HINT;

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = false;
        gridData.verticalAlignment = SWT.BEGINNING;

        compositeCommand = new Composite(parent, SWT.NONE);
        compositeCommand.setLayoutData(gridData);
        compositeCommand.setLayout(gridLayout);
        
        labelOperation = new CLabel(compositeCommand, SWT.NONE);
        labelOperation.setText(Messages.SOComposite_operation);
        labelOperation.setLayoutData(gridData1);
        
        comboOperations = new CCombo(compositeCommand, SWT.BORDER | SWT.READ_ONLY);
        comboOperations.setLayoutData(gridData2);
        
        labelImageDemo = new CLabel(compositeCommand, SWT.NONE);
        labelImageDemo.setLayoutData(gridData3);
        
        buttonPerform = new Button(compositeCommand, SWT.NONE);
        buttonPerform.setText(Messages.SOComposite_perform);
        buttonPerform.setLayoutData(gridData4);
        buttonPerform.setEnabled(false);
        
        buttonPerform.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseUp( @SuppressWarnings("unused")
                                 MouseEvent e ) {
                
                executeOperation();
            }} );
        
    }

    /**
     * Load operations' images for each operation register in combo
     *
     * @return ImageRegistry
     */
    private ImageRegistry  createImageRegistry() {
        
        // Put the images in the registry
        ImageRegistry registry = new ImageRegistry(this.getDisplay());

        assert this.mapOperationsNames.size() > 0;
        
        for( Entry<String,String> element : this.mapOperationsNames.entrySet() ) {
            
            String opId = element.getValue();
            String imgFile = "images/" + opId + ".png"; //$NON-NLS-1$ //$NON-NLS-2$
            registry.put(opId, ImageDescriptor.createFromFile(SOComposite.class, imgFile)); 
            
            
        }
        return registry;
    }

    /**
     * This method initializes compositeInformation	
     *
     */
    private void createCompositeInformation(final Composite parent) {

      GridData gridData6 = new GridData();
      gridData6.horizontalAlignment = GridData.FILL;
      gridData6.grabExcessHorizontalSpace = true;
      gridData6.grabExcessVerticalSpace = true;
      gridData6.verticalAlignment = GridData.FILL;

      compositeInformation = new Composite(parent, SWT.NONE);
      GridLayout gridLayout = new GridLayout(2, false);
      compositeInformation.setLayoutData(gridData6);
      compositeInformation.setLayout(gridLayout);

      this.messageImage = new CLabel(compositeInformation, SWT.NONE);
      GridData gridData7 = new GridData();
      gridData7.horizontalAlignment = GridData.BEGINNING;
      gridData7.minimumWidth = 30;
      gridData7.widthHint = 30;
      this.messageImage.setLayoutData(gridData7);
      
      this.messageText = new CLabel(compositeInformation, SWT.NONE);
      GridData gridData8 = new GridData();
      gridData8.horizontalAlignment = GridData.FILL;
      gridData8.grabExcessHorizontalSpace = true;
      gridData8.grabExcessVerticalSpace = true;
      gridData8.verticalAlignment = GridData.FILL;
      this.messageText.setLayoutData(gridData8);
//      this.messageText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
      this.messageText.setFont(JFaceResources.getDialogFont());
      
        
// ImageAndMessageArea layout has problem        
//              
//        GridData gridData6 = new GridData();
//        gridData6.horizontalAlignment = GridData.FILL;
//        gridData6.grabExcessHorizontalSpace = true;
//        gridData6.grabExcessVerticalSpace = false;
//        gridData6.verticalAlignment = GridData.BEGINNING;
//
//        compositeInformation = new Composite(parent, SWT.NONE);
//        compositeInformation.setLayout(new GridLayout());//
//        compositeInformation.setLayoutData(gridData6);
//        
//        GridData gridData7 = new GridData();
//        gridData7.horizontalAlignment = GridData.FILL;
//        gridData7.grabExcessHorizontalSpace = true;
//        gridData7.grabExcessVerticalSpace = false;
//        gridData7.verticalAlignment = GridData.BEGINNING;
//
//        this.messageAreaControl = new ImageAndMessageArea(compositeInformation, SWT.WRAP);
//        this.messageAreaControl.setLayout(new GridLayout());
//        this.messageAreaControl.setLayoutData(gridData7);
//        this.messageAreaControl.setFont(JFaceResources.getDialogFont());
//        this.messageAreaControl.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
//        this.messageAreaControl.setVisible(false);
//        
//        animator = Policy.getAnimatorFactory().createAnimator(this.messageAreaControl);

    }

    /**
     * This method initializes compositeSOParameters    
     */
    private Composite createCompositeSOParameters(final Composite parent) {


        ScrolledComposite scrollComposite = new ScrolledComposite(parent,SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
        scrollComposite.setLayout(new FillLayout());

        // creates the container for controls in scroll composite
        this.compositeSOParameters = new Composite(scrollComposite, SWT.NONE);
        
        
        this.stackSOParamsLayout = new StackLayout();
        compositeSOParameters.setLayout(this.stackSOParamsLayout);

        
        // adds the parameters containter to scroll composite
        scrollComposite.setContent(compositeSOParameters);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setMinWidth(300);
        scrollComposite.setMinHeight(300); 

        createOperationParamPresenters(this.compositeSOParameters);
        
        return scrollComposite;
    }

    /**
     * Creates composites for each operation.
     * In this method is established the relation between 
     * Presenters, controller and command for each spatial 
     * operation
     
     * @param paramsContainer 
     */
    protected void createOperationParamPresenters(final Composite paramsContainer) {
        
        // create buffer operation
        ISOParamsPresenter bufferParamsPresenter =  new BufferComposite(paramsContainer,SWT.NONE);

        ISOController bufferController = new BufferController();
        bufferController.setSpatialOperationPresenter(this);
        bufferController.addParamsPresenter(bufferParamsPresenter);
        bufferController.setCommand(new BufferCommand());

        addOperationOptions(bufferParamsPresenter);

        // create clip operation
        ISOParamsPresenter clipParamsPresenter = new ClipComposite(paramsContainer, SWT.NONE);

        ISOController clipController = new ClipController();
        clipController.setSpatialOperationPresenter(this);
        clipController.addParamsPresenter(clipParamsPresenter);
        clipController.setCommand(new ClipCommand());

        addOperationOptions(clipParamsPresenter);
        
        // creates intersect operation
        ISOParamsPresenter intersectParamsPresenter = new IntersectComposite(paramsContainer, SWT.NONE); 

        ISOController intersectController = new IntersectController();
        intersectController.setSpatialOperationPresenter(this);
        intersectController.addParamsPresenter(intersectParamsPresenter);
        intersectController.setCommand(new IntersectCommand());

        addOperationOptions(intersectParamsPresenter);
        
        // create spatial join geometries operation
// TODO working was comment for 0.1.0
//        ISOParamsPresenter sjgParamsPresenter = new SpatialJoinGeomComposite(paramsContainer, SWT.NONE);
//        
//        ISOController sjgController = new  SpatialJoinGeomController();
//        sjgController.setSpatialOperationPresenter(this);
//        sjgController.addParamsPresenter(sjgParamsPresenter);
//        sjgController.setCommand(new SpatialJoinGeomCommand());
//        
//        addOperationOptions(sjgParamsPresenter);
        
        // sets selection event
        this.comboOperations.clearSelection();
        
        this.comboOperations.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected( @SuppressWarnings("unused") 
                                        SelectionEvent e ) {
               
                int index = comboOperations.getSelectionIndex();
                
                if(index == -1) return;
                
                final String opName = comboOperations.getItem(index);
                
                switchOperation(opName);
            }});

        // load all operations' images and sets the default selction
        this.imagesRegistry = createImageRegistry();

    }

    
    
    /**
     * Adds the operation parameters to combo box.
     * @param paramsPresenter
     */
    private void addOperationOptions(final ISOParamsPresenter  paramsPresenter) {

        String opName = paramsPresenter.getOperationName();
        
        this.comboOperations.add(opName);
        
        ISOController opController = paramsPresenter.getController();
        final String opID = opController.getOperationID();

        this.mapOperationsNames.put(opName, opID);
        
        this.comboOperations.setData(opID, paramsPresenter);
        
        paramsPresenter.visible(false);
        
        opController.shutUp();
    }

    

    /**
     * shows operation's parameters of the selected operation. 
     * The current operation will be invisible
     *
     * @param opSelected
     */
    private void switchOperation(final String opSelected ) {

        // hides the current parameters and shows the selected parameters
        if(this.currentOpPresenter != null){

            this.currentOpPresenter.visible(false);
        }
        final String opID = mapOperationsNames.get(opSelected);
        assert (opID != null) && (opID.length() >0);
        
        // initialize operation selected
        this.currentOpPresenter= (ISOParamsPresenter)this.comboOperations.getData(opID);

        // sets the operation tooltip with initial message
        String msg = this.currentOpPresenter.getToolTipText();
        this.comboOperations.setToolTipText(msg);
        this.labelImageDemo.setToolTipText(msg);
        
        // sets demo image
        Image img = this.imagesRegistry.get(opID);
        this.labelImageDemo.setImage(img);
        
        // pulls up the parameter's presentation for the selected operation
        Composite paramsPresenter = (Composite) this.currentOpPresenter; 
        this.stackSOParamsLayout.topControl =paramsPresenter;        
        paramsPresenter.getParent().layout();
        
        this.currentOpPresenter.setContext(this.context);
        this.currentOpPresenter.visible(true);
        
    }
    
    /**
     * Sets the new context. Map deleted, added or layer list changes
     * 
     * @param newContext
     */
    public void setContext(final IToolContext newContext){
        
        this.context = newContext;
        
        this.currentOpPresenter.setContext(newContext);
    }

    
    /**
     * Shows the message in the standard information area
     */
    public void displayMessage(final Message message){
        
        assert message != null;
        if(Message.NULL.equals(message)) {
            return;
        }
        // The following sentences does a filter of those obvious messages
        Message filteredMessage = message;
        if(message.getType() == Message.Type.INFORMATION){
            filteredMessage = this.currentOpPresenter.getController().getDefaultMessage();
        }
        
        // then shows important, warnings and fail messages
        
        // show the message
        this.compositeInformation.setVisible(true);
        this.messageImage.setImage(filteredMessage.getImage());
        this.messageText.setToolTipText(filteredMessage.getText());
        this.messageText.setText(filteredMessage.getText());
        
    }

    /**
     * Enable the perform button
     */
    public void setPerformEnabled( boolean enabled ) {
        
        this.buttonPerform.setEnabled(enabled);
    }

    /**
     * enable/disable this composite
     */
    @Override
    public void setEnabled( boolean enabled ) {
        super.setEnabled(enabled);
        
        this.buttonPerform.setEnabled(enabled);
        this.comboOperations.setEnabled(enabled);
        this.compositeSOParameters.setEnabled(enabled);
        
        this.currentOpPresenter.setEnabled(enabled);
        
    }
    
    
    /**
     * Executes the operation associated to control selected.
     *
     */
    private void executeOperation() {

        setEnabled(false);

        // sets the wait cursor and disables this panel 
        ViewportPane pane = this.context.getViewportPane();
        Display display = getDisplay();
        
        pane.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
        
        final ISOController controller = currentOpPresenter.getController();
        controller.executeCommand();

        refresh();

        pane.setCursor(null);
        setEnabled(true);
    }

    /**
     * Map refresh
     */
    private void refresh(){
        
        if(this.context == null) return;
    
        IMap curMap = this.context.getMap();
        if(curMap == null) return;
    
    }
    

    /**
     * Clear the inputs values
     */
    public void initializeInputs() {
        
        if( this.currentOpPresenter == null) return;
        
        this.setPerformEnabled(false);
        
        
        this.currentOpPresenter.clear();

        ISOController controller = this.currentOpPresenter.getController();
        Message message = controller.getMessage();
        this.displayMessage(message);
        
    }

    /**
     * Display the first operation listed in combo.
     */
    private void displayDefaultOperation() {
        
        // takes the first operaton of list and run it controller
        
        if (this.comboOperations.getItemCount() == 0)
            return;

        final int iSelected = 0;
        this.comboOperations.select(iSelected);
        final String opSelected = this.comboOperations.getItem(iSelected);
        switchOperation(opSelected);
        
        
    }
    
    

}  //  @jve:decl-index=0:visual-constraint="10,10"
