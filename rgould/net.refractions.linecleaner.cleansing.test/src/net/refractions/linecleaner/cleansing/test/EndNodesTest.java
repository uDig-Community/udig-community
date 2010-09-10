package net.refractions.linecleaner.cleansing.test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import net.refractions.linecleaner.cleansing.EndNodesProcessor;
import net.refractions.linecleaner.cleansing.NodeCollection;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.resources.TestData;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class EndNodesTest extends TestCase {
	private IndexedShapefileDataStore ds;
	private String typeName;
	private FeatureSource source;
	private FeatureStore featureStore;

	protected void setUp() throws Exception {
        super.setUp();
        
        long before = System.currentTimeMillis();

		ds = getDataStore("endNodesTest.shp");
		
        typeName = ds.getTypeNames()[0];
        source = ds.getFeatureSource( typeName );
        featureStore = (FeatureStore) ds.getFeatureSource();
        System.out.println(source.getFeatures().getCount());
        long after = System.currentTimeMillis();
        after = after-before;
        System.out.println("Startup time: " + after);
    }
    
	public IndexedShapefileDataStore getDataStore(String filename) throws Exception {

        File shp = TestData.file( this, filename );
		Map params=new HashMap();
		params.put( IndexedShapefileDataStoreFactory.URLP.key, shp.toURL());
		params.put( IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, new Boolean(true));
		params.put( IndexedShapefileDataStoreFactory.MEMORY_MAPPED.key, new Boolean(false));
//		params.put( IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key, IndexedShapefileDataStoreFactory.TREE_GRX);
		params.put( IndexedShapefileDataStoreFactory.SPATIAL_INDEX_TYPE.key, IndexedShapefileDataStoreFactory.TREE_QIX);
        
		Logger.getLogger("org.geotools.data").setLevel(Level.WARNING);
		
		IndexedShapefileDataStoreFactory fac=new IndexedShapefileDataStoreFactory();
		return (IndexedShapefileDataStore) fac.createDataStore(params);
	}
	
	public void XtestEndNodes() throws Exception {
		EndNodesProcessor processor = new EndNodesProcessor(this.featureStore, 3, 81);
		processor.run();
		List<NodeCollection> nodeCollections = processor.getNodeCollections();
		processor.printNodeCollections();
		assertEquals(21, nodeCollections.size());
		
		
		
		NodeCollection collection = nodeCollections.get(0);
		
		checkNodeCollection(collection, 2, new Coordinate[] {
				new Coordinate(108, 150),
				new Coordinate(109.31792104741125, 150.30065463201748)
		} );
		
		checkNodeCollection(nodeCollections.get(1), 2, new Coordinate[] {
				new Coordinate(105.01934536798255, 140.56915691331088),
				new Coordinate(105.7954770878794, 140.39004959333468)
		} );
		
		checkNodeCollection(nodeCollections.get(2), 2, new Coordinate[] {
			new Coordinate(108.42238444753026, 138.3004641936124),
			new Coordinate(109.31792104741125, 138.41986907359654)
		} );
		
		checkNodeCollection(nodeCollections.get(3), 7, new Coordinate[] {
			new Coordinate(111.9448284070621, 140.15123983336642),
			new Coordinate(112.60155524697483, 139.91243007339816),
			new Coordinate(114.57173576671296, 138.18105931362828),
			new Coordinate(115.46727236659395, 138.24076175362035),
			new Coordinate(117.43745288633208, 139.97213251339022),
			new Coordinate(118.4523943661972, 139.91243007339816),
			new Coordinate(120.30317000595122, 138.06165443364415)
		} );
		
		checkNodeCollection(nodeCollections.get(4), 2, new Coordinate[] {
			new Coordinate(162.36619883323013, 217.57945862308756),
			new Coordinate(163.88458503323193, 217.83252298975455)
		} );
		
		checkNodeCollection(nodeCollections.get(5), 2, new Coordinate[] {
			new Coordinate(149.71298049988178, 217.4529264397541),
			new Coordinate(148.7007230332139, 218.33865172308847)
		} );
		
		checkNodeCollection(nodeCollections.get(6), 2, new Coordinate[] {
			new Coordinate(137.69242308320085, 189.61584610638772),
			new Coordinate(139.84347019987007, 188.85665300638684)
		} );
		
		checkNodeCollection(nodeCollections.get(7), 2, new Coordinate[] {
			new Coordinate(137.56589089986736, 181.01165763971085),
			new Coordinate(137.56589089986736, 179.2402070730421)
		} );
		
		checkNodeCollection(nodeCollections.get(8), 2, new Coordinate[] {
			new Coordinate(183.3705412665884, 180.37899672304343),
			new Coordinate(184.12973436658928, 178.86061052304163)
		} );
		
		checkNodeCollection(nodeCollections.get(9), 2, new Coordinate[] {
			new Coordinate(184.50933091658973, 216.69373333975318),
			new Coordinate(185.77465274992457, 217.57945862308756)
		} );
		
		checkNodeCollection(nodeCollections.get(10), 2, new Coordinate[] {
			new Coordinate(135.9209725165321, 218.71824827308893),
			new Coordinate(137.4393587165339, 217.95905517308802)
		} );
		
		checkNodeCollection(nodeCollections.get(11), 2, new Coordinate[] {
			new Coordinate(162.74789444973896, 257.5176921539472),
			new Coordinate(161.4934168575729, 258.04589324538557)
		} );
		
		checkNodeCollection(nodeCollections.get(12), 2, new Coordinate[] {
			new Coordinate(163.01199499545814, 249.99082660095078),
			new Coordinate(164.00237204190503, 250.7831282381083)
		} );
		
		checkNodeCollection(nodeCollections.get(13), 2, new Coordinate[] {
			new Coordinate(151.25952071095494, 257.1215413353685),
			new Coordinate(152.24989775740184, 255.33886265176403)
		} );
		
		checkNodeCollection(nodeCollections.get(14), 3, new Coordinate[] {
			new Coordinate(171.9253884134802, 246.42546923374195),
			new Coordinate(173.37794141493566, 246.42546923374195),
			new Coordinate(174.10421791566338, 246.0293184151632)
		} );
		
		checkNodeCollection(nodeCollections.get(15), 7, new Coordinate[] {
			new Coordinate(155.80679036352993, 165.8434831733956),
			new Coordinate(157.77142564377422, 165.8273166719513),
			new Coordinate(159.28068062227194, 165.7329882357952),
			new Coordinate(160.60127872845746, 165.7329882357952),
			new Coordinate(162.11053370695515, 165.7329882357952),
			new Coordinate(163.52546024929677, 165.4500029273269),
			new Coordinate(165.97799958935556, 166.11030198041962)
		} );
		
		checkNodeCollection(nodeCollections.get(16), 7, new Coordinate[] {
			new Coordinate(191.9312133336433, 250.25475807982778),
			new Coordinate(192.0659817670335, 248.6375368791458),
			new Coordinate(192.26813441711872, 246.48124194490322),
			new Coordinate(192.53767128389904, 244.3249470106606),
			new Coordinate(192.67243971728922, 242.2360362931131),
			new Coordinate(192.73982393398433, 240.28189400895573),
			new Coordinate(192.80720815067937, 238.5299043748836)
		} );
		
		checkNodeCollection(nodeCollections.get(17), 27, new Coordinate[] {
			new Coordinate(198.06317705289572, 250.25475807982778),
			new Coordinate(197.7936401861154, 248.6375368791458),
			new Coordinate(197.72625596942032, 246.48124194490322),
			new Coordinate(197.8610244028105, 244.3249470106606),
			new Coordinate(197.65887175272528, 242.2360362931131),
			new Coordinate(197.8610244028105, 240.28189400895573),
			new Coordinate(197.99579283620065, 238.5972885915787),
			new Coordinate(199.27609295340721, 237.7886779912377),
			new Coordinate(201.63454053773506, 237.99083064132296),
			new Coordinate(204.060372338758, 238.1255990747131),
			new Coordinate(206.28405148969568, 238.19298329140818),
			new Coordinate(208.50773064063335, 238.1255990747131),
			new Coordinate(210.59664135818088, 238.1255990747131),
			new Coordinate(212.61816785903332, 238.05821485801803),
			new Coordinate(214.63969435988577, 238.05821485801803),
			new Coordinate(216.52645242734806, 238.05821485801803),
			new Coordinate(217.67198411116445, 238.5972885915787),
			new Coordinate(217.87413676124967, 239.54066762530982),
			new Coordinate(217.1329103776038, 240.95573617590654),
			new Coordinate(216.59383664404314, 242.37080472650325),
			new Coordinate(216.39168399395788, 244.05541014388027),
			new Coordinate(216.59383664404314, 245.94216821134256),
			new Coordinate(216.3242997772628, 247.69415784541468),
			new Coordinate(216.18953134387263, 250.456910729913),
			new Coordinate(216.3242997772628, 252.07413193059497),
			new Coordinate(214.5723101431907, 252.47843723076545),
			new Coordinate(212.28124677555792, 252.47843723076545)
		} );
		
		checkNodeCollection(nodeCollections.get(18), 9, new Coordinate[] {
			new Coordinate(199.3434771701023, 233.7456249895328),
			new Coordinate(201.70192475443014, 233.8130092062279),
			new Coordinate(204.060372338758, 233.7456249895328),
			new Coordinate(206.28405148969568, 233.7456249895328),
			new Coordinate(208.7098832907186, 233.880393422923),
			new Coordinate(210.59664135818088, 233.880393422923),
			new Coordinate(213.02247315920383, 233.94777763961807),
			new Coordinate(214.97661544336117, 233.67824077283774),
			new Coordinate(217.60459989446935, 233.94777763961807)
		} );
		
		checkNodeCollection(nodeCollections.get(19), 5, new Coordinate[] {
			new Coordinate(221.5128844627841, 239.6080518420049),
			new Coordinate(221.24334759600376, 242.23603629311307),
			new Coordinate(220.83904229583325, 244.45971544405077),
			new Coordinate(221.58026867947916, 247.2224683285491),
			new Coordinate(221.31073181269883, 249.98522121304745)
		} );
		
		checkNodeCollection(nodeCollections.get(20), 2, new Coordinate[] {
			new Coordinate(213.62893110945956, 256.7910270992507),
			new Coordinate(211.13571509174153, 256.7910270992507)
		} );
		
		assertTrue(nodeCollections.get(17).isFlagged());
		
//    	for (int i = 0; i < nodeCollections.size(); i++) {
//    		NodeCollection nc = nodeCollections.get(i);
//    		System.out.print("NodeCollection["+i+"] contains EndNodes: ");
//    		Iterator iter = nc.iterator();
//    		while (iter.hasNext()) {
//    			Point point = (Point) iter.next();
//    			Feature feature = (Feature) point.getUserData();
//    			
//    			System.out.print(feature.getID()+" ");
//    		}
//    		System.out.println();
//    	}
	}

	private void checkNodeCollection(NodeCollection collection, int expectedSize, Coordinate[] coordinates) {
		assertEquals(collection.size(), expectedSize);
		
		
		GeometryFactory factory = new GeometryFactory();
		
		for (Coordinate coord : coordinates) {
			
			Point point = factory.createPoint(coord);
			
			Iterator iter = collection.iterator();
			
			boolean found = false;
			
			while (iter.hasNext() && found == false) {
				Point compare = (Point) iter.next();
				if (compare.equals(point)) {
					found = true;
				} 
			}
			if (!found) {
				assertTrue("Node Collection does not contain coordinate " + point, false);
			}
		}
	}
	
	public void XtestPerformance100Features() throws Exception {
		ratePerformance(100, "endNodesTest.shp");
	}
	
	public void XtestPerformance400Features() throws Exception {
		ratePerformance(400, "400features.shp");
	}
	
	public void testPerformance1000Features() throws Exception {
		ratePerformance(1092, "1092features.shp");
	}
	
	public void XtestPerformance2000Features() throws Exception {
		ratePerformance(2076, "2076features.shp");
	}
	
	public void XtestPerformance4000Features() throws Exception {
		ratePerformance(4000, "4000features.shp");
	}
	
	public void XtestPerformancFTENRoads() throws Exception {
		ratePerformance(17000, "ftenroads.shp");
	}
	
	public void XtestBug() throws Exception {
		ratePerformance(4, "newBigger.shp");
	}
	
	private void ratePerformance(int numberOfFeatures, String shapefile) throws Exception {
		IndexedShapefileDataStore datastore = getDataStore(shapefile);
		datastore.getFeatureSource(); //force building of index before we calc.
		
		EndNodesProcessor enp = new EndNodesProcessor(
				(FeatureStore) datastore.getFeatureSource(),  25, 2500);
		long before = System.currentTimeMillis();
		enp.run();
		long after = System.currentTimeMillis();
		
		after = after-before;
		
		System.out.println("Time to create Collection for "+numberOfFeatures+" features: " + after);
		System.out.println("Number of node collections constructed: " + enp.getNodeCollections().size());
	}
}
