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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.preferences.Preferences;
import es.axios.udig.spatialoperations.internal.ui.processconnectors.BufferCommand;
import es.axios.udig.spatialoperations.tasks.IBufferTask.CapStyle;
import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;

/**
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
final class BufferAdvancedOptComposite extends AggregatedPresenter {

	private int			GRID_DATA_1_WIDTH_HINT	= 125;
	private int			GRID_DATA_2_WIDTH_HINT	= 150;

	private Button		checkBoxMergeResult		= null;
	private Label		labelQuadrantSegments	= null;
	private Spinner		spinnerQuadrantSegments	= null;

	private Group		groupCapStyle			= null;
	private Button		radioCapRound			= null;
	private Button		radioCapFlat			= null;
	private Button		radioCapSquare			= null;
	private Button		currentRadioCapStyle	= null;
	private Button		sessionRadioCapStyle	= null;

	private Integer		currentQuadrantSegments	= null;
	private Integer		sessionQuadrantSegments	= null;
	private Boolean		sessionAggregateOption	= null;
	private Boolean		currentAggregateOption	= null;
	private CapStyle	capStyle				= CapStyle.capRound;

	public BufferAdvancedOptComposite(Composite parent, int style) {

		super(parent, style);
		super.initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @es.axios.udig.spatialoperations.internal.ui.parameters.
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
		createCapStyleOptions(this);
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

		checkBoxMergeResult = new Button(parent, SWT.CHECK);
		checkBoxMergeResult.setText(Messages.BufferOptionsComposite_checkMergeResults_text);
		checkBoxMergeResult.setToolTipText(Messages.BufferOptionsComposite_chekMergeResults_tooltip);
		checkBoxMergeResult.setLayoutData(gridData1);
		checkBoxMergeResult.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {

				validateParameters();

			}
		});

		GridData gridData21 = new GridData();
		gridData21.horizontalAlignment = GridData.BEGINNING;
		gridData21.grabExcessHorizontalSpace = false;
		gridData21.verticalAlignment = GridData.CENTER;
		// gridData21.widthHint = GRID_DATA_1_WIDTH_HINT;

		labelQuadrantSegments = new Label(parent, SWT.NONE);
		labelQuadrantSegments.setText(Messages.BufferOptionsComposite_labelQuadrantSegments_text);
		labelQuadrantSegments.setLayoutData(gridData21);
		labelQuadrantSegments.setToolTipText(Messages.BufferOptionsComposite_Circular_Approximation_tooltip);

		GridData gridData31 = new GridData();
		gridData31.horizontalAlignment = GridData.BEGINNING;
		gridData31.grabExcessHorizontalSpace = false;
		gridData31.verticalAlignment = GridData.CENTER;
		gridData31.widthHint = 20;

		spinnerQuadrantSegments = new Spinner(parent, SWT.BORDER);
		spinnerQuadrantSegments.setMinimum(3);
		spinnerQuadrantSegments.setLayoutData(gridData31);
		spinnerQuadrantSegments.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {

				validateParameters();
			}

		});

		spinnerQuadrantSegments.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {

				validateParameters();

			}

		});

	}

	/**
	 * Create cap style option buttons
	 * 
	 * @param CapStyleOptionsGroup
	 */
	private void createCapStyleOptions(Composite CapStyleOptionsGroup) {

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;

		groupCapStyle = new Group(CapStyleOptionsGroup, SWT.NONE);
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.BEGINNING;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.verticalAlignment = GridData.BEGINNING;
		groupCapStyle.setLayoutData(gridData4);
		groupCapStyle.setLayout(gridLayout);
		groupCapStyle.setText("Cap style"); //$NON-NLS-1$

		radioCapRound = new Button(groupCapStyle, SWT.RADIO);
		radioCapRound.setText("Round"); //$NON-NLS-1$
		radioCapRound.setToolTipText("Round"); //$NON-NLS-1$
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.BEGINNING;
		gridData5.grabExcessHorizontalSpace = true;
		radioCapRound.setLayoutData(gridData5);

		radioCapFlat = new Button(groupCapStyle, SWT.RADIO);
		radioCapFlat.setText("Flat"); //$NON-NLS-1$
		radioCapFlat.setToolTipText("Flat"); //$NON-NLS-1$
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = GridData.BEGINNING;
		gridData7.grabExcessHorizontalSpace = true;
		radioCapFlat.setLayoutData(gridData7);

		radioCapSquare = new Button(groupCapStyle, SWT.RADIO);
		radioCapSquare.setText("Square"); //$NON-NLS-1$
		radioCapSquare.setToolTipText("Square"); //$NON-NLS-1$
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.BEGINNING;
		gridData6.grabExcessHorizontalSpace = true;
		radioCapSquare.setLayoutData(gridData6);

		radioCapSquare.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				setSelectionCapOption(radioCapSquare);
				validateParameters();
			}
		});

		radioCapFlat.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				setSelectionCapOption(radioCapFlat);
				validateParameters();
			}
		});

		radioCapRound.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				setSelectionCapOption(radioCapRound);
				validateParameters();
			}
		});
	}

	/**
	 * Sets the cap options (radio buttons).
	 * 
	 * @param selectedRadio
	 */
	private void setSelectionCapOption(final Button selectedRadio) {

		// sets cap style options
		this.currentRadioCapStyle = selectedRadio;
		this.radioCapFlat.setSelection(selectedRadio.equals(this.radioCapFlat));
		this.radioCapRound.setSelection(selectedRadio.equals(this.radioCapRound));
		this.radioCapSquare.setSelection(selectedRadio.equals(this.radioCapSquare));

		if (selectedRadio.equals(this.radioCapFlat)) {
			capStyle = CapStyle.capFlat;
		} else if (selectedRadio.equals(this.radioCapSquare)) {
			capStyle = CapStyle.capSquare;
		} else {
			capStyle = CapStyle.capRound;
		}
		this.sessionRadioCapStyle = currentRadioCapStyle;
		BufferCommand cmd = (BufferCommand) getCommand();
		cmd.setCapStyle(capStyle);
	}

	/**
	 * set the current advanced option on command
	 */
	@Override
	protected void setParametersOnCommand(ISOCommand command) {

		BufferCommand cmd = (BufferCommand) command;

		// gets the options values and sends it to controller which does the
		// validation.

		Integer quadrantSegments = getQuadrantSegments();

		Boolean aggregate = getAggregateOption();

		CapStyle cap = getCapStyleOptions();

		cmd.setAdvancedOptions(aggregate, quadrantSegments, cap);
	}

	private CapStyle getCapStyleOptions() {

		CapStyle cap;

		if (this.radioCapSquare.getSelection()) {
			cap = CapStyle.capSquare;
			this.currentRadioCapStyle = radioCapSquare;
		} else if (this.radioCapFlat.getSelection()) {
			cap = CapStyle.capFlat;
			this.currentRadioCapStyle = radioCapFlat;
		} else {
			cap = CapStyle.capRound;
			this.currentRadioCapStyle = radioCapRound;
		}
		this.sessionRadioCapStyle = this.currentRadioCapStyle;
		return cap;
	}

	/**
	 * @return the number of line segments to use per circle quadrant when
	 *         approximating curves generated by the buffer algorithm
	 */
	private Integer getQuadrantSegments() {

		this.currentQuadrantSegments = spinnerQuadrantSegments.getSelection();
		this.sessionQuadrantSegments = this.currentQuadrantSegments;
		return this.currentQuadrantSegments;
	}

	/**
	 * Gets the aggregate option and set the current value.
	 * 
	 * @return true if aggregate was selected.
	 */
	private Boolean getAggregateOption() {

		this.currentAggregateOption = this.checkBoxMergeResult.getSelection();
		this.sessionAggregateOption = this.currentAggregateOption;
		return this.currentAggregateOption;
	}

	/**
	 * Gets the default value from preferences
	 */
	@Override
	protected void initState() {
		// Maintain the values in the session. Only the first time is setted
		// from preferences
		if (this.sessionAggregateOption == null) {
			this.sessionAggregateOption = Preferences.bufferMergeGeometries();
		}
		if (this.sessionQuadrantSegments == null) {
			this.sessionQuadrantSegments = Preferences.bufferQuadrantSegments();
		}
		if (this.sessionRadioCapStyle == null) {
			setSelectionCapOption(this.radioCapRound);
			this.sessionRadioCapStyle = this.radioCapRound;
		}
		BufferCommand cmd = (BufferCommand) getCommand();
		cmd.setDefaultValues(this.sessionAggregateOption, this.sessionQuadrantSegments, capStyle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seees.axios.udig.spatialoperations.internal.ui.parameters.
	 * AbstractParamsPresenter#populate()
	 */
	@Override
	protected void populate() {

		if (this.currentAggregateOption == null) {
			this.checkBoxMergeResult.setSelection(this.sessionAggregateOption);
			this.currentAggregateOption = this.sessionAggregateOption;
		}
		if (this.currentQuadrantSegments == null) {
			this.spinnerQuadrantSegments.setSelection(this.sessionQuadrantSegments);
			this.currentQuadrantSegments = this.sessionQuadrantSegments;

		}
		if (this.currentRadioCapStyle == null) {
			setSelectionCapOption(this.sessionRadioCapStyle);
			this.currentRadioCapStyle = this.sessionRadioCapStyle;
		}
		BufferCommand cmd = (BufferCommand) getCommand();

		cmd.setDefaultValues(this.currentAggregateOption, this.currentQuadrantSegments, capStyle);

		validateParameters();
	}

	/**
	 * Clears the user's inputs
	 */
	@Override
	protected final void clearInputs() {

		this.currentAggregateOption = null;
		this.currentQuadrantSegments = null;
		this.currentRadioCapStyle = null;
	}

	/**
	 * Sets enable/disable the composite's widgets
	 */
	@Override
	public void setEnabled(boolean enabled) {

		this.checkBoxMergeResult.setEnabled(enabled);
		this.spinnerQuadrantSegments.setEnabled(enabled);
		this.groupCapStyle.setEnabled(enabled);
	}

}
