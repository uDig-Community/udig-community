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
package es.axios.udig.spatialoperations.ui.parameters;

import java.util.LinkedList;

import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.swt.widgets.Composite;

import es.axios.udig.spatialoperations.ui.view.ISOPresenter;

/**
 * <pre>
 * Aggregated presenter
 * <p>
 * This class is responsible to do the broadcast of messages between the
 * components of aggregated presenter.
 * 
 * Every setProperty for a paramsPresenter that is called outside a
 * paramsPresenter must be defined here:
 * 
 * -Usage sample:
 * 
 * For example, from from SOComposite is created a new command and set to one paramsPresenter:
 * 
 *  BufferCommand cmd = new BufferCommand();
 *  bufferParamsPresenter.setCommand(cmd);
 *  
 *  The method setCommand must be defined here with that structure:
 *  
 *  	public void setCommand(ISOCommand command) {
 * 
 *  		super.setCommand(command);
 * 
 *  		for (ISOParamsPresenter presenter : this.components) {
 * 
 *  			presenter.setCommand(command);
 *  		}
 *  	}
 *  
 * 	By this way, we are setting the bufferCommand for every
 *  paramsPresenter that the bufferParamsPresenter contains, because bufferParamsPresenter will contains
 *  more than one paramsPresenter but from SOComposite bufferCommand only is set one time.
 *  
 * 
 * </p>
 * </pre>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
public abstract class AggregatedPresenter extends AbstractParamsPresenter implements ISOAggregatedPresenter {

	private LinkedList<ISOParamsPresenter>	components	= new LinkedList<ISOParamsPresenter>();

	/**
	 * @param parent
	 * @param style
	 */
	public AggregatedPresenter(Composite parent, int style) {

		super(parent, style);

	}

	public void addPresenter(final ISOParamsPresenter soParamPresenter) {

		assert soParamPresenter != null;

		components.add(soParamPresenter);

		soParamPresenter.setParentPresenter(this);
	}

	/**
	 * Set a new map as current for this presenter and its components
	 */
	@Override
	public void setContext(final IToolContext context) {

		super.setContext(context);

		for (ISOParamsPresenter presenter : this.components) {

			presenter.setContext(context);
		}
	}

	@Override
	public void clear() {

		super.clear();

		for (ISOParamsPresenter presenter : this.components) {

			presenter.clear();
		}

	}

	@Override
	public void visible(boolean present) {

		displayPresenter(present);

		for (ISOParamsPresenter presenter : this.components) {

			presenter.visible(present);
		}
	}

	@Override
	public void open() {

		super.open();

		for (ISOParamsPresenter presenter : this.components) {

			presenter.open();
		}
	}

	@Override
	public void setEnabled(boolean b) {

		super.setEnabled(b);

		for (ISOParamsPresenter presenter : this.components) {

			presenter.setEnabled(b);
		}
	}

//	@Override
//	public void setCommand(ISOCommand command) {
//
//		super.setCommand(command);
//
//		for (ISOParamsPresenter presenter : this.components) {
//
//			presenter.setCommand(command);
//		}
//	}

	@Override
	public void setSpatialOperationPresenter(ISOPresenter soPresenter) {

		super.setSpatialOperationPresenter(soPresenter);

		for (ISOParamsPresenter presenter : this.components) {

			presenter.setSpatialOperationPresenter(soPresenter);
		}
	}

}
