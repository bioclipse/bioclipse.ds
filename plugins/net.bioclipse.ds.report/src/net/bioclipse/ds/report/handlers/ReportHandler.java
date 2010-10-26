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
package net.bioclipse.ds.report.handlers;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;


import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.util.FileUtil;
import net.bioclipse.core.util.ImageUtils;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.report.Activator;
import net.bioclipse.ds.report.DSSingleReportModel;
import net.bioclipse.ds.report.ReportHelper;
import net.bioclipse.ds.report.model.TestEndpoint;
import net.bioclipse.ds.ui.utils.PieChartProducer;
import net.bioclipse.ds.ui.views.DSView;
import net.bioclipse.jasper.editor.ReportEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * A handler to handle Report invocation
 * @author ola
 *
 */
public class ReportHandler extends AbstractHandler{

	private static final Logger logger = Logger.getLogger(ReportHandler.class);

    public Object execute( ExecutionEvent event ) throws ExecutionException {


    	//Make sure we either have jobs running or a result
    	if (!DSView.getInstance().isExecuted()){
            logger.debug("DSView not executed.");
            
            MessageDialog.openInformation(
            		HandlerUtil.getActiveShell(event),
                    "Decision support reporting",
                    "\nYou need to execute tests before creating a report.");
            return null;
    	}

        logger.debug("Executing Jasper Report.");

        Job job=new Job("Decision Support Report") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				monitor.beginTask("Creating decision support report", 4);

				monitor.subTask("Waiting for tests and gathering data");
				monitor.worked(1);

				//Collect the data
		        DSSingleReportModel rmodel = 
		        	DSView.getInstance().waitAndReturnReportModel(
		        			new SubProgressMonitor(monitor, 1));
		        
		        if (rmodel==null || rmodel.getQueryMol()==null 
		        		         || rmodel.getEndpoints()==null){
		    		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
    				"Could not get data for report from View.");
		        }

				monitor.subTask("Setting up report data");
				monitor.worked(1);

		        final List beanCollection=ReportHelper.createBeanCollection(rmodel);
		    	final Map parameters=ReportHelper.createParameters(rmodel);
		    	
		    	if (beanCollection==null || beanCollection.size()<=0){
		    		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
		    				"Report BeanCollection is empty.");
//		    		throw new ExecutionException("Report BeanCollection is empty.");
		    	}
		    	if (parameters==null || parameters.size()<=0){
//		    		throw new ExecutionException("Report Parameters is empty.");
		    		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
		    				"Report Parameters is empty.");
		    	}

		    	monitor.subTask("Opening report");
		    	monitor.worked(1);

		    	//We need to run the following in UI thread
		    	Display.getDefault().syncExec(new Runnable() {

		    		@Override
		    		public void run() {

		    			//Create a bogus editor input
		    			IEditorInput input = createEditorInput();

		    			try {

		    				String reportPath = FileUtil.getFilePath("reports/ds-safety.jasper", Activator.PLUGIN_ID);
		    				String basePath = FileUtil.getFilePath("reports/", Activator.PLUGIN_ID);
		    				parameters.put("DS_BASE_PATH",basePath);

		    				IWorkbenchPage page = PlatformUI.getWorkbench()
		    				.getActiveWorkbenchWindow()
		    				.getActivePage();

		    				IEditorPart part = page.openEditor( input ,"net.bioclipse.jasper.report.editor" );
		    				if ( part instanceof ReportEditor ) {
		    					ReportEditor editor = (ReportEditor) part;
		    					editor.openReport(reportPath, parameters, beanCollection);
		    				}

		    			} catch ( Exception e ) {
		    				LogUtils.handleException(e, logger, Activator.PLUGIN_ID);
		    			} 

		    		}
		    	});
		    	
		    	monitor.done();

		    	//All went well
		    	return Status.OK_STATUS;

			}
        };
        job.setUser(true);
		job.schedule();
        
        return null;
    }
    
    //Create a forged editorInput
    private IEditorInput createEditorInput() {
        IStorage storage = new StringStorage("REPORT");
        IEditorInput input = new StringInput(storage);
        return input;
    }



    private void showError(String message) {
        MessageDialog.openError( 
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                "Decision support",
                message);
    }

}


