package net.bioclipse.ds.lipinski.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.RuleOfFiveDescriptor;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.qsar.result.IntegerResult;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.SimpleResult;
import net.bioclipse.ds.model.impl.DSException;


public class RuleOfFiveTest extends AbstractDSTest{

    private static final Logger logger = Logger.getLogger(RuleOfFiveTest.class);

    
    public void initialize( IProgressMonitor monitor ) throws DSException {
    }

    public List<? extends ITestResult> runWarningTest( IMolecule molecule,
                                                       IProgressMonitor monitor ) {

        
        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");
        
        //Store results here
        List<ITestResult> results=new ArrayList<ITestResult>();
                
        if (getTestErrorMessage().length()>1){
            return results;
        }

        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();
        
        ICDKMolecule cdkmol = null;
        ICDKMolecule cdkmol_in = null;
        try {
            cdkmol_in = cdk.create( molecule );
            cdkmol=new CDKMolecule((IAtomContainer)cdkmol_in.getAtomContainer().clone());
//            cdkmol = cdk.create( molecule );
        } catch ( BioclipseException e ) {
            return returnError( "Could not create CDKMolecule", e.getMessage() );
        } catch ( CloneNotSupportedException e ) {
            return returnError( "Could not clone CDKMolecule", e.getMessage() );
        }

        IAtomContainer ac=cdkmol.getAtomContainer();
        RuleOfFiveDescriptor descriptor = new RuleOfFiveDescriptor();
        Object[] params = { new Boolean(true) };
        try {
            descriptor.setParameters(params);
        } catch ( CDKException e ) {
            return returnError( e.getMessage(), e.getMessage() );
        }
        DescriptorValue res= descriptor.calculate( ac);
        IntegerResult val = (IntegerResult) res.getValue();
        int lipinskires=val.intValue();
//        if (lipinskires>0)
            results.add(new SimpleResult("Failures: " + lipinskires, ITestResult.INFORMATIVE));
        
        return results;
    }

}
