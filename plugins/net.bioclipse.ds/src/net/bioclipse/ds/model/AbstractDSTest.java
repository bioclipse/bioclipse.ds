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

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.impl.result.ExternalMoleculeMatch;
import net.bioclipse.ds.impl.result.SimpleResult;
import net.bioclipse.ds.model.report.AbstractTestReportModel;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

/**
 * An abstract base class for all test implementations.
 * @author ola
 *
 */
public abstract class AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(AbstractDSTest.class);

    private String id;
    private String name;
    private String description;
    private String icon;
    private String pluginID;
    private Map<String, String > parameters;
    private boolean excluded;
    private boolean informative;
    private boolean clone;
    private boolean initialized;
    private boolean visible;
    private AbstractTestReportModel reportmodel;
    private Endpoint endpoint;
    
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

    
    public AbstractTestReportModel getReportmodel() {
    
        return reportmodel;
    }

    
    public void setReportmodel( AbstractTestReportModel reportmodel ) {
    
        this.reportmodel = reportmodel;
    }

    
    public boolean isClone() {
    
        return clone;
    }

    
    public void setClone( boolean clone ) {
    
        this.clone = clone;
    }

    public boolean isInitialized() {
        
        return initialized;
    }

    
    public void setInitialized( boolean initialized ) {
    
        this.initialized = initialized;
    }

    

    /**
     * Set up input molecule, clone if extension says so, and call doRunTest().
     */
    public List<? extends ITestResult> runWarningTest( IMolecule molecule, IProgressMonitor monitor ){

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        //Read database file if not already done that
        try {
            if (!isInitialized())
                initialize(monitor);
        } catch ( Exception e1 ) {
            logger.error( "Failed to initialize test: " + getId() + " due to: " 
                          + e1.getMessage() );
            setTestErrorMessage( "Failed to initialize: " + e1.getMessage() );
        }

        if (getTestErrorMessage().length()>1){
            return new ArrayList<ExternalMoleculeMatch>();
        }

        //Create CDKMolecule from the IMolecule to get a clean API
        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();
        ICDKMolecule cdkmol=null;
        try {
            cdkmol = cdk.asCDKMolecule( molecule );
        } catch ( BioclipseException e ) {
            return returnError( "Could not create CDKMolecule", e.getMessage() );
        }

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        return doRunTest( cdkmol, monitor );

    }


    protected abstract List<? extends ITestResult> doRunTest( 
                                                  ICDKMolecule cdkmol, 
                                                  IProgressMonitor monitor);

    public void setVisible( boolean visible ) {

        this.visible = visible;
    }

    public boolean isVisible() {

        return visible;
    }

    public void setEndpoint( Endpoint endpoint ) {

        this.endpoint = endpoint;
    }

    public Endpoint getEndpoint() {

        return endpoint;
    }

    
    
}
