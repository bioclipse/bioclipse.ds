package net.bioclipse.ds.r;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.matcher.BaseSignaturesMatcher;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.DoubleResult;
import net.bioclipse.ds.model.result.PosNegIncMatch;
import net.bioclipse.r.business.Activator;
import net.bioclipse.r.business.IRBusinessManager;

/**
 * A class building on R and Signatures for prediction
 * 
 * @author ola
 *
 */
public abstract class SignaturesRModelMatcher extends RModelMatcher{
	
	BaseSignaturesMatcher signaturesMatcher;
	protected IRBusinessManager R;
	

	public SignaturesRModelMatcher() {
		super();
		
		signaturesMatcher=new BaseSignaturesMatcher() {
			@Override
			protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol, IProgressMonitor monitor) {
				return null;
			}
		};
		
	}
	
	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {
		// TODO Auto-generated method stub
		super.initialize(monitor);
		
        R = Activator.getDefault().getJavaRBusinessManager();

		//copy parameters for this model to wrapped sign model
		signaturesMatcher.setPluginID(getPluginID());
		signaturesMatcher.setParameters(getParameters());
		signaturesMatcher.initialize(monitor);
	}


}
