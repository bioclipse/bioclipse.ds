/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.model;



public class TestEntry {
    
    private String id;
    private String name;
    private String icon;
    private IDSTest test;
    
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
    
    

}
