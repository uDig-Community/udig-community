package net.refractions.linecleaner.cleansing.test;

import java.util.logging.Level;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

import net.refractions.linecleaner.cleansing.PseudoNodeProcessor;

public class PseudoNodeProcessorTest extends ProcessorTestCase {

	@Override
	protected String getFilename() {
//		return "1092features.shp";
        return "dra81.shp";
	}
	
	public void testPseudoNodeProcessor() throws Exception {
        FeatureIterator i = this.source.getFeatures().features();
        try {
            while (i.hasNext()) {
                Feature f = i.next();
                Geometry g = f.getDefaultGeometry();
                Coordinate start = g.getCoordinates()[0];
                int length = g.getCoordinates().length;
                Coordinate end = g.getCoordinates()[length-1];
                
                System.out.println("s: " + start + "e: " + end);
            }
        } finally {
            i.close();
        }
//      PseudoNodeProcessor pnp = new PseudoNodeProcessor((FeatureStore) this.source, Level.FINEST);
//      pnp.runInternal(new NullProgressMonitor());
	}

    public void testLineMerge() throws Exception {
        LineMerger merger = new LineMerger();
        
        GeometryFactory gf = new GeometryFactory();
        LineString one = gf.createLineString(new Coordinate[] {
                new Coordinate(1446519.920063, 616518.304967),
                new Coordinate(1446511.853485, 616526.363228),
                new Coordinate(1446506.270196, 616530.701216),
                new Coordinate(1446505.153539, 616531.568813)
        });
        LineString two = gf.createLineString(new Coordinate[] {
                new Coordinate(1446519.920063, 616518.304967),
                new Coordinate(1446522.778316, 616511.607076),
                new Coordinate(1446520.678215, 616504.284477),
                new Coordinate(1446516.594773, 616496.711995),
                new Coordinate(1446509.903053, 616493.852326),
                new Coordinate(1446501.103004, 616491.735313),
                new Coordinate(1446491.803008, 616493.588457),
                new Coordinate(1446489.944653, 616492.346035)
        });
        
        merger.add(one);
        merger.add(two);
        for (Object o: merger.getMergedLineStrings()) {
            LineString ls = (LineString)o;
            System.out.println(ls);
        }
    }
}
