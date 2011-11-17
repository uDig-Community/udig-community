/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2010, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under General Public License (GPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package es.axios.udig.sextante.linetopolygon;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.sextante.task.LineToPolygonTask;
import es.axios.udig.spatialoperations.ui.parameters.SOCommandException;
import es.axios.udig.spatialoperations.ui.parameters.SimpleCommand;
import es.axios.udig.spatialoperations.ui.taskmanager.SOFeatureStore;
import es.axios.udig.ui.commons.mediator.AppGISMediator;
import es.axios.udig.ui.commons.message.InfoMessage;
import es.axios.udig.ui.commons.util.DialogUtil;
import es.axios.udig.ui.commons.util.FeatureUtil;
import es.axios.udig.ui.commons.util.LayerUtil;
import es.axios.udig.ui.commons.util.MapUtil;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;

/**
 * This command make use of the SimpleCommand and here are defined every text,
 * type, etc, that uses the SimplePresenter within the GUI.
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.3.0
 */
public class LineToPolygonCommand extends SimpleCommand {

	protected static InfoMessage	INITIAL_MESSAGE	= new InfoMessage("Convert closed lines into polygons.",
																InfoMessage.Type.IMPORTANT_INFO);
	private IMap map;

	public LineToPolygonCommand() {

		super(INITIAL_MESSAGE);
	}

	@Override
	public void setGroupSourceText() {

		this.groupSourceText = "Source";
	}

	@Override
	public void setGroupTargetInputsText() {

		this.groupTargetInputsText = "Result";
	}

	@Override
	public void setLabelSourceLayerText() {

		this.labelSourceLayerText = "Source";
	}

	@Override
	public void setOperationID() {

		this.operationID = "LineToPolygon";
	}

	@Override
	public void setOperationName() {

		this.operationName = "LineToPolygon";
	}

	@Override
	public void setTabItemAdvancedText() {

		this.tabItemAdvancedText = "Advanced";
	}

	@Override
	public void setTabItemBasicText() {
		this.tabItemBasicText = "Basic";
	}

	@Override
	public void setTargetLabelText() {

		this.targetLabelText = "Layer";
	}

	@Override
	public void setTargetLabelToolTipText() {

		this.targetLabelToolTipText = "The layer where the result will go. Select an existent layer or write a new one.";
	}

	@Override
	public void setToolTipText() {

		this.toolTipText = "Convert lines to polygon.";
	}

	@Override
	protected Object[] getSourceGeomertyClass() {

		Object[] obj = new Object[] {

		LineString.class, MultiLineString.class };

		return obj;

	}

	@Override
	protected Object[] getResultLayerGeometry() {

		Object[] obj;
		if (sourceLayer != null) {

			obj = new Object[1];
			Class<? extends Geometry> sourceGeom = LayerUtil.getGeometryClass(this.sourceLayer);

			// is a geometry collection
			if (sourceGeom.getSuperclass().equals(GeometryCollection.class)) {

				obj[0] = MultiPolygon.class;
			} else {

				obj[0] = Polygon.class;
			}

		} else {

			obj = new Object[2];
			obj[0] = Polygon.class;
			obj[1] = MultiPolygon.class;
		}

		return obj;
	}

	@Override
	public void executeOperation() throws SOCommandException {

		final NullProgressMonitor progress = new NullProgressMonitor();
		this.map = sourceLayer.getMap();

		// 
		final FeatureStore<SimpleFeatureType, SimpleFeature> source = getFeatureStore(sourceLayer);

		try {
			progress.setTaskName("LinesToPolygon Spatial Operation");
			String msg = MessageFormat.format("Doing lines to polygon of {0}", sourceLayer.getName());
			progress.beginTask(msg, IProgressMonitor.UNKNOWN);
			IRunnableWithProgress runner = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final FeatureStore<SimpleFeatureType, SimpleFeature> resultStore;
					LineToPolygonTask task = new LineToPolygonTask(source);

					try {
						task.run();
						resultStore = task.getResult();
						addFeaturesToTargetStore(resultStore);
					} catch (GeoAlgorithmExecutionException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (SOCommandException e) {
						e.printStackTrace();
					} finally {
						map.getRenderManager().refresh(targetLayer, map.getBounds(progress));
						progress.done();
					}
				}
			};
			DialogUtil.runInProgressDialog("Executing LinesToPolygon Operation", true, runner, true, true);
		} catch (Exception e) {
			throw new SOCommandException(e.getMessage());
		}
	}

	private final FeatureStore<SimpleFeatureType, SimpleFeature> getFeatureStore(final ILayer layer)
		throws SOCommandException {

		assert layer != null;

		IGeoResource resource = layer.getGeoResource();

		if (resource == null) {
			throw new SOCommandException("The layer does not have GeoResource");
		}
		FeatureStore<SimpleFeatureType, SimpleFeature> store;
		try {
			store = resource.resolve(FeatureStore.class, new NullProgressMonitor());

			return store;

		} catch (IOException e) {
			throw new SOCommandException(e.getMessage());
		}
	}

	private final void addFeaturesToTargetStore(FeatureStore<SimpleFeatureType, SimpleFeature> resultStore)
		throws SOCommandException, IOException {

		FeatureStore<SimpleFeatureType, SimpleFeature> emptyTargetStore = null;

		if (this.targetLayer != null) {

			emptyTargetStore = this.getFeatureStore(this.targetLayer);

		} else { // create a new layer

			this.targetLayer = createNewLayer();
			IGeoResource targetGeoResource = targetLayer.getGeoResource();
			try {
				emptyTargetStore = targetGeoResource.resolve(FeatureStore.class, null);
			} catch (IOException e) {
				throw new SOCommandException(e.getMessage());
			}
		}

		Transaction transactionOld = ((net.refractions.udig.project.internal.Map) this.map).getEditManagerInternal()
					.getTransaction();

		SOFeatureStore soStore = new SOFeatureStore(emptyTargetStore, transactionOld);

		// add to target store.
		addToStore(soStore, resultStore);
	}

	/**
	 * Add the features from the resultStore, which is the output off the
	 * sextante operation; to the soStore.
	 * 
	 * @param soStore
	 * @param emptyTargetStore
	 * @param resultStore
	 * @throws IOException
	 */
	private void addToStore(SOFeatureStore soStore, FeatureStore<SimpleFeatureType, SimpleFeature> resultStore)
		throws IOException {

		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = resultStore.getFeatures();
		FeatureIterator<SimpleFeature> iter = collection.features();
		SimpleFeatureType newFeatureType = soStore.getSchema();

		Transaction transaction = soStore.getTransaction();
		try {

			while (iter.hasNext()) {
				SimpleFeature sextanteFeature = iter.next();

				SimpleFeature featureToAdd = FeatureUtil.createFeatureUsing(sextanteFeature, newFeatureType,
							(Geometry) sextanteFeature.getDefaultGeometry());

				soStore.addFeatures(DataUtilities.collection(new SimpleFeature[] { featureToAdd }));
				transaction.commit();
			}
		} catch (Exception ex) {
			transaction.rollback();
			ex.printStackTrace();
		} finally {
			collection.close(iter);
			transaction.close();
		}

	}

	private final CoordinateReferenceSystem getTargetCRS() {
		if (this.targetLayer != null) {
			return this.targetLayer.getCRS();
		} else {
			return getMapCRS();
		}
	}

	private CoordinateReferenceSystem getMapCRS() {

		return MapUtil.getCRS(map);
	}

	private final ILayer createNewLayer() throws SOCommandException {

		FeatureStore<SimpleFeatureType, SimpleFeature> targetStore;
		try {
			SimpleFeatureType type = FeatureUtil.createFeatureType(this.sourceLayer.getSchema(), targetLayerName,
						getTargetCRS(), this.targetGeometryClass);

			IGeoResource targetGeoResource = AppGISMediator.createTempGeoResource(type);
			assert targetGeoResource != null;

			targetStore = targetGeoResource.resolve(FeatureStore.class, null);

			assert targetStore != null;

			ILayer newLayer = MapUtil.addLayerToMap((IMap) this.map, targetGeoResource);

			return newLayer;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SOCommandException(e);
		}
	}
}
