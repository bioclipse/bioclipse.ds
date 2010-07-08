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
import net.bioclipse.ds.signatures.CDKMoleculeSignatureAdapter;
import net.bioclipse.ds.signatures.business.ISignaturesManager;
import net.bioclipse.ds.signatures.prop.calc.AtomSignatures;

import org.junit.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import signature.chemistry.MoleculeSignature;

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
    
    
    @Test
    public void benzene() {
       IAtomContainer benzene = new AtomContainer();
       benzene.addAtom(new Atom("C"));
       benzene.addAtom(new Atom("C"));
       benzene.addAtom(new Atom("C"));
       benzene.addAtom(new Atom("C"));
       benzene.addAtom(new Atom("C"));
       benzene.addAtom(new Atom("C"));
       benzene.addBond(0, 1, IBond.Order.SINGLE);
       benzene.addBond(1, 2, IBond.Order.SINGLE);
       benzene.addBond(2, 3, IBond.Order.SINGLE);
       benzene.addBond(3, 4, IBond.Order.SINGLE);
       benzene.addBond(4, 5, IBond.Order.SINGLE);
       benzene.addBond(5, 0, IBond.Order.SINGLE);
       for (IBond bond : benzene.bonds()) {
           bond.setFlag(CDKConstants.ISAROMATIC, true);
       }
       
       signature.chemistry.Molecule mol =
           CDKMoleculeSignatureAdapter.convert(benzene);
       MoleculeSignature molSig = new MoleculeSignature(mol);
       System.out.println(molSig.toCanonicalString());
    }



}
