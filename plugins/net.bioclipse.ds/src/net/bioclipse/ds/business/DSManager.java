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
package net.bioclipse.ds.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;

/**
 * A Bioclipse Manager Decision Support
 * 
 * @author ola
 */
public class DSManager implements IBioclipseManager {

    private static final Logger logger =Logger.getLogger( DSManager.class );

    private DSBusinessModel dsBusinessModel; 
    
    public String getManagerName() {
        return "ds";
    }
    
    private void initialize() throws BioclipseException{

        if (dsBusinessModel==null){
            dsBusinessModel=new DSBusinessModel();
            dsBusinessModel.initialize();
        }
        if (dsBusinessModel==null)
            throw new BioclipseException("Error initializing DS model.");

    }

    /**
     * Get a list of all available tests
     * @return
     * @throws BioclipseException
     */
    public List<String> getTests() throws BioclipseException{

        initialize();
        
        List<String> testIDS=new ArrayList<String>();
        for (IDSTest test : dsBusinessModel.getTests()){
            testIDS.add( test.getId());
        }

        return testIDS;
    }


    public IDSTest getTest( String testID ) throws BioclipseException {

        if (testID==null)
            throw new BioclipseException(
                          "Test: " + testID + " must not be null." );
        
        initialize();

        for (IDSTest test : dsBusinessModel.getTests()){
            if (testID.equals( test.getId() ))
                return test;
        }

        logger.warn("Test: " + testID + " could not be found.");
        throw new BioclipseException(
                      "Test: " + testID + " could not be found." );
    }

    /**
     * Get a list of all available tests
     * @return
     * @throws BioclipseException
     */
    public List<String> getEndpoints() throws BioclipseException{

        initialize();
        
        List<String> epIDs=new ArrayList<String>();
        for (Endpoint ep : dsBusinessModel.getEndpoints()){
            epIDs.add( ep.getId());
        }
        return epIDs;
    }


    public Endpoint getEndpoint( String endpointID ) throws BioclipseException {

        if (endpointID==null)
            throw new BioclipseException(
                          "Endpoint: " + endpointID + " must not be null." );
        
        initialize();

        for (Endpoint ep : dsBusinessModel.getEndpoints()){
            if (endpointID.equals( ep.getId() ))
                return ep;
        }

        logger.warn("Endpoint: " + endpointID + " could not be found.");
        throw new BioclipseException(
                      "Endpoint: " + endpointID + " could not be found." );
    }

    
    public void runTest( String testID, IMolecule mol, 
                             IReturner<List<? extends ITestResult>> returner, 
                             IProgressMonitor monitor) 
                             throws BioclipseException{

        IDSTest test = getTest( testID );
        List<? extends ITestResult> ret = test.runWarningTest( mol, monitor);
        monitor.done();
        returner.completeReturn( ret );
    }

    public void runEndpoint( String endpointID, IMolecule mol, 
                   IReturner<Map<String, List<? extends ITestResult>>> returner, 
                   IProgressMonitor monitor) 
                   throws BioclipseException{

        Map<String, List<? extends ITestResult>> ret = 
                             new HashMap<String, List<? extends ITestResult>>();

        //Loop over all tests in this endpoint and run them
        for (IDSTest test : getEndpoint( endpointID ).getTests()){
            List<? extends ITestResult> testres = test.runWarningTest( mol, 
                                                                       monitor);
            ret.put( test.getId(), testres );
        }

        monitor.done();
        returner.completeReturn( ret );
}
        
}
