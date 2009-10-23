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

import org.apache.axis2.context.ConfigurationContext;

/**
 * This is the counterpart of {@link ConfigurationManager}. On the message sending side,
 * the ConfigurationManager will send a context replication message, which on the receiving
 * side will be handed over to the implementer of this interface. So when a node sends a message
 * through using its ConfigurationManager, all receivers will be notified through their respective
 * ConfigurationManagerListeners.
 */
public interface ConfigurationManagerListener {

    /**
     * Notification that a load service groups message has been received.
     *
     * @param command The message
     */
    void serviceGroupsLoaded(ConfigurationClusteringCommand command);

    /**
     * Notification that an unload seervice groups message has been received.
     *
     * @param command  The message
     */
    void serviceGroupsUnloaded(ConfigurationClusteringCommand command);

    /**
     * Notification that an apply policy to service message has been received.
     *
     * @param command The message
     */
    void policyApplied(ConfigurationClusteringCommand command);

    /**
     * Notification that a reload configuration message has been received.
     *
     * @param command The message
     */
    void configurationReloaded(ConfigurationClusteringCommand command);

    /**
     * Prepare to commit cpmfiguration changes
     */
    void prepareCalled();

    /**
     * Rollback configuration changes
     */
    void rollbackCalled();

    /**
     * Commit configuration changes
     */
    void commitCalled();

    /**
     * An exception has occurred on a remote node while processing a configuration change command
     *
     * @param throwable The exception that occurred on the remote node
     */
    void handleException(Throwable throwable);

    /**
     * Set the system's configuration context. This will be used by the clustering implementations
     * to get information about the Axis2 environment and to correspond with the Axis2 environment
     *
     * @param configurationContext The configuration context
     */
    void setConfigurationContext(ConfigurationContext configurationContext);
}
