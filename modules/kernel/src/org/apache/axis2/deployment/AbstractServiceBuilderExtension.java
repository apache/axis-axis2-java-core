/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
