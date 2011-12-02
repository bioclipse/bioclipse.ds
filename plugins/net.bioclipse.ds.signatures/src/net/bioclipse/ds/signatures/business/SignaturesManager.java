/*******************************************************************************
 * Copyright (c) 2010-2011  Ola Spjuth <ola@bioclipse.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.ds.signatures.business;

import java.awt.Point;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.DenseDataset;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.SparseDataset;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.signatures.prop.calc.AtomSignatures;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.AtomSignature;
import org.openscience.cdk.signature.MoleculeSignature;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;


/**
 * 
 * @author ola
 */
public class SignaturesManager implements IBioclipseManager {

	private static final Logger logger = Logger.getLogger(
			SignaturesManager.class);
	private static final int SIGNATURES_DEFAULT_HEIGHT = 1;

	/**
	 * Gives a short one word name of the manager used as variable name when
	 * scripting.
	 */
	public String getManagerName() {
		return "signatures";
	}

	/**
	 * Use default height (1).
	 * @param mol
	 * @return
	 * @throws BioclipseException
	 */
	public AtomSignatures generate(IMolecule mol) 
	throws BioclipseException{
		return generate( mol, SIGNATURES_DEFAULT_HEIGHT );
	}

	public AtomSignatures generate(IMolecule mol, int height) 
	throws BioclipseException{

		//Adapt IMolecule to ICDKMolecule
		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
		ICDKMolecule cdkmol = cdk.asCDKMolecule(mol);


		List<String> signatureString=new ArrayList<String>();

		//Create one signature per atom of height h
		for ( int atomNr = 0; atomNr < cdkmol.getAtomContainer().getAtomCount(); atomNr++){
			AtomSignature gensign = new AtomSignature(atomNr, height, cdkmol.getAtomContainer());
			signatureString.add( gensign.toCanonicalString());
			//    		logger.debug("Sign for atom " + atomNr + ": " +gensign);
		}

		//Test correct number of produced signatures (should not be an issue)
		if (signatureString.size()!=cdkmol.getAtomContainer().getAtomCount())
			throw new BioclipseException("Number of atoms and atom signatures " +
					"differ for molecule: " + cdkmol +" ; Number of atoms: " + 
					cdkmol.getAtomContainer().getAtomCount() + 
					" and number of signatures produced: " 
					+ signatureString.size());

		return new AtomSignatures(signatureString);

	}

	/**
	 * Generate Signatures for a list of molecules.
	 * @param mols List of IMoleculs
	 * @return list of Signatures
	 */
	public Map<IMolecule, AtomSignatures> generate(List<? extends IMolecule> mols, IProgressMonitor monitor){
		return generate(mols, SIGNATURES_DEFAULT_HEIGHT, monitor);
	}

	/**
	 * Generate AtomSignatures for a list of molecules.
	 * @param mols List of IMoleculs
	 * @param height Signatures height
	 * @return Map from IMolecule to AtomSignatures
	 */
	public Map<IMolecule, AtomSignatures> generate(List<? extends IMolecule> mols, 
			int height, IProgressMonitor monitor){

		Map<IMolecule, AtomSignatures> retmap = 
			new HashMap<IMolecule, AtomSignatures>();

		monitor.beginTask("Calculating signatures", mols.size());

		int cnt=0;
		for (IMolecule mol : mols){
			try {

				AtomSignatures as = generate( mol , height);

				if (as.getSignatures() == null || as.getSignatures().size()<=0)
					logger.error( "No signatures generated for for molecule: " 
							+ mol + ".  Skipping this entry." );
				else
					//All is well
					retmap.put( mol, as );


			} catch ( BioclipseException e ) {
				logger.error( "Error generating signatures for molecule: " 
						+ mol + ".  Skipping this entry. " +
						"Reason: " + e.getMessage() );
			}

			monitor.worked(1);
			cnt++;
			if (cnt%100==0){
				monitor.subTask("Generating signatures: " + cnt + "/" + mols.size() + " molecules");
			}
		}

		monitor.done();

		return retmap;
	}


	public Map<IMolecule, AtomSignatures> generate(IFile file, IProgressMonitor monitor) 
	throws BioclipseException, CoreException, IOException{
		return generate( file, SIGNATURES_DEFAULT_HEIGHT , monitor);
	}

	public Map<IMolecule, AtomSignatures> generate(IFile file, int height, IProgressMonitor monitor) 
	throws BioclipseException, CoreException, IOException{

		//Adapt IMolecule to ICDKMolecule
		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
		List<ICDKMolecule> mols = cdk.loadMolecules(file);

		return generate(mols, height, monitor);
	}


	public String generateMoleculeSignature( IMolecule mol )
	throws BioclipseException{

		//Adapt IMolecule to ICDKMolecule
		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
		ICDKMolecule cdkmol = cdk.asCDKMolecule(mol);

		cdkmol=cdk.perceiveAromaticity(cdkmol);
		try {
			cdkmol=cdk.addImplicitHydrogens(cdkmol);
		} catch (InvocationTargetException e) {
			throw new BioclipseException("Error adding implicit hydrogens: " 
					+ e.getMessage());
		}


		MoleculeSignature signature = new MoleculeSignature(cdkmol.getAtomContainer());
		return signature.toCanonicalString();

	}



	/*    
    public AtomSignatures generateChiral(IMolecule molecule, int height) 
    throws BioclipseException, CDKException{

    	//Step 1: generate 3D with balloon
    	ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
    	if (!cdk.has3d(molecule)){
        	IBalloonManager ballon = net.bioclipse.balloon.business.Activator.getDefault().getJavaBalloonManager();
        	molecule = ballon.generate3Dcoordinates(molecule);
    	}

    	List<String> signatures = CalculateChiralSignatures.generate(
    			cdk.getMDLMolfileString(molecule), height);
    	System.out.println("Chiral signs: " + signatures.toString());

    	return new AtomSignatures(signatures);
    }
	 */

	public DenseDataset generateDataset(List<? extends IMolecule> mols, int height, IProgressMonitor monitor){
		return generateDataset(mols, height, null, null, monitor);
	}

	public DenseDataset generateDataset(List<? extends IMolecule> mols, int height, String nameProperty, 
								String responseProperty, IProgressMonitor monitor){

		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();

		monitor.beginTask("Generating signatures dataset", 2);
		
		standardizeMolecules(mols);
		
		//Generate all signatures
		Map<IMolecule, AtomSignatures> signMap = generate(mols, height, new SubProgressMonitor(monitor, 1));

		//Add all atom signatures to a unique list
		List<String> allSignaturesList=new ArrayList<String>();
		for (IMolecule mol : signMap.keySet()){

				for (String s : signMap.get(mol).getSignatures()){
					if (!allSignaturesList.contains(s))
						allSignaturesList.add(s);
				}

		}

		//Set up the dataset
		List<List<Float>> dataset=new ArrayList<List<Float>>();
		List<String> names = new ArrayList<String>();
		List<String> responseValues = new ArrayList<String>();
		
		
		//Process one mol at a time and count frequency for each signature
		SubProgressMonitor submon = new SubProgressMonitor(monitor, 1);
		submon.beginTask("Counting signature occurences", signMap.keySet().size());
		int i=0;
		for (IMolecule mol : signMap.keySet()){

			AtomSignatures molsigns = signMap.get(mol);
			List<Float> row = new ArrayList<Float>();
			dataset.add(row);
			i++;
			if (i%100==0){
				monitor.subTask("Processed " + i + "/" + signMap.keySet().size() + " molecules");
				System.out.println("Processed " + i + "/" + signMap.keySet().size() + " molecules");
			}

			//Handle name of molecule
			String name = null;
			if (nameProperty!=null){
				try {
					name = (String) cdk.asCDKMolecule(mol).getProperty(nameProperty, null);
				} catch (BioclipseException e) {
					logger.error(e.getMessage());
				}
			}
			if (name==null){
				name="Compound-" + i;
			}
			names.add(name);

			//Loop over all already stored signatures and count frequency
			for (String sign : allSignaturesList){
				//Count occurrences
				int nohits=Collections.frequency(molsigns.getSignatures(), sign);
				row.add((float)nohits);

				//remove all occurrences in molsigns - we have now processed this list of signatures
				while (molsigns.getSignatures().contains(sign))
					molsigns.getSignatures().remove(sign);
			}
			
			//Handle response values
			if (responseProperty!=null){
				try {
					String response = (String) cdk.asCDKMolecule(mol).getProperty(responseProperty, null);
					responseValues.add(response);
				} catch (BioclipseException e) {
					logger.error(e.getMessage());
				}
			}
			submon.worked(1);

		}

		monitor.subTask("Setting up dataset");
		DenseDataset ds = new DenseDataset(allSignaturesList, names, dataset, responseProperty, responseValues);
		
		submon.done();
		monitor.done();

		return ds;

	}
	
	
	public SparseDataset generateSparseDataset(List<? extends IMolecule> mols, int height, IProgressMonitor monitor){
		return generateSparseDataset(mols, height, null, null, monitor);
	}

	public SparseDataset generateSparseDataset(List<? extends IMolecule> mols, int height, String nameProperty, 
								String responseProperty, IProgressMonitor monitor){

		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();

		monitor.beginTask("Generating signatures dataset", 2);
		
		mols=standardizeMolecules(mols);
		
		//Generate all signatures
		Map<IMolecule, AtomSignatures> signMap = generate(mols, height, new SubProgressMonitor(monitor, 1));

		//Add all atom signatures to a unique list
		List<String> allSignaturesList=new ArrayList<String>();
		for (IMolecule mol : signMap.keySet()){

				for (String s : signMap.get(mol).getSignatures()){
					if (!allSignaturesList.contains(s))
						allSignaturesList.add(s);
				}

		}

		//Set up the dataset
		LinkedHashMap<Point, Integer> values = new LinkedHashMap<Point, Integer>();
		List<String> names = new ArrayList<String>();
		List<String> responseValues = new ArrayList<String>();
		
		//Process one mol at a time and count frequency for each signature
		SubProgressMonitor submon = new SubProgressMonitor(monitor, 1);
		submon.beginTask("Counting signature occurences", signMap.keySet().size());
		int moleculeNo=1;  //line number = matrix row number, starts on 1
		for (IMolecule mol : signMap.keySet()){

			AtomSignatures molsigns = signMap.get(mol);
			ICDKMolecule cdkmol =null;
			try {
				cdkmol = cdk.asCDKMolecule(mol);
			} catch (BioclipseException e) {
				LogUtils.debugTrace(logger, e);
				continue;
			}
			

			//Handle name of molecule
			String name = null;
			if (nameProperty!=null){
					name = (String) cdkmol.getProperty(nameProperty, null);
			}
			if (name==null){
				name="Compound-" + moleculeNo;
			}
			names.add(name);

			//Loop over all already stored signatures and count frequency
			for (String sign : allSignaturesList){
				//Count occurrences
				int nohits=Collections.frequency(molsigns.getSignatures(), sign);
				
				//SPARSE, so only store if value > 0
				if (nohits>0){
					values.put(new Point(moleculeNo, allSignaturesList.indexOf(sign)+1), nohits);
				}
				
				//remove all occurrences in molsigns - we have now processed this signature
				//if this is not done, could result in duplicates since same sign could occur
				//many times in a mol
				while (molsigns.getSignatures().contains(sign))
					molsigns.getSignatures().remove(sign);
			}
			
			//Handle response values
			if (responseProperty!=null){
				String response = (String) cdkmol.getProperty(responseProperty, null);
				responseValues.add(response);
			}
			submon.worked(1);
			
			moleculeNo++;
			if (moleculeNo%100==0){
				monitor.subTask("Counting frequency: " + moleculeNo + "/" + signMap.keySet().size() + " molecules");
//				System.out.println("Processed " + moleculeNo + "/" + signMap.keySet().size() + " molecules");
			}


		}

		monitor.subTask("Setting up dataset");
		SparseDataset ds = new SparseDataset(allSignaturesList, names, responseProperty, responseValues,values);
		
		submon.done();
		monitor.done();

		return ds;

	}
	

	public static List<ICDKMolecule> standardizeMolecules(List<? extends IMolecule> mols){
		
		ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
		List<ICDKMolecule> standardizedMols=new ArrayList<ICDKMolecule>();
		
		int c=0;
		for (IMolecule mol : mols){
			try {
				ICDKMolecule cdkmol = cdk.asCDKMolecule(mol);
				standardizeMolecule(cdkmol.getAtomContainer());
				standardizedMols.add(cdkmol);
			} catch (BioclipseException e) {
				logger.error("Error standardizing molecule: " + c + ": " + e.getMessage());
			} catch (CDKException e) {
				logger.error("Error standardizing molecule: " + c + ": " + e.getMessage());
			}
			c++;
		}
		
		return standardizedMols;
		
	}
	public static IAtomContainer standardizeMolecule(IAtomContainer mol) throws CDKException{

		//Remove explicit hydrogens
		for (int i=mol.getAtomCount()-1; i>=0; i--) {
			IAtom atom = mol.getAtom(i);
			if ("H".equals(atom.getSymbol())) {
				mol.removeAtomAndConnectedElectronContainers(atom);
			}
		}

		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		CDKHueckelAromaticityDetector.detectAromaticity(mol);

		CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(mol.getBuilder());
		hAdder.addImplicitHydrogens(mol);

		return mol;

	}

}
