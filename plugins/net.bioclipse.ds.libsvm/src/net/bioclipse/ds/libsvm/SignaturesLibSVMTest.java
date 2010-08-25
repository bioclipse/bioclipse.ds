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
package net.bioclipse.ds.libsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.FileUtil;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;


/**
 * An abstract base class for DS tests using LibSVM and Signatures
 * 
 * @author Ola Spjuth
 *
 */
public abstract class SignaturesLibSVMTest extends AbstractDSTest implements IDSTest{
    
    //The logger of the class
    private static final Logger logger = Logger.getLogger(SignaturesLibSVMTest.class);

    //The model file
    private String MODEL_FILE;
    private String RANGE_FILE;
    private String SIGNATURES_FILE;

    private final String MODEL_FILE_PARAMETER="modelfile";
    private final String RANGE_FILE_PARAMETER="rangefile";
    private final String SIGNATURES_FILE_PARAMETER="signaturesfile";

    //This is an array of the signatures used in the model. 
    //Read from signatures file
    protected String[] signatureList;

    //Range variables
    //Defined by the range file.
    protected double lower_range;
    protected double upper_range;
    protected double[] feature_min;
    protected double[] feature_max;

    //The SVM model.
    public svm_model svmModel;
    
    /**
     * Default constructor
     */
    public SignaturesLibSVMTest(){
        super();
    }

    @Override
    public List<String> getRequiredParameters() {
        List<String> ret=super.getRequiredParameters();
        ret.add( MODEL_FILE_PARAMETER );
        ret.add( RANGE_FILE_PARAMETER );
        ret.add( SIGNATURES_FILE_PARAMETER );
        return ret;
    }
    
    @Override
    public String toString() {
        return getName();
    }


    /**
     * Initialize all paths and read model files into memory.
     * @throws DSException 
     */
    public void initialize(IProgressMonitor monitor) throws DSException {

    	logger.debug("Initializing libsvm test: " + getName());

        //Get parameters from extension
        //We know they exist since required parameters
        MODEL_FILE=getParameters().get( "modelfile" );
        RANGE_FILE=getParameters().get( "rangefile" );
        SIGNATURES_FILE=getParameters().get( "signaturesfile" );

        //Get model path depending on OS
        String modelPath="";
        String rangePath = "";
        String signaturesPath = "";

        try {
			modelPath = FileUtil.getFilePath(MODEL_FILE, getPluginID());
	        logger.debug( "Model file path is: " + modelPath );

	        rangePath = FileUtil.getFilePath(RANGE_FILE, getPluginID());
	        logger.debug( "Range file path is: " + rangePath );

	        signaturesPath = FileUtil.getFilePath(SIGNATURES_FILE, getPluginID());
	        logger.debug( "Signatures file path is: " + signaturesPath );

        } catch (Exception e) {
            throw new DSException("Error initializing libsvm test: '" 
            		+ getName() + " due to: " + e.getMessage());
		} 

        //Verify that the signatures file is accessible
        signatureList=readSignaturesFile(signaturesPath);
        if (signatureList==null || signatureList.length<=0)
            throw new DSException("Signatures file: " + signaturesPath 
            		+ " was empty for test " + getName());

        logger.debug("Read signatures file " + signaturesPath + " with size " 
        		+ signatureList.length);

        // Add the reading of the range file and set up the related variables.
        readRangeFile(rangePath);
        
        logger.debug("Read range file " + rangePath + " with size " 
        		+ feature_min.length);
        
        if (signatureList.length != feature_min.length)
            throw new DSException("Size of signatures and range arrays is " +
            		"not equal in test: " + getName());

        
        //Load the model file into memory using SVM
        try {
            svmModel = svm.svm_load_model(modelPath);
        } catch (IOException e) {
            throw new DSException("Could not read model file '" + modelPath 
                                  + "' due to: " + e.getMessage());
        }

    	logger.debug("Initializing of libsvm test: " + getName() 
    			+ " completed successfully.");

    }


    private void readRangeFile(String rangePath) throws DSException {
    	
    	logger.debug("Reading Range file: " + rangePath);

    	try {
    		BufferedReader br = 
    			new BufferedReader(new FileReader(new File(rangePath)));

    		String line=br.readLine();
    		
    		//Skip first line if starts with x
    		if (line.startsWith("x"))
    			line=br.readLine();

    		String[] splitLine = line.split(" ");
    		lower_range=Double.parseDouble(splitLine[0].trim());
    		upper_range=Double.parseDouble(splitLine[1].trim());
    		logger.debug("Lower range=" + lower_range 
    				+ " ; Upper range=" + upper_range);

    		//Third line and on...
    		line=br.readLine();
    		List<Double> feature_min_list=new ArrayList<Double>();
    		List<Double> feature_max_list=new ArrayList<Double>();
    		int r_lineno=1;	//range line number
    		while(line!=null){
        		splitLine = line.split(" ");
        		if (splitLine.length==2){
        		  feature_min_list.add(Double.parseDouble(splitLine[0].trim()));
        		  feature_max_list.add(Double.parseDouble(splitLine[1].trim()));
        		}
        		else if (splitLine.length==3){
        			//Confirm we read r_lineno in first, else impute 0,0
        			double nextLine=Double.parseDouble(splitLine[0].trim());

        			while (nextLine > r_lineno) {
    					feature_min_list.add(new Double(0));
    					feature_max_list.add(new Double(0));
    					logger.debug("Imputed range (0,0) for entry: " 
    							+ r_lineno);
    					r_lineno++;
					}
        			
        			//Ok, process with read values
        			feature_min_list.add(Double.parseDouble(
        					splitLine[1].trim()));
        			feature_max_list.add(Double.parseDouble(
        					splitLine[2].trim()));
        		}
        		else{
        			throw new DSException("Range file: " + rangePath + " line " 
        					+ line + " does not have either 2 or 3 " +
        							"entries separated by space.");
        		}

    			//Read next line
    			line=br.readLine();
    			r_lineno++;
    		}
    		
    		br.close();

    		//Convert lists to array of primitives
    		feature_min=new double[feature_min_list.size()];
    		for (int i=0; i<feature_min_list.size(); i++){
    			feature_min[i]=feature_min_list.get(i);
    		}
    		feature_max=new double[feature_max_list.size()];
    		for (int i=0; i<feature_max_list.size(); i++){
    			feature_max[i]=feature_max_list.get(i);
    		}
    		
    		logger.debug("Range file feature_min size: " + feature_min.length);
    		logger.debug("Range file feature_max size: " + feature_max.length);

    	} catch (Exception e) {
    		LogUtils.debugTrace(logger, e);
    		throw new DSException("Error reading range file " 
    				+ rangePath + ": " + e.getMessage());
    	}
    	
    	logger.debug("Reading Range file: " + rangePath + " completed successfully");

    }
    
    
	private String[] readSignaturesFile(String signaturesPath) throws DSException {

    	logger.debug("Reading signature file: " + signaturesPath);

		List<String> readsigns=new ArrayList<String>();
    	try {
    		BufferedReader br = 
    			new BufferedReader(new FileReader(new File(signaturesPath)));

    		String line=br.readLine();

    		while(line!=null){
    			//No assertion yet that this is a valid signature.
    			//Maybe add this later
    			readsigns.add(line);

    			//Read next line
    			line=br.readLine();
    		}
    		
    		br.close();

    	} catch (Exception e) {
    		LogUtils.debugTrace(logger, e);
    		throw new DSException("Error reading signatures file " 
    				+ signaturesPath + ": " + e.getMessage());
    	}
    	
    	logger.debug("Reading signature file: " + signaturesPath + " completed successfully");

    	return readsigns.toArray(new String[readsigns.size()]);

    }

}
