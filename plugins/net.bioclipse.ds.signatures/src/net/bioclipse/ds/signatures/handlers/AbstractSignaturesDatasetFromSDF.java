/* *****************************************************************************
 * Copyright (c) 2011 Ola Spjuth
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.signatures.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IDataset;

/**
 * An abstract handler to generate signature datasets from SDF
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
		
		//Read mol 1 into mem and extract properties
		List<String> availableProperties = new ArrayList<String>();
		try {
			IteratingMDLReader reader = new IteratingMDLReader(file.getContents(), NoNotificationChemObjectBuilder.getInstance());
			IMolecule mol = (IMolecule)reader.next();
			for (Object p : mol.getProperties().keySet()){
				availableProperties.add((String) p);
			}
			
		} catch (CoreException e1) {
			throw new ExecutionException("Could not parse molecules from input file");
		}
		
		SignaturesDatasetParametersDialog dlg = new SignaturesDatasetParametersDialog(
												HandlerUtil.getActiveShell(event),
												file.getParent(), getFileExtension(), 
												availableProperties);

		//Input dialog regarding height, name, and select property for response value
		int ret = dlg.open();
		if (ret==Window.CANCEL) return null;
		final SignaturesDatasetModel datasetModel = dlg.getDatasetModel();
		
//		System.out.println("height is " + datasetModel.getHeight());
//		System.out.println("responseProp is " + datasetModel.getResponseProperty());
//		System.out.println("nameProp is " + datasetModel.getNameProperty());
//		System.out.println("File is " + datasetModel.getNewFile());

		
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
				
				//Generate the dataset using the signatures manager
				IDataset ds = generateDataset(mols, datasetModel.getHeight(), datasetModel.getNameProperty(), datasetModel.getResponseProperty(), monitor);

				System.out.println("File: " + datasetModel.getNewFile());
				
				//Create output file, replace extension with dataset-specific
				IContainer folder = file.getParent();
				IPath outpath = folder.getFullPath().append(new Path(datasetModel.getNewFile()));
				for (int j=1; j<10; j++){
					if (j>1)
						outpath = new Path(outpath.removeFileExtension().toOSString()+" ("+j+")").addFileExtension(ds.getFileExtension());
					if (writeFile(outpath, ds.getFileContents().getBytes())){
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

	protected abstract  IDataset generateDataset(List<ICDKMolecule> mols, int height,
			String nameProperty, String responseProperty,
			IProgressMonitor monitor);

	protected abstract String getFileExtension();

}
