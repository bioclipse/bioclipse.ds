package net.bioclipse.ds.model;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.core.domain.IMolecule;

import org.apache.log4j.Logger;
import org.openscience.cdk.exception.CDKException;

/**
 * 
 * @author ola
 *
 */
public abstract class AbstractDSMolModel extends AbstractDSTest{

    private static final Logger logger = Logger.getLogger(AbstractDSMolModel.class);

    @Override
    public IBioObject preProcessInput(IBioObject input) throws BioclipseException {
    	
    	if (!(input instanceof IMolecule)) {
    		throw new BioclipseException("Input not of type IMolecule");
		}
		IMolecule mol = (IMolecule) input;
    	
        //Create CDKMolecule from the IMolecule to get a clean API
        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();
        ICDKMolecule cdkmol=null;
        cdkmol = cdk.asCDKMolecule( mol );

        //Preprocess the molecule: Remove explicit and add implicit hydrogens
        try {
			cdkmol = new CDKMolecule(standardizeMolecule(cdkmol.getAtomContainer()));
		} catch (CDKException e) {
			throw new BioclipseException("Could not standardize molecule: " + e.getMessage());
		}
    	
    	return cdkmol;
    }

}
