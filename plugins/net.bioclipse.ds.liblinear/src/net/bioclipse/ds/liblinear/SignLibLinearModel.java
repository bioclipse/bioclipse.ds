package net.bioclipse.ds.liblinear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;


/**
 * A SignaturesLibLinearModel. Consists of a Model and a list of signatures.
 * 
 * @author ola
 *
 */
public class SignLibLinearModel {

	private static final Logger logger = Logger.getLogger(SignLibLinearModel.class);

	List<String> modelSignatures;
	Model model;

	public SignLibLinearModel(Model model, List<String> signatures) {
		super();
		this.model = model;
		this.modelSignatures = signatures;
	}


	public List<String> getModelSignatures() {
		return modelSignatures;
	}
	public void setModelSignatures(List<String> modelSignatures) {
		this.modelSignatures = modelSignatures;
	}
	public Model getModel() {
		return model;
	}
	public void setModel(Model model) {
		this.model = model;
	}


	/**
	 * Wrap a standard internal LibLinear prediction.
	 * 
	 * @return
	 */
	public double predict(Feature[] instance){
		return Linear.predict(model, instance);
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

		//Set up input for LibLinear
		List<FeatureNode> nodes = new ArrayList<FeatureNode>();
		Iterator<String> signaturesIter = modelSignatures.iterator();
		while (signaturesIter.hasNext()){
			String currentSignature = signaturesIter.next();
			if (moleculeSignatures.containsKey(currentSignature)){
				int index = modelSignatures.indexOf(currentSignature)+1; // libsvm assumes that the index starts at one.
				Double value = (Double) moleculeSignatures.get(currentSignature);
				FeatureNode node = new FeatureNode(index, value);
				nodes.add(node);

//				System.out.println(currentSignature + " ==> " 
//						+ node.index + "=" + node.value);

			}
		}

		//This is the array with signature descriptor values
		Feature[] instance = nodes.toArray(new Feature[0]);

		return predict(instance);
	}



}
