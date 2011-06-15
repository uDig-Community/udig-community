/*
 * uDig Spatial Operations - Tutorial - http://www.axios.es (C) 2009,
 * Axios Engineering S.L. This product is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This product is distributed as part of tutorial, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */package es.axios.udig.spatialoperations.tutorial;

import org.eclipse.swt.widgets.Composite;

import es.axios.udig.spatialoperations.tutorial.ui.centroid.CentroidCommand;
import es.axios.udig.spatialoperations.tutorial.ui.centroid.CentroidUIFactory;
import es.axios.udig.spatialoperations.ui.parameters.AbstractSpatialOperationBuilder;
import es.axios.udig.spatialoperations.ui.parameters.ISOBuilder;
/**
 * 
 * Build the Spatial Operation for this tutorial
 *
 * @author Mauricio Pazos (www.axios.es)
 *
 */
public class SOBuilder extends AbstractSpatialOperationBuilder implements
		ISOBuilder {

	@Override
	public void build(Composite parentComposite) {
	
		addCollaboration(
				parentComposite, new CentroidUIFactory(),
				new CentroidCommand() );
	}

}
