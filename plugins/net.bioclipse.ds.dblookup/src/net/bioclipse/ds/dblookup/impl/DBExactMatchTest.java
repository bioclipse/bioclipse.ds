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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.business.SDFileIndex;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.AbstractWarningTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.model.impl.DSException;


/**
 * A test that takes a 2 column file with toxicophores as input<br>
 * Col 1 = smarts (toxicophores)<br>
 * Col 2 = name of toxicophore<br>
 * @author ola
 *
 */
public class DBExactMatchTest extends AbstractWarningTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(DBExactMatchTest.class);
    private SDFileIndex sdfIndex;

    

    /**
     * Read database file into memory
     * @throws WarningSystemException 
     */
    private void initialize() throws DSException {
        
        ICDKManager cdk=Activator.getDefault().getCDKManager();

        String filepath=getParameters().get( "file" );
        logger.debug("Filename is: "+ filepath);

        
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

        logger.debug( "Untransformed file: " + path );
        IFile file=ResourcePathTransformer.getInstance().transform( path );
        logger.debug( "Transformed file: " + file.getFullPath() );
        
        sdfIndex = cdk.createSDFIndex( file, new NullProgressMonitor() );
        
        logger.debug("Loaded SDF index successfully");
        
    }

    public List<ITestResult> runWarningTest( IMolecule molecule )
                                                                 throws DSException, BioclipseException {
        //Read database file if not already done that
        if (sdfIndex==null)
            initialize();

        //Store results here
        List<ITestResult> results=new ArrayList<ITestResult>();

        ICDKManager cdk=Activator.getDefault().getCDKManager();
        
        ICDKMolecule cdkmol=null;
        try {
            cdkmol = cdk.create( molecule );
        } catch ( BioclipseException e ) {
            throw new DSException("Unable to create CDKMolceule: " + e.getMessage());
        }

        //Start by searching for inchiKey
        //================================
        String molInchiKey = cdkmol.getInChIKey( false );
        logger.debug( "Inchikey to search for: " + molInchiKey);
        //Search the index for this InchiKey
        //TODO

        //Next, search by inchi (Maybe only do this part)
        //================================
        String molInchi = cdkmol.getInChI( false );
        logger.debug( "Inchi to search for: " + molInchi);
        //Search the remainder of the index for this Inchi
        //TODO

        return results;
    }

}
