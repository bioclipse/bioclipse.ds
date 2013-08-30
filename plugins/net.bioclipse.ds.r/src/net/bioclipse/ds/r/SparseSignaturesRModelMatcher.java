package net.bioclipse.ds.r;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.interfaces.IAtom;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.matcher.model.SignatureFrequenceyResult;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.DoubleResult;
import net.bioclipse.ds.model.result.PosNegIncMatch;

/**
 * A class building on R and Signatures for prediction with 
 * sparse signature representation
 * 
 * @author ola
 *
 */
public class SparseSignaturesRModelMatcher extends SignaturesRModelMatcher{

	private static final Logger logger = Logger.getLogger(SparseSignaturesRModelMatcher.class);
	
//    DecimalFormatSymbols sym=new DecimalFormatSymbols();
//    sym.setDecimalSeparator( '.' );
    DecimalFormat formatter = new DecimalFormat("0.000");

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

		//Set up the values for the sparse R-matrix
		Iterator<String> signaturesIter = signaturesMatcher.getSignatures().iterator();
		int no = 0;
		List<Integer> values = new ArrayList<Integer>();
		List<Integer> indices = new ArrayList<Integer>();
		while (signaturesIter.hasNext()){
			no++;
			String currentSignature = signaturesIter.next();
			
			//If we have match, store its index in the modelSignatures array along with its frequency
			if (signResults.getMoleculeSignatures().containsKey(currentSignature)){
				values.add(signResults.getMoleculeSignatures().get(currentSignature));
				indices.add(signaturesMatcher.getSignatures().indexOf(currentSignature)+1);  //We add one to get on bas 1, which is used by R
			}

		}
		
		//Formulate input to R in sparse format

//		> tmp <- new("matrix.csr", 
//		"ra" = c(1,2,3), 			   //VALUES
//		"ja"=as.integer(c(3,5,7)),     //COLUMN INDICES
//		"ia" = as.integer(c(1,4)),     //WHERE DO NEW LINES START IN THE VALUES ARRAY
//		dimension=as.integer(c(1,9)))  //TOTAL SIZE
//		
//		> as.matrix(tmp)
//	     [,1] [,2] [,3] [,4] [,5] [,6] [,7] [,8] [,9]
//	[1,]    0    0    1    0    2    0    3    0    0

		
		String modelSpecificMatrix="tmp."+getId();

		//Set up the input matrix in sparse format
		String rCommand= "new(\"matrix.csr\", " +
				"\"ra\" = c(" + values.toString().substring(1,values.toString().length()-1) + "), " + 
				"\"ja\" = as.integer(c(" + indices.toString().substring(1,indices.toString().length()-1) + ")), " +
				"\"ia\" = as.integer(c(1," + (values.size()+1) + "))," +
				"\"dimension\" = as.integer(c(1," + signaturesMatcher.getSignatures().size() + ")))";				;
		
		String output = R.eval(modelSpecificMatrix + " <- " + rCommand, servi);
		if (output.startsWith("Error")) return returnError(output, output);
		
		//Do predictions in R
		for (String rcmd : getPredictionString(modelSpecificMatrix)){
			System.out.println(rcmd);
			output = R.eval(rcmd, servi);
//	        System.out.println("R said: " + ret);
		}
		        
        //Parse result and create testresults
        double posProb = Double.parseDouble(output.substring(4));
        
        //Check what rho is, if negative then invert predictions
        //FIXME
		output = R.eval("cas.svm$rho", servi);
		if (output.substring(4).startsWith("-")){
			System.out.println("RHO IS NEG - INVERT!");
			posProb=1-posProb;
		}
	
        
		int overallPrediction;
        if (posProb>=0.5)
        	overallPrediction = ITestResult.POSITIVE;
        else
        	overallPrediction = ITestResult.NEGATIVE;

        
		//Create the result for the classification, overwrite name later if we have sign signature
        PosNegIncMatch match = new PosNegIncMatch("Probability: " + formatter.format(posProb), posProb,  overallPrediction);

		//Try to predict important modelSignatures
        String mostImportantRcmd = getMostImportantSignaturesCommand(modelSpecificMatrix);
		output = R.eval(mostImportantRcmd, servi);
		if (output.contains("An error occurred") || output.startsWith("Error")){
			return results;
		}

		//Result should be on form: [1]  191  434 1683

		//Parse and create TestResults
		String[] parts = output.trim().split("\\s+");
		String posSign = "";
		String negSign = "";
		String zeroSign = "";
		try{
			int pos = Integer.parseInt(parts[1])-1;  //We subtract by one to get back into bas 0 used in Java
			posSign = signaturesMatcher.getSignatures().get(pos);
		}catch(NumberFormatException e){
			logger.debug("Could not parse positive significant signature: " + parts[1]);
		}
		try{
			int neg = Integer.parseInt(parts[2])-1;  //We subtract by one to get back into bas 0 used in Java
			negSign = signaturesMatcher.getSignatures().get(neg);
		}catch(NumberFormatException e){
			logger.debug("Could not parse positive significant signature: " + parts[1]);
		}
		try{
			int zero = Integer.parseInt(parts[3])-1;  //We subtract by one to get back into bas 0 used in Java
			zeroSign = signaturesMatcher.getSignatures().get(zero);
		}catch(NumberFormatException e){
			logger.debug("Could not parse positive significant signature: " + parts[1]);
		}

		//TODO: Also include negative and zero significant modelSignatures
        
        if (posSign.length()>0){
			//OK, color atoms
        	
        	//We need the center atoms for this signature (could be more than one)
        	List<Integer> centerAtoms = signResults.getMoleculeSignaturesAtomNr().get(posSign);
        	Integer height = signResults.getMoleculeSignaturesHeight().get(posSign);

        	//Should we do this?
//        	match.setName(posSign);
        	
        	for (int centerAtom : centerAtoms){
        		
                match.putAtomResult( centerAtom, match.getClassification() );
                
                int currentHeight=0;
                List<Integer> lastNeighbours=new ArrayList<Integer>();
                lastNeighbours.add(centerAtom);
                
                while (currentHeight < height){
                	
                    List<Integer> newNeighbours=new ArrayList<Integer>();

                    //for all lastNeighbours, get new neighbours
                	for (Integer lastneighbour : lastNeighbours){
                        for (IAtom nbr : cdkmol.getAtomContainer().getConnectedAtomsList(
                             	  cdkmol.getAtomContainer().getAtom( lastneighbour )) ){
                        	
                            //Set each neighbour atom to overall match classification
                        	int nbrAtomNr = cdkmol.getAtomContainer().getAtomNumber(nbr);
                        	match.putAtomResult( nbrAtomNr, match.getClassification() );
                        	
                        	newNeighbours.add(nbrAtomNr);
                        	
                        }
                	}
                	
                	lastNeighbours=newNeighbours;

                	currentHeight++;
                }
                
        	}
        	

		}
        
        

        //We can have multiple hits...
        //...but here we only have one
        results.add( match );
        
        return results;

/*        
		DoubleResult accuracy = new DoubleResult("Probability", posProb, overallPrediction);
		results.add(accuracy);

		//Try to predict important modelSignatures
        String mostImportantRcmd = getMostImportantSignaturesCommand();
		ret = R.eval(mostImportantRcmd);
		if (ret.contains("An error occurred") || ret.startsWith("Error")){
			return results;
		}

		//Result should be on form: [1]  191  434 1683

		//Parse and create TestResults
		String[] parts = ret.trim().substring(4).split(" ");
		int pos = Integer.parseInt(parts[0]);
		int neg = Integer.parseInt(parts[1]);
		int zero = Integer.parseInt(parts[2]);
		
		String posSign = signaturesMatcher.getSignatures().get(pos);
		String negSign = signaturesMatcher.getSignatures().get(neg);
		String zeroSign = signaturesMatcher.getSignatures().get(zero);
        	
		PosNegIncMatch posMatch = new PosNegIncMatch("pos: " + posSign, overallPrediction);
		PosNegIncMatch negMatch = new PosNegIncMatch("neg: " + negSign, overallPrediction);
		PosNegIncMatch zeroMatch = new PosNegIncMatch("zero: " + zeroSign, overallPrediction);

		results.add(posMatch);
		results.add(negMatch);
		results.add(zeroMatch);

        return results;
		*/
	}

	
	/**
	 * Provide the R commands to deliver the prediction command to R
	 * from the input String (dense numerical vector with signature frequency).
	 */
	protected List<String> getPredictionString(String input){

		String tempvar="tmp.pred."+getId();
		List<String> ret = new ArrayList<String>();
        ret.add(tempvar + " <- predict(" + rmodel + "," + input + ", probability=T)");
        ret.add("attributes(" + tempvar + ")$probabilities[1,1]\n");
		return ret;
	}

	protected String getMostImportantSignaturesCommand(String input) {
		return "getMostImportantSignature.sparse.svm(" + rmodel + ", " + input + ")";
	}

}
