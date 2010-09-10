package es.axios.udig.ui.spatialoperations.buffer.internal.transaction;

import java.util.List;

import junit.framework.TestCase;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.command.UndoableMapCommand;
import net.refractions.udig.project.command.factory.SelectionCommandFactory;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import es.axios.udig.spatialoperations.internal.parameters.IBufferInNewLayerParameters;
import es.axios.udig.spatialoperations.internal.processmanager.SOProcessManager;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.testsupport.TestData;
import es.axios.udig.ui.testsupport.TestWorkBenchBuilder;

public class BufferRunnableProcessTest extends TestCase {

    ILayer sourceLayer;
    IBufferInNewLayerParameters params;
    TestWorkBenchBuilder testData;

    protected void setUp() throws Exception {
        super.setUp();
        testData = new TestWorkBenchBuilder();
        testData.setUp();

        IMap map = testData.getMap();
        sourceLayer = map.getMapLayers().get(0);

// FIXME        
//        params = new BufferParameters();
//        params.setBufferWidth(new Double(2));
//        params.setUnitsOfMeasure(GeoToolsUtils.DEGREES);
//
//        IGeoResource polygons = testData.getPolygons();
//        params.setTargetGeoResource(polygons);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        testData.tearDown();
        testData = null;
    }

    /**
     * Asserts a new layer is added to the map as result of the operation if the input
     * {@link BufferInNewLayerParameters} states the feature type is new
     * 
     * @throws Exception
     */
    public void testAddsNewLayer() throws Exception {
        SimpleFeatureType targetType = TestData.createType(TestData.FTYPE_POLYGONS);
        List<AttributeDescriptor> attributeTypes = targetType.getAttributes();
        String[] properties = new String[attributeTypes.size()];
        for( int i = 0; i < attributeTypes.size(); i++ ) {
            properties[i] = attributeTypes.get(i).getLocalName();
        }
        targetType = DataUtilities.createSubType(targetType, properties, null, "testName", null);
        // set a not yet registered target type so the op creates the IGeoResource
//FIXME
//        params.setTargetFeatureType(targetType);
//        params.setNewResource(true);

        SOProcessManager.bufferOperation(params);
//        BufferProcess process = new BufferProcess(params);

        IMap map = testData.getMap();
        int prevCount = map.getMapLayers().size();
  
        int layerCount = map.getMapLayers().size();

        assertEquals(prevCount + 1, layerCount);
    }

    public void testRespectsSelection() throws Exception {
        try {
            final IGeoResource points = testData.getPoints();
            final IGeoResource polygons = testData.getPolygons();

            FeatureSource<SimpleFeatureType, SimpleFeature> source = points.resolve(FeatureSource.class, new NullProgressMonitor());
            SimpleFeature feature = GeoToolsUtils.firstFeature(source.getFeatures());

            SelectionCommandFactory instance = SelectionCommandFactory.getInstance();
            UndoableMapCommand command = instance.createFIDSelectCommand(sourceLayer, feature);
            sourceLayer.getMap().sendCommandSync(command);
            assertTrue(sourceLayer.getFilter() instanceof FidFilter);

            FeatureStore<SimpleFeatureType, SimpleFeature> targetStore = polygons.resolve(FeatureStore.class, null);
            // empty the target feature store
            targetStore.removeFeatures(Filter.NONE);

            SOProcessManager.bufferOperation(params);
            
//            BufferProcess process = new BufferProcess(params);
  //          process.run(new NullProgressMonitor());

            // ensure a buffer only for the selected feature was created
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = targetStore.getFeatures();
            assertEquals(1, features.size());
            SimpleFeature bufferFeature = GeoToolsUtils.firstFeature(features);

            final String expected = (String) feature.getAttribute("name");
            final String actual = (String) bufferFeature.getAttribute("name");
            assertEquals(expected, actual);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
