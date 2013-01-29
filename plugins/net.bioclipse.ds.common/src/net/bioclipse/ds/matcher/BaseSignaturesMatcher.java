/*******************************************************************************
 * Copyright (c) 2009-2011 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.matcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.matcher.model.SignatureFrequenceyResult;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.signatures.business.ISignaturesManager;


/**
 * An abstract base class for DS tests using LibSVM and Signatures
 * 
 * @author Ola Spjuth
 *
 */
public abstract class BaseSignaturesMatcher extends AbstractDSTest implements IDSTest{
    
    //The logger of the class
    private static final Logger logger = Logger.getLogger(BaseSignaturesMatcher.class);

    //The model file
    private String signatures_file;
    protected int startHeight;
	protected int endHeight;

    private final String SIGNATURES_FILE_PARAMETER="signaturesfile";

    private final String SIGNATURES_MIN_HEIGHT="signatures.min.height";
    private final String SIGNATURES_MAX_HEIGHT="signatures.max.height";

    //This is an array of the signatures used in the model. 
    //Read from signatures file
    protected List<String> signatures;
    
    
    /**
     * Default constructor
     */
    public BaseSignaturesMatcher(){
        super();
    }

    @Override
    public List<String> getRequiredParameters() {
        List<String> ret=new ArrayList<String>();
        ret.add( SIGNATURES_FILE_PARAMETER );
        ret.add( SIGNATURES_MAX_HEIGHT );
        ret.add( SIGNATURES_MIN_HEIGHT );
        return ret;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    


    public int getStartHeight() {
		return startHeight;
	}

	public int getEndHeight() {
		return endHeight;
	}

	public List<String> getSignatures() {
		return signatures;
	}

	/**
     * Initialize all paths and read model files into memory.
     * @throws DSException 
     */
    public void initialize(IProgressMonitor monitor) throws DSException {

    	super.initialize(monitor);
    	logger.debug("Initializing base signatures model: " + getName());

        //Get parameters from extension
        //We know they exist since required parameters
    	signatures_file = getFileFromParameter(SIGNATURES_FILE_PARAMETER );
        startHeight=Integer.parseInt(getParameters().get( SIGNATURES_MIN_HEIGHT ));
        endHeight=Integer.parseInt(getParameters().get( SIGNATURES_MAX_HEIGHT ));

        //Read signatures into memory
        signatures=readSignaturesFile(signatures_file);

        if (signatures==null || signatures.size()<=0)
            throw new DSException("Signatures file: " + signatures_file 
            		+ " was empty for test " + getName());

        logger.debug("Read signatures file " + signatures_file + " with size " 
        		+ signatures.size());

    	logger.debug("Initializing of base signatures model: " + getName() 
    			+ " completed successfully.");

    }


	public List<String> readSignaturesFile(String signaturesPath) throws DSException {

    	logger.debug("Reading signature file: " + signaturesPath);

		List<String> signatures = new ArrayList<String>(); // Contains signatures. We use the indexOf to retrieve the order of specific signatures in descriptor array.
		try {
			BufferedReader signaturesReader = new BufferedReader(new FileReader(new File(signaturesPath)));
			String signature;
			while ( (signature = signaturesReader.readLine()) != null ) {
				signatures.add(signature);
			}
		} catch (FileNotFoundException e) {
    		LogUtils.debugTrace(logger, e);
    		throw new DSException("Error reading signatures file " 
    				+ signaturesPath + ": " + e.getMessage());
		} catch (IOException e) {
    		LogUtils.debugTrace(logger, e);
    		throw new DSException("Error reading signatures file " 
    				+ signaturesPath + ": " + e.getMessage());
		} 

    	logger.debug("Reading signature file: " + signaturesPath + " completed successfully");

    	return signatures;

    }

	
	public SignatureFrequenceyResult countSignatureFrequency(ICDKMolecule cdkmol) throws BioclipseException {

		ISignaturesManager sign=net.bioclipse.ds.signatures.Activator.
        getDefault().getJavaSignaturesManager();
		
		Map<String, Integer> moleculeSignatures = new HashMap<String, Integer>(); // Contains the signatures for a molecule and the count. We store the count as a double although it is an integer.
		Map<String, Integer> moleculeSignaturesHeight = new HashMap<String, Integer>(); //Contains the height for a specific signature.
		Map<String, List<Integer>> moleculeSignaturesAtomNr = new HashMap<String, List<Integer>>(); //Contains the atomNr for a specific signature.
		
		for (int height = startHeight; height <= endHeight; height++){
			
			//Use the sign manager to generate signatures
			List<String> signs = sign.generate( cdkmol, height ).getSignatures();
			
			Iterator<String> signsIter = signs.iterator();
			int signsIndex = 0;
			while (signsIter.hasNext()){
				String currentSignature = signsIter.next();
				if (signatures.contains(currentSignature)){
					if (!moleculeSignaturesAtomNr.containsKey(currentSignature)){
						moleculeSignaturesAtomNr.put(currentSignature, new ArrayList<Integer>());
					}
					moleculeSignaturesHeight.put(currentSignature, height);
					List<Integer> tmpList = moleculeSignaturesAtomNr.get(currentSignature);
					tmpList.add(signsIndex);
					moleculeSignaturesAtomNr.put(currentSignature, tmpList);
					if (moleculeSignatures.containsKey(currentSignature)){
						moleculeSignatures.put(currentSignature, moleculeSignatures.get(currentSignature)+1);
					}
					else{
						moleculeSignatures.put(currentSignature, 1);
					}
				}
				signsIndex++;
			}
		}
		
		return new SignatureFrequenceyResult(moleculeSignatures, 
				   						     moleculeSignaturesHeight, 
				   						     moleculeSignaturesAtomNr);

	}

	
}
