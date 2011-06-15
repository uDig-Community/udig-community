package es.axios.udig.spatialoperations.process.internal;


public class BufferRunnableProcessTest {

//    ILayer sourceLayer;
//    IBufferInNewLayerParameters params;
//    TestWorkBenchBuilder testData;

    protected void setUp() throws Exception {
//        super.setUp();
//        testData = new TestWorkBenchBuilder();
//        testData.setUp();
//
//        IMap map = testData.getMap();
//        sourceLayer = map.getMapLayers().get(0);

// FIXME        
//        params = new BufferParameters();
//        params.setBufferWidth(new Double(2));
//        params.setUnitsOfMeasure(GeoToolsUtils.DEGREES);
//
//        IGeoResource polygons = testData.getPolygons();
//        params.setTargetGeoResource(polygons);
    }

    protected void tearDown() throws Exception {
//        super.tearDown();
//        testData.tearDown();
//        testData = null;
    }

    /**
     * Asserts a new layer is added to the map as result of the operation if the input
     * {@link BufferInNewLayerParameters} states the feature type is new
     * 
     * @throws Exception
     */
    public void testAddsNewLayer() throws Exception {
//        FeatureType targetType = TestData.createType(TestData.FTYPE_POLYGONS);
//        AttributeType[] attributeTypes = targetType.getAttributeTypes();
//        String[] properties = new String[attributeTypes.length];
//        for( int i = 0; i < attributeTypes.length; i++ ) {
//            properties[i] = attributeTypes[i].getName();
//        }
//        targetType = DataUtilities.createSubType(targetType, properties, null, "testName", null);
        // set a not yet registered target type so the op creates the IGeoResource
//FIXME
//        params.setTargetFeatureType(targetType);
//        params.setNewResource(true);
//
//        SOProcessManager.bufferOperation(params);
////        BufferProcess process = new BufferProcess(params);
//
//        IMap map = testData.getMap();
//        int prevCount = map.getMapLayers().size();
//  
//        int layerCount = map.getMapLayers().size();
//
//        assertEquals(prevCount + 1, layerCount);
//    }
//
//    public void testRespectsSelection() throws Exception {
//        try {
//            final IGeoResource points = testData.getPoints();
//            final IGeoResource polygons = testData.getPolygons();
//
//            FeatureSource source = points.resolve(FeatureSource.class, new NullProgressMonitor());
//            Feature feature = GeoToolsUtils.firstFeature(source.getFeatures());
//
//            SelectionCommandFactory instance = SelectionCommandFactory.getInstance();
//            UndoableMapCommand command = instance.createFIDSelectCommand(sourceLayer, feature);
//            sourceLayer.getMap().sendCommandSync(command);
//            assertTrue(sourceLayer.getFilter() instanceof FidFilter);
//
//            FeatureStore targetStore = polygons.resolve(FeatureStore.class, null);
//            // empty the target feature store
//            targetStore.removeFeatures(Filter.NONE);
//
//            SOProcessManager.bufferOperation(params);
//            
////            BufferProcess process = new BufferProcess(params);
//  //          process.run(new NullProgressMonitor());
//
//            // ensure a buffer only for the selected feature was created
//            FeatureCollection features = targetStore.getFeatures();
//            assertEquals(1, features.size());
//            Feature bufferFeature = GeoToolsUtils.firstFeature(features);
//
//            final String expected = (String) feature.getAttribute("name");
//            final String actual = (String) bufferFeature.getAttribute("name");
//            assertEquals(expected, actual);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
    }
}
