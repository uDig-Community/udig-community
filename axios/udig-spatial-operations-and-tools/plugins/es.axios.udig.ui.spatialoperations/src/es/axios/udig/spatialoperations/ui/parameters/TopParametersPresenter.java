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
package es.axios.udig.spatialoperations.ui.parameters;

import java.util.Observable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import es.axios.udig.spatialoperations.internal.preferences.Preferences;
import es.axios.udig.spatialoperations.internal.ui.common.DemoComposite;

/**
 * Encapsulates common behavior of the composite that present the general layout of parameters presenter. 
 * That is the data parameters and the demo image. Both composite are included in a sash composite.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.2.0
 */
public class TopParametersPresenter extends AggregatedPresenter implements ISOTopParamsPresenter {

	
	private DemoComposite demoComposite = null;
	private Composite dataComposite = null;
	private ScrolledComposite dataScrollComposite = null;
	private SashForm sash = null;
	private ISOParametersPresenterFactory presenterFactory = null;
	private ISOCommand cmd = null;
	private Image soIcon = null;
	
	
	
	public TopParametersPresenter(Composite parent, ISOParametersPresenterFactory presenterFactory, int style) {
		super(parent, style);
		
		assert parent != null;
		assert presenterFactory != null;
		
		this.presenterFactory  = presenterFactory;
		
		super.initialize();
	}
	
    @Override
	public ISOCommand getCommand(){
    	return this.cmd;
    	
    }

    /**
     * Retrieves the image for default spatial operation. If you extends the standard behavior you 
     * must provide an implementation to retrieve the icon for the new spatial operations. 
     */
	public Image getImage(){
		
		if(this.soIcon == null){
			this.soIcon = this.presenterFactory.createIcon(); 
		}
		assert this.soIcon != null;
		
		return this.soIcon;
	}

	/**
	 * Creates and associates the required composites to present the parameters and the demo image for
	 * the spatial operation.
	 * 
	 * @param builder
	 */
	@Override
	protected void createContents() {

		assert this.presenterFactory != null: "builder cannot not be null";

		GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);
		
		this.sash = new SashForm(this, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		this.sash.setLayoutData(gridData);

		this.dataScrollComposite = new ScrolledComposite(this.sash, SWT.BORDER | SWT.H_SCROLL);
		this.dataScrollComposite.setLayout(new FillLayout());

		this.dataComposite = (Composite) this.presenterFactory.createDataComposite(this.dataScrollComposite, SWT.BORDER);
			
		dataComposite.setLayoutData(gridData);

		this.dataScrollComposite.setContent(dataComposite);
		this.dataScrollComposite.setExpandHorizontal(true);
		this.dataScrollComposite.setExpandVertical(true);
		this.dataScrollComposite.setMinSize(dataComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		ScrolledComposite demoScrollComposite = new ScrolledComposite(sash, SWT.BORDER | SWT.H_SCROLL);
		demoScrollComposite.setLayout(new FillLayout());


		IImageOperation demoImages  = this.presenterFactory.createDemoImages();
		
		this.demoComposite=  new DemoComposite(demoScrollComposite, SWT.BORDER, demoImages);
		
		this.demoComposite.setLayoutData(gridData);

		demoScrollComposite.setContent(this.demoComposite);
		demoScrollComposite.setExpandHorizontal(true);
		demoScrollComposite.setExpandVertical(true);
		demoScrollComposite.setMinSize(demoComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		sash.setWeights(new int[] { 70, 30 });

		super.addPresenter((AggregatedPresenter)dataComposite);
	}

	public DemoComposite getDemoComposite() {
		return this.demoComposite;
	}

	public Composite getDataComposite() {
		return this.dataComposite;
	}
	
	@Override
	protected void initState() {

		// associates demo composite as observer of the command
		this.demoComposite.setCommand(getCommand());
	}

	@Override
	protected void populate() {
		
		switchShowHideDemo(Preferences.showDemo()); 
	}

	@Override
	public void update(Observable o, Object arg) {

		
	}

	/**
	 * Takes the user preference to show or not show the demo panel.
	 * 
	 * @param showDemo
	 */
	public void switchShowHideDemo(final boolean showDemo) {

		demoComposite.setVisible(true);
		if (showDemo) {
			sash.setMaximizedControl(null);
		} else {
			sash.setMaximizedControl(dataScrollComposite);
		}
	}
	
	public final String getOperationName() {

		return getCommand().getOperationName();
	}

	@Override
	public String getToolTipText() {

		return getCommand().getToolTipText();
	}

	public void setCommand(ISOCommand command) {
			this.cmd = command;
	}
	

}
