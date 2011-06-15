package es.axios.so.extension.spike;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import es.axios.udig.spatialoperations.ui.parameters.AggregatedPresenter;

public final class SO1ParametersPresenter extends AggregatedPresenter {

	private CLabel basicComposite;

	public SO1ParametersPresenter(Composite parent, int style) {
		super(parent, style);

		super.initialize();
	}

	@Override
	protected void createContents() {
		// TODO Auto-generated method stub

		FillLayout gridLayout = new FillLayout();
		setLayout(gridLayout);
		
		this.basicComposite = new CLabel(this, SWT.NONE);
		basicComposite.setLayout(gridLayout);
	}

	@Override
	protected void populate() {
		// TODO Auto-generated method stub
		basicComposite.setText("hello");

	}
	
	public ImageDescriptor getImageDescriptor(){
		
		final String iconFile = "images/" + "spatial-operation.gif"; //$NON-NLS-1$ //$NON-NLS-2$

		ImageDescriptor descriptor = ImageDescriptor.createFromFile(
				SO1ParametersPresenter.class, iconFile);

		return descriptor;

	}
	
	

}
