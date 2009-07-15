/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
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
      return "MyAppContext";
    }

  }
