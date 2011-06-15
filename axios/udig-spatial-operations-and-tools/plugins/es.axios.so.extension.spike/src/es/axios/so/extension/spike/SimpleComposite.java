package es.axios.so.extension.spike;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

public class SimpleComposite extends Composite {

	private Button checkBox = null;
	private Button radioButton = null;
	private Button button = null;
	private Label label = null;
	private Label label1 = null;

	public SimpleComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	private void initialize() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		label1 = new Label(this, SWT.NONE);
		label1.setText("Label");
		checkBox = new Button(this, SWT.CHECK);
		Label filler2 = new Label(this, SWT.NONE);
		label = new Label(this, SWT.NONE);
		label.setText("Label");
		radioButton = new Button(this, SWT.RADIO);
		button = new Button(this, SWT.NONE);
		this.setLayout(gridLayout);
		setSize(new Point(300, 200));
	}

}
