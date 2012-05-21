package net.bioclipse.ds.r;

import java.util.ArrayList;
import java.util.List;


/**
 * A class building on R and Signatures for prediction with 
 * dense signature representation
 * 
 * @author ola
 *
 */
public class U251RModelMatcher extends DenseCDKRModelMatcher{

	
	protected String getRowNames(String tempvar) {
		String rnames="names("+ tempvar + ") <- names(attr(u251.rf$terms, \"dataClasses\"))[-1]";
		return rnames;
	}


	
	/**
	 * Provide the R commands to deliver the prediction command to R
	 * from the input String (dense numerical vector with signature frequency).
	 */
	protected List<String> getPredictionString(String input){
		
		List<String> ret = new ArrayList<String>();
		
		String tempvar="tmp.pred."+getId();
		String predictvar="predicted."+getId();
		
		ret.add(tempvar + " <- predict(u251.naAndStdvTreatment, " + input + ")");
		ret.add(tempvar + " <- predict(u251.imputed, " + tempvar + ")");
		ret.add("names("+ tempvar + ") <- rownames(u251.rf$importance)");
		ret.add(predictvar + " <- predict(u251.rf, t(" + tempvar + "), type=\"prob\")[2]");
		ret.add(predictvar);
		return ret;
	}
	
}
