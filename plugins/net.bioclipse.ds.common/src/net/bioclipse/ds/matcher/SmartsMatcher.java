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
import org.openscience.cdk.interfaces.IAtom;
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
 * @author ola
 *
 */
public class SmartsMatcher extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(SmartsMatcher.class);

    private static final String FILE_PROPERTY_PARAM="file";

    Map<String, String> smarts;  //Smarts string -> Smarts Name

    /**
     * Verify parameters, read/parse SDF file into model, and verify properties
     * @param monitor 
     * @throws DSException
     */
    public void initialize(IProgressMonitor monitor) throws DSException {

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

        //This implementations requires the 'file' parameter (resource)
        //pointing to a file of SMARTS
        String filepath=getParameters().get( FILE_PROPERTY_PARAM );
        if (filepath==null)
            throw new DSException("Required parameter '" + FILE_PROPERTY_PARAM 
                                  + "' was not provided by test " + getId() ); 


        smarts=new HashMap<String, String>();

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
                if (monitor.isCanceled())
                    throw new DSException("Cancelled.");

                StringTokenizer tk=new StringTokenizer(line);
                if (tk.countTokens()==2) {
                    String sm=tk.nextToken();
                    String nm=tk.nextToken();
                    if (isValidSmarts( sm ))
                        smarts.put(sm,nm);
                    else{
                        logger.error("SMARTS: " + sm + " in file: " 
                                     + filepath + " is invalid so skipped.");
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
                        logger.error("SMARTS: " + sm + " in file: " + filepath 
                                     + " is invalid so skipped.");
                        noSkipped++;
                    }
                }

                else if (tk.countTokens()==1) {
                    //Interpret this as only a SMARTS without a name
                    String sm=tk.nextToken();
                    if (isValidSmarts( sm ))
                        smarts.put(sm,sm);
                    else{
                        logger.error("SMARTS: " + sm + " in file: " + filepath 
                                     + " is invalid so skipped.");
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

        logger.debug("SmartsMatchingTest.init parsed: " + smarts.size() 
                     + " SMARTS in file: " + filepath 
                     + " and skipped: " + noSkipped);

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
    protected List<? extends ITestResult> doRunTest( ICDKMolecule cdkmol,
                                                     IProgressMonitor monitor ) {

    	//Task size is number of smarts
//    	monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
    	
        //Store results here
        ArrayList<SimpleResult> results=new 
        ArrayList<SimpleResult>();

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

                //Toxicophores are by definition positive
                StructuralAlertsMatch match=new StructuralAlertsMatch(
                                                  smartsName, ITestResult.POSITIVE);
                for (int aindex : matchingAtoms){
                    match.putAtomResult( aindex, ITestResult.POSITIVE );
                }
                match.setSmartsString( currentSmarts );

                results.add( match );
            }        

        }

        logger.debug("Test: " + getId() + " queried " + smarts.keySet().size() 
                     + " smarts. # hits=" +noHits +", # errors=" + noErr);

        return results;

    }
}
