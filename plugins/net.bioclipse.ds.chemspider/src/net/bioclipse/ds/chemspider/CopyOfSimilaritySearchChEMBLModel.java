package net.bioclipse.ds.chemspider;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.chemspider.Activator;
import net.bioclipse.chemspider.business.IChemspiderManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;

/**
 * Model for DS based on ChemSpider SimilaritySearch.
 * 
 * @author Ola Spjuth
 *
 */
public class CopyOfSimilaritySearchChEMBLModel extends AbstractDSTest{

	private static final String TANIMOTO_DISTANCE = "distance.tanimoto";
	private static final int MAX_NEIGHBORS = 15;

	
	private static final Logger logger = Logger.getLogger(CopyOfSimilaritySearchChEMBLModel.class);

	private float tanimoto;
    DecimalFormat formatter = new DecimalFormat( "0.00" );

	@Override
	public List<String> getRequiredParameters() {
		List<String> ret=new ArrayList<String>();
		ret.add( TANIMOTO_DISTANCE );
		return ret;
	}

	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {
		super.initialize(monitor);

		tanimoto=Float.parseFloat(getParameters().get(TANIMOTO_DISTANCE));
		logger.debug("Chemspider similaritysearch is using tanimoto: " + tanimoto);

	}


	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule molecule,
			IProgressMonitor monitor) {

		//Managers we need
		IChemspiderManager chemspider = Activator.getDefault().getJavaChemspiderManager();
		ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();

		//Hold results here
		ArrayList<ExternalMoleculeMatch> results = new ArrayList<ExternalMoleculeMatch>();

		//Do chemspider search
		List<ICDKMolecule> resultMols=null;
		try {
			resultMols = chemspider.similaritySearch(molecule, tanimoto, monitor);
		} catch (BioclipseException e1) {
			return returnError("Service failed to complete. Incorrect security token?", e1.getMessage());
		}
		if (resultMols==null)
			return returnError("Null results","");
		logger.debug("Model " + getName() + " had " + resultMols.size() + " hits");

//		List<ICDKMolecule> resultMols = new ArrayList<ICDKMolecule>();
//		try {
//			resultMols.add(cdk.fromSMILES("CCC"));
//		} catch (BioclipseException e1) {
//			e1.printStackTrace();
//		}

		//Set up results, reduce to max 15 results
		logger.debug("Setting up results...");
		for (ICDKMolecule cdkmol : resultMols.subList(0, Math.min(resultMols.size(), MAX_NEIGHBORS))){

			//We calculate tanimoto locally for now
			String distance = "N/A";
			try {
				distance = "" + formatter.format(cdk.calculateTanimoto(molecule, cdkmol));
			} catch (BioclipseException e) {
				logger.error("Could not calculate tanimoto: " + e.getMessage());
			}

			//Get name and other params from stored props on mol, set in chemspider manager
			String molname=(String) cdkmol.getProperty("chemspider.commonname", null);

			logger.debug("Hit: " + molname + " ; distance: " + distance);

			ExternalMoleculeMatch match = new ExternalMoleculeMatch(molname + " (tanimoto=" + distance + ")" , cdkmol, 
					ITestResult.INFORMATIVE);
			
			Map<String, Map<String, String>> categories = new HashMap<String, Map<String,String>>();
			Map<String,String> props = new HashMap<String, String>();
			Integer csid = (Integer) cdkmol.getProperty("chemspider.id", null);
            props.put("Chemspider ID" , "" + csid);
            props.put("InChI" , "" + cdkmol.getProperty("chemspider.inchi", null));
            props.put("InChIkey" , "" + cdkmol.getProperty("chemspider.inchikey", null));
            props.put("AlogP" , "" + cdkmol.getProperty("chemspider.alogp", null));
            props.put("XlogP" , "" + cdkmol.getProperty("chemspider.xlogp", null));
            props.put("Average mass" , "" + cdkmol.getProperty("chemspider.averagemass", null));
            props.put("Molecular formula" , "" + cdkmol.getProperty("chemspider.mf", null));
            props.put("Molecular wheight" , "" + cdkmol.getProperty("chemspider.mw", null));
            props.put("Monoisotopic mass" , "" + cdkmol.getProperty("chemspider.monoisotopicmass", null));
            props.put("Nominal mass" , "" + cdkmol.getProperty("chemspider.nominalmass", null));
            categories.put("Chemspider", props);

            //Look up interactions in ChEMBL
            try {
				List<ChemblInteraction> chemblInteractions = ChemblLookup.lookupCSID(csid);
//				List<ChemblInteraction> chemblInteractions = ChemblLookup.lookupCSID(10368587);

				if (chemblInteractions!=null){
					for (ChemblInteraction ci : chemblInteractions){
						Map<String,String> ciprops = new HashMap<String, String>();
						if (ci.getRelation()!=null)
							ciprops.put("Value" , ci.getRelation() + ci.getValue());
						else
							ciprops.put("Value" , ci.getValue());
						ciprops.put("Unit" , ci.getUnit());
						ciprops.put("Target type" , ci.getTargetType());
						ciprops.put("Interaction type" , ci.getInteractionType());
						ciprops.put("Description" , ci.getDescription());

						categories.put("Target: " + ci.getTitle(), ciprops);
					}
				}
				else{
					logger.debug("No ChEMBL interactions for CID=" + csid);
				}
				
			} catch (BioclipseException e) {
				logger.error("Error querying chembl: " + e.getMessage());
			}
			
            match.setProperties(categories);
			results.add(match);
		}

		return results;
	}

}