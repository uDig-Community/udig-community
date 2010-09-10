/**
 * 
 */
package net.refractions.linecleaner;

public abstract class LogAction {
	public abstract String getStageName();
	public abstract String getProcessName();
	
	public String toString() {
		return getStageName()+LoggingSystem.SEPARATOR+getProcessName();
	}
}