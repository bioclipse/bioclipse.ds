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
package net.bioclipse.ds.impl;

import java.io.BufferedReader;
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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.impl.result.SimpleResult;
import net.bioclipse.ds.impl.result.SmartsMatch;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.IDSTest;


/**
 * A test that takes a 2 column file with toxicophores as input<br>
 * Col 1 = smarts (toxicophores)<br>
 * Col 2 = name of toxicophore<br>
 * @author ola
 *
 */
public class SmartsMatching extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(SmartsMatching.class);

    String smartsFile;
    Map<String, String> smarts;  //Smarts string -> Smarts Name


    /**
     * Read smarts file from disk into array
     * @param monitor 
     * @throws WarningSystemException 
     */
    public void initialize(IProgressMonitor monitor) throws DSException {

        if (getTestErrorMessage().length()>1){
            logger.error("Trying to initialize test: " + getName() + " while " +
                "error message exists");
            return;
        }

        smarts=new HashMap<String, String>();
        
        String filepath=getParameters().get( "file" );
        logger.debug("Initializing SmartsMatchingTest from file: " + filepath);

        if (filepath==null)
            throw new DSException("No data file provided for SmartsMatchingTest: " + getId());

        
        String path="";
        try {
            logger.debug("Trying to locate file: " + filepath + " from plugin: " + getPluginID());
            URL url = Platform.getBundle(getPluginID()).getEntry(filepath);
            logger.debug("File has URL: " + url);
            URL fileURL = FileLocator.toFileURL(url);
            logger.debug("File has fileURL: " + fileURL);
            path=fileURL.getFile();
        } catch ( Exception e1 ) {
            throw new DSException("Could not locate or read file: " + filepath);
        }

        //File could not be read
        if ("".equals( path )){
            throw new DSException("File: " + filepath + " could not be read.");
        }

        int noSkipped=0;
        
        try {
            BufferedReader r=new BufferedReader(new FileReader(path));
            String line=r.readLine();
            int linenr=1;
            while(line!=null){
                if (monitor.isCanceled())
                    throw new DSException("Cancelled.");
                
                StringTokenizer tk=new StringTokenizer(line);
                if (tk.countTokens()==2) {
                    String sm=tk.nextToken();
                    String nm=tk.nextToken();
                    if (isValidSmarts( sm ))
                        smarts.put(sm,nm);
                    else{
                        logger.error("SMARTS: " + sm + " in file: " + smartsFile + " is invalid so skipped.");
                        noSkipped++;
                    }
                }
                else if (tk.countTokens()>2) {
                    //Interpret this as name has spaces in it
                    String sm=tk.nextToken();
                    String nm=tk.nextToken();
                    while(tk.hasMoreTokens()){
                        nm=nm+" " + tk.nextToken();
                    }
                    if (isValidSmarts( sm ))
                        smarts.put(sm,nm);
                    else{
                        logger.error("SMARTS: " + sm + " in file: " + smartsFile + " is invalid so skipped.");
                        noSkipped++;
                    }
                }

                else if (tk.countTokens()==1) {
                    //Interpret this as only a SMARTS without a name
                    String sm=tk.nextToken();
                    if (isValidSmarts( sm ))
                        smarts.put(sm,sm);
                    else{
                        logger.error("SMARTS: " + sm + " in file: " + smartsFile + " is invalid so skipped.");
                        noSkipped++;
                    }
                }
                
                //Read next line
                line=r.readLine();
                linenr++;
            }
            
        } catch ( FileNotFoundException e ) {
            throw new DSException("File: " + filepath + " could not be found.");
        } catch ( IOException e ) {
            throw new DSException("File: " + filepath + " could not be read.");
        }
        
        logger.debug("SmartsMatchingTest.init parsed: " + smarts.size() + " SMARTS in file: " + filepath + " and skipped: " + noSkipped);
        
    }

    //Test if a smarts is valid
    private boolean isValidSmarts( String smarts ) {
        try {
            new SMARTSQueryTool(smarts);
            return true;
        } catch ( Exception e ) {
            return false;
        } catch ( Error e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return getName();
    }


    public List<ITestResult> runWarningTest( IMolecule molecule, IProgressMonitor monitor ){
        
        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");
        
        //Store results here
        List<ITestResult> results=new ArrayList<ITestResult>();

        //Initialize if not already done
        if (smarts==null){
            try {
                initialize(monitor);
            } catch ( DSException e1 ) {
                logger.error( "Failed to initialize DBNNTest: " + e1.getMessage() );
                setTestErrorMessage( "Failed to initialize: " + e1.getMessage() );
            }
        }

        //Return empty if error message
        if (getTestErrorMessage().length()>1){
            return results;
        }

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");

        //Create mol from input mol, if needed
        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();
        ICDKMolecule cdkmol=null;
        ICDKMolecule cdkmol_in = null;
        try {
            cdkmol_in = cdk.asCDKMolecule( molecule );
            cdkmol=new CDKMolecule((IAtomContainer)cdkmol_in.getAtomContainer().clone());
//            cdkmol = cdk.create( molecule );
        } catch ( BioclipseException e ) {
            return returnError( "Could not create CDKMolecule", e.getMessage() );
        } catch ( CloneNotSupportedException e ) {
            return returnError( "Could not clone CDKMolecule", e.getMessage() );
        }

        
        
        
        IAtomContainer ac = cdkmol.getAtomContainer();
        int noHits=0;
        int noErr=0;
        for (String currentSmarts : smarts.keySet()){
            
            String smartsName = smarts.get( currentSmarts );

            //Check for cancellation
            if (monitor.isCanceled())
                return returnError( "Cancelled","");

            SMARTSQueryTool querytool=null;
            boolean status=false;
            try {
                querytool = new SMARTSQueryTool(currentSmarts);
                status = querytool.matches(ac);
            } catch(Exception e){
                
                logger.error(
                             "Smarts name='" + smartsName +"' with SMARTS='" + currentSmarts 
                             + "' is not a valid CDK smarts.");
                LogUtils.debugTrace( logger, e );

                //Create an error result and add it
                SimpleResult res = new SimpleResult( 
                                 "Smarts '" + currentSmarts 
                                 + "' with name='"+ smartsName 
                                 +"' is not a valid CDK smarts"
                                 , ITestResult.ERROR);
                res.setDetailedMessage( e.getMessage());
                results.add( res );
                noErr++;
            }
            
            if (status) {
                noHits++;
                int nmatch = querytool.countMatches();

                List<Integer> matchingAtoms=new ArrayList<Integer>();
                List<List<Integer>> mappings = querytool.getMatchingAtoms();
                for (int i = 0; i < nmatch; i++) {
                    List<Integer> atomIndices = (List<Integer>) mappings.get(i);
                    matchingAtoms.addAll( atomIndices );
                }

                //Create new ac to hold substructure
                IAtomContainer subAC=ac.getBuilder().newAtomContainer();
                for (int aindex : matchingAtoms){
//                    subAC.addAtom( ac.getAtom( aindex ) );
                    subAC.addAtom( cdkmol_in.getAtomContainer().getAtom( aindex ) );
                }

                //Toxicophores are by definition positive
                SmartsMatch match=new SmartsMatch(
                                              smartsName, ITestResult.POSITIVE);
                match.setAtomContainer( subAC );
                match.setSmartsString( currentSmarts );
                
                results.add( match );

            }
            
        }
        
        logger.debug("Queried " + smarts.keySet().size() + " smarts. # hits=" +noHits +", # errors=" + noErr);

        return results;
    }

}
