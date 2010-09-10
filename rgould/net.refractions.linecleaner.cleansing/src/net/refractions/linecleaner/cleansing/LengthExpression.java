package net.refractions.linecleaner.cleansing;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterVisitor;

/**
 * Returns the length of the provided Feature.
 * 
 * @author rgould
 */
public class LengthExpression implements Expression {

	private IProgressMonitor monitor;

	public LengthExpression(IProgressMonitor monitor) {
		if (monitor == null) monitor = new NullProgressMonitor();
		this.monitor = monitor;
	}

	public short getType() {
		return Expression.LITERAL_DOUBLE;
	}

	public Object getValue(Feature feature) {
		return feature.getDefaultGeometry().getLength();
	}

	public void accept(FilterVisitor visitor) {
		monitor.worked(1);
		visitor.visit(this);
	}
}
