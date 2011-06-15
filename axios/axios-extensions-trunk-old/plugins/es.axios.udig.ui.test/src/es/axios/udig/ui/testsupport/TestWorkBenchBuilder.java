package es.axios.udig.ui.testsupport;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.memory.MemoryServiceExtensionImpl;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.internal.commands.CreateMapCommand;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Builds a testing environment with test georesources and data
 * <p>
 * Sample usage:
 * 
 * <pre>
 * <code>
 * public class FooTest extends TestCase {
 *     private TestWorkBenchBuilder testData;
 * 
 *     public void setUp() {
 *         testData = new TestWorkbenchBuilder();
 *         testData.setUp();
 *     }
 * 
 *     public void tearDown() {
 *         testData.tearDown();
 *     }
 * 
 *     public void testXXX(){
 *          IGeoResource points = testData.getPoints();
 *          ... do something 
 *      }
 * }
 * </code>
 * </pre>
 * 
 * </p>
 * 
 * @author gabriel
 * @since 1.1.0
 */
public class TestWorkBenchBuilder {

    private IMap map;
    private IGeoResource points;
    private IGeoResource lines;
    private IGeoResource polygons;

    private ILayer pointsLayer;
    private ILayer linesLayer;
    private ILayer polygonsLayer;

    /**
     * Creates a Map and populates it with three sample georesources from the scratch resource
     * provider
     * 
     * @throws Exception
     */
    public void setUp() throws Exception {
        removeScratchResource();

        List<IGeoResource> resources = new ArrayList<IGeoResource>();
        List<SimpleFeature> features;

        features = TestData.createPointFeatures();
        points = createGeoResource(features);
        resources.add(points);

        features = TestData.createLineFeatures();
        lines = createGeoResource(features);
        resources.add(lines);

        features = TestData.createPolygontFeatures();
        polygons = createGeoResource(features);
        resources.add(polygons);

        CreateMapCommand command = new CreateMapCommand(null, resources, null);
        ApplicationGIS.getActiveProject().sendSync(command);
        command.run(new NullProgressMonitor());
        map = command.getCreatedMap();
        final boolean wait = true;
        ApplicationGIS.openMap(map, wait);

        for( ILayer layer : map.getMapLayers() ) {
            String name = layer.getName();
            if (TestData.FTYPE_LINES.equals(name)) {
                linesLayer = layer;
            } else if (TestData.FTYPE_POINTS.equals(name)) {
                pointsLayer = layer;
            } else if (TestData.FTYPE_POLYGONS.equals(name)) {
                polygonsLayer = layer;
            }
        }
    }

    /**
     * TODO: remove map or find a way to not get a question message befor closing
     * 
     * @throws Exception
     */
    public void tearDown() throws Exception {
        removeScratchResource();
    }

    public IGeoResource createGeoResource( List<SimpleFeature> features ) throws IOException {

        final ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();

        SimpleFeatureType SimpleFeatureType = features.get(0).getFeatureType();
        IGeoResource resource;
        resource = catalog.createTemporaryResource(SimpleFeatureType);
        FeatureStore<SimpleFeatureType, SimpleFeature> store = resource.resolve(FeatureStore.class, new NullProgressMonitor());
        SimpleFeature[] toArray = features.toArray(new SimpleFeature[]{});
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = DataUtilities.collection(toArray);
        store.addFeatures(collection);
        return resource;
    }

    public void removeScratchResource() throws IOException {
        final ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
        URL url = MemoryServiceExtensionImpl.URL;
        List<IResolve> services = catalog.find(url, null);
        for( IResolve res : services ) {
            if (res.canResolve(IService.class)) {
                IService service = res.resolve(IService.class, null);
                catalog.remove(service);
            }
        }
    }

    public IGeoResource getLines() {
        return lines;
    }

    public IMap getMap() {
        return map;
    }

    public IGeoResource getPoints() {
        return points;
    }

    public IGeoResource getPolygons() {
        return polygons;
    }

    public ILayer getLinesLayer() {
        return linesLayer;
    }

    public ILayer getPointsLayer() {
        return pointsLayer;
    }

    public ILayer getPolygonsLayer() {
        return polygonsLayer;
    }

    public ILayer getLayerWithPolygonGeometry() {
        return null;
    }

}
