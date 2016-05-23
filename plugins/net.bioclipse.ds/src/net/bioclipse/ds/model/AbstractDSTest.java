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

import static java.lang.String.format;
import static net.bioclipse.ds.Activator.PLUGIN_ID;
import static org.eclipse.jface.resource.ImageDescriptor.createFromURL;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.FileUtil;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.Stopwatch;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;
import net.bioclipse.ds.model.result.SimpleResult;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

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
    private Endpoint endpoint;
    private long executionTimeMilliSeconds;
    private String iconpath;
    private Image excludedIcon;
    private IConsensusCalculator consensusCalculator;
    private String propertycalculator;
    public boolean override;

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
    
    public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
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

    public String getFileFromParameter(String parameter) throws DSException {
        try {
            String localPath = getParameters().get( parameter );
            if (localPath.isEmpty())
                throw new DSException("Error initializing file parameter " + parameter + " for model "
                		+ getName() + " due to empty parameter ");

            String path = localPath = FileUtil.getFilePath(localPath, getPluginID());
            return path;

        } catch (Exception e) {
        	LogUtils.debugTrace(logger, e);
            throw new DSException("Could not get file from parameter: " + parameter);
    	} 
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
        ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
        //Create the icon if not already done so
        if (testErrorMessage!=null && testErrorMessage.length()>1){
            if (errorIcon==null)
                initIcon( PLUGIN_ID, ERROR_ICON );
            errorIcon = imageRegistry.get( PLUGIN_ID + ERROR_ICON );
            return errorIcon;
                       
        }
        else if (isExcluded()){
            if (excludedIcon==null)
                initIcon( PLUGIN_ID, EXCLUDED_ICON );
            errorIcon = imageRegistry.get( PLUGIN_ID + EXCLUDED_ICON );
            return excludedIcon;
                       
        }
        else if (iconpath==null) {
            initIcon( PLUGIN_ID, EXCLUDED_ICON );
            icon = imageRegistry.get( PLUGIN_ID + DEFAULT_TEST_ICON );
        }else if (icon==null && pluginID!=null && iconpath!=null){
            if ( icon == null )
                initIcon( pluginID, iconpath );
            icon = imageRegistry.get( pluginID + iconpath );
        }        
        return icon;
    }
    
    private void initIcon( String id, String path ) {

        ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
        if ( imageRegistry.get( id + path ) != null )
            return;

        final String platformPath = "platform:plugin/%s/%s";
        try {
            URL imageUrl = new URL( format( platformPath, id, path ) );
            imageRegistry.put( id + path, createFromURL( imageUrl ) );
        } catch ( MalformedURLException e ) {
            logger.warn( format( "Faild to read icon %", path ) );
        }
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
        return SilentChemObjectBuilder.getInstance().
        newInstance(IAtomContainer.class);
    }
    
    public java.awt.Color getHighlightingColor( IAtom atom ) {
        return java.awt.Color.YELLOW;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter ) {
        if (adapter.isAssignableFrom(IPropertySource.class)) {
            return new ModelPropertySource(this);
        }
        return Platform.getAdapterManager().getAdapter( this, adapter );
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

    	//Do not initialize if we have an error message
        if (getTestErrorMessage().length()>1){
            logger.error("Trying to initialize test: " + getName() + " while " +
                "error message exists");
            return returnError( "Error message already exists for test: " 
            		+ getName() + ". Should not attempt to run test.","");
        }
        
        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        //Start stopwatch
        Stopwatch watch=new Stopwatch();
        watch.start();
        
        //Initialize if not already initialized
        try {
            if (!isInitialized()){
            	
            	//Assert required parameters for this test
            	assertRequiredParameters();
 
            	//Delegate initialization to test
                initialize(monitor);

                //If no exception, assume correctly initialized
                setInitialized(true);
            }
        } catch ( Exception e1 ) {
            logger.error( "Failed to initialize test: " + getId() + " due to: " 
                          + e1.getMessage() );
            setTestErrorMessage( "Initialization error: " + e1.getMessage() );
        }

        //Exit if error messages have occured.
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

        //Preprocess the molecule: Remove explicit and add implicit hydrogens
        
        try {
        	cdkmol = new CDKMolecule(standardizeMolecule(cdkmol.getAtomContainer()));
        } catch (CDKException e) {
        	return returnError("Error standardizing molecule", e.getMessage());
        }
        
        /*
		try {
				cdk.removeExplicitHydrogens(cdkmol);
				cdk.addImplicitHydrogens(cdkmol); 
			} catch (BioclipseException e) {
				logger.error(e.getMessage());
	            return returnError( "Error: " + e.getMessage(),e.getMessage());
			} catch (InvocationTargetException e) {
				logger.error(e.getTargetException().getMessage());
	            return returnError( "Error: " + e.getTargetException()
	            		.getMessage(),e.getTargetException().getMessage());
			} catch (RuntimeException e) {
				if (e.getCause() instanceof InvocationTargetException) {
					InvocationTargetException ie = 
						(InvocationTargetException) e.getCause();
					logger.error(ie.getTargetException().getMessage());
		            return returnError( "Error: " + ie.getTargetException()
		            		.getMessage(),ie.getTargetException().getMessage());
				}
			}
			*/

		//Delegate the actual test to the implementation
        List<? extends ITestResult> ret = doRunTest( cdkmol, monitor );
        
        //Store timing of test
        watch.stop();
        executionTimeMilliSeconds=watch.elapsedTimeMillis();

        monitor.done();

        return ret;

    }

    
	public static IAtomContainer standardizeMolecule(IAtomContainer mol) throws CDKException{

		//Remove explicit hydrogens
		for (int i=mol.getAtomCount()-1; i>=0; i--) {
			IAtom atom = mol.getAtom(i);
			if ("H".equals(atom.getSymbol())) {
				mol.removeAtomAndConnectedElectronContainers(atom);
			}
		}

		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
        Aromaticity aromaticity = new Aromaticity( ElectronDonation.cdk(), Cycles.cdkAromaticSet() );
        aromaticity.apply( mol );

		CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(mol.getBuilder());
		hAdder.addImplicitHydrogens(mol);

		return mol;

	}
	


    /**
     * Make sure that we have the required parameters for the test
     * defined in plugin.xml. Subclasses should override getRequiredParameters()
     * to state the required parameters.
     * @throws DSException 
     */
    private void assertRequiredParameters() throws DSException {
    	if (getRequiredParameters()!=null && getRequiredParameters().size()>0){
    		for (String reqParam : getRequiredParameters()){
    			String param=getParameters().get( reqParam );
    			if (param==null)
    				throw new DSException("Test '" + getName() + "' is missing " +
    						"required parameter: '" + reqParam + "'"); 
    		}
    	}
		
	}
    
    /**
     * The required SDF properties in the file. 
     * Default is empty, Subclasses may override.
     * @return
     */
    public abstract List<String> getRequiredParameters();


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
    
    @Override
    public void initialize(IProgressMonitor monitor) throws DSException {
    	
    	assertRequiredParameters();
    	//The rest should be done by extensions
    	
    }

    
}
