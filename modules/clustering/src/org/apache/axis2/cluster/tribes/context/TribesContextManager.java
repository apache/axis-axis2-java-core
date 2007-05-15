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
import org.apache.axis2.cluster.CommandType;
import org.apache.axis2.cluster.context.ContextEvent;
import org.apache.axis2.cluster.context.ContextManager;
import org.apache.axis2.cluster.context.ContextManagerListener;
import org.apache.axis2.cluster.listeners.DefaultContextManagerListener;
import org.apache.axis2.cluster.tribes.ChannelSender;
import org.apache.axis2.cluster.tribes.CommandMessage;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class TribesContextManager implements ContextManager {

    private static final Log log = LogFactory.getLog(TribesContextManager.class);

    private ConfigurationContext configContext;
    private ContextUpdater updater;

    private Map orphanedServiceCtxs = new HashMap();
    private List listeners = new ArrayList();
    private Map parameters = new HashMap();

    private ChannelSender sender;
    private ContextReplicationProcessor processor = new ContextReplicationProcessor();

    public void setSender(ChannelSender sender) {
        this.sender = sender;
    }

    public TribesContextManager() {
    }

    public void addContext(final AbstractContext context) throws ClusteringFault {
        processor.process(new AddContextCommand(), context);
    }

    public void removeContext(AbstractContext context) throws ClusteringFault {
        processor.process(new RemoveContextCommand(), context);
    }

    public void updateContext(AbstractContext context) throws ClusteringFault {
        processor.process(new UpdateContextCommand(), context);
    }

    public boolean isContextClusterable(AbstractContext context) {
        return (context instanceof ConfigurationContext) ||
               (context instanceof ServiceContext) ||
               (context instanceof ServiceGroupContext);
    }

    public void notifyListeners(ContextEvent event, int eventType) {

        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ContextManagerListener listener = (ContextManagerListener) it.next();
            if (eventType == ContextListenerEventType.ADD_CONTEXT) {
                listener.contextAdded(event);
            } else if (eventType == ContextListenerEventType.REMOVE_CONTEXT) {
                listener.contextRemoved(event);
            } else if (eventType == ContextListenerEventType.UPDATE_CONTEXT) {
                listener.contextUpdated(event);
            }
        }
    }

    private String getContextID(AbstractContext context) {
        String id = null;
        if (context instanceof ServiceContext) {
            AxisService axisService = ((ServiceContext) context).getAxisService();
            return axisService.getName();
        } else if (context instanceof ServiceGroupContext) {
            return ((ServiceGroupContext) context).getId();
        }
        return id;
    }

    public void addToDuplicateServiceGroupContexts(String id) {

    }

    public void addToDuplicateServiceContexts(String id) {

    }

    public void addContextManagerListener(ContextManagerListener listener) {
        if (configContext != null) {
            listener.setConfigurationContext(configContext);
        }
        listeners.add(listener);
    }

    public void setUpdater(ContextUpdater updater) {
        this.updater = updater;

        //updating the listeners
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            DefaultContextManagerListener listener = (DefaultContextManagerListener) it.next();
            listener.setUpdater(updater);
        }
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

    private void send(CommandMessage command) throws ClusteringFault {
        sender.send(command);
    }

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

    private interface ContextCommand {
        void execute(AbstractContext context) throws ClusteringFault;
    }

    private class AddContextCommand implements ContextCommand {

        public void execute(AbstractContext context) throws ClusteringFault {
            if (log.isDebugEnabled()) {
                log.debug("Enter: TribesContextManager::addContext");
            }
            ContextCommandMessage comMsg;

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
                    comMsg = new ContextCommandMessage(CommandType.CREATE_SERVICE_CONTEXT,
                                                       parentContextId,
                                                       contextId,
                                                       contextId,
                                                       ContextType.SERVICE_CONTEXT);
                    send(comMsg);
                } else {
                    // put in the queue until the service group context is created
                    // with an id
                    comMsg = new ContextCommandMessage(CommandType.CREATE_SERVICE_CONTEXT,
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

                if (log.isDebugEnabled()) {
                    log.debug("Exit: TribesContextManager::addContext");
                }

                ServiceGroupContext srvGrpCtx = (ServiceGroupContext) context;

                // The new serialization code sets the service group name as it's id initially
                if (srvGrpCtx.getId().equals(srvGrpCtx.getDescription().getServiceGroupName())) {

                    if (log.isDebugEnabled()) {
                        log.debug("Exit: TribesContextManager::addContext");
                    }

                    return;
                }

                updater.addServiceGroupContext(contextId);

                comMsg = new ContextCommandMessage(CommandType.CREATE_SERVICE_GROUP_CONTEXT,
                                                   "",
                                                   contextId,
                                                   srvGrpCtx.getDescription().getServiceGroupName(),
                                                   ContextType.SERVICE_GROUP_CONTEXT);

                send(comMsg);

                // now iterate through the list of service contexts and replicate them
                List list = (List) orphanedServiceCtxs.get(srvGrpCtx.getDescription()
                        .getServiceGroupName());

                if (list != null) {
                    for (Iterator it = list.iterator(); it.hasNext();) {

                        ContextCommandMessage command = (ContextCommandMessage) it.next();
                        updater.addServiceContext(contextId, command.getContextId());
                        command.setParentId(contextId);

                        send(command);

                    }
                    orphanedServiceCtxs.remove(srvGrpCtx.getDescription().getServiceGroupName());
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Exit: TribesContextManager::addContext");
            }
        }
    }

    private class RemoveContextCommand implements ContextCommand {

        public void execute(AbstractContext context) throws ClusteringFault {
            if (log.isDebugEnabled()) {
                log.debug("Enter: TribesContextManager::removeContext");
            }

            ContextCommandMessage comMsg = null;

            String contextId = getContextID(context);
            String parentContextId = getContextID(context.getParent());

            if (context instanceof ServiceContext) {
                updater.removeServiceContext(parentContextId, contextId);
                comMsg = new ContextCommandMessage(CommandType.CREATE_SERVICE_GROUP_CONTEXT,
                                                   parentContextId,
                                                   contextId,
                                                   contextId,
                                                   ContextType.SERVICE_CONTEXT);
            } else if (context instanceof ServiceGroupContext) {
                updater.removeServiceGroupContext(contextId);
                comMsg = new ContextCommandMessage(CommandType.REMOVE_SERVICE_GROUP_CONTEXT,
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

            ContextCommandMessage comMsg = new ContextCommandMessage(CommandType.UPDATE_STATE,
                                                                     parentContextId,
                                                                     contextId,
                                                                     contextId,
                                                                     contextType);

            send(comMsg);

            if (log.isDebugEnabled()) {
                log.debug("Exit: TribesContextManager::updateState");
            }
        }
    }

    private class ContextReplicationProcessor {
        public void process(final ContextCommand cmd,
                            final AbstractContext context) throws ClusteringFault {

            // If the updater is NULL, it means the TribesClusterManager is still being initailized
            // So we need to busy wait.
            if (updater == null) {
                Thread processorThread = new Thread("ProcessorThread") {
                    public void run() {
                        while (updater == null) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            cmd.execute(context);
                        } catch (ClusteringFault clusteringFault) {
                            throw new RuntimeException(clusteringFault);
                        }
                    }
                };
                processorThread.start();
            } else {
                cmd.execute(context);
            }
        }
    }
}
