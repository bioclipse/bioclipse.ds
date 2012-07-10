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

import net.bioclipse.balloon.business.IBalloonManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.DenseDataset;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.DoubleResult;
import net.bioclipse.qsar.business.IQsarManager;
import net.bioclipse.qsar.init.Activator;

/**
 * A class building on R and Signatures for prediction with 
 * dense signature representation
 * 
 * @author ola
 *
 */
public abstract class DenseCDKRModelMatcher extends RModelMatcher{

    //The logger of the class
    private static final Logger logger = Logger.getLogger(DenseCDKRModelMatcher.class);

	private static final String CDK_DESCRIPTOR_FILE = "cdk.descriptors";
	private static final String REQUIRES_3D = "requires3d";
	private static final String NO_VARIABLES = "Variables";

	private String descriptorFile;
	private List<String> descriptors;
	private boolean requires3d;
	private int noVariables;

	private IQsarManager qsar;


	@Override
	public List<String> getRequiredParameters() {
		
		List<String> ret = super.getRequiredParameters();
        ret.add( CDK_DESCRIPTOR_FILE );
        ret.add( REQUIRES_3D );
        ret.add( NO_VARIABLES );
		return ret;
		
	}

	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {
		
		super.initialize(monitor);
		
		requires3d=Boolean.parseBoolean(getParameters().get(REQUIRES_3D));
		noVariables=Integer.parseInt(getParameters().get(NO_VARIABLES));

		descriptorFile = getFileFromParameter( CDK_DESCRIPTOR_FILE );
		descriptors=readLinesFromFile(descriptorFile);
		
		qsar = Activator.getDefault().getJavaQsarManager();

	}
	
	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {
		
        //Make room for results
        List<ITestResult> results=new ArrayList<ITestResult>();

        if (requires3d){
        	logger.debug("Calculating 3D coordinates for " + getName() + " since requires 3D");
        	monitor.subTask("Calculating 3D coordinates");
        	IBalloonManager balloon = net.bioclipse.balloon.business.
        							  Activator.getDefault().getJavaBalloonManager();
        	
        	try {
				cdkmol = balloon.generate3Dcoordinates(cdkmol);
			} catch (BioclipseException e) {
				LogUtils.debugTrace(logger, e);
				return returnError("Error generating 3D", e.getMessage());
			}
        }
                
        List<IMolecule> mols = new ArrayList<IMolecule>();
        mols.add(cdkmol);

        //Calculate selected descriptors
        try {
        	monitor.subTask("Calculating " + descriptors.size() + " descriptors");
			DenseDataset dataset = qsar.calculate(mols, descriptors);
			System.out.println("DATASET:\n\n"+dataset.asCSV("\t"));
			
			//We know we only have one molecule for now
			List<Float> values = dataset.getValues().get(0);
			
			//Assert correct size
			if (values.size()!=noVariables){
				return returnError("Dimension incorrect for descriptors: " + values.size() + ", expected " + noVariables, "");
			}
			
	        //Set up prediction vector for R
			String tempvar = "tmp."+getId();
			
			String rInputValues=tempvar + " <- c(" + toRString(dataset.getValues().get(0)) + ")";
			R.eval(rInputValues);

			String namesVectorForR="";
			for (String nam : dataset.getColHeaders()){
				namesVectorForR = namesVectorForR + "\"" + nam + "\"" + ",";
			}
			
//			String rnames = getRowNames(tempvar);
//			System.out.println(rnames);
//			R.eval(rnames);
//			R.eval(tempvar);
			
			//Do predictions in R
			String ret="";
			for (String rcmd : getPredictionString(tempvar)){
				System.out.println(rcmd);
				ret = R.eval(rcmd);
//		        System.out.println("R said: " + ret);
			}
			        
	        //Parse result and create testresults
	        double posProb = Double.parseDouble(ret.substring(4));
	        System.out.println("Parsed prediction prob: " + posProb);
	        
			int overallPrediction;
	        if (posProb>=0.5)
	        	overallPrediction = ITestResult.NEGATIVE;
	        else
	        	overallPrediction = ITestResult.POSITIVE;

			DoubleResult accuracy = new DoubleResult("Probability", posProb, overallPrediction);
			results.add(accuracy);
			
		} catch (BioclipseException e) {
			LogUtils.debugTrace(logger, e);
			return returnError("Error calculating descritpors", e.getMessage());
		}
        
        return results;
		
	}


	/**
	 * Transform a list of values into an R-values string
	 * 
	 * @param values
	 * @return
	 */
	private String toRString(List<Float> values) {
		return values.toString().substring(1,values.toString().length()-1);
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
	


	/**
	 * Get a list of the row names from the R data model.
	 * @param tempvar
	 * @return
	 */
	protected abstract String getRowNames(String input);


	
	/**
	 * Provide the R commands to deliver the prediction command to R
	 * from the input String (dense numerical vector with signature frequency).
	 * 
	 * 
	 */
	protected abstract List<String> getPredictionString(String input);
	
}
