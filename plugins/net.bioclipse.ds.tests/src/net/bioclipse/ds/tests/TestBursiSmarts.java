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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.impl.DSException;

import org.junit.Test;


public class TestBursiSmarts {

    @Test
    public void testListTests() throws BioclipseException{

        IDSManager ds = Activator.getDefault().getManager();

        System.out.println("=============================");
        System.out.println("Available tests:");
        for (String test : ds.getTests()){
            System.out.println("   -" + test);
        }
        System.out.println("=============================");
        assertEquals( "Number of tests", 4, ds.getTests().size() );

    }

    @Test
    public void testGetTests() throws BioclipseException{

        IDSManager ds = Activator.getDefault().getManager();
        
        //Get an existing test
        IDSTest t = ds.getTest("smarts.sample");
        assertNotNull( t );
        
        assertNotNull( t.getId());
        assertNotNull( t.getName());

        //Get a non-existing test
        try{
            t = ds.getTest("NONEXISTING");
            fail("Found a test that should not be found");
        }catch (BioclipseException e){}
        catch (UndeclaredThrowableException e){}

        //Get a non-existing test
        try{
            t = ds.getTest(null);
            fail("Found a test that should not be found");
        }catch (BioclipseException e){}
        catch (UndeclaredThrowableException e){}

    }
    @Test
    public void testeadSmartsFiles() throws BioclipseException, DSException{

        //TODO: read and count smarts from files
        
    }
    
    @Test
    public void runSmartsSampleTest() throws BioclipseException, DSException{

        IDSManager ds = Activator.getDefault().getManager();
        ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
        
        IMolecule mol = cdk.fromSMILES( "C1CCCCC1CCN(CC)CC" );
        List<ITestResult> ret = ds.runTest( "smarts.sample", mol );
        
        assertNotNull( ret);
        assertEquals( 1, ret.size() ); //One match expected
        
        System.out.println("=============================");
        System.out.println("Results:");
        for (ITestResult res : ret){
            System.out.println(res);
        }

        System.out.println("=============================");

    }

    @Test
    public void runSmartsBursiTest() throws BioclipseException, DSException{

        IDSManager ds = Activator.getDefault().getManager();
        ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
        
        IMolecule mol = cdk.fromSMILES( "O=C1CCO1CC" );
        List<ITestResult> ret = ds.runTest( "smarts.bursi", mol );
        
        assertNotNull( ret);
        
        System.out.println("=============================");
        System.out.println("Results:");
        for (ITestResult res : ret){
            System.out.println(res);
        }

        System.out.println("=============================");

    }

    @Test
    public void runSmartsBursiInclexclTest() throws BioclipseException, DSException{

        IDSManager ds = Activator.getDefault().getManager();
        ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();
        
        IMolecule mol = cdk.fromSMILES( "O=C1CCO1CC" );
        try{
        List<ITestResult> ret = ds.runTest( "smarts.bursi.inclexcl", mol );
        assertNotNull( ret);
        
        System.out.println("=============================");
        System.out.println("Results:");
        for (ITestResult res : ret){
            System.out.println(res);
        }

        System.out.println("=============================");
        }catch (UndeclaredThrowableException e){
//            int a=0;
        }

    }

}
