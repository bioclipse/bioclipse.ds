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
package net.bioclipse.ds;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.business.IJavaDSManager;
import net.bioclipse.ds.business.IJavaScriptDSManager;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

  	// The plug-in ID
  	public static final String PLUGIN_ID = "net.bioclipse.ds";
  
  	// The shared instance
  	private static Activator plugin;
  	
    private static final Logger logger = Logger.getLogger(Activator.class);
  
    private ServiceTracker javaDsManagerTracker;
    private ServiceTracker javaScriptDsManagerTracker;
  
  	/**
  	 * The constructor
  	 */
  	public Activator() {
  	}
  
  	public void start(BundleContext context) throws Exception {
  		super.start(context);
  		plugin = this;
  		
      javaDsManagerTracker = new ServiceTracker( 
          context, 
          IJavaDSManager.class.getName(), 
          null 
      );
      javaDsManagerTracker.open();
      
      javaScriptDsManagerTracker = new ServiceTracker( 
          context, 
          IJavaScriptDSManager.class.getName(), 
          null 
      );
      javaScriptDsManagerTracker.open();
  	}
  
  	public void stop(BundleContext context) throws Exception {
  		plugin = null;
  		super.stop(context);
  	}
  
  	/**
  	 * Returns the shared instance
  	 *
  	 * @return the shared instance
  	 */
  	public static Activator getDefault() {
  		return plugin;
  	}
  
    public static ImageDescriptor getImageDecriptor(String path){
        return imageDescriptorFromPlugin( PLUGIN_ID, path );
    }
  
    public IDSManager getJavaManager() {
        IDSManager manager = null;
        try {
            manager = (IDSManager) javaDsManagerTracker.waitForService(1000*10);
        } catch (InterruptedException e) {
            logger.warn( 
            "Exception occurred while attempting to get the DSManager" + e);
            LogUtils.debugTrace(logger, e);
        }
        if(manager == null) {
            throw new IllegalStateException("Could not get the DS manager");
        }
        return manager;
    }
    
    public IDSManager getJavaScriptManager() {
        IDSManager manager = null;
        try {
            manager = (IDSManager) 
                      javaScriptDsManagerTracker.waitForService(1000*10);
        } catch (InterruptedException e) {
            logger.warn( 
            "Exception occurred while attempting to get the DSManager" + e);
            LogUtils.debugTrace(logger, e);
        }
        if(manager == null) {
            throw new IllegalStateException("Could not get the DS manager");
        }
        return manager;
    }
}
