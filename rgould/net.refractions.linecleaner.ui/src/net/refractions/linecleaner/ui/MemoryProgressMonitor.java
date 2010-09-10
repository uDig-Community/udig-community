package net.refractions.linecleaner.ui;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class can be used to occasionally check memory usage, and do something
 * if available memory is too low. 
 * 
 * It wraps itself around another IProgressMonitor, and whenever worked() is
 * called, it compares Runtime.freeMemory() to the value minimum passed into
 * the constructor. If freeMemory is less than minimum, runnable.run() is
 * called, which ideally should free up some memory.
 * 
 * @author rgould
 *
 */
public class MemoryProgressMonitor implements IProgressMonitor {

	IProgressMonitor delegate;
	private long minimum;
	private Runnable runnable;
	
	/**
	 * 
	 * @param delegate
	 * @param minimum if freeMemory drops below this, runnable.run() is called. measured in bytes.
	 * @param runnable
	 */
	public MemoryProgressMonitor(IProgressMonitor delegate, long minimum, Runnable runnable) {
		this.delegate = delegate;
		this.minimum = minimum;	
		this.runnable = runnable;
	}

	public void beginTask(String name, int totalWork) {
		delegate.beginTask(name, totalWork);
	}

	public void done() {
		delegate.done();
	}

	public void internalWorked(double work) {
		
		long freeMemory = Runtime.getRuntime().freeMemory();
		
		if (freeMemory <= minimum) {
			runnable.run();
		}
		
		delegate.internalWorked(work);
	}

	public boolean isCanceled() {
		return delegate.isCanceled();
	}

	public void setCanceled(boolean value) {
		delegate.setCanceled(value);
	}

	public void setTaskName(String name) {
		delegate.setTaskName(name);
	}

	public void subTask(String name) {
		delegate.subTask(name);
	}

	public void worked(int work) {
		this.internalWorked(work);
	}

}
