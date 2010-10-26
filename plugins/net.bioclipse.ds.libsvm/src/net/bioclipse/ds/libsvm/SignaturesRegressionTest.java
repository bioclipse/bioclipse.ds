/*******************************************************************************
 * Copyright (c) 2009-2010 Ola Spjuth, Lars Carlsson, Martin Eklund
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     Lars Carlsson
 *     Martin Eklund
 ******************************************************************************/
package net.bioclipse.ds.libsvm;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import libsvm.svm;
import libsvm.svm_node;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.interfaces.IAtom;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.api.domain.IMolecule;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ScaledResultMatch;
import net.bioclipse.ds.model.result.SimpleResult;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

/**
 * 
 * @author Ola Spjuth, Lars Carlsson, Martin Eklund
 *
 */
public abstract class SignaturesRegressionTest extends SignaturesLibSVMTest implements IDSTest{

    //The logger of the class
    private static final Logger logger = Logger.getLogger(SignaturesRegressionTest.class);
    
    private svm_node[] xScaled;
    private double[] x;

    //Instance fields for a prediction
    private Map<Integer,Integer> attributeValues;
    private Map<String,ArrayList<Integer>> signatureAtoms;
    private double prediction;
    
    // Add control over number of selected variables
    // The present proof-of-principle default is to look at all variables in the regression case
    private double fractionOfVars = 1;	// Selects all variables
    private Map<Integer, Double> varsAndDerivs = null;
    private Map<Integer, Integer> atomNrAndVars = null;
    
    //We need to ensure that '.' is always decimal separator in all locales
    DecimalFormat formatter=new DecimalFormat("0.000");
    
    /**
     * Default constructor
     */
    public SignaturesRegressionTest(){
        super();
    }




    private double partialDerivative(int component){

        // Component numbering starts at 1.
        double pD, xScaledCompOld;
        xScaledCompOld = xScaled[component-1].value; // Store the old component so we can copy it back.

        // Forward difference high value point.
        xScaled[component-1].value = lower_range + (upper_range-lower_range) * 
        (1.0+x[component-1]-feature_min[component-1])/
        (feature_max[component-1]-feature_min[component-1]);

        // Retrieve the decision function value.
        double[] decValues = new double[1]; // We only have two classes so this should be one. Look in svm_predict_values for an explanation. 
        svm.svm_predict_values(svmModel, xScaled, decValues);

        xScaled[component-1].value = xScaledCompOld;

        pD = decValues[0]-prediction;

        return pD;
    }

    
    private void predict() throws DSException{
        
        prediction = svm.svm_predict(svmModel, xScaled);
        
        logger.debug("libsvm prediction: " + prediction);

        // Collect the model atribute numbers as key and the corresponding component of the gradient. 
        // A TreeMap is probably not needed but rather a HashMap. But it probably won't slow things down much.
        varsAndDerivs = new HashMap<Integer, Double>();
        for (int key : attributeValues.keySet()) {
       		varsAndDerivs.put(key, partialDerivative(key));
        }
    }


    private void scale()
    {
        //Initialize xScaled. In this case the lower value is -1. This is defined in the range file.
        for (int i = 0; i < signatureList.length; i++){
            xScaled[i] = new svm_node();
            xScaled[i].index = i + 1;
            xScaled[i].value = lower_range + (upper_range-lower_range) * 
            (x[i]-feature_min[i])/
            (feature_max[i]-feature_min[i]);
       }
    }


    private void predictAndComputeSignificance() throws DSException {
        logger.debug("Predicting and computing significance.");

        // The unscaled attributes. 
        for (int i = 0; i < signatureList.length; i++){
            int signatureNr = i + 1;
            if (attributeValues.containsKey(signatureNr) ){
                x[i] = attributeValues.get(signatureNr);
//                logger.debug("Singature number: " + signatureNr + ", value: " + x[i]);
            }
            else{
                x[i] = 0.0;
            }
        }
        // Do a scaling.
        scale();
        
        // Predict
        predict();

    }

    /**
     * Use the plugin net.bioclipse.signatures to generate signatures
     * @param mol Molecule to generate signatures for
     * @throws BioclipseException
     */
    private void createSignatures(IMolecule mol) throws BioclipseException{

        ISignaturesManager sign=net.bioclipse.ds.signatures.Activator.
                getDefault().getJavaSignaturesManager();
        atomNrAndVars = new HashMap<Integer, Integer>();
        
        List<String> signatures = sign.generate( mol ).getSignatures();
        
        //Loop over all generated signatures for the current molecule
        int molSignature=0;
        for (String sig : signatures){

            molSignature++;   //Next signature
            
            int modelSignatureNr = 0; //The current signature in the model
            Integer currentAttributeValue = 0;
            
            //Loop over all signatures in model
            for (String signature : signatureList) {
                modelSignatureNr = modelSignatureNr + 1;
                if (signature.equals(sig)){
                    // We have a matching signatures. Add 1 to the attribute and append the atomNr to the signature hashmap list.
                	atomNrAndVars.put(molSignature, modelSignatureNr);
                	if (attributeValues.containsKey(modelSignatureNr)){
                        currentAttributeValue = (Integer) attributeValues.get(modelSignatureNr);
                        attributeValues.put(modelSignatureNr, new Integer(currentAttributeValue + 1));
                    }
                    else {
                        attributeValues.put(modelSignatureNr, new Integer(1));
                    }
                    if (signatureAtoms.containsKey(signature)){ 
                      signatureAtoms.get(signature).add(molSignature);
                    }
                    else {
                      signatureAtoms.put(signature, new ArrayList<Integer>());
                      signatureAtoms.get(signature).add(molSignature);
                    }
                }
            }
        }
    }
        

    @Override
    public String toString() {
        return getName();
    }


    @Override
    protected List<? extends ITestResult> doRunTest( ICDKMolecule cdkmol,
                                                     IProgressMonitor monitor ) {

//    	monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

        //Make room for new predicions
        attributeValues=new HashMap<Integer, Integer>();
        signatureAtoms=new HashMap<String, ArrayList<Integer>>();
//        significantAtoms=new ArrayList<Integer>();
        xScaled = new svm_node[signatureList.length];
        x=new double[signatureList.length];

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        //Create signatures for this molecule
        try {
            createSignatures( cdkmol);
        } catch ( BioclipseException e1 ) {
            return returnError("Signatures error", e1.getMessage());
        }
        
        //Ensure we have what we need
        if (signatureAtoms.size()<=0){
        	logger.debug("No matching signature atoms for test: " + getName());
        	return new ArrayList<ITestResult>();
//            return returnError("No matching signature atoms", "");
        }

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        //Predict using the signatureatoms and attributevalues
        try {
            predictAndComputeSignificance();
        } catch ( DSException e ) {
            return returnError( e.getMessage(),"");
        }
        
        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");


        //A positive valued prediction means we were able to predicta TD50.
        ScaledResultMatch match = new ScaledResultMatch("Result: " 
                                      + formatter.format( prediction ), 
                                      ITestResult.POSITIVE);
        
        //Neg prediction means green - Negative results!
        if (prediction<=0)
        	match.setClassification(ITestResult.NEGATIVE);
        
        //For debugging of gradients
        List<Double> gradients=new ArrayList<Double>();
        
        // How many variabels do we want to look at?
        // Go through all the variables that we want to look at
        for (int key : attributeValues.keySet()) {
        	Double currentDeriv = null;
        	//Integer currentAtomNr = null;
        	if (varsAndDerivs.size()<=0){
              return returnError( "varsAndDerivs was empty","");
        	}

        	currentDeriv = varsAndDerivs.get(key);
        	for (int currentAtomNr : atomNrAndVars.keySet()) {
        		if (atomNrAndVars.get(currentAtomNr) == key) {
        			// We should add atoms from the input cdkmol, not the clone!
        			// Get center atom
        			//Integer tmp = signatureAtoms.get(signatureList[currentVar-1]).get(0);
        	
        			// Scaling. We rescale the derivative to be between -1 and 1.
        			double scaledDeriv = scaleDerivative(currentDeriv);
        			match.putAtomResult( currentAtomNr-1, scaledDeriv );
        			logger.debug("Atom " + (currentAtomNr-1) + " with pD=" + currentDeriv 
        					+ " is scaled to: " + scaledDeriv + " and has model signature nr: " + key + ", with signature: " + signatureList[key-1]);
        	
        			gradients.add(currentDeriv);
        		}
        	}
        }
        //We can have multiple hits...
        List<ITestResult> results=new ArrayList<ITestResult>();
        //...but here we only have one
        results.add( match );

        //For debugging all gradients to stdout
        String s = gradients.toString();
        System.out.print(s.substring(1,s.length()-1) + ",");

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
		if (currentDeriv<=getLowPercentileDeriv())
			return -1;
		else if (currentDeriv>=getHighPercentileDeriv())
			return 1;
		else if (currentDeriv==0)
			return 0;

		//Since not symmetric around 0, scale pos and neg intervals individually
		if (currentDeriv<0)
			return currentDeriv/(-getLowPercentileDeriv());
		else
			return currentDeriv/getHighPercentileDeriv();
	}

	//Below are model-specific
	public abstract Double getHighPercentileDeriv();
	public abstract Double getLowPercentileDeriv();
}
