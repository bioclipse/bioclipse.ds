package net.bioclipse.ds.r.epafhm;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.ds.r.DenseSignaturesRModelMatcher;

public class DenseEPAFHMMatcher extends DenseSignaturesRModelMatcher{

	/**
	 * Provide the R commands to deliver the prediction command to R
	 * from the input String (dense numerical vector with signature frequency).
	 */
	@Override
	protected List<String> getPredictionString(String input){
		
		List<String> ret = new ArrayList<String>();
		ret.add("inpf <- as.data.frame(t(" + input + "))\n");
		ret.add("colnames(inpf) <- colnames(epafhm.train.svm$SV)\n");
        ret.add("epafhm.test.predicted <- predict(epafhm.train.svm, inpf, probability=T)");
        ret.add("attributes(epafhm.test.predicted)$probabilities[1,1]\n");
		                                                
		return ret;
	}

	@Override
	protected String getMostImportantSignaturesCommand() {
		return "getMostImportantSignature.svm(epafhm.train.svm, inpf)";
	}

	
}
