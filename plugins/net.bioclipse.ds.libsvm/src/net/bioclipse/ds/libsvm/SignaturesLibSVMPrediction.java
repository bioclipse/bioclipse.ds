package net.bioclipse.ds.libsvm;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import libsvm.svm;
import libsvm.svm_node;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.PosNegIncMatch;
import net.bioclipse.ds.model.result.ScaledResultMatch;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.interfaces.IAtom;

public class SignaturesLibSVMPrediction extends SignaturesLibSVMTest{

    //The logger of the class
    private static final Logger logger = Logger.getLogger(SignaturesLibSVMPrediction.class);

	private static final String HIGH_PERCENTILE = "highPercentile";
	private static final String LOW_PERCENTILE = "lowPercentile";
	
	protected double lowPercentile;
	protected double highPercentile;

    //We need to ensure that '.' is always decimal separator in all locales
    DecimalFormat formatter=new DecimalFormat("0.000");

    
    
    @Override
    public List<String> getRequiredParameters() {
        List<String> ret=super.getRequiredParameters();
        ret.add( HIGH_PERCENTILE );
        ret.add( LOW_PERCENTILE );
        return ret;
    }
    
    @Override
    public void initialize(IProgressMonitor monitor) throws DSException {
    	super.initialize(monitor);

    	highPercentile=Double.parseDouble( getParameters().get( HIGH_PERCENTILE ));
    	lowPercentile=Double.parseDouble( getParameters().get( LOW_PERCENTILE ));

    }

    
	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {
		
        ISignaturesManager sign=net.bioclipse.ds.signatures.Activator.
        getDefault().getJavaSignaturesManager();

        //Make room for results
        List<ITestResult> results=new ArrayList<ITestResult>();

		
		Map<String, Double> moleculeSignatures = new HashMap<String, Double>(); // Contains the signatures for a molecule and the count. We store the count as a double although it is an integer. libsvm wants a double.
		Map<String, Integer> moleculeSignaturesHeight = new HashMap<String, Integer>(); //Contains the height for a specific signature.
		Map<String, Integer> moleculeSignaturesAtomNr = new HashMap<String, Integer>(); //Contains the atomNr for a specific signature.
		for (int height = startHeight; height <= endHeight; height++){
			
			//Use the sign manager to generate signatures
			List<String> signs;
			try {
				signs = sign.generate( cdkmol, height ).getSignatures();
			} catch (BioclipseException e) {
	              return returnError( "Error generating signatures","");
			}

			Iterator<String> signsIter = signs.iterator();
			while (signsIter.hasNext()){
				String currentSignature = signsIter.next();
				if (signatures.contains(currentSignature)){
					moleculeSignaturesHeight.put(currentSignature, height);
					moleculeSignaturesAtomNr.put(currentSignature, signs.indexOf(currentSignature));
					if (moleculeSignatures.containsKey(currentSignature)){
						moleculeSignatures.put(currentSignature, (Double)moleculeSignatures.get(currentSignature)+1.00);
					}
					else{
						moleculeSignatures.put(currentSignature, 1.0);
					}
				}
			}
		}
		// Do a prediction for a single molecule.
		// Create a descriptor array for the molecule in libsvm format.
		svm_node[] moleculeArray = new svm_node[moleculeSignatures.size()];
		Iterator<String> signaturesIter = signatures.iterator();
		int i = 0;
		while (signaturesIter.hasNext()){
			String currentSignature = signaturesIter.next();
			if (moleculeSignatures.containsKey(currentSignature)){
				moleculeArray[i] = new svm_node();
				moleculeArray[i].index = signatures.indexOf(currentSignature)+1; // libsvm assumes that the index starts at one.
				moleculeArray[i].value = (Double) moleculeSignatures.get(currentSignature);
				i = i + 1;
			}
		}
		// Predict
		double prediction = 0.0;
		prediction = svm.svm_predict(svmModel, moleculeArray);
		System.out.println("Pred: " + prediction);
		
		List<Double> gradientComponents = new ArrayList<Double>();
		// Get the most significant signature.
		double decValues[] = new double[1];
		double lowerPointValue = 0.0, higherPointValue = 0.0;
		svm.svm_predict_values(svmModel, moleculeArray, decValues);
		lowerPointValue = decValues[0];
		for (int element = 0; element < moleculeArray.length; element++){
			// Temporarily increase the descriptor value by one to compute the corresponding component of the gradient of the decision function.
			moleculeArray[element].value = moleculeArray[element].value + 1.00;
			svm.svm_predict_values(svmModel, moleculeArray, decValues);
			higherPointValue = decValues[0];
			if (svmModel.rho[0] > 0.0){ // Check if the decision function is reversed.
				gradientComponents.add(higherPointValue-lowerPointValue);
			}
			else{
				gradientComponents.add(lowerPointValue-higherPointValue);						
			}
			// Set the value back to what it was.
			moleculeArray[element].value = moleculeArray[element].value - 1.00;
				
		}
		//If Classification
		if (svmModel.param.svm_type == 0){ // This is a classification model.
			
			String significantSignature="";
			int centerAtom = -1;
			int height = -1;
			
			if (prediction > 0.0){ // Look for most positive component, we have a poitive prediction
				double maxComponent = -1.0;
				int elementMaxVal = -1;
				for (int element = 0; element < moleculeArray.length; element++){
					if (gradientComponents.get(element) > maxComponent){
						maxComponent = gradientComponents.get(element);
						elementMaxVal = element;
					}
				}
				if (maxComponent > 0.0){
					significantSignature=signatures.get(moleculeArray[elementMaxVal].index-1);
					centerAtom=moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[elementMaxVal].index-1));
					height = moleculeSignaturesHeight.get(signatures.get(moleculeArray[elementMaxVal].index-1));
					
					System.out.println("Max atom: " + centerAtom  + 
							", max val: " + gradientComponents.get(elementMaxVal) + 
							", signature: " + significantSignature + 
							", height: " + height);
					
				}
				else{
					System.out.println("No significant signature.");						
				}
			}
			else{ // Look for most negative component, we have a negative prediction
				double minComponent = 1.0;
				int elementMinVal = -1;
				for (int element = 0; element < moleculeArray.length; element++){
					if (gradientComponents.get(element) < minComponent){
						minComponent = gradientComponents.get(element);
						elementMinVal = element;
					}
				}
				if (minComponent < 0.0){

					significantSignature=signatures.get(moleculeArray[elementMinVal].index-1);
					centerAtom=moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[elementMinVal].index-1));
					height = moleculeSignaturesHeight.get(signatures.get(moleculeArray[elementMinVal].index-1));
					
					System.out.println("Min atom: " + centerAtom + 
							", min val: " + gradientComponents.get(elementMinVal) + 
							", signature: " + significantSignature + 
							", height: " + height);

				}
				else{
					System.out.println("No significant signature.");
				}
			}

			//Create the result
	        PosNegIncMatch match = new PosNegIncMatch(significantSignature, 
	        		ITestResult.INCONCLUSIVE);
	        if (prediction>0)
	            match.setClassification( ITestResult.POSITIVE );
	        else
	            match.setClassification( ITestResult.NEGATIVE );

	        
            if (significantSignature.length()>0){
				//OK, color atoms
            	
                match.putAtomResult( centerAtom, 
                		match.getClassification() );
                
                int currentHeight=0;
                List<Integer> lastNeighbours=new ArrayList<Integer>();
                lastNeighbours.add(centerAtom);
                
                while (currentHeight<height){
                	
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
                
    	        //We can have multiple hits...
    	        //...but here we only have one
                results.add( match );

			}

		}

		//Else we have a regression model
		//Sum up all atom gradients
		else {
			Map<Integer, Double> atomGreadientComponents = new HashMap<Integer, Double>(); // Contains a sum of all gradient components for a given atom.
			for (int element = 0; element < moleculeArray.length; element++){
				int atomNr = moleculeSignaturesAtomNr.get(signatures.get(moleculeArray[element].index-1));
				double componentVal = gradientComponents.get(element);
				if (atomGreadientComponents.containsKey(atomNr)){
					atomGreadientComponents.put(atomNr, atomGreadientComponents.get(atomNr)+componentVal);
				}
				else{
					atomGreadientComponents.put(atomNr,componentVal);
					
				}
			}
			
			
	        ScaledResultMatch match = new ScaledResultMatch("Result: " 
                    + formatter.format( prediction ), 
                    ITestResult.POSITIVE);
	        
	        //Neg prediction means green - Negative overall results for the model
	        if (prediction<=0)
	        	match.setClassification(ITestResult.NEGATIVE);

	        //Color atoms according to accumulated gradient values
	        for (int currentAtomNr : atomGreadientComponents.keySet()){
	        	Double currentDeriv = atomGreadientComponents.get(currentAtomNr);
	        	
    			double scaledDeriv = scaleDerivative(currentDeriv);
    			match.putAtomResult( currentAtomNr-1, scaledDeriv );
	        	
	        }

			System.out.println(atomGreadientComponents.toString());					
			
	        //We can have multiple hits...
	        //...but here we only have one
	        results.add( match );
	        
		}

        return results;

	}
	
	
	
    /**
     * Return a scaling between -1 and 1
     * @param currentDeriv
     * @return
     */
	private double scaleDerivative(Double currentDeriv) {

		//We have a fixed boundary on a low and high percentile
		//so cut away anything below or above this
		if (currentDeriv<=lowPercentile)
			return -1;
		else if (currentDeriv>=highPercentile)
			return 1;
		else if (currentDeriv==0)
			return 0;

		//Since not symmetric around 0, scale pos and neg intervals individually
		if (currentDeriv<0)
			return currentDeriv/(-lowPercentile);
		else
			return currentDeriv/highPercentile;
	}

}
