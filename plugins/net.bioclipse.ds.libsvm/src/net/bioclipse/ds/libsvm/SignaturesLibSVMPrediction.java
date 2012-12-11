/*******************************************************************************
 * Copyright (c) 2010-2012  Ola Spjuth <ola@bioclipse.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/

package net.bioclipse.ds.libsvm;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.libsvm.model.PredictionModel;
import net.bioclipse.ds.libsvm.model.SignLibsvmModel;
import net.bioclipse.ds.libsvm.model.SignLibsvmUtils;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.AtomResultMatch;
import net.bioclipse.ds.model.result.PosNegIncMatch;
import net.bioclipse.ds.model.result.ScaledResultMatch;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.interfaces.IAtom;


/**
 * A DS implementation of signatures and libsvm.
 * 
 * @author ola
 *
 */
public class SignaturesLibSVMPrediction extends AbstractDSTest{


	//The logger of the class
	private static final Logger logger = Logger.getLogger(SignaturesLibSVMPrediction.class);

	private static final String HIGH_PERCENTILE = "highPercentile";
	private static final String LOW_PERCENTILE = "lowPercentile";

	private static final String REGRESSION_LOWER_THRESHOLD = "lower_threshold";
	private static final String REGRESSION_UPPER_THRESHOLD = "upper_threshold";
	private static final String LOW_IS_NEGATIVE = "lowIsNegative";

	private static final String MODEL_FILE_PARAMETER="modelfile";
	private static final String SIGNATURES_FILE_PARAMETER="signaturesfile";
	private final String SIGNATURES_MIN_HEIGHT="signatures.min.height";
	private final String SIGNATURES_MAX_HEIGHT="signatures.max.height";

	protected String trainFilename;
	protected int startHeight;
	protected int endHeight;

	protected double lowPercentile;
	protected double highPercentile;

	protected Float regrUpperThreshold=null;
	protected Float regrLowerThreshold=null;
	protected boolean lowIsNegative=true;

	private String positiveValue;
	private String negativeValue;
	private List<String> classLabels;

	//The SVM model, holds the signatures list and native svm_model
	public SignLibsvmModel signSvmModel;

	//We need to ensure that '.' is always decimal separator in all locales
	DecimalFormat formatter=new DecimalFormat("0.000");



	public List<String> getRequiredParameters() {
		List<String> ret=new ArrayList<String>();

		//Parameters special for regression model
		if (getParameters().get("isClassification")!=null && 
				getParameters().get("isClassification").equals("false")){
			ret.add( HIGH_PERCENTILE );
			ret.add( LOW_PERCENTILE );
		}

		ret.add( MODEL_FILE_PARAMETER );
		ret.add( SIGNATURES_FILE_PARAMETER );
		ret.add( SIGNATURES_MAX_HEIGHT );
		ret.add( SIGNATURES_MIN_HEIGHT );

		return ret;
	}

	//Shows in DSView
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
		String modelPath = getFileFromParameter(MODEL_FILE_PARAMETER );
		String signaturesPath = getFileFromParameter(SIGNATURES_FILE_PARAMETER );
		startHeight=Integer.parseInt(getParameters().get( SIGNATURES_MIN_HEIGHT ));
		endHeight=Integer.parseInt(getParameters().get( SIGNATURES_MAX_HEIGHT ));

		//These are for regression
		if ("false".equalsIgnoreCase(getParameters().get( "isClassification" ))){
			logger.debug("The model " + getName() + " is a regression model.");

			if (getParameters().get( LOW_PERCENTILE )!=null)
				lowPercentile = Double.parseDouble(getParameters().get( LOW_PERCENTILE ));
			else
				throw new DSException("Missing LowPercentile parameter");

			if (getParameters().get( HIGH_PERCENTILE )!=null)
				highPercentile = Double.parseDouble(getParameters().get( HIGH_PERCENTILE ));
			else
				throw new DSException("Missing HighPercentile parameter");

			System.out.println("Low percentile is: " + lowPercentile);
			System.out.println("High percentile is: " + highPercentile);

			try{
				regrLowerThreshold=Float.parseFloat(getParameters().get(REGRESSION_LOWER_THRESHOLD));
				regrUpperThreshold=Float.parseFloat(getParameters().get(REGRESSION_UPPER_THRESHOLD));
				lowIsNegative=Boolean.parseBoolean(getParameters().get(LOW_IS_NEGATIVE));
			}catch (Exception e){
				logger.debug(getName() + " is a regression model without thresholds");
			}

		}else{
			//Classification parameters
			positiveValue=getParameters().get( "positiveValue" );
			negativeValue=getParameters().get( "negativeValue" );
			String classString = getParameters().get( "classLabels" );
			if (classString!=null){
				String[] csplit = classString.split(",");
				classLabels=new ArrayList<String>();
				for (String l : csplit){
					classLabels.add(l.trim());
				}
			}

		}

		//Some debugging
		logger.debug( "Model file path is: " + modelPath );
		logger.debug( "Signatures file path is: " + signaturesPath );
		logger.debug( "Classlabels are: " + classLabels );

		List<String> modelSignatures;
		//Read modelSignatures into memory
		try {
			modelSignatures=SignLibsvmUtils.readSignaturesFile(signaturesPath);
		} catch (IOException e1) {
			throw new DSException("Unable to read signatures file: " + signaturesPath);
		}

		if (modelSignatures==null || modelSignatures.size()<=0)
			throw new DSException("Signatures file: " + signaturesPath 
					+ " was empty for test " + getName());

		logger.debug("Read modelSignatures file " + signaturesPath + " with size " 
				+ modelSignatures.size());

		//Load the model file into memory using SVM
		try {
			svm_model svmModel = svm.svm_load_model(modelPath);
			signSvmModel = new SignLibsvmModel(svmModel, modelSignatures);
		} catch (IOException e) {
			throw new DSException("Could not read model file '" + modelPath 
					+ "' due to: " + e.getMessage());
		}


		if (signSvmModel.getSvmModel().param.svm_type == 0) // This is a classification model.
			return;

		logger.debug("Initializing of libsvm test: " + getName() 
				+ " completed successfully.");

	}




	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {

		return predictLibSVM(cdkmol,null,true,monitor);

	}

	/**
	 * 
	 * Allow for running multiple predictions in subclasses
	 * 
	 * @param cdkmol
	 * @param object
	 * @param monitor
	 * @return
	 */
	public List<? extends ITestResult> predictLibSVM(ICDKMolecule cdkmol,
			List<svm_node> extraDescriptors, boolean extractSignificantSignatures, 
			IProgressMonitor monitor) {

		//Make room for results
		List<ITestResult> results=new ArrayList<ITestResult>();
		
		//We create a prediction model from the molecule. Includes its signatures.
		PredictionModel predModel=null;
		try {
			predModel = createPredModel(cdkmol);
		} catch (BioclipseException e) {
			returnError("Unable to create prediction model", e.getMessage());
		}

		// Do a prediction for a single molecule.
		// Create a descriptor array for the molecule in libsvm format.
		List<svm_node> svmNodeList = new ArrayList<svm_node>();
		Iterator<String> signaturesIter = signSvmModel.getModelSignatures().iterator();
		while (signaturesIter.hasNext()){
			String currentSignature = signaturesIter.next();
			if (predModel.getMoleculeSignatures().containsKey(currentSignature)){
				svm_node node = new svm_node();
				svmNodeList.add(node);
				node.index = signSvmModel.getModelSignatures().indexOf(currentSignature)+1; // libsvm assumes that the index starts at one.
				node.value = (Double) predModel.getMoleculeSignatures().get(currentSignature);

				System.out.println(currentSignature + " ==> " 
				+ node.index + "=" + node.value);

			}
		}

		//This is the array with signature descriptor values
		svm_node[] svmPredictionArray = svmNodeList.toArray(new svm_node[0]);

		// Predict
		double prediction = 0.0;
		if (extraDescriptors!=null && extraDescriptors.size()>0){

			//We have extra descriptors, so concatenate them
			svmNodeList.addAll(extraDescriptors);

			//This is the array with signature descriptor values + extra descriptors
			svm_node[] svmExtraDescPredictionArray = svmNodeList.toArray(new svm_node[0]);
//			for (svm_node node : svmExtraDescPredictionArray)
//				System.out.println(node.index + "=" + node.value);

//			prediction = svm.svm_predict(svmModel, svmExtraDescPredictionArray);
			prediction = signSvmModel.predict(svmExtraDescPredictionArray);

		}else{

			//No extra descriptors, use only signatures
//			prediction = svm.svm_predict(svmModel, svmPredictionArray);
			prediction = signSvmModel.predict(svmPredictionArray);

		}
		System.out.println("--> PREDICTION: " + prediction);
		
		
		//Create the result for the classification, overwrite name later if we have sign signature
		AtomResultMatch match = new PosNegIncMatch("No significant signature", 
				ITestResult.INCONCLUSIVE);
		results.add(match);

		//Logic for multi-class results here
		int intPrediction=(int)prediction;
		if (classLabels==null){
			return returnError("No classlabels found in model", "Classlabels are null for model");
		}
		String predictedClassLabel = classLabels.get(intPrediction);
		System.out.println("predictedClassLabel=" + predictedClassLabel);

		//If this is higher than posVal in list, set POSITIVE color
		int posIX = classLabels.indexOf(positiveValue);
		int negIX = classLabels.indexOf(negativeValue);
		int predIX = classLabels.indexOf(predictedClassLabel);

		if (predIX==posIX){
			match.setClassification( ITestResult.POSITIVE );
			match.setName("Result: Positive");
		}
		else if (predIX==negIX){
			match.setClassification( ITestResult.NEGATIVE );
			match.setName("Result: Negative");
		}
		else{
			match.setClassification( ITestResult.INCONCLUSIVE );
			match.setName("Result: Inconclusive");
		}


		//End here if we should not extract significant signatures
		if (!extractSignificantSignatures)
			return results;

		
		
		//************************
		//* Significant signatures
		//************************
		svm_model svmModel = signSvmModel.getSvmModel();  //Get the SVM model from the wrapper object
		
		// Get the most significant signature for classification or the sum of all gradient components for regression.
		List<Double> gradientComponents = new ArrayList<Double>();
		int nOverk = fact(svmModel.nr_class)/(fact(2)*fact(svmModel.nr_class-2)); // The number of decision functions for a classification.
		double decValues[] = new double[nOverk];
		double lowerPointValue[] = new double[nOverk]; 
		double higherPointValue[] = new double[nOverk];
		svm.svm_predict_values(svmModel, svmPredictionArray, decValues);
		lowerPointValue = decValues.clone();
		for (int element = 0; element < svmPredictionArray.length; element++){
			// Temporarily increase the descriptor value by one to compute the corresponding component of the gradient of the decision function.
			svmPredictionArray[element].value = svmPredictionArray[element].value + 1.00;
			svm.svm_predict_values(svmModel, svmPredictionArray, decValues);
			higherPointValue = decValues.clone();
			double gradComponentValue = 0.0;
			if (svmModel.nr_class == 2) { // Two class case.
				for (int curDecisionFunc = 0; curDecisionFunc < nOverk; curDecisionFunc++) {
					if (svmModel.rho[curDecisionFunc] > 0.0){ // Check if the decision function is reversed.
						gradComponentValue = gradComponentValue + higherPointValue[curDecisionFunc]-lowerPointValue[curDecisionFunc];
					}
					else{
						gradComponentValue = gradComponentValue + lowerPointValue[curDecisionFunc]-higherPointValue[curDecisionFunc];						
					}
				}
			}
			else {// Take the absolute value and sum up contributions for all models in multi-class cases.
				for (int curDecisionFunc = 0; curDecisionFunc < nOverk; curDecisionFunc++) {
					gradComponentValue = gradComponentValue + Math.abs(higherPointValue[curDecisionFunc]-lowerPointValue[curDecisionFunc]);
				}
			}
			gradientComponents.add(gradComponentValue);
			// Set the value back to what it was.
			svmPredictionArray[element].value = svmPredictionArray[element].value - 1.00;

		}

		String significantSignature="";
		List<Integer> centerAtoms = new ArrayList<Integer>();
		int height = -1;

		if (svmModel.param.svm_type == 0){ // This is a classification model.


			if (svmModel.nr_class == 2) { // Two class case.
				if (prediction > 1.0*(svmModel.nr_class-1)/2.0){ // Look for most positive component.
					double maxComponent = -1.0;
					int elementMaxVal = -1;
					for (int element = 0; element < svmPredictionArray.length; element++){
						if (gradientComponents.get(element) > maxComponent){
							maxComponent = gradientComponents.get(element);
							elementMaxVal = element;
						}
					}
					if (maxComponent > 0.0){
						System.out.println("Max atom: " + predModel.getMoleculeSignaturesAtomNr().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1)) + ", max val: " + gradientComponents.get(elementMaxVal) + ", signature: " + signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1) + ", height: " + predModel.getMoleculeSignaturesHeight().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1)));

						significantSignature=signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1);
						height=predModel.getMoleculeSignaturesHeight().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1));
						centerAtoms=predModel.getMoleculeSignaturesAtomNr().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1));

					}
					else{
						System.out.println("No significant signature.");						
					}
				}
				else{
					double minComponent = 1.0;
					int elementMinVal = -1;
					for (int element = 0; element < svmPredictionArray.length; element++){
						if (gradientComponents.get(element) < minComponent){
							minComponent = gradientComponents.get(element);
							elementMinVal = element;
						}
					}
					if (minComponent < 0.0){
						System.out.println("Min atom: " + predModel.getMoleculeSignaturesAtomNr().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMinVal].index-1)) + ", min val: " + gradientComponents.get(elementMinVal) + ", signature: " + signSvmModel.getModelSignatures().get(svmPredictionArray[elementMinVal].index-1) + ", height: " + predModel.getMoleculeSignaturesHeight().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMinVal].index-1)));

						significantSignature=signSvmModel.getModelSignatures().get(svmPredictionArray[elementMinVal].index-1);
						height=predModel.getMoleculeSignaturesHeight().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMinVal].index-1));
						centerAtoms=predModel.getMoleculeSignaturesAtomNr().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMinVal].index-1));

					}
					else{
						System.out.println("No significant signature.");
					}
				}
			}
			else { // Multi-class case.
				double maxComponent = -1.0;
				int elementMaxVal = -1;
				for (int element = 0; element < svmPredictionArray.length; element++){
					if (gradientComponents.get(element) > maxComponent){
						maxComponent = gradientComponents.get(element);
						elementMaxVal = element;
					}
				}
				if (maxComponent/svmModel.nr_class > 0.01){ // Two avoid flat regions.
					System.out.println("Max atom: " + predModel.getMoleculeSignaturesAtomNr().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1)) + ", max val: " + gradientComponents.get(elementMaxVal) + ", signature: " + signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1) + ", height: " + predModel.getMoleculeSignaturesHeight().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1)));

					significantSignature=signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1);
					height=predModel.getMoleculeSignaturesHeight().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1));
					centerAtoms=predModel.getMoleculeSignaturesAtomNr().get(signSvmModel.getModelSignatures().get(svmPredictionArray[elementMaxVal].index-1));

				}
				else{
					System.out.println("No significant signature.");						
				}

			}


			
			/*=================
			 * Add significant signatures to the match
			 */

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


			}

			//We can have multiple hits...
			//...but here we only have one
//			results.add( match );

		}

		//Else we have a regression model
		//Sum up all atom gradients
		else {
			Map<Integer, Double> atomGreadientComponents = new HashMap<Integer, Double>(); // Contains a sum of all gradient components, based on modelSignatures, for a given atom.
			for (int element = 0; element < svmPredictionArray.length; element++){
				double componentVal = gradientComponents.get(element);
				List<Integer> atomNrList = predModel.getMoleculeSignaturesAtomNr().get(signSvmModel.getModelSignatures().get(svmPredictionArray[element].index-1));
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

			match = new ScaledResultMatch("Result: " 
					+ formatter.format( prediction ), 
					ITestResult.INFORMATIVE);

			int result=ITestResult.INCONCLUSIVE;
			if (regrLowerThreshold!=null && regrUpperThreshold!=null){

				if (prediction<regrLowerThreshold){
					if (lowIsNegative)
						result=ITestResult.NEGATIVE;
					else
						result=ITestResult.POSITIVE;
				}else if (prediction>regrUpperThreshold){
					if (lowIsNegative)
						result=ITestResult.POSITIVE;
					else
						result=ITestResult.NEGATIVE;
				}
				match.setClassification(result);
			}


			System.out.println("Scaled results:");
			//Color atoms according to accumulated gradient values
			for (int currentAtomNr : atomGreadientComponents.keySet()){
				Double currentDeriv = atomGreadientComponents.get(currentAtomNr);

				double scaledDeriv = scaleDerivative(currentDeriv);
				match.putAtomResult( currentAtomNr, scaledDeriv );
				System.out.println("Atom: " + currentAtomNr + " has deriv=" + currentDeriv +" scaled=" + scaledDeriv );

			}

		}

		return results;
	}

	/**
	 * A prediction model tales a mol as input, sets up all signatures needed, 
	 * and packs thie into a model for prediction.
	 * 
	 * @param cdkmol
	 * @return
	 * @throws BioclipseException
	 */
	private PredictionModel createPredModel(ICDKMolecule cdkmol) throws BioclipseException {
		
		ISignaturesManager sign=net.bioclipse.ds.signatures.Activator.
				getDefault().getJavaSignaturesManager();

		PredictionModel predModel = new PredictionModel(cdkmol);
		
		for (int height = startHeight; height <= endHeight; height++){

			//Use the sign manager to generate Signatures for the molecule
			List<String> signs;
			signs = sign.generate( cdkmol, height ).getSignatures();

			Iterator<String> signsIter = signs.iterator();
			int signsIndex = 0;
			while (signsIter.hasNext()){
				String currentSignature = signsIter.next();
				if (signSvmModel.getModelSignatures().contains(currentSignature)){
					if (!predModel.getMoleculeSignaturesAtomNr().containsKey(currentSignature)){
						predModel.getMoleculeSignaturesAtomNr().put(currentSignature, new ArrayList<Integer>());
					}
					predModel.getMoleculeSignaturesHeight().put(currentSignature, height);
					List<Integer> tmpList = predModel.getMoleculeSignaturesAtomNr().get(currentSignature);
					tmpList.add(signsIndex);
					predModel.getMoleculeSignaturesAtomNr().put(currentSignature, tmpList);
					if (predModel.getMoleculeSignatures().containsKey(currentSignature)){
						predModel.getMoleculeSignatures().put(currentSignature, (Double)predModel.getMoleculeSignatures().get(currentSignature)+1.00);
					}
					else{
						predModel.getMoleculeSignatures().put(currentSignature, 1.0);
					}
				}
				signsIndex++;
			}
		}		
		
		return predModel;
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



	/**
	 * Calculate the factorial of n.
	 *
	 * @param n the number to calculate the factorial of.
	 * @return n! - the factorial of n.
	 */
	static int fact(int n) {

		// Base Case: 
		//    If n <= 1 then n! = 1.
		if (n <= 1) {
			return 1;
		}
		// Recursive Case:  
		//    If n > 1 then n! = n * (n-1)!
		else {
			return n * fact(n-1);
		}
	}


}
