package net.refractions.linecleaner.cleansing;

import net.refractions.udig.project.internal.Layer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geotools.data.FeatureStore;

public class PerformCleansingAction {

    private FeatureStore featureStore;
    private double nodeDistanceTolerance;
	private double areaTolerance;
	private double douglasPeuckerTolerance;
	private double minimumLength;
    private double minimumCycleLength;
	private Layer layer;
	private PauseMonitor pauseMonitor;
	
	// constants representing relative time taken by each processor.  total = 200
    public static final int MIN_LENGTH_TICKS = 1;
    public static final int PSEUDO_NODES_TICKS = 38;
    public static final int DOUGLAS_PEUCKER_TICKS = 10;
    public static final int CYCLES_TICKS = 2;
    public static final int NODE_INSERTION_TICKS = 54;
    public static final int END_NODES_TICKS = 64;
    public static final int PSEUDO_NODES_2_TICKS = 30;
    public static final int CYCLES_2_TICKS = 2;
    
    public static final int TOTAL_TICKS = MIN_LENGTH_TICKS + PSEUDO_NODES_TICKS + DOUGLAS_PEUCKER_TICKS + CYCLES_TICKS
    + NODE_INSERTION_TICKS + END_NODES_TICKS + PSEUDO_NODES_2_TICKS + CYCLES_2_TICKS;
	
    public PerformCleansingAction (Layer layer, PauseMonitor pauseMonitor, FeatureStore featureStore, double minimumLength, 
    		double minimumCycleLength, double nodeDistanceTolerance, double areaTolerance, double douglasPeuckerTolerance) {
    	this.layer = layer;
    	this.pauseMonitor = pauseMonitor;
        this.featureStore = featureStore;
        this.nodeDistanceTolerance = nodeDistanceTolerance;
        this.minimumLength = minimumLength;
        this.minimumCycleLength = minimumCycleLength;
        this.areaTolerance = areaTolerance;
        this.douglasPeuckerTolerance = douglasPeuckerTolerance;
    }
    
    public void run(IProgressMonitor monitor) throws Exception {        
    	monitor.beginTask("Data Preparation: ", TOTAL_TICKS);
    	if (monitor.isCanceled()) {
    		return;
    	}

        MinimumLengthProcessor mlp = new MinimumLengthProcessor(layer.getMapInternal(), featureStore, minimumLength);
        mlp.setName("ZeroLength");
        mlp.run(new SubProgressMonitor(monitor, MIN_LENGTH_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
        		pauseMonitor);
        mlp = null;
        
        if (monitor.isCanceled()) {
        	return;
        }
        
    	PseudoNodeProcessor pnp = new PseudoNodeProcessor(layer.getMapInternal(), featureStore, nodeDistanceTolerance);
    	pnp.setName("PseudoNodes");
    	pnp.run(new SubProgressMonitor(monitor, PSEUDO_NODES_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
    			pauseMonitor);
    	pnp = null;
    	
		layer.getMapInternal().getEditManagerInternal().commitTransaction();
		
    	if (monitor.isCanceled()) {
    		return;
    	}
    	
    	System.gc();
    	DouglasPeuckerProcessor dpp = new DouglasPeuckerProcessor(layer.getMapInternal(), featureStore, this.douglasPeuckerTolerance);
    	dpp.setName("DouglasPeucker");
    	dpp.run(new SubProgressMonitor(monitor, DOUGLAS_PEUCKER_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), pauseMonitor);
    	dpp = null;
    	
		layer.getMapInternal().getEditManagerInternal().commitTransaction();

    	if (monitor.isCanceled()) {
    		return;
    	}

    	System.gc();
        CyclesProcessor cycles1 = new CyclesProcessor(layer.getMapInternal(), featureStore, minimumCycleLength);
        cycles1.setName("Cycles1");
        cycles1.run(new SubProgressMonitor(monitor, CYCLES_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), pauseMonitor);
        cycles1 = null;
        

		layer.getMapInternal().getEditManagerInternal().commitTransaction();

    	if (monitor.isCanceled()) {
    		return;
    	}

    	System.gc();
    	NodeInsertionProcessor nipProcessor = new NodeInsertionProcessor(layer.getMapInternal(), featureStore, nodeDistanceTolerance);
    	nipProcessor.setName("NodeInsertion");
        nipProcessor.run(new SubProgressMonitor(monitor, NODE_INSERTION_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), pauseMonitor);
        nipProcessor = null;
        

		layer.getMapInternal().getEditManagerInternal().commitTransaction();
       
        if (monitor.isCanceled()) {
    		return;
    	}
        
        System.gc();
        EndNodesProcessor enProcessor = new EndNodesProcessor(layer.getMapInternal(),
        		featureStore, nodeDistanceTolerance, areaTolerance);
        enProcessor.setName("EndNodes");
        enProcessor.run(new SubProgressMonitor(monitor, END_NODES_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), pauseMonitor);
        enProcessor = null;
        

		layer.getMapInternal().getEditManagerInternal().commitTransaction();

    	if (monitor.isCanceled()) {
    		return;
    	}
    	
    	System.gc();
    	PseudoNodeProcessor pnp2 = new PseudoNodeProcessor(layer.getMapInternal(), featureStore);
    	pnp2.setName("PseudoNodes2");
    	pnp2.run(new SubProgressMonitor(monitor, PSEUDO_NODES_2_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), pauseMonitor);
    	pnp2 = null;
    	
		layer.getMapInternal().getEditManagerInternal().commitTransaction();
		
    	if (monitor.isCanceled()) {
    		return;
    	}
    	
    	System.gc();
        CyclesProcessor cycles2 = new CyclesProcessor(layer.getMapInternal(), featureStore, minimumCycleLength);
        cycles2.setName("Cycles2");
        cycles2.run(new SubProgressMonitor(monitor, CYCLES_2_TICKS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), pauseMonitor);
        cycles2 = null;
        

		layer.getMapInternal().getEditManagerInternal().commitTransaction();

        monitor.done();
    }
}
