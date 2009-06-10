package net.bioclipse.ds.birt;
import java.util.Map;

import net.bioclipse.ds.birt.handlers.WrappedBrowserEditor;

import org.eclipse.birt.report.viewer.api.AppContextExtension;
public class MyAppContext extends AppContextExtension{

    public Map getAppContext(Map appContext) {
        Map hm = super.getAppContext(appContext);
        hm.put("PARENT_CLASSLOADER", WrappedBrowserEditor.class.getClassLoader());
        hm.put("webapplication.projectclasspath", "c:/jars/mjo.jar");  
        return hm;      
    }
    public String getName() {
        return "MyAppContext";
    }
}
