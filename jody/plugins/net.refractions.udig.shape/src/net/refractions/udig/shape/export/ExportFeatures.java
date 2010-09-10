package net.refractions.udig.shape.export;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;

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
public class ExportFeatures extends ExportTo {

    @Override
    public boolean canExport( Object target ) {
        try {
            FeatureSource source = (FeatureSource) target;
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
        FeatureSource source = (FeatureSource) target;

        String filename = source.getSchema().getTypeName();        
        filename = filename.replace(':', '_');
        return filename;
    }
    @Override
    public String prompt( Object target ) {
        FeatureSource source = (FeatureSource) target;
        FeatureType schema = source.getSchema();
        String typeName = schema.getTypeName();
       
        return "Export "+typeName+" to Shapefile";         
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
        FeatureSource source = (FeatureSource) target;
        
        URL url = toURL( file );
        
        // create a new shapfile data store
        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        ShapefileDataStore newShapeFile = (ShapefileDataStore) factory.createDataStore( url );
        
        // create a schema in the new datastore
        newShapeFile.createSchema( source.getSchema() );

        FeatureStore store = (FeatureStore) newShapeFile.getFeatureSource();                
               
        store.addFeatures( source.getFeatures().reader() );        
    }
}
