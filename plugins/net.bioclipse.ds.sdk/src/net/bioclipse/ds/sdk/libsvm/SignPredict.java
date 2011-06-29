package net.bioclipse.ds.sdk.libsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.bioclipse.ds.sdk.cdk.CDKHelper;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

/**
 * PLEASE NOTE: This class is deprecated and for standalone use, an integrated class 
 * is available at net.bioclipse.ds.libsvm/SignaturesLibSVMPrediction.java
 * 
 * @author ola
 *
 */
@Deprecated
public class SignPredict {

	/**
	 * @param args
	 */
	private static int startHeight = 0, endHeight = 3; 
	private static String svmModelName = "/tmp/svmModel.txt";
	private static String signaturesFilename = "/tmp/signatures.txt";
	//private static String pathToSDFile = "/home/lc/molsWithAct.sdf";
	private static String pathToSDFile = "/Users/ola/repos/bioclipse.ds/plugins/net.bioclipse.ds.ames/data/bursi_nosalts_molsign.sdf";
//	private static String pathToSDFile = "/Users/ola/Downloads/chang.sdf";

	public static void main(String[] args) throws IOException, CDKException {
		// Predict and create other info related to a prediction.
		
		//Read the signatures file.
		List<String> signatures = new ArrayList<String>(); // Contains signatures. We use the indexOf to retrieve the order of specific signatures in descriptor array.
		try {
			BufferedReader signaturesReader = new BufferedReader(new FileReader(new File(signaturesFilename)));
			String signature;
			while ( (signature = signaturesReader.readLine()) != null ) {
				signatures.add(signature);
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		// Read in the model.
		svm_model svmModel = new svm_model(); 
		try {
			svmModel = svm.svm_load_model(svmModelName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Read the train file here so that near neighbors can be searched.
		
		// Read the compounds to be predicted.
		// Create the signatures for a molecule and add them to the signatures map
		BufferedReader br = new BufferedReader(new FileReader(new File(pathToSDFile)));
		IteratingMDLReader reader = new IteratingMDLReader(br, NoNotificationChemObjectBuilder.getInstance());
		while (reader.hasNext()){
			IMolecule mol = (IMolecule) reader.next();
							
			Map<String, Double> moleculeSignatures = new HashMap<String, Double>(); // Contains the signatures for a molecule and the count. We store the count as a double although it is an integer. libsvm wants a double.
			Map<String, Integer> moleculeSignaturesHeight = new HashMap<String, Integer>(); //Contains the height for a specific signature.
			Map<String, Integer> moleculeSignaturesAtomNr = new HashMap<String, Integer>(); //Contains the atomNr for a specific signature.
			for (int height = startHeight; height <= endHeight; height++){
				List<String> signs = CDKHelper.calculateSignatures(mol, height);
				Iterator<String> signsIter = signs.iterator();
				while (signsIter.hasNext()){
					String currentSignature = signsIter.next();
					if (signatures.contains(currentSignature)){
						moleculeSignaturesHeight.put(currentSignature, height);
						moleculeSignaturesAtomNr.put(currentSignature, signs.indexOf(currentSignature));
						if (moleculeSignatures.containsKey(currentSignature)){
							moleculeSignatures.put(currentSignature, (Double)moleculeSignatures.get(currentSignature)+1.00);
						}
						else{
							moleculeSignatures.put(currentSignature, 1.0);
						}
					}
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
			if (svmModel.param.svm_type == 0){ // This is a classification model.
				if (prediction > 0.0){ // Look for most positive compoent.
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
					}
					else{
						System.out.println("No significant signature.");						
					}
				}
				else{
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
					}
					else{
						System.out.println("No significant signature.");
					}
				}
			}
			else {
				Map<Integer, Double> atomGreadientComponents = new HashMap<Integer, Double>(); // Contains a sum of all gradient components for a given atom.
				for (int element = 0; element < moleculeArray.length; element++){
					int atomNr = moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[element].index-1));
					double componentVal = gradientComponents.get(element);
					if (atomGreadientComponents.containsKey(atomNr)){
						atomGreadientComponents.put(atomNr, atomGreadientComponents.get(atomNr)+componentVal);
					}
					else{
						atomGreadientComponents.put(atomNr,componentVal);
						
					}
				}
				System.out.println(atomGreadientComponents.toString());					
			}
		}
	}

}
