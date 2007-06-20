/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.clustering;

import org.apache.axis2.clustering.configuration.ConfigurationManager;
import org.apache.axis2.clustering.context.ContextManager;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.context.ConfigurationContext;

public interface ClusterManager extends ParameterInclude {

    /**
     * Initialize the ClusteManager
     *
     * @throws ClusteringFault
     */
    public void init() throws ClusteringFault;

    /**
     *
     * @return The ContextManager
     */
    public ContextManager getContextManager ();

    /**
     *
     * @return The ConfigurationManager
     */
    public ConfigurationManager getConfigurationManager ();

    /**
     *
     * @param contextManager
     */
    public void setContextManager (ContextManager contextManager);

    /**
     *
     * @param configurationManager
     */
    public void setConfigurationManager (ConfigurationManager configurationManager);

    /**
     *
     * @throws ClusteringFault
     */
    public void shutdown () throws ClusteringFault;

    /**
     * Set the configuration context
     *
     * @param configurationContext
     */
    void setConfigurationContext(ConfigurationContext configurationContext);

}