/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C) 2004,
 * Refractions Research Inc. This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package net.refractions.udig.wps.internal.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.refractions.udig.project.ui.internal.properties.FeaturePropertySource;
import net.refractions.udig.wps.internal.Messages;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.PropertySheetEntry;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.geotools.data.Parameter;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * An adaptor to let Parameters be placed in a property sheet for editing.
 * 
 * @author GDavis, Refractions Research
 * @since 1.1.0
 */
public class InputParamPropertySource implements IPropertySource2 {
    
    private Map<String, Parameter< ? >> parameterInfo;
    private Map<String, Object> paramValues;
    private Map<String, Object> originalParamValues;
    //IPropertyDescriptor[] descriptors;
    private Map<String, IPropertyDescriptor> descriptors;
    
    private final String keySuffix = "_LIST_";
    
    /** Are the attributes editable in cell editors (default) */ 
    private boolean editable = true;    
    
    public InputParamPropertySource(Map<String, Parameter< ? >> p, Map<String, Object> value) {
        init( p, value, editable);
    }

    public InputParamPropertySource( Map<String, Parameter< ? >> p, Map<String, Object> value, boolean edit ) {
        init( p, value, edit);
    }    

    private void init( Map<String, Parameter< ? >> p, Map<String, Object> value, boolean edit ) {
        this.parameterInfo = p;
        this.paramValues = value;
        this.originalParamValues = value;
        this.editable = edit;
    }

    public Object getEditableValue() {
        if (this.paramValues == null) {
            this.paramValues = new HashMap<String, Object>();
        }
        return this.paramValues;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (this.descriptors == null && this.parameterInfo == null) {
            return new PropertyDescriptor[]{new PropertyDescriptor("ID",Messages.WPSProcessView_noInputs)}; //$NON-NLS-1$
        }
        // if we don't have any descriptors yet, create the base ones
        if (this.descriptors == null) {
            this.descriptors = new HashMap<String, IPropertyDescriptor>();
            Iterator<Entry<String, Parameter< ? >>> iterator = this.parameterInfo.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Parameter< ? >> entry = iterator.next();
                Parameter< ? > param = entry.getValue();
                TextPropertyDescriptor tpd = null;
                int i=1;
                String key;
                // If this parameter supports more than 1 value, then
                // create a category for them.  Also, any param that
                // can have more than 1 value will have a key appended by
                // "_LIST_X" where X is a number of 1 or greater.  This needs to be
                // caught when searching for values based on their key.
                if (param.maxOccurs == -1 || param.maxOccurs > 1 || param.minOccurs > 1) {
                    key = new String(entry.getKey()+this.keySuffix+i);
                    tpd = new TextPropertyDescriptor(key, new String(param.title.toString()+" "+i)); //$NON-NLS-1$
                    tpd.setCategory(param.title.toString());
                }
                else {
                    key = entry.getKey();
                    tpd = new TextPropertyDescriptor(entry.getKey(),param.title.toString());
                }
                this.descriptors.put(key, tpd);
                // if this parameter has a min requirement that is more than 1, create
                // a property descriptor for each in the list of min
                while (i < param.minOccurs) {
                    i++;
                    key = new String(entry.getKey()+this.keySuffix+i);
                    tpd = new TextPropertyDescriptor(key, new String(param.title.toString()+" "+i)); //$NON-NLS-1$
                    tpd.setCategory(param.title.toString());
                    this.descriptors.put(key, tpd);
                }
            }
        }
        // return an array of descriptors
        return (IPropertyDescriptor[])this.descriptors.values().toArray(
                new IPropertyDescriptor[this.descriptors.size()]);

    }

    public Object getPropertyValue( Object id ) {
        if (this.parameterInfo == null) {
            return null;
        }
        // loop through each parameter and find the object id
        String idString = (String) id;
        String baseId = getBaseKey(idString);
        Iterator<Entry<String, Parameter< ? >>> iterator = this.parameterInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Parameter< ? >> entry = iterator.next();
            if (baseId.equals(entry.getKey())) {
                Parameter< ? > param = entry.getValue();
                return getParamValue(param, idString);
            }
        }
        return null;
    }
    
    /**
     * Because input param keys can be appended by "_LIST_X" where X is a number 1 or
     * greater, we need to be able to fetch the base key name.
     *
     * @param idString
     * @return base key name with the "_LIST_X" removed
     */
    private String getBaseKey( String idString ) {
        int indexOf = idString.indexOf(this.keySuffix);
        if (indexOf > -1 && indexOf > 0) {
            idString = idString.substring(0, indexOf);
        }
        return idString;
    }

    /**
     * Check if the given param has a value set, and return it
     *
     * @param param
     * @param key the key of the input value
     * @return the param's value, or null if there isn't one
     */
    private Object getParamValue( Parameter< ? > param, String key ) {
        if (param == null || this.paramValues == null) {
            return Messages.WPSPropertySource_clickToAdd;
        }        
        String baseId = getBaseKey(key);
        Iterator<Entry<String, Object>> iterator = this.paramValues.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            if (baseId.equals(entry.getKey())) {
                Object obj = entry.getValue();
                
                // if the value is a Map, get the obj from there with the
                // original key (not the base key)
                if (obj instanceof Map) {
                    obj = ((Map)obj).get(key);
                    if (obj == null) {
                        return Messages.WPSPropertySource_clickToAdd;
                    }
                    return getValue(obj);
                }
                return getValue(obj);
            }
        }
        return Messages.WPSPropertySource_clickToAdd;
    }   
    
    /**
     * Determine what value to return by the type of object given
     *
     * @param obj
     * @return value for editing
     */
    private Object getValue( Object obj ) {
        if (obj instanceof Geometry) {
            return geomToText((Geometry)obj);
            //return new GeomPropertySource((Geometry)obj);
        }
        else if (obj instanceof SimpleFeature) {
            return new FeaturePropertySource((SimpleFeature)obj);
        }
        else if (obj == null) {
            return null;
        }
        // default to string value
        return obj.toString();
    }

    private Object geomToText(Geometry geom) {
        WKTWriter writer = new WKTWriter();
        String text = writer.write(geom);
        text = text.replaceAll("[\\n\\r\\t]", " ");
        return text;
    }   

    public boolean isPropertySet( Object id ) {
        // TODO: check for the given property
        if (this.originalParamValues != null) {
            return true;
        }   
        return false;
    }

    public void resetPropertyValue( Object id ) {
       // TODO: reset just the given property 
       this.paramValues = this.originalParamValues;
    }

    public void setPropertyValue( Object id, Object value ) {
        // loop through each parameter and find the object id
        String idString = (String) id;
        String baseId = getBaseKey(idString);
        Iterator<Entry<String, Parameter< ? >>> iterator = this.parameterInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Parameter< ? >> entry = iterator.next();
            if (baseId.equals(entry.getKey())) {
                Parameter< ? > param = entry.getValue();
                setParamValue(param, value, idString);
            }
        }
    }

    /**
     * Set the given param's value in the corresponding value map
     *
     * @param param parameter to set
     * @param value to set 
     * @param key
     */
    private void setParamValue( Parameter< ? > param, Object value, String key ) {
        if (param == null) {
            return;
        }
        if (this.paramValues == null) {
            this.paramValues = new HashMap<String, Object>();
        }
        
        // if the param values already has a value for this key and it's a
        // map, update that map
        Map<String, Object> values = null;
        Object object = this.paramValues.get(param.key);
        if (object != null && object instanceof Map) {
            values = ((Map<String, Object>)object);
        }        
        else {
            values = new HashMap<String, Object>();
        }        
        
        // Our value is going to be a String since everything is using a
        // TextPropertySource, so figure out what sort of object to make from
        // it.
        try {
            if (param.type == Geometry.class) {
                WKTReader reader = new WKTReader( new GeometryFactory() );
                value = reader.read((String)value);
                values.put(key, value);
            }
            else if (param.type == Double.class) {
                value = new Double((String)value);
                values.put(key, value);
            }
            else if (param.type == Integer.class) {
                value = new Integer((String)value);
                values.put(key, value);
            }
        } catch (Exception e) {
            // invalid WKT/Double/Int, reset the text back and return
            return;
        }
        this.paramValues.put(param.key, values);
    }

    public boolean isPropertyResettable( Object id ) {
        return true;
    }
    
    public Map<String, Object> getParamValues() {
        return this.paramValues;
    }
    
    /**
     * Add a new descriptor based on the selection given
     *
     * @param currentSelection
     */
    public void addNewDescriptor(PropertySheetEntry currentSelection) {
        if (currentSelection == null) {
            return;
        }
        // Determine the info we need to add a new descriptor based on the given
        // selection object
        String category = currentSelection.getCategory();
        String displayName = currentSelection.getDisplayName();
        String lookupname = displayName;
        if (category != null && !category.equals("")) { //$NON-NLS-1$
            lookupname = category;
        }
        Parameter< ? > param = getParamFromName(lookupname);
        
        if (param != null) { 
            // figure out what is the next key that should be used
            String base = new String(param.key+this.keySuffix);
            int i = 1;
            String key = new String(base+i);
            while (this.descriptors.containsKey(key)) {
                i++;
                key = new String(base+i);
            }
            // add a new descriptor
            TextPropertyDescriptor tpd = new TextPropertyDescriptor(key, new String(param.title.toString()+" "+i)); //$NON-NLS-1$
            tpd.setCategory(category);
            this.descriptors.put(key, tpd);
        }
    }
    
    /**
     * Lookup the parameter from the given name
     *
     * @param lookupname
     * @return param that matches
     */
    public Parameter< ? > getParamFromName( String lookupname ) {
        if (lookupname == null) {
            return null;
        }
        Iterator<Entry<String, Parameter< ? >>> iterator = this.parameterInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Parameter< ? >> entry = iterator.next();
            Parameter< ? > param = entry.getValue();
            if (param.title.toString().equals(lookupname)) {
                return param;
            }
        }
        return null;
    }
    
    /**
     * Delete any descriptor and matching input value for the given display name
     *
     * @param lookupname
     * @return true if it was deleted, false if nothing was deleted
     */
    public boolean deleteFromDisplayName( String lookupname ) {
        if (lookupname == null) {
            return false;
        }
        Iterator<Entry<String, IPropertyDescriptor>> iterator = this.descriptors.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, IPropertyDescriptor> entry = iterator.next();
            IPropertyDescriptor descr = entry.getValue();
            if (descr.getDisplayName().equals(lookupname)) {
                // delete the descriptor by exact key name
                String key = entry.getKey();
                this.descriptors.remove(key);
                
                // find the input param from the category key name and delete
                // from its internal map by the exact name
                String baseKey = getBaseKey(key);
                if (baseKey != null && !baseKey.equals("")) { //$NON-NLS-1$
                    Object object = this.paramValues.get(baseKey);
                    if (object != null && object instanceof Map) {
                        Map<String, Object> values = (Map<String, Object>) object;
                        values.remove(key);
                    }
                }
                return true;
            }
        }
        return false;
    }    
    
    /**
     * Return the number of items already existing in the given category
     *
     * @param cat
     * @return number of items in cat
     */
    public int getCategoryCount(String cat) {
        if (cat == null || cat.equals("")) { //$NON-NLS-1$
            return 0;
        }
        Iterator<Entry<String, IPropertyDescriptor>> iterator = this.descriptors.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Entry<String, IPropertyDescriptor> next = iterator.next();
            IPropertyDescriptor descr = next.getValue();
            if (cat.equals(descr.getCategory())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Find the first property id (key) that matches the given display name
     *
     * @param displayName
     * @return first matching property id, or null if not found
     */
    public String getPropertyIdFromDisplayName( String displayName ) {
        if (displayName == null) {
            return null;
        }
        Iterator<Entry<String, IPropertyDescriptor>> iterator = this.descriptors.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, IPropertyDescriptor> entry = iterator.next();
            IPropertyDescriptor descr = entry.getValue();
            if (descr.getDisplayName().equals(displayName)) {
                return entry.getKey();    
            }
        }
        return null;
    }


}
