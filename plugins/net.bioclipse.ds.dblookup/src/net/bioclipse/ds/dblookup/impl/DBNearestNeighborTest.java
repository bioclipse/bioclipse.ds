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
import java.util.BitSet;
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
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.SDFileIndex;
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
public class DBNearestNeighborTest extends AbstractWarningTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(DBNearestNeighborTest.class);
    private SDFileIndex sdfIndex;



    /**
     * Read database file into memory
     * @throws WarningSystemException 
     */
    private void initialize() throws DSException {

        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();

        String filepath=getParameters().get( "file" );
        logger.debug("File parameter is: "+ filepath);

        String tanimoto=getParameters().get( "distance.tanimoto" );
        logger.debug("NearestTest tanimoto parameter is : "+ tanimoto);

        //Assert parameters are present
        if (filepath==null)
            throw new DSException("No file provided for DBNearestNeighbourTest: " 
                                  + getId());
        if (tanimoto==null)
            throw new DSException("No tanimoto distance provided for " +
            		"DBNearestNeighbourTest: " + getId());

        //Read the file
        String path="";
        try {
            URL url2 = FileLocator.toFileURL(Platform.getBundle(getPluginID()).
                                             getEntry(filepath));
            path=url2.getFile();
        } catch ( IOException e1 ) {
            e1.printStackTrace();
            return;
        }

        //File could not be read
        if ("".equals( path )){
            throw new DSException("File: " + filepath + " could not be read.");
        }

        logger.debug( "File path: " + path );
//        sdfIndex = cdk.createSDFIndex( path);

//        logger.debug("Loaded SDF index successfully NOT implemented. No mols: " + sdfIndex.size());

        //TODO: load property from file to index

    }

    public List<ITestResult> runWarningTest( IMolecule molecule ){

        //Store results here
        List<ITestResult> results=new ArrayList<ITestResult>();

        //        if (true) return results;

        //Read database file if not already done that
        if (sdfIndex==null)
            try {
                initialize();
            } catch ( DSException e1 ) {
                return returnError( e1.getMessage(), e1.getStackTrace().toString());
            }
            ICDKManager cdk=Activator.getDefault().getJavaCDKManager();

            ICDKMolecule cdkmol=null;
            try {
                cdkmol = cdk.create( molecule );

            //Start by searching for inchiKey
            //================================
            BitSet molFP = cdkmol.getFingerprint( IMolecule.Property.
                                                  USE_CACHED_OR_CALCULATED );
            logger.debug( "FP to search for: " + molFP);
            //Search the index for this FP
            //TODO: implement

            //TODO: come up with better serialization than the default java serialization of FP

            } catch ( BioclipseException e ) {
                return returnError( "Unable to create CDKMolceule" , e.getMessage());
            }

            //TODO: Remove when implemented
            if (true)
            return returnError("Exact DB lookup is not yet implemented.", "");


            return results;
    }

}
