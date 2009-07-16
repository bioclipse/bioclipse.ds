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
package net.bioclipse.ds.tests;

import static org.junit.Assert.*;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.IDSTest;

import org.junit.Test;


public class DSManagerPluginTest {

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
        }
        System.out.println("=============================");
        
        assertEquals( "Number of tests", 4, ds.getTests().size() );
        assertTrue( ds.getTests().contains( "bursi.sdflookup.exact" ) );
        assertTrue( ds.getTests().contains( "bursi.sdflookup.nearest" ) );
        assertTrue( ds.getTests().contains( "bursi.smarts" ) );
        assertTrue( ds.getTests().contains( "bursi.consensus" ) );
        
    }


}
