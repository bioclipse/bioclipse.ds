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

import java.util.List;
import java.util.Map;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.managers.business.IBioclipseManager;


@PublishedClass( "Contains methods for Bioclipse Decision Support")
/**
 * Contains methods for Bioclipse Decision Support
 * 
 * @author ola
 *
 */
public interface IDSManager extends IBioclipseManager {

    @Recorded
    @PublishedMethod( 
                    methodSummary = "List available Decision Support Tests")
    public List<String> getTests() throws BioclipseException;

    public IDSTest getTest( String testID ) throws BioclipseException;

    @Recorded
    @PublishedMethod( 
                    methodSummary = "List available Decision Support Endpoints")
    public List<String> getEndpoints() throws BioclipseException;

    public Endpoint getEndpoint( String endpointID ) throws BioclipseException;
    
    /**
     * Run a test for a molecule, return list of matches
     * @param test
     * @param mol
     * @return
     * @throws BioclipseException 
     * @throws DSException 
     */
    @Recorded
    @PublishedMethod( 
                    params = "String testID, IMolecule mol,",
                    methodSummary = "Run a Decision Support Test on a Molecule")
    public List<ITestResult> runTest(String testID, IMolecule mol) 
                                         throws BioclipseException;

    public void runTest( String testID, IMolecule mol, 
                                         BioclipseUIJob<List<ITestResult>> job)
                                         throws BioclipseException;


    public BioclipseJob<List<ITestResult>> runTest(
                   String testID, 
                   IMolecule mol, 
                   BioclipseJobUpdateHook<List<ITestResult>> h)
                   throws BioclipseException;

    @Recorded
    @PublishedMethod( 
                  params = "String endpointID, IMolecule mol,",
                  methodSummary = "Run a Decision Support Test for an endpoint")
    public Map<String, List<? extends ITestResult>> runEndpoint(
                   String testID, 
                   IMolecule mol) 
                   throws BioclipseException;

    public void runEndpoint(
                   String testID, 
                   IMolecule mol, 
                   BioclipseUIJob<Map<String, List<? extends ITestResult>>> job)
                   throws BioclipseException;

    public BioclipseJob<Map<String, List<? extends ITestResult>>> runEndpoint(
                   String testID, 
                   IMolecule mol, 
                   BioclipseJobUpdateHook<Map<String, List<? extends ITestResult>>> h)
                   throws BioclipseException;
    
}
