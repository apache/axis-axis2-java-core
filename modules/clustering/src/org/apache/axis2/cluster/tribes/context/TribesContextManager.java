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

package org.apache.axis2.cluster.tribes.context;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.context.ContextCommandMessage;
import org.apache.axis2.cluster.context.ContextManager;
import org.apache.axis2.cluster.context.ContextManagerListener;
import org.apache.axis2.cluster.tribes.ChannelSender;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class TribesContextManager implements ContextManager {

    private static final Log log = LogFactory.getLog(TribesContextManager.class);

    private ConfigurationContext configContext;

    private List listeners = new ArrayList();
    private Map parameters = new HashMap();

    private ChannelSender sender;
    private ContextReplicationProcessor processor = new ContextReplicationProcessor();

    private Map excludedReplicationPatterns = new HashMap();

    public void setSender(ChannelSender sender) {
        this.sender = sender;
    }

    public TribesContextManager() {
    }

    public void addContext(final AbstractContext context) throws ClusteringFault {
        processor.process(ContextCommandMessageFactory.getCreateMessage(context));
    }

    public void removeContext(AbstractContext context) throws ClusteringFault {
        processor.process(ContextCommandMessageFactory.getRemoveMessage(context));
    }

    public void updateContext(AbstractContext context) throws ClusteringFault {
        ContextCommandMessage message =
                ContextCommandMessageFactory.getUpdateMessage(context,
                                                              excludedReplicationPatterns);
        processor.process(message);
    }

    public boolean isContextClusterable(AbstractContext context) {
        return (context instanceof ConfigurationContext) ||
               (context instanceof ServiceContext) ||
               (context instanceof ServiceGroupContext);
    }

    public void notifyListeners(ContextCommandMessage command) throws ClusteringFault {

        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            ContextManagerListener listener = (ContextManagerListener) iter.next();
            switch (command.getMessageType()) {
                case ContextCommandMessage.CREATE_SERVICE_CONTEXT_MSG:
                case ContextCommandMessage.CREATE_SERVICE_GROUP_CONTEXT_MSG:
                    listener.contextAdded(command);
                    break;
                case ContextCommandMessage.UPDATE_SERVICE_CONTEXT_MSG:
                case ContextCommandMessage.UPDATE_SERVICE_GROUP_CONTEXT_MSG:
                case ContextCommandMessage.UPDATE_CONFIGURATION_CONTEXT_MSG:
                    listener.contextUpdated(command);
                    break;
                case ContextCommandMessage.DELETE_SERVICE_CONTEXT_MSG:
                case ContextCommandMessage.DELETE_SERVICE_GROUP_CONTEXT_MSG:
                    listener.contextRemoved(command);
            }
        }
    }

    public void addContextManagerListener(ContextManagerListener listener) {
        if (configContext != null) {
            listener.setConfigurationContext(configContext);
        }
        listeners.add(listener);
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configContext = configurationContext;

        //setting this to the listeners as well.
        if (listeners != null) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                ContextManagerListener listener = (ContextManagerListener) it.next();
                listener.setConfigurationContext(configurationContext);
            }
        }
    }

    public void setReplicationExcludePatterns(String contextType, List patterns) {
        System.out.println("### contextType=" + contextType);
        System.out.println("### pattern=" + patterns);
        //TODO: Method implementation
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

/*    private class AddContextCommand implements ContextCommand {

    public void execute(AbstractContext context) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Enter: TribesContextManager::addContext");
        }
        ContextCommand comMsg;

        String contextId = getContextID(context);
        String parentContextId = getContextID(context.getParent());

        // The ServiceContex does not define a contextId
        // therefore the service name is used
        if (context instanceof ServiceContext) {

            if (log.isDebugEnabled()) {
                log.debug("Exit: TribesContextManager::addContext");
            }

            if (updater.getServiceGroupProps(parentContextId) != null) {
                updater.addServiceContext(parentContextId, contextId);

                public ContextCommandMessage(
                int commandType,
                String parentId,
                String contextId,
                String axisDescriptionName,
                int contextType)

                comMsg = new ContextCommand(CommandType.CREATE_SERVICE_CONTEXT,
                                            parentContextId,
                                            contextId,
                                            contextId,
                                            ContextType.SERVICE_CONTEXT);
                send(comMsg);
            } else {
                // put in the queue until the service group context is created
                // with an id
                comMsg = new ContextCommand(CommandType.CREATE_SERVICE_CONTEXT,
                                            parentContextId,
                                            contextId,
                                            contextId,
                                            ContextType.SERVICE_CONTEXT);

                AxisServiceGroup serviceGroupDesc = ((ServiceContext) context)
                        .getServiceGroupContext().getDescription();
                List list = (List) orphanedServiceCtxs.get(serviceGroupDesc
                        .getServiceGroupName());
                if (list == null) {
                    list = new ArrayList();
                    orphanedServiceCtxs.put(serviceGroupDesc.getServiceGroupName(), list);
                }
                list.add(comMsg);
            }
        } else if (context instanceof ServiceGroupContext) {
            log.debug("Exit: TribesContextManager::addServiceGroupContext");
            ServiceGroupContext sgCtx = (ServiceGroupContext) context;
            CreateServiceGroupContextCommand cmd =
                    new CreateServiceGroupContextCommand();
            cmd.setServiceGroupName(sgCtx.getDescription().getServiceGroupName());
            send(cmd);
            log.debug("Exit: TribesContextManager::addContext");
        }
    }
}

private class RemoveContextCommand implements ContextCommand {

    public void execute(AbstractContext context) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Enter: TribesContextManager::removeContext");
        }

        ContextCommand comMsg = null;

        String contextId = getContextID(context);
        String parentContextId = getContextID(context.getParent());

        if (context instanceof ServiceContext) {
            updater.removeServiceContext(parentContextId, contextId);
            comMsg = new ContextCommand(CommandType.CREATE_SERVICE_GROUP_CONTEXT,
                                        parentContextId,
                                        contextId,
                                        contextId,
                                        ContextType.SERVICE_CONTEXT);
        } else if (context instanceof ServiceGroupContext) {
            updater.removeServiceGroupContext(contextId);
            comMsg = new ContextCommand(CommandType.REMOVE_SERVICE_GROUP_CONTEXT,
                                        "",
                                        contextId,
                                        ((ServiceGroupContext) context).getDescription().getServiceGroupName(),
                                        ContextType.SERVICE_CONTEXT);
        }

        send(comMsg);

        if (log.isDebugEnabled()) {
            log.debug("Exit: TribesContextManager::removeContext");
        }
    }
}

private class UpdateContextCommand implements ContextCommand {

    public void execute(AbstractContext context) throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Enter: TribesContextManager::updateState");
        }

        String contextId = getContextID(context);
        String parentContextId = getContextID(context.getParent());

        Map props = context.getProperties();

        List mapEntryMsgs = null;

        int contextType = 0;
        if (context instanceof ServiceContext) {
            contextType = ContextType.SERVICE_CONTEXT;
            mapEntryMsgs = updater
                    .updateStateOnServiceContext(parentContextId, contextId, props);
        } else if (context instanceof ServiceGroupContext) {
            contextType = ContextType.SERVICE_GROUP_CONTEXT;
            mapEntryMsgs = updater.updateStateOnServiceGroupContext(contextId, props);
        }

        if (mapEntryMsgs != null) {
            for (Iterator it = mapEntryMsgs.iterator(); it.hasNext();) {
                ContextUpdateEntryCommandMessage msg = (ContextUpdateEntryCommandMessage) it.next();
                send(msg);
            }
        }

        ContextCommand comMsg = new ContextCommand(CommandType.UPDATE_STATE,
                                                   parentContextId,
                                                   contextId,
                                                   contextId,
                                                   contextType);

        send(comMsg);

        if (log.isDebugEnabled()) {
            log.debug("Exit: TribesContextManager::updateState");
        }
    }
}*/

    private class ContextReplicationProcessor {
        public void process(final ContextCommandMessage cmd) throws ClusteringFault {

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
                            sender.send(cmd);
                        } catch (ClusteringFault clusteringFault) {
                            throw new RuntimeException(clusteringFault);
                        }
                    }
                };
                processorThread.start();
            } else {
                sender.send(cmd);
            }
        }
    }
}
