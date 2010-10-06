package net.bioclipse.ds.signatures.chiral;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.openscience.cdk.*;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.cip.*;
import org.openscience.cdk.geometry.cip.CIPTool.CIP_CHIRALITY;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.*;
import org.openscience.cdk.signature.*;
import org.openscience.cdk.stereo.*;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

public class CalculateChiralSignatures {

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
			System.out.println("java CalculateChiralSignatures height [FILE.mol]");
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
			IAtomType type = matcher.findMatchingAtomType(mol, atom);
			AtomTypeManipulator.configure(atom, type);
		}
		CDKHydrogenAdder hAdder
		= CDKHydrogenAdder.getInstance(mol.getBuilder());
		hAdder.addImplicitHydrogens(mol);


		// for MDL molfile with 3D coordinates
		for (IAtom atom : mol.atoms()) {
			//			if (atom.getProperty(MDLV2000Reader.MDL_ATOM_STEREO_PARITY) != null) {
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
				try{
					chiralities.put(
							atom,
							CIPTool.getCIPChirality(mol, tetraStereo)
					);
				}catch(Exception e){
					System.out.println("ERROR in atom " + atom + " - " + e.getMessage());
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
