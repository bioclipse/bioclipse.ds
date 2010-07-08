package net.bioclipse.ds.signatures;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import signature.chemistry.Molecule.BondOrder;


/**
* Adapts a CDK IAtomContainer class to a signature.chemistry.Molecule class,
* to allow for aromatic bonds.
*
* @author maclean
*
*/
public class CDKMoleculeSignatureAdapter {

    public static signature.chemistry.Molecule convert(
            IAtomContainer atomContainer) {
        signature.chemistry.Molecule molecule =
            new signature.chemistry.Molecule();
        for (IAtom atom : atomContainer.atoms()) {
            molecule.addAtom(atom.getSymbol());
        }
        
        for (IBond bond : atomContainer.bonds()) {
            BondOrder o = convertCDKBondOrderToSignatureChemistry(bond);
            int atomNumberA = atomContainer.getAtomNumber(bond.getAtom(0));
            int atomNumberB = atomContainer.getAtomNumber(bond.getAtom(1));
            molecule.addBond(atomNumberA, atomNumberB, o);
        }
        return molecule;
    }
    
    public static BondOrder convertCDKBondOrderToSignatureChemistry(IBond bond) {
        IBond.Order o = bond.getOrder();
        if (bond.getFlag(CDKConstants.ISAROMATIC)) {
            return BondOrder.AROMATIC;
        }
        switch (o) {
            case SINGLE : return BondOrder.SINGLE;
            case DOUBLE : return BondOrder.DOUBLE;
            case TRIPLE : return BondOrder.TRIPLE;
            default : return BondOrder.SINGLE;
        }
    }
    
}

