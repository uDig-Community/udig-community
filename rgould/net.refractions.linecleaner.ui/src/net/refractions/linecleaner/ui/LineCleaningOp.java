package net.refractions.linecleaner.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.refractions.linecleaner.cleansing.PauseMonitor;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class LineCleaningOp implements IOp {
	
	public void op(final Display display, Object target, IProgressMonitor monitor) throws Exception {
		
		List<ILayer> layers = new ArrayList<ILayer>();

		if (target instanceof ILayer) {
			layers.add((ILayer) target);
		} else if (target instanceof ILayer[]) {
			ILayer[] array = (ILayer[]) target;
			layers = Arrays.asList(array);
		}
		
		final LineCleaningWizard wizard = new LineCleaningWizard(layers);
		
		display.asyncExec(new Runnable() {
			public void run() {
				WizardDialog dialog = new PauseableWizardDialog(display.getActiveShell(), wizard);
				dialog.open();
			}
		});
	}

	class PauseableWizardDialog extends WizardDialog {
		protected Button pauseButton;
		protected IWizard wizard;
		
		public final PauseMonitor pauseMonitor = new PauseMonitor();
		
		public PauseableWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
			this.wizard = newWizard;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			pauseButton = createButton(parent, -1, "Pause", false);
			pauseButton.setEnabled(false);
			
			super.createButtonsForButtonBar(parent);
			
			pauseMonitor.attachToPauseButton(pauseButton);
			pauseMonitor.attachToCancelComponent(getButton(IDialogConstants.CANCEL_ID));
		}

		@Override
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			try {
				super.run(fork, cancelable, runnable);
			} finally {
				pauseMonitor.removeFromCancelComponent(getButton(IDialogConstants.CANCEL_ID));
				pauseMonitor.removeFromPauseButton(pauseButton);
			}
		}
		
		@Override
		protected void finishPressed() {
			pauseButton.setEnabled(true);
			Cursor arrowCursor = new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW);
            pauseButton.setCursor(arrowCursor);
			super.finishPressed();
		}
	}
}
