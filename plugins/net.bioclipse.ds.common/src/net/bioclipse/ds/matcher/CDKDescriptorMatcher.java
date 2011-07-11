package net.bioclipse.ds.matcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;

public class CDKDescriptorMatcher extends AbstractDSTest{
	
    protected static final String DESCRIPTOR_PARAM = "descriptors";

	@Override
    public List<String> getRequiredParameters() {
    	return new ArrayList<String>(){{
    	    add(DESCRIPTOR_PARAM);
    	}};
    }

	

	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {
    	super.initialize(monitor);

    	String descriptors=getParameters().get( DESCRIPTOR_PARAM );
        //Validate descriptors
        //TODO

	}

	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {
		
		//Calculate descriptors for mol
		//TODO
		
		//Send to model in R
		//TODO
		
		//Handle results
		//TODO
		
		return null;
	}

}
