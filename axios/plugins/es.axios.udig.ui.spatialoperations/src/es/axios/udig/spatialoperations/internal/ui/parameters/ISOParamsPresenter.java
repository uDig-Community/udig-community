/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.internal.ui.parameters;

import net.refractions.udig.project.ui.tool.IToolContext;
import es.axios.udig.spatialoperations.internal.control.ISOController;
/**
 * Interface for spatial operation's parameters.
 * <p>
 * The implementation class of this interface are responsible to 
 * provide the presentation for spatial operation's parameters.
 * </p>
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */

public interface ISOParamsPresenter {
    
    
    /**
     * Sets the context
     * @param context
     */    
    void setContext(final  IToolContext context);    
    
    /**
     * @return true if the presenter is ready to work, false in other case
     */
    boolean wasInitialized();
    

    /**
     * Opens the presenter populating its widgets. 
     */
    void open();

    /**
     * closes the presentation
     */
    void close();

    /**
     * Sets the controller for this presenter
     *
     * @param controller
     */
    void setController( final ISOController controller );
    
    /**
     * @return reference to controller associated
     */
    ISOController getController();

    /**
     * Clears inputs values
     *
     */
    void clear();

    /**
     * Shows or hides  parameters's widgets  
     *
     * @param present 
     */
    void visible( boolean present );
    

    
    /**
     * @return the operation name (for human interface)
     */
    String getOperationName();



    /**
     *
     * @return true if this presenter is the top container for parameters
     */
    boolean isTop();

    /**
     *
     * @return the top container
     */
    ISOParamsPresenter getTopPresenter();

    /**
     *
     * @param presenter
     */
    void setParentPresenter( ISOParamsPresenter presenter );

    /**
     * Enable / Diseanble this presenter
     * 
     * @param b
     */
    void setEnabled( boolean b );

    /**
     *
     * @return the tool tip text
     */
    String getToolTipText();

    


    
}