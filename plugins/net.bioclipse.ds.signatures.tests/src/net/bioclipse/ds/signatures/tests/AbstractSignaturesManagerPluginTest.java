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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.signatures.business.ISignaturesManager;
import net.bioclipse.ds.signatures.prop.calc.AtomSignatures;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.junit.Test;

/**
 * 
 * @author ola
 *
 */
public abstract class AbstractSignaturesManagerPluginTest {

	protected static ISignaturesManager managerNamespace;

	/**
	 * Test generate atom signatures and molecular signatures
	 * for a bunch of molecules with expected values stored as
	 * properties in an SDFile
	 * 
	 * @throws BioclipseException
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws CoreException 
	 */
	@Test
	public void testAtomSignature() throws BioclipseException, URISyntaxException, MalformedURLException, IOException, CoreException {

		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();

		URI uri = getClass().getResource("/testdata/3drugs_sign.sdf").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path = url.getFile();
        List<ICDKMolecule> mols = cdk.loadMolecules( path);
        for (ICDKMolecule mol : mols){
        	System.out.println("Testing mol: " + mol.getName());
        	testSignaturesAgainstProperties(mol);
        }
	}


	private void testSignaturesAgainstProperties(ICDKMolecule mol) throws BioclipseException {
		//Get the properties
    	String ap0=(String) mol.getAtomContainer().getProperty("Atom Signatures height 0");
    	assertNotNull(ap0);
    	String ap1=(String) mol.getAtomContainer().getProperty("Atom Signatures height 1");
    	assertNotNull(ap1);
    	String ap2=(String) mol.getAtomContainer().getProperty("Atom Signatures height 2");
    	assertNotNull(ap2);
    	String ap3=(String) mol.getAtomContainer().getProperty("Atom Signatures height 3");
    	assertNotNull(ap3);
    	String ap4=(String) mol.getAtomContainer().getProperty("Atom Signatures height 4");
    	assertNotNull(ap4);
    	String ap5=(String) mol.getAtomContainer().getProperty("Atom Signatures height 5");
    	assertNotNull(ap5);
    	String mp=(String) mol.getAtomContainer().getProperty("Molecular Signature");
    	assertNotNull(mp);

    	//Calculate Signatures
		AtomSignatures as = managerNamespace.generate( mol );
		assertEquals(ap1, as.toString());
		AtomSignatures as0 = managerNamespace.generate( mol,0 );
		assertEquals(ap0, as0.toString());
		AtomSignatures as1 = managerNamespace.generate( mol,1 );
		assertEquals(ap1, as1.toString());
		AtomSignatures as2 = managerNamespace.generate( mol,2 );
		assertEquals(ap2, as2.toString());
		AtomSignatures as3 = managerNamespace.generate( mol,3 );
		assertEquals(ap3, as3.toString());
		AtomSignatures as4 = managerNamespace.generate( mol,4 );
		assertEquals(ap4, as4.toString());
		AtomSignatures as5 = managerNamespace.generate( mol,5 );
		assertEquals(ap5, as5.toString());
		String ms = managerNamespace.generateMoleculeSignature(mol);
		assertEquals(mp, ms);
    	
	}



}
