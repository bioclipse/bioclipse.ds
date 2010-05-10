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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.signatures.prop.calc.Signatures;
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
    public Signatures generate(IMolecule mol) 
    throws BioclipseException{
        return generate( mol, SIGNATURES_DEFAULT_HEIGHT );
    }


    public Signatures generate(IMolecule mol, int height) 
    throws BioclipseException{
        
        //Serialize to SDF
        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        
        String mdlString=cdk.getMDLMolfileString( mol );
        mdlString=mdlString+"\n$$$$";
        ByteArrayInputStream b= new ByteArrayInputStream( mdlString.getBytes());
        
        List<Signatures> list = doGenerateFromSDFStream( b, height );
        if (list==null || list.size()<=0)
            throw new BioclipseException( "Signatures empty" );
        if (list.size()>1)
            throw new BioclipseException( "Signatures contained more than one "+
            		"result." );
        else
            return list.get( 0 );
    }

    /**
     * Generate Signatures for a list of molecules.
     * @param mols List of IMoleculs
     * @return list of Signatures
     */
    public Map<IMolecule, Signatures> generate(List<IMolecule> mols){

        Map<IMolecule, Signatures> retmap = 
                                   new HashMap<IMolecule, Signatures>();
        
        for (IMolecule mol : mols){
            try {
                Signatures s = generate( mol );
                if (s.getSignatures() !=null && s.getSignatures().size()>0)
                    retmap.put( mol, s );
            } catch ( BioclipseException e ) {
                logger.error( "Error generating signatures for molecule: " 
                              + mol + ".  Skipping this entry." );
            }
            
        }
    
        return retmap;
    }

    /**
     * Generate Signatures for a list of molecules.
     * @param mols List of IMoleculs
     * @param height Signatures height
     * @return list of Signatures
     */
    public Map<IMolecule, Signatures> generate(List<IMolecule> mols, 
                                                       int height){

        Map<IMolecule, Signatures> retmap = 
                                   new HashMap<IMolecule, Signatures>();
        
        for (IMolecule mol : mols){
            try {
                Signatures s = generate( mol , height);
                if (s.getSignatures() !=null && s.getSignatures().size()>0)
                    retmap.put( mol, s );
            } catch ( BioclipseException e ) {
                logger.error( "Error generating signatures for molecule: " 
                              + mol + ".  Skipping this entry." );
            }
            
        }
    
        return retmap;
    }

    
    public List<Signatures> generate(IFile file) 
    throws BioclipseException, CoreException{
        return generate( file, SIGNATURES_DEFAULT_HEIGHT );
    }

    public List<Signatures> generate(IFile file, int height) 
    throws BioclipseException, CoreException{
        return doGenerateFromSDFStream( file.getContents(), height);
    }


    
    
    /**
     * This is the actual Signature generation.
     * It accepts an SDF content as an InputStream.
     * 
     * @param inputstream SDF content as an InputStream.
     * @param height Signatures height
     * @return Map from molecule > property
     * @throws BioclipseException if reading or calculation failed
     */
    private List<Signatures> doGenerateFromSDFStream(
                                                        InputStream inputstream,
                                                       int height)
                                                     throws BioclipseException {

          List<Molecule> molecules = MoleculeReader.readSDFfromStream( 
                                                                  inputstream );

          List<Signatures> signaturesList = 
              new ArrayList<Signatures>();

          //Should be only one
          if (molecules==null || molecules.size()<=0)
              throw new BioclipseException( "Could not read any molecules " +
                  "from SDF inputstream" );

          //Loop over all molecules
          for (Molecule mol : molecules){
              List<String> signatureString=new ArrayList<String>();

              MoleculeSignature signature = new MoleculeSignature(mol);
              for ( int atomNr = 0; atomNr < mol.getAtomCount(); atomNr++){
                  String gensign=signature.signatureStringForVertex(atomNr, 
                                                    height);//.toCanonicalString();
                  signatureString.add( gensign);
//                  logger.debug("Sign for atom " + atomNr + ": " +gensign);
              }
              Signatures signprop = new Signatures(
                                                               signatureString);

              signaturesList.add( signprop );
          }
          
          return signaturesList;
          
//          Molecule molecule = molecules.get( 0 );
//          MoleculeSignature signature = new MoleculeSignature(molecule);
//          for ( int atomNr = 0; atomNr < molecule.getAtomCount(); atomNr++){
//              String gensign=signature.signatureStringForVertex(atomNr, 
//                                                  height).toCanonicalString();
//              signatureString.add( gensign);
////              logger.debug("Sign for atom " + atomNr + ": " +gensign);
//          }
//
//          return new SignaturesProperty(signatureString);
      }


}
