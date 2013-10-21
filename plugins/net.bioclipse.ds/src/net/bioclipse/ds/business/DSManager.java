/* *****************************************************************************
 * Copyright (c) 2009-2011 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.business;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.interfaces.IAtomContainer;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.FileUtil;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TopLevel;
import net.bioclipse.ds.model.result.SimpleResult;
import net.bioclipse.jobs.BioclipseJob;
import net.bioclipse.jobs.BioclipseJobUpdateHook;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

/**
 * A Bioclipse Manager Decision Support
 * 
 * @author ola
 */
public class DSManager implements IBioclipseManager {

	private static final Logger logger =Logger.getLogger( DSManager.class );

	private DSBusinessModel dsBusinessModel; 

	public String getManagerName() {
		return "ds";
	}

	private void initialize() throws BioclipseException{

		if (dsBusinessModel==null){
			dsBusinessModel=new DSBusinessModel();
			dsBusinessModel.initialize();
		}
		if (dsBusinessModel==null)
			throw new BioclipseException("Error initializing DS model.");

	}

	public void reInitialize() throws BioclipseException{
		dsBusinessModel=null;
		initialize();
	}


	/**
	 * Get a list of all available tests
	 * @return
	 * @throws BioclipseException
	 */
	public List<String> getTests() throws BioclipseException{

		initialize();

		List<String> testIDS=new ArrayList<String>();
		for (IDSTest test : dsBusinessModel.getTests()){
			testIDS.add( test.getId());
		}

		return testIDS;
	}

	public List<IDSTest> getFullTests() throws BioclipseException{
		initialize();
		return dsBusinessModel.getTests();
	}


	public IDSTest getTest( String testID ) throws BioclipseException {

		if (testID==null)
			throw new BioclipseException(
					"Test: " + testID + " must not be null." );

		initialize();

		for (IDSTest test : dsBusinessModel.getTests()){
			if (testID.equals( test.getId() ))
				return test;
		}

		logger.warn("Test: " + testID + " could not be found.");
		throw new BioclipseException(
				"Test: " + testID + " could not be found." );
	}

	/**
	 * Get a list of all available tests
	 * @return
	 * @throws BioclipseException
	 */
	public List<String> getEndpoints() throws BioclipseException{

		initialize();

		List<String> epIDs=new ArrayList<String>();
		for (Endpoint ep : dsBusinessModel.getEndpoints()){
			epIDs.add( ep.getId());
		}
		return epIDs;
	}

	public List<Endpoint> getFullEndpoints() throws BioclipseException{
		initialize();
		return dsBusinessModel.getEndpoints();
	}

	public List<TopLevel> getFullTopLevels() throws BioclipseException{
		initialize();
		return dsBusinessModel.getToplevels();
	}
	
	
	public Endpoint getEndpoint( String endpointID ) throws BioclipseException {

		if (endpointID==null)
			throw new BioclipseException(
					"Endpoint: " + endpointID + " must not be null." );

		initialize();

		for (Endpoint ep : dsBusinessModel.getEndpoints()){
			if (endpointID.equals( ep.getId() ))
				return ep;
		}

		logger.warn("Endpoint: " + endpointID + " could not be found.");
		throw new BioclipseException(
				"Endpoint: " + endpointID + " could not be found." );
	}


	public void runTest( String testID, IMolecule mol, 
			IReturner<List<? extends ITestResult>> returner, 
			IProgressMonitor monitor) 
	throws BioclipseException{

		ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();

		
		IDSTest test = getTest( testID );
		List<? extends ITestResult> ret=null;
		try{

			//Preprocess the molecule
			ICDKMolecule cdkmol = cdk.asCDKMolecule(mol);

			//Clone the mol
			IAtomContainer clonedAC=null;
			try {
				clonedAC = (IAtomContainer) cdkmol.getAtomContainer().clone();
			} catch (CloneNotSupportedException e) {
				logger.error("Could not clone mol: " + cdkmol);
				return;
			}

			ICDKMolecule clonedMol=new CDKMolecule(clonedAC);
			ret = test.runWarningTest( clonedMol, monitor);

		}catch (Exception e){
			//in case of error...
			LogUtils.debugTrace(logger, e);
			ITestResult er=new SimpleResult("Error: " + e.getMessage(), 
					ITestResult.ERROR);
			er.setDetailedMessage( e.getMessage() );
			List<ITestResult> trlist=new ArrayList<ITestResult>();
			trlist.add( er );
			monitor.done();
			returner.completeReturn( trlist );
			return;
		}

		monitor.done();
		returner.completeReturn( ret );
	}



	/**
	 * Run all tests in an EndPoint in a job.
	 * 
	 * @param endpointID
	 * @param mol
	 * @param returner
	 * @param monitor
	 * @throws BioclipseException
	 */
	public void runEndpoint( String endpointID, IMolecule mol, 
			IReturner<Map<String, List<? extends ITestResult>>> returner, 
			IProgressMonitor monitor) 
	throws BioclipseException{

		getEndpoint(endpointID);

		//Set up result container
		Map<String, List<? extends ITestResult>> ret = 
			new HashMap<String, List<? extends ITestResult>>();

		//Get manager interface, we want to use the AOP
		IDSManager ds = Activator.getDefault().getJavaManager();
		Map<String,BioclipseJob<List<ITestResult>>> jobs = new HashMap<String, BioclipseJob<List<ITestResult>>>();


		//This is done in abstractDSTest + ds.runTest
		
//		ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
//		//Preprocess the molecule
//		ICDKMolecule cdkmol = cdk.asCDKMolecule(mol);
//
//		try {
//			cdkmol = cdk.removeExplicitHydrogens(cdkmol);
//			cdkmol=cdk.perceiveAromaticity(cdkmol);
//			cdkmol=cdk.addImplicitHydrogens(cdkmol);
//		} catch (InvocationTargetException e1) {
//			throw new BioclipseException("Could not preprocess molecule: " + e1.getMessage());
//		} 


		//Loop over all tests in this endpoint and run them
		for (IDSTest test : getEndpoint( endpointID ).getTests()){

//			//Clone the mol
//			IAtomContainer clonedAC=null;
//			try {
//				clonedAC = (IAtomContainer) cdkmol.getAtomContainer().clone();
//			} catch (CloneNotSupportedException e) {
//				//Should not happen
//				logger.error("Could not clone mol: " + cdkmol);
//				continue;
//			}
//			ICDKMolecule clonedMol=new CDKMolecule(clonedAC);


			//Start up a job with the test
			BioclipseJob<List<ITestResult>> job = 
				ds.runTest( test.getId(), mol, 
						new BioclipseJobUpdateHook<List<ITestResult>>(test.getName()));
			jobs.put(test.getId(),job);
			job.schedule();

		}

		//Join all jobs, wait for them to end
		for (String testID : jobs.keySet()){
			try {
				BioclipseJob<List<ITestResult>> job = jobs.get(testID);
				logger.debug("Waiting for test: " + testID);
				job.join();
				logger.debug("  - Test: " + testID + " finished.");
				List<ITestResult> testres  = job.getReturnValue();
				ret.put( testID, testres );

			} catch (InterruptedException e) {
				throw new BioclipseException("Job interrupted");
			}
		}

		monitor.done();
		returner.completeReturn( ret );
	}

	/**
	 * Removes a model. Removes extension and any files. 
	 * 
	 * @param model
	 */
	public void removeModel(IDSTest model, String pluginID){
		
		//Remove from DSBusinessModel
		dsBusinessModel.getTests().remove(model);
		for (Endpoint ep : dsBusinessModel.getEndpoints()){
			if (ep.getTests()!=null){
				ep.getTests().remove(model);
				logger.debug("Removed model " + model.getName() + " from EP " + ep.getName());
			}
		}

		//Remove extensions in models.container plugin
		//=============================================
		File pluginXMLfile;
		try {
			pluginXMLfile = new File(FileUtil.getFilePath("plugin.xml", pluginID));

			Builder parser = new Builder();
			Document doc = parser.build(pluginXMLfile);
			Element root = doc.getRootElement();

			Element extension = null;

			//Find extension in plugin.xml, if exists
			Elements existingExtensions = root.getChildElements("extension");
			if (existingExtensions!=null && existingExtensions.size()>0){
				for (int i=0; i < existingExtensions.size(); i++){
					extension = existingExtensions.get(i);

					//If exists a model with same name, remove it
					Elements existingTests = extension.getChildElements("test");
					if (existingTests!=null && existingTests.size()>0){
						for (int j=0; j < existingTests.size(); j++){
							Element test = existingTests.get(j);
							String testName=test.getAttribute("name").getValue();

							//Remove spaces included by XML serialization
							while(testName.contains("  "))
								testName=testName.replace("  ", " ");

							if (model.getName().equals(testName)){
								test.getParent().removeChild(test);
								logger.debug("Removing existing model extension: " + model.getName());

								//Also remove the files
								Elements resources = test.getChildElements("resource");
								for (int rescnt = 0; rescnt<resources.size(); rescnt++){
									Element resource = resources.get(rescnt);
									String path = resource.getAttribute("path").getValue();
									try{
										File reFile = new File(FileUtil.getFilePath(path, pluginID));
										if (reFile.exists()){
											logger.debug("Removing file: " + reFile);
											reFile.delete();
										}
										else
											logger.debug("Unable to locate file to remove: " + path);
									}catch(Exception e){
										logger.error("Problems removing file: " + path);
									}

								}

							}
						}
					}

				}

			}

			//Serialize the updated plugin.xml to file
			Serializer serializer = new Serializer(new FileOutputStream(pluginXMLfile));
			serializer.setIndent(4);
			serializer.setMaxLength(64);
			serializer.write(doc);  

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		}

	}


}
