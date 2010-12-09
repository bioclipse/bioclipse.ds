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
import java.io.ByteArrayInputStream;
import java.io.File;
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import net.bioclipse.ui.business.IUIManager;

/**
 * A handler that can convert SMILES files into SDFiles.
 * A SMIELS file is expected to have a header line with property names, 
 * and data lines should start with a SMILES string, and have 
 * properties separated by either ',','\t', or ' '. Properties are also 
 * stored in the SDF, with header names as identifiers.
 * 
 * @author ola
 */
public abstract class AbstractSignaturesDatasetFromSDF extends AbstractHandler{

	private static final Logger logger =
		Logger.getLogger(AbstractSignaturesDatasetFromSDF.class);

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

				monitor.beginTask("Signatures creation", 11);
				monitor.subTask("Reading SD-file..");
				ICDKManager cdk = Activator.getDefault().getJavaCDKManager();
				List<ICDKMolecule> mols;
				try {
					mols = cdk.loadMolecules(file, new SubProgressMonitor(monitor, 5));

				if (mols==null || mols.size()<=0)
					return new Status(IStatus.ERROR, 
							net.bioclipse.ds.signatures.Activator.PLUGIN_ID, 
							"Error reading SDfile: no molecules. ");
				
				List<String> allSignaturesList=new ArrayList<String>();
				List<List<Float>> dataset=new ArrayList<List<Float>>();
				List<String> names = new ArrayList<String>();
				
				SubProgressMonitor molmonitor = new SubProgressMonitor(monitor, 5);
				molmonitor.beginTask("Calculating signatures", mols.size());

				int i=0;
				//Process the mols
				for (IMolecule mol : mols){
					AtomSignatures molsigns = generateSignatures(mol);
					List<Float> row = new ArrayList<Float>();
					dataset.add(row);
					i++;
					names.add("Compound " + i + ",");
					
					//Loop over all already stored signatures
					for (String sign : allSignaturesList){
						//Count occurrences
						int nohits=Collections.frequency(molsigns.getSignatures(), sign);
						row.add((float)nohits);
						
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
						row.add((float)nohits);
						allSignaturesList.add(sign);
					}

					//get next molecule
					molmonitor.worked(1);
				}
				
				molmonitor.done();
				
				//Fill up with zeros to length
				for (List<Float> row : dataset){
					while (row.size()<allSignaturesList.size())
						row.add(new Float(0));
				}
				
				//debug it out
				System.out.println("== ds ==");
				
				StringBuffer buf = new StringBuffer();
				buf.append("Compound, " + allSignaturesList.toString() + "\n");
				int c=0;
				for (List<Float> row : dataset){
					buf.append(names.get(c) + " " + row.toString().substring(1,row.toString().length()-1) + "\n");
					c++;
				}

				//Create output file, replace extension with csv
				String filename= file.getName();
				IPath sdpath = file.getFullPath().removeLastSegments(1);
				String apx=getImplSpecificAppendix();
				IPath outpath = new Path(sdpath.append(filename).removeFileExtension().toOSString()+apx).addFileExtension("csv");

				for (int j=1; j<10; j++){
					if (j>1)
						outpath = new Path(sdpath.append(filename).removeFileExtension().toOSString()+apx+" ("+j+")").addFileExtension("csv");
					if (writeFile(outpath, buf.toString().getBytes())){
						break;
					}
					
				}

				file.getParent().refreshLocal(IFile.DEPTH_INFINITE, monitor);

				logger.debug("Wrote file" + outpath);


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

				

				monitor.done();
				return Status.OK_STATUS;

			}


			private boolean writeFile(IPath outpath, byte[] bytes) {

				try {
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();
					IFile file=root.getFile( outpath );

					if (!file.exists()){
						InputStream source = new ByteArrayInputStream(bytes);
						file.create(source, true, new NullProgressMonitor());
						return true;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}

				return false;
			}


		};
		job.setUser(true);
		job.schedule();

		return null;
	}		

	protected abstract String getImplSpecificAppendix();

	protected abstract AtomSignatures generateSignatures(IMolecule mol)
			throws BioclipseException;
	
}
