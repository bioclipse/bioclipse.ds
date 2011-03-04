package net.bioclipse.ds.sdk.qsar;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class SignTools {
	

	public static List<String> calculateSignatures(IAtomContainer mol, int height) throws CDKException{

		CDKHueckelAromaticityDetector.detectAromaticity(mol);
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);

		List<String> ret = new ArrayList<String>();
		
		for (int i=0; i<mol.getAtomCount(); i++){
			AtomSignature as = new AtomSignature(i, height, mol);
			ret.add(as.toCanonicalString());
		}
		
		return ret;
		
	}
	

}
