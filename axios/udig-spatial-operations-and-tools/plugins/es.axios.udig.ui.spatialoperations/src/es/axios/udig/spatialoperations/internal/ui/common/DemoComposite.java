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
package es.axios.udig.spatialoperations.internal.ui.common;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.parameters.IImageOperation;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;

/**
 * 
 * Abstract class implemented by the classes that shows the demo. It's an
 * observer because it have to changes the demo image when the operations
 * options changed.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @author Iratxe Lejarreta (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class DemoComposite extends Composite implements Observer {

	private Label				demoLabel		= null;
	private Button				checkBoxSource	= null;
	private Button				checkBoxResult	= null;

	private Boolean				sourceChecked	= null;
	private Boolean				resultChecked	= null;

	protected IImageOperation	images			= null;
	protected ISOCommand		cmd				= null;

	protected Thread			uiThread		= null;

	public DemoComposite(Composite parent, int style, IImageOperation image) {

		super(parent, style);
		createContents();

		this.images = image;
		this.visibleImage(images.getDefaultImage());
	}

	protected void createContents() {

		this.uiThread = Thread.currentThread();

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		setLayout(layout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;

		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = false;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.horizontalSpan = 2;

		checkBoxSource = new Button(this, SWT.CHECK);
		checkBoxSource.setLayoutData(gridData);
		checkBoxSource.setText(Messages.DemoComposite_checkSource_text);
		checkBoxSource.setToolTipText(Messages.DemoComposite_checkSource_tooltip);
		checkBoxSource.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {

				checkBoxes();
			}
		});

		checkBoxResult = new Button(this, SWT.CHECK);
		checkBoxResult.setLayoutData(gridData);
		checkBoxResult.setText(Messages.DemoComposite_checkResult_text);
		checkBoxResult.setToolTipText(Messages.DemoComposite_checkResult_tooltip);
		checkBoxResult.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {

				checkBoxes();
			}
		});

		checkBoxSource.setSelection(true);
		checkBoxResult.setSelection(true);
		this.sourceChecked = checkBoxSource.getSelection();
		this.resultChecked = checkBoxResult.getSelection();

		demoLabel = new Label(this, SWT.NONE);
		demoLabel.setText(""); //$NON-NLS-1$
		demoLabel.setLayoutData(gridData1);
	}

	/**
	 * Check the Source and Result checkboxes if the are checked.
	 */
	private void checkBoxes() {

		this.sourceChecked = checkBoxSource.getSelection();
		this.resultChecked = checkBoxResult.getSelection();

		update();
	}

	/**
	 * 
	 * @return True if source checkbox is checked
	 */
	protected Boolean getSourceChecked() {

		return this.sourceChecked;
	}

	/**
	 * 
	 * @return True if result checkbox is checked
	 */
	protected Boolean getResultChecked() {

		return this.resultChecked;
	}

	/**
	 * 
	 * @param img
	 *            The image to be showed on the demoComposite.
	 */
	protected void visibleImage(final Image img) {

		Display.findDisplay(uiThread).asyncExec(new Runnable() {

			public void run() {
				if (demoLabel.isDisposed()) {
					return;
				}

				if (img == null) {

					demoLabel.setVisible(false);
				} else {

					demoLabel.setImage(img);
					demoLabel.setVisible(true);
					// Refresh the label to show the image.
					demoLabel.pack();
				}
			}
		});
	}

	/**
	 * When the observer update, call the update() thats is defined in each
	 * subclasses and there is implemented the logical of update the
	 * correspondent image.
	 * 
	 * @param o
	 *            An instance of ISOCommand
	 * 
	 */
	public void update(Observable o, Object arg) {

		update();
	}

	/**
	 * Change the current image the demo is showing.
	 */
	public void update() {

		// images.setCmd(this.cmd);

		this.visibleImage(images.getImage(getSourceChecked(), getResultChecked()));

	}

	/**
	 * Set the command with the demo composite.
	 * 
	 * @param cmd
	 *            The correspondent command of each spatial operation.
	 */
	public void setCommand(ISOCommand cmd) {

		assert cmd != null : "command can't be null"; //$NON-NLS-1$

		this.cmd = cmd;

		this.cmd.addObserver(this);

		this.images.setCommand(this.cmd);
	}

}
