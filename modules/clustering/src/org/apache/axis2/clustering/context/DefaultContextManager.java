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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.MessageSender;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.Parameter;

import java.util.*;

public class DefaultContextManager implements ContextManager {

    private ConfigurationContext configContext;

    private ContextManagerListener listener;
    private Map parameters = new HashMap();

    private MessageSender sender;
    private ContextReplicationProcessor processor = new ContextReplicationProcessor();

    private Map excludedReplicationPatterns = new HashMap();

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public DefaultContextManager() {
    }

    public void addContext(final AbstractContext context) throws ClusteringFault {
        processor.process(ContextClusteringCommandFactory.getCreateMessage(context));
    }

    public void removeContext(AbstractContext context) throws ClusteringFault {
        processor.process(ContextClusteringCommandFactory.getRemoveMessage(context));
    }

    public void updateContext(AbstractContext context) throws ClusteringFault {
        ContextClusteringCommand message =
                ContextClusteringCommandFactory.getUpdateMessage(context,
                                                                 excludedReplicationPatterns);
        processor.process(message);
    }

    public boolean isContextClusterable(AbstractContext context) {
        return (context instanceof ConfigurationContext) ||
               (context instanceof ServiceContext) ||
               (context instanceof ServiceGroupContext);
    }

    public void notifyListener(ContextClusteringCommand command) throws ClusteringFault {
        switch (command.getCommandType()) {
            case ContextClusteringCommand.CREATE_SERVICE_CONTEXT:
            case ContextClusteringCommand.CREATE_SERVICE_GROUP_CONTEXT:
                listener.contextAdded(command);
                break;
            case ContextClusteringCommand.UPDATE_SERVICE_CONTEXT:
            case ContextClusteringCommand.UPDATE_SERVICE_GROUP_CONTEXT:
            case ContextClusteringCommand.UPDATE_CONFIGURATION_CONTEXT:
                listener.contextUpdated(command);
                break;
            case ContextClusteringCommand.DELETE_SERVICE_CONTEXT:
            case ContextClusteringCommand.DELETE_SERVICE_GROUP_CONTEXT:
                listener.contextRemoved(command);
                break;
            default:
                throw new ClusteringFault("Invalid ContextClusteringCommand " +
                                          command.getClass().getName());
        }
    }

    public void setContextManagerListener(ContextManagerListener listener) {
        if (configContext != null) {
            listener.setConfigurationContext(configContext);
        }
        this.listener = listener;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configContext = configurationContext;
        listener.setConfigurationContext(configurationContext);
    }

    public void setReplicationExcludePatterns(String contextType, List patterns) {
        excludedReplicationPatterns.put(contextType, patterns);
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
                            sender.sendToGroup(cmd);
                        } catch (ClusteringFault clusteringFault) {
                            throw new RuntimeException(clusteringFault);
                        }
                    }
                };
                processorThread.start();
            } else {
                sender.sendToGroup(cmd);
            }
        }
    }
}
