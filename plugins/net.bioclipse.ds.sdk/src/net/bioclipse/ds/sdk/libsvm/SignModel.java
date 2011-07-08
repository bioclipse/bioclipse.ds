package net.bioclipse.ds.sdk.libsvm;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import net.bioclipse.ds.sdk.Stopwatch;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;


public class SignModel {

	private static final double TEST_SET_PART = 0.2;

	//Fields with default values
	private int nrFolds = 5, startHeight = 0, endHeight = 3;
	private int cStart = 0, cEnd = 5, gammaStart = 3, gammaEnd = 10;
//	private int cStart = 2, cEnd = 3, gammaStart = 5, gammaEnd = 7; //FIXME: Remove line!
	private int noParallelJobs=1;

	//Fields without default values, set by constructor
	private String positiveActivity; 
	private String pathToSDFile;
	private boolean classification;
	private String activityProperty;
	private String outputDir;

	//Filled by logic
	private String out_svmModelName;
	private String out_signaturesFilename;
	private String out_trainFilename;
	private String out_optmimizationFilename;
	private String optimizationType;
	private List<Point> arrayOptimizationParams;
	private boolean trainFinal = true;
	private String parallelType;
	private double gammafinal;
	private double cfinal;
	private boolean echoTime=true;
	private String jarpath;

	

	//Path to SDF
	//private static String pathToSDFile = "molsWithAct.sdf";

	//AMES
	//	private static String pathToSDFile = "bursi_nosalts_molsign.sdf";
	//	private static String ACTIVITY_PROPERTY = "Ames test categorisation";
	//	private static boolean classification = true;
	//	private static String positiveActivity = "mutagen"; 

	//AHR
	//	private static String pathToSDFile = "2796_nosalts_molsign.sdf";
	//	private static String ACTIVITY_PROPERTY = "c#Activity";
	//	private static boolean classification = true;
	//	private static String positiveActivity = "2"; 

	//CPDB
	//	private static String pathToSDFile = "cpdbForRegression.sdf";
	//	private static String ACTIVITY_PROPERTY = "TD50_Rat_mmol";
	//	private static boolean classification = false;

	//OTHER?
	//private static String pathToSDFile = "chang.sdf";
	//private static String ACTIVITY_PROPERTY = "BIO";
	//private static boolean classification = false;

	//private static String pathToSDFile = "/home/lc/hERG_train.sdf";
	//private static String ACTIVITY_PROPERTY = "field_1";
	//private static boolean classification = false;

	//The property in the SDF to read, e. g. as activity



	
	public int getNrFolds() {
		return nrFolds;
	}

	public String getJarpath() {
		return jarpath;
	}

	public void setJarpath(String jarpath) {
		this.jarpath = jarpath;
	}

	public boolean isEchoTime() {
		return echoTime;
	}

	public void setEchoTime(boolean echoTime) {
		this.echoTime = echoTime;
	}

	public double getGammafinal() {
		return gammafinal;
	}

	public void setGammafinal(double gammafinal) {
		this.gammafinal = gammafinal;
	}

	public double getCfinal() {
		return cfinal;
	}

	public void setCfinal(double cfinal) {
		this.cfinal = cfinal;
	}

	public void setNrFolds(int nrFolds) {
		this.nrFolds = nrFolds;
	}

	public int getStartHeight() {
		return startHeight;
	}

	public void setStartHeight(int startHeight) {
		this.startHeight = startHeight;
	}

	public int getEndHeight() {
		return endHeight;
	}

	public void setEndHeight(int endHeight) {
		this.endHeight = endHeight;
	}

	public int getcStart() {
		return cStart;
	}

	public void setcStart(int cStart) {
		this.cStart = cStart;
	}

	public int getcEnd() {
		return cEnd;
	}

	public void setcEnd(int cEnd) {
		this.cEnd = cEnd;
	}

	public int getGammaStart() {
		return gammaStart;
	}

	public void setGammaStart(int gammaStart) {
		this.gammaStart = gammaStart;
	}

	public int getGammaEnd() {
		return gammaEnd;
	}

	public void setGammaEnd(int gammaEnd) {
		this.gammaEnd = gammaEnd;
	}

	public int getNoParallelJobs() {
		return noParallelJobs;
	}

	public void setNoParallelJobs(int noParallelJobs) {
		this.noParallelJobs = noParallelJobs;
	}

	public String getPositiveActivity() {
		return positiveActivity;
	}

	public void setPositiveActivity(String positiveActivity) {
		this.positiveActivity = positiveActivity;
	}

	public String getPathToSDFile() {
		return pathToSDFile;
	}

	public void setPathToSDFile(String pathToSDFile) {
		this.pathToSDFile = pathToSDFile;
	}

	public boolean isClassification() {
		return classification;
	}

	public void setClassification(boolean classification) {
		this.classification = classification;
	}

	public String getActivityProperty() {
		return activityProperty;
	}

	public void setActivityProperty(String activityProperty) {
		this.activityProperty = activityProperty;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getOptimizationType() {
		return optimizationType;
	}

	public void setOptimizationType(String optimizationType) {
		this.optimizationType = optimizationType;
	}

	public List<Point> getArrayOptimizationParams() {
		return arrayOptimizationParams;
	}

	public void setArrayOptimizationParams(List<Point> arrayOptimizationParams) {
		this.arrayOptimizationParams = arrayOptimizationParams;
	}

	public boolean isTrainFinal() {
		return trainFinal;
	}

	public void setTrainFinal(boolean trainFinal) {
		this.trainFinal = trainFinal;
	}

	public String getParallelType() {
		return parallelType;
	}

	public void setParallelType(String parallelType) {
		this.parallelType = parallelType;
	}



	public SignModel(String pathToSDFile, String activityProperty, String outputDir, boolean classification) {
		this();
//		this.positiveActivity = positiveActivity;
		this.pathToSDFile = pathToSDFile;
		this.activityProperty = activityProperty;
		this.classification = classification;
		this.outputDir=outputDir;
	}

	public SignModel() {

		//Use current dir as default output
		if (outputDir==null || outputDir.isEmpty()){
			outputDir= System.getProperty("user.dir");;  //should not end with slash
		}

		updateOutputFilenames();

	}

	private void updateOutputFilenames() {

		out_svmModelName = outputDir + "/svmModel.txt";
		out_signaturesFilename = outputDir + "/signatures.txt";
		out_trainFilename = outputDir + "/train.txt";
		out_optmimizationFilename = outputDir + "/optimization.txt";

	}

	public OptimizationResult BuildModel() throws IOException{
		assertParameters();

		//==============================
		// Assert input and output files
		//==============================
		File sdfile = new File(pathToSDFile);
		if (!sdfile.canRead()){
			throw new IllegalArgumentException("Cannot read file: " + sdfile.getAbsolutePath());
		}

		File outFolder = new File(outputDir);
		if (!outFolder.exists()){
			if (!outFolder.mkdir())
				throw new IllegalArgumentException("Could not create directory: " + outFolder.getAbsolutePath());
		}
		outFolder.setWritable(true, false);

		File trainFile = new File(out_trainFilename);
		if (!trainFile.exists()){
			if (!trainFile.createNewFile())
				throw new IllegalArgumentException("Could not create file: " + trainFile.getAbsolutePath());
		}
		if (!trainFile.canWrite()){
			throw new IllegalArgumentException("Cannot write output file: " + trainFile.getAbsolutePath());
		}

		File signFile = new File(out_signaturesFilename);
		if (!signFile.exists()){
			if (!signFile.createNewFile())
				throw new IllegalArgumentException("Could not create file: " + signFile.getAbsolutePath());
		}
		if (!signFile.canWrite()){
			throw new IllegalArgumentException("Cannot write output file: " + signFile.getAbsolutePath());
		}

		File optFile = new File(out_optmimizationFilename);
		if (!optFile.exists()){
			if (!optFile.createNewFile())
				throw new IllegalArgumentException("Could not create file: " + optFile.getAbsolutePath());
		}
		if (!optFile.canWrite()){
			throw new IllegalArgumentException("Cannot write output file: " + optFile.getAbsolutePath());
		}

		//Assume we can write the SVM file for now.


		//Set up values to read from input file with mols
		BufferedReader br = new BufferedReader(new FileReader(sdfile));
		IteratingMDLReader molReader = new IteratingMDLReader(br, NoNotificationChemObjectBuilder.getInstance());
		List<String> signatures = new ArrayList<String>(); // Contains signatures. We use the indexOf to retrieve the order of specific signatures in descriptor array.
		List<Double> activityList = new ArrayList<Double>();
		List<svm_node[]> descriptorList = new ArrayList<svm_node[]>();

		//======================
		// READ INPUT FILE
		// 
		// Parse SDF into an SVM matrix
		// Also write a train-file of the descriptors in libsvm format
		//======================
		System.out.println("=== Parsing input file...");
		try {
			processInputFile(trainFile, molReader, signatures,
					activityList, descriptorList);
		} catch (CDKException e2) {
			System.err.println("Error processing input file: " + e2.getMessage());
			System.err.println("Exiting");
			System.exit(1);
		}

		//Check if we have sound values, e.g. all should not be 0
		if (isActivityListSane(activityList)==false){
			System.out.println("Classification is selected but no positive activity found as property. Misspelled?");
			System.out.println("Aborted.");
			System.exit(1);
		}

		// Write the signatures to a file, One per line.
		try {
			BufferedWriter signaturesWriter = new BufferedWriter(new FileWriter(signFile));
			Iterator<String> signaturesIter = signatures.iterator();
			while (signaturesIter.hasNext()){
				signaturesWriter.write(signaturesIter.next());
				signaturesWriter.newLine();
			}
			signaturesWriter.close();
		} catch (IOException e1) {
			System.out.println("Error writing signatures to file: " + e1.getMessage());
			System.out.println("Aborted.");
			System.exit(1);
		}

		//We now do the following approach:
		// 1. Split dataset in a trainingset and testset
		// 2. Estimate C and gamma using a grid-search on the trainingset, build model A on best estimates
		// 3. Predict testset on model A - this produces an external accuracy estimate
		// 4. Estimate C and gamma using a grid-search on the entire dataset, build model B on best estimates
		
		
		//========================================
		//== 1. Split dataset in training and test
		//========================================
		System.out.println("=== Setting up trainingset and testset...");
		int datasetLength=descriptorList.size();
		int testLength = (int) (datasetLength * TEST_SET_PART);
		int trainingLength=datasetLength-testLength;
		System.out.println("length of dataset: " + datasetLength);
		System.out.println("length of trainingset: " + trainingLength);
		System.out.println("length of testset: " + testLength);

		List<Double> activityList_train = new ArrayList<Double>();
		List<svm_node[]> descriptorList_train = new ArrayList<svm_node[]>();
		List<Double> activityList_test = new ArrayList<Double>();
		List<svm_node[]> descriptorList_test = new ArrayList<svm_node[]>();
		
		//Draw randomly entries from dataset into TESTSET
		//Keep track of what we have drawn
		Random generator = new Random();
		List<Integer> drawnIndices=new ArrayList<Integer>();
		for (int i = 0; i < testLength;i++){
			int ix = generator.nextInt(datasetLength); //Our next random index to extract
			while (drawnIndices.contains(ix)) //Draw until we find a non-drawn already
				ix = generator.nextInt(datasetLength);

			activityList_test.add(activityList.get(ix));
			descriptorList_test.add(descriptorList.get(ix));
			drawnIndices.add(ix);
		}
		//Put all non-drawn in TRAININGSET
		for (int i=0; i<descriptorList.size(); i++){
			if (!drawnIndices.contains(i)){
				activityList_train.add(activityList.get(i));
				descriptorList_train.add(descriptorList.get(i));
			}
		}
		//Assert correct dimensions
		if (activityList_test.size()!=testLength || activityList_train.size()!=trainingLength){
			System.out.println("Incorrect division into train/test sets!");
			System.out.println("Aborted.");
			System.exit(1);
		}
		
		//====================================
		// 2. DO GRID SEARCH ON TRAININGSET
		//====================================
		System.out.println("=== Starting grid search on trainingset...");
		svm_problem svmProblem_train = new svm_problem();
		svm_parameter svmParameter_train = new svm_parameter();
		OptimizationResult optResTrain = setUpAndRunGridSearch(activityList_train,
															   descriptorList_train, 
															   svmProblem_train, 
															   svmParameter_train);

		//Train a model on the obtained parameter estimates
		System.out.println("=== Training best model based on trainingset (C=" 
				+ optResTrain.getOptimumC()
				+ ", gamma=" + optResTrain.getOptimumGamma() + ", ObjectiveValue=" + optResTrain.getOptimumValue() + ")...");
		svmParameter_train.C = optResTrain.getOptimumC();
		svmParameter_train.gamma = optResTrain.getOptimumGamma();
		svm_model svmModel_train = svm.svm_train(svmProblem_train, svmParameter_train);

		//====================================
		// 3. PREDICT TESTSET
		//====================================
		System.out.println("=== Predicting testset...");
		//Predict all entries in the testset
		int noCorrect=0;
		for (int i=0; i< testLength; i++){
			svm_node[] testArray = descriptorList_test.get(i);
			double prediction = svm.svm_predict(svmModel_train, testArray);
			if (prediction==activityList_test.get(i))
				noCorrect++;
		}
		double externalAccuracy = 1.0*noCorrect/testLength;
		System.out.println("=== External prediction accuracy: " + externalAccuracy);
		
		//====================================
		// 4. DO GRID SEARCH ON COMPLETE DATASET
		//====================================
		System.out.println("=== Starting grid search on complete dataset...");
		svm_problem svmProblem_complete = new svm_problem();
		svm_parameter svmParameter_complete = new svm_parameter();
		OptimizationResult optResComplete = setUpAndRunGridSearch(activityList_train,
															   descriptorList_train, 
															   svmProblem_complete, 
															   svmParameter_complete);
		
		//======================
		// TRAIN FINAL MODEL
		//
		// We use the obtained parameter estimates from GRID SEARCH on complete dataset 
		// and train on the complete training set 
		//======================
		if (trainFinal){
			System.out.println("=== Training FINAL model based on complete dataset (C=" 
					+ optResComplete.getOptimumC()
					+ ", gamma=" + optResComplete.getOptimumGamma() + ", ObjectiveValue=" + optResComplete.getOptimumValue() + ")...");

			//We now have the optimum values, train a model for these parameters based on all data
			svmParameter_complete.C = optResComplete.getOptimumC();
			svmParameter_complete.gamma = optResComplete.getOptimumGamma();
			svm_model svmModel_final = svm.svm_train(svmProblem_complete, svmParameter_complete);
			try {
				svm.svm_save_model(out_svmModelName , svmModel_final);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Return the final optimization result. Note that accuracy is not returned for now.
		System.out.println("++++++ SUMMARY ++++++");
		System.out.println("Training set parameter estimates: " + optResTrain);
		System.out.println("External prediction accuracy: " + externalAccuracy);
		System.out.println("Complete dataset parameter estimates: " + optResComplete);
		System.out.println("++++++ SUMMARY ENDS ++++++");
		return optResTrain;
	}


	
	private OptimizationResult setUpAndRunGridSearch(List<Double> activityList,
			List<svm_node[]> descriptorList, svm_problem svmProblem, svm_parameter svmParameter) throws IOException {

		//======================
		// GRID SEARCH
		// We estimate parameters by CV
		//======================

		// Add values to the SVM problem.
		svmProblem.l = descriptorList.size() - 1;
		svmProblem.x = new svm_node[svmProblem.l][];
		svmProblem.y = new double[svmProblem.l];
		for (int exampleNr = 0; exampleNr < svmProblem.l; exampleNr++){
			svmProblem.x[exampleNr] = descriptorList.get(exampleNr);
			svmProblem.y[exampleNr] = activityList.get(exampleNr);
		}

		// Do the grid search to find the best set of gamma for the RBF kernel and C for the cost.
		double optimumValue, optimumC = 1, optimumGamma = 0.01;
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
			optimumValue = -1;
		}

		System.out.println("svm_check_parameter: " + svm.svm_check_parameter(svmProblem, svmParameter));
		//TODO: What is the above?

		OptimizationResult optRes=null;
		if ("grid".equals(optimizationType))
			optRes=gridSearchNew(svmParameter, svmProblem, optimumValue, optimumC, optimumGamma);
		else if ("array".equals(optimizationType))
			optRes=arraySearchNew(svmParameter, svmProblem, optimumValue, optimumC, optimumGamma);
		else if ("none".equals(optimizationType)){

			if (gammafinal<0)
				throw new IllegalArgumentException("gammafinal not set or negative");
			if (cfinal<0)
				throw new IllegalArgumentException("gammafinal not set or negative");

			optRes=new OptimizationResult(Double.NaN, gammafinal, cfinal);
		}
		else
			throw new IllegalArgumentException("optimization type neither 'grid', 'array', nor 'none'");

		return optRes;
	}

	/**
	 * A simple test to check if we have read in correct values.
	 * 
	 * @param activityList
	 * @return
	 */
	private boolean isActivityListSane(List<Double> activityList) {
		//Do a sanity check for classification, we should at least have one active property
		boolean isSane=false;
		for (Double d : activityList){
			if (classification){
				if (d.doubleValue()==1.0)	//If we have at least one positive value
					isSane=true;
			}else{
				if (d.doubleValue()>0.0 || d.doubleValue()<0.0)	//If we have at least one non-zero value
					isSane=true;
			}
		}
		return isSane;
	}

	private void processInputFile(File trainFile, IteratingMDLReader reader,
			List<String> signatures, List<Double> activityList,
			List<svm_node[]> descriptorList) throws IOException, CDKException {
		BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainFile));
		int cnt=0;
		while (reader.hasNext()){
			IMolecule mol = (IMolecule) reader.next();

			// Check the activity.
			String activity = (String) mol.getProperty(activityProperty);

			if (activity==null){
				System.out.println("Activity property: " + activityProperty + " not found in molecule: " + (cnt+1));
				System.out.println("Exiting.");
				System.exit(1);
			}

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
			Map<String, Double> moleculeSignatures = new HashMap<String, Double>(); // Contains the signatures for a molecule and the count. We store the count as a double although it is an integer. libsvm wants a double.
			for (int height = startHeight; height <= endHeight; height++){
				List<String> signs = SignTools.calculateSignatures(mol, height);
				Iterator<String> signsIter = signs.iterator();
				while (signsIter.hasNext()){
					String currentSignature = signsIter.next();
					//System.out.println(currentSignature);
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
						if (moleculeSignatures.containsKey(currentSignature)){
							moleculeSignatures.put(currentSignature, (Double)moleculeSignatures.get(currentSignature)+1.00);
						}
						else{
							moleculeSignatures.put(currentSignature, 1.0);
						}
					}
				}
			}
			// Add the values of the current molecule's signatures as svm data.
			// Write the output as it reads in the sdf.
			trainWriter.write(activity);

			svm_node[] moleculeArray = new svm_node[moleculeSignatures.size()];
			Iterator<String> signaturesIter = signatures.iterator();
			int i = 0;
			while (signaturesIter.hasNext()){
				String currentSignature = signaturesIter.next();
				if (moleculeSignatures.containsKey(currentSignature)){
					moleculeArray[i] = new svm_node();
					moleculeArray[i].index = signatures.indexOf(currentSignature)+1; // libsvm assumes that the index starts at one.
					moleculeArray[i].value = (Double) moleculeSignatures.get(currentSignature);
					// The train file output.
					trainWriter.write(" " + moleculeArray[i].index + ":" + moleculeArray[i].value);
					i = i + 1;
				}
			}
			trainWriter.newLine();
			descriptorList.add(moleculeArray);

			//System.out.println("Molecule " + (cnt+1) + " (Activity=" + activity + "): " +  signs);
			cnt++;
		}
		trainWriter.close();
	}

	/**
	 * MUST BE MIGRATED TO NEW GRID SEARCH IMPLEMENTATION!! - FIXME
	 * 
	 */
	@Deprecated
	private OptimizationResult arraySearchNew(svm_parameter svmParameter, svm_problem svmProblem, double optimumValue, double optimumC, double optimumGamma) throws IOException{

		BufferedWriter optwriter = new BufferedWriter(new FileWriter(out_optmimizationFilename));

		for (Point combo : arrayOptimizationParams){

			int cExponent=combo.x;
			int gammaExponent=combo.y;

			double[] target = new double[svmProblem.l];
			svmParameter.C = Math.pow(10.0,(cExponent/2));
			svmParameter.gamma = Math.pow(2.0, -gammaExponent);
			System.out.println("Estimating SVM for c:gamma = " + svmParameter.C+" : " + svmParameter.gamma);
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
				System.out.println("Objective Value:C:gamma: "+objectiveValue+":"+svmParameter.C+":"+svmParameter.gamma);
				optwriter.write("Objective Value:C:gamma: "+objectiveValue+":"+svmParameter.C+":"+svmParameter.gamma+"\n");
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
				System.out.println("Objective Value:C:gamma: "+objectiveValue+":"+svmParameter.C+":"+svmParameter.gamma);
				optwriter.write("Objective Value:C:gamma: "+objectiveValue+":"+svmParameter.C+":"+svmParameter.gamma+"\n");
			}

		}

		System.out.println("ARRAY SEARCH FINISHED. Optimum Value:C:gamma: "+optimumValue+":"+optimumC+":"+optimumGamma);
		optwriter.write("ARRAY SEARCH FINISHED. Optimum Value:C:gamma: "+optimumValue+":"+optimumC+":"+optimumGamma+"\n");
		optwriter.close();
		return new OptimizationResult(optimumValue, optimumC, optimumGamma);

	}


	private OptimizationResult gridSearchNew(svm_parameter svmParameter, svm_problem svmProblem, double optimumValue, double optimumC, double optimumGamma) throws IOException{

		BufferedWriter optwriter = new BufferedWriter(new FileWriter(out_optmimizationFilename));
		
		int problemSize=(cEnd - cStart +1) * (gammaEnd-gammaStart +1);

		int problemIX=0;
		for (int cExponent = cStart; cExponent <= cEnd; cExponent++){
			for (int gammaExponent = gammaStart; gammaExponent <= gammaEnd; gammaExponent++){

				problemIX++;
				System.out.println("GRID search iteration " + problemIX + "/" + problemSize);

				double[] target = new double[svmProblem.l];
				svmParameter.C = Math.pow(10.0,(cExponent/2));
				svmParameter.gamma = Math.pow(2.0, -gammaExponent);
				System.out.println("Estimating SVM for c:gamma = " + svmParameter.C+" : " + svmParameter.gamma);
				svm.svm_cross_validation(svmProblem, svmParameter, nrFolds, target);

				double objectiveValue=0.0;
				if (classification){

					//For regression we calculate accuracy
					int nrCorrect = 0;
					for (int i = 0; i < svmProblem.l; i++){
						if (target[i] == svmProblem.y[i]){ // Can you compare doubles like this in java or should it be abs(target-y) < eps?
							nrCorrect = nrCorrect + 1;
						}
					}
					objectiveValue = 1.0*nrCorrect/svmProblem.l;
				}
				else{
					
					//For regression we calculate R^2
					double meanTarget = 0.0;
					double sumSquareTot = 0.0;
					double sumSquareError =0.0;
					for (int i = 0; i < svmProblem.l; i++){
						meanTarget = meanTarget + target[i];
					}
					meanTarget = meanTarget/svmProblem.l;

					for (int i = 0; i < svmProblem.l; i++){
						sumSquareError = sumSquareError + (target[i] - svmProblem.y[i]) * (target[i] - svmProblem.y[i]);
						sumSquareTot = sumSquareTot + (target[i]-meanTarget)*(target[i]-meanTarget);
					}
					objectiveValue = 1.0 - sumSquareError/sumSquareTot;

				}
				
				//We seek the highest accuracy or highest R^2
				if (objectiveValue > optimumValue){
					optimumValue = objectiveValue;
					optimumC = svmParameter.C;
					optimumGamma = svmParameter.gamma;
				}
				System.out.println("Objective Value:C:gamma: "+objectiveValue+":"+svmParameter.C+":"+svmParameter.gamma);
				optwriter.write("Objective Value:C:gamma: "+objectiveValue+":"+svmParameter.C+":"+svmParameter.gamma+"\n");


				//				double objectiveValue = doSVM_CV(svmParameter, svmProblem, cExponent, gammaExponent);
				//				if (objectiveValue > optimumValue){
				//					optimumValue = objectiveValue;
				//					optimumC = svmParameter.C;
				//					optimumGamma = svmParameter.gamma;
				//				}


			}
		}

		System.out.println("GRID SEARCH FINISHED. Optimum Value:C:gamma: "+optimumValue+":"+optimumC+":"+optimumGamma);
		optwriter.write("GRID SEARCH FINISHED. Optimum Value:C:gamma: "+optimumValue+":"+optimumC+":"+optimumGamma+"\n");
		return new OptimizationResult(optimumValue, optimumGamma, optimumC);

	}


	private void outputSettings() {

		System.out.println("Path to SDF: " + pathToSDFile);
		System.out.println("Output dir: " + outputDir);
		System.out.println("Activity property: " + activityProperty);
		System.out.println("Classification: " + classification);
		System.out.println("Positive activity: " + positiveActivity);

		System.out.println("\n == Output files ==");
		System.out.println("Output file for SVM model: " + out_svmModelName);
		System.out.println("Output file for Signatures: " + out_signaturesFilename);
		System.out.println("Output file for Training data: " + out_trainFilename);

		System.out.println("\n == Parameters for Model building ==");
		System.out.println("Nr CV folds: " + nrFolds);
		System.out.println("Signature height start: " + startHeight);
		System.out.println("Signature height end: " + endHeight);

		System.out.println("\n == Parameters for optimization ==");
		System.out.println("optimize: " + optimizationType);
		if ("array".equals(optimizationType)){
			System.out.println(arrayOptimizationParams.toString());
		}else if ("grid".equals(optimizationType)){
			System.out.println("cStart: " + cStart);
			System.out.println("cEnd: " + cEnd);
			System.out.println("gammaStart: " + gammaStart);
			System.out.println("gammaEnd: " + gammaEnd);
		}else{
			System.out.println("No optimization");
		}
		if (trainFinal)
			System.out.println("Train and save final model:  true");
		else
			System.out.println("Train and save final model:  false");

	}

	private void parseArgs(String[] args_in) {

		// Parse arguments
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(args_in));
		Iterator<String> it = args.iterator();
		while (it.hasNext()) {
			String arg = it.next();

			if ("-h".equals(arg)) {
				printUsage();
				System.exit(0);
			}

			//input file
			if ("-i".equals(arg)) {
				pathToSDFile = it.next();
			}

			//output dir
			if ("-o".equals(arg)) {
				outputDir = it.next();
				updateOutputFilenames();
			}

			if ("-ap".equals(arg)) {
				activityProperty = it.next();
			}

			if ("-c".equals(arg)) {
				classification= Boolean.parseBoolean(it.next());
			}

			if ("-pa".equals(arg)) {
				positiveActivity = it.next();
			}

			//Optional args
			if ("-cstart".equals(arg)) {
				cStart = Integer.parseInt(it.next());
			}
			if ("-cend".equals(arg)) {
				cEnd = Integer.parseInt(it.next());
			}
			if ("-gammastart".equals(arg)) {
				gammaStart = Integer.parseInt(it.next());
			}
			if ("-gammaend".equals(arg)) {
				gammaEnd = Integer.parseInt(it.next());
			}

			if ("-folds".equals(arg)) {
				nrFolds = Integer.parseInt(it.next());
			}

			if ("-hstart".equals(arg)) {
				startHeight = Integer.parseInt(it.next());
			}
			if ("-hend".equals(arg)) {
				endHeight = Integer.parseInt(it.next());
			}

			if ("-pjobs".equals(arg)) {
				noParallelJobs = Integer.parseInt(it.next());
			}
			if ("-optimize".equals(arg)) {
				optimizationType = it.next();
			}
			if ("-optarray".equals(arg)) {
				arrayOptimizationParams=parseArrayOptimizationInput(it.next());
			}

			if ("-trainfinal".equals(arg)) {
				trainFinal= Boolean.parseBoolean(it.next());
			}

			if ("-ptype".equals(arg)) {
				parallelType= it.next();
			}

			if ("-cfinal".equals(arg)) {
				cfinal= Double.parseDouble(it.next());
			}
			if ("-gammafinal".equals(arg)) {
				gammafinal= Double.parseDouble(it.next());
			}

			if ("-time".equals(arg)) {
				echoTime= Boolean.parseBoolean(it.next());
			}
			if ("-jarpath".equals(arg)) {
				jarpath= it.next();
			}


		}

	}



	private List<Point> parseArrayOptimizationInput(String arrayParams) {

		List<Point> parsedArray=new ArrayList<Point>();
		String[] entries = arrayParams.split(";");
		for (String entry : entries){
			String[] parts=entry.split(",");
			Point p = new Point(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
			parsedArray.add(p);
		}

		return parsedArray;
	}

	private void assertParameters() {

		//Assert all except file permissions, handled in buildModel

		if (pathToSDFile==null || pathToSDFile.isEmpty()) 
			throw new IllegalArgumentException("Input file is not defined");
		if (activityProperty==null || activityProperty.isEmpty()) 
			throw new IllegalArgumentException("Activity property is not defined");

		if (classification==true && (positiveActivity==null || positiveActivity.isEmpty())){
			throw new IllegalArgumentException("Missing parameter: POSITIVE ACTIVITY (required for classification)");
		}

	}


	private static void printUsage() {
		System.out.println();
		System.out.println("Signatures LibSVM model builder using gridsearch (supports classification and regression models)");
		System.out.println("If -optimize=grid, then perform grid search between [cstart to cend, gammastart-gammaend]");
		System.out.println("If -optimize=fixed, then optimize using paramarray (on form c1,gamma1;c2,gamma2");
		System.out.println("If -optimize=false, then predict using ");
		System.out.println();
		System.out.println("Usage: java -jar signbuild.jar <params>");
		System.out.println("-h                       display help");
		System.out.println("-i <input file>          path to SDFile [REQUIRED]");
		System.out.println("-o <output path>         path to where output files are written (DEFAULT=current dir)");
		System.out.println("-ap                      activity property in SDFile [REQUIRED]");
		System.out.println("-c                       if classification, set to 'true' (DEFAULT='false')");
		System.out.println("-pa                      positive activity (REQUIRED if classification model)");
		System.out.println("-time                    should elapsed time be echoed (DEFAULT='true')");
		System.out.println("-jarpath                 path to SignModel executable (for running in paralell)");

		//For optimization
		System.out.println("-optimize                perform optimization [grid | array | none] (DEFAULT='grid')");
		System.out.println("-cstart     	         cStart for grid-search (DEFAULT=0)");
		System.out.println("-cend                    cEnd for grid-search (DEFAULT=5)");
		System.out.println("-gammastart              gammaStart for grid-search (DEFAULT=3)");
		System.out.println("-gammaend                gammaEnd for grid-search (DEFAULT=10)");
		System.out.println("-optarray                array of c,gamma combinations for array search (e.g. 4,6;4,7;4,8)");

		System.out.println("-trainfinal              if model on all data should be built and saved (DEFAULT='true')");
		System.out.println("-cfinal                  final c for model building");
		System.out.println("-gammafinal              final gamma for model building");

		//For model building
		System.out.println("-hstart                  signature start height (DEFAULT=0)");
		System.out.println("-hend                    signature end height (DEFAULT=3)");
		System.out.println("-folds                   nr folds in cross-validation (DEFAULT=5)");

		//For parallel setup
		System.out.println("-pjobs                   Generate script for jobs");
		System.out.println("-ptype                   Type of parallelization [threads | slurm | cloud] [DEFAULT=threads]");
//		System.out.println("-slurmTemplate           path to slurm template file. Project no should be $PROJECT_NO$.");
//		System.out.println("-slurmProject            path to slurm template file. Project no should be $PROJECT_NO$.");
		System.out.println();
	}


	/**
	 * The main class.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		SignModel modelBuilder= new SignModel();
		modelBuilder.parseArgs(args);

		try{

			modelBuilder.assertParameters();
			modelBuilder.outputSettings();

			//If jobs > 1, return a script for execution
			if (modelBuilder.noParallelJobs>1){
				modelBuilder.generateParallelExecution();
				return;
			}

			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start();

			//Optimize and build
			modelBuilder.BuildModel();

			stopwatch.stop();
			if (modelBuilder.echoTime)
				System.out.println("Model building finsihed. Elapsed time: " + stopwatch.toString());

		}catch (IllegalArgumentException e){
			System.err.println("Error: " + e.getMessage());
			System.err.flush();
			printUsage();
			System.exit(1);
		}

	}

	//	private void doArraySearchAndBuildModel() {
	//		//		throw new UnsupportedOperationException("doArraySearchAndBuildModel not implemented");
	//
	//		//Set up params for running the models
	//
	//		for (Point combo : arrayOptimizationParams){
	//
	//		}
	//
	//
	//	}

	/**
	 * Generate script, dividing up grid search over multiple model builders
	 * @throws IOException 
	 */
	private void generateParallelExecution() throws IOException {

		List<List<Point>> jobs = setUpJobs();

		if ("slurm".equals(parallelType))
			generateSLURMfiles(jobs);
		if ("shell".equals(parallelType))
			generateShellCommands(jobs);
		else if ("threads".equals(parallelType))
			throw new UnsupportedOperationException("Parallel type THREADS not implemented");
		else if ("cloud".equals(parallelType))
			throw new UnsupportedOperationException("Parallel type CLOUD not implemented");
		else
			throw new UnsupportedOperationException("Unknown parallel type: " + parallelType);

	}

	public List<List<Point>> setUpJobs() {
		// Compute dimensions. Number of models: c * gamma
		int cSize=(cEnd-cStart+1);
		int gammaSize=(gammaEnd-gammaStart + 1);
		int totalSize=cSize*gammaSize;

		//Prune number of jobs if not enough models to fill
		if (totalSize<noParallelJobs){
			System.out.println("Number of models less than number oj jobs. Pruned number of jobs to: " + totalSize);
			noParallelJobs=totalSize;
		}

		int modelsPerJob = totalSize/noParallelJobs;

		System.out.println("Number of models to build: " + totalSize);
		System.out.println("Desired number of parrallel jobs: " + noParallelJobs);
		System.out.println("Number of models per job: " + modelsPerJob);

		//Set up list of jobs, each job is a list of points
		List<List<Point>> jobs = new ArrayList<List<Point>>();
		for (int i=0; i<noParallelJobs; i++){
			List<Point> job = new ArrayList<Point>();
			jobs.add(job);
		}

		int currjobix=0;
		int currjobSize=0;

		for (int c = cStart; c <= cEnd; c++){
			for (int g = gammaStart; g <= gammaEnd; g++){
				Point p = new Point(c, g);
				if (currjobix < (noParallelJobs-1)){    //Do not increase if we are in last job, this can be larger than the other
					if (currjobSize >= modelsPerJob){
						//Get next job
						currjobix++;
						currjobSize=0;
					}
				}
				List<Point> currjob = jobs.get(currjobix);
				currjob.add(p);
				currjobSize++;
			}
		}

		System.out.println("We have the following jobs:");

		int cnt=0;
		for (List<Point> job : jobs){
			System.out.println("Job " + cnt + ": " + job.toString());
			cnt++;
		}
		return jobs;
	}

	public List<String> generateShellCommands(List<List<Point>> jobs) {
		
		List<String> commands = new ArrayList<String>();
		
		int cnt=1;
		for (List<Point> job : jobs){
			
//			String sdfile_without_path=pathToSDFile.substring(pathToSDFile.lastIndexOf("/")+1);
			
			
//			params=params + " -i " + sdfile_without_path;
			if (jarpath==null)
				jarpath="";
			if (!(jarpath.endsWith(File.separator)))
				jarpath=jarpath+File.separator;

			String params="java -jar " + jarpath + "signmodel-withdeps.jar";

			params=params + " -i " + pathToSDFile;
			params=params + " -ap \'" + activityProperty+"\'";
			params=params + " -c " + classification;
			params=params + " -trainfinal false"; 
			
			if (positiveActivity!=null && !positiveActivity.isEmpty())
				params=params+" -pa " + positiveActivity;

			params=params+" -o output"+cnt;
			params=params+" -optimize " + getOptimizationType();
			params=params+" -time " + echoTime;
			
			params=params+" -optarray ";
			String points="";
			for (Point p : job){
				points=points+p.x+","+p.y+";";
			}

			String cmd =  params + "\'" + points.substring(0,points.length()-1)+"\'\n";
			System.out.print(cmd);
			commands.add(cmd);

			cnt++;
		}

		
		return commands;
		
		
	}



	private void generateSLURMfiles(List<List<Point>> jobs) throws IOException {

		System.out.println("Generating parameter calls for " + jobs.size() + " files");
		

		int cnt=1;
		for (List<Point> job : jobs){
			
			String template="#! /bin/bash -l\n\n" +
			"#SBATCH -J sign-" + cnt + "\n" +
			"#SBATCH -A p2010009\n" +
			"#SBATCH -p core -n 1\n" +
			"#SBATCH -t 3:00:00\n\n" +
			"module load java\n\n";

			String sdfile_without_path=pathToSDFile.substring(pathToSDFile.lastIndexOf("/")+1);
			
			String params="java -jar signmodel-withdeps.jar";
			
			params=params + " -i " + sdfile_without_path;
			params=params + " -ap \'" + activityProperty+"\'";
			params=params + " -c " + classification;
			if (positiveActivity!=null && !positiveActivity.isEmpty())
				params=params+" -pa " + positiveActivity;

			params=params+" -o output"+cnt;
			params=params+" -optimize array";
			
			params=params+" -optarray ";
			String points="";
			for (Point p : job){
				points=points+p.x+","+p.y+";";
			}
			String filename=outputDir+"/runjob" + cnt + ".sh";
			
			BufferedWriter optwriter = new BufferedWriter(new FileWriter(filename));
			optwriter.write(template);
			optwriter.write(params + "\'" + points.substring(0,points.length()-1)+"\'\n");
			optwriter.close();
			
//			System.out.println(params + points.substring(0,points.length()-1));
			
			System.out.println("Wrote file " + filename);

			cnt++;
		}

	}

}
