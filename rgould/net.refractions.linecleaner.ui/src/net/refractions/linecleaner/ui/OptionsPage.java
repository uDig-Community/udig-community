package net.refractions.linecleaner.ui;

import net.refractions.linecleaner.cleansing.CyclesProcessor;
import net.refractions.linecleaner.cleansing.DouglasPeuckerProcessor;
import net.refractions.linecleaner.cleansing.EndNodesProcessor;
import net.refractions.linecleaner.cleansing.MinimumLengthProcessor;
import net.refractions.linecleaner.cleansing.SimilarLinesProcessor;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class OptionsPage extends WizardPage {

	private static final String NAME = "Options";
	private static final String TITLE = "Configure options for the operations.";
	private static final ImageDescriptor IMAGE = null;
	private Text samplingDistance;
	private Text verySimilarTolerance;
	private Text similarTolerance;
	private Text minimumLength;
	private Text distanceTolerance;
	private Text areaTolerance;
	private Text dpTolerance;
	private Text cyclesLength;
	
	private String samplingDistanceDefault;
	private String verySimilarToleranceDefault;
	private String similarToleranceDefault;
	private String minimumLengthDefault;
	private String distanceToleranceDefault;
	private String areaToleranceDefault;
	private String dpToleranceDefault;
	private String cyclesLengthDefault;
	
	private IDialogSettings savedSettings;
	
	protected OptionsPage(IDialogSettings savedSettings) {
		super(NAME, TITLE, IMAGE);
		this.savedSettings = savedSettings;
		loadSettings();
	}

	public void saveSettings() {
		savedSettings.put("areaTolerance", getAreaTolerance());
		savedSettings.put("cyclesLength", getCyclesLength());
		savedSettings.put("distanceTolerance", getDistanceTolerance());
		savedSettings.put("dpTolerance", getDPTolerance());
		savedSettings.put("minLength", getMinimumLength());
		savedSettings.put("samplingDistance", getSamplingDistance());
		savedSettings.put("similarTolerance", getSimilarTolerance());
		savedSettings.put("verySimilarTolerance", getVerySimilarTolerance());
	}
	
	public void loadSettings() {
		areaToleranceDefault = savedSettings.get("areaTolerance");
		cyclesLengthDefault = savedSettings.get("cyclesLength");
		distanceToleranceDefault = savedSettings.get("distanceTolerance");
		dpToleranceDefault = savedSettings.get("dpTolerance");
		minimumLengthDefault = savedSettings.get("minLength");
		samplingDistanceDefault = savedSettings.get("samplingDistance");
		similarToleranceDefault = savedSettings.get("similarTolerance");
		verySimilarToleranceDefault = savedSettings.get("verySimilarTolerance");
		
		if (areaToleranceDefault == null) 
			areaToleranceDefault = Double.toString(EndNodesProcessor.DEFAULT_AREA_TOLERANCE);
		if (cyclesLengthDefault == null)
			cyclesLengthDefault = Double.toString(CyclesProcessor.DEFAULT_LENGTH_TOLERANCE);
		if (distanceToleranceDefault == null)
			distanceToleranceDefault = Double.toString(EndNodesProcessor.DEFAULT_DISTANCE_TOLERANCE);
		if (dpToleranceDefault == null)
			dpToleranceDefault = Double.toString(DouglasPeuckerProcessor.DEFAULT_DISTANCE_TOLERANCE);
		if (minimumLengthDefault == null)
			minimumLengthDefault = Double.toString(MinimumLengthProcessor.DEFAULT_MINIMUM_LENGTH);
		if (samplingDistanceDefault == null)
			samplingDistanceDefault = Double.toString(SimilarLinesProcessor.DEFAULT_SAMPLING_DISTANCE);
		if (similarToleranceDefault == null)
			similarToleranceDefault = Double.toString(SimilarLinesProcessor.DEFAULT_SIMILAR_TOLERANCE);
		if (verySimilarToleranceDefault == null)
			verySimilarToleranceDefault = Double.toString(SimilarLinesProcessor.DEFAULT_VERY_SIMILAR_TOLERANCE);
	}
	
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        

        GridLayout mainGridLayout = new GridLayout();
        mainGridLayout.numColumns = 2;
        mainGridLayout.makeColumnsEqualWidth = true;
        composite.setLayout(mainGridLayout);
        
        GridLayout columnGridLayout = new GridLayout();
        columnGridLayout.numColumns = 1;
        

        
        Composite left = new Composite(composite, SWT.NULL);
        left.setLayoutData(getDefaultGridData());
        left.setLayout(columnGridLayout);
        
        Composite right = new Composite(composite, SWT.NULL);
        right.setLayoutData(getDefaultGridData());
        right.setLayout(columnGridLayout);
        
        
        Group minimumLength = createMinimumLengthGroup(left);
        Group cycles = createCyclesGroup(left);
        Group endNodes = createEndNodesGroup(left);

        Group douglasP = createDouglasPeuckerGroup(right);
        Group cleaning = createCleaningGroup(right);
        
        cycles.setLayoutData(getDefaultGridData());
        douglasP.setLayoutData(getDefaultGridData());
        endNodes.setLayoutData(getDefaultGridData());
        minimumLength.setLayoutData(getDefaultGridData());
        cleaning.setLayoutData(getDefaultGridData());
        
		setControl(composite);
	}
	
	
	private GridData getDefaultGridData() {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        return gridData;
	}
	
	private GridData getLabelGridData() {
		GridData labelData = new GridData();
        labelData.horizontalAlignment = SWT.FILL;
        labelData.verticalAlignment = SWT.BEGINNING;
        labelData.grabExcessHorizontalSpace = true;
        labelData.grabExcessVerticalSpace = true;
        
        return labelData;
	}
	
	private GridData getTextGridData() {
		GridData textData = new GridData();
        textData.horizontalAlignment = SWT.FILL;
        textData.verticalAlignment = SWT.BEGINNING;
        textData.grabExcessHorizontalSpace = true;
        textData.grabExcessVerticalSpace = false;
        
        return textData;
	}

	private Group createCleaningGroup(Composite parent) {
		Group group = new Group(parent, SWT.NULL);		
		GridLayout gridLayout = new GridLayout(2, false);
		
		GridData labelData = getLabelGridData();
		GridData textData = getTextGridData();
		
		group.setLayout(gridLayout);
        group.setText("Light Conflation");
        
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(labelData);
        label.setText("Sampling distance: ");
        label.setToolTipText("The default is " + SimilarLinesProcessor.DEFAULT_SAMPLING_DISTANCE);
        
        samplingDistance = new Text(group, SWT.BORDER);
        samplingDistance.setLayoutData(textData);
        samplingDistance.setText(samplingDistanceDefault);
        samplingDistance.addKeyListener(updateButtonsKeyListener);
        
        label = new Label(group, SWT.NONE);
        label.setText("Very similar tolerance: ");
        label.setLayoutData(labelData);
        label.setToolTipText("The default is " + SimilarLinesProcessor.DEFAULT_VERY_SIMILAR_TOLERANCE);
        
        verySimilarTolerance = new Text(group, SWT.BORDER);
        verySimilarTolerance.setLayoutData(textData);
        verySimilarTolerance.setText(verySimilarToleranceDefault);
        verySimilarTolerance.addKeyListener(updateButtonsKeyListener);
        
        label = new Label(group, SWT.NONE);
        label.setText("Similar tolerance: ");
        label.setLayoutData(labelData);
        label.setToolTipText("The default is " + SimilarLinesProcessor.DEFAULT_SIMILAR_TOLERANCE);
        
        similarTolerance = new Text(group, SWT.BORDER);
        similarTolerance.setLayoutData(textData);
        similarTolerance.setText(similarToleranceDefault);
        similarTolerance.addKeyListener(updateButtonsKeyListener);
        
        return group;
	}

	private Group createMinimumLengthGroup(Composite parent) {
		Group group = new Group(parent, SWT.NULL);		
		GridLayout gridLayout = new GridLayout(2, false);
		group.setLayout(gridLayout);
        group.setText("Minimum Length");
        
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(getLabelGridData());
        label.setText("Minimum length: ");
        label.setToolTipText("For every line geometry in the dataset, its length is calculated. If it is equal to or less" +
        		" than the minimum length, it is deleted. The default value is " + MinimumLengthProcessor.DEFAULT_MINIMUM_LENGTH);
        
        minimumLength = new Text(group, SWT.BORDER);
        minimumLength.setLayoutData(getTextGridData());
        minimumLength.setText(minimumLengthDefault);
        minimumLength.addKeyListener(updateButtonsKeyListener);
        
        return group;
	}

	private Group createEndNodesGroup(Composite parent) {
		Group group = new Group(parent, SWT.NULL);		
		GridLayout gridLayout = new GridLayout(2, false);
		group.setLayout(gridLayout);
        group.setText("End Nodes");
        
        Label label = new Label(group, SWT.NONE);
        label.setText("Distance tolerance: ");
        label.setLayoutData(getLabelGridData());
        label.setToolTipText("If the distance between the any of the end-points of any two geometries " +
        		"is less than the distance tolerance, the two end-points will be snapped (moved) to the " +
        		"same point. The default value is " + EndNodesProcessor.DEFAULT_DISTANCE_TOLERANCE);
        
        distanceTolerance = new Text(group, SWT.BORDER);
        distanceTolerance.setLayoutData(getTextGridData());
        distanceTolerance.setText(distanceToleranceDefault);
        distanceTolerance.addKeyListener(updateButtonsKeyListener);
        
        label = new Label(group, SWT.NONE);
        label.setText("Area tolerance: ");
        label.setLayoutData(getLabelGridData());
        label.setToolTipText("Before a group of nodes are to be snapped together, their total area is taken" +
        		" into consideration. If this value is equal to or greater than area tolerance, the nodes are" +
        		" flagged, a warning is logged, and nothing further is done. The default value is " + EndNodesProcessor.DEFAULT_AREA_TOLERANCE);
        
        areaTolerance = new Text(group, SWT.BORDER);
        areaTolerance.setLayoutData(getTextGridData());
        areaTolerance.setText(areaToleranceDefault);
        areaTolerance.addKeyListener(updateButtonsKeyListener);
        
        return group;
	}

	private Group createDouglasPeuckerGroup(Composite parent) {
		Group group = new Group(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout(2, false);
		group.setLayout(gridLayout);
        group.setText("Douglas-Peucker");
        
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(getLabelGridData());
        label.setText("Distance tolerance: ");
        label.setToolTipText("For Douglas-Peucker, a line is generated between the start and end-points" +
        		" of a line, and vertices that are farther away than the distance tolerance are removed. " +
        		"The default value is " + DouglasPeuckerProcessor.DEFAULT_DISTANCE_TOLERANCE);
        
        dpTolerance = new Text(group, SWT.BORDER);
        dpTolerance.setLayoutData(getTextGridData());
        dpTolerance.setText(dpToleranceDefault);
        dpTolerance.addKeyListener(updateButtonsKeyListener);
        
		return group;
	}

	private Group createCyclesGroup(Composite parent) {
		Group group = new Group(parent, SWT.NULL);		
		GridLayout gridLayout = new GridLayout(2, false);
		group.setLayout(gridLayout);
        group.setText("Cycles");
        
        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(getLabelGridData());
        label.setText("Total length tolerance: " );
        label.setToolTipText("For the cycles processor, if a feature is a cycle, it will be deleted, unless" +
        		" it has a total length equal to or greater than the total length tolerance. " +
        		"If the total length tolerance is <= 0, then it is disabled. The default value is " +
        		CyclesProcessor.DEFAULT_LENGTH_TOLERANCE);
        
        cyclesLength = new Text(group, SWT.BORDER);
        cyclesLength.setLayoutData(getTextGridData());
        cyclesLength.setText(cyclesLengthDefault);
        cyclesLength.addKeyListener(updateButtonsKeyListener);
        return group;
	}
	
	private void updateButtons() {
		getWizard().getContainer().updateButtons();
	}

	@Override
	public boolean isPageComplete() {

		
		setErrorMessage(null);
		return true;
	}
	
	private Double getValue(Text text) {
		try {
			Double result = Double.parseDouble(text.getText());
			return result;
		} catch (NumberFormatException e) {
			text.setFocus();
			text.selectAll();
			setErrorMessage("Values must be numerical.");
			updateButtons();
			return null;
		}
	}
	
	public double getAreaTolerance() {
		Double value = getValue(areaTolerance);
		return value;
	}
	
	public double getCyclesLength() {
		return getValue(cyclesLength);
	}
	
	public double getDistanceTolerance() {
		return getValue(distanceTolerance);
	}
	
	public double getDPTolerance() {
		return getValue(dpTolerance);
	}
	
	public double getMinimumLength() {
		return getValue(minimumLength);
	}
	
	public double getSamplingDistance() {
		return getValue(samplingDistance);
	}
	
	public double getSimilarTolerance() {
		return getValue(similarTolerance);
	}
	
	public double getVerySimilarTolerance() {
		return getValue(verySimilarTolerance);
	}

	private UpdateKeyListener updateButtonsKeyListener = new UpdateKeyListener();
	
	private class UpdateKeyListener implements KeyListener {

		public void keyPressed(KeyEvent e) {
			updateButtons();
		}

		public void keyReleased(KeyEvent e) {
			updateButtons();
		}

	}
	

}
