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

package org.apache.axis2.clustering.handlers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusterManager;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.context.ContextManager;
import org.apache.axis2.context.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class ReplicationHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(ReplicationHandler.class);

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        log.debug("Going to replicate state on invoke");
        replicateState(msgContext);
        return InvocationResponse.CONTINUE;
    }

    public void flowComplete(MessageContext msgContext) {
        log.debug("Going to replicate state on flowComplete");
        try {
            replicateState(msgContext);
        } catch (Exception e) {
            String message = "Could not replicate the state";
            log.error(message, e);
        }
    }

    private void replicateState(MessageContext message) throws ClusteringFault {

        ConfigurationContext configurationContext = message.getConfigurationContext();
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
            ServiceGroupContext sgContext = message.getServiceGroupContext();
            if (sgContext != null && !sgContext.getPropertyDifferences().isEmpty()) {
                contexts.add(sgContext);
            }

            // Do we need to replicate state stored in ServiceContext?
            ServiceContext serviceContext = message.getServiceContext();
            if (serviceContext != null && !serviceContext.getPropertyDifferences().isEmpty()) {
                contexts.add(serviceContext);
            }

            // Do the actual replication here
            if (!contexts.isEmpty()) {
                contextManager.
                        updateContexts((AbstractContext[]) contexts.
                                toArray(new AbstractContext[contexts.size()]));
            }
        } else {
            String msg = "Cannot replicate contexts since " +
                         "ClusterManager is not specified in the axis2.xml file.";
            throw new ClusteringFault(msg);
        }
    }
}
