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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.signatures.CDKMoleculeSignatureAdapter;
import net.bioclipse.ds.signatures.prop.calc.AtomSignatures;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

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
                signatureString.add( gensign);
//                logger.debug("Sign for atom " + atomNr + ": " +gensign);
            }
        
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
                AtomSignatures s = generate( mol , height);
                if (s.getSignatures() !=null && s.getSignatures().size()>0)
                    retmap.put( mol, s );
            } catch ( BioclipseException e ) {
                logger.error( "Error generating signatures for molecule: " 
                              + mol + ".  Skipping this entry." );
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
        
    	Molecule signmol = CDKMoleculeSignatureAdapter.convert(cdkmol.getAtomContainer());

        MoleculeSignature signature = new MoleculeSignature(signmol);
        return signature.getMolecularSignature();

    }

}
