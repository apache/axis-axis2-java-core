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
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.ParameterInclude;

import java.util.List;
import java.util.Map;

public interface ContextManager extends ParameterInclude {

    /**
     * This method is called when properties in an {@link AbstractContext} are updated.
     * This could be addition of new properties, modifications of existing properties or
     * removal of properties.
     *
     * @param context The AbstractContext containing the properties to be replicated
     * @return The UUID of the message that was sent to the group communications framework
     * @throws ClusteringFault If replication fails
     */
    String updateContext(AbstractContext context) throws ClusteringFault;

    /**
     * This method is called when one need to update/replicate only certains properties in the
     * specified <code>context</code>
     *
     * @param context       The AbstractContext containing the properties to be replicated
     * @param propertyNames The names of the specific properties that should be replicated
     * @return The UUID of the message that was sent to the group communications framework
     * @throws ClusteringFault If replication fails
     */
    String updateContext(AbstractContext context, String[] propertyNames) throws ClusteringFault;

    /**
     * This method is called when properties in a collection of {@link AbstractContext}s are updated.
     * This could be addition of new properties, modifications of existing properties or
     * removal of properties.
     *
     * @param contexts The AbstractContexts containing the properties to be replicated
     * @return The UUID of the message that was sent to the group communications framework
     * @throws ClusteringFault If replication fails
     */
    String updateContexts(AbstractContext[] contexts) throws ClusteringFault;

    /**
     * This method is called when {@link AbstractContext} is removed from the system
     *
     * @param context The AbstractContext to be removed
     * @return The UUID of the message that was sent to the group communications framework
     * @throws ClusteringFault If context removal fails
     */
    String removeContext(AbstractContext context) throws ClusteringFault;

    /**
     * @param context AbstractContext
     * @return True - if the provided {@link AbstractContext}  is clusterable
     */
    boolean isContextClusterable(AbstractContext context);

    /**
     * Indicates whether a particular message has been ACKed by all members of a cluster
     *
     * @param messageUniqueId The UUID of the message in concern
     * @return true - if all memebers have ACKed the message with ID <code>messageUniqueId</code>
     *         false - otherwise
     * @throws ClusteringFault If an error occurs while checking whether a message is ACKed
     */
    boolean isMessageAcknowledged(String messageUniqueId) throws ClusteringFault;

    /**
     * @param listener ContextManagerListener
     */
    void setContextManagerListener(ContextManagerListener listener);

    /**
     * @param configurationContext ConfigurationContext
     */
    void setConfigurationContext(ConfigurationContext configurationContext);

    /**
     * All properties in the context with type <code>contextType</code> which have
     * names that match the specified pattern will be excluded from replication.
     * <p/>
     * Generally, we can use the context class name as the context type.
     *
     * @param contextType The type of the context such as
     *                    org.apache.axis2.context.ConfigurationContext,
     *                    org.apache.axis2.context.ServiceGroupContext &
     *                    org.apache.axis2.context.ServiceContext.
     *                    Also "defaults" is a special type, which will apply to all contexts
     * @param patterns    The patterns
     */
    void setReplicationExcludePatterns(String contextType, List patterns);

    /**
     * Get all the excluded context property name patterns
     *
     * @return All the excluded pattern of all the contexts. The key of the Map is the
     *         the <code>contextType</code>. See {@link #setReplicationExcludePatterns(String,List)}.
     *         The values are of type {@link List} of {@link String} Objects,
     *         which are a collection of patterns to be excluded.
     */
    Map getReplicationExcludePatterns();
}
