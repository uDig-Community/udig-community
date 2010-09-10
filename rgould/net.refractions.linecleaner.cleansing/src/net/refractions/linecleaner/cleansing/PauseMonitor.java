package net.refractions.linecleaner.cleansing;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class PauseMonitor {
	private boolean paused = false;
	private Button pauseButton;
	private Set<Object> listeners = new HashSet<Object>(); 
	
	private Listener cancelListener = new Listener() {
		public void handleEvent(Event event) {
			pauseButton.setEnabled(false);
			setPaused(false);
			for (Object o: listeners) {
				synchronized (o) {
					o.notify();
				}
			}
		}
	};
	
    protected Listener pauseListener = new Listener() {
        public void handleEvent(Event e) {
            setPaused(!isPaused());
            if (isPaused()) {
            	pauseButton.setText("Resume");
            }
        }
    };

    public void attachToPauseButton(Button pauseComponent) {
        pauseButton = pauseComponent;
        pauseButton.addListener(SWT.Selection, pauseListener);
    }
    
    public void attachToCancelComponent(Control cancel) {
    	cancel.addListener(SWT.Selection, cancelListener);
    }
    
	public synchronized void setPaused(boolean value) {
		paused = value;
		if (!paused) {
			pauseButton.setText("Pause");
			for (Object o: listeners) {
				synchronized (o) {
					o.notify();
				}
			}
		} else {
			pauseButton.setText("Resume");
		}
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public void addListener(Object o) {
		listeners.add(o);
	}
	
	public void removeListener(Object o) {
		listeners.remove(o);
	}

	public void removeFromCancelComponent(Control control) {
		control.removeListener(SWT.Selection, cancelListener);
	}
	
	public void removeFromPauseButton(Button cancel) {
		cancel.removeListener(SWT.Selection, pauseListener);
		pauseButton = null;
	}
}
