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
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.CDKConstants;


/**
 * Exact match implementation for SDFiles using Signatures
 * 
 * @author ola
 *
 */
public abstract class BaseSDFExactMatcher extends BaseSDFMatcher implements IDSTest{

    private static final Logger logger = Logger.getLogger(BaseSDFExactMatcher.class);

    /**
     * We require mol sign as property in SDFile
     */
    @Override
    public List<String> getRequiredProperties() {
        List<String> ret=new ArrayList<String>();
        ret.add(getPropertyKey());
        return ret;    
    }

	/**
     * InChI implementation for finding exact matches in an SDFModel
     */
	protected List<? extends ITestResult> doRunTest(IBioObject input,
			IProgressMonitor monitor) {
		
		if (!(input instanceof ICDKMolecule))
			return returnError("Input is not a Molecule", "");
		ICDKMolecule cdkmol = (ICDKMolecule) input;

		//Store results here
        ArrayList<ExternalMoleculeMatch> results=new 
                                             ArrayList<ExternalMoleculeMatch>();

        //Calculate property using subclass implementation
        String calculatedProperty;
		try {
			calculatedProperty = getCalculatedProperty(cdkmol);
		} catch (DSException e) {
			LogUtils.debugTrace(logger, e);
        	return returnError("Could not calculate property: " + getPropertyKey(), "");
		}
        if (calculatedProperty==null || calculatedProperty.length()<=0)
        	return returnError("Could not calculate property: " + getPropertyKey(), "");


        //Search the entire SDFmodel for the query property
        logger.debug( "Quering for: " + calculatedProperty);
        for (int i=0; i<getSDFmodel().getNumberOfMolecules(); i++){

            Object storedPropObject=getSDFmodel().getPropertyFor( i, 
                                                      getPropertyKey() );
            if (storedPropObject==null){
            	logger.warn("Stored prop is null for index: " + i);
            	continue;
            }
            
            String storedProp=null;
			try {
				storedProp = processQueryResult(storedPropObject);
			} catch (DSException e) {
				logger.error("Could not process object " + storedPropObject); //Should not happen
			}

             //Compare signatures
            if (calculatedProperty.equals( storedProp )){
                
//                logger.debug("Found match for mol " + i + ": " + storedProp);
                
                ICDKMolecule matchmol = getSDFmodel().getMoleculeAt( i );
                String molResponse = getSDFmodel().getPropertyFor( i, 
                                                         getResponseProperty());

                String cdktitle=(String) matchmol.getAtomContainer()
                .getProperty( CDKConstants.TITLE );
                String molname="Index " + i;
                if (cdktitle!=null)
                    molname=cdktitle;

                ExternalMoleculeMatch match =null;
                if (isClassification){
                    match = 
                        new ExternalMoleculeMatch(molname, matchmol, 
                                                  getConclusion(molResponse));
                }else{
                    match = 
                        new ExternalMoleculeMatch(molname + ", value=" + molResponse, matchmol, 
                                                  getConclusion(molResponse));
                }

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
        
        return results;
    }


    /**
     * @return the stored property in SDF that we compare with
     */
    public abstract String getPropertyKey();


    /**
     * 
     * @param cdkmol Molecule to calculate on
     * @return The calculated property that we compare against all mols in SDF
     * @throws DSException 
     */
    public abstract String getCalculatedProperty(ICDKMolecule cdkmol) throws DSException;

    
    /**
     * Optional logic to get from a read property to String 
     * used for comparison with calculated property.
     * 
     * Subclasses may override, default impl is toString();
     * 
     * @param obj input to process
     * @return String of processed input.
     * @throws DSException if serialization fails.
     */
	public String processQueryResult(Object obj) throws DSException{
		return obj.toString();
	}
}
