/* *****************************************************************************
 * Copyright (c) 2009-2010 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/

package net.bioclipse.ds.report;

import java.awt.Image;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.HBondAcceptorCountDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.IntegerResult;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.api.domain.IMolecule.Property;
import net.bioclipse.ds.cons.MajorityVote;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.report.model.Endpoint;
import net.bioclipse.ds.report.model.Result;
import net.bioclipse.ds.report.model.Test;
import net.bioclipse.ds.ui.ImageHelper;
import net.bioclipse.ds.ui.VotingConsensus;
import net.bioclipse.ds.ui.views.DSView;

/**
 * A helper class for DS Jasper Reports.
 * 
 * @author ola
 *
 */
public class ReportHelper {

    private static final Logger logger = Logger.getLogger(ReportHelper.class);

	/**
	 * Create the parameters part of a JasperReport
	 * @param rmodel
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map createParameters(DSSingleReportModel rmodel) {


		ICDKManager cdk = net.bioclipse.cdk.business.Activator
		.getDefault().getJavaCDKManager();

		ICDKMolecule mol = rmodel.getQueryMol();

		Map parameters=new HashMap();

		try {
			parameters.put("DS_STRUCTURE_IMAGE", createQueryMolImage(mol));
		} catch (BioclipseException e1) {
			parameters.put("DS_STRUCTURE_IMAGE", null);
		}
		
		//TODO: IMPLEMENT!
		parameters.put("DS_CONSENSUS_IMAGE",null);		
		

		//=============
		//Compound info
		//=============

		parameters.put("DS_COMPOUND_NAME",mol.getName());
		parameters.put("DS_COMPOUND_FORMULA",cdk.molecularFormula(mol));

	
		try {
			parameters.put("DS_COMPOUND_SMILES", cdk.calculateSMILES( mol ));
		} catch (BioclipseException e1) {
			parameters.put("DS_COMPOUND_SMILES", "N/A");
		}

		try {
			parameters.put("DS_COMPOUND_INCHI", 
					mol.getInChI(Property.USE_CALCULATED));
		} catch (BioclipseException e1) {
			parameters.put("DS_COMPOUND_INCHI", "N/A");
		}

		
		//==========
		//Properties
		//==========
		
		DecimalFormat format = new DecimalFormat("0.000");
		
		try {
			parameters.put("DS_PROP_MW", ""+format.format(cdk.calculateMass( mol )));
		} catch (BioclipseException e) {
			parameters.put("DS_PROP_MW", "N/A");
		}

		try {
			ALOGPDescriptor alogp=new ALOGPDescriptor();
			DescriptorValue res = alogp.calculate( mol.getAtomContainer() );
			DoubleArrayResult  val = (DoubleArrayResult ) res.getValue();

			parameters.put("DS_PROP_ALOGP", "" + format.format(val.get(0)));
		} catch (CDKException e) {
			parameters.put("DS_PROP_ALOGP", "N/A");
		}

		HBondAcceptorCountDescriptor haccdesc= new HBondAcceptorCountDescriptor();
		DescriptorValue res = haccdesc.calculate( mol.getAtomContainer() );
		IntegerResult  val = (IntegerResult) res.getValue();
		parameters.put("DS_PROP_HACC", "" + val.intValue());

		HBondAcceptorCountDescriptor hdondesc= new HBondAcceptorCountDescriptor();
		res = hdondesc.calculate( mol.getAtomContainer() );
		val = (IntegerResult) res.getValue();
		parameters.put("DS_PROP_HDONORS", "" + val.intValue());

		parameters.put("DS_PAGE_FOOTER", "This safety prediction was done " +
				"with Bioclipse version: " + System.getProperty( "eclipse.buildId" ));

		int npos=0;
		int nneg=0;
		int ninc=0;
		List<Integer> ress=new ArrayList<Integer>();
		for (net.bioclipse.ds.model.Endpoint ep : rmodel.getEndpoints()){
			ress.add(ep.getConsensus());
			if (ep.getConsensus()==ITestResult.POSITIVE)
				npos++;
			if (ep.getConsensus()==ITestResult.NEGATIVE)
				nneg++;
			if (ep.getConsensus()==ITestResult.INCONCLUSIVE)
				ninc++;
		}
		String consString="Endpoints: ";
		if (npos>0)
			consString+="" + npos + " positive, ";
		if (nneg>0)
			consString+="" + nneg + " negative, ";
		if (ninc>0)
			consString+="" + ninc + " inconclusive, ";
		consString=consString.substring(0, consString.length()-2);
		int consensusStatus=new MajorityVote().calculate(ress);
		consString+="\nMajority consensus: " 
			+ StatusHelper.statusToString(consensusStatus);
		
		parameters.put("DS_ENDPOINTS_SIZE", ress.size());
		parameters.put("DS_CLASSIFICATION_STRING", consString);
		parameters.put("DS_CONSENSUS_IMAGE", 
				StatusHelper.statusToImageData(consensusStatus));

		return parameters;
	}

	@SuppressWarnings("unchecked")
	public static List createBeanCollection(DSSingleReportModel rmodel) {

		// simulated collection for use in iReport
		List endpoints = new ArrayList ();

		for (net.bioclipse.ds.model.Endpoint ep: rmodel.getEndpoints()){
			
			if (ep.getTestruns()==null || ep.getTestruns().size()==0) continue;

			Endpoint rep=new Endpoint(ep.getName(), ep.getConsensusString(), 
					null, getEndpointConsensusImage(ep) );
			endpoints.add (rep);

			for (TestRun tr : ep.getTestruns()){
				
				if (tr.getMatches()==null || tr.getTest().isExcluded() 
									      || !tr.getTest().isVisible()  ){
					logger.debug("Excluded testrun: " + tr + " from report.");
					continue;
				}
				
				Test test=new Test(tr.getTest().getName(), 
						tr.getConsensusString(), 
						getTestConsensusImage(tr));
				rep.addTest(test);

				for (ITestResult match : tr.getMatches()){
					
					Image resultImage=null;
					try {
						resultImage = createResultImage(rmodel.getQueryMol(), 
								match);
					} catch (BioclipseException e) {
						e.printStackTrace();
					}
					Result res = new Result(match.getName(), 
							StatusHelper.statusToString(
									match.getClassification()),
							"",
							resultImage,
							getMatchConsensusImage(match.getClassification()));
					test.addResult(res);
				}
			}
		}

		return endpoints;


	}

	private static Image getMatchConsensusImage(int classification) {
		
		return StatusHelper.statusToImageData(classification);

	}


	private static Image createResultImage(ICDKMolecule mol,
			ITestResult match) throws BioclipseException {

		return ImageHelper.createImage(mol, match, 150, 150 , 0.5);

	}

	private static Image getTestConsensusImage(TestRun tr) {

		return StatusHelper.statusToImageData(tr.getConsensusStatus());

	}

	private static Image getEndpointConsensusImage(
			net.bioclipse.ds.model.Endpoint ep) {

		return StatusHelper.statusToImageData(ep.getConsensus());

	}

	private static Image createQueryMolImage(ICDKMolecule mol) 
	throws BioclipseException {

		return ImageHelper.createImage(mol, null, 190, 190 , 1);

	}

	
}
