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
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.matcher.model.SmartsMatchEntry;
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
 * The implementation takes a 2 column file with SMARTS as input<br>
 * Col 1 = smarts, separated by , and start with ! if non-matching<br>
 * Col 2 = name of structural alert<br>
 * 
 * @author ola
 *
 */
public class SmartsAlertMatcher extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(SmartsAlertMatcher.class);

    private static final String FILE_PROPERTY_PARAM="file";

    List<SmartsMatchEntry> smarts;


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

//        //TODO: try to remove this
//        if (getTestErrorMessage().length()>1){
//            logger.error("Trying to initialize test: " + getName() + " while " +
//            "error message exists");
//            return;
//        }

        //=================
        //Verify parameters
        //=================

        //All SDFile implementations require the 'file' parameter (resource)
        String filepath=getParameters().get( FILE_PROPERTY_PARAM );
        if (filepath==null)
            throw new DSException("Required parameter '" + FILE_PROPERTY_PARAM 
                                  + "' was not provided by test " + getId() ); 


        smarts=new ArrayList<SmartsMatchEntry>();

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
           lineloop: while(line!=null){

                logger.debug("Read line: " + line);
                
                String[] parts = line.split("\t");

                if (parts.length<=0){
                	logger.error("Line " + linenr + " does not have 2 columns");
                	noSkipped++;
                }
                else{

                	SmartsMatchEntry sm = new SmartsMatchEntry(parts[1]);
                	
                	if (parts[0].startsWith("[$")){
                		//We have multiple smarts, so cut them out to list
                		
//                		System.out.println("Orig=" + parts[0]);
                		String noBrackets = parts[0].substring(3,parts[0].length()-2);
//                		System.out.println("noBrackets=" + noBrackets);
                		
                		//Replace the ; with , as they are equal in this case
                		noBrackets=noBrackets.replace(");!$(","),!$(");
                		noBrackets=noBrackets.replace(");$(","),$(");

                		//First cut of the ! parts from the end
                		String tabs = noBrackets.replace("),!$(","\t");
                		String[] nonsmartparts=tabs.split("\t");

                		String firstpart=nonsmartparts[0];
                		String semic = firstpart.replace("),$(","\t");
                		String[] smartparts=semic.split("\t");

                		for (String s : smartparts){
                    		if (!isValidSmarts(s)){
                    			logger.error("SMARTS " + s + " is invalid - skipping line");
                    			noSkipped++;
                                //Read next line in file
                                line=r.readLine();
                                linenr++;
                    			continue lineloop;
                    		}
//                			System.out.println("S: " + s);
                			sm.addSmarts(s);
                		}
                		if (nonsmartparts.length>1){
                			for (int i=1; i <nonsmartparts.length;i++){
                				String ns = nonsmartparts[i];
                        		if (!isValidSmarts(ns)){
                        			logger.error("SMARTS " + ns + " is invalid - skipping line");
                        			noSkipped++;
                                    //Read next line in file
                                    line=r.readLine();
                                    linenr++;
                        			continue lineloop;
                        		}
//                				System.out.println("NS: " + ns);
                    			sm.addNonMatchingSmarts(ns);
                			}
                		}
                		
//                		System.out.println("we");
                	}
                	else{
                		//only one smarts on line
                		if (!isValidSmarts(parts[0])){
                			logger.error("SMARTS " + parts[0] + " is invalid - skipping line");
                			noSkipped++;
                            //Read next line in file
                            line=r.readLine();
                            linenr++;
                			continue lineloop;
                		}
            			sm.addSmarts(parts[0]);
                	}
                	                	
                	logger.debug("Model " + getName() + " added SMARTS=" + sm.toString());
                	smarts.add(sm);
                }

                //Read next line in file
                line=r.readLine();
                linenr++;
            }

        } catch ( FileNotFoundException e ) {
            throw new DSException("File: " + filepath + " could not be found.");
        } catch ( IOException e ) {
            throw new DSException("File: " + filepath + " could not be found.");
        }

        logger.debug("Model " +  getName() + " parsed: " + smarts.size() 
        		+ " Lines in file: " + filepath + " and skipped " + noSkipped + " lines");

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
        ArrayList<SimpleResult> results=new ArrayList<SimpleResult>();

        IAtomContainer ac = cdkmol.getAtomContainer();
        int noHits=0;
        int noErr=0;

        //Loop over all entries
        for (SmartsMatchEntry smartsEntry : smarts){

            //Check for cancellation
            if (monitor.isCanceled())
                return returnError( "Cancelled","");
            
            List<Integer> matchingAtoms=new ArrayList<Integer>();

            //First, make sure it passes either of the smarts
            boolean isSmartsMatch=false;
            for (String smart : smartsEntry.getMatchingSmarts()){
                SMARTSQueryTool sqt;
				try {
					sqt = new SMARTSQueryTool(smart);
	            	if (sqt.matches(ac)){
	            		
	            		isSmartsMatch=true;

	            		//Add the matching atom indices
        				int nmatch = sqt.countMatches();
        				List<List<Integer>> mappings = sqt.getMatchingAtoms();
        				for (int i = 0; i < nmatch; i++) {
        					List<Integer> atomIndices = (List<Integer>) mappings.get(i);
        					matchingAtoms.addAll( atomIndices );
        				}

	            	}
				} catch (CDKException e) {
					logger.error("Error querying SMARTS: " + smart);
				}
            }

            //If no matches, take next smartsentry
            if (!isSmartsMatch)
            	continue;

            //If match, see if it also matches nonMatching
            boolean isNonSmartsMatch=false;
            if (smartsEntry.getNonMatchingSmarts()!=null){
            	for (String smart : smartsEntry.getNonMatchingSmarts()){
            		SMARTSQueryTool sqt;
            		try {
            			sqt = new SMARTSQueryTool(smart);
            			if (sqt.matches(ac)){
            				isNonSmartsMatch=true;
            			}
            		} catch (CDKException e) {
            			logger.error("Error querying SMARTS: " + smart);
            		}
            	}
            }


            //If match in nonMatching, do not add results
            if (isNonSmartsMatch)
            	continue;

            //Match in ShouldMatch but no match in NonMatch - add result

            //Toxicophores are by definition positive
            StructuralAlertsMatch match=new StructuralAlertsMatch(
            		smartsEntry.getName(), ITestResult.POSITIVE);
            
            for (int aindex : matchingAtoms){
                match.putAtomResult( aindex, ITestResult.POSITIVE );
            }

            if (smartsEntry.getNonMatchingSmarts()!=null){
            	match.setSmartsString( smartsEntry.getMatchingSmarts() 
            			+ " ; " + smartsEntry.getNonMatchingSmarts());
            }
            else
            	match.setSmartsString( smartsEntry.getMatchingSmarts().toString());

            results.add( match );
            noHits++;

        }

        logger.debug("Model " + getName() + " queried " + smarts.size() 
        		+ " structural alerts (SMARTS). # hits=" 
        		+ noHits +", # errors=" + noErr);

        return results;   

    }

    @SuppressWarnings("serial")
    @Override
    public List<String> getRequiredParameters() {
    	return new ArrayList<String>(){{add(FILE_PROPERTY_PARAM);}};
    }
}
