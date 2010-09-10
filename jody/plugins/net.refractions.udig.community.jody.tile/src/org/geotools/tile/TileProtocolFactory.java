package org.geotools.tile;

import java.io.IOException;
import java.net.URL;

import org.opengis.util.InternationalString;

import com.sun.org.apache.xerces.internal.util.URI;

/**
 * Glue code for teaching TileServer additional protocols.
 * <p>
 * As with any FactorySPI implementation we ask you to record an entry in your jar
 * MANIFEST for each implementation.
 * </p> 
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface TileProtocolFactory {
	/** Human readible name for this protocol */	
	InternationalString getName();
	
	/** Human readible description of protocol, often instructions for end-users */	
	InternationalString getDescription();
	
	/**
	 * Check if this protocol can be used to process the provided url.
	 * <p>
	 * Please note that this method may block, especially if it needs to confirm
	 * the content type, check any schema or perform version negotiation.
	 * </p>
	 * @param url Location of service to connect to, subclass should javadoc specifics.
	 * @return <code>ture</code> if protocol can be handled
	 */
    boolean canTile( URL url );
    
    /**
     * Create tile protocol for the provided url.
     * @param url Location of service to connect to
     * @return TileProtocol connected to indicated service, subclass should document specifics.
     * @throws IOException If connection failed due to communication problems
     */
    TileProtocol createTileStratagy( URL url ) throws IOException;
}
