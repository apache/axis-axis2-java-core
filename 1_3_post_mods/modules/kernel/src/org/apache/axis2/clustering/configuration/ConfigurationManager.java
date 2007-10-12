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

package org.apache.axis2.clustering.configuration;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.ParameterInclude;

public interface ConfigurationManager extends ParameterInclude {

    // ###################### Configuration management methods ##########################
    /**
     * Load a set of service groups
     *
     * @param serviceGroupNames The set of service groups to be loaded
     * @throws ClusteringFault
     */
    void loadServiceGroups(String[] serviceGroupNames) throws ClusteringFault;

    /**
     * Unload a set of service groups
     *
     * @param serviceGroupNames The set of service groups to be unloaded
     * @throws ClusteringFault
     */
    void unloadServiceGroups(String[] serviceGroupNames) throws ClusteringFault;

    /**
     * Apply a policy to a service
     *
     * @param serviceName The name of the service to which this policy needs to be applied
     * @param policy      The serialized policy to be applied to the service
     * @throws ClusteringFault
     */
    void applyPolicy(String serviceName, String policy) throws ClusteringFault;

    /**
     * Reload the entire configuration of an Axis2 Node
     *
     * @throws ClusteringFault
     */
    void reloadConfiguration() throws ClusteringFault;

    // ###################### Transaction management methods ##########################

    /**
     * First phase of the 2-phase commit protocol.
     * Notifies a node that it needs to prepare to switch to a new configuration.
     *
     * @throws ClusteringFault
     */
    void prepare() throws ClusteringFault;

    /**
     * Rollback whatever was done
     *
     * @throws ClusteringFault
     */
    void rollback() throws ClusteringFault;

    /**
     * Second phase of the 2-phase commit protocol.
     * Notifies a node that it needs to switch to a new configuration.
     *
     * @throws ClusteringFault
     */
    void commit() throws ClusteringFault;

    // ######################## General management methods ############################
    /**
     * To notify other nodes that an Exception occurred, during the processing
     * of a {@link ConfigurationClusteringCommand}
     *
     * @param throwable The throwable which has to be propogated to other nodes
     */
    void exceptionOccurred(Throwable throwable) throws ClusteringFault;

    /**
     * For registering a configuration event listener.
     */
    void setConfigurationManagerListener(ConfigurationManagerListener listener);

    /**
     * Set the configuration context
     *
     * @param configurationContext
     */
    void setConfigurationContext(ConfigurationContext configurationContext);
}