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
     * @param context
     * @return The UUID of the message that was sent to the group communications framework
     * @throws ClusteringFault
     */
    String updateContext(AbstractContext context) throws ClusteringFault;

    /**
     * This method is called when properties in a collection of {@link AbstractContext}s are updated.
     * This could be addition of new properties, modifications of existing properties or
     * removal of properties.
     *
     * @param contexts
     * @return The UUID of the message that was sent to the group communications framework
     * @throws ClusteringFault
     */
    String updateContexts(AbstractContext[] contexts) throws ClusteringFault;

    /**
     * This method is called when a new {@link AbstractContext} is removed from the system
     *
     * @param context
     * @return The UUID of the message that was sent to the group communications framework
     * @throws ClusteringFault
     */
    String removeContext(AbstractContext context) throws ClusteringFault;

    /**
     * @param context
     * @return True - if the provided {@link AbstractContext}  is clusterable
     */
    boolean isContextClusterable(AbstractContext context);

    /**
     * Indicates whether a particular message has been ACKed by all members of a cluster
     *
     * @param messageUniqueId
     * @return true - if all memebers have ACKed the message with ID <code>messageUniqueId</code>
     *         false - otherwise
     * @throws ClusteringFault
     */
    boolean isMessageAcknowledged(String messageUniqueId) throws ClusteringFault;

    /**
     * @param listener
     */
    void setContextManagerListener(ContextManagerListener listener);

    /**
     * @param configurationContext
     */
    void setConfigurationContext(ConfigurationContext configurationContext);

    /**
     * All properties in the context with type <code>contextType</code> which have
     * names that match the specified pattern will be excluded from replication.
     * <p/>
     * Generally, we can use the context class name as the context type.
     *
     * @param contextType
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
