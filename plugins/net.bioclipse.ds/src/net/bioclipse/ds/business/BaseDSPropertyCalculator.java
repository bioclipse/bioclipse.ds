/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.business;

import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.impl.result.SimpleResult;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.model.report.ReportHelper;

import org.apache.log4j.Logger;


public abstract class BaseDSPropertyCalculator implements IPropertyCalculator<TestRun> {

    private static final Logger logger = Logger.getLogger(
                                               BaseDSPropertyCalculator.class);

    public abstract String getTestID();
    public abstract String getPropertyName();

    public TestRun calculate( ICDKMolecule molecule ) {

        IDSManager ds = net.bioclipse.ds.Activator.getDefault().getJavaManager();
        try {
            List<ITestResult> results = ds.runTest( getTestID(), molecule );
            TestRun tr= new TestRun();
            tr.setTest( ds.getTest( getTestID() ) );
            
            if (tr.getTest().getTestErrorMessage()!="")
                tr.setStatus( TestRun.ERROR );
            else
                tr.setStatus( TestRun.FINISHED );
            
            for (ITestResult result : results){
                tr.addResult( result );
                result.setTestRun( tr );
            }
            
            return tr;
        } catch ( BioclipseException e ) {
            LogUtils.handleException( e, logger, Activator.PLUGIN_ID);
        }
        return null;
    }


    public TestRun parse( String value ) {

        TestRun consrun=new TestRun();
        IDSTest test;
        try {
            test = Activator.getDefault().getJavaManager().getTest( getTestID() );
            consrun.setTest( test );
            SimpleResult res=new SimpleResult(getPropertyName(), 
                                              ReportHelper.stringToStatus( value ));
            res.setTestRun( consrun );
            consrun.addResult( res );
            return consrun;

        } catch ( BioclipseException e ) {
            LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
        }
        
        return null;
    }

    public String toString( Object value ) {

        TestRun tr = (TestRun)value;
        return tr.getConsensusString();
    }

}
