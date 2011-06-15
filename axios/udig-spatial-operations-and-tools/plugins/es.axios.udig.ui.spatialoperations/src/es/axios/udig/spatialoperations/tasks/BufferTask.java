/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.spatialoperations.tasks;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.measure.unit.Unit;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureStore;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import es.axios.geotools.util.FeatureUtil;
import es.axios.geotools.util.GeoToolsUtils;
import es.axios.udig.spatialoperations.internal.i18n.Messages;

/**
 * Implements the strategy to make the buffer based on the features's geometries
 * provided by the client.
 * <p>
 * The process take each {@link Feature} to create a new one that conforms to
 * the target FeatureType, and holds the buffered geometry.
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * 
 */
final class BufferTask extends AbstractSpatialOperationTask<SimpleFeatureStore> implements
			IBufferTask {

	protected static final Logger								LOGGER	= Logger.getLogger(BufferTask.class.getName());
	private FeatureCollection<SimpleFeatureType, SimpleFeature>	source;
	private Double												distance;
	private boolean												mergeGeometries;
	private Integer												quadrantSegments;
	private CoordinateReferenceSystem							sourceCrs;
	private CoordinateReferenceSystem							crs;
	private CoordinateReferenceSystem							targetCrs;

	// End cap style definition
	private CapStyle											endCapStyle;

	/**
	 * To create an instance must use
	 * {@link #createProcess(FeatureCollection, FeatureStore, CoordinateReferenceSystem, Double, Unit, boolean, Integer)}
	 */
	private BufferTask() {
	}

	/**
	 * Creates the buffer process implementation
	 * 
	 * @param source
	 * @param targetStore
	 * @param crs
	 * @param distance
	 * @param unitsOfMeasure
	 * @param mergeGeometries
	 * @param quadrantSegments
	 *            the number of segments used to approximate a quarter circle
	 * @param endCapStyle
	 *            the end cap style to use
	 * @param targetCRS2
	 * @param sourceCRS2
	 * 
	 * @return new instance of {@link BufferTask}
	 */
	public static IBufferTask createProcess(final FeatureCollection<SimpleFeatureType, SimpleFeature> source,
											final SimpleFeatureStore targetStore,
											final CoordinateReferenceSystem crs,
											Double distance,
											final Unit<?> unitsOfMeasure,
											final boolean mergeGeometries,
											final Integer quadrantSegments,
											final CapStyle endCapStyle,
											final CoordinateReferenceSystem sourceCRS,
											final CoordinateReferenceSystem targetCRS) {

		assert source != null;
		assert targetStore != null;
		assert crs != null;
		assert distance != null;
		assert unitsOfMeasure != null;
		assert mergeGeometries == true || mergeGeometries == false;
		assert (quadrantSegments != null) && (quadrantSegments.intValue() >= 1) : "Segments must be >= 1"; //$NON-NLS-1$

		BufferTask task = new BufferTask();

		task.source = source;
		task.targetStore = targetStore;
		task.distance = distance;
		task.mergeGeometries = mergeGeometries;
		task.quadrantSegments = quadrantSegments;
		task.endCapStyle = endCapStyle;

		task.crs = crs;
		task.targetCrs = targetCRS;
		task.sourceCrs = sourceCRS;

		return task;
	}

	/**
	 * Returns the {@link FeatureStore} with the buffer result
	 */
	@Override
	protected SimpleFeatureStore getResult() {

		return this.targetStore;
	}

	/**
	 * Takes each source feature and makes the buffer putting the result on
	 * target store
	 */
	@Override
	protected void perform() throws SpatialOperationException {

		final int quadSegments = this.quadrantSegments.intValue();
		final Double width = this.distance.doubleValue();

		SimpleFeature sourceFeature = null;
		Geometry mergedGeometry = null;
		FeatureIterator<SimpleFeature> iterator = null;

		try {
			iterator = this.source.features();
			while (iterator.hasNext()) {

				sourceFeature = iterator.next();

				Geometry geometry = (Geometry) sourceFeature.getDefaultGeometry();
				geometry = GeoToolsUtils.reproject(geometry, this.sourceCrs, this.crs);
				geometry = makeBufferGeometry(geometry, width, quadSegments, this.endCapStyle);
				geometry = GeoToolsUtils.reproject(geometry, this.crs, this.targetCrs);

				if (this.mergeGeometries) {
					if (mergedGeometry == null) {
						mergedGeometry = geometry;
					} else {
						mergedGeometry = mergedGeometry.union(geometry);
					}
					// TODO if the target layer has simple geometry and the
					// union result is Multi... this operation will haven't
					// effects
					// TODO Must be checked by the command
					// TODO Is needed a review of all scenario for this option,
					// now it produce only a feature for multipolygon.
					// First ie: multipoligons with intersects == false , I
					// think this option must produce merge features "only"
					// if its intersection is true
				} else {
					insertBufferedFeature(sourceFeature, geometry, this.targetStore);
				}
			}
			if (this.mergeGeometries) {
				insertBufferedFeature(mergedGeometry, this.targetStore);
			}

		} catch (OperationNotFoundException e) {
			final String message = MessageFormat.format(Messages.BufferProcess_failed_transforming, sourceFeature
						.getID(), e.getMessage());
			throw makeException(e, message);
		} catch (TransformException e) {
			String message = MessageFormat.format(Messages.BufferProcess_failed_transforming_feature_to_crs,
						sourceFeature.getID(), e.getMessage());
			throw makeException(e, message);
		} catch (Exception e) {

			throw makeException(e);
		} finally {

			if (iterator != null) {
				iterator.close();
			}

		}
	}

	/**
	 * Does the buffer for the source geometry
	 * 
	 * @param sourceGeometry
	 * @param distance
	 * @param quadrantSegments
	 *            the number of segments used to approximate a quarter circle
	 * @param endCapStyle
	 *            the end cap style to use
	 * @return the Buffer of geometry
	 */
	private Geometry makeBufferGeometry(final Geometry sourceGeometry,
										final double distance,
										final int quadrantSegments,
										final CapStyle endCapStyle) {

		Geometry bufferedGeometry = com.vividsolutions.jts.operation.buffer.BufferOp.bufferOp(sourceGeometry, distance,
					quadrantSegments, endCapStyle.value());

		return bufferedGeometry;
	}

	/**
	 * Creates the buffer feature. This process does not copy the property's
	 * values present in the original feature.
	 * 
	 * @param sourceFeature
	 *            feature containing source properties to match over
	 *            <code>target</code>, or <code>null</code>
	 * @param bufferedGeometry
	 * @param targetStore
	 * @throws SpatialOperationException
	 * 
	 */
	private void insertBufferedFeature(	final SimpleFeature sourceFeature,
										final Geometry bufferedGeometry,
										final FeatureStore<SimpleFeatureType, SimpleFeature> targetStore) 
		throws SpatialOperationException {

		Transaction transaction = targetStore.getTransaction();
		try {
			// copy the feature's property values
			final SimpleFeatureType targetType = targetStore.getSchema();
			SimpleFeature newFeature = FeatureUtil.createFeatureUsing(sourceFeature, targetType, bufferedGeometry);
			targetStore.addFeatures(DataUtilities.collection(new SimpleFeature[] { newFeature }));

			transaction.commit();

		} catch (DataSourceException de) {

			String msg = ""; //$NON-NLS-1$
			msg += "source ID: " + sourceFeature.getID() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			msg += "Source geom: " + sourceFeature + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			throw makeException(de, msg);

		} catch (Exception e) {
			try {
				targetStore.getTransaction().rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();

			final String msg = Messages.BufferProcess_adding_feature_to_store + ":" + e.getMessage(); //$NON-NLS-1$
			LOGGER.log(Level.SEVERE, msg);

		} finally {
			try {
				transaction.close();
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Creates and store a new feature using the buffer geometry.
	 * 
	 * @param bufferedGeometry
	 * @param targetStore
	 * @throws SpatialOperationException
	 */
	private void insertBufferedFeature(	final Geometry bufferedGeometry,
										final FeatureStore<SimpleFeatureType, SimpleFeature> targetStore)
		throws SpatialOperationException {

		Transaction transaction = targetStore.getTransaction();
		try {
			final SimpleFeatureType targetType = targetStore.getSchema();
			SimpleFeature newFeature = FeatureUtil.createFeatureWithGeometry(targetType, bufferedGeometry);
			targetStore.addFeatures(DataUtilities.collection(new SimpleFeature[] { newFeature }));

			transaction.commit();
		} catch (Exception e) {
			try {
				transaction.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();

			final String msg = Messages.BufferProcess_adding_feature_to_store + ":" + e.getMessage(); //$NON-NLS-1$
			throw makeException(e, msg);
		} finally {
			try {
				transaction.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw makeException(e);
			}
		}
	}

	@Override
	protected void featuresAdded(Envelope bounds) {

	}

	@Override
	protected void featuresChanged(Envelope bounds) {

	}

	@Override
	protected void addListeners() {

	}
}
