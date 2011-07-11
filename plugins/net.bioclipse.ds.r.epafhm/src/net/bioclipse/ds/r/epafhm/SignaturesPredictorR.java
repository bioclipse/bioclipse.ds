package net.bioclipse.ds.r.epafhm;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import libsvm.svm_node;

import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.matcher.BaseSignaturesMatcher;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.DoubleResult;
import net.bioclipse.ds.model.result.PosNegIncMatch;
import net.bioclipse.ds.model.result.SimpleResult;
import net.bioclipse.ds.signatures.business.ISignaturesManager;
import net.bioclipse.r.business.Activator;
import net.bioclipse.r.business.IRBusinessManager;

public class SignaturesPredictorR extends BaseSignaturesMatcher{
	
	private static final String R_MODEL_PARAMETER = "rmodel";

	private IRBusinessManager R;

	//We need to ensure that '.' is always decimal separator in all locales
    DecimalFormat formatter=new DecimalFormat("0.000");
	
    @Override
    public List<String> getRequiredParameters() {
        List<String> ret=super.getRequiredParameters();
        ret.add( R_MODEL_PARAMETER );
        return ret;
    }


	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {
		super.initialize(monitor);

        R = Activator.getDefault().getJavaRBusinessManager();
        
        monitor.beginTask("Initializing " + getName(), 3);
        monitor.worked(1);
        
		//Load R data frames from property on model
        monitor.subTask("Loading model file into R");
        String rmodelFile = getFileFromParameter( R_MODEL_PARAMETER );
        String ret = R.eval("load(\"" + rmodelFile + "\")");
        if (ret.length()>0 && !(ret.trim().startsWith("[1] \"epafhm.train.svm\"")))
            throw new DSException("Error initializing R model: " 
            		+ rmodelFile + ". R said: " + ret);

        monitor.worked(1);
        monitor.subTask("Loading prediction library into R");
        ret=R.eval("library(e1071)");
        if (ret.length()>0 && !(ret.trim().startsWith("[1] \"e1071\"")))
            throw new DSException("Error loading package e1071 (libsvm). R said: " + ret);
        
        monitor.done();
        
//      ret=R.eval("library(randomForest)");
//        if (ret.length()>0 && !(ret.startsWith("[1] \"randomForest\"")))
//            throw new DSException("Error loading package RandomForest. R said: " + ret);
		
	}
	

	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {
		
        ISignaturesManager sign=net.bioclipse.ds.signatures.Activator.
        getDefault().getJavaSignaturesManager();

        //Make room for results
        List<ITestResult> results=new ArrayList<ITestResult>();

		Map<String, Integer> moleculeSignatures = new HashMap<String, Integer>(); // Contains the signatures for a molecule and the count. We store the count as a double although it is an integer.
		Map<String, Integer> moleculeSignaturesHeight = new HashMap<String, Integer>(); //Contains the height for a specific signature.
		Map<String, List<Integer>> moleculeSignaturesAtomNr = new HashMap<String, List<Integer>>(); //Contains the atomNr for a specific signature.
		for (int height = startHeight; height <= endHeight; height++){
			
			//Use the sign manager to generate signatures
			List<String> signs;
			try {
				signs = sign.generate( cdkmol, height ).getSignatures();
			} catch (BioclipseException e) {
	              return returnError( "Error generating signatures","");
			}
			
			
			Iterator<String> signsIter = signs.iterator();
			int signsIndex = 0;
			while (signsIter.hasNext()){
				String currentSignature = signsIter.next();
				if (signatures.contains(currentSignature)){
					if (!moleculeSignaturesAtomNr.containsKey(currentSignature)){
						moleculeSignaturesAtomNr.put(currentSignature, new ArrayList<Integer>());
					}
					moleculeSignaturesHeight.put(currentSignature, height);
					List<Integer> tmpList = moleculeSignaturesAtomNr.get(currentSignature);
					tmpList.add(signsIndex);
					moleculeSignaturesAtomNr.put(currentSignature, tmpList);
					if (moleculeSignatures.containsKey(currentSignature)){
						moleculeSignatures.put(currentSignature, moleculeSignatures.get(currentSignature)+1);
					}
					else{
						moleculeSignatures.put(currentSignature, 1);
					}
				}
				signsIndex++;
			}
		}

		//Construct string to send to R
		System.out.println("Signatures array is length: " + signatures.size());
		
		Iterator<String> signaturesIter = signatures.iterator();
		List<String> cmdParts= new ArrayList<String>();
		int no = 0;
		StringBuffer buf = new StringBuffer("c(");
		while (signaturesIter.hasNext()){
			no++;
			String currentSignature = signaturesIter.next();
			if (moleculeSignatures.containsKey(currentSignature))
				buf.append(moleculeSignatures.get(currentSignature) + ",");
			else
				buf.append("0,");
			if (no%500==0){
				cmdParts.add(buf.substring(0, buf.length()-1) + ")");
				buf = new StringBuffer("c(");
			}
		}
		//Terminate the last buffer
		cmdParts.add(buf.substring(0, buf.length()-1) + ")");
		buf = new StringBuffer("c(");
		
        int cnt = 0;
		for (String cmdpart : cmdParts){
	        R.eval("tmp <- " + cmdpart);
			if (cnt==0)
				R.eval("inp <- tmp");
			else
		        R.eval("inp <- c(inp,tmp)");
			cnt++;
		}

		//Do predictions in R
		String ret="";
		for (String rcmd : getRFPredictionString("inp")){
//			System.out.println(rcmd);
			ret = R.eval(rcmd);
//	        System.out.println("R said: " + ret);
		}
		        
        //Parse result and create testresults
        double posProb = Double.parseDouble(ret.substring(4));
        
		ret = R.eval("getMostImportantSignature.svm(epafhm.train.svm, inpf)");
		//on form: [1]  191  434 1683
		String[] parts = ret.trim().substring(4).split(" ");
		int pos = Integer.parseInt(parts[0]);
		int neg = Integer.parseInt(parts[1]);
		int zero = Integer.parseInt(parts[2]);
		
		String posSign = signatures.get(pos);
		String negSign = signatures.get(neg);
		String zeroSign = signatures.get(zero);

		int overallPrediction;
        if (posProb>=0.5)
        	overallPrediction = ITestResult.POSITIVE;
        else
        	overallPrediction = ITestResult.NEGATIVE;
        	
		DoubleResult accuracy = new DoubleResult("accuracy", posProb, overallPrediction);
		PosNegIncMatch posMatch = new PosNegIncMatch("pos: " + posSign, overallPrediction);
		PosNegIncMatch negMatch = new PosNegIncMatch("neg: " + negSign, overallPrediction);
		PosNegIncMatch zeroMatch = new PosNegIncMatch("zero: " + zeroSign, overallPrediction);

		results.add(accuracy);
		results.add(posMatch);
		results.add(negMatch);
		results.add(zeroMatch);

        return results;
		
	}
	
	
	private List<String> getRFPredictionString(String input){
		
		List<String> ret = new ArrayList<String>();
		ret.add("inpf <- as.data.frame(t(" + input + "))\n");
		ret.add("colnames(inpf) <- colnames(epafhm.train.svm$SV)\n");
        ret.add("epafhm.test.predicted <- predict(epafhm.train.svm, inpf, probability=T)");
        ret.add("attributes(epafhm.test.predicted)$probabilities[1,1]\n");
//        ret.add("predict(epafhm.train.rf, inpf, type=\"prob\")[1,1]\n");
		                                                
		return ret;
	}
	
}
