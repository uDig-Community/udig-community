package es.axios.udig.spatialoperations.ui.common;

import net.refractions.udig.project.ILayer;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Layer Validator
 * 
 * 
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1
 */
public class LayerValidator implements ISOValidator {

	private InfoMessage	message	= null;
	private ILayer		layer	= null;

	public InfoMessage getMessage() {
		return this.message;
	}

	public final void setParameters(final ILayer layer) {

		this.layer = layer;
	}

	public final boolean validate() throws Exception {

		this.message = null;

		if (layer == null) {
			this.message = new InfoMessage(Messages.LayerValidator_layer_null, InfoMessage.Type.INFORMATION);
			return false;
		}
		return true;
	}

	/**
	 * Validate if the layer geometry type is Polygon or MultiPolygon.
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean validatePolygon() throws Exception {

		this.message = null;
		if (validate()) {

			Class<?> geomClass = layer.getSchema().getGeometryDescriptor().getType().getBinding();

			if ((geomClass.equals(Polygon.class) || geomClass.equals(MultiPolygon.class))) {
				return true;
			} else {

				this.message = new InfoMessage(Messages.LayerValidator_source_polygon, InfoMessage.Type.ERROR);
				return false;
			}
		}
		return false;
	}

}
