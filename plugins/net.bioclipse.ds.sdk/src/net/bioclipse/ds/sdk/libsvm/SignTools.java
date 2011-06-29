package net.bioclipse.ds.sdk.libsvm;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class SignTools {
	

	public static List<String> calculateSignatures(IAtomContainer mol, int height) throws CDKException{

		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		CDKHueckelAromaticityDetector.detectAromaticity(mol);
		mol=AtomContainerManipulator.removeHydrogens(mol);

		List<String> ret = new ArrayList<String>();
		
		for (int i=0; i<mol.getAtomCount(); i++){
			AtomSignature as = new AtomSignature(i, height, mol);
			ret.add(as.toCanonicalString());
		}
		
		return ret;
		
	}
	
	public static void main(String[] args) throws CDKException {
		
		SmilesParser sp = new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
		IMolecule mol = sp.parseSmiles("CCN(CC)CCOCCOC(=O)C(CC)(CC)C1=CC=CC=C1"); //Oxeladin
		for (int i=0; i<4; i++){
			System.out.println("Height " + i);
			List<String> signs = calculateSignatures(mol, i);
			System.out.println(signs);
		}
		
	}
	

}
