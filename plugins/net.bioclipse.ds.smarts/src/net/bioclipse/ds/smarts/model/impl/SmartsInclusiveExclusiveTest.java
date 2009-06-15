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
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.SimpleResult;


/**
 * A test that takes a 3 column file with toxicophores as input<br>
 * Col 1 = smarts (toxicophores)<br>
 * Col 2 = smarts (if match, not toxic)<br>
 * Col 3 = name of toxicophore<br>
 * @author ola
 *
 */
public class SmartsInclusiveExclusiveTest extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(SmartsMatchingTest.class);

    String smartsFile;
    //Smarts Name -> incl smarts, exkl smarts
    Map<String, Map<String,String>> smarts;



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

        smarts=new HashMap<String, Map<String,String>>();
        

        String filepath=getParameters().get( "file" );
        logger.debug("Initializing SmartsInclusiveExclusiveTest from file: " + filepath);

        if (filepath==null){
            setTestErrorMessage( "No data file provided for " +
            		"SmartsInclusiveExclusiveTest: " + getId() );
            return;
        }

        String path="";
            logger.debug("Trying to locate file: " + filepath + " from plugin: " + getPluginID());
            URL url = Platform.getBundle(getPluginID()).getEntry(filepath);
            logger.debug("File has URL: " + url);
            URL fileURL=null;
            try {
                fileURL = FileLocator.toFileURL(url);
            } catch ( IOException e1 ) {
                throw new DSException("Smarts file : " + url +" could not be located.");
            }
            logger.debug("File has fileURL: " + fileURL);
            path=fileURL.getFile();

        //File could not be read
        if ("".equals( path )){
            setTestErrorMessage( "File: " + filepath + " could not be read.");
            return;
        }

        int noSkipped=0;
        try {
            BufferedReader r=new BufferedReader(new FileReader(path));
            String line=r.readLine();
            
            int linenr=1;
            while(line!=null){

                logger.debug("Read line: " + line);

                StringTokenizer tk=new StringTokenizer(line);

                String incls=null;
                String excls=null;
                String tokenname="";
                String first=tk.nextToken();
                if (isValidSmarts(first)){
                    incls=first;
                    if (tk.hasMoreTokens()){
                        String second=tk.nextToken();

                        if (isValidSmarts(second)){
                            excls=second;
                        }else{
                            logger.warn( "+++++++ Second part of incl/excl SMARTS could mean an invalid SMARTS: " + second );
                            //If not a smarts, it's a name
                            tokenname=second+" ";
                        }

                        //Treat rest as name parts
                        while (tk.hasMoreTokens()){
                            tokenname=tokenname+tk.nextToken();
                        }
                        
                    }
                    
                    //So, store the stuff
                    Map<String, String> both=new HashMap<String, String>();
                    if (excls==null)
                        excls="";
                    both.put( incls, excls );
                    int namecnt=2;
                    String storedname=tokenname;
                    while (smarts.containsKey( tokenname )){
                        tokenname=storedname+namecnt;
                        namecnt++;
                    }
                    smarts.put(tokenname,both);
                    logger.debug("(" + linenr + ") Added SMARTS name: '" + tokenname + "' [incl=" + incls + "] [excl=" + excls + "]");
                }
                else{
                    logger.error( "Invalid SMARTS as first token in file: " + smartsFile + ", so skipped. Smarts=" + first );
                    noSkipped++;
                }

                //Read next line
                line=r.readLine();
                linenr++;
            }

        } catch ( FileNotFoundException e ) {
            throw new DSException("File: " + filepath + " could not be found.");
        } catch ( IOException e ) {
            throw new DSException("File: " + filepath + " could not be found.");
        }

        logger.debug("SmartsInclusiveExclusiveTest.init parsed: " + smarts.size() + " SMARTS in file: " + filepath + " and skipped: " + noSkipped);

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

    /**
     * Run and return any hits in the smartsmatching, excluding smarts on column 2
     */
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

        for (String smartName : smarts.keySet()){

            //Check for cancellation
            if (monitor.isCanceled())
                return returnError( "Cancelled","");

            String inclSmart=(String) smarts.get( smartName ).keySet().toArray()[0];
            String exclSmart=(String) smarts.get( smartName ).get( inclSmart );

//            logger.debug("Query: Incl smarts=" + inclSmart + " excl smarts=" + exclSmart+ " for mol: " + cdkmol);

            SMARTSQueryTool inclQuerytool=null;
            SMARTSQueryTool exclQuerytool=null;
            boolean inclStatus=false;
            boolean exclStatus=false;
            boolean status=false;
            try {
                inclQuerytool = new SMARTSQueryTool(inclSmart);
                inclStatus = inclQuerytool.matches(ac);
                
            } catch(Exception e){
                logger.error(
                             "InclSmarts '" + inclSmart
                             + "' with name='" + smartName 
                             + "' is not a valid CDK smarts");
                LogUtils.debugTrace( logger, e );
                
                //Create an error result and add it
                SimpleResult res = new SimpleResult( 
                                   "InclSmarts '" + inclSmart
                                   + "' with name='" + smartName 
                                   + "' is not a valid CDK smarts"
                                   , ITestResult.ERROR);
                res.setDetailedMessage( e.getMessage());
                results.add( res );

                noErr++;
            } 
            
            //If we had a match previously, try to exclude if a match in excluded
            if ((inclStatus) && (exclSmart!=null) && (exclSmart.length()>0)){
                try{
                exclQuerytool = new SMARTSQueryTool(exclSmart);
                exclStatus = exclQuerytool.matches(ac);
                } catch(Exception e){
                    logger.error(
                                 "ExclSmarts '" + exclSmart
                                 + "' with name='" + smartName 
                                 + "' is not a valid CDK smarts");
                    LogUtils.debugTrace( logger, e );
                    
                    //Create an error result and add it
                    SimpleResult res = new SimpleResult( 
                                       "ExclSmarts '" + exclSmart
                                       + "' with name='" + smartName 
                                       + "' is not a valid CDK smarts"
                                       , ITestResult.ERROR);
                    res.setDetailedMessage( e.getMessage());
                    results.add( res );

                    noErr++;
                } 

                //If included matched but excluded did not, this is a hit to report
                if (inclStatus && (!exclStatus)){
                    status=true;
                }
            }else{
                //If no exclSmarts, then this is the result
                status=inclStatus;
            }

            
            //If we have matches, take care of them
            if (status) {

                int nmatch = inclQuerytool.countMatches();

                List<Integer> matchingAtoms=new ArrayList<Integer>();
                List<List<Integer>> mappings = inclQuerytool.getMatchingAtoms();
                for (int i = 0; i < nmatch; i++) {
                    List<Integer> atomIndices = (List<Integer>) mappings.get(i);
                    matchingAtoms.addAll( atomIndices );
                }
 
                //Create new ac to hold substructure
                IAtomContainer subAC=ac.getBuilder().newAtomContainer();
                for (int aindex : matchingAtoms){
                    subAC.addAtom( ac.getAtom( aindex ) );
                }
                
                
                //Toxicophores are by definition positive
                SmartsMatchingTestMatch match=new SmartsMatchingTestMatch(
                                              smartName, ITestResult.POSITIVE);
                match.setAtomContainer( subAC );
                match.setSmartsString( inclSmart + " ; " + exclSmart );
                

                results.add( match );
                noHits++;
            }

        }

        logger.debug("Queried " + smarts.keySet().size() + " smarts. # hits=" +noHits +", # errors=" + noErr);

        return results;    

    }

}
