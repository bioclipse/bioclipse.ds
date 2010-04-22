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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;

import signature.chemistry.Molecule;
import signature.chemistry.MoleculeReader;
import signature.chemistry.MoleculeSignature;

/**
 * 
 * @author ola
 */
public class SignaturesManager implements IBioclipseManager {

    private static final Logger logger = Logger.getLogger(SignaturesManager.class);
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
    public List<String> generate(IMolecule mol) 
    throws BioclipseException{
        return generate( mol, SIGNATURES_DEFAULT_HEIGHT );
    }


    public List<String> generate(IMolecule mol, int height) throws BioclipseException{
        
        List<String> signatures=new ArrayList<String>();
        
        //Serialize to SDF
        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        
        String mdlString=cdk.getMDLMolfileString( mol );
        mdlString=mdlString+"\n$$$$";
        ByteArrayInputStream b= new ByteArrayInputStream( mdlString.getBytes() );
        
        List<Molecule> molecules = MoleculeReader.readSDFfromStream( b );

        //Should be only one
        if (molecules==null || molecules.size()<=0)
            throw new BioclipseException( "Could not read any molecules " +
            		"from SDF stream" );
        
        Molecule molecule = molecules.get( 0 );
        MoleculeSignature signature = new MoleculeSignature(molecule);
        for ( int atomNr = 0; atomNr < molecule.getAtomCount(); atomNr++){
            String gensign=signature.signatureStringForVertex(atomNr, 
                                                    height).toCanonicalString();
            signatures.add( gensign);
            logger.debug("Sign for atom " + atomNr + ": " +gensign);
        }

        return signatures;
    }

    /**
     * Generate Signatures for a list of molecules.
     * @param mols List of IMoleculs
     * @return list of Signatures serialized as Strings
     */
    public Map<IMolecule, List<String>> generate(List<IMolecule> mols){

        Map<IMolecule, List<String>> retmap = 
                                         new HashMap<IMolecule, List<String>>();
        
        for (IMolecule mol : mols){
            try {
                List<String> s = generate( mol );
                if (s!=null && s.size()>0)
                    retmap.put( mol, s );
            } catch ( BioclipseException e ) {
                logger.error( "Error generating signatures for molecule: " 
                              + mol + ".  Skipping this entry." );
            }
            
        }
    
        return retmap;
    }


}
