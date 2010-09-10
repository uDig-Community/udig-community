package net.refractions.linecleaner.cleansing;

/**
 * Constants for use with eclipse tracing api.
 * Remember: only engage tracing if CleansingPlugin.getDefault().isDebugging().
 * <p>
 * Sample use:<pre><code>
 * static import net.refractions.linecleaner.cleansing.PROCESSING;
 * 
 * if( CleansingPlugin.isDebugging( PROCESSING ) ){
 *      System.out.println( "your message here" );
 * }
 * </code></pre>
 * </p>
 */
public interface Trace {
    /** You may set this to "true" in your .options file */
    public static final String REQUEST =
        "net.refractions.linecleaner.cleansing/debug/processing"; //$NON-NLS-1$    
}