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
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.AbstractWarningTest;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.impl.DSException;


/**
 * A test that takes a 3 column file with toxicophores as input<br>
 * Col 1 = smarts (toxicophores)<br>
 * Col 2 = smarts (if match, not toxic)<br>
 * Col 3 = name of toxicophore<br>
 * @author ola
 *
 */
public class SmartsInclusiveExclusiveTest extends AbstractWarningTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(SmartsMatchingTest.class);

    String smartsFile;
    //Smarts Name -> incl smarts, exkl smarts
    Map<String, Map<String,String>> smarts;



    /**
     * Read smarts file from disk into array
     * @throws WarningSystemException 
     */
    private void initialize() throws DSException {

        smarts=new HashMap<String, Map<String,String>>();

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
                    logger.error( "Invalid SMARTS as first token, so skipped: " + first );
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

        logger.debug("Parsed: " + smarts.size() + " SMARTS in file: " + filepath);

    }

    //Test if a smarts is valid
    private boolean isValidSmarts( String smarts ) {
        try {
            new SMARTSQueryTool(smarts);
            return true;
        } catch ( Exception e ) {
            logger.error("  ## Smarts: " + smarts + " is invalid.");
            //          logger.debug( e.getMessage());
            return false;
        } catch ( Error e) {
            logger.error("  ## Smarts: " + smarts + " is invalid.");
            //        logger.debug( e.getMessage());
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
    public List<ITestResult> runWarningTest( IMolecule molecule )
    throws DSException {


        if (smarts==null){
            initialize();
        }

        //Store results here
        List<ITestResult> results=new ArrayList<ITestResult>();

        ICDKManager cdk=Activator.getDefault().getCDKManager();
        ICDKMolecule cdkmol;
        try {
            cdkmol = cdk.create( molecule );
        } catch ( BioclipseException e ) {
            throw new DSException("Unable to create CDKMolceule: " + e.getMessage());
        }

        IAtomContainer ac = cdkmol.getAtomContainer();

        for (String smartName : smarts.keySet()){

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
            } catch ( CDKException e ) {
                logger.debug(" ** Smarts: " + smartName + " (" + inclSmart + " ; " +
                                   exclSmart +" )" + " failed to query.");
//                
//                System.out.println(inclSmart);
                
                //                logger.debug(e.getMessage());
                //                throw new WarningSystemException("Unable to query smartsmol: " + e.getMessage());
            }
            try{
                if (exclSmart!=null){
                    exclQuerytool = new SMARTSQueryTool(exclSmart);
                    exclStatus = exclQuerytool.matches(ac);

                    if (inclStatus && (!exclStatus)){
                        status=true;
                    }
                }else{
                    //If only inclSmarts, then this is the result
                    status=inclStatus;
                }

            } catch ( CDKException e ) {
                logger.debug(" ** Smarts: " + smartName + " (" + inclSmart + " ; " +
                                   exclSmart +" )" + " failed to query.");
                
//                System.out.println(exclSmart);
                
                //                logger.debug(e.getMessage());
                //                throw new WarningSystemException("Unable to query smartsmol: " + e.getMessage());
            }
            if (status) {
                //At least one match
                SmartsMatchingTestMatch match=new SmartsMatchingTestMatch();

                int nmatch = inclQuerytool.countMatches();
                logger.debug("Found " + nmatch + " in mol");

                List<Integer> matchingAtoms=new ArrayList<Integer>();
                List<List<Integer>> mappings = inclQuerytool.getMatchingAtoms();
                for (int i = 0; i < nmatch; i++) {
                    logger.debug("Match no: " + i);
                    List<Integer> atomIndices = (List<Integer>) mappings.get(i);
                    matchingAtoms.addAll( atomIndices );
                }
 
                //Create new ac to hold substructure
                IAtomContainer subAC=ac.getBuilder().newAtomContainer();
                for (int aindex : matchingAtoms){
                    subAC.addAtom( ac.getAtom( aindex ) );
                }
                match.setAtomContainer( subAC );

                match.setSmartsString( inclSmart + " ; " + exclSmart );
                match.setSmartsName( smartName);

                results.add( match );

            }

        }

        return results;    

    }

}
