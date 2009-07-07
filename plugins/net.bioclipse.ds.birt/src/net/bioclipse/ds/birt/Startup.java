package net.bioclipse.ds.birt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.viewer.ViewerPlugin;
import org.eclipse.birt.report.viewer.utilities.WebViewer;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;


public class Startup implements IStartup {

    Browser browser;

    public void earlyStartup() {

        Display.getDefault().syncExec( new Runnable(){

            public void run() {
                final Shell shell = new Shell(Display.getDefault());        
                browser=new Browser(shell, SWT.NONE);
            }} );




        //Start up a background job for starting BIRT
        Job loadBirtJob=new Job("Starting BIRT engine"){

            protected IStatus run2(IProgressMonitor monitor) {
                monitor.beginTask( "Starting BIRT ", 3 );
                monitor.worked( 1 );

                WebViewer.startup();

                EngineConfig config = new EngineConfig();
                IReportEngineFactory factory = (IReportEngineFactory) org.eclipse.birt.core.framework.Platform
                .createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY );
                IReportEngine engine = factory.createReportEngine( config );

                Bundle bundle = org.eclipse.core.runtime.Platform.getBundle(
                                                                            Activator.PLUGIN_ID); 
                URL url = FileLocator.find(bundle, 
                                           new Path("/reports/ds-single.rptdesign"), null);
                String rpt;
                try {
                    rpt = FileLocator.toFileURL(url).getPath();
                    IReportRunnable design = engine.openReportDesign(rpt);

                    //================================================
                    ReportDesignHandle designHandle = design.getDesignHandle().getDesignHandle();
                    ElementFactory elementFactory = designHandle.getElementFactory( );

                    // Create task to run the report - use the task to execute and run
                    // the report,
                    IRunAndRenderTask task = engine.createRunAndRenderTask(design);


                    HTMLRenderContext renderContext = new HTMLRenderContext();
                    Map contextMap = new HashMap();
                    contextMap.put(EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT,
                                   renderContext);
                    task.setAppContext(contextMap);

                    IRenderOption options = new HTMLRenderOption();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    options.setOutputStream(stream);
                    options.setOutputFormat("html");

                    task.setRenderOption(options);
                    task.run();
                    task.close();


                } catch ( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch ( EngineException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                return Status.OK_STATUS;
            }

            @Override
            protected IStatus run( IProgressMonitor monitor ) {

                monitor.beginTask( "Starting BIRT ", 3 );
                monitor.worked( 1 );

                WebViewer.startup();

                Bundle bundle = org.eclipse.core.runtime.Platform.getBundle(
                                                                            Activator.PLUGIN_ID); 
                URL url = FileLocator.find(bundle, 
                                           new Path("/reports/empty.rptdesign"), null);
                final String rpt;
                try {
                    rpt = FileLocator.toFileURL(url).getPath();

                    //Do new viewer
                    ViewerPlugin.getDefault( ).getPluginPreferences( ).setValue("APPCONTEXT_EXTENSION_KEY", "MyAppContext");

                    final HashMap myparms = new HashMap();
                    myparms.put("SERVLET_NAME_KEY", "frameset");
                    myparms.put("FORMAT_KEY", "html");

                    monitor.worked( 1 );

                    Display.getDefault().syncExec( new Runnable(){

                        public void run() {
                            WebViewer.display(rpt, browser, myparms);
                        }} );


                } catch ( IOException e ) {
                    e.printStackTrace();
                }

                monitor.done();
                return Status.OK_STATUS;
            }

        };
        loadBirtJob.setUser( false );
        loadBirtJob.schedule();


    }

}
