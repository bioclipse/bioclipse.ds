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
package net.bioclipse.ds.signatures.business;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.balloon.business.BalloonManager;
import net.bioclipse.balloon.business.IBalloonManager;
import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.signatures.CDKMoleculeSignatureAdapter;
//import net.bioclipse.ds.signatures.chiral.CalculateChiralSignatures;
import net.bioclipse.ds.signatures.prop.calc.AtomSignatures;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.openscience.cdk.exception.CDKException;

import signature.chemistry.Molecule;
import signature.chemistry.MoleculeReader;
import signature.chemistry.MoleculeSignature;

/**
 * 
 * @author ola
 */
public class SignaturesManager implements IBioclipseManager {

    private static final Logger logger = Logger.getLogger(
                                                       SignaturesManager.class);
    private static final int SIGNATURES_DEFAULT_HEIGHT = 1;

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "signatures";
    }

    /**
     * Use default height (1).
     * @param mol
     * @return
     * @throws BioclipseException
     */
    public AtomSignatures generate(IMolecule mol) 
    throws BioclipseException{
        return generate( mol, SIGNATURES_DEFAULT_HEIGHT );
    }


    public AtomSignatures generate(IMolecule mol, int height) 
    throws BioclipseException{

    	//Adapt IMolecule to ICDKMolecule
    	ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
    	ICDKMolecule cdkmol = cdk.asCDKMolecule(mol);

    	Molecule signmol = CDKMoleculeSignatureAdapter.convert(cdkmol.getAtomContainer());

    	List<String> signatureString=new ArrayList<String>();

    	MoleculeSignature signature = new MoleculeSignature(signmol);
    	for ( int atomNr = 0; atomNr < signmol.getAtomCount(); atomNr++){
    		String gensign=signature.signatureStringForVertex(atomNr, height);
    		if (gensign==null || gensign.isEmpty()){
    			logger.error("Produced null or empty " +
    					"signature for atom: " + atomNr 
    					+ " in molecule: " + cdkmol);
    			throw new BioclipseException("Produced null or empty " +
    					"signature for atom: " + atomNr 
    					+ " in molecule: " + cdkmol);
    		}

    		signatureString.add( gensign);
//    		logger.debug("Sign for atom " + atomNr + ": " +gensign);
    	}
    	
    	//Test correct number of produced signatures (should not be an issue)
    	if (signatureString.size()!=cdkmol.getAtomContainer().getAtomCount())
			throw new BioclipseException("Number of atoms and atom signatures " +
					"differ for molecule: " + cdkmol +" ; Number of atoms: " + 
					cdkmol.getAtomContainer().getAtomCount() + 
					" and number of signatures produced: " 
					+ signatureString.size());

    	return new AtomSignatures(signatureString);

    }

    /**
     * Generate Signatures for a list of molecules.
     * @param mols List of IMoleculs
     * @return list of Signatures
     */
    public Map<IMolecule, AtomSignatures> generate(List<? extends IMolecule> mols){
        return generate(mols, SIGNATURES_DEFAULT_HEIGHT);
    }

    /**
     * Generate AtomSignatures for a list of molecules.
     * @param mols List of IMoleculs
     * @param height Signatures height
     * @return Map from IMolecule to AtomSignatures
     */
    public Map<IMolecule, AtomSignatures> generate(List<? extends IMolecule> mols, 
                                                       int height){

        Map<IMolecule, AtomSignatures> retmap = 
                                   new HashMap<IMolecule, AtomSignatures>();
        
        
        for (IMolecule mol : mols){
            try {
                AtomSignatures as = generate( mol , height);
                
                if (as.getSignatures() == null || as.getSignatures().size()<=0)
                    logger.error( "No signatures generated for for molecule: " 
                            + mol + ".  Skipping this entry." );
                else
                	//All is well
                	retmap.put( mol, as );


            } catch ( BioclipseException e ) {
                logger.error( "Error generating signatures for molecule: " 
                              + mol + ".  Skipping this entry. " +
                              		"Reason: " + e.getMessage() );
            }

        }
    
        return retmap;
    }

    
    public Map<IMolecule, AtomSignatures> generate(IFile file) 
    throws BioclipseException, CoreException, IOException{
        return generate( file, SIGNATURES_DEFAULT_HEIGHT );
    }

    public Map<IMolecule, AtomSignatures> generate(IFile file, int height) 
    throws BioclipseException, CoreException, IOException{

    	//Adapt IMolecule to ICDKMolecule
        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        List<ICDKMolecule> mols = cdk.loadMolecules(file);

        return generate(mols, height);
    }

    
    public String generateMoleculeSignature( IMolecule mol )
        throws BioclipseException{
            
    	//Adapt IMolecule to ICDKMolecule
        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        ICDKMolecule cdkmol = cdk.asCDKMolecule(mol);
        
        cdkmol=cdk.perceiveAromaticity(cdkmol);
        try {
        	cdkmol=cdk.addImplicitHydrogens(cdkmol);
		} catch (InvocationTargetException e) {
			throw new BioclipseException("Error adding implicit hydrogens: " 
					+ e.getMessage());
		}
        
    	Molecule signmol = CDKMoleculeSignatureAdapter.convert(cdkmol.getAtomContainer());

        MoleculeSignature signature = new MoleculeSignature(signmol);
        return signature.getMolecularSignature();

    }
    
/*    
    public AtomSignatures generateChiral(IMolecule molecule, int height) 
    throws BioclipseException, CDKException{
    	
    	//Step 1: generate 3D with balloon
    	ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
    	if (!cdk.has3d(molecule)){
        	IBalloonManager ballon = net.bioclipse.balloon.business.Activator.getDefault().getJavaBalloonManager();
        	molecule = ballon.generate3Dcoordinates(molecule);
    	}
    	
    	List<String> signatures = CalculateChiralSignatures.generate(
    			cdk.getMDLMolfileString(molecule), height);
    	System.out.println("Chiral signs: " + signatures.toString());
    	
    	return new AtomSignatures(signatures);
    }
*/
    
}
