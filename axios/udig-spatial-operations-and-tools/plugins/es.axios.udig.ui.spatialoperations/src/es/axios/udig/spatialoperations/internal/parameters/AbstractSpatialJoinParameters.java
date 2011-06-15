package es.axios.udig.spatialoperations.internal.parameters;

import net.refractions.udig.project.ILayer;

import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import es.axios.udig.spatialoperations.tasks.SpatialRelation;

/**
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @author Alain Jimeno (www.axios.es)
 * @since 1.1.0
 */
abstract class AbstractSpatialJoinParameters implements ISpatialJoinGeomParameters {

	private ILayer						firstLayer			= null;
	private CoordinateReferenceSystem	firstCRS			= null;
	private ILayer						secondLayer			= null;
	private CoordinateReferenceSystem	secondCRS			= null;
	private SpatialRelation				relation			= null;
	private CoordinateReferenceSystem	mapCrs				= null;
	private Filter						filterInFirstLayer	= null;
	private Filter						filterInSecondLayer	= null;
	private Boolean						selection			= null;

	public AbstractSpatialJoinParameters(	final ILayer firstLayer,
											final CoordinateReferenceSystem firstCRS,
											final ILayer secondLayer,
											final CoordinateReferenceSystem secondCRS,
											final SpatialRelation spatialRelation,
											final CoordinateReferenceSystem mapCrs,
											final Filter filterInFirstLayer,
											final Filter filterInSecondLayer,
											final Boolean selection) {

		assert firstLayer != null;
		assert firstCRS != null;
		assert secondLayer != null;
		assert secondCRS != null;
		assert spatialRelation != null;
		assert mapCrs != null;
		assert filterInFirstLayer != null;
		assert filterInSecondLayer != null;
		assert selection != null;

		this.firstLayer = firstLayer;
		this.firstCRS = firstCRS;
		this.secondLayer = secondLayer;
		this.secondCRS = secondCRS;
		this.relation = spatialRelation;
		this.mapCrs = mapCrs;
		this.filterInFirstLayer = filterInFirstLayer;
		this.filterInSecondLayer = filterInSecondLayer;
		this.selection = selection;
	}

	public CoordinateReferenceSystem getMapCrs() {
		return this.mapCrs;
	}

	public ILayer getFirstLayer() {
		return this.firstLayer;
	}

	public CoordinateReferenceSystem getFirstCRS() {
		return this.firstCRS;
	}

	public ILayer getSecondLayer() {
		return this.secondLayer;
	}

	public CoordinateReferenceSystem getSecondCRS() {
		return this.secondCRS;
	}

	public SpatialRelation getSpatialRelation() {
		return this.relation;
	}

	public Filter getFilterInFirstLayer() {
		return filterInFirstLayer;
	}

	public Filter getFilterInSecondLayer() {
		return filterInSecondLayer;
	}

	public Boolean isSelection() {
		return this.selection;
	}
}
