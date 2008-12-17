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

package org.apache.axis2.clustering.context;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;

/**
 * This is the countepart of the {@link ContextManager} implementation. On the message sending side,
 * the ContextManager will send a context replication message, which on the receiving
 * side will be handed over to the implementer of this interface. So when a node sends a message
 * through using its ContextManager, all receivers will be notified through their respective
 * ContextManagerListeners.
 */
public interface ContextManagerListener {

    /**
     * Notification that a context replication message has been received. The receiver will have to
     * take appropriate action to process this message
     *
     * @param message The context replication message
     * @throws ClusteringFault If an error occurs while processing the received message
     */
    public void contextUpdated(ContextClusteringCommand message) throws ClusteringFault;

    /**
     * Set the system's configuration context. This will be used by the clustering implementations
     * to get information about the Axis2 environment and to correspond with the Axis2 environment
     *
     * @param configurationContext The configuration context
     */
    public void setConfigurationContext(ConfigurationContext configurationContext);
}
