package org.apache.axis2.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;

/**
 * <p>The Class AbstractServiceBuilderExtension is abstract class that can be used
 * to write new ServiceBuilderExtensions.</p>
 * 
 * @since 1.7.0
 */
public abstract class AbstractServiceBuilderExtension implements ServiceBuilderExtension {

    /** The configuration context. */
    ConfigurationContext configurationContext;

    /** The axis configuration. */
    AxisConfiguration axisConfiguration;

    /**
     * The directory associated with base Deployer of with this
     * AbstractServiceBuilderExtension instance.
     */
    String directory;

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        this.axisConfiguration = this.configurationContext.getAxisConfiguration();
    }

    /**
     * Gets the directory.
     * 
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Sets the directory.
     * 
     * @param directory
     *            the new directory
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * Gets the configuration context.
     * 
     * @return the configuration context
     */
    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    /**
     * Gets the axis configuration.
     * 
     * @return the axis configuration
     */
    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }

}
