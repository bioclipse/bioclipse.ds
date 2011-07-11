/* *****************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.adme.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.RuleOfFiveDescriptor;
import org.openscience.cdk.qsar.result.IntegerResult;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.SimpleResult;


public class RuleOfFiveTest extends AbstractDSTest{

    private static final Logger logger = Logger.getLogger(RuleOfFiveTest.class);

    
    public void initialize( IProgressMonitor monitor ) throws DSException {
    }


    @Override
    protected List<? extends ITestResult> doRunTest( ICDKMolecule cdkmol,
                                                     IProgressMonitor monitor ) {

//    	monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

        //Store results here
        ArrayList<SimpleResult> results=new 
                                             ArrayList<SimpleResult>();

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


	@Override
	public List<String> getRequiredParameters() {
		return null;
	}

}
