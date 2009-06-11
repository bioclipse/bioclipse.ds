package net.bioclipse.ds.birt.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import net.bioclipse.ds.birt.Activator;
import net.bioclipse.ds.birt.editors.WrappedBrowserEditor;
import net.bioclipse.ds.birt.util.StringInput;
import net.bioclipse.ds.birt.util.StringStorage;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
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
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.FileEditorInput;

/**
 * A handler to handle Report invocation
 * @author ola
 *
 */
public class ReportHandler extends AbstractHandler{

    public Object execute( ExecutionEvent event ) throws ExecutionException {


        System.out.println("REPOOOOOOORT!");
//        openScriptedReportInBrowser();
//        openWebViewer();
        

        IWorkbenchPage page = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow()
        .getActivePage();

        IEditorInput input = createEditorInput();
        
        try {
            IEditorPart part = page.openEditor( input ,"net.bioclipse.ds.birt.editor" );
            if ( part instanceof WrappedBrowserEditor ) {
                WrappedBrowserEditor editor = (WrappedBrowserEditor) part;
//                editor.openFile( "/Users/ola/Workspaces/workspaceBIRT/ola/eQsar.rptdesign" );
//                editor.openZone( "" );
//                editor.useScriptedDS();
                try {
                    editor.openNewViewer();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
            

        } catch ( PartInitException e ) {
            e.printStackTrace();
        }


        return null;
    }
    
    private IEditorInput createEditorInput() {
        IStorage storage = new StringStorage("WEEE");
        IEditorInput input = new StringInput(storage);
        return input;
    }



    public static void openScriptedReportInBrowser( ) {
        try {

            String reportFile = "/Users/ola/Workspaces/workspaceBIRT/ola/eQsar.rptdesign";

            System.setProperty( "RUN_UNDER_ECLIPSE", "true" );

            EngineConfig config = new EngineConfig();
            HashMap hm = config.getAppContext();
            hm.put( EngineConstants.APPCONTEXT_CLASSLOADER_KEY, Platform.getContextClassLoader());
            hm.put( EngineConstants.WEBAPP_CLASSPATH_KEY, Platform.getContextClassLoader());
            hm.put("PARENT_CLASSLOADER", Platform.getContextClassLoader());
            //            hm.put("APPCONTEXT_EXTENSION_KEY", "QSARAppContext");
            config.setAppContext(hm); 

            // Create the report engine
            IReportEngineFactory factory = (IReportEngineFactory) Platform
            .createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY );
            IReportEngine engine = factory.createReportEngine( config );

            // Open an existing report design
            IReportRunnable design = engine.openReportDesign(reportFile);

            //================================================
            ReportDesignHandle designHandle = design.getDesignHandle().getDesignHandle();
            ElementFactory elementFactory = designHandle.getElementFactory( );

            // Create task to run the report - use the task to execute and run
            // the report,
            IRunAndRenderTask task = engine.createRunAndRenderTask(design);

            // Set Render context to handle url and image locataions

            HTMLRenderContext renderContext = new HTMLRenderContext();
            renderContext.setImageDirectory("image");
            HashMap contextMap = new HashMap();
            contextMap.put(EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT,
                           renderContext);
            task.setAppContext(contextMap);

            // Set rendering options - such as file or stream output,
            // output format, whether it is embeddable, etc
            IRenderOption options = new HTMLRenderOption();
            options.setOutputFileName("/tmp/reportWEEeQSAR2.html");
            options.setOutputFormat("html");
            task.setRenderOption(options);

            // run the report and destroy the engine
            task.run();

            engine.destroy();
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }


    public static void openWebViewer() {


        IWorkbenchBrowserSupport support =
            PlatformUI.getWorkbench().getBrowserSupport();
        try
        {
            IWebBrowser browser = support.createBrowser("birt.browser");       
            //            browser.openURL(new URL("https://www.google.com"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        //Use the contributed context
//        ViewerPlugin.getDefault( ).getPluginPreferences( ).setValue("APPCONTEXT_EXTENSION_KEY", "QSARAppContext");

        String reportFile = "/Users/ola/Workspaces/workspaceBIRT/ola/eQsar.rptdesign";
        System.setProperty( "RUN_UNDER_ECLIPSE", "true" );
        WebViewer.display(reportFile, WebViewer.HTML, "frameset");

    }

}


