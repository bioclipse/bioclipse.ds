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
package net.bioclipse.ds.dblookup.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IMoleculeTableManager;
import net.bioclipse.cdk.ui.sdfeditor.editor.SDFIndexEditorModel;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.inchi.InChI;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;


/**
 * A test that tests for an exakt match in an SDFile
 * @author ola
 *
 */
public class DBExactMatchTest extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(DBExactMatchTest.class);
    
    private static final String INCHI_PROPERTY_KEY="net.bioclipse.cdk.InChI";
    private static final String CONSLUSION_PROPERTY_KEY="Ames test categorisation";

    /**
     * This is the cached model of the entries in the SDFile with properties
     */
    private SDFIndexEditorModel moleculesmodel;
    

    /**
     * Read database file into memory
     * @param monitor 
     * @throws Exception 
     * @throws WarningSystemException 
     */
    public void initialize(IProgressMonitor monitor) throws DSException {
        
        if (getTestErrorMessage().length()>1){
            logger.error("Trying to initialize test: " + getName() + " while " +
            		"error message exists");
            return;
        }
        
            String filepath=getParameters().get( "file" );
            logger.debug("Filename is: "+ filepath);

            if (filepath==null)
                throw new DSException("No file provided for DBExactMatchTest: " + getId());


            String path="";
            URL url2;
            try {
                url2 = FileLocator.toFileURL(Platform.getBundle(getPluginID()).getEntry(filepath));
                path=url2.getFile();
            } catch ( IOException e ) {
                throw new DSException(e.getMessage());
            }

            //File could not be read
            if ("".equals( path )){
                throw new DSException("File: " + filepath + " could not be read.");
            }

            logger.debug( "file path: " + path );

            IMoleculeTableManager moltable = net.bioclipse.cdk.ui.sdfeditor.Activator.getDefault()
            .getMoleculeTableManager();

            //Read index and parse properties
            IFile file = net.bioclipse.core.Activator.getVirtualProject()
            .getFile( "dbLookup.sdf" );
            if(!file.exists()) {
            try {
                InputStream is = url2.openStream();
                    file.create( is
                                 , true, null );
                } catch ( CoreException e1 ) {
                    // TODO Auto-generated catch block
                    LogUtils.debugTrace( logger, e1 );
                } catch ( IOException e ) {
                    // TODO Auto-generated catch block
                    LogUtils.debugTrace( logger, e );
                }
            }
            BioclipseJob<SDFIndexEditorModel> job1 = 
                moltable.createSDFIndex( file, new BioclipseJobUpdateHook<SDFIndexEditorModel>("job") {
                    @Override
                    public void completeReturn( SDFIndexEditorModel object ) {
                    moleculesmodel = object;
                    }
                } );
            
            try {
                job1.join();
            } catch ( InterruptedException e ) {
                throw new DSException("Initialization of DBExactMatch cancelled");
            }
            
            if (moleculesmodel.getNumberOfMolecules()<=0){
                throw new DSException("No molecules could be read in database");
            }
            
            //We need to define that we want to read extra properties as well
            List<String> extraProps=new ArrayList<String>();
            extraProps.add( CONSLUSION_PROPERTY_KEY );

            BioclipseJob<Void> job = moltable.
                                       parseProperties( moleculesmodel, 
                                       extraProps, 
                                       new BioclipseJobUpdateHook<Void>(
                                                "Parsing SDFile"));

            //Wait for job to finish
            try {
                job.join();
            } catch ( InterruptedException e ) {
                throw new DSException("Initialization of DBExactMatch cancelled");
            }
            
            //Verify we have inchi for all
            for (int i=0; i<moleculesmodel.getNumberOfMolecules(); i++){
                InChI readInchi = moleculesmodel.getPropertyFor( i, INCHI_PROPERTY_KEY );
                
//                logger.debug("Mol " + i + " has inchi: " + readInchi);
                
                if (readInchi==null)
                    throw new DSException("Molecule " + i + " in " +
                    		                  "DB has no inchi property");

                String amesCategor = moleculesmodel.getPropertyFor(
                                                   i, CONSLUSION_PROPERTY_KEY );
                
//                logger.debug("Mol " + i + " has ames: " + amesCategor);

                if (amesCategor==null)
                    throw new DSException("Molecule " + i + " in DB has no AMES " +
                    		"test categorization property.");
            }

            logger.debug("Loaded SDF index with properties successfully. " +
            		         "No mols: " + moleculesmodel.getNumberOfMolecules());

    }

    /**
     * Run the actual warning test
     */
    public List<ITestResult> runWarningTest( IMolecule molecule, 
                                             IProgressMonitor monitor ){

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");
        
        //Store results here
        List<ITestResult> results=new ArrayList<ITestResult>();
                
        //Read database file if not already done that
        try {
            if (moleculesmodel==null)
                initialize(monitor);
        } catch ( Exception e1 ) {
            logger.error( "Failed to initialize DBExactMatchTest: " 
                          + e1.getMessage() );
            LogUtils.handleException( e1, 
                                 logger, net.bioclipse.ds.Activator.PLUGIN_ID );
            setTestErrorMessage( "Failed to initialize: " + e1.getMessage() );
        }

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

        //Start by searching for inchiKey
        //================================
        String molInchiKey;
        String molInchi;
        try {
            molInchiKey = cdkmol.getInChIKey( 
                                     IMolecule.Property.USE_CALCULATED  );
            molInchi = cdkmol.getInChI(
                                       IMolecule.Property.USE_CALCULATED  );
        } catch ( BioclipseException e ) {
//            LogUtils.debugTrace( logger, e );
            return returnError( "Error generating Inchi", e.getMessage() );
        }

        logger.debug( "Inchikey to search for: " + molInchiKey);

        //Search the index for this InchiKey
        for (int i=0; i<moleculesmodel.getNumberOfMolecules(); i++){
             InChI readInchi = moleculesmodel.getPropertyFor( i, INCHI_PROPERTY_KEY );
             //Null check not required since verified in initialize()

             if (molInchiKey.equals( readInchi.getKey() )){
                if (molInchi.equals( readInchi.getValue() )){
                    ICDKMolecule matchmol = moleculesmodel.getMoleculeAt( i );
                    String amesCat = moleculesmodel.getPropertyFor( i, CONSLUSION_PROPERTY_KEY);
                    String cdktitle=(String) matchmol.getAtomContainer().getProperty( CDKConstants.TITLE );
                    String molname="Index " + i;
                    if (cdktitle!=null)
                        molname=cdktitle;
                    int concl=getConclusion(amesCat);
                    ExternalMoleculeMatch match = 
                        new ExternalMoleculeMatch(molname, matchmol, concl);
                    results.add( match );
                }
            }

             //TODO: check how much this check slows down, probably not much
             if (monitor.isCanceled())
                 return returnError( "Cancelled","");

        }
        
        return results;
        
    }

    private int getConclusion( String amesCat ) {

        if (amesCat.equals( "mutagen" ))
            return ITestResult.POSITIVE;
        else if (amesCat.equals( "nonmutagen" ))
            return ITestResult.NEGATIVE;

        logger.error("Ames test could not parse categorization from SDFile. " +
        		"Result set to INCONCLUSIVE");
        return ITestResult.INCONCLUSIVE;
    }


}
