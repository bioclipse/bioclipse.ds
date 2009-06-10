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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

/**
 * An abstract base class for all test implementations.
 * @author ola
 *
 */
public abstract class AbstractDSTest implements IDSTest{

    private String id;
    private String name;
    private String description;
    private String icon;
    private String pluginID;
    private Map<String, String > parameters;
    private boolean excluded;
    private boolean informative;
    
    /**
     * Empty if no problems
     */
    private String testErrorMessage;
    
    public AbstractDSTest(){
        parameters=new HashMap<String, String>();
        excluded=false;
        testErrorMessage="";
    }
    
    public boolean isExcluded() {
        return excluded;
    }
    public void setExcluded( boolean excluded ) {
        this.excluded = excluded;
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
    
    
    public String getPluginID() {
    
        return pluginID;
    }

    
    public void setPluginID( String pluginID ) {
    
        this.pluginID = pluginID;
    }

    /**
     * Default toString is the name of the test
     */
    @Override
    public String toString() {
        return getName();
    }

    
    protected List<ITestResult> returnError(String name, String errorMessage) {

        ITestResult er=new SimpleResult(name, ITestResult.ERROR);
        er.setDetailedMessage( errorMessage );
        List<ITestResult> trlist=new ArrayList<ITestResult>();
        trlist.add( er );
        return trlist;
    }

    
    public IAtomContainer getAtomContainer() {
        return NoNotificationChemObjectBuilder.getInstance().
        newAtomContainer();
    }
    
    public java.awt.Color getHighlightingColor( IAtom atom ) {
        return java.awt.Color.YELLOW;
    }
    
    public Object getAdapter( Class adapter ) {
        return null;
    }

    
    public String getTestErrorMessage() {
    
        return testErrorMessage;
    }

    
    public void setTestErrorMessage( String testErrorMessage ) {
    
        this.testErrorMessage = testErrorMessage;
    }

    
    public String getDescription() {
    
        return description;
    }

    
    public void setDescription( String description ) {
    
        this.description = description;
    }

    
    public boolean isInformative() {
    
        return informative;
    }

    
    public void setInformative( boolean informative ) {
    
        this.informative = informative;
    }

}
