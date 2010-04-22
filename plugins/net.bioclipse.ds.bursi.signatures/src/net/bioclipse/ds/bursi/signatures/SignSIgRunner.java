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
package net.bioclipse.ds.bursi.signatures;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.impl.result.ScaledResultMatch;
import net.bioclipse.ds.impl.result.SubStructureMatch;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.signatures.business.ISignaturesManager;


public class SignSIgRunner extends AbstractDSTest implements IDSTest{

    
    
    //The logger of the class
    private static final Logger logger = Logger.getLogger(SignSIgRunner.class);

    //The model file
    private static final String MODEL_FILE="/data/bursiSign_1.train.model";

    // Block of member variables. Can they reside here?
    // Most of the member variables below are hardcoded for the specific example
    //with 198 descriptors.

    // This is an array of the signatures used in the model. 
    //All model specific files resides in the directory ModelSpecificFiles.
    //TODO: read this info from file
    public static final String[] signatureList = {"[C]([C][C][N])", "[C]([C][C][C])", "[N]([C][C])", "[C]([C][C])", "[C]([C][N][N])", "[C]([C][C][O])", "[O]([C][C])", "[C]([O])", "[C]([C])", "[C]([N][O])", "[O]([C])", "[C]([C][C][C][S])", "[S]([C][C][O][O])", "[C]([C][N])", "[N]([C][C][C])", "[C]([C][O][O])", "[C]([C][N][S])", "[O]([S])", "[N]([C][C][N])", "[C]([C][N][O])", "[N]([N][N])", "[N]([C][N])", "[C]([C][O])", "[C]([N])", "[C]([C][C][Cl])", "[Cl]([C])", "[N]([N][O])", "[O]([N])", "[C]([C][S])", "[S]([C][C])", "[N]([C][O][O])", "[C]([Br][C][C])", "[Br]([C])", "[C]([C][C][C][O])", "[N]([C][S])", "[S]([C][N][O][O])", "[C]([S])", "[C]([N][N][N])", "[N]([C])", "[C]([C][C][S])", "[C]([C][C][C][C])", "[C]([C][C][C][N])", "[O]", "[Cl]", "[C]([C][C][F][F])", "[C]([C][Cl][F][F])", "[C]([C][Cl][F])", "[F]([C])", "[C]([C][C][I])", "[I]([C])", "[C]([N][N])", "[S]([C])", "[K]", "[C]([C][Cl][O])", "[N]([C][C][C][N])", "[C]([O][O][O])", "[C]([C][Cl])", "[P]([O][O][O])", "[O]([C][P])", "[N]([C][C][O])", "[C]([N][O][O])", "[N]([N])", "[S]([C][O][O][O])", "[C]([I][I])", "[O]([C][S])", "[C]([Br][C])", "[C]([C][C][F])", "[N]([C][O])", "[C]([O][O])", "[Na]", "[C]([N][N][O])", "[C]([C][C][P])", "[P]([C][O][O][O])", "[O]([P])", "[S]([C][Cl][O][O])", "[Cl]([S])", "[S]([C][N])", "[C]([C][Cl][N])", "[S]([C][S])", "[C]([C][Cl][S])", "[C]([C][Cl][Cl])", "[P]([O][O][O][S])", "[S]([P])", "[P]([O][O][O][O])", "[N]([C][N][O])", "[O]([C][N])", "[N]([C][C][S])", "[C]([Cl][N][N])", "[P]([O][O][S][S])", "[S]([C][P])", "[C]([C][O][S])", "[N]([S])", "[C]([C][F][F][F])", "[C]([C][F])", "[N]([C][N][N])", "[C]([N][S])", "[N]([C][P])", "[P]([N][N][N][N])", "[N]([P][P])", "[S]([C][C][N][O])", "[C]([N][N][S])", "[C]([N][O][S])", "[C]([C][C][Cl][F])", "[N]([C][C][Cl])", "[Cl]([N])", "[P]([N][N][O][O])", "[N]([C][C][P])", "[S]([O][O][O][O])", "[S]([N][N][O][O])", "[C]([C][F][S])", "[N]([O][O][O])", "[O]([C][O])", "[C]([F][F][F][S])", "[O]([O])", "[C]([C][Cl][Cl][Cl])", "[C]([C][C][O][O])", "[N]([C][C][C][C])", "[I]([O][O][O][O])", "[O]([I])", "[C]([C][P])", "[P]([C][C][C][C])", "[C]([O][P])", "[C]([Cl][O][O])", "[P]([N][N][N][S])", "[C]([C][I])", "[N]([N][N][O])", "[C]([Cl][N])", "[N]([N][O][O])", "[Br]", "[C]([Br][C][Cl])", "[C]([N][S][S])", "[S]([C][C][C])", "[P]([N][O][O][O])", "[N]([C][Cl])", "[C]([Cl][O][S])", "[C]([C][O][P][P])", "[C]([C][C][C][Cl])", "[C]([C][C][Cl][Cl])", "[S]([O][O][O])", "[C]([Cl][Cl][Cl][F])", "[C]([Br][Br])", "[C]([Cl][Cl])", "[C]([C][C][N][S])", "[C]([S][S])", "[N]([P])", "[C]([Br][Br][Br][N])", "[C]([C][C][C][F])", "[C]([Cl][S])", "[C]([Cl][Cl][N])", "[N]([O][O])", "[P]([Cl][N][N][O])", "[Cl]([P])", "[C]([Br][C][C][C])", "[P]([N][N][N][O])", "[O]([N][P])", "[C]([Br][Cl])", "[N]([O][S])", "[P]([C][C][C])", "[N]", "[S]([C][C][O])", "[S]([N][O][O][O])", "[N]([N][S])", "[N]([C][C][C][O])", "[Li]", "[Ca]", "[Cl]([O][O][O][O])", "[O]([Cl])", "[S]([N][N])", "[C]([C][C][N][O])", "[C]([Cl][Cl][Cl][S])", "[P]([C][N][O][O])", "[C]([N][P])", "[C]([C][N][P])", "[P]([C][F][O][O])", "[C]([P])", "[F]([P])", "[C]([C][C][S][S])", "[C]([Cl][Cl][Cl][N])", "[C]([C][F][N])", "[C]([Br][Cl][Cl][Cl])", "[N]([O])", "[C]([O][O][S])", "[C]([Br][Br][C])", "[C]([C][C][N][N])", "[P]([N][O][O][S])", "[C]([C][O][P])", "[Br]([N])", "[N]([Br][C][C])", "[C]([C][C][Cl][N])", "[C]([Br])", "[O]([P][P])", "[C]([Cl][N][O])", "[N]([C][S][S])", "[C]([Cl][Cl][F][S])", "[C]([C][F][F][O])", "[C]([C][Cl][Cl][N])", "[C]([S][S][S])", "[C]([N][N][N][N])", "[C]([C][F][F])", "[C]([C][S][S])", "[P]([C][Cl][O][O])", "[C]([C][O][O][O])", "[C]([C][N][O][O])"};
    public static final int nrSignatures = 203;

    // These variables are defined by the range file.
    //We include them here to avoid parsing the range file.
    //TODO: read this info from file
    public static final double lower = -1.0;
    public static final double upper = 1.0;
    public static final double[] feature_min = {0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0};
    public static final double[] feature_max = {23.0 , 16.0 , 32.0 , 37.0 , 4.0 , 23.0 , 14.0 , 6.0 , 12.0 , 2.0 , 39.0 , 1.0 , 2.0 , 10.0 , 6.0 , 7.0 , 4.0 , 12.0 , 2.0 , 29.0 , 2.0 , 8.0 , 15.0 , 6.0 , 8.0 , 12.0 , 2.0 , 12.0 , 5.0 , 4.0 , 6.0 , 10.0 , 10.0 , 4.0 , 2.0 , 2.0 , 4.0 , 6.0 , 13.0 , 4.0 , 6.0 , 2.0 , 3.0 , 2.0 , 8.0 , 1.0 , 1.0 , 19.0 , 6.0 , 6.0 , 6.0 , 2.0 , 2.0 , 2.0 , 1.0 , 2.0 , 6.0 , 2.0 , 6.0 , 2.0 , 2.0 , 2.0 , 4.0 , 1.0 , 2.0 , 3.0 , 5.0 , 2.0 , 3.0 , 4.0 , 3.0 , 3.0 , 2.0 , 8.0 , 1.0 , 1.0 , 2.0 , 2.0 , 2.0 , 1.0 , 2.0 , 2.0 , 2.0 , 2.0 , 1.0 , 4.0 , 2.0 , 2.0 , 1.0 , 2.0 , 1.0 , 1.0 , 2.0 , 1.0 , 2.0 , 2.0 , 6.0 , 3.0 , 3.0 , 1.0 , 2.0 , 1.0 , 3.0 , 3.0 , 3.0 , 1.0 , 3.0 , 1.0 , 1.0 , 1.0 , 4.0 , 4.0 , 1.0 , 1.0 , 2.0 , 3.0 , 1.0 , 1.0 , 4.0 , 1.0 , 1.0 , 4.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 2.0 , 2.0 , 1.0 , 3.0 , 1.0 , 1.0 , 8.0 , 2.0 , 1.0 , 1.0 , 1.0 , 1.0 , 2.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 2.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 4.0 , 1.0 , 2.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 2.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 2.0};

    //The SVM model. Read once on initialization.
    public static svm_model bursiModel;
    
    // The ones below are needed, but they depend on some hardcoded info.
    //We need to avoid this, reading 198.
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
    public SignSIgRunner(){
        super();
    }


    /**
     * Initialize all paths and read model files into memory.
     * @throws DSException 
     */
    public void initialize(IProgressMonitor monitor) throws DSException {

        if (getTestErrorMessage().length()>1){
            logger.error("Trying to initialize test: " + getName() + " while " +
                "error message exists");
            return;
        }

        //Get model path depending on OS
        String modelPath = getModelPath();
        logger.debug( "Model file path is: " + modelPath );
        
        //So, load the model file into memory
        //===================================
        try {
            bursiModel = svm.svm_load_model(modelPath);
        } catch (IOException e) {
            throw new DSException("Could not read model file '" + modelPath 
                                  + "' due to: " + e.getMessage());
        }
        
        //Verify that the signatures file is accessible
        //TODO
        
        // Add the reading of the range file and set up the related variables.
        //TODO
    }

    
    /**
     * Get the path of the model file in plugin directory
     * @return path to model file.
     * @throws DSException
     */
    private String getModelPath() throws DSException {

        String modelPath="";
        try {
            URL url = FileLocator.toFileURL(Platform.getBundle(getPluginID()).getEntry(MODEL_FILE));
            modelPath=url.getFile();
        } catch ( IOException e1 ) {
            throw new DSException("Could not read model file: " + MODEL_FILE 
                                  + ". Reason: " + e1.getMessage());
        }

        //File could not be read
        if ("".equals( modelPath )){
            throw new DSException("Could not read model file: " + MODEL_FILE);
        }
        
        return modelPath;
    }



    private double partialDerivative(int component)
    {
        // Component numbering starts at 1.
        double pD, xScaledCompOld;
        xScaledCompOld = xScaled[component-1].value; // Store the old component so we can copy it back.

        // Forward difference high value point.
        xScaled[component-1].value = lower + (upper-lower) * 
        (1.0+x[component-1]-feature_min[component-1])/
        (feature_max[component-1]-feature_min[component-1]);

        // Retrieve the decision function value.
        double[] decValues = new double[1]; // We only have two classes so this should be one. Look in svm_predict_values for an explanation. 
        svm.svm_predict_values(bursiModel, xScaled, decValues);

        xScaled[component-1].value = xScaledCompOld;

        pD = decValues[0];

        return pD;
    }

    
    private void predict() throws DSException
    {
        prediction = svm.svm_predict(bursiModel, xScaled);
        
        logger.debug("libsvm prediction: " + prediction);

        // Retrieve the decision function value.
        double lowPointDecisionFuncValue;
        double[] decValues = new double[1]; // We only have two classes so this should be one. Look in svm_predict_values for an explanation. 
        svm.svm_predict_values(bursiModel, xScaled, decValues);
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
        for (int i : signatureAtoms.get(signatureList[significantSignatureNr-1])){
        	significantAtoms.add(i-1);
        }
    }


    private void scale()
    {
        //Initialize xScaled. In this case the lower value is -1. This is defined in the range file.
        for (int i = 0; i < nrSignatures; i++){
            xScaled[i] = new svm_node();
            xScaled[i].index = i + 1;
            xScaled[i].value = lower + (upper-lower) * 
            (x[i]-feature_min[i])/
            (feature_max[i]-feature_min[i]);
       }
    }


    private void predictAndComputeSignificance() throws DSException {
        logger.debug("Predicting and computing significance.");

        // The unscaled attributes. 
        for (int i = 0; i < nrSignatures; i++){
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
        
        List<String> signatures = sign.generate( mol );
        
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

        //Remove all hydrogens in molecule
        //cdkmol=cdk.removeExplicitHydrogens( cdkmol );
        //cdkmol=cdk.removeImplicitHydrogens( cdkmol );

        //Make room for new predicions
        attributeValues=new HashMap<Integer, Integer>();
        signatureAtoms=new HashMap<String, ArrayList<Integer>>();
        significantAtoms=new ArrayList<Integer>();
        xScaled = new svm_node[nrSignatures];
        x=new double[nrSignatures];

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
        if (signatureAtoms.size()<=0){
            return returnError("No signature atoms produced by signaturesrunner", "");
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
        
        //Remove the temp file
//        tempfile.delete();

        //Create a new match with correct coloring
//        SubStructureMatch match = new SubStructureMatch(significantSignature,ITestResult.INCONCLUSIVE);
        ScaledResultMatch match = new ScaledResultMatch(significantSignature,ITestResult.INCONCLUSIVE);
        
        if (prediction>0)
            match.setClassification( ITestResult.POSITIVE );
        else
            match.setClassification( ITestResult.NEGATIVE );
            
        IAtomContainer significantAtomsContainer=cdkmol.getAtomContainer().getBuilder().newAtomContainer();
        for (int significantAtom : significantAtoms){

            //We should add atoms from the input cdkmol, not the clone!
            significantAtomsContainer.addAtom( cdkmol.getAtomContainer().getAtom( significantAtom-1 ));
            logger.debug("center atom: " + significantAtom);
            //Also add all atoms connected to significant atoms to list
            for (IAtom nbr : cdkmol.getAtomContainer().getConnectedAtomsList(cdkmol.getAtomContainer().getAtom( significantAtom-1 )) ){
                int nbrAtomNr = cdkmol.getAtomContainer().getAtomNumber(nbr) + 1;
                IAtom atomToAdd = cdkmol.getAtomContainer().getAtom(nbrAtomNr-1);
                significantAtomsContainer.addAtom(atomToAdd);
                logger.debug("nbr: " + nbrAtomNr);
                
                //Set to max for scaledresult
                match.putAtomResult( atomToAdd, 100 );
            }
        }
        logger.debug("Number of center atoms: " + significantAtoms.size());
        
        //We want to set the color of the hilighting depending on the prediction. If the decision function > 0.0 the color should be red, otherwise it should be green.
        //we also want the filled circles to be larger so that they become visible for non carbons.
        match.setAtomContainer( significantAtomsContainer );
        match.writeResultsAsProperties( cdkmol.getAtomContainer(), 
             net.bioclipse.ds.bursi.signatures.Activator.BURSI_RESULT_PROPERTY);

        //We can have multiple hits...
        List<ITestResult> results=new ArrayList<ITestResult>();
        //...but here we only have one
        results.add( match );

        return results;
        
    }


}
