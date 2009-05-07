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
public class SmartsMatchingTest extends AbstractWarningTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(SmartsMatchingTest.class);

    String smartsFile;
    Map<String, String> smarts;  //Smarts string -> Smarts Name

    /**
     * Run and return any hits in the smartsmatching
     * @throws WarningSystemException 
     */
    public List<ITestResult> runWarningTest( IMolecule molecule , TestRun testRun) throws DSException {
        
        if (smarts==null){
            initialize();
        }

        //Store results here
        List<ITestResult> ret=new ArrayList<ITestResult>();
        
        ICDKManager cdk=Activator.getDefault().getCDKManager();
        ICDKMolecule cdkmol=null;
        try {
            cdkmol = cdk.create( molecule );
        } catch ( BioclipseException e ) {
            throw new DSException("Unable to create CDKMolceule: " + e.getMessage());
        }
        
        //TODO: Replace with manager call
        
/*        
        IAtomContainer ac = cdkmol.getAtomContainer();

        for (String currentSmarts : smarts.keySet()){
            
            logger.debug("Querying smarts: " + currentSmarts + " for mol: " + cdkmol);
            
            SMARTSQueryTool querytool=null;
            boolean status=false;
            try {
                querytool = new SMARTSQueryTool(currentSmarts);
                status = querytool.matches(ac);
            } catch ( CDKException e ) {
                logger.debug("Smarts: " + currentSmarts + " failed to query.");
//                logger.debug(e.getMessage());
//                throw new WarningSystemException("Unable to query smartsmol: " + e.getMessage());
            }
            if (status) {
                //At least one match
                SmartsMatchingTestMatch match=new SmartsMatchingTestMatch();


                int nmatch = querytool.countMatches();
                logger.debug("Found " + nmatch + " in mol");

                List<Integer> matchingAtoms=new ArrayList<Integer>();
                List<List<Integer>> mappings = querytool.getMatchingAtoms();
                for (int i = 0; i < nmatch; i++) {
                    logger.debug("Match no: " + i);
                    List<Integer> atomIndices = (List<Integer>) mappings.get(i);
                    matchingAtoms.addAll( atomIndices );
                }
                match.setMatchingAtoms( matchingAtoms );
                match.setSmartsString( currentSmarts );
                match.setSmartsName( smarts.get( currentSmarts ));
                match.setTestRun( testRun );

                ret.add( match );

            }
            
        }
*/
        return ret;
    }


    /**
     * Read smarts file from disk into array
     * @throws WarningSystemException 
     */
    private void initialize() throws DSException {

        smarts=new HashMap<String, String>();
        
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
                
                StringTokenizer tk=new StringTokenizer(line);
                if (tk.countTokens()==2) {
                    String sm=tk.nextToken();
                    String nm=tk.nextToken();
                    smarts.put(sm,nm);
                }
                else if (tk.countTokens()>2) {
                    //Interpret this as name has spaces in it
                    String sm=tk.nextToken();
                    String nm=tk.nextToken();
                    while(tk.hasMoreTokens()){
                        nm=nm+" " + tk.nextToken();
                    }
                    smarts.put(sm,nm);
                }

                else if (tk.countTokens()==1) {
                    //Interpret this as only a SMARTS without a name
                    String sm=tk.nextToken();
                    smarts.put(sm,sm);
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

    @Override
    public String toString() {
        return getName();
    }


    public List<ITestResult> runWarningTest( IMolecule molecule )
                                                                 throws DSException {
        //Read smarts file if not already done that
        if (smarts==null){
            initialize();
        }

        //Store results here
        List<ITestResult> results=new ArrayList<ITestResult>();
        
        ICDKManager cdk=Activator.getDefault().getCDKManager();
        ICDKMolecule cdkmol=null;
        try {
            cdkmol = cdk.create( molecule );
        } catch ( BioclipseException e ) {
            throw new DSException("Unable to create CDKMolceule: " + e.getMessage());
        }

        IAtomContainer ac = cdkmol.getAtomContainer();

        for (String currentSmarts : smarts.keySet()){
            
            logger.debug("Querying smarts: " + currentSmarts + " for mol: " + cdkmol);
            
            SMARTSQueryTool querytool=null;
            boolean status=false;
            try {
                querytool = new SMARTSQueryTool(currentSmarts);
                status = querytool.matches(ac);
            } catch ( CDKException e ) {
                logger.debug(" ## Smarts: " + currentSmarts + " failed to query");
//                System.out.println("===============\n" + e.getMessage());
            }
            if (status) {
                //At least one match
                SmartsMatchingTestMatch match=new SmartsMatchingTestMatch();

                int nmatch = querytool.countMatches();
                logger.debug("    Found " + nmatch + " match(es)");

                List<Integer> matchingAtoms=new ArrayList<Integer>();
                List<List<Integer>> mappings = querytool.getMatchingAtoms();
                for (int i = 0; i < nmatch; i++) {
                    List<Integer> atomIndices = (List<Integer>) mappings.get(i);
                    matchingAtoms.addAll( atomIndices );
                }
                match.setMatchingAtoms( matchingAtoms );
                match.setSmartsString( currentSmarts );
                match.setSmartsName( smarts.get( currentSmarts ));

                results.add( match );

            }else{
                logger.debug("    Found no matches");
            }
            
        }

        return results;
    }

}
