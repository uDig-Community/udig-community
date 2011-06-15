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
package es.axios.udig.spatialoperations.internal.ui.parameters.spatialjoingeom;

import org.eclipse.swt.graphics.Image;

import es.axios.udig.spatialoperations.internal.ui.processconnectors.SpatialJoinGeomCommand;
import es.axios.udig.spatialoperations.tasks.SpatialRelation;
import es.axios.udig.spatialoperations.ui.parameters.AbstractImagesOperation;
import es.axios.udig.spatialoperations.ui.parameters.LayersCheckState;

/**
 * 
 * A class that stores the different spatial join geometry images and show the
 * correspondent image.
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
final class SpatialJoinGeomImages extends AbstractImagesOperation {

	private static final String		CONTAINS_SOURCE			= "ContainsSource";		//$NON-NLS-1$
	private static final String		CONTAINS_RESULT			= "ContainsResult";		//$NON-NLS-1$
	private static final String		CONTAINS_RESULT_SELECT	= "ContainsResultSelect";	//$NON-NLS-1$
	private static final String		CONTAINS_FINAL			= "ContainsFinal";			//$NON-NLS-1$
	private static final String		CONTAINS_FINAL_SELECT	= "ContainsFinalSelect";	//$NON-NLS-1$
	private static final String		CROSSES_SOURCE			= "CrossesSource";			//$NON-NLS-1$
	private static final String		CROSSES_RESULT			= "CrossesResult";			//$NON-NLS-1$
	private static final String		CROSSES_RESULT_SELECT	= "CrossesResultSelect";	//$NON-NLS-1$
	private static final String		CROSSES_FINAL			= "CrossesFinal";			//$NON-NLS-1$
	private static final String		CROSSES_FINAL_SELECT	= "CrossesFinalSelect";	//$NON-NLS-1$
	private static final String		DISJOINT_SOURCE			= "DisjointSource";		//$NON-NLS-1$
	private static final String		DISJOINT_RESULT			= "DisjointResult";		//$NON-NLS-1$
	private static final String		DISJOINT_RESULT_SELECT	= "DisjointResultSelect";	//$NON-NLS-1$
	private static final String		DISJOINT_FINAL			= "DisjointFinal";			//$NON-NLS-1$
	private static final String		DISJOINT_FINAL_SELECT	= "DisjointFinalSelect";	//$NON-NLS-1$
	private static final String		EQUALS_SOURCE			= "EqualsSource";			//$NON-NLS-1$
	private static final String		EQUALS_RESULT			= "EqualsResult";			//$NON-NLS-1$
	private static final String		EQUALS_RESULT_SELECT	= "EqualsResultSelect";	//$NON-NLS-1$
	private static final String		EQUALS_FINAL			= "EqualsFinal";			//$NON-NLS-1$
	private static final String		EQUALS_FINAL_SELECT		= "EqualsFinalSelect";		//$NON-NLS-1$
	private static final String		INTERSECT_SOURCE		= "IntersectSource";		//$NON-NLS-1$
	private static final String		INTERSECT_RESULT		= "IntersectResult";		//$NON-NLS-1$
	private static final String		INTERSECT_RESULT_SELECT	= "IntersectResultSelect";	//$NON-NLS-1$
	private static final String		INTERSECT_FINAL			= "IntersectFinal";		//$NON-NLS-1$
	private static final String		INTERSECT_FINAL_SELECT	= "IntersectFinalSelect";	//$NON-NLS-1$
	private static final String		TOUCHES_SOURCE			= "TouchesSource";			//$NON-NLS-1$
	private static final String		TOUCHES_RESULT			= "TouchesResult";			//$NON-NLS-1$
	private static final String		TOUCHES_RESULT_SELECT	= "TouchesResultSelect";	//$NON-NLS-1$
	private static final String		TOUCHES_FINAL			= "TouchesFinal";			//$NON-NLS-1$
	private static final String		TOUCHES_FINAL_SELECT	= "TouchesFinalSelect";	//$NON-NLS-1$
	private static final String		WITHIN_SOURCE			= "WithinSource";			//$NON-NLS-1$
	private static final String		WITHIN_RESULT			= "WithinResult";			//$NON-NLS-1$
	private static final String		WITHIN_RESULT_SELECT	= "WithinResultSelect";	//$NON-NLS-1$
	private static final String		WITHIN_FINAL			= "WithinFinal";			//$NON-NLS-1$
	private static final String		WITHIN_FINAL_SELECT		= "WithinFinalSelect";		//$NON-NLS-1$

	private SpatialRelation			relation				= null;

	private Boolean					selection				= null;

	private Image					currentImage			= null;

	private LayersCheckState		currentState			= null;

	/**
	 * 
	 */
	public SpatialJoinGeomImages() {
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

		this.relation = ((SpatialJoinGeomCommand)getCommand()).getRelation();
		this.selection = ((SpatialJoinGeomCommand)getCommand()).getSelection();

		// if the user doesn't selected yet a relation, don't show any image.
		if (this.relation == null) {
			return null;
		}

		switch (this.relation) {
		case Contains:

			this.currentImage = getContainsImage();
			break;
		case Crosses:

			this.currentImage = getCrossesImage();
			break;
		case Disjoint:

			this.currentImage = getDisjointImage();
			break;
		case Equals:

			this.currentImage = getEqualsImage();
			break;
		case Intersects:

			this.currentImage = getIntersectImage();
			break;
		case Touches:

			this.currentImage = getTouchesImage();
			break;
		case Within:

			this.currentImage = getWithinImage();
			break;
		default:

			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		assert currentImage != null : "currentImage couldn't be null"; //$NON-NLS-1$

		return currentImage;
	}

	/**
	 * 
	 * @return The correspondent image.
	 */
	private Image getContainsImage() {

		Image img = null;

		switch (this.currentState) {
		case Source:

			img = getImage(CONTAINS_SOURCE);
			break;
		case Result:

			if (this.selection) {
				img = getImage(CONTAINS_RESULT_SELECT);
			} else {
				img = getImage(CONTAINS_RESULT);
			}
			break;
		case Final:

			if (this.selection) {
				img = getImage(CONTAINS_FINAL_SELECT);
			} else {
				img = getImage(CONTAINS_FINAL);
			}
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		return img;
	}

	/**
	 * 
	 * @return The correspondent image.
	 */
	private Image getCrossesImage() {

		Image img = null;

		switch (this.currentState) {
		case Source:

			img = getImage(CROSSES_SOURCE);
			break;
		case Result:

			if (this.selection) {
				img = getImage(CROSSES_RESULT_SELECT);
			} else {
				img = getImage(CROSSES_RESULT);
			}
			break;
		case Final:

			if (this.selection) {
				img = getImage(CROSSES_FINAL_SELECT);
			} else {
				img = getImage(CROSSES_FINAL);
			}
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		return img;
	}

	/**
	 * 
	 * @return The correspondent image.
	 */
	private Image getDisjointImage() {

		Image img = null;

		switch (this.currentState) {
		case Source:

			img = getImage(DISJOINT_SOURCE);
			break;
		case Result:

			if (this.selection) {
				img = getImage(DISJOINT_RESULT_SELECT);
			} else {
				img = getImage(DISJOINT_RESULT);
			}
			break;
		case Final:

			if (this.selection) {
				img = getImage(DISJOINT_FINAL_SELECT);
			} else {
				img = getImage(DISJOINT_FINAL);
			}
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		return img;
	}

	/**
	 * 
	 * @return The correspondent image.
	 */
	private Image getEqualsImage() {

		Image img = null;

		switch (this.currentState) {
		case Source:

			img = getImage(EQUALS_SOURCE);
			break;
		case Result:

			if (this.selection) {
				img = getImage(EQUALS_RESULT_SELECT);
			} else {
				img = getImage(EQUALS_RESULT);
			}
			break;
		case Final:

			if (this.selection) {
				img = getImage(EQUALS_FINAL_SELECT);
			} else {
				img = getImage(EQUALS_FINAL);
			}
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		return img;
	}

	/**
	 * 
	 * @return The correspondent image.
	 */
	private Image getIntersectImage() {

		Image img = null;

		switch (this.currentState) {
		case Source:

			img = getImage(INTERSECT_SOURCE);
			break;
		case Result:

			if (this.selection) {
				img = getImage(INTERSECT_RESULT_SELECT);
			} else {
				img = getImage(INTERSECT_RESULT);
			}
			break;
		case Final:

			if (this.selection) {
				img = getImage(INTERSECT_FINAL_SELECT);
			} else {
				img = getImage(INTERSECT_FINAL);
			}
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		return img;
	}

	/**
	 * 
	 * @return The correspondent image.
	 */
	private Image getTouchesImage() {

		Image img = null;

		switch (this.currentState) {
		case Source:

			img = getImage(TOUCHES_SOURCE);
			break;
		case Result:

			if (this.selection) {
				img = getImage(TOUCHES_RESULT_SELECT);
			} else {
				img = getImage(TOUCHES_RESULT);
			}
			break;
		case Final:

			if (this.selection) {
				img = getImage(TOUCHES_FINAL_SELECT);
			} else {
				img = getImage(TOUCHES_FINAL);
			}
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		return img;
	}

	/**
	 * 
	 * @return The correspondent image.
	 */
	private Image getWithinImage() {

		Image img = null;

		switch (this.currentState) {
		case Source:

			img = getImage(WITHIN_SOURCE);
			break;
		case Result:

			if (this.selection) {
				img = getImage(WITHIN_RESULT_SELECT);
			} else {
				img = getImage(WITHIN_RESULT);
			}
			break;
		case Final:

			if (this.selection) {
				img = getImage(WITHIN_FINAL_SELECT);
			} else {
				img = getImage(WITHIN_FINAL);
			}
			break;
		default:
			assert false : "Error"; //$NON-NLS-1$
			break;
		}

		return img;
	}

	@Override
	public Image getDefaultImage() {

		this.currentImage = getImage(CONTAINS_FINAL);
		return currentImage;
	}

	/**
	 * Creates the imageregistry where saves all the images.
	 * 
	 * @return
	 */
	@Override
	protected void createImageRegistry() {

		addImageToRegistry(CONTAINS_SOURCE, SpatialJoinGeomImages.class);
		addImageToRegistry(CONTAINS_RESULT, SpatialJoinGeomImages.class);
		addImageToRegistry(CONTAINS_RESULT_SELECT, SpatialJoinGeomImages.class);
		addImageToRegistry(CONTAINS_FINAL, SpatialJoinGeomImages.class);
		addImageToRegistry(CONTAINS_FINAL_SELECT, SpatialJoinGeomImages.class);

		addImageToRegistry(CROSSES_SOURCE, SpatialJoinGeomImages.class);
		addImageToRegistry(CROSSES_RESULT, SpatialJoinGeomImages.class);
		addImageToRegistry(CROSSES_RESULT_SELECT, SpatialJoinGeomImages.class);
		addImageToRegistry(CROSSES_FINAL, SpatialJoinGeomImages.class);
		addImageToRegistry(CROSSES_FINAL_SELECT, SpatialJoinGeomImages.class);

		addImageToRegistry(DISJOINT_SOURCE, SpatialJoinGeomImages.class);
		addImageToRegistry(DISJOINT_RESULT, SpatialJoinGeomImages.class);
		addImageToRegistry(DISJOINT_RESULT_SELECT, SpatialJoinGeomImages.class);
		addImageToRegistry(DISJOINT_FINAL, SpatialJoinGeomImages.class);
		addImageToRegistry(DISJOINT_FINAL_SELECT, SpatialJoinGeomImages.class);

		addImageToRegistry(EQUALS_SOURCE, SpatialJoinGeomImages.class);
		addImageToRegistry(EQUALS_RESULT, SpatialJoinGeomImages.class);
		addImageToRegistry(EQUALS_RESULT_SELECT, SpatialJoinGeomImages.class);
		addImageToRegistry(EQUALS_FINAL, SpatialJoinGeomImages.class);
		addImageToRegistry(EQUALS_FINAL_SELECT, SpatialJoinGeomImages.class);

		addImageToRegistry(INTERSECT_SOURCE, SpatialJoinGeomImages.class);
		addImageToRegistry(INTERSECT_RESULT, SpatialJoinGeomImages.class);
		addImageToRegistry(INTERSECT_RESULT_SELECT, SpatialJoinGeomImages.class);
		addImageToRegistry(INTERSECT_FINAL, SpatialJoinGeomImages.class);
		addImageToRegistry(INTERSECT_FINAL_SELECT, SpatialJoinGeomImages.class);

		addImageToRegistry(TOUCHES_SOURCE, SpatialJoinGeomImages.class);
		addImageToRegistry(TOUCHES_RESULT, SpatialJoinGeomImages.class);
		addImageToRegistry(TOUCHES_RESULT_SELECT, SpatialJoinGeomImages.class);
		addImageToRegistry(TOUCHES_FINAL, SpatialJoinGeomImages.class);
		addImageToRegistry(TOUCHES_FINAL_SELECT, SpatialJoinGeomImages.class);

		addImageToRegistry(WITHIN_SOURCE, SpatialJoinGeomImages.class);
		addImageToRegistry(WITHIN_RESULT, SpatialJoinGeomImages.class);
		addImageToRegistry(WITHIN_RESULT_SELECT, SpatialJoinGeomImages.class);
		addImageToRegistry(WITHIN_FINAL, SpatialJoinGeomImages.class);
		addImageToRegistry(WITHIN_FINAL_SELECT, SpatialJoinGeomImages.class);
	}

}
