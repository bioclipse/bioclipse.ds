package net.bioclipse.ds.model;

import java.util.List;

/**
 * An endpoint in Decision Support.
 * @author ola
 *
 */
public class Endpoint {

    public Endpoint(String pid, String pname, String pdesc) {
        this(pid, pname);
        description=pdesc;
    }

    
    public String getDescription() {
    
        return description;
    }

    
    public void setDescription( String description ) {
    
        this.description = description;
    }

    
    public List<IDSTest> getTests() {
    
        return tests;
    }

    
    public void setTests( List<IDSTest> tests ) {
    
        this.tests = tests;
    }

    
    public String getId() {
    
        return id;
    }

    
    public String getName() {
    
        return name;
    }

    public Endpoint(String pid, String pname) {
        id=pid;
        name=pname;
    }

    String id;
    String name;
    String description;
    List<IDSTest> tests;
    
}
