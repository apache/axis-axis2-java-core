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
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class ReplicationHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(ReplicationHandler.class);

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
//        System.err.println("### [INVOKE] Going to replicate state. Flow:" + msgContext.getFLOW());
        /* log.debug("Going to replicate state on invoke");
        try {
            replicateState(msgContext);
        } catch (Exception e) {
            System.err.println("###########################");
            String message = "Could not replicate the state";
            log.error(message, e);
            System.err.println("###########################");
        }*/
        return InvocationResponse.CONTINUE;
    }

    public void flowComplete(MessageContext msgContext) {
        int flow = msgContext.getFLOW();
        String mep = msgContext.getAxisOperation().getMessageExchangePattern();

        // The ReplicationHandler should be added to all 4 flows. We will replicate on flowComplete
        // only during one of the flows
        boolean replicateOnInFLow =
                ((mep.equals(WSDL2Constants.MEP_URI_IN_ONLY) ||
                  mep.equals(WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT) ||
                  mep.equals(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY))
                 && (flow == MessageContext.IN_FLOW || flow == MessageContext.IN_FAULT_FLOW));
        
        boolean replicateOnOutFlow =
                (mep.equals(WSDL2Constants.MEP_URI_IN_OUT) ||
                 mep.equals(WSDL2Constants.MEP_URI_OUT_ONLY) ||
                 mep.equals(WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN) ||
                 mep.equals(WSDL2Constants.MEP_URI_OUT_IN) ||
                 mep.equals(WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY))
                && (flow == MessageContext.OUT_FLOW || flow == MessageContext.OUT_FAULT_FLOW);

        if (replicateOnInFLow || replicateOnOutFlow) {
            System.err.println("### [FLOW COMPLETE] Going to replicate state. Flow:" + flow);
            try {
                replicateState(msgContext);
            } catch (Exception e) {
                String message = "Could not replicate the state";
                log.error(message, e);
            }
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
                String msgUUID =
                        contextManager.updateContexts((AbstractContext[]) contexts.
                                toArray(new AbstractContext[contexts.size()]));

                long start = System.currentTimeMillis();

                // Wait till all members have ACKed receipt & successful processing of
                // the message with UUID 'msgUUID'
                /*do {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        log.error(e);
                        break;
                    }
                    if (System.currentTimeMillis() - start > 20000) {
                        throw new ClusteringFault("ACKs not received from all members within 20 sec. " +
                                                  "Aborting wait.");
                    }
                } while (!contextManager.isMessageAcknowledged(msgUUID));*/
            }

        } else {
            String msg = "Cannot replicate contexts since " +
                         "ClusterManager is not specified in the axis2.xml file.";
            throw new ClusteringFault(msg);
        }
    }
}
