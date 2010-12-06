package net.bioclipse.ds.signatures.chiral;


import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.cip.CIPTool;
import org.openscience.cdk.geometry.cip.CIPTool.CIP_CHIRALITY;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import org.openscience.cdk.interfaces.ITetrahedralChirality.Stereo;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.signature.ChiralAtomSignature;
import org.openscience.cdk.stereo.StereoTool;
import org.openscience.cdk.stereo.TetrahedralChirality;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

public class CalculateChiralSignatures {

    private static final Logger logger = Logger.getLogger(
    		CalculateChiralSignatures.class);

	public void calculate(String height, String filename) throws Exception {
		File file = new File(filename);
		MDLV2000Reader reader = new MDLV2000Reader(
				new FileReader(file)
		);
		IMolecule mol = reader.read(new Molecule());
		int sigheight = Integer.valueOf(height);
		Map<IAtom, CIP_CHIRALITY> chiralities = new HashMap<IAtom, CIP_CHIRALITY>();

		// for SMILES:
		/* for (IStereoElement stereo : mol.stereoElements()) {
if (stereo instanceof ITetrahedralChirality) {
ITetrahedralChirality tetraStereo = (ITetrahedralChirality)stereo;
chiralities.put(
tetraStereo.getChiralAtom(),
CIPTool.getCIPChirality(mol, tetraStereo)
);
}
}*/

		// for MDL molfile with 3D coordinates
		for (IAtom atom : mol.atoms()) {
			if (atom.getProperty(MDLV2000Reader.MDL_ATOM_STEREO_PARITY) != null) {
				List<IAtom> atoms = mol.getConnectedAtomsList(atom);
				if (atoms.size() != 4) {
					// OK, something unexpected
				} else {
					IAtom[] ligandAtoms = new IAtom[4];
					ligandAtoms[0] = atoms.get(0);
					ligandAtoms[1] = atoms.get(1);
					ligandAtoms[2] = atoms.get(2);
					ligandAtoms[3] = atoms.get(3);
					ITetrahedralChirality tetraStereo = new TetrahedralChirality(
							atom, ligandAtoms,
							StereoTool.getStereo(
									ligandAtoms[0], ligandAtoms[1], ligandAtoms[2], ligandAtoms[3]
							)
					);
					chiralities.put(
							atom,
							CIPTool.getCIPChirality(mol, tetraStereo)
					);
				}
			}
		}
		for (IAtom atom : mol.atoms()) {
			ChiralAtomSignature signature = new ChiralAtomSignature(
					atom, sigheight, mol, chiralities
			);
			System.out.println(signature.toCanonicalString());
		}
	}

	public static void main(String[] args) throws Exception {
		CalculateChiralSignatures calculator = new CalculateChiralSignatures();
		if (args.length < 2) {
			logger.error("java CalculateChiralSignatures height [FILE.mol]");
			System.exit(0);
		}

		calculator.calculate(args[0], args[1]);
	}


	public static List<String> generate(String mdl, int height) throws CDKException {


		//We need to read with MDL reader as it is the only to support chiralty for now
		MDLV2000Reader reader = new MDLV2000Reader(
				new StringReader(mdl)
		);
		IMolecule mol = reader.read(new Molecule());

		Map<IAtom, CIP_CHIRALITY> chiralities = new HashMap<IAtom, CIP_CHIRALITY>();

		//Perceive aromaticity and add implicit hydrogens
		CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(
				mol.getBuilder() );

		Iterator<IAtom> natoms = mol.atoms().iterator();

		while (natoms.hasNext()) {
			IAtom atom = natoms.next();
			IAtomType atomType = matcher.findMatchingAtomType(mol, atom);
			
			if (atomType == null) {
				// try + charge
				atom.setFormalCharge(+1);
				atomType = matcher.findMatchingAtomType(mol, atom);
				if (atomType == null){
					// try - charge
					atom.setFormalCharge(-1);			
					atomType = matcher.findMatchingAtomType(mol, atom);
				}
			}

			if (atomType==null){
				System.out.println("Could not find type for atom: " + atom 
						+ " in mol " + mol);
			}else{
				AtomTypeManipulator.configure(atom, atomType);
			}
		}
		CDKHydrogenAdder hAdder
		= CDKHydrogenAdder.getInstance(mol.getBuilder());
		hAdder.addImplicitHydrogens(mol);

//		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		CDKHueckelAromaticityDetector.detectAromaticity(mol);

		boolean isAromatic=false;
		for (IBond bond : mol.bonds()) {
			if (bond.getFlag(CDKConstants.ISAROMATIC))
				isAromatic=true;
		}
		if (isAromatic)
			System.out.println("** This molecule is aromatic");
		

		// for MDL molfile with 3D coordinates
		for (IAtom atom : mol.atoms()) {
			
			System.out.println(" Atom: " + mol.getAtomNumber(atom) + " - AT: " + atom.getAtomTypeName());
			
			List<IAtom> atoms = mol.getConnectedAtomsList(atom);
			if (atoms.size() != 4) {
				// OK, something unexpected
			} else {
				IAtom[] ligandAtoms = new IAtom[4];
				ligandAtoms[0] = atoms.get(0);
				ligandAtoms[1] = atoms.get(1);
				ligandAtoms[2] = atoms.get(2);
				ligandAtoms[3] = atoms.get(3);
				Stereo stereo = StereoTool.getStereo(
					ligandAtoms[0], ligandAtoms[1], ligandAtoms[2], ligandAtoms[3]
				);
				System.out.println(" stereo: " + stereo);
				ITetrahedralChirality tetraStereo = new TetrahedralChirality(
						atom, ligandAtoms, stereo
				);
				try {
					CIP_CHIRALITY chirality = CIPTool.getCIPChirality(mol, tetraStereo);
					System.out.println(" chirality: " + chirality);
					chiralities.put(
						atom, chirality
					);
				}catch(Exception e){
					logger.error("ERROR in atom " + atom + " - " + e.getMessage());
				}
			}
			//			}
		}

		List<String> signs=new ArrayList<String>();

		for (IAtom atom : mol.atoms()) {
			ChiralAtomSignature signature = new ChiralAtomSignature(
					atom, height, mol, chiralities
			);
			signs.add(signature.toCanonicalString());
		}

		return signs;
	}


}
