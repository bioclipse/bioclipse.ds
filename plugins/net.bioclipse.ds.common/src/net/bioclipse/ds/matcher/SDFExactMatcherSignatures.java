package net.bioclipse.ds.matcher;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.signatures.Activator;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

/**
 * Search for exact matches by Molecular Signature
 * 
 * @author Ola Spjuth
 *
 */
public class SDFExactMatcherSignatures extends BaseSDFExactMatcher {

	@Override
	public String getPropertyKey() {
		return "Molecular Signature";
	}

	@Override
	public String getCalculatedProperty(ICDKMolecule cdkmol) throws DSException {
		
        ISignaturesManager signatures= Activator.getDefault()
        .getJavaSignaturesManager();

        String querySignature=null;
        try {
            querySignature = signatures.generateMoleculeSignature( cdkmol );
            
        } catch ( Exception e ) {
        	throw new DSException("Failed to calculate Signatures for mol: " + cdkmol);
        }
        
        return querySignature;
	}

}
