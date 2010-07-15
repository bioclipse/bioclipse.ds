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
package net.bioclipse.ds.matcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import net.bioclipse.cdk.ui.sdfeditor.business.IMoleculeTableManager;
import net.bioclipse.cdk.ui.sdfeditor.business.SDFIndexEditorModel;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.FileUtil;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;


/**
 * A base class that handles reading of SDF into model. All tests using SDF as 
 * backend should extend this class.
 * 
 * @author ola
 *
 */
public abstract class BaseSDFMatcher extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(BaseSDFMatcher.class);

    private static final String RESPONSE_PROPERTY_PARAM="responseProperty";
    private static final String FILE_PROPERTY_PARAM="file";

    private static final boolean VALIDATE_SDF_PROPERTIES_ON_LOAD = false;

    /**
     * The response property read from params in manifest
     */
    private String responseProperty;
    
    /**
     * The model holding the molecules and properties
     */
    private SDFIndexEditorModel SDFmodel;


    /**
     * Enables subclasses to provide validation of response property values 
     * upon initialization. Default does nothing, subclasses may override.
     * @param obj
     * @throws DSException 
     */
    void validateResponseValue( Object obj ) throws DSException{
    }


    /**
     * The required SDF properties in the file. 
     * Default is empty, Subclasses may override.
     * @return
     */
    List<String> getRequiredProperties() {
        return new ArrayList<String>();
    }
    
    @Override
    public List<String> getRequiredParameters() {
    	return new ArrayList<String>(){{
    	    add(RESPONSE_PROPERTY_PARAM);
    	    add(FILE_PROPERTY_PARAM);
    	}};
    }
    
    /**
     * Accessor for the parsed SDFIndexModel
     * @return
     */
    public SDFIndexEditorModel getSDFmodel() {
        return SDFmodel;
    }

    /**
     * Return the response property
     * @return
     */
    public String getResponseProperty() {
        return responseProperty;
    }



    /**
     * Verify parameters, read/parse SDF file into model, and verify properties
     * @param monitor 
     * @throws DSException
     */
    public void initialize(IProgressMonitor monitor) throws DSException {

        if (monitor.isCanceled())
            throw new DSException("Initialization of test " + 
                                                        getId() + " cancelled");

        //Read parameters as defined in extension
        //We have already asserted that they exist
        String filepath=getParameters().get( FILE_PROPERTY_PARAM );
        responseProperty=getParameters().get( RESPONSE_PROPERTY_PARAM );

        //=================
        //Read SDFile
        //=================

        //Read the SDFile. First convert the local path to absolute
        String path="";
        URL url2;
        try {
            Bundle bundle = Platform.getBundle(getPluginID());
            logger.debug("BASESDF Bundle: " + bundle.getBundleId());
            URL url=bundle.getEntry(filepath);
            logger.debug("BASESDF URL: " + url);
            url2 = FileLocator.toFileURL(url);
            path=url2.getFile();
        } catch ( IOException e ) {
            throw new DSException(e.getMessage());
        }

        //Check that absolute file is not empty
        if ("".equals( path )){
            throw new DSException("File: " + filepath + " could not be read by " +
            		"test: " + getId());
        }

        if (monitor.isCanceled())
            throw new DSException("Initialization of test " + 
                                                        getId() + " cancelled");

        logger.debug("Test " + getId() + " reading and parsing file: " 
                     + path );

        //Use moltablemanager for parsing the SDFile
        IMoleculeTableManager moltable = net.bioclipse.cdk.ui.sdfeditor.
                               Activator.getDefault().getMoleculeTableManager();
        
        IFile file=null;
        try {
            file = FileUtil.createLinkedFile(path );
        } catch ( CoreException e2 ) {
            e2.printStackTrace();
            throw new DSException("Could not write linked file: " + path);
        }

        //Read index and parse properties
        //Start by copying file to Virtual since we require IFile
        //Use a random name since might be duplicated uses of this class
//        Random generator=new Random(System.currentTimeMillis());
//        String virtualfile= "sdfile" + (generator.nextInt(9000) + 1000) +".sdf";
//        IFile file = net.bioclipse.core.Activator.getVirtualProject()
//                                              .getFile( virtualfile );
        if(!file.exists()) {
            try {
                InputStream is = url2.openStream();
                file.create( is
                             , true, null );
            } catch ( CoreException e1 ) {
                LogUtils.debugTrace( logger, e1 );
                throw new DSException("Error creating virtual file for test: " 
                                      + getId());
            } catch ( IOException e ) {
                LogUtils.debugTrace( logger, e );
                throw new DSException("Error creating virtual file for test: " 
                                      + getId());
            }
        }
//        logger.debug("Test " + getId() + " wrote virtual file: " + virtualfile);
        
        if (monitor.isCanceled())
            throw new DSException("Initialization of test " + 
                                                        getId() + " cancelled");


        //Create the SDF index and wait until ready
        BioclipseJob<SDFIndexEditorModel> job1 = 
            moltable.createSDFIndex( file, 
                        new BioclipseJobUpdateHook<SDFIndexEditorModel>(
                                     "Initializing data for test: " + getId()) {
                @Override
                public void completeReturn( SDFIndexEditorModel object ) {
                
                 SDFmodel = object;
                }
            } );

        try {
            job1.join();
        } catch ( InterruptedException e ) {
            throw new DSException("Initialization of DB exact match FP cancelled");
        }
        
        if (SDFmodel.getNumberOfMolecules()<=0){
            throw new DSException("No molecules could be read in database");
        }
        logger.debug("Test " + getId() + " created SDFindex successfully");

        if (monitor.isCanceled())
            throw new DSException("Initialization of test " + 
                                                        getId() + " cancelled");
        
        //=================
        //Parse SDFile
        //=================

        //We need to define that we want to read extra properties as well
        List<String> extraProps=new ArrayList<String>();
        //First add the response property, since this is always needed
        extraProps.add( responseProperty );
        //Add other implementation specific properties
        getRequiredProperties();

        //Start job to parse the SDFmodel
        BioclipseJob<Void> job = moltable.
                                   parseProperties( SDFmodel,
                                   extraProps,
                                   new BioclipseJobUpdateHook<Void>(
                                            "Parsing data for test " + getId()));

        //Wait for parsing job to finish
        try {
            job.join();
        } catch ( InterruptedException e ){
            throw new DSException("SDF parsing  in test " + getId() 
                                                            + " was cancelled");
        }
        
        logger.debug("Parsed SDF index with properties successfully. No mols: " + 
                     SDFmodel.getNumberOfMolecules());

        if (monitor.isCanceled())
            throw new DSException("Initialization of test " + 
                                                        getId() + " cancelled");
        
        //========================
        //Verify SDFile properties (if set)
        //========================
        if (VALIDATE_SDF_PROPERTIES_ON_LOAD){

            //For all molecules...
            for (int i=0; i<SDFmodel.getNumberOfMolecules(); i++){

                //Verify response property exists
                Object obj = SDFmodel.getPropertyFor( i, responseProperty );
                if (obj==null)
                    throw new DSException("Molecule index " + i 
                                          + " does not have response property: " 
                                          + responseProperty 
                                          + " in test " + getId());

                //Allow for subclasses to validate responses upon initialization
                validateResponseValue(obj);

                //Verify we have all other required properties != null
                for (String prop : getRequiredProperties()){
                    Object propvalue = SDFmodel.getPropertyFor( i, prop );
                    if (propvalue==null)
                        throw new DSException("Molecule index " + i 
                                              + " does not have required property " 
                                              + prop + " in test " + getId());
                }
            }
        }
        
        logger.debug("Test " + getId() + " base initialization completed " +
        		"                                                    successfully");
    }
    
}
