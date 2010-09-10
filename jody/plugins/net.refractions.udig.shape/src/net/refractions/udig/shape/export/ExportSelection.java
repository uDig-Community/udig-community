package net.refractions.udig.shape.export;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.project.internal.Layer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.filter.Filter;

/**
 * This is my own version of the ExportToShapefile opperation
 * from the uDig tutorials.
 * <p>
 * I am moving in the direction of setting a generatic Export
 * system that can export to any DataStoreFactory that
 * supports createSchema.
 * </p>
 * @author Jody Garnett
 * @since 1.0.0
 */
public class ExportSelection extends ExportTo {

    @Override
    public boolean canExport( Object target ) {
        Layer layer=(Layer) target;
        if( Filter.ALL == layer.getFilter() ){
            status( "Nothing is selected" );
            return false; // nothing selected!
        }
        try {
            FeatureSource source = (FeatureSource) target;//layer.getAdapter( FeatureSource.class/*, null */);
            FeatureType schema = source.getSchema();
            int count=0;
            for( AttributeType attribute : schema.getAttributeTypes() ){
                if( attribute.getName().length() > 17 ) {
                    status( attribute.getName()+" is too long for shapefile");
                    return false;
                }
                if( attribute instanceof GeometryAttributeType ){
                    count++;
                }
            }
            if( count != 1 ) {
                status( "Shape file only supports a single geometry attribute" );
                return false;
            }
        }
        catch (Throwable t ){
            status( "Unable to export: "+t );
            return false;
        }
        return true;
    }
    
    @Override
    public String defaultName( Object target ) {
        Layer layer=(Layer) target;
        String filename = layer.getName();
        
        filename = filename.replace(':', '_');
        return filename+" selected";
    }
    @Override
    public String prompt( Object target ) {       
        return "Export Selection to Shapefile";         
    }
    @Override
    public String[] getExtentions() {
        return new String[]{ "shp", };
    }
    @Override
    public String[] getFilterNames() {
        return new String[]{ "Shapefile", };
    }
    
    /** Fix url for ShapefileDataStore */
    URL toURL( File file ) throws MalformedURLException{
        URL url = file.toURL();
        String spec = url.toExternalForm();
        spec = spec.substring( 0, spec.lastIndexOf('.'));
        return new URL( spec );        
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void exportTo( Object target, File file, IProgressMonitor monitor ) throws Exception {
        Layer layer=(Layer) target;        
        FeatureSource source = layer.getAdapter( FeatureSource.class, null );
                
        URL url = toURL( file );
        
        // create a new shapfile data store
        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        ShapefileDataStore newShapeFile = (ShapefileDataStore) factory.createDataStore( url );
        
        // create a schema in the new datastore
        newShapeFile.createSchema( source.getSchema() );

        FeatureStore store = (FeatureStore) newShapeFile.getFeatureSource();                
               
        store.addFeatures( source.getFeatures( layer.getFilter() ).reader() );        
    }
}
