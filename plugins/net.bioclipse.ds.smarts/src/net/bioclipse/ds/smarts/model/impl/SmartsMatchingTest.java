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
package net.bioclipse.ds.smarts.model.impl;

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
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.AbstractWarningTest;
import net.bioclipse.ds.model.ErrorResult;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.impl.DSException;


/**
 * A test that takes a 2 column file with toxicophores as input<br>
 * Col 1 = smarts (toxicophores)<br>
 * Col 2 = name of toxicophore<br>
 * @author ola
 *
 */
public class SmartsMatchingTest extends AbstractWarningTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(SmartsMatchingTest.class);

    String smartsFile;
    Map<String, String> smarts;  //Smarts string -> Smarts Name


    /**
     * Read smarts file from disk into array
     * @param monitor 
     * @throws WarningSystemException 
     */
    private void initialize(IProgressMonitor monitor) throws DSException {

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
            e1.printStackTrace();
            return;
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
        
        //Read smarts file if not already done that
        if (smarts==null){
            try {
                initialize(monitor);
            } catch ( DSException e1 ) {
                return returnError( e1.getMessage(), e1.getStackTrace().toString());
            }
        }

        //Check for cancellation
        if (monitor.isCanceled())
            return returnError( "Cancelled","");


        //Store results here
        List<ITestResult> results=new ArrayList<ITestResult>();
        
        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();
        ICDKMolecule cdkmol=null;
        try {
            cdkmol = cdk.create( molecule );
        } catch ( BioclipseException e ) {
            return returnError( "Unable to create CDKMolceule" , e.getMessage());
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
                results.add( new ErrorResult( 
                                             "Smarts '" + currentSmarts 
                                             + "' with name='"+ smartsName 
                                             +"' is not a valid CDK smarts"
                                             ,e.getMessage()));
                noErr++;
            }
            
            if (status) {
                noHits++;
                //At least one match
                SmartsMatchingTestMatch match=new SmartsMatchingTestMatch();

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
                    subAC.addAtom( ac.getAtom( aindex ) );
                }
                match.setAtomContainer( subAC );
                match.setSmartsString( currentSmarts );
                match.setSmartsName( smartsName);
                
                results.add( match );

            }
            
        }
        
        logger.debug("Queried " + smarts.keySet().size() + " smarts. # hits=" +noHits +", # errors=" + noErr);

        return results;
    }

}
