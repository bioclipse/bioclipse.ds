package net.bioclipse.ds.libsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.FileUtil;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.matcher.BaseSDFMatcher;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.DoubleResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;
import net.bioclipse.ds.model.result.PosNegIncMatch;
import net.bioclipse.ds.model.result.ScaledResultMatch;
import net.bioclipse.ds.model.result.SimpleResult;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;


public class SignaturesLibSVMPrediction extends BaseSDFMatcher{

    //The logger of the class
    private static final Logger logger = Logger.getLogger(SignaturesLibSVMPrediction.class);

	private static final String HIGH_PERCENTILE = "highPercentile";
	private static final String LOW_PERCENTILE = "lowPercentile";
	private static final String TRAIN_PARAMETER = "trainFile";

	private static final int NR_NEAR_NEIGHBOURS = 3;
	
	protected double lowPercentile;
	protected double highPercentile;
	protected String trainFilename;

    //We need to ensure that '.' is always decimal separator in all locales
    DecimalFormat formatter=new DecimalFormat("0.000");

	private Vector<svm_node[]> nearNeighborData;
    

    //The model file
    private String model_file;
    private String signatures_file;
    protected int startHeight;
    protected int endHeight;

    private final String MODEL_FILE_PARAMETER="modelfile";
    private final String SIGNATURES_FILE_PARAMETER="signaturesfile";

    private final String SIGNATURES_MIN_HEIGHT="signatures.min.height";
    private final String SIGNATURES_MAX_HEIGHT="signatures.max.height";

    //This is an array of the signatures used in the model. 
    //Read from signatures file
    List<String> signatures;
    
    //The SVM model.
    public svm_model svmModel;

	private String positiveValue;

	private String negativeValue;
    

	
    public List<String> getRequiredParameters() {
        List<String> ret=super.getRequiredParameters();

        if (svmModel.param.svm_type == 0) // This is a classification model.
			return ret;

		//Else regression, we need these parameters
        ret.add( MODEL_FILE_PARAMETER );
        ret.add( SIGNATURES_FILE_PARAMETER );
        ret.add( SIGNATURES_MAX_HEIGHT );
        ret.add( SIGNATURES_MIN_HEIGHT );
        ret.add( HIGH_PERCENTILE );
        ret.add( LOW_PERCENTILE );
        ret.add( TRAIN_PARAMETER );
        return ret;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void initialize(IProgressMonitor monitor) throws DSException {
    	super.initialize(monitor);
    	
    	logger.debug("Initializing libsvm test: " + getName());

        //Get parameters from extension
        //We know they exist since required parameters
        model_file=getParameters().get( MODEL_FILE_PARAMETER );
        signatures_file=getParameters().get( SIGNATURES_FILE_PARAMETER );
        startHeight=Integer.parseInt(getParameters().get( SIGNATURES_MIN_HEIGHT ));
        endHeight=Integer.parseInt(getParameters().get( SIGNATURES_MAX_HEIGHT ));
        
        positiveValue=getParameters().get( "positiveValue" );
        negativeValue=getParameters().get( "negativeValue" );

        //Get model path depending on OS
        String modelPath="";
        String signaturesPath = "";

        try {
			modelPath = FileUtil.getFilePath(model_file, getPluginID());
	        logger.debug( "Model file path is: " + modelPath );

	        signaturesPath = FileUtil.getFilePath(signatures_file, getPluginID());
	        logger.debug( "Signatures file path is: " + signaturesPath );
	        

        } catch (Exception e) {
            throw new DSException("Error initializing libsvm test: '" 
            		+ getName() + " due to: " + e.getMessage());
		} 

        //Verify that the signatures file is accessible
        signatures=readSignaturesFile(signaturesPath);
        if (signatures==null || signatures.size()<=0)
            throw new DSException("Signatures file: " + signaturesPath 
            		+ " was empty for test " + getName());

        logger.debug("Read signatures file " + signaturesPath + " with size " 
        		+ signatures.size());

        //Load the model file into memory using SVM
        try {
            svmModel = svm.svm_load_model(modelPath);
        } catch (IOException e) {
            throw new DSException("Could not read model file '" + modelPath 
                                  + "' due to: " + e.getMessage());
        }

    	String p_trainFilename = getParameters().get( TRAIN_PARAMETER );

    	try {
			trainFilename = FileUtil.getFilePath(p_trainFilename, getPluginID());
		} catch (Exception e) {
			e.printStackTrace();
			throw new DSException("Error reading train file: " + e.getMessage());
		}

		// Read the train file. It will be used to retrieve near neighbors to a query from the training data.
		try {
			nearNeighborData = createNearNeighborData(trainFilename);
		} catch (IOException e) {
			returnError("could not read train file", "could not read train file");
		}


		if (svmModel.param.svm_type == 0) // This is a classification model.
			return;
		
    	highPercentile=Double.parseDouble( getParameters().get( HIGH_PERCENTILE ));
    	lowPercentile=Double.parseDouble( getParameters().get( LOW_PERCENTILE ));
    	



    	logger.debug("Initializing of libsvm test: " + getName() 
    			+ " completed successfully.");


    }

	private List<String> readSignaturesFile(String signaturesPath) throws DSException {

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

    
	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {
		
        ISignaturesManager sign=net.bioclipse.ds.signatures.Activator.
        getDefault().getJavaSignaturesManager();

        //Make room for results
        List<ITestResult> results=new ArrayList<ITestResult>();

		Map<String, Double> moleculeSignatures = new HashMap<String, Double>(); // Contains the signatures for a molecule and the count. We store the count as a double although it is an integer. libsvm wants a double.
		Map<String, Integer> moleculeSignaturesHeight = new HashMap<String, Integer>(); //Contains the height for a specific signature.
		Map<String, List<Integer>> moleculeSignaturesAtomNr = new HashMap<String, List<Integer>>(); //Contains the atomNr for a specific signature.
		for (int height = startHeight; height <= endHeight; height++){
			
			//Use the sign manager to generate signatures
			List<String> signs;
			try {
				signs = sign.generate( cdkmol, height ).getSignatures();
			} catch (BioclipseException e) {
	              return returnError( "Error generating signatures","");
			}
			
			
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
						moleculeSignatures.put(currentSignature, (Double)moleculeSignatures.get(currentSignature)+1.00);
					}
					else{
						moleculeSignatures.put(currentSignature, 1.0);
					}
				}
				signsIndex++;
			}
		}
		

		// Do a prediction for a single molecule.
		// Create a descriptor array for the molecule in libsvm format.
		svm_node[] moleculeArray = new svm_node[moleculeSignatures.size()];
		Iterator<String> signaturesIter = signatures.iterator();
		int i = 0;
		while (signaturesIter.hasNext()){
			String currentSignature = signaturesIter.next();
			if (moleculeSignatures.containsKey(currentSignature)){
				moleculeArray[i] = new svm_node();
				moleculeArray[i].index = signatures.indexOf(currentSignature)+1; // libsvm assumes that the index starts at one.
				moleculeArray[i].value = (Double) moleculeSignatures.get(currentSignature);
				i = i + 1;
			}
		}
		// Predict
		double prediction = 0.0;
		prediction = svm.svm_predict(svmModel, moleculeArray);
		System.out.println("Pred: " + prediction);
		
		List<Double> gradientComponents = new ArrayList<Double>();
		// Get the most significant signature.
		double decValues[] = new double[1];
		double lowerPointValue = 0.0, higherPointValue = 0.0;
		svm.svm_predict_values(svmModel, moleculeArray, decValues);
		lowerPointValue = decValues[0];
		for (int element = 0; element < moleculeArray.length; element++){
			// Temporarily increase the descriptor value by one to compute the corresponding component of the gradient of the decision function.
			moleculeArray[element].value = moleculeArray[element].value + 1.00;
			svm.svm_predict_values(svmModel, moleculeArray, decValues);
			higherPointValue = decValues[0];
			if (svmModel.rho[0] > 0.0){ // Check if the decision function is reversed.
				gradientComponents.add(higherPointValue-lowerPointValue);
			}
			else{
				gradientComponents.add(lowerPointValue-higherPointValue);						
			}
			// Set the value back to what it was.
			moleculeArray[element].value = moleculeArray[element].value - 1.00;
				
		}
		//If Classification
		if (svmModel.param.svm_type == 0){ // This is a classification model.
			
			String significantSignature="";
			List<Integer> centerAtoms = new ArrayList<Integer>();
			int height = -1;
			
			if (prediction > 0.0){ // Look for most positive component, we have a positive prediction
				double maxComponent = -1.0;
				int elementMaxVal = -1;
				for (int element = 0; element < moleculeArray.length; element++){
					if (gradientComponents.get(element) > maxComponent){
						maxComponent = gradientComponents.get(element);
						elementMaxVal = element;
					}
				}
				if (maxComponent > 0.0){
					System.out.println("Max atom: " + moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[elementMaxVal].index-1)) + ", max val: " + gradientComponents.get(elementMaxVal) + ", signature: " + signatures.get(moleculeArray[elementMaxVal].index-1) + ", height: " + moleculeSignaturesHeight.get(signatures.get(moleculeArray[elementMaxVal].index-1)));

					significantSignature=signatures.get(moleculeArray[elementMaxVal].index-1);
					height=moleculeSignaturesHeight.get(signatures.get(moleculeArray[elementMaxVal].index-1));
					centerAtoms=moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[elementMaxVal].index-1));

				}
				else{
					System.out.println("No significant signature.");						
				}
			}
			else{ // Look for most negative component, we have a negative prediction
				double minComponent = 1.0;
				int elementMinVal = -1;
				for (int element = 0; element < moleculeArray.length; element++){
					if (gradientComponents.get(element) < minComponent){
						minComponent = gradientComponents.get(element);
						elementMinVal = element;
					}
				}
				if (minComponent < 0.0){

					System.out.println("Min atom: " + moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[elementMinVal].index-1)) + ", min val: " + gradientComponents.get(elementMinVal) + ", signature: " + signatures.get(moleculeArray[elementMinVal].index-1) + ", height: " + moleculeSignaturesHeight.get(signatures.get(moleculeArray[elementMinVal].index-1)));
					significantSignature=signatures.get(moleculeArray[elementMinVal].index-1);
					height=moleculeSignaturesHeight.get(signatures.get(moleculeArray[elementMinVal].index-1));
					centerAtoms=moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[elementMinVal].index-1));
				}
				else{
					System.out.println("No significant signature.");
				}
			}

			//Create the result
	        PosNegIncMatch match = new PosNegIncMatch(significantSignature, 
	        		ITestResult.INCONCLUSIVE);
	        if (prediction>0)
	            match.setClassification( ITestResult.POSITIVE );
	        else
	            match.setClassification( ITestResult.NEGATIVE );

	        
            if (significantSignature.length()>0){
				//OK, color atoms
            	
            	for (int centerAtom : centerAtoms){
            		
                    match.putAtomResult( centerAtom, 
                    		match.getClassification() );
                    
                    int currentHeight=0;
                    List<Integer> lastNeighbours=new ArrayList<Integer>();
                    lastNeighbours.add(centerAtom);
                    
                    while (currentHeight<height){
                    	
                        List<Integer> newNeighbours=new ArrayList<Integer>();

                        //for all lastNeighbours, get new neighbours
                    	for (Integer lastneighbour : lastNeighbours){
                            for (IAtom nbr : cdkmol.getAtomContainer().getConnectedAtomsList(
                                 	  cdkmol.getAtomContainer().getAtom( lastneighbour )) ){
                            	
                                //Set each neighbour atom to overall match classification
                            	int nbrAtomNr = cdkmol.getAtomContainer().getAtomNumber(nbr);
                            	match.putAtomResult( nbrAtomNr, match.getClassification() );
                            	
                            	newNeighbours.add(nbrAtomNr);
                            	
                            }
                    	}
                    	
                    	lastNeighbours=newNeighbours;

                    	currentHeight++;
                    }
                    
            	}
            	

    	        //We can have multiple hits...
    	        //...but here we only have one
                results.add( match );

			}

		}

		//Else we have a regression model
		//Sum up all atom gradients
		else {
//			Map<Integer, Double> atomGreadientComponents = new HashMap<Integer, Double>(); // Contains a sum of all gradient components for a given atom.
//			for (int element = 0; element < moleculeArray.length; element++){
//				int atomNr = moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[element].index-1));
//				double componentVal = gradientComponents.get(element);
//				if (atomGreadientComponents.containsKey(atomNr)){
//					atomGreadientComponents.put(atomNr, atomGreadientComponents.get(atomNr)+componentVal);
//				}
//				else{
//					atomGreadientComponents.put(atomNr,componentVal);
//					
//				}
//			}
			Map<Integer, Double> atomGreadientComponents = new HashMap<Integer, Double>(); // Contains a sum of all gradient components, based on signatures, for a given atom.
			for (int element = 0; element < moleculeArray.length; element++){
				double componentVal = gradientComponents.get(element);
				List<Integer> atomNrList = moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[element].index-1));
				Iterator<Integer> atomNrInteger = atomNrList.iterator();
				while (atomNrInteger.hasNext()){
					int atomNr = atomNrInteger.next();
					if (atomGreadientComponents.containsKey(atomNr)){
						atomGreadientComponents.put(atomNr, atomGreadientComponents.get(atomNr)+componentVal);
					}
					else{
						atomGreadientComponents.put(atomNr,componentVal);	
					}
				}
			}
			System.out.println(atomGreadientComponents.toString());					
			
			
	        ScaledResultMatch match = new ScaledResultMatch("Result: " 
                    + formatter.format( prediction ), 
                    ITestResult.POSITIVE);
	        
	        //Neg prediction means green - Negative overall results for the model
	        if (prediction<=0)
	        	match.setClassification(ITestResult.NEGATIVE);

	        //Color atoms according to accumulated gradient values
	        for (int currentAtomNr : atomGreadientComponents.keySet()){
	        	Double currentDeriv = atomGreadientComponents.get(currentAtomNr);
	        	
    			double scaledDeriv = scaleDerivative(currentDeriv);
    			match.putAtomResult( currentAtomNr-1, scaledDeriv );
	        	
	        }

			System.out.println(atomGreadientComponents.toString());					
			
	        //We can have multiple hits...
	        results.add( match );
	        
		}
		
		
		// Retrieve near neighbors.
		List<Tuple> nearMolecules = retrieveNearestNeighbors(svmModel,nearNeighborData,moleculeArray,NR_NEAR_NEIGHBOURS);
		System.out.println(nearMolecules.toString());

		double mean=0;
		for (int j=0; j< nearMolecules.size(); j++){
			int ix = nearMolecules.get(j).getX();
			mean = mean + nearMolecules.get(j).getY();
			ICDKMolecule nearMol = getSDFmodel().getMoleculeAt(ix);

            String cdktitle=(String) nearMol
            .getAtomContainer().getProperty( CDKConstants.TITLE );
          
          String molname="Index " + ix;
          if (cdktitle!=null)
              molname=cdktitle;

          ExternalMoleculeMatch match = 
              new ExternalMoleculeMatch(molname, nearMol, 
                       (float)nearMolecules.get(j).getY(),  ITestResult.INFORMATIVE);

          results.add( match);

		}
		
		mean = mean / nearMolecules.size();
		System.out.println("Mean conf = " + mean);
		String conf="";
		if (mean<0.33)
			conf="Low";
		if (mean>=0.33 && mean<0.66 )
			conf="Medium";
		if (mean>=0.66)
			conf="High";
		
		SimpleResult confRes= new SimpleResult("Confidence: " + conf, ITestResult.INFORMATIVE);
//		DoubleResult meanresult = new DoubleResult("Confidence", mean, ITestResult.INFORMATIVE);
        results.add( confRes);
		

        return results;

	}
	
	
	
    /**
     * Return a scaling between -1 and 1
     * @param currentDeriv
     * @return
     */
	private double scaleDerivative(Double currentDeriv) {

		//We have a fixed boundary on a low and high percentile
		//so cut away anything below or above this
		if (currentDeriv<=lowPercentile)
			return -1;
		else if (currentDeriv>=highPercentile)
			return 1;
		else if (currentDeriv==0)
			return 0;

		//Since not symmetric around 0, scale pos and neg intervals individually
		if (currentDeriv<0)
			return currentDeriv/(-lowPercentile);
		else
			return currentDeriv/highPercentile;
	}
	
	
	private static Vector<svm_node[]> createNearNeighborData(String tFilename) throws IOException {
		List<svm_node[]> nnData = new ArrayList<svm_node[]>();
		// Ripped from libsvm's java code.
		
		BufferedReader fp = new BufferedReader(new FileReader(tFilename));
        Vector<String> vy = new Vector<String>();
        Vector<svm_node[]> vx = new Vector<svm_node[]>();
        int max_index = 0;

        while(true)
        {
                String line = fp.readLine();
                if(line == null) break;

                StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

                vy.addElement(st.nextToken());
                int m = st.countTokens()/2;
                svm_node[] x = new svm_node[m];
                for(int j=0;j<m;j++)
                {
                        x[j] = new svm_node();
                        x[j].index = atoi(st.nextToken());
                        x[j].value = atof(st.nextToken());
                }
                if(m>0) max_index = Math.max(max_index, x[m-1].index);
                vx.addElement(x);
        }

        fp.close();

		return vx;
	}

	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	
	private static List<Tuple> retrieveNearestNeighbors(svm_model svmModel,
			Vector<svm_node[]> nearNeighborData, svm_node[] moleculeArray, int nrNearNeighbors) {
		Iterator<svm_node[]> nearNeighborIterator = nearNeighborData.iterator();
		
		Map<Integer,Double> nearNeighborMap = new HashMap<Integer,Double>();
		
		
		int element = 0;
		while (nearNeighborIterator.hasNext()){
			nearNeighborMap.put(element,k_function(nearNeighborIterator.next(),moleculeArray,svmModel.param));
			element++;
		}
		
		List<Integer> sortedNeighborKeys = sortMapByValue(nearNeighborMap);

		List<Integer> nNearNeighborKeys = sortedNeighborKeys.subList(sortedNeighborKeys.size()-nrNearNeighbors, sortedNeighborKeys.size());
		
		// Print the similarity values. See if these can be embedded in some sort of struct and class along with the list of the compounds actually being the near neighbors.
		Iterator<Integer> nNearNeighborKeysIterator = nNearNeighborKeys.iterator();
		List<Tuple> indexAndSimilarity = new ArrayList<Tuple>();
		while (nNearNeighborKeysIterator.hasNext()){
			Integer x = nNearNeighborKeysIterator.next();
			Double y = nearNeighborMap.get(x);
			indexAndSimilarity.add(new Tuple(x, y));
			System.out.println("similarity for NN " + x + ": " + y);
		}
		
		return indexAndSimilarity;
	}
	
	
	private static double k_function(svm_node[] x, svm_node[] y, svm_parameter param) {
		// Ripped from libsvm 2.89, should be 3.0 to be consistent with the model and prediction code. These functions are private in libsvm.
		// Is it possible to access them in some way and remove the functions in here?
		switch(param.kernel_type)
		{
			case svm_parameter.LINEAR:
				return dot(x,y);
			case svm_parameter.POLY:
				return powi(param.gamma*dot(x,y)+param.coef0,param.degree);
			case svm_parameter.RBF:
			{
				double sum = 0;
				int xlen = x.length;
				int ylen = y.length;
				int i = 0;
				int j = 0;
				while(i < xlen && j < ylen)
				{
					if(x[i].index == y[j].index)
					{
						double d = x[i++].value - y[j++].value;
						sum += d*d;
					}
					else if(x[i].index > y[j].index)
					{
						sum += y[j].value * y[j].value;
						++j;
					}
					else
					{
						sum += x[i].value * x[i].value;
						++i;
					}
				}

				while(i < xlen)
				{
					sum += x[i].value * x[i].value;
					++i;
				}

				while(j < ylen)
				{
					sum += y[j].value * y[j].value;
					++j;
				}

				return Math.exp(-param.gamma*sum);
			}
			case svm_parameter.SIGMOID:
				return Math.tanh(param.gamma*dot(x,y)+param.coef0);
			case svm_parameter.PRECOMPUTED:
				return	x[(int)(y[0].value)].value;
			default:
				return 0;	// java
		}
	}
	
	
	static double dot(svm_node[] x, svm_node[] y)
	{
		double sum = 0;
		int xlen = x.length;
		int ylen = y.length;
		int i = 0;
		int j = 0;
		while(i < xlen && j < ylen)
		{
			if(x[i].index == y[j].index)
				sum += x[i++].value * y[j++].value;
			else
			{
				if(x[i].index > y[j].index)
					++j;
				else
					++i;
			}
		}
		return sum;
	}

	private static double powi(double base, int times)
	{
		double tmp = base, ret = 1.0;

		for(int t=times; t>0; t/=2)
		{
			if(t%2==1) ret*=tmp;
			tmp = tmp * tmp;
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private static List<Integer> sortMapByValue(final Map m) {
		// Sorts a map based on its values and returns the keys.
		List keys = new ArrayList();
		keys.addAll(m.keySet());
		
		Collections.sort(keys, new Comparator() {
		public int compare(Object o1, Object o2) {
			Object v1 = m.get(o1);
			Object v2 = m.get(o2);
			if (v1 == null){
				return (v2 == null) ? 0 : 1;
			}
			else if (v1 instanceof Comparable) {
				return ((Comparable) v1).compareTo(v2);
			}
			else {
				return 0;
			}
		}
		
		});
		
		return keys;
	}
}
