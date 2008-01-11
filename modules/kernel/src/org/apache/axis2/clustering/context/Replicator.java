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

import org.apache.axis2.clustering.ClusterManager;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Replicates serializable properties
 */
public final class Replicator {

    private static final Log log = LogFactory.getLog(Replicator.class);

    /**
     * Replicates all serializable properties in the ConfigurationContext, ServiceGroupContext &
     * ServiceContext
     *
     * @param msgContext The MessageContext associated with the ServiceContext,
     *                   ServiceGroupContext and ConfigurationContext to be replicated
     * @throws ClusteringFault If replication fails
     */
    public static void replicate(MessageContext msgContext) throws ClusteringFault {
        if (!canReplicate(msgContext)) {
            return;
        }
        log.debug("Going to replicate state stored in ConfigurationContext," +
                  " ServiceGroupContext, ServiceContext associated with " + msgContext + "...");
        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        ContextManager contextManager = getContextManager(msgContext);
        List contexts = new ArrayList();

        // Do we need to replicate state stored in ConfigurationContext?
        if (!configurationContext.getPropertyDifferences().isEmpty()) {
            contexts.add(configurationContext);
        }

        // Do we need to replicate state stored in ServiceGroupContext?
        ServiceGroupContext sgContext = msgContext.getServiceGroupContext();
        if (sgContext != null && !sgContext.getPropertyDifferences().isEmpty()) {
            contexts.add(sgContext);
        }

        // Do we need to replicate state stored in ServiceContext?
        ServiceContext serviceContext = msgContext.getServiceContext();
        if (serviceContext != null && !serviceContext.getPropertyDifferences().isEmpty()) {
            contexts.add(serviceContext);
        }

        // Do the actual replication here
        if (!contexts.isEmpty()) {
            AbstractContext[] contextArray =
                    (AbstractContext[]) contexts.toArray(new AbstractContext[contexts.size()]);
            contextManager.updateContexts(contextArray);
        }
    }

    /**
     * Replicate all serializable properties stored in the given <code>abstractContext</code>.
     *
     * @param abstractContext The AbstractContext which holds the properties to be replicated
     * @throws ClusteringFault If replication fails
     */
    public static void replicate(AbstractContext abstractContext) throws ClusteringFault {
        if (!canReplicate(abstractContext)) {
            return;
        }
        log.debug("Going to replicate state in " + abstractContext + "...");
        ContextManager contextManager = getContextManager(abstractContext);
        if (!abstractContext.getPropertyDifferences().isEmpty()) {
            contextManager.updateContext(abstractContext);
        }
    }

    /**
     * Replicate all the properties given in <code>propertyNames</code>
     * in the specified <code>abstractContext</code>
     *
     * @param abstractContext The context to be replicated
     * @param propertyNames   The names of the properties to be replicated
     * @throws ClusteringFault IF replication fails
     */
    public static void replicate(AbstractContext abstractContext,
                                 String[] propertyNames) throws ClusteringFault {
        if (!canReplicate(abstractContext)) {
            return;
        }
        log.debug("Going to replicate selected properties in " + abstractContext + "...");
        ContextManager contextManager = getContextManager(abstractContext);
        contextManager.updateContext(abstractContext, propertyNames);
    }

    private static ClusterManager getClusterManager(AbstractContext abstractContext) {
        return abstractContext.getRootContext().getAxisConfiguration().getClusterManager();
    }

    private static ContextManager getContextManager(AbstractContext abstractContext) {
        return getClusterManager(abstractContext).getContextManager();
    }

    /**
     * Check whether the state store in the specified <code>abstractContext</code> can be replicated.
     * Also note that if there are no members, we need not do any replication
     *
     * @param abstractContext The context to be subjected to this test
     * @return true - State needs to be replicated
     *         false - otherwise
     */
    private static boolean canReplicate(AbstractContext abstractContext) {
        ClusterManager clusterManager =
                abstractContext.getRootContext().getAxisConfiguration().getClusterManager();
        boolean canReplicate = false;
        if (clusterManager != null && clusterManager.getContextManager() != null) {
            canReplicate =
                    clusterManager.getContextManager().isContextClusterable(abstractContext);
        }
        return canReplicate;
    }

    /**
     * Check whether the state store in the specified <code>messageContext</code> can be replicated.
     * Also note that if there are no members, we need not do any replication
     *
     * @param messageContext The MessageContext to be subjected to this test
     * @return true - State needs to be replicated
     *         false - otherwise
     */
    private static boolean canReplicate(MessageContext messageContext) {
        ClusterManager clusterManager =
                messageContext.getRootContext().getAxisConfiguration().getClusterManager();
        return clusterManager != null && clusterManager.getContextManager() != null;
    }
}
