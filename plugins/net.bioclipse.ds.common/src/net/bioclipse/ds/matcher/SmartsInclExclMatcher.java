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

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.SimpleResult;
import net.bioclipse.ds.model.result.StructuralAlertsMatch;


/**
 * An implementation that handles reading of a file of SMARTS into memory and 
 * allows for smarts searching of this model.
 * 
 * The implementation takes a 3 column file with toxicophores as input<br>
 * Col 1 = smarts (toxicophores)<br>
 * Col 2 = smarts (if match, not toxic)<br>
 * Col 3 = name of toxicophore<br>
 * 
 * @author ola
 *
 */
public class SmartsInclExclMatcher extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(SmartsInclExclMatcher.class);

    private static final String FILE_PROPERTY_PARAM="file";

    //Smarts Name -> incl smarts, exkl smarts
    Map<String, Map<String,String>> smarts;


    /**
     * Verify parameters, read/parse SDF file into model, and verify properties
     * @param monitor 
     * @throws DSException
     */
    public void initialize(IProgressMonitor monitor) throws DSException {

    	super.initialize(monitor);

        if (monitor.isCanceled())
            throw new DSException("Initialization of test " + 
                                  getId() + " cancelled");

        //TODO: try to remove this
        if (getTestErrorMessage().length()>1){
            logger.error("Trying to initialize test: " + getName() + " while " +
            "error message exists");
            return;
        }

        //=================
        //Verify parameters
        //=================

        //All SDFile implementations require the 'file' parameter (resource)
        String filepath=getParameters().get( FILE_PROPERTY_PARAM );
        if (filepath==null)
            throw new DSException("Required parameter '" + FILE_PROPERTY_PARAM 
                                  + "' was not provided by test " + getId() ); 


        smarts=new HashMap<String, Map<String,String>>();

        String path="";
        try {
            logger.debug("Trying to locate file: " + filepath + " from plugin: "
                         + getPluginID());
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
                            logger.warn( "+++++++ Second part of incl/excl " +
                            " SMARTS could mean an invalid SMARTS: " + second );
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
                    logger.debug("(" + linenr + ") Added SMARTS name: '" 
                                 + tokenname + "' [incl=" 
                                 + incls + "] [excl=" + excls + "]");
                }
                else{
                    logger.error( "Invalid SMARTS as first token in file: " + 
                                  path + ", so skipped. Smarts=" + first );
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


    @Override
    protected List<? extends ITestResult> doRunTest( ICDKMolecule cdkmol,
                                                     IProgressMonitor monitor ) {

        //Store results here
        ArrayList<SimpleResult> results=new 
        ArrayList<SimpleResult>();

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
 
                //Toxicophores are by definition positive
                StructuralAlertsMatch match=new StructuralAlertsMatch(
                                              smartName, ITestResult.POSITIVE);
                match.setAtomNumbers( matchingAtoms );
                match.setSmartsString( inclSmart + " ; " + exclSmart );
                

                results.add( match );
                noHits++;
            }

        }

        logger.debug("Queried " + smarts.keySet().size() + " smarts. # hits=" 
                     + noHits +", # errors=" + noErr);

        return results;   

    }
}
