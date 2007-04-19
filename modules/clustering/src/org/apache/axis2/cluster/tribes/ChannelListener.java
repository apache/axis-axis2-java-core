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

package org.apache.axis2.cluster.tribes;

import java.io.Serializable;
import java.util.Map;

import org.apache.axis2.cluster.CommandType;
import org.apache.axis2.cluster.configuration.ConfigurationEvent;
import org.apache.axis2.cluster.context.ContextEvent;
import org.apache.axis2.cluster.tribes.configuration.ConfigurationCommand;
import org.apache.axis2.cluster.tribes.configuration.TribesConfigurationManager;
import org.apache.axis2.cluster.tribes.context.ContextCommandMessage;
import org.apache.axis2.cluster.tribes.context.ContextListenerEventType;
import org.apache.axis2.cluster.tribes.context.ContextType;
import org.apache.axis2.cluster.tribes.context.ContextUpdateEntryCommandMessage;
import org.apache.axis2.cluster.tribes.context.ContextUpdater;
import org.apache.axis2.cluster.tribes.context.TribesContextManager;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ChannelListener implements org.apache.catalina.tribes.ChannelListener {

    ContextUpdater updater = null;
    TribesContextManager contextManager = null;
    TribesConfigurationManager configurationManager = null;

    private static final Log log = LogFactory.getLog(ChannelListener.class);

    public ChannelListener(TribesConfigurationManager configurationManager,
                           TribesContextManager contextManager) {
        this.configurationManager = configurationManager;
        this.contextManager = contextManager;
    }

    public void setContextManager(TribesContextManager contextManager) {
        this.contextManager = contextManager;
    }

    public void setConfigurationManager(TribesConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void setUpdater(ContextUpdater updater) {
        this.updater = updater;
    }

    public boolean accept(Serializable msg, Member sender) {
        return true;
    }

    public void messageReceived(Serializable msg, Member sender) {


        if (msg instanceof ContextCommandMessage) {

            ContextCommandMessage comMsg = (ContextCommandMessage) msg;

            // TODO make sure to remove from the duplicate lists when remove is
            // requested for both service group and service contexts
            // TODO fix this to support scopes other than SOAP Session.

            if (comMsg.getCommandType() == CommandType.CREATE_SERVICE_GROUP_CONTEXT) {

                // add to the duplicate list to prevent cyclic replication
                contextManager.addToDuplicateServiceGroupContexts(comMsg.getContextId());
                updater.addServiceGroupContext(comMsg.getContextId());

                ContextEvent event = new ContextEvent();
                event.setContextType(ContextType.SERVICE_GROUP_CONTEXT);
                event.setContextID(comMsg.getContextId());
                event.setParentContextID(comMsg.getParentId());
                event.setDescriptionID(comMsg.getAxisDescriptionName());

                contextManager.notifyListeners(event, ContextListenerEventType.ADD_CONTEXT);

            } else if (comMsg.getCommandType() == CommandType.CREATE_SERVICE_CONTEXT) {

                // add to the duplicate list to prevent cyclic replication
                contextManager.addToDuplicateServiceContexts(comMsg.getParentId()
                                                             + comMsg.getContextId());
                updater.addServiceContext(comMsg.getParentId(), comMsg.getContextId());

                ContextEvent event = new ContextEvent();
                event.setContextType(ContextType.SERVICE_CONTEXT);
                event.setContextID(comMsg.getContextId());
                event.setParentContextID(comMsg.getParentId());
                event.setDescriptionID(comMsg.getAxisDescriptionName());

                contextManager.notifyListeners(event, ContextListenerEventType.ADD_CONTEXT);

            } else if (comMsg.getCommandType() == CommandType.UPDATE_STATE) {

                if (comMsg.getContextType() == ContextType.SERVICE_GROUP_CONTEXT) {

                    ContextEvent event = new ContextEvent();
                    event.setContextType(ContextType.SERVICE_GROUP_CONTEXT);
                    event.setContextID(comMsg.getContextId());
                    event.setParentContextID(comMsg.getParentId());
                    event.setDescriptionID(comMsg.getAxisDescriptionName());

                    contextManager.notifyListeners(event, ContextListenerEventType.UPDATE_CONTEXT);

                } else if (comMsg.getContextType() == ContextType.SERVICE_CONTEXT) {

                    ContextEvent event = new ContextEvent();
                    event.setContextType(ContextType.SERVICE_CONTEXT);
                    event.setContextID(comMsg.getContextId());
                    event.setParentContextID(comMsg.getParentId());
                    event.setDescriptionID(comMsg.getAxisDescriptionName());

                    contextManager.notifyListeners(event, ContextListenerEventType.UPDATE_CONTEXT);

                }

            } else if (comMsg.getCommandType() == CommandType.UPDATE_STATE_MAP_ENTRY) {

                ContextUpdateEntryCommandMessage mapEntryMsg =
                        (ContextUpdateEntryCommandMessage) comMsg;
                if (mapEntryMsg.getCtxType() ==
                    ContextUpdateEntryCommandMessage.SERVICE_GROUP_CONTEXT) {
                    Map props = updater.getServiceGroupProps(comMsg.getContextId());
                    if (mapEntryMsg.getOperation() ==
                        ContextUpdateEntryCommandMessage.ADD_OR_UPDATE_ENTRY) {
                        props.put(mapEntryMsg.getKey(), mapEntryMsg.getValue());
                    } else {
                        props.remove(mapEntryMsg.getKey());
                    }
                } else
                if (mapEntryMsg.getCtxType() == ContextUpdateEntryCommandMessage.SERVICE_CONTEXT) {
                    Map props = updater
                            .getServiceProps(comMsg.getParentId(), comMsg.getContextId());
                    if (mapEntryMsg.getOperation() ==
                        ContextUpdateEntryCommandMessage.ADD_OR_UPDATE_ENTRY) {
                        props.put(mapEntryMsg.getKey(), mapEntryMsg.getValue());
                    } else {
                        props.remove(mapEntryMsg.getKey());
                    }
                }
            } else {
                log.error("TribesClusterManager received an unknown Context Command");
            }
        } else if (msg instanceof ConfigurationCommand) {

            ConfigurationCommand command = (ConfigurationCommand) msg;
            ConfigurationEvent event = new ConfigurationEvent();
            int commandType = command.getCommandType();
            event.setConfigurationType(command.getCommandType());
            switch (commandType) {
                case CommandType.LOAD_SERVICE_GROUPS:
                    event.setServiceGroupNames(command.getServiceGroupNames());
                    configurationManager.notifyListeners(commandType, event);
                    break;
                case CommandType.UNLOAD_SERVICE_GROUPS:
                    event.setServiceGroupNames(command.getServiceGroupNames());
                    configurationManager.notifyListeners(commandType, event);
                    break;
                case CommandType.RELOAD_CONFIGURATION:
                    configurationManager.notifyListeners(commandType, event);
                    break;
                case CommandType.APPLY_POLICY:
                    event.setServiceName(command.getServiceName());
                    event.setPolicy(command.getPolicy());
                    configurationManager.notifyListeners(commandType, event);
                    break;
                case CommandType.PREPARE:
                    configurationManager.notifyListeners(commandType, event);
                    break;
                case CommandType.COMMIT:
                    configurationManager.notifyListeners(commandType, event);
                    break;
                case CommandType.ROLLBACK:
                    configurationManager.notifyListeners(commandType, event);
                    break;
                default:
                    log.error("TribesClusterManager received an unknown Configuration Command");
            }
        }
    }
}
