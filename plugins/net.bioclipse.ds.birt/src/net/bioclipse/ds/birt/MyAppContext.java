package net.bioclipse.ds.birt;
import java.util.Map;


import net.bioclipse.ds.birt.editors.WrappedBrowserEditor;

import org.eclipse.birt.report.viewer.api.AppContextExtension;
public class MyAppContext extends AppContextExtension{

    @Override
    public Map getAppContext(Map appContext) {

      Map hm = super.getAppContext(appContext);
      hm.put("PARENT_CLASSLOADER", WrappedBrowserEditor.class.getClassLoader());
      return hm;
    
    }

    @Override
    public String getName() {
      // TODO Auto-generated method stub
      return "MyAppContext";
    }

  }