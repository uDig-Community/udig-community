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
package es.axios.udig.ui.editingtools.precisionsegmentcopy.view;

import java.util.Observable;
import java.util.Observer;

import net.refractions.udig.project.EditManagerEvent;
import net.refractions.udig.project.IEditManager;
import net.refractions.udig.project.IEditManagerListener;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.vividsolutions.jts.geom.Coordinate;

import es.axios.udig.ui.editingtools.internal.i18n.Messages;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.SegmentCopyContext;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsContext;
import es.axios.udig.ui.editingtools.precisiontools.commons.internal.PrecisionToolsMode;

/**
 * Main composite of the parallel view. Only will show its widget when the
 * {@link SegmentCopyContext} is set.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * 
 */
public class SegmentCopyParametersComposite extends Composite implements Observer {

	private SegmentCopyContext		segmentCopyContext	= null;

	// controls

	private CLabel					labelReferenceLine	= null;
	private Text					textInitialCoorX	= null;
	private CLabel					labelInitialCoorX	= null;
	private Text					textInitialCoorY	= null;
	private CLabel					labelInitialCoorY	= null;
	private Text					textDistanceX		= null;
	private CLabel					labelDistanceX		= null;
	private Text					textDistanceY		= null;
	private CLabel					labelDistanceY		= null;
	private Text					textlength			= null;
	private CLabel					labelLength			= null;
	private Button					buttonConversion	= null; // TODO change
	// the name.
	private Button					buttonRestart		= null;

	// Data

	private String					referenceLine		= "";
	private String					distanceX			= "";
	private String					distanceY			= "";
	private String					coordinateX			= "";
	private String					coordinateY			= "";
	private String					length				= "";

	private IEditManagerListener	editListener		= null;
	/**
	 * The thread that creates the SWT controls, is needed because only the
	 * creator thread could edit SWT controls.
	 */
	private Thread					fatherThread		= null;

	private IToolContext			context;

	/** Identifier, knows which text box has changed. */
	private static enum Identifier {
		DISTANCE_X, DISTANCE_Y, COOR_X, COOR_Y
	};

	/**
	 * Default constructor for all the composites. Only will show its widget
	 * when the {@link SegmentCopyContext} is set.
	 * 
	 * @param parent
	 * @param style
	 */
	public SegmentCopyParametersComposite(Composite parent, int style) {

		super(parent, style);
		createContent();
		this.setVisible(false);
	}

	/**
	 * Set the parallelContext, add this composite as observer to
	 * parallelContext and show its widget.
	 * 
	 * @param context
	 */
	public void setSegmentCopyContext(SegmentCopyContext context) {

		assert context != null;

		this.segmentCopyContext = context;
		this.segmentCopyContext.addObserver(this);
		this.setVisible(true);
	}

	/**
	 * Creates its controls.
	 */
	private void createContent() {

		// Store the thread which creates the controls.
		this.fatherThread = Thread.currentThread();

		initListener();

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		this.setLayout(gridLayout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalSpan = 3;
		gridData.verticalAlignment = GridData.BEGINNING;

		labelReferenceLine = new CLabel(this, SWT.NONE);
		labelReferenceLine.setLayoutData(gridData);
		labelReferenceLine.setText(Messages.PrecisionSegmentCopy_reference_line + ":" + referenceLine);
		labelReferenceLine.setToolTipText(Messages.PrecisionSegmentCopy_reference_line);

		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = false;
		gridData1.verticalAlignment = GridData.BEGINNING;

		buttonRestart = new Button(this, SWT.NONE);
		buttonRestart.setLayoutData(gridData1);
		buttonRestart.setText(Messages.PrecisionSegmentCopy_buttonRestartText);
		buttonRestart.setToolTipText(Messages.PrecisionSegmentCopy_buttonRestartTooltip);
		buttonRestart.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {

				if (!(segmentCopyContext.isLineSegmentEmpty())) {
					segmentCopyContext.initContext();
				}
			}
		});

		labelInitialCoorX = new CLabel(this, SWT.NONE);
		labelInitialCoorX.setLayoutData(gridData1);
		labelInitialCoorX.setText(Messages.PrecisionSegmentCopy_initial_coorX + ":");
		labelInitialCoorX.setToolTipText(Messages.PrecisionSegmentCopy_initial_coorX + ":" + this.coordinateX);

		textInitialCoorX = new Text(this, SWT.BORDER);
		textInitialCoorX.setLayoutData(gridData1);
		textInitialCoorX.setText(this.coordinateX);
		textInitialCoorX.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				changeValues(textInitialCoorX, Identifier.COOR_X);
			}
		});

		labelInitialCoorY = new CLabel(this, SWT.NONE);
		labelInitialCoorY.setLayoutData(gridData1);
		labelInitialCoorY.setText(Messages.PrecisionSegmentCopy_initial_coorY + ":");
		labelInitialCoorY.setToolTipText(Messages.PrecisionSegmentCopy_initial_coorY + ":" + this.coordinateY);

		textInitialCoorY = new Text(this, SWT.BORDER);
		textInitialCoorY.setLayoutData(gridData1);
		textInitialCoorY.setText(this.coordinateY);
		textInitialCoorY.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				changeValues(textInitialCoorY, Identifier.COOR_Y);
			}
		});

		labelDistanceX = new CLabel(this, SWT.NONE);
		labelDistanceX.setLayoutData(gridData1);
		labelDistanceX.setText(Messages.PrecisionSegmentCopy_distanceX + ":");
		labelDistanceX.setToolTipText(Messages.PrecisionSegmentCopy_distanceX + ":" + this.distanceX);

		textDistanceX = new Text(this, SWT.BORDER);
		textDistanceX.setLayoutData(gridData1);
		textDistanceX.setText(this.distanceX);
		textDistanceX.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				changeValues(textDistanceX, Identifier.DISTANCE_X);
			}
		});

		labelDistanceY = new CLabel(this, SWT.NONE);
		labelDistanceY.setLayoutData(gridData1);
		labelDistanceY.setText(Messages.PrecisionSegmentCopy_distanceY + ":");
		labelDistanceY.setToolTipText(Messages.PrecisionSegmentCopy_distanceY + ":" + this.distanceY);

		textDistanceY = new Text(this, SWT.BORDER);
		textDistanceY.setLayoutData(gridData1);
		textDistanceY.setText(this.distanceY);
		textDistanceY.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				changeValues(textDistanceY, Identifier.DISTANCE_Y);
			}
		});

		labelLength = new CLabel(this, SWT.NONE);
		labelLength.setLayoutData(gridData1);
		labelLength.setText(Messages.PrecisionSegmentCopy_length + ":");
		labelLength.setToolTipText(Messages.PrecisionSegmentCopy_length + ":" + this.length);

		textlength = new Text(this, SWT.BORDER);
		textlength.setLayoutData(gridData1);
		textlength.setText(this.length);

		buttonConversion = new Button(this, SWT.NONE);
		buttonConversion.setLayoutData(gridData1);
		buttonConversion.setText(Messages.PrecisionSegmentCopy_buttonConversionText);
		buttonConversion.setToolTipText(Messages.PrecisionSegmentCopy_buttonConversionToolTip);
		buttonConversion.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				// //Only change it when reference line exist.
				// if(parallelContext.getReferenceLine()!=null){
				// parallelContext.changeReferenceCoor();
				// }
				// Only change it when reference line exist.
				if (!(segmentCopyContext.isLineSegmentEmpty())) {
					segmentCopyContext.changeReferenceCoor();
				}
			}
		});

	}

	/**
	 * Creates the default listeners.
	 */
	private void initListener() {

		this.editListener = new IEditManagerListener() {

			public void changed(EditManagerEvent event) {

				updateEditManagerAction(event);
			}
		};
	}

	/**
	 * When a roll back occurs
	 */
	private void updateEditManagerAction(EditManagerEvent event) {

		int eventType = event.getType();

		switch (eventType) {

		case EditManagerEvent.POST_ROLLBACK:
		case EditManagerEvent.POST_COMMIT:

			clearData();
			break;
		default:
			break;
		}
	}

	/**
	 * Called when a Roll back occurred. Initializes the segment copy context
	 * and clear the data showed in the view.
	 */
	private void clearData() {

		if (segmentCopyContext != null) {
			segmentCopyContext.initContext();
			referenceLine = "";
			distanceX = "";
			distanceY = "";
			coordinateX = "";
			coordinateY = "";
			length = "";
			populate();
		}
	}

	// TODO improve the code about: Don't calculate distance when is introduced
	// by the user.
	/**
	 * Captures the changes made by the user and reflect it on the segment copy
	 * preview. While in progress, set the state as BUSY, and update the context
	 * at the end.
	 * 
	 * @param text
	 *            The text box that has changed.
	 * @param identifier
	 *            Knows which text box has changed its value.
	 */
	private void changeValues(Text textControl, Identifier identifier) {

		if ((this.segmentCopyContext.getMode() == PrecisionToolsMode.BUSY)
					|| (this.segmentCopyContext.getMode() == PrecisionToolsMode.EDITING)) {
			return;
		}

		PrecisionToolsMode previousState = this.segmentCopyContext.getMode();

		// When we modified the distance between reference coordinate and
		// initial coordinate,
		// we set the state as EDITING, because later, don't want to calculate
		// automatically the distance.
		if (identifier == Identifier.DISTANCE_X || identifier == Identifier.DISTANCE_Y) {
			this.segmentCopyContext.setMode(PrecisionToolsMode.EDITING);
		} else {
			this.segmentCopyContext.setMode(PrecisionToolsMode.BUSY);
		}

		try {
			double value = Double.valueOf(textControl.getText());

			Coordinate oldCoordinate, newCoordinate, refCoordinate;
			switch (identifier) {
			case DISTANCE_X:
				oldCoordinate = this.segmentCopyContext.getInitialCoordinate();
				refCoordinate = this.segmentCopyContext.getReferenceCoordinate();

				newCoordinate = new Coordinate(refCoordinate.x + value, oldCoordinate.y);
				this.segmentCopyContext.setInitialCoordinate(newCoordinate);
				this.segmentCopyContext.setDistanceCoorX(value);
				break;
			case COOR_X:
				oldCoordinate = this.segmentCopyContext.getInitialCoordinate();

				newCoordinate = new Coordinate(value, oldCoordinate.y);
				this.segmentCopyContext.setInitialCoordinate(newCoordinate);
				break;
			case DISTANCE_Y:
				oldCoordinate = this.segmentCopyContext.getInitialCoordinate();
				refCoordinate = this.segmentCopyContext.getReferenceCoordinate();

				newCoordinate = new Coordinate(oldCoordinate.x, refCoordinate.y + value);
				this.segmentCopyContext.setInitialCoordinate(newCoordinate);
				this.segmentCopyContext.setDistanceCoorY(value);
				break;
			case COOR_Y:
				oldCoordinate = this.segmentCopyContext.getInitialCoordinate();

				newCoordinate = new Coordinate(oldCoordinate.x, value);
				this.segmentCopyContext.setInitialCoordinate(newCoordinate);
				break;
			default:
				assert false : "Illegal control";
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {

			this.segmentCopyContext.setMode(previousState);
			this.segmentCopyContext.update(PrecisionToolsContext.UPDATE_LAYER);
		}
	}

	/**
	 * Called when the user select a Reference line of the Map or when
	 * select/change the Initial Coordinate of the segment copy preview.
	 * 
	 * Access to the Display which have the thread that creates the SWT
	 * controls. This thread is needed for modifying the SWT controls. After
	 * this, refresh the text box with the new values.
	 */
	private void populate() {

		Display.findDisplay(fatherThread).asyncExec(new Runnable() {

			public void run() {

				PrecisionToolsMode previousState = segmentCopyContext.getMode();
				segmentCopyContext.setMode(PrecisionToolsMode.BUSY);

				if (!labelReferenceLine.isDisposed()) {
					labelReferenceLine.setText(Messages.PrecisionSegmentCopy_reference_line + ":" + referenceLine);
					labelReferenceLine.setToolTipText(Messages.PrecisionSegmentCopy_reference_line + ":"
								+ referenceLine);
				}
				if (!textInitialCoorX.isDisposed()) {
					textInitialCoorX.setText(coordinateX);
				}
				if (!textInitialCoorY.isDisposed()) {
					textInitialCoorY.setText(coordinateY);
				}
				if (!textDistanceX.isDisposed()) {
					textDistanceX.setText(distanceX);
				}
				if (!textDistanceY.isDisposed()) {
					textDistanceY.setText(distanceY);
				}
				if (!textlength.isDisposed()) {
					textlength.setText(length);
				}
				segmentCopyContext.setMode(previousState);
			}
		});

	}

	/**
	 * Reflect changes made on the map in the view. Set the parameters, later
	 * other event will updates the changes.
	 */
	public void update(Observable o, Object arg) {

		// Only will update data when the State is WAITING (have a
		// reference line and is
		// waiting for initial coor) and when the State is READY ( have a
		// reference line and initial
		// coor)
		if ((this.segmentCopyContext.getMode() == PrecisionToolsMode.BUSY)
					|| (this.segmentCopyContext.getMode() == PrecisionToolsMode.EDITING)) {
			return;
		}

		if (!(segmentCopyContext.isLineSegmentEmpty())) {
			this.referenceLine = segmentCopyContext.getLineSegmentToString();
		} else {
			this.referenceLine = "";
		}

		if (segmentCopyContext.getInitialCoordinate() != null) {
			Coordinate coor = segmentCopyContext.getInitialCoordinate();
			this.coordinateX = String.valueOf(coor.x);
			this.coordinateY = String.valueOf(coor.y);

			this.segmentCopyContext.calculateDistanceCoordinate();
			this.distanceX = String.valueOf(segmentCopyContext.getDistanceCoorX());
			this.distanceY = String.valueOf(segmentCopyContext.getDistanceCoorY());
			// TODO calculate the length
			// this.length = String.valueOf(segmentCopyContexts.getLength());
			this.length = "";
		} else {
			this.coordinateX = "";
			this.coordinateY = "";
			this.distanceX = "";
			this.distanceY = "";
			this.length = "";
		}
		populate();
	}

	public void setContext(IToolContext context) {

		IEditManager editManager;
		if (context == null) {
			// initialize or reinitialize
			editManager = getCurrentEditManager();
			if (editManager != null) {
				removeListenerFrom(editManager);
			}
		} else {
			// sets editManager and add its listeners
			editManager = context.getEditManager();
			if (editManager != null) {

				addListenersTo(editManager);
			}
		}
		this.context = context;

	}

	/**
	 * Removes the listener from the edit manager.
	 * 
	 * @param editManager
	 */
	private void removeListenerFrom(IEditManager editManager) {

		assert editManager != null;
		assert this.editListener != null;

		editManager.removeListener(this.editListener);
	}

	/**
	 * Get the current edit manager.
	 * 
	 * @return
	 */
	private IEditManager getCurrentEditManager() {

		if (this.context == null) {
			return null;
		}
		return context.getEditManager();
	}

	/**
	 * Add listener to the edit manager.
	 * 
	 * @param editManager
	 */
	private void addListenersTo(IEditManager editManager) {

		assert editManager != null;
		assert this.editListener != null;

		editManager.addListener(this.editListener);
	}

}
