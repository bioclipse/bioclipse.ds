/* *****************************************************************************
 * Copyright (c) 2010 Ola Spjuth
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.signatures.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.osgi.framework.AllServiceListener;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.signatures.business.ISignaturesManager;
import net.bioclipse.ds.signatures.prop.calc.AtomSignatures;

/**
 * A handler that can convert SMILES files into SDFiles.
 * A SMIELS file is expected to have a header line with property names, 
 * and data lines should start with a SMILES string, and have 
 * properties separated by either ',','\t', or ' '. Properties are also 
 * stored in the SDF, with header names as identifiers.
 * 
 * @author ola
 */
public class SignaturesDatasetFromSDF extends AbstractHandler{

	private static final Logger logger =
		Logger.getLogger(SignaturesDatasetFromSDF.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection sel=HandlerUtil.getCurrentSelection(event);
		if (!(sel instanceof IStructuredSelection))
			throw new ExecutionException("Selection is not an SDF file");

		IStructuredSelection ssel=(IStructuredSelection)sel;
		
		//We onlu operate on a single file currently
		Object obj = ssel.getFirstElement();
		
		if (!(obj instanceof IFile)) 
			throw new ExecutionException("Selection is not an SDF file");

		final IFile file = (IFile) obj;
		
		//Select property for response value
		//TODO
		
		Job job=new Job("Calculating Signatures Dataset"){

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Signatures creation", 10);
				monitor.subTask("Reading SD-file..");
				ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
				List<ICDKMolecule> mols;
				try {
					mols = cdk.loadMolecules(file, new SubProgressMonitor(monitor, 1));

				if (mols==null || mols.size()<=0)
					return new Status(IStatus.ERROR, 
							net.bioclipse.ds.signatures.Activator.PLUGIN_ID, 
							"Error reading SDfile: no molecules. ");
				
				ISignaturesManager signatures=net.bioclipse.ds.signatures.Activator.getDefault().getJavaSignaturesManager();

				List<String> allSignaturesList=new ArrayList<String>();
				List<List<Integer>> dataset=new ArrayList<List<Integer>>();
				List<String> names = new ArrayList<String>();

				int i=0;
				//Process the mols
				for (IMolecule mol : mols){
					AtomSignatures molsigns = signatures.generateChiral(mol,1);
					List<Integer> row = new ArrayList<Integer>();
					dataset.add(row);
					names.add("nr: " + i + ": ");
					i++;
					
					//Loop over all already stored signatures
					for (String sign : allSignaturesList){
						//Count occurrences
						int nohits=Collections.frequency(molsigns.getSignatures(), sign);
						row.add(nohits);
						
						//remove all occurrences in molsigns - we have now processed this list of signatures
						while (molsigns.getSignatures().contains(sign))
							molsigns.getSignatures().remove(sign);
					}
					
					//process the new signs
					List<String> duplicateSigns=new ArrayList<String>();
					for (String sign : molsigns.getSignatures()){

						//If already processed, just take next
						if (duplicateSigns.contains(sign)) continue;

						//Count occurrences
						int nohits=Collections.frequency(molsigns.getSignatures(), sign);
						if (nohits>1)  //If duplicates...
							duplicateSigns.add(sign);

						//Add the new sign to allSignaturesList
						row.add(nohits);
						allSignaturesList.add(sign);
					}
					
				}
				
				//Fill up with zeros to length
				for (List<Integer> row : dataset){
					while (row.size()<allSignaturesList.size())
						row.add(0);
				}
				
				//debug it out
				System.out.println("== ds ==");
				
				System.out.println(allSignaturesList.toString());
				int c=0;
				for (List<Integer> row : dataset){
					System.out.println(names.get(c) + " " + row.toString().substring(1,row.toString().length()-1));
					c++;
				}
				

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BioclipseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
				//Create output filename

				monitor.done();
				logger.debug("Wrote file" + "");
				return Status.OK_STATUS;

			}};
			job.setUser(true);
			job.schedule();
		
		return null;
	}		


}
