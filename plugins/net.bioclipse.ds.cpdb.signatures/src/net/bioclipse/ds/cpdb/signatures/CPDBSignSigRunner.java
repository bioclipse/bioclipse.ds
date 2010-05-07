/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth, Lars Carlsson, Martin Eklund
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
package net.bioclipse.ds.cpdb.signatures;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import net.bioclipse.ds.model.AbstractDSTest;
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
public class CPDBSignSigRunner extends AbstractDSTest implements IDSTest{

    //The logger of the class
    private static final Logger logger = Logger.getLogger(CPDBSignSigRunner.class);

    //The model file
    private static final String MODEL_FILE="/data/cpdbasRatMmol_Sign1.train.scale.model";
    
    // Block of member variables. Can they reside here?
    // Most of the member variables below are hardcoded for the specific example
    //with 198 descriptors.

    // This is an array of the signatures used in the model. 
    //All model specific files resides in the directory ModelSpecificFiles.
    //TODO: read this info from file
    public static final String[] signatureList = {"[C]([C])", "[C]([C][O])", "[O]([C])", "[C]([C][N][O])", "[N]([C])", "[C]([C][C][N])", "[C]([C][C])", "[N]([C][C])", "[C]([C][C][O])", "[N]([C][N])", "[C]([N][N][S])", "[C]([C][N])", "[C]([C][C][S])", "[S]([C][C])", "[O]([C][C])", "[N]([C][O][O])", "[O]([N])", "[N]([C][O])", "[C]([O][O])", "[C]([C][C][C])", "[C]([C][O][O])", "[C]([N])", "[N]([C][C][C])", "[C]([O])", "[C]([C][C][C][O])", "[C]([C][C][H][N])", "[H]([C])", "[C]([C][H][O][O])", "[C]([C][C][C][H])", "[C]([N][S])", "[S]([C])", "[N]([C][C][N])", "[C]([N][N][O])", "[N]([N][O])", "[C]([Br][C][C])", "[Br]([C])", "[C]([N][N][N])", "[C]([C][S])", "[C]([C][N][N])", "[N]([C][S])", "[S]([N][N])", "[C]([C][N][S])", "[C]([C][C][C][C])", "[C]([N][N])", "[O]([C][S])", "[S]([O][O][O])", "[O]([S])", "[C]([C][Cl])", "[Cl]([C])", "[C]([Cl][N][N])", "[N]([N])", "[N]([C][N][O])", "[N]([N][N])", "[O]([C][N])", "[C]([Br][C])", "[C]([Cl][O])", "[C]([Br][Cl][Cl])", "[N]([C][C][S])", "[S]([C][N])", "[C]([C][Cl][Cl][S])", "[C]([C][Cl][Cl])", "[C]([Cl][Cl][Cl][S])", "[C]([N][O][O])", "[C]([Cl][Cl][Cl][Cl])", "[C]([C][C][Cl])", "[C]([C][C][C][Cl])", "[C]([C][C][Cl][Cl])", "[S]([C][N][O][O])", "[C]([C][F][F][F])", "[F]([C])", "[C]([C][Cl][N])", "[C]([Cl][F])", "[C]([Cl][Cl][Cl])", "[N]([C][C][C][C])", "[C]([C][C][N][O])", "[C]([C][C][C][N])", "[N]([C][C][P])", "[P]([N][N][O][O])", "[O]([P])", "[N]([C][P])", "[O]([C][P])", "[S]([C][C][O][O])", "[C]([C][Cl][Cl][Cl])", "[C]([C][Cl][Cl][F])", "[P]([O][O][O][O])", "[N]([C][C][O])", "[C]([N][O])", "[P]([H][O][O][O])", "[H]([P])", "[C]([P])", "[P]([C][O][O][O])", "[P]([N][O][O][O])", "[N]([N][O][O])", "[C]([C][C][F])", "[C]([C][F])", "[P]([N][N][N][O])", "[N]([C][C][C][O])", "[C]([C][C][H])", "[N]([O][O])", "[S]([C][C][O])", "[C]([S])", "[C]([N][S][S])", "[C]([Cl][Cl])", "[S]([C][O][O][O])", "[C]([C][S][S])", "[C]([C][F][F])", "[C]([N][N][N][N])", "[S]([P])", "[P]([N][N][N][S])", "[N]([S])", "[C]([C][C][C][F])", "[C]([C][C][O][O])", "[C]([Br][Br][Br])", "[N]([O][O][O])"};
    public static final int nrSignatures = 114;

    // These variables are defined by the range file.
    //We include them here to avoid parsing the range file.
    //TODO: read this info from file
    public static final double lower = -1.0;
    public static final double upper = 1.0;
    public static final double[] feature_min = {0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0};
    public static final double[] feature_max = {12.0 , 6.0 , 13.0 , 10.0 , 4.0 , 9.0 , 18.0 , 5.0 , 8.0 , 2.0 , 1.0 , 6.0 , 2.0 , 2.0 , 7.0 , 4.0 , 8.0 , 2.0 , 1.0 , 9.0 , 6.0 , 6.0 , 6.0 , 6.0 , 2.0 , 2.0 , 4.0 , 1.0 , 4.0 , 1.0 , 1.0 , 2.0 , 1.0 , 2.0 , 10.0 , 10.0 , 3.0 , 3.0 , 3.0 , 2.0 , 1.0 , 2.0 , 2.0 , 4.0 , 2.0 , 1.0 , 2.0 , 6.0 , 12.0 , 1.0 , 1.0 , 1.0 , 1.0 , 3.0 , 3.0 , 2.0 , 1.0 , 1.0 , 1.0 , 1.0 , 2.0 , 1.0 , 2.0 , 1.0 , 6.0 , 8.0 , 2.0 , 1.0 , 2.0 , 6.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 3.0 , 1.0 , 1.0 , 1.0 , 3.0 , 1.0 , 2.0 , 1.0 , 1.0 , 1.0 , 6.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 2.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 2.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 1.0 , 3.0};

    //The SVM model. Read once on initialization.
    public static svm_model bursiModel;
    
    // The ones below are needed, but they depend on some hardcoded info.
    //We need to avoid this, reading 198.
    private svm_node[] xScaled;
    private double[] x;

    //Instance fields for a prediction
    private Map<Integer,Integer> attributeValues;
    private Map<String,ArrayList<Integer>> signatureAtoms;
//    private ArrayList<Integer> significantAtoms;
    private double prediction;
//    private String significantSignature = "";
    
    // Add control over number of selected variables
    // The present proof-of-principle default is to look at all variables in the regression case
    private double fractionOfVars = 1;	// Selects all variables
    private TreeMap<Double, Integer> varsAndDerivs = null;
    
    //We need to ensure that '.' is always decimal separator in all locales
    DecimalFormat formatter;
    
    /**
     * Default constructor
     */
    public CPDBSignSigRunner(){
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
        //We need to ensure that '.' is always decimal separator in all locales
        DecimalFormatSymbols sym=new DecimalFormatSymbols();
        sym.setDecimalSeparator( '.' );
        formatter = new DecimalFormat("0.00", sym);

        //Get model path
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
            URL url = FileLocator.toFileURL(Platform.getBundle(getPluginID())
                                            .getEntry(MODEL_FILE));
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


    private double partialDerivative(int component){

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

    
    private void predict() throws DSException{
        
        prediction = svm.svm_predict(bursiModel, xScaled);
        
        logger.debug("libsvm prediction: " + prediction);

//        // Retrieve the decision function value.
//        double lowPointDecisionFuncValue;
//        double[] decValues = new double[1]; // We only have two classes so this should be one. Look in svm_predict_values for an explanation. 
//        svm.svm_predict_values(bursiModel, xScaled, decValues);
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

        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();

        //Remove all hydrogens in molecule
        //cdkmol=cdk.removeExplicitHydrogens( cdkmol );
        //cdkmol=cdk.removeImplicitHydrogens( cdkmol );
        //TODO: Should we remove this?

        //Make room for new predicions
        attributeValues=new HashMap<Integer, Integer>();
        signatureAtoms=new HashMap<String, ArrayList<Integer>>();
//        significantAtoms=new ArrayList<Integer>();
        xScaled = new svm_node[nrSignatures];
        x=new double[nrSignatures];

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
        System.out.println("Largest: " + largestDeriv + " - " + " smallest: " + smallestDeriv);
        // Create a new match with correct coloring
//        RGBMatch match = new RGBMatch("Result: " 
//                                      + formatter.format( prediction ), 
//                                      ITestResult.INCONCLUSIVE);
        ScaledResultMatch match = new ScaledResultMatch("Result: " 
                                      + formatter.format( prediction ), 
                                      ITestResult.INCONCLUSIVE);
        IAtomContainer significantAtomsContainer = cdkmol.getAtomContainer()
            .getBuilder().newAtomContainer();
        
        List<Integer> atomNumbers=new ArrayList<Integer>();
        
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
        	
        	//TODO: Verify tmp-1 is atom number. Is Signatures base 1?
        	
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

          // Scaling. We rescale the derivative to be between 1 and 100.
          double scaledDeriv = (currentDeriv-smallestDeriv)/(largestDeriv-smallestDeriv)*100+1;
          match.putAtomResult( tmp-1, (int) scaledDeriv );
        	
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
        
        match.writeResultsAsProperties(cdkmol.getAtomContainer(), 
               net.bioclipse.ds.cpdb.signatures.Activator.CPDB_RESULT_PROPERTY);

        //We can have multiple hits...
        List<ITestResult> results=new ArrayList<ITestResult>();
        //...but here we only have one
        results.add( match );

        return results;
        
    }


}
