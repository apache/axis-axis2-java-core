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

package org.apache.axis2.clustering;

import org.apache.axis2.clustering.configuration.ConfigurationManager;
import org.apache.axis2.clustering.context.ContextManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.ParameterInclude;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * This is the main interface in the Axis2 clustering implementation.
 * In order to plug-in a new clustering implementation, this interface has to be
 * implemented.
 * </p>
 * <p>
 * The initilization of a node in the cluster is handled here. It is also responsible for getting
 * this node to join the cluster. This node should not process any Web services requests until it
 * successfully joins the cluster. Generally, this node will also need to obtain the state
 * information and/or configuration information from a neighboring node.
 * This interface is also responsible for
 * properly instantiating a {@link ContextManager} & {@link ConfigurationManager}. In the case of
 * a static <a href="http://afkham.org/2008/05/group-membership-management-schemes.html">
 * membership scheme</a>,
 * this members are read from the axis2.xml file and added to the ClusterManager.
 * </p>
 * <p>
 * In the axis2.xml, the instance of this interface is specified using the "cluster" class attribute.
 * e.g.
 * <code><b>
 * <nobr>&lt;cluster class="org.apache.axis2.cluster.tribes.TribesClusterManager"&gt;</nobr>
 * </b>
 * </code>
 * specifies that the TribesClusterManager class is the instance of this interface that
 * needs to be used.
 * </p>
 * <p>
 * There can also be several "parameter" elements, which are children of the "cluster" element
 * in the axis2.xml file. Generally, these parameters will be specific to the ClusterManager
 * implementation.
 * </p>
 */
public interface ClusterManager extends ParameterInclude {

    /**
     * Initialize this node, and join the cluster
     *
     * @throws ClusteringFault If an error occurs while initializing this node or joining the cluster
     */
    void init() throws ClusteringFault;

    /**
     * @return The ContextManager
     */
    ContextManager getContextManager();

    /**
     * @return The ConfigurationManager
     */
    ConfigurationManager getConfigurationManager();

    /**
     * Set the ContextManager corresponding to this ClusterManager. This is an optional attribute.
     * We can have a cluster with no context replication, in which case the contextManager will be
     * null. This value is set by the {@link org.apache.axis2.deployment.ClusterBuilder}, by
     * reading the  "contextManager" element in the axis2.xml
     * <p/>
     * e.g.
     * <code>
     * <b>
     * <contextManager class="org.apache.axis2.cluster.configuration.TribesContextManager">
     * </b>
     * </code>
     *
     * @param contextManager The ContextManager instance
     */
    void setContextManager(ContextManager contextManager);

    /**
     * Set the ConfigurationManager corresponding to this ClusterManager. This is an optional attribute.
     * We can have a cluster with no configuration management, in which case the configurationManager
     * will be null. This value is set by the {@link org.apache.axis2.deployment.ClusterBuilder}, by
     * reading the  "configurationManager" element in the axis2.xml
     * <p/>
     * e.g.
     * <code>
     * <b>
     * <configurationManager class="org.apache.axis2.cluster.configuration.TribesConfigurationManager">
     * </b>
     * </code>
     *
     * @param configurationManager The ConfigurationManager instance
     */
    void setConfigurationManager(ConfigurationManager configurationManager);

    /**
     * Disconnect this node from the cluster. This node will no longer receive membership change
     * notifications, state change messages or configuration change messages. The node will be "
     * "standing alone" once it is shutdown. However, it has to continue to process Web service
     * requests.
     *
     * @throws ClusteringFault If an error occurs while leaving the cluster
     */
    void shutdown() throws ClusteringFault;

    /**
     * Set the system's configuration context. This will be used by the clustering implementations
     * to get information about the Axis2 environment and to correspond with the Axis2 environment
     *
     * @param configurationContext The configuration context
     */
    void setConfigurationContext(ConfigurationContext configurationContext);

    /**
     * Set the static members of the cluster. This is used only with
     * <a href="http://afkham.org/2008/05/group-membership-management-schemes.html">
     * static group membership </a>
     *
     * @param members Members to be added
     */
    void setMembers(List<Member> members);

    /**
     * Get the list of members in a
     * <a href="http://afkham.org/2008/05/group-membership-management-schemes.html">
     * static group
     * </a>
     *
     * @return The members if static group membership is used. If any other membership scheme is used,
     *         the values returned may not be valid
     */
    List<Member> getMembers();

    /**
     * Set the load balance event handler which will be notified when load balance events occur.
     * This will be valid only when this node is running in loadBalance mode
     *
     * @param eventHandler      The load balance event handler
     * @param applicationDomain The application domain which is handled by the eventHandler
     */
    void addLoadBalanceEventHandler(LoadBalanceEventHandler eventHandler, String applicationDomain);

    /**
     * Get the LoadBalanceEventHandler which corresponds to the <code>applicationDomain</code>
     * This will be valid only when this node is running in loadBalance mode
     *
     * @param applicationDomain The application domain to which the application nodes being
     *                          load balanced belong to
     * @return LoadBalanceEventHandler which corresponds to the <code>applicationDomain</code>
     */
    LoadBalanceEventHandler getLoadBalanceEventHandler(String applicationDomain);


    /**
     * Get all the domains that this ClusterManager belongs to
     *
     * @return the domains of this ClusterManager
     */
    Set<String> getDomains();
}