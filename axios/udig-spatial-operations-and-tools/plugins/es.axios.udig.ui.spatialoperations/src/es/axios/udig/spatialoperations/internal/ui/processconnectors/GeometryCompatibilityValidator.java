/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 * 		Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 * 		http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to license under Lesser General Public License (LGPL).
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
package es.axios.udig.spatialoperations.internal.ui.processconnectors;

import java.text.MessageFormat;
import java.util.List;

import net.refractions.udig.project.ILayer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

import es.axios.lib.geometry.util.GeometryUtil;
import es.axios.udig.spatialoperations.internal.i18n.Messages;
import es.axios.udig.spatialoperations.ui.common.ISOValidator;
import es.axios.udig.ui.commons.message.InfoMessage;

/**
 * Geometry Compatibility Validator
 * <p>
 * Valids the compatibility of the setted geometries
 * </p>
 * 
 * @author Mauricio Pazos (www.axios.es)
 * @author Aritz Davila (www.axios.es)
 * @since 1.1.0
 */
class GeometryCompatibilityValidator implements ISOValidator {

	private Class<? extends Geometry>		expectedGeometry		= null;
	private Class<? extends Geometry>		targetGeometry			= null;
	private InfoMessage						message					= null;
	private List<Class<? extends Geometry>>	expectedGeometryList	= null;

	public InfoMessage getMessage() {
		return this.message;
	}

	/**
	 * Used to validate when the expected geometry could be only one geometry
	 * type.
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean validate() throws Exception {

		this.message = InfoMessage.NULL;

		if (Geometry.class.equals(targetGeometry)) {
			return true;
		}

		if (expectedGeometry.equals(targetGeometry)) {
			return true;
		}

		// if it is multigeometry the expected geometry must have same
		// multigeometry
		if ((expectedGeometry.equals(MultiPolygon.class)) || (expectedGeometry.equals(MultiLineString.class))
					|| (expectedGeometry.equals(MultiPolygon.class))) {

			if (!expectedGeometry.equals(targetGeometry)) {

				String msg = MessageFormat.format(Messages.GeometryCompatibilityValidator_expected_geometry_type,
							expectedGeometry.getSimpleName());
				this.message = new InfoMessage(msg, InfoMessage.Type.ERROR);
				return false;
			}

			return true;
		}
		if (Geometry.class.equals(this.expectedGeometry)) {

			if (!Geometry.class.equals(this.targetGeometry)) {

				String msg = MessageFormat.format(Messages.GeometryCompatibilityValidator_expected_geometry_type,
							Geometry.class.getSimpleName());
				this.message = new InfoMessage(msg, InfoMessage.Type.ERROR);
				return false;
			}
		}
		// test simple vs mutigeometry compatibility
		if (isCompatibleGeometryCollection(expectedGeometry, targetGeometry)) {
			return true;
		}
		// it is not compatible geometry
		final String typeExpectedName = expectedGeometry.getSimpleName();
		String text = MessageFormat.format(Messages.IntersectCommand_expected_geometries, typeExpectedName);
		this.message = new InfoMessage(text, InfoMessage.Type.ERROR);

		return false;
	}

	/**
	 * Used to validate when the expected geometry could be 2 types of geometry.
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean validateBoth() throws Exception {

		this.message = InfoMessage.NULL;

		if (Geometry.class.equals(targetGeometry)) {
			return true;
		}

		Class<? extends Geometry> secondExpected = null;
		Class<? extends Geometry> thirdExpected = null;
		Class<? extends Geometry> fourExpected = null;

		secondExpected = expectedGeometryList.get(1);

		if (expectedGeometryList.size() >= 3) {
			thirdExpected = expectedGeometryList.get(2);
		}
		if (expectedGeometryList.size() >= 4) {
			fourExpected = expectedGeometryList.get(3);
		}

		if (expectedGeometry.equals(targetGeometry)
					|| (secondExpected != null && secondExpected.equals(targetGeometry))
					|| (thirdExpected != null && thirdExpected.equals(targetGeometry))
					|| (fourExpected != null && fourExpected.equals(targetGeometry))) {
			return true;
		}

		// if it is multigeometry the expected geometry must have same
		// multigeometry
		if ((expectedGeometry.equals(MultiPolygon.class)) || (expectedGeometry.equals(MultiLineString.class))) {

			if (!expectedGeometry.equals(targetGeometry) || !(secondExpected.equals(targetGeometry))
						|| !(thirdExpected.equals(targetGeometry))
						|| !(fourExpected != null && fourExpected.equals(targetGeometry))) {

				String msg = MessageFormat.format(Messages.GeometryCompatibilityValidator_expected_geometry_type,
							expectedGeometry.getSimpleName());
				msg = msg
							+ " " //$NON-NLS-1$
							+ MessageFormat.format(Messages.GeometryCompatibilityValidator_expected_geometry_type,
										secondExpected.getSimpleName());
				this.message = new InfoMessage(msg, InfoMessage.Type.ERROR);
				return false;
			}

			return true;
		}
		if (Geometry.class.equals(this.expectedGeometry) || Geometry.class.equals(this.expectedGeometryList)) {

			if (!Geometry.class.equals(this.targetGeometry)) {

				String msg = MessageFormat.format(Messages.GeometryCompatibilityValidator_expected_geometry_type,
							Geometry.class.getSimpleName());
				this.message = new InfoMessage(msg, InfoMessage.Type.ERROR);
				return false;
			}
		}
		// test simple vs mutigeometry compatibility
		if (isCompatibleGeometryCollection(expectedGeometry, targetGeometry)) {
			return true;
		}
		// test simple vs mutigeometry compatibility
		if (isCompatibleGeometryCollection(secondExpected, targetGeometry)) {
			return true;
		}
		// test simple vs mutigeometry compatibility
		if (isCompatibleGeometryCollection(thirdExpected, targetGeometry)) {
			return true;
		}

		// test simple vs mutigeometry compatibility
		if (isCompatibleGeometryCollection(fourExpected, targetGeometry)) {
			return true;
		}
		// it is not compatible geometry
		final String typeExpectedName = expectedGeometry.getSimpleName() + "&" + secondExpected.getSimpleName(); //$NON-NLS-1$
		String text = MessageFormat.format(Messages.IntersectCommand_expected_geometries, typeExpectedName);
		this.message = new InfoMessage(text, InfoMessage.Type.ERROR);

		return false;
	}

	/**
	 * Analysis if simple Geometry has a correspondent multygeometry
	 * 
	 * @param simpleGeometry
	 * @param targetGeometry
	 * @return true if simpleGeometry is compatible to target Geometry
	 */
	private boolean isCompatibleGeometryCollection(Class<? extends Geometry> simpleGeometry, Class<?> targetGeometry) {

		Class<?> compatible = GeometryUtil.getCompatibleCollection(simpleGeometry);

		boolean result = compatible.equals(targetGeometry);

		return result;
	}

	/**
	 * 
	 * @param expectedGeometry
	 */
	public void setExpected(Class<? extends Geometry> expectedGeometry) {
		this.expectedGeometry = expectedGeometry;
	}

	/**
	 * The second expected geometry.
	 * 
	 * @param expectedGeometry
	 */
	public void setExpectedList(List<Class<? extends Geometry>> expectedGeometry) {
		this.expectedGeometryList = expectedGeometry;
	}

	/**
	 * 
	 * @param targetGeometry
	 */
	public void setTarget(Class<? extends Geometry> targetGeometry) {

		this.targetGeometry = targetGeometry;
	}

	public void setParameters(ILayer layer) {
		// TODO Auto-generated method stub

	}

}
