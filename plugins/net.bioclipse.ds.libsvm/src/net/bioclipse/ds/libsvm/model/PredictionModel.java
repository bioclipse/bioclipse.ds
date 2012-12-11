package net.bioclipse.ds.libsvm.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.domain.ICDKMolecule;


/**
 * A model for prediction. Based on a molecule.
 * 
 * @author ola
 *
 */
public class PredictionModel {

	Map<String, Double> moleculeSignatures; // Contains the signatures for a molecule and the count. We store the count as a double although it is an integer. libsvm wants a double.
	Map<String, Integer> moleculeSignaturesHeight; //Contains the height for a specific signature.
	Map<String, List<Integer>> moleculeSignaturesAtomNr; //Contains the atomNr for a specific signature.

	private ICDKMolecule cdkmol;

	public PredictionModel(ICDKMolecule cdkmol) {

		this.cdkmol=cdkmol;
		moleculeSignatures = new HashMap<String, Double>(); // Contains the signatures for a molecule and the count. We store the count as a double although it is an integer. libsvm wants a double.
		moleculeSignaturesHeight = new HashMap<String, Integer>(); //Contains the height for a specific signature.
		moleculeSignaturesAtomNr = new HashMap<String, List<Integer>>(); //Contains the atomNr for a specific signature.

	}

	public Map<String, Double> getMoleculeSignatures() {
		return moleculeSignatures;
	}

	public void setMoleculeSignatures(Map<String, Double> moleculeSignatures) {
		this.moleculeSignatures = moleculeSignatures;
	}

	public Map<String, Integer> getMoleculeSignaturesHeight() {
		return moleculeSignaturesHeight;
	}

	public void setMoleculeSignaturesHeight(
			Map<String, Integer> moleculeSignaturesHeight) {
		this.moleculeSignaturesHeight = moleculeSignaturesHeight;
	}

	public Map<String, List<Integer>> getMoleculeSignaturesAtomNr() {
		return moleculeSignaturesAtomNr;
	}

	public void setMoleculeSignaturesAtomNr(
			Map<String, List<Integer>> moleculeSignaturesAtomNr) {
		this.moleculeSignaturesAtomNr = moleculeSignaturesAtomNr;
	}
	
	
	
}
