/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
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
package es.axios.udig.ui.editingtools.precisionsegmentcopy.view;

import net.refractions.udig.project.ui.IUDIGView;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.opengis.feature.simple.SimpleFeature;

import es.axios.udig.ui.editingtools.precisionsegmentcopy.PrecisionSegmentCopyTool;
import es.axios.udig.ui.editingtools.precisionsegmentcopy.internal.SegmentCopyContext;

/**
 * The view of the {@link PrecisionSegmentCopyTool}.
 * 
 * While the user is working with the tool, its data, like reference line,
 * initial coordinate,etc... is showed on this view. Purpose of this view, is to
 * show the data, and also, if the user changes the data showed here, those
 * changes will be reflected on the map.
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class SegmentCopyParametersView extends ViewPart implements IUDIGView {

	private SegmentCopyContext				segmentCopyContext	= null;
	private SegmentCopyParametersComposite	parametersComposite	= null;
	private IToolContext					context				= null;
	public static final String				id					= "es.axios.udig.ui.editingtools.precisionsegmentcopy.view.SegmentCopyParametersView";	//$NON-NLS-1$

	/**
	 * Set the parallel context after creating it. Also set parameters view
	 * context that will set the context and add itself as observer.
	 * 
	 * @param context
	 */
	public void setSegmentCopyContext(SegmentCopyContext context) {

		assert context != null;

		this.segmentCopyContext = context;
		this.parametersComposite.setSegmentCopyContext(this.segmentCopyContext);
	}

	/**
	 * Get the parameters view which is the observer.
	 * 
	 * @return
	 */
	public SegmentCopyParametersComposite getParamsView() {
		return this.parametersComposite;
	}

	@Override
	public void createPartControl(Composite parent) {

		parametersComposite = new SegmentCopyParametersComposite(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {

	}

	public void editFeatureChanged(SimpleFeature feature) {

	}

	public IToolContext getContext() {

		return this.context;
	}

	public void setContext(IToolContext newContext) {

		this.context = newContext;
		this.parametersComposite.setContext(newContext);
	}

}
