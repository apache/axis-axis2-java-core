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

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.CommandType;
import org.apache.axis2.cluster.configuration.ConfigurationEvent;
import org.apache.axis2.cluster.context.ContextCommandMessage;
import org.apache.axis2.cluster.tribes.configuration.ConfigurationCommand;
import org.apache.axis2.cluster.tribes.configuration.TribesConfigurationManager;
import org.apache.axis2.cluster.tribes.context.TribesContextManager;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;


public class ChannelListener implements org.apache.catalina.tribes.ChannelListener {

    private TribesContextManager contextManager = null;
    private TribesConfigurationManager configurationManager = null;

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

    public boolean accept(Serializable msg, Member sender) {
        return true;
    }

    public void messageReceived(Serializable msg, Member sender) {
        System.err.println("####### Message received " + msg);
        if (msg instanceof ContextCommandMessage) {
            try {
                ContextCommandMessage comMsg = (ContextCommandMessage) msg;
                contextManager.notifyListeners(comMsg);
            } catch (ClusteringFault e) {
                // TODO: Handle this
                log.error(e);
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
