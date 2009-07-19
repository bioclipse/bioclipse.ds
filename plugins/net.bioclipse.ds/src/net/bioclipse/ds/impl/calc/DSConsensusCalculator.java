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
package net.bioclipse.ds.impl.calc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.impl.result.SimpleResult;
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IConsensusCalculator;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.model.report.ReportHelper;

/**
 * 
 * @author ola
 *
 */
public abstract class DSConsensusCalculator implements IPropertyCalculator<TestRun>{

    private static final Logger logger = 
                                  Logger.getLogger(DSConsensusCalculator.class);

    public abstract String getPropertyName();

    protected abstract String getTestID();

    protected abstract String getEndpoint();


    
    public TestRun calculate( ICDKMolecule molecule ) {

        List<Integer> classifications=new ArrayList<Integer>();
        for(IPropertyCalculator<TestRun> calculator : getCalculators()) {
            String id = calculator.getPropertyName();
            TestRun tr = (TestRun) molecule.getProperty( id, Property.USE_CACHED );
            
            if (!(tr.getTest().isInformative())){
                if (tr.getTest().getTestErrorMessage().length()<1){
                    classifications.add( new Integer(tr.getConsensusStatus()));
                    logger.debug(" $$ Test: " + tr.getTest() + " got " +
                    		"          classification: " + tr.getConsensusStatus());
                }
            }
        }
        
        //The consensustest must provide a consensus calculator
        IDSManager ds = Activator.getDefault().getJavaManager();
        IDSTest consensusTest;
        try {
            consensusTest = ds.getTest( getTestID() );
        
        IConsensusCalculator consCalc=consensusTest.getConsensusCalculator();
        
        TestRun consrun=new TestRun();
        consrun.setTest( consensusTest );
        //Add a single result to consrun
        SimpleResult consres = new SimpleResult(getPropertyName(), 
                         consCalc.calculate( classifications ) );
        consres.setTestRun( consrun );
        consrun.addResult( consres);
        logger.debug(" $$ Consensus classification: " + 
                              consCalc.calculate( classifications ));
        
        return consrun;
        } catch ( BioclipseException e ) {
            LogUtils.handleException( e, logger, Activator.PLUGIN_ID);
            return null;
        }
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
    
    protected Collection<IPropertyCalculator<TestRun>> getCalculators() {
        
        List<IPropertyCalculator<TestRun>> calculators=new 
                                      ArrayList<IPropertyCalculator<TestRun>>();

        //Read EP for all calculators and select the ones in DS
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] elements = registry.getConfigurationElementsFor(
                                       "net.bioclipse.cdk.propertyCalculator" );

        for(IConfigurationElement element:elements) {

            try {

                String propid = element.getAttribute( "id" );
                
                IDSManager ds = Activator.getDefault().getJavaManager();
                Endpoint ep = ds.getEndpoint( getEndpoint() );
                for (IDSTest test : ep.getTests()){
                    if (propid.equals( test.getPropertycalculator())){

                        @SuppressWarnings("unchecked")
                        IPropertyCalculator<TestRun> calculator = (IPropertyCalculator<TestRun>)
                        element.createExecutableExtension( "class" );

                        calculators.add( calculator );
                        logger.debug("Added calculator: " + calculator 
                                     + " to endpoint: " + getEndpoint());

                    }
                }

            } catch ( CoreException e ) {
                LogUtils.handleException( e, logger, Activator.PLUGIN_ID);
            } catch ( BioclipseException e ) {
                LogUtils.handleException( e, logger, Activator.PLUGIN_ID);
            }
        }
        
        return calculators;
    }

    
}
