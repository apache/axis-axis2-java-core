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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.context.commands.ContextClusteringCommandCollection;
import org.apache.axis2.clustering.tribes.AckManager;
import org.apache.axis2.clustering.tribes.ChannelSender;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.Parameter;

import java.util.*;

public class DefaultContextManager implements ContextManager {

    private ConfigurationContext configContext;

    private Map parameters = new HashMap();

    private ChannelSender sender;
    private ContextReplicationProcessor processor = new ContextReplicationProcessor();

    private Map excludedReplicationPatterns = new HashMap();

    //TODO: Try how to use an interface
    public void setSender(ChannelSender sender) {
        this.sender = sender;
    }

    public DefaultContextManager() {
    }
    
    public String updateContext(AbstractContext context) throws ClusteringFault {
        ContextClusteringCommand cmd =
                ContextClusteringCommandFactory.getUpdateCommand(context,
                                                                 excludedReplicationPatterns,
                                                                 false);
        if (cmd != null) {
            processor.process(cmd);
            return cmd.getUniqueId();
        }
        return null;
    }

    public String updateContexts(AbstractContext[] contexts) throws ClusteringFault {
        ContextClusteringCommandCollection cmd =
                ContextClusteringCommandFactory.getCommandCollection(contexts,
                                                                     excludedReplicationPatterns);
        processor.process(cmd);
        return cmd.getUniqueId();
    }

    public String removeContext(AbstractContext context) throws ClusteringFault {
        ContextClusteringCommand cmd = ContextClusteringCommandFactory.getRemoveCommand(context);
        processor.process(cmd);
        return cmd.getUniqueId();
    }

    public boolean isContextClusterable(AbstractContext context) {
        return (context instanceof ConfigurationContext) ||
               (context instanceof ServiceContext) ||
               (context instanceof ServiceGroupContext);
    }

    public boolean isMessageAcknowledged(String messageUniqueId) throws ClusteringFault {
        return AckManager.isMessageAcknowledged(messageUniqueId, sender);
    }

    public void process(ContextClusteringCommand command) throws ClusteringFault {
        command.execute(configContext);
    }

    public void setContextManagerListener(ContextManagerListener listener) {
        if (configContext != null) {
            listener.setConfigurationContext(configContext);
        }
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configContext = configurationContext;
    }

    public void setReplicationExcludePatterns(String contextType, List patterns) {
        excludedReplicationPatterns.put(contextType, patterns);
    }

    public Map getReplicationExcludePatterns() {
        return excludedReplicationPatterns;
    }

    // ---------------------- Methods from ParameterInclude ----------------------------------------
    public void addParameter(Parameter param) throws AxisFault {
        parameters.put(param.getName(), param);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    public Parameter getParameter(String name) {
        return (Parameter) parameters.get(name);
    }

    public ArrayList getParameters() {
        ArrayList list = new ArrayList();
        for (Iterator iter = parameters.keySet().iterator(); iter.hasNext();) {
            list.add(parameters.get(iter.next()));
        }
        return list;
    }

    public boolean isParameterLocked(String parameterName) {
        return getParameter(parameterName).isLocked();
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        throw new UnsupportedOperationException();
    }
    // ---------------------------------------------------------------------------------------------

    private class ContextReplicationProcessor {
        public void process(final ContextClusteringCommand cmd) throws ClusteringFault {

            // If the sender is NULL, it means the TribesClusterManager is still being initialized
            // So we need to busy wait.
            if (sender == null) {
                Thread processorThread = new Thread("ProcessorThread") {
                    public void run() {
                        do {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } while (sender == null);
                        try {
                            long tts = sender.sendToGroup(cmd);
                            configContext.setNonReplicableProperty(ClusteringConstants.TIME_TO_SEND,
                                                                   new Long(tts));
                        } catch (ClusteringFault clusteringFault) {
                            throw new RuntimeException(clusteringFault);
                        }
                    }
                };
                processorThread.start();
            } else {
                long tts = sender.sendToGroup(cmd);
                configContext.setNonReplicableProperty(ClusteringConstants.TIME_TO_SEND,
                                                       new Long(tts));
            }
        }
    }
}
