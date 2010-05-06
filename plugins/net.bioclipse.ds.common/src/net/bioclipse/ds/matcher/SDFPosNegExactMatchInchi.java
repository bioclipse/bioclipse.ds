/* *****************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
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
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;
import net.bioclipse.inchi.InChI;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.CDKConstants;


/**
 * InChI exact match implementation for SDFiles
 * 
 * @author ola
 *
 */
public class SDFPosNegExactMatchInchi extends BaseSDFPosNegMatcher implements IDSTest{

    private static final String INCHI_PROPERTY_KEY="net.bioclipse.cdk.InChI";

    private static final Logger logger = Logger.getLogger(SDFPosNegExactMatchInchi.class);

    /**
     * Adds INCHI as required property in SDFile
     */
    @Override
    List<String> getRequiredProperties() {

        //Add fingerprint property, we need this in the impl below
        List<String> ret=new ArrayList<String>();
        ret.add(INCHI_PROPERTY_KEY);
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

        //Calculate inchi and inchi key for query molecule
        String molInchiKey;
        String molInchi;
        try {
            molInchiKey = cdkmol.getInChIKey( 
                                     IMolecule.Property.USE_CALCULATED  );
            molInchi = cdkmol.getInChI(
                                       IMolecule.Property.USE_CALCULATED  );
        } catch ( BioclipseException e ) {
//            LogUtils.debugTrace( logger, e );
            return returnError( "Error generating Inchi", e.getMessage() );
        }

        logger.debug( "Inchikey to search for: " + molInchiKey);

        //Search the entire SDFmodel
        for (int i=0; i<getSDFmodel().getNumberOfMolecules(); i++){

            InChI readInchi = getSDFmodel().getPropertyFor( i, 
                                                           INCHI_PROPERTY_KEY );
            //Null check not required since verified in initialize()

             //Compare InChI Key
             if (molInchiKey.equals( readInchi.getKey() )){
                 
                 //If key equal, compare InChI just to be sure
                if (molInchi.equals( readInchi.getValue() )){
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

                    results.add( match );
                }
            }

             if (monitor.isCanceled())
                 return returnError( "Cancelled","");

        }

        return results;
    }
    
}
