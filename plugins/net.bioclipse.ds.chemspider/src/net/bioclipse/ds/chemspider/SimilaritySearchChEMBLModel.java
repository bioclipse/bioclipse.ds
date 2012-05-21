package net.bioclipse.ds.chemspider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;

/**
 * Model for DS based on ChemSpider SimilaritySearch, 
 * annotates with ChEMBL interaction data. 
 * 
 * @author Ola Spjuth
 *
 */
public class SimilaritySearchChEMBLModel extends SimilaritySearchModel{

	private static final Logger logger = Logger.getLogger(SimilaritySearchChEMBLModel.class);

	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule molecule,
			IProgressMonitor monitor) {
		
		List<? extends ITestResult> matches = super.doRunTest(molecule, monitor);
		List<ExternalMoleculeMatch> toRemove = new ArrayList<ExternalMoleculeMatch>();

		for (ITestResult tr : matches){
			if (!(tr instanceof ExternalMoleculeMatch)) {
				continue;
			}
			ExternalMoleculeMatch match = (ExternalMoleculeMatch)tr;
			
			Map<String, Map<String, String>> categories = match.getProperties();
			Map<String, String> csprops = categories.get("Chemspider");
			int csid=Integer.parseInt(csprops.get("Chemspider ID"));
			
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
					toRemove.add(match);
				}
				
			} catch (BioclipseException e) {
				logger.error("Error querying chembl: " + e.getMessage());
			}
			
		}

		//Remove the ones with no ChEMBL data
		matches.removeAll(toRemove);
		return matches;
	}

}