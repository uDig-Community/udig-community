package net.refractions.linecleaner.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.refractions.linecleaner.FeatureUtil;
import net.refractions.linecleaner.cleansing.MinimumLengthProcessor;
import net.refractions.linecleaner.cleansing.PauseMonitor;
import net.refractions.linecleaner.cleansing.PerformCleansingAction;
import net.refractions.linecleaner.cleansing.SimilarLinesProcessor;
import net.refractions.linecleaner.ui.LineCleaningOp.PauseableWizardDialog;
import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.LayerFactory;
import net.refractions.udig.project.internal.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;

public class LineCleaningWizard extends Wizard {

	private static final String WINDOW_TITLE = "Line Cleaning Wizard";
	private List<ILayer> layers;
	private DataSelectionPage dataSelectionPage;
	private OutputSelectionPage outputSelectionPage;
	private OptionsPage optionsPage;
	private IGeoResource resource;
	private List<FeatureStore> featureStores;
	private Layer layer;

	
	public LineCleaningWizard (List<ILayer> layers) {
		this.layers = layers;
		setWindowTitle(WINDOW_TITLE);
		setNeedsProgressMonitor(true);
	}
		
	@Override
	public void addPages() {
		dataSelectionPage = new DataSelectionPage(layers);
		outputSelectionPage = new OutputSelectionPage();
		optionsPage = new OptionsPage(UiPlugin.getDefault().getDialogSettings());
		
		dataSelectionPage.setWizard(this);
		outputSelectionPage.setWizard(this);
		optionsPage.setWizard(this);
		
		addPage(dataSelectionPage);
		addPage(outputSelectionPage);
		addPage(optionsPage);
	}
	
	@Override
	public boolean performFinish() {
		optionsPage.saveSettings();
        long start=System.currentTimeMillis();
		this.featureStores = getFeatureStores();
		final double minimumLength = optionsPage.getMinimumLength();
		final double minimumCycleLength = optionsPage.getCyclesLength();
		final double nodeDistanceTolerance = optionsPage.getDistanceTolerance();
		final double areaTolerance = optionsPage.getAreaTolerance();
		final double douglasPeuckerTolerance = optionsPage.getDPTolerance();
		final double samplingDistance = optionsPage.getSamplingDistance();
		final double verySimilarTolerance = optionsPage.getVerySimilarTolerance();
		final double similarTolerance = optionsPage.getSimilarTolerance();
		
		final String outputFileNoExt = outputSelectionPage.getFileNoExtension();
		
		final boolean cleanse = dataSelectionPage.cleanse.getSelection();
		final boolean clean = dataSelectionPage.clean.getSelection();
		
		final PauseMonitor pauseMonitor = ((PauseableWizardDialog) getContainer()).pauseMonitor;
		
		IRunnableWithProgress process = new IRunnableWithProgress() {
			final int CLEANSE_TICKS = 82;
			final int MIN_LENGTH_TICKS = 1;
			final int CLEAN_TICKS = 14;
			final int MIN_LENGTH_TICKS2 = 1;
			final int FINAL_COMMIT_TICKS = 2;
			
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				try {
					if (monitor == null) monitor = new NullProgressMonitor();
//					monitor = new MemoryProgressMonitor(monitor, 100000, new Runnable() {
//					
//						public void run() {
//							try {
//								System.out.println("Committing...");
//								layer.getMapInternal().getEditManagerInternal().commitTransaction();
//                                Runtime.getRuntime().gc();
////                                Thread.sleep(2000);
//							} catch (IOException e) {
//								new RuntimeException("Unable to commit", e);
////							} catch (InterruptedException e) {
////                                e.printStackTrace();
////                                System.err.println("Trying to continue...");
//                            }
//						}
//					
//					});

					final int TOTAL_TICKS;
					if (cleanse && clean) {
						TOTAL_TICKS = CLEANSE_TICKS + MIN_LENGTH_TICKS + CLEAN_TICKS 
						+ MIN_LENGTH_TICKS2 + FINAL_COMMIT_TICKS;
					} else if (cleanse) {
						TOTAL_TICKS = CLEANSE_TICKS + MIN_LENGTH_TICKS2 + FINAL_COMMIT_TICKS;
					} else {
						TOTAL_TICKS = CLEAN_TICKS + MIN_LENGTH_TICKS + FINAL_COMMIT_TICKS;
					}
					
					monitor.beginTask("Line Cleaning: ", TOTAL_TICKS);
					if (layers.size() > 1) {
						monitor.subTask("Merging datasets");
					} else {
						monitor.subTask("Copying dataset to new location");
					}
					FeatureStore mergedFeatureStore = mergeLayers(outputFileNoExt);
					
					layer.getMapInternal().getEditManagerInternal().commitTransaction();
					
					if (monitor.isCanceled()) {
						return;
					}
					
					if (cleanse) {
						monitor.subTask("Data Preparation: ");
						PerformCleansingAction action = new PerformCleansingAction(layer, pauseMonitor, mergedFeatureStore, 
								minimumLength, minimumCycleLength, nodeDistanceTolerance, areaTolerance, douglasPeuckerTolerance);
						action.run(new SubProgressMonitor(monitor, CLEANSE_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
					}
					
					if (monitor.isCanceled()) {
						return;
					}
					
					if (clean) {
						monitor.subTask("Light Conflation: ");
						List<String> priorityOrder = new ArrayList<String>();
						for (FeatureStore store : featureStores) {
							priorityOrder.add(store.getSchema().getTypeName());
						}
						
						System.gc();
                        MinimumLengthProcessor mlp = new MinimumLengthProcessor(layer.getMapInternal(), mergedFeatureStore, 0);
                        mlp.setName("ZeroLength2");
                        mlp.run(new SubProgressMonitor(monitor, MIN_LENGTH_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), pauseMonitor);
                        mlp = null;
                        
                        System.gc();
						SimilarLinesProcessor cleaner = new SimilarLinesProcessor(layer.getMapInternal(), mergedFeatureStore, 
								samplingDistance, verySimilarTolerance, similarTolerance);
                        cleaner.setName("LineCleaner");
						cleaner.run(new SubProgressMonitor(monitor, CLEAN_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), pauseMonitor);
					}
					
					layer.getMapInternal().getEditManagerInternal().commitTransaction();
					
					if (monitor.isCanceled()) {
						return;
					}
					
					if (cleanse) {
						monitor.subTask("Final Cleaning: ");
						System.gc();
				        MinimumLengthProcessor mlp = new MinimumLengthProcessor(layer.getMapInternal(), mergedFeatureStore, minimumLength);
				        mlp.setName("MinLength2");
				        mlp.run(new SubProgressMonitor(monitor, MIN_LENGTH_TICKS2, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), pauseMonitor);
					}
					
					monitor.subTask("Saving");
					layer.getMapInternal().getEditManagerInternal().commitTransaction();
					monitor.worked(FINAL_COMMIT_TICKS);

					monitor.done();
					
				} catch( Throwable erp ) {
                    throw new InvocationTargetException( erp );
                }
			}
		};
        
        try {
            getContainer().run( true, true, process );
            long end=System.currentTimeMillis();
            System.out.println("INFO: Time for operation="+(((double)end-start)/60000.0)+" min");
			layer.setVisible(true);
        } catch (InvocationTargetException e) {
        	((WizardPage) this.getContainer().getCurrentPage()).setErrorMessage( 
        			e.getTargetException().getLocalizedMessage() );
        	e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            this.getContainer().getCurrentPage().canFlipToNextPage(); // reset message
            e.printStackTrace();
            return false;
        }
        return true;
	}
	
	private List<FeatureStore> getFeatureStores() {
		List<FeatureStore> featureStores = new ArrayList<FeatureStore>();
		String[] selectedLayers = dataSelectionPage.layersList.getItems();
		
		for (String layerName : selectedLayers) {
			for (ILayer layer : dataSelectionPage.layers) {
				if (layer.getName().equals(layerName)) {
					try {
						FeatureStore store = layer.getResource(FeatureStore.class, null);
						featureStores.add(store);
					} catch (IOException e) {
						System.err.println("Cannot obtain FeatureStore from layer '"+layerName+"'");
						e.printStackTrace();
					}
				}
			}
		}
		return featureStores;
	}

    private void deleteFileIfExists(File file) throws IOException {
        if (file.exists()) { 
            file.delete();
        }
        if (file.exists()) {
            throw new IOException("Could not delete file " + file.getName());
        }
    }
    
	private FeatureStore mergeLayers(String outputFileNoExt) throws IOException {
		List<FeatureStore> storesToMerge = featureStores;

		
		
		String shpFileName =  outputFileNoExt + ".shp";
		
		File shpFile = new File(shpFileName);

        // TODO:  maybe we should use a dialog to confirm overwriting a
        //        file?  as it is, we only display a warning at the top
        //        of the wizard.
		deleteFileIfExists(shpFile);
		deleteFileIfExists(new File(outputFileNoExt + ".shx"));
		deleteFileIfExists(new File(outputFileNoExt + ".dbf"));
		deleteFileIfExists(new File(outputFileNoExt + ".fix"));
		deleteFileIfExists(new File(outputFileNoExt + ".qix"));
                
		try {
			if ((shpFile.exists() && !shpFile.canWrite())
					|| !shpFile.createNewFile()) {
				throw new IOException("Cannot write to file " + shpFileName);
			}
		} catch (IOException e) {
			throw (IOException) new IOException("Cannot write to file " + shpFileName).initCause(e);
		}
		
		URL shpFileURL;
		try {
			shpFileURL = shpFile.toURL();
		} catch (MalformedURLException e) {
			throw (IOException) new IOException().initCause(e);
		}
				
		if (storesToMerge.size() == 1) {
			FeatureStore store = storesToMerge.iterator().next();
			FeatureCollection fc = store.getFeatures();
			
			IndexedShapefileDataStoreFactory factory = new IndexedShapefileDataStoreFactory();
            ShapefileDataStore ds = (ShapefileDataStore)factory.createDataStore(shpFileURL);
            ds.createSchema(fc.getSchema());
            ((FeatureStore) ds.getFeatureSource()).addFeatures(fc.reader());
            

            
		} else {
		    try {
                FeatureUtil.mergeFeatureStores(storesToMerge, shpFileURL);
            } catch (Exception e) {
                // TODO Handle Exception
                throw (RuntimeException) new RuntimeException( ).initCause( e );
            }
		}
		
		/*
		 * Turn the newly created Shapefile into a uDig resource
		 */
		
		List<IService> services = CatalogPlugin.getDefault().getServiceFactory().acquire(shpFileURL);
		
		IService service = services.get(0);
		CatalogPlugin.getDefault().getLocalCatalog().add(service);
		List resources = service.members(null);
		
		resource = (IGeoResource) resources.get(0);
		
		Map map = ((Map)this.layers.get(0).getMap());
        LayerFactory factory = map.getLayerFactory();
		layer = factory.createLayer(resource);
		layer.setVisible(false);
        map.getLayersInternal().add(layer);
		FeatureStore udigStore = layer.getResource(FeatureStore.class, null);
		return udigStore;
	}

}
