package es.axios.udig.spatialoperations.internal.ui.dialogs;

import net.refractions.udig.project.ILayer;

/**
 * Allows to define if a given ILayer has to be shown on the layers list.
 * <p>
 * Implementations will receive a ILayer from this viewer's Map
 * </p>
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public interface LayerFilter {
    /**
     * This method serves as the protocol to decide if a given layer is to be shown in the
     * enclosing {@link TargetLayerSelectionPage}.
     * 
     * @param layer an ILayer upon which to decide wether to show it in the list or not
     * @return <code>true</code> if the Layer has to be shown on the layers list,
     *         <code>false</code> otherwise.
     */
    public boolean show( ILayer layer );
}