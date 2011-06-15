package es.axios.so.extension.copy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

import es.axios.udig.spatialoperations.tasks.SpatialOperationException;
import es.axios.udig.spatialoperations.ui.taskmanager.SOFeatureStore;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.util.FeatureUtil;
import es.axios.udig.ui.commons.util.GeoToolsUtils;
import es.axios.udig.ui.commons.util.GeometryUtil;
import es.axios.udig.ui.commons.util.LayerUtil;

public class CopyTask {

	private Filter						filter				= null;
	private CoordinateReferenceSystem	mapCrs				= null;
	private ILayer						sourceLayer			= null;
	private String						targetLayerName		= null;
	private Class<? extends Geometry>	targetGeom			= null;
	private ILayer						targetLayer			= null;

	private FeatureStore				targetStore			= null;
	private FeatureCollection			featuresFromSource	= null;
	private CoordinateReferenceSystem	sourceLayerCrs		= null;
	private String						oldfidAttributeName	= "OLDFID";
	private int							count				= 0;

	/**
	 * set parameters
	 * 
	 * @param filter
	 * @param mapCrs
	 * @param sourceLayer
	 * @param targetLayerName
	 * @param targetGeometryClass
	 * @throws IOException
	 */
	public void setParameters(	Filter filter,
								CoordinateReferenceSystem mapCrs,
								ILayer sourceLayer,
								String targetLayerName,
								Class<? extends Geometry> targetGeometryClass) throws IOException {

		this.filter = filter;
		this.mapCrs = mapCrs;
		this.sourceLayer = sourceLayer;
		this.targetLayerName = targetLayerName;
		this.targetGeom = targetGeometryClass;

	}

	/**
	 * set parameters
	 * 
	 * @param filter
	 * @param mapCrs
	 * @param sourceLayer
	 * @param targetLayer
	 * @throws IOException
	 */
	public void setParameters(Filter filter, CoordinateReferenceSystem mapCrs, ILayer sourceLayer, ILayer targetLayer)
		throws IOException {

		this.filter = filter;
		this.mapCrs = mapCrs;
		this.sourceLayer = sourceLayer;
		this.targetLayer = targetLayer;

	}

	/**
	 * Convert the object into geotools objects. This will go on the monitor.
	 * 
	 * @throws IOException
	 * @throws SchemaException
	 */
	public void convertToGeotools() throws IOException, SchemaException {

		this.sourceLayerCrs = LayerUtil.getCrs(this.sourceLayer);
		this.featuresFromSource = LayerUtil.getSelectedFeatures(sourceLayer, filter);

		targetStore = getTargetStore();
	}

	/**
	 * Method copied from a Monitor.
	 * 
	 * @return
	 * @throws SchemaException
	 * @throws IOException
	 */
	private FeatureStore getTargetStore() throws SchemaException, IOException {

		FeatureStore targetStore = null;

		if (this.targetLayer != null) {

			targetStore = getFeatureStore(targetLayer);

		} else {

			FeatureType type = FeatureUtil.createFeatureType(this.sourceLayer.getSchema(), targetLayerName, mapCrs,
						targetGeom);

			oldfidAttributeName = "OLDFID";

			// By default the attribute will be of the String class.
			type = FeatureUtil.addAttributeToFeatureType(type, oldfidAttributeName, String.class);

			IGeoResource targetGeoResource = AppGISMediator.createTempGeoResource(type);
			assert targetGeoResource != null;

			NullProgressMonitor progress = new NullProgressMonitor();
			targetStore = targetGeoResource.resolve(FeatureStore.class, progress);

			this.targetLayer = addLayerToMap(getMap(), targetGeoResource);
		}

		return targetStore;
	}

	/**
	 * * Method copied from a Monitor.
	 * 
	 * @return
	 */
	private IMap getMap() {

		IMap map = this.sourceLayer.getMap();
		assert map != null;
		return map;
	}

	/**
	 * * Method copied from a Monitor.
	 * 
	 * @param map
	 * @param geoResource
	 * @return
	 */
	protected ILayer addLayerToMap(IMap map, IGeoResource geoResource) {

		int index = map.getMapLayers().size();
		List<? extends ILayer> listLayer = AppGISMediator.addLayersToMap(map, Collections.singletonList(geoResource),
					index);

		assert listLayer.size() == 1; // creates only one layer

		ILayer layer = listLayer.get(0);

		return layer;
	}

	private FeatureStore getFeatureStore(ILayer layer) throws IOException {

		assert layer != null;

		FeatureType featureType = layer.getSchema();

		IGeoResource resource = layer.getGeoResource();

		if (resource == null) {
			// new resource is required because new layer was selected
			final ICatalog catalog = AppGISMediator.getCatalog();
			assert catalog != null;

			resource = catalog.createTemporaryResource(featureType);
		}
		final FeatureStore targetStore;

		targetStore = resource.resolve(FeatureStore.class, new NullProgressMonitor());

		SOFeatureStore store = new SOFeatureStore(targetStore, targetStore.getTransaction());

		return store;

	}

	public void refreshTargetLayer() {

		targetLayer.refresh(null);

	}

	/***************************************************************************
	 * 
	 * <pre>
	 * Code before this goes to CopyMonitor
	 * Code after that goes to CopyTask
	 * </pre>
	 * 
	 * 
	 * 
	 * 
	 */

	public void execute() {

		FeatureIterator iter = null;
		Feature featureToCopy = null;

		try {

			count = 0;
			iter = featuresFromSource.features();

			FeatureType featureType = this.targetStore.getSchema();
			CoordinateReferenceSystem targetLayerCrs = featureType.getDefaultGeometry().getCoordinateSystem();

			if (targetLayerCrs == null) {
				targetLayerCrs = mapCrs;
			}

			while (iter.hasNext() && count < 110) {

				featureToCopy = iter.next();

				long ini = System.currentTimeMillis();
				copyFeature(featureToCopy, targetLayerCrs, featureType);
				long end = System.currentTimeMillis();
				System.out.println("Total: " + (end - ini)); //$NON-NLS-1$

			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		finally {

			if (iter != null) {
				featuresFromSource.close(iter);
			}
		}

	}

	private void copyFeature(Feature featureToCopy, CoordinateReferenceSystem targetLayerCrs, FeatureType featureType)
		throws OperationNotFoundException, TransformException, IllegalAttributeException, SpatialOperationException {

		Geometry baseGeometry = featureToCopy.getDefaultGeometry();
		Geometry baseGeomOnMapCrs = GeoToolsUtils.reproject(baseGeometry, sourceLayerCrs, targetLayerCrs);

		Feature copiedFeature = copyFeature(featureToCopy, featureType, baseGeomOnMapCrs,
					oldfidAttributeName);
		insert(copiedFeature);
	}

	private void insert(Feature copiedFeature) throws SpatialOperationException {

		Transaction transaction = targetStore.getTransaction();
		try {
			Set<?> newIds = targetStore.addFeatures(DataUtilities.collection(new Feature[] { copiedFeature }));
			if (newIds.size() != 1) {
				final String msg = "failed inserting.";
				throw new SpatialOperationException(msg);
			}
			System.out.println(newIds.toString());
			transaction.commit();
		}
		catch (IOException e) {
			try {
				transaction.rollback();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		finally {
			try {
				transaction.close();
				count++;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	

	/**
	 * Function to copy a feature and add the field "oldfid" that will contain
	 * the ID of the original feature.
	 * 
	 * @param sourceFeature
	 * @param targetType
	 * @param newGeometry
	 * @param oldfidAtt
	 * @return a New Feature
	 * @throws IllegalAttributeException
	 */
	public Feature copyFeature(	final Feature sourceFeature,
										final FeatureType targetType,
										final Geometry newGeometry,
										final String oldfidAtt) throws IllegalAttributeException {

		try {

			Feature newFeature = DataUtilities.template(targetType);
			final GeometryAttributeType sourceGeometryType = sourceFeature.getFeatureType().getDefaultGeometry();
			final String geoAttName = sourceGeometryType.getName();
			List<String> omitAttr = new ArrayList<String>(1);

			omitAttr.add(geoAttName);
			//FIXME name
			newFeature = FeatureUtil.copyAttributesCheater(sourceFeature, newFeature, omitAttr);

			final Class geomClass = sourceGeometryType.getType();
			final Geometry adaptedGeometry = GeometryUtil.adapt(newGeometry, geomClass);

			final GeometryAttributeType targetGeometryType = targetType.getDefaultGeometry();
			final String targetGeoAttName = targetGeometryType.getName();

			newFeature.setAttribute(targetGeoAttName, adaptedGeometry);

			String id = sourceFeature.getID();

			String newString = id.substring(id.indexOf(".") + 1);//$NON-NLS-1$

			newFeature.setAttribute(oldfidAtt, newString);

			return newFeature;

		}
		catch (IllegalAttributeException e) {
			throw new IllegalAttributeException(e.getMessage());
		}
	}
	

}
