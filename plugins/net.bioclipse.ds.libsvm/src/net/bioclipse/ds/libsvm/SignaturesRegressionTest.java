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
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ScaledResultMatch;
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
    private TreeMap<Double, Integer> varsAndDerivs = null;
    
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

//        // Retrieve the decision function value.
//        double lowPointDecisionFuncValue;
//        double[] decValues = new double[1]; // We only have two classes so this should be one. Look in svm_predict_values for an explanation. 
//        svm.svm_predict_values(svmModel, xScaled, decValues);
//        logger.debug("Decision function value: " + decValues[0]);
//        lowPointDecisionFuncValue = decValues[0];
//
//        // Confirm that the libsvm prediction and our prediction give the same result. 
//        if ((decValues[0] * prediction)<0){
//            throw new DSException("Ambiguous result.");
//        }

        // Searching for the 'numberOfVars' largest (absolute valued) gradients
        // The easiest way (which is also reasonably fast) is to collect all gradients in a TreeMap
        varsAndDerivs = new TreeMap<Double, Integer>();
        for (int key : attributeValues.keySet()) {
       		varsAndDerivs.put(partialDerivative(key), key);
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
        
        List<String> signatures = sign.generate( mol ).getSignatures();
        
        //Loop over all generated signature for the current molecule
        int molSignature=0;
        for (String sig : signatures){

            molSignature++;   //Next signature
            
            int modelSignatureNr = 0; //The current signature in the model
            Integer currentAttributeValue = 0;
            
            //Loop over all signatures in model
            for (String signature : signatureList) {
                modelSignatureNr = modelSignatureNr + 1;
                if (signature.equals(sig)){
                    // We have a matching signature. Add 1 to the attribute and append the atomNr to the signature hashmap list.
                    if (attributeValues.containsKey(modelSignatureNr)){
                        currentAttributeValue = (Integer) attributeValues.get(modelSignatureNr);
                        attributeValues.put(modelSignatureNr, new Integer(currentAttributeValue + 1));
                    }
                    else {
                        attributeValues.put(modelSignatureNr, new Integer(1));
                    }
                    if (signatureAtoms.containsKey(signature)){ 
                      signatureAtoms.get(signature).add(molSignature);
                      //System.out.println("Significant atom:" + lineNr);
                      //System.out.println(signature);
                    }
                    else {
                      signatureAtoms.put(signature, new ArrayList<Integer>());
                      signatureAtoms.get(signature).add(molSignature);
                      //System.out.println("Significant atom:" + lineNr);
                      //System.out.println(signature);
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
            return returnError("No signature atoms produced by signaturesrunner"
                               , "");
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

        // Get largest and smallest derivate for scaling (see below)
        Double largestDeriv = varsAndDerivs.lastKey();
        Double smallestDeriv = varsAndDerivs.firstKey();
//        System.out.println("Largest: " + largestDeriv + " - " + " smallest: " + smallestDeriv);
        // Create a new match with correct coloring

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
        int numberOfVars = (int) fractionOfVars*attributeValues.size();
        // Go through all the variables that we want to look at
        for (int i = 0; i < numberOfVars; i++)	{
        	Double currentDeriv = null;
        	// Select absolute valued largest remaining variables
        	if (varsAndDerivs.size()<=0){
              return returnError( "varsAndDerivs was empty","");
        	}
        	if(Math.abs(varsAndDerivs.lastKey()) > 
        	    Math.abs(varsAndDerivs.firstKey()))	{
        		currentDeriv = varsAndDerivs.lastKey();
        	}
        	else	{
        		currentDeriv = varsAndDerivs.firstKey();
        	}
        	Integer currentVar = varsAndDerivs.get(currentDeriv);
        	// Remove current largest derivative
        	varsAndDerivs.remove(currentDeriv);
            // We should add atoms from the input cdkmol, not the clone!
        	// Get center atom
        	Integer tmp = signatureAtoms.get(signatureList[currentVar-1]).get(0);
        	
//        	IAtom currentAtom = cdkmol.getAtomContainer().getAtom(tmp-1);
//        	significantAtomsContainer.addAtom(currentAtom);

//            logger.debug("center atom: " + significantAtom);
//            //Also add all atoms connected to significant atoms to list
//            for (IAtom nbr : cdkmol.getAtomContainer().getConnectedAtomsList(cdkmol.getAtomContainer().getAtom( significantAtom-1 )) ){
//                int nbrAtomNr = cdkmol.getAtomContainer().getAtomNumber(nbr) + 1;
//                IAtom atomToAdd = cdkmol.getAtomContainer().getAtom(nbrAtomNr-1);
//                significantAtomsContainer.addAtom(atomToAdd);
//                
//                //This is where we set the color of the atom. These values are just example.
//                match.putAtomColor( atomToAdd, new Color(100,200,150) );
//                logger.debug("nbr: " + nbrAtomNr);
//            }

          // Scaling. We rescale the derivative to be between -1 and 1.
        	double scaledDeriv = scaleDerivative(currentDeriv);
        	match.putAtomResult( tmp-1, scaledDeriv );
        	System.out.println("Atom " + (tmp-1) + " with pD=" + currentDeriv 
        			+ " is scaled to: " + scaledDeriv);
        	
//        	IAtom currentAtom = cdkmol.getAtomContainer().getAtom(tmp-1);
//        	currentAtom.setProperty(getId(),scaledDeriv);

        	gradients.add(currentDeriv);
          
/*        	
        	
            // Scaling. We rescale the derivative to be between 0 and 1.
            // FIXME: The scaling should probably be done in a more clever way. It is probably more desirable that the scaling isn't relative - now small differences will look "inflated".
            double scaledDeriv = (currentDeriv-smallestDeriv)/(largestDeriv-smallestDeriv);
            // Calculate color
            double frequency = 2*Math.PI;
            int red = (int) Math.round(Math.sin(frequency*scaledDeriv + 0)*127 + 128);
            int green = (int) Math.round(Math.sin(frequency*scaledDeriv + 2*Math.PI/3)*127 + 128);
            int blue = (int) Math.round(Math.sin(frequency*scaledDeriv + 4*Math.PI/3)*127 + 128);
            match.putAtomColor(currentAtom, new Color(red, green, blue));
*/
        	
        }
//        logger.debug("Number of center atoms: " + significantAtoms.size());
        
        //We want to set the color of the hilighting depending on the prediction. If the decision function > 0.0 the color should be red, otherwise it should be green.
        //we also want the filled circles to be larger so that they become visible for non carbons.
//        match.setAtomContainer( significantAtomsContainer );
        
//        match.writeResultsAsProperties(cdkmol.getAtomContainer(), "BOGUS");

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
			return currentDeriv/getLowPercentileDeriv();
		else
			return currentDeriv/getHighPercentileDeriv();
	}

	//Below are model-specific
	public abstract Double getHighPercentileDeriv();
	public abstract Double getLowPercentileDeriv();
}
