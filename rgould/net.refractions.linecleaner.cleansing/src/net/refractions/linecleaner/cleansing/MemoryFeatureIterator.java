package net.refractions.linecleaner.cleansing;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import net.refractions.udig.project.internal.Map;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactoryFinder;

public class MemoryFeatureIterator {
	// TODO: make the threshold proportional to the size of input and total heap memory
	private static final long DEFAULT_LOW_MEMORY_THRESHOLD = 150000000; // 150MB
	FeatureIterator delegate;
	FeatureSource source;
	Query query;
	long minimum;
	private Map map;
	private Set<String> visitedFids;
	
	public MemoryFeatureIterator(FeatureSource source, Map map, Query query, long minimum) throws IOException {
		this.source = source;
		this.map = map;
		this.query = query;
		this.minimum = minimum;
		
		this.delegate = source.getFeatures(query).features();
		this.visitedFids = new TreeSet<String>();
	}
	
	public MemoryFeatureIterator(FeatureSource source, Map map, Query query) throws IOException {
		this(source, map, query, DEFAULT_LOW_MEMORY_THRESHOLD);
	}

	public void close() {
		delegate.close();
	}

	public boolean hasNext() throws IOException {
		if (isMemoryLow()) {
			delegate.close();
			map.getEditManagerInternal().commitTransaction();
			System.gc();
			System.runFinalization();
			DefaultQuery newQuery = new DefaultQuery(query);
			
			FidFilter newFilter = FilterFactoryFinder.createFilterFactory().createFidFilter();
			
			newFilter.addAllFids(visitedFids);
			
			newQuery.setFilter(query.getFilter().and(newFilter.not()));
			
			delegate = source.getFeatures(newQuery).features();
		}
		return delegate.hasNext();
	}

	public Feature next() throws NoSuchElementException, IOException {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		Feature current = delegate.next();
		visitedFids.add(current.getID());
		return current;
	}
	
	private boolean isMemoryLow() {
		// freeMemory (on windows at least) is actually the amount of currently
		// allocated heap memory - memory used.  Committing when this is low
		// causes more commits than is needed.  UnmallocedMemory is when we
		// hit the real upper bound of maximum memory available for the heap.
		Runtime r = Runtime.getRuntime();
		long unmallocedMemory = r.maxMemory() - r.totalMemory();
		long freeMemory = r.freeMemory();
		
		if (unmallocedMemory <= minimum && freeMemory <= minimum) {
			return true;
		}
		return false;
	}
	
	/**
	 * Creates a new MemoryFeatureIterator with a query that uses Filter.NONE and only the geometry name.
	 * @param store
	 * @param map
	 * @return
	 * @throws IOException
	 */
	public static MemoryFeatureIterator createDefault(FeatureStore store, Map map) throws IOException {
		return new MemoryFeatureIterator(store, map, 
				new DefaultQuery(store.getSchema().getTypeName(), Filter.NONE, 
						new String[] { store.getSchema().getDefaultGeometry().getName() }));
	}
}
