/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.internal.ui.parameters.buffer;

import org.eclipse.swt.graphics.Image;

import es.axios.udig.spatialoperations.internal.ui.processconnectors.BufferCommand;
import es.axios.udig.spatialoperations.tasks.IBufferTask.CapStyle;
import es.axios.udig.spatialoperations.ui.parameters.AbstractImagesOperation;
import es.axios.udig.spatialoperations.ui.parameters.ISOCommand;
import es.axios.udig.spatialoperations.ui.parameters.LayersCheckState;

/**
 * A class that stores the different buffer images and shows the correspondent
 * image.
 * 
 * TODO for each option add getters on the command and implement the logical
 * here
 * 
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @author Iratxe Lejarreta (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
final class BufferImages extends AbstractImagesOperation {

	private static final String	BUFFER_SOURCE					= "BufferSource";				//$NON-NLS-1$
	private static final String	BUFFER_RESULT_CAP_ROUND			= "BufferResultCapRound";		//$NON-NLS-1$
	private static final String	BUFFER_RESULT_CAP_FLAT			= "BufferResultCapFlat";		//$NON-NLS-1$
	private static final String	BUFFER_RESULT_CAP_SQUARE		= "BufferResultCapSquare";		//$NON-NLS-1$
	private static final String	BUFFER_RESULT_MERGE_CAP_ROUND	= "BufferResultMergeCapRound";	//$NON-NLS-1$
	private static final String	BUFFER_RESULT_MERGE_CAP_FLAT	= "BufferResultMergeCapFlat";	//$NON-NLS-1$
	private static final String	BUFFER_RESULT_MERGE_CAP_SQUARE	= "BufferResultMergeCapSquare"; //$NON-NLS-1$
	private static final String	BUFFER_FINAL_CAP_ROUND			= "BufferFinalCapRound";		//$NON-NLS-1$
	private static final String	BUFFER_FINAL_CAP_FLAT			= "BufferFinalCapFlat";		//$NON-NLS-1$
	private static final String	BUFFER_FINAL_CAP_SQUARE			= "BufferFinalCapSquare";		//$NON-NLS-1$
	private static final String	BUFFER_FINAL_MERGE_CAP_ROUND	= "BufferFinalMergeCapRound";	//$NON-NLS-1$
	private static final String	BUFFER_FINAL_MERGE_CAP_FLAT		= "BufferFinalMergeCapFlat";	//$NON-NLS-1$
	private static final String	BUFFER_FINAL_MERGE_CAP_SQUARE	= "BufferFinalMergeCapSquare";	//$NON-NLS-1$

	private Boolean				aggregate						= null;
	private Image				currentImage					= null;
	private LayersCheckState	currentState					= null;
	private CapStyle			capStyle						= null;

	public BufferImages() {
		super();
	}

	@Override
	public Image getImage(Boolean chkSource, Boolean chkResult) {

		this.currentState = (setCurrentLayerCheckState(chkSource, chkResult));

		if (this.currentState == null) {
			// no check selected = no image will display
			return null;
		}

		// TODO implement check for each option, and finally will return the
		// correspondent image.

		retrieveCapStyle();

		assert capStyle != null : "it should set its value"; //$NON-NLS-1$

		switch (this.currentState) {
		case Source:

			this.currentImage = getImage(BUFFER_SOURCE);
			break;
		case Result:

			this.currentImage = getResultImage();
			break;
		case Final:

			this.currentImage = getFinalImage();
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		assert currentImage != null : "currentImage couldn't be null"; //$NON-NLS-1$

		return currentImage;
	}

	/**
	 * Return the image when the final check is selected.
	 * 
	 * @return
	 */
	private Image getFinalImage() {
		switch (this.capStyle) {
		case capRound:

			if (checkAggregate()) {
				this.currentImage = getImage(BUFFER_FINAL_MERGE_CAP_ROUND);
			} else {
				this.currentImage = getImage(BUFFER_FINAL_CAP_ROUND);
			}
			break;
		case capFlat:

			if (checkAggregate()) {
				this.currentImage = getImage(BUFFER_FINAL_MERGE_CAP_FLAT);
			} else {
				this.currentImage = getImage(BUFFER_FINAL_CAP_FLAT);
			}
			break;
		case capSquare:

			if (checkAggregate()) {
				this.currentImage = getImage(BUFFER_FINAL_MERGE_CAP_SQUARE);
			} else {
				this.currentImage = getImage(BUFFER_FINAL_CAP_SQUARE);
			}
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}
		return currentImage;
	}

	/**
	 * Retrieve the image for the result.
	 * 
	 * @return The alone image of the buffer when the result check is selected.
	 */
	private Image getResultImage() {

		switch (this.capStyle) {
		case capRound:

			if (checkAggregate()) {
				this.currentImage = getImage(BUFFER_RESULT_MERGE_CAP_ROUND);
			} else {
				this.currentImage = getImage(BUFFER_RESULT_CAP_ROUND);
			}
			break;
		case capFlat:

			if (checkAggregate()) {
				this.currentImage = getImage(BUFFER_RESULT_MERGE_CAP_FLAT);
			} else {
				this.currentImage = getImage(BUFFER_RESULT_CAP_FLAT);
			}
			break;
		case capSquare:

			if (checkAggregate()) {
				this.currentImage = getImage(BUFFER_RESULT_MERGE_CAP_SQUARE);
			} else {
				this.currentImage = getImage(BUFFER_RESULT_CAP_SQUARE);
			}
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}
		return currentImage;
	}

	/**
	 * Checked if the aggregated is selected.
	 * 
	 * @return True if it's selected
	 */
	private boolean checkAggregate() {

		ISOCommand cmd = getCommand();

		this.aggregate = ((BufferCommand) cmd).getAggregate();

		if (this.aggregate == null) {
			return false;
		}

		return (this.aggregate) ? true : false;
	}

	/**
	 * Return the specific cap style.
	 */
	private void retrieveCapStyle() {

		ISOCommand cmd = getCommand();

		this.capStyle = ((BufferCommand) cmd).getCapStyle();
	}

	@Override
	public Image getDefaultImage() {

		this.currentImage = getImage(BUFFER_FINAL_CAP_ROUND);
		return currentImage;
	}

	/**
	 * Creates the image registry where saves all the images.
	 * 
	 * @return ImageRegistry
	 */
	@Override
	public void createImageRegistry() {

		addImageToRegistry(BUFFER_SOURCE, BufferImages.class);
		addImageToRegistry(BUFFER_RESULT_CAP_ROUND, BufferImages.class);
		addImageToRegistry(BUFFER_RESULT_CAP_FLAT, BufferImages.class);
		addImageToRegistry(BUFFER_RESULT_CAP_SQUARE, BufferImages.class);
		addImageToRegistry(BUFFER_RESULT_MERGE_CAP_ROUND, BufferImages.class);
		addImageToRegistry(BUFFER_RESULT_MERGE_CAP_FLAT, BufferImages.class);
		addImageToRegistry(BUFFER_RESULT_MERGE_CAP_SQUARE, BufferImages.class);
		addImageToRegistry(BUFFER_FINAL_CAP_ROUND, BufferImages.class);
		addImageToRegistry(BUFFER_FINAL_CAP_FLAT, BufferImages.class);
		addImageToRegistry(BUFFER_FINAL_CAP_SQUARE, BufferImages.class);
		addImageToRegistry(BUFFER_FINAL_MERGE_CAP_ROUND, BufferImages.class);
		addImageToRegistry(BUFFER_FINAL_MERGE_CAP_FLAT, BufferImages.class);
		addImageToRegistry(BUFFER_FINAL_MERGE_CAP_SQUARE, BufferImages.class);
	}

}
