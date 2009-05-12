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
import net.bioclipse.ds.model.ISubstructureMatch;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.SubStructureMatch;
import net.bioclipse.ds.model.impl.DSException;

import org.junit.Test;


public class TestSignSIc {

    @Test
    public void runSmartsBursiInclexclTest() throws BioclipseException, DSException{

        IDSManager ds = Activator.getDefault().getManager();
        ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getCDKManager();

        IMolecule mol = cdk.fromSMILES( "C1CCCCC1CC(CCC)CCC" );
        List<ITestResult> ret = ds.runTest( "signsic.bursi", mol );
        assertNotNull( ret);

        System.out.println("=============================");
        System.out.println("Results:");
        for (ITestResult res : ret){
            System.out.println(res);
        }
        System.out.println("=============================");

        //Some QA
        assertTrue( ret.size()==1 );
        ITestResult testres = ret.get( 0 );
        
        assertTrue( testres instanceof ISubstructureMatch );
        ISubstructureMatch submatch=(ISubstructureMatch)testres;
        
        assertEquals( "HIT 1", testres.getName());
        assertTrue( submatch.getMatchingAtoms().contains( 1 ) );
        assertTrue( submatch.getMatchingAtoms().contains( 5 ) );
        assertTrue( submatch.getMatchingAtoms().contains( 8 ) );
        assertFalse( submatch.getMatchingAtoms().contains( 2 ) );

    }

}
