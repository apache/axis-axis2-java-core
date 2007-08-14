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
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public final class Replicator {

    private static final Log log = LogFactory.getLog(Replicator.class);

    public static void replicate(MessageContext msgContext) throws ClusteringFault {
        if (!doReplication(msgContext)) {
            return;
        }
        log.debug("Going to replicate state...");
        try {
            replicateState(msgContext);
        } catch (Exception e) {
            String message = "Could not replicate the state";
            log.error(message, e);
            throw new ClusteringFault(message, e);
        }
    }

    public static void replicate(AbstractContext abstractContext) throws ClusteringFault {
        if (!doReplication(abstractContext)) {
            return;
        }
        log.debug("Going to replicate state...");
        try {
            replicateState(abstractContext);
        } catch (Exception e) {
            String message = "Could not replicate the state";
            log.error(message, e);
            throw new ClusteringFault(message, e);
        }
    }

    /**
     * Do replication only if context replication is enabled.
     * Also note that if there are no members, we need not do any replication
     *
     * @param abstractContext
     * @return true - State needs to be replicated
     *         false - otherwise
     */
    private static boolean doReplication(AbstractContext abstractContext) {
        ClusterManager clusterManager =
                abstractContext.getRootContext().getAxisConfiguration().getClusterManager();
        return clusterManager != null &&
               clusterManager.getContextManager() != null;
    }

    private static void replicateState(AbstractContext abstractContext) throws ClusteringFault {
        ClusterManager clusterManager =
                abstractContext.getRootContext().getAxisConfiguration().getClusterManager();
        if (clusterManager != null) {
            ContextManager contextManager = clusterManager.getContextManager();
            if (contextManager == null) {
                String msg = "Cannot replicate contexts since " +
                             "ContextManager is not specified in the axis2.xml file.";
                throw new ClusteringFault(msg);
            }
            if (!abstractContext.getPropertyDifferences().isEmpty()) {
                String msgUUID = contextManager.updateContext(abstractContext);
                waitForACKs(contextManager, msgUUID, abstractContext.getRootContext());
            }
        } else {
            String msg = "Cannot replicate contexts since " +
                         "ClusterManager is not specified in the axis2.xml file.";
            throw new ClusteringFault(msg);
        }
    }

    private static void replicateState(MessageContext msgContext) throws ClusteringFault {
        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        ClusterManager clusterManager = axisConfiguration.getClusterManager();

        if (clusterManager != null) {

            ContextManager contextManager = clusterManager.getContextManager();
            if (contextManager == null) {
                String msg = "Cannot replicate contexts since " +
                             "ContextManager is not specified in the axis2.xml file.";
                throw new ClusteringFault(msg);
            }

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
                String msgUUID = contextManager.updateContexts(contextArray);
                waitForACKs(contextManager, msgUUID, msgContext.getRootContext());
            }

        } else {
            String msg = "Cannot replicate contexts since " +
                         "ClusterManager is not specified in the axis2.xml file.";
            throw new ClusteringFault(msg);
        }
    }

    private static void waitForACKs(ContextManager contextManager,
                                    String msgUUID,
                                    ConfigurationContext configCtx) throws ClusteringFault {
        long start = System.currentTimeMillis();

        // Wait till all members have ACKed receipt & successful processing of
        // the message with UUID 'msgUUID'
        do {

            // Wait sometime before checking whether message is ACKed
            try {
                Long tts =
                        (Long) configCtx.getPropertyNonReplicable(ClusteringConstants.TIME_TO_SEND);
                if (tts == null) {
                    Thread.sleep(5);
                } else if (tts.longValue() >= 0) {
                    Thread.sleep(tts.longValue() + 5); // Time to recv ACK + time in queue & processing replication request
                }
            } catch (InterruptedException ignored) {
            }
            if (System.currentTimeMillis() - start > 45000) {
                throw new ClusteringFault("ACKs not received from all members within 45 sec. " +
                                          "Aborting wait.");
            }
        } while (!contextManager.isMessageAcknowledged(msgUUID));
    }
}
