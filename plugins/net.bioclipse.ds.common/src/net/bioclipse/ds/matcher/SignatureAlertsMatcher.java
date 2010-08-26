/* *****************************************************************************
 * Copyright (c) 2010 Ola Spjuth.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.matcher.model.SignificantSignature;
import net.bioclipse.ds.matcher.model.SignificantSignatureMatch;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.SimpleResult;
import net.bioclipse.ds.signatures.Activator;
import net.bioclipse.ds.signatures.business.ISignaturesManager;
import net.bioclipse.ds.signatures.prop.calc.AtomSignatures;


/**
 * An implementation that handles reading of a file of SignificantSignatures 
 * into memory and allows for querying for structural alerts
 * 
 * @author ola
 *
 */
public class SignatureAlertsMatcher extends AbstractDSTest implements IDSTest{

    private static final Logger logger = Logger.getLogger(SignatureAlertsMatcher.class);

    private static final String FILE_PROPERTY_PARAM="file";

    //Model, from height to list of significant signatures
	Map<Integer, List<SignificantSignature>> significantSignatures;
	Map<Integer, List<String>> significantSignatureStrings;

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
        //containing significant signatures
        String filepath=getParameters().get( FILE_PROPERTY_PARAM );
        if (filepath==null)
            throw new DSException("Required parameter '" + FILE_PROPERTY_PARAM 
                                  + "' was not provided by test " + getId() ); 


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

        //=================
        // Parse file contents
        //=================
        significantSignatures=new HashMap<Integer,List<SignificantSignature>>();
        significantSignatureStrings=new HashMap<Integer, List<String>>();
		List<SignificantSignature> signsignlist=null;
		List<String> signsignStringlist=null;
		int signCount = 0;

		try {
			BufferedReader r=new BufferedReader(new FileReader(path));
			String line=r.readLine();
			int linenr=1;
			int height=0;
			while(line!=null){

				//height X:
				if (line.length()==1){
					height=Integer.parseInt(line);

					//Create a new list for this height and add to map
					signsignlist=new ArrayList<SignificantSignature>();
					significantSignatures.put(height, signsignlist);

					//Create a new list of strings and add to map
					//This is to speed up searching
					signsignStringlist=new ArrayList<String>();
					significantSignatureStrings.put(height, signsignStringlist);
				}

				else if (line.startsWith("significantSignature")){

					StringTokenizer tk=new StringTokenizer(line, " ");
//					logger.debug("Parsing line: " + line);

					int i=0;
					String sign="";
					int nrpos=-1;
					int nrtot=-1;
					double pvalue=-1;
					double accuracy=-1;
					String activeCall="";

					while (tk.hasMoreTokens()){
						String part=tk.nextToken();
						if (i==1)
							sign=part;
						if (i==3)
							nrpos=Integer.parseInt(part);
						if (i==5)
							nrtot=Integer.parseInt(part);
						if (i==7)
							pvalue=Double.parseDouble(part);
						if (i==9)
							accuracy=Double.parseDouble(part);
						if (i==11)
							activeCall=part;
						i++;
					}

					//Create and add signatures to list
					SignificantSignature signsign=new SignificantSignature(
  					  sign, nrpos, nrtot, pvalue, accuracy, activeCall, height);
					signsignlist.add(signsign);
					signsignStringlist.add(sign);
//					logger.debug("  Added SignSignature: " + signsign);
					signCount++;
				}
				else{
					throw new DSException("File: " + filepath + " line " 
							+ linenr + " could not be parsed. Contents: " 
							+ line);
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

        logger.debug("SignaturesMatcher.init parsed: " + signCount 
                     + " Signatures in file: " + filepath);

    }


    @Override
    protected List<? extends ITestResult> doRunTest( ICDKMolecule cdkmol,
                                                     IProgressMonitor monitor ) {

        //Store results here
        ArrayList<SignificantSignatureMatch> results = 
        	new ArrayList<SignificantSignatureMatch>();
        
        //Generate atom signatures for molecule
        ISignaturesManager signatures= Activator.getDefault()
        .getJavaSignaturesManager();

		logger.debug("Generating AtomSignatures and comparing with stored.");
		Map<Integer, List<String>> matches=new HashMap<Integer, List<String>>();
		
        try {
        	
        	for (int height=0; height<6; height++){
        		
                if (monitor.isCanceled())
                    return returnError( "Cancelled","");

                AtomSignatures as = signatures.generate(cdkmol, height);

                //For all stored signatures at this height
                //Compare with calculated and see any overlaps
    			List<String> calclist = as.getSignatures();
    			logger.debug("Calculated atom signatures for height " + height 
    					+ ": " + calclist.toString());
    			
    			//Get all signatures for this height
    			List<String> currentSignStringlist = 
    				significantSignatureStrings.get(height);
    			logger.debug("Significant atom signatures for height " + height 
    					+ ": " + currentSignStringlist.toString());

    			//Compute union to identify matches
    			List<String> union=new ArrayList<String>();
    			union.addAll(calclist);
    			union.retainAll(currentSignStringlist);
    			
    			logger.debug("On height " + height + " there were " + union.size() 
    					+ " matches.");
    			
    			//If we have matches for this height...
    			if (union.size()>0){
    				
    				//Loop over all hits
    				for (String hit : union){
    					
    	                if (monitor.isCanceled())
    	                    return returnError( "Cancelled","");

    					List<Integer> matchingAtoms =new ArrayList<Integer>();

    					//Get atoms from this hit, since possible to get several
    					int centeratom=0;
    					for (String psig : calclist){
    						
    						//If this is not the one we are looking for...
    						if (psig!=hit){
    							centeratom++;
    							continue;
    						}
    						
    						//This is the atom which has produced this signature
    						logger.debug("Sign: " + psig + " center atom hit: " + centeratom);

							matchingAtoms.addAll(getAtomIndices(cdkmol, 
									centeratom, height));

    						//Take next centeratom, if exists
    						centeratom++;
    					}
    					
						SignificantSignature signinfo=null;    					
						//Look up in comprehensive list
						for (SignificantSignature s : 
							significantSignatures.get(height)){
							if (s.getSignature().equals(hit)){
								signinfo=s;
							}
						}
						if (signinfo==null){
							logger.error("A signature was found in string " +
									"list but could not be found in " +
							"comprehensive info list.");
						}
						else{
							//Found good result
							SignificantSignatureMatch match=null;
							
							//Have we added the same sign already? If so, 
							//this is a duplicate so add atoms to existing list
							for (SignificantSignatureMatch oldMatch : results){
								if (oldMatch.getSignificantSignature()
										.getSignature().equals(
												signinfo.getSignature())){
									match=oldMatch;
								}
							}

							//If no prev match with this sign found, create new
							if (match==null){
								match = new SignificantSignatureMatch(
											signinfo, ITestResult.POSITIVE);
								results.add( match );
							}

							for (int aindex : matchingAtoms){
								match.putAtomResult( aindex, 
										ITestResult.POSITIVE );
							}
						}
    				}
    			}
        	}
        } catch ( Exception e ) {
            logger.error( "Failed to calculate AtomSignatures for mol: " 
            		+ cdkmol);
            return returnError( "Error generating Atom Signatures"
            		, e.getMessage() );
        }

        return results;

    }


    /**
     * Return all atom numbers centered around this atom on a certain height
     * 
     * @param cdkmol
     * @param centeratomno
     * @param height
     * @return
     */
	private List<Integer> getAtomIndices(ICDKMolecule cdkmol, int centeratomno,
			int height) {
		
		IAtomContainer ac = cdkmol.getAtomContainer();
		IAtom centeratom=ac.getAtom(centeratomno);

		//Compute neighbors for all
		Map<Integer, List<IAtom>> atomOnDistance=
			new HashMap<Integer, List<IAtom>>();
		
		for (int i=0; i<=height; i++){
			List<IAtom> alist=new ArrayList<IAtom>();
			if (i==0)
				alist.add(centeratom);
			else{
				//Find neighbors to the previously added atoms
				for (IAtom a : atomOnDistance.get(i-1)){
					for (IAtom a2 : ac.getConnectedAtomsList(a)){
						alist.add(a2);
					}
				}
			}

			if (!alist.isEmpty()){
				atomOnDistance.put(i, alist);
			}
			
		}
		
		//Truncate height-dependent map into a list of atom numbers
		List<Integer> ret=new ArrayList<Integer>();
		for (Integer i : atomOnDistance.keySet()){
			for (IAtom atom : atomOnDistance.get(i)){
				if (!ret.contains(ac.getAtomNumber(atom))){
					ret.add(ac.getAtomNumber(atom));
				}
			}
		}
		
		logger.debug("  all atom hits: " + ret.toString());
		
		return ret;
	}
	
	/**
	 * Seems unused? Schedule for removal.
	 * @param arlList
	 */
	@Deprecated
	public static void removeDuplicateWithOrder(List<IAtom> arlList)
	{
		Set<IAtom> set = new HashSet<IAtom>();
		List<IAtom> newList = new ArrayList<IAtom>();
		for (Iterator<IAtom> iter = arlList.iterator(); iter.hasNext(); ) {
			IAtom element = iter.next();
			if (set.add(element))
				newList.add(element);
		}
		arlList.clear();
		arlList.addAll(newList);
	}




}
