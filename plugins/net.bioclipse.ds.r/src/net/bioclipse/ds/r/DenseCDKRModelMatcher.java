package net.bioclipse.ds.r;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.matcher.BaseSignaturesMatcher;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.SimpleResult;

/**
 * A class building on R and Signatures for prediction with 
 * dense signature representation
 * 
 * @author ola
 *
 */
public class DenseCDKRModelMatcher extends RModelMatcher{

    //The logger of the class
    private static final Logger logger = Logger.getLogger(DenseCDKRModelMatcher.class);

	private static final String CDK_DESCRIPTOR_FILE = "cdk.descriptors";

	private String descriptorFile;
	private List<String> descriptors;

	@Override
	public List<String> getRequiredParameters() {
		
		List<String> ret = super.getRequiredParameters();
        ret.add( CDK_DESCRIPTOR_FILE );
		return ret;
	}

	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {
		
		super.initialize(monitor);

		descriptorFile = getFileFromParameter( CDK_DESCRIPTOR_FILE );
		descriptors=readLinesFromFile(descriptorFile);

	}
	
	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {
		
        //Make room for results
        List<ITestResult> results=new ArrayList<ITestResult>();

        //Calculate selected descriptors
        //TODO
        
        //Set up prediction vector
        //Assert descriptor labels are in same order as training set
        //TODO
        
        //Predict using R
        //TODO
        
        results.add(new SimpleResult("NOT IMPLEMENTED", ITestResult.ERROR));
        return results;
		
	}

	
	public List<String> readLinesFromFile(String path) throws DSException {

    	logger.debug("Reading lines from file: " + path);

		List<String> lines = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			String line;
			while ( (line = reader.readLine()) != null ) {
				lines.add(line);
			}
		} catch (FileNotFoundException e) {
    		LogUtils.debugTrace(logger, e);
    		throw new DSException("Error reading file " 
    				+ path + ": " + e.getMessage());
		} catch (IOException e) {
    		LogUtils.debugTrace(logger, e);
    		throw new DSException("Error reading file " 
    				+ path + ": " + e.getMessage());
		} 

    	logger.debug("Reading Lines from file: " + path + " completed successfully");

    	return lines;

    }
	
}
