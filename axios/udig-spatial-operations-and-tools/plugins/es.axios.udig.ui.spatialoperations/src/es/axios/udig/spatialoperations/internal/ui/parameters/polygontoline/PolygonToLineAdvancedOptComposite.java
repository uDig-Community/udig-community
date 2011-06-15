/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
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
package es.axios.udig.spatialoperations.internal.ui.parameters.polygontoline;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.preferences.Preferences;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.PolygonToLineCommand;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;

/**
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.2.0
 */
class PolygonToLineAdvancedOptComposite extends AggregatedPresenter {

	private int	GRID_DATA_1_WIDTH_HINT	= 125;
	private int	GRID_DATA_2_WIDTH_HINT	= 150;

	public PolygonToLineAdvancedOptComposite(Composite parent, int style) {

		super(parent, style);
		super.initialize();
	}

	private Button	checkBoxExplode			= null;

	private Boolean	defaultExplodeOption	= null;
	private Boolean	currentExplodeOption	= null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.axios.udig.spatialoperations.internal.ui.parameters.
	 * AbstractParamsPresenter#createContents()
	 */
	@Override
	protected void createContents() {

		GridLayout layoutAdvancedOptions = new GridLayout();
		layoutAdvancedOptions.numColumns = 4;

		GridData gridDataAdvancedOptions = new GridData();
		gridDataAdvancedOptions.horizontalAlignment = GridData.FILL;
		gridDataAdvancedOptions.grabExcessHorizontalSpace = true;
		gridDataAdvancedOptions.grabExcessVerticalSpace = true;
		gridDataAdvancedOptions.verticalAlignment = GridData.FILL;
		gridDataAdvancedOptions.horizontalSpan = 2;

		this.setLayout(layoutAdvancedOptions);
		this.setLayoutData(gridDataAdvancedOptions);

		createAdvancedOptionItems(this);
	}

	/**
	 * This method initializes groupUnits
	 */
	private void createAdvancedOptionItems(Composite parent) {

		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.BEGINNING;
		gridData1.grabExcessHorizontalSpace = false;
		gridData1.horizontalSpan = 2;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.widthHint = GRID_DATA_1_WIDTH_HINT + GRID_DATA_2_WIDTH_HINT;

		checkBoxExplode = new Button(parent, SWT.CHECK);
		checkBoxExplode.setText(Messages.PolygonToLineAdvancedOptComposite_advanced_text);
		checkBoxExplode.setToolTipText(Messages.PolygonToLineAdvancedOptComposite_addvanced_tool_tip);
		checkBoxExplode.setLayoutData(gridData1);
		checkBoxExplode.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {

				validateParameters();

			}
		});

	}

	/**
	 * Validate the current option's values
	 */
	@Override
	protected void setParametersOnCommand(ISOCommand command) {

		PolygonToLineCommand cmd = (PolygonToLineCommand) command;

		// gets the options values and sends it to controller which does the
		// validation.

		Boolean explode = getExplodeOption();

		cmd.setAdvancedOptions(explode);
	}

	/**
	 * Gets the aggregate option and set the current value.
	 * 
	 * @return true if aggregate was selected.
	 */
	private Boolean getExplodeOption() {

		this.currentExplodeOption = this.checkBoxExplode.getSelection();

		this.defaultExplodeOption = this.currentExplodeOption;

		return this.currentExplodeOption;
	}

	/**
	 * Populate using the default values
	 */
	@Override
	protected void populate() {

		PolygonToLineCommand cmd = (PolygonToLineCommand) getCommand();
		this.defaultExplodeOption = cmd.getExplodeOption();
		if (this.defaultExplodeOption == null) {
			this.defaultExplodeOption = Preferences.polygonToLineExplode();
		}

		setExplodeOption(this.defaultExplodeOption);

	}

	private void setExplodeOption(final Boolean option) {

		PolygonToLineCommand cmd = (PolygonToLineCommand) getCommand();

		cmd.setDefaultValues(option);

		this.checkBoxExplode.setSelection(option);

		this.currentExplodeOption = option;
	}

	/**
	 * Sets enable/disable the composite's widgets
	 */
	@Override
	public void setEnabled(boolean enabled) {

		this.checkBoxExplode.setEnabled(enabled);

	}
}
