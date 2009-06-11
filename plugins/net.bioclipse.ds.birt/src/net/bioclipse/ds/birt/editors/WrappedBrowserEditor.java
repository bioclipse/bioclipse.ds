package net.bioclipse.ds.birt.editors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.ds.birt.Activator;
import net.bioclipse.ds.ui.IDSViewNoCloseEditor;

import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.viewer.ViewerPlugin;
import org.eclipse.birt.report.viewer.utilities.WebViewer;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.osgi.framework.Bundle;


public class WrappedBrowserEditor extends EditorPart implements IDSViewNoCloseEditor{

    private Browser browser;



    @Override
    public void createPartControl( Composite parent ) {

        GridLayout layout = new GridLayout();
        parent.setLayout(layout);

        browser = new Browser(parent, SWT.NONE);

        System.setProperty( "RUN_UNDER_ECLIPSE", "true" );
        WebViewer.startup(browser);

        //Use the contributed context
        //        ViewerPlugin.getDefault( ).getPluginPreferences( ).setValue("APPCONTEXT_EXTENSION_KEY", "QSARAppContext");



        //        browser.setUrl( "http://www.dn.se" );
        GridData gd=new GridData(GridData.FILL_BOTH);
        browser.setLayoutData(gd);


    }

    public void openFile(String file){

        //      WebViewer.display(getReportFile(), WebViewer.HTML, "frameset");
        //        WebViewer.display(getReportFile(), WebViewer.HTML, browser, "run");

        

        WebViewer.display(file, WebViewer.HTML, browser, "run");

    }


    public void openZone(String reportfile){


        try{

            Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
            
            

            URL url = FileLocator.find(bundle, new Path("/bin/net/bioclipse/ds/birt/handlers"), null);
            url = FileLocator.resolve(url);
            String classPath = url.getFile();
//            System.setProperty(EngineConstants.PROJECT_CLASSPATH_KEY, classPath);
//            System.setProperty(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, classPath);
//            System.setProperty(EngineConstants.WEBAPP_CLASSPATH_KEY, classPath);

            
            EngineConfig config = new EngineConfig();
            config.getAppContext( ).put(
                                        EngineConstants.APPCONTEXT_CLASSLOADER_KEY,WrappedBrowserEditor.class.getClassLoader() ); 
            config.getAppContext( ).put(
                                        "PARENT_CLASSLOADER",WrappedBrowserEditor.class.getClassLoader() ); 
            
            IReportEngineFactory factory = (IReportEngineFactory)
            org.eclipse.birt.core.framework.Platform.createFactoryObject
            (IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            IReportEngine engine = factory.createReportEngine(config);

            URL designUrl = FileLocator.find(bundle, new Path("/designs/eQsar.rptdesign"),
                                             null);
            designUrl = FileLocator.resolve(designUrl);
            String file = designUrl.getFile();
            IReportRunnable design = engine.openReportDesign(file);
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

            browser.setText(stream.toString());
            engine.destroy();         

        }catch (Exception e){
            e.printStackTrace();
        }

    }




    public void useScriptedDS(){
        
        
        URL url;
        String jarpath="";
        String designpath ="";
        
        try {
            url = FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID)
                                                  .getEntry("DSEventHandlers.jar"));
            jarpath = url.getFile();
            url = FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID)
                                        .getEntry("ds-single.rptdesign"));
            designpath = url.getFile();
        } catch ( IOException e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        System.out.println("Jar path: " + jarpath);
        System.out.println("Design path: " + designpath);
        
        EngineConfig config = new EngineConfig();

        try{

            //use this to set the resource path   
            //Bundle bundle = org.eclipse.core.runtime.Platform.getBundle("org.eclipse.birt.examples.rcpengine");     
            //URL url = FileLocator.find(bundle, new Path("/resources"), null);
            //String myresourcepath = FileLocator.toFileURL(url).getPath();       
            //config.setResourcePath(myresourcepath);

            // Create the report engine
            IReportEngineFactory factory = (IReportEngineFactory) org.eclipse.birt.core.framework.Platform
            .createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY );
            IReportEngine engine = factory.createReportEngine( config );

            IReportRunnable design = null;
            

            //use this if the report is in the bundle
            //Bundle bundle = org.eclipse.core.runtime.Platform.getBundle("org.eclipse.birt.examples.rcpengine");     
            //URL url = FileLocator.find(bundle, new Path("/reports/TopNPercent.rptdesign"), null);
            //String rpt = FileLocator.toFileURL(url).getPath();

            //add to the classpath
            config.getAppContext().put(EngineConstants.WEBAPP_CLASSPATH_KEY, jarpath);
            config.getAppContext( ).put(
                 EngineConstants.APPCONTEXT_CLASSLOADER_KEY,WrappedBrowserEditor.class.getClassLoader() );
            design = engine.openReportDesign(designpath);

            IRunAndRenderTask task = engine.createRunAndRenderTask(design);

            HTMLRenderOption options = new HTMLRenderOption();

            options = new HTMLRenderOption( );
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            options.setOutputStream(bos);
            options.setOutputFormat("html");

            task.setRenderOption(options);
            task.setParameterValue( "model", "WEE" );
            task.run();
            task.close();

            browser.setText(bos.toString());
            System.out.println("finished");
            //engine.destroy();
        } catch (Exception e) {
            e.printStackTrace();

        } 
        
        
    }
    
    
    public void openNewViewer() throws IOException{
        
        
        Bundle bundle = org.eclipse.core.runtime.Platform.getBundle(
                                                           Activator.PLUGIN_ID); 
        URL url = FileLocator.find(bundle, 
                                new Path("/reports/ds-single.rptdesign"), null);
        String rpt = FileLocator.toFileURL(url).getPath();


//        System.out.println("Jar path: " + jarpath);
        System.out.println("Design path: " + rpt);

        
        //Do new viewer
        ViewerPlugin.getDefault( ).getPluginPreferences( ).setValue("APPCONTEXT_EXTENSION_KEY", "MyAppContext");

        HashMap myparms = new HashMap();
        myparms.put("SERVLET_NAME_KEY", "frameset");
//        myparms.put("SERVLET_NAME_KEY", "run");
        myparms.put("FORMAT_KEY", "html");
        //myparms.put("RESOURCE_FOLDER_KEY", "c:/myresources");
        //myparms.put("ALLOW_PAGE", false);
        //myparms.put("MAX_ROWS_KEY", "500");
        WebViewer.display(rpt, browser, myparms);

        
        
    }
    
    
    /*
     * UNUSED BELOW
     */

    @Override
    public void doSave( IProgressMonitor monitor ) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init( IEditorSite site, IEditorInput input )
    throws PartInitException {
        setSite( site );
        setInput( input );

    }

    @Override
    public void setFocus() {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

}
