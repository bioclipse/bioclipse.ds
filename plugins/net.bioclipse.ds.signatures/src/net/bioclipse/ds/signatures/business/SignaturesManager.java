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
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
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
        
        //Serialize to SDF
        ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
        
        String mdlString=cdk.getMDLMolfileString( mol );
        mdlString=mdlString+"\n$$$$";
        ByteArrayInputStream b= new ByteArrayInputStream( mdlString.getBytes());
        
        List<AtomSignatures> list = doGenerateAtomSignaturesFromSDFStream( b, height );
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
    public Map<IMolecule, AtomSignatures> generate(List<IMolecule> mols){

        Map<IMolecule, AtomSignatures> retmap = 
                                   new HashMap<IMolecule, AtomSignatures>();
        
        for (IMolecule mol : mols){
            try {
                AtomSignatures s = generate( mol );
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
    public Map<IMolecule, AtomSignatures> generate(List<IMolecule> mols, 
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

    
    public List<AtomSignatures> generate(IFile file) 
    throws BioclipseException, CoreException{
        return generate( file, SIGNATURES_DEFAULT_HEIGHT );
    }

    public List<AtomSignatures> generate(IFile file, int height) 
    throws BioclipseException, CoreException{
        return doGenerateAtomSignaturesFromSDFStream( file.getContents(), height);
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
    private List<AtomSignatures> doGenerateAtomSignaturesFromSDFStream(
                                                        InputStream inputstream,
                                                       int height)
                                                     throws BioclipseException {

          List<Molecule> molecules = MoleculeReader.readSDFfromStream( 
                                                                  inputstream );

          List<AtomSignatures> signaturesList = 
              new ArrayList<AtomSignatures>();

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
              AtomSignatures signprop = new AtomSignatures(
                                                               signatureString);

              signaturesList.add( signprop );
          }
          
          return signaturesList;
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
    private List<String> doGenerateMoleculeSignaturesFromSDFStream(
                                                        InputStream inputstream)
                                                     throws BioclipseException {

          List<Molecule> molecules = MoleculeReader.readSDFfromStream( 
                                                                  inputstream );

          //Store results
          List<String> signaturesList = new ArrayList<String>();

          //Should be only one
          if (molecules==null || molecules.size()<=0)
              throw new BioclipseException( "Could not read any molecules " +
                  "from SDF inputstream" );

          //Loop over all molecules
          for (Molecule mol : molecules){
              MoleculeSignature signature = new MoleculeSignature(mol);
              signaturesList.add( signature.getMolecularSignature() );
          }
          
          return signaturesList;
          
      }
    
    public String generateMoleculeSignature( ICDKMolecule mol )
        throws BioclipseException{
            
            //Serialize to SDF
            ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
            
            String mdlString=cdk.getMDLMolfileString( mol );
            mdlString=mdlString+"\n$$$$";
            ByteArrayInputStream b= new ByteArrayInputStream( mdlString.getBytes());
            
            List<String> list = doGenerateMoleculeSignaturesFromSDFStream( b);
            if (list==null || list.size()<=0)
                throw new BioclipseException( "Signatures empty" );
            if (list.size()>1)
                throw new BioclipseException( "Signatures contained more than one "+
                    "result." );
            else
                return list.get( 0 );
        }

}
