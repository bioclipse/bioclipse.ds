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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import libsvm.svm;
import libsvm.svm_node;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.interfaces.IAtom;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.api.domain.IMolecule;
import net.bioclipse.ds.libsvm.SignaturesLibSVMTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.PosNegIncMatch;
import net.bioclipse.ds.signatures.business.ISignaturesManager;


/**
 * A classification model test for data expressed as Signatures, implemented 
 * using LIBSVM. Superclass is responsible for loading of model, this class 
 * only implements the classification step for a two-class problem.
 * 
 * @author Ola Spjuth, Lars Carlsson, Martin Eklund
 *
 */
public class Signatures2ClassesPredictionTest extends SignaturesLibSVMTest implements IDSTest{
    
    //The logger of the class
    private static final Logger logger = Logger.getLogger(Signatures2ClassesPredictionTest.class);
   
    private svm_node[] xScaled;
    private double[] x;

    //Instance fields for a prediction
    private Map<Integer,Integer> attributeValues;
    private Map<String,ArrayList<Integer>> signatureAtoms;
    private ArrayList<Integer> significantAtoms;
    private double prediction;
    private String significantSignature = "";
    
    /**
     * Default constructor
     */
    public Signatures2ClassesPredictionTest(){
        super();
    }


    private double partialDerivative(int component)
    {
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

        pD = decValues[0];

        return pD;
    }

    
    private void predict() throws DSException
    {
        prediction = svm.svm_predict(svmModel, xScaled);
        
        logger.debug("libsvm prediction: " + prediction);

        // Retrieve the decision function value.
        double lowPointDecisionFuncValue;
        double[] decValues = new double[1]; // We only have two classes so this should be one. Look in svm_predict_values for an explanation. 
        svm.svm_predict_values(svmModel, xScaled, decValues);
        logger.debug("Decision function value: " + decValues[0]);
        lowPointDecisionFuncValue = decValues[0];

        //Confirm that the libsvm prediction and our prediction give the same result. 
        if ((decValues[0] * prediction)<0){
            throw new DSException("Ambiguous result.");
        }


        // For a positive decision function we are looking for the largest positive component of the gradient.
        // For a negative, we are looking for the largest negative component.
        boolean maximum;
        double highPointDecisionFuncValue;
        if (lowPointDecisionFuncValue > 0)
        {
            maximum = true;
        }
        else
        {
            maximum = false;
        }
 
        //NB. We aren't looking for saddle points which is wrong but probably very rare.
        //For example if the lowPointDecisionFuncValue is greater than the highPointDecisionPointValue for a maximum case.
        double extremeValue = 0;
        int significantSignatureNr = 1;
        for (int key : attributeValues.keySet()) {
//            logger.debug("Keys:" + key);
            highPointDecisionFuncValue = partialDerivative(key);
            if (maximum)
            {
                if (extremeValue < highPointDecisionFuncValue)
                {
                    extremeValue = highPointDecisionFuncValue;
                    significantSignatureNr = key;
                }
            }
            else
            {
                if (extremeValue > highPointDecisionFuncValue)
                {
                    extremeValue = highPointDecisionFuncValue;
                    significantSignatureNr = key;
                }
            }
//            logger.debug(highPointDecisionFuncValue);			
        }
        logger.debug("Extreme value: " + extremeValue);
        logger.debug("Keys: " + significantSignatureNr);
        significantSignature = signatureList[significantSignatureNr-1];
        // Make sure significantAtoms is empty.
        significantAtoms.clear();
        if (signatureAtoms==null)
        	throw new DSException("SignatureAtoms was null in SVM prediction");
        for (int i : signatureAtoms.get(signatureList[significantSignatureNr-1])){
        	significantAtoms.add(i-1);
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
                logger.debug("Singature number: " + signatureNr + ", value: " + x[i]);
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
    protected List<? extends ITestResult> doRunTest( ICDKMolecule cdkmol,
                                                     IProgressMonitor monitor ) {

//    	monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

        //Make room for new predicions
        attributeValues=new HashMap<Integer, Integer>();
        signatureAtoms=new HashMap<String, ArrayList<Integer>>();
        significantAtoms=new ArrayList<Integer>();
        xScaled = new svm_node[signatureList.length];
        x=new double[signatureList.length];

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        //Create signatures for this molfile
        try {
            createSignatures( cdkmol);
        } catch ( BioclipseException e1 ) {
            e1.printStackTrace();
            return returnError("Signatures error", e1.getMessage());
        }
        
        //Ensure we have what we need
        if (signatureAtoms==null || signatureAtoms.size()<=0){
        	logger.debug("No matching signature atoms for test: " + getName());
        	return new ArrayList<ITestResult>();
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
        
        PosNegIncMatch match = new PosNegIncMatch(significantSignature, 
        		ITestResult.INCONCLUSIVE);
        if (prediction>0)
            match.setClassification( ITestResult.POSITIVE );
        else
            match.setClassification( ITestResult.NEGATIVE );

        logger.debug("Number of center atoms (significant atoms): " 
        		+ significantAtoms.size());

        for (int significantAtom : significantAtoms){

            int significantAtomNumber=significantAtom;
            
            //We should add atoms from the input cdkmol, not the clone!
            logger.debug("center atom: " + significantAtom);

            //Set each atom to overall match classification
            match.putAtomResult( significantAtomNumber, 
            		match.getClassification() );

            //Also add all atoms connected to significant atoms to list
            for (IAtom nbr : cdkmol.getAtomContainer().getConnectedAtomsList(
             	  cdkmol.getAtomContainer().getAtom( significantAtomNumber )) ){
                int nbrAtomNr = cdkmol.getAtomContainer().getAtomNumber(nbr);
                logger.debug("nbr atom: " + nbrAtomNr);
                
                //Set each atom to overall match classification
                match.putAtomResult( nbrAtomNr, match.getClassification() );
            }
        }
        
        //We can have multiple hits, but here only one
        List<ITestResult> results=new ArrayList<ITestResult>();
        results.add( match );

        return results;
        
    }


}
