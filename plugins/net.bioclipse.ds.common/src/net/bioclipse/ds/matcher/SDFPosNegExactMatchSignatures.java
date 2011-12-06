/* *****************************************************************************
 * Copyright (c) 2010 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;
import net.bioclipse.ds.signatures.Activator;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;


/**
 * Exact match implementation for SDFiles using Signatures
 * 
 * @author ola
 *
 */
public class SDFPosNegExactMatchSignatures extends BaseSDFPosNegMatcher implements IDSTest{

    private static final String SIGNATURES_PROPERTY_KEY="Molecular Signature";

    private static final Logger logger = Logger.getLogger(SDFPosNegExactMatchSignatures.class);

    /**
     * Adds INCHI as required property in SDFile
     */
    @Override
    List<String> getRequiredProperties() {

        //Add signature property, we need this in the impl below
        List<String> ret=new ArrayList<String>();
        ret.add(SIGNATURES_PROPERTY_KEY);
        return ret;    
    }
    
    /**
     * InChI implementation for finding exact matches in an SDFModel
     */
    protected List<? extends ITestResult> doRunTest( 
                                                     ICDKMolecule cdkmol, 
                                                     IProgressMonitor monitor) {
        //Store results here
        ArrayList<ExternalMoleculeMatch> results=new 
                                             ArrayList<ExternalMoleculeMatch>();

        ISignaturesManager signatures= Activator.getDefault()
        .getJavaSignaturesManager();

        String querySignature=null;
        try {
            querySignature = signatures.generateMoleculeSignature( cdkmol );
            
            
        } catch ( Exception e ) {
            logger.error( "Failed to calculate Signatures for mol: " + cdkmol);
            return returnError( "Error generating Signatures", e.getMessage() );
        }
        
        if (querySignature==null){
            logger.error( "MolSignatures empty for mol: " + cdkmol);
            return returnError( "No MolSignature created for molecule: " 
            		+ cdkmol, "" );
        }

        //What to search for
//        logger.debug( "Query MolecularSignature: " + querySignature);

        //Search the entire SDFmodel
        for (int i=0; i<getSDFmodel().getNumberOfMolecules(); i++){

            String sigprop=getSDFmodel().getPropertyFor( i, 
                                                      SIGNATURES_PROPERTY_KEY );
            //Null check not required since verified in initialize()

             //Compare signatures
            if (querySignature.equals( sigprop )){
                
//                logger.debug("Found signature match for mol " + i + ": " + sigprop);
                
                ICDKMolecule matchmol = getSDFmodel().getMoleculeAt( i );
                String molResponse = getSDFmodel().getPropertyFor( i, 
                                                         getResponseProperty());

                String cdktitle=(String) matchmol.getAtomContainer()
                .getProperty( CDKConstants.TITLE );
                String molname="Index " + i;
                if (cdktitle!=null)
                    molname=cdktitle;

                ExternalMoleculeMatch match = 
                    new ExternalMoleculeMatch(molname, matchmol, 
                                              getConclusion(molResponse));

				Map<String, Map<String, String>> categories = new HashMap<String, Map<String,String>>();
				Map<String,String> props = new HashMap<String, String>();
                props.put("Observed value" , molResponse);
                categories.put("Observations", props);
                match.setProperties(categories);

                results.add( match );
            }

             if (monitor.isCanceled())
                 return returnError( "Cancelled","");

        }
        
//        if (results.size()<=0)
//        	logger.debug("No Mol Signatures matches found");
//        else
//        	logger.debug("Mol Signatures found " + results.size() + " matches");

        return results;
    }
    
}
