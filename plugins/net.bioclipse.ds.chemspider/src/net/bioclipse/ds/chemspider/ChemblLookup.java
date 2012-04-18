package net.bioclipse.ds.chemspider;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.rdf.Activator;
import net.bioclipse.rdf.business.IRDFManager;

/**
 * Look up interactions in ChEMBL-rdf for a given Chemspider ID
 *  
 * @author Ola Spjuth
 *
 */
public class ChemblLookup {

	public static List<ChemblInteraction> lookupCSID(Integer csid) throws BioclipseException{

		IRDFManager rdf = Activator.getDefault().getJavaManager();
		String endpoint = "http://rdf.farmbio.uu.se/chembl/sparql";

		//10368587 is one example
		
		String query =			
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
		"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"+
		"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#>"+
		"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/>"+
		"PREFIX bibo: <http://purl.org/ontology/bibo/>"+
		"PREFIX cito: <http://purl.org/spar/cito/>"+
		"PREFIX cheminf: <http://semanticscience.org/resource/>"+
		"PREFIX pro: <http://purl.obolibrary.org/obo/>"+
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+

		"SELECT ?val ?units ?targetType ?title ?interactionType ?relation ?description ?assay WHERE {"+
		"?csid <http://semanticscience.org/resource/SIO_000300> " + csid + " ."+
		"?chembl cheminf:CHEMINF_000200 ?csid ."+
		"?mol owl:equivalentClass ?chembl ."+
		"?act chembl:forMolecule ?mol ."+
		"?act chembl:standardValue ?val ."+
		"FILTER (?val < 100000)"+
		"?act chembl:standardUnits ?units ."+
		"?act chembl:onAssay ?assay ."+
		"?act chembl:type ?interactionType ."+
		"?assay chembl:hasTarget ?target ."+
		 "?assay chembl:hasDescription ?description ."+
		"?target dc:title ?title ."+
		"OPTIONAL { ?target chembl:classL1 ?targetType . }"+
		"OPTIONAL { ?act chembl:relation ?relation . }"+
		"}";		
		
		IStringMatrix res = rdf.sparqlRemote(endpoint, query);

//		System.out.println(res);
		
		if (res.getColumnCount()<=0)
			return null;

		System.out.println("Got " + res.getRowCount() + " hits for CSID=" + csid);

		List<ChemblInteraction> interactions = new ArrayList<ChemblInteraction>();
		for (int i=0; i < res.getRowCount(); i++){
			String value = res.getColumn("val").get(i);
			String unit = res.getColumn("units").get(i);
			String inttype = res.getColumn("interactionType").get(i);
			String targetType = res.getColumn("targetType").get(i);
			String title = res.getColumn("title").get(i);
			String interactionType = res.getColumn("interactionType").get(i);
			String relation = res.getColumn("relation").get(i);
			String description = res.getColumn("description").get(i);
			String assay = res.getColumn("assay").get(i);

			value=value.substring(0,value.indexOf("^^"));
			if (null==title || title.length()<=0)
				title=assay.substring(assay.lastIndexOf("/")+1);

			ChemblInteraction interaction = new ChemblInteraction(value, unit, inttype);
			
			interaction.setTitle(title);
			interaction.setRelation(relation);
			interaction.setTargetType(targetType);
			interaction.setInteractionType(interactionType);
			interaction.setDescription(description);

			interactions.add(interaction);
//			System.out.println(interaction);

		}
		
		return interactions;
	}
	
}
