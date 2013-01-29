package net.bioclipse.ds.r;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.matcher.BaseSignaturesMatcher;
import net.bioclipse.ds.matcher.model.SignatureFrequenceyResult;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.DoubleResult;
import net.bioclipse.ds.model.result.PosNegIncMatch;
import net.bioclipse.r.business.Activator;
import net.bioclipse.r.business.IRBusinessManager;

/**
 * A class building on R and Signatures for prediction with 
 * dense signature representation
 * 
 * @author ola
 *
 */
public abstract class DenseSignaturesRModelMatcher extends SignaturesRModelMatcher{


	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {
		

		
        //Make room for results
        List<ITestResult> results=new ArrayList<ITestResult>();

        //Calculate the frequency of the modelSignatures
        SignatureFrequenceyResult signResults;
		try {
			signResults = signaturesMatcher.countSignatureFrequency(cdkmol);
		} catch (BioclipseException e) {
            return returnError( "Error generating modelSignatures",e.getMessage());
		}

		//Construct string to send to R
		System.out.println("Signatures array is length: " + signaturesMatcher.getSignatures().size());
		
		Iterator<String> signaturesIter = signaturesMatcher.getSignatures().iterator();
		List<String> cmdParts= new ArrayList<String>();
		int no = 0;
		StringBuffer buf = new StringBuffer("c(");
		while (signaturesIter.hasNext()){
			no++;
			String currentSignature = signaturesIter.next();
			if (signResults.getMoleculeSignatures().containsKey(currentSignature))
				buf.append(signResults.getMoleculeSignatures().get(currentSignature) + ",");
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
		for (String rcmd : getPredictionString("inp")){
//			System.out.println(rcmd);
			ret = R.eval(rcmd);
//	        System.out.println("R said: " + ret);
		}
		        
        //Parse result and create testresults
        double posProb = Double.parseDouble(ret.substring(4));
        
        
        String mostImportantRcmd = getMostImportantSignaturesCommand();
		ret = R.eval(mostImportantRcmd);
		//Result should be on form: [1]  191  434 1683
		//TODO: handle errors here...

		//Parse and create TestResults
		String[] parts = ret.trim().substring(4).split(" ");
		int pos = Integer.parseInt(parts[0]);
		int neg = Integer.parseInt(parts[1]);
		int zero = Integer.parseInt(parts[2]);
		
		String posSign = signaturesMatcher.getSignatures().get(pos);
		String negSign = signaturesMatcher.getSignatures().get(neg);
		String zeroSign = signaturesMatcher.getSignatures().get(zero);

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

	protected abstract List<String> getPredictionString(String input);
	protected abstract String getMostImportantSignaturesCommand();

}
