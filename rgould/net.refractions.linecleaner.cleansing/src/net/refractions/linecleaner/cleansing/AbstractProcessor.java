package net.refractions.linecleaner.cleansing;

import java.io.File;
import java.io.IOException;

import net.refractions.udig.project.internal.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;

public abstract class AbstractProcessor {
	
	private static int count = 0;
	
	protected FeatureStore featureStore;
	protected Map map;
	private String name;
	
	
	public AbstractProcessor(Map map, FeatureStore featureStore) {
		count++;
		this.map = map;
		this.featureStore = featureStore;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	protected String getName() {
		return name;
	}

	public void run() throws IOException {
		run(validateMonitor(null), null);
	}
	
	public void run(IProgressMonitor monitor, PauseMonitor pauseMonitor) throws IOException {
		try {
			pauseMonitor.addListener(this);
			preRun();
			runInternal(validateMonitor(monitor), pauseMonitor);
		} finally {
			pauseMonitor.removeListener(this);
		}
	}

	protected void pauseIfNecessary(PauseMonitor pauseMonitor) {
		if (pauseMonitor != null && pauseMonitor.isPaused()) {
			synchronized (this) {
				while (pauseMonitor.isPaused()) {
					try {
						// Wait for pauseMonitor to notify() us.
						// This object is added as a listener
						// to the pauseMonitor for this purpose in run()
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private IProgressMonitor validateMonitor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();
		return monitor;
	}

	protected abstract void runInternal(IProgressMonitor monitor, PauseMonitor pauseMonitor)
	throws IOException;

	protected void preRun() throws IOException {
		if (!isDebugging()) {
			return;
		}
		
		String typename = featureStore.getSchema().getTypeName();
		String tmpDir = System.getProperty("java.io.tmpdir");
		String separator = System.getProperty("file.separator");
		String number = "";
		if (count < 10) {
			number = "0";
		}
		number = number.concat(""+count);
		File file = new File(tmpDir + separator + typename + number + "-pre"+getName()+".shp");
		if (file.exists()) {
			if (!file.delete()) {
				System.err.println("Unable to delete " + file);
				return;
			}
		}
		if (!file.createNewFile()) {
			System.err.println("Unable to create file " + file);
			return;
		}
		
		FeatureCollection fc = featureStore.getFeatures();
		
		ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        ShapefileDataStore ds = (ShapefileDataStore)factory.createDataStore(file.toURL());
        ds.createSchema(fc.getSchema());
        ((FeatureStore) ds.getFeatureSource()).addFeatures(fc.reader());
	}
	
	protected boolean isDebugging() {
		if (CleansingPlugin.getDefault() != null)
			return CleansingPlugin.getDefault().isDebugging();
		return false;
	}
}
