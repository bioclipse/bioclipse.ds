package net.bioclipse.ds.model;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractWarningTest implements IDSTest{

    private String id;
    private String name;
    private String icon;
    private IDSTest test;
    private String pluginID;
    private Map<String, String > parameters;
    
    
    public AbstractWarningTest() {
        parameters=new HashMap<String, String>();
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    public void setParameters( Map<String, String> parameters ) {
        this.parameters = parameters;
    }
    
    public void addParameter( String name, String value ) {
        parameters.put( name, value );
    }

    public String getId() {
    
        return id;
    }
    
    public void setId( String id ) {
    
        this.id = id;
    }
    
    public String getName() {
    
        return name;
    }
    
    public void setName( String name ) {
    
        this.name = name;
    }
    
    public String getIcon() {
    
        return icon;
    }
    
    public void setIcon( String icon ) {
    
        this.icon = icon;
    }
    
    public IDSTest getTest() {
    
        return test;
    }
    
    public void setTest( IDSTest test ) {
    
        this.test = test;
    }

    
    public String getPluginID() {
    
        return pluginID;
    }

    
    public void setPluginID( String pluginID ) {
    
        this.pluginID = pluginID;
    }
    
    
    
}
