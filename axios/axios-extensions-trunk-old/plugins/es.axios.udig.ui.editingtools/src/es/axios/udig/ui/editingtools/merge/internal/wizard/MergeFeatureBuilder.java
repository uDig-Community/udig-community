/* uDig-Spatial Operations plugins
 * http://b5m.gipuzkoa.net
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.ui.editingtools.merge.internal.wizard;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.type.Types;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A builder for a {@link SimpleFeature} that allows to select which attributes from a set of source
 * features the new SimpleFeature is going to be composed of.
 * <p>
 * The merge feature is initialized with the values of the first feature found in the provided
 * {@link FeatureCollection}. If a precomputed merged geometry is provided, the default geometry in
 * the merged feature will hold that value, otherwise it'll hold the geometry from the first
 * SimpleFeature.
 * </p>
 * <p>
 * Sample usage:
 * 
 * <pre><code>
 * FeatureCollection<SimpleFeatureType, SimpleFeature> features = &lt;get desired source features&gt;...
 * Geometry mergedGeometry = &lt;get unioned geometry from features&gt;...
 * MergeFeatureBuilder builder = new MergeFeatureBuilder(features, union);
 * //set the first attribute of the merge feature to be the first attribute
 * //of the third source feature
 * int featureIndex = 2;
 * int attributeIndex = 0;
 * builder.setMergeAttribute(featureIndex, attributeIndex);
 * .... repeat as desired
 * //set the 3th attribute of the merge feature to null
 * builder.clearMergeAttribute(2);
 * 
 * // check if the geometry of the merge feature is the 
 * // unioned one provided at the constructor
 * boolean isGeomUnion = builder.mergeGeomIsUnion();
 * 
 * // set all the attributes of the merge feature to be the ones
 * // of the last source feature
 * int fcount = builder.getFeatureCount();
 * int attCount = builder.getAttributeCount();
 * int fIndex = fount - 1;
 * for(int attIndex = 0; attIndex &lt; attCount; attIndex++){
 *  builder.setMergeAttribute(fIndex, attIndex);
 * }
 * 
 * //get the resulting SimpleFeature
 * try{
 *  SimpleFeature mergedFeature = builder.buildMergeFeature();
 * }catch(IllegalAttributeException e){
 *  //got an invalid attribute (may be a non nillable one got null?)
 *  LOGGER.log(Level.WARNING, &quot;Failed to create merge feature&quot;, e);
 * }
 * </code></pre>
 * 
 * </p>
 * <p>
 * Among being able to set the merge geometry attributes from the indexes of the source features and
 * their attributes, this class also allows client code to register a {@link ChangeListener} to be
 * notified every time an attribute for the merge feature is changed.
 * </p>
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class MergeFeatureBuilder {

    /**
     * Null mergeGeometryValue to check for equality with a feature's default geometry when both the
     * provided union geom and the assigned attribute are null. see {@link #mergeGeomIsUnion()}
     */
    private static final Object NULL_GEOM = new Object();

    private EventListenerList   listeners = new EventListenerList();

    private SimpleFeatureType         fType;

    private List<SimpleFeature>       sourceFeatures;

    private Object              mergedGeometry;

    private Object[]            mergedFeature;

    private int                 defaultGeometryIndex;

    FeatureFactory              factory = null;
    /**
     * Creates a MergeFeatureBuilder that works over the provided features and unioned geometry
     * <p>
     * Preconditions:
     * <ul>
     * <li><code>sourceFeatures.size() > 0</code>
     * <li><code>forall SimpleFeature f1,f2 in sourceFeatures: f1.getSchema() == f2.getSchema()</code>
     * <li><code>sourceFeatures.getSchema().getDefaultGeometry() != null</code>
     * <li><code>if (mergedGeometry != null) sourceFeatures.getSchema().getDefaultGeometry().getType().isAssignableFrom(medgedGeometry.getClass()) == true 
     * </ul>
     * </p>
     * 
     * @param sourceFeatures non null collection containing non null elements, of features with the same schema from whose members to
     *        select the merged feature attributes
     * @param mergedGeometry the Geometry to use as the default geometry for the merge feature, or
     *        <code>null</code>
     */
    public MergeFeatureBuilder( SimpleFeature[] sourceFeatures, Geometry mergedGeometry ) {
        assert sourceFeatures != null;
        this.mergedGeometry = mergedGeometry;
        if (mergedGeometry == null) {
            this.mergedGeometry = NULL_GEOM;
        }
        if (sourceFeatures.length < 1) {
            throw new IllegalArgumentException("Expected at least one source feature");
        }
        this.sourceFeatures = new ArrayList<SimpleFeature>(sourceFeatures.length);
        this.fType = sourceFeatures[0].getFeatureType();

        for( int i = 0; i < sourceFeatures.length; i++ ) {
            SimpleFeature next = sourceFeatures[i];
            if (fType != next.getFeatureType()) {
                throw new IllegalArgumentException("Features in the collection must "
                        + "conform to a common schema");
            }
            this.sourceFeatures.add(next);
        }

        SimpleFeature feature = this.sourceFeatures.get(0);
        GeometryDescriptor defaultGeometry = fType.getDefaultGeometry();
        if (null == defaultGeometry) {
            throw new IllegalArgumentException("SimpleFeature schema contains no default geometry");
        }
        if (mergedGeometry != null) {
        	Class binding = defaultGeometry.getType().getBinding();
        	
        	if (!binding.isAssignableFrom(mergedGeometry.getClass())) {
                throw new IllegalArgumentException("Can't assign "
                        + mergedGeometry.getClass().getName() + " to "
                        + defaultGeometry.getClass().getName());
            }
        }        
        this.defaultGeometryIndex = fType.indexOf( defaultGeometry.getName() );

        mergedFeature = new Object[fType.getAttributeCount()];
        mergedFeature = feature.getAttributes().toArray(mergedFeature);

        if (mergedGeometry != null) {
            mergedFeature[defaultGeometryIndex] = mergedGeometry;
        }
    }

    /**
     * Event listener interface for implementors to listen to merge attribute changes
     * 
     * @author Gabriel Roldan (www.axios.es)
     * @author Mauricio Pazos (www.axios.es)
     * @since 1.1.0
     * @see MergeFeatureBuilder#addChangeListener(es.axios.udig.ui.editingtools.merge.internal.wizard.MergeFeatureBuilder.ChangeListener)
     * @see MergeFeatureBuilder#removeChangeListener(es.axios.udig.ui.editingtools.merge.internal.wizard.MergeFeatureBuilder.ChangeListener)
     */
    public static interface ChangeListener extends EventListener {
        /**
         * Called when an attribute value for the merged feature changes
         * 
         * @param builder the {@link MergeFeatureBuilder} where the change occurred
         * @param attributeIndex the index of the attribute changed
         * @param oldValue the previous value of the attribute changed
         */
        public void attributeChanged( MergeFeatureBuilder builder, int attributeIndex,
                                      Object oldValue );
    }

    /**
     * Adds a listener for changes in the target feature attribute values
     * 
     * @see #setMergeAttribute(int, int)
     * @see #clearMergeAttribute(int)
     */
    public void addChangeListener( ChangeListener listener ) {
        assert listener != null;
        listeners.add(ChangeListener.class, listener);
    }

    /**
     * Removes a listener for changes in the target feature attribute values
     */
    public void removeChangeListener( ChangeListener listener ) {
        listeners.remove(ChangeListener.class, listener);
    }

    /**
     * Ask the MergeFeatureBuilder to use the provided FeatureFactory.
     * <p>
     * Currently this is used in test cases to force failure; in general
     * it is used to configure the kind of feature class produced from the
     * merge.
     *
     * @param factory
     */
    public void setFactory( FeatureFactory factory ) {
        this.factory = factory;
    }
    
    /**
     * Builds the merged {@link SimpleFeature} from the internal state of this builder
     * 
     * @return a new {@link SimpleFeature} of the same {@link SimpleFeatureType} than the provided source
     *         features, built with the attribute values established through this builders methods
     * @throws IllegalStateException if it is not possible to create the SimpleFeature with the current
     *         list of attribute values
     */
    public SimpleFeature buildMergedFeature() throws IllegalStateException {
        SimpleFeature feature;
        try {
            // feature = SimpleFeatureBuilder.build(fType, mergedFeature, null );
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder( fType );
            if( factory != null ){
                builder.setFeatureFactory( factory );
            }
            builder.addAll( mergedFeature );
            feature = builder.buildFeature( null ); // no id?
        	
        } catch (IllegalAttributeException e) {
            throw new IllegalStateException("Can't create merged feature: " + e.getMessage(), e);
        }
        return feature;
    }

    /**
     * @return the original geometry passed as constructor argument to be used as the merge geometry
     */
    public Geometry getMergedGeometry() {
        return mergedGeometry == NULL_GEOM ? null : (Geometry) mergedGeometry;
    }

    /**
     * @return the attribute index of the default geometry
     */
    public int getDefaultGeometryIndex() {
        return defaultGeometryIndex;
    }

    /**
     * @return whether the geometry currently being used as the default geometry for the target
     *         feature is the same than the one provided in the constructor to be used as the
     *         geometry merge result of all the source features
     */
    public boolean mergeGeomIsUnion() {
        int defaultGeometryIndex = getDefaultGeometryIndex();
        boolean geomAttIsMerged = mergedGeometry == mergedFeature[defaultGeometryIndex];
        return geomAttIsMerged;
    }

    /**
     * @param attIndex the index of an attribute in the builder's {@link SimpleFeatureType}
     * @return the name of the attribute at index <code>attIndex</code>
     */
    public String getAttributeName( int attIndex ) {
        assert attIndex < getAttributeCount();
        AttributeDescriptor attributeType = fType.getAttribute(attIndex);
        return attributeType.getLocalName();
    }

    /**
     * @param attributeIndex the index of the merge feature attribute to return
     * @return the current attribute value for the merge feature at index
     *         <code>attributeIndex</code>
     */
    public Object getMergeAttribute( int attributeIndex ) {
        assert attributeIndex < getAttributeCount();
        Object attribute = mergedFeature[attributeIndex];
        return attribute;
    }

    /**
     * Sets the value of the attribute at index {@code attritbuteIndex} in the merged feature to be
     * the value of the {@code attritbuteIndex} attribute in the {@code featureIndex} source
     * feature, and fires a change event to be catched by the registered {@link ChangeListener}
     * 
     * @param featureIndex
     * @param attributeIndex index of the attribute at feature {@code featureIndex} index
     * @throws IllegalArgumentException
     * @see {@link #addChangeListener(es.axios.udig.ui.editingtools.merge.internal.wizard.MergeFeatureBuilder.ChangeListener)}
     */
    public void setMergeAttribute( int featureIndex, int attributeIndex ) throws IllegalArgumentException {
        assert featureIndex < getFeatureCount();
        assert attributeIndex < getAttributeCount();

        Object value = getAttribute(featureIndex, attributeIndex);
        setMergeValue(attributeIndex, value);
    }

    /**
     * Clears the value of the merge feature's attribute at index {@code attributeIndex}, and fires
     * a change event to be catched by the registered {@link ChangeListener}
     * <p>
     * If {@code attributeIndex == #getDefaultGeometryIndex()}, sets the geometry value of the
     * merged feature to {@link #getMergedGeometry()}
     * </p>
     * 
     * @param attributeIndex the attribute index of the target feature to be cleared
     * @see {@link #addChangeListener(es.axios.udig.ui.editingtools.merge.internal.wizard.MergeFeatureBuilder.ChangeListener)}
     */
    public void clearMergeAttribute( int attributeIndex ) {
        assert attributeIndex < getAttributeCount();
        Object value = null;
        if (attributeIndex == getDefaultGeometryIndex()) {
            value = getMergedGeometry();
        }
        setMergeValue(attributeIndex, value);
    }

    /**
     * Returns the attribute value at index <code>attributeIndex</code>of the source feature at
     * index <code>featureIndex</code>
     * 
     * @param featureIndex the index of the feature to retrieve the attribute from
     * @param attributeIndex the index of the attribute to retrieve from the feature at index
     *        <coed>featureIndex</code>
     * @return the attribute value at index <code>attributeIndex</code>of the source feature at
     *         index <code>featureIndex</code>
     */
    public Object getAttribute( int featureIndex, int attributeIndex ) {
        assert featureIndex < getFeatureCount();
        assert attributeIndex < getAttributeCount();

        SimpleFeature feature = getFeature(featureIndex);
        Object attribute = feature.getAttribute(attributeIndex);
        return attribute;
    }

    /**
     * @return the number of source features available to select target feature attributes from
     */
    public int getFeatureCount() {
        return sourceFeatures.size();
    }

    /**
     * @return the number of attributes defined in the shared {@link SimpleFeatureType} for the source
     *         features and the one to be created
     */
    public int getAttributeCount() {
        return fType.getAttributeCount();
    }

    /**
     * Returns the feature ID of the source feature at index <code>featureIndex</code>
     * 
     * @param featureIndex
     * @return
     */
    public String getID( int featureIndex ) {
        assert featureIndex < getFeatureCount();
        SimpleFeature feature = getFeature(featureIndex);
        return feature.getID();
    }

    /**
     * Sets the attribute value at index <code>attributeIndex</code> of the target feature to be
     * <code>value</code> and fires a change event to be catched by the registered
     * {@link ChangeListener}
     * 
     * @param attributeIndex
     * @param value
     */
    private void setMergeValue( int attributeIndex, Object value ) {
        Object oldValue = getMergeAttribute(attributeIndex);
        mergedFeature[attributeIndex] = value;
        fireChangedEvent(attributeIndex, oldValue);
    }

    private void fireChangedEvent( final int attributeIndex, Object oldValue ) {
        for( ChangeListener listener : listeners.getListeners(ChangeListener.class) ) {
            listener.attributeChanged(this, attributeIndex, oldValue);
        }
    }

    private SimpleFeature getFeature( int featureIndex ) {
        SimpleFeature feature = sourceFeatures.get(featureIndex);
        return feature;
    }
}
