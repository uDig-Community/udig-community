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

import java.util.Observable;
import java.util.Observer;

import net.refractions.udig.project.ui.IUDIGView;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.opengis.feature.simple.SimpleFeature;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;

/**
 * View for spatial operations
 * <p>
 * This view presents the spatial operation and its parameters.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
public final class SOView extends ViewPart implements IUDIGView, Observer {

	private ISOPresenter		soComposite			= null;
	private IToolContext		context				= null;

	private runButtonAction		runButton			= null;

	private menuAction			showHideDemoMenu	= null;

	private IAction				runAction;
	private Thread				displayThread		= null;

	public static final String	id					= "es.axios.udig.spatialoperations.ui.view.SOView"; //$NON-NLS-1$

	/**
	 * New instance of SOView
	 * 
	 */
	public SOView() {

		this.displayThread = Display.getCurrent().getThread();
	}

	@Override
	public void createPartControl(Composite parent) {

		SOComposite composite = new SOComposite(this, parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		composite.open();

		createActions();
		createToolbar(composite);

		this.soComposite = composite;

	}

	/**
	 * Create the toolbar
	 * 
	 * @param composite
	 */
	private void createToolbar(SOComposite composite) {

		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(runButton);
		this.runButton.setEnabled(false);

		// Create the menu on the toolbar
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager viewMenu = actionBars.getMenuManager();

		viewMenu.add(runAction);
		viewMenu.add(new Separator());
		viewMenu.add(showHideDemoMenu);

	}

	/**
	 * Create actions, linking view to current map.
	 */
	private void createActions() {
		this.runButton = new runButtonAction();
		this.showHideDemoMenu = new menuAction(Messages.SOComposite_show_hide_demo_tooltip);

		this.runAction = new Action() {

			@Override
			public void run() {

				soComposite.executeOperation();
			}
		};

		this.runAction.setText(Messages.SOComposite_perform);
		this.runAction.setToolTipText(Messages.SOComposite_perform);
		String imgFile = "images/run.gif"; //$NON-NLS-1$
		this.runAction.setImageDescriptor(ImageDescriptor.createFromFile(SOComposite.class, imgFile));
		this.runAction.setEnabled(false);
	}

	/**
	 * Create the runButtonAction
	 * 
	 */
	private class runButtonAction extends Action {

		public runButtonAction() {

			setToolTipText(Messages.SOComposite_perform);
			String imgFile = "images/run.gif"; //$NON-NLS-1$
			setImageDescriptor(ImageDescriptor.createFromFile(SOComposite.class, imgFile));

		}

		@Override
		public void run() {
			soComposite.executeOperation();
		}

	}

	/**
	 * Creates the menu
	 */
	private class menuAction extends Action {

		public menuAction(String text) {

			super(text, AS_PUSH_BUTTON);

		}

		@Override
		public void run() {
			soComposite.switchShowHide();
		}

	}

	@Override
	public void setFocus() {

	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
	}

	public void editFeatureChanged(SimpleFeature feature) {
		// this is not important for spatial operations
	}

	public IToolContext getContext() {
		return this.context;
	}

	/**
	 * @return the spatial operation composite
	 */
	public ISOPresenter getSpatialOperationPresenter() {

		assert this.soComposite != null;

		return this.soComposite;
	}

	public void setSpatialOperationPresenter(SOComposite composite) {
		assert composite != null;

		this.soComposite = composite;
	}

	/**
	 * Called by the uDig framework when a new map is created or its layer list
	 * is modified.
	 * 
	 * @param newContext
	 *            it could be null if the map is deleted
	 */
	public void setContext(final IToolContext newContext) {

		assert this.displayThread != null;

		Display display = Display.findDisplay(this.displayThread);
		display.syncExec(new Runnable() {

			public void run() {
				context = newContext;

				soComposite.setContext(context);

				soComposite.open();
			}
		});

	}

	/**
	 * Called by the SOCommand (an instance of observable class) Enable/Disable
	 * the run button
	 */
	public void update(Observable o, Object arg) {

		assert o instanceof ISOCommand;

		ISOCommand cmd = (ISOCommand) o;

		enableRunButton(cmd.canExecute());

	}

	/**
	 * Enable the runButton
	 * 
	 * @param enable
	 */
	public void enableRunButton(boolean enable) {
		if (this.runButton != null) {
			this.runButton.setEnabled(enable);
		}
		if (this.runAction != null) {
			this.runAction.setEnabled(enable);
		}
	}

}
