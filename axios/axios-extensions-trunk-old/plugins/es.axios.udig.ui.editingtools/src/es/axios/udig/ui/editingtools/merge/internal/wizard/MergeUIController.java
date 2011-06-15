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

import javax.swing.text.TableView;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Controls the events produced in a {@link Tree} of source features to populate a {@link Table} of
 * a merged SimpleFeature, by using a {@link MergeFeatureBuilder} to create the merged feature.
 * <p>
 * The provided {@link TreeViewer} and {@link TableView} to serve as views for the source and target
 * features respectively are assumed to be empty. Once an instance of this class is created, the
 * {@link #initialize()} method needs to be called for this controller to populate the views with
 * the current state of the provided {@link MergeFeatureBuilder}.
 * </p>
 * <p>
 * This controller knows nothing about the nature of the elements (i.e. whether they're Features,
 * POJOS, etc) being built by the {@link MergeFeatureBuilder}, it restricts itself to operate over
 * the {@link MergeFeatureBuilder} interface.
 * </p>
 * 
 * @author Gabriel Roldan (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 * @since 1.1.0
 */
public class MergeUIController {
    /**
     * Index of the column holding the attribute names in both views
     */
    private static final int    NAME_COLUMN    = 0;
    /**
     * Index of the column holding the attribute values in both views
     */
    private static final int    VALUE_COLUMN   = 1;
    /**
     * Label to use as attribute value in the merged view when an attribue is {@code null}
     */
    private static final String NULL_LABEL     = "<null>";

    /**
     * The {@link TableItem}s data property in the targetFeature Table hold Integers representing
     * its corresponding attribute index inside a SimpleFeature.
     */
    private Table               targetFeature  = null;

    /**
     * The {@link TreeItem}s data property in the source features hold Integers representing the
     * index of the feaure as per {@link MergeFeatureBuilder#getFeatureCount()} for SimpleFeature items,
     * and the attribute index inside a SimpleFeature for Attribute items.
     */
    private Tree                sourceFeatures = null;

    private MergeFeatureBuilder builder;

    public MergeUIController( MergeFeatureBuilder builder, Tree sourceFeatures, Table targetFeature ) {
        this.builder = builder;
        this.sourceFeatures = sourceFeatures;
        this.targetFeature = targetFeature;
    }

    /**
     * Populates the source and target features views and registers the listeners to catch merge
     * feature change events, as well as to notify the {@link MergeFeatureBuilder} when a user event
     * in the source features view needs to update the builder's state
     */
    public void initialize() {

        // presents the source in tree view
        populateSourceFeaturesView();

        populateMergeFeatureView();

        sourceFeatures.addListener(SWT.Selection, new Listener(){
            public void handleEvent( Event event ) {
                if (event.detail == SWT.CHECK) {
                    handleTreeEvent(event);
                }
            }
        });

        builder.addChangeListener(new MergeFeatureBuilder.ChangeListener(){
            public void attributeChanged( MergeFeatureBuilder builder, int attributeIndex,
                                          Object oldValue ) {
                updateMergedFeatureView();
            }
        });
        updateMergedFeatureView();
    }

    /**
     * Callback function invoked every time a user interface event occurs over an item of the source
     * features view.
     * <p>
     * Applies the following logic:
     * <ul>
     * <li>If the TreeItem state change <code>event</code> was produced on a SimpleFeature item,
     * selects or deselects all the attributes of the corresponding feature
     * <li>If the TreeItem state change <code>event</code> was produced on an Attribute item,
     * that same item checked state will be respected, and all the attribute items at the same index
     * for the rest of the source features will become <b>unchecked</b>
     * <li>The internal {@link MergeFeatureBuilder} state will be updated accordingly, whether all
     * the attributes of a given feature has to be set for the target feature, or just the attribute
     * selected, depending on if the event were raised at a SimpleFeature item or an Attribute item
     * </ul>
     * </p>
     * <p>
     * No manipulation of the target feature view is done here. Instead, as this method calls
     * {@link #setAttributeValue(int, int, boolean)}, the {@link MergeFeatureBuilder} will raise
     * change events that will be catched up by {@link #updateMergedFeatureView()}
     * </p>
     * 
     * @param event
     * @see #setSelectedFeature(int, boolean)
     * @see #selectAttributePropagate(int, int, boolean)
     * @see #setAttributeValue(int, int, boolean)
     */
    private void handleTreeEvent( Event event ) {
        TreeItem item = (TreeItem) event.item;
        final boolean isFeatureItem = isFeatureItem(item);
        final boolean checked = item.getChecked();
        if (isFeatureItem) {
            int featureIndex = ((Integer) item.getData()).intValue();
            setSelectedFeature(featureIndex, checked);
        } else {
            TreeItem featureItem = item.getParentItem();
            int featureIndex = ((Integer) featureItem.getData()).intValue();
            int attributeIndex = ((Integer) item.getData()).intValue();
            selectAttributePropagate(featureIndex, attributeIndex, checked);
            setAttributeValue(featureIndex, attributeIndex, checked);
        }
    }

    /**
     * Called whenever a merged feature attribute value changed to update the merge feature view
     */
    private void updateMergedFeatureView() {
        final int attributeCount = builder.getAttributeCount();
        for( int attIndex = 0; attIndex < attributeCount; attIndex++ ) {
            TableItem attrItem = targetFeature.getItem(attIndex);
            Object attrValue = builder.getMergeAttribute(attIndex);
            String strValue = (attrValue == null)
                    ? NULL_LABEL
                    : String.valueOf(attrValue.toString());

            attrItem.setText(VALUE_COLUMN, strValue);
        }
    }

    /**
     * This is the single point where the {@link MergeFeatureBuilder} state is modified. This method
     * is called whenever a ui event in the source features view implies to change the value of an
     * attribute for the merge feature.
     * <p>
     * As a result of calling {@link MergeFeatureBuilder#setMergeAttribute(int, int)} or
     * {@link MergeFeatureBuilder#clearMergeAttribute(int)}, the change event will be catched up by
     * {@link #updateMergedFeatureView()} to reflect the change in the merge feature view
     * </p>
     * 
     * @param featureIndex the index of the source feature where the UI event event occurred
     * @param attributeIndex the index of the attribute of the source feature where the event
     *        occurred
     * @param setValue whether to set or clear the target feature attribute value at index
     *        <code>attributeIndex</code>
     * @see MergeFeatureBuilder#setMergeAttribute(int, int)
     * @see MergeFeatureBuilder#clearMergeAttribute(int)
     */
    private void setAttributeValue( int featureIndex, int attributeIndex, boolean setValue ) {
        if (setValue) {
            builder.setMergeAttribute(featureIndex, attributeIndex);
        } else {
            builder.clearMergeAttribute(attributeIndex);
        }
    }

    private void setSelectedFeature( final int featureIndex, final boolean checked ) {
        final int numFeatures = builder.getFeatureCount();
        final int numAttributes = builder.getAttributeCount();
        final TreeItem[] featureItems = sourceFeatures.getItems();

        assert numFeatures == featureItems.length;

        for( int currFeatureIdx = 0; currFeatureIdx < numFeatures; currFeatureIdx++ ) {
            TreeItem featureItem = featureItems[currFeatureIdx];
            final boolean checkIt = checked && currFeatureIdx == featureIndex;
            featureItem.setChecked(checkIt);

            for( int attIdx = 0; attIdx < numAttributes; attIdx++ ) {
                selectAttribute(currFeatureIdx, attIdx, checkIt);
                if (currFeatureIdx == featureIndex) {
                    setAttributeValue(featureIndex, attIdx, checkIt);
                }
            }
        }
    }

    /**
     * Simply selects or deselects an item in the source features view, does not make any state
     * change in the {@link MergeFeatureBuilder}
     * 
     * @param featureIndex the index of the feature item the desired attribute item belongs to
     * @param attributeIndex the index of the attribute to change the checked state
     * @param checked whether to check or uncheck the pointed attribute item
     */
    private void selectAttribute( final int featureIndex, final int attributeIndex,
                                  final boolean checked ) {
        TreeItem featureItem = sourceFeatures.getItem(featureIndex);
        TreeItem attItem = featureItem.getItem(attributeIndex);
        attItem.setChecked(checked);
    }

    /**
     * Selects a source feature attribute item in the source features view, propagating the contrary
     * effect to the TreeItems for the attributes of the other features at the same attribute index.
     * In other words, if selecting an attribute of one feature, deselects the same attribute of the
     * other features.
     * 
     * @param featureIndex
     * @param attributeIndex
     * @param checked
     * @see #selectAttribute(int, int, boolean)
     */
    private void selectAttributePropagate( final int featureIndex, final int attributeIndex,
                                           final boolean checked ) {
        final int numFeatures = builder.getFeatureCount();

        for( int currFIndex = 0; currFIndex < numFeatures; currFIndex++ ) {
            boolean checkIt = checked && currFIndex == featureIndex;
            selectAttribute(currFIndex, attributeIndex, checkIt);
        }
    }

    /**
     * Checks if <code>item</code> corresponds to the root item of a source feature (aka, it has
     * children)
     * 
     * @param item
     * @return <code>true</code> if item is the root item of a SimpleFeature, <code>false</code>
     *         otherwise
     */
    private boolean isFeatureItem( final TreeItem item ) {
        Object itemData = item.getData();
        boolean isFeatureItem = item.getItemCount() > 0;
        isFeatureItem = isFeatureItem && itemData instanceof Integer;
        return isFeatureItem;
    }

    /**
     * Populates the treeview with the source features
     * <p>
     * SimpleFeature item's {@link TreeItem#getData() data} are <code>Integer</code> values with the
     * corresponding feature index in the {@link MergeFeatureBuilder}. SimpleFeature's attribute data are
     * the attribute value as per {@link MergeFeatureBuilder#getAttribute(int, int)}
     * </p>
     */
    private void populateSourceFeaturesView() {
        final int featureCount = builder.getFeatureCount();
        // add feature as parent
        for( int featureIndex = 0; featureIndex < featureCount; featureIndex++ ) {
            TreeItem featureItem = new TreeItem(this.sourceFeatures, SWT.NONE);
            // store the
            featureItem.setData(Integer.valueOf(featureIndex));
            featureItem.setText(builder.getID(featureIndex));

            final int geometryIndex = builder.getDefaultGeometryIndex();
            boolean isFisrtFeature = featureIndex == 0;
            // adds feature's attribute as child items
            for( int attIndex = 0; attIndex < builder.getAttributeCount(); attIndex++ ) {

                TreeItem attrItem = new TreeItem(featureItem, SWT.NONE);

                // sets Name
                String attrName = builder.getAttributeName(attIndex);
                attrItem.setText(0, attrName);
                attrItem.setData(Integer.valueOf(attIndex));

                // sets value
                Object attrValue = builder.getAttribute(featureIndex, attIndex);
                String strValue = attrValue == null ? NULL_LABEL : String.valueOf(attrValue);
                attrItem.setText(VALUE_COLUMN, strValue);

                // check geometry only if it is not union and it is the first feature
                if (isFisrtFeature && attIndex == geometryIndex) {
                    attrItem.setChecked(!builder.mergeGeomIsUnion());
                } else {
                    attrItem.setChecked(isFisrtFeature);
                }

            }
            featureItem.setExpanded(isFisrtFeature);
        }
    }

    /**
     * Adds the target feature and its attributes. The merge feature view {@link TableItem}s value
     * property will hold integers representing the index of each attribute in the target feature's
     * schema
     */
    private void populateMergeFeatureView() {

        final int attributeCount = builder.getAttributeCount();
        for( int attIndex = 0; attIndex < attributeCount; attIndex++ ) {

            TableItem attrItem = new TableItem(targetFeature, SWT.NONE);
            attrItem.setData(Integer.valueOf(attIndex));
            String attrName = builder.getAttributeName(attIndex);
            attrItem.setText(NAME_COLUMN, attrName);
        }
    }
}
