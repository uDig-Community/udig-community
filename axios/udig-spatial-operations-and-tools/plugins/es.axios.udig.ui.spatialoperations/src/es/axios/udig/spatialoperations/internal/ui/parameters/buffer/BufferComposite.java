/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
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
package es.axios.udig.spatialoperations.internal.ui.parameters.buffer;

import net.refractions.udig.project.ILayer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;

/**
 * This class presents the buffer parameters.
 * <p>
 * Presents source, target layer and options
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 * @since 1.1.0
 */
final class BufferComposite extends AggregatedPresenter {

	private BufferLayersComposite		layersComposite			= null;
	private BufferOptionsComposite		optionsComposite		= null;
	private BufferAdvancedOptComposite	advancedOptComposite	= null;
	private TabFolder					tabFolder;

	/**
	 * Creates a new instance of BufferComposite
	 * 
	 * @param parent
	 * @param style
	 */
	public BufferComposite(Composite parent, int style) {

		super(parent, style);
		super.initialize();

	}

	@Override
	protected final void createContents() {

		GridLayout gridLayout = new GridLayout();
		setLayout(gridLayout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(gridData);

		layersComposite = new BufferLayersComposite(tabFolder, SWT.NONE);
		GridData gridDataLayers = new GridData();
		gridDataLayers.horizontalAlignment = SWT.FILL;
		gridDataLayers.grabExcessHorizontalSpace = true;
		gridDataLayers.verticalAlignment = SWT.BEGINNING;
		layersComposite.setLayoutData(gridDataLayers);

		advancedOptComposite = new BufferAdvancedOptComposite(tabFolder, SWT.NONE);
		GridData gridDataOptions = new GridData();
		gridDataOptions.horizontalAlignment = GridData.FILL;
		gridDataOptions.grabExcessHorizontalSpace = true;
		gridDataOptions.grabExcessVerticalSpace = true;
		gridDataOptions.verticalAlignment = GridData.BEGINNING;
		advancedOptComposite.setLayoutData(gridDataOptions);

		optionsComposite = new BufferOptionsComposite(layersComposite, SWT.NONE);
		optionsComposite.setLayoutData(gridDataOptions);

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.Composite_tab_folder_basic);
		tabItem.setControl(layersComposite);
		TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
		tabItem1.setText(Messages.Composite_tab_folder_advanced);
		tabItem1.setControl(advancedOptComposite);

		super.addPresenter(this.layersComposite);
		super.addPresenter(this.optionsComposite);
		super.addPresenter(this.advancedOptComposite);
	}

	public ILayer getSourceLayer() {

		return this.layersComposite.getSourceLayer();
	}

	/**
	 * @return Returns the BufferLayersComposite
	 */
	public BufferLayersComposite getLayersComposite() {

		assert this.layersComposite != null;

		return this.layersComposite;
	}

	@Override
	protected void populate() {

		// nothing

	}

	/**
	 * Reinitialize parameter values Set the 1st tabfolder as selected one.
	 */
	@Override
	protected final void clearInputs() {

		tabFolder.setSelection(tabFolder.getItem(0));
	}

} // @jve:decl-index=0:visual-constraint="10,10"
