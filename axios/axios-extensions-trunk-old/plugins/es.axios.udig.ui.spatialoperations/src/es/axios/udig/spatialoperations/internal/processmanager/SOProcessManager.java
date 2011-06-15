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
package es.axios.udig.spatialoperations.internal.processmanager;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.internal.parameters.IBufferParameters;
import es.axios.udig.spatialoperations.internal.parameters.IClipParameters;
import es.axios.udig.spatialoperations.internal.parameters.IIntersectParameters;
import es.axios.udig.ui.commons.util.DialogUtil;

/**
 * Facade for spatial operation processes.
 * <p>
 * This class implement the methods to execute the spatial operations.
 * 
 * </p>
 * TODO The process fail exception require to be improved. 
 * Now, if the precess fail the user is not informed.
 * Thinking that this processes are asynchSronous an special 
 * mechanism will be required like an ProcessMonitor with its view (or panel in process). Then the user could inspect the status (running, finished, progress ) of all process.  
 * 
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Gabriel Roldan (www.axios.es)
 * @since 1.1.0
 */
public final class SOProcessManager {
    
    
    /**
     * Runs the buffer process 
     * 
     * @param params implementation of IBufferParameters
     * @throws SOProcessException 
     */
    static public void bufferOperation( final IBufferParameters params ) throws SOProcessException{
        
        final ISOProcess process = new BufferProcess(params);

        run(Messages.SpatialOperationProcessManager_buffer_process, process );
    }
    
    
    /**
     * Runs the intersection proecess
     *
     * @param params implementation of IIntersectParameters
     * @throws SOProcessException 
     */
    static public void intersectOperation(final IIntersectParameters params) throws SOProcessException{

        final ISOProcess process = new IntersectProcess(params);

        run(Messages.SpatialOperationProcessManager_intersect_process, process);
    
    }

    
    /**
     * Clips the source layer using other layer
     *
     * @param params
     * @throws SOProcessException 
     */
    static public void clipOperation( final IClipParameters params) throws SOProcessException {
        
        final ISOProcess process = new ClipProcess(params);

        run(Messages.SpatialOperationProcessManager_clip_process, process);
    }


    /**
     * Executes the process
     *
     * @param processName
     * @param process
     * @throws SOProcessException 
     */
    static private void run( final String processName, final ISOProcess process) throws SOProcessException {

        DialogUtil.runInProgressDialog(processName, true, process, true, true);
        
    }
   
}
