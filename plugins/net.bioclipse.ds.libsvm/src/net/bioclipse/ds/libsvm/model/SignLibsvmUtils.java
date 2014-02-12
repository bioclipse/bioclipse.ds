package net.bioclipse.ds.libsvm.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.signatures.Activator;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

import org.apache.log4j.Logger;

public class SignLibsvmUtils {

	private static final Logger logger = Logger.getLogger(SignLibsvmUtils.class);




	public static SignLibsvmModel ModelFromFile(String modelFile, String signaturesFile)
			throws IOException, DSException{

		List<String> signatures=readSignaturesFile(signaturesFile);
		svm_model svmModel = svm.svm_load_model(modelFile);
		
		SignLibsvmModel model = new SignLibsvmModel(svmModel,signatures);

		return model;
	}

    /**
     * Read a list of signatures from file into an arraylist
     * 
     * @param signaturesPath
     * @return
     * @throws IOException
     * @throws DSException
     */
	public static List<String> readSignaturesFile(String signaturesPath) throws IOException {
	    logger.debug("Reading signature file: " + signaturesPath);
	    BufferedReader signaturesReader = new BufferedReader( new FileReader( new File( signaturesPath ) ) );
	    List<String> signatures = readSignaturesFile( signaturesReader );
	    logger.debug("Reading signature file: " + signaturesPath + " completed successfully");
	    return signatures;
	}

    /**
     * Reads a list of signatures from a reader into a list.
     * 
     * @param reader
     * @return list of signatures
     * @throws IOException
     */
	public static List<String> readSignaturesFile(BufferedReader signaturesReader) throws IOException {

        List<String> signatures = new ArrayList<String>(); // Contains modelSignatures. We use the indexOf to retrieve the order of specific modelSignatures in descriptor array.
		try {
			String signature;
			while ( (signature = signaturesReader.readLine()) != null ) {
				//				if (modelSignatures.contains(signature))
				//					throw new DSException("Duplicate signature in modelSignatures list");
				signatures.add(signature);
			}
		}finally{
				if (signaturesReader!=null)
					signaturesReader.close();
		}

		return signatures;

	}
	
	
	public static List<String> generateSignatures(ICDKMolecule cdkmol,int startheight, int endheight)
			throws BioclipseException {
		ISignaturesManager signatures = Activator.getDefault().getJavaSignaturesManager();
		
		List<String> molsigns = new ArrayList<String>(); 
		for (int h=startheight; h< endheight; h++){
			molsigns.addAll(signatures.generate(cdkmol, h).getSignatures());
		}

		return molsigns;
	}

	
}
