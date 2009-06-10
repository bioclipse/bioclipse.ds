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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
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
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;


/**
 * A test that looks up nearest neighbours a database (SDF) with CDK 
 * fingerprints and Tanimoto distance.
 * 
 * @author ola
 *
 */
public class DBNearestNeighborTest extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(DBNearestNeighborTest.class);
    private static final String FP_PROPERTY_KEY="net.bioclipse.cdk.fingerprint";
    private static final String CONSLUSION_PROPERTY_KEY="Ames test categorisation";

    //Instance variables, set up by initialize()
    private SDFIndexEditorModel moleculesmodel;
    private float tanimoto;
    

    /**
     * Read database file into memory
     * @param monitor 
     * @throws IOException 
     * @throws WarningSystemException 
     */
    public void initialize(IProgressMonitor monitor) throws DSException {

        if (getTestErrorMessage().length()>1){
            logger.error("Trying to initialize test: " + getName() + " while " +
                "error message exists");
            return;
        }

        String filepath=getParameters().get( "file" );
        logger.debug("File parameter is: "+ filepath);

        String tanimotoString=getParameters().get( "distance.tanimoto" );
        logger.debug("NearestTest tanimoto parameter is : "+ tanimotoString);

        //Assert parameters are present
        if (filepath==null)
            throw new DSException("No file provided for DBNearestNeighbourTest: " 
                                  + getId());
        if (tanimotoString==null)
            throw new DSException("No tanimoto distance provided for " +
            		"DBNearestNeighbourTest: " + getId());

        //Parse tanimoto string into a Float
        tanimoto=Float.parseFloat( tanimotoString );

        //Read the SDFile
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

        logger.debug( "File path: " + path );

        IMoleculeTableManager moltable = net.bioclipse.cdk.ui.sdfeditor.
        Activator.getDefault()
        .getMoleculeTableManager();

        //Read index and parse properties
        IFile file = net.bioclipse.core.Activator.getVirtualProject()
        .getFile( "dbLookupNN.sdf" );
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
            throw new DSException("Initialization of DBNN cancelled");
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
            throw new DSException("Initialization of DBNN cancelled");
        }

        logger.debug("Loaded SDF index with propertisuccessfully. No mols: " + 
                     moleculesmodel.getNumberOfMolecules());

        //Verify we have inchi for all
        for (int i=0; i<moleculesmodel.getNumberOfMolecules(); i++){
            BitSet fp = moleculesmodel.getPropertyFor( i, FP_PROPERTY_KEY );
            if (fp==null)
                throw new DSException("Not all molecules in DB have Fingerprint" +
                		                  " calculated");
            
            String amesCategor = moleculesmodel.getPropertyFor(
                                                   i, CONSLUSION_PROPERTY_KEY );
            if (amesCategor==null)
                throw new DSException("Not all molecules in DB has AMES " +
                "test categorization property.");

        }

    }

    public List<? extends ITestResult> runWarningTest( IMolecule molecule, IProgressMonitor monitor ){

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        //Store results here
        ArrayList<ExternalMoleculeMatch> results=new ArrayList<ExternalMoleculeMatch>();

        //Read database file if not already done that
        try {
            if (moleculesmodel==null)
                initialize(monitor);
        } catch ( Exception e1 ) {
            logger.error( "Failed to initialize DBNNTest: " + e1.getMessage() );
            setTestErrorMessage( "Failed to initialize: " + e1.getMessage() );
        }

        if (getTestErrorMessage().length()>1){
            return results;
        }

        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();

        ICDKMolecule cdkmol=null;
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

        
        try {

            //Check for cancellation
            if (monitor.isCanceled())
                return returnError( "Cancelled","");

            //Start by searching for inchiKey
            //================================
            BitSet molFP = cdkmol.getFingerprint( IMolecule.Property.
                                                  USE_CALCULATED );
            logger.debug( "FP to search for: " + molFP);
            logger.debug( "Molecule to search for: " + cdkmol);

            DecimalFormat twoDForm = new DecimalFormat("#.##");
            
            //Search the index for this FP
            for (int i=0; i<moleculesmodel.getNumberOfMolecules(); i++){
                BitSet dbFP = moleculesmodel.getPropertyFor( i, FP_PROPERTY_KEY);
                //Null check not required since verified in initialize()

//                logger.debug( "Searching mol " + i + " with FP: " + dbFP );
//                logger.debug("  legth: DBFP=" + dbFP.length() + ", molFP=" + molFP.length());
//                logger.debug("  size: DBFP=" + dbFP.size() + ", molFP=" + molFP.size());
                
                if (dbFP.size()!=molFP.size()){
//                    logger.warn( "Index " + i + " in DB has FP size=" 
//                                 + dbFP.size() + 
//                                 " but molecule searched for has FP size=" 
//                                 + molFP.size());
                }else{

                    float calcTanimoto = cdk.calculateTanimoto( dbFP, molFP );
                    if (calcTanimoto>tanimoto){
                        //HIT
                        ICDKMolecule matchmol = moleculesmodel.getMoleculeAt( i );
                        String amesCat = moleculesmodel.getPropertyFor( i, CONSLUSION_PROPERTY_KEY);
                        String cdktitle=(String) matchmol.getAtomContainer().getProperty( CDKConstants.TITLE );
                        String molname="Index " + i;
                        if (cdktitle!=null)
                            molname=cdktitle;

                        molname=molname+ " [tanimoto=" + twoDForm.format( calcTanimoto ) +"]";
                        int concl=getConclusion(amesCat);
                        ExternalMoleculeMatch match = 
                            new ExternalMoleculeMatch(molname, matchmol, 
                                                      calcTanimoto,  concl);
                        results.add( match);
                    }
                }
                //TODO: check how much this check slows down, probably not much
                if (monitor.isCanceled())
                    return returnError( "Cancelled","");

            }
            
        } catch ( Exception e ) {
            LogUtils.debugTrace( logger, e );
            return returnError( "Test failed: " , e.getMessage());
        }

        //Sort results by tanimoto
        Collections.sort( results, new Comparator<ExternalMoleculeMatch>(){
            public int compare( ExternalMoleculeMatch o1,
                                ExternalMoleculeMatch o2 ) {
                return Float.compare( o2.getSimilarity() , o1.getSimilarity());
            }
        });
        
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
