package net.bioclipse.ds;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.business.IDSManager;

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

  private ServiceTracker finderTracker;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
    finderTracker = new ServiceTracker( context, 
                                        IDSManager.class.getName(), 
                                        null );
    finderTracker.open();

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
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

  public IDSManager getManager() {
          IDSManager manager = null;
          try {
              manager = (IDSManager) finderTracker.waitForService(1000*10);
          } catch (InterruptedException e) {
              logger.warn("Exception occurred while attempting to get the DSManager" + e);
              LogUtils.debugTrace(logger, e);
          }
          if(manager == null) {
              throw new IllegalStateException("Could not get the DS manager");
          }
          return manager;
      }

}
