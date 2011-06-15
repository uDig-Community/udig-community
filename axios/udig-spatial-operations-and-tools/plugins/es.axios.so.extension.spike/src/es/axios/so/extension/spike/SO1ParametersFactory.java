package es.axios.so.extension.spike;

import org.eclipse.swt.custom.ScrolledComposite;

import es.axios.udig.spatialoperations.ui.parameters.IImageOperation;
import es.axios.udig.spatialoperations.ui.parameters.ISOAggregatedPresenter;
import es.axios.udig.spatialoperations.ui.parameters.ISOParametersPresenterFactory;

public class SO1ParametersFactory implements ISOParametersPresenterFactory {

	public ISOAggregatedPresenter createDataComposite(ScrolledComposite dataParent, int style) {
		// TODO Auto-generated method stub
		return new SO1ParametersPresenter(dataParent, style);
	}

	public IImageOperation createDemoImages() {
		// TODO Auto-generated method stub
		return new SO1Images();
	}

}
