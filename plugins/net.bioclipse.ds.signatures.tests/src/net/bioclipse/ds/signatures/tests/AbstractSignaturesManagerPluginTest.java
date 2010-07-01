/*******************************************************************************
 * Copyright (c) 2010  Ola Spjuth <ola@bioclipse.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.ds.signatures.tests;

import static org.junit.Assert.*;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.signatures.business.ISignaturesManager;
import net.bioclipse.ds.signatures.prop.calc.AtomSignatures;

import org.junit.Test;

/**
 * 
 * @author ola
 *
 */
public abstract class AbstractSignaturesManagerPluginTest {

    protected static ISignaturesManager managerNamespace;
    
    /**
     * Test generate signatures for a single molecule
     * 
     * @throws BioclipseException
     */
    @Test public void testDoSomething() throws BioclipseException {

        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        ICDKMolecule mol=cdk.fromSMILES( "C1CCCCC1CC(CC)" );
        AtomSignatures sp = managerNamespace.generate( mol );
        assertTrue( sp.getSignatures().size()==1 );
        assertEquals( "WEEE", sp );
        
    }

}
