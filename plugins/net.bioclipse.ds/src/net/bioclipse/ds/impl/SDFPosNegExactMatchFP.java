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
package net.bioclipse.ds.impl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.impl.result.ExternalMoleculeMatch;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.CDKConstants;


/**
 * FP implementation for exact matches in SDFile where tanimoto=1
 * 
 * @author ola
 *
 */
public class SDFPosNegExactMatchFP extends BaseSDFPosNegMatcher implements IDSTest{

    private static final String CDK_FP_PROPERTY_KEY="net.bioclipse.cdk.fingerprint";

    private static final Logger logger = Logger.getLogger(SDFPosNegExactMatchFP.class);

    /**
     * Adds CDK FP as required property in SDFile
     */
    @Override
    List<String> getRequiredProperties() {
        //Add fingerprint property, we need this in the impl below
        List<String> ret=new ArrayList<String>();
        ret.add(CDK_FP_PROPERTY_KEY);
        return ret;    
    }

    /**
     * FP implementation for finding exact matches in an SDFModel
     */
    protected List<? extends ITestResult> doRunTest( 
                                                     ICDKMolecule cdkmol, 
                                                     IProgressMonitor monitor) {
        //Store results here
        ArrayList<ExternalMoleculeMatch> results=new 
                                             ArrayList<ExternalMoleculeMatch>();

        //The CDK manager is needed for tanimoto
        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();

        try {
            //Start by searching for inchiKey
            //================================
            BitSet molFP = cdkmol.getFingerprint( IMolecule.Property.
                                                  USE_CALCULATED );
            logger.debug( "FP to search for: " + molFP);
            logger.debug( "Molecule to search for: " + cdkmol);

            //Search the index for this FP
            for (int i=0; i<getSDFmodel().getNumberOfMolecules(); i++){
                BitSet dbFP = getSDFmodel().getPropertyFor( i, CDK_FP_PROPERTY_KEY);
                

                if (dbFP==null){
                    return returnError( "Error reading fingerprint in database","");
                }

                //We ignore this case for now 
                if (dbFP.size()!=molFP.size()){
//                    logger.warn( "Index " + i + " in DB has FP size=" 
//                                 + dbFP.size() + 
//                                 " but molecule searched for has FP size=" 
//                                 + molFP.size());
                }else{

                    float calcTanimoto = cdk.calculateTanimoto( dbFP, molFP );
                    
                    //Exact match demands 1.0
                    if (calcTanimoto >= 1.0){

                        //A hit found
                        ICDKMolecule matchmol = getSDFmodel().getMoleculeAt( i );
                        String molResponse = getSDFmodel().getPropertyFor( i, 
                                                         getResponseProperty());
                        String cdktitle=(String) matchmol
                          .getAtomContainer().getProperty( CDKConstants.TITLE );
                        
                        String molname="Index " + i;
                        if (cdktitle!=null)
                            molname=cdktitle;

                        ExternalMoleculeMatch match = 
                            new ExternalMoleculeMatch(molname, matchmol, 
                                                    getConclusion(molResponse));
                        results.add( match);
                    }
                }

                if (monitor.isCanceled())
                    return returnError( "Cancelled","");

            }
            
        } catch ( Exception e ) {
            LogUtils.debugTrace( logger, e );
            return returnError( "Test failed: " , e.getMessage());
        }
        
        return results;
    }
    
}
