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
package net.bioclipse.ds.business;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IConsensusCalculator;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestDiscovery;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * 
 * @author ola
 *
 */
public class DSBusinessModel {

    private static final Logger logger = Logger.getLogger(DSBusinessModel.class);

    private static final String DEFAULT_CONSENSUS_CALCULATOR = 
                        "net.bioclipse.ds.consensus.majority.emptyinconclusive";

    volatile List<IDSTest> tests;
    volatile List<Endpoint> endpoints;

    public List<IDSTest> getTests() {
        return tests;
    }
    
    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void initialize() {
        readEndpointsFromEP();
        readTestsFromEP();
        addTestsFromDiscovery();
    }



	public void readEndpointsFromEP(){

        endpoints = new ArrayList<Endpoint>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();

        if ( registry == null ) 
            throw new UnsupportedOperationException("Extension registry is null. " +
            "Cannot read tests from EP.");
        // it likely means that the Eclipse workbench has not
        // started, for example when running tests

        IExtensionPoint serviceObjectExtensionPoint = registry
        .getExtensionPoint("net.bioclipse.decisionsupport");

        IExtension[] serviceObjectExtensions
        = serviceObjectExtensionPoint.getExtensions();

        for(IExtension extension : serviceObjectExtensions) {
            for( IConfigurationElement element
                    : extension.getConfigurationElements() ) {

                //Read all endpoints
                if (element.getName().equals("endpoint")){

                    String pid=element.getAttribute("id");
                    String pname=element.getAttribute("name");
                    String pdesc=element.getAttribute("description");
                    String picon=element.getAttribute("icon");
                    String pluginID=element.getNamespaceIdentifier();

                    Endpoint ep=new Endpoint(pid, pname, pdesc, picon, pluginID);
                    endpoints.add( ep );
                    
                    //Add dedicated consensus calculator, or use default
                    String pconsid = element.getAttribute("consensus");
                    if (pconsid==null) pconsid=DEFAULT_CONSENSUS_CALCULATOR;
                    IConsensusCalculator conscalc = createNewConsCalc(pconsid);
                    ep.setConsensusCalculator( conscalc );

                    
                }
            }
        }
    }

    public void readTestsFromEP(){

    	if (tests==null)
    		tests = new ArrayList<IDSTest>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();

        if ( registry == null ) 
            throw new UnsupportedOperationException("Extension registry is null. " +
            "Cannot read tests from EP.");
        // it likely means that the Eclipse workbench has not
        // started, for example when running tests

        IExtensionPoint serviceObjectExtensionPoint = registry
        .getExtensionPoint("net.bioclipse.decisionsupport");

        IExtension[] serviceObjectExtensions
        = serviceObjectExtensionPoint.getExtensions();

        for(IExtension extension : serviceObjectExtensions) {
            for( IConfigurationElement element
                    : extension.getConfigurationElements() ) {

                //Read all tests from EP
                //======================
                if (element.getName().equals("test")){

                    String pname=element.getAttribute("name");

                    IDSTest test=null;
                    Object obj;
                    try {
                        obj = element.createExecutableExtension("class");
                        if (obj instanceof IDSTest){

                            test=(IDSTest)obj;

                            test.setName(pname);
                            String pid=element.getAttribute("id");
                            test.setId(pid);
                            String picon=element.getAttribute("icon");
                            test.setIcon(picon);
                            String ppropcalc=element.getAttribute("propertycalculator");
                            test.setPropertycalculator( ppropcalc);

                            String phelppage=element.getAttribute("helppage");
                            test.setHelppage( phelppage);

                            String pdescription=element.getAttribute("description");
                            test.setDescription( pdescription);

                            String pep=element.getAttribute("endpoint");
                            //Look up endpoint by id and add to test
                            for (Endpoint ep : endpoints){
                                if (ep.getId().equals( pep )){
                                    test.setEndpoint( ep );
                                    ep.addTest(test);
                                }
                            }

                            String pinformative=element.getAttribute("informative");
                            if (pinformative!=null){
                                if (pinformative.equalsIgnoreCase( "true" ))
                                    test.setInformative(true);
                                else
                                    test.setInformative(false);
                            }
                            else
                                test.setInformative(false);
                            
                            String pOver=element.getAttribute("override");
                            if (pOver!=null){
                                if (pOver.equalsIgnoreCase( "true" ))
                                    test.setOverride(true);
                                else
                                	test.setOverride(false);
                            }
                            else
                            	test.setOverride(false);


                            String pvisible=element.getAttribute("visible");
                            if (pvisible!=null){
                                if (pvisible.equalsIgnoreCase( "false" ))
                                    test.setVisible( false );
                                else
                                    test.setVisible( true );
                            }
                            else
                                test.setVisible( true );

                            String pluginID=element.getNamespaceIdentifier();
                            test.setPluginID( pluginID );
                            
                            //Add dedicated consensus calculator, or use default
                            String pconsid = element.getAttribute("consensus");
                            if (pconsid==null) pconsid=DEFAULT_CONSENSUS_CALCULATOR;
                            IConsensusCalculator conscalc = createNewConsCalc(pconsid);
                            test.setConsensusCalculator( conscalc );

                            //Process children
                            for( IConfigurationElement subelement
                                    : element.getChildren() ) {
                                if ("resource".equals( subelement.getName() )){
                                    String name=subelement.getAttribute( "name" );
                                    String path=subelement.getAttribute( "path" );
                                    test.addParameter(name,path);
                                }
                                else if ("parameter".equals( subelement.getName() )){
                                    String name=subelement.getAttribute( "name" );
                                    String path=subelement.getAttribute( "value" );
                                    test.addParameter(name,path);
                                }
                            }
                            tests.add( test );

                            logger.debug("Added Decision support Test from EP: "
                                         + element.getAttribute("name") + 
                                         " to " + pname);
                        }else{
                            logger.error("Test class " + pname 
                                         + " must implement IDSTest ");
                        }
                    } catch ( CoreException e ) {
                        logger.error("Error creating class for: " + pname + 
                                     ": " + e.getLocalizedMessage() );
                        LogUtils.handleException( e, logger, Activator.PLUGIN_ID);
                    }
                }
            }
        }
    }
    
    /**
     * Delegate test discovery to plugins via extension 'testdiscovery'
     */
    private void addTestsFromDiscovery() {
    	
    	if (tests==null)
    		tests = new ArrayList<IDSTest>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();

        if ( registry == null ) 
            throw new UnsupportedOperationException("Extension registry is null. " +
            "Cannot read tests from EP.");
        // it likely means that the Eclipse workbench has not
        // started, for example when running tests

        IExtensionPoint serviceObjectExtensionPoint = registry
        .getExtensionPoint("net.bioclipse.decisionsupport");

        IExtension[] serviceObjectExtensions
        = serviceObjectExtensionPoint.getExtensions();

        for(IExtension extension : serviceObjectExtensions) {
            for( IConfigurationElement element
                    : extension.getConfigurationElements() ) {

                //Read all testdiscovery classes from EP
                //======================
                if (element.getName().equals("testdiscovery")){

//                    String pid=element.getAttribute("id");
//                    String pep=element.getAttribute("endpoint");

                    try {
						ITestDiscovery discovery = (ITestDiscovery) 
									 element.createExecutableExtension("class");
						
						List<IDSTest> newTests = discovery.discoverTests();
						if (newTests!=null){
							logger.debug("Discovered " + newTests.size() 
									+ " new tests");
						}else{
							logger.debug("Discovery returned null ");
						}
						
						tests.addAll(newTests);
						
						
					} catch (CoreException e) {
						LogUtils.debugTrace(logger, e);
					} catch (BioclipseException e) {
						LogUtils.debugTrace(logger, e);
					}
                        
                }
            }
        }
        
    			
	}


    public static IConsensusCalculator createNewConsCalc( String pconsid ) {

    	//If null provided, use default
    	if (pconsid==null){
    		logger.debug("using default consensus calculator");
    		pconsid=DEFAULT_CONSENSUS_CALCULATOR;
    	}

        IConsensusCalculator conscalc=null;
        
        IExtensionRegistry registry = Platform.getExtensionRegistry();

        if ( registry == null ) 
            throw new UnsupportedOperationException("Extension registry is " +
            		"null. Cannot read tests from EP.");
        // it likely means that the Eclipse workbench has not
        // started, for example when running tests

        IExtensionPoint serviceObjectExtensionPoint = registry
        .getExtensionPoint("net.bioclipse.decisionsupport");

        IExtension[] serviceObjectExtensions
        = serviceObjectExtensionPoint.getExtensions();

        for(IExtension extension : serviceObjectExtensions) {
            for( IConfigurationElement element
                    : extension.getConfigurationElements() ) {

                //Read all tests from EP
                //======================
                if (element.getName().equals("consensus")){

                    String pid=element.getAttribute("id");
                    
                    if (pid.equals( pconsid )){
                        
                        //This is the one we should instantiate
                        
                        String pname=element.getAttribute("name");
                        String pdesc=element.getAttribute("description");
                        
                        try {
                            conscalc = (IConsensusCalculator) element
                                            .createExecutableExtension("class");
                            conscalc.setId( pid);
                            conscalc.setName( pname );
                            conscalc.setDescription( pdesc );
                        } catch ( CoreException e ) {
                            logger.error("Consensus calculator " + pid 
                                  + " failed to initialize: " + e.getMessage());
                            LogUtils.handleException( e, logger, 
                                                           Activator.PLUGIN_ID);
                        }

                    }
                }
            }
        }
        return conscalc;
    }


}
