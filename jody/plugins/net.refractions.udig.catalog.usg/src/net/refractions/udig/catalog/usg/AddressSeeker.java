/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
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
package net.refractions.udig.catalog.usg;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientException;
import org.apache.xmlrpc.XmlRpcException;
import org.geotools.data.store.AbstractDataStore2;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.SchemaException;

/**
 *
 * @author James
 */
public class AddressSeeker {
    
    // protected String host = "http://geocoder.us/service/xmlrpc";
    protected String username = "";
    protected String password = "";
    
    static FeatureType ADDRESS = createAddressType();
    
    /** Creates a new instance of USGCDataStore */
    public AddressSeeker() {
        
    }
    
    public void setPassword(String pwd){
        password = pwd;
    }
    
    static private FeatureType createAddressType() {
        AttributeTypeFactory fac = AttributeTypeFactory.defaultInstance();
        AttributeType[] types = new AttributeType[9];
        types[0] = fac.newAttributeType("number", Number.class);
        types[1] = fac.newAttributeType("prefix", String.class);
        types[2] = fac.newAttributeType("type", String.class);
        types[3] = fac.newAttributeType("street", String.class);
        types[4] = fac.newAttributeType("suffix", String.class);
        types[5] = fac.newAttributeType("city", String.class);
        types[6] = fac.newAttributeType("state", String.class);
        types[7] = fac.newAttributeType("zip", String.class);
        types[8] = fac.newAttributeType("location", Point.class);
        FeatureType type;
        try {
            type = FeatureTypeBuilder.newFeatureType(types, "Address");
            FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance("Thingy");
            builder.importType(type);
            return  builder.newFeatureType(new AttributeType[]{}, "Address");          
        } catch (Throwable e) {
            return null;
        }
    }
    
    public void setUsername(String uname){
        username = uname;
    }
    
    /** Returns a List<Feature> of ADDRESS */
    public Point where(String address) throws IOException,XmlRpcException {
        GeometryFactory fac = new GeometryFactory();
        
        XmlRpcClient geocoder;
        if(username != null && password != null){
            geocoder = new XmlRpcClient("http://"+username+":"+password+"@geocoder.us/service/xmlrpc");
        }else{
            geocoder = new XmlRpcClient("http://geocoder.us/service/xmlrpc");
        }
        
        Vector params = new Vector();
        params.addElement(address);
        // this method returns a string
        Vector vec = (Vector)geocoder.execute("geocode", params);
        System.out.println("vec"+vec);
        
        Hashtable table = (Hashtable)vec.get(0);
        double lat = ((Number)table.get("lat")).doubleValue();
        double lon = ((Number)table.get("long")).doubleValue();
        Coordinate c = new Coordinate(lat, lon);
        Point p = fac.createPoint(c);
        return p;
    }
    
    public List<Feature> geocode(String address) throws IOException,XmlRpcException {
        GeometryFactory fac = new GeometryFactory();
        
        XmlRpcClient geocoder;
        if(username != null && password != null){
            geocoder = new XmlRpcClient("http://"+username+":"+password+"@geocoder.us/service/xmlrpc");
        }else{
            geocoder = new XmlRpcClient("http://geocoder.us/service/xmlrpc");
        }
        Vector params = new Vector();
        params.addElement(address);
        
        // this method returns a string
        Vector<Hashtable> vec = (Vector<Hashtable>)geocoder.execute("geocode", params);
        System.out.println("vec"+vec);

        List<Feature> places = new ArrayList<Feature>( vec.size() );

        for( Hashtable table : vec ){
            Number number = ((Number)table.get("number")).intValue();
            String prefix = (String)table.get("prefix");
            String street = (String)table.get("street");
            String type = (String)table.get("type");
            String state = (String)table.get("state");
            String city = (String)table.get("city");
            
            double lat = ((Number)table.get("lat")).doubleValue();
            double lon = ((Number)table.get("long")).doubleValue();
            Coordinate c = new Coordinate(lat, lon);
            Point p = fac.createPoint(c);
            try{
                Feature f = ADDRESS.create(new Object[]{number,prefix,street,type,state,city,p});
                places.add( f );
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return places;
    }
}
