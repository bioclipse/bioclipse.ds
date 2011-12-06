/* *****************************************************************************
 * Copyright (c) 2010 Ola Spjuth.
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
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;

/**
 * 
 * @author ola
 *
 */
public class LogPTest extends AbstractDSTest{

    private static final Logger logger = Logger.getLogger(LogPTest.class);

    
    public void initialize( IProgressMonitor monitor ) throws DSException {
    }


    @Override
    protected List<? extends ITestResult> doRunTest( ICDKMolecule cdkmol,
                                                     IProgressMonitor monitor ){

//    	monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

        //Store results here
        ArrayList<net.bioclipse.ds.model.result.DoubleResult> results 
                 = new ArrayList<net.bioclipse.ds.model.result.DoubleResult>();

        try {
            Activator.getDefault().getJavaCDKManager()
                .addExplicitHydrogens( cdkmol );
        } catch ( Exception e1 ) {
            return returnError( "Error adding explicit hydrogens"
                                , e1.getMessage() );
        }

        IAtomContainer ac=cdkmol.getAtomContainer();
        
        /*
         * XLOGP
         */
        XLogPDescriptor descriptor = new XLogPDescriptor();

        //Check aromaticity and use salicyl correction factor
        try {
            Object[] params = { new Boolean(true), new Boolean(true) };
            descriptor.setParameters(params);
        } catch ( CDKException e ) {
            return returnError( "Error setting XLOGP parameters"
                                , e.getMessage() );
        }
        DescriptorValue res= descriptor.calculate( ac);
        DoubleResult val = (DoubleResult) res.getValue();
        double result=val.doubleValue();
        if (Double.isNaN(result))
	        results.add(new net.bioclipse.ds.model.result.DoubleResult(
                                                    "XLogP"
                                                    , result
                                                    , ITestResult.ERROR));
        else{
            results.add(new net.bioclipse.ds.model.result.DoubleResult(
                    "XLogP"
                    , result
                    , ITestResult.INFORMATIVE));
            logger.debug("   XLogP" + "=" + result);
        }        
        /*
         * ALOGP
         */
        //Descriptor requires us to detect ariomaticity.
        try {
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(ac);
            CDKHueckelAromaticityDetector.detectAromaticity(ac);
        } catch ( CDKException e ) {
            return returnError( "Error detecting aromaticity", e.getMessage() );
        }

        ALOGPDescriptor alogpDescriptor;
        try {
            alogpDescriptor = new ALOGPDescriptor();
        } catch ( CDKException e ) {
            return returnError( "Error initializing ALogP", e.getMessage() );
        }

        DescriptorValue res2= alogpDescriptor.calculate( ac);
        DoubleArrayResult val2 = (DoubleArrayResult) res2.getValue();
        for (int i=0; i< res2.getNames().length;i++){
            String name = res2.getNames()[i];
            double value = val2.get( i );

            //Skip alogp2, which is just the squared alogp
            if (i!=1){
                if (Double.isNaN(value))
                    results.add(new net.bioclipse.ds.model.result.DoubleResult(
                            name, value, ITestResult.ERROR));
                else{
                    results.add(new net.bioclipse.ds.model.result.DoubleResult(
                            name, value, ITestResult.INFORMATIVE));
                    logger.debug("   " + name + "=" + value);
                }
            }
        }
        
        return results;
    }


	@Override
	public List<String> getRequiredParameters() {
		return null;
	}

}
