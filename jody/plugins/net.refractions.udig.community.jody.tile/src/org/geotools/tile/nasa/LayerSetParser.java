package org.geotools.tile.nasa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jdom.Element;

/**
 * Utility class for dealing with LayerSet
 * 
 * @author Jody Garnett
 */
public class LayerSetParser {

    /**
     * Grab ToolBarImage from ExtendedInformation/ToolBarImage
     * <pre>
     * [ExtendedInformation]
     *     [ToolBarImage]Data\Icons\Interface\\usgs-1m-ortho.png[/ToolBarImage]
     * [/ExtendedInformation]
     * </pre>
     * 
     * @param layerSet
     * @return Icon
     */
    static Icon infoIcon( Element layerSet ) {
        try {
            Element extendedInformation = layerSet.getChild("ExtendedInformation");        
            String icon = extendedInformation.getChildText("ToolBarImage");
            
            return new ImageIcon( icon );
        }
        catch (NullPointerException noInfo ){
            return null; 
        }
    }

    /**
     * Extract extended information abstract form a layer set.
     */
    static String description( Element layerSet ){
        try {
            Element extendedInformation = layerSet.getChild("ExtendedInformation");        
            return extendedInformation.getChildText("Abstract");
        }
        catch( NullPointerException ignore ){
            return null;
        }
    }

    /**
     * List all layerSets, recursive...
     *
     * @param layerSet
     * @return List<Element> where ChildLayerSet
     */
    static List childLayerSets( Element layerSet ){        
        List childLayerSet = layerSet.getChildren("ChildLayerSet");
        
        if( childLayerSet.isEmpty() ){
            return Collections.EMPTY_LIST;
        }
        List list = new ArrayList( childLayerSet );
        
        // check for subchildren
        for( Iterator i = childLayerSet.iterator(); i.hasNext();){
            list.addAll( childLayerSets( (Element) i.next() ));
        }
        return list;
    }

    /**
     * List of recognized children, will navigate ChildLayerSet as well.
     * <p>
     * Recognized children:
     * <ul>
     * <li>QuadTileSet
     * </ul>
     * Please note that not all recognized children may be accessed, additional
     * acessors may defined in the future.
     * @see Accessor.create
     *
     * @param dom
     * @return recognized child layers, or empty list
     */
    public static List childLayers( Element layerSet ) {
        List quadTiles = layerSet.getChildren("QuadTileSet");        
        List list = new ArrayList( quadTiles );

        List childLayerSet = layerSet.getChildren("ChildLayerSet");        
        if( !childLayerSet.isEmpty() ){
            for( Iterator i = childLayerSet.iterator(); i.hasNext();){
                list.addAll( childLayers( (Element) i.next() ));
            }            
        }
        return list;
    }

}
