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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Logger;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.render.displayAdapter.ViewportPane;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import es.axios.udig.spatialoperations.internal.preferences.Preferences;
import es.axios.udig.spatialoperations.ui.parameters.ISOBuilder;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.ISOTopParamsPresenter;
import es.axios.udig.spatialoperations.ui.parameters.SOContext;
import es.axios.udig.spatialoperations.ui.parameters.SpatialOperationDefaultBuilder;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Frame to present all spatial operation .
 * 
 * <p>
 * This composite defines the layout for the following widgets:
 * 
 * <ul>
 * <li>Operation selection
 * <li>Demonstration area
 * <li>Information area
 * <li>Parameters area
 * <li>Perform Command
 * </ul>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
final public class SOComposite extends Composite implements ISOPresenter, Observer {

	private static final Logger		LOGGER							= Logger.getLogger(SOComposite.class.getName());

	private static final String		SPATIAL_OPERATION_EXTENSIONS_ID	= "es.axios.udig.spatialoperations.ui";			//$NON-NLS-1$

	private static final int		GRID_DATA_1_WIDTH_HINT			= 125;

	/** Used as key in the data map associated to spatial operation's tree item */
	private static final String		SO_CONTEXT_KEY					= "ctx";											//$NON-NLS-1$

	/** Spatial operation id 's key in the data map */
	private static final String		SO_ID_KEY						= "id";											//$NON-NLS-1$

	/** Spatial operation name's key in the data map related with a tree item */
	private static final String		SO_NAME_KEY						= "name";											//$NON-NLS-1$

	/** spatial operation names presentation */
	private TreeViewer				treeView						= null;

	private Composite				compositeSOSelection			= null;
	private Composite				compositeSOParameters			= null;

	private ISOTopParamsPresenter	currentOpPresenter				= null;
	private Composite				compositeInformation			= null;
	private CLabel					messageImage					= null;
	private CLabel					messageText						= null;
	private CLabel					messageOperation				= null;

	private StackLayout				stackSOParamsLayout;
	private IToolContext			udigContext						= null;
	private SOView					soView							= null;
	private ViewForm				viewForm;
	private Composite				compositeTree					= null;
	private SashForm				sashForm						= null;
	private TreeNode				treeOperations;

	public SOComposite(SOView view, Composite parent, int style) {

		super(parent, style);

		// set the view which contains the observer
		setSOView(view);
		view.setSpatialOperationPresenter(this);

		createContent();

	}

	/**
	 * Opens this widget populating its widgets
	 */
	public void open() {

		// if nothing selected
		if (this.treeView.getTree().getSelection().length == 0) {
			displayOperation(null);
		} else {
			displayOperation(this.treeView.getTree().getSelection()[0].getText());
		}
	}

	private void createContent() {

		sashForm = new SashForm(this, SWT.NONE);

		createCompositeSOSelection(sashForm);

		viewForm = new ViewForm(sashForm, SWT.NONE);
		viewForm.setLayout(new FillLayout());

		Composite soComposite = createCompositeSOlegend(viewForm);
		viewForm.setTopLeft(soComposite);

		Composite paramsComposite = createCompositeSOParameters(viewForm);
		viewForm.setContent(paramsComposite);

		// must go after populating the sash whit composites
		sashForm.setWeights(new int[] { 18, 82 });
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
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = 150;
		gridData.verticalAlignment = GridData.FILL;

		compositeSOSelection = new Composite(parent, SWT.BORDER);
		compositeSOSelection.setLayoutData(gridData);
		compositeSOSelection.setLayout(layout);

		createCompositeCommand(compositeSOSelection);

		return compositeSOSelection;

	}

	private Composite createCompositeSOlegend(final Composite parent) {

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

		createCompositeInformation(compositeSOSelection);

		return compositeSOSelection;

	}

	/**
	 * This method initializes compositeCommand
	 * 
	 */
	private void createCompositeCommand(final Composite parent) {

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;

		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = SWT.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.grabExcessVerticalSpace = true;
		gridData4.verticalAlignment = SWT.FILL;
		gridData4.widthHint = GRID_DATA_1_WIDTH_HINT;

		compositeTree = new Composite(parent, SWT.NONE);
		compositeTree.setLayoutData(gridData4);
		compositeTree.setLayout(gridLayout);

		treeView = new TreeViewer(compositeTree, SWT.FILL);
		treeView.setContentProvider(new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {

				return ((TreeNode) parentElement).getChildren().toArray();
			}

			public Object getParent(Object element) {

				return ((TreeNode) element).getParent();
			}

			public boolean hasChildren(Object element) {

				return ((TreeNode) element).getChildren().size() > 0;
			}

			public Object[] getElements(Object inputElement) {

				return ((TreeNode) inputElement).getChildren().toArray();
			}

			public void dispose() {

			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

			}

		});

		treeView.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {

				TreeItem[] index = treeView.getTree().getSelection();
				if (index.length == 0) {
					return;
				}
				TreeItem selectedItem = treeView.getTree().getSelection()[0];

				switchOperation(selectedItem);
			}

		});

		treeView.setInput(createTree());
		treeView.getTree().setLayoutData(gridData4);
	}

	/**
	 * Create a tree on the treeviewer
	 */
	private TreeNode createTree() {

		treeOperations = new TreeNode("General"); //$NON-NLS-1$

		return treeOperations;
	}

	class TreeNode {

		private String			name;
		private List<TreeNode>	children	= new ArrayList<TreeNode>();
		private TreeNode		parent;

		public TreeNode(String n) {

			name = n;
		}

		protected Object getParent() {

			return parent;
		}

		public TreeNode addChild(TreeNode child) {

			children.add(child);
			child.parent = this;
			return this;
		}

		public List<TreeNode> getChildren() {

			return children;
		}

		@Override
		public String toString() {

			return name;
		}

	}

	/**
	 * Load operations' images for each operation register in combo
	 * 
	 * @return ImageRegistry
	 */
	private ImageRegistry createImageRegistry() {

		ImageRegistry registry = new ImageRegistry(this.getDisplay());
		for (int i = 0; i < this.treeView.getTree().getItemCount(); i++) {

			TreeItem item = this.treeView.getTree().getItem(i);
			String opId = (String) item.getData(SO_ID_KEY);
			String imgFile = "images/" + opId + ".gif"; //$NON-NLS-1$ //$NON-NLS-2$
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
		GridLayout gridLayout = new GridLayout(3, false);
		compositeInformation.setLayoutData(gridData6);
		compositeInformation.setLayout(gridLayout);

		this.messageImage = new CLabel(compositeInformation, SWT.NONE);
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = GridData.BEGINNING;
		gridData7.minimumWidth = 30;
		gridData7.widthHint = 30;
		this.messageImage.setLayoutData(gridData7);

		this.messageOperation = new CLabel(compositeInformation, SWT.NONE);
		GridData gridData5 = new GridData();
		gridData5.widthHint = 70;
		this.messageOperation.setLayoutData(gridData5);

		this.messageText = new CLabel(compositeInformation, SWT.NONE);
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.FILL;
		gridData8.grabExcessHorizontalSpace = true;
		gridData8.grabExcessVerticalSpace = true;
		gridData8.verticalAlignment = GridData.FILL;
		this.messageText.setLayoutData(gridData8);
		// this.messageText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		this.messageText.setFont(JFaceResources.getDialogFont());
	}

	/**
	 * This method initializes compositeSOParameters
	 */
	private Composite createCompositeSOParameters(final Composite parent) {

		ScrolledComposite scrollComposite = new ScrolledComposite(parent, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
		scrollComposite.setLayout(new FillLayout());

		// creates the container for controls in scroll composite
		this.compositeSOParameters = new Composite(scrollComposite, SWT.NONE);

		this.stackSOParamsLayout = new StackLayout();
		compositeSOParameters.setLayout(this.stackSOParamsLayout);

		// adds the parameters container to scroll composite
		scrollComposite.setContent(compositeSOParameters);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setMinHeight(300);

		createOperationParamsPresenters(this.compositeSOParameters);

		return scrollComposite;
	}

	/**
	 * Creates the default spatial operations and extensions
	 * 
	 * @param paramsContainer
	 */
	private void createOperationParamsPresenters(final Composite paramsContainer) {

		createSpatialOperations(paramsContainer, new SpatialOperationDefaultBuilder());

		createSpatialOperationExtensions(paramsContainer);

		createImageRegistry();
	}

	/**
	 * Creates the spatial operations and establish the association with the
	 * spatial operation framework
	 * 
	 * @param paramsContainer
	 */
	private void createSpatialOperations(Composite paramsContainer, ISOBuilder soBuilder) {

		soBuilder.build(paramsContainer);
		Set<Object[]> soList = soBuilder.getResult();

		if (soList.isEmpty()) {
			final String msg = "The builder " + soBuilder.getClass().getName()
						+ " has not created any spatial operation.";
			LOGGER.severe(msg);
		}

		// associates the spatial operations components with the view
		for (Object[] soComponent : soList) {

			SOContext ctx = new SOContext(this, (ISOTopParamsPresenter) soComponent[0], (ISOCommand) soComponent[1],
						this.soView);

			addInOperationsPanel(this.treeView.getTree(), ctx);

		}
	}

	/**
	 * Loads the spatial operation extensions from each provider plug-in .
	 * 
	 * @param paramsContainer
	 */
	private void createSpatialOperationExtensions(Composite paramsContainer) {

		try {

			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
						SPATIAL_OPERATION_EXTENSIONS_ID);

			for (IConfigurationElement e : config) {

				// creates the executable builder for this spatial operation
				// extension
				Object builder = e.createExecutableExtension("SOBuilder");

				createSpatialOperations(paramsContainer, (ISOBuilder) builder);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			LOGGER.severe(ex.getMessage());
		}
	}

	/**
	 * Adds operations to the tree
	 * 
	 * @param operationsTree
	 * 
	 * @param paramsPresenter
	 */
	private void addInOperationsPanel(final Tree operationsTree, final SOContext ctx) {

		ISOTopParamsPresenter paramsPresenter = ctx.getTopPresenter();

		final String opID = paramsPresenter.getOperationID();
		final String opName = paramsPresenter.getOperationName();

		TreeItem treeItem = new TreeItem(operationsTree, SWT.NONE);
		treeItem.setText(opName);
		treeItem.setData(SO_ID_KEY, opID);
		treeItem.setData(SO_NAME_KEY, opName);
		treeItem.setData(SO_CONTEXT_KEY, ctx);

		// sets the tree item image.
		Image image = paramsPresenter.getImage();

		treeItem.setImage(image);

		paramsPresenter.visible(false);
	}

	/**
	 * Shows operation's parameters of the selected operation. The current
	 * operation will be invisible
	 * 
	 * @param opSelected
	 */
	private void switchOperation(final TreeItem selectedItem) {

		// disable the runButton
		this.soView.enableRunButton(false);

		if (this.currentOpPresenter != null) {
			this.currentOpPresenter.close();
		}

		SOContext ctx = (SOContext) selectedItem.getData(SO_CONTEXT_KEY);

		this.currentOpPresenter = ctx.getTopPresenter();

		// sets the operation's tool tip with initial message
		String msg = this.currentOpPresenter.getToolTipText();

		this.treeView.getTree().setToolTipText(msg);

		// pulls up the parameter's presentation for the selected operation
		Composite paramsPresenter = (Composite) this.currentOpPresenter;
		this.stackSOParamsLayout.topControl = paramsPresenter;
		paramsPresenter.getParent().layout();

		currentOpPresenter.setContext(udigContext);

		currentOpPresenter.visible(true);

		IMap currentMap = null;
		if (udigContext != null) {
			currentMap = udigContext.getMap();
		}

		if (currentMap != null) {

			currentOpPresenter.open();

		} else {

			currentOpPresenter.close();
		}
	}

	/**
	 * Sets the new context. Map deleted, added or layer list changes
	 * 
	 * @param newContext
	 */
	public void setContext(final IToolContext newContext) {

		this.udigContext = newContext;

		this.currentOpPresenter.setContext(newContext);
	}

	/**
	 * Shows the message in the standard information area
	 */
	public void displayMessage(final InfoMessage message) {

		assert message != null;

		// The following sentences does a filter of those obvious messages
		InfoMessage filteredMessage = message;
		if (InfoMessage.Type.INFORMATION.equals(message.getType())) {
			filteredMessage = this.currentOpPresenter.getDefaultMessage();
		}

		// then shows important, warnings and fail messages
		String opName;
		if (this.treeView.getTree().getSelection().length == 0) {
			opName = this.treeView.getTree().getItem(0).getText();
		} else {
			opName = this.treeView.getTree().getSelection()[0].getText();
		}
		// shows the message
		this.compositeInformation.setVisible(true);
		this.messageImage.setImage(filteredMessage.getImage());
		this.messageOperation.setText(opName);
		this.messageText.setToolTipText(filteredMessage.getText());
		this.messageText.setText(filteredMessage.getText());

	}

	/**
	 * enable/disable this composite
	 */
	@Override
	public void setEnabled(boolean enabled) {

		super.setEnabled(enabled);

		this.treeView.getTree().setEnabled(enabled);

		this.compositeSOParameters.setEnabled(enabled);

		this.currentOpPresenter.setEnabled(enabled);

	}

	/**
	 * Executes the operation associated to selected control.
	 * 
	 */
	public void executeOperation() {

		setEnabled(false);

		// sets the wait cursor and disables this panel
		ViewportPane pane = this.udigContext.getViewportPane();
		Display display = getDisplay();

		pane.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));

		this.currentOpPresenter.executeCommand();

		pane.setCursor(null);

		setEnabled(true);
	}

	/**
	 * Clear the inputs values
	 */
	public void initializeInputs() {

		if (this.currentOpPresenter == null) {
			return;
		}

		this.currentOpPresenter.clear();

		InfoMessage message = this.currentOpPresenter.getMessage();
		this.displayMessage(message);
	}

	/**
	 * Select an operation if there isn't one selected and switch to that
	 * operation.
	 */
	private void displayOperation(String treeName) {

		if (treeName == null) {
			treeName = this.treeView.getTree().getItem(0).getText();
			this.treeView.getTree().setSelection(this.treeView.getTree().getItem(0));
		}

		TreeItem selected = this.treeView.getTree().getSelection()[0];
		switchOperation(selected);

	}

	/**
	 * The view whit the observer
	 * 
	 * @param view
	 */
	private void setSOView(final SOView view) {

		assert view != null;

		this.soView = view;

	}

	/**
	 * show/hide the demo image for all presenters in the spatial
	 */
	public void switchShowHide() {

		// switch the preverence value
		boolean visible = !Preferences.isDemoVisible();
		Preferences.setDemoVisible(visible);
		// switch show or hide all demo presenters
		for (int i = 0; i < this.treeView.getTree().getItemCount(); i++) {

			TreeItem item = this.treeView.getTree().getItem(i);
			Object itemData = item.getData(SO_CONTEXT_KEY);

			SOContext ctx = (SOContext) itemData;
			ISOTopParamsPresenter presenter = ctx.getTopPresenter();

			presenter.switchShowHideDemo(visible);
		}
		this.sashForm.layout();
	}

	public void update(Observable o, Object arg) {

		if (this.currentOpPresenter == null) {
			return;
		}

		InfoMessage message = this.currentOpPresenter.getCommand().getMessage();
		displayMessage(message);

	}

} // @jve:decl-index=0:visual-constraint="10,10"
