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

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IDSTest;

import org.junit.Test;

/**
 * 
 * @author ola
 *
 */
public class DSManagerPluginTest {

    @Test
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
        
//        Endpoint ep = ds.getEndpoint( "net.bioclipse.ds.mutagenicity" );
//        assertEquals( "Number of tests", 4, ep.getTests().size() );
        
    }

    @Test
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
        
        assertTrue( "Number of tests", ds.getTests().size() >=0 );
//        assertTrue( ds.getTests().contains( "bursi.sdflookup.exact" ) );
//        assertTrue( ds.getTests().contains( "bursi.sdflookup.nearest" ) );
//        assertTrue( ds.getTests().contains( "bursi.smarts" ) );
//        assertTrue( ds.getTests().contains( "bursi.consensus" ) );
        
    }
    

}
