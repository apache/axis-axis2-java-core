package org.apache.axis2.tool.codegen.eclipse.plugin;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class CodegenWizardPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static CodegenWizardPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private static ImageDescriptor wizardImageDescriptor;
	
	/**
	 * The constructor.
	 */
	public CodegenWizardPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.apache.axis2.tool.codegen.resource.Codegen");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static CodegenWizardPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = CodegenWizardPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	public static ImageDescriptor getWizardImageDescriptor(){
	    if (wizardImageDescriptor==null){
	        wizardImageDescriptor =CodegenWizardPlugin.imageDescriptorFromPlugin("Axis2_Codegen_Wizard","icons/asf-feather.gif");
	    }
	    return wizardImageDescriptor;
	}
	
}
