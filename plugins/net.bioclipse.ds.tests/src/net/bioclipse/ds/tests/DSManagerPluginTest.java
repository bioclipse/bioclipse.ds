/* *****************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.tests;

import static org.junit.Assert.*;

import java.util.List;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.api.domain.IMolecule;
import net.bioclipse.core.api.domain.SMILESMolecule;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.report.StatusHelper;

import org.junit.Test;

/**
 * 
 * @author ola
 *
 */
public class DSManagerPluginTest {

//    @Test
    public void testListEndpoints() throws BioclipseException{

        IDSManager ds = Activator.getDefault().getJavaManager();

        System.out.println("=============================");
        System.out.println("Available Endpoints:");
        for (String epID : ds.getEndpoints()){
            System.out.println("   -" + epID);
            Endpoint ep = ds.getEndpoint( epID );
            assertNotNull( ep );
            assertNotNull( ep.getId());
            assertNotNull( ep.getName());
            assertNotNull( ep.getTests());
            assertNotNull( ep.getConsensusCalculator() );
        }
        System.out.println("=============================");
        
        Endpoint ep = ds.getEndpoint( "net.bioclipse.ds.mutagenicity" );
        assertEquals( "Number of tests", 4, ep.getTests().size() );
        
    }

//    @Test
    public void testListTests() throws BioclipseException{

        IDSManager ds = Activator.getDefault().getJavaManager();

        System.out.println("=============================");
        System.out.println("Available tests:");
        for (String testID : ds.getTests()){
            System.out.println("   -" + testID);
            IDSTest test = ds.getTest( testID );
            assertNotNull( test );
            assertNotNull( test.getId());
            assertNotNull( test.getName());
            assertNotNull( test.getEndpoint());
            assertNotNull( test.getConsensusCalculator() );
        }
        System.out.println("=============================");
        
        assertTrue( "Number of tests", ds.getTests().size() >=4 );
        assertTrue( ds.getTests().contains( "bursi.sdflookup.exact" ) );
        assertTrue( ds.getTests().contains( "bursi.sdflookup.nearest" ) );
        assertTrue( ds.getTests().contains( "bursi.smarts" ) );
        assertTrue( ds.getTests().contains( "bursi.consensus" ) );
        
    }
    
    @Test
    public void testBursiExactMatches() throws BioclipseException{

        IDSManager ds = Activator.getDefault().getJavaManager();

        IMolecule exactHit=new SMILESMolecule("O=C(N(O)C=1C=CC(=CC=1)C)C");
        IMolecule noExactHit=new SMILESMolecule("CCCCCCNCCCNCO");

        //Should hit
        List<ITestResult> results = ds.runTest( "bursi.sdflookup.exact", exactHit );
        assertNotNull( results );
        assertTrue( results.size()>0 );
        for (ITestResult result : results){
            System.out.println("Found exact result: " 
                               + result.getName() +" - " + StatusHelper.statusToString( result.getClassification()));
        }

        //No hits
        results = ds.runTest( "bursi.sdflookup.exact", noExactHit );
        assertNotNull( results );
        assertFalse( results.size()>0 );
        for (ITestResult result : results){
            System.out.println("Found exact result: " 
                               + result.getName() +" - " + StatusHelper.statusToString( result.getClassification()));
        }

        
    }
    
    @Test
    public void testBursiNearestNaighbor() throws BioclipseException{

        IDSManager ds = Activator.getDefault().getJavaManager();

        //should give 0.72 or so
        IMolecule nearestHit=new SMILESMolecule("CC(=O)N(O)C4=CC=C1C=CC=2C=CC=C3C=CC4(=C1C=23)");

        //should give less than 0.7 or so
        IMolecule noNearestHit=new SMILESMolecule("CCCCCCNCCCNCO");

        //Should hit
        List<ITestResult> results = ds.runTest( "bursi.sdflookup.nearest", nearestHit );
        assertNotNull( results );
        assertTrue( results.size()>0 );
        for (ITestResult result : results){
            System.out.println("Found nearest result: " 
                               + result.getName() +" - " + StatusHelper.statusToString( result.getClassification()));
        }

        //No hits
        results = ds.runTest( "bursi.sdflookup.nearest", noNearestHit );
        assertNotNull( results );
        assertFalse( results.size()>0 );
        for (ITestResult result : results){
            System.out.println("Found nearest result: " 
                               + result.getName() +" - " + StatusHelper.statusToString( result.getClassification()));
        }

        
    }


}
