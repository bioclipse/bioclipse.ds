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


import net.bioclipse.core.business.BioclipseException;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


/**
 * A handler to handle Report invocation
 * @author ola
 *
 */
public class ReportHandler extends AbstractHandler{

	private static final Logger logger = Logger.getLogger(ReportHandler.class);

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        

        logger.debug("Executing Jasper Report.");

        IWorkbenchPage page = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow()
        .getActivePage();
        
        //Collect the data
        DSSingleReportModel rmodel = DSView.getInstance().waitAndReturnReportModel();
        
        List beanCollection=ReportHelper.createBeanCollection(rmodel);
    	Map parameters=ReportHelper.createParameters(rmodel);
    	
    	if (beanCollection==null || beanCollection.size()<=0){
    		throw new ExecutionException("Report BeanCollection is empty.");
    	}
    	if (parameters==null || parameters.size()<=0){
    		throw new ExecutionException("Report Parameters is empty.");
    	}

        try {

        	//Create a bogus editor input
        	IEditorInput input = createEditorInput();

			String reportPath = FileUtil.getFilePath("reports/ds-safety.jasper", Activator.PLUGIN_ID);
			String basePath = FileUtil.getFilePath("reports/", Activator.PLUGIN_ID);
	        parameters.put("DS_BASE_PATH",basePath);

			IEditorPart part = page.openEditor( input ,"net.bioclipse.jasper.report.editor" );
            if ( part instanceof ReportEditor ) {
            	ReportEditor editor = (ReportEditor) part;
            	editor.openReport(reportPath, parameters, beanCollection);
            }

        } catch ( Exception e ) {
        	LogUtils.handleException(e, logger, Activator.PLUGIN_ID);
        } 

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


