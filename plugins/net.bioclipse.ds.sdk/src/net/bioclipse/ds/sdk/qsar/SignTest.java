package net.bioclipse.ds.sdk.qsar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;


public class SignTest {

	//Default values
	private static int nrFolds = 5, startHeight = 0, endHeight = 3;
	
	private static String positiveActivity = "mutagen"; 

	
	//Path to SDF
	//private static String pathToSDFile = "/home/lc/molsWithAct.sdf";
	private static String pathToSDFile = "/home/lc/bursi_nosalts_molsign.sdf";
	private static String ACTIVITY_PROPERTY = "Ames test categorisation";

	private static boolean classification = true;  //Else, regression

	//private static String pathToSDFile = "/home/lc/Downloads/chang.sdf";
	//private static String ACTIVITY_PROPERTY = "BIO";
	//private static boolean classification = false;
	
	//The property in the SDF to read, e. g. as activity
	
	
	
	
	private static void gridSearch(svm_parameter svmParameter, svm_problem svmProblem, double optimumValue, double optimumC, double optimumGamma){
		for (int cExponent = 0; cExponent < 7; cExponent++){
			for (int gammaExponent = 3; gammaExponent < 11; gammaExponent++){
				double[] target = new double[svmProblem.l];
				svmParameter.C = Math.pow(10.0,(cExponent/2));
				svmParameter.gamma = Math.pow(2.0, -gammaExponent);
				System.out.println(svmParameter.C+" : " + svmParameter.gamma);
				svm.svm_cross_validation(svmProblem, svmParameter, nrFolds, target);
		
				if (classification){
					int nrCorrect = 0;
					for (int i = 0; i < svmProblem.l; i++){
						if (target[i] == svmProblem.y[i]){ // Can you compare doubles like this in java or should it be abs(target-y) < eps?
							nrCorrect = nrCorrect + 1;
						}
					}
					double objectiveValue = 1.0*nrCorrect/svmProblem.l;
					if (objectiveValue > optimumValue){
						optimumValue = objectiveValue;
						optimumC = svmParameter.C;
						optimumGamma = svmParameter.gamma;
					}
				}
				else{
					double sumSquareError = 0.0;
					for (int i = 0; i < svmProblem.l; i++){
						sumSquareError = sumSquareError + (target[i] - svmProblem.y[i]) * (target[i] - svmProblem.y[i]);
					}
					double objectiveValue = sumSquareError/svmProblem.l;
					if (objectiveValue < optimumValue){
						optimumValue = objectiveValue;
						optimumC = svmParameter.C;
						optimumGamma = svmParameter.gamma;
					}
				}
				System.out.println("Optimum Value:C:gamma: "+optimumValue+":"+optimumC+":"+optimumGamma);
			}
		}

	}
	
	
	/**
	 * The main class.
	 */
	public static void main(String[] args) throws FileNotFoundException {

		
		System.out.println("Start");

		BufferedReader br = new BufferedReader(new FileReader(new File(pathToSDFile)));
		IteratingMDLReader reader = new IteratingMDLReader(br, NoNotificationChemObjectBuilder.getInstance());

		try {
			List<String> signatures = new ArrayList<String>(); // Contains signatures. We use the indexOf to retrieve the order of specific signatures in descriptor array.
			svm_problem svmProblem = new svm_problem();
			List<Double> activityList = new ArrayList<Double>();
			List<svm_node[]> descriptorList = new ArrayList<svm_node[]>();
			int cnt=1;
			while (reader.hasNext()){
				IMolecule mol = (IMolecule) reader.next();
								
				// Check the activity.
				String activity = (String) mol.getProperty(ACTIVITY_PROPERTY);
				double activityValue = 0.0;
				if (classification){
					if (positiveActivity.equals(activity)){
						activityValue = 1.0;
					}
				}
				else { // Regression
					activityValue = Double.valueOf(activity);
				}
				activityList.add(activityValue);

				// Create the signatures for a molecule and add them to the signatures map
				HashMap moleculeSignatures = new HashMap<String, Double>(); // Contains the signatures for a molecule and the count. We store the count as a double although it is an integer. libsvm wants a double.
				for (int height = startHeight; height <= endHeight; height++){
					List<String> signs = SignTools.calculateSignatures(mol, height);
					Iterator<String> signsIter = signs.iterator();
					while (signsIter.hasNext()){
						String currentSignature = signsIter.next();
						if (signatures.contains(currentSignature)){
							if (moleculeSignatures.containsKey(currentSignature)){
								moleculeSignatures.put(currentSignature, (Double)moleculeSignatures.get(currentSignature)+1.00);
							}
							else{
								moleculeSignatures.put(currentSignature, 1.0);
							}
						}
						else{
							signatures.add(currentSignature);
						}
					}
				}
				// Add the values of the current molecule's signatures as svm data.
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
				descriptorList.add(moleculeArray);
				
				//System.out.println("Molecule " + cnt + " (Activity=" + activity + "): " +  signs);
				cnt++;
			}
			
			//This is the list of signatures for inclusion in model - should go in model file
			System.out.println(signatures);
			
			// Add values to the SVM problem.
			svmProblem.l = cnt - 1;
			svmProblem.x = new svm_node[svmProblem.l][];
			svmProblem.y = new double[svmProblem.l];
			for (int exampleNr = 0; exampleNr < svmProblem.l; exampleNr++){
				svmProblem.x[exampleNr] = descriptorList.get(exampleNr);
				svmProblem.y[exampleNr] = activityList.get(exampleNr);
			}
			
			// Do the grid search to find the best set of gamma for the RBF kernel and C for the cost.
			double optimumValue, optimumC = 1, optimumGamma = 0.01;
			svm_parameter svmParameter = new svm_parameter();
			svmParameter.kernel_type = svm_parameter.RBF;
			svmParameter.cache_size = 1000.0; // Cache size for training in MB.
			svmParameter.eps = 0.001;
			svmParameter.C = optimumC;
			svmParameter.gamma = optimumGamma;
			if (classification){
				svmParameter.svm_type = svm_parameter.C_SVC;
				optimumValue = 0.0;
			}
			else {
				svmParameter.svm_type = svm_parameter.EPSILON_SVR;
				optimumValue = 1000.0;
			}
			
			System.out.println(svm.svm_check_parameter(svmProblem, svmParameter)); //null if OK
			
			gridSearch(svmParameter, svmProblem, optimumValue, optimumC, optimumGamma);
			
			svmParameter.C = optimumC;
			svmParameter.gamma = optimumGamma;
			svm_model svmModel = new svm_model();
			svmModel = svm.svm_train(svmProblem, svmParameter);
			String svmModelName = "/tmp/svmModel.txt";			
			try {
				svm.svm_save_model(svmModelName , svmModel);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		} catch (CDKException e) {
			e.printStackTrace();
		}

		System.out.println("End");

	}

}
