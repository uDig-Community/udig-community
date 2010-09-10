package net.refractions.linecleaner.cleansing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class NodeCollection {

    List<Point> endNodes;
    
    private boolean flagged;
    
    public NodeCollection () {
        this.endNodes = new ArrayList<Point>();
    }

    public boolean add( Point feature ) {
        return endNodes.add(feature);
    }
    
    public Point remove(int index) {
        return endNodes.remove(index);
    }
    
    public boolean remove(Point endNode) {
        return endNodes.remove(endNode);
    }

    public void setFlagged( boolean flagged ) {
        this.flagged = flagged;
    }
    
    public Iterator<Point> iterator() {
        return endNodes.iterator();
    }

    /**
     * 
     * If this collection is flagged, no processing should be performed on it,
     * as manual intervention is required.
     *
     * @return
     */
    public boolean isFlagged() {
        return flagged;
    }

    public boolean contains( Point endNode ) {
        return endNodes.contains(endNode);
    }
    
    public int size() {
    	return endNodes.size();
    }
    
    public Envelope getEnvelope() {
    	return createGeometryCollection().getEnvelopeInternal();
    }
    
    private GeometryCollection createGeometryCollection() {
    	GeometryFactory factory = new GeometryFactory();
    	Geometry[] geometries = endNodes.toArray(new Geometry[endNodes.size()]);
    	GeometryCollection geometryCollection = factory.createGeometryCollection(geometries);
    	return geometryCollection;
    }

	public Point calculateAveragePoint() {
		return createGeometryCollection().getCentroid();
	}
	
	public static final String NEWLINE = System.getProperty("line.separator");
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("Size: " + size() + " Flagged: " + isFlagged() + "[" + NEWLINE);
		
		Iterator iter = iterator();
		while (iter.hasNext()) {
			Point endNode = (Point) iter.next();
			
			buffer.append(EndNodesProcessor.endNodeToString(endNode));
			
			if (!iter.hasNext()) {
				buffer.append("]");
			}
			buffer.append(NEWLINE);
		}
		
		return buffer.toString();
	}

	public void merge(NodeCollection collectionB) {
		this.endNodes.addAll(collectionB.endNodes);
	}
	
//	public Envelope getEnvelope() {
//		Envelope envelope = new Envelope();
//		
//		for (Point point : endNodes) {
//			envelope.expandToInclude(point.getCoordinate());
//		}
//		return envelope;
//	}

}
