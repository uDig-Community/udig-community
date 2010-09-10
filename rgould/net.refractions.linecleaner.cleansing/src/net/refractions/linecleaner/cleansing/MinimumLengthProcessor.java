package net.refractions.linecleaner.cleansing;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.refractions.linecleaner.LoggingSystem;
import net.refractions.udig.project.internal.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;

/**
 * Runs through a FeatureStore and deletes all features that are less
 * than <code>minimumLength</code>.
 * 
 * @author rgould
 *
 */
public class MinimumLengthProcessor extends AbstractProcessor {

	public static final double DEFAULT_MINIMUM_LENGTH = 10;
	private double minimumLength;
	LoggingSystem loggingSystem = LoggingSystem.getInstance();

	/**
	 * FeatureStore is the set of data to perform the operation on.
	 * MinimumLength is the measurement used to determine whether a given
	 * feature should be allowed to continue to exist or not.
	 * 
	 * If the length of a feature is less than <code>minimumLength</code> the
	 * feature will be erased.
	 * 
	 * @param dataStore
	 * @param minimumLength
	 */
	public MinimumLengthProcessor (Map map, FeatureStore featureStore, double minimumLength) {
		super(map, featureStore);
		this.minimumLength = minimumLength;
	}
	
	protected void runInternal(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {

		if (monitor == null) monitor = new NullProgressMonitor();
		
		monitor.beginTask("", featureStore.getCount(Query.ALL));
		monitor.subTask("Removing small features");
		
		loggingSystem.setCurrentAction(LoggingSystem.MINIMUM_LENGTH);
		loggingSystem.begin();
		
		FilterFactory factory = FilterFactoryFinder.createFilterFactory();
		CompareFilter compareFilter;
		try {
			compareFilter = factory.createCompareFilter(FilterType.COMPARE_LESS_THAN_EQUAL);
			compareFilter.addLeftValue(new LengthExpression(monitor));
			compareFilter.addRightValue(factory.createLiteralExpression(minimumLength));
			
		} catch (IllegalFilterException e) {
			throw (IOException) new IOException().initCause(e);
		}
		
		Handler handler = new Handler() {
			public void publish(LogRecord record) {
				System.out.println(record.getMessage());
			}
			public void flush() {}
			public void close() throws SecurityException {}
		};
		
		Logger logger = Logger.getLogger("org.geotools.data");
		Level preserve = logger.getLevel();
		logger.setLevel(Level.FINEST);
		logger.addHandler(handler);
		
		Logger loggerCore = Logger.getLogger("org.geotools.core");
		Level preserve2 = loggerCore.getLevel();
		loggerCore.setLevel(Level.FINEST);
		logger.addHandler(handler);
		
		featureStore.removeFeatures(compareFilter);
		logger.setLevel(preserve);
		loggerCore.setLevel(preserve2);
		
		loggingSystem.finish();
		monitor.done();
	}
}
