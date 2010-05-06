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
package net.bioclipse.ds.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.Stopwatch;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;
import net.bioclipse.ds.model.result.SimpleResult;
import net.bioclipse.ds.report.AbstractTestReportModel;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.IHelpResource;
import org.eclipse.swt.graphics.Image;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

/**
 * An abstract base class for all test implementations.
 * @author ola
 *
 */
public abstract class AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(AbstractDSTest.class);

    /**
     * This icon is used if icon is not specified by the test
     */
    private static final String DEFAULT_TEST_ICON = "icons/test_case_deac.gif";
    private static final String EXCLUDED_ICON = "icons/exclude.png";
    private static final String ERROR_ICON = "/icons/fatalerror.gif";

    private String id;
    private String name;
    private String description;
    private Image icon;
    private String pluginID;
    private Map<String, String > parameters;
    private boolean excluded;
    private boolean informative;
    private boolean initialized;
    private boolean visible;
    private AbstractTestReportModel reportmodel;
    private Endpoint endpoint;
    private long executionTimeMilliSeconds;
    private String iconpath;
    private Image excludedIcon;
    private IConsensusCalculator consensusCalculator;
    private String propertycalculator;

    private String helppage;

    
    /**
     * Empty if no problems
     */
    private String testErrorMessage;

    private Image errorIcon;

    
    public AbstractDSTest(){
        parameters=new HashMap<String, String>();
        excluded=false;
        testErrorMessage="";
    }
    
    public boolean isExcluded() {
        return excluded;
    }
    public void setExcluded( boolean excluded ) {
        this.excluded = excluded;
    }


    public Map<String, String> getParameters() {
        return parameters;
    }
    public void setParameters( Map<String, String> parameters ) {
        this.parameters = parameters;
    }
    
    public void addParameter( String name, String value ) {
        parameters.put( name, value );
    }

    public String getHelppage() {
        return helppage;
    }
    public void setHelppage( String helppage ) {
        this.helppage = helppage;
    }

    public String getId() {
    
        return id;
    }
    
    public void setId( String id ) {
    
        this.id = id;
    }
    
    public String getName() {
    
        return name;
    }
    
    public void setName( String name ) {
    
        this.name = name;
    }
    
    public void setIcon( String iconpath ) {
        this.iconpath=iconpath;
    }

    public Image getIcon() {
        //Create the icon if not already done so
        if (testErrorMessage!=null && testErrorMessage.length()>1){
            if (errorIcon==null)
                errorIcon=Activator.imageDescriptorFromPlugin( 
                                           net.bioclipse.ds.Activator.PLUGIN_ID, 
                                           ERROR_ICON ).createImage();
            return errorIcon;
                       
        }
        else if (isExcluded()){
            if (excludedIcon==null)
                excludedIcon=Activator.imageDescriptorFromPlugin( 
                                           net.bioclipse.ds.Activator.PLUGIN_ID, 
                                           EXCLUDED_ICON ).createImage();
            return excludedIcon;
                       
        }
        else if (iconpath==null)
            icon=Activator.imageDescriptorFromPlugin( 
                                    net.bioclipse.ds.Activator.PLUGIN_ID, 
                                    DEFAULT_TEST_ICON ).createImage();
        else if (icon==null && pluginID!=null && iconpath!=null)
            icon=Activator.imageDescriptorFromPlugin( 
                      pluginID, iconpath ).createImage();
        
        return icon;
    }
    
    
    public String getPluginID() {
    
        return pluginID;
    }

    
    public void setPluginID( String pluginID ) {
    
        this.pluginID = pluginID;
    }

    /**
     * Default toString is the name of the test
     */
    @Override
    public String toString() {
        return getName();
    }

    
    protected List<ITestResult> returnError(String name, String errorMessage) {

        ITestResult er=new SimpleResult(name, ITestResult.ERROR);
        er.setDetailedMessage( errorMessage );
        List<ITestResult> trlist=new ArrayList<ITestResult>();
        trlist.add( er );
        return trlist;
    }

    
    public IAtomContainer getAtomContainer() {
        return NoNotificationChemObjectBuilder.getInstance().
        newAtomContainer();
    }
    
    public java.awt.Color getHighlightingColor( IAtom atom ) {
        return java.awt.Color.YELLOW;
    }
    
    public Object getAdapter( Class adapter ) {
        return null;
    }

    
    public String getTestErrorMessage() {
    
        return testErrorMessage;
    }

    
    public void setTestErrorMessage( String testErrorMessage ) {
    
        this.testErrorMessage = testErrorMessage;
    }

    
    public String getDescription() {
    
        return description;
    }

    
    public void setDescription( String description ) {
    
        this.description = description;
    }

    
    public boolean isInformative() {
    
        return informative;
    }

    
    public void setInformative( boolean informative ) {
    
        this.informative = informative;
    }

    
    public AbstractTestReportModel getReportmodel() {
    
        return reportmodel;
    }

    
    public void setReportmodel( AbstractTestReportModel reportmodel ) {
    
        this.reportmodel = reportmodel;
    }

    public boolean isInitialized() {
        
        return initialized;
    }

    
    public void setInitialized( boolean initialized ) {
    
        this.initialized = initialized;
    }

    

    /**
     * Set up input molecule, and call doRunTest(). Also time execution.
     */
    public List<? extends ITestResult> runWarningTest( IMolecule molecule, IProgressMonitor monitor ){

        //Start stopwatch
        Stopwatch watch=new Stopwatch();
        watch.start();
        
        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        //Read database file if not already done that
        try {
            if (!isInitialized())
                initialize(monitor);
        } catch ( Exception e1 ) {
            logger.error( "Failed to initialize test: " + getId() + " due to: " 
                          + e1.getMessage() );
            setTestErrorMessage( "Failed to initialize: " + e1.getMessage() );
        }

        if (getTestErrorMessage().length()>1){
            return new ArrayList<ExternalMoleculeMatch>();
        }

        //Create CDKMolecule from the IMolecule to get a clean API
        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();
        ICDKMolecule cdkmol=null;
        try {
            cdkmol = cdk.asCDKMolecule( molecule );
        } catch ( BioclipseException e ) {
            return returnError( "Could not create CDKMolecule", e.getMessage() );
        }

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        List<? extends ITestResult> ret = doRunTest( cdkmol, monitor );
        
        //Store timing of test
        watch.stop();
        executionTimeMilliSeconds=watch.elapsedTimeMillis();

        return ret;

    }



    protected abstract List<? extends ITestResult> doRunTest( 
                                                  ICDKMolecule cdkmol, 
                                                  IProgressMonitor monitor);

    public void setVisible( boolean visible ) {

        this.visible = visible;
    }

    public boolean isVisible() {

        return visible;
    }

    public void setEndpoint( Endpoint endpoint ) {

        this.endpoint = endpoint;
    }

    public Endpoint getEndpoint() {

        return endpoint;
    }

    public long getExecutionTimeMilliSeconds() {
        return executionTimeMilliSeconds;
    }

    public void setConsensusCalculator( IConsensusCalculator consensusCalculator ) {

        this.consensusCalculator = consensusCalculator;
    }

    public IConsensusCalculator getConsensusCalculator() {

        return consensusCalculator;
    }

    public void setPropertycalculator( String propertycalculator ) {

        this.propertycalculator = propertycalculator;
    }

    public String getPropertycalculator() {

        return propertycalculator;
    }

    /*
     * BELOW is for CONTEXT
     */
    
    public String getText() {
        return "WEE text";
    }
    
    public String getStyledText() {
        return "Wee <@#$b> weeestyled </@#$b> whoow";
    }
    
    
    public IHelpResource[] getRelatedTopics() {
        
        IHelpResource res=new IHelpResource(){

            public String getHref() {
                return getHelppage();
            }
            ///help/topic/net.bioclipse.qsar.ui/html/descriptors.html
            public String getLabel() {
              return getName();
            }
          };
        return new IHelpResource[]{res};
    }
    

    public String getCategory( IHelpResource topic ) {
        //TODO: implement
        return null;
    }
    
    public String getTitle() {
        return "mamma mia title";
    }
    
}
