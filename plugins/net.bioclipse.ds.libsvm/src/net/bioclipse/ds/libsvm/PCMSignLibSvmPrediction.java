package net.bioclipse.ds.libsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import libsvm.svm_node;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

public class PCMSignLibSvmPrediction extends SignaturesLibSVMPrediction {

	private static final Logger logger = Logger.getLogger(PCMSignLibSvmPrediction.class);

	private static final String PROTEIN_DESCRIPTOR_FILE = "proteinDescriptorFile";
	private static final String PROTEIN_NAMES = "proteinNames";
	private static final String PROTEIN_DESCRIPTOR_START_INDEX= "proteinDescriptorStartIndex";
	
	List<List<Double>> protDescList;
	List<String> protNames;
	int proteinDescriptorStartIndex;
	
	//PCM requires a protein descriptor file
	@Override
    public List<String> getRequiredParameters() {
		List<String> params = super.getRequiredParameters();
		params.add(PROTEIN_DESCRIPTOR_FILE);
		params.add(PROTEIN_NAMES);
		params.add(PROTEIN_DESCRIPTOR_START_INDEX);
		return params;
    }


	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {
		super.initialize(monitor);
		
		//Read protein names from extension
    	String protnamesraw = getParameters().get(PROTEIN_NAMES );
    	protNames=Arrays.asList(protnamesraw.split(","));

		//Read matrix index for protein descriptors
    	String protStartIX = getParameters().get(PROTEIN_DESCRIPTOR_START_INDEX);
    	proteinDescriptorStartIndex=Integer.parseInt(protStartIX);

		//PCM requires a protein descriptor file
    	String protDescFile = getFileFromParameter(PROTEIN_DESCRIPTOR_FILE );
		logger.debug("Reading protein descriptor file: " + protDescFile);

		BufferedReader protDescReader=null;
		try {
			protDescReader = new BufferedReader(new FileReader(new File(protDescFile)));
			protDescList=new ArrayList<List<Double>>();
			String line;
			while ( (line = protDescReader.readLine()) != null ) {
				String[] parts = line.split("\t");
//				System.out.println("protdesc size: " + parts.length);
				List<Double> currprot=new ArrayList<Double>();
				for (String part : parts){
					double val = Double.parseDouble(part);
					currprot.add(val);
				}
				protDescList.add(currprot);
			}
		} catch (FileNotFoundException e) {
			LogUtils.debugTrace(logger, e);
			throw new DSException("Error reading protdesc file " 
					+ protDescFile + ": " + e.getMessage());
		} catch (IOException e) {
			LogUtils.debugTrace(logger, e);
			throw new DSException("Error reading protdesc file " 
					+ protDescFile + ": " + e.getMessage());
		}finally{
			try {
				if (protDescReader!=null)
					protDescReader.close();
			} catch (IOException e) {
				throw new DSException("Error closing file " 
						+ protDescFile + ": " + e.getMessage());
			}
		}

		logger.debug("Reading protein descriptor file: " + protDescFile + " completed successfully");
		logger.debug("Protein names: " + protNames.toString());
		logger.debug("Protein data size: " + protDescList.size());
		for (List<Double> r : protDescList){
			logger.debug(r.toString());
		}


	}
	
	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {
		
		List<ITestResult> allResults = new ArrayList<ITestResult>();

		for (int ix=0; ix<protNames.size();ix++){
			
			String protName = protNames.get(ix);
			List<Double> protDesc = protDescList.get(ix);
			
			logger.debug("Predicting PCM for: " + protName);

			//Take proteins one by ones
			List<svm_node> protSvmNodes = new ArrayList<svm_node>();
			for (int i=0; i< protDesc.size();i++){
				svm_node node = new svm_node();
				node.index = proteinDescriptorStartIndex+i; // libsvm assumes that the index starts at one.
				node.value = protDesc.get(i);
				protSvmNodes.add(node);
			}
			
			//Predict for this mol+prot combo
			List<? extends ITestResult> molprotPred = 
					super.predictLibSVM(cdkmol, protSvmNodes, false, monitor);

			//Update names for prediction
			for (ITestResult tr : molprotPred){
				tr.setName(protName);
			}
			allResults.addAll(molprotPred);
		}
		
		return allResults;
		
	}

}
