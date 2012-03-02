package net.bioclipse.ds.matcher;


import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.inchi.InChI;

/**
 * Search for exact matches by InChI.
 * 
 * @author Ola Spjuth
 *
 */
public class SDFExactMatcherInchi extends BaseSDFExactMatcher {

	@Override
	public String getPropertyKey() {
		return "net.bioclipse.cdk.InChI";
	}

	@Override
	public String processQueryResult(Object obj) throws DSException {
		if (!(obj instanceof InChI)) throw new DSException("Result not an InChI");
		InChI readInchi = (InChI) obj;
		return readInchi.getValue();
	}

	@Override
	public String getCalculatedProperty(ICDKMolecule cdkmol) throws DSException {
		
        String molInchi;
        try {
            molInchi = cdkmol.getInChI(IMolecule.Property.USE_CALCULATED  );
        } catch ( BioclipseException e ) {
        	throw new DSException("Error generating Inchi: " + e.getMessage() );
        }

        return molInchi;
	}

}
