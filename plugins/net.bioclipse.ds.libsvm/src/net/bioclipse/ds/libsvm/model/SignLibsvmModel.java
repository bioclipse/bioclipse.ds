package net.bioclipse.ds.libsvm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;


/**
 * A SignaturesSVMmodel. Consists of an svm_model and a list of signatures.
 * 
 * @author ola
 *
 */
public class SignLibsvmModel {

	private static final Logger logger = Logger.getLogger(SignLibsvmModel.class);

	List<String> modelSignatures;
	svm_model svmModel;

	public SignLibsvmModel(svm_model svmModel, List<String> signatures) {
		super();
		this.svmModel = svmModel;
		this.modelSignatures = signatures;
	}


	public List<String> getModelSignatures() {
		return modelSignatures;
	}
	public void setModelSignatures(List<String> modelSignatures) {
		this.modelSignatures = modelSignatures;
	}
	public svm_model getSvmModel() {
		return svmModel;
	}
	public void setSvmModel(svm_model svmModel) {
		this.svmModel = svmModel;
	}


	/**
	 * Wrap a standard internal SVM prediction.
	 * 
	 * @return
	 */
	public double predict(svm_node[] svmNodes){
		return svm.svm_predict(svmModel, svmNodes);
	}
	
	

	/**
	 * For a given set of signatures, predict the results for this model.
	 * 
	 * @param querySignatures
	 * @return
	 */
	public double predict(List<String> querySignatures){
		
		
		// Contains the signatures for a molecule and the count. 
		//We store the count as a double although it is an integer. libsvm wants a double.
		Map<String, Double> moleculeSignatures = new HashMap<String, Double>(); 

		Iterator<String> signsIter = querySignatures.iterator();
		int signsIndex = 0;
		while (signsIter.hasNext()){
			String currentSignature = signsIter.next();
			if (modelSignatures.contains(currentSignature)){
				if (moleculeSignatures.containsKey(currentSignature)){
					moleculeSignatures.put(currentSignature, (Double)moleculeSignatures.get(currentSignature)+1.00);
				}
				else{
					moleculeSignatures.put(currentSignature, 1.0);
				}
			}
			signsIndex++;
		}
		
//		logger.debug("Signatures with count: " + moleculeSignatures.toString());

		//Set up input for libsvm
		List<svm_node> svmNodeList = new ArrayList<svm_node>();
		Iterator<String> signaturesIter = modelSignatures.iterator();
		while (signaturesIter.hasNext()){
			String currentSignature = signaturesIter.next();
			if (moleculeSignatures.containsKey(currentSignature)){
				svm_node node = new svm_node();
				svmNodeList.add(node);
				node.index = modelSignatures.indexOf(currentSignature)+1; // libsvm assumes that the index starts at one.
				node.value = (Double) moleculeSignatures.get(currentSignature);

//				System.out.println(currentSignature + " ==> " 
//						+ node.index + "=" + node.value);

			}
		}

		//This is the array with signature descriptor values
		svm_node[] svmPredictionArray = svmNodeList.toArray(new svm_node[0]);

		return svm.svm_predict(svmModel, svmPredictionArray);
	}



}
