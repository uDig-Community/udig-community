package sld.editor.codegen;

import java.util.*;
import org.jdom.*;

public class EditPartTemplate
{
  protected static String nl;
  public static synchronized EditPartTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    EditPartTemplate result = new EditPartTemplate();
    nl = null;
    return result;
  }

  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "";
  protected final String TEXT_2 = NL + "/*" + NL + "*/" + NL + "package net.refractions.udig.sld.editor.internal.ui;" + NL + "" + NL + "import java.util.Collections;" + NL + "import java.util.List;" + NL + "" + NL + "import org.eclipse.draw2d.IFigure;" + NL + "import org.eclipse.draw2d.geometry.Rectangle;" + NL + "import org.eclipse.gef.GraphicalEditPart;" + NL + "import org.eclipse.gef.editparts.AbstractGraphicalEditPart;" + NL + "" + NL + "public class ";
  protected final String TEXT_3 = "EditPart extends AbstractGraphicalEditPart  {" + NL + "" + NL + "    protected IFigure createFigure() {       " + NL + "        return new SLDFigure(";
  protected final String TEXT_4 = ",new Rectangle(10,10, 300,300));" + NL + "    }" + NL + "" + NL + "    @Override" + NL + "    protected void createEditPolicies() {" + NL + "    }" + NL + "" + NL + "    protected void refreshVisuals() {" + NL + "        Rectangle rectangle = new Rectangle(5,150, 300,250);" + NL + "        " + NL + "        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);" + NL + "    }" + NL + "" + NL + "    @Override" + NL + "    public List getModelChildren() {" + NL + "        return Collections.singletonList(new Integer(5));" + NL + "    }" + NL + "" + NL + "    " + NL + "}" + NL;
  protected final String TEXT_5 = NL;

  public String generate(Object argument)
  {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
    
	Element element = (Element) argument;

    stringBuffer.append(TEXT_2);
    stringBuffer.append( element.getName() );
    stringBuffer.append(TEXT_3);
    stringBuffer.append( element.getName() );
    stringBuffer.append(TEXT_4);
    stringBuffer.append(TEXT_5);
    return stringBuffer.toString();
  }
}
