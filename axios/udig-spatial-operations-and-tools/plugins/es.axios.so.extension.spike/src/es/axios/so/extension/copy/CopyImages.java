package es.axios.so.extension.copy;

import org.eclipse.swt.graphics.Image;

import es.axios.udig.spatialoperations.ui.parameters.AbstractImagesOperation;
import es.axios.udig.spatialoperations.ui.parameters.LayersCheckState;

public class CopyImages extends AbstractImagesOperation {

	private static final String	INTERSECT_SOURCE	= "IntersectSource";	//$NON-NLS-1$
	private static final String	INTERSECT_RESULT	= "IntersectResult";	//$NON-NLS-1$
	private static final String	INTERSECT_FINAL		= "IntersectFinal";	//$NON-NLS-1$

	private Image				currentImage	= null;

	private LayersCheckState	currentState	= null;


	@Override
	public Image getDefaultImage() {

		return getImage(INTERSECT_FINAL);
	}

	@Override
	public Image getImage(Boolean chkSource, Boolean chkResult) {

		Image img = null;
		if (chkSource) {
	
			img = getImage(INTERSECT_SOURCE);
			
		} else if( chkResult) {

			img = getImage(INTERSECT_RESULT);

		} else {
		
			img = getImage(INTERSECT_FINAL);
		}
		return img;
	}

	@Override
	protected void createImageRegistry() {

		addImageToRegistry(INTERSECT_SOURCE, CopyImages.class);
		addImageToRegistry(INTERSECT_RESULT, CopyImages.class);
		addImageToRegistry(INTERSECT_FINAL, CopyImages.class);
	}
}
