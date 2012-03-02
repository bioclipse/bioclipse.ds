package net.bioclipse.ds.matcher.model;

import java.util.List;
import java.util.Map;

public class SignatureFrequenceyResult {

	Map<String, Integer> moleculeSignatures;             //Contains the signatures for a molecule and the count (# occurrences).
	Map<String, Integer> moleculeSignaturesHeight;       //Contains the height for a specific signature.
	Map<String, List<Integer>> moleculeSignaturesAtomNr; //Contains the atomNr for a specific signature.
	public Map<String, Integer> getMoleculeSignatures() {
		return moleculeSignatures;
	}
	public void setMoleculeSignatures(Map<String, Integer> moleculeSignatures) {
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
	public SignatureFrequenceyResult(Map<String, Integer> moleculeSignatures,
			Map<String, Integer> moleculeSignaturesHeight,
			Map<String, List<Integer>> moleculeSignaturesAtomNr) {
		super();
		this.moleculeSignatures = moleculeSignatures;
		this.moleculeSignaturesHeight = moleculeSignaturesHeight;
		this.moleculeSignaturesAtomNr = moleculeSignaturesAtomNr;
	}

	
	
}
