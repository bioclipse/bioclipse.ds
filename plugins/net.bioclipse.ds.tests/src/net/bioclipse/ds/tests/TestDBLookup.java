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

import java.util.List;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.impl.DSException;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtom;


public class TestDBLookup {

    @Test
    public void runExactMatchTest() throws BioclipseException, DSException {

        IDSManager ds = Activator.getDefault().getManager();
        ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();

        ICDKMolecule mol = cdk.fromSMILES( "C1CCCCC1CC(CCC)CCC" );
        List<ITestResult> ret = ds.runTest( "dblookup.exact.bursi", mol );
        assertNotNull( ret);

        System.out.println("=============================");
        System.out.println("Results:");
        for (ITestResult res : ret){
            System.out.println(res);
        }
        System.out.println("=============================");

        
    }
    
    @Test
    public void runNearestNeighbourTest() throws BioclipseException, DSException {

        IDSManager ds = Activator.getDefault().getManager();
        ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();

        ICDKMolecule mol = cdk.fromSMILES( "C1CCCCC1CC(CCC)CCC" );
        List<ITestResult> ret = ds.runTest( "dblookup.nearest.bursi", mol );
        assertNotNull( ret);

        System.out.println("=============================");
        System.out.println("Results:");
        for (ITestResult res : ret){
            System.out.println(res);
        }
        System.out.println("=============================");

        
    }
}
