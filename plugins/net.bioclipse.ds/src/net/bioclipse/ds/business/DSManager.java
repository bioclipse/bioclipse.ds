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
import java.util.List;

import org.apache.log4j.Logger;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestHelper;
import net.bioclipse.ds.model.impl.DSException;

/**
 * A Bioclipse Manager Decision Support
 * 
 * @author ola
 */
public class DSManager implements IDSManager {

    private static final Logger logger =Logger.getLogger( DSManager.class );

    private volatile List<IDSTest> tests; 
    
    /**
     * Defines the Bioclipse namespace for DS.
     * Appears in the scripting language as the namespace/prefix
     */
    public String getNamespace() {
        return "ds";
    }

    public List<String> getTests() throws BioclipseException{

        if (tests==null)
            tests = TestHelper.readTestsFromEP();
        if (tests==null)
            throw new BioclipseException("No existing tests available.");
        
        List<String> testIDS=new ArrayList<String>();
        for (IDSTest test : tests){
            testIDS.add( test.getId());
        }

        return testIDS;
    }


    public IDSTest getTest( String testID ) throws BioclipseException {

        if (testID==null)
            throw new BioclipseException(
                          "Test: " + testID + " must not be null." );
        
        if (tests==null)
            tests = TestHelper.readTestsFromEP();
        if (tests==null)
            throw new BioclipseException("No existing tests available.");

        for (IDSTest test : tests){
            if (testID.equals( test.getId() ))
                return test;
        }

        logger.debug("Test: " + testID + " could not be found.");
        throw new BioclipseException(
                      "Test: " + testID + " could not be found." );
    }
 
    
    public List<ITestResult> runTest( String testID, IMolecule mol ) 
                             throws BioclipseException, DSException {
        IDSTest test = getTest( testID );
        return test.runWarningTest( mol);
    }
}
