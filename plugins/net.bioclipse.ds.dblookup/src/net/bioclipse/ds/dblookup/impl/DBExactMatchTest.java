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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IMoleculeTableManager;
import net.bioclipse.cdk.ui.sdfeditor.business.SDFileIndex;
import net.bioclipse.cdk.ui.sdfeditor.editor.SDFIndexEditorModel;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.AbstractWarningTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.impl.DSException;


/**
 * A test that tests for an exakt match in an SDFile
 * @author ola
 *
 */
public class DBExactMatchTest extends AbstractWarningTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(DBExactMatchTest.class);

    /**
     * This is the cached model of the entries in the SDFile with properties
     */
    private SDFIndexEditorModel moleculesmodel;
    

    /**
     * Read database file into memory
     * @param monitor 
     * @throws WarningSystemException 
     */
    private void initialize(IProgressMonitor monitor) throws DSException {
        
        String filepath=getParameters().get( "file" );
        logger.debug("Filename is: "+ filepath);
        
        if (filepath==null)
            throw new DSException("No file provided for DBExactMatchTest: " + getId());

        
        String path="";
        try {
            URL url2 = FileLocator.toFileURL(Platform.getBundle(getPluginID()).getEntry(filepath));
            path=url2.getFile();
        } catch ( IOException e1 ) {
            e1.printStackTrace();
            return;
        }

        //File could not be read
        if ("".equals( path )){
            throw new DSException("File: " + filepath + " could not be read.");
        }

        logger.debug( "file path: " + path );
        
        IMoleculeTableManager moltable = net.bioclipse.cdk.ui.sdfeditor.Activator.getDefault()
        .getMoleculeTableManager();

        //Read index and parse properties
        SDFileIndex sdfIndex = moltable.createSDFIndex( path);
        moleculesmodel = new SDFIndexEditorModel(sdfIndex);
        moltable.parseProperties( moleculesmodel );

        logger.debug("Loaded SDF index successfully. No mols: " + 
                                         moleculesmodel.getNumberOfMolecules());
        
    }

    /**
     * Run the actual warning test
     */
    public List<ITestResult> runWarningTest( IMolecule molecule, IProgressMonitor monitor ){

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        //Store results here
        List<ITestResult> results=new ArrayList<ITestResult>();
        
//        if (true) return results;
        
        //Read database file if not already done that
        if (moleculesmodel==null)
            try {
                initialize(monitor);
            } catch ( DSException e1 ) {
                return returnError(e1.getMessage(), e1.getStackTrace().toString());
            }

            

        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();
        
        ICDKMolecule cdkmol=null;
        try {
            cdkmol = cdk.create( molecule );
        } catch ( BioclipseException e ) {
            return returnError( "Could not create CDKMolecule", e.getMessage() );
        }

        //Start by searching for inchiKey
        //================================
        String molInchiKey;
        try {
            molInchiKey = cdkmol.getInChIKey( 
                                     IMolecule.Property.USE_CACHED_OR_CALCULATED  );
        } catch ( BioclipseException e ) {
            return returnError( "Could not create InchiKey", e.getMessage() );
        }

        logger.debug( "Inchikey to search for: " + molInchiKey);


        List<Integer> inchikeyMatches=new ArrayList<Integer>();
        //Search the index for this InchiKey
        for (int i=0; i<moleculesmodel.getNumberOfMolecules(); i++){
            String readInchi = moleculesmodel.getPropertyFor( i, "inchi.key.property.name" );
            if (molInchiKey.equals( readInchi )){
                //InchiKey MATCH
                inchikeyMatches.add( i );
            }
        }
        
        //If no matches, fall through and return the empty list
        
        //If one match, return it
        if (inchikeyMatches.size()==1){
            ICDKMolecule matchmol = moleculesmodel.getMoleculeAt( inchikeyMatches.get( 0 ) );
            ExternalMoleculeMatch match = new ExternalMoleculeMatch(matchmol);
            results.add( match );
        }

        //More than one match, try inchi on them
        else if (inchikeyMatches.size()>1){
            //Search by inchi (Maybe only do this part?)
            //================================
            String molInchi;
            try {
                molInchi = cdkmol.getInChI(
                                        IMolecule.Property.USE_CACHED_OR_CALCULATED  );
            } catch ( BioclipseException e ) {
                return returnError( "Could not create Inchi", e.getMessage() );
            }

            logger.debug( "Inchi to search for: " + molInchi);
            
            for (Integer i : inchikeyMatches){
                String readInchi = moleculesmodel.getPropertyFor( i, "inchi.property.name" );
                if (molInchi.equals( readInchi )){
                    //Inchi MATCH
                    ICDKMolecule matchmol = moleculesmodel.getMoleculeAt( i );
                    ExternalMoleculeMatch match = new ExternalMoleculeMatch(matchmol);
                    results.add( match );
                }
            }
        }
        
        return results;
        
    }


}
