package net.bioclipse.ds.sdk;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "net.bioclipse.ds.sdk";
	private static Activator sharedInstance;
	
	public Activator() {}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		sharedInstance = this;
	}

	public void stop(BundleContext context) throws Exception {
		sharedInstance = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return sharedInstance;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path to the image
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}